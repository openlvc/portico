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

import org.portico3.common.logging.Log4jConfigurator;
import org.portico3.common.utils.SystemInformation;
import org.portico3.rti.RtiMain;

/**
 * <p>Portico Launcher. This class is also set as the main class for the jar file.</p>
 * 
 * <p>If invoked with no arguments, it will print some diagnostic information. If it is invoked
 * with arguments, the first argument will be considered the "application" we want to load. Once
 * we've identified the application we can hand execution off to its main class.</p>
 * 
 * The supported applications are:
 * <ul>
 *     <li>      "rti": The RTI server</li> 
 *     <li>"wanrouter": The Portico WAN Router for connecting disconnected sites</li>
 * </ul>
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
	public static void main( String[] args ) throws Exception
	{
		// Initialize environment
		// The container will parse config the set up logging
		System.setProperty( "java.net.preferIPv4Stack", "true" );

		if( args.length == 0 )
		{
			System.out.println( SystemInformation.getSystemInformationSummary() );
			return;
		}
		
		// Find the application we need to launch
		// Prepare its command line (current line, less the application indicator)
		String application = args[0];
		String[] remaining = new String[args.length-1];
		for( int i = 1; i < args.length; i++ )
			remaining[i-1] = args[i];

		// Launch the appropriate application
		if( args[0].equals("rti") )
		{
			RtiMain.main( remaining );
		}
		else if( args[0].equals("wanrouter") )
		{
			/////////////////////////////////
			// Application: WAN Router   ////
			/////////////////////////////////
			// configure logging first
			Log4jConfigurator.DEFAULT_PATTERN = "%-5p [%c]: %x%m%n";
//			Container.instance();
			Log4jConfigurator.setLevel( "INFO", "org.jgroups" );
			org.portico.bindings.jgroups.wan.global.Main.main( args );
			return;
		}
	}
}
