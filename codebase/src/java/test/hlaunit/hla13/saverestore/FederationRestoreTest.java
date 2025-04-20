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
import hla.rti.RTIinternalError;
import hla.rti.RestoreInProgress;
import hla.rti.RestoreNotRequested;
import hla.rti.SaveInProgress;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * These tests validate that the process of calls required to carry out a federation restore works
 * correctly (and that when fed improper input, that they fail correctly). Please note that these
 * tests do NOT validate that the appropriate state was saved/restored properly. For example, the
 * tests to ensure that the proper time-status for federates is saved/restored are located in the
 * time management tests. 
 */
@Test(singleThreaded=true, groups={"FederationRestoreTest", "federationRestore", "SaveRestore"})
public class FederationRestoreTest extends Abstract13Test
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
		
		defaultFederate.quickSaveToCompletion( "save" );
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
	
	/////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Request Restore Test Methods //////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	// public void requestFederationRestore( String label )
	//     throws FederateNotExecutionMember,
	//            SaveInProgress,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

	//////////////////////////////////////////////////
	// TEST: (valid) testRequestFederationRestore() //
	//////////////////////////////////////////////////
	@Test
	public void testRequestFederationRestore()
	{
		try
		{
			defaultFederate.rtiamb.requestFederationRestore( "save" );
		}
		catch( Exception e )
		{
			unexpectedException( "Requesting a valid federaiton restore", e );
		}
		
		defaultFederate.fedamb.waitForRestoreRequestSuccess( "save" );
	}

	///////////////////////////////////////////////////////
	// TEST: testRequestFederationRestoreWithNullLabel() //
	///////////////////////////////////////////////////////
	@Test
	public void testRequestFederationRestoreWithNullLabel()
	{
		try
		{
			defaultFederate.rtiamb.requestFederationRestore( null );
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

	//////////////////////////////////////////////////////////
	// TEST: testRequestFederationRestoreWithUnknownLabel() //
	//////////////////////////////////////////////////////////
	@Test
	public void testRequestFederationRestoreWithUnknownLabel()
	{
		try
		{
			defaultFederate.rtiamb.requestFederationRestore( "unknown" );
		}
		catch( Exception e )
		{
			unexpectedException(
			 "Requesting restore with unknown label. Should fail through callback, not exception", e );
		}
		
		defaultFederate.fedamb.waitForRestoreRequestFailure( "unknown" );
	}

	///////////////////////////////////////////////////////
	// TEST: testRequestFederationRestoreWhenNotJoined() //
	///////////////////////////////////////////////////////
	@Test
	public void testRequestFederationRestoreWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			defaultFederate.rtiamb.requestFederationRestore( "save" );
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

	////////////////////////////////////////////////////////////
	// TEST: testRequestFederationRestoreWhenSaveInProgress() //
	////////////////////////////////////////////////////////////
	@Test
	public void testRequestFederationRestoreWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.requestFederationRestore( "save" );
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

	///////////////////////////////////////////////////////////////
	// TEST: testRequestFederationRestoreWhenRestoreInProgress() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testRequestFederationRestoreWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.requestFederationRestore( "save" );
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

	/////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Restore Complete Test Methods /////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	// public void federateRestoreComplete()
	//     throws RestoreNotRequested,
	//            FederateNotExecutionMember,
	//            SaveInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

	///////////////////////////////////////////////////
	// TEST: (valid) testFederationRestoreComplete() //
	///////////////////////////////////////////////////
	@Test
	public void testFederationRestoreComplete()
	{
		defaultFederate.quickRestoreRequest( "save" );
		defaultFederate.fedamb.waitForRestoreRequestSuccess( "save" );
		defaultFederate.fedamb.waitForFederationRestoreBegun();
		secondFederate.fedamb.waitForFederationRestoreBegun();
		defaultFederate.fedamb.waitForFederateRestoreInitiated( "save" );
		secondFederate.fedamb.waitForFederateRestoreInitiated( "save" );
		
		try
		{
			defaultFederate.rtiamb.federateRestoreComplete();
			secondFederate.rtiamb.federateRestoreComplete();
		}
		catch( Exception e )
		{
			unexpectedException( "Telling the RTI that federate has restored", e );
		}

		defaultFederate.fedamb.waitForFederationRestored();
		secondFederate.fedamb.waitForFederationRestored();
	}

	///////////////////////////////////////////////////////////////
	// TEST: testFederationRestoreCompleteWithoutActiveRestore() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testFederationRestoreCompleteWithoutActiveRestore()
	{
		try
		{
			defaultFederate.rtiamb.federateRestoreComplete();
			expectedException( RestoreNotRequested.class );
		}
		catch( RestoreNotRequested rnr )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RestoreNotRequested.class );
		}
	}

	////////////////////////////////////////////////////////
	// TEST: testFederationRestoreCompleteWhenNotJoined() //
	////////////////////////////////////////////////////////
	@Test
	public void testFederationRestoreCompleteWhenNotJoined()
	{
		defaultFederate.quickResign();

		try
		{
			defaultFederate.rtiamb.federateRestoreComplete();
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

	/////////////////////////////////////////////////////////////
	// TEST: testFederationRestoreCompleteWhenSaveInProgress() //
	/////////////////////////////////////////////////////////////
	@Test
	public void testFederationRestoreCompleteWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.federateRestoreComplete();
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

	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Restore Not Complete Test Methods ///////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	// public void federateRestoreNotComplete()
	//     throws RestoreNotRequested,
	//            FederateNotExecutionMember,
	//            SaveInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

	//////////////////////////////////////////////////////
	// TEST: (valid) testFederationRestoreNotComplete() //
	//////////////////////////////////////////////////////
	@Test
	public void testFederationRestoreNotComplete()
	{
		defaultFederate.quickRestoreRequest( "save" );
		defaultFederate.fedamb.waitForRestoreRequestSuccess( "save" );
		defaultFederate.fedamb.waitForFederationRestoreBegun();
		secondFederate.fedamb.waitForFederationRestoreBegun();
		defaultFederate.fedamb.waitForFederateRestoreInitiated( "save" );
		secondFederate.fedamb.waitForFederateRestoreInitiated( "save" );
		defaultFederate.quickRestoreComplete();
		
		try
		{
			secondFederate.rtiamb.federateRestoreNotComplete();
		}
		catch( Exception e )
		{
			unexpectedException( "Telling the RTI that federate has not restored", e );
		}

		defaultFederate.fedamb.waitForFederationNotRestored();
		secondFederate.fedamb.waitForFederationNotRestored();
	}

	//////////////////////////////////////////////////////////////////
	// TEST: testFederationRestoreNotCompleteWithoutActiveRestore() //
	//////////////////////////////////////////////////////////////////
	@Test
	public void testFederationRestoreNotCompleteWithoutActiveRestore()
	{
		try
		{
			defaultFederate.rtiamb.federateRestoreNotComplete();
			expectedException( RestoreNotRequested.class );
		}
		catch( RestoreNotRequested rnr )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RestoreNotRequested.class );
		}
	}

	///////////////////////////////////////////////////////////
	// TEST: testFederationRestoreNotCompleteWhenNotJoined() //
	///////////////////////////////////////////////////////////
	@Test
	public void testFederationRestoreNotCompleteWhenNotJoined()
	{
		defaultFederate.quickResign();

		try
		{
			defaultFederate.rtiamb.federateRestoreNotComplete();
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

	////////////////////////////////////////////////////////////////
	// TEST: testFederationRestoreNotCompleteWhenSaveInProgress() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testFederationRestoreNotCompleteWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.federateRestoreNotComplete();
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

	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Miscellaneous Test Methods //////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testFederationRestoreCompletesWhenFederateResignsBeforeRestoreComplete() //
	////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testFederationRestoreCompletesWhenFederateResignsBeforeRestoreComplete()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		// compelte in the default federate
		defaultFederate.quickRestoreComplete();

		// resign the second federate
		secondFederate.quickResign();

		// wait for the federation saved notice
		defaultFederate.fedamb.waitForFederationRestored();
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testFederationRestoreCompletesWhenOneFederateDoesntCompleteButResigns() //
	///////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testFederationRestoreCompletesWhenOneFederateDoesntCompleteButResigns()
	{
		defaultFederate.quickRestoreInProgress( "save" );

		// fail in the second federate and make sure the default federate has time to hear about it
		secondFederate.quickRestoreNotComplete();
		defaultFederate.quickTick( 0.1, 1.0 ); // make sure the default fedeate knows about it
		
		// resign the default federate
		secondFederate.quickResign();

		// wait for the federation saved notice
		defaultFederate.quickRestoreComplete();
		defaultFederate.fedamb.waitForFederationRestored();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
