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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.portico3.common.compatibility.JConfigurationException;
import org.portico3.common.rid.connection.ConnectionSettings;
import org.portico3.rti.commandline.CommandLine;

/**
 * Represents the configuration options from the RTI section of a {@link RID}.
 */
public class RtiSettings
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// HLA Options
	private boolean momEnabled;
	private File saveDirectory;
	private boolean unsupportedExceptions;
	private boolean uniqueFederateNames;
	
	// Connection Settings
	private Map<String,ConnectionSettings> connectionSettings;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected RtiSettings()
	{
		// HLA Options
		this.momEnabled = true;
		this.saveDirectory = new File( "./savedata" );
		this.unsupportedExceptions = false;
		this.uniqueFederateNames = true;
		
		// Connection Settings
		this.connectionSettings = new HashMap<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/// HLA Options  ///////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public boolean isMomEnabled()
	{
		return momEnabled;
	}

	public void setMomEnabled( boolean enabled )
	{
		this.momEnabled = enabled;
	}

	public File getSaveDirectory()
	{
		return saveDirectory;
	}

	public void setSaveDirectory( File saveDirectory )
	{
		this.saveDirectory = saveDirectory;
	}
	
	public void setSaveDirectory( String saveDirectory )
	{
		this.saveDirectory = new File( saveDirectory );
	}

	public boolean isUnsupportedExceptionsEnabled()
	{
		return unsupportedExceptions;
	}

	public void setUnsupportedExceptions( boolean enabled )
	{
		this.unsupportedExceptions = enabled;
	}

	public boolean isUniqueFederateNamesEnabled()
	{
		return uniqueFederateNames;
	}

	public void setUniqueFederateNames( boolean enabled )
	{
		this.uniqueFederateNames = enabled;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/// Connection Settings  ///////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public ConnectionSettings getConnectionSettings( String connectionName )
	{
		return connectionSettings.get( connectionName );
	}

	/**
	 * Adds configuration for a new connnection to the RTI settings. If one already exists with
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
			throw new JConfigurationException( "Connection [%s] already exists in RTI config", name );
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
