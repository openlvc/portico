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
package org.portico.impl.cpp13;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.lrc.PorticoConstants;

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
		this.logger = LogManager.getFormatterLogger( "portico.lrc.cpp13" );
		
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
			logger.debug( "SUCCESS (loadback) Loaded "+libraryDescription );
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
		logger.debug( "Attempting to load Portico C++ native interface libraries" );

		//////////////////////////////////////////////////
		// Build the library name based on architecture //
		//////////////////////////////////////////////////
		String libraryName = "";

		// get the interface name
		if( PorticoConstants.isCppHla13() )
			libraryName = "libRTI-NG";
		else
			libraryName = "librti13";
		
		// append for 64-bit
		if( PorticoConstants.isCpp64bit() )
			libraryName += "_64";
		
		// append for a debug library
		if( PorticoConstants.isCppDebugSession() )
			libraryName += "d";
		
		// generate a nice description string
		String libraryDescription = "HLA v1.3 C++ library ("+libraryName+".dll)";

		//////////////////////////////
		// Try and load the library //
		//////////////////////////////
		if( loadLibrary(libraryName,libraryDescription) )
			return;

		// Couldn't find what we're after, see if we can do better manually trying to
		// figure out what library to load via RTI_HOME
		String rtiHome = System.getenv( "RTI_HOME" );
		logger.debug( "Using RTI_HOME value: "+rtiHome );
		if( rtiHome == null )
			rtiHome = System.getProperty("user.dir"); // PWD
		
		logger.debug( "Could not load native libraries via system path, trying to auto-detect path" );
		logger.debug( "Using RTI_HOME: "+rtiHome );

		// try again
		String compiler = PorticoConstants.getCppCompilerString();
		String name = rtiHome+"\\bin\\"+compiler+"\\"+libraryName+".dll";
		if( loadLibrary(name,libraryDescription) )
			return;
		
		// fail!
		System.out.println( "ERROR (loadback) Could not locate HLA v1.3 C++ library ("+libraryName+".dll)" );
		System.out.println( "ERROR (loadback) Make sure %RTI_HOME% is set and %RTI_HOME%\\bin\\[compiler] is on you %PATH%" );
		System.out.println( "      (loadback) Search path: "+System.getProperty("java.library.path") );
	}
	
	private void loadUnixLibraries()
	{
		logger.debug( "Attempting to load Portico C++ native interface libraries" );

		//////////////////////////////////////////////////
		// Build the library name based on architecture //
		//////////////////////////////////////////////////
		String libraryName = "";

		// get the interface name
		if( PorticoConstants.isCppHla13() )
			libraryName = "RTI-NG";
		else
			libraryName = "rti13";
		
		// append for 64-bit
		if( PorticoConstants.isCpp64bit() )
			libraryName += "_64";
		
		// append for a debug library
		if( PorticoConstants.isCppDebugSession() )
			libraryName += "d";
		
		// generate a nice description string
		String libraryDescription = "lib"+libraryName+".so";

		//////////////////////////////
		// Try and load the library //
		//////////////////////////////
		if( loadLibrary(libraryName,libraryDescription) )
			return;
		
		System.out.println( "ERROR (loadback) Could not locate HLA v1.3 C++ library ("+libraryDescription+")" );
		System.out.println( "ERROR (loadback) Make sure $RTI_HOME is set and $RTI_HOME/lib/[compiler] is on you $LD_LIBRARY_PATH" );
		System.out.println( "      (loadback) Search path: "+System.getProperty("java.library.path") );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void load()
	{
		new NativeLibraryLoader().loadNativeLibraries();
	}
}
