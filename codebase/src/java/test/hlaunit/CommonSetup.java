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
package hlaunit;

import java.io.File;

import org.portico.bindings.jgroups.Configuration;
import org.portico.bindings.jvm.JVMConnection;
import org.portico.lrc.PorticoConstants;
import org.portico.utils.logging.Log4jConfigurator;
import org.testng.Assert;

public class CommonSetup
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/** The root directory from which any files that need to be loaded should be prefixed */
	public static final String TEST_ROOT_DIR = System.getProperty( "test.root.dir" );
	
	/**
	 * The system property that specifies the binding that will be used for test federates. If
	 * the value of the property is null, it is ignored. If it is one of the predefined values
	 * (jvm or jsop), then the appropriate binding is used. If it is neither of these, the value
	 * is taken to be the fully qualified name of the binding implementation to use. 
	 */
	public static final String BINDING_PROPERTY = "test.binding";
	
	public static long TIMEOUT = 1000;

	private static final String FILE_LOG_LEVEL = System.getProperty( "test.fileLogLevel","no" );

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
	public static void commonBeforeSuiteSetup()
	{
		///////////////////////////////////////////////////////////////////////////////
		// set the global log level based off the level provided on the command line //
		///////////////////////////////////////////////////////////////////////////////
		// set the log level if it has been specified in the system properties
		String loglevel = System.getProperty( "test.loglevel", "OFF" );
		if( loglevel.equals("${test.loglevel}") )
				loglevel = "OFF";
		
		// set the global log level based off the given level above
		System.setProperty( PorticoConstants.PROPERTY_PORTICO_LOG_LEVEL, loglevel );
		
		/////////////////////////////////////////////////////////////////////////////
		// set up the plugins directory, otherwise the handlers might not be found //
		/////////////////////////////////////////////////////////////////////////////
		File plugindir = new File( System.getProperty("user.dir") + "/build/java/portico/classes" );
		System.setProperty( PorticoConstants.PROPERTY_PLUGIN_PATH, plugindir.getAbsolutePath() );
		
		try
		{
			/////////////////////////////////////////////////////////////////
			// do some environment setup based on the binding we are using //
			/////////////////////////////////////////////////////////////////
			String binding = System.getProperty( BINDING_PROPERTY, "jvm" );
			if( binding.equals( "jvm" ) || binding == null )
			{
				//////////////////
				// BINDING: JVM //
				//////////////////
				// set the conneciton implementation that federates should use
				System.setProperty( PorticoConstants.PROPERTY_CONNECTION,
				                    JVMConnection.class.getCanonicalName() );
				TIMEOUT = JVMConnection.CONNECTION_TIMEOUT;
			}
			else if( binding.equals("jgroups") )
			{
				// set the system property that contains the connection implementation
				System.setProperty( PorticoConstants.PROPERTY_CONNECTION,
				                    "org.portico.bindings.jgroups.JGroupsConnection" );
				TIMEOUT = 100;

				// set the system property to reduce the jgroups GMS discovery timeout
				// this can take a while and really slows the tests down, but seeing as
				// we're running everything off the same machine, there is little worry
				// about needing a bigger timeout to discover an active group
				System.setProperty( Configuration.PROP_JGROUPS_GMS_TIMEOUT, "100" );
			}
			else if( binding.equals("ptalk") )
			{
				System.setProperty( PorticoConstants.PROPERTY_CONNECTION,
				                    "org.portico.bindings.ptalk.LrcConnection" );
			}
			else
			{
				/////////////////////
				// UNKNOWN BINDING //
				/////////////////////
				// a value has been set, but we don't recognise it, assume it is the fully
				// qualified name of the binding class to use
				System.setProperty( PorticoConstants.PROPERTY_CONNECTION, binding );
			}

		}
		catch( Exception e )
		{
			// Couldn't create the RTI, fail the test
			Assert.fail( "Error during test initialization: " + e.getMessage(), e );
		}
	}
	
	public static void commonAfterSuiteCleanup()
	{

	}
	
	public static void testStarting( String className, String methodName )
	{
		// don't set things up unless per-test log files are enabled
		if( FILE_LOG_LEVEL.equals("${test.fileLogLevel}") || FILE_LOG_LEVEL.equals("no") )
			return;

		String testSuite = System.getProperty( "test.suite", "unknownSuite" );
		String filename = "logs/"+testSuite+"/"+className+"/"+methodName+".log";
		Log4jConfigurator.redirectFileOutput( filename, false );
	}
}
