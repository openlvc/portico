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

import org.apache.logging.log4j.Logger;
import org.portico.lrc.PorticoConstants;
import org.portico.utils.SystemInformation;
import org.portico2.common.configuration.commandline.Argument;
import org.portico2.common.configuration.commandline.CommandLine;

/**
 * Logs information about Portico on startup. Pulled out into its own class so that we don't
 * have to pollute the {@link RID} class with logging and formatting information.
 */
public class StartupLogger
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

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
	public static void logGenericStartupHeader( Logger logger, org.portico2.common.configuration.RID rid )
	{
		// get the system information
		SystemInformation info = SystemInformation.LOCAL;
		logger.info( "" );
		logger.info( "##########################################################" );
		logger.info( "#                   Portico Open RTI                     #" );
		logger.info( "#            Welcome to Portico for the HLA!             #" );
		logger.info( "#                                                        #" );
		logger.info( "#      Portico is distributed by under the terms of      #" );
		logger.info( "#     the Apache Software License (Version 2). For a     #" );
		logger.info( "#    copy of the license, see the LICENSE file included  #" );
		logger.info( "#         in your Portico installation directory.        #" );
		logger.info( "#                                                        #" );
		logger.info( "##########################################################" );
		logger.info( "#                                                        #" );
		logger.info( "#                    System Information                  #" );
		logger.info( "#                                                        #" );
		logger.info( pad( "# Portico Version:  " + PorticoConstants.RTI_VERSION ) );
		logger.info( pad( "# Platform:         " + info.getPlatform() ) );
		logger.info( pad( "# CPUs:             " + info.getCPUCount() ) );
		logger.info( pad( "# Operating System: " + info.getOS() ) );
		logger.info( pad( "#                   " + info.getOSVersion() ) );
		logger.info( pad( "# Java Version:     " + info.getJavaVersion() ) );
		logger.info( pad( "# Java Vendor:      " + info.getJavaVendor() ) );
		logger.info( "#                                                        #" );
		logger.info( pad( "# Startup Time:     "+info.getStartupTime() ) );
		logger.info( pad( "# RID File:         "+rid.getRidPath()) );
		logger.info( pad( "# Log Level:        "+rid.getLog4jConfiguration().getLevel()) );
		logger.info( "#                                                        #" );
		logger.info( "##########################################################" );
		logger.info( "" );
		
		logConfigurationInformation( logger, rid );
	}

	private static String pad( String text )
	{
		// at the moment the length of the main delimiters for the licence info
		// is 54 characters. Pad it out to that
		int count = text.length();
		StringBuffer buf = new StringBuffer( text );
		while( count < 57 )
		{
			buf.append( " " );
			++count;
		}

		buf.append( "#" );
		return buf.toString();
	}
	
	private static void logConfigurationInformation( Logger logger, org.portico2.common.configuration.RID rid )
	{
		// log the command line arguments
		CommandLine commandline = rid.getCommandLine();
		if( commandline.isEmpty() == false )
		{
			int maxlength = 0;
			for( Argument argument : commandline.keySet() )
			{
				if( argument.getName().length() > maxlength )
					maxlength = argument.getName().length();
			}
			
			logger.info( "Command line argument overrides: " );
			String formatString = "     %" + maxlength + "s = %s";
			for( Argument argument : commandline.keySet() )
			{
				logger.info( String.format(formatString,
				                           argument.getName(),
				                           commandline.get(argument)) );
			}
			logger.info( "" );
		}
		
		// If the RID file doesn't happen to exist, log that fact.
		if( rid.getRidFile().exists() == false )
		{
			logger.info( "Configuration file not found: "+rid.getRidFile().getAbsolutePath() );
			logger.info( "Using configuration defaults" );
		}

		/////////////////////////////////////////////////////////////
		// log all the configuration properties for debugging   /////
		/////////////////////////////////////////////////////////////
		// Get out early unless this is useful to print
		if( logger.isDebugEnabled() == false )
			return;
		
		// firstly, to make printing nice, figure out the longest property name
//		List<String> keys = new ArrayList<String>(); // sort later and then use this to pull out in order
//		int longestName = 0;
//		for( Object key : rid.getConfigurationProperties().keySet() )
//		{
//			if( key.toString().length() > longestName )
//				longestName = key.toString().length();
//			
//			keys.add( key.toString() );
//		}
//		
//		logger.debug( "" );
//		logger.debug( " ==============================" );
//		logger.debug( " == Configuration Properties ==" );
//		logger.debug( " ==============================" );
//		
//		//String formatString = " %"+longestName+"s = %s %s";
//		String formatString = " %s = %s %s";
//		Collections.sort( keys );
//		for( String key : keys )
//		{
//			// was this specified on the command line? If so we'll include that info
//			boolean isCommandLine = false;
//			for( Argument argument : commandline.keySet() )
//			{
//				if( argument.getProperty().equals(key.toString()) )
//					isCommandLine = true;
//			}
//			
//			String value = rid.getConfigurationProperties().get(key).toString();
//			String cline = isCommandLine ? " (command line)" : "";
//			logger.debug( String.format(formatString,key.toString(),value,cline) );
//		}
		
		logger.debug( "" );	
	}
}
