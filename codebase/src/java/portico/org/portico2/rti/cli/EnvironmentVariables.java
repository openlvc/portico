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
package org.portico2.rti.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import org.portico.lrc.PorticoConstants;

public class EnvironmentVariables
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<String,Supplier<String>> suppliers;
	private RtiCli container;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public EnvironmentVariables( RtiCli container )
	{
		this.container = container;
		this.suppliers = new HashMap<>();
		initialize();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private void initialize()
	{
		suppliers.put( "PWD", this::getCurrentContextPath );
		suppliers.put( "RTI_HOME", this::getRtiHome );
		suppliers.put( "RTI_NAME", ()-> PorticoConstants.RTI_NAME );
		suppliers.put( "RTI_RID_FILE", this::getRidFile );
		suppliers.put( "RTI_VERSION", ()-> PorticoConstants.RTI_VERSION );
	}

	private String getCurrentContextPath()
	{
		return container.getCurrentContext().getHeirachicalName();
	}
	
	private String getRtiHome()
	{
		return container.getRti().getRid().getRtiHome().getAbsolutePath();
	}
	
	private String getRidFile()
	{
		return container.getRti().getRid().getRidFile().getAbsolutePath();
	}
	
	/**
	 * Replaces any instances of environment variable tokens in the provided string with their
	 * corresponding values
	 * 
	 * @param commandline the string to replace the environment variable tokens in
	 * @return a copy of <code>commandline</code>, with any environment variable tokens replaced
	 */
	public String replaceTokens( String commandline )
	{
		String newCommandline = commandline;
		for( Entry<String,Supplier<String>> entry : this.suppliers.entrySet() )
		{
			String query = "$" + entry.getKey();
			newCommandline = newCommandline.replace( query, entry.getValue().get() );
		}
		
		return newCommandline;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the value of the specified environment variable
	 * 
	 * @param key the name of the environment variable
	 * @return the value of the environment variable
	 */
	public String get( String key )
	{
		String value = null;
		Supplier<String> supplier = this.suppliers.get( key );
		if( supplier != null )
			value = supplier.get();
		
		return value;
	}

	/**
	 * @return all environment variable keys that are registered
	 */
	public Set<String> getKeys()
	{
		return this.suppliers.keySet();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
