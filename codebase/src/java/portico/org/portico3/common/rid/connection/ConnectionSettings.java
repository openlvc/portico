/*
 *   Copyright 2021 The Portico Project
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
package org.portico3.common.rid.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.portico3.common.rid.RID;

/**
 * A generic class for representing Connection settings in either the RTI or LRC section of
 * a {@link RID} file.
 */
public class ConnectionSettings
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final String KEY_ENABLED = "enabled";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String name;
	private boolean enabled;
	private Map<String,String> values;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private ConnectionSettings( String name )
	{
		this.name = name;
		this.enabled = false;
		this.values = new HashMap<>();
	}

	public ConnectionSettings( String name, Map<String,String> givenValues )
	{
		this( name );
		this.values.clear();
		this.values.putAll( givenValues );
		
		// refresh our locally caches values from the set of given values
		refreshFromValues();
	}
	
	public ConnectionSettings( String name, Properties givenProperties )
	{
		this( name );
		this.values.clear();
		givenProperties.forEach( (key,value) -> values.put(key.toString(),value.toString()) );
		
		// refresh our locally caches values from the set of given values
		refreshFromValues();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	/**
	 * Will merge the given values with those that already exist locally
	 * 
	 * @param givenValues The values to be merged
	 */
	public void updateValues( Map<String,String> givenValues )
	{
		this.values.putAll( givenValues );
		refreshFromValues();
	}

	/**
	 * Replace any existing values with those that are given.
	 * 
	 * @param givenValues The values that should be used once any existing values are cleared
	 */
	public void replaceValues( Map<String,String> givenValues )
	{
		this.values.clear();
		this.values.putAll( givenValues );
		refreshFromValues();
	}
	
	public boolean containsKey( String key )
	{
		return this.values.containsKey( key );
	}

	/**
	 * @return The raw string value for the key (or null if the key is not present)
	 * @param key The key to look up the value for
	 */
	public String getRawValue( String key )
	{
		return this.values.get( key );
	}
	
	public void putRawValue( String key, String value )
	{
		this.values.put( key, value );
		refreshFromValues();
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Accessor and Mutator Methods   /////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public String getName()
	{
		return this.name;
	}

	public boolean isEnabled()
	{
		return this.enabled;
	}
	
	public void setEnabled( boolean enabled )
	{
		this.enabled = false;
		this.values.put( "enabled", ""+enabled );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Private Helper Methods   ///////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Refresh any locally cached values from the set of raw strings
	 */
	private void refreshFromValues()
	{
		this.enabled = Boolean.valueOf( values.getOrDefault(KEY_ENABLED,"false") );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
