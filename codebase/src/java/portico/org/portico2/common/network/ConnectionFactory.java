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
import org.portico2.common.configuration.ConnectionType;
import org.portico2.common.network.jvm.JvmConnection;
import org.portico2.common.network.multicast.MulticastConnection;
import org.portico2.common.network.tcp.client.TcpClientConnection;
import org.portico2.common.network.tcp.server.TcpServerConnection;

/**
 * Instantiate instances of {@link IConnection}.
 */
public class ConnectionFactory
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

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Create and return a connection of the given type. This connection is not configured and 
	 * has had no setup operations done to it. It is simply created and returned.
	 * 
	 * @param type The type of connection to create
	 * @return     The newly created {@link IConnection} of the appropriate type
	 * @throws JConfigurationException If the connection type is not known
	 */
	public static IConnection createConnection( ConnectionType type ) throws JConfigurationException
	{
		switch( type )
		{
			case MULTICAST:
				return new MulticastConnection();
			case JVM:
				return new JvmConnection();
			case TCP_SERVER:
				return new TcpServerConnection();
			case TCP_CLIENT:
				return new TcpClientConnection();
			case UDP:
			default:
				throw new JConfigurationException( "Connection type not supported yet: "+type );
		}
	}
}
