/*
 *   Copyright 2015 The Portico Project
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
package org.portico.bindings.jgroups.wan.global;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.utils.logging.Log4jConfigurator;

public class Main
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
	public static void main( String[] args )
	{
		//
		// Check to see if they have asked us to print our help information
		//
		for( String arg : args )
		{
			if( arg.trim().equalsIgnoreCase("--help") ||
				arg.trim().equalsIgnoreCase("-h") )
			{
				System.out.println( Configuration.Argument.toHelpString() );
				System.out.println( Configuration.getAvailableAddresses() );
				System.out.println( "" );
				return;
			}
		}

		//
		// Parse the Configuration
		//
		Configuration configuration;
		try
		{
			configuration = Configuration.parse( args );
		}
		catch( Exception e )
		{
			System.out.println( Configuration.Argument.toHelpString() );
			e.printStackTrace();
			return;
		}
		
		//
		// Start the WAN Router
		//
		Log4jConfigurator.setLevel( "INFO", "portico.wan" );
		Logger logger = LogManager.getFormatterLogger( "portico.wan" );
		try
		{

			// Fire the server up and start listening for the trigger to exit!
			Server server = new Server( configuration );
			server.startup();

			BufferedReader reader = new BufferedReader( new InputStreamReader(System.in) );
			boolean keepalive = true;
			do
			{
				String command = reader.readLine();
				if( command.equals("x") )
					keepalive = false;
				else
					logger.error( "Unknown command: ["+command+"]" );
			}
			while( keepalive );

			logger.info( "Received shutdown command" );
			server.shutdown();
			logger.info( "Router has shutdown" );
		}
		catch( Exception e )
		{
			logger.error( "Error occured: "+e.getMessage(), e );
		}
	}
}
