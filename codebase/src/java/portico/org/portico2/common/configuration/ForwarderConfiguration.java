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

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.configuration.connection.ConnectionConfiguration;
import org.portico2.common.configuration.connection.ConnectionType;

public class ForwarderConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final String PFX_DOWNSTREAM = "fwd.network.downstream";
	private static final String PFX_UPSTREAM   = "fwd.network.upstream";

	// Firewall properties
	public static final String KEY_FIREWALL_ENABLED = "fwd.firewall.enabled";
	public static final String KEY_FIREWALL_IMPORT_OBJ = "fwd.firewall.import.object";
	public static final String KEY_FIREWALL_IMPORT_INT = "fwd.firewall.import.interaction";
	public static final String KEY_FIREWALL_EXPORT_OBJ = "fwd.firewall.export.object";
	public static final String KEY_FIREWALL_EXPORT_INT = "fwd.firewall.export.interaction";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Properties properties;
	private ConnectionConfiguration downstreamConfiguration;
	private ConnectionConfiguration upstreamConfiguration;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Configuration Loading   ////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	protected void parseProperties( Properties properties ) throws JConfigurationException
	{
		this.properties = properties;

		//
		// Network Configuration
		//
		// Local-side connection configuration
		String typeString = properties.getProperty( PFX_DOWNSTREAM+".type", "multicast" );
		ConnectionType localType = ConnectionType.fromString( typeString );
		this.downstreamConfiguration = localType.newConfig( "downstream" );
		this.downstreamConfiguration.parseConfiguration( PFX_DOWNSTREAM, properties );
		
		// Upstream-side connection configuration
		typeString = properties.getProperty( PFX_UPSTREAM+".type", "tcp-client" );
		ConnectionType upstreamType = ConnectionType.fromString( typeString );
		this.upstreamConfiguration = upstreamType.newConfig( "upstream" );
		this.upstreamConfiguration.parseConfiguration( PFX_UPSTREAM, properties );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Accessors and Mutators   ///////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public ConnectionConfiguration getDownstreamConfiguration()
	{
		return this.downstreamConfiguration;
	}
	
	public ConnectionConfiguration getUpstreamConfiguration()
	{
		return this.upstreamConfiguration;
	}

	public boolean isFirewallEnabled()
	{
		return Boolean.valueOf(properties.getProperty(KEY_FIREWALL_ENABLED,"false"));
	}
	
	public Set<String> getAllowedImportObjects()
	{
		return stringToSet( properties.getProperty(KEY_FIREWALL_IMPORT_OBJ,"") );
	}
	
	public Set<String> getAllowedImportInteractions()
	{
		return stringToSet( properties.getProperty(KEY_FIREWALL_IMPORT_INT,"") );
	}
	
	public Set<String> getAllowedExportObjects()
	{
		return stringToSet( properties.getProperty(KEY_FIREWALL_EXPORT_OBJ,"") );
	}
	
	public Set<String> getAllowedExportInteractions()
	{
		return stringToSet( properties.getProperty(KEY_FIREWALL_EXPORT_INT,"") );
	}

	private Set<String> stringToSet( String string )
	{
		HashSet<String> set = new HashSet<String>();
		StringTokenizer tokenizer = new StringTokenizer( string, "," );
		while( tokenizer.hasMoreTokens() )
			set.add( tokenizer.nextToken().trim() );
		
		return set;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
