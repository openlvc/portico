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
package hlaunit.ieee1516e.time;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeInterval;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.InTimeAdvancingState;
import hla.rti1516e.exceptions.InvalidLogicalTime;
import hla.rti1516e.exceptions.LogicalTimeAlreadyPassed;
import hla.rti1516e.exceptions.RequestForTimeConstrainedPending;
import hla.rti1516e.exceptions.RequestForTimeRegulationPending;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;
import hlaunit.ieee1516e.common.TypeFactory;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"TimeNextMessageRequestTest",
                                   "nextMessageRequest",
                                   "nextMessageRequest",
                                   "timeManagement"})
public class TimeNextMessageRequestTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate constrainedFederate;
	private TestFederate regulatingFederate;

	private LogicalTime oneHundred;
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
		this.constrainedFederate = new TestFederate( "constrainedFederate", this );
		this.regulatingFederate = new TestFederate( "regulatingFederate", this );
		
		this.oneHundred = TypeFactory.createTime( 100.0 );
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		// create the federation and join the federates
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		defaultFederate.quickSubscribe( "InteractionRoot.X" );
		
		// set up the constrained federate
		constrainedFederate.quickJoin();
		constrainedFederate.quickEnableConstrained();
		constrainedFederate.quickSubscribe( "InteractionRoot.X" );
		
		// set up the regulating federaet
		regulatingFederate.quickJoin();
		regulatingFederate.quickEnableRegulating( 5.0 );
		regulatingFederate.quickPublish( "InteractionRoot.X" );
		
		// place some messages in the pipe so that the default federate
		// has some events to advance to
		regulatingFederate.quickSend( "InteractionRoot.X", null, null, 5.0 );
		regulatingFederate.quickAdvanceAndWait( 5.0 );
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		regulatingFederate.quickResign();
		constrainedFederate.quickResign();
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

	// public void nextMessageRequest( LogicalTime theTime )
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
	// TEST: (valid) testNmr() //
	/////////////////////////////
	@Test(enabled=false)
	public void testNmr()
	{
		// issue the ner
		try
		{
			constrainedFederate.rtiamb.nextMessageRequest( oneHundred );
		}
		catch( Exception e )
		{
			unexpectedException( "issuing NMR", e );
		}
		
		// wait to see if we get the queued TSO message
		constrainedFederate.fedamb.waitForTSOInteraction( "InteractionRoot.X" );
		// make sure we are now advanced to the appropriate time
		constrainedFederate.fedamb.waitForTimeAdvance( 5.0 );
	}

	/////////////////////////////////
	// TEST: testNmrWithNullTime() //
	/////////////////////////////////
	@Test
	public void testNmrWithNullTime()
	{
		try
		{
			constrainedFederate.rtiamb.nextMessageRequest( null );
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
	// TEST: testNmrWithNegativeTime() //
	/////////////////////////////////////
	@Test
	public void testNmrWithNegativeTime()
	{
		try
		{
			LogicalTime time = TypeFactory.createTime( -1 );
			constrainedFederate.rtiamb.nextMessageRequest( time );
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

	///////////////////////////////////////
	// TEST: testNmrWithWithTimeInPast() //
	///////////////////////////////////////
	@Test
	public void testNmrWithWithTimeInPast()
	{
		// advance the federate first -- uses the default federate so we don't have
		//                               to deal with the events already in the pipe
		defaultFederate.quickAdvanceAndWait( 10.0 );
		
		try
		{
			LogicalTime time = TypeFactory.createTime( 5.0 );
			defaultFederate.rtiamb.nextMessageRequest( time );
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

	///////////////////////////////////////////////
	// TEST: testNmrWithOutstandingTimeAdvance() //
	///////////////////////////////////////////////
	@Test
	public void testNmrWithOutstandingTimeAdvance()
	{
		// get an NMR going
		try
		{
			LogicalTime time = TypeFactory.createTime( 10.0 );
			defaultFederate.rtiamb.nextMessageRequest( time );
		}
		catch( Exception e )
		{
			unexpectedException( "issuing NMR", e );
		}
		
		// issue another NMR, which should give us an exception because an
		// advance is already in progress
		try
		{
			LogicalTime time = TypeFactory.createTime( 15.0 );
			defaultFederate.rtiamb.nextMessageRequest( time );
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
	// TEST: testNmrWhileRegulationIsPending() //
	/////////////////////////////////////////////
	@Test
	public void testNmrWhileRegulationIsPending()
	{
		// set regulation pending
		try
		{
			LogicalTimeInterval interval = TypeFactory.createInterval( 10.0 );
			defaultFederate.rtiamb.enableTimeRegulation( interval );
		}
		catch( Exception e )
		{
			unexpectedException( "enabling time regulation", e );
		}
		
		// issue the ner before we get the regulation enabled callback
		try
		{
			LogicalTime time = TypeFactory.createTime( 25.0 );
			defaultFederate.rtiamb.nextMessageRequest( time );
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

	//////////////////////////////////////////////
	// TEST: testNmrWhileConstrainedIsPending() //
	//////////////////////////////////////////////
	@Test
	public void testNmrWhileConstrainedIsPending()
	{
		// set constrained pending
		try
		{
			defaultFederate.rtiamb.enableTimeConstrained();
		}
		catch( Exception e )
		{
			unexpectedException( "enabling time constrained", e );
		}
		
		// issue the ner before we get the regulation enabled callback
		try
		{
			LogicalTime time = TypeFactory.createTime( 25.0 );
			defaultFederate.rtiamb.nextMessageRequest( time );
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
	// TEST: testNmrWhenNotJoined() //
	//////////////////////////////////
	@Test
	public void testNmrWhenNotJoined()
	{
		// resign from federation so we can run the test
		defaultFederate.quickResign();
		
		// issue the ner before we get the regulation enabled callback
		try
		{
			LogicalTime time = TypeFactory.createTime( 25.0 );
			defaultFederate.rtiamb.nextMessageRequest( time );
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
	// The methods above check that the NMR method takes the   //
	// appropriate actions to check the parameters that it is  //
	// given. These methods confirm that the NMR calls trigger //
	// the proper behaviour. They set up particular scenarios  //
	// between coordinated federates to validate this. These   //
	// methods don't use the RTIambassador directly, so as to  //
	// avoid a bunch of exception handling and keep the code   //
	// as clear and clean as possible.                         //
	/////////////////////////////////////////////////////////////

	@Test
	public void testNmrWhenEventTimestampIsGreaterThanRequested()
	{
		// event is waiting at time 5.0
		constrainedFederate.quickNextEventRequest( 3.0 );
		constrainedFederate.fedamb.waitForTSOInteractionTimeout( "InteractionRoot.X" );
		constrainedFederate.fedamb.waitForTimeAdvance( 3.0 );
	}

	@Test
	public void testNmrWhenEventTimestampIsLessThanRequested()
	{
		// event is waiting at time 5.0
		constrainedFederate.quickNextEventRequest( 10.0 );
		constrainedFederate.fedamb.waitForTSOInteraction( "InteractionRoot.X" );
		constrainedFederate.fedamb.waitForTimeAdvance( 5.0 );
		constrainedFederate.fedamb.waitForTimeAdvanceTimeout( 10.0 );
	}
	
	@Test
	public void testNmrWhenNoTsoEventsAreQueued()
	{
		// advance the regulating federate so that it won't hold us up
		regulatingFederate.quickAdvanceAndWait( 10.0 );
		
		// make the default federate constrained so we don't have to worry about
		// flushing the TSO queue due to the message placed in there during setup
		defaultFederate.quickEnableConstrained();
		defaultFederate.quickNextEventRequest( 6.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 6.0 );
	}

	@Test
	public void testNmrWhileNotRegulatingOrConstrained()
	{
		defaultFederate.quickNextEventRequest( 10.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 10.0 );
		
		defaultFederate.quickNextEventRequest( 50.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 50.0 );
	}

	@Test
	public void testNmrWhileConstrainedOnly()
	{
		// prepare the federates //
		defaultFederate.quickEnableConstrained();
		regulatingFederate.quickDisableRegulating();
		regulatingFederate.quickEnableConstrained();
		
		// advance the federates //
		
		// the constrained federate will have a message waiting from the set up, thus
		// we call this twice and should only get an advance to 5.0 (the TSO of the
		// waiting message) the first time
		constrainedFederate.quickNextEventRequest( 10.0 );
		constrainedFederate.fedamb.waitForTimeAdvance( 5.0 );
		//System.out.println( "HERE: " + defaultFederate.fedamb.logicalTime );
		constrainedFederate.quickNextEventRequest( 10.0 );
		constrainedFederate.fedamb.waitForTimeAdvance( 10.0 );
		
		// these federates should be able to do anything they want
		defaultFederate.quickNextEventRequest( 30.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 30.0 );
		// regulating federate is no longer regulating
		regulatingFederate.quickNextEventRequest( 4500.7 );
		regulatingFederate.fedamb.waitForTimeAdvance( 4500.7 );
	}

	@Test
	public void testNmrWhileRegulatingOnly()
	{
		// enable regulation on all the federates //
		defaultFederate.quickEnableRegulating( 5.0 );
		constrainedFederate.quickDisableConstrained();
		constrainedFederate.quickEnableRegulating( 5.0 );
		
		// attempt to advance them in time //
		
		// these two federates should have had messages waiting for them in the TSO
		// queue previously, but they should have been cleared out when they disabled
		// time constrained. Those messages had a TSO of 5, so when we request a NMR
		// with 10 as the default time, we should get to 10 because the messages should
		// have been moved to the RO queue and no longer be considered during the request
		defaultFederate.quickNextEventRequest( 10.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 10.0 );
		constrainedFederate.quickNextEventRequest( 10.0 );
		constrainedFederate.fedamb.waitForTimeAdvance( 10.0 );
		
		// this federate has already advanced to 5, so we'll need to advance to something
		// higher than that
		regulatingFederate.quickNextEventRequest( 422455663.4 );
		regulatingFederate.fedamb.waitForTimeAdvance( 422455663.4 );
	}

	@Test(enabled=false)
	public void testNmrWhileConstrainedAndRegulating()
	{
		// resign all the federates and reset their status (it's just easier this way)
		// resign
		regulatingFederate.quickResign();
		constrainedFederate.quickResign();
		defaultFederate.quickResign();
		// re-join
		regulatingFederate.quickJoin();
		defaultFederate.quickJoin();
		constrainedFederate.quickJoin();
		// enable time status
		regulatingFederate.quickEnableRegulating( 5.0 );
		defaultFederate.quickEnableRegulating( 5.0 );
		constrainedFederate.quickEnableConstrained();

		// request an advance for a regulating federate, should get it right away //
		regulatingFederate.quickNextEventRequest( 10.0 );
		regulatingFederate.fedamb.waitForTimeAdvance( 10.0 );
		
		// request an advance for a constrained federate, should have to wait //
		constrainedFederate.quickNextEventRequest( 10.0 );
		constrainedFederate.fedamb.waitForTimeAdvanceTimeout( 10.0 );

		// request an advance of the second federate again, should get it no problems (again) //
		regulatingFederate.quickNextEventRequest( 20.0 );
		regulatingFederate.fedamb.waitForTimeAdvance( 20.0 );
		
		// request an advance of the third federate to 5.0, thus giving it an LBTS of 10.0 //
		// this is the same as the requested time for the default federate (constrained),  //
		// however, as we can't be sure that the third federate won't generate TSOs with a //
		// timestamp that is less than OR EQUAL TO the time requested by the constrained   //
		// federate, we can't grant its advance yet.                                       //
		defaultFederate.quickNextEventRequest( 5.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 5.0 );
		constrainedFederate.fedamb.waitForTimeAdvanceTimeout( 10.0 );
		
		// request an advance of the third federate way ahead, should get it and should also
		// free up the constrained one
		defaultFederate.quickNextEventRequest( 20.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 20.0 );
		constrainedFederate.fedamb.waitForTimeAdvance( 10.0 );
		
		// now request an advancement for the constrained federate (before any of
		// the regulating federates have requested an advance
		constrainedFederate.quickNextEventRequest( 40.0 );
		constrainedFederate.fedamb.waitForTimeAdvanceTimeout( 40.0 );
		
		// advance the third federate and make sure the constrained doesn't get it //
		defaultFederate.quickNextEventRequest( 35.00000001 );
		defaultFederate.fedamb.waitForTimeAdvance( 35.00000001 );
		constrainedFederate.fedamb.waitForTimeAdvanceTimeout( 40.0 );
		
		// advance the second federate to 40.0, which means that the default federate //
		// still shouldn't get the advance yet //
		regulatingFederate.quickNextEventRequest( 35.0 );
		regulatingFederate.fedamb.waitForTimeAdvance( 35.0 );
		constrainedFederate.fedamb.waitForTimeAdvanceTimeout( 40.0 );
		
		// push the second federate way ahead. with the third just over 40, the constrained //
		// federate should now also be able to get the advance that it has been waiting for //
		regulatingFederate.quickNextEventRequest( 100.0 );
		constrainedFederate.fedamb.waitForTimeAdvance( 40.0 );
		regulatingFederate.fedamb.waitForTimeAdvance( 100.0 );
	}

	@Test
	public void testNmrEnforcesLookahead()
	{
		// resign all the federates and reset their status (it's just easier this way)
		// resign
		regulatingFederate.quickResign();
		constrainedFederate.quickResign();
		defaultFederate.quickResign();
		// re-join
		regulatingFederate.quickJoin();
		defaultFederate.quickJoin();
		constrainedFederate.quickJoin();
		// enable time status
		regulatingFederate.quickEnableRegulating( 5.0 );
		defaultFederate.quickEnableRegulating( 5.0 );
		constrainedFederate.quickEnableConstrained();
		
		// request an advance for the constrained federate to less than the current
		// LBTS. should get it right away
		constrainedFederate.quickNextEventRequest( 4.9 );
		constrainedFederate.fedamb.waitForTimeAdvance( 4.9 );
		
		// request an advance past the current federation LBTS
		constrainedFederate.quickNextEventRequest( 10.0 );
		constrainedFederate.fedamb.waitForTimeAdvanceTimeout( 10.0 );
		
		// advance the regulating federates such that the LBTS will still remain too low to
		// grant an advance to the constrained federate
		regulatingFederate.quickNextEventRequest( 4.9 );
		regulatingFederate.fedamb.waitForTimeAdvance( 4.9 );
		defaultFederate.quickNextEventRequest( 4.9 );
		defaultFederate.fedamb.waitForTimeAdvance( 4.9 );
		constrainedFederate.fedamb.waitForTimeAdvanceTimeout( 10.0 );

		// advance the regulating federates a little bit further, but still not enough
		regulatingFederate.quickNextEventRequest( 5.0 );
		regulatingFederate.fedamb.waitForTimeAdvance( 5.0 );
		defaultFederate.quickNextEventRequest( 5.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 5.0 );
		constrainedFederate.fedamb.waitForTimeAdvanceTimeout( 10.0 );
		
		// advance the regulating federates so the constrained gets an advance
		regulatingFederate.quickNextEventRequest( 5.1 );
		defaultFederate.quickNextEventRequest( 5.1 );
		constrainedFederate.fedamb.waitForTimeAdvance( 10.0 );
	}
	
	@Test
	public void testNmrWithRegulatingResign()
	{
		// resign all the federates and reset their status (it's just easier this way)
		// resign
		regulatingFederate.quickResign();
		constrainedFederate.quickResign();
		defaultFederate.quickResign(); // not needed
		// re-join
		regulatingFederate.quickJoin();
		constrainedFederate.quickJoin();
		// enable time status
		regulatingFederate.quickEnableRegulating( 1.0 );
		constrainedFederate.quickEnableConstrained();
		
		// request an advance we'll have to wait for
		constrainedFederate.quickNextEventRequest( 5.0 );
		constrainedFederate.fedamb.waitForTimeAdvanceTimeout( 5.0 );
		
		// have the regulating federate resign, this should free up the constrained one //
		regulatingFederate.quickResign();
		constrainedFederate.fedamb.waitForTimeAdvance( 5.0 );
		
		// now that there are no regulating federates, make sure we can do whatever we want //
		constrainedFederate.quickNextEventRequest( 100.0 );
		constrainedFederate.fedamb.waitForTimeAdvance( 100.0 );
	}

	@Test(enabled=false)
	public void testNmrResetsWhenAllResign()
	{
		// advance to 5
		defaultFederate.quickNextEventRequest( 5.0 );
		constrainedFederate.quickNextEventRequest( 5.0 );
		regulatingFederate.quickNextEventRequest( 10.0 ); // it has already requested to 5 in setup
		defaultFederate.fedamb.waitForTimeAdvance( 5.0 );
		constrainedFederate.fedamb.waitForTimeAdvance( 5.0 );
		regulatingFederate.fedamb.waitForTimeAdvance( 10.0 );
		
		// all resign
		defaultFederate.quickResign();
		constrainedFederate.quickResign();
		regulatingFederate.quickResign();
		
		// join and do it all again
		defaultFederate.quickJoin();
		constrainedFederate.quickJoin();
		regulatingFederate.quickJoin();
		
		// enabled regulation and constrained
		defaultFederate.quickEnableRegulating( 10.0 );
		defaultFederate.quickEnableConstrained();
		constrainedFederate.quickEnableRegulating( 10.0 );
		constrainedFederate.quickEnableConstrained();
		regulatingFederate.quickEnableRegulating( 10.0 );
		regulatingFederate.quickEnableConstrained();
		
		// advance to 10
		defaultFederate.quickNextEventRequest( 10.0 );
		constrainedFederate.quickNextEventRequest( 10.0 );
		regulatingFederate.quickNextEventRequest( 10.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 10.0 );
		constrainedFederate.fedamb.waitForTimeAdvance( 10.0 );
		regulatingFederate.fedamb.waitForTimeAdvance( 10.0 );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
