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
package org.portico2.common.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.messaging.ResponseMessage;
import org.portico2.common.network.configuration.ConnectionConfiguration;
import org.portico2.common.services.federation.msg.RtiProbe;

/**
 * The {@link Connection} class is the main interface for the rest of the Portico framework
 * to the networking stack. Within each instance there is:
 * 
 * <ul>
 *   <li>A {@link ProtocolStack}: Handles the processing of messages as they are sent/received.</li>
 *   <li>An {@link ITransport}: The actual network component that does the raw sending/receiving.</li>
 *   <li>Other misc items (such as a component to correlate requests to resposnes)...</li>
 * </ul>
 * 
 * <b>Sending Messages</b>
 * When a message is given to the connection for sending, it will pass it <i>DOWN</i> the protocol
 * stack, allowing each {@link IProtocol} to process the message. At the conclusion of this process
 * the message is pushed into the {@link ITransport} for sending.
 * <p/>
 * 
 * <b>Receiving Messages</b>
 * When a message is received from the underlying {@link ITransport}, it is passed <i>UP</i> the
 * protocol stack until it is received by the connection. If the message is a control response,
 * the connection will try to link it up with any outstanding request. If it is any other type,
 * the message will be passed to the {@link IApplicationReceiver} for processing by the RTI.
 */
public class Connection
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String name;
	private ConnectionConfiguration configuration;
	private IApplicationReceiver appReceiver;
	private Logger logger;

	protected ITransport transport;
	private ProtocolStack protocolStack;
	private ResponseCorrelator<ResponseMessage> responseCorrelator;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Connection()
	{
		this.name = "unknown";       // set in configure()
		this.configuration = null;   // set in configure()
		this.appReceiver = null;     // set in configure()
		this.logger = null;          // set in configure()

		this.transport = null;       // set in configure()
		this.protocolStack = new ProtocolStack( this );
		this.responseCorrelator = new ResponseCorrelator<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	///////////////////////////////////////////////////////////////////////////////////////
	///  Connection Lifecycle Methods   ///////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Configure the connection using the given configuration data and link it to the provided
	 * receiver. Incoming messages should be routed to the receiver.
	 * 
	 * @param configuration Configuration data that was extracted from the RID
	 * @param receiver The component that should receive incoming messages from the network medium
	 * @throws JConfigurationException Thown if there is an error in the configuration data
	 */
	public void configure( ConnectionConfiguration configuration, IApplicationReceiver appReceiver )
		throws JConfigurationException
	{
		// extract the properties we need to store
		this.name = configuration.getName();
		this.configuration = configuration;
		this.appReceiver = appReceiver;
		//this.logger = appReceiver.getLogger();
		this.logger = LogManager.getFormatterLogger( appReceiver.getLogger().getName()+"."+name );
		
		// create the transport
		this.transport = configuration.getTransportType().newTransport();
		this.transport.configure( configuration, this );
		
		// populate the protocol stack
		this.protocolStack.empty();
		// FIXME do population!!
	}

	/**
	 * This method is called when the connection should establish itself and commence processing.
	 * Prior to {@link #connect()} calls, the connection should be configured but not active.
	 * 
	 * @throws JRTIinternalError If there is a problem encountered during connection
	 */
	public void connect() throws JRTIinternalError
	{
		// log some initialization information
		logger.debug( "Opening connection [%s]", name );
		//logger.debug( BufferInformation );
		//logger.debug( ProtocolStack );
		//logger.debug( "Transport: "+this.transport.getType() );
		
		logger.trace( "Opening transport [%s/%s]", name, this.transport.getType() );
		this.transport.open();
		logger.trace( "Transport is now open [%s/%s]", name, this.transport.getType() );
	}
	
	/**
	 * Called when the owner is shutting down. All active connections should be closed.
	 * 
	 * @throws JRTIinternalError If there is a problem encountered during connection
	 */
	public void disconnect() throws JRTIinternalError
	{
		logger.debug( "Disconnecting..." );

		logger.trace( "Closing transport: %s", this.transport.getType() );
		this.transport.close();
	}

	/**
	 * This method will send an {@link RtiProbe}. If we get a response, we know there
	 * is an RTI out there. If we don't, we know there isn't one.
	 * 
	 * @return <code>true</code> if there is an RTI out there; <code>false</code> otherwise
	 */
	public boolean findRti()
	{
		MessageContext context = new MessageContext( new RtiProbe() );
		sendControlRequest( context );
		return context.isSuccessResponse();
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Message SENDING methods   ////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Data messages are intended to be sent to all federates within a federation.
	 * Although they may be routed through the RTI, they are not a control message.
	 * Data messages do NOT require or expect a response. 
	 * <p/>
	 * Their use is currently limited to attribute reflections and interactions. Although
	 * these messages are only a small subset of all those available, in any given federation
	 * they will represent the _vast_ majority of the volume of messages exchanged and so often
	 * can use a faster network path than control messages.
	 * 
	 * @param message The message to send to all other federates
	 * @throws JException If there is a problem sending the message
	 */
	public void sendDataMessage( PorticoMessage message ) throws JException
	{
		Message outgoing = new Message( message, CallType.DataMessage, 0 );
		protocolStack.down( outgoing );
	}

	/**
	 * Control messages are ones that expect a response. When a component wants to send a
	 * control message it packs it into a {@link MessageContext} and passes it to the connection.
	 * This call is expected to BLOCK while a response is sought. At the conclusion of this
	 * call, a response should be populated in the {@link MessageContext}. 
	 * 
	 * @param context Contains the request. Response will be put in here.
	 * @throws JRTIinternalError If there is a problem processing the request.
	 */
	public void sendControlRequest( MessageContext context ) throws JRTIinternalError
	{
		// if async, just send and mark context a success
		PorticoMessage request = context.getRequest();
		if( request.isAsync() )
		{
			protocolStack.down( new Message(request, CallType.ControlAsync, 0) );
			context.success();
		}
		else
		{
			// Get an ID for the request
			int requestId = responseCorrelator.register();
			
			// Send the message
			protocolStack.down( new Message(request,CallType.ControlSync,requestId) );
			
			// Wait for the response
			ResponseMessage response = responseCorrelator.waitFor( requestId );

			// Package the response
			if( response != null )
				context.setResponse( response );
			else
				context.error( "No response received (request:"+request.getType()+") - RTI/Federates still running?" );
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Message RECEIVING methods   //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * A message has been received (and has come all the way up the protocol stack).
	 * It's time to do something intelligent with it.
	 * 
	 * @param message The message that was received. 
	 */
	protected void receive( Message message )
	{
		Header header = message.getHeader();
		switch( header.getCallType() )
		{
			case DataMessage:
				appReceiver.receiveDataMessage( message.inflateAsPorticoMessage() );
				break;
			case ControlSync:
			case ControlAsync:
				receiveControlRequest( message );
				break;
			case ControlResp:
				responseCorrelator.offer( header.getRequestId(), message.inflateAsResponse() );
				break;
			default: break;
		}
	}
	
	private final void receiveControlRequest( Message message )
	{
		// turn the message into a PorticoMessage
		PorticoMessage request = message.inflateAsPorticoMessage();
		
		// check to make sure we can actually work with this message
		if( appReceiver.isReceivable(message.getHeader()) == false )
			return;

		// fire the message to the receiver to see what happens
		MessageContext context = new MessageContext( request );
		appReceiver.receiveControlRequest( context );
		
		// check to make sure we got a response
		if( context.hasResponse() == false )
		{
			logger.warn( "No response received for Control Request "+request.getType() );
			return;
		}
		
		// if we need to return the response, do so now
		if( request.isAsync() == false )
		{
			// convert the message into a response (from the original request)
			message.deflateAndStoreResponse( context.getResponse() );
			protocolStack.down( message );
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public Logger getLogger()
	{
		return this.logger;
	}

	public ProtocolStack getProtocolStack()
	{
		return this.protocolStack;
	}

	public ResponseCorrelator<ResponseMessage> getResponseCorrelator()
	{
		return this.responseCorrelator;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
