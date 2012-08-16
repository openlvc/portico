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
package org.portico.console.binding;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import com.lbf.commons.component.ComponentException;
import com.lbf.commons.config.ConfigurationException;
import com.lbf.commons.messaging.MessageSink;
import com.lbf.commons.messaging.Module;
import com.lbf.commons.messaging.ModuleConfigurator;
import com.lbf.commons.messaging.ModuleGroup;
import com.lbf.commons.utils.Bag;
import org.portico.console.binding.comms.ConsoleJSOPDaemon;
import org.portico.console.binding.handlers.ConsoleMessageHandler;
import org.portico.console.shared.comms.ConsoleJSOPConstants;
import org.portico.core.Bootstrap;
import org.portico.core.MulticastRegistry;

/**
 * This class represents the rticonsole binding. When started it will fire up a Daemon to listen 
 * for connections from remote clients.
 */
public class ConsoleBootstrap extends Bootstrap
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	//private static final String KEY_PORT = "configuration.port";
	private static final String KEY_MODULE_FILE = "configuration.module.file";
	private static final String KEY_PORT = "configuration.jsopendpoint.port";
	private static final String DEFAULT_MODULE_FILE_PATH = "etc/console-binding-module.xml";
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private MessageSink consoleRequestSink;
	private ConsoleJSOPDaemon daemon;
	private Thread daemonThread;
	private int consoleJsopPort;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ConsoleBootstrap()
	{
		super( "console-bootstrap" );
		this.consoleJsopPort = ConsoleJSOPConstants.DEFAULT_PORT;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 *  
	 */
	public void configureBootstrap( Bag<String,?> properties ) throws ConfigurationException
	{
		// ====================================
		// 1. CONFIGURE MODULES
		// ====================================
		
		// Create an underlying message sink to handle requests to the console
		this.consoleRequestSink = new MessageSink( "console-request" );
		
		// Get a list of modules that describe how the message sink should look
		List<?> moduleConfigCollection = properties.getMultiple( KEY_MODULE_FILE );
		
		// Aggregate each module into a module group
		ModuleGroup group = new ModuleGroup( this.name );

		
		// If there were no modules specified
		if ( moduleConfigCollection.size() == 0 )
		{
			// Use the default module configuration
			logger.trace("No custom console binding module file specified, using defaults");
			Module module = ModuleConfigurator.createModule( DEFAULT_MODULE_FILE_PATH );
			group.addModule( module );
		}
		else
		{
			// otherwise iterate through all of the module files given in the config file
			for ( Object value : moduleConfigCollection )
			{
				String config = null;
				try
				{
					config = (String)value;
				}
				catch (ClassCastException cce)
				{
					throw new ConfigurationException(KEY_MODULE_FILE + 
					                                 " is not a plain string attribute");
				}
				Module module = ModuleConfigurator.createModule( config );
				group.addModule( module );
			}
		}
		// Pass in the RTI Execution to the handlers in the sink
		Bag<String,Object> additionals = new Bag<String,Object>();
		additionals.put( ConsoleMessageHandler.KEY_RTIEXEC, this.execution );
		additionals.put(ConsoleMessageHandler.KEY_SINK, this.consoleRequestSink);
		
		// Apply the module group to the sink
		group.applyGroup( additionals, this.consoleRequestSink );
		
		logger.debug( "Configured console request sink with [" + moduleConfigCollection.size() +
		              "] modules.");
		
		// ====================================
		// 2. CONFIGURE ENDPOINT
		// ====================================
		
		// Get a list of which port to use
		List<?> portConfigCollection = properties.getMultiple( KEY_PORT );
		
		// If there were no entries for which port to use
		if ( portConfigCollection.size() == 0 )
		{
			// Use the default port
			this.consoleJsopPort = ConsoleJSOPConstants.DEFAULT_PORT;
		}
		else
		{
			int value = 0;
			
			// get the first port entry
			try
			{
				value = new Integer(((String)portConfigCollection.get(0))).intValue();
			}
			catch (ClassCastException cce)
			{
				throw new ConfigurationException (KEY_PORT + " is not a plain string attribute", 
						cce);
			}
			
			// set the port to the value of the first port entry
			this.consoleJsopPort = value;
		}
	}
	
	@Override
	public void startBootstrap() throws ComponentException
	{
		/////////////////////////////////////////////////////
		// register the binding with the discover registry //
		/////////////////////////////////////////////////////
		try
		{
			String value = "" + InetAddress.getLocalHost().getHostAddress() + ":" + consoleJsopPort;
			MulticastRegistry registry = getExecution().getBootstrapManager().getMulticastRegistry();
			if( registry.register("console", value) == false )
			{
				throw new Exception( "Binding [console] taken" );
			}
			
			logger.trace( "(console) registered binding with discover registry" );
		}
		catch( Exception e )
		{
			logger.info( "Multicast discovery not possible for console, exception while " +
			             "contacting discover regsitry: " + e.getMessage() );
		}
		
		//////////////////////////////////////
		// create and start the JSOP daemon //
		//////////////////////////////////////
		try
		{
			// create the daemon and start it up //
			this.daemon = new ConsoleJSOPDaemon( this, this.consoleJsopPort );
		}
		catch( IOException ioe )
		{
			// advise the calling environment of any exceptions
			logger.error( "FAILURE (console) Could not start console-daemon: "  
			              + ioe.getMessage(), ioe );
			throw new ComponentException( "Could not start console-daemon: " 
			              + ioe.getMessage(), ioe );
		}

		// start the daemon //
		this.daemonThread = new Thread( daemon, "console:" + getExecution().getExecutionName() );
		this.daemonThread.start();
	}
	
	@Override
	public void stopBootstrap() throws ComponentException
	{
		///////////////////////////////////////////////////////
		// unregister the binding with the discover registry //
		///////////////////////////////////////////////////////
		MulticastRegistry registry = getExecution().getBootstrapManager().getMulticastRegistry();
		registry.unregister( "console" );
		logger.debug( "(console) unregistered binding from discover registry" );
		
		//////////////////////////////////////////////////
		// shut down the daemon and wait for it to exit //
		//////////////////////////////////////////////////
		logger.trace( "(console) shutting down ConsoleJSOPDaemon:" + execution.getExecutionName() );
		
		this.daemon.shutdown();
		while( this.daemonThread.isAlive() )
		{
			try
			{
				this.daemonThread.join();
			}
			catch( InterruptedException ie )
			{
				// exception squelch
			}
		}
		
		logger.debug( "(console) shut down ConsoleJSOPDaemon:" + execution.getExecutionName() );
	}
	
	public ConsoleJSOPDaemon getDaemon()
	{
		return this.daemon;
	}
	
	public MessageSink getConsoleRequestSink()
	{
		return this.consoleRequestSink;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}


