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

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.network.configuration.ConnectionConfiguration;
import org.portico2.common.network.transport.TransportType;
import org.portico2.common.utils.XmlUtils;
import org.w3c.dom.Element;

public class RtiConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

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

	////////////////////////////////////////////////////////////////////////////////////////
	/// Network Configuration Options    ///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
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
			if( alltypes.contains(configuration.getTransportConfiguration().getTransportType()) )
				toRemove.add( name );
		}
		
		for( String name : toRemove )
			rtiConnections.remove( name );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	/// Configuration Parsing Methods   ////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	protected void parseConfiguration( RID rid, Element rtiElement ) throws JConfigurationException
	{
		// Fetch the Network Properties
		Element network = XmlUtils.getChild( rtiElement, "network", true );
		List<Element> connections = XmlUtils.getChildren( network, "connection" );
		for( Element connectionElement : connections )
		{
			// get the name of the connection
			String name = connectionElement.getAttribute( "name" );
			if( name == null || name.trim().equals("") )
				throw new JConfigurationException( "RTI <connection> missing attribute \"name\"" );
			
			// create the configuration
			ConnectionConfiguration configuration = new ConnectionConfiguration( name );
			configuration.parseConfiguration( rid, connectionElement );
			this.rtiConnections.put( name, configuration );
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
