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
package hlaunit.hla13.time;

import hla.rti.EnableTimeConstrainedPending;
import hla.rti.FederateNotExecutionMember;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hla.rti.TimeAdvanceAlreadyInProgress;
import hla.rti.TimeConstrainedAlreadyEnabled;
import hla.rti.TimeConstrainedWasNotEnabled;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"TimeConstrainedTest", "timeConstrained", "timeManagement"})
public class TimeConstrainedTest extends Abstract13Test
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
	
	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		secondFederate.quickResign();
		defaultFederate.quickResign();
		defaultFederate.quickDestroy();
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Enable Constrained Methods ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// public void enableTimeConstrained()
	//        throws TimeConstrainedAlreadyEnabled,
	//               EnableTimeConstrainedPending,
	//               TimeAdvanceAlreadyInProgress,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	////////////////////////////////////////////
	// TEST: (valid) testConstrainedEnabled() //
	////////////////////////////////////////////
	@Test
	public void testConstrainedEnabled()
	{
		try
		{
			defaultFederate.rtiamb.enableTimeConstrained();
		}
		catch( Exception e )
		{
			unexpectedException( "enabling time constrained", e );
		}
		
		// make sure we get the callback
		defaultFederate.fedamb.waitForConstrainedEnabled();
		
		// validate that we are now constrained
		secondFederate.quickEnableRegulating( 1.0 );
		defaultFederate.quickAdvanceRequest( 10.0 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 10.0 );
		secondFederate.quickAdvanceAndWait( 10.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 10.0 );
	}

	//////////////////////////////////////////////////////
	// TEST: testConstrainedEnabledWhenAlreadyEnabled() //
	//////////////////////////////////////////////////////
	@Test
	public void testConstrainedEnabledWhenAlreadyEnabled()
	{
		defaultFederate.quickEnableConstrained();
		try
		{
			defaultFederate.rtiamb.enableTimeConstrained();
			expectedException( TimeConstrainedAlreadyEnabled.class );
		}
		catch( TimeConstrainedAlreadyEnabled tcae )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, TimeConstrainedAlreadyEnabled.class );
		}
	}

	///////////////////////////////////////////////
	// TEST: testConstrainedEnabledWhenPending() //
	///////////////////////////////////////////////
	@Test
	public void testConstrainedEnabledWhenPending()
	{
		try
		{
			defaultFederate.rtiamb.enableTimeConstrained();
			defaultFederate.rtiamb.enableTimeConstrained();
			expectedException( EnableTimeConstrainedPending.class );
		}
		catch( EnableTimeConstrainedPending etcp )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, EnableTimeConstrainedPending.class );
		}
	}

	////////////////////////////////////////////////////
	// TEST: testConstrainedEnableWhenTarInProgress() //
	////////////////////////////////////////////////////
	@Test
	public void testConstrainedEnableWhenTarInProgress()
	{
		// kick off a time advance
		defaultFederate.quickAdvanceRequest( 1.0 );
		
		// enable constrained
		try
		{
			defaultFederate.rtiamb.enableTimeConstrained();
			expectedException( TimeAdvanceAlreadyInProgress.class );
		}
		catch( TimeAdvanceAlreadyInProgress taaip )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, TimeAdvanceAlreadyInProgress.class );
		}
	}

	/////////////////////////////////////////////////////
	// TEST: testConstrainedEnableWhenTaraInProgress() //
	/////////////////////////////////////////////////////
	@Test
	public void testConstrainedEnableWhenTaraInProgress()
	{
		// kick off a time advance
		defaultFederate.quickAdvanceRequestAvailable( 1.0 );
		
		// enable constrained
		try
		{
			defaultFederate.rtiamb.enableTimeConstrained();
			expectedException( TimeAdvanceAlreadyInProgress.class );
		}
		catch( TimeAdvanceAlreadyInProgress taaip )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, TimeAdvanceAlreadyInProgress.class );
		}
	}

	////////////////////////////////////////////////////
	// TEST: testConstrainedEnableWithNerInProgress() //
	////////////////////////////////////////////////////
	@Test
	public void testConstrainedEnableWithNerInProgress()
	{
		// kick off a time advance
		defaultFederate.quickNextEventRequest( 1.0 );
		
		// enable constrained
		try
		{
			defaultFederate.rtiamb.enableTimeConstrained();
			expectedException( TimeAdvanceAlreadyInProgress.class );
		}
		catch( TimeAdvanceAlreadyInProgress taaip )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, TimeAdvanceAlreadyInProgress.class );
		}
	}

	//////////////////////////////////////////////////////////
	// TEST: testConstrainedEnabledWhenNotExecutionMember() //
	//////////////////////////////////////////////////////////
	@Test
	public void testConstrainedEnabledWhenNotExecutionMember()
	{
		// resign so we can run the test
		defaultFederate.quickResign();
		
		try
		{
			defaultFederate.rtiamb.enableTimeConstrained();
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

	//////////////////////////////////////////////////////
	// TEST: testConstrainedEnabledWhenSaveInProgress() //
	//////////////////////////////////////////////////////
	@Test
	public void testConstrainedEnabledWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.enableTimeConstrained();
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

	//////////////////////////////////////////////////////
	// TEST: testConstrainedEnabledWhenSaveInProgress() //
	//////////////////////////////////////////////////////
	@Test
	public void testConstrainedEnabledWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.enableTimeConstrained();
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

	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Disable Constrained Methods ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// public void disableTimeConstrained()
	//        throws TimeConstrainedWasNotEnabled,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	////////////////////////////////////////////
	// TEST: (valid) testConstrainedDisable() //
	////////////////////////////////////////////
	@Test
	public void testConstrainedDisable()
	{
		// enabble constrained so we can run the test
		defaultFederate.quickEnableConstrained();
		
		try
		{
			defaultFederate.rtiamb.disableTimeConstrained();
		}
		catch( Exception e )
		{
			unexpectedException(  "disabling time constrained", e  );
		}
		
		// validate that time constrained is disabled
		
		// enable regulating in the second federate
		// so that there is something to hold up constrained federates
		secondFederate.quickEnableRegulating( 5.0 );
		
		// try and advance, which should be fine because the other federate shouldn't affect us
		defaultFederate.quickAdvanceAndWait( 100.0 );
	}

	//////////////////////////////////////////////////
	// TEST: testConstrainedDisableWhenNotEnabled() //
	//////////////////////////////////////////////////
	@Test
	public void testConstrainedDisableWhenNotEnabled()
	{
		try
		{
			defaultFederate.rtiamb.disableTimeConstrained();
			expectedException( TimeConstrainedWasNotEnabled.class );
		}
		catch( TimeConstrainedWasNotEnabled tcwne )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, TimeConstrainedWasNotEnabled.class );
		}
	}

	/////////////////////////////////////////////////
	// TEST: testConstrainedDisableWhenNotJoined() //
	/////////////////////////////////////////////////
	@Test
	public void testConstrainedDisableWhenNotJoined()
	{
		// resign so we can run the test
		defaultFederate.quickResign();
		
		try
		{
			defaultFederate.rtiamb.disableTimeConstrained();
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

	//////////////////////////////////////////////////////
	// TEST: testConstrainedDisableWhenSaveInProgress() //
	//////////////////////////////////////////////////////
	@Test
	public void testConstrainedDisableWhenSaveInProgress()
	{
		// resign so we can run the test
		defaultFederate.quickSaveInProgress( "save" );
		
		try
		{
			defaultFederate.rtiamb.disableTimeConstrained();
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

	/////////////////////////////////////////////////////////
	// TEST: testConstrainedDisableWhenRestoreInProgress() //
	/////////////////////////////////////////////////////////
	@Test
	public void testConstrainedDisableWhenRestoreInProgress()
	{
		// resign so we can run the test
		defaultFederate.quickRestoreInProgress( "save" );
		
		try
		{
			defaultFederate.rtiamb.disableTimeConstrained();
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

	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Correctness Test Methods ////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// While the other test methods of this class test that  //
	// the RTIambassador methods behave correctly when given //
	// either valid or invalid parameters (or when called by //
	// federates in valid or invalid state), these methods   //
	// make sure that the RTI enforces the proper behaviour  //
	// by coordinating actions between a group of federates. //
	// These methods use helper methods to avoid having to   //
	// handle a large number of exceptions.                  //
	///////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////
	// TEST: (valid) testEnableConstrainedRestrictsAdvancement() //
	///////////////////////////////////////////////////////////////
	/**
	 * This method tests that when a federate becomes constrained, its advancement progress
	 * becomes dependent on the regulating federates in the federation
	 */
	@Test
	public void testEnableConstrainedRestrictsAdvancement()
	{
		// do some basic setup
		defaultFederate.quickEnableConstrained();
		secondFederate.quickEnableRegulating( 5.0 );
		
		// try and advance the constrained federate ane make sure
		// that the regulating federate restricts this
		defaultFederate.quickAdvanceRequest( 10.0 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 10.0 );
		secondFederate.quickAdvanceAndWait( 20.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 10.0 );
	}

	////////////////////////////////////////////////////////////////
	// TEST: (valid) testEnableConstrainedForAdvancedFederate13() //
	////////////////////////////////////////////////////////////////
	// NOTE: The method below outlines how the process works for the 1516 spec, not
	//       the 1.3 spec. In RTI-NGv6, time constrained is enabled immediately according
	//       to appendix b.
	/**
	 * This method ensures that when a federate that has advanced some way attempts to become
	 * constrained, that it is not delivered the "constrained enabled" callback until the
	 * regulating federates in the federation have advanced such that the constrained-to-be
	 * federate will not receive events in the past
	 */
	/*
	@Test
	public void testEnableConstrainedStallsForWellAdvancedFederate()
	{
		// do some basic setup
		secondFederate.quickEnableRegulating( 5.0 );
		defaultFederate.quickAdvanceAndWait( 100.0 );
		defaultFederate.quickEnableConstrainedRequest();
		defaultFederate.fedamb.waitForConstrainedEnabledTimeout();
		
		// advance regulating federate so that the other federate can get the constrained callback
		secondFederate.quickAdvanceAndWait( 200.0 );
		defaultFederate.fedamb.waitForConstrainedEnabled();
	}*/
	
	/**
	 * This method tests the enable constrained process for a federae that has a logical time
	 * that is greater than the federation LBTS. In 1.3 (according to Appendix B), the enable
	 * callback is delivered right away, regardless of the state of the other federates. However,
	 * in 1516, the callback should not be delivered until the federation LBTS is greater than the
	 * logical time of the federate. In this method we test the 1.3 version
	 */
	@Test
	public void testEnableConstrainedForAdvancedFederate13()
	{
		// do some basic setup
		secondFederate.quickEnableRegulating( 5.0 );
		defaultFederate.quickAdvanceAndWait( 100.0 );
		
		// now enable time constrained
		// we should get this right away, even though the federation lbts
		// is currently well below our logical time (and thus, events could
		// still be generated in the past)
		defaultFederate.quickEnableConstrained();
		
		// this situations, as defined in 1.3, could actually cause us some
		// pretty annoying problems. we should change the behaviour to conform
		// to the 1516 way of doing things, as it is better. That said, this
		// situation should rarely (if ever) arise in the real world.
		log( "testEnableConstrainedForAdvancedFederate13() is HLA 1.3 specific" );
	}

	////////////////////////////////////////////////////////
	// TEST: (valid) testEnabledConstrainedAfterDisable() //
	////////////////////////////////////////////////////////
	/**
	 * This method tests that constrained can be re-enabled after it has been previously disabled
	 */
	@Test
	public void testEnabledConstrainedAfterDisable()
	{
		// enable constrained, then disable it, then enable it again
		defaultFederate.quickEnableConstrained();
		defaultFederate.quickDisableConstrained();
		defaultFederate.quickEnableConstrained();
		
		// validate that we are now constrained
		secondFederate.quickEnableRegulating( 1.0 );
		defaultFederate.quickAdvanceRequest( 10.0 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 10.0 );
		secondFederate.quickAdvanceAndWait( 10.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 10.0 );
	}

	//////////////////////////////////////////////////////////////
	// TEST: testDisableConstrainedMidAdvanceReleasesFederate() //
	//////////////////////////////////////////////////////////////
	/**
	 * This test makes sure that when a federate with an outstanding TAR (that is being held up by
	 * other federates) disables time constrained, that it gets its grant right away, even though
	 * the regulating federates are in a position that would deny it.
	 */
	@Test
	public void testDisableConstrainedMidAdvanceReleasesFederate()
	{
		// do some basic setup
		secondFederate.quickEnableRegulating( 5.0 );
		defaultFederate.quickEnableConstrained();
		
		// try and advance the constrained federate but have it
		// held up by the regulating federate
		defaultFederate.quickAdvanceRequest( 50.0 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 50.0 );
		
		// disable constrained and make sure the federate is now released
		defaultFederate.quickDisableConstrained();
		defaultFederate.fedamb.waitForTimeAdvance( 50.0 );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
