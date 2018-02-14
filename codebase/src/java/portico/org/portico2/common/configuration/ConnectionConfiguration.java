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

import java.util.Properties;

/**
 * This class is the parent of all connection configurations 
 */
public abstract class ConnectionConfiguration
{
	//----------------------------------------------------------
	//                      ENUMERATIONS
	//----------------------------------------------------------

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

	public abstract ConnectionType getType();
	
	/**
	 * Parse the configuration for the connection from the given set of properties. The concrete
	 * connection type should look for properties with the given prefix.
	 * 
	 * @param prefix The prefix to look for properties under
	 * @param properties The properties set to look in
	 */
	protected abstract void parseConfiguration( String prefix, Properties properties );

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
