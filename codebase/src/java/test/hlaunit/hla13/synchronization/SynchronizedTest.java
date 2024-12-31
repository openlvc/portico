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
package hlaunit.hla13.synchronization;

import hla.rti.FederateNotExecutionMember;
import hla.rti.RTIinternalError;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hla.rti.SynchronizationLabelNotAnnounced;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

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
public class SynchronizedTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate secondFederate;
	private Test13Federate thirdFederate;
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
		secondFederate = new Test13Federate( "federateTwo", this );
		thirdFederate = new Test13Federate( "federateThree", this );
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
		thirdFederate.quickJoin();
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		defaultFederate.quickResign();
		secondFederate.quickResign();
		thirdFederate.quickResign();

		defaultFederate.quickDestroy();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void synchronizationPointAchieved( String synchronizationPointLabel )
	//     throws SynchronizationLabelNotAnnounced,
	//            FederateNotExecutionMember,
	//            SaveInProgress,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

	///////////////////////////////////////////
	// TEST: (valid) testAchievedSyncPoint() //
	///////////////////////////////////////////
	@Test
	public void testAchievedSyncPoint()
	{
		// register a federation wide synchronization point //
		defaultFederate.quickAnnounce( pointOne, tag );
		secondFederate.fedamb.waitForSyncAnnounce( pointOne );
		thirdFederate.fedamb.waitForSyncAnnounce( pointOne );

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
			// 3. achieve the sync point in the second and third federates
			secondFederate.rtiamb.synchronizationPointAchieved( pointOne );
			thirdFederate.rtiamb.synchronizationPointAchieved( pointOne );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while waiting attempting valid synchronize: " +
			             e.getMessage(), e );
		}

		// 4. make sure BOTH federates have recevied the "synchronized" callback
		defaultFederate.fedamb.waitForSynchronized( pointOne );
		secondFederate.fedamb.waitForSynchronized( pointOne );
		thirdFederate.fedamb.waitForSynchronized( pointOne );
	}

	////////////////////////////////////////////////
	// TEST: (valid) testAchievedGroupSyncPoint() //
	////////////////////////////////////////////////
	@Test
	public void testAchievedGroupSyncPoint()
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
	
	//////////////////////////////////////////////
	// TEST: (valid) testSyncReleasedOnResign() //
	//////////////////////////////////////////////
	@Test
	public void testSyncReleasedOnResign()
	{
		// register a sync point
		defaultFederate.quickAnnounce( pointOne, tag );
		// validate that it has been announced
		defaultFederate.fedamb.waitForSyncAnnounce( pointOne );
		secondFederate.fedamb.waitForSyncAnnounce( pointOne );
		thirdFederate.fedamb.waitForSyncAnnounce( pointOne );
		
		// achieve the point in the first federate
		defaultFederate.quickAchieved( pointOne );
		// ensure that the point isn't synchronized on
		defaultFederate.fedamb.waitForSynchronizedTimeout( pointOne );
		secondFederate.fedamb.waitForSynchronizedTimeout( pointOne );
		
		// resign the second and third federates
		secondFederate.quickResign();
		thirdFederate.quickResign();

		// make sure the default federate gets the synchronized notification
		defaultFederate.fedamb.waitForSynchronized( pointOne );
	}
	
	///////////////////////////////////////////////////
	// TEST: (valid) testGroupSyncReleasedOnResign() //
	///////////////////////////////////////////////////
	@Test
	public void testGroupSyncReleasedOnResign()
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
		defaultFederate.fedamb.waitForSynchronized( pointOne );
	}

	////////////////////////////////////////////////////////////////
	// TEST: (valid) testSynchronizationWithLateJoiningFederate() //
	////////////////////////////////////////////////////////////////
	/**
	 * A key to this test, and a point of difference with the test in the method
	 * {@link RegisterSyncPointTest#testSyncPointAnnouncedForLateJoiningFederate()} is that
	 * the late-joining federate in this test will come in AFTER at least one of the other
	 * federates has achieved the point. In the other test, it only came in after the point
	 * had been announced, but not after anyone had acheived the point.
	 * <p/>
	 * This test has been developed because it is suspected that although the announcement
	 * gets through to late joining federates, if any federates have achieved the point, that
	 * fact won't be recorded in the late federate, thus holding it.
	 */
	@Test
	public void testSynchronizationWithLateJoiningFederate()
	{
		// yank the third federate
		thirdFederate.quickResign();
		
		// announce a point and have the defaultFederate achieve it
		defaultFederate.quickAnnounce( pointOne, null );
		defaultFederate.fedamb.waitForSyncAnnounce( pointOne );
		secondFederate.fedamb.waitForSyncAnnounce( pointOne );
		defaultFederate.quickAchieved( pointOne );
		defaultFederate.fedamb.waitForSynchronizedTimeout( pointOne );
		secondFederate.fedamb.waitForSynchronizedTimeout( pointOne );
		
		// join the third federate
		thirdFederate.quickJoin();
		thirdFederate.fedamb.waitForSyncAnnounce( pointOne );
		thirdFederate.quickAchieved( pointOne );
		thirdFederate.fedamb.waitForSynchronizedTimeout( pointOne );
		
		// achieve the point in the second federate (the last hold out) and make sure
		// the federation synchronizes and moves on with life
		secondFederate.quickAchieved( pointOne );
		
		defaultFederate.fedamb.waitForSynchronized( pointOne );
		secondFederate.fedamb.waitForSynchronized( pointOne );
		thirdFederate.fedamb.waitForSynchronized( pointOne );
	}

	///////////////////////////////////////////////////////
	// TEST: testAcheivedSyncPointFromUnjoinedFederate() //
	///////////////////////////////////////////////////////
	@Test
	public void testAchievedSyncPointFromUnjoinedFederate()
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

	/////////////////////////////////////////////////////
	// TEST: testAchievedSyncPointWhenSaveInProgress() //
	/////////////////////////////////////////////////////
	@Test
	public void testAchievedSyncPointWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.synchronizationPointAchieved( pointOne );
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
	
	////////////////////////////////////////////////////////
	// TEST: testAchievedSyncPointWhenRestoreInProgress() //
	////////////////////////////////////////////////////////
	@Test
	public void testAchievedSyncPointWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.synchronizationPointAchieved( pointOne );
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
			expectedException( SynchronizationLabelNotAnnounced.class );
		}
		catch( SynchronizationLabelNotAnnounced slna )
		{
			// SUCCESS
		}
		catch( RTIinternalError rtie )
		{
			// Also would be valid
		}
		catch( Exception e )
		{
			wrongException( e, SynchronizationLabelNotAnnounced.class, RTIinternalError.class );
		}
	}
	
	/////////////////////////////////////////////////
	// TEST: testAchievedSyncPointWithEmptyLabel() //
	/////////////////////////////////////////////////
	@Test
	public void testAchievedSyncPointWithEmptyLabel()
	{
		// test achieved with empty label
		try
		{
			defaultFederate.rtiamb.synchronizationPointAchieved( "" );
			expectedException( SynchronizationLabelNotAnnounced.class );
		}
		catch( SynchronizationLabelNotAnnounced slna )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, SynchronizationLabelNotAnnounced.class );
		}
		
		// test achieved with whitespace label
		try
		{
			defaultFederate.rtiamb.synchronizationPointAchieved( "   " );
			expectedException( SynchronizationLabelNotAnnounced.class );
		}
		catch( SynchronizationLabelNotAnnounced slna )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, SynchronizationLabelNotAnnounced.class );
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
			expectedException( SynchronizationLabelNotAnnounced.class );
		}
		catch( SynchronizationLabelNotAnnounced slna )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException ( e, SynchronizationLabelNotAnnounced.class );
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
		thirdFederate.fedamb.waitForSyncAnnounce( pointOne );
		
		// synchronize on the point
		defaultFederate.quickAchieved( pointOne );
		secondFederate.quickAchieved( pointOne );
		thirdFederate.quickAchieved( pointOne );
		defaultFederate.fedamb.waitForSynchronized( pointOne );
		secondFederate.fedamb.waitForSynchronized( pointOne );
		thirdFederate.fedamb.waitForSynchronized( pointOne );
		
		// try and achieve the point again
		try
		{
			defaultFederate.rtiamb.synchronizationPointAchieved( pointOne );
			expectedException( SynchronizationLabelNotAnnounced.class );
		}
		catch( SynchronizationLabelNotAnnounced slna )
		{
			// SUCCESS!
		}
		catch( Exception e )
		{
			wrongException( e , SynchronizationLabelNotAnnounced.class );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
