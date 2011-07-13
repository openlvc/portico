/*
 *   Copyright 2010 The Portico Project
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
package org.portico.bindings.ptalk.zz;

import java.io.File;

import org.portico.lrc.PorticoConstants;

import hla.rti.RTIambassador;
import hla.rti.jlc.RtiFactoryFactory;

public class Test
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
		System.setProperty( PorticoConstants.PROPERTY_PORTICO_LOG_LEVEL, "TRACE" );
		System.setProperty( PorticoConstants.PROPERTY_CONNECTION, "org.portico.bindings.ptalk.LrcConnection" );
		File plugindir = new File( System.getProperty("user.dir") + "/build/java/classes" );
		if( plugindir.exists() )
			System.setProperty( PorticoConstants.PROPERTY_PLUGIN_PATH, plugindir.getAbsolutePath() );
		
		// create the first RTIambassador
		System.out.println( "" );
		System.out.println( " ================================================================= " );
		System.out.println( "                  Creating first RTIambassador" );
		System.out.println( " ================================================================= " );
		System.out.println( "" );
		RTIambassador rtiamb = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();
		File fom = new File( "resources/test-data/fom/testfom.fed" );
		rtiamb.createFederationExecution( "test", fom.toURI().toURL() );
		
		// create the second RTIambassador
		sleep( 5000 );
		System.out.println( "" );
		System.out.println( " ================================================================= " );
		System.out.println( "                  Creating second RTIambassador" );
		System.out.println( " ================================================================= " );
		System.out.println( "" );
		RTIambassador rtiamb2 = RtiFactoryFactory.getRtiFactory().createRtiAmbassador();
		rtiamb2.createFederationExecution( "test", fom.toURI().toURL() ); // exception expected!
		
	}
	
	public static void sleep( long millis )
	{
		try
		{
			Thread.sleep( millis );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
