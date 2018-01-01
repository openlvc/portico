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

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Message.Flag;
import org.jgroups.util.DefaultThreadFactory;
import org.portico.bindings.jgroups.Configuration;
import org.portico.bindings.jgroups.Federation;
import org.portico.lrc.compat.JRTIinternalError;

/**
 * This class represents a channel devoted to supporting an active Portico Federation.
 * The purpose of this class is primarily just to handle the JGroups-specific comms. Any
 * broader logic should be handled by the {@link Federation} class in which this sits.
 * 
 * For outgoing messages, we have `sendXxxx` methods. For incoming messages we set up
 * an instance of a {@link ChannelListener} which then routes incoming messages back
 * to the {@link Federation}, once any JGroups stuff has been stripped away.
 */
public class Channel
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
	private Federation federation;
	private String channelName;

	// JGroups connection information
	private boolean connected;
	protected JChannel jchannel;
	private ChannelListener jlistener;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Channel( Federation federation )
	{
		this.logger = LogManager.getFormatterLogger( "portico.lrc.jgroups" );
		this.federation = federation;
		this.channelName = federation.getFederationName();

		// channel details set when we connect
		this.connected = false;
		this.jchannel = null;
		this.jlistener = new ChannelListener( federation );
	}


	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Channel Lifecycle Methods /////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	/** Close off the connection to the existing JGroups channel */
	public void disconnect()
	{
		// send a goodbye notification
		try
		{
			sendAsyncControlMessage( ControlHeader.goodbye(), new byte[]{} );
		}
		catch( Exception e )
		{
			logger.error( "Exception while sending channel goodbye message: "+e.getMessage(), e );
		}

		// Disconnect
		this.jchannel.disconnect();
		this.jchannel.close();
		this.connected = false;
		logger.debug( "Connection closed to channel ["+channelName+"]" );
	}

	/**
	 * Create the underlying JGroups channel and connect to it. If we have WAN support
	 * enabled then we will create a slightly different protocol stack based on the
	 * configuration. Should we be unable to create or connect to the channel, a generic
	 * internal error exception will be thrown.
	 */
	public void connect() throws JRTIinternalError
	{
		// connect to the channel
		if( this.isConnected() )
			return;
		
		try
		{
			logger.trace( "ATTEMPT Connecting to channel ["+channelName+"]" );

			// set the channel up
			this.jchannel = constructChannel();
			this.jchannel.setReceiver( jlistener );

			// connects to the channel and fetches state in single action
			this.jchannel.connect( channelName );
			
			// all done
			this.connected = true;
			logger.debug( "SUCCESS Connected to channel ["+channelName+"]" );
		}
		catch( Exception e )
		{
			logger.error( "ERROR Failed to connect to channel ["+channelName+"]: "+
			              e.getMessage(), e );
			throw new JRTIinternalError( e.getMessage(), e );
		}
	}
	
	/**
	 * This method constructs the channel, including any nitty-gritty details (such as thread pool
	 * details or the like).
	 */
	private JChannel constructChannel() throws Exception
	{
		// create a different channel depending on whether we are trying to use the WAN
		// or local network infrastructure
		JChannel channel = null;
		channel = new JChannel( "etc/jgroups-udp.xml" );

		// if we're not using daemon threads, return without resetting the thread groups
		if( Configuration.useDaemonThreads() == false )
			return channel;

		// we are using daemon threds, so let's set the channel up to do so
		// set the thread factory on the transport
		DefaultThreadFactory factory = new DefaultThreadFactory( "JG", true );
		channel.getProtocolStack().getTransport().setThreadFactory( factory );
		
//		ThreadGroup threadGroup = channel.getProtocolStack().getTransport().getChannelThreadGroup();
//		DefaultThreadFactory factory = new DefaultThreadFactory( threadGroup, "Incoming", true );
//		channel.getProtocolStack().getTransport().setThreadFactory( factory );
//		channel.getProtocolStack().getTransport().setOOBThreadPoolThreadFactory( factory );
//		channel.getProtocolStack().getTransport().setTimerThreadFactory( factory );

		// set the thread pools on the transport
//		ThreadPoolExecutor regular =
//		    (ThreadPoolExecutor)channel.getProtocolStack().getTransport().getDefaultThreadPool();
//		regular.setThreadFactory( new DefaultThreadFactory(threadGroup,"Regular",true) );
//
//		// do the same for the oob pool
//		ThreadPoolExecutor oob =
//		    (ThreadPoolExecutor)channel.getProtocolStack().getTransport().getOOBThreadPool();
//		oob.setThreadFactory( new DefaultThreadFactory(threadGroup,"OOB",true) );

		return channel;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// General Sending Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	public String getChannelName() { return this.channelName; }
	public boolean isConnected() { return this.connected; }
	public Address getChannelAddress() { return this.jchannel.getAddress(); }

	/**
	 * This method will send the provided message to all federates connected to the same JGroups
	 * channel. If there is a problem sending the message, a {@link JRTIinternalError} is thrown.
	 * 
	 * No special flags are set on these messages. They are asynchronous, subject to flow control
	 * and bundling.
	 * 
	 * @param message The message to be sent.
	 * @throws JRTIinternalError If there is a problem serializing or sending the message
	 */
	public void send( byte[] payload ) throws JRTIinternalError
	{
		// send the message
		try
		{
			Message message = new Message( null /*destination*/, payload );
			jchannel.send( message );
		}
		catch( Exception e )
		{
			throw new JRTIinternalError( "Problem sending message: channel="+channelName+
			                             ", error message="+e.getMessage(), e );
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// Federation Lifecycle Methods ////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sends out a "Find Coordinator" control message to try and locate the process that is
	 * acting as the coordinator for this federation/channel. This method just sends the request
	 * and immediately returns. Responses, if there are any, will come in asynchronously.
	 */
	public void sendFindCoordinator() throws Exception
	{
		sendAsyncControlMessage( ControlHeader.findCoordinator(), new byte[]{} );
	}

	/**
	 * Sents the "SetManifest" response to an incoming "FindCoordinator" request. These are
	 * only sent out by channel coordinators.
	 */
	public void sendSetManifest( byte[] payload ) throws Exception
	{
		sendAsyncControlMessage( ControlHeader.setManifest(), payload );
	}

	public void sendCreateFederation( byte[] payload ) throws Exception
	{
		sendSyncControlMessage( ControlHeader.newCreateHeader(), payload );
	}

	public void sendJoinFederation( byte[] payload ) throws Exception
	{
		sendSyncControlMessage( ControlHeader.newJoinHeader(), payload );
	}

	public void sendResignFederation( byte[] payload ) throws Exception
	{
		sendSyncControlMessage( ControlHeader.newResignHeader(), payload );
	}
	
	public void sendDestroyFederation( byte[] payload ) throws Exception
	{
		sendSyncControlMessage( ControlHeader.newDestroyHeader(), payload );
	}
	
	public void sendCrashedFederate( UUID crashed ) throws Exception
	{
		sendAsyncControlMessage( crashed, ControlHeader.goodbye(), new byte[]{} );
	}
	
	/** Sends without the RSVP header and thus will not block waiting for responses */
	private void sendAsyncControlMessage( ControlHeader header, byte[] payload ) throws Exception
	{
		sendAsyncControlMessage( null, header, payload );
	}
	
	/** Sends without the RSVP header and thus will not block waiting for responses */
	private void sendAsyncControlMessage( UUID sender, ControlHeader header, byte[] payload )
		throws Exception
	{
		if( sender == null )
			sender = federation.getLocalUUID();

		Message message = new Message();
		message.putHeader( ControlHeader.HEADER, header );
		message.putHeader( UUIDHeader.HEADER, new UUIDHeader(sender) );
		message.setBuffer( payload );
		message.setFlag( Flag.DONT_BUNDLE );
		message.setFlag( Flag.NO_FC );
//		message.setFlag( Flag.OOB );
		this.jchannel.send( message );
	}
	
	/** Sends with the RSVP header. Will block while all other clients acknowledge */
	private void sendSyncControlMessage( ControlHeader header, byte[] payload ) throws Exception
	{
		sendSyncControlMessage( null, header, payload );
	}

	/** Sends with the RSVP header and lets the user override who the sender is */
	private void sendSyncControlMessage( UUID sender, ControlHeader header, byte[] payload )
		throws Exception
	{
		if( sender == null )
			sender = federation.getLocalUUID();
		
		Message message = new Message();
		message.putHeader( ControlHeader.HEADER, header );
		message.putHeader( UUIDHeader.HEADER, new UUIDHeader(sender) );
		message.setBuffer( payload );
		message.setFlag( Flag.DONT_BUNDLE );
		message.setFlag( Flag.NO_FC );
//		message.setFlag( Flag.OOB );
		message.setFlag( Flag.RSVP );

		// send the message out - because this is marked with the RSVP flag the send
		// call will block until all other channel participants have received the
		// message and acknowledged it
		this.jchannel.send( message );
	}

	/**
	 * This method is used by the `LocalGateway` class to forward messages received from the
	 * WAN to the local cluster.
	 */
	public void forwardToChannel( Message message ) throws Exception
	{
		this.jchannel.send( message );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
