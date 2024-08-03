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

import java.util.HashMap;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.LogicalTimeInterval;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.InvalidLogicalTime;
import hla.rti1516e.exceptions.InvalidLookahead;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;
import hlaunit.ieee1516e.common.TypeFactory;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This test validates the enforcement of lookahead values and tests their runtime modification.
 * <p/>
 * This test covers the calls <code>modifyLookahead</code> and <code>queryLookahead</code>, while
 * also using the various helper methods of {@link TestFederate} to validate that lookahead
 * values are enforces for things like interactions and attribute updates. 
 */
@Test(singleThreaded=true, groups={"LookaheadTest", "lookahead", "timeManagement"})
public class LookaheadTest extends Abstract1516eTest
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
		defaultFederate.quickEnableRegulating( 5.0 );
		
		secondFederate.quickJoin();
		secondFederate.quickEnableAsyncDelivery();
		secondFederate.quickEnableConstrained();
		secondFederate.quickSubscribe( "InteractionRoot.X" );
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
	///////////////////////////////////// Helper Methods /////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	private double validateRegulating( double fromTime )
	{
		// send a TSO interaction from the regulating federate to the constrained federate
		
		// make sure we are publishing the interaction
		defaultFederate.quickPublish( "InteractionRoot.X" );
		
		// send the interaction with a timestamp of the given time + the federates lookahead
		double sendTimestamp = fromTime + defaultFederate.quickQueryLookahead();
		defaultFederate.quickSend( "InteractionRoot.X", null, "".getBytes(), sendTimestamp );
		
		// wait for the interaction to come in TSO
		defaultFederate.quickAdvanceAndWait( sendTimestamp );
		secondFederate.quickAdvanceRequest( sendTimestamp );
		secondFederate.fedamb.waitForTSOInteraction( "InteractionRoot.X" );
		secondFederate.fedamb.waitForTimeAdvance( sendTimestamp );
		
		// return the new time that the default federate should have advanced to
		return sendTimestamp;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Modify Lookahead Methods ////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void modifyLookahead( LogicalTimeInterval theLookahead )
	//        throws TimeRegulationIsNotEnabled,
	//               InvalidLookahead,
	//               InTimeAdvancingState,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

	/////////////////////////////////////////////////////
	// TEST: (valid) testModifyLookaheadByIncreasing() //
	/////////////////////////////////////////////////////
	@Test
	public void testModifyLookaheadByIncreasing()
	{
		// enable regulating with a lookahead of 5
		//defaultFederate.quickEnableRegulating( 5.0 ); -- done in beforeMethod
		
		// validate the regulation
		double timeOne = validateRegulating( 0.0 );
		Assert.assertEquals( timeOne, 5.0 );
		
		// modify the lookahead
		try
		{
			LogicalTimeInterval interval = TypeFactory.createInterval( 10.0 );
			defaultFederate.rtiamb.modifyLookahead( interval );
		}
		catch( Exception e )
		{
			unexpectedException( "modifying lookahead", e );
		}
		
		// validate the updated time
		double timeTwo = validateRegulating( timeOne );
		Assert.assertEquals( timeTwo, 15.0 );
	}

	/////////////////////////////////////////////////////
	// TEST: (valid) testModifyLookaheadByDecreasing() //
	/////////////////////////////////////////////////////
	@Test
	public void testModifyLookaheadByDecreasing()
	{
		log( "Decresing the lookahead if not currently supported" );
	}

	//////////////////////////////////////////////////////
	// TEST: testModifyLookaheadWithNegativeLookahead() //
	//////////////////////////////////////////////////////
	@Test
	public void testModifyLookaheadWithNegativeLookahead()
	{
		try
		{
			LogicalTimeInterval interval = TypeFactory.createInterval( -10.0 );
			defaultFederate.rtiamb.modifyLookahead( interval );
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

	//////////////////////////////////////////////////
	// TEST: testModifyLookaheadWithNullLookahead() //
	//////////////////////////////////////////////////
	@Test
	public void testModifyLookaheadWithNullLookahead()
	{
		try
		{
			defaultFederate.rtiamb.modifyLookahead( null );
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

	//////////////////////////////////////////////
	// TEST: testModifyLookaheadWhenNotJoined() //
	//////////////////////////////////////////////
	@Test
	public void testModifyLookaheadWhenNotJoined()
	{
		// resign so we can run the test
		defaultFederate.quickResign();

		// modify the lookahead
		try
		{
			LogicalTimeInterval interval = TypeFactory.createInterval( 10.0 );
			defaultFederate.rtiamb.modifyLookahead( interval );
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
	//////////////////////////////// Enforce Lookahead Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////
	// TEST: testEnforceLookaheadOnUpdate() //
	//////////////////////////////////////////
	/**
	 * This test will attempt to send an attribute update with a time value less than the current
	 * time + lookahead. This attempt should result in an exception
	 */
	@Test(expectedExceptions=InvalidLogicalTime.class)
	public void testEnforceLookaheadOnUpdate() throws Exception
	{
		// initialize the test //
		defaultFederate.quickPublish( "ObjectRoot.A", "aa" );
		int oid = defaultFederate.quickRegister( "ObjectRoot.A" );
		
		// try the update //
		HashMap<Integer,byte[]> values = new HashMap<Integer,byte[]>();
		values.put( defaultFederate.quickACHandle("ObjectRoot.A","aa"), "aa".getBytes() );
		LogicalTime time = TypeFactory.createTime( 2.0 );
		ObjectInstanceHandle oHandle = TypeFactory.getObjectHandle( oid ); 
		defaultFederate.rtiamb.updateAttributeValues( oHandle,
		                                              TypeFactory.newAttributeMap( values ),
		                                              "tag".getBytes(),
		                                              time );
	}
	
	//////////////////////////////////////////
	// TEST: testEnforceLookaheadOnDelete() //
	//////////////////////////////////////////
	/**
	 * This test will attempt to delete an instance with a time value less than the current time
	 * + lookahead. THis should result in an exception.
	 */
	@Test(expectedExceptions=InvalidLogicalTime.class)
	public void testEnforceLookaheadOnDelete() throws Exception
	{
		// initialize the test //
		defaultFederate.quickPublish( "ObjectRoot.A", "aa" );
		int oid = defaultFederate.quickRegister( "ObjectRoot.A" );
		
		// try the delete //
		LogicalTime time = TypeFactory.createTime( 2.0 );
		ObjectInstanceHandle oHandle = TypeFactory.getObjectHandle( oid ); 
		defaultFederate.rtiamb.deleteObjectInstance( oHandle, "tag".getBytes(), time );
	}

	//////////////////////////////////////////
	// TEST: testEnforceLookaheadOnUpdate() //
	//////////////////////////////////////////
	/**
	 * This test will attempt to send an interaction with a time value less than the current time
	 * + lookahead. This attempt should result in an exception
	 */
	@Test(expectedExceptions=InvalidLogicalTime.class)
	public void testEnforceLookaheadOnInteraction() throws Exception
	{
		// initialize the test //
		defaultFederate.quickPublish( "InteractionRoot.X" );
		
		// try the send //
		int handle = defaultFederate.quickICHandle( "InteractionRoot.X" );
		LogicalTime time = TypeFactory.createTime( 2.0 );
		defaultFederate.rtiamb.sendInteraction( TypeFactory.getInteractionHandle(handle),
		                                        TypeFactory.newParameterMap(),
		                                        "tag".getBytes(),
		                                        time );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
