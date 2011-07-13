/*
 *   Copyright 2009 The Portico Project
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
package org.portico.lrc.utils;

import java.io.File;
import java.util.Properties;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.utils.ResourceLocator;

/**
 * This class handles the location and loading of a RID file. See {@link #load()} for more details.
 * The properties that were loaded from the RID, along with the location of the RID file can be
 * obtained through static methods.
 */
public class RID
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static boolean RID_LOADED = false;
	private static Properties RID_PROPERTIES = new Properties();
	private static File RID_LOCATION = null;

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static boolean isRidLoaded()
	{
		return RID_LOADED;
	}
	
	public static Properties getRidProperties()
	{
		return RID_PROPERTIES;
	}
	
	/**
	 * The RID file, unless there is none, in which case this will be <code>null</code>.
	 */
	public static File getRidLocation()
	{
		return RID_LOCATION;
	}
	
	/**
	 * Locate and load the RTI Initialization Data file. With the loaded data, the two private
	 * static variables ({@link #RID_PROPERTIES} and {@link #RID_LOCATION}) will be set.
	 * <p/>
	 * To find the file, this method will first check the <code>RTI_RID_FILE</code> environment
	 * variable. If it finds a file there, it will try to load it and then return. If it doesn't
	 * find a file, it will check the default location (<code>./RTI.rid</code>). Should that file
	 * exist, it will be loaded, if not, this method will just return and the null value for
	 * {@link #RID_LOCATION} will indicate that no RID file was loaded.
	 * <p/>
	 * If there is a problem locating the RID file (for example, it exists but is not readable, or
	 * perhaps it is in a dodgy format) a ConfigurationException will be thrown.
	 */
	public static void load() throws JConfigurationException
	{
		if( RID_LOADED )
			return;
		else
			RID_LOADED = true;

		// check the environment variable for the file (PORT-592)
		String ridfile = System.getenv( "RTI_RID_FILE" );
		if( ridfile != null )
		{
			// found an environment variable with the RID file, try to read it
			File file = new File( ridfile );
			if( file.exists() == false )
			{
				throw new JConfigurationException( "RID file specified in RTI_RID_FILE env.var "+
				                                   "could not be located: "+file );
			}
			
			if( file.canRead() == false )
			{
				throw new JConfigurationException( "RID file exists but cannot be read (set from"+
				                                   "RTI_RID_FILE environment variable): " + file );
			}
			
			RID_PROPERTIES = ResourceLocator.loadPropertiesFile( file.getAbsolutePath() );
			RID_LOCATION = file;
		}
		
		// fall back on the default RID file location, if it doesn't exist, just exit
		File file = new File( "RTI.rid" );
		if( file.exists() == false )
		{
			RID_PROPERTIES = new Properties(); // use an empty Properties
		}
		else if( file.canRead() == false )
		{
			throw new JConfigurationException( "RID file exists but cannot be read:" +file );
		}
		else
		{
			RID_PROPERTIES = ResourceLocator.loadPropertiesFile( file.getAbsolutePath() );
			RID_LOCATION = file;
		}
	}

}
