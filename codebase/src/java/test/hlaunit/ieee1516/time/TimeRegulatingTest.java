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
import hla.rti1516.InvalidLookahead;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.RequestForTimeRegulationPending;
import hla.rti1516.TimeRegulationAlreadyEnabled;
import hla.rti1516.TimeRegulationIsNotEnabled;
import hlaunit.ieee1516.common.Abstract1516Test;
import hlaunit.ieee1516.common.TestFederate;
import hlaunit.ieee1516.common.TimeoutException;
import hlaunit.ieee1516.common.TypeFactory;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"TimeRegulatingTest", "timeRegulating", "timeManagement"})
public class TimeRegulatingTest extends Abstract1516Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate secondFederate;

	private LogicalTimeInterval defaultInterval;

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
		secondFederate = new TestFederate( "secondFederate", this );
		
		defaultInterval = TypeFactory.createInterval( 10.0 );
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
	}
	
	@Override
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

	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Enable Regulation Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void enableTimeRegulation( LogicalTimeInterval theLookahead )
	//        throws TimeRegulationAlreadyEnabled,
	//               InvalidLookahead,
	//               InTimeAdvancingState,
	//               RequestForTimeRegulationPending,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

	//////////////////////////////////////////
	// TEST: (valid) testRegulationEnable() //
	//////////////////////////////////////////
	@Test
	public void testRegulationEnable()
	{
		try
		{
			defaultFederate.rtiamb.enableTimeRegulation( defaultInterval );
		}
		catch( Exception e )
		{
			unexpectedException( "enabling time regulation", e );
		}
		
		// wait for the enabled callback
		defaultFederate.fedamb.waitForRegulatingEnabled();
		
		// validate that we are regulating
		// send a TSO interaction from the regulating federate to the constrained federate
		secondFederate.quickEnableConstrained();
		secondFederate.quickSubscribe( "InteractionRoot.X" );
		defaultFederate.quickPublish( "InteractionRoot.X" );
		defaultFederate.quickSend( "InteractionRoot.X", null, "".getBytes(), 20.0 );
		defaultFederate.quickAdvanceAndWait( 100.0 );
		secondFederate.quickAdvanceRequest( 30.0 );
		// wait for the interaction to come in TSO
		secondFederate.fedamb.waitForTSOInteraction( "InteractionRoot.X" );
	}

	///////////////////////////////////////////////////
	// TEST: testRegulationEnableWithNullLookahead() //
	///////////////////////////////////////////////////
	@Test
	public void testRegulationEnableWithNullLookahead()
	{
		try
		{
			defaultFederate.rtiamb.enableTimeRegulation( null );
			expectedException( InvalidLookahead.class );
		}
		catch( InvalidLookahead il )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidLookahead.class );
		}
	}

	/////////////////////////////////////////////////
	// TEST: testRegulationWithNegativeLookahead() //
	/////////////////////////////////////////////////
	@Test
	public void testRegulationWithNegativeLookahead()
	{
		LogicalTimeInterval interval = TypeFactory.createInterval( -1.0 );
		try
		{
			defaultFederate.rtiamb.enableTimeRegulation( interval );
			expectedException( InvalidLookahead.class );
		}
		catch( InvalidLookahead il )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidLookahead.class );
		}
	}

	/////////////////////////////////////////////
	// TEST: testRegulationEnableWhenPending() //
	/////////////////////////////////////////////
	@Test
	public void testRegulationEnableWhenPending()
	{
		// get a regulation request out there
		try
		{
			defaultFederate.rtiamb.enableTimeRegulation( defaultInterval );
		}
		catch( Exception e )
		{
			unexpectedException( "enabling time regulation", e );
		}
		
		// try and get another request going
		try
		{
			defaultFederate.rtiamb.enableTimeRegulation( defaultInterval );
			expectedException( RequestForTimeRegulationPending.class );
		}
		catch( RequestForTimeRegulationPending rftrp )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RequestForTimeRegulationPending.class );
		}
	}

	////////////////////////////////////////////////////
	// TEST: testRegulationEnableWhenAlreadyEnabled() //
	////////////////////////////////////////////////////
	@Test
	public void testRegulationEnableWhenAlreadyEnabled()
	{
		// enable regulation
		defaultFederate.quickEnableRegulating( 1.0 );
		
		// try and enable regulation again
		try
		{
			defaultFederate.rtiamb.enableTimeRegulation( defaultInterval );
			expectedException( TimeRegulationAlreadyEnabled.class );
		}
		catch( TimeRegulationAlreadyEnabled traa )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, TimeRegulationAlreadyEnabled.class );
		}
	}

	///////////////////////////////////////////////////
	// TEST: testRegulationEnableWhenTarInProgress() //
	///////////////////////////////////////////////////
	@Test
	public void testRegulationEnableWhenTarInProgress()
	{
		// kick off a time advance
		defaultFederate.quickAdvanceRequest( 1.0 );
		
		// try and enable regulation
		try
		{
			defaultFederate.rtiamb.enableTimeRegulation( defaultInterval );
			expectedException( InTimeAdvancingState.class );
		}
		catch( InTimeAdvancingState itas )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InTimeAdvancingState.class );
		}
	}

	////////////////////////////////////////////////////
	// TEST: testRegulationEnableWhenTaraInProgress() //
	////////////////////////////////////////////////////
	@Test
	public void testRegulationEnableWhenTaraInProgress()
	{
		// kick off a time advance
		defaultFederate.quickAdvanceRequestAvailable( 1.0 );
		
		// try and enable regulation
		try
		{
			defaultFederate.rtiamb.enableTimeRegulation( defaultInterval );
			expectedException( InTimeAdvancingState.class );
		}
		catch( InTimeAdvancingState itas )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InTimeAdvancingState.class );
		}
	}

	///////////////////////////////////////////////////
	// TEST: testRegulationEnableWithNerInProgress() //
	///////////////////////////////////////////////////
	@Test
	public void testRegulationEnableWithNerInProgress()
	{
		// kick off a time advance
		defaultFederate.quickNextEventRequest( 1.0 );
		
		// try and enable regulation
		try
		{
			defaultFederate.rtiamb.enableTimeRegulation( defaultInterval );
			expectedException( InTimeAdvancingState.class );
		}
		catch( InTimeAdvancingState itas )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InTimeAdvancingState.class );
		}
	}

	///////////////////////////////////////////////
	// TEST: testRegulationEnableWhenNotJoined() //
	///////////////////////////////////////////////
	@Test
	public void testRegulationEnableWhenNotJoined()
	{
		// resign so we can run the test
		defaultFederate.quickResign();
		
		try
		{
			defaultFederate.rtiamb.enableTimeRegulation( defaultInterval );
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
	/////////////////////////////// Disable Regulation Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void disableTimeRegulation()
	//        throws TimeRegulationIsNotEnabled,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

	///////////////////////////////////////////
	// TEST: (valid) testRegulationDisable() //
	///////////////////////////////////////////
	@Test
	public void testRegulationDisable()
	{
		// quickly enable regulation so we can run the test
		defaultFederate.quickEnableRegulating( 10.0 );
		
		try
		{
			defaultFederate.rtiamb.disableTimeRegulation();
		}
		catch( Exception e )
		{
			unexpectedException( "disabling time regulation", e );
		}
		
		// validate that regulation is disabled
		// send a TSO interaction from the federate to the constrained federate
		secondFederate.quickEnableConstrained();
		secondFederate.quickEnableAsyncDelivery();
		secondFederate.quickSubscribe( "InteractionRoot.X" );
		defaultFederate.quickPublish( "InteractionRoot.X" );
		defaultFederate.quickSend( "InteractionRoot.X", null, "".getBytes(), 20.0 );
		secondFederate.fedamb.waitForROInteraction( "InteractionRoot.X" );
	}

	/////////////////////////////////////////////////
	// TEST: testRegulationDisableWhenNotEnabled() //
	/////////////////////////////////////////////////
	@Test
	public void testRegulationDisableWhenNotEnabled()
	{
		try
		{
			defaultFederate.rtiamb.disableTimeRegulation();
			expectedException( TimeRegulationIsNotEnabled.class );
		}
		catch( TimeRegulationIsNotEnabled trine )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, TimeRegulationIsNotEnabled.class );
		}
	}

	////////////////////////////////////////////////
	// TEST: testRegulationDisableWhenNotJoined() //
	////////////////////////////////////////////////
	@Test
	public void testRegulationDisableWhenNotJoined()
	{
		// resign so that we can run the test
		defaultFederate.quickResign();
		
		try
		{
			defaultFederate.rtiamb.disableTimeRegulation();
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

	/////////////////////////////////////////////////////////////////
	// TEST: (valid) testRegulationEnableWithLateJoiningFederate() //
	/////////////////////////////////////////////////////////////////
	/**
	 * This method tests that the enabling of time regulation happens as expected. A single
	 * federate is constrained and advances to 100. After this, a second federate attempts to
	 * become regulating. That federate should receive a federate time of 100-lookahead when
	 * it becomes regulating.
	 */
	@Test
	public void testRegulationEnableWithLateJoiningFederate()
	{
		// set a federate as constrained and advance it
		secondFederate.quickEnableConstrained();
		secondFederate.quickAdvanceRequest( 100.0 );
		secondFederate.fedamb.waitForTimeAdvance( 100.0 );
		
		// enable time regulation on the other federate and ensure that we're given the right time
		try
		{
			LogicalTimeInterval lookahead = TypeFactory.createInterval( 5.0 );
			defaultFederate.rtiamb.enableTimeRegulation( lookahead );
			// wait until we are regulating
			defaultFederate.fedamb.waitForRegulatingEnabled();
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid enableTimeRegulation() process" );
		}
		
		// ensure that the time for the federate is correct
		Assert.assertEquals( defaultFederate.fedamb.logicalTime, 95.0,
		    "Enabled time regulation request successful, BUT received incorrect federate time" );
	}

	//////////////////////////////////////////////////////
	// TEST: (valid) testRegulationEnableAfterDisable() //
	//////////////////////////////////////////////////////
	@Test
	public void testRegulationEnableAfterDisable()
	{
		// enable regulation
		defaultFederate.quickEnableRegulating( 10.0 );

		// disable regulation
		defaultFederate.quickDisableRegulating();

		// enable regulation again
		defaultFederate.quickEnableRegulating( 10.0 );
	}

	////////////////////////////////////////////////////
	// TEST: (valid) testRegulationEnableMidAdvance() //
	////////////////////////////////////////////////////
	@Test
	public void testRegulationEnableMidAdvance()
	{
		// request a time advance //
		defaultFederate.quickAdvanceRequest( 5.0 );
		
		// don't tick! otherwise we'll get the callback //
		
		// request enable regulating before we get the advance //
		try
		{
			LogicalTimeInterval lookahead  = TypeFactory.createInterval( 1.0 );
			defaultFederate.rtiamb.enableTimeRegulation( lookahead );
		}
		catch( InTimeAdvancingState itas )
		{
			// success!
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during attempt to enable regulating with " +
			             "advance in progress", e );
		}
	}

	/////////////////////////////////////////////////////
	// TEST: (valid) testRegulationDisableMidAdvance() //
	/////////////////////////////////////////////////////
	/**
	 * Test that a federate can disable regulation mid-advance (and that this will free
	 * up any constrained federates that were being held back)
	 */
	@Test
	public void testRegulationDisableMidAdvance()
	{
		///////////////////////////
		// prepare the federates //
		///////////////////////////
		defaultFederate.quickEnableConstrained();
		secondFederate.quickEnableRegulating( 5 );
		
		// STEP ONE
		// request a time advance in the constrained federate to 50  //
		// this should cause us to block because it is past the LBTS //
		// for the regulating federate                               //
		try
		{
			defaultFederate.quickAdvanceRequest( 50.0 );
			defaultFederate.fedamb.waitForTimeAdvance( 50.0 );
			Assert.fail( "Was able to advance constrained federate past federation LBTS" );
		}
		catch( TimeoutException te )
		{
			// SUCCESS - should be a timeout
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while attempting to advance constrained federate " +
			             "past federation LBTS", e );
		}
		
		// STEP TWO
		// disable time regulation for the second federate. this should //
		// free us up to receive the time advancement right away        //
		secondFederate.quickDisableRegulating();
		defaultFederate.fedamb.waitForTimeAdvance( 50.0 );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
