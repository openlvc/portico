/*
 *   Copyright 2022 The Portico Project
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
package org.portico3.common.rid;

import java.util.HashMap;
import java.util.Map;

import org.portico3.common.compatibility.JConfigurationException;
import org.portico3.common.rid.connection.ConnectionSettings;
import org.portico3.rti.commandline.CommandLine;

/**
 * Represents the configuration options from the LRC section of a {@link RID}.
 */
public class LrcSettings
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// Carrier Settings
	private String carrierManagement;
	private String carrierReflections;
	private String carrierInteractions;
	
	// Options
	private long lrcTickTimeout;
	
	// Connection Settings
	private Map<String,ConnectionSettings> connectionSettings;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected LrcSettings()
	{
		// Carrier Settings
		this.carrierManagement = "default";
		this.carrierReflections = "default";
		this.carrierInteractions = "default";

		// Options
		this.lrcTickTimeout = 5;
		
		// Connetion Settings
		this.connectionSettings = new HashMap<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Carrier Settings  //////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public String getManagementCarrier()
	{
		return this.carrierManagement;
	}
	
	public void setManagementCarrier( String carrier )
	{
		this.carrierManagement = carrier;
	}

	public String getReflectionsCarrier()
	{
		return carrierReflections;
	}

	public void setReflectionsCarrier( String carrier )
	{
		this.carrierReflections = carrier;
	}

	public String getInteractionsCarrier()
	{
		return carrierInteractions;
	}

	public void setInteractionsCarrier( String carrier )
	{
		this.carrierInteractions = carrier;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/// Options   //////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public long getLrcTickTimeout()
	{
		return lrcTickTimeout;
	}

	public void setLrcTickTimeout( long timeout )
	{
		this.lrcTickTimeout = timeout;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Connection Settings  ///////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public ConnectionSettings getConnectionSettings( String connectionName )
	{
		return connectionSettings.get( connectionName );
	}

	/**
	 * Adds configuration for a new connnection to the LRC settings. If one already exists with
	 * the given name, an exception is thrown.
	 * 
	 * @param settings The connection settings values to be added
	 * @throws JConfigurationException If a connection with the name already exists in the settings
	 */
	public void addConnectionSettings( ConnectionSettings settings )
		throws JConfigurationException
	{
		String name = settings.getName();
		if( connectionSettings.containsKey(name) )
			throw new JConfigurationException( "Connection [%s] already exists in LRC config", name );
		else
			connectionSettings.put( settings.getName(), settings );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Command Line Overriding Support   //////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	protected void applyOverrides( CommandLine commandline )
	{
		
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
