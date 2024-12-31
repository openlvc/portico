/*
 *   Copyright 2006 The Portico Project
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
package hlaunit.hla13.time;

import hla.rti.AsynchronousDeliveryAlreadyEnabled;
import hla.rti.FederateNotExecutionMember;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"AsyncTest", "async"})
public class AsyncTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate secondFederate;

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
		this.secondFederate = new Test13Federate( "secondFederate", this );
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
	// public void enableAsynchronousDelivery()
	//     throws AsynchronousDeliveryAlreadyEnabled,
	//            FederateNotExecutionMember,
	//            SaveInProgress,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

	///////////////////////////////////////////////////////////////////
	// TEST (valid): testAsynchronouslessDeliveryForNonSpecMessage() //
	///////////////////////////////////////////////////////////////////
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
	
	/////////////////////////////////////////////////////////////
	// TEST (valid): testAsynchronousDeliveryWithSpecMessage() //
	/////////////////////////////////////////////////////////////
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
	
	///////////////////////////////////////////////////
	// TEST(valid): testEnableAsynchronousDelivery() //
	///////////////////////////////////////////////////
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

	//////////////////////////////////////////////////////////////
	// TEST: testEnableAsynchronousDeliveryWhenAlreadyEnabled() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testEnableAsynchronousDeliveryWhenAlreadyEnabled()
	{
		defaultFederate.quickEnableAsyncDelivery();
		try
		{
			defaultFederate.rtiamb.enableAsynchronousDelivery();
			expectedException( AsynchronousDeliveryAlreadyEnabled.class );
		}
		catch( AsynchronousDeliveryAlreadyEnabled adae )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AsynchronousDeliveryAlreadyEnabled.class );
		}
	}

	/////////////////////////////////////////////////////////
	// TEST: testEnableAsynchronousDeliveryWhenNotJoined() //
	/////////////////////////////////////////////////////////
	@Test
	public void testEnableAsynchronousDeliveryWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			defaultFederate.rtiamb.enableAsynchronousDelivery();
			expectedException( FederateNotExecutionMember.class );
		}
		catch( FederateNotExecutionMember fnem )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, FederateNotExecutionMember.class );
		}
	}
	
	//////////////////////////////////////////////////////////////
	// TEST: testEnableAsynchronousDeliveryWhenSaveInProgress() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testEnableAsynchronousDeliveryWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.enableAsynchronousDelivery();
			expectedException( SaveInProgress.class );
		}
		catch( SaveInProgress sip )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, SaveInProgress.class );
		}
	}

	/////////////////////////////////////////////////////////////////
	// TEST: testEnableAsynchronousDeliveryWhenRestoreInProgress() //
	/////////////////////////////////////////////////////////////////
	@Test
	public void testEnableAsynchronousDeliveryWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.enableAsynchronousDelivery();
			expectedException( RestoreInProgress.class );
		}
		catch( RestoreInProgress rip )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RestoreInProgress.class );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
