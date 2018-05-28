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
package org.portico2.common.network2.tcp;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.network2.Connection;
import org.portico2.common.network2.ITransport;
import org.portico2.common.network2.Message;
import org.portico2.common.network2.configuration.ConnectionConfiguration;
import org.portico2.common.network2.configuration.TransportType;

public class TcpClientTransport implements ITransport
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TcpClientTransport()
	{
		
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	@Override
	public TransportType getType()
	{
		return TransportType.TcpClient;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Transport Lifecycle Methods   ////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure( ConnectionConfiguration configuration, Connection connection )
		throws JConfigurationException
	{
		
	}

	/**
	 * Open the socket, perform a small handshake and then wrap the socket in a {@link TcpChannel}. 
	 */
	@Override
	public void open() throws JRTIinternalError
	{
		
	}
	
	/**
	 * Close off the {@link TcpChannel} and the underlying socket.
	 */
	@Override
	public void close() throws JRTIinternalError
	{
		
	}

	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Transport Messaging Methods   ////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void send( Message message )
	{
		
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
