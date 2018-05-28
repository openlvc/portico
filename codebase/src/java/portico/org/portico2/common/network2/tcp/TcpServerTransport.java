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

public class TcpServerTransport implements ITransport
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
	public TcpServerTransport()
	{
		
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	@Override
	public TransportType getType()
	{
		return TransportType.TcpServer;
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
	 * Open up a server socket to start accepting connection requests. The connection
	 * establishment process is handled by the {@lik ConnectionAcceptor} inner class.
	 * 
	 * @throws JRTIinternalError If there is a problem encountered during connection
	 */
	@Override
	public void open() throws JRTIinternalError
	{
		
	}
	
	/**
	 * Close out the server socket so that we can't accept any more connections.
	 * 
	 * @throws JRTIinternalError If there is a problem encountered during connection
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
