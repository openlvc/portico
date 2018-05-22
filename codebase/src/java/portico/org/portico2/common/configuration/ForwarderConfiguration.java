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

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
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

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
