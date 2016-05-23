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
package org.portico.utils.logging;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;

public class Log4jConfigurator
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static boolean LOGGING_CONFIGURED = false;
	
	/** The default pattern to use in layouts */
	public static String DEFAULT_PATTERN = "%-5p [%t] %c: %x%m%n";
	
	/** Flag to tell setConsoleLevel() methods to create color console appenders */
	//public static boolean USE_COLOR_CONSOLE = false;
	
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
	 * Convenience method that takes the given level as a string, validates that it is actually
	 * a Log4j level, and sets it on all the loggers.
	 */
	public static void setLevel( String level, String... loggers ) throws JConfigurationException
	{
		Level log4jLevel = validateLevel( level );
		for( String loggerName : loggers )
		{
			// get the logger, thus instantiating it if it doesn't already exist
			Logger logger = Logger.getLogger( loggerName );
			
			// check to see if it has any appenders anywhere in the hierarchy. If not, we'll
			// want to add one, otherwise there will be no logging output!
			boolean appenderFound = false;
			Logger temp = logger;
			while( temp != null )
			{
				if( temp.getAllAppenders().hasMoreElements() )
				{
					appenderFound = true;
					break;
				}
				
				temp = (Logger)temp.getParent();
			}
			
			if( appenderFound == false )
				attachConsoleAppender( logger );
			
			logger.setLevel( log4jLevel );
		}
	}
	
	/**
	 * Redirects portico's log file output to a file at the given location.
	 */
	public static void redirectFileOutput( String logfile, boolean append )
		throws JConfigurationException
	{
		// remove any existing file appenders from the portico logger
		removeFileAppenders();
		
		try
		{
    		// create the appender
    		PatternLayout layout = new PatternLayout( DEFAULT_PATTERN );
    		FileAppender appender = new FileAppender( layout, logfile, false );
    		
    		// attach the appender
    		Logger porticoLogger = Logger.getLogger( "portico" );
    		porticoLogger.addAppender( appender );
    		
    		// attach the same appender to the jgroups logger
    		Logger jgroupsLogger = Logger.getLogger( "org.jgroups" );
    		jgroupsLogger.addAppender( appender );
		}
		catch( IOException ioex )
		{
			throw new JConfigurationException( ioex );
		}
	}
	
	private static void removeFileAppenders()
	{
		Logger logger = Logger.getLogger( "portico" );
		Enumeration<?> appenders = logger.getAllAppenders();
		HashSet<Appender> toRemove = new HashSet<Appender>();
		while( appenders.hasMoreElements() )
		{
			Appender appender = (Appender)appenders.nextElement();
			if( appender instanceof FileAppender )
				toRemove.add( appender );
		}

		for( Appender appender : toRemove )
		{
			logger.removeAppender( appender );
			appender.close();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Bootstrapping Methods ////////////////////////////////// 
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Set up the Log4j Logging environment for Portico
	 */
	public static void bootstrapLogging() throws JConfigurationException
	{
		// only run our configuration once
		if( LOGGING_CONFIGURED )
			return;

		// fetch and validate the levels for the various loggers
		Level porticoLevel = validateLevel(
		      System.getProperty(PorticoConstants.PROPERTY_PORTICO_LOG_LEVEL,
		                         PorticoConstants.PORTICO_LOG_LEVEL) );
		Level containerLevel = validateLevel(
		      System.getProperty( PorticoConstants.PROPERTY_CONTAINER_LOG_LEVEL,
		                          PorticoConstants.CONTAINER_LOG_LEVEL) );
	
		// if all logging is turned off, don't bother doing anything
		if( porticoLevel == Level.OFF && containerLevel == Level.OFF )
			return;
		
		// set up the console and file loggers
		enableConsole();
		enableFile();

		// set the level on the loggers as appropritate to the configuration
		Logger.getLogger("portico").setLevel( porticoLevel );
		Logger.getLogger("portico.container").setLevel( containerLevel );
	}
	
	private static final void enableConsole()
	{
		attachConsoleAppender( Logger.getLogger("portico") );
		attachConsoleAppender( Logger.getLogger("org.jgroups") );
	}
	
	private static final void attachConsoleAppender( Logger logger )
	{
		attachConsoleAppender( logger, DEFAULT_PATTERN );
	}
	
	private static final void attachConsoleAppender( Logger logger, String pattern )
	{
		// create the appender
		PatternLayout layout = new PatternLayout( pattern );
		ConsoleAppender appender = new ConsoleAppender( layout, ConsoleAppender.SYSTEM_OUT );
		appender.setThreshold( Level.TRACE ); // output restricted at logger level, not appender

		// attach the appender
		logger.addAppender( appender );
	}
	
	private static final void enableFile() throws JConfigurationException
	{
		// get the log file location
		String logfile = System.getProperty(PorticoConstants.PROPERTY_LOG_DIR,"logs")+"/portico.log";
		
		redirectFileOutput( logfile, true );
	}
	
	/**
	 * Validates the given String, ensuring that it identifies a proper log4j level. If it doesn't
	 * an exception is thrown. If it does, the appropriate <code>Level</code> object is returned.
	 */
	private static Level validateLevel( String level ) throws RuntimeException
	{
		if( level.equalsIgnoreCase("ALL") )
			return Level.ALL;
		else if( level.equalsIgnoreCase("TRACE") )
			return Level.TRACE;
		else if( level.equalsIgnoreCase("DEBUG") )
			return Level.DEBUG;
		else if( level.equalsIgnoreCase("INFO") )
			return Level.INFO;
		else if( level.equalsIgnoreCase("WARN") )
			return Level.WARN;
		else if( level.equalsIgnoreCase("ERROR") )
			return Level.ERROR;
		else if( level.equalsIgnoreCase("FATAL") )
			return Level.FATAL;
		else if( level.equalsIgnoreCase("OFF") )
			return Level.OFF;
		else
			throw new RuntimeException( "Log Level [" + level + "] not valid" );
	}
}
