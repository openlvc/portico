/*
 *   Copyright 2009 The Portico Project
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
package hlaunit.hla13.saverestore;

import hla.rti.FederateNotExecutionMember;
import hla.rti.FederationTimeAlreadyPassed;
import hla.rti.InvalidFederationTime;
import hla.rti.LogicalTime;
import hla.rti.RTIinternalError;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hla.rti.SaveNotInitiated;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * These tests validate that the process of calls required to carry out a federation save works
 * correctly (and that when fed improper input, that they fail correctly). Please note that these
 * tests do NOT validate that the appropriate state was saved/restored properly. For example, the
 * tests to ensure that the proper time-status for federates is saved/restored are located in the
 * time management tests. 
 */
@Test(singleThreaded=true, groups={"FederationSaveTest", "federationSave", "SaveRestore"})
public class FederationSaveTest extends Abstract13Test
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
	
	/**
	 * This method will enable time constrained and regulating in all federates and advance the
	 * current federation time to 5.0. This is really just a setup method for those tests that
	 * want to use logical time. Rather than forcing this to happen for every test, it's easier
	 * to just localize this stuff into a helper method that can be called as required.
	 */
	private void timeManageFederation()
	{
		// initialize time settings in the default federate
		defaultFederate.quickEnableAsyncDelivery();
		defaultFederate.quickEnableRegulating( 1.0 );
		defaultFederate.quickEnableConstrained();
		// initialize time settings in the second federate
		secondFederate.quickEnableAsyncDelivery();
		secondFederate.quickEnableRegulating( 1.0 );
		secondFederate.quickEnableConstrained();
		
		// advance time a little
		defaultFederate.quickAdvanceRequest( 5.0 );
		secondFederate.quickAdvanceAndWait( 5.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 5.0 );
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Request Save Test Methods ///////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	// public void requestFederationSave( String label )
	//     throws FederateNotExecutionMember,
	//            SaveInProgress,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;
	
	///////////////////////////////////////////////
	// TEST: (valid) testRequestFederationSave() //
	///////////////////////////////////////////////
	@Test
	public void testRequestFederationSave()
	{
		// request a save
		try
		{
			defaultFederate.rtiamb.requestFederationSave( "save" );
		}
		catch( Exception e )
		{
			unexpectedException( "Requesting a federation save", e );
		}
		
		// make sure the proper callbacks are sent out
		defaultFederate.fedamb.waitForSaveInitiated( "save" );
		secondFederate.fedamb.waitForSaveInitiated( "save" );
	}

	////////////////////////////////////////////////////
	// TEST: testRequestFederationSaveWithNullLabel() //
	////////////////////////////////////////////////////
	@Test
	public void testRequestFederationSaveWithNullLabel()
	{
		try
		{
			defaultFederate.rtiamb.requestFederationSave( null );
			expectedException( RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RTIinternalError.class );
		}
	}

	////////////////////////////////////////////////////
	// TEST: testRequestFederationSaveWhenNotJoined() //
	////////////////////////////////////////////////////
	@Test
	public void testRequestFederationSaveWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			defaultFederate.rtiamb.requestFederationSave( "save" );
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

	/////////////////////////////////////////////////////////
	// TEST: testRequestFederationSaveWhenSaveInProgress() //
	/////////////////////////////////////////////////////////
	@Test
	public void testRequestFederationSaveWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.requestFederationSave( "save2" );
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

	////////////////////////////////////////////////////////////
	// TEST: testRequestFederationSaveWhenRestoreInProgress() //
	////////////////////////////////////////////////////////////
	@Test
	public void testRequestFederationSaveWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.requestFederationSave( "save" );
			expectedException( RestoreInProgress.class );
		}
		catch( RestoreInProgress rip )
		{
			// success
		}
		catch( Exception e )
		{
			wrongException( e, RestoreInProgress.class );
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////// Request Save With Time Test Methods //////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	// public void requestFederationSave( String label, LogicalTime theTime )
    //     throws FederationTimeAlreadyPassed,
	//            InvalidFederationTime,
	//            FederateNotExecutionMember,
	//            SaveInProgress,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

	//////////////////////////////////////////////////////////
	// TEST: (valid) testTimestampedRequestFederationSave() //
	//////////////////////////////////////////////////////////
	@Test
	public void testTimestampedRequestFederationSave()
	{
		timeManageFederation();
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.requestFederationSave( "save", time );
		}
		catch( Exception e )
		{
			unexpectedException( "Requesting federation save @ time 10.0", e );
		}
		
		// we shouldn't get any callbacks yet, as neither federate is at 10.0
		defaultFederate.fedamb.waitForSaveInitiatedTimeout( "save" );
		secondFederate.fedamb.waitForSaveInitiatedTimeout( "save" );
		
		// advance the federates to 10.0
		defaultFederate.quickAdvanceFederation( 10.0 );
		
		// now we should get the save notification
		defaultFederate.fedamb.waitForSaveInitiated( "save" );
		secondFederate.fedamb.waitForSaveInitiated( "save" );
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testTimestampedRequestFederationSaveWithConstrainedAndUnconstrainedFederates() //
	//////////////////////////////////////////////////////////////////////////////////////////////////
	@Test(enabled=false)
	public void testTimestampedRequestFederationSaveWithConstrainedAndUnconstrainedFederates()
	{
		timeManageFederation();
		secondFederate.quickDisableConstrained();
		
		defaultFederate.quickSaveRequest( "save", 10.0 );
		
		// we shouldn't get any callbacks yet, as neither federate is at 10.0.
		// even in the federate that is not time constrained should not received it
		// until all time constrained federates can (according to the spec).
		defaultFederate.fedamb.waitForSaveInitiatedTimeout( "save" );
		secondFederate.fedamb.waitForSaveInitiatedTimeout( "save" );

		// advance the federates to 10.0
		defaultFederate.quickAdvanceAndWait( 10.0 );
		
		// now we should get the save notification
		defaultFederate.fedamb.waitForSaveInitiated( "save" );
		secondFederate.fedamb.waitForSaveInitiated( "save" );
	}
	
	////////////////////////////////////////////////////////////////
	// TEST: testTimestampedRequestFederationSaveWithTimeInPast() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testTimestampedRequestFederationSaveWithTimeInPast()
	{
		timeManageFederation();

		try
		{
			LogicalTime time = defaultFederate.createTime( 1.0 );
			defaultFederate.rtiamb.requestFederationSave( "save", time );
			expectedException( FederationTimeAlreadyPassed.class );
		}
		catch( FederationTimeAlreadyPassed ftap )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, FederationTimeAlreadyPassed.class );
		}
	}

	/////////////////////////////////////////////////////////////////
	// TEST: testTimestampedRequestFederationSaveWithInvalidTime() //
	/////////////////////////////////////////////////////////////////
	@Test
	public void testTimestampedRequestFederationSaveWithInvalidTime()
	{
		try
		{
			LogicalTime time = defaultFederate.createTime( -1.0 );
			defaultFederate.rtiamb.requestFederationSave( "save", time );
			expectedException( InvalidFederationTime.class );
		}
		catch( InvalidFederationTime ift )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidFederationTime.class );
		}
	}

	///////////////////////////////////////////////////////////////
	// TEST: testTimestampedRequestFederationSaveWithNullLabel() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testTimestampedRequestFederationSaveWithNullLabel()
	{
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.requestFederationSave( null, time );
			expectedException( RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RTIinternalError.class );
		}
	}

	///////////////////////////////////////////////////////////////
	// TEST: testTimestampedRequestFederationSaveWhenNotJoined() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testTimestampedRequestFederationSaveWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.requestFederationSave( "save", time );
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

	////////////////////////////////////////////////////////////////////
	// TEST: testTimestampedRequestFederationSaveWhenSaveInProgress() //
	////////////////////////////////////////////////////////////////////
	@Test
	public void testTimestampedRequestFederationSaveWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.requestFederationSave( "save", time );
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

	///////////////////////////////////////////////////////////////////////
	// TEST: testTimestampedRequestFederationSaveWhenRestoreInProgress() //
	///////////////////////////////////////////////////////////////////////
	@Test
	public void testTimestampedRequestFederationSaveWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.requestFederationSave( "save", time );
			expectedException( RestoreInProgress.class );
		}
		catch( RestoreInProgress rip )
		{
			// success
		}
		catch( Exception e )
		{
			wrongException( e, RestoreInProgress.class );
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Save Begun Test Methods ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	// public void federateSaveBegun()
	//     throws SaveNotInitiated,
	//            FederateNotExecutionMember,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

	///////////////////////////////////////////
	// TEST: (valid) testFederateSaveBegun() //
	///////////////////////////////////////////
	@Test
	public void testFederateSaveBegun()
	{
		defaultFederate.quickSaveRequest( "save" );
		defaultFederate.fedamb.waitForSaveInitiated( "save" );
		secondFederate.fedamb.waitForSaveInitiated( "save" );
		
		try
		{
			defaultFederate.rtiamb.federateSaveBegun();
		}
		catch( Exception e )
		{
			unexpectedException( "Telling RTI that a save has begun", e );
		}
		
		// the call didn't result in an error, so we must be good.
	}

	////////////////////////////////////////////////////
	// TEST: testFederateSaveBegunWithoutActiveSave() //
	////////////////////////////////////////////////////
	@Test
	public void testFederateSaveBegunWithoutActiveSave()
	{
		try
		{
			defaultFederate.rtiamb.federateSaveBegun();
			expectedException( SaveNotInitiated.class );
		}
		catch( SaveNotInitiated sni )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, SaveNotInitiated.class );
		}
	}

	////////////////////////////////////////////////
	// TEST: testFederateSaveBegunWhenNotJoined() //
	////////////////////////////////////////////////
	@Test
	public void testFederateSaveBegunWhenNotJoined()
	{
		defaultFederate.quickResign();

		try
		{
			defaultFederate.rtiamb.federateSaveBegun();
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

	////////////////////////////////////////////////////////
	// TEST: testFederateSaveBegunWhenRestoreInProgress() //
	////////////////////////////////////////////////////////
	@Test
	public void testFederateSaveBegunWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.federateSaveBegun();
			expectedException( RestoreInProgress.class );
		}
		catch( RestoreInProgress rip )
		{
			// success
		}
		catch( Exception e )
		{
			wrongException( e, RestoreInProgress.class );
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Save Complete Test Methods //////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	// public void federateSaveComplete()
	//     throws SaveNotInitiated,
	//            FederateNotExecutionMember,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

	//////////////////////////////////////////////
	// TEST: (valid) testFederateSaveComplete() //
	//////////////////////////////////////////////
	@Test
	public void testFederateSaveComplete()
	{
		defaultFederate.quickSaveRequest( "save" );
		defaultFederate.fedamb.waitForSaveInitiated( "save" );
		secondFederate.fedamb.waitForSaveInitiated( "save" );
		defaultFederate.quickSaveBegun();
		secondFederate.quickSaveBegun();
		
		try
		{
			defaultFederate.rtiamb.federateSaveComplete();
			secondFederate.rtiamb.federateSaveComplete();
		}
		catch( Exception e )
		{
			unexpectedException( "Telling RTI that a save has been completed", e );
		}

		// all are complete, wait for notification that federation has saved happily
		defaultFederate.fedamb.waitForFederationSaved();
		secondFederate.fedamb.waitForFederationSaved();
	}

	///////////////////////////////////////////////////////
	// TEST: testFederateSaveCompleteWithoutActiveSave() //
	///////////////////////////////////////////////////////
	@Test
	public void testFederateSaveCompleteWithoutActiveSave()
	{
		try
		{
			defaultFederate.rtiamb.federateSaveComplete();
			expectedException( SaveNotInitiated.class );
		}
		catch( SaveNotInitiated sni )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, SaveNotInitiated.class );
		}
	}

	///////////////////////////////////////////////////
	// TEST: testFederateSaveCompleteWhenNotJoined() //
	///////////////////////////////////////////////////
	@Test
	public void testFederateSaveCompleteWhenNotJoined()
	{
		defaultFederate.quickResign();

		try
		{
			defaultFederate.rtiamb.federateSaveComplete();
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

	///////////////////////////////////////////////////////////
	// TEST: testFederateSaveCompleteWhenRestoreInProgress() //
	///////////////////////////////////////////////////////////
	@Test
	public void testFederateSaveCompleteWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.federateSaveComplete();
			expectedException( RestoreInProgress.class );
		}
		catch( RestoreInProgress rip )
		{
			// success
		}
		catch( Exception e )
		{
			wrongException( e, RestoreInProgress.class );
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Save Not Complete Test Methods ////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	// public void federateSaveNotComplete()
	//     throws SaveNotInitiated,
	//            FederateNotExecutionMember,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

	//////////////////////////////////////////////////
	// TEST: (valid) testFederateSaveNotCompleted() //
	//////////////////////////////////////////////////
	@Test
	public void testFederateSaveNotCompleted()
	{
		defaultFederate.quickSaveRequest( "save" );
		defaultFederate.fedamb.waitForSaveInitiated( "save" );
		secondFederate.fedamb.waitForSaveInitiated( "save" );
		defaultFederate.quickSaveBegun();
		secondFederate.quickSaveBegun();
		defaultFederate.quickSaveComplete();
		
		try
		{
			secondFederate.rtiamb.federateSaveNotComplete();
		}
		catch( Exception e )
		{
			unexpectedException( "Telling RTI that a save has been not completed", e );
		}

		// all are complete, wait for notification that federation has saved happily
		defaultFederate.fedamb.waitForFederationNotSaved();
		secondFederate.fedamb.waitForFederationNotSaved();
	}

	///////////////////////////////////////////////////////////
	// TEST: testFederateSaveNotCompletedWithoutActiveSave() //
	///////////////////////////////////////////////////////////
	@Test
	public void testFederateSaveNotCompletedWithoutActiveSave()
	{
		try
		{
			defaultFederate.rtiamb.federateSaveNotComplete();
			expectedException( SaveNotInitiated.class );
		}
		catch( SaveNotInitiated sni )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, SaveNotInitiated.class );
		}
	}

	///////////////////////////////////////////////////////
	// TEST: testFederateSaveNotCompletedWhenNotJoined() //
	///////////////////////////////////////////////////////
	@Test
	public void testFederateSaveNotCompletedWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			defaultFederate.rtiamb.federateSaveNotComplete();
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

	///////////////////////////////////////////////////////////////
	// TEST: testFederateSaveNotCompletedWhenRestoreInProgress() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testFederateSaveNotCompletedWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.federateSaveNotComplete();
			expectedException( RestoreInProgress.class );
		}
		catch( RestoreInProgress rip )
		{
			// success
		}
		catch( Exception e )
		{
			wrongException( e, RestoreInProgress.class );
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Miscellaneous Test Methods //////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testFederationSaveCompletesWhenFederateResignsMidwayThrough() //
	/////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testFederationSaveCompletesWhenFederateResignsBeforeSaveBegun()
	{
		defaultFederate.quickSaveRequest( "save" );
		defaultFederate.fedamb.waitForSaveInitiated( "save" );
		secondFederate.fedamb.waitForSaveInitiated( "save" );
		defaultFederate.quickSaveBegun();
		// compelte in the default federate
		defaultFederate.quickSaveComplete();

		// resign the second federate
		secondFederate.quickResign();

		// wait for the federation saved notice
		defaultFederate.fedamb.waitForFederationSaved();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testFederationSaveCompletesWhenFederateResignsBeforeSaveComplete() //
	//////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testFederationSaveCompletesWhenFederateResignsBeforeSaveComplete()
	{
		defaultFederate.quickSaveRequest( "save" );
		defaultFederate.fedamb.waitForSaveInitiated( "save" );
		secondFederate.fedamb.waitForSaveInitiated( "save" );
		defaultFederate.quickSaveBegun();
		secondFederate.quickSaveBegun();
		// compelte in the default federate
		defaultFederate.quickSaveComplete();
		
		// resign the second federate
		secondFederate.quickResign();

		// wait for the federation saved notice
		defaultFederate.fedamb.waitForFederationSaved();
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testFederationSaveCompletesWhenOneFederateDoesntCompleteButResigns() //
	////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testFederationSaveCompletesWhenOneFederateDoesntCompleteButResigns()
	{
		defaultFederate.quickSaveRequest( "save" );
		defaultFederate.fedamb.waitForSaveInitiated( "save" );
		secondFederate.fedamb.waitForSaveInitiated( "save" );
		defaultFederate.quickSaveBegun();
		secondFederate.quickSaveBegun();
		
		secondFederate.quickSaveNotComplete();
		defaultFederate.quickTick( 0.1, 1.0 ); // make sure the default fedeate knows about it
		
		// resign the default federate
		secondFederate.quickResign();

		// wait for the federation saved notice
		defaultFederate.quickSaveComplete();
		defaultFederate.fedamb.waitForFederationSaved();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
