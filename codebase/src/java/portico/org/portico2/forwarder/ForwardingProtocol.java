/*
 *   Copyright 2018 The Portico Project
 *
 *   This file is part of portico.
 *
 *   portico is free software; you can redistribute it and/or modify
 *   it under the terms of the Common Developer and Distribution License (CDDL) 
 *   as published by Sun Microsystems. For more information see the LICENSE file.
 *   
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *   (that goes for your lawyer as well)
 *
 */
package org.portico2.forwarder;

import org.apache.logging.log4j.Logger;
import org.portico2.common.network.Connection;
import org.portico2.common.network.IProtocol;
import org.portico2.common.network.Message;
import org.portico2.common.network.ProtocolStack;

/**
 * A Connection interfaces with outside application components via "full fat" objects. These are
 * PorticoMessage objects and ResponseObjects. To send these on the network, they have to be turned
 * to/from byte[]'s, and that process can be quite expensive.
 * <p/>
 * 
 * The Forwarder really just looks at the message headers in the byte[]'s and doesn't need (for the
 * most part) the full-fat objects. However, because the forwarder wraps two connections, and the
 * pathway in/out of the connections is as full-fat objects, we incur the cost of this. For example,
 * when a message is recieved from downstream, the byte[] has to be turned into a PorticoMessage, handed
 * to the upstream connection and then immediately "deflated" again into a byte[]. Quite wasteful.
 * <p/>
 * 
 * {@link IProtocol} implementations sit inside a connection and process a message potentially before
 * this conversion has taken place. Implementations of this class are created in pairs (one for upstream,
 * one for downstream) and their job is to intercept messages before the costly seiralization takes place
 * and forward the message over to their sibling in its byte[] form, thus saving the inflation and
 * subsequent deflation cost.
 * <p/>
 * 
 * The {@link Exchanger} creates one of these for each of the upstream/downstream connections, links
 * them (via a reference to the exchanger itself) and then inserts them into the connection after it
 * has been created, thus giving us a fastpath!
 */
public class ForwardingProtocol implements IProtocol
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Direction direction;
	private Exchanger exchanger;

	private Connection hostConnection;	
	private Logger logger;
	private ProtocolStack targetStack; // where we want to dump messages

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ForwardingProtocol( Direction direction, Exchanger exchanger )
	{
		this.direction = direction;
		this.exchanger = exchanger;

		this.hostConnection = null; // set in open()
		this.logger = null;         // set in open()
		this.targetStack = null;    // set in open()
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Management   ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void configure( Connection hostConnection )
	{
		this.hostConnection = hostConnection;
		this.logger = hostConnection.getLogger();
	}

	public void open()
	{
		ForwardingProtocol sibling = direction == Direction.Upstream ? exchanger.downstreamForwarder :
		                                                               exchanger.upstreamForwarder; 
		
		this.targetStack = sibling.hostConnection.getProtocolStack();
	}

	public void close()
	{
		
	}


	////////////////////////////////////////////////////////////////////////////////////////
	///  Message Passing   /////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public boolean down( Message message )
	{
		// No-op for us. We only intercept messages coming in and forward them over so that
		// they can go out the other side.
		return true;
	}

	public boolean up( Message message )
	{
		// Apply firewall rules
		
		// Let through special cases that we may need to watch
		
		// Divert all other messages across to the other half
		logger.fatal( "%s Handing message off to target", direction.flowDirection() );
		targetStack.down( message );
		return true;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public String getName()
	{
		return "Forwarding ("+direction+")";
	}


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
