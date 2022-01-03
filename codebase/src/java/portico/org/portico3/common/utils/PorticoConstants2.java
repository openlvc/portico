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
package org.portico3.common.utils;

import java.io.File;

import org.portico.utils.ResourceLocator;

public class PorticoConstants2
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/** The official name for the RTI, used in the RTIFactory */
	public static final String RTI_NAME = "Portico";
	
	/** The current version of the RTI */
	public static final String RTI_VERSION = getRtiVersion();
	
	/** Consistent form for identifying an invalid time */
	public static final double NULL_TIME = -1.0;
	
	/** Consistent value for an invalid handle */
	public static final int NULL_HANDLE = -1;
	
	/** The handle to use for the "RTI" */
	public static final int RTI_HANDLE = 0;
	

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Accessor and Mutator Methods   /////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Load the properties file "build.properties" into the system properties and return
	 * the value of the value of "${build.version} (build ${build.number})"
	 */
	private static String getRtiVersion()
	{
		if( System.getProperty("build.version") == null )
		{
			try
			{
				ResourceLocator.loadPropertiesResource( "build.properties" );
			}
			catch( Exception e )
			{
				// do nothing, not much we can do
			}
		}
		
		//  
		String buildVersion = System.getProperty( "build.version", "unknown" );
		String buildNumber = System.getProperty( "build.number", "unknown" );
		return buildVersion + " (build "+buildNumber+")";
	}

	/**
	 * Gets the RTI_HOME value where Portico is installed. If the system property
	 * {@link #PROPERTY_RTI_HOME} is set, that value will be used. If it isn't, this method will
	 * check the <code>RTI_HOME</code> environment variable. If that is also not set, the current
	 * directory will be used.
	 */
	public static File getRtiHome()
	{
		// check the rti.home system property
		String location = System.getProperty( "rti.home" );
		if( location != null )
			return new File( location );
		
		// check the RTI_HOME environment variable
		location = System.getenv( "RTI_HOME" );
		if( location != null )
			return new File( location );
		else
			return new File( System.getProperty("user.dir") );
	}


}
