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
package org.portico2.common.network.jvm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.configuration.ConnectionConfiguration;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.network.IConnection;
import org.portico2.common.network.IMessageReceiver;

public class JvmConnection implements IConnection
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean isConnected;
	private Logger logger;
	private IMessageReceiver receiver;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	///////////////////////////////////////////////////////////////////////////////////////
	///  Connection Lifecycle Methods   ///////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure( ConnectionConfiguration configuration, IMessageReceiver receiver )
		throws JConfigurationException
	{
		this.logger = LogManager.getFormatterLogger( receiver.getLogger().getName()+".jvm" );
		this.receiver = receiver;
	}

	/**
	 * 
	 */
	@Override
	public void connect() throws JRTIinternalError
	{
		if( this.isConnected )
			return;
		
		logger.debug( "Attaching to the JVM Exchange" );
		JvmExchange.instance().attachLrc( this );
		
		this.isConnected = true;
	}
	
	/**
	 * 
	 */
	@Override
	public void disconnect() throws JRTIinternalError
	{
		if( this.isConnected == false )
			return;

		logger.debug( "Removing LRC to the JVM Exchange" );
		JvmExchange.instance().detachLrc( this );
		
		this.isConnected = false;
	}


	///////////////////////////////////////////////////////////////////////////////////////
	///  Message SENDING methods   ////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Forward the control request to the exchange.
	 */
	@Override
	public void sendControlRequest( MessageContext context ) throws JRTIinternalError
	{
		JvmExchange.instance().sendControlRequest( this, context );
	}
	
	/**
	 * Forward the data message to the exchange.
	 */
	@Override
	public void sendDataMessage( PorticoMessage message ) throws JException
	{
		JvmExchange.instance().sendDataMessage( this, message );
	}

	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Message RECEIVING methods   //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * FIXME Make Proctected
	 * @param message
	 * @throws JRTIinternalError
	 */
	public void receiveDataMessage( JvmConnection sender, PorticoMessage message ) throws JRTIinternalError
	{
		// FIXME Filter out our own messages
		receiver.receiveDataMessage( message );
	}

	public void receiveControlRequest( JvmConnection sender, MessageContext context ) throws JException
	{
		receiver.receiveControlRequest( context );
	}

	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
