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
package org.portico2.common.configuration;

import org.portico.lrc.compat.JConfigurationException;

public enum ConnectionType
{
	//----------------------------------------------------------
	//                        VALUES
	//----------------------------------------------------------
	MULTICAST
	{
		public ConnectionConfiguration newConfig( String name ) { return new MulticastConnectionConfiguration(name); }
	},
	TCP_CLIENT
	{
		public ConnectionConfiguration newConfig( String name ) { return new TcpConnectionConfiguration(name); }
	},
	TCP_SERVER
	{
		public ConnectionConfiguration newConfig( String name ) { return new TcpConnectionConfiguration(name); }
	},
	UDP
	{
		public ConnectionConfiguration newConfig( String name ) { throw new IllegalArgumentException("Not yet supported"); }
	},
	JVM
	{
		public ConnectionConfiguration newConfig( String name ) { return new JvmConnectionConfiguration(name); }
	};

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public abstract ConnectionConfiguration newConfig( String name );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Turn the given string into a {@link ConnectionType}.
	 * 
	 * @param string The string to load a connection type from
	 * @return The type of connection represented by the string
	 * @throws JConfigurationException If the type is not known
	 */
	public static ConnectionType fromString( String string ) throws JConfigurationException
	{
		// put any specific ones here
		if( string.equalsIgnoreCase("tcp-server") )
			return TCP_SERVER;
		else if( string.equalsIgnoreCase("tcp-client") )
			return TCP_CLIENT;
		
		// loop through the list of the others
		for( ConnectionType type : ConnectionType.values() )
			if( type.name().equalsIgnoreCase(string) )
				return type;
		
		throw new JConfigurationException( "No such type: "+string );
	}
	
}
