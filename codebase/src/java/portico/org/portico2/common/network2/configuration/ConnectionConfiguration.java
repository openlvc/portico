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

/**
 * This class represents a generic structure for the configuration of a connection.
 * Each connection has a root transport type that it uses. This is expressed in the
 * {@link TransportType} enumeration.
 * <p/>
 * 
 * Each connection also has a set of common properties, such as a name, encryption
 * and filtering settings. The common properties are accessed through this parent
 * class, but the transport-specific properties are contained in subclasses. 
 */
public abstract class ConnectionConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String name;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected ConnectionConfiguration( String name )
	{
		this.name = name;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * @return Get the underlying transport type that this connection will use.
	 */
	public abstract TransportType getTransportType();
	
	/**
	 * Parse the configuration for the connection from the given set of properties. The concrete
	 * connection type should look for properties with the given prefix.
	 * 
	 * @param prefix The prefix to look for properties under
	 * @param properties The properties set to look in
	 */
	public abstract void parseConfiguration( String prefix, Properties properties );

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public String getName()
	{
		return this.name;
	}
	
	public void setName( String name )
	{
		this.name = name;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
