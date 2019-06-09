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
package org.portico2.rti;


import org.portico2.common.configuration.RID;
import org.portico2.common.configuration.commandline.Argument;
import org.portico2.rti.cli.RtiCli;

/**
 * The main class that starts the RTI server.
 */
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
		// Initialize environment
		// The container will parse config the set up logging
		System.setProperty( "java.net.preferIPv4Stack", "true" );

		// Check for the --help command line arg and bail out early if its there
		for( String argument : args )
		{
			if( argument.contains("--help") )
			{
				System.out.println( Argument.getCommandLineHelp() );
				return;
			}
		}

		// Load up our configuration data
		// RID file may be specified on command line, in env var or from default location
		// See javadoc on RID.loadRid() for more details
		RID rid = RID.loadRid( args );

		// create and launch the RTI process
		RTI rti = new RTI( rid );
		rti.startup();
		
		// keep the RTI active - in the future we will start a simple command line processor here
		RtiCli cli = new RtiCli( rti );
		cli.start();
	}
}
