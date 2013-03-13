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
package org.portico.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.portico.lrc.compat.JConfigurationException;

/**
 * Provides static helper methods for locating and loading system resources. 
 */
public class ResourceLocator
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	///////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Resource Locating Methods ////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Locate the resource of the given name using the ClassLoader that loaded this class. If there
	 * is no resource by that name, null will be returned.
	 */
	public static URL locateResource( String resource )
	{
		return locateResource( resource, ResourceLocator.class.getClassLoader() );
	}
	
	/**
	 * Using the given ClassLoader, locate the resource of the given name. If none can be found,
	 * null will be returned. 
	 */
	public static URL locateResource( String resource, ClassLoader loader )
	{
		if( loader == null || resource == null )
		{
			return null;
		}
		
		if( resource == null || resource.trim().equals("") )
		{
			return null;
		}
		
		return loader.getResource( resource );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Property File Loading Methods //////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will attempt to take all the properties identified in the given file and load
	 * them as system properties. If the file cannot be open or read, or an error occurs during
	 * the process, a {@link JConfigurationException} will be thrown.
	 *  
	 * @param filename The name of the properties file to load
	 */
	public static Properties loadPropertiesFile( String filename ) throws JConfigurationException
	{
		return loadPropertiesFile( filename, "" );
	}

	/**
	 * Load the specified properties file into a properties instance, prefixing all found
	 * properties with the given string. If the string is null or empty, no prefix will be added.
	 * If the prefix contains a value, it will be put on the front of each key, separated by a "."
	 * character. For example, the prefix of "portico" would cause "portico." to be prefixed to
	 * all keys found in the properties file.
	 * 
	 * If the file cannot be open or read, or a general error occurs, a
	 * {@link JConfigurationException} will be thrown.
	 *  
	 * @param filename The name of the properties file to load
	 * @param prefix The prefix to apply to all properties found in the specified properties file
	 */
	public static Properties loadPropertiesFile( String filename, String prefix )
		throws JConfigurationException
	{
		// check to see if the file exists //
		File file = new File( filename );
		if( file.canRead() == false )
		{
			throw new JConfigurationException( "The properties file [" + filename +
			                                   "] either does not exist or cannot be read" );
		}
		
		// open the file and turn it into a properties instance //
		Properties properties = new Properties();
		try
		{
			properties.load( new FileInputStream(file) );
		}
		catch( IOException io )
		{
			throw new JConfigurationException( "Error opening properties file [" +
			                                   file.getAbsolutePath() + "]: "+ io.getMessage(), io );
		}
		
		// apply the prefix
		if( prefix != null && !prefix.trim().isEmpty() )
		{
			prefix = prefix.trim();
			Properties prefixedProperties = new Properties();
			for( Object key : properties.keySet() )
			{
				prefixedProperties.put( prefix+"."+key.toString(), properties.get(key) );
			}
			
			// replace the properties with the new set
			properties = prefixedProperties;
		}
		
		// load the properties as system properties //
		System.getProperties().putAll( properties );
		return properties;
	}
	
	/**
	 * This method will attempt to take all the properties identified in the given resource and
	 * load them as system properties. If the resource cannot be open or read, or an error occurs
	 * during the process, a {@link JConfigurationException} will be thrown.
	 * <p/>
	 * <b>NOTE:</b> This differs from {@link #loadPropertiesFile(String)} in that it will try to
	 * find the file as a system resource (located on the classpath and within jar/archives on the
	 * classpath) where the other method will only load a file that exists direclty on the
	 * filesystem.
	 *  
	 * @param resource The name of the resource representing the properties to load
	 * @return The properties that were loaded
	 */
	public static Properties loadPropertiesResource( String resource ) throws JConfigurationException
	{
		return loadPropertiesResource( resource, "" );
	}

	/**
	 * Load the specified properties resource into a properties instance, prefixing all found
	 * properties with the given string. If the string is null or empty, no prefix will be added.
	 * If the prefix contains a value, it will be put on the front of each key, separated by a "."
	 * character. For example, the prefix of "portico" would cause "portico." to be prefixed to
	 * all keys found in the properties file.
	 * 
	 * If the file cannot be open or read, {@link JConfigurationException} will be thrown.
	 * 
	 * NOTE: This differs from {@link #loadPropertiesFile(String)} in that it will try to
	 * find the file as a system resource (located on the classpath and within jar/archives on the
	 * classpath) where the other method will only load a file that exists direclty on the
	 * filesystem.
	 *  
	 * @param resource The name of the resource representing the properties to load
	 * @param prefix The prefix to apply to all properties found in the specified properties file
	 */
	public static Properties loadPropertiesResource( String resource, String prefix )
		throws JConfigurationException
	{
		// find the resource //
		URL url = ResourceLocator.locateResource( resource );
		if( url == null )
		{
			throw new JConfigurationException( "The resource ["+resource+"] could not be located" );
		}
		
		// load the resource into a set of properties //
		Properties properties = new Properties();
		try
		{
			properties.load( url.openStream() );
		}
		catch( IOException io )
		{
			throw new JConfigurationException( "Error opening properties resource [" + url + "]: " +
			                                   io.getMessage(), io );
		}
		
		// apply the prefix
		if( prefix != null && !prefix.trim().isEmpty() )
		{
			prefix = prefix.trim();
			Properties prefixedProperties = new Properties();
			for( Object key : properties.keySet() )
			{
				prefixedProperties.put( prefix+"."+key.toString(), properties.get(key) );
			}
			
			// replace the properties with the new set
			properties = prefixedProperties;
		}

		// load the properties into the system properties and return
		System.getProperties().putAll( properties );
		return properties;
	}
}
