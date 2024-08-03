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
import hlaunit.ieee1516.common.TestInteraction;
import hlaunit.ieee1516.common.TypeFactory;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"FlushQueueRequestTest", "optimistic", "timeManagement"})
public class FlushQueueRequestTest extends Abstract1516Test
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

		// publication and subscription
		defaultFederate.quickPublish( "InteractionRoot.X" );
		secondFederate.quickSubscribe( "InteractionRoot.X" );
		
		// time management
		defaultFederate.quickEnableAsyncDelivery();
		secondFederate.quickEnableAsyncDelivery();
		defaultFederate.quickEnableRegulating( 5.0 );
		secondFederate.quickEnableConstrained();
		
		// advance time a little
		defaultFederate.quickAdvanceAndWait( 10.0 );
		secondFederate.quickAdvanceAndWait( 10.0 );
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

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void flushQueueRequest( LogicalTime theTime )
	//     throws InvalidFederationTime,
	//            FederationTimeAlreadyPassed,
	//            TimeAdvanceAlreadyInProgress,
	//            EnableTimeRegulationPending,
	//            EnableTimeConstrainedPending,
	//            FederateNotExecutionMember,
	//            SaveInProgress,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted

	/**
	 * Flush queue request should release all messages (RO and TSO) currently held in the message
	 * queue up until the specified time. It gives no regard to the LBTS, so messages can be
	 * delivered out-of-order and on completion a time advance can be granted to a time where the
	 * RTI can't guarantee that events in the past aren't possible.
	 * <p/>
	 * This test will send events with timestamps beyond what would normally be delivered from
	 * the defaultFederate to the secondFederate and will then flush the queue of the second
	 * federate and expect to see those messages come in.
	 */
	@Test
	public void testFlushQueueRequst()
	{
		// send some interactions
		defaultFederate.quickSend( "InteractionRoot.X", 15.0 );
		defaultFederate.quickSend( "InteractionRoot.X", 100.0 );
		
		// issue the request in the second federate
		// normally this advance would be constrained by the regulating defaultFederate, but
		// with a flush queue request, the current RO/TSO messages will be delivered regardless
		// of the federation-wide LBTS and an advance will be granted to the LESSER or the
		// requested time -OR- the timestamp of the highest TSO message
		secondFederate.quickFlushQueueRequest( 200.0 );
		TestInteraction interaction = null;
		interaction = secondFederate.fedamb.waitForTSOInteraction( "InteractionRoot.X" );
		Assert.assertEquals( interaction.getTimestamp(), 15.0 );
		interaction = secondFederate.fedamb.waitForTSOInteraction( "InteractionRoot.X" );
		Assert.assertEquals( interaction.getTimestamp(), 100.0 );

		secondFederate.fedamb.waitForTimeAdvance( 100.0 );
		
		// send an event in the past by the regulating federate
		defaultFederate.quickSend( "InteractionRoot.X", 50.0 );
		secondFederate.fedamb.waitForTSOInteractionTimeout( "InteractionRoot.X" );
	}

	/**
	 * The flushQueueRequest call instructs the LRC to drain all current messages (RO and TSO)
	 * up to a specific time, even if this means violating ordering. It then issues a time advance
	 * for the federate to either the cutoff time or the timestamp of the next TSO even, whichever
	 * is smaller. This is done without any guarantee that messages with a lower timestamp might
	 * show up later on (hence the reason this is referred to as optimistic time management).
	 * <p/>
	 * As such, if this service is working properly, a constrained federate should be able to
	 * advance itself beyond the federation LBTS. This method asserts that this is infact true.
	 */
	@Test
	public void testFlushQueueRequestAdvancesConstrainedFederate()
	{
		// advance the constrained federate
		secondFederate.quickFlushQueueRequest( 100.0 );
		secondFederate.fedamb.waitForTimeAdvance( 100.0 );
		
		// try and advance it using normal mechanics now
		secondFederate.quickAdvanceRequest( 110.0 );
		secondFederate.fedamb.waitForTimeAdvanceTimeout( 110.0 );
		
		// advance the regulating federate
		defaultFederate.quickAdvanceAndWait( 150.0 );
		secondFederate.fedamb.waitForTimeAdvance( 110.0 );
	}
	
	//////////////////////////////////////////////////
	// TEST: testFlushQueueRequestWithInvalidTime() //
	//////////////////////////////////////////////////
	@Test
	public void testFlushQueueRequestWithInvalidTime()
	{
		try
		{
			secondFederate.rtiamb.flushQueueRequest( null );
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

	/////////////////////////////////////////////////
	// TEST: testFlushQueueRequestWithTimeInPast() //
	/////////////////////////////////////////////////
	@Test
	public void testFlushQueueRequestWithTimeInPast()
	{
		try
		{
			secondFederate.rtiamb.flushQueueRequest( TypeFactory.createTime(1.0) );
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

	////////////////////////////////////////////////////////
	// TEST: testFlushQueueRequestWhenAdvanceInProgress() //
	////////////////////////////////////////////////////////
	@Test
	public void testFlushQueueRequestWhenAdvanceInProgress()
	{
		// make the initial request
		try
		{
			secondFederate.rtiamb.timeAdvanceRequest( TypeFactory.createTime(100.0) );
		}
		catch( Exception e )
		{
			unexpectedException( "requesting tar", e );
		}
		
		// make a second request before we receive the advance grant
		try
		{
			secondFederate.rtiamb.timeAdvanceRequest( TypeFactory.createTime(120.0) );
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

	//////////////////////////////////////////////////////////////
	// TEST: testFlushQueueRequestWhenEnableRegulationPending() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testFlushQueueRequestWhenEnableRegulationPending()
	{
		// start the enable regulation process
		try
		{
			LogicalTimeInterval lookahead = TypeFactory.createInterval( 1.0 );
			secondFederate.rtiamb.enableTimeRegulation( lookahead );
		}
		catch( Exception e )
		{
			unexpectedException( "enabling time regulation", e );
		}
		
		// make the request before time regulation is enabled
		try
		{
			LogicalTime time = TypeFactory.createTime( 120.0 );
			secondFederate.rtiamb.timeAdvanceRequest( time );
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

	///////////////////////////////////////////////////////////////
	// TEST: testFlushQueueRequestWhenEnableConstrainedPending() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testFlushQueueRequestWhenEnableConstrainedPending()
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
			LogicalTime time = TypeFactory.createTime( 100.0 );
			defaultFederate.rtiamb.flushQueueRequest( time );
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

	////////////////////////////////////////////////
	// TEST: testFlushQueueRequestWhenNotJoined() //
	////////////////////////////////////////////////
	@Test
	public void testFlushQueueRequestWhenNotJoined()
	{
		secondFederate.quickResign();
		try
		{
			secondFederate.rtiamb.flushQueueRequest( TypeFactory.createTime(100.0) );
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

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
