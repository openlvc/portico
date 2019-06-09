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

import org.portico2.common.messaging.MessageType;
import org.portico2.common.network.Connection;
import org.portico2.common.network.Header;
import org.portico2.common.network.Message;
import org.portico2.common.network.ProtocolStack;
import org.portico2.common.network.configuration.protocol.ProtocolConfiguration;
import org.portico2.common.network.protocol.Protocol;
import org.portico2.forwarder.firewall.Firewall;
import org.portico2.forwarder.tracking.StateTracker;

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
public class ForwardingProtocol extends Protocol
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final EnumSet ControlRequestPassthrough  = EnumSet.of( MessageType.CreateFederation,
	                                                            MessageType.JoinFederation,
	                                                            MessageType.RegisterObject,
	                                                            MessageType.DeleteObject );
	
	private static final EnumSet NotificationPassthrough = EnumSet.of( MessageType.DiscoverObject,
	                                                            MessageType.DeleteObject );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Direction side;
	private Direction directionOfTravel; // messages only flow up() through the stack, thus the
	                                     // actual direction of travel is the reverse of the side
	                                     // of the forwarder that a connection is on
	private Exchanger exchanger;

	private ProtocolStack targetStack;   // where we want to dump messages
	private StateTracker stateTracker;
	private Firewall firewall;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ForwardingProtocol( Direction direction, Exchanger exchanger )
	{
		this.side = direction;
		this.directionOfTravel = side.reverse();
		this.exchanger = exchanger;

		this.targetStack = null;    // set in open()
		this.stateTracker = null;   // set in open()
		this.firewall = null;       // set in open()
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Management   ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void doConfigure( ProtocolConfiguration configuration, Connection hostConnection )
	{
	}

	@Override
	public void open()
	{
		ForwardingProtocol sibling = side == Direction.Upstream ? exchanger.downstreamForwarder :
		                                                          exchanger.upstreamForwarder; 
		
		this.targetStack = sibling.hostConnection.getProtocolStack();
		
		this.stateTracker = exchanger.stateTracker;
		this.firewall = exchanger.firewall;
	}

	@Override
	public void close()
	{
		// no-op
	}


	////////////////////////////////////////////////////////////////////////////////////////
	///  Message Passing   /////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public final void down( Message message )
	{
		// No-op for us. Just pass on to the next.
		passDown( message );
	}

	@Override
	public final void up( Message message )
	{
		// short-circuit the whole thing and just hand the
		// message directly across to the other side
		if( firewall.isEnabled() == false )
		{
			targetStack.down( message );
			return;
		}

		// different course of action depending on the call type
		switch( message.getCallType() )
		{
			case DataMessage:
			{
				// Only data messages that pass our filtering rules can get through.
				// Check the rulez and link it through to its siblings if it is cool.

				// Check the rulez!
				Header header = message.getHeader();
				if( firewall.acceptUpdate(directionOfTravel,
				                          header.isFilteringObjectClass(),
				                          header.getFederation(),
				                          header.getFilteringId()) )
				{
					targetStack.down( message );
				}
				
				break;
			}
			
			case ControlRequest:
			{
				// We _must_ forward all control messages to the other side; no questions.
				// However, there is a small subset we want to snoop on, so check for that.

				// Do we care about this type?
				if( ControlRequestPassthrough.contains(message.getHeader().getMessageType()) )
					stateTracker.receiveControlRequest( message );
				
				// Pass to the other side
				targetStack.down( message );
				break;
			}
			
			case Notification:
			{
				// We _must_ forward all control messages to the other side; no questions.
				// However, there is a small subset we want to snoop on, so check for that.

				// Do we care about this type?
				if( NotificationPassthrough.contains(message.getHeader().getMessageType()) )
					stateTracker.receiveNotification( message );
				
				targetStack.down( message );
				break;
			}

			case ControlResponseOK:
			case ControlResponseErr:
			{
				// We only track a control response if we took a look at the original
				// request and decided that we needed to see the response
				if( stateTracker.isResponseWanted(message.getRequestId()) )
					stateTracker.receiveControlResponse( message );
				
				targetStack.down( message );
				break;
			}

			default:
				break; // Nope. Don't know what it is. YOU. SHALL NOT. PASSSS!
		}
		
		// Never pass messages up the stack any further. We don't do local processing.
		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getName()
	{
		return "Forwarding ("+side+")";
	}


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
