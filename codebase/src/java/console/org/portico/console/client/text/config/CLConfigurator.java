/*
 *   Copyright 2006 The Portico Project
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
package org.portico.console.client.text.config;

import com.lbf.commons.config.clp.Argument;
import com.lbf.commons.config.clp.CLProcessor;
import com.lbf.commons.config.ConfigurationException;
import com.lbf.commons.logging.Log4jConfigurator;
import org.portico.console.client.text.MainProperties;

import java.io.File;

/**
 * Handles the configuration from command line properties 
 */
public class CLConfigurator
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private CLProcessor processor;
	
	/* argument specific variables */
	private boolean levelSet = false;
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public CLConfigurator() throws ConfigurationException
	{
		// create and initialize the processor
		this.processor = new CLProcessor();
		this.initialize();
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void runConfigurator( String[] args ) throws ConfigurationException
	{
		// check for valid args
		if( args == null )
		{
			throw new ConfigurationException( "Can't process args. Given arguments was null" ); 
		}
		
		// process the arguments
		this.processor.process( args );
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// PRIVATE METHODS /////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	private void initialize() throws ConfigurationException
	{
		///////////////////////////
		// ARGUMENT: -e, --endpoint //
		///////////////////////////
		Argument endpointArgument = new Argument( this, "argEndpoint", "e", "endpoint", 1 );
		this.processor.registerArgument( endpointArgument );
		
		///////////////////////////
		// ARGUMENT: -ch, --home //
		///////////////////////////
		Argument homeArgument = new Argument( this, "argHome", "ch", "home", 1 );
		this.processor.registerArgument( homeArgument );
	
		/////////////////////////////
		// ARGUMENT: -v, --verbose //
		/////////////////////////////
		Argument verboseArgument = new Argument( this, "argVerbose", "v", "verbose", 0 );
		this.processor.registerArgument( verboseArgument );
		
		///////////////////////////////////
		// ARGUMENT: -vv, --very-verbose //
		///////////////////////////////////
		Argument vverboseArgument = new Argument( this, "argVVerbose", "vv", "very-verbose", 0 );
		this.processor.registerArgument( vverboseArgument );
		
		/////////////////////////////
		// ARGUMENT: -q, --quiet //
		/////////////////////////////
		Argument quietArgument = new Argument( this, "argQuiet", "q", "quiet", 0 );
		this.processor.registerArgument( quietArgument );

		/////////////////////////////
		// ARGUMENT: -s, --silent  //
		/////////////////////////////
		Argument silentArgument = new Argument( this, "argSilent", "s", "silent", 0 );
		this.processor.registerArgument( silentArgument );

		////////////////////////////////
		// ARGUMENT: -ll, --log-level //
		////////////////////////////////
		Argument logLevelArgument = new Argument( this, "argLogLevel", "ll", "log-level", 1 );
		this.processor.registerArgument( logLevelArgument );
				
		//////////////////////////
		// ARGUMENT: -h, --help //
		//////////////////////////
		Argument helpArgument = new Argument( this, "argHelp", "h", "help", 0 );
		this.processor.registerArgument( helpArgument );

	}
	
	///////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// ARG HANDLER METHODS ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	/** Specify the location of the portico installation (if you are starting from another dir */
	public void argHome( String[] args ) throws ConfigurationException
	{
		// check to make sure we have a valid value
		File file = new File( args[0] );
		// does the location exist?
		if( file.exists() == false )
		{
			throw new ConfigurationException( "Invalid console home value [" + args[0] +
												"] Location doesn't exist" );
		}
		
		// can we read the location??
		if( file.canRead() == false )
		{
			throw new ConfigurationException( "Invalid console home value [" + args[0] +
												"] Can't read from location" );
		}
		
		// can we write to the location? (for the log file etc...)
		if( file.canRead() == false )
		{
			throw new ConfigurationException( "Invalid console home value [" + args[0] +
												"] Can't write to location" );
		}

		// set the location
		MainProperties.setConsoleHome( args[0] );
	}
		
	/** Turns verbose mode on for the command line and log file (same as --log-level DEBUG) */
	public void argVerbose( String[] args ) throws ConfigurationException
	{
		Log4jConfigurator.setLevel( "DEBUG", "portico.console" );
		this.levelSet = true;
	}
	
	/** Turns verbose mode on for the command line and log file (same as --log-level TRACE) */
	public void argVVerbose( String[] args ) throws ConfigurationException
	{
		Log4jConfigurator.setLevel( "TRACE", "portico.console" );
		this.levelSet = true;
	}

	/** Turns quiet mode on for command line ONLY (sets log level to ERROR) */
	public void argQuiet( String[] args ) throws ConfigurationException
	{
		Log4jConfigurator.setLevel( "ERROR", "portico.console" );
		this.levelSet = true;
	}
	
	/** Turns silent mode on for the command line ONLY (sets level of OFF) */
	public void argSilent( String[] args ) throws ConfigurationException
	{
		Log4jConfigurator.setLevel( "OFF" );
		this.levelSet = true;
	}
	
	/**
	 * Allows the user to set the log level that will be used. One argument is expected, valid
	 * values are: "TRACE", "DEBUG", "INFO", "WARN", "ERROR" and "FATAL". NOTE: sets level for
	 * both console and log file. 
	 */
	public void argLogLevel( String[] args ) throws ConfigurationException
	{
		if( this.levelSet == false )
		{
			Log4jConfigurator.setLevel( args[0] );
		}
	}
		
	/** Will just throw a ConfigurationException to cause the help to be printed */
	public void argHelp( String[] args ) throws ConfigurationException
	{
		throw new ConfigurationException( "User requires help" );
	}

	public void argEndpoint( String[] args ) throws ConfigurationException
	{
		MainProperties.setEndpoint( args[0] );
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
