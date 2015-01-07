/*
 *   Copyright 2012 The Portico Project
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
package org.portico.bindings.jgroups;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * All configuration information is stored in system properties as keys. This class
 * provides statics that can be used to identify the specific keys.
 */
public class JGroupsProperties
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	///// configuration system properties ////////////////////////////////////////////////////
	/** The system property that carries the log level to use for the JGroups logger */
	public static final String PROP_JGROUPS_LOGLEVEL = "portico.jgroups.loglevel";

	/** The system property that specifies how long (millis) we should wait to received
	    shared state from the group coordinator when joining a jgroups channel */
	public static final String PROP_JGROUPS_JOIN_TIMEOUT = "portico.jgroups.jointimeout";

	/** The system property that specifies whether or not the JGroups threads should be daemons */
	public static final String PROP_JGROUPS_DAEMON = "portico.jgroups.daemon";

	/** The system property that specifies the timeout value is used when waiting for response
	    messages (in milliseconds), default value is 1000 */
	public static final String PROP_JGROUPS_TIMEOUT = "portico.jgroups.timeout";

	/** The period of time to wait for a response when joining a channel before assuming
	    that there is no existing co-ordinator and appointing ourselves to that lofty title */
	public static String PROP_JGROUPS_GMS_TIMEOUT = "portico.jgroups.gms.jointimeout";

	///// auditor settings
	/** Whether or not the auditor is enabled */
	public static String PROP_JGROUPS_AUDITOR_ENABLED = "portico.jgroups.auditor.enabled";

	/** Auditor filtering settings */
	public static String PROP_JGROUPS_AUDITOR_FILTER_DIR = "portico.jgroups.auditor.filter.direction";
	public static String PROP_JGROUPS_AUDITOR_FILTER_MSG = "portico.jgroups.auditor.filter.message";
	public static String PROP_JGROUPS_AUDITOR_FILTER_FOM = "portico.jgroups.auditor.filter.fomtype";

	///// jgroups properties /////////////////////////////////////////////////////////////////
	/** The amount of time (in milliseconds) to wait for a response to a request, defaults to 1000,
	    controllable through system property {@link #PROP_JGROUPS_TIMEOUT}  */
	public static long RESPONSE_TIMEOUT =
		Long.parseLong(System.getProperty(PROP_JGROUPS_TIMEOUT,"1000") );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * @return The jgroups join timeout to use in milliseconds. Defaults to 5000.
	 */
	public static final long getJoinTimeout()
	{
		return Integer.parseInt( System.getProperty(PROP_JGROUPS_JOIN_TIMEOUT,"5000") );
	}

	/**
	 * @return True if JGroups channels should be set up to use Daemon threads for all processing
	 *         so that they don't keep systems alive and active when not requied. False otherwise.
	 *         Default is true.
	 */
	public static final boolean useDaemonThreads()
	{
		return Boolean.valueOf( System.getProperty(PROP_JGROUPS_DAEMON,"true") );
	}

	/**
	 * @return True if the Auditor has been turned on in configuration, false otherwise.
	 *         Default is false.
	 */
	public static final boolean isAuditorEnabled()
	{
		return Boolean.valueOf( System.getProperty(PROP_JGROUPS_AUDITOR_ENABLED,"false") );
	}

	/**
	 * @return A list of all the configured direction filters. If no configuration is provided,
	 *         list will contain one entry - "all". These will be converted to lower case before
	 *         being returned.
	 */
	public static List<String> getAuditorDirectionFilters()
	{
		String value = System.getProperty( PROP_JGROUPS_AUDITOR_FILTER_DIR,"all");
		return explode( value, "," );
	}
	
	/**
	 * @return A list of all the configured message filters. If no configuration is provided,
	 *         list will contain one entry - "all". These will be converted to lower case before
	 *         being returned.
	 */
	public static List<String> getAuditorMessageFilters()
	{
		String value = System.getProperty( PROP_JGROUPS_AUDITOR_FILTER_MSG,"all");
		return explode( value, "," );
	}
	
	/**
	 * @return A list of all the configured fomtype filters. If no configuration is provided,
	 *         list will contain one entry - "all". These will be converted to lower case before
	 *         being returned.
	 */
	public static List<String> getAuditorFomtypeFilters()
	{
		String value = System.getProperty( PROP_JGROUPS_AUDITOR_FILTER_FOM,"all");
		return explode( value, "," );
	}
	
	private static List<String> explode( String string, String delimiter )
	{
		List<String> list = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer( string, delimiter );
		while( tokenizer.hasMoreTokens() )
		{
			String temp = tokenizer.nextToken().trim();
			if( temp.equals("") )
				continue;
			else
				list.add( temp );
		}
		
		return list;
	}
	
}
