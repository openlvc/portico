/*
 *   Copyright 2012 The Portico Project
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
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.MessageListener;
import org.jgroups.View;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.util.Util;
import org.portico.lrc.compat.JResignAction;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.services.federation.msg.ResignFederation;
import org.portico.lrc.utils.MessageHelpers;

/**
 * This class implements the various JGroups listener interfaces that allow it to receive
 * notifications from a channel when channel membership changes, state is required or messages
 * are ready for processing.
 * <p/>
 * Instances of this class should be contained inside a {@link FederationChannel}. Incoming
 * message are handed off to the message receiver that sits inside the FederationChannel (which
 * in turn routes the messages to the appropriate LRC or /dev/null if we're not connected).
 */
public class FederationListener implements RequestHandler, MessageListener, MembershipListener
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;
	private String federationName;
	private FederationChannel channel;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected FederationListener( FederationChannel federationChannel )
	{
		this.channel = federationChannel;
		this.logger = federationChannel.logger;
		this.federationName = channel.federationName;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Membership Handling Methods ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * When joining a channel, this method is called before setState().
	 * <p/>
	 * This method is called when the JGroups channel membership changes. The new View 
	 * contains the new memberhsip set for the channel. This can be triggered by a member
	 * coming into or leaving the channel.
	 * <p/>
	 * Within each channel there are two types of members: basic channel members that have
	 * conencted to the JGroups channel, and joined federates, that have connected AND joined
	 * the federation (via a join request).
	 * <p/>
	 * When a federate resigns from a federation, it does so with a resign request. This
	 * notification removes removes the application from the set of federates, even though
	 * it is still connected to the channel. The request is passed through to the core
	 * Portico infrastructure to allow the local federate to do any cleanup operations.
	 * <p/>
	 * When a basic channel member leaves, there is no need for special action. The channel
	 * member was not a joined federate and not part of the federation, so there is nothing
	 * to clean up. When a joined federate crashes or leaves the channel without sending a
	 * resign, the other federates are exposed to potential issues as they expect the federate
	 * that disappeared to still be there.
	 * <p/>
	 * As part of the process of installing a new view, this method will check to see if any
	 * *joined federates* are no longer present in the new view. If they are not, this means
	 * they've left without sending a resign, so we have to synthesize a resign request for
	 * them and pass this back into the local federate so that it can clean up appropriately.
	 * 
	 * @param newView The new channel membership
	 */
	public void viewAccepted( View newView )
	{
		// when we first join a channel, our manifest will be null
		if( channel.manifest == null )
		{
			channel.manifest = new FederationManifest( federationName,
			                                           channel.jchannel.getAddress(),
			                                           newView );
			
			channel.connected = true;
		}

		// update the manifest with membership information from the new view
		Map<Integer,String> disappeared = channel.manifest.updateMembership( newView );

		// loop through each of the federates that disappeared and fake up a resign
		// action to give to the local LRC
		for( Integer federateHandle : disappeared.keySet() )
		{
			String federateName = disappeared.get( federateHandle );
			
			// synthesize a resign notification
			ResignFederation resign =
				new ResignFederation( JResignAction.DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES );
			resign.setSourceFederate( federateHandle );
			resign.setFederateName( federateName );
			resign.setFederationName( federationName );
			resign.setImmediateProcessingFlag( true );
			
			Message resignMessage = new Message( channel.jchannel.getAddress(),
			                                     channel.jchannel.getAddress(),
			                                     MessageHelpers.deflate(resign) );
			
			logger.info( "Federate ["+federateName+","+federateHandle+
			             "] disconnected, synthesizing resign message" );
		}
	}

	/**
	 * Called when a new application has joined the channel and they need a
	 * copy of the {@link FederationManifest} that has the channel details.
	 */
	public void getState( OutputStream stream )
	{
		try
		{
			// write the manifest down the pipe
			stream.write( Util.objectToByteBuffer(channel.manifest) );
		}
		catch( Exception e )
		{
			logger.fatal( "Could not provide manifest to cluster when requested: "+
			              e.getMessage(), e );
		}
	}

	/**
	 * Called when we have joined an existing channel and it has some state to share
	 * with us. This state should contain a {@link FederationManifest} instance. If
	 * we can extract the manifest successfully, set our "connected" property to true.
	 */
	public void setState( InputStream stream )
	{
		try
		{
			// if there is data available it should be the federation manifest
			// replace ours with the one provided
			if( stream.available() == 0 )
			{
				logger.fatal( "Channel contains no FederationManifest, unable to join." );
			}
			else
			{
				byte[] buffer = new byte[stream.available()];
				// because we're using STATE_TRANSFER, this should read all in one go
				int readCount = stream.read( buffer );
				channel.manifest = (FederationManifest)Util.objectFromByteBuffer( buffer );
				channel.manifest.setLocalAddress( channel.jchannel.getAddress() );
				channel.connected = true;
			}
		}
		catch( Exception e )
		{
			logger.fatal( "Couldn't deserialize FederationManifest received from cluster "+
			              "coordinator. Unable to join federation channel properly.", e );
		}
	}

	/**
	 * A hint from JGroups that this federate may have gone AWOL.
	 */
	public void suspect( Address suspectedDropout )
	{
		// just log for information
		channel.manifest.getFederateName( suspectedDropout );
		logger.warn( "Detected that federate ["+1+"] may have crashed, investigating..." );
	}

	/**
	 * The block/unblock messages are called when a FLUSH is invoked. A FLUSH will prevent
	 * channel members from sending new messages until all the existing messages that they
	 * have sent have been delivered to all participants. Typically this is done when a new
	 * connection to the channel is made, to ensure that all exiting messages are sent to
	 * the same set of group members that were present when the message was sent (not the
	 * updated set modified by the joining of a new member).
	 * <p/>
	 * The implementation of the FLUSH protocol will block when a flush is invoked, so
	 * we don't have to do anything special. This is a no-op for us. 
	 */
	public void block()
	{
		// ignore
	}

	/**
	 * @see #block()
	 */
	public void unblock()
	{
		// ignore
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Message Handling Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Asynchronous message received
	 * <p/>
	 * Called when JGroups has received an asynchronous method for us to process.
	 */
	public void receive( Message message )
	{
		// log that we have an incoming message
		if( logger.isTraceEnabled() )
		{
			logger.trace( "(incoming) asynchronous, channel="+federationName+", size="+
			              message.getLength()+", source="+message.getSrc() );
		}

		ControlHeader header = getControlHeader( message );
		if( header == null )
		{
			// just a regular message, hand it off to our receiver
			channel.receiver.receiveAsynchronous( message );
		}
		else
		{
			// a special federation control message, process appropriately
			switch( header.getMessageType() )
			{
				case ControlHeader.CREATE_FEDERATION:
					incomingCreateFederation( message );
					break;
				case ControlHeader.JOIN_FEDERATION:
					incomingJoinFederation( message );
					break;
				case ControlHeader.RESIGN_FEDERATION:
					incomingResignFederation( message );
					break;
				case ControlHeader.DESTROY_FEDERATION:
					incomingDestroyFederation( message );
					break;
				default:
					logger.error( "Unknown control message [type="+header.getMessageType()+"]. Ignoring." );
			}
		}
	}

	/**
	 * Synchronous message received
	 * <p/>
	 * This method is called by JGroups when we have been provided with a synchronous message
	 * against which we are expected to supply a response. We pretty much just hand it off to
	 * the channel's MessageReceiver.
	 */
	public Object handle( Message message )
	{
		// log that we have an incoming message
		if( logger.isTraceEnabled() )
		{
			logger.trace( "(incoming) synchronous, channel="+federationName+", size="+
			              message.getLength()+", source="+message.getSrc() );
		}

		return channel.receiver.receiveSynchronous( message );
	}

	////////////////////////////////////////////////////////////
	///////////////// Message Handling Helpers /////////////////
	////////////////////////////////////////////////////////////
	private ControlHeader getControlHeader( Message message )
	{
		return (ControlHeader)message.getHeader( ControlHeader.HEADER );
	}

	/**
	 * This method is invoked when the channel has received a federation creation message.
	 * It goes through the process of storing the object model inside the manifest and marking
	 * the federation as created.
	 */
	private void incomingCreateFederation( Message message )
	{
		byte[] buffer = message.getBuffer();
		logger.debug( "Received federation creation notification: federation="+federationName+
		              ", fomSize="+buffer.length+"b, source=" + message.getSrc() );
		
		try
		{
			// turn the buffer into a FOM and store it on the federation manifest
			channel.manifest.setFom( (ObjectModel)Util.objectFromByteBuffer(buffer) );
			channel.manifest.setCreated( true );
			logger.info( "Federation ["+federationName+"] has been created" );
		}
		catch( Exception e )
		{
			logger.fatal( "Error installing FOM for federation ["+federationName+"]: "+
			              "this federate will not be able to join the federation", e );
		}
	}

	/**
	 * This method handles join notifications from external federates. We record the join
	 * information inside the manifest and log any errors we encounter. As we're just handling
	 * remote notifications, there is little we can do if something is wrong except log it.
	 */
	private void incomingJoinFederation( Message message )
	{
		String federateName = new String( message.getBuffer() );
		logger.debug( "Received federate join notification: federate=" +federateName+
			              ", federation="+federationName+", source=" + message.getSrc() );
		
		String error = channel.manifest.federateJoined( message.getSrc(), federateName );
		if( error != null )
			logger.error( error );
		else
			logger.info( "Federate ["+federateName+"] joined federation ["+federationName+"]" );
	}

	/**
	 * This method is called when a federate is resigning from a federation. We catch the message
	 * and hand it off to the receiver to process, and then we go through the steps of removing
	 * the connection as a joined federate inside the manifest. The connection is still a member
	 * of the channel, just no longer a joined federate.
	 */
	private void incomingResignFederation( Message message )
	{
		// queue the message for processing
		channel.receiver.receiveAsynchronous( message );

		// get the federate name
		String federateName = channel.manifest.getFederateName( message.getSrc() );
		
		// record the resignation in the roster
		logger.trace( "Received federate resign notification: federate="+federateName+
			          ", federation="+federationName+", source="+message.getSrc() );
			
		String error = channel.manifest.federateResigned( message.getSrc() );
		if( error != null )
			logger.error( error );
		else
			logger.info( "Federate ["+federateName+"] has resigned from ["+federationName+"]" );
	}

	/**
	 * Received a remote federation destroy notification. Check to make sure we're in a state
	 * where we can do this (we have an active federation but it doesn't contain any joined
	 * federates). If we are not in a suitable state we log an error and ignore the request,
	 * otherwise we remove the FOM associated with the channel and flick the created status
	 * to false.
	 */
	private void incomingDestroyFederation( Message message )
	{
		String federationName = new String( message.getBuffer() );
		Address address = message.getSrc();

		logger.trace( "Received federate destroy notification: federation="+federationName+
		              ", source=" + address );
		
		if( channel.manifest.isCreated() == false )
		{
			logger.error( "Connection ["+address+"] apparently destroyed federation ["+
			              federationName+"], but we didn't know it existed, ignoring..." );
		}
		else if( channel.manifest.getFederateHandles().size() > 0 )
		{
			logger.error( "Connection ["+address+"] apparnetly destoryed federation ["+
			              federationName+"], but we still have active federates, ignoring... " );
		}
		else
		{
			channel.manifest.setCreated( false );
			channel.manifest.setFom( null );
			logger.info( "Federation ["+federationName+"] has been destroyed" );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
