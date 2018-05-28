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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.utils.StringUtils;
import org.portico2.common.PorticoConstants;
import org.portico2.common.network2.configuration.ConnectionConfiguration;
import org.portico2.common.network2.configuration.JvmConfiguration;
import org.portico2.common.network2.configuration.MulticastConfiguration;
import org.portico2.common.network2.configuration.TransportType;

public class RtiConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final String KEY_RTI_CONNECTIONS = "rti.network.connections";
	public static final String PFX_RTI_CONNECTION  = "rti.network"; // rti.network.$name

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<String,ConnectionConfiguration> rtiConnections;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected RtiConfiguration()
	{
		this.rtiConnections = new HashMap<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Network Configuration Options    ///////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public ConnectionConfiguration getConnection( String name )
	{
		return rtiConnections.get( name );
	}
	
	public void addConnection( ConnectionConfiguration configuration )
	{
		String name = configuration.getName();
		if( rtiConnections.containsKey(name) )
			throw new IllegalArgumentException( "RTI already contains connection with name "+name );
		else
			rtiConnections.put( name, configuration );
	}

	/**
	 * @param name The name of the connection to remove
	 * @return The connection that was removed, or null if none could be found with that name
	 */
	public ConnectionConfiguration removeConnection( String name )
	{
		return rtiConnections.remove( name );
	}
	
	public Map<String,ConnectionConfiguration> getConnections()
	{
		return Collections.unmodifiableMap( this.rtiConnections );
	}

	/**
	 * Go through all the connections and remove any that are of the given transport type.
	 * This helps you force the use of a specific connection type. 
	 */
	public void removeConnectionsOfType( TransportType... types )
	{
		List<String> toRemove = new ArrayList<>();
		List<TransportType> alltypes = Arrays.asList( types );
		for( String name : rtiConnections.keySet() )
		{
			ConnectionConfiguration configuration = rtiConnections.get( name );
			if( alltypes.contains(configuration.getTransportType()) )
				toRemove.add( name );
		}
		
		for( String name : toRemove )
			rtiConnections.remove( name );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/// Configuration Loading   ////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	protected void parseProperties( Properties properties ) throws JConfigurationException
	{
		String property = properties.getProperty( KEY_RTI_CONNECTIONS, "" ).trim();
		if( property.equals("") )
		{
			if( PorticoConstants.OVERRIDE_CONNECTION == null )
			{
				// no configurations have been specified - fall back on just having a JVM connection
				this.rtiConnections.put( "jvm", new JvmConfiguration("jvm") );
				this.rtiConnections.put( "multicast", new MulticastConfiguration("multicast") );
			}
			else
			{
				// an override is in place, use it
				String override = PorticoConstants.OVERRIDE_CONNECTION;
				ConnectionConfiguration config = TransportType.fromString(override).newConfiguration("hlaunit");
				this.rtiConnections.put( "hlaunit", config );
			}
		}
		else
		{
    		String[] names = StringUtils.splitAndTrim( property, "," );
    		
			// empty out any existing connection information
			rtiConnections.clear();
			
    		// build a configuration object for each type
    		for( String name : names )
    		{
    			String prefix = PFX_RTI_CONNECTION+"."+name;
    			
    			// get the connection type
    			String typeString = properties.getProperty( prefix+".type" );
    			if( typeString == null )
    				throw new JConfigurationException( "RTI Connection [%s] does not specify a type", name );
    			
    			TransportType type = TransportType.fromString( typeString );
    
    			// create the configuration and store it
    			ConnectionConfiguration configuration = type.newConfiguration( name );
    			configuration.parseConfiguration( prefix, properties );
    			this.rtiConnections.put( name, configuration );
    		}
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
