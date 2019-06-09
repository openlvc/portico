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
package org.portico2.forwarder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.configuration.RID;
import org.portico2.common.configuration.StartupLogger;
import org.portico2.common.logging.Log4jConfigurator;

public class Forwarder
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RID rid;
	private Logger logger;
	private boolean running;
	
	private Exchanger exchanger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Forwarder( RID rid ) throws JConfigurationException
	{
		if( rid == null )
			throw new JConfigurationException( "Cannot create an Forwarder without RID (initialization data)" );
		
		// Store the configuration and bootstrap logging
		this.rid = rid;
		this.logger = null;            // set in startup()
		this.running = false;          // set in startup()
		this.exchanger = null;         // set in startup()
//		this.firewall = null;          // set in startup()
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Management   ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void startup()
	{
		if( this.running )
			return;
		
		//
		// Bootstrap
		//
		// initialize the logging framework
		Log4jConfigurator.activate( this.rid.getLog4jConfiguration() );
		this.logger = LogManager.getFormatterLogger( "portico.forwarder" );
		
		// build the firewall and parse rules
//		this.firewall = new Firewall();

		// log some startup information
		StartupLogger.logGenericStartupHeader( logger, rid );
		
		// prepare the Forwarder
		this.exchanger = new Exchanger( this );

		//
		// Start
		//
		logger.info( "Starting the Forwarder" );
		this.exchanger.startup();
		
		// install a shutdown hook to clean up gracefully if the JVM is terminated
		Runtime.getRuntime().addShutdownHook( new ForwarderShutdownHook(this) );
		
		this.running = true;
		logger.info( "Forwarder is up and ready for Bizness!\n" );
	}
	
	public void shutdown()
	{
		if( this.running == false )
			return;

		logger.info( "Shutting down the RTI" );

		// Kill the connections so that we're not getting the stream any more
		// The exchanger manages all this
		this.exchanger.shutdown();

		logger.info( "Forwarder is shutdown" );
		this.running = false;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Getters and Setters   /////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public Logger getLogger()
	{
		return this.logger;
	}
	
	public RID getRid()
	{
		return this.rid;
	}

	public Exchanger getExchanger()
	{
		return this.exchanger;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

	///////////////////////////////////////////////////////////////////////////////////////
	/// Private Inner Class: Shutdown Hook   //////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	private class ForwarderShutdownHook extends Thread
	{
		private Forwarder forwarder;

		public ForwarderShutdownHook( Forwarder forwarder )
		{
			super( "ForwarderShutdownHook" );
			this.forwarder = forwarder;
		}

		@Override
		public void run()
		{
			if( this.forwarder.logger != null )
				this.forwarder.logger.info( "Execution terminated. Shutting down Forwarder." );
			
			this.forwarder.shutdown();
		}
	}
}
