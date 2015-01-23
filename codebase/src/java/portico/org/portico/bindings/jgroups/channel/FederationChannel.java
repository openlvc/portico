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

import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Message.Flag;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.util.DefaultThreadFactory;
import org.jgroups.util.Util;
import org.portico.bindings.jgroups.Auditor;
import org.portico.bindings.jgroups.JGroupsProperties;
import org.portico.bindings.jgroups.MessageReceiver;
import org.portico.lrc.LRC;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JFederateAlreadyExecutionMember;
import org.portico.lrc.compat.JFederatesCurrentlyJoined;
import org.portico.lrc.compat.JFederationExecutionAlreadyExists;
import org.portico.lrc.compat.JFederationExecutionDoesNotExist;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.utils.MessageHelpers;
import org.portico.utils.logging.Log4jConfigurator;
import org.portico.utils.messaging.PorticoMessage;

/**
 * This class represents a channel devoted to supporting an active Portico Federation.
 * Convenience methods are provided here to manage the joining of the federation and the
 * sending of asynchronous and synchronous messages.
 */
public class FederationChannel
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
	protected Logger logger;
	protected String federationName;
	
	// write metadata about incoming/outgoing message flow
	private Auditor auditor;

	// JGroups connection information
	protected boolean connected;
	protected JChannel jchannel;
	private MessageDispatcher jdispatcher;
	private FederationListener jlistener;
	
	protected MessageReceiver receiver;

	// federation and shared state
	protected FederationManifest manifest;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FederationChannel( String federationName )
	{
		this.federationName = federationName;
		this.logger = Logger.getLogger( "portico.lrc.jgroups" );
		
		// set the appropriate level for the jgroups logger, by default we'll just turn it
		// off because it is quite noisy. that said, we will allow for it to be turned back
		// on via a configuration property
		String jglevel = System.getProperty( JGroupsProperties.PROP_JGROUPS_LOGLEVEL, "OFF" );
		Log4jConfigurator.setLevel( jglevel, "org.jgroups" );
		
		// create this, but leave as disabled for now - gets turned on in joinFederation
		this.auditor = new Auditor();
		
		// channel details set when we connect
		this.connected = false;
		this.jchannel = null;
		this.jdispatcher = null;
		this.jlistener = new FederationListener( this );
		
		this.receiver = new MessageReceiver( this.auditor );

		// the manifest is created in the viewAccepted method which is called
		// as soon as we connect to a channel
		this.manifest = null; 
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Accessors and Mutators /////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	public boolean isConnected()
	{
		return this.connected;
	}

	public FederationManifest getManifest()
	{
		return this.manifest;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Connection Management //////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will connect us to the federation channel. It is important to note that
	 * connecting to a channel does not mean we are part of a federation. When we connect,
	 * we are put in a kind of "lurker" state. We have the manifest, but there might not be an
	 * active federation associated with it, and even if there is, we haven't joined that
	 * federation yet. The {@link #createFederation(ObjectModel)} and {@link #joinFederation(String)}
	 * calls are what put an active federation in a channel and allow us to participate in it
	 * respectively.
	 * 
	 * @throws JRTIinternalError If there is a problem connecting to or fetching state from
	 *                           the federation channel.
	 */
	public void connect() throws JRTIinternalError
	{
		if( this.isConnected() )
			return;
		
		try
		{
			logger.trace( "ATTEMPT Connecting to channel ["+federationName+"]" );
			
    		// set the channel up
    		this.jchannel = constructChannel();
    		this.jdispatcher = new MessageDispatcher( jchannel, jlistener, jlistener, jlistener );
    		
    		// connects to the channel and fetches state in single action
    		//  *null indicates we get state from the coordinator (rather than a specific member)
    		//  *the timeout is how long we want to wait for the state
    		//  *the true will trigger jgroups to use a FLUSH on our join
    		this.jchannel.connect( federationName, null, JGroupsProperties.getJoinTimeout(), true );
    		
    		// make sure we connected successfully
    		if( this.isConnected() == false )
    			throw new JRTIinternalError( "Connection to channel failed, consult log for error" );
    		
    		logger.debug( "SUCCESS Connected to channel ["+federationName+"]" );
		}
		catch( Exception e )
		{
			logger.error( "ERROR Failed to connect to channel ["+federationName+"]: "+
			              e.getMessage(), e );
			throw new JRTIinternalError( e.getMessage(), e );
		}
	}

	/**
	 * Close the channel off and set out connected status to false.
	 */
	public void disconnect()
	{
		this.jchannel.disconnect();
		this.jchannel.close();
		this.connected = false;
		logger.debug( "Connection closed to channel ["+federationName+"]" );
	}

	/**
	 * This method constructs the channel, including any nitty-gritty details (such as
	 * thread pool details or the like).
	 */
	private JChannel constructChannel() throws Exception
	{
		JChannel channel = new JChannel( "etc/jgroups-udp.xml" );
		
		// if we're not using daemon threads, return without resetting the thread groups
		if( JGroupsProperties.useDaemonThreads() == false )
			return channel;

		// we are using daemon threds, so let's set the channel up to do so
		// set the thread factory on the transport
		ThreadGroup threadGroup = channel.getProtocolStack().getTransport().getChannelThreadGroup();
		DefaultThreadFactory factory = new DefaultThreadFactory( threadGroup, "Incoming", true );
		channel.getProtocolStack().getTransport().setThreadFactory( factory );
		channel.getProtocolStack().getTransport().setOOBThreadPoolThreadFactory( factory );
		channel.getProtocolStack().getTransport().setTimerThreadFactory( factory );

		// set the thread pools on the transport
		ThreadPoolExecutor regular = 
			(ThreadPoolExecutor)channel.getProtocolStack().getTransport().getDefaultThreadPool();
		regular.setThreadFactory( new DefaultThreadFactory(threadGroup,"Regular",true) );

		// do the same for the oob pool
		ThreadPoolExecutor oob = 
			(ThreadPoolExecutor)channel.getProtocolStack().getTransport().getOOBThreadPool();
		oob.setThreadFactory( new DefaultThreadFactory(threadGroup,"OOB",true) );

		return channel;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Message Sending Methods /////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will send the provided message to all federates in the federation. If there
	 * is a problem serializing or sending the message, a {@link JRTIinternalError} is thrown.
	 * <p/>
	 * No special flags are set on these messages. They are asynchronous, subject to flow control
	 * and bundling.
	 * 
	 * @param payload The message to be sent.
	 * @throws JRTIinternalError If there is a problem serializing or sending the message
	 */
	public void send( PorticoMessage payload ) throws JRTIinternalError
	{
		// turn the packet into a message
		byte[] data = MessageHelpers.deflate( payload );
		Message message = new Message( null /*destination*/, null /*source*/, data );
		message.setBuffer( data );

		// Log an audit message for the send
		if( auditor.isRecording() )
			auditor.sent( payload, data.length );
		
		// write the message
		if( logger.isTraceEnabled() )
		{
			logger.trace( "(outgoing) payload="+payload.getClass().getSimpleName()+", size="+
			              data.length+", channel="+federationName );
		}
		
		try
		{
			jchannel.send( message );
		}
		catch( Exception e )
		{
			throw new JRTIinternalError( "Problem sending message: channel="+federationName+
			                             ", error message="+e.getMessage(), e );
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Federation Management //////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Just because a JGroups channel exists doesn't mean that a federation has been created
	 * inside it. To actually create a federation we have to issue a create order and provide
	 * the base object model. We first check to make sure there isn't already an active
	 * federation and if there isn't, we tell all the other channel members, blocking until
	 * they acknowledge receipt.
	 *   
	 * @param fom The object model for the federation
	 * @throws Exception If we don't get an acknowledgement from all other channel members in
	 *                   time of there is already an active federation in place.
	 */
	public void createFederation( ObjectModel fom ) throws Exception
	{
		logger.debug( "REQUEST createFederation: name=" + federationName );

		// make sure we're not already connected to an active federation
		if( manifest.isCreated() )
		{
			logger.error( "FAILURE createFederation: already exists, name="+federationName );
			throw new JFederationExecutionAlreadyExists( "federation exists: "+federationName );
		}
		
		// send out create federation call and get an ack from everyone
		Message message = new Message();
		message.putHeader( ControlHeader.HEADER, ControlHeader.newCreateHeader() );
		message.setBuffer( Util.objectToByteBuffer(fom) );
		message.setFlag( Flag.DONT_BUNDLE );
		message.setFlag( Flag.NO_FC );
		message.setFlag( Flag.OOB );
		message.setFlag( Flag.RSVP );

		// send the message out - because this is marked with the RSVP flag the send
		// call will block until all other channel participants have received the
		// message and acknowledged it
		this.jchannel.send( message );

		logger.info( "SUCCESS createFederation: name=" + federationName );
	}

	/**
	 * Attempt to join the active fedeation in the channel. We check that there is an active
	 * federation to join and we're not already joined to it (nor is someone with our name).
	 * 
	 * @param federateName The name we want to join with
	 * @param lrc The LRC we want messages to flow into on successful join
	 * @throws Exception If there is no federation to join, we're already joined or someone
	 *                   else is already joined with out name (unless the RID file has been
	 *                   configured to not require unique names).
	 */
	public String joinFederation( String federateName, LRC lrc ) throws Exception
	{
		logger.debug( "REQUEST joinFederation: federate="+federateName+", federation="+federationName );

		// check to see if there is an active federation
		if( manifest.isCreated() == false )
		{
			logger.info( "FAILURE joinFederation: federation doesn't exist, name="+federationName );
			throw new JFederationExecutionDoesNotExist( "federation doesn't exist: "+federationName );
		}

		// check to see if there is a federate with our name already present
		// check to see if someone already exists with the same name
		// If we have been configured to allow non-unique names, check, but don't error
		// on failure, rather, augment the name from "federateName" to "federateName (handle)"
		if( manifest.containsFederate(federateName) )
		{
			if( PorticoConstants.isUniqueFederateNamesRequired() )
			{
				logger.info( "FAILURE joinFederation: federate="+federateName+", federation="+
				             federationName+": Federate name in use" );
				throw new JFederateAlreadyExecutionMember( "federate name in use: "+federateName );
			}
			else
			{
				federateName += " ("+manifest.getLocalFederateHandle()+")";
				logger.warn( "WARNING joinFederation: name in use, changed to "+federateName );
			}
		}

		// Enable the auditor if we are configured to use it
		if( JGroupsProperties.isAuditorEnabled() )
			this.auditor.startAuditing( federationName, federateName, lrc );
		
		// link up the message receiver to the LRC we're joined to so that
		// messages can start flowing right away
		this.receiver.linkToLRC( lrc );
	
		// send the notification to all other members and get an ack from everyone
		Message message = new Message();
		message.putHeader( ControlHeader.HEADER, ControlHeader.newJoinHeader() );
		message.setBuffer( federateName.getBytes() );
		message.setFlag( Flag.DONT_BUNDLE );
		message.setFlag( Flag.NO_FC );
		message.setFlag( Flag.OOB );
		message.setFlag( Flag.RSVP );

		// send the message out, blocks until all acknowledge its receipt
		this.jchannel.send( message );
		
		logger.info( "SUCCESS Joined federation with name="+federateName );
		return federateName;
	}

	/**
	 * Sends the resignation notification out with the appropriate header so that it is
	 * detected by other channel members, allowing them to update their manifest appropriately.
	 * If there is a problem sending the messaage, an exception is thrown.
	 */
	public void resignFederation( PorticoMessage resignMessage ) throws Exception
	{
		// get the federate name before we send and cause it to be removed from the manifest
		String federateName = this.manifest.getLocalFederateName();
		logger.debug( "REQUEST resignFederation: federate="+federateName+
		              ", federation="+federationName );
		
		// send the notification out to all receivers and wait for acknowledgement from each
		Message message = new Message();
		message.putHeader( ControlHeader.HEADER, ControlHeader.newResignHeader() );
		message.setBuffer( MessageHelpers.deflate(resignMessage) );
		message.setFlag( Flag.DONT_BUNDLE );
		message.setFlag( Flag.NO_FC );
		message.setFlag( Flag.OOB );
		message.setFlag( Flag.RSVP );
		this.jchannel.send( message );
		
		// all done, disconnect our incoming receiver
		// we received the message sent above as well, so the receiver will update the
		// manifest as it updates it for any federate resignation
		logger.info( "SUCCESS Federate ["+federateName+"] resigned from ["+federationName+"]" );
		this.receiver.unlink();
		this.auditor.stopAuditing();
	}

	/**
	 * Validates that the federation is in a destroyable state (there is a federatoin active
	 * but there are no joined federates) and then sends the destroy request to all other
	 * connected members, blocking until they acknowledge it.
	 * 
	 * @throws Exception If there is no federation active or there are federates still joined to
	 *                   it, or if there is a problem flushing the channel before we send.
	 */
	public void destroyFederation() throws Exception
	{
		logger.debug( "REQUEST destroyFederation: name=" + federationName );
		
		// check to make sure there is a federation to destroy
		if( manifest.isCreated() == false )
		{
			logger.error( "FAILURE destoryFederation ["+federationName+"]: doesn't exist" );
			throw new JFederationExecutionDoesNotExist( "doesn't exist: "+federationName );
		}

		// check to make sure there are no federates still joined
		if( manifest.getFederateHandles().size() > 0 )
		{
			String stillJoined = manifest.getFederateHandles().toString();
			logger.info( "FAILURE destroyFederation ["+federationName+"]: federates still joined "+
			             stillJoined );
			throw new JFederatesCurrentlyJoined( "federates still joined: "+stillJoined );
		}

		// send the notification out to all receivers and wait for acknowledgement from each
		Message message = new Message();
		message.putHeader( ControlHeader.HEADER, ControlHeader.newDestroyHeader() );
		message.setBuffer( federationName.getBytes() );
		message.setFlag( Flag.DONT_BUNDLE );
		message.setFlag( Flag.NO_FC );
		message.setFlag( Flag.OOB );
		message.setFlag( Flag.RSVP );
		this.jchannel.send( message );
		
		// we don't need to update the manifest directly here, that will be done by
		// out message listener, which will have received the message we sent above

		logger.info( "SUCCESS destroyFederation: name=" + federationName );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
