/*
 *   Copyright 2015 The Portico Project
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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.portico.lrc.compat.JConfigurationException;

/**
 * All configuration information is stored in system properties as keys. This class
 * provides statics that can be used to identify the specific keys.
 */
public class Configuration
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
	public static final String PROP_JGROUPS_GMS_TIMEOUT = "portico.jgroups.gms.jointimeout";

	///// auditor settings
	/** Whether or not the auditor is enabled */
	public static final String PROP_JGROUPS_AUDITOR_ENABLED = "portico.jgroups.auditor.enabled";
	public static final String PROP_JGROUPS_AUDITOR_DETAILS = "portico.jgroups.auditor.details";

	/** Auditor filtering settings */
	public static final String PROP_JGROUPS_AUDITOR_FILTER_DIR = "portico.jgroups.auditor.filter.direction";
	public static final String PROP_JGROUPS_AUDITOR_FILTER_MSG = "portico.jgroups.auditor.filter.message";
	public static final String PROP_JGROUPS_AUDITOR_FILTER_FOM = "portico.jgroups.auditor.filter.fomtype";

	///// jgroups properties /////////////////////////////////////////////////////////////////
	/** The amount of time (in milliseconds) to wait for a response to a request, defaults to 1000,
	    controllable through system property {@link #PROP_JGROUPS_TIMEOUT}  */
	public static long RESPONSE_TIMEOUT =
		Long.parseLong(System.getProperty(PROP_JGROUPS_TIMEOUT,"1000") );

	
	///// wan properties /////////////////////////////////////////////////////////////////////
	public static final String PROP_JGROUPS_WAN_ENABLED = "portico.wan.enabled";
	public static final String PROP_JGROUPS_WAN_ROUTER  = "portico.wan.router";

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
	 * @return An unvalidated log level from the RID return as a String. Returns "OFF" if not set.
	 */
	public static final String getLogLevel()
	{
		return System.getProperty( PROP_JGROUPS_LOGLEVEL, "OFF" );
	}
	
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
	 * @return True if the Auditor has been set to "summary-only" mode. This happens when
	 *         a value of "summary" is set in the RID file. A value of "full" in the RID
	 *         file will cause this to return false. Any other value will cause a configuration
	 *         exception to be thrown.
	 *         
	 *         NOTE: This is only expected to called if the Auditor is enabled. Without the
	 *               auditor enabled, this setting should have no effect.
	 *         
	 * @throws JConfigurationException If the provided value is unknown
	 */
	public static final boolean isAuditorSummaryOnly() throws JConfigurationException
	{
		String originalValue = System.getProperty( PROP_JGROUPS_AUDITOR_DETAILS, "full" );
		String value = originalValue.toLowerCase().trim();
		
		// Check to see if we have any malformed data. This doesn't happen when the RID loads,
		// so we should do it now.
		if( value.equals("summary") )
		{
			return true;
		}
		else if( value.equals("full") )
		{
			return false;
		}
		else
		{
			throw new JConfigurationException( "RID property ["+PROP_JGROUPS_AUDITOR_DETAILS+
			                                   "] was set to ["+originalValue+
				                               "]: Valid values are \"full\" or \"summary\"" );
		}
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

	/**
	 * @return True if the wan mode has been enabled in the RID, false otherwise
	 */
	public static boolean isWanEnabled()
	{
		return Boolean.valueOf( System.getProperty(PROP_JGROUPS_WAN_ENABLED,"false") ); 
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

	/**
	 * Return the parsed, validated IP & Port of the WAN Router configuration property
	 * contained in the RID. Throws an exception if the string is not in an appropriate
	 * format from one of the following:
	 * 
	 *   - 111.111.111.111
	 *   - 111.111.111.111:22222
	 *   - hostname
	 *   - hostname:port
	 */
	public static InetSocketAddress getWanRouter()
	{
		String value = System.getProperty( PROP_JGROUPS_WAN_ROUTER, "127.0.0.1:23114" );
		String host = "127.0.0.1";
		int port = 23114;
		if( value.contains(":") )
		{
			int indexOfSeparator = value.indexOf( ":" );
			host = value.substring( 0, indexOfSeparator );
			port = Integer.parseInt( value.substring(indexOfSeparator+1) );
		}
		else
		{
			host = value;
		}

		try
		{
			return new InetSocketAddress( InetAddress.getByName(host), port );
		}
		catch( Exception e )
		{
			throw new JConfigurationException( "Error parsing WAN Router address", e );
		}
	}
}
