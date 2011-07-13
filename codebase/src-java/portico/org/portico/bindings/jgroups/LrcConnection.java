/*
 *   Copyright 2009 The Portico Project
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
package org.portico.bindings.jgroups;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jgroups.Message;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;
import org.portico.bindings.ConnectedRoster;
import org.portico.bindings.IConnection;
import org.portico.lrc.LRC;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JFederateAlreadyExecutionMember;
import org.portico.lrc.compat.JFederateNotExecutionMember;
import org.portico.lrc.compat.JFederatesCurrentlyJoined;
import org.portico.lrc.compat.JFederationExecutionAlreadyExists;
import org.portico.lrc.compat.JFederationExecutionDoesNotExist;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.services.federation.msg.CreateFederation;
import org.portico.lrc.services.federation.msg.DestroyFederation;
import org.portico.lrc.services.federation.msg.JoinFederation;
import org.portico.lrc.services.federation.msg.ResignFederation;
import org.portico.lrc.utils.MessageHelpers;
import org.portico.utils.logging.Log4jConfigurator;
import org.portico.utils.messaging.PorticoMessage;

/**
 * Default JGroups {@link IConnection} implementation intended for use by LRCs.
 */
public class LrcConnection implements IConnection
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	static
	{
		// we need this to get around a problem with JGroups and IPv6 on a Linux/Java 5 combo
		System.setProperty( "java.net.preferIPv4Stack", "true" );
	}

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean running;
	protected LRC lrc;
	protected Logger logger;
	private Map<String,ChannelWrapper> activeChannels;
	private ChannelWrapper currentChannel;
	private int federateHandle;
	

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public LrcConnection()
	{
		this.running = false;
		this.lrc = null;
		this.logger = null;
		this.activeChannels = new HashMap<String,ChannelWrapper>();
		this.currentChannel = null;
		this.federateHandle = PorticoConstants.NULL_HANDLE;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// lifecycle methods //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	public void configure( LRC lrc, Map<String,Object> properties )
		throws JConfigurationException
	{
		this.lrc = lrc;
		this.logger = Logger.getLogger( "portico.lrc.jgroups" );
		// set the appropriate level for the jgroups logger, by default we'll just turn it
		// off because it is quite noisy. that said, we will allow for it to be turned back
		// on via a configuration property
		String jglevel = System.getProperty( JGProperties.PROP_JGROUPS_LOGLEVEL, "OFF" );
		Log4jConfigurator.setLevel( jglevel, "org.jgroups" );
	}

	/**
	 * This will not actually connect to any channel, it will just mark the connection as "running".
	 * Channels are only connected to when they are needed.
	 */
	public synchronized void connect() throws JRTIinternalError
	{
		if( this.running )
			return;

		logger.info( "Starting JGroups LrcConnection..." );
		this.running = true;
	}
	
	/**
	 * If the connection is currently running, this method will from any channels the connection
	 * is currently attached to.
	 */
	public synchronized void disconnect() throws JRTIinternalError
	{
		// only try and shutdown if we are running
		if( this.running == false )
		{
			logger.debug( "No need to stop connection...already stopped" );
			return;
		}
		
		logger.info( "SHUTDOWN JGroups LrcConnection shutting down" );

		// disconnect from all the active channels
		for( String channelName : activeChannels.keySet() )
		{
			logger.debug( "diconnecting from channel: name=" + channelName );
			activeChannels.get(channelName).shutdown();
		}

		// close off local state
		this.running = false;
		//this.logger = null;
		this.activeChannels.clear();
		this.currentChannel = null;
		this.federateHandle = PorticoConstants.NULL_HANDLE;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// bootstrapping methods ////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	public synchronized void createFederation( CreateFederation request ) throws Exception
	{
		String federationName = request.getFederationName();
		logger.trace( "REQUEST createFederation: name=" + federationName );
		
		// if we haven't got a channel wrapper already active, create a new one
		ChannelWrapper wrapper = activeChannels.get( federationName );
		if( wrapper == null )
			wrapper = new ChannelWrapper( federationName, ChannelWrapper.DEV_NULL, logger );
		activeChannels.put( federationName, wrapper );

		// see if the Roster already has a FOM, if an  ObjectModel already exists, this
		// indicates that the federation already exists
		if( wrapper.getRoster().getObjectModel() != null )
		{
			logger.info( "FAILURE createFederation: already exists, name="+federationName );
			throw new JFederationExecutionAlreadyExists( "federation exists: " + federationName );
		}

		// if we are the controller, store the model on our roster so that new channel
		// connections get it when they join
		if( wrapper.getRoster().isController() )
			wrapper.getRoster().setObjectModel( request.getModel() );

		// tell everyone that is in the channel or has joined since this started
		// about the federation about the new object model
		byte[] bytes = Util.objectToByteBuffer( request.getModel() );
		castAndWaitForAll( wrapper, bytes, JGProperties.MSG_CREATE );
		logger.info( "SUCCESS createFederation: name=" + federationName );
		
		// IMPLEMENTATION NOTE: We maintain all channel connections until this Portico connection
		//                      is told to shutdown (stored in activeChannels). Someone needs to
		//                      exist inside the channel to maintain the state (complete with the
		//                      ObjectModel) or else the federation ceases to exist. We keep the
		//                      connection open for this purpose. The channel will be reused in
		//                      "join" calls if required. If the Portico connection shuts down
		//                      before any other federates join a federation, the federate ceases
		//                      to exist.
	}
	
	public synchronized void destroyFederation( DestroyFederation request ) throws Exception
	{
		// pass the request to the system group and wait for a response
		String federationName = request.getFederationName();
		logger.trace( "REQUEST destroyFederation: name=" + federationName );
		
		// if we haven't got a channel wrapper already active, create a new one
		ChannelWrapper wrapper = activeChannels.get( federationName );
		if( wrapper == null )
			wrapper = new ChannelWrapper( federationName, ChannelWrapper.DEV_NULL, logger );
		activeChannels.put( federationName, wrapper );
		
		// check to see if there is a federation in the channel
		//if( wrapper.getRoster().getObjectModel() == null )
		//{
		//	logger.info( "FAILURE destroyFederation ["+federationName+"]: does not exist" );
		//	throw new JFederationExecutionDoesNotExist( "federation: " + federationName );
		//}

		// check to see if there are any federates in the federation
		if( wrapper.getRoster().getFederates().size() > 0 )
		{
			logger.info( "FAILURE destroyFederation ["+federationName+"]: federates still joined "+
			             wrapper.getRoster().getFederates() );
			throw new JFederatesCurrentlyJoined( "federates still joined: "+
			                                     wrapper.getRoster().getFederates() );
		}
		
		// send the notification, this should cause us to remove the object model
		byte[] bytes = federationName.getBytes();
		castAndWaitForAll( wrapper, bytes, JGProperties.MSG_DESTROY );
		logger.info( "SUCCESS destroyFederation: name=" + federationName );
	}
	
	/**
	 * Handles the process of joining a federate to a federation. Once the method has joined the
	 * appropriate channel (the name of the federation is used), this method will check to see if
	 * the {@link Roster} the channel has contains an ObjectModel. If it does, it means a federation
	 * has been created and can be joined, if it doesn't, no actual federation exists yet (even
	 * through the channel may have many members).
	 * <p/>
	 * Once we know there is a federation to join, the method will issue a special request to all
	 * the other jgroups bindings saying it wants to join. This will cause them to allocate the
	 * next available handle to the incoming federate. This method will block until it has an
	 * acknowledgement from all active members of the channel.
	 */
	public synchronized ConnectedRoster joinFederation( JoinFederation request ) throws Exception
	{
		String federateName = request.getFederateName();
		String federationName = request.getFederationName();
		logger.trace( "REQUEST joinFederation: federate="+federateName+", federation="+federationName );
		
		ChannelWrapper wrapper = activeChannels.get( federationName );
		if( wrapper == null )
			wrapper = new ChannelWrapper( federationName, ChannelWrapper.DEV_NULL, logger );
		activeChannels.put( federationName, wrapper );
		
		// check to see if there is an object model, otherwise there is no federation to join!
		if( wrapper.getRoster().getObjectModel() == null )
		{
			logger.info( "FAILURE joinFederation: federation doesn't exist, name="+federationName );
			throw new JFederationExecutionDoesNotExist( "name=" + federationName );
		}

		// check to see if someone already exists with the same name
		// IF we have been configured NOT to check this, still check, but don't error on failure,
		// rather, augment the name from "federateName" to "federateName (handle)"
		if( wrapper.getRoster().containsFederate(federateName) )
		{
			if( PorticoConstants.isUniqueFederateNamesRequired() )
			{
				logger.info( "FAILURE joinFederate: federate="+federateName+
				             ", federation="+federationName+": Federate name in use" );
				throw new JFederateAlreadyExecutionMember( "federate name in use: "+federateName );
			}
			else
			{
				String newFederateName = federateName+" ("+wrapper.getRoster().getLocalId()+")";
				logger.warn( "WARNING joinFederate: federate="+federateName+
				             ", federation="+federationName+": Federate name in use, changing to "+
				             newFederateName );
				federateName = newFederateName;
				request.setFederateName( newFederateName );
			}
		}
		
		// install a new receiver that will route incoming messages to the kernel now that
		// we're joining to a particular federation on this channel
		KernelRoutingJGReceiver receiver = new KernelRoutingJGReceiver( lrc );
		wrapper.setReceiver( receiver );
		
		// send out the join notification with our handle
		// NOTE: we will receive this as well, so the updating of the Roster will take place then
		castAndWaitForAll( wrapper, federateName.getBytes(), JGProperties.MSG_JOINED );
		
		// update local state
		this.currentChannel = wrapper;
		this.federateHandle = wrapper.getRoster().getLocalId();
		
		// generate the connection roster and return it
		ConnectedRoster roster = new JGroupsRoster( federateHandle,
		                                            wrapper.getRoster().getFederateHandles(),
		                                            wrapper.getRoster().getObjectModel() );
		return roster;
	}
	
	public synchronized void resignFederation( ResignFederation request ) throws Exception
	{
		String federate = request.getFederateName();
		String federation = request.getFederationName();
		logger.trace( "REQUEST resignFederation: federate="+federate+", federation="+federation );
		
		// make sure we're joined to the federation
		ChannelWrapper wrapper = activeChannels.get( federation );
		if( wrapper == null || !wrapper.getRoster().containsFederate(federate) )
		{
			throw new JFederateNotExecutionMember( "Federate ["+federate+
			                                       "] not joined to ["+federation+"]" );
		}
		
		// send out the resign notification
		// NOTE: we will receive this as well, so the updating of the Roster will take place then
		// FIX: PORT-859: see bugnote for full details as to why the message is sent in place
		// of the federate name
		castAndWaitForAll( wrapper, MessageHelpers.deflate(request), JGProperties.MSG_RESIGNED );
		//castAndWaitForAll( wrapper, federate.getBytes(), JGProperties.MSG_RESIGNED );

		// redirect all incoming messages back to the null receiver now that we're
		// no longer joined to the federation on this channel
		wrapper.setReceiver( ChannelWrapper.DEV_NULL );
		this.currentChannel = null;
		this.federateHandle = PorticoConstants.NULL_HANDLE;
	}
	
	/**
	 * This method returns an array of all the known active federations.
	 */
	public synchronized String[] listActiveFederations() throws Exception
	{
		return new String[]{ "not supported" };
	}
	
	private RspList castAndWaitForAll( ChannelWrapper channel, byte[] data, byte messageType )
	{
		Message message = new Message( null, channel.getChannel().getLocalAddress(), data );
		message.setFlag( Message.OOB );
		message.setFlag( Message.HIGH_PRIO );
		message.setFlag( messageType );
		
		if( logger.isTraceEnabled() )
		{
			String messageTypeString = "UNKNOWN";
			if( messageType == JGProperties.MSG_CREATE )
				messageTypeString = "MSG_CREATE";
			else if( messageType == JGProperties.MSG_DESTROY )
				messageTypeString = "MSG_DESTROY";
			else if( messageType == JGProperties.MSG_JOINED )
				messageTypeString = "MSG_JOINED";
			else if( messageType == JGProperties.MSG_RESIGNED )
				messageTypeString = "MSG_RESIGNED";
			
			logger.trace( "(outgoing) {OOB|"+messageTypeString+"} channel="+
			              channel.getChannel().getClusterName()+", size="+data.length );
		}
		
		return channel.getDispatcher().castMessage( null, message, GroupRequest.GET_ALL, 0 );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Federation Group Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Runs a simple check to make sure this connection is connected to a FEDERATION. If it isn't
	 * an exception is thrown, if it is, the method will happily return.
	 */
	private void validateConnected() throws JFederateNotExecutionMember
	{
		if( federateHandle == PorticoConstants.NULL_HANDLE || currentChannel == null )
			throw new JFederateNotExecutionMember( "No connection to federation channel" );
	}

	/**
	 * Sends the given message to the {@link ChannelWrapper} associated with the connection.
	 */
	public void broadcast( PorticoMessage message ) 
		throws JRTIinternalError, JFederateNotExecutionMember
	{
		validateConnected();
		currentChannel.writeAsync( message );
	}

	/**
	 * Sends the given message to the {@link ChannelWrapper} associated with the connection.
	 * This method will then sleep for a period of time defined in
	 * {@link JGProperties#RESPONSE_TIMEOUT} (in millis)
	 */
	public void broadcastAndSleep( PorticoMessage message ) 
		throws JRTIinternalError, JFederateNotExecutionMember
	{
		validateConnected();
		currentChannel.writeAsync( message );
		PorticoConstants.sleep( JGProperties.RESPONSE_TIMEOUT );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
