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
package org.portico2.common.network.multicast.jgroups;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.Message.Flag;
import org.jgroups.MessageListener;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.Response;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.util.DefaultThreadFactory;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.utils.MessageHelpers;
import org.portico.utils.StringUtils;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.PorticoConstants;
import org.portico2.common.network2.CallType;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.messaging.ResponseMessage;

/**
 * This class represents a channel devoted to supporting an active Portico Federation.
 * The purpose of this class is primarily just to handle the JGroups-specific comms. Any
 * broader logic should be handled by the {@link Federation} class in which this sits.
 * 
 * For outgoing messages, we have `sendXxxx` methods. For incoming messages we set up
 * an instance of a {@link ChannelListener} which then routes incoming messages back to
 * an application-specific listener via an {@link IJGroupsListener} interface provided when
 * the channel is created.
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
		ClassConfigurator.add( TypeHeader.ID, TypeHeader.class );
		ClassConfigurator.add( MessageIdHeader.ID, MessageIdHeader.class );
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
	
	private long    responseTimeout;      // how long to wait for responses
	private int     responseId;           // the ID of the message we are waiting for a response on
	private Message responseMessage;      // the response message object we are waiting for
	private Lock    responseLock;         // lock to avoid race conditions on incoming responses
	private Condition responseCondition;  // what we use to signal when message has arrived
	private Random  random;
	
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

		this.responseTimeout   = configuration.getGmsJoinTimeout();
		this.responseId        = PorticoConstants.NULL_HANDLE;
		this.responseMessage   = null; // set by ChannelListener inner class
		this.responseLock      = new ReentrantLock();
		this.responseCondition = this.responseLock.newCondition();
		this.random            = new Random();
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
			this.jchannel.connect( name, null, responseTimeout );
			
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
	///  Federation Lifecycle Methods   //////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Send a given data message to the federation. Notionally this will be broadcast out to all
	 * participants for them to decide whether they are interested or not. However, if there are
	 * Forwarders or network boundaries in the way, they may be filtered out by the infrastructure.
	 * 
	 * @param porticoMessage The message to send
	 * @throws JRTIinternalError If we are not connected or there is an I/O problem while sending
	 */
	public final void sendDataMessage( PorticoMessage porticoMessage ) throws JRTIinternalError
	{
		byte[] payload = MessageHelpers.deflate2( porticoMessage, CallType.DataMessage );
		Message message = new Message( null /*target*/, payload );
		message.putHeader( TypeHeader.ID, TypeHeader.dataMessage() );
		send( message, porticoMessage );
	}

	/**
	 * Sends the given message as a control request. If the request is NOT from the RTI then
	 * if <b>will block</b> until either a response is returned or timeout has happened. If
	 * the message is from the RTI (as determined by {@link PorticoMessage#isFromRti()}) then
	 * this method will return automatically.
	 * <p/>
	 * This avoids some deadlock situations as the LRC waits for the RTI to acknowledge a message
	 * it sent while the RTI waits for the LRC to also acknowledge a request it sent during the
	 * processing of the LRC's request. 
	 * 
	 * @param request The request to serialize and send
	 * @return The received response
	 * @throws JRTIinternalError If there is no response returned in time 
	 */
	public final void sendControlRequest( MessageContext context ) throws JRTIinternalError
	{
		PorticoMessage request = context.getRequest();
		
		// Serialize the message
		CallType calltype = request.isAsync() ? CallType.ControlAsync : CallType.ControlSync;
		byte[] payload = MessageHelpers.deflate2( request, calltype );
		Message message = new Message( null, payload );

		// Insert a header describing the type of message
		TypeHeader typeHeader = request.isFromRti() ? TypeHeader.controlRequestAsync() :
		                                              TypeHeader.controlRequest();
		message.putHeader( typeHeader.getMagicId(), typeHeader );

		if( request.isAsync() )
		{
			// Send ASYNCHRONOUSLY
			send( message, request );
			context.success();
		}
		else
		{
			// Send SYNCHRONOUSLY
			Message returned = sendAndWait( message, request );
			if( returned == null )
			{
				context.error( new JRTIinternalError("No response received (request:%s) - RTI/Federates still running?",
				                                     request.getType()) );
				return;
			}
			
			ResponseMessage response = MessageHelpers.inflate2( returned.getRawBuffer(), ResponseMessage.class );
			context.setResponse( response );
		}
	}
	
	/**
	 * Send a control <b>response</b> message. The first paramter is the ID of the request that
	 * we are responding to. This will populate the sent message with the appropriate headers to
	 * identify the message as a response with the given ID.
	 * 
	 * @param requestId The ID of the request we are responding to.
	 * @param payload The payload of the message we are to send.
	 */
	public final void sendControlResponse( int requestId, byte[] payload )
	{
		Message message = new Message( null, payload );
		message.putHeader( TypeHeader.ID, TypeHeader.controlResponse() );
		message.putHeader( MessageIdHeader.ID, new MessageIdHeader(requestId) );
		message.setFlag( Flag.DONT_BUNDLE );
		message.setFlag( Flag.NO_FC );
		send( message, null );
	}

	//////////////////////////////////////////////////////////////////////////////////////
	///  General Message Sending Methods    //////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Send the given message to the JGroups channel exactly as it is.
	 * 
	 * @param message The message to send
	 * @param portico The {@link PorticoMessage} (so that we can do some better logging)
	 */
	private final void send( Message message, PorticoMessage portico )
	{
		checkConnected();
		
		try
		{
			this.jchannel.send( message );
		}
		catch( Exception e )
		{
			throw new JRTIinternalError( "Problem sending message (%s): channel=%s, error=%s",
			                             message.getHeader(TypeHeader.ID),
			                             name,
			                             e.getMessage(), e );
		}

		//
		// Log the outgoing message - So ugly
		//
		if( logger.isTraceEnabled() )
		{
			//logger.trace( "(outgoing) channel=%s, size=%d, source=%s, headers=%s",
			//              name, message.getLength(), message.getSrc(), message.getHeaders() );

			TypeHeader typeHeader = message.getHeader( TypeHeader.ID );
			MessageIdHeader idHeader = message.getHeader( MessageIdHeader.ID );
			String id = "none";
			if( idHeader != null )
				id = idHeader.toString();

			if( portico == null )
			{
				// must be a response message as we don't have the original request
				// can't log much more, just the basics
				logger.trace( "(outgoing) type=%s (%s), size=%d, app=%s",
				              typeHeader,
				              id,
				              message.getLength(),
				              message.getSrc() );
			}
			else
			{
				// we have the PorticoMessage original, let's log some stuff!
				logger.trace( "(outgoing) type=%s (%s), ptype=%s, from=%s, to=%s, size=%d, app=%s",
				              typeHeader,
				              id,
				              portico.getType(),
				              StringUtils.sourceHandleToString(portico),
				              StringUtils.targetHandleToString(portico),
				              message.getLength(),
				              message.getSrc() );
			}
		}
	}

	/**
	 * Send the given message to the channel and then wait for a response.
	 * <p/>
	 * If we get a response, that is returned. If we timeout waiting, null is returned.
	 * <p/>
	 * We also provide the unserialized request message so that we can do some better logging.
	 *
	 * @param message    The JGroups message to send
	 * @param portico    The original request (for better logging)
	 * @return           The response we received, or null if none was received
	 */
	private final Message sendAndWait( Message message, PorticoMessage portico )
	{
		// Timeout we will wait for responses.
		long timeout = responseTimeout;

		// Add in a request ID header so that we can track responses against it
		message.setFlag( Flag.DONT_BUNDLE );
		message.setFlag( Flag.NO_FC );
		MessageIdHeader idHeader = new MessageIdHeader();
		message.putHeader( MessageIdHeader.ID, idHeader );
		
		// Store the request ID for later use and then send the message.
		// Make sure we have exclusive access to the request correlator so that
		// a fast response combined with unlucky thread scheduling on our end
		// don't conspire to receive the response before we start waiting for
		// the signal
		responseLock.lock();
		try
		{
			// check to make sure we are not already waiting!
			if( this.responseId != PorticoConstants.NULL_HANDLE )
				throw new JRTIinternalError( "There is already an outstanding request in place (id=%d)", responseId );

			// store the ID we are looking for
			this.responseId = idHeader.getId();
			this.responseMessage = null;
			
			// send the message
			send( message, portico );
			
			// wait for a response (use local timeout value)
			this.responseCondition.await( timeout, TimeUnit.MILLISECONDS );
			
			// clear the waiting ID and return whatever we have (null or not)
			Message response = this.responseMessage;
			this.responseMessage = null;
			this.responseId = PorticoConstants.NULL_HANDLE;
			return response;
		}
		catch( InterruptedException ie )
		{
			// we were interrupted waiting for the condition to signal, so we must be shutting down
			// no-op for us, just return null
			return null;
		}
		finally
		{
			responseLock.unlock();
		}
	}

	private final void checkConnected() throws JRTIinternalError
	{
		if( !connected )
			throw new JRTIinternalError( "Channel [%s] is not connected yet", name );
	}

	//////////////////////////////////////////////////////////////////////////////////////
	///  Incoming Message Handling Methods    ////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The {@link ChannelListener} has received a message and we now need to process it
	 * @param message The message that was received
	 */
	private void receivedMessage( Message message ) throws JRTIinternalError
	{
		// Step 1. Discard our own messages
		if( message.getSrc().equals(jchannel.getAddress()) )
			return;

		// Step 2. Determine the type of message
		// 	       If there is no type header, discard the message
		TypeHeader type = message.getHeader( TypeHeader.ID );
		if( type == null )
		{
			logger.warn( "Received message without type header, discard. (Src=%s, Headers=%s)",
			             message.getSrc(),
			             message.getHeaders() );
			return;
		}

		// Step 3. Do some specialized logging
		short typeid = type.getMessageType();
		if( logger.isTraceEnabled() )
		{
			switch( typeid )
			{
				case TypeHeader.DATA_MESSAGE:
				case TypeHeader.CONTROL_RESP:
					logBasicMessage( message );
					break;
				case TypeHeader.CONTROL_REQ_SYNC:
				case TypeHeader.CONTROL_REQ_ASYNC:
					logControlRequest( message );
					break;
			}
		}
		
		// Step 4. Decide how to handle the message
		switch( typeid )
		{
			case TypeHeader.DATA_MESSAGE:
				appListener.receiveDataMessage( this, message );
				break;
			case TypeHeader.CONTROL_REQ_SYNC:
				appListener.receiveControlRequest( this, MessageIdHeader.getMessageId(message), message );
				break;
			case TypeHeader.CONTROL_REQ_ASYNC:
				appListener.receiveControlRequest( this, PorticoConstants.NULL_HANDLE, message );
				break;
			case TypeHeader.CONTROL_RESP:
				receivedControlResponse( message );
				break;
			default:
				logger.warn( "Unknown jgroups message in TypeHeader; id= "+typeid );
				break;
		}
	}

	//
	// Handle incoming response messages, correlating them to outstanding requests.
	//
	private final void receivedControlResponse( Message message )
	{
		// get the uuid header
		MessageIdHeader idHeader = message.getHeader( MessageIdHeader.ID );
		if( idHeader == null )
		{
			logger.warn( "Control response without Message ID header, discarding" );
			return;
		}

		int id = idHeader.getId();
		this.responseLock.lock();
		try
		{
			// Ignore if we are not waiting for this ID
			if( this.responseId != id )
				return;

			// Make sure we're not about to overwrite an unprocessed response.
			// This should not happen, but it can't hurt to drop a warning to alert to bugs
			// introduced thanks to future changes
			if( this.responseMessage != null )
				logger.warn( "Received a control response while we still have one pending" );
			
			// We're clear, store the response and signal anyone waiting
			this.responseMessage = message;
			this.responseCondition.signalAll();
		}
		finally
		{
			this.responseLock.unlock();
		}
	}

	/////////////////////////////////////////////////
	///  Logging Helpers   //////////////////////////
	/////////////////////////////////////////////////
	private final void logBasicMessage( Message message )
	{
		logger.trace( "(incoming) type=%s (id=%s), size=%d, app=%s",
		              message.getHeader(TypeHeader.ID),
		              message.getHeader(MessageIdHeader.ID),
		              message.getLength(),
		              message.getSrc() );
	}

	private final void logControlRequest( Message message )
	{
		TypeHeader typeHeader = message.getHeader( TypeHeader.ID );
		MessageIdHeader idHeader = message.getHeader( MessageIdHeader.ID );
		String id = "none";
		if( idHeader != null )
			id = idHeader.toString();

		PorticoMessage portico = MessageHelpers.inflate2( message.getRawBuffer(), PorticoMessage.class );
		
		logger.trace( "(incoming) type=%s (id=%s), ptype=%s, from=%s, to=%s, size=%d, app=%s",
		              typeHeader,
		              id,
		              portico.getType(),
		              StringUtils.sourceHandleToString(portico),
		              StringUtils.targetHandleToString(portico),
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
