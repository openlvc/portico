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

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.configuration.connection.ConnectionConfiguration;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.federation.msg.RtiProbe;

/**
 * This interface is implemented by the various underlying networking/message exchange classes.
 * Inside the LRC, RTI and Forwarder are connection classes that then leverage {@link IConnection}
 * instances to send and receive the messages. The underlying implementation of the connection
 * is agnostic to where it is being used.
 */
public interface IConnection
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
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
	public void configure( ConnectionConfiguration configuration, IMessageReceiver receiver )
		throws JConfigurationException;

	/**
	 * This method is called when the connection should establish itself and commence processing.
	 * Prior to {@link #connect()} calls, the connection should be configured but not active.
	 * 
	 * @throws JRTIinternalError If there is a problem encountered during connection
	 */
	public void connect() throws JRTIinternalError;
	
	/**
	 * Called when the owner is shutting down. All active connections should be closed.
	 * 
	 * @throws JRTIinternalError If there is a problem encountered during connection
	 */
	public void disconnect() throws JRTIinternalError;

	/**
	 * This method will send an {@link RtiProbe}. If we get a response, we know there
	 * is an RTI out there. If we don't, we know there isn't one.
	 * 
	 * @return <code>true</code> if there is an RTI out there; <code>false</code> otherwise
	 */
	public default boolean findRti()
	{
		MessageContext context = new MessageContext( new RtiProbe() );
		sendControlRequest( context );
		return context.isSuccessResponse();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Message SENDING methods   ////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Control messages are ones that expect a response. When a component wants to send a
	 * control message it packs it into a {@link MessageContext} and passes it to the connection.
	 * This call is expected to BLOCK while a response is sought. At the conclusion of this
	 * call, a response should be populated in the {@link MessageContext}. 
	 * 
	 * @param context Contains the request. Response will be put in here.
	 * @throws JRTIinternalError If there is a problem processing the request.
	 */
	public void sendControlRequest( MessageContext context ) throws JRTIinternalError;
	
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
	public void sendDataMessage( PorticoMessage message ) throws JException;

}
