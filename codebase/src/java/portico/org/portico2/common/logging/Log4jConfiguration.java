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

import java.io.File;

import org.apache.logging.log4j.Level;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.utils.logging.Log4jConfigurator;
import org.portico2.common.PorticoConstants;

/**
 * Defines a logging configuration that can be applied by the {@link Log4jConfigurator}.
 * These configurations can set up whether or not console and/or file logging is used, the
 * various properties of those loggers, the threshold of messages that should be let through
 * to those loggers and so on.
 * 
 * Each configuration has a "base" logger that it applies to. When appyling a config, the
 * {@link Log4jConfigurator} will create console and file appenders to it, so care must be
 * taken if applying multiple configurations at different levels due to inheritence (messages
 * sent to a logger will be sent to all appenders of parent loggers).
 * 
 * To apply a configuration, create and populate it, then pass it to {@link Log4jConfigurator}.
 * If changes are needed *after configuration*, those changes need to be made through the
 * configurator itself. Updating an instance of this object and re-applying the configuration
 * will _not_ change the setup, but rather add to it.
 */
public class Log4jConfiguration
{
	//----------------------------------------------------------
	//                      ENUMERAITONS
	//----------------------------------------------------------
	public enum Console
	{
		STD_OUT, STD_ERR;
		public String toString() { return this == STD_OUT ? "stdout" : "stderr"; }
	};

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	//public static String DEFAULT_PATTERN = "%d{ABSOLUTE} %-5p [%t] %c: %m%n";
	public static String DEFAULT_PATTERN = "%d{ABSOLUTE} %-5p %c: %m%n";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// General Settings
	private String base;
	private String level;
	private String pattern;

	// Console Settings
	private boolean consoleOn;
	private String  consoleThreshold;
	private Console consoleTarget;
	private String  consolePattern;
	
	// File Settings
	private boolean fileOn;
	private File    fileDir;
	private String  fileName;
	private String  fileThreshold;
	private String  filePattern;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create a new configuration object with the base logger to which it will be applied.
	 * To make this configuration active, call {@link Log4jConfigurator#apply......}.
	 * 
	 * Note that by default, the file logger is turned OFF. You will need to activate it and
	 * specify the path you want to use.
	 * 
	 * @param base The base logger this configuration will apply to
	 */
	public Log4jConfiguration( String base )
	{
		if( base == null )
			base = "";
		
		// General Settings
		this.base = base;
		this.level = PorticoConstants.PORTICO_LOG_LEVEL;
		this.pattern = DEFAULT_PATTERN;
		
		// Console Settings
		this.consoleOn = true;
		this.consoleThreshold = "TRACE";
		this.consoleTarget = Console.STD_OUT;
		this.consolePattern = null;
		
		// File Settings
		this.fileOn = false;
		this.fileDir = new File( "./" );
		this.fileName = "portico.log";
		this.fileThreshold = "TRACE";
		this.filePattern = null;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//////////////////////////////////////////////////////////////////////////////////
	///  General Configuration   /////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////

	/**
	 * Set the base logger against which all our configuration will be applied.
	 * Be wary when applying multiple configurations within the same hierarchy.
	 * For example, if one configuration has a base of `portico` and another of
	 * `portico.other` and the log to the same file or console you will end up
	 * with doubled-up messages.
	 * 
	 * @param base Dot-separated base logger for which we want this configuration to apply to.
	 */
	public void setBase( String base )
	{
		if( base != null )
			this.base = base;
	}

	/**
	 * @return The base logger that this configuration will be applied to.
	 */
	public String getBase()
	{
		return this.base;
	}
	
	/**
	 * Set the base log level to be used. Default is "ERROR"
	 * 
	 * @param level The base level to use
	 * @throws ConfigurationException If the given level is not a valid Log4j level
	 */
	public void setLevel( String level ) throws IllegalArgumentException
	{
		if( Level.getLevel(level.toUpperCase()) == null )
			throw new IllegalArgumentException( level+" is not a valid level" );
		else
			this.level = level;
	}

	/**
	 * @return The default log level for the base logger
	 */
	public String getLevel()
	{
		return this.level;
	}

	/**
	 * Set a pattern to use for all log messages. Note that this is used as a fallback.
	 * Individual patterns can be set individually for consoles and files. If they are
	 * not set, this will be used as a fallback.
	 * 
	 * The default value defined in {@link #DEFAULT_PATTERN} will be used if a format is not set.
	 * 
	 * @param pattern A Log4j format string to use for all messages
	 */
	public void setPattern( String pattern )
	{
		if( pattern != null )
			this.pattern = pattern;
	}

	/**
	 * @return The pattern string we are using as a fallback (note that the pattern for either the
	 *         file or logger can be individually overriden).
	 */
	public String getPattern()
	{
		return this.pattern;
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	///  Console Configuration   /////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////
	public boolean isConsoleOn()
	{
		return this.consoleOn;
	}
	
	public void turnConsoleOn()
	{
		this.consoleOn = true;
	}
	
	public void turnConsoleOn( boolean on )
	{
		this.consoleOn = on;
	}
	
	public void turnConsoleOff()
	{
		this.consoleOn = false;
	}

	/**
	 * Set the threshold of message that will be let through to the console. Any message below
	 * this level will not be logged. Note that this is used in combination with the level of
	 * the logger to which the message was sent. By default, this value is `TRACE`, with the 
	 * expectation that the logger itself will be the real limiter. Any message that gets through
	 * the limits applied by a given _logger_ will be logged. 
	 * 
	 * @param level `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `FATAL`, `OFF`
	 * @throws ConfigurationException If the given level if not a valid log4j level
	 */
	public void setConsoleThreshold( String level ) throws JConfigurationException
	{
		if( Level.getLevel(level) == null )
			throw new JConfigurationException( level+" is not a valid level" );
		else
			this.consoleThreshold = level;
	}

	/**
	 * Get the level below which messages will be discarded by the console appender. By default
	 * this is `TRACE`, with the real filtering happening at the logger itself.
	 * 
	 * @return Threshold level below which messages will not be sent through to the console
	 */
	public String getConsoleThreshold()
	{
		return this.consoleThreshold;
	}
	
	public void setConsoleTarget( Console target )
	{
		this.consoleTarget = target;
	}
	
	public Console getConsoleTarget()
	{
		return this.consoleTarget;
	}

	/**
	 * Set the Log4j logging pattern to use for the console specifically. This is not set by
	 * default and falls back to the value of {@link #getPattern()} if it is unset.
	 * 
	 * @param pattern the log4j pattern string to use just for the console
	 */
	public void setConsolePattern( String pattern )
	{
		if( pattern != null )
			this.consolePattern = pattern;
	}

	/**
	 * @return The pattern string used by the console. If no explicit value has been set this
	 *         will fall back to {@link #getPattern()}.
	 */
	public String getConsolePattern()
	{
		if( this.consolePattern == null )
			return this.pattern;
		else
			return this.consolePattern;
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	///  File Configuration   ////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////
	public void turnFileOn()
	{
		this.fileOn = true;
	}
	public void turnFileOff()
	{
		this.fileOn = false;
	}
	
	public boolean isFileOn()
	{
		return this.fileOn;
	}
	
	public File getFileDir()
	{
		return this.fileDir;
	}
	
	public void setFileDir( String path )
	{
		this.fileDir = new File( path );
	}
	
	public String getFileName()
	{
		return this.fileName;
	}
	
	public void setFileName( String fileName )
	{
		this.fileName = fileName;
	}
	
	public File getFilePath()
	{
		return new File( this.fileDir, this.fileName );
	}
	
	public void setFilePath( String directory, String filename )
	{
		setFileDir( directory );
		setFileName( filename );
	}
	
	public void setFilePath( String fullpath )
	{
		File file = new File( fullpath );
		setFileDir( file.getParentFile().getAbsolutePath() );
		setFileName( file.getName() );
	}

	/**
	 * Set the threshold of message that will be let through to the log file. Any message below
	 * this level will not be logged. Note that this is used in combination with the level of
	 * the logger to which the message was sent. By default, this value is `TRACE`, with the 
	 * expectation that the logger itself will be the real limiter. Any message that gets through
	 * the limits applied by a given _logger_ will be logged. 
	 * 
	 * @param level `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `FATAL`, `OFF`
	 * @throws ConfigurationException If the given level if not a valid log4j level
	 */
	public void setFileThreshold( String level )
	{
		if( Level.getLevel(level) == null )
			throw new JConfigurationException( level+" is not a valid level" );
		else
			this.fileThreshold = level;
	}

	/**
	 * Get the level below which messages will be discarded by the file appender. By default this
	 * is `TRACE`, with the real filtering happening at the logger itself.
	 * 
	 * @return Threshold level below which messages will not be sent through to the log file
	 */
	public String getFileThreshold()
	{
		return this.fileThreshold;
	}
	
	/**
	 * Set the Log4j logging pattern to use for the log file specifically. This is not set by
	 * default and falls back to the value of {@link #getPattern()} if it is unset.
	 * 
	 * @param pattern the log4j pattern string to use just for the log file
	 */
	public void setFilePattern( String pattern )
	{
		if( pattern != null )
			this.filePattern = pattern;
	}

	/**
	 * @return The pattern string used by the log file. If no explicit value has been set this
	 *         will fall back to {@link #getPattern()}.
	 */
	public String getFilePattern()
	{
		if( this.filePattern == null )
			return this.pattern;
		else
			return this.filePattern;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
