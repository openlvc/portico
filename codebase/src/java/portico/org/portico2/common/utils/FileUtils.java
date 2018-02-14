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
package org.portico2.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.portico.lrc.compat.JConfigurationException;

public class FileUtils
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
	public static Properties loadPropertiesFile( File file ) throws JConfigurationException
	{
		// load the RID file into a property set
		try( FileInputStream fis = new FileInputStream(file) )
		{
			Properties properties = new Properties();
			properties.load( fis );
			return properties;
		}
		catch( FileNotFoundException fnfe )
		{
			throw new JConfigurationException( "File not found: "+file.getAbsolutePath() );
		}
		catch( IOException ioex )
		{
			throw new JConfigurationException( ioex,
			                                   "Error reading file [%s]: %s",
			                                   file.getAbsolutePath(),
			                                   ioex.getMessage() );
		}
	}
	
}
