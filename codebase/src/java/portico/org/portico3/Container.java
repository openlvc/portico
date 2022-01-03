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
package org.portico3;

import java.io.File;

import org.portico3.common.compatibility.JConfigurationException;
import org.portico3.common.compatibility.JException;
import org.portico3.common.rid.RID;
import org.portico3.rti.RtiServer;
import org.portico3.rti.commandline.Argument;
import org.portico3.rti.commandline.CommandLine;

/**
 * The {@link Container} has all the system bootstrapping and factory methods required to
 * bootstrap and create new {@link RtiServer} and LRC instances. The creation methods will
 * perform initial command-line handling, environmental parsing and so on. It will also locate
 * and parse the RID file that should be used in the creation of an new RTI/LRC. 
 */
public class Container
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static volatile Container INSTANCE; // lazy-load

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// We don't actually store the RID content - we parse them each time
	private String ridFilePath;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private Container() throws JConfigurationException
	{
		// Local the system RID file, either from an environment variable, or from a location
		// relative to the executable. If none is found, the system defaults are used.
		this.ridFilePath = findRidFile();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Create a new {@link RtiServer} that has been populated with an appropriate RID file and
	 * the given command line arguments. If the location of the {@link RID} file is specified in
	 * the arguments, that is the path that is used to parse/load the file from, otherwise we 
	 * fall back to the default RID path as determined by the {@link Container}.
	 * 
	 * @param args The arguments to parse and given to the RTI
	 * @return An {@link RtiServer} instance populated with the RID and command line args
	 * @throws JConfigurationException If there is a problem parsing either the RID or command line
	 */
	public RtiServer createRti( String[] args ) throws JConfigurationException
	{
		// Load the command line arguments into something we can use first
		CommandLine commandline = new CommandLine( args );
		
		// If the command line arguments specify a RID file, use that, otherwise, use the default
		RID rid = null;
		if( commandline.isPresent(Argument.RidFile) )
			rid = RID.load( commandline.getValue(Argument.RidFile) );
		else if( ridFilePath != null )
			rid = RID.load( ridFilePath );
		else
			rid = new RID();
		
		// Override RID properties with any command line arguments
		rid.applyOverrides( commandline );
		
		// Create the RTI and return it
		return new RtiServer( rid, commandline );
	}
	
	/**
	 * Find the default RID and make sure we can parse it. When looking for the RID, we use the
	 * given process:
	 * 
	 * <ul>
	 *   <li>If the <code>RTI_RID_FILE</code> environment variable is set and points to a file
	 *       path that exists, use this as the default RID path.</li>
	 *   <li>If the environment variable is NOT set, or the file it points to does NOT exist,
	 *       fall back to the default relative path of <code>./RTI.rid</code>.</li>
	 *   <li>If <code>./RTI.rid</code> doesn't exist, fall back to the default RID settings
	 *       as hard-coded into the class itself.</li>
	 * </ul>
	 * 
	 * @throws JConfigurationException If there is a problem parsing any of the found RID files
	 *                                 (such as syntax errors).
	 */
	private String findRidFile() throws JConfigurationException
	{
		// Check the RTI_RID_FILE environment variable to see if it has specified a RID
		String envRidPath = System.getenv( "RTI_RID_FILE" );
		if( envRidPath != null )
		{
			// The value has been specified, make sure it exists
			File envRidFile = new File( envRidPath );
			if( envRidFile.exists() )
			{
				// make sure it loads without exception
				RID.load( envRidPath );
				
				// now that we know it loads, store its path
				return envRidPath;
			}
			else
			{
				System.err.println( "WARNING: Environment variable RTI_RID_FILE points to RID "+
				                    "file that doesn't exist: "+envRidFile.getAbsolutePath() );
				// fall through to try and find the RID relative to the launch directory
			}
		}
		
		// Fall through and try to find the system RID relative to the launch directory
		File relativeRidFile = new File( "RTI.xml" );
		if( relativeRidFile.exists() )
		{
			// make sure it loads without exception
			RID.load( "RTI.xml" );
			
			// now that we know it loads, store its path
			return "RTI.xml";
		}
		
		// No system RID was found - we'll just have to rely on hard-coded defaults
		return null;
	}
	
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * @return The singleton {@link Container} instance.
	 */
	public static synchronized final Container instance()
	{
		try
		{
    		if( INSTANCE == null )
    			INSTANCE = new Container();
    		
    		return INSTANCE;
		}
		catch( JException je )
		{
			throw new RuntimeException( je.getMessage(), je );
		}
    }

}
