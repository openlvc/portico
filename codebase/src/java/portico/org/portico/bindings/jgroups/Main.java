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
package org.portico.bindings.jgroups;

import org.portico.bindings.jgroups.channel.FederationChannel;
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
	public void run() throws Exception
	{
		for( int i = 0; i < 10; i++ )
			cycleApplication();

		//runSingleApplication();
		System.out.println( "Hit enter to exit" );
		System.console().readLine();
	}

	public void cycleApplication() throws Exception
	{
		System.out.println( "["+Thread.activeCount()+"] Starting application" );		
		FederationChannel federation = new FederationChannel( "test-federation" );
		federation.connect();
		federation.disconnect();
		System.out.println( "["+Thread.activeCount()+"] Finished application, waiting" );
		Thread.sleep( 10000 );
		System.out.println( "["+Thread.activeCount()+"] Waiting long enough" );
	}

	public void runSingleApplication() throws Exception
	{
		System.out.println( "["+Thread.activeCount()+"] Starting application" );
		sleep();
		
		FederationChannel federation = new FederationChannel( "test-federation" );
		System.out.println( "["+Thread.activeCount()+"] Created Federation" );
		
		federation.connect();
		System.out.println( "["+Thread.activeCount()+"] Connected to Federation" );
		sleep();

		federation.disconnect();
		System.out.println( "["+Thread.activeCount()+"] Disconnected from Federation" );
		sleep();
		sleep();

		System.out.println( "["+Thread.activeCount()+"] Shutting down application" );
		Thread.sleep( 10000 );
		System.out.println( "["+Thread.activeCount()+"] Exiting" );		
	}
	
	public void sleep() throws Exception
	{
		Thread.sleep( 2000 );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void main( String[] args ) throws Exception
	{
		//Log4jConfigurator.bootstrapLogging();
		//Log4jConfigurator.setLevel( "TRACE", "portico.lrc.jgroups", "org.jgroups" );

		new Main().run();
	}
}
