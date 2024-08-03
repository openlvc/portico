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

import hla.rti.FederateHandleSet;
import hla.rti.FederateNotExecutionMember;
import hla.rti.RTIinternalError;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hlaunit.hla13.TestSetup;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"RegisterSyncPointTest",
                                   "registerSyncPoint",
                                   "synchronization",
                                   "federationManagement"})
public class RegisterSyncPointTest extends Abstract13Test
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
	private String pointTwo = "SyncPointTwo";
	
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
		
		// initialize the second federate
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
		// create and join the federation
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
		thirdFederate.quickJoin();
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		// resign from and destroy the federation
		defaultFederate.quickResign();
		secondFederate.quickResign();
		thirdFederate.quickResign();
		defaultFederate.quickDestroy();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// Register Sync Point Test Methods ////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void registerFederationSynchronizationPoint( String label, byte[] tag )
	//     throws FederateNotExecutionMember,
	//            SaveInProgress,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

	///////////////////////////////////////////
	// TEST: (valid) testRegisterSyncPoint() //
	///////////////////////////////////////////
	@Test
	public void testRegisterSyncPoint()
	{
		// register a valid sync point
		try
		{
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( pointOne, tag );
			// wait for the result to be returned //
			if( defaultFederate.fedamb.waitForSyncResult(pointOne) == false )
			{
				Assert.fail( "Received syncpoint reg fail for valid sync point" );
			}
			
			// wait for the second federate to pick it up //
			byte[] received = secondFederate.fedamb.waitForSyncAnnounce( pointOne );
			if( received == null )
			{
				Assert.fail( "Second federate didn't receive valid syncpoint announce" );
			}
			else
			{
				// validate that we have the correct tag
				String given = new String( received );
				if( given.equals("le tag") == false )
				{
					Assert.fail( "Second federate got invalid tag on valid syncpoint announce" );
				}
			}
		}
		catch( Exception e )
		{
			Assert.fail( "Failure while registering valid sync point", e );
		}
	}
	
	//////////////////////////////////////////////////////
	// TEST: (valid) testRegisterSyncPointWithNullTag() //
	//////////////////////////////////////////////////////
	@Test
	public void testRegisterSyncPointWithNullTag()
	{
		// register a valid sync point with a null tag
		try
		{
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( pointTwo, null );
			// wait for the result to be returned //
			if( defaultFederate.fedamb.waitForSyncResult(pointTwo) == false )
			{
				Assert.fail( "Received syncpoint reg fail for valid sync point with null tag" );
			}

			// wait for the second federate to pick it up //
			byte[] received = secondFederate.fedamb.waitForSyncAnnounce( pointTwo );
			if( received != null )
			{
				Assert.fail( "Second federate got invalid tag on valid syncpoint announce" );
			}
		}
		catch( Exception e )
		{
			Assert.fail( "Failure while registering valid sync point with null tag", e );
		}
	}

	//////////////////////////////////////////////////////////////////
	// TEST: (valid) testSyncPointAnnouncedForLateJoiningFederate() //
	//////////////////////////////////////////////////////////////////
	@Test
	public void testSyncPointAnnouncedForLateJoiningFederate()
	{
		// resign the second federate so that it can rejoin after the initial announcement
		secondFederate.quickResign();
		
		// announce the point in the first federate
		defaultFederate.quickAnnounce( pointOne, tag );
		defaultFederate.fedamb.waitForSyncAnnounce( pointOne );

		// join the second federate back and make sure it gets the announcement
		secondFederate.quickJoin();
		secondFederate.fedamb.waitForSyncAnnounce( pointOne );
		
		// make sure the default federate can't synchronize without the second one
		defaultFederate.quickAchieved( pointOne );
		thirdFederate.quickAchieved( pointOne );
		defaultFederate.fedamb.waitForSynchronizedTimeout( pointOne );
		
		// now achieve in the second and wait for synchronization
		secondFederate.quickAchieved( pointOne );
		defaultFederate.fedamb.waitForSynchronized( pointOne );
		secondFederate.fedamb.waitForSynchronized( pointOne );
	}

	///////////////////////////////////////////////////////
	// TEST: testRegisterSyncPointFromUnjoinedFederate() //
	///////////////////////////////////////////////////////
	@Test
	public void testRegisterSyncPointFromUnjoinedFederate()
	{
		// test registration when not joined
		defaultFederate.quickResign();
		
		try
		{
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( pointOne, tag );
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
	// TEST: testRegisterSyncPointWhenSaveInProgress() //
	/////////////////////////////////////////////////////
	@Test
	public void testRegisterSyncPointWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( pointOne, tag );
			expectedException( SaveInProgress.class );
		}
		catch( SaveInProgress sip )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, SaveInProgress.class );
		}
	}

	////////////////////////////////////////////////////////
	// TEST: testRegisterSyncPointWhenRestoreInProgress() //
	////////////////////////////////////////////////////////
	@Test
	public void testRegisterSyncPointWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( pointOne, tag );
			expectedException( RestoreInProgress.class );
		}
		catch( RestoreInProgress rip )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, RestoreInProgress.class );
		}
	}
	
	////////////////////////////////////////////////
	// TEST: testRegisterSyncPointWithNullLabel() //
	////////////////////////////////////////////////
	@Test
	public void testRegisterSyncPointWithNullLabel()
	{
		// test registration with null label
		try
		{
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( null, tag );
			expectedException( RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, RTIinternalError.class );
		}
	}
	
	//////////////////////////////////////////////////////
	// TEST: testRegisterSyncPointWithWhitespaceLabel() //
	//////////////////////////////////////////////////////
	@Test
	public void testRegisterSyncPointWithWhitespaceLabel()
	{
		// test registration with empty label
		try
		{
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( "", tag );
			expectedException( RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, RTIinternalError.class );
		}
		
		// test registration with whitespace label
		try
		{
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( "  ", tag );
			expectedException( RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, RTIinternalError.class );
		}
	}
	
	////////////////////////////////////////////////////
	// TEST: testRegisterSyncPointWithExistingLabel() //
	////////////////////////////////////////////////////
	@Test
	public void testRegisterSyncPointWithExistingLabel()
	{
		// setup: register the point so we can validate it can't be re-registered while active
		defaultFederate.quickAnnounce( pointOne, tag );
		
		// test registartion of already registred sync point
		try
		{
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( pointOne, tag );
			if( defaultFederate.fedamb.waitForSyncResult(pointOne) )
			{
				Assert.fail( "Was able to register sync point that is already registered" );
			}
		}
		catch( Exception e )
		{
			Assert.fail( "Invalid exception while registering already registered sync point", e );
		}
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// Register Group Sync Point Test Methods /////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void registerFederationSynchronizationPoint( String label, byte[] tag, FederateHandleSet set )
	//     throws FederateNotExecutionMember,
	//            SaveInProgress,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

	////////////////////////////////////////////////
	// TEST: (valid) testRegisterGroupSyncPoint() //
	////////////////////////////////////////////////
	@Test
	public void testRegisterGroupSyncPoint()
	{
		// this test will use three federates //
		// defaultFederate: registers sync point with group //
		// secondFederate:  not part of the group, validates that non-members don't get announce //
		// thirdFederate:   part of the group, validates that members get announce //
		
		/////////////////////////////////////////////////
		// register a sync point with a federate group //
		/////////////////////////////////////////////////
		FederateHandleSet set = TestSetup.getRTIFactory().createFederateHandleSet();
		set.add( defaultFederate.federateHandle );
		set.add( thirdFederate.federateHandle );
		
		try
		{
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( pointOne, tag, set );
			secondFederate.quickTick();
			thirdFederate.quickTick();
			// wait for the success notice //
			if( defaultFederate.fedamb.waitForSyncResult(pointOne) == false )
			{
				Assert.fail( "Failed to register valid sync point with a group" );
			}
			
			// make sure that third federate receives the announcement
			thirdFederate.fedamb.waitForSyncAnnounce( pointOne );
			// make sure the second federate doesn't receive the announcement
			secondFederate.fedamb.waitForSyncAnnounceTimeout( pointOne );
		}
		catch( Exception e )
		{
			Assert.fail( "Invalid exception while registering syncpoint with a group", e );
		}
	}
	
	///////////////////////////////////////////////////////////
	// TEST: (valid) testRegisterGroupSyncPointWithNullTag() //
	///////////////////////////////////////////////////////////
	@Test
	public void testRegisterGroupSyncPointWithNullTag()
	{
		// this test will use three federates //
		// defaultFederate: registers sync point with group //
		// secondFederate:  not part of the group, validates that non-members don't get announce //
		// thirdFederate:   part of the group, validates that members get announce //
		
		/////////////////////////////////////////////////
		// register a sync point with a federate group //
		/////////////////////////////////////////////////
		FederateHandleSet set = TestSetup.getRTIFactory().createFederateHandleSet();
		set.add( defaultFederate.federateHandle );
		set.add( thirdFederate.federateHandle );
		
		try
		{
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( pointOne, null, set );
			// wait for the success notice //
			if( defaultFederate.fedamb.waitForSyncResult(pointOne) == false )
			{
				Assert.fail( "Failed to register valid sync point with a group with null tag" );
			}
			
			// make sure that third federate receives the announcement
			byte[] received = thirdFederate.fedamb.waitForSyncAnnounce( pointOne );
			if( received != null )
				Assert.fail( "Third federate got invalid tag on valid syncpoint announce" );

			// make sure the second federate doesn't receive the announcement
			secondFederate.fedamb.waitForSyncAnnounceTimeout( pointOne );
		}
		catch( Exception e )
		{
			Assert.fail( "Invalid exception while registering syncpoint with a group", e );
		}
	}
	
	////////////////////////////////////////////////////////////
	// TEST: (valid) testRegisterGroupSyncPointWithEmptySet() //
	////////////////////////////////////////////////////////////
	@Test
	public void testRegisterGroupSyncPointWithEmptySet()
	{
		////////////////////////////////
		// register with an empty set //
		////////////////////////////////
		FederateHandleSet set = TestSetup.getRTIFactory().createFederateHandleSet();
		try
		{
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( pointTwo, tag, set );
			
			// make sure ALL federates get the point //
			if( defaultFederate.fedamb.waitForSyncResult(pointTwo) == false )
				Assert.fail( "Registered syncpoint with emtpy set, but got failure callback" );

			secondFederate.fedamb.waitForSyncAnnounce( pointTwo );
			thirdFederate.fedamb.waitForSyncAnnounce( pointTwo );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while registering sync point with empty set", e );
		}
	}
	
	///////////////////////////////////////////////////////////
	// TEST: (valid) testRegisterGroupSyncPointWithNullSet() //
	///////////////////////////////////////////////////////////
	@Test
	public void testRegisterGroupSyncPointWithNullSet()
	{
		//////////////////////////////////////////
		// test register with a null handle set // - this is the same as supplying an empty set
		//////////////////////////////////////////
		try
		{
			String nullSet = "nullSet";
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( nullSet, tag, null );
			
			// make sure ALL federates get the point //
			if( defaultFederate.fedamb.waitForSyncAnnounce(nullSet) == null ||
				secondFederate.fedamb.waitForSyncAnnounce(nullSet) == null ||
				thirdFederate.fedamb.waitForSyncAnnounce(nullSet) == null )
			{
				Assert.fail( "Registered syncpoint with null set, but some federates" +
				             " didn't get an announce" );
			}
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while registering sync point with null set", e );
		}
	}
	
	
	////////////////////////////////////////////////////////////
	// TEST: testRegisterGroupSyncPointFromUnjoinedFederate() //
	////////////////////////////////////////////////////////////
	@Test
	public void testRegisterGroupSyncPointFromUnjoinedFederate()
	{
		FederateHandleSet set = TestSetup.getRTIFactory().createFederateHandleSet();
		set.add( defaultFederate.federateHandle );
		set.add( secondFederate.federateHandle );

		// test registration when not joined
		defaultFederate.quickResign();
		
		try
		{
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( pointOne, tag, set );
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
	// TEST: testRegisterSyncPointWhenSaveInProgress() //
	/////////////////////////////////////////////////////
	@Test
	public void testRegisterGroupSyncPointWhenSaveInProgress()
	{
		FederateHandleSet set = TestSetup.getRTIFactory().createFederateHandleSet();
		set.add( defaultFederate.federateHandle );
		set.add( secondFederate.federateHandle );

		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( pointOne, tag, set );
			expectedException( SaveInProgress.class );
		}
		catch( SaveInProgress sip )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, SaveInProgress.class );
		}
	}

	////////////////////////////////////////////////////////
	// TEST: testRegisterSyncPointWhenRestoreInProgress() //
	////////////////////////////////////////////////////////
	@Test
	public void testRegisterGroupSyncPointWhenRestoreInProgress()
	{
		FederateHandleSet set = TestSetup.getRTIFactory().createFederateHandleSet();
		set.add( defaultFederate.federateHandle );
		set.add( secondFederate.federateHandle );

		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( pointOne, tag, set );
			expectedException( RestoreInProgress.class );
		}
		catch( RestoreInProgress rip )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, RestoreInProgress.class );
		}
	}

	/////////////////////////////////////////////////////
	// TEST: testRegisterGroupSyncPointWithNullLabel() //
	/////////////////////////////////////////////////////
	@Test
	public void testRegisterGroupSyncPointWithNullLabel()
	{		
		// test registration with null label
		try
		{
			// create the set
			FederateHandleSet set = TestSetup.getRTIFactory().createFederateHandleSet();
			set.add( defaultFederate.federateHandle );
			set.add( secondFederate.federateHandle );
			
			// attempt registration
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( null, tag, set );
			expectedException( RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, RTIinternalError.class );
		}
	}
	
	///////////////////////////////////////////////////////////
	// TEST: testRegisterGroupSyncPointWithWhitespaceLabel() //
	///////////////////////////////////////////////////////////
	@Test
	public void testRegisterGroupSyncPointWithWhitespaceLabel()
	{
		// test registration with whitespace label
		try
		{
			// create the set
			FederateHandleSet set = TestSetup.getRTIFactory().createFederateHandleSet();
			set.add( defaultFederate.federateHandle );
			set.add( secondFederate.federateHandle );
			
			// attempt registration
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( "   ", tag, set );
			expectedException( RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, RTIinternalError.class );
		}
		
		// test registration with empty label
		try
		{
			// create the set
			FederateHandleSet set = TestSetup.getRTIFactory().createFederateHandleSet();
			set.add( defaultFederate.federateHandle );
			set.add( secondFederate.federateHandle );
			
			// attempt registration
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( "", tag, set );
			expectedException( RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, RTIinternalError.class );
		}
	}
	
	/////////////////////////////////////////////////////////
	// TEST: testRegisterGroupSyncPointWithExistingLabel() //
	/////////////////////////////////////////////////////////
	@Test
	public void testRegisterGroupSyncPointWithExistingLabel()
	{
		// setup: register the point so we can validate it can't be re-registered while active
		defaultFederate.quickAnnounce( pointOne, tag );
		
		// test registartion of already registred sync point
		try
		{
			// create the set
			FederateHandleSet set = TestSetup.getRTIFactory().createFederateHandleSet();
			set.add( defaultFederate.federateHandle );
			set.add( secondFederate.federateHandle );

			// attempt registration
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( pointOne, tag );
			if( defaultFederate.fedamb.waitForSyncResult(pointOne) )
			{
				Assert.fail( "Was able to register group sync point that is already registered" );
			}
		}
		catch( Exception e )
		{
			Assert.fail(
			    "Invalid exception while registering (group) already registered sync point", e );
		}
	}
	
	/////////////////////////////////////////////////////////
	// TEST: testRegisterGroupSyncPointWithInvalidHandle() //
	/////////////////////////////////////////////////////////
	@Test
	public void testRegisterGroupSyncPointWithInvalidHandle()
	{
		//////////////////////////////////////////////////////
		// register a sync point with a bad federate handle //
		//////////////////////////////////////////////////////
		FederateHandleSet set = TestSetup.getRTIFactory().createFederateHandleSet();
		set.add( defaultFederate.federateHandle );
		set.add( -1 );
		
		try
		{
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( pointOne, tag, set );
			// wait for the success notice //
			if( defaultFederate.fedamb.waitForSyncResult(pointOne) )
			{
				Assert.fail( "Registered sync point using group with invalid federate handle" );
			}
		}
		catch( Exception e )
		{
			Assert.fail( "Exception when registering syncpoint using group with invalid handle",e );
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// General Sync Point Test Methods /////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////
	// TEST: testRegisterSyncPointAfterSynchronized() //
	////////////////////////////////////////////////////
	@Test
	public void testRegisterSyncPointAfterSynchronized()
	{
		///////////
		// setup //
		///////////
		thirdFederate.quickResign();
		// register a sync point and make sure it is synchronized on
		defaultFederate.quickAnnounce( pointOne, tag );
		//defaultFederate.fedamb.waitForSyncResult( pointOne, true ); -- quickAnnounce does this
		defaultFederate.fedamb.waitForSyncAnnounce( pointOne );
		secondFederate.fedamb.waitForSyncAnnounce( pointOne );

		// synchronize on the point
		defaultFederate.quickAchieved( pointOne );
		secondFederate.quickAchieved( pointOne );
		defaultFederate.fedamb.waitForSynchronized( pointOne );
		secondFederate.fedamb.waitForSynchronized( pointOne );
		
		// clear any previous state - this is due to a limitation of the
		// test federate for handling the re-announcement of a point
		defaultFederate.fedamb.announced.clear();
		defaultFederate.fedamb.synched.clear();
		defaultFederate.fedamb.syncFailed = "";
		defaultFederate.fedamb.syncSucceeded = "";
		secondFederate.fedamb.announced.clear();
		secondFederate.fedamb.synched.clear();
		
		//////////
		// test //
		//////////
		defaultFederate.quickAnnounce( pointOne, tag );
		//defaultFederate.fedamb.waitForSyncResult( pointOne, true ); -- quickAnnounce does this
		defaultFederate.fedamb.waitForSyncAnnounce( pointOne );
		secondFederate.fedamb.waitForSyncAnnounce( pointOne );
	}
	
	/////////////////////////////////////////////////////////
	// TEST: testRegisterGroupSyncPointAfterSynchronized() //
	/////////////////////////////////////////////////////////
	@Test
	public void testRegisterGroupSyncPointAfterSynchronized()
	{
		///////////
		// setup //
		///////////
		thirdFederate.quickResign();
		FederateHandleSet set = TestSetup.getRTIFactory().createFederateHandleSet();
		set.add( defaultFederate.federateHandle );
		set.add( secondFederate.federateHandle );

		try
		{
			// register the point with a group
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( pointOne, tag, set );
			defaultFederate.fedamb.waitForSyncResult( pointOne, true );
			defaultFederate.fedamb.waitForSyncAnnounce( pointOne );
			secondFederate.fedamb.waitForSyncAnnounce( pointOne );
			
			// achieve and wait for synchronized
			defaultFederate.quickAchieved( pointOne );
			secondFederate.quickAchieved( pointOne );
			defaultFederate.fedamb.waitForSynchronized( pointOne );
			secondFederate.fedamb.waitForSynchronized( pointOne );

			// attempt RE-REGISTER
			defaultFederate.rtiamb.registerFederationSynchronizationPoint( pointOne, tag, set );
			defaultFederate.fedamb.waitForSyncResult( pointOne, true );
			defaultFederate.fedamb.waitForSyncAnnounce( pointOne );
			secondFederate.fedamb.waitForSyncAnnounce( pointOne );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexcpeted exception: testRegisterGroupSyncPointAfterSynchronized: " +
			             e.getMessage(), e );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
