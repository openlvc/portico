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
import org.portico2.common.network.Connection.Host;
import org.portico2.common.network.Connection.Status;
import org.portico2.common.network.configuration.ConnectionConfiguration;
import org.portico2.forwarder.tracking.StateTracker;

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
	private StateTracker stateTracker;
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
		this.stateTracker = exchanger.stateTracker;
		this.logger = LogManager.getFormatterLogger( exchanger.getLogger().getName()+"."+direction );
		this.logger = exchanger.getLogger();

		// Connection Settings
		this.configuration = configuration;
		this.connection = new Connection( Host.Forwarder, exchanger.getForwarder() );
		this.connection.configure( configuration, this );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void connect()
	{
		// 1. Tell the connection to attach
		this.connection.connect();
		
		// 2. Record that we are attached!
		this.connection.setStatus( Status.Connected );
	}
	
	public void disconnect()
	{
		// Tell the connection to detatch
		this.connection.disconnect();
		
		// Record that we are no longer connected
		this.connection.setStatus( Status.Disconnected );
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Message RECEIVING methods   //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void receiveControlRequest( MessageContext context ) throws JRTIinternalError
	{
		// no-op
		logger.warn( "Received Control Request - should never get here (%s) source=%s, target=%s",
		             context.getRequest().getType(),
		             context.getRequest().getSourceFederate(),
		             context.getRequest().getTargetFederate() );
	}

	@Override
	public void receiveDataMessage( PorticoMessage message ) throws JException
	{
		// No-op
		logger.warn( "Received Data Message - should never get here (%s) source=%s, target=%s",
		             message.getType(),
		             message.getSourceFederate(),
		             message.getTargetFederate() );
	}

	@Override
	public void receiveNotification( PorticoMessage message ) throws JException
	{
		// No-op
		logger.warn( "Received Notification - should never get here (%s) source=%s, target=%s",
		             message.getType(),
		             message.getSourceFederate(),
		             message.getTargetFederate() );
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
	
	protected Connection getConnection()
	{
		return this.connection;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
