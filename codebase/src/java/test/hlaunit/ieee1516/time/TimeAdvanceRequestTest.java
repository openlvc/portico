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
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeAlreadyPassed;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.RequestForTimeConstrainedPending;
import hla.rti1516.RequestForTimeRegulationPending;
import hlaunit.ieee1516.common.Abstract1516Test;
import hlaunit.ieee1516.common.TestFederate;
import hlaunit.ieee1516.common.TypeFactory;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"TimeAdvanceRequestTest", "timeAdvanceRequest", "timeManagement"})
public class TimeAdvanceRequestTest extends Abstract1516Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate secondFederate;
	private TestFederate thirdFederate;

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
		
		// create the additional federates we need for the test
		this.secondFederate = new TestFederate( "secondFederate", this );
		this.thirdFederate = new TestFederate( "thirdFederate", this );
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
		thirdFederate.quickJoin();
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		thirdFederate.quickResign();
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
	////////////////////////////////// General Spec Methods //////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// These methods test the basic usage of the spec method, //
	// rather than the entire correct behaviour of the calls. //
	// The section below titled "Correctness Test Methods"    //
	// contains tests that verify proper behaviour.           //
	////////////////////////////////////////////////////////////
	
	// public void timeAdvanceRequest( LogicalTime theTime )
	//        throws InvalidLogicalTime,
	//               LogicalTimeAlreadyPassed,
	//               InTimeAdvancingState,
	//               RequestForTimeRegulationPending,
	//               RequestForTimeConstrainedPending,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

	/////////////////////////////
	// TEST: (valid) testTar() //
	/////////////////////////////
	@Test
	public void testTar()
	{
		// request the time advance
		try
		{
			LogicalTime time = TypeFactory.createTime( 10.0 );
			defaultFederate.rtiamb.timeAdvanceRequest( time );
		}
		catch( Exception e )
		{
			unexpectedException( "requesting tar", e );
		}
		
		// wait for it to be granted
		defaultFederate.fedamb.waitForTimeAdvance( 10.0 );
	}

	/////////////////////////////////
	// TEST: testTarWithNullTime() //
	/////////////////////////////////
	@Test
	public void testTarWithNullTime()
	{
		try
		{
			defaultFederate.rtiamb.timeAdvanceRequest( null );
			expectedException( InvalidLogicalTime.class );
		}
		catch( InvalidLogicalTime ilt )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidLogicalTime.class );
		}
	}
	
	/////////////////////////////////////
	// TEST: testTarWithNegativeTime() //
	/////////////////////////////////////
	@Test
	public void testTarWithNegativeTime()
	{
		LogicalTime time = TypeFactory.createTime( -1.0 );
		try
		{
			defaultFederate.rtiamb.timeAdvanceRequest( time );
			expectedException( LogicalTimeAlreadyPassed.class, InvalidLogicalTime.class );
		}
		catch( LogicalTimeAlreadyPassed ltap )
		{
			// success!
		}
		catch( InvalidLogicalTime ilt )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, LogicalTimeAlreadyPassed.class, InvalidLogicalTime.class );
		}
	}

	///////////////////////////////////
	// TEST: testTarWithTimeInPast() //
	///////////////////////////////////
	@Test
	public void testTarWithTimeInPast()
	{
		// quickly advance to 10 then request advance to 5
		defaultFederate.quickAdvanceAndWait( 10.0 );
		LogicalTime time = TypeFactory.createTime( 5.0 );
		try
		{
			defaultFederate.rtiamb.timeAdvanceRequest( time );
			expectedException( LogicalTimeAlreadyPassed.class );
		}
		catch( LogicalTimeAlreadyPassed ltap )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, LogicalTimeAlreadyPassed.class );
		}
	}

	/////////////////////////////////////////////////
	// TEST: testTarWhileAnotherTarIsOutstanding() //
	/////////////////////////////////////////////////
	@Test
	public void testTarWhileAnotherTarIsOutstanding()
	{
		// make the initial request
		LogicalTime time = TypeFactory.createTime( 5.0 );
		try
		{
			defaultFederate.rtiamb.timeAdvanceRequest( time );
		}
		catch( Exception e )
		{
			unexpectedException( "requesting tar", e );
		}
		
		// make a second request before we receive the advance grant
		LogicalTime time2 = TypeFactory.createTime( 10.0 );
		try
		{
			defaultFederate.rtiamb.timeAdvanceRequest( time2 );
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

	/////////////////////////////////////////////
	// TEST: testTarWhileRegulationIsPending() //
	/////////////////////////////////////////////
	@Test
	public void testTarWhileRegulationIsPending()
	{
		// start the enable regulation process
		try
		{
			LogicalTimeInterval lookahead = TypeFactory.createInterval( 1.0 );
			defaultFederate.rtiamb.enableTimeRegulation( lookahead );
		}
		catch( Exception e )
		{
			unexpectedException( "enabling time regulation", e );
		}
		
		// make the request before time regulation is enabled
		try
		{
			LogicalTime time = TypeFactory.createTime( 20.0 );
			defaultFederate.rtiamb.timeAdvanceRequest( time );
			expectedException( RequestForTimeRegulationPending.class );
		}
		catch( RequestForTimeRegulationPending rfrp )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RequestForTimeRegulationPending.class );
		}
	}

	//////////////////////////////////////////////
	// TEST: testTarWhileConstrainedIsPending() //
	//////////////////////////////////////////////
	@Test
	public void testTarWhileConstrainedIsPending()
	{
		// start the enable regulation process
		try
		{
			defaultFederate.rtiamb.enableTimeConstrained();
		}
		catch( Exception e )
		{
			unexpectedException( "enabling time constrained", e );
		}
		
		// make the request before time regulation is enabled
		try
		{
			LogicalTime time = TypeFactory.createTime( 20.0 );
			defaultFederate.rtiamb.timeAdvanceRequest( time );
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

	//////////////////////////////////
	// TEST: testTarWhenNotJoined() //
	//////////////////////////////////
	@Test
	public void testTarWhenNotJoined()
	{
		// resign so that we can run the test
		defaultFederate.quickResign();
		
		try
		{
			LogicalTime time = TypeFactory.createTime( 20.0 );
			defaultFederate.rtiamb.timeAdvanceRequest( time );
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
	// The methods above check that the TAR method takes the   //
	// appropriate actions to check the parameters that it is  //
	// given. These methods confirm that the TAR calls trigger //
	// the proper behaviour. They set up particular scenarios  //
	// between coordinated federates to validate this. These   //
	// methods don't use the RTIambassador directly, so as to  //
	// avoid a bunch of exception handling and keep the code   //
	// as clear and clean as possible.                         //
	/////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////
	// TEST: (valid) testTarWhileNotRegulatingOrConstrained() //
	////////////////////////////////////////////////////////////
	@Test
	public void testTarWhileNotRegulatingOrConstrained()
	{
		defaultFederate.quickAdvanceRequest( 10.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 10.0 );
		
		defaultFederate.quickAdvanceRequest( 50.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 50.0 );
	}

	/////////////////////////////////////////////////
	// TEST: (valid) testTarWhileConstrainedOnly() //
	/////////////////////////////////////////////////
	@Test
	public void testTarWhileConstrainedOnly()
	{
		// prepare the federates //
		defaultFederate.quickEnableConstrained();
		secondFederate.quickEnableConstrained();
		thirdFederate.quickEnableConstrained();
		
		// advance the federates //
		defaultFederate.quickAdvanceRequest( 10.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 10.0 );
		secondFederate.quickAdvanceRequest( 30.0 );
		secondFederate.fedamb.waitForTimeAdvance( 30.0 );
		thirdFederate.quickAdvanceRequest( 4500.7 );
		thirdFederate.fedamb.waitForTimeAdvance( 4500.7 );
	}

	////////////////////////////////////////////////
	// TEST: (valid) testTarWhileRegulatingOnly() //
	////////////////////////////////////////////////
	@Test
	public void testTarWhileRegulatingOnly()
	{
		// enable regulation on all the federates //
		defaultFederate.quickEnableRegulating( 5.0 );
		secondFederate.quickEnableRegulating( 5.0 );
		thirdFederate.quickEnableRegulating( 5.0 );
		
		// attempt to advance them in time //
		defaultFederate.quickAdvanceRequest( 5.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 5.0 );
		
		secondFederate.quickAdvanceRequest( 10.0 );
		secondFederate.fedamb.waitForTimeAdvance( 10.0 );
		
		thirdFederate.quickAdvanceRequest( 422455663.4 );
		thirdFederate.fedamb.waitForTimeAdvance( 422455663.4 );
	}

	//////////////////////////////////////////////////////////
	// TEST: (valid) testTarWhileConstrainedAndRegulating() //
	//////////////////////////////////////////////////////////
	@Test
	public void testTarWhileConstrainedAndRegulating()
	{
		// enable time status on federats //
		defaultFederate.quickEnableConstrained();
		secondFederate.quickEnableRegulating( 5.0 );
		thirdFederate.quickEnableRegulating( 5.0 );
		
		// request an advance for a regulating federate, should get it right away //
		secondFederate.quickAdvanceRequest( 10.0 );
		secondFederate.fedamb.waitForTimeAdvance( 10.0 );
		
		// request an advance for a constrained federate, should have to wait //
		defaultFederate.quickAdvanceRequest( 10.0 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 10.0 );

		// request an advance of the second federate again, should get it no problems (again) //
		secondFederate.quickAdvanceRequest( 20.0 );
		secondFederate.fedamb.waitForTimeAdvance( 20.0 );
		
		// request an advance of the third federate to 5.0, thus giving it an LBTS of 10.0 //
		// this is the same as the requested time for the default federate (constrained),  //
		// however, as we can't be sure that the third federate won't generate TSOs with a //
		// timestamp that is less than OR EQUAL TO the time requested by the constrained   //
		// federate, we can't grant its advance yet.                                       //
		thirdFederate.quickAdvanceRequest( 5.0 );
		thirdFederate.fedamb.waitForTimeAdvance( 5.0 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 10.0 );
		
		// request an advance of the third federate way ahead, should get it and should also
		// free up the constrained one
		thirdFederate.quickAdvanceRequest( 20.0 );
		thirdFederate.fedamb.waitForTimeAdvance( 20.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 10.0 );
		
		// now request an advancement for the constrained federate (before any of
		// the regulating federates have requested an advance
		defaultFederate.quickAdvanceRequest( 40.0 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 40.0 );
		
		// advance the third federate and make sure the constrained doesn't get it //
		thirdFederate.quickAdvanceRequest( 35.00000001 );
		thirdFederate.fedamb.waitForTimeAdvance( 35.00000001 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 40.0 );
		
		// advance the second federate to 40.0, which means that the default federate //
		// still shouldn't get the advance yet //
		secondFederate.quickAdvanceRequest( 35.0 );
		secondFederate.fedamb.waitForTimeAdvance( 35.0 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 40.0 );
		
		// push the second federate way ahead. with the third just over 40, the constrained //
		// federate should now also be able to get the advance that it has been waiting for //
		secondFederate.quickAdvanceRequest( 100.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 40.0 );
		secondFederate.fedamb.waitForTimeAdvance( 100.0 );
	}

	//////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testTarWhileRegulatingAndConstrainedWithDefaultLookahead() //
	//////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will have a single regulating and constrained federate that became regulating
	 * using the default lookahead. This test has been created due to a bug that was identified
	 * while trying to get the RTI-NGv6 HelloWorld federate working with Portico.
	 */
	@Test
	public void testTarWithSingleRegulatingAndConstrained()
	{
		// create a whole other federation for us to work in with an
		// entirely separate test federate
		TestFederate singleTest = new TestFederate( "singleTest", this );
		singleTest.quickCreate( "singleTestFederation" );
		singleTest.quickJoin( "singleTestFederation" );
		singleTest.quickEnableConstrained();
		singleTest.quickEnableRegulating( singleTest.quickQueryLookahead() );
		singleTest.quickEnableAsyncDelivery();
		
		// try the advance
		try
		{
			singleTest.quickAdvanceAndWait( 10.0 );
		}
		finally
		{
			singleTest.quickResign();
			singleTest.quickDestroy( "singleTestFederation" );
		}
	}

	//////////////////////////////////////////////////////
	// TEST: (valid) testTimeAdvanceEnforcesLookahead() //
	//////////////////////////////////////////////////////
	/**
	 * This test ensures that time advancement takes proper account of the lookahead values of the
	 * regulating federate. Two regulating federates are used with one constrained federate.
	 */
	@Test
	public void testTarEnforcesLookahead()
	{
		// prepare the federates //
		defaultFederate.quickEnableConstrained();
		secondFederate.quickEnableRegulating( 5.0 );
		thirdFederate.quickEnableRegulating( 5.0 );
		
		// request an advance for the constrained federate to less than the current
		// LBTS. should get it right away
		defaultFederate.quickAdvanceRequest( 4.9 );
		defaultFederate.fedamb.waitForTimeAdvance( 4.9 );
		
		// request an advance past the current federation LBTS
		defaultFederate.quickAdvanceRequest( 10.0 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 10.0 );
		
		// advance the regulating federates such that the LBTS will still remain too low to
		// grant an advance to the constrained federate
		secondFederate.quickAdvanceRequest( 4.9 );
		secondFederate.fedamb.waitForTimeAdvance( 4.9 );
		thirdFederate.quickAdvanceRequest( 4.9 );
		thirdFederate.fedamb.waitForTimeAdvance( 4.9 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 10.0 );

		// advance the regulating federates a little bit further, but still not enough
		secondFederate.quickAdvanceRequest( 5.0 );
		secondFederate.fedamb.waitForTimeAdvance( 5.0 );
		thirdFederate.quickAdvanceRequest( 5.0 );
		thirdFederate.fedamb.waitForTimeAdvance( 5.0 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 10.0 );
		
		// advance the regulating federates so the constrained gets an advance
		secondFederate.quickAdvanceRequest( 5.1 );
		thirdFederate.quickAdvanceRequest( 5.1 );
		defaultFederate.fedamb.waitForTimeAdvance( 10.0 );
	}
	
	////////////////////////////////////////////////////////////////////
	// TEST: (valid) testTimeAdvanceRegulatingConstrainedAndNeither() //
	////////////////////////////////////////////////////////////////////
	/**
	 * This test consists of two federates. One regulating, one constrained and one neither.
	 * It is used to validate that the federate that is neither regulating nor constrained has no
	 * effect on the other two.
	 */
	@Test
	public void testTarWithRegulatingConstrainedAndNeither()
	{
		// prepare the federates //
		defaultFederate.quickEnableConstrained();
		secondFederate.quickEnableRegulating( 5.0 );
		
		// request an advance that the constrained federate won't get //
		defaultFederate.quickAdvanceRequest( 10.0 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 10.0 );
		
		// request an advance in the other federate, make sure we get it right away //
		thirdFederate.quickAdvanceRequest( 100.0 );
		thirdFederate.fedamb.waitForTimeAdvance( 100.0 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 10.0 );
		
		// request an advance in the regulating federate to free up the constrained one //
		secondFederate.quickAdvanceRequest( 50.0 );
		secondFederate.fedamb.waitForTimeAdvance( 50.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 10.0 );
	}
	
	//////////////////////////////////////////////////////////
	// TEST: (valid) testMultipleRegulatingAndConstrained() //
	//////////////////////////////////////////////////////////
	/**
	 * This method consists of two federates, each of which is BOTH regulating AND constrained.
	 * This is a very typical combination and is used to validate time advancement when all
	 * federates must wait for the others.
	 */
	@Test
	public void testTarWithMultipleRegulatingAndConstrained()
	{
		// prepare the federates //
		defaultFederate.quickEnableRegulating( 1.0 );
		defaultFederate.quickEnableConstrained();
		secondFederate.quickEnableRegulating( 1.0 );
		secondFederate.quickEnableConstrained();
		
		// request an advance, should get it if we advance by 1's //
		defaultFederate.quickAdvanceRequest( 1.0 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 1.0 ); // not yet
		secondFederate.quickAdvanceRequest( 1.0 );
		secondFederate.fedamb.waitForTimeAdvance( 1.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 1.0 );
		
		// request an advance that will put them past the LBTS, ensure that there is no grant //
		defaultFederate.quickAdvanceRequest( 2.1 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 2.1 );
		
		// now advance the other and make sure we're all good //
		secondFederate.quickAdvanceRequest( 2.1 );
		secondFederate.fedamb.waitForTimeAdvance( 2.1 );
		defaultFederate.fedamb.waitForTimeAdvance( 2.1 );
	}

	/////////////////////////////////////////////////////
	// TEST: (valid) testAdvanceWithRegulatingResign() //
	/////////////////////////////////////////////////////
	/**
	 * This method tests what happens when a regulating federate resigns mid-advance. If everything
	 * is working properly, any constrained federates that were currently waiting for that federate
	 * to advance before they could should receive their advance.
	 */
	@Test
	public void testTarWithRegulatingResign()
	{
		// prepare the federates //
		defaultFederate.quickEnableConstrained();
		secondFederate.quickEnableRegulating( 1.0 );
		
		// request an advance we'll have to wait for
		defaultFederate.quickAdvanceRequest( 5.0 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 5.0 );
		
		// have the regulating federate resign, this should free up the constrained one //
		secondFederate.quickResign();
		defaultFederate.fedamb.waitForTimeAdvance( 5.0 );
		
		// now that there are no regulating federates, make sure we can do whatever we want //
		defaultFederate.quickAdvanceRequest( 100.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 100.0 );
	}

	///////////////////////////////////////////////
	// TEST: (valid) testTimeResetsOnAllResign() //
	///////////////////////////////////////////////
	/**
	 * This test will ensure that when all federates resign from a federation, the internal
	 * time clock resets. All federates will be regulating and constrained, and will advance
	 * to time 10.0. Then, the federates will all resign, rejoin and try to do the same again.
	 */
	@Test
	public void testTarResetsWhenAllResign()
	{
		// enabled regulation and constrained
		defaultFederate.quickEnableRegulating( 10.0 );
		defaultFederate.quickEnableConstrained();
		secondFederate.quickEnableRegulating( 10.0 );
		secondFederate.quickEnableConstrained();
		thirdFederate.quickEnableRegulating( 10.0 );
		thirdFederate.quickEnableConstrained();
		
		// advance to 10
		defaultFederate.quickAdvanceRequest( 10.0 );
		secondFederate.quickAdvanceRequest( 10.0 );
		thirdFederate.quickAdvanceRequest( 10.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 10.0 );
		secondFederate.fedamb.waitForTimeAdvance( 10.0 );
		thirdFederate.fedamb.waitForTimeAdvance( 10.0 );
		
		// all resign
		defaultFederate.quickResign();
		secondFederate.quickResign();
		thirdFederate.quickResign();
		
		// join and do it all again
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
		thirdFederate.quickJoin();
		
		// enabled regulation and constrained
		defaultFederate.quickEnableRegulating( 10.0 );
		defaultFederate.quickEnableConstrained();
		secondFederate.quickEnableRegulating( 10.0 );
		secondFederate.quickEnableConstrained();
		thirdFederate.quickEnableRegulating( 10.0 );
		thirdFederate.quickEnableConstrained();
		
		// advance to 10
		defaultFederate.quickAdvanceRequest( 10.0 );
		secondFederate.quickAdvanceRequest( 10.0 );
		thirdFederate.quickAdvanceRequest( 10.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 10.0 );
		secondFederate.fedamb.waitForTimeAdvance( 10.0 );
		thirdFederate.fedamb.waitForTimeAdvance( 10.0 );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
