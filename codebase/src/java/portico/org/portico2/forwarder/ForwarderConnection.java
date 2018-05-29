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
package org.portico2.forwarder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.network.Connection;
import org.portico2.common.network.Header;
import org.portico2.common.network.IApplicationReceiver;
import org.portico2.common.network.configuration.ConnectionConfiguration;

public class ForwarderConnection implements IApplicationReceiver
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Direction direction;
	private Exchanger exchanger;
	private Logger logger;
	
	// Connection Settings
	private ConnectionConfiguration configuration;
	private Connection connection;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected ForwarderConnection( Direction direction,
	                               Exchanger exchanger,
	                               ConnectionConfiguration configuration )
		throws JConfigurationException
	{
		this.direction = direction;
		this.exchanger = exchanger;
		this.logger = LogManager.getFormatterLogger( exchanger.getLogger().getName()+"."+direction );

		// Connection Settings
		this.configuration = configuration;
		this.connection = new Connection();
		this.connection.configure( configuration, this );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void connect()
	{
		// 1. Tell the connection to attach
		this.connection.connect();
	}
	
	public void disconnect()
	{
		// Tell the connection to detatch
		this.connection.disconnect();
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Message RECEIVING methods   //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void receiveControlRequest( MessageContext context ) throws JRTIinternalError
	{
		// No-op
		// FIXME - Throw exception?
	}

	@Override
	public void receiveDataMessage( PorticoMessage message ) throws JException
	{
		// No-op
		// FIXME - Throw exception?
	}

	@Override
	public final boolean isReceivable( Header header )
	{
		// accept everything
		return true;
	}
	
	@Override
	public Logger getLogger()
	{
		return logger;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Accessor and Mutator Methods   /////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public Direction getDirection()
	{
		return this.direction;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
