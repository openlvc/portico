/*
 *   Copyright 2015 The Portico Project
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
package org.portico.bindings.jgroups.channel;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.Message.Flag;
import org.jgroups.MessageListener;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.Response;
import org.portico.bindings.jgroups.Federation;

/**
 * This class implements the various JGroups listener interface methods that allow it to
 * receive notifications from a JGroups channel. Incoming messages are assessed as being
 * either part of Portico group management, or general messages for processing. Either way,
 * they are routed to the appropriate methods on the linked {@link Federation}.
 * 
 * If WAN mode is enabled, messages received from the local cluster will also be forwarded
 * to the `LocalGateway` class to send out to the broader network. WAN mode is enabled/disabled
 * from within the RID file and only a single federate should have this enabled per network.
 */
public class ChannelListener implements MessageListener, MembershipListener, Receiver,
                                        /*Still use this without MessageDispatcher?*/ RequestHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;
	private String channelName;
	private Federation federation;
	
	// Set of addresses we have received a Suspect message for
	// Use to determine whether an incoming View update is a result
	// of a removal following a failure
	private Map<Address,UUID> allSeenMembers;  // map of all channel members
	private Set<Address> lastView;             // set of all federates in last view update

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected ChannelListener( Federation federation )
	{
		this.federation = federation;
		this.logger = federation.getLogger();
		this.channelName = federation.getFederationName();

		this.allSeenMembers = new HashMap<>();
		this.lastView = new HashSet<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// MembershipListener Methods ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////
	/** No-op */ @Override public void block() {}
	/** No-op */ @Override public void unblock() {}

	/** Watch changing views to see if anyone crashes */
	@Override
	public void viewAccepted( View newView )
	{
		// check to see if anyone left the federation since the last update we received
		List<Address> newMembers = newView.getMembers();
		for( Address oldMember : lastView )
		{
			if( newMembers.contains(oldMember) == false )
				federation.receiveCrashed( allSeenMembers.get(oldMember) );
		}

		lastView.clear();
		lastView.addAll( newMembers );
	}

	/**
	 * A hint from JGroups that this federate may have gone AWOL.
	 */
	@Override
	public void suspect( Address suspectedDropout )
	{
		UUID uuid = this.allSeenMembers.get( suspectedDropout );
		String federateName = federation.getManifest().getFederateName( uuid );
		logger.warn( "Detected that federate ["+federateName+"] may have crashed, investigating..." );
	}

	/////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// RequestHandler Methods ///////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Synchronous message received.
	 * 
	 * This method is called by JGroups when we have been provided with a synchronous message
	 * against which we are expected to supply a response. We hand off to the federation for
	 * processing.
	 */
	@Override
	public Object handle( Message message )
	{
		// log that we have an incoming message
		if( logger.isTraceEnabled() )
		{
			logger.trace( "(incoming) synchronous, channel="+channelName+", size="+
			              message.getLength()+", source="+message.getSrc() );
		}

		return federation.receiveSynchronous( message.getBuffer() );
	}

	/////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// MessageListener Methods //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////
	/** No-op. */ public void getState( OutputStream stream ) {}
	/** No-op. */ public void setState( InputStream stream ) {}

	@Override
	public void handle( Message message, Response response )
	{
		System.out.println( "ERROR! ERROR! SHOULD NEVER HAPPEN!! ERROR! ERROR!" );
	}
	
	/**
	 * Asynchronous message received.
	 */
	@Override
	public void receive( Message message )
	{
		// log that we have an incoming message
		if( logger.isTraceEnabled() )
		{
			logger.trace( "(incoming) asynchronous, channel="+channelName+", size="+
			              message.getLength()+", source="+message.getSrc() );
		}

		ControlHeader header = (ControlHeader)message.getHeader( ControlHeader.HEADER );
		if( header == null )
		{
			// just a regular message, hand it off to our receiver
			federation.receiveAsynchronous( message.getBuffer() );
		}
		else
		{
			// this is a Control Message - pull the sender UUID out
			UUID sender = ((UUIDHeader)message.getHeader(UUIDHeader.HEADER)).getUUID();
			
			switch( header.getMessageType() )
			{
				case ControlHeader.FIND_COORDINATOR:
					logger.debug( "(GMS) findCoordinator("+message.getSrc()+")" );
					federation.receiveFindCoordinator( sender, message.getBuffer() );
					break;
				case ControlHeader.SET_MANIFEST:
					logger.debug( "(GMS) setManifest("+message.getSrc()+")" );
					federation.receiveSetManifest( sender, message.getBuffer() );
					break;
				case ControlHeader.CREATE_FEDERATION:
					logger.debug( "(GMS) createFederation("+message.getSrc()+")" );
					federation.receiveCreateFederation( sender, message.getBuffer() );
					break;
				case ControlHeader.JOIN_FEDERATION:
					logger.debug( "(GMS) joinFederation("+message.getSrc()+")" );
					federation.receiveJoinFederation( sender, message.getBuffer() );
					break;
				case ControlHeader.RESIGN_FEDERATION:
					logger.debug( "(GMS) resignFederation("+message.getSrc()+")" );
					federation.receiveResignFederation( sender, message.getBuffer() );
					break;
				case ControlHeader.DESTROY_FEDERATION:
					logger.debug( "(GMS) destroyFederation("+message.getSrc()+")" );
					federation.receiveDestroyFederation( sender, message.getBuffer() );
					break;
				case ControlHeader.GOODBYE:
					logger.debug( "(GMS) goodbye("+message.getSrc()+")" );
					federation.receiveGoodbye( sender, message.getBuffer() );
					break;
				default:
					logger.warn( "Unknown control message [type="+header.getMessageType()+"]. Ignore." );
			}
			
			// store the UUID against the address so we can watch to see if it disappears later
			allSeenMembers.put( message.getSrc(), sender );
		}
		
		//
		// WAN Forwarding - Forward on to the WAN if appropriate
		//
		if( federation.isWanEnabled() && message.isFlagSet(Flag.NO_RELAY) == false )
			federation.getGateway().forwardToGateway( header, message );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
