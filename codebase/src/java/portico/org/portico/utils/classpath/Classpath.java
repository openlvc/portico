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
package org.portico.utils.classpath;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class contains a bunch of utility methods for querying and managing the system classpath.
 * Methods for extending it and finding out what locations are on the search path are provided.
 * As instances of this class all work on the same underlying data (the system classpath), changes
 * made through one instance will be reflected in the others.
 * <p/>
 * NOTE: This uses a dirty reflection hack to extend the system classpath.
 */
public class Classpath
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Method addURL;
	private URLClassLoader systemLoader;
	private FilenameFilter filter;
	private Logger logger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Creates a new {@link Classpath} instance, fetching the system classloader and setting up all
	 * the necessary internal data. If there is a problem with this process a
	 * {@link ClasspathException} will be thrown.
	 */
	public Classpath() throws ClasspathException
	{
		this.logger = LogManager.getFormatterLogger( "portico.container" );
		
		// get the system class loader and ensure that it is a URLClassLoader
		if( (ClassLoader.getSystemClassLoader() instanceof URLClassLoader) == false )
		{
			throw new ClasspathException( "System classloader isn't a URLClassLoader" );
		}
		
		systemLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
		
		// prefetch the addURL method for later invocation
		try
		{
			addURL = URLClassLoader.class.getDeclaredMethod( "addURL", new Class[] {URL.class} );
			addURL.setAccessible( true ); // SOOOOOOO DIRTY!!!!!!!!
		}
		catch( Exception e )
		{
			// this shouldn't occur, but throw a RuntimeException if it does.
			throw new ClasspathException( "Error creating Classpath instance: "+e.getMessage(), e );
		}
		
		// create the filename filter
		filter = new FilenameFilter()
		{
			public boolean accept( File file, String name )
			{
				if( name.endsWith(".jar") || name.endsWith(".zip") )
					return true;
				else
					return false;
			}
		};
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * This method will extend the *system* classpath, adding the given URL. If there is an error
	 * doing so, a {@link ClasspathException} will be thrown. 
	 */
	public void extend( URL url ) throws ClasspathException
	{
		try
		{
			// invoke the method
			addURL.invoke( systemLoader, new Object[] {url} );
			logger.trace( "Extended system classpath with location: " + url );
		}
		catch( Exception e )
		{
			throw new ClasspathException( "Can't extend classpath: " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method will extend the *system classpath*, adding the given location. It will first
	 * turn the file into a URL and then pass it to {@link #extend(URL)}.
	 */
	public void extend( File file ) throws ClasspathException
	{
		try
		{
			extend( file.toURI().toURL() );
		}
		catch( MalformedURLException e )
		{
			throw new ClasspathException( "Can't extend classpath: " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method will extend the *system classpath*, adding the given location. It will first
	 * turn the location into a URL and then pass it to {@link #extend(URL)}.
	 */
	public void extend( String location ) throws ClasspathException
	{
		extend( new File(location) );
	}

	/**
	 * This is the same as {@link #scan(File,boolean)} (except that it will first turn the given
	 * location into a java.io.File). This method will return an a set of all the locations it
	 * finds and adds to the classpath. If there are no files to add, an empty set will be returned
	 */
	public Set<File> scan( String location, boolean recursive ) throws ClasspathException
	{
		return scan( new File(location), recursive );
	}
	
	/**
	 * This method scans the given location for jar files. Each file found will be added to the
	 * system classpath.
	 * <p/>
	 * If the given location is a file, it will be added to the path and the method will exit
	 * <p/>
	 * If the given location is a directory, it will be searched for all containing .jar and .zip
	 * files, with each being added to the classpath. Should the <code>recursive</code> flag be
	 * set to true, all contained subdirectories will also be scanned.
	 * <p/>
	 * Should any of the files or directories not be readable, they will be ignored.
	 * 
	 * @param location The location to scan for jar/zip files
	 * @param recursive Should all the directories contained within the given directory also be
	 *                  scanned themselves
	 * @return The set of all files found and added to the classpath
	 */
	public Set<File> scan( File location, boolean recursive ) throws ClasspathException
	{
		logger.trace( "(JarScan) Scanning (recursive="+recursive+"): location="+location );
		
		Set<File> returnSet = new HashSet<File>();
		
		// can we read the location? if not, no point scanning it
		if( location.canRead() == false || location.isHidden() )
			return returnSet;

		// is it a directory? if it is not, add the location to the classpath and exit
		if( location.isDirectory() == false )
		{
			// add the location and return
			if( containsPath(location) == false )
				extend( location );

			returnSet.add( location );
			return returnSet;
		}

		// we have a directory, find all the jar/zip files it contains
		File[] archives = location.listFiles( filter );
		for( File temp : archives )
		{
			// ensure that we don't already have this on the classpath
			if( containsPath(temp) == false )
			{
				extend( temp );
				returnSet.add( temp );
			}
		}

		// if we have been told to recurse into subdirectories, find and scan them
		if( recursive )
		{
			File[] contents = location.listFiles();
			for( File temp : contents )
			{
				if( temp.isDirectory() && temp.canRead() )
					returnSet.addAll( scan(temp,recursive) );
			}
		}
		
		return returnSet;
	}
	
	/**
	 * This method will return the current search path as a set of strings.
	 */
	public String[] getSearchPath()
	{
		URL[] urls = systemLoader.getURLs();
		String[] path = new String[urls.length];
		for( int i = 0; i < urls.length; i++ )
		{
			path[i] = urls[i].toExternalForm();
		}
		
		return path;
	}

	/**
	 * This method will return the current search path as a set of URLs
	 */
	public URL[] getUrlSearchPath()
	{
		return systemLoader.getURLs();
	}
	
	/**
	 * Returns true if the system classpath contains the given URL. False otherwise.
	 */
	public boolean containsPath( URL url )
	{
		for( URL temp : systemLoader.getURLs() )
		{
			if( temp.equals(url) )
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns true of the system classpath contains the given File. False otherwise.
	 */
	public boolean containsPath( File file )
	{
		String absolutePath = file.getAbsolutePath();
		
		for( URL temp : systemLoader.getURLs() )
		{
			try
			{
				String tempPath = new File(temp.toURI()).getAbsolutePath();
				if( tempPath.equals(absolutePath) )
					return true;
			}
			catch( Exception e )
			{
				continue;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns true if the system classpath contains the given location. The location will first
	 * be turned into a java.io.File and then {@link #containsPath(File)} will be called.
	 */
	public boolean containsPath( String location )
	{
		return containsPath( new File(location) );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

