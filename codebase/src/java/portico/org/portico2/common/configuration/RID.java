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
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.configuration.commandline.Argument;
import org.portico2.common.configuration.commandline.CommandLine;
import org.portico2.common.logging.Log4jConfiguration;
import org.portico2.common.utils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RID
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final String DEFAULT_RID_FILE = "./RTI.rid";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Log4jConfiguration log4jConfiguration;
	private Logger logger;
	private CommandLine commandline;

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
	 * Create a new RID using all the defaults and overriding it with the values from the given
	 * configuration file, overriding those values with the ones extracted from the command line.
	 * 
	 * @param ridfile The XML file containing the configuration data
	 * @param commandline The command line to use as the final value for any specific properties
	 * @throws JConfigurationException If there is a problem parsing any of the configuraion data
	 */
	private RID( File ridfile, CommandLine commandline )
	{
		this();
		
		// Step 1. Preload
		//         Extract any important command line values that affect subsequent loading
		if( commandline != null )
			preload( commandline );
		
		// Step 2. Parse the RID file
		if( ridfile != null )
			parseRid( ridfile );

		// Step 3. Override whatever we've loaded with any command line args
		if( commandline != null )
			override( commandline );
		
		// store the location of teh RID file for later read-only reference
		this.ridpath = ridfile.getPath();
		// FIXME what about rtihome and rtidata???
		this.commandline = commandline;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
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

	public CommandLine getCommandLine()
	{
		return this.commandline;
	}
	
	//// Internal Only ////
	private void setRtiHome( String path ) throws JConfigurationException
	{
		File file = new File( path );
		if( file.exists() == false )
			throw new JConfigurationException( "RTI Home location does not exist: "+file.getAbsolutePath() );
		else
			this.rtihome = file;
	}
	
	private void setRtiData( String path ) throws JConfigurationException
	{
		// FIXME
		throw new JConfigurationException( "Not Yet Implemented" );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	/// Path-Based Configuration Options    ////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
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
	///  Override Methods   ////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Called before the parsing is started properly to ensure we pull out important 
	 * values that has relevance throughout the loading process.
	 */
	private void preload( CommandLine commandline )
	{
		// Constructor is private and this already factored into RID.loadXxxx() methods
		//if( commandline.hasArgument(Argument.RidFile) )
		//	;

		if( commandline.hasArgument(Argument.RtiHome) )
		{
			this.rtihome = new File( commandline.get(Argument.RtiHome) );
			if( this.rtihome.exists() == false )
			{
				throw new JConfigurationException( "Specified RTI Home value doesn't exist: "+
				                                   this.rtihome.getAbsolutePath() );
			}
		}
	}

	private void override( CommandLine commandline ) throws JConfigurationException
	{
		Map<Argument,String> arguments = commandline.getArguments();
		for( Argument argument : arguments.keySet() )
			override( argument, arguments.get(argument) );
	}
	
	private void override( Properties properties ) throws JConfigurationException
	{
		for( String propertyName : properties.stringPropertyNames() )
		{
			Argument argument = Argument.getArgumentForProperty( propertyName );
			override( argument, properties.getProperty(propertyName) );
		}
	}
	
	private void override( Argument argument, String value )
	{
		switch( argument )
		{
			case RidFile: break;   // no-op - it's too late, we'll have pre-processed for this
			case RtiHome: break;   // no-op - handled in preload()
			case LogLevel: log4jConfiguration.setLevel( value ); break;
			case LogFile:  log4jConfiguration.setFilePath( value ); break;
			default: throw new JConfigurationException( "Unknown argument: "+argument );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Configuration Parsing Methods   ////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private void parseRid( File file ) throws JConfigurationException
	{
		if( file.exists() == false )
			throw new JConfigurationException( "RID file does not exist: "+file.getAbsolutePath() );

		// Parse the XML file into something we can work with
		Document xml = XmlUtils.parseXmlFile( file );

		// Grab the RID pieces that we handle directly
		Element portico = xml.getDocumentElement();
		if( portico.getTagName().equals("portico") == false )
			throw new JConfigurationException( "RID file must contain <portico> as document element" );

		// Get the <common> section of the RID
		Element common  = XmlUtils.getChild( portico, "common" );
		this.parseCommon( common );

		// Grab the RID pieces for major sub-components
		Element rti = XmlUtils.getChild( portico, "rti" );
		Element lrc = XmlUtils.getChild( portico, "lrc" );
		Element fwd = XmlUtils.getChild( portico, "forwarder" );
		
		// Parse the major sub-components
		this.rtiConfiguration.parseConfiguration( this, rti );
		this.lrcConfiguration.parseConfiguration( this, lrc );
		this.fwdConfiguration.parseConfiguration( this, fwd );
	}

	private void parseCommon( Element element ) throws JConfigurationException
	{
		Element rtihome = XmlUtils.getChild( element, "rtihome", false ); // optional
		Element rtidata = XmlUtils.getChild( element, "rtidata", false ); // optional
		Element logging = XmlUtils.getChild( element, "logging" );
		Element special = XmlUtils.getChild( element, "special" );
		
		if( this.rtihome == null && rtihome != null )
			this.setRtiHome( substituteSymbols(rtihome.getTextContent()) );
		
		if( this.rtidata == null && rtidata != null )
			this.setRtiData( substituteSymbols(rtidata.getTextContent()) );
		
		this.parseLogging( logging );
		this.parseSpecial( special );
	}
	
	private void parseLogging( Element element ) throws JConfigurationException
	{
		if( element.hasAttribute("logdir") )
		{
			String temp = element.getAttribute("logdir");
			this.log4jConfiguration.setFilePath( substituteSymbols(temp), "portico.log" );
		}
		
		if( element.hasAttribute("loglevel") )
			this.log4jConfiguration.setLevel( element.getAttribute("loglevel") );
		
		if( element.hasAttribute("logformat") )
			this.log4jConfiguration.setPattern( element.getAttribute("logformat") );
	}

	private void parseSpecial( Element element ) throws JConfigurationException
	{
		// FIXME Add support for setting JGroups log level
	}

	/**
	 * RID properties can use symbols for certain configuration settings such as common important
	 * paths. This method looks at the given value and substitutes any strings it finds with the
	 * approrpiate values. 
	 * 
	 * Supported symbols:
	 *  - `${user.home} `- Location of the user's home directory
	 *  - `${rti.home} ` - Location of the root RTI installation directory
	 *  - `${rti.data}`  - Location where we can write data such as log and working files
	 *  
	 * @param value The value to check for the special <code>${}</code> values in
	 * @return The string, with any symbol paths substituted
	 */
	public String substituteSymbols( String value )
	{
		if( value.contains("${user.home}") )
		{
			String path = System.getProperty("user.home").replace( "\\","\\\\" );
			value.replace( "${user.home}", path ); 
		}
		
		if( value.contains("${rti.home}") )
		{
			String path = this.rtihome.getAbsolutePath().replace( "\\","\\\\" );
			value.replace( "${rti.home}", path ); 
		}
		
		if( value.contains("${rti.data}") )
		{
			String path = this.rtidata.getAbsolutePath().replace( "\\","\\\\" );
			value.replace( "${rti.data}", path ); 
		}
		
		return value;
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
		return new RID( null, commandline );
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
			rid.override( overrides );
		return rid;
	}
}
