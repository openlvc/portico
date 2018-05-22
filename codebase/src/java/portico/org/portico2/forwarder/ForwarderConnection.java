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
import org.portico2.common.configuration.connection.ConnectionConfiguration;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.network.ConnectionFactory;
import org.portico2.common.network.IConnection;
import org.portico2.common.network.IMessageReceiver;

public class ForwarderConnection implements IMessageReceiver
{
	//----------------------------------------------------------
	//                      ENUMERATIONS
	//----------------------------------------------------------
	/** The "direction" that a connection will pass messages. */
	public enum Direction
	{
		UPSTREAM, DOWNSTREAM;
		public String toString() { return name().toLowerCase(); }
		public String flowDirection() { return this == UPSTREAM ? "upstream>>>downstream" : "upstream<<<downstream"; }
		public Direction reverse() { return this == UPSTREAM ? DOWNSTREAM : UPSTREAM; }
	}
	

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
	private IConnection connection;

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
		this.connection = ConnectionFactory.createConnection( configuration.getType() );
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
	///  Message SENDING methods   ////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	public void sendControlRequest( MessageContext context ) throws JRTIinternalError
	{
		connection.sendControlRequest( context );
	}
	
	public void sendDataMessage( PorticoMessage message ) throws JException
	{
		connection.sendDataMessage( message );
	}

	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Message RECEIVING methods   //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void receiveControlRequest( MessageContext context ) throws JRTIinternalError
	{
		exchanger.controlRequestReceived( direction, context );
	}
	
	@Override
	public void receiveDataMessage( PorticoMessage incoming ) throws JException
	{
		exchanger.dataMessageReceived( direction, incoming );
	}

	@Override
	public final boolean isReceivable( int targetFederate )
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
