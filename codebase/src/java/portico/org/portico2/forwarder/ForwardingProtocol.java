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

import java.util.EnumSet;

import org.apache.logging.log4j.Logger;
import org.portico2.common.messaging.MessageType;
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
	private static final EnumSet PassthroughTypes = EnumSet.of( MessageType.CreateFederation,
	                                                            MessageType.JoinFederation );

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
		// no-op
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
		switch( message.getCallType() )
		{
			case DataMessage:
			{
				// Only data messages that pass our filtering rules can get through.
				// Check the rulez and link it through to its siblings if it is cool.

				// TODO check the rulez!
				targetStack.down( message );
				break;
			}
			
			case ControlSync:
			{
				// We _must_ forward all control messages to the other side; no questions.
				// However, there is a small subset we want to snoop on, so check for that.

				// Do we care about this type?
				if( PassthroughTypes.contains(message.getHeader().getMessageType()) )
					exchanger.stateTracker.receiveControlRequest( message );
				
				// Pass to the other side
				targetStack.down( message );
				break;
			}
			
			case ControlAsync:
			{
				// We just forward async/from RTI control messages through.
				// Nothing to track for now.
				targetStack.down( message );
				break;
			}

			case ControlResp:
			{
				// We only track a control response if we took a look at the original
				// request and decided that we needed to see the response
				if( exchanger.stateTracker.isResponseWanted(message.getRequestId()) )
					exchanger.stateTracker.receiveControlResponse( message );
				
				targetStack.down( message );
				break;
			}

			default:
				break; // Nope. Don't know what it is. YOU. SHALL NOT. PASSSS!
		}
		
		// Never pass messages up the stack any further. We don't do local processing.
		return false;
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
