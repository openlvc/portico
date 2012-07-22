/*
 *   Copyright 2006 The Portico Project
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
package org.portico.console.client.text;

/**
 * Class that provides a central location for a number of constant or configuration elements. 
 */
public class MainProperties
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/** The location of the console installation */
	private static String CONSOLE_HOME = "";
	
	/** Location of the log4j configuration file */
	private static String LOG4J_CONFIG = "etc/console-log4j.properties";
	
	/** Console binding endpoint **/
	private static String ENDPOINT = null;
	
	/** Location of the module file for the sink */
	private static String MODULE_LOCATION = "etc/console-client-module.xml";
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	private MainProperties()
	{
		
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	public static String getConsoleHome()
	{
		return CONSOLE_HOME;
	}
	
	public static void setConsoleHome( String home )
	{
		if( home.equals(".") || home.equals("./") || home.equals(".\\") )
		{
			CONSOLE_HOME = "";
			return;
		}
		
		if( home.endsWith("/") || home.endsWith("\\") )
		{
			// strip the trailing path designator
			home = home.substring( 0, home.length()-1 );
		}
		
		if( home.length() == 0 )
		{
			return;
		}
		
		CONSOLE_HOME = home + System.getProperty("file.separator");
	}
			
	public static String getLog4jConfig()
	{
		return LOG4J_CONFIG;
	}
	
	public static String getEndpoint()
	{
		return ENDPOINT;
	}
	
	public static void setEndpoint( String newEndpoint )
	{
		ENDPOINT = newEndpoint;
	}
	
	public static String getModuleLocation()
	{
		return MODULE_LOCATION;
	}
}
