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

import org.portico2.common.PorticoConstants;
import org.portico2.common.logging.Log4jConfigurator;
import org.portico2.common.network.transport.multicast.JGroupsConfiguration;
import org.testng.Assert;

public class CommonSetup
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/** The root directory from which any files that need to be loaded should be prefixed */
	public static final String TEST_ROOT_DIR = System.getProperty( "test.root.dir" );
	
	/**
	 * The system property that specifies the connection type that will be used for test federates.
	 * If the value of the property is null, it is ignored. If it is one of the predefined values
	 * (jvm, multicast, unicast), then the appropriate connection type is used. If it is neither
	 * of these, the value is taken to be the fully qualified name of the IConnection
	 * implementation to use. 
	 */
	public static final String CONNECTION_PROPERTY = "test.connection";

	/**
	 * Which interface to tell JGroups to use. Can be: SITE_LOCAL, LINK_LOCAL, LOOPBACK, GLOBAL
	 * or the IP of the interface you want used.
	 */
	public static final String JGROUPS_INTERFACE = "jgroups.interface";

	/**
	 * Set to true when the JGroups binding is active. There are some instances in which we have
	 * to treat things slightly differently to keep the tests happy if we are using JGroups.
	 * These are largely inconsequential. For example, with JGroups we can't tell the difference
	 * between a federation that has been destroyed and one which simply has no federates. This
	 * can prevent us throwing a no-such-federation exception, which is nice, but has no significant
	 * consequence. Rather than get bent out of shape about this, we'll just selectively ignore
	 * these causes for JGroups.
	 */
	public static boolean JGROUPS_ACTIVE = false; 
	
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
		PorticoConstants.PORTICO_LOG_LEVEL = loglevel;
		
		//////////////////////////////////////////////////////////////////////////////
		// do special environmental setup based on the connection type we are using //
		//////////////////////////////////////////////////////////////////////////////
		try
		{
			// figure out what connection we are using, defaulting to JVM
			String connection = System.getProperty( CONNECTION_PROPERTY, "jvm" );
			if( connection == null || connection.equalsIgnoreCase("jvm") )
			{
				//
				// Connection: JVM
				//
				PorticoConstants.OVERRIDE_CONNECTION = "jvm";
				TIMEOUT = 100;
			}
			else if( connection.equalsIgnoreCase("multicast") )
			{
				//
				// Connection: Multicast (JGroups)
				//
				PorticoConstants.OVERRIDE_CONNECTION = "multicast";
				TIMEOUT = 100;
				
				// specify the join timeout
				JGroupsConfiguration.DEFAULT_GMS_JOIN_TIMOUT = "100";

				// specify the interface
				String jgroupsInterface = System.getProperty( JGROUPS_INTERFACE );
				if( jgroupsInterface != null )
				{
					System.out.println( "JGroups Interface: "+jgroupsInterface );
					JGroupsConfiguration.DEFAULT_INTERFACE = jgroupsInterface;
				}
			}
			else if( connection.equalsIgnoreCase("unicast") )
			{
				//
				// Connection: Unicast
				//
				PorticoConstants.OVERRIDE_CONNECTION = "unicast";
			}
			else
			{
				// Connection is not a pre-configured on. Assume it is the class name and use it
				Assert.fail( "Unknown connection type: "+connection );
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
