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
import org.portico2.forwarder.firewall.Firewall;
import org.portico2.forwarder.tracking.StateTracker;

/**
 * The Forwarder is a component that sits between a local federation and a remote RTI.
 * Typically, local federates communicate with each other over a high-speed local connection
 * using multicast. This also means that we cannot bridge across multiple multicast domains,
 * which is a problem when federates are separated by network (as is common in many cloud or
 * container frameworks) or physically separated.
 * <p/>
 * The Forwarder can also act as a basic firewall of sorts. For federates that are running on
 * low power devices, but are operating in federates with high-powered, chatty federates, the
 * volume of traffic can be overwhelming. In this case, we can the low-power federates behind
 * a Forwarder and block off various pieces of traffic. The federates will still be able to
 * exchange data for various object classes locally (that is, they can remain subscribed), but
 * the fact that they're running on a separate network will allow us to use the Forwarder as a
 * filtering point. In this case, <b>all CONTROL messages</b> will be allowed to pass, ALWAYS.
 * However, <b>DATA messages may be blocked</b> (unless the forwarder is configured to allow
 * them through.
 * <p/>
 * Each Forwarder has two sides: <code>local</code> and <code>upstream</code> (the RTI). At
 * startup, a Forwarder will connect to an upstream RTI (to ensure that one exists). It will
 * then start forwarding messages from the local side to the upstream side (and vice versa). 
 */
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
	
	// Connection Management
	private Exchanger exchanger;
	
	// Firewall and State Tracking
	private Firewall firewall;
	private StateTracker tracker;   // watches what is happening to support filtering

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Forwarder( RID rid ) throws JConfigurationException
	{
		if( rid == null )
			throw new JConfigurationException( "Cannot create an RTI without RID (initialization data)" );
		
		// Store the configuration and bootstrap logging
		this.rid = rid;
		this.logger = null;            // set in startup()
		this.exchanger = null;         // set in startup()
		this.firewall = null;          // set in startup()
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
		this.firewall = new Firewall();

		// log some startup information
		StartupLogger.logGenericStartupHeader( logger, rid );
		
		// prepare the Forwarder
		this.exchanger = new Exchanger( this );

		//
		// Start
		//
		logger.info( "Starting the Forwarder" );
		
		// start the contained components
		this.exchanger.startup();
		
		// install a shutdown hook to clean up gracefully if the JVM is terminated
		Runtime.getRuntime().addShutdownHook( new ForwarderShutdownHook(this) );
		
		this.running = true;
		logger.info( "Forwarder is up and ready for Bizness!" );
	}
	
	public void shutdown()
	{
		if( this.running == false )
			return;

		logger.info( "Shutting down the RTI" );
		
		// kill the active connections
		//this.connectionManager.shutdown();
		
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

	public Firewall getFirewall()
	{
		return this.firewall;
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
