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
package org.portico2.rti;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.network.Connection;
import org.portico2.common.network.Connection.Host;
import org.portico2.common.network.Connection.Status;
import org.portico2.common.network.Header;
import org.portico2.common.network.IApplicationReceiver;
import org.portico2.common.network.configuration.ConnectionConfiguration;
import org.portico2.common.network.transport.TransportType;
import org.portico2.common.services.federation.msg.RtiProbe;

public class RtiConnection implements IApplicationReceiver
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTI rti;
	private ConnectionConfiguration configuration;
	private Connection connection;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public RtiConnection( RTI rti, ConnectionConfiguration configuration )
		throws JConfigurationException
	{
		this.rti = rti;
		this.configuration = configuration;
		
		// Create the underlying connection and configure it
		this.connection = new Connection( Host.RTI, rti );
		this.connection.configure( configuration, this );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	public void connect()
	{
		// 1. Tell the connection to attach
		this.connection.connect();
		
		// 2. Check to see if there is an RTI out there
		MessageContext context = new MessageContext( new RtiProbe() );
		sendControlRequest( context );
		if( context.isSuccessResponse() )
		{
			// RTI is present
			disconnect();
			throw new JRTIinternalError( "An RTI is already running" );
		}
		
		// 3. Record that we are up and everything is good
		connection.setStatus( Status.Connected );
	}
	
	public void disconnect()
	{
		// 1. Tell the connection to detatch
		this.connection.disconnect();
		
		// 2. Record that we are no longer connected
		connection.setStatus( Status.Disconnected );
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Message SENDING methods   ////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * FIXME
	 */
	public void sendControlRequest( MessageContext context ) throws JRTIinternalError
	{
		this.connection.sendControlRequest( context );
	}
	
	/**
	 * FIXME
	 */
	public void sendDataMessage( PorticoMessage message ) throws JException
	{
		this.connection.sendDataMessage( message );
	}


	///////////////////////////////////////////////////////////////////////////////////////
	///  Message RECEIVING methods   //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * FIXME
	 */
	@Override
	public void receiveControlRequest( MessageContext context ) throws JRTIinternalError
	{
		// pass the message to the RTI for processing
		rti.getInbox().receiveControlMessage( context, this );
		
		// return the resulting response message, but check it first
		if( context.hasResponse() == false )
			context.error( new JRTIinternalError("No response after passing to RTI") );
	}
	
	/**
	 * FIXME
	 */
	@Override
	public void receiveDataMessage( PorticoMessage incoming ) throws JException
	{
		rti.getInbox().receiveDataMessage( incoming, this );
	}
	
	@Override
	public void receiveNotification( PorticoMessage incoming ) throws JException
	{
		rti.getInbox().receiveDataMessage( incoming, this );
	}
	
	@Override
	public final boolean isReceivable( Header header )
	{
		return true;
		// only process message if it is
		//   (a) -NOT- from the RTI
		//   (b) The target is either the RTI or Everyone
//		if( header.getSourceFederate() != PorticoConstants.RTI_HANDLE )
//		{
//			return 
//		}
//		else
//		{
//			return false;
//		}
//		
//		return header.getSourceFederate() != PorticoConstants.RTI_HANDLE &&
//		       header.getTargetFederate() == PorticoConstants.RTI_HANDLE ||
//		
//		return sourceFederate != PorticoConstants.RTI_HANDLE;
//		
//		return targetFederate == PorticoConstants.TARGET_ALL_HANDLE ||
//		       (targetFederate == PorticoConstants.RTI_HANDLE &&
//		        sourceFederate != PorticoConstants.RTI_HANDLE );
	}

	@Override
	public Logger getLogger()
	{
		return rti.getLogger();
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public String getName()
	{
		return configuration.getName();
	}
	
	public TransportType getTransportType()
	{
		return configuration.getTransportConfiguration().getTransportType();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
