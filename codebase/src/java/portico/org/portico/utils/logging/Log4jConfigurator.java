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

import java.util.Collection;
import java.util.HashSet;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;
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
		// Level log4jLevel = validateLevel( level );
		for( String loggerName : loggers )
		{
			// get the logger, thus instantiating it if it doesn't already exist
			// Logger logger = LogManager.getLogger( loggerName );
			
			// check to see if it has any appenders anywhere in the hierarchy. If not, we'll
			// want to add one, otherwise there will be no logging output!
			// boolean appenderFound = false;
			// Logger temp = logger;
			// while( temp != null )
			// {
			// 	if( temp.getAllAppenders().hasMoreElements() )
			// 	{
			// 		appenderFound = true;
			// 		break;
			// 	}
				
			// 	temp = (Logger)temp.getParent();
			// }
			
			// if( appenderFound == false )
			// 	attachConsoleAppender( logger );
			
			Configurator.setLevel( loggerName, level );
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
		
		// create the appender
		PatternLayout layout = PatternLayout.newBuilder().withPattern( DEFAULT_PATTERN ).build();
		DefaultRolloverStrategy rolloverStrategy = DefaultRolloverStrategy.newBuilder().withMax("2").build();
		SizeBasedTriggeringPolicy triggerPolicy = SizeBasedTriggeringPolicy.createPolicy( "10MB" );
		RollingFileAppender appender = RollingFileAppender.newBuilder()
		                                                  .setLayout( layout )
		                                                  .withFileName( logfile )
		                                                  .withStrategy( rolloverStrategy )
		                                                  .withPolicy( triggerPolicy )
		                                                  .withAppend( append )
		                                                  .build();
		appender.start();
	
		// attach the appender
		Logger porticoLogger = LogManager.getLogger( "portico" );
		((org.apache.logging.log4j.core.Logger)porticoLogger).addAppender( appender );
		
		// attach the same appender to the jgroups logger
		Logger jgroupsLogger = LogManager.getLogger( "org.jgroups" );
		((org.apache.logging.log4j.core.Logger)jgroupsLogger).addAppender( appender );
	}
	
	private static void removeFileAppenders()
	{
		org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger)LogManager.getLogger( "portico" );
		Collection<Appender> appenders = logger.getAppenders().values();
		HashSet<Appender> toRemove = new HashSet<>();
		for( Appender appender : appenders )
		{
			if( appender instanceof FileAppender )
				toRemove.add( appender );
		}

		for( Appender appender : toRemove )
		{
			logger.removeAppender( appender );
			appender.stop();
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

		// set the level on the loggers as appropriate to the configuration
		Configurator.setLevel( "portico", porticoLevel );
		Configurator.setLevel( "portico.container", containerLevel );
	}
	
	private static final void enableConsole()
	{
		attachConsoleAppender( LogManager.getLogger("portico") );
		attachConsoleAppender( LogManager.getLogger("org.jgroups") );
	}
	
	private static final void attachConsoleAppender( Logger logger )
	{
		PatternLayout layout = PatternLayout.newBuilder().withPattern( DEFAULT_PATTERN ).build();
		ThresholdFilter logFilter = ThresholdFilter.createFilter( Level.TRACE, Result.ACCEPT, Result.DENY );
		
		// create the appender
		ConsoleAppender appender = ConsoleAppender.newBuilder()
		                                          .setLayout( layout )
		                                          .setTarget( ConsoleAppender.Target.SYSTEM_OUT )
		                                          .setFilter( logFilter )
		                                          .build();
		
		// attach the appender
		((org.apache.logging.log4j.core.Logger)logger).addAppender( appender );
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
