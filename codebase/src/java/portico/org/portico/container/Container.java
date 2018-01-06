/*
 *   Copyright 2009 The Portico Project
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
package org.portico.container;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.lrc.LRC;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.utils.RID;
import org.portico.utils.logging.Log4jConfigurator;
import org.portico.utils.messaging.HandlerRegistry;
import org.portico.utils.messaging.Module;

/**
 * A {@link Container} represents the top-level in the Portico environment. Into a Container, a
 * number of {@link IDaemon}s (allowing background/separate tasks to be run) and a number of
 * {@link LRC}s can be deployed.
 * 
 * <p>
 * <b>LRCs</b>
 * </p>
 * An {@link LRC} is the main message processing nerve-center of the Portico framework. Typically,
 * an LRC will represent an individual federate. However, it can be used as a general data-gathering
 * entity for third party plugins. 
 * 
 * <p>
 * <b>Daemons</b>
 * </p>
 * The Container also houses a set of {@link IDaemon}s. These components allow arbitrary behaviour
 * to be inserted into the framework and run almost like background processes. See the javadoc for
 * the {@link IDaemon} interface for more information.
 * 
 * <p>
 * <b>Plugins</b>
 * </p>
 * The container will locate and load Portico plugins (handler modules, daemons, etc...). It will
 * keep this information stored and will manage any classpath management concerns.
 */
public class Container
{
	// NOTE: Load the RID file before anything else happens
	static
	{
		RID.load();
	}

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static Container CONTAINER = null;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected Logger logger;
	protected Map<String,LRC> activeLrcs;
	private HandlerRegistry handlerRegistry;
	private DaemonManager daemonManager;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	private Container() throws JConfigurationException
	{
		this.logger = LogManager.getFormatterLogger( "portico.container" );
		this.activeLrcs = new HashMap<String,LRC>();
		this.handlerRegistry = new HandlerRegistry();
		this.daemonManager = new DaemonManager( this );
		
		// do the basic container configuration (like logging and module/plugin location)
		initializeContainer();
		
		// start up all the located Daemons
		this.daemonManager.startDaemons();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Shutdown all the components in the container. Currently this will only stop the
	 * contained daemons.
	 */
	public void killContainer()
	{
		// stop the daemons
		this.daemonManager.stopDaemons();
	}

	/**
	 * Shutdown all the active LRCs with extreme prejudice. This is really only used by the
	 * testing framework which has to clean up all LRCs to avoid connection resource leaks
	 * at the end of each test.
	 */
	public void killLrcs()
	{
		for( LRC lrc : activeLrcs.values() )
		{
			try
			{
				lrc.stopLrc();
				this.daemonManager.notifyLrcDestroyed( lrc );
			}
			catch( Exception e )
			{
				logger.error( "Received exception while terminating LRC" );
			}
		}
		
		this.activeLrcs.clear();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Container Configuration Methods /////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will do all the container initialization. This includes:
	 * <ul>
	 *   <li>Configuring the logging framework</li>
	 *   <li>Loading all the libraries in the plugins directory and extending the classpath.
	 *       This will just store the library information and extend the classpath, it will
	 *       not load the plugin data yet.</li>
	 * </ul>
	 */
	private void initializeContainer()
	{
		// set up the logging
		Log4jConfigurator.bootstrapLogging();
		
		// log RID file information
		if( RID.getRidLocation() != null )
		{
			logger.info( "[RID] Loaded RID file from: " + RID.getRidLocation().getAbsolutePath() +
			             " (" + RID.getRidProperties().size() + " properties)" );
			for( Object key : RID.getRidProperties().keySet() )
				logger.debug( "[RID] " + key + "=" + RID.getRidProperties().get(key) );
		}
		else
		{
			logger.info( "[RID] Unable to locate a RID file, skipping..." );
		}

		// Print some information about the loaded Daemons and Handlers
		logger.info( "Located the following plugins:" );
		for( IDaemon daemon : daemonManager.getAllDaemons() )
			logger.info( "  (daemon) -> " + daemon.getClass().getCanonicalName() );
		for( Module module : handlerRegistry.getAllModules() )
			logger.info( "  (module) -> " + module );
	}

	/**
	 * Get a group of all the modules located in the container. If there are no modules, an empty
	 * set will be returned.
	 */
	public HandlerRegistry getHandlerRegistry()
	{
		return this.handlerRegistry;
	}
	
	public DaemonManager getDaemonManager()
	{
		return this.daemonManager;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// LRC Management Methods //////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a new LRC and returns it. This method will make sure that the LRC is appropriately
	 * configured with all the handlers found on the classpath.
	 */
	public void registerLrc( LRC lrc )
	{
		// notify the daemon manager that this has happened
		this.daemonManager.notifyLrcCreated( lrc );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Container Plugin Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns all the user specified locations to search for plugins. This method will extract
	 * this path from the system propery {@link PorticoConstants#PROPERTY_PLUGIN_PATH} (typically
	 * set through the RID file).
	 * <p/>
	 * Note that if the environment variable RTI_HOME is set, the location RTI_HOME/plugins
	 * will always be added to the search path (after a test to ensure it exists). If a location on
	 * the provided path doesn't exist or can't be read, an exception will be thrown.
	 * <p/>
	 * <b>NOTE:</b> The directory $RTI_HOME/plugins will ALWAYS be added to the search path (if,
	 *              it exists) regardless of whether another path is specified or not.
	 * 
	 * @return An array of all the locations to search for plugins
	 */
	private List<File> getContainerPluginPath() throws JConfigurationException
	{
		// the final set of paths to return
		List<File> paths = new ArrayList<File>();
		
		// Check to see if we have a path to search in the system property
		// We don't use the real default location here for two reasons:
		//  1) we want to make sure we ALWAYS check for the default location (not just when
		//     the system property isn't set)
		//  2) we want bad paths to cause an exception EXCEPT when it is the default path,
		//     which we are happy to allow not to exist
		// For these reasons, we'll only check for/validate the existence of the default
		// path if it is explicitly provided in the system property, otherwise we'll do a
		// special (tolerant of non-existence) check on the default location later, regardless
		// of whether a value exists in the system property or not
		String givenPath = System.getProperty( PorticoConstants.PROPERTY_PLUGIN_PATH, "" );
		String pathSeparator = System.getProperty( "path.separator" );
		StringTokenizer tokenizer = new StringTokenizer( givenPath, pathSeparator );
		while( tokenizer.hasMoreTokens() )
		{
			// check to see if the given path is a valid file
			File location = new File( tokenizer.nextToken() );
			if( location.exists() == false )
				throw new JConfigurationException("(PluginPath) location doesn't exist: "+location);
			else if( location.canRead() == false )
				throw new JConfigurationException("(PluginPath) location can't be read: "+location);
			else
				paths.add( getCanonicalFile(location) );
		}
		
		// do a separate check for the default locations
		File localLocation = new File( "./plugins" );
		if( localLocation.exists() && localLocation.canRead() )
			paths.add( getCanonicalFile(localLocation) );
		
		String rtiHome = System.getenv( "RTI_HOME" );
		if( rtiHome != null )
		{
			File defaultLocation = new File( rtiHome + pathSeparator + "plugins" );
			if( defaultLocation.exists() && defaultLocation.canRead() )
				paths.add( getCanonicalFile(defaultLocation) );
		}

		return paths;
	}

	/**
	 * This method is just a convenience to turn a file into its canonical form without having
	 * to try/catch an IOException. It throws a ConfigurationException if there is a problem,
	 * which is the same exception type thrown by the method that uses this method, so no
	 * catchy needed!
	 */
	private File getCanonicalFile( File file ) throws JConfigurationException
	{
		try
		{
			return file.getCanonicalFile();
		}
		catch( Exception ioex )
		{
			throw new JConfigurationException( "Problem finding canonical file path", ioex );
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Get the single container instance for this JVM.
	 */
	public static Container instance()
	{
		if( Container.CONTAINER == null )
			Container.CONTAINER = new Container();

		return Container.CONTAINER;
	}
}
