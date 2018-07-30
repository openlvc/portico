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
package org.portico2.aaa;

import java.util.Properties;

import org.portico2.common.configuration.RID;
import org.portico2.rti.RTI;

import hla.rti1516e.ObjectInstanceHandle;

public class Tester
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTI rti;
	private Federate federateOne;
	private Federate federateTwo;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private Tester()
	{
		
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private void run( String[] args ) throws Exception
	{
		//java.util.Properties props = System.getProperties();
		//for( Object key : props.keySet() )
		//{
		//	System.out.println( key +" = "+props.get(key) );
		//}
		
		// Define the properties we want to override
		Properties overrides = new Properties();
		overrides.put( "portico.loglevel", "TRACE" );
//		overrides.put( "rti.network.connections", "jvm" );
//		overrides.put( "rti.network.jvm.type", "jvm" );
//		overrides.put( "lrc.network.connection", "jvm" );
//		overrides.put( "lrc.network.jvm.type", "jvm" );
		String localSettings = overrides.toString();

		// Create the RTI
		RID rid = RID.loadRid( overrides );
		this.rti = new RTI( rid );
		this.rti.startup();

		// Create each of the federates
		this.federateOne = new Federate( localSettings );
		this.federateTwo = new Federate( localSettings );
		
		// Connect, Create the federation and join it
		this.federateOne.createAndJoin( "federateOne", "testFederation" );
		this.federateTwo.createAndJoin( "federateTwo", "testFederation" );
		
		// Time management
		this.federateOne.enableTimePolicy();
		this.federateTwo.enableTimePolicy();
		
		// Initial Sync
		this.federateOne.registerSyncPoint( "START" );
//		this.federateOne.registerSyncPoint( "START" ); // will fail
		this.federateOne.waitForAnnounce( "START" );
		this.federateTwo.waitForAnnounce( "START" );
		this.federateTwo.achieve( "START" );
		this.federateOne.achieve( "START" );
		this.federateOne.waitForSynchronized( "START" );
		this.federateTwo.waitForSynchronized( "START" );

		// Publication and Subscription
		this.federateOne.publishAndSubscribe();
		this.federateTwo.publishAndSubscribe();
		
		// Object Management
		ObjectInstanceHandle objectOne = this.federateOne.registerObject();
		ObjectInstanceHandle objectTwo = this.federateTwo.registerObject();
		this.federateOne.waitForDiscovery( objectTwo );
		this.federateTwo.waitForDiscovery( objectOne );
		
		for( int i = 0; i < 3; i++ )
		{
    		// Send an attribute update
    		long start = System.currentTimeMillis();
    		this.federateOne.updateObject();
    		this.federateTwo.updateObject();

    		// Send an Interaction
    		this.federateOne.sendInteraction();
    		this.federateTwo.sendInteraction();
			
			// Advance time
			this.federateOne.requestTimeAdvance( i+1 );
			this.federateTwo.requestTimeAdvance( i+1 );
			
			// Wait for reflections
    		this.federateOne.waitForReflection( objectTwo, start );
    		this.federateTwo.waitForReflection( objectOne, start );
			
			// Wait for advance grant
			this.federateOne.waitForTimeAdvance( i+1 );
			this.federateTwo.waitForTimeAdvance( i+1 );
		}    		

		// Delete the object
		this.federateOne.deleteObject( objectOne );
		this.federateTwo.deleteObject( objectTwo );
		this.federateOne.waitForDelete( objectTwo );
		this.federateTwo.waitForDelete( objectOne );
		
		// Tear things down
		this.federateOne.unpublishAndUnsubscribe();
		this.federateTwo.unpublishAndUnsubscribe();
		this.federateOne.resignAndDestroy();
		this.federateTwo.resignAndDestroy();
	
		try { Thread.sleep(5000); } catch( Exception e ) {}
		System.out.println( "Execution Over" );
		this.rti.shutdown();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void main( String[] args ) throws Exception
	{
		new Tester().run( args );
		
//		for( int i = 0; i < 10; i++ )
//		{
//			java.util.Random random = new java.util.Random();
//			long t0 = System.nanoTime();
//			for( int j = 0; j < 1000000; j++ )
//			{
////				int l = random.nextInt();
//				java.util.UUID uuid = java.util.UUID.randomUUID();
//				if( System.currentTimeMillis() < 0 )
////					System.out.println( l );
//					System.out.println( uuid.toString() );
//			}
//			System.out.println( "1M generated in "+(java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-t0))+"ms" );
//		}
	}
}
