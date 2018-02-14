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
package org.portico2.common.logging;

import java.util.HashSet;

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
import org.portico.lrc.compat.JConfigurationException;

/**
 * This class has two roles. Firstly, it takes instances of {@link Log4jConfiguration} objects
 * and activates them, setting up the appropriate appender configurations and so on. Note that
 * each configuration can only be applied once. The base name ({@link Log4jConfiguration#getBase()})
 * will be stored for each configuration that is activated. Subsequent calls to activate a 
 * configuration with the same name will be ignored.
 * 
 * Secondly, it can be used to make runtime changes after a configuration has been activated.
 * This includes things such as changing the level of a logger or adjusting/redirecting its output.
 */
public class Log4jConfigurator
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static boolean BOOTSTRAPPED = false;

	// The ids/bases of all the configurations we have applied, so we don't double up
	private static HashSet<String> APPLIED = new HashSet<>();

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
	public static void setLevel( String level, String... loggers )
	{
		Level log4jLevel = validateLevel( level );
		for( String loggerName : loggers )
			Configurator.setLevel( loggerName, log4jLevel );
	}
	
	/**
	 * Redirect log file output to a file at the given location.
	 */
	public static void redirectFileOutput( Log4jConfiguration settings )
	{
		redirectFileOutput( settings.getBase(),
		                    settings.getFilePath().getAbsolutePath(),
		                    settings.getFilePattern(),
		                    settings.getFileThreshold() );
	}
	
	public static void redirectFileOutput( String logfile, boolean append )
		throws JConfigurationException
	{
		redirectFileOutput( "portico", logfile, Log4jConfiguration.DEFAULT_PATTERN, "TRACE" );
	}
	
	/**
	 * Redirect log file output to a file at the given location.
	 */
	public static void redirectFileOutput( String base, String logfile, String pattern, String threshold )
	{
		// remove the existing file appender from the root logger
		LoggerContext context = (LoggerContext)LogManager.getContext( false );
		Configuration configuration = context.getConfiguration();
		configuration.getRootLogger().removeAppender( base+"-file" );

		// create the new file appender
		PatternLayout layout = PatternLayout.newBuilder().withPattern( pattern ).build();
		FileAppender appender = FileAppender.newBuilder().withName( base+"-file" )
		                                                 .withFileName( logfile )
		                                                 .withLayout( layout )
		                                                 .withAppend( false )
		                                                 .setConfiguration( configuration )
		                                                 .build();
		
		// start the new appender and add it to the root logger
		appender.start();
		configuration.getRootLogger().addAppender( appender, Level.toLevel(threshold), null );
		
		// update all loggers in the context, not sure if this is needed but can't hurt
		context.updateLoggers();	}

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
		FileAppender appender = FileAppender.newBuilder().withName( id+"-file" )
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
	private static void bootstrap()
	{
		// only run our configuration once
		if( BOOTSTRAPPED )
			return;

		// build the root configuration
		ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
		builder.setStatusLevel( Level.WARN );
		builder.setConfigurationName( "root" );
		builder.add( builder.newRootLogger("OFF") );
		
		// initialize the logger context and we're ready to go!
		LoggerContext context = Configurator.initialize( builder.build() );

		// remove the stupid default console logger
		// --no longer needed-- we are adding an empty root logger above now
		// --needed again-- when using TestNG for some reason I need to to prevent two appenders
		// --someimtes!--   needed when running unit tests but not regularly :( FIXME
//		LoggerConfig rootConfig = context.getConfiguration().getRootLogger();
//		String defaultName = rootConfig.getAppenders().keySet().iterator().next();
//		rootConfig.removeAppender( defaultName );
//		context.updateLoggers();

		BOOTSTRAPPED = true;
	}

	/**
	 * Activates the given configuration for the logger identified in its base name
	 * {@link Log4jConfiguration#getBase()}). If a configuration with that name has
	 * already been activated, no changes will be made.
	 * 
	 * @param settings The configuration settings to activate.
	 */
	public static void activate( Log4jConfiguration settings )
	{
		//  make sure we haven't already applied settings for this base yet
		if( APPLIED.contains(settings.getBase()) )
			return;
		
		bootstrap();

		// console appender
		if( settings.isConsoleOn() )
			turnConsoleOn( settings );
		
		// file appender
		if( settings.isFileOn() )
			turnFileOn( settings );
		
		// set the general levels
		if( settings.getBase().trim().equalsIgnoreCase("root") )
			setLevel( settings.getLevel(), "" );
		else
			setLevel( settings.getLevel(), settings.getBase() );
		
		APPLIED.add( settings.getBase() );
	}
	
	private static void turnConsoleOn( Log4jConfiguration settings )
	{
		// pattern, target, threshold
		String base      = settings.getBase();
		String pattern   = settings.getConsolePattern();
		String target    = settings.getConsoleTarget().toString();
		String threshold = settings.getConsoleThreshold();
		
		// get a reference to the logger context and configuration
		LoggerContext context = (LoggerContext)LogManager.getContext( false );
		Configuration configuration = context.getConfiguration();

		// create the appender
		PatternLayout layout = PatternLayout.newBuilder().withPattern( pattern ).build();
		ConsoleAppender appender = ConsoleAppender.newBuilder().withName( target )
		                                                       .withLayout( layout )
		                                                       .setConfiguration( configuration )
		                                                       .build();
		
		// attach the appender
		appender.start();
		configuration.getLoggerConfig(base).addAppender( appender, Level.toLevel(threshold), null );
		
		// update all loggers in the context, not sure if this is needed but can't hurt
		context.updateLoggers();
	}

	private static void turnFileOn( Log4jConfiguration settings )
	{
		// pattern, file, threshold
		String base      = settings.getBase();
		String pattern   = settings.getFilePattern();
		String logfile   = settings.getFilePath().getAbsolutePath();
		String threshold = settings.getFileThreshold();
		
		// create the appender for the logger
		LoggerContext context = (LoggerContext)LogManager.getContext( false );
		Configuration configuration = context.getConfiguration();

		// create the appender
		PatternLayout layout = PatternLayout.newBuilder().withPattern( pattern ).build();
		FileAppender appender = FileAppender.newBuilder().withName( base+"-file" )
		                                                 .withFileName( logfile )
		                                                 .withLayout( layout )
		                                                 .withAppend( false )
		                                                 .setConfiguration( configuration )
		                                                 .build();

		// attach the appender
		appender.start();
		configuration.getLoggerConfig(base).addAppender( appender, Level.toLevel(threshold), null );
		
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
