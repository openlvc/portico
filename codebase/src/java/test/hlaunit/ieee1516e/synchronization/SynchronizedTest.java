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
package hlaunit.ieee1516e.synchronization;

import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.SynchronizationPointLabelNotAnnounced;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"SynchronizedTest",
                                   "syncPointAchieved",
                                   "synchronization",
                                   "federationManagement"})
public class SynchronizedTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate secondFederate;
	private byte[] tag = "le tag".getBytes();
	private String pointOne = "SyncPointOne";

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	@BeforeClass(alwaysRun=true)
	public void beforeClass()
	{
		super.beforeClass();
		secondFederate = new TestFederate( "federateTwo", this );
	}
	
	@Override
	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();

		// join the two federates //
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		defaultFederate.quickResign();
		secondFederate.quickResign();
		
		defaultFederate.quickDestroy();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void synchronizationPointAchieved( String synchronizationPointLabel )
	//        throws SynchronizationPointLabelNotAnnounced,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

	///////////////////////////////////////////
	// TEST: (valid) testAchievedSyncPoint() //
	///////////////////////////////////////////
	@Test
	public void testAchievedSyncPoint()
	{
		// register a federation wide synchronization point //
		defaultFederate.quickAnnounce( pointOne, tag );
		secondFederate.fedamb.waitForSyncAnnounce( pointOne );

		try
		{
			// 1. achieve the point in the first federate
			defaultFederate.rtiamb.synchronizationPointAchieved( pointOne );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while waiting attempting valid synchronize: " +
			             e.getMessage(), e );
		}
		
		// 2. wait for the point to be acheived - this should timeout
		defaultFederate.fedamb.waitForSynchronizedTimeout( pointOne );

		try
		{
			// 3. achieve the sync point in the second federate
			secondFederate.rtiamb.synchronizationPointAchieved( pointOne );
			
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while waiting attempting valid synchronize: " +
			             e.getMessage(), e );
		}

		// 4. make sure BOTH federates have recevied the "synchronized" callback
		defaultFederate.fedamb.waitForSynchronized( pointOne );
		secondFederate.fedamb.waitForSynchronized( pointOne );
	}

	////////////////////////////////////////////////
	// TEST: (valid) testAchievedGroupSyncPoint() //
	////////////////////////////////////////////////
	@Test
	public void testAchievedGroupSyncPoint()
	{
		// we need a third federate, so we can have two in the group but can exlucde one
		TestFederate thirdFederate = new TestFederate( "thirdFederate", this );
		thirdFederate.quickJoin();
		
		try
		{
    		// announce the point, but for a specific group
    		defaultFederate.quickAnnounce( pointOne,
    		                               tag,
    		                               defaultFederate.federateHandle,
    		                               thirdFederate.federateHandle );
    		
    		// make sure it is announced in the first and third federates, but not the second
    		defaultFederate.fedamb.waitForSyncAnnounce( pointOne );
    		thirdFederate.fedamb.waitForSyncAnnounce( pointOne );
    		secondFederate.fedamb.waitForSyncAnnounceTimeout( pointOne ); // timeout
    		
    		try
    		{
    			// achieve the point in the first, but make sure it doesn't synchronize
    			defaultFederate.rtiamb.synchronizationPointAchieved( pointOne );
    		}
    		catch( Exception e )
    		{
    			Assert.fail( "Unexpected exception in achieving sync point [" + pointOne + "]: " +
    			             e.getMessage(), e );
    		}
    		
    		// wait for everyont to synchronized, should timeout
    		defaultFederate.fedamb.waitForSynchronizedTimeout( pointOne );
    		thirdFederate.fedamb.waitForSynchronizedTimeout( pointOne );
    		
    		try
    		{
    			// achieve the point in the third, this should give up the synchronization
    			thirdFederate.rtiamb.synchronizationPointAchieved( pointOne );
    		}
    		catch( Exception e )
    		{
    			Assert.fail( "Unexpected exception in achieving sync point [" + pointOne + "]: " +
    			             e.getMessage(), e );
    		}
    		
    		// wait for everyone to be synced up
    		defaultFederate.fedamb.waitForSynchronized( pointOne );
    		thirdFederate.fedamb.waitForSynchronized( pointOne );
    		
		}
		finally
		{
    		// resign the third federate (afterMethod won't do this for us)
    		thirdFederate.quickResign();
    		thirdFederate.quickDisconnect();
		}
	}
	
	//////////////////////////////////////////////
	// TEST: (valid) testSyncReleasedOnResign() //
	//////////////////////////////////////////////
	/**
	 * Depends on other method so that we have validated that the raw HLA methods work and can
	 * just call the quickXxx methods to expidite things.
	 */
	@Test
	public void testSyncReleasedOnResign()
	{
		// register a sync point
		defaultFederate.quickAnnounce( pointOne, tag );
		// validate that it has been announced
		defaultFederate.fedamb.waitForSyncAnnounce( pointOne );
		secondFederate.fedamb.waitForSyncAnnounce( pointOne );
		
		// achieve the point in the first federate
		defaultFederate.quickAchieved( pointOne );
		// ensure that the point isn't synchronized on
		defaultFederate.fedamb.waitForSynchronizedTimeout( pointOne );
		secondFederate.fedamb.waitForSynchronizedTimeout( pointOne );
		
		// resign the second federate
		secondFederate.quickResign();
		// make sure the default federate gets the synchronized notification
		defaultFederate.fedamb.waitForSynchronized( pointOne );
	}
	
	///////////////////////////////////////////////////
	// TEST: (valid) testGroupSyncReleasedOnResign() //
	///////////////////////////////////////////////////
	/**
	 * Depends on other method so that we have validated that the raw HLA methods work and can
	 * just call the quickXxx methods to expidite things.
	 */
	@Test
	public void testGroupSyncReleasedOnResign()
	{
		// we use a third federate to ensure that the group
		// point is actually limited to a specific group
		TestFederate thirdFederate = new TestFederate( "thirdFederate", this );
		thirdFederate.quickJoin();
		boolean cleanResign = false;
		
		try
		{
    		// register a sync point, limited to the first and third federate
    		defaultFederate.quickAnnounce( pointOne,
    		                               tag,
    		                               defaultFederate.federateHandle,
    		                               thirdFederate.federateHandle );

    		// validate that it has been announced
    		defaultFederate.fedamb.waitForSyncAnnounce( pointOne );
    		thirdFederate.fedamb.waitForSyncAnnounce( pointOne );
    		secondFederate.fedamb.waitForSyncAnnounceTimeout( pointOne );
    		
    		// achieve the point in the first federate
    		defaultFederate.quickAchieved( pointOne );
    		defaultFederate.fedamb.waitForSynchronizedTimeout( pointOne );
    		thirdFederate.fedamb.waitForSynchronizedTimeout( pointOne );
    		
    		// resign in the third federate, this should free the point up to be synchronized on
    		thirdFederate.quickResign();
    		cleanResign = true;
    		defaultFederate.fedamb.waitForSynchronized( pointOne );
		}
		finally
		{
			// only resign if we haven't already
			if( !cleanResign )
				thirdFederate.quickResign();
		}
	}

	///////////////////////////////////////////////////////
	// TEST: testAcheivedSyncPointFromUnjoinedFederate() //
	///////////////////////////////////////////////////////
	@Test
	public void testAcheivedSyncPointFromUnjoinedFederate()
	{
		// test achieved when not joined
		defaultFederate.quickResign();
		
		try
		{
			defaultFederate.rtiamb.synchronizationPointAchieved( pointOne );
			expectedException( FederateNotExecutionMember.class );
		}
		catch( FederateNotExecutionMember fnem )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, FederateNotExecutionMember.class );
		}
		finally
		{
			// join again because afterMethod is going to try to clean us up
			defaultFederate.quickJoin();
		}
	}
	
	////////////////////////////////////////////////
	// TEST: testAchievedSyncPointWithNullLabel() //
	////////////////////////////////////////////////
	@Test
	public void testAchievedSyncPointWithNullLabel()
	{
		// test achieved with null label
		try
		{
			defaultFederate.rtiamb.synchronizationPointAchieved( null );
			expectedException(SynchronizationPointLabelNotAnnounced.class, RTIinternalError.class);
		}
		catch( SynchronizationPointLabelNotAnnounced slna )
		{
			// SUCCESS
		}
		catch( RTIinternalError rtie )
		{
			// Also would be valid
		}
		catch( Exception e )
		{
			wrongException( e, SynchronizationPointLabelNotAnnounced.class, RTIinternalError.class );
		}
	}
	
	//////////////////////////////////////////////////////
	// TEST: testAchievedSyncPointWithWhitespaceLabel() //
	//////////////////////////////////////////////////////
	@Test
	public void testAchievedSyncPointWithWhitespaceLabel()
	{
		// test achieved with empty label
		try
		{
			defaultFederate.rtiamb.synchronizationPointAchieved( "" );
			expectedException( SynchronizationPointLabelNotAnnounced.class );
		}
		catch( SynchronizationPointLabelNotAnnounced slna )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, SynchronizationPointLabelNotAnnounced.class );
		}
		
		// test achieved with whitespace label
		try
		{
			defaultFederate.rtiamb.synchronizationPointAchieved( "   " );
			expectedException( SynchronizationPointLabelNotAnnounced.class );
		}
		catch( SynchronizationPointLabelNotAnnounced slna )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, SynchronizationPointLabelNotAnnounced.class );
		}
	}
	
	/////////////////////////////////////////////////////////
	// TEST: testAchievedSyncPointWithNonAnnouncedHandle() //
	/////////////////////////////////////////////////////////
	@Test
	public void testAchievedSyncPointWithNonAnnouncedHandle()
	{
		// test achieved a non-announced label
		try
		{
			defaultFederate.rtiamb.synchronizationPointAchieved( "no,no,no" );
			expectedException( SynchronizationPointLabelNotAnnounced.class );
		}
		catch( SynchronizationPointLabelNotAnnounced slna )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException ( e, SynchronizationPointLabelNotAnnounced.class );
		}
	}
	
	////////////////////////////////////////////////////
	// TEST: testAchievedSyncPointWithAchievedLabel() //
	////////////////////////////////////////////////////
	@Test
	public void testAchievedSyncPointWithAchievedLabel()
	{
		// announce a point
		defaultFederate.quickAnnounce( pointOne, tag );
		defaultFederate.fedamb.waitForSyncAnnounce( pointOne );
		secondFederate.fedamb.waitForSyncAnnounce( pointOne );
		
		// synchronize on the point
		defaultFederate.quickAchieved( pointOne );

		try
		{
			defaultFederate.rtiamb.synchronizationPointAchieved( pointOne );
			expectedException( RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// SUCCESS!
		}
		catch( Exception e )
		{
			wrongException( e, RTIinternalError.class );
		}
	}
	
	//////////////////////////////////////////////////////////////////
	// TEST: testAchievedSyncPointWithPreviouslySynchronizedLabel() //
	//////////////////////////////////////////////////////////////////
	@Test
	public void testAchievedSyncPointWithPreviouslySynchronizedLabel()
	{
		// announce a point
		defaultFederate.quickAnnounce( pointOne, tag );
		//defaultFederate.fedamb.waitForSyncAnnounce( pointOne ); // Performed in quickAnnounce()
		secondFederate.fedamb.waitForSyncAnnounce( pointOne );
		
		// synchronize on the point
		defaultFederate.quickAchieved( pointOne );
		secondFederate.quickAchieved( pointOne );
		defaultFederate.fedamb.waitForSynchronized( pointOne );
		secondFederate.fedamb.waitForSynchronized( pointOne );
		
		// try and achieve the point again
		try
		{
			defaultFederate.rtiamb.synchronizationPointAchieved( pointOne );
			expectedException( SynchronizationPointLabelNotAnnounced.class );
		}
		catch( SynchronizationPointLabelNotAnnounced slna )
		{
			// SUCCESS!
		}
		catch( Exception e )
		{
			wrongException( e , SynchronizationPointLabelNotAnnounced.class );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
