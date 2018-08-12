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
package org.portico2.common.network.transport;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.network.configuration.ConnectionConfiguration;
import org.portico2.common.network.configuration.transport.JvmConfiguration;
import org.portico2.common.network.configuration.transport.MulticastConfiguration;
import org.portico2.common.network.configuration.transport.TcpConfiguration;
import org.portico2.common.network.configuration.transport.TransportConfiguration;
import org.portico2.common.network.transport.jvm.JvmTransport;
import org.portico2.common.network.transport.multicast.MulticastTransport;
import org.portico2.common.network.transport.tcp.TcpClientTransport;
import org.portico2.common.network.transport.tcp.TcpServerTransport;

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
	 * Create a transport configuration within the given {@link ConnectionConfiguration}.
	 * 
	 * @param connection The connection we are part of
	 * @return A new, empty/default configuration for a connection with the given transport.
	 */
	public TransportConfiguration newConfiguration( ConnectionConfiguration connection )
	{
		switch( this )
		{
			case JVM:       return new JvmConfiguration( connection );
			case Multicast: return new MulticastConfiguration( connection );
			case TcpClient: return new TcpConfiguration( connection, TcpClient );
			case TcpServer: return new TcpConfiguration( connection, TcpServer );
			case UdpClient: throw new JRTIinternalError( "UDP Connection Not Yet Supported" );
			case UdpServer: throw new JRTIinternalError( "UDP Connection Not Yet Supported" );
			default:        throw new JRTIinternalError( "Unknown Transport: "+this );
		}
	}

	/**
	 * @return A new instance of the {@link ITransport} that is represented by this type.
	 *         Note that this instance has not yet been configured and will only have been
	 *         initialized with defaults.
	 */
	public Transport newTransport()
	{
		switch( this )
		{
			case JVM:       return new JvmTransport();
			case Multicast: return new MulticastTransport();
			case TcpClient: return new TcpClientTransport();
			case TcpServer: return new TcpServerTransport();
			case UdpClient: throw new JRTIinternalError( "UDP Connection Not Yet Supported" );
			case UdpServer: throw new JRTIinternalError( "UDP Connection Not Yet Supported" );
			default:        throw new JRTIinternalError( "Unknown Transport: "+this );
		}
	}

	/** Return the name of the sub-element under the <connection> that contains the transport
	    specific configuration options */
	public String getConfigurationName()
	{
		switch( this )
		{
			case JVM:       return "jvm";
			case Multicast: return "multicast";
			case TcpClient: return "tcp-client";
			case TcpServer: return "tcp-server";
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
