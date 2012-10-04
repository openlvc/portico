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
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.portico.lrc.LRC;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.utils.RID;
import org.portico.utils.ObjectFactory;
import org.portico.utils.annotations.AnnotationLocator;
import org.portico.utils.classpath.Classpath;
import org.portico.utils.logging.Log4jConfigurator;
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
	protected Map<String,Module> modules;
	private DaemonManager daemonManager;
	
	// plugin paths
	private URL porticoJarFile;
	private List<File> userPluginPath;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	private Container() throws JConfigurationException
	{
		this.logger = Logger.getLogger( "portico.container" );
		this.activeLrcs = new HashMap<String,LRC>();
		this.modules = new HashMap<String,Module>();
		this.daemonManager = new DaemonManager( this );
		this.userPluginPath = new ArrayList<File>();
		
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
	
	/**
	 * Get a list of all locations on the user plugin path. This can include the user specified
	 * directories and all the jar files contained within them.
	 */
	public List<File> getUserPluginPath()
	{
		return this.userPluginPath;
	}

	/**
	 * Returns a reference to the URL that points to the location of the portico.jar file.
	 */
	public URL getPorticoJarFile()
	{
		return this.porticoJarFile;
	}
	
	/**
	 * This method will return a list of URLs that consists of the entire path you can search for
	 * plugins. This is the augmentation of {@link #getUserPluginPath()} and
	 * {@link #getPorticoJarFile()}
	 */
	private List<URL> getCompletePluginSearchPath()
	{
		ArrayList<URL> list = new ArrayList<URL>();
		if( porticoJarFile != null )
			list.add( porticoJarFile );
		
		for( File file : this.userPluginPath )
			list.add( PorticoConstants.fileToUrl(file) );
		
		return list;
	}
	
	/**
	 * Find a list of all classes on the classpath that provide the specified annotation. If the
	 * provided <code>requiredInterface</code> is not null, the method will only include those
	 * classes that declare the annoation and conform to the provided interface type. If the value
	 * is null, no check for interface conformance will be made.
	 * 
	 * @param annotationType The annotation the class should provide
	 * @param requiredInterface The interface the class should implement if it is to be included.
	 *                          If this is null, no check will be made and any class that declares
	 *                          the interface will be returned.
	 * @return A set of all the classes on the classpath that declare the provided annotation in
	 *         addition to the provided interface type
	 */
	public Set<Class<?>> getClassesWithAnnotation( Class<? extends Annotation> annotationType,
	                                               Class<?> requiredInterface )
	    throws JConfigurationException
	{
		Set<Class<?>> filteredTypes = new HashSet<Class<?>>();

		// iterate of each URL available to us, looking for those classes that have the annotation
		for( URL url : Container.instance().getCompletePluginSearchPath() )
		{
			if( logger.isDebugEnabled() )
			{
				logger.debug( "(PluginLocator) Scanning ["+url+"] for ["+
				              annotationType.getCanonicalName()+"]" );
			}
			
			// find all the types that declare the annotation
			Set<Class<?>> foundTypes = new HashSet<Class<?>>(); // empty set, just being safe
			try
			{
				AnnotationLocator.locateClassesWithAnnotation( annotationType, url );
			}
			catch( Exception e )
			{
				throw new JConfigurationException( e );
			}
			
			// check that each located class implements the provided interface. If no interface
			// type is provided, just add the full set of found types with the annotation
			if( requiredInterface == null )
				filteredTypes.addAll( foundTypes );
			
			for( Class<?> clazz : foundTypes )
			{
				if( requiredInterface.isAssignableFrom(clazz) )
				{
					filteredTypes.add( clazz );
				}
				else
				{
					logger.error( "(PluginLocator) Class ["+clazz.getName()+"] declares "+
					              annotationType.getSimpleName()+" annotation, but doesn't implement "+
					              requiredInterface.getCanonicalName()+": skipping" );
				}
			}
		}
		
		return filteredTypes;
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

		// load all the plugins
		locatePlugins();
	}

	/**
	 * This method will find all the libraries in the plugins directory, load them, scan them for
	 * messaging modules and {@link Daemon}s. Any found modules will be stored for later use,
	 * Daemons will be added to the containers {@link DaemonManager}.
	 * <p/>
	 * This method will scan the plugins directories (as specified by
	 * {@link #getContainerPluginPath()}). It will add any contained jar files to the system
	 * classpath.
	 * <p/>
	 * It will then use the {@link AnnotationLocator} to find all {@link Daemon} classes. For each
	 * class found, an instance will be instantiated, have its name set and then added to the
	 * {@link DaemonManager}.
	 * <p/>
	 * Following this, all the {@link Module}s on the classpath will be located and stored for
	 * later use when creating and configuring new {@link LRC} instances.
	 * <p/>
	 * <b>NOTE:</b> The directory $RTI_HOME/plugins and ./plugins will ALWAYS be added to the
	 *              search path, regardless of whether another path is specified or not.
	 * 
	 * @throws JConfigurationException If there is a problem reading the plugins directory, locating
	 *                                 or instantiating any {@link IDaemon} instances or Modules.
	 */
	private void locatePlugins() throws JConfigurationException
	{
		// make sure we can find the portico.jar file on the system classpath first
		Classpath classpath = new Classpath();
		URL[] originalSystemPath = classpath.getUrlSearchPath();
		this.porticoJarFile = null;
		for( URL url : originalSystemPath )
		{
			if( url.getPath().endsWith("portico.jar") )
			{
				this.porticoJarFile = url;
				break;
			}
		}
		
		if( this.porticoJarFile == null )
		{
			logger.warn( "Couldn't locate RTI_HOME/lib/portico.jar on system classpath, is RTI_HOME set?" );
		}
		else
		{
			logger.debug( "Found portico.jar at location: " + this.porticoJarFile );
		}
		
		//////////////////////////////////////////////////////////////
		// Step One: Find all jar files on the provided plugin path //
		//////////////////////////////////////////////////////////////
		// User specified locations to search for plugins. This data comes from the RID file
		this.userPluginPath = getContainerPluginPath();

		// print the search path being used
		if( this.userPluginPath.size() == 0 )
		{
			logger.debug( "(PluginLoader) No user defined plugin path to search" );
		}
		else
		{
    		logger.debug( "(PluginLoader) The following paths will be searched for plugins" );
    		for( File file : this.userPluginPath )
    			logger.debug( "(PluginLoader) "+file );
		}

		// For each location provided on the plugin path, look for all the jar files contained
		// within it. This will recursively search through the subdirectories looking for jar files
		// Note that the scan method will add all found files to the system classpath
		Set<File> userPluginLocations = new HashSet<File>();
		for( File pluginLocation : this.userPluginPath )
		{
			logger.trace( "(PluginLoader) SCANNING location for jar files: "+pluginLocation );
			Set<File> found = classpath.scan( pluginLocation, true );
			userPluginLocations.addAll( found );
			for( File foundJarFile : found )
				logger.trace( "(PluginLoader)  found: "+foundJarFile );
			
			// if the file itself is a directory, add it to the classpath
			if( pluginLocation.isDirectory() && pluginLocation.canRead() )
			{
				userPluginLocations.add( pluginLocation );
				classpath.extend( pluginLocation );
				logger.trace( "(PluginLoader)  found: "+pluginLocation );
			}
		}

		//////////////////////////////////////////////////////////////////
		// Step Two: Search all relevant jar files for plugins/handlers //
		//////////////////////////////////////////////////////////////////
		// To make sure we find all the core handlers, we need to at an absolute minimum scan
		// the RTI_HOME/lib/portico.jar file. This can be a touch problematic if RTI_HOME isn't
		// set properly. Previously we just defaulted back to scanning the entire classpath as the
		// only way this code could execute would be if it was on there.
		//
		// To optimize this process a little, we now only scan the following locations:
		//  1) All the jar/zip files found via the user-defined plugin-path
		//  2) Search the system classpath for all files called "portico.jar" & scan them.
		//
		// There could be problems here if a user extracts all of portico and bundles it into their
		// own application jar file (as we do with things like log4j), but that is a pretty fringe
		// case and we'll worry about it when it becomes a problem.

		// scan the portico.jar file for plugins so that we apply the default stuff first
		// Make sure we found the jar file first. If we are running the tests, we don't use it, but
		// rather, the code is just put on the user plugin-path. This is valid, but will cause an
		// NPE if we attempt to scan it when it doesn't exist. Hence this check 
		if( this.porticoJarFile != null )
			scanForDaemons( this.porticoJarFile );
		
		// scan each of the entries on the user plugin path for Daemons
		for( File file : userPluginLocations )
			scanForDaemons( PorticoConstants.fileToUrl(file) );

		// scan the portico.jar for messaging modules
		// check to make sure we found the jar, see above scanForDaemons call a few lines up for
		// the full description of why we do this.
		if( this.porticoJarFile != null )
			scanForModules( this.porticoJarFile );
		
		// scan each of the entries on the user plugin path for messaging Modules
		for( File file : userPluginLocations )
			scanForModules( PorticoConstants.fileToUrl(file) );
		
		logger.info( "Located the following plugins:" );
		for( IDaemon daemon : daemonManager.getAllDaemons() )
			logger.info( "  (daemon) -> " + daemon.getClass().getCanonicalName() );
		for( Module module : modules.values() )
			logger.info( "  (module) -> " + module );
	}		

	/**
	 * This method will scan the file located at the given URL for any classes that implement the
	 * {@link IDaemon} interface. If any are found, an attempt will be made to instantiate them
	 * through their no-arg constructor, and to add them to the {@link DaemonManager}.
	 * 
	 * @param location The URL of the jar file to search
	 * @throws JConfigurationException If the Daemon can't be created or registered with the manager
	 */
	private void scanForDaemons( URL location ) throws JConfigurationException
	{
		logger.debug( "(PluginLoader) Scanning ["+location+"] for Daemons" );

		try
		{
			Set<Class<?>> daemonClasses =
				AnnotationLocator.locateClassesWithAnnotation( Daemon.class, location );
			
			// instantiate each of the Daemon classes
			for( Class<?> potentialDaemon : daemonClasses )
			{
				IDaemon theDaemon = ObjectFactory.create( potentialDaemon, IDaemon.class );
				Daemon annotation = potentialDaemon.getAnnotation( Daemon.class );

				if( annotation.name().equals("<undefined>") )
					theDaemon.setName( potentialDaemon.getSimpleName() );
				else
					theDaemon.setName( annotation.name() );
				
				this.daemonManager.registerDaemon( theDaemon );
				logger.debug( "Loaded Daemon [" + theDaemon.getName() + "] from class: " +
				             potentialDaemon.getCanonicalName() );
			}
		}
		catch( Exception e )
		{
			throw new JConfigurationException( "Error loading Daemons (source: "+location+"): "+
			                                   e.getMessage(), e );
		}
	}

	/**
	 * This method will scan the file located at the given URL for any message {@link Module}s.
	 * All found modules will be stored inside this {@link Container} so that as new {@link LRC}s
	 * are created, the modules can be applied to them.
	 */
	private void scanForModules( URL location ) throws JConfigurationException
	{
		logger.debug( "(PluginLoader) Scanning ["+location+"] for Message Modules" );

		// load all the modules from the given location
		Collection<Module> foundModules = Module.findModules( location );
		
		// integrate these modules into the existing collection incase some of the new handlers
		// declare themselves as being in existing modules
		for( Module module : foundModules )
		{
			logger.debug( "(PluginLoader) Located Module ["+module+"] ("+module.size()+
			              " handlers) in ["+location +"]" );
			
			Module existing = modules.get( module.getName() );
			if( existing != null )
				existing.combine( module );
			else
				modules.put( module.getName(), module );
		}
	}
	
	/**
	 * Get a set of all the modules located in the container. If there are no modules, an empty
	 * set will be returned.
	 */
	public Set<Module> getModules()
	{
		return new HashSet<Module>( modules.values() );
	}
	
	/**
	 * Get a set of all the modules that the Container knows about whose names are equal to all
	 * those specified in the given collection. If the Container doesn't know about a module with
	 * one of the provided names, an exception will be thrown.
	 */
	protected Set<Module> getModules( Collection<String> moduleNames ) throws RuntimeException
	{
		Set<Module> moduleSet = new HashSet<Module>();
		for( String moduleName : moduleNames )
		{
			if( modules.containsKey(moduleName) == false )
				throw new RuntimeException( "Can't find needed module named: " + moduleName );
			else
				moduleSet.add( modules.get(moduleName) );
		}
		
		return moduleSet;
	}
	
	/**
	 * Return the set of all modules whose names start with the given prefix. If none exist, an
	 * empty set will be returned.
	 */
	protected Set<Module> getModulesWithPrefix( String prefix )
	{
		Set<Module> moduleSet = new HashSet<Module>();
		for( String moduleName : modules.keySet() )
		{
			if( moduleName.startsWith(prefix) )
				moduleSet.add( modules.get(moduleName) );
		}
		
		return moduleSet;
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
