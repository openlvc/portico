/*
 *   Copyright 2022 The Portico Project
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
package org.portico3.rti;

import java.io.File;

import org.apache.log4j.Logger;
import org.portico3.common.compatibility.JException;
import org.portico3.common.logging.Log4jConfigurator;
import org.portico3.common.rid.RID;
import org.portico3.common.utils.SystemInformation;
import org.portico3.rti.commandline.CommandLine;

/**
 * The main RTI Server. 
 */
public class RtiServer
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RID rid;
	private CommandLine commandline;

	private Logger logger;

	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public RtiServer( RID rid, CommandLine commandline )
	{
		this.rid = rid;
		this.commandline = commandline;
		
		// Runtime Initialized
		this.logger = null; 
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	//////////////////////////////////////////////////////////////////////////////////////////
	/// Lifecycle Methods   //////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	public void initialize() throws JException
	{
		///////////////////////////////////
		// 1. Initialize the logging  /////
		///////////////////////////////////
		this.initializeLogging();
		logger.info( SystemInformation.getSystemInformationSummary(rid) );

		///////////////////////////////////
		// 2. Start up the connections  ///
		///////////////////////////////////
	}
	
	public void start() throws JException
	{
		
	}
	
	public void shutdown() throws JException
	{
		
	}
	
	
	private void initializeLogging() throws JException
	{
		// Check the RID for the configuration values.
		// Note that the command line overrides should already have been applied.
		String loglevel = rid.getLogSettings().getLogLevel();
		File logdir = rid.getLogSettings().getLogDir();
		
		// Bootstrap the logging environment
		Log4jConfigurator.bootstrapLogging();

		// Store out logger (it's appenders will be manipulated shortly)
		this.logger = Logger.getLogger( "portico.rti" );

		// Set the log level
		Log4jConfigurator.setLevel( loglevel, "portico" );
		
		// Set up the log file
		Log4jConfigurator.redirectFileOutput( logdir.getAbsolutePath()+"/portico.log", true );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
