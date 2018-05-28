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
package org.portico2.common.network2.configuration;

import java.util.Properties;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.network2.ITransport;

/**
 * Every connection has an {@link ITransport} that it ultimately uses to facilite communication.
 * This enumeration lays out the different types of transports that there are and provides utility
 * methods to create {@link ConnectionConfiguration} instances (that wrap the transport) and empty
 * instances of the {@link ITransport} they represent and into which the configs can be loaded.
 */
public enum TransportType
{
	//----------------------------------------------------------
	//                        VALUES
	//----------------------------------------------------------
	JVM,
	Multicast,
	TcpClient,
	TcpServer,
	UdpClient,
	UdpServer;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Create a new connection configuration with the given name and return it.
	 * 
	 * @param name The name to pass to the connection configuration.
	 * @return A new, empty/default configuration for a connection with the given transport.
	 */
	public ConnectionConfiguration newConfiguration( String name )
	{
		switch( this )
		{
			case JVM:       return new JvmConfiguration( name );
			case Multicast: return new MulticastConfiguration( name );
			case TcpClient: return new TcpConfiguration( name, TcpClient );
			case TcpServer: return new TcpConfiguration( name, TcpServer );
			case UdpClient: throw new JRTIinternalError( "UDP Connection Not Yet Supported" );
			case UdpServer: throw new JRTIinternalError( "UDP Connection Not Yet Supported" );
			default:        throw new JRTIinternalError( "Unknown Transport: "+this );
		}
	}

	/**
	 * Create a new connection configuration with the given name and initialize it from the
	 * provided properties. The configuration should look up all its relevant values using
	 * the provided prefix.
	 * 
	 * @param name        The name of the connection
	 * @param prefix      The prefix that the connections config properties are stored under
	 * @param properties  The properties object containing the connection configuration
	 * @return            A new {@link ConnectionConfiguration} that has been initialized with
	 *                    the given configuration properties.
	 * @throws JConfigurationException If there is a problem with any of the provided config data
	 */
	public ConnectionConfiguration newConfiguration( String name, String prefix, Properties properties )
		throws JConfigurationException
	{
		ConnectionConfiguration configuration = newConfiguration( name );
		configuration.parseConfiguration( prefix, properties );
		return configuration;
	}

	/**
	 * @return A new instance of the {@link ITransport} that is represented by this type.
	 *         Note that this instance has not yet been configured and will only have been
	 *         initialized with defaults.
	 */
	public ITransport newTransport()
	{
		switch( this )
		{
			case JVM:       return null;
			case Multicast: return null;
			case TcpClient: return null;
			case TcpServer: return null;
			case UdpClient: throw new JRTIinternalError( "UDP Connection Not Yet Supported" );
			case UdpServer: throw new JRTIinternalError( "UDP Connection Not Yet Supported" );
			default:        throw new JRTIinternalError( "Unknown Transport: "+this );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Turn the given string into a {@link TransportType}.
	 * 
	 * @param string The string to load a connection type from
	 * @return The type of transport represented by the string
	 * @throws JConfigurationException If the type is not known
	 */
	public static TransportType fromString( String string ) throws JConfigurationException
	{
		// put any specific ones here
		if( string.equalsIgnoreCase("tcp-server") )
			return TcpServer;
		else if( string.equalsIgnoreCase("tcp-client") )
			return TcpClient;
		
		// loop through the list of the others
		for( TransportType type : TransportType.values() )
			if( type.name().equalsIgnoreCase(string) )
				return type;
		
		throw new JConfigurationException( "No such type: "+string );
	}

}
