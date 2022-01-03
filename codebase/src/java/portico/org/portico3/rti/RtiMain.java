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
package org.portico3.rti;

import org.portico3.Container;
import org.portico3.rti.commandline.CommandLine;

/**
 * Main launcher class for the RTI server. The {@link #main(String[])} method will pass control
 * directly to the {@link Container} class that will then handle all the command-line argument
 * processing and bootstrapping. 
 */
public class RtiMain
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
	public static void main( String[] args ) throws Exception
	{
		// Initialize environment
		// The container will parse config the set up logging
		System.setProperty( "java.net.preferIPv4Stack", "true" );

		//if( args.length == 0 )
		//	System.out.println( SystemInformation.getSystemInformationSummary() );

		for( String arg : args )
		{
			if( arg.equalsIgnoreCase("--help") )
			{
				System.out.println( CommandLine.getUsage() );
				return;
			}
		}
		
		// Load the Container and get a new RTI instance from it
		RtiServer server = Container.instance().createRti( args );
		
		// Initialize and start the server
		server.initialize();
		server.start();
	}
}
