/*
 *   Copyright 2007 The Portico Project
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
package hlaunit.ieee1516.time;

import hlaunit.ieee1516.common.Abstract1516Test;
import hlaunit.ieee1516.common.TestFederate;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"AsyncTest", "async"})
public class AsyncTest extends Abstract1516Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate secondFederate;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	@BeforeClass(alwaysRun=true)
	public void beforeClass()
	{
		super.beforeClass();
		this.secondFederate = new TestFederate( "secondFederate", this );
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		secondFederate.quickResign();
		defaultFederate.quickResign();
		defaultFederate.quickDestroy();
	}
	
	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////
	// TEST: testAsynchronouslessDeliveryForNonSpecMessage() //
	///////////////////////////////////////////////////////////
	/**
	 * Tests that non specification defined "messages" can be delivered, even if asynchronous
	 * delivery isn't enabled and no advance is in progress.
	 */
	@Test
	public void testAsynchronouslessDeliveryForNonSpecMessage()
	{
		// initialize the test //
		defaultFederate.quickEnableConstrained();
		
		// test with a sync point announcement //
		secondFederate.quickAnnounce( "test", null );
		defaultFederate.fedamb.waitForSyncAnnounce( "test" );
		
		// test with a time advance request //
		defaultFederate.quickAdvanceAndWait( 10.0 );
	}
	
	/////////////////////////////////////////////////////
	// TEST: testAsynchronousDeliveryWithSpecMessage() //
	/////////////////////////////////////////////////////
	/**
	 * Tests that messages defined within the specification as "messages" (sec:3.1.52 in 1516) are
	 * not delivered to constrained, non-advancing federates unless asynchronous delivery is
	 * enabled.
	 */
	@Test
	public void testAsynchronousDeliveryWithSpecMessage()
	{
		// initialize the test //
		defaultFederate.quickPublish( "InteractionRoot.X" );
		secondFederate.quickSubscribe( "InteractionRoot.X" );
		
		// ensure that RO callbacks flow freely before the second federate is constrained //
		// send an interaction with the default federate //
		defaultFederate.quickSend( "InteractionRoot.X", null, null );
		secondFederate.fedamb.waitForROInteraction( "InteractionRoot.X" );
		
		// turn on constrained for the second federate and ensure that RO messages are not
		// received in the absence of an active time advance request
		secondFederate.quickEnableConstrained();
		
		// send an interaction in the default federate //
		defaultFederate.quickSend( "InteractionRoot.X", null, null );
		secondFederate.fedamb.waitForROInteractionTimeout( "InteractionRoot.X" );
		
		// request an advance and make sure we now get the interaction //
		secondFederate.quickAdvanceRequest( 100.0 );
		secondFederate.fedamb.waitForROInteraction( "InteractionRoot.X" );
	}
	
	////////////////////////////////////////////
	// TEST: testEnableAsynchronousDelivery() //
	////////////////////////////////////////////
	@Test(dependsOnMethods={"testAsynchronousDeliveryWithSpecMessage",
	                        "testAsynchronouslessDeliveryForNonSpecMessage"})
	public void testEnableAsynchronousDelivery()
	{
		// initialize the test //
		defaultFederate.quickPublish( "InteractionRoot.X" );
		secondFederate.quickSubscribe( "InteractionRoot.X" );
		secondFederate.quickEnableConstrained();
		
		// ensure that we don't get messages in the absence of an active advance request //
		defaultFederate.quickSend( "InteractionRoot.X", null, null );
		secondFederate.fedamb.waitForROInteractionTimeout( "InteractionRoot.X" );
		
		try
		{
			// disable async delivery //
			secondFederate.rtiamb.enableAsynchronousDelivery();
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during disable async: " + e.getMessage(), e );
		}
		
		// make sure we not get the RO message outside any advance request //
		secondFederate.fedamb.waitForROInteraction( "InteractionRoot.X" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
