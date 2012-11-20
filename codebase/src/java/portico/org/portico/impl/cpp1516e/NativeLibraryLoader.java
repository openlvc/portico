/*
 *   Copyright 2012 The Portico Project
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
package org.portico.impl.cpp1516e;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This class is responsbile for loading the native libraries that contain the C++ side
 * of the Portico interface bindings. When a C++ federate is started, it creates a JVM
 * and calls into it to load the standard Portico infrastructure. From here, to allow
 * callbacks to go back from Java to C++, the Java side has to load the C++ native libraries
 * as well. This class is responsible for doing this on all platform.
 */
public class NativeLibraryLoader
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Load the correct libraries for the given platform, delegating to the various
	 * platform dependent methods as required.
	 */
	private void loadNativeLibraries()
	{
		boolean windows = System.getProperty("os.name").contains("indows");
		this.logger = Logger.getLogger( "portico.lrc.cpp1516e" );
		this.logger.setLevel( Level.ERROR );
		
		if( windows )
			loadWindowsLibraries();
		else
			loadUnixLibraries();
	}

	/**
	 * This method will try and load the library of the given name. If it fails, a message will
	 * be logged at debug level (containing the description) and this method will return false.
	 */
	private boolean loadLibrary( String libraryName, String libraryDescription )
	{
		try
		{
			System.loadLibrary( libraryName );
			return true;
		}
		catch( UnsatisfiedLinkError ule )
		{
			// dammit!
			logger.debug( "Could not load "+libraryDescription );
			return false;
		}
		catch( Throwable e )
		{
			logger.debug( "Unknown error while loading native library "+libraryDescription, e );
			return false;
		}
	}

	private void loadWindowsLibraries()
	{
		// When loading the C++ side of the interface, there are a number of different libraries
		// that we may have to load:
		//   * IEEE-1516e 32-bit (librti1516e.dll)
		//   * IEEE-1516e 64-bit (librti1516e_64.dll)
		//
		// Work through these in order until we get something that loads
		// We start by assuming that the path is set up correctly. If that fails, we
		// then try and manually determine the path from RTI_HOME
		logger.debug( "Attempting to load Portico C++ native interface libraries" );
		
		// IEEE-1516e 32-bit (librti1516e.dll)
		if( loadLibrary("librti1516e","IEEE-1516e 32-bit library (librti1516e.dll)") )
			return;
		
		// IEEE-1516e 64-bit (librti1516e_64.dll)
		if( loadLibrary("librti1516e_64","IEEE-1516e 64-bit library (librti1516e_64.dll)") )
			return;

		// Couldn't find what we're after, see if we can do better manually trying to
		// figure out what library to load via RTI_HOME
		String rtiHome = System.getenv( "RTI_HOME" );
		logger.debug( "Using RTI_HOME value: "+rtiHome );
		if( rtiHome == null )
			rtiHome = System.getProperty("user.dir"); // PWD
		
		logger.debug( "Could not load native libraries via system path, trying to auto-detect path" );
		logger.debug( "Using RTI_HOME: "+rtiHome );

		// IEEE-1516e 32-bit VC10 (librti1516e.dll)
		String name = rtiHome+"\\bin\\vc10\\librti1516e.dll";
		if( loadLibrary(name,"IEEE-1516e 32-bit VC10 library (librti1516e.dll)") )
			return;
		
		// IEEE-1516e 64-bit VC10 (librti1516e_64.dll)
		name = rtiHome+"\\bin\\vc10\\librti1516e_64.dll";
		if( loadLibrary(name,"IEEE-1516e 64-bit VC10 library (librti1516e_64.dll)") )
			return;

		// fail!
		System.out.println( "ERROR Could not locate Portico C++ library for IEEE-1516e" );
		System.out.println( "ERROR Make sure %RTI_HOME% is set and "+
		                    "%RTI_HOME%\\bin\\[compiler] is on you %PATH%" );
	}
	
	private void loadUnixLibraries()
	{
		// When loading the C++ side of the interface, there are a number of different libraries
		// that we may have to load:
		//   * IEEE-1516e 32-bit (librti1516e)
		//   * IEEE-1516e 64-bit (librti1516e_64)
		//
		// Work through these in order until we get something that loads
		// We start by assuming that the path is set up correctly. If that fails, we
		// then try and manually determine the path from RTI_HOME
		logger.debug( "Attempting to load Portico C++ native interface libraries" );
		
		// IEEE-1516e 32-bit (librti1516e)
		if( loadLibrary("rti1516e","IEEE-1516e 32-bit library (librti1516e)") )
			return;
		
		// IEEE-1516e 64-bit (librti1516e_64)
		if( loadLibrary("rti1516e_64","IEEE-1516e 64-bit library (librti1516e_64)") )
			return;
		
		System.out.println( "ERROR Could not locate Portico C++ library for IEEE-1516e" );
		System.out.println( "ERROR Make sure $RTI_HOME is set and $RTI_HOME/lib/[compiler] is on you $LD_LIBRARY_PATH" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void load()
	{
		new NativeLibraryLoader().loadNativeLibraries();
	}
}
