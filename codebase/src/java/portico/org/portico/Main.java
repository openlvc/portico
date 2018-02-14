/*
 *   Copyright 2013 The Portico Project
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
package org.portico;

import org.portico.container.Container;
import org.portico.utils.SystemInformation;
import org.portico.utils.logging.Log4jConfigurator;

/**
 * `Main` is set as the Portico jar file's main class in the manifest. If invoked with no
 * arguments, it will just print some diagnostic information. If it is invoked with arguments,
 * the first argument will be considered the "application" we want to load. Currently, the
 * following applications are supported:
 * 
 *   - "wanrouter": The Portico WAN Router for connecting disconnected sites
 * 
 * If the application is known, processing, and all additional command line args will be
 * handed off the applicable application.
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

		if( args.length == 0 )
			System.out.println( SystemInformation.getSystemInformationSummary() );

		//
		// look for an application we support
		//
		if( args[0].equals("wanrouter") )
		{
			/////////////////////////////////
			// Application: WAN Router   ////
			/////////////////////////////////
			// configure logging first
			Log4jConfigurator.DEFAULT_PATTERN = "%-5p [%c]: %x%m%n";
			Container.instance();
			Log4jConfigurator.setLevel( "INFO", "org.jgroups" );
			org.portico.bindings.jgroups.wan.global.Main.main( args );
			return;
		}
		
		if( args[0].equalsIgnoreCase("rti") )
		{
			////////////////////////////////
			// Application: RTI Server  ////
			////////////////////////////////
			org.portico2.rti.Main.main( trimArgs(args,1) );
			return;
		}
		
		if( args[0].equalsIgnoreCase("forwarder") )
		{
			///////////////////////////////////////
			// Application: Portico Forwarder  ////
			///////////////////////////////////////
			org.portico2.forwarder.Main.main( trimArgs(args,1) );
			return;
		}

		
	}
	
	private static String[] trimArgs( String[] array, int reduceBy )
	{
		String[] newArray = new String[array.length-reduceBy];
		for( int i = reduceBy; i < array.length; i++ )
			newArray[i-reduceBy] = array[i];
		
		return newArray;
	}
}
