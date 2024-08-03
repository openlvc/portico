/*
 *   Copyright 2006 The Portico Project
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

import hla.rti.FederateNotExecutionMember;
import hla.rti.InvalidFederationTime;
import hla.rti.InvalidLookahead;
import hla.rti.LogicalTime;
import hla.rti.LogicalTimeInterval;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hla.rti.SuppliedAttributes;
import hla.rti.SuppliedParameters;
import hlaunit.hla13.TestSetup;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

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
 * also using the various helper methods of {@link Test13Federate} to validate that lookahead
 * values are enforces for things like interactions and attribute updates. 
 */
@Test(singleThreaded=true, groups={"LookaheadTest", "lookahead", "timeManagement"})
public class LookaheadTest extends Abstract13Test
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
	//        throws InvalidLookahead,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

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
			LogicalTimeInterval interval = defaultFederate.createInterval( 10.0 );
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
		log( "Decreasing the lookahead is not currently supported" );
	}

	//////////////////////////////////////////////////////
	// TEST: testModifyLookaheadWithNegativeLookahead() //
	//////////////////////////////////////////////////////
	@Test
	public void testModifyLookaheadWithNegativeLookahead()
	{
		try
		{
			LogicalTimeInterval interval = defaultFederate.createInterval( -10.0 );
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
			LogicalTimeInterval interval = defaultFederate.createInterval( 10.0 );
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

	///////////////////////////////////////////////////
	// TEST: testModifyLookaheadWhenSaveInProgress() //
	///////////////////////////////////////////////////
	@Test
	public void testModifyLookaheadWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			LogicalTimeInterval interval = defaultFederate.createInterval( 10.0 );
			defaultFederate.rtiamb.modifyLookahead( interval );
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
	// TEST: testModifyLookaheadWhenRestoreInProgress() //
	//////////////////////////////////////////////////////
	@Test
	public void testModifyLookaheadWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			LogicalTimeInterval interval = defaultFederate.createInterval( 10.0 );
			defaultFederate.rtiamb.modifyLookahead( interval );
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
	//////////////////////////////// Enforce Lookahead Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////
	// TEST: testEnforceLookaheadOnUpdate() //
	//////////////////////////////////////////
	/**
	 * This test will attempt to send an attribute update with a time value less than the current
	 * time + lookahead. This attempt should result in an exception
	 */
	@Test(expectedExceptions=InvalidFederationTime.class)
	public void testEnforceLookaheadOnUpdate() throws Exception
	{
		// initialize the test //
		defaultFederate.quickPublish( "ObjectRoot.A", "aa" );
		int oid = defaultFederate.quickRegister( "ObjectRoot.A" );
		
		// try the update //
		SuppliedAttributes atts = TestSetup.getRTIFactory().createSuppliedAttributes();
		atts.add( defaultFederate.quickACHandle("ObjectRoot.A","aa"), "aa".getBytes() );
		LogicalTime time = defaultFederate.createTime( 2.0 );
		defaultFederate.rtiamb.updateAttributeValues( oid, atts, "tag".getBytes(), time );
	}
	
	//////////////////////////////////////////
	// TEST: testEnforceLookaheadOnDelete() //
	//////////////////////////////////////////
	/**
	 * This test will attempt to delete an instance with a time value less than the current time
	 * + lookahead. THis should result in an exception.
	 */
	@Test(expectedExceptions=InvalidFederationTime.class)
	public void testEnforceLookaheadOnDelete() throws Exception
	{
		// initialize the test //
		defaultFederate.quickPublish( "ObjectRoot.A", "aa" );
		int oid = defaultFederate.quickRegister( "ObjectRoot.A" );
		
		// try the delete //
		LogicalTime time = defaultFederate.createTime( 2.0 );
		defaultFederate.rtiamb.deleteObjectInstance( oid, "tag".getBytes(), time );
	}

	//////////////////////////////////////////
	// TEST: testEnforceLookaheadOnUpdate() //
	//////////////////////////////////////////
	/**
	 * This test will attempt to send an interaction with a time value less than the current time
	 * + lookahead. This attempt should result in an exception
	 */
	@Test(expectedExceptions=InvalidFederationTime.class)
	public void testEnforceLookaheadOnInteraction() throws Exception
	{
		// initialize the test //
		defaultFederate.quickPublish( "InteractionRoot.X" );
		
		// try the send //
		int handle = defaultFederate.quickICHandle( "InteractionRoot.X" );
		SuppliedParameters params = TestSetup.getRTIFactory().createSuppliedParameters();
		LogicalTime time = defaultFederate.createTime( 2.0 );
		defaultFederate.rtiamb.sendInteraction( handle, params, "tag".getBytes(), time );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
