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
package org.portico2.common.network.transport.multicast;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.logging.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.MessageListener;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.Response;
import org.jgroups.util.DefaultThreadFactory;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.StringUtils;
import org.portico2.common.network.Header;

/**
 * This class represents a channel devoted to supporting an active Portico Federation.
 * The purpose of this class is primarily just to handle the JGroups-specific comms.
 */
public class JGroupsChannel
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	static
	{
		// we need this to get around a problem with JGroups and IPv6 on a Linux/Java 5 combo
		System.setProperty( "java.net.preferIPv4Stack", "true" );
		
		// Add some important JGroups headers.
		// We do it here so we can do it in one chunk. Doing them statically in each class
		// (and thus loading once the class is first referenced) seemed to cause issues that
		// resulted in no messages being delivered. Doing them all at once, prior to making
		// or connecting to a channel seems to be OK
		//ClassConfigurator.add( TypeHeader.ID, TypeHeader.class );
		//ClassConfigurator.add( MessageIdHeader.ID, MessageIdHeader.class );
	}
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private JGroupsConfiguration configuration;
	protected Logger logger;
	private String name;

	// JGroups connection information
	private boolean connected;
	private IJGroupsListener appListener; // the listener the app gives us and we pass to
	private ChannelListener  ourListener; // the listener we give JGroups
	protected JChannel jchannel;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create a new channel using the given configuration. The name given will be that used for the
	 * channel (the value inside the configuration will be ignored).
	 * 
	 * @param name   Name of the channel to connect to
	 * @param configuration The configuration information to use
	 * @param appListener   The listener that should be called back when events happen
	 * @param logprefix     Prefix for the logger (so that you can flava is for the local context)
	 */
	public JGroupsChannel( String channelName,
	                       JGroupsConfiguration configuration,
	                       IJGroupsListener appListener )
	{
		this.configuration = configuration;
		this.name = channelName;
		this.logger = appListener.provideLogger();

		// channel details set when we connect
		this.connected = false;
		this.appListener = appListener;
		this.ourListener = new ChannelListener();
		this.jchannel = null;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Channel Lifecycle Methods /////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
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
			logger.trace( "ATTEMPT Connecting to channel ["+name+"]" );

			// activate the configuration (copied things into system properties)
			this.configuration.copyToSystemProperties();
			
			// set the channel up
			this.jchannel = constructChannel();
			this.jchannel.setReceiver( ourListener );
			
			// connects to the channel and fetches state in single action
			this.jchannel.connect( name, null, 2000 );
			
			// all done
			this.connected = true;
			logger.debug( "SUCCESS Connected to channel ["+name+"]" );
		}
		catch( Exception e )
		{
			logger.error( "ERROR Failed to connect to channel ["+name+"]: "+
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
		channel = new JChannel( "etc/jgroups-multicast.xml" );

		// if we're not using daemon threads, return without resetting the thread groups

		if( configuration.useDamonThreads() == false )
			return channel;

		// we are using daemon threds, so let's set the channel up to do so
		// set the thread factory on the transport
		logger.trace( "Creating JGroups stack with daemon threads" );
		DefaultThreadFactory factory = new DefaultThreadFactory( "<"+name+">", true, true );
		channel.getProtocolStack().getTransport().setThreadFactory( factory );
		channel.getProtocolStack().getTransport().setThreadPoolThreadFactory( factory );
		
		// Every channel transport has a timer thread in it. By the time we create the channel
		// it is too late to set the factory for that thread as it is up and running. A bit of
		// a pain if we want to make it use daemon threads. The only option is to create a new
		// timer of the type JGroups wants so we can control the factory it uses from the start.
		
		//
		// NOTE - For some reason, when I make this change group discovery starts to fail (FML).
		//        Need a real solution, but leaving it commented out for now and noting that this
		//        timer thread will keep the whole execution alive.
		//TimeScheduler3 timer = new TimeScheduler3( channel.getProtocolStack().getTransport().getThreadPool(),
		//                                           factory );

		// stop the existing timer so the thread that has already started also stops
		//channel.getProtocolStack().getTransport().getTimer().stop();
		// load in our new timer with the daemon threads
		//channel.getProtocolStack().getTransport().setTimer( timer );
		
		return channel;
	}

	/** Close off the connection to the existing JGroups channel */
	public void disconnect()
	{
		this.jchannel.disconnect();
		this.jchannel.close();
		this.connected = false;
		logger.trace( "Connection closed to channel ["+name+"]" );
	}

	
	//////////////////////////////////////////////////////////////////////////////////////
	///  General Message Sending Methods    //////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	public final void send( byte[] payload )
	{
		if( !connected )
			throw new JRTIinternalError( "Channel [%s] is not connected yet", name );

		Message message = new Message( null, payload );
		
		try
		{
			this.jchannel.send( message );
		}
		catch( Exception e )
		{
			Header header = new Header( payload, 0 );
			throw new JRTIinternalError( "Problem sending message (%s): channel=%s, error=%s",
			                             header.getMessageType(),
			                             name,
			                             e.getMessage(), e );
		}

		// Log the outgoing message
		if( logger.isTraceEnabled() )
			logMessage( "outgoing", message );
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	///  Incoming Message Handling Methods    ////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The {@link ChannelListener} has received a message and we now need to process it
	 * @param message The message that was received
	 */
	private final void receivedMessage( Message message ) throws JRTIinternalError
	{
		// Step 1. Discard our own messages
		if( message.getSrc().equals(jchannel.getAddress()) )
			return;

		// Step 2. Do some specialized logging
		if( logger.isTraceEnabled() )
			logMessage( "incoming", message );
		
		// Step 3. Handle the message up the chain
		appListener.receive( this, message.getRawBuffer() );
	}

	/////////////////////////////////////////////////
	///  Logging Helpers   //////////////////////////
	/////////////////////////////////////////////////
	private final void logMessage( String direction, Message message )
	{
		Header header = new Header( message.getRawBuffer(), 0 );
		logger.trace( "(%s) type=%s (id=%d), ptype=%s, from=%s, to=%s, size=%s, app=%s",
		              direction,
		              header.getCallType(),
		              header.getRequestId(),
		              header.getMessageType(),
		              StringUtils.sourceHandleToString( header.getSourceFederate() ),
		              StringUtils.targetHandleToString( header.getTargetFederate() ),
		              message.getLength(),
		              message.getSrc() );
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators    ///////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	public String getChannelName()     { return this.name; }
	public boolean isConnected()       { return this.connected; }
	public Address getChannelAddress() { return this.jchannel.getAddress(); }


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	//////////////////////////////////////////////////////////////////////////////////////////
	/// Private Inner Class: ChannelListener  ////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	private class ChannelListener implements MessageListener,
	                                         MembershipListener,
	                                         Receiver,
	                                         RequestHandler
	{
		////////////////////////////////////////////////////////////////////////////
		//////////////////////// MembershipListener Methods ////////////////////////
		////////////////////////////////////////////////////////////////////////////
		/** No-op */ @Override public void block() {}
		/** No-op */ @Override public void unblock() {}

		/** Watch changing views to see if anyone crashes */
		@Override
		public void viewAccepted( View newView )
		{
			logger.trace( "View accepted. New view: "+newView );
		}

		/**
		 * A hint from JGroups that this federate may have gone AWOL.
		 */
		@Override
		public void suspect( Address suspectedDropout )
		{
			logger.trace( "Suspect: "+suspectedDropout );
		}

		////////////////////////////////////////////////////////////////////////////
		////////////////////////// RequestHandler Methods //////////////////////////
		////////////////////////////////////////////////////////////////////////////
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
				logger.trace( "(incoming) synchronous, channel="+name+", size="+
				              message.getLength()+", source="+message.getSrc() );
			}

			throw new JRTIinternalError( "handle(Message) was called but is not supported" );
		}

		///////////////////////////////////////////////////////////////////////////
		///////////////////////// MessageListener Methods /////////////////////////
		///////////////////////////////////////////////////////////////////////////
		/** No-op. */ public void getState( OutputStream stream ) {}
		/** No-op. */ public void setState( InputStream stream ) {}

		@Override
		public void handle( Message message, Response response )
		{
			throw new JRTIinternalError( "handle(Message,Response) was called but is not supported" );
		}
		
		/**
		 * Asynchronous message received.
		 */
		@Override
		public void receive( Message message )
		{
			try
			{
    			// hand the message off for processing
    			receivedMessage( message );
			}
			catch( Exception e )
			{
				logger.warn( "Exception while processing received message", e );
				logger.warn( "Message: src=%s, length=%d, headers=%s",
				             message.getSrc(),
				             message.getLength(),
				             message.getHeaders() );
			}
		}
	}
	
}
