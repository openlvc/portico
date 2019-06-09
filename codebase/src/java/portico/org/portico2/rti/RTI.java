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
package org.portico2.rti;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.configuration.RID;
import org.portico2.common.configuration.StartupLogger;
import org.portico2.common.logging.Log4jConfigurator;
import org.portico2.rti.federation.FederationManager;

public class RTI
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
	
	// Network Connections / Listeners
	private RtiConnectionManager connectionManager;
	private RtiInbox inbox; // processes messages
	
	// Active Federations
	private FederationManager federationManager;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public RTI( RID rid ) throws JConfigurationException
	{
		if( rid == null )
			throw new JConfigurationException( "Cannot create an RTI without RID (initialization data)" );
		
		// Store the configuration and bootstrap logging
		this.rid = rid;
		this.logger = null;            // set in startup()

		this.connectionManager = null; // set in startup()
		this.inbox             = null; // set in startup()
		this.federationManager = null; // set in startup()
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Management   ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public synchronized void startup()
	{
		if( this.running )
			return;
		
		//
		// Bootstrap
		//
		// initialize the logging framework
		this.rid.getLog4jConfiguration().setFileName( "rtiexec.log" );
		this.rid.getLog4jConfiguration().turnFileOn();
		Log4jConfigurator.activate( this.rid.getLog4jConfiguration() );
		this.logger = LogManager.getFormatterLogger( "portico.rti" );

		// log some startup information
		StartupLogger.logGenericStartupHeader( logger, rid );
		
		// prepare the RTI
		this.federationManager = new FederationManager();
		this.inbox             = new RtiInbox( this );
		this.connectionManager = new RtiConnectionManager();
		this.connectionManager.configure( this );

		//
		// Start
		//
		logger.info( "Starting the RTI" );
		
		// start the contained components
		this.connectionManager.startup();
		
		// install a shutdown hook to clean up gracefully if the JVM is terminated
		Runtime.getRuntime().addShutdownHook( new RtiShutdownHook(this) );
		
		this.running = true;
		logger.info( "RTI is up. Connections active and ready for bizness!\n" );
	}
	
	public synchronized void shutdown()
	{
		if( this.running == false )
			return;

		logger.info( "Shutting down the RTI" );
		
		// kill the active connections
		this.connectionManager.shutdown();
		
		this.running = false;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Message Handling   ////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public RtiInbox getInbox()
	{
		return this.inbox;
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
	
	public RtiConnectionManager getConnectionManager()
	{
		return this.connectionManager;
	}
	
	public FederationManager getFederationManager()
	{
		return this.federationManager;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	///////////////////////////////////////////////////////////////////////////////////////
	/// Private Inner Class: Shutdown Hook   //////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	private class RtiShutdownHook extends Thread
	{
		private RTI rti;

		public RtiShutdownHook( RTI rti )
		{
			super( "RtiShutdownHook" );
			this.rti = rti;
		}

		@Override
		public void run()
		{
			if( this.rti.logger != null )
				this.rti.logger.info( "Execution terminated. Shutting down RTI." );
			
			this.rti.shutdown();
		}
	}

}
