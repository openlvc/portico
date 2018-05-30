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

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.network.Connection;
import org.portico2.common.network.ITransport;
import org.portico2.common.network.Message;
import org.portico2.common.network.ProtocolStack;
import org.portico2.common.network.configuration.ConnectionConfiguration;
import org.portico2.common.network.configuration.TransportType;

public class JvmTransport implements ITransport
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean isConnected;
	private Logger logger;
	private Connection connection;
	private ProtocolStack protocolStack;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public TransportType getType()
	{
		return TransportType.JVM;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Transport Lifecycle Methods   ////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure( ConnectionConfiguration configuration, Connection connection )
		throws JConfigurationException
	{
		this.connection = connection;
		this.logger = connection.getLogger();
		this.protocolStack = connection.getProtocolStack();
		
		// drop the response wait time
		connection.getResponseCorrelator().setTimeout( 100 );
	}

	@Override
	public void open() throws JRTIinternalError
	{
		if( this.isConnected )
			return;
		
		logger.trace( "Attaching to the JVM Exchange" );
		JvmExchange.instance().attachLrc( this );
		
		this.isConnected = true;
	}
	
	@Override
	public void close() throws JRTIinternalError
	{
		if( this.isConnected == false )
			return;

		logger.trace( "Removing LRC to the JVM Exchange" );
		JvmExchange.instance().detachLrc( this );
		
		this.isConnected = false;
	}

	@Override
	public boolean isOpen()
	{
		return this.isConnected;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Transport Messaging Methods   ////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void send( Message message )
	{
		JvmExchange.instance().sendMessage( this, message.getBuffer() );
	}

	protected void receive( byte[] message )
	{
		protocolStack.up( new Message(message) );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
