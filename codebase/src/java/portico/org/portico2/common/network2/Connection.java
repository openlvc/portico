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
package org.portico2.common.network2;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.configuration.connection.ConnectionConfiguration;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.messaging.ResponseMessage;

public class Connection
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private IApplicationReceiver appReceiver;
	
	private Logger logger;
	private ProtocolStack protocolStack;
	private ResponseCorrelator<ResponseMessage> responseCorrelator;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

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
		this.appReceiver = appReceiver;
		this.logger = appReceiver.getLogger();
	}

	/**
	 * This method is called when the connection should establish itself and commence processing.
	 * Prior to {@link #connect()} calls, the connection should be configured but not active.
	 * 
	 * @throws JRTIinternalError If there is a problem encountered during connection
	 */
	public void connect() throws JRTIinternalError
	{
		
	}
	
	/**
	 * Called when the owner is shutting down. All active connections should be closed.
	 * 
	 * @throws JRTIinternalError If there is a problem encountered during connection
	 */
	public void disconnect() throws JRTIinternalError
	{
		
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
			if( response == null )
				context.setResponse( response );
			else
				context.error( "No response received (request:"+request.getType()+") - RTI/Federates still running?" );
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Message RECEIVING methods   //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
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


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
