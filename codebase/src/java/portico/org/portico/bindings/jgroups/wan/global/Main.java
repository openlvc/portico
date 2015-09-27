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
		try
		{
			Configuration configuration = Configuration.parse( args );
    		System.out.println( "Starting Portico WAN Router. Press \"x\" to exit" );
    		System.out.println( "Configuration" );
    		System.out.println( "  - Address: "+configuration.getAddress() );
    		System.out.println( "  -    Port: "+configuration.getPort() );
    		System.out.println( "" );
    		
    		
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
    				System.out.println( "Unknown command: ["+command+"]" );
    		}
    		while( keepalive );
    		
    		System.out.println( "Shutting down" );
    		server.shutdown();
    		System.out.println( "Exiting" );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
