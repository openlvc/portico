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

import java.util.HashMap;

import hla.rti.EnableTimeConstrainedPending;
import hla.rti.EnableTimeRegulationPending;
import hla.rti.FederateNotExecutionMember;
import hla.rti.FederationTimeAlreadyPassed;
import hla.rti.InvalidFederationTime;
import hla.rti.LogicalTime;
import hla.rti.LogicalTimeInterval;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hla.rti.TimeAdvanceAlreadyInProgress;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;
import hlaunit.hla13.common.Test13Interaction;
import hlaunit.hla13.common.TimeoutException;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"TimeAdvanceRequestTest", "timeAdvanceRequest", "timeManagement"})
public class TimeAdvanceRequestTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate secondFederate;
	private Test13Federate thirdFederate;

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
		this.secondFederate = new Test13Federate( "secondFederate", this );
		this.thirdFederate = new Test13Federate( "thirdFederate", this );
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
		thirdFederate.quickJoin();
		
		defaultFederate.quickPublish( "InteractionRoot.X" );
		secondFederate.quickSubscribe( "InteractionRoot.X" );
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
	//        throws InvalidFederationTime,
	//               FederationTimeAlreadyPassed,
	//               TimeAdvanceAlreadyInProgress,
	//               EnableTimeRegulationPending,
	//               EnableTimeConstrainedPending,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	/////////////////////////////
	// TEST: (valid) testTar() //
	/////////////////////////////
	@Test
	public void testTar()
	{
		// request the time advance
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
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
	
	/////////////////////////////////////
	// TEST: testTarWithNegativeTime() //
	/////////////////////////////////////
	@Test
	public void testTarWithNegativeTime()
	{
		LogicalTime time = defaultFederate.createTime( -1.0 );
		try
		{
			defaultFederate.rtiamb.timeAdvanceRequest( time );
			expectedException( FederationTimeAlreadyPassed.class, InvalidFederationTime.class );
		}
		catch( FederationTimeAlreadyPassed ftap )
		{
			// success!
		}
		catch( InvalidFederationTime ift )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, FederationTimeAlreadyPassed.class, InvalidFederationTime.class );
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
		LogicalTime time = defaultFederate.createTime( 5.0 );
		try
		{
			defaultFederate.rtiamb.timeAdvanceRequest( time );
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

	/////////////////////////////////////////////////
	// TEST: testTarWhileAnotherTarIsOutstanding() //
	/////////////////////////////////////////////////
	@Test
	public void testTarWhileAnotherTarIsOutstanding()
	{
		// make the initial request
		LogicalTime time = defaultFederate.createTime( 5.0 );
		try
		{
			defaultFederate.rtiamb.timeAdvanceRequest( time );
		}
		catch( Exception e )
		{
			unexpectedException( "requesting tar", e );
		}
		
		// make a second request before we receive the advance grant
		LogicalTime time2 = defaultFederate.createTime( 10.0 );
		try
		{
			defaultFederate.rtiamb.timeAdvanceRequest( time2 );
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

	/////////////////////////////////////////////
	// TEST: testTarWhileRegulationIsPending() //
	/////////////////////////////////////////////
	@Test
	public void testTarWhileRegulationIsPending()
	{
		// start the enable regulation process
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			LogicalTimeInterval lookahead = defaultFederate.createInterval( 1.0 );
			defaultFederate.rtiamb.enableTimeRegulation( time, lookahead );
		}
		catch( Exception e )
		{
			unexpectedException( "enabling time regulation", e );
		}
		
		// make the request before time regulation is enabled
		try
		{
			LogicalTime time = defaultFederate.createTime( 20.0 );
			defaultFederate.rtiamb.timeAdvanceRequest( time );
			expectedException( EnableTimeRegulationPending.class );
		}
		catch( EnableTimeRegulationPending etrp )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, EnableTimeRegulationPending.class );
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
			LogicalTime time = defaultFederate.createTime( 20.0 );
			defaultFederate.rtiamb.timeAdvanceRequest( time );
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
			LogicalTime time = defaultFederate.createTime( 20.0 );
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
	
	///////////////////////////////////////
	// TEST: testTarWhenSaveInProgress() //
	///////////////////////////////////////
	@Test
	public void testTarWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			LogicalTime time = defaultFederate.createTime( 20.0 );
			defaultFederate.rtiamb.timeAdvanceRequest( time );
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

	///////////////////////////////////////
	// TEST: testTarWhenSaveInProgress() //
	///////////////////////////////////////
	@Test
	public void testTarWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			LogicalTime time = defaultFederate.createTime( 20.0 );
			defaultFederate.rtiamb.timeAdvanceRequest( time );
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
		Test13Federate singleTest = new Test13Federate( "singleTest", this );
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

	///////////////////////////////////////////////////////////////////////
	// TEST: (valid) testAllTSOMessagesReceivedBeforeAdvanceToSameTime() //
	///////////////////////////////////////////////////////////////////////
	/**
	 * This test ensures that all TSO messages queued for delivery at a certain time are sent
	 * as callbacks to a federate BEFORE a time advance granted callback is delivered to the
	 * federate for that time. 
	 */
	@Test
	public void testAllTSOMessagesReceivedBeforeAdvanceToSameTime()
	{
		// enabled regulation and constrained
		defaultFederate.quickEnableRegulating( 1.0 );
		defaultFederate.quickEnableConstrained();
		secondFederate.quickEnableRegulating( 1.0 );
		secondFederate.quickEnableConstrained();

		// second: request advance to 1.0
		secondFederate.quickAdvanceRequest( 1.0 );
		
		// default: send an interaction at time 1.0 and request advance to 1.0
		defaultFederate.quickSend( "InteractionRoot.X",
		                           new HashMap<String,byte[]>(),
		                           "test".getBytes(),
		                           1.0 );
		defaultFederate.quickAdvanceAndWait( 1.0 );
		
		// second: make sure we get the interaction FIRST and the grant after it
		double giveupTime = secondFederate.fedamb.getTimeout();
		while( secondFederate.fedamb.getTsoInteractions().size() == 0 )
		{
			secondFederate.quickTickSingle( 1.0 );
			if( giveupTime < System.currentTimeMillis() )
				throw new TimeoutException( "Timeout waiting for TSO interaction" );
		}
		
		// make sure we haven't got the tag
		Assert.assertEquals(secondFederate.fedamb.logicalTime, 0.0, "got TAR before we should have");
		secondFederate.fedamb.waitForTimeAdvance( 1.0 );
	}

	/////////////////////////////////////////////////////////////////////
	// TEST: (valid) testTSOMessagesReceivedInAbsoluteTimestampOrder() //
	/////////////////////////////////////////////////////////////////////
	/**
	 * The purpose of this test is to ensure that TSO messages are delivered in absolute ordering.
	 * That is to say, to make sure that a message with a timestamp of 10.0 is always delivered
	 * before one with 11.0 (for example). While this might seem obvious, this test will try to
	 * break this like so:
	 * <ol>
	 *   <li>Publish interaction in default and secondFederate, subcribe in third.</li>
	 *   <li>Enable regulating in the defaultFedrate and secondFederate</li>
	 *   <li>Enable constrained in the third federate</li>
	 *   <li>Have the third federate request an advance to 100.0</li>
	 *   <li>Have the defaultFederate send an interaction with timestamp 50.0</li>
	 *   <li>Ensure the interaction is not received in the third federate (we can't guarantee
	 *       that all regulating federates won't deliver messages earlier yet).</li>
	 *   <li>Wait for a timeout listening for the interaction (ensuring all contained messages
	 *       that can be released have been released)</li>
	 *   <li>Have the defaultFederate send an interaction with timestamp 40.0</li>
	 *   <li>Again, ensure the message isn't received yet, as all regulating federates haven't
	 *       advanced far enough to ensure no earlier messages will be sent yet</li>
	 *   <li>Advance the regulating federate to 100.0</li>
	 *   <li>Wait for an interaction in the third federate, assert that it's timestamp is 40.0</li>
	 *   <li>Wait for an interaction in the third federate, assert that it's timestamp is 50.0</li>
	 * </ol>
	 * 
	 * If messages are delivered early, the test will fail. If the messages are delivered out of
	 * order, the asserts will see this and the test will fail. 
	 */
	@Test
	public void testTSOMessagesReceivedInAbsoluteTimestampOrder()
	{
		//defaultFederate.quickPublish( "InteractionRoot.X" ); -- done in beforeMethod
		secondFederate.quickPublish( "InteractionRoot.X" );
		thirdFederate.quickSubscribe( "InteractionRoot.X" );
		defaultFederate.quickEnableRegulating( 1.0 );
		secondFederate.quickEnableRegulating( 1.0 );
		thirdFederate.quickEnableAsyncDelivery();
		thirdFederate.quickEnableConstrained();
		
		// request big advance for third federate
		thirdFederate.quickAdvanceRequest( 100.0 );
		
		// send message @ 50 from defaultFederate
		defaultFederate.quickSend( "InteractionRoot.X", 50.0 );
		thirdFederate.fedamb.waitForTSOInteractionTimeout( "InteractionRoot.X" );

		// now send an interaction with a timestamp less than the previous one
		secondFederate.quickSend( "InteractionRoot.X", 40.0 );
		thirdFederate.fedamb.waitForTSOInteractionTimeout( "InteractionRoot.X" );
		
		// advance the default and second federates such that the message can now be
		// released to the third federate
		defaultFederate.quickAdvanceAndWait( 100.0 );
		secondFederate.quickAdvanceAndWait( 100.0 );
		quickSleep();
		Test13Interaction interaction = thirdFederate.fedamb.waitForTSOInteraction( "InteractionRoot.X" );
		Assert.assertEquals( interaction.getTimestamp(), 40.0 );
		interaction = thirdFederate.fedamb.waitForTSOInteraction( "InteractionRoot.X" );
		Assert.assertEquals( interaction.getTimestamp(), 50.0 );
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testTSOMessagesNotDeliveredToConstrainedFederateBeforeItAdvancesFarEnough() //
	///////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The time-advancements of regulating federates directly affect the federation-LBTS, and thus,
	 * affect when TSO messages are released to constrained federates. This test ensures that TSO
	 * messages are *NOT* delivered to a constrained, non-regulating federate before it has
	 * requested a time advancement to a particular point equal to or past the timestamp of the
	 * message.
	 * <p/>
	 * This may sound obvious at first, but given that the federate is non-regulating, it has no
	 * control over the federation-LBTS. So the regulating federates could advance from 0.0-10.0,
	 * thus making the federation LBTS something over 10.0 (taking lookahead into account) while
	 * the constrained, non-regulating federate is still on 0.0. At this point, it is safe to
	 * deliver the TSO messages to the federate, because the regulating federates (the only ones
	 * that can produce TSO messages) have moved on, however, we naturally must also consider the
	 * status of the local federate, which is what this method tests.
	 */
	@Test
	public void testTSOMessagesNotDeliveredToConstrainedFederateBeforeItAdvancesFarEnough()
	{
		//defaultFederate.quickPublish( "InteractionRoot.X" ); -- done in beforeMethod
		secondFederate.quickSubscribe( "InteractionRoot.X" );
		defaultFederate.quickEnableRegulating( 1.0 );
		secondFederate.quickEnableAsyncDelivery();
		secondFederate.quickEnableConstrained();

		// send an interaction at some point in the future and make sure it isn't received yet
		defaultFederate.quickSend( "InteractionRoot.X", 10.0 );
		secondFederate.fedamb.waitForTSOInteractionTimeout( "InteractionRoot.X" );
		
		// advance time in the regulating federate so the federation-LBTS is higher than the time
		// the previous TSO message was sent, thus making it safe to deliver
		defaultFederate.quickAdvanceAndWait( 100.0 );

		// make sure the interaction still isn't received in the constrained federate because
		// it hasn't advances its own local time far enough yet
		secondFederate.fedamb.waitForTSOInteractionTimeout( "InteractionRoot.X" );
		
		// now advance time in the constrained federate and make sure the interaction comes through
		secondFederate.quickAdvanceRequest( 11.0 );
		secondFederate.fedamb.waitForTSOInteraction( "InteractionRoot.X" );
		secondFederate.fedamb.waitForTimeAdvance( 11.0 );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
