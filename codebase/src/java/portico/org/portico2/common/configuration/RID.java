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

import java.io.File;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.configuration.commandline.Argument;
import org.portico2.common.configuration.commandline.CommandLine;
import org.portico2.common.logging.Log4jConfiguration;
import org.portico2.common.utils.FileUtils;

public class RID
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final String DEFAULT_RID_FILE = "./RTI.rid";
	
	/** The system property we will look to use as our command line */
	public static final String KEY_RTI_COMMAND_LINE = "rti.commandline";
	
	// General Settings
	public static final String KEY_RID_FILE = "rid.file";
	public static final String KEY_RTI_HOME = "rti.home";
	public static final String KEY_RTI_DATA = "rti.data";
	
	// Logging Settings
	public static final String KEY_LOG_LEVEL  = "portico.loglevel";
	public static final String KEY_LOG_DIR    = "portico.logdir";
	public static final String KEY_LOG_FORMAT = "portico.logformat";
	
	// Connection Settings
	// FIXME ???
	public static final String PFX_LRC_CONNECTION  = "portico.lrc.network";
	public static final String PFX_FWD_UPSTREAM    = "portico.fwd.network.upstream";
	public static final String PFX_FWD_DOWNSTREAM  = "portico.fwd.network.downstream";

	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Log4jConfiguration log4jConfiguration;
	private Logger logger;
	private CommandLine commandline;
	private Properties unifiedProperties; // set after parsing, contains rid and cl properties
	
	// File path properties
	private String ridpath;
	private File   rtihome;
	private File   rtidata;

	// Component-Specific Configurations
	private RtiConfiguration rtiConfiguration;
	private LrcConfiguration lrcConfiguration;
	private ForwarderConfiguration fwdConfiguration;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private RID()
	{
		// Logging configuration
		this.log4jConfiguration = new Log4jConfiguration( "portico" );
		this.log4jConfiguration.setFileDir( "logs" );
		this.log4jConfiguration.setFileName( "portico.log" );
		this.logger = null; // lazy loaded

		this.commandline = null;
		this.unifiedProperties = new Properties(); // filled out at end of parsing process
		
		// File path properties
		this.ridpath = DEFAULT_RID_FILE;
		this.rtihome = null;
		this.rtidata = null;
		
		// Component-Specific Configurations
		this.rtiConfiguration = new RtiConfiguration();
		this.lrcConfiguration = new LrcConfiguration();
		this.fwdConfiguration = new ForwarderConfiguration();
	}

	/**
	 * Create a new RID using all the defaults and overridingn it with the values from the given
	 * command line. Note that this constructor will ignore any command line argument that points
	 * to a specific RID file. 
	 * @param commandline
	 * @throws JConfigurationException
	 */
	private RID( CommandLine commandline ) throws JConfigurationException
	{
		// set-up the defaults
		this();

		// parse configuration (we have no RID file to parse)
		parseConfiguration( new Properties(), commandline.asProperties() );
		
		// store the command line for later read-only reference
		this.commandline = commandline;
	}

	private RID( File ridfile, CommandLine commandline )
	{
		this();
		
		// parse the configuration
		parseConfiguration( FileUtils.loadPropertiesFile(ridfile), commandline.asProperties() );
		
		// store the location of teh RID file for later read-only reference
		this.ridpath = ridfile.getPath();
		this.commandline = commandline;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public CommandLine getCommandLine()
	{
		return this.commandline;
	}
	
	public Properties getConfigurationProperties()
	{
		return this.unifiedProperties;
	}
	
	/**
	 * @return The application logger. *WARNING* This object may not be valid until the full
	 *         logging framework has been initialized.
	 */
	public Logger getLogger()
	{
		if( this.logger == null )
			this.logger = LogManager.getLogger( "portico" ); 
		
		return this.logger;
	}

	public Log4jConfiguration getLog4jConfiguration()
	{
		return this.log4jConfiguration;
	}

	public RtiConfiguration getRtiConfiguration()
	{
		return this.rtiConfiguration;
	}
	
	public LrcConfiguration getLrcConfiguration()
	{
		return this.lrcConfiguration;
	}
	
	public ForwarderConfiguration getForwarderConfiguration()
	{
		return this.fwdConfiguration;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Path-Based Configuration Options    ////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	// Read-only
	public File getRidFile()
	{
		return new File( this.ridpath );
	}

	// Read-only
	public String getRidPath()
	{
		return this.ridpath;
	}
	
	public File getRtiHome()
	{
		return this.rtihome;
	}
	
	public void setRtiHome( File path )
	{
		this.rtihome = path;
	}
	
	public File getRtiDataDir()
	{
		return this.rtidata;
	}
	
	public void setRtiDataDir( File path )
	{
		this.rtidata = path;
	}


	////////////////////////////////////////////////////////////////////////////////////////////
	/// Configuration Loading   ////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Parse the configuration for this RID from the given ridfile properties and command line
	 * properties. Not that any pathing related to the location of the RID file should have
	 * already been determined by this point.
	 * 
	 * @param ridproperties The properties contained in the RID file (or null if no RID file was
	 *                      specified or found)
	 * @param clproperties  The command line arguments as a property set
	 */
	private void parseConfiguration( Properties ridproperties, Properties clproperties )
		throws JConfigurationException
	{
		// 1. Extract any properties that can be used as symbols in other properties
		this.rtihome = findRtiHome( ridproperties, clproperties );
		this.rtidata = findRtiData( ridproperties, clproperties );

		// 2. Replace any special symbols in the given property sets
		substituteSymbols( clproperties );
		substituteSymbols( ridproperties );
		
		// 3. Unify all the properties so that we have one set
		if( ridproperties != null )
			this.unifiedProperties.putAll( ridproperties );
		if( clproperties != null )
			this.unifiedProperties.putAll( clproperties );
		
		// 4. Process the unified set of properties
		parseProperties( this.unifiedProperties );
	}
	
	/**
	 * Parse and store any local configuration from the given property set
	 * 
	 * @param properties The properties to pull configuration information from
	 * @throws JConfigurationException If any of the configuration data is invalid
	 */
	private void parseProperties( Properties properties ) throws JConfigurationException
	{
		// Extract the properties we manage locally
		if( properties.containsKey(KEY_LOG_DIR) )
			this.log4jConfiguration.setFilePath( properties.getProperty(KEY_LOG_DIR), "portico.log" );

		if( properties.containsKey(KEY_LOG_LEVEL) )
			this.log4jConfiguration.setLevel( properties.getProperty(KEY_LOG_LEVEL) );
		
		if( properties.containsKey(KEY_LOG_FORMAT) )
			this.log4jConfiguration.setPattern( properties.getProperty(KEY_LOG_FORMAT) );

		// Pass to all the sub-components
		this.rtiConfiguration.parseProperties( properties );
		this.lrcConfiguration.parseProperties( properties );
		this.fwdConfiguration.parseProperties( properties );
	}

	private File findRtiHome( Properties ridproperties, Properties clproperties )
		throws JConfigurationException
	{
		// Priority 1: Command Line Args
		String rtihome = clproperties.getProperty( KEY_RTI_HOME );
		if( rtihome != null )
		{
			File file = new File( rtihome );
			if( file.exists() )
				return file;
			else
				throw new JConfigurationException( "RTI HOME path specified on command line but does not exist: %s", rtihome );
		}
		
		// Priority 2: Contained in RID File (if we have one)
		if( ridproperties != null && ridproperties.containsKey(KEY_RTI_HOME) )
		{
			rtihome = ridproperties.getProperty( KEY_RTI_HOME );
			File file = new File( rtihome );
			if( file.exists() )
				return file;
			else
				throw new JConfigurationException( "RTI HOME path specified in RID file but does not exist: %s", rtihome );
		}

		// Priority 3: Environment Variable
		rtihome = System.getenv( "RTI_HOME" );
		if( rtihome != null )
		{
			File file = new File( rtihome );
			if( file.exists() )
				return file;
			else
				throw new JConfigurationException( "RTI_HOME environment variable specified but path does not exist: %s", rtihome );
		}

		// Priority 4: ./		
		return new File( "./" );
	}

	/*
	 * Find the RTI data directory. If it is specified but does not exist, try and create it.
	 */
	private File findRtiData( Properties ridproperties, Properties clproperties )
	{
		// Priority 1: Command Line Args
		String rtidata = clproperties.getProperty( KEY_RTI_DATA );
		if( rtidata != null )
		{
			File file = new File( rtidata );
			file.mkdirs();
			return file;
		}
		
		// Priority 2: Contained in RID File (if we have one)
		if( ridproperties != null && ridproperties.containsKey(KEY_RTI_DATA) )
		{
			rtidata = ridproperties.getProperty( KEY_RTI_DATA );
			File file = new File( rtidata );
			file.mkdirs();
			return file;
		}
		
		// Priority 4: System default
		boolean windows = System.getProperty("os.name").contains("indows");
		if( windows )
			rtidata = System.getProperty("user.home")+"\\My Documents\\Portico";
		else
			rtidata = System.getProperty("user.home")+"/.portico";

		File file = new File( rtidata );
		file.mkdirs();
		return file;
	}

	/**
	 * RID properties can use symbols for certain configuration settings such as common important
	 * paths. This method loops through all properties and replaces any instances of the symbols
	 * that it finds in their values with the actual values.
	 * 
	 * Supported symbols:
	 *  - `${rti.home} `- Location of the root RTI installation directory
	 *  - `${rti.data}` - Location where we can write data such as log and working files 
	 */
	private void substituteSymbols( Properties properties )
	{
		// pull out the tokens we need to replace and replace them anywhere they are inside
		// the values of the properties set
		String rtihome_string  = this.rtihome.getAbsolutePath().replace( "\\","\\\\" );
		String rtidata_string  = this.rtidata.getAbsolutePath().replace( "\\","\\\\" );
		String userhome_string = System.getProperty("user.home").replace( "\\","\\\\" );
		for( String key : properties.stringPropertyNames() )
		{
			String value = properties.getProperty( key );
			value = value.replace( "${user.home}", userhome_string );
			value = value.replace( "${rti.home}",  rtihome_string );
			value = value.replace( "${rti.data}",  rtidata_string );
			properties.setProperty( key, value );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Calls {@link #loadRid(String...)} with no command line arguments.
	 * 
	 * @return A RID object loaded from defaults.
	 * @throws JConfigurationException If there is a problem with any defualt configurations
	 * @see {@link #loadRid(String...)}
	 */
	public static RID loadDefaultRid() throws JConfigurationException
	{
		return RID.loadRid();
	}
	
	/**
	 * Create a new RID file using the given command line arguments. A RID file can be loaded
	 * from a number of locations. This method will check each of the locations below in the
	 * order given. If a location is specified, but it does not point to a valid file, an
	 * exception will be thrown.
	 * 
	 * <ol>
	 *   <li>Explicitly defined on the {@link CommandLine}</li>
	 *   <li>Value of <code>RTI_RID_FILE</code> environment variable</li>
	 *   <li>Default location <code>./RTI.rid</code></li>
	 *   <li>No file is loaded. Defaults used.</li>
	 * </ol>
	 * 
	 * @param commandline The command line arguments to use when creating the RID
	 * @return A populated RID file, with the given command line args applied to it
	 * @throws JConfigurationException If there is a problem locating or parsing the RID file
	 */
	public static RID loadRid( String... commandline ) throws JConfigurationException
	{
		return loadRid( new CommandLine(commandline) );
	}
	
	/**
	 * Create a new RID file using the given {@link CommandLine} arguments. A RID file can be
	 * loaded from a number of locations. This method will check each of the locations below
	 * in the order given. If a location is specified, but it does not point to a valid file,
	 * an exception will be thrown.
	 * 
	 * <ol>
	 *   <li>Explicitly defined on the {@link CommandLine}</li>
	 *   <li>Value of <code>RTI_RID_FILE</code> environment variable</li>
	 *   <li>Value of <code>./RTI.rid</code> (if file exists)</li>
	 *   <li>Value of <code>${rti.home}/RTI.rid (if RTI home is specified on command line)</li>
	 *   <li>Value of <code>$RTI_HOME/RTI.rid</code> (if RTI_HOME environment var is specified)</li>
	 *   <li>No file is loaded. Defaults and given command line used</li>
	 * </ol>
	 * 
	 * @param commandline The command line arguments to use when creating the RID
	 * @return A populated RID file, with the given command line args applied to it
	 * @throws JConfigurationException If there is a problem locating or parsing the RID file
	 */
	public static RID loadRid( CommandLine commandline ) throws JConfigurationException
	{
		//
		// Priority 1: Explicit command line arg
		//
		String ridpath = commandline.get( Argument.RidFile );
		if( ridpath != null )
		{	
			File ridfile = new File( ridpath );
			if( ridfile.exists() == false )
				throw new JConfigurationException( "RID file does not exist: command line specified=%s", ridpath );
			else
				return new RID( ridfile, commandline );
		}
		
		//
		// Priority 2: RTI_RID_FILE environment variable
		//
		ridpath = System.getenv( "RTI_RID_FILE" );
		if( ridpath != null )
		{
			File ridfile = new File( ridpath );
			if( ridfile.exists() == false )
				throw new JConfigurationException( "RID file does not exist: environment variable RTI_RID_FILE=%s", ridpath );
			else
				return new RID( ridfile, commandline );
		}

		//
		// Priority 3: ./RTI.rid
		//
		ridpath = "./RTI.rid";
		File ridfile = new File( ridpath );
		if( ridfile.exists() )
			return new RID( ridfile, commandline );
		
		//
		// Priority 4: Value of ${rti.home}/RTI.rid (if rti.home specified on command line)
		//
		String rtihome = commandline.get( Argument.RtiHome );
		if( rtihome != null )
		{
			ridfile = new File( new File(rtihome), "RTI.rid" );
			if( ridfile.exists() )
				return new RID( ridfile, commandline );
		}

		//
		// Priority 5: Value of $RTI_HOME/RTI.rid
		//
		rtihome = System.getenv( "RTI_HOME" );
		if( rtihome != null )
		{
			ridfile = new File( new File(rtihome), "RTI.rid" );
			if( ridfile.exists() )
				return new RID( ridfile, commandline );
		}

		//
		// All done - no RID file to be found
		//
		return new RID( commandline );
	}

	/**
	 * Create a new RID with its default values, but override those with the values given as
	 * an argument. This is useful for bulk overriding of settings.
	 * 
	 * @param overrides Values that should override RID defaults
	 * @return A RID with default values, except those overridden
	 * @throws JConfigurationException There was a problem parsing or loading the properties
	 */
	public static RID loadRid( Properties overrides ) throws JConfigurationException
	{
		RID rid = loadRid();
		if( overrides != null )
			rid.parseProperties( overrides );
		return rid;
	}
}
