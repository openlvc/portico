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

import hla.rti1516.FederateNotExecutionMember;
import hla.rti1516.InTimeAdvancingState;
import hla.rti1516.RequestForTimeConstrainedPending;
import hla.rti1516.TimeConstrainedAlreadyEnabled;
import hla.rti1516.TimeConstrainedIsNotEnabled;
import hlaunit.ieee1516.common.Abstract1516Test;
import hlaunit.ieee1516.common.TestFederate;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"TimeConstrainedTest", "timeConstrained", "timeManagement"})
public class TimeConstrainedTest extends Abstract1516Test
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
	//               InTimeAdvancingState,
	//               RequestForTimeConstrainedPending,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;
	
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
			expectedException( RequestForTimeConstrainedPending.class );
		}
		catch( RequestForTimeConstrainedPending rftcp )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RequestForTimeConstrainedPending.class );
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
			expectedException( InTimeAdvancingState.class );
		}
		catch( InTimeAdvancingState taaip )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InTimeAdvancingState.class );
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
			expectedException( InTimeAdvancingState.class );
		}
		catch( InTimeAdvancingState taaip )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InTimeAdvancingState.class );
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
			expectedException( InTimeAdvancingState.class );
		}
		catch( InTimeAdvancingState taaip )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InTimeAdvancingState.class );
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
	
	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Disable Constrained Methods ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// public void disableTimeConstrained()
	//        throws TimeConstrainedIsNotEnabled,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

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
			expectedException( TimeConstrainedIsNotEnabled.class );
		}
		catch( TimeConstrainedIsNotEnabled tcine )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, TimeConstrainedIsNotEnabled.class );
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
	@Test(enabled=false)
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
