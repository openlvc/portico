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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;

public class Log4jConfigurator
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static boolean LOGGING_CONFIGURED = false;
	
	/** The default pattern to use in layouts */
	public static String DEFAULT_PATTERN = "%d{ABSOLUTE} %-5p [%t] %c: %m%n";

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
			Configurator.setLevel( loggerName, log4jLevel );
		}
	}
	
	/**
	 * Redirects portico's log file output to a file at the given location.
	 */
	public static void redirectFileOutput( String logfile, boolean append )
		throws JConfigurationException
	{
		// remove the existing file appender from the root logger
		LoggerContext context = (LoggerContext)LogManager.getContext( false );
		Configuration configuration = context.getConfiguration();
		configuration.getRootLogger().removeAppender( "file" );

		// create the new file appender
		PatternLayout layout = PatternLayout.newBuilder().withPattern( DEFAULT_PATTERN ).build();
		FileAppender appender = FileAppender.newBuilder().withName( "file" )
		                                                 .withFileName( logfile )
		                                                 .withLayout( layout )
		                                                 .withAppend( append )
		                                                 .setConfiguration( configuration )
		                                                 .build();
		
		// start the new appender and add it to the root logger
		appender.start();
		configuration.getRootLogger().addAppender( appender, Level.TRACE, null );
		
		// update all loggers in the context, not sure if this is needed but can't hurt
		context.updateLoggers();
	}

	/**
	 * Creates a new FileAppender with the given <code>id</code>, log file and logging pattern and
	 * adds it to all of the given loggers
	 * 
	 * @param id The unique ID for the appender so we can identy and remove it later
	 * @param logfile The file to log to
	 * @param pattern The pattern to log with
	 * @param loggers The names of all the loggers to attach the file appender to
	 */
	public static void addLogFileForLogger( String id, String logfile, String pattern, String... loggers )
	{
		// create the appender for the logger
		LoggerContext context = (LoggerContext)LogManager.getContext( false );
		Configuration configuration = context.getConfiguration();

		PatternLayout layout = PatternLayout.newBuilder().withPattern( pattern ).build();
		FileAppender appender = FileAppender.newBuilder().withName( id )
		                                                 .withFileName( logfile )
		                                                 .withLayout( layout )
		                                                 .withAppend( false )
		                                                 .setConfiguration( configuration )
		                                                 .build();

		// start the appender; needs to happen before we add it
		appender.start();
		
		// attach the appender to all loggers
		for( String loggerName : loggers )
		{
			// create the new logger so that is has a configuration
			Logger temp = LogManager.getFormatterLogger( loggerName );
			configuration.getLoggerConfig(loggerName).addAppender( appender, Level.TRACE, null );
		}
		
		// update all loggers in the context, not sure if this is needed but can't hurt
		context.updateLoggers();
	}

	/**
	 * Remove the FileAppender with the given <code>id</code> from all the loggers specified
	 * in the given list.
	 * 
	 * @param id The ID of the appender to remove
	 * @param loggers The loggers to remove the appender from
	 */
	public static void removeLogFileForLogger( String id, String... loggers )
	{
		// remove the existing file appender from the root logger
		LoggerContext context = (LoggerContext)LogManager.getContext( false );
		Configuration configuration = context.getConfiguration();
		
		for( String loggerName : loggers )
		{
			LoggerConfig loggerConfig = configuration.getLoggerConfig( loggerName );
			if( loggerConfig != null && loggerConfig != configuration.getRootLogger() )
				loggerConfig.removeAppender( id );
		}
		
		// update all loggers in the context, not sure if this is needed but can't hurt
		context.updateLoggers();		
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Bootstrapping Methods ////////////////////////////////// 
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Set up the Log4j Logging environment for Portico
	 */
	public static void bootstrapLogging()
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
		
		// build the configuration
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		builder.setStatusLevel( Level.WARN );
		builder.setConfigurationName( "portico" );

		// create the root logger and attach appenders
		//RootLoggerComponentBuilder rootBuilder = builder.newRootLogger( /*level*/ porticoLevel );
		//builder.add( rootBuilder );
		
		// initialize the logger context and we're ready to go!
		LoggerContext context = Configurator.initialize( builder.build() );
	
		// remove the stupid default console logger
		LoggerConfig rootConfig = context.getConfiguration().getRootLogger();
		String defaultName = rootConfig.getAppenders().keySet().iterator().next();
		rootConfig.removeAppender( defaultName );
		context.updateLoggers();

		// turn the console appender on
		turnConsoleOn();
		turnFileOn();
		
		// set the levels for the default loggers that we want 
		setLevel( porticoLevel.toString(), "portico" );
		setLevel( containerLevel.toString(), "portico.container" );

		LOGGING_CONFIGURED = true;
	}
	
	private static void turnConsoleOn()
	{
		// get a reference to the logger context and configuration
		LoggerContext context = (LoggerContext)LogManager.getContext( false );
		Configuration configuration = context.getConfiguration();

		// create the appender
		PatternLayout layout = PatternLayout.newBuilder().withPattern( DEFAULT_PATTERN ).build();
		ConsoleAppender appender = ConsoleAppender.newBuilder().withName( "stdout" )
		                                                       .withLayout( layout )
		                                                       .setConfiguration( configuration )
		                                                       .build();
		
		// attach the appender
		appender.start();
		configuration.getRootLogger().addAppender( appender, Level.TRACE, null );
		
		// update all loggers in the context, not sure if this is needed but can't hurt
		context.updateLoggers();
	}

	private static void turnFileOn()
	{
		// create the appender for the logger
		LoggerContext context = (LoggerContext)LogManager.getContext( false );
		Configuration configuration = context.getConfiguration();

		// create the appender
		String logfile = System.getProperty(PorticoConstants.PROPERTY_LOG_DIR,"logs")+"/portico.log";
		PatternLayout layout = PatternLayout.newBuilder().withPattern( DEFAULT_PATTERN ).build();
		FileAppender appender = FileAppender.newBuilder().withName( "file" )
		                                                 .withFileName( logfile )
		                                                 .withLayout( layout )
		                                                 .withAppend( false )
		                                                 .setConfiguration( configuration )
		                                                 .build();

		// attach the appender
		appender.start();
		configuration.getRootLogger().addAppender( appender, Level.TRACE, null );
		
		// update all loggers in the context, not sure if this is needed but can't hurt
		context.updateLoggers();
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
