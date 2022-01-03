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

import org.portico3.common.compatibility.JConfigurationException;
import org.portico3.common.rid.parser.RidXmlParser;
import org.portico3.rti.commandline.CommandLine;

/**
 * The {@link RID} represents the primary configuration structure that is used to tell
 * Portico what its various configuration values should be. Portico ships with a default RID
 * file that is used as the base template. If a user specifies an additional RID file, it will
 * be parsed, and any values inside it will be used to override the default RID.
 */
public class RID
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String ridFileLocation;
	private LogSettings logSettings;
	private RtiSettings rtiSettings;
	private LrcSettings lrcSettings;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public RID()
	{
		this.ridFileLocation = null;
		this.logSettings = new LogSettings();
		this.rtiSettings = new RtiSettings();
		this.lrcSettings = new LrcSettings();		
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public String getRidFileLocation()
	{
		return this.ridFileLocation == null ? "" : this.ridFileLocation;
	}
	
	public LogSettings getLogSettings()
	{
		return this.logSettings;
	}
	
	public RtiSettings getRtiSettings()
	{
		return this.rtiSettings;
	}
	
	public LrcSettings getLrcSettings()
	{
		return this.lrcSettings;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	/// Command Line Overriding Support   //////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void applyOverrides( CommandLine commandline ) throws JConfigurationException
	{
		logSettings.applyOverrides( commandline );
		rtiSettings.applyOverrides( commandline );
		lrcSettings.applyOverrides( commandline );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static final RID load( String path )  throws JConfigurationException
	{
		File file = new File( path );
		if( file.exists() == false )
			throw new JConfigurationException( "RID File does not exist [%s]", path );
		
		RID rid = new RidXmlParser(file).parse();
		rid.ridFileLocation = path;
		return rid;
	}
	
}
