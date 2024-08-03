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
package hlaunit.hla13.time;

import hla.rti.AsynchronousDeliveryAlreadyEnabled;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"TimeManagementSaveRestoreTest", "timeManagement", "SaveRestore"})
public class TimeManagementSaveRestoreTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate secondFederate;
	private String saveLabel;

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
		this.saveLabel = "TimeManagementSaveRestoreTest";
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
		
		// set up some basic time management state
		defaultFederate.quickEnableAsyncDelivery();
		defaultFederate.quickEnableRegulating( 5.0 );
		defaultFederate.quickEnableConstrained();
		secondFederate.quickEnableAsyncDelivery();
		secondFederate.quickEnableRegulating( 5.0 );
		secondFederate.quickEnableConstrained();
		
		defaultFederate.quickAdvanceRequest( 100.0 );
		secondFederate.quickAdvanceAndWait( 100.0 );
		defaultFederate.fedamb.waitForTimeAdvance( 100.0 );

		// get that data saved out
		defaultFederate.quickSaveToCompletion( saveLabel );
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
	////////////////////////// Time Management Save/Restore Tests ///////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////
	// TEST: (valid) testTimeAfterSaveRestore() //
	//////////////////////////////////////////////
	@Test
	public void testTimeAfterSaveRestore()
	{
		// advance time some
		defaultFederate.quickAdvanceFederation( 150.0 );
		Assert.assertEquals( defaultFederate.quickQueryCurrentTime(), 150.0 );
		Assert.assertEquals( secondFederate.quickQueryCurrentTime(), 150.0 );

		// restore and check
		defaultFederate.quickRestoreToCompletion( saveLabel );
		Assert.assertEquals( defaultFederate.quickQueryCurrentTime(), 100.0 );
		Assert.assertEquals( secondFederate.quickQueryCurrentTime(), 100.0 );
	}

	///////////////////////////////////////////////////
	// TEST: (valid) testLookaheadAfterSaveRestore() //
	///////////////////////////////////////////////////
	@Test
	public void testLookaheadAfterSaveRestore()
	{
		defaultFederate.quickModifyLookahead( 10.0 );
		Assert.assertEquals( defaultFederate.quickQueryLookahead(), 10.0 );
		
		// restore and check
		defaultFederate.quickRestoreToCompletion( saveLabel );
		Assert.assertEquals( defaultFederate.quickQueryLookahead(), 5.0 );
	}

	///////////////////////////////////////////////////////////
	// TEST: (valid) testRegulatingEnabledAfterSaveRestore() //
	///////////////////////////////////////////////////////////
	@Test
	public void testRegulatingEnabledAfterSaveRestore()
	{
		// disable regulating and make sure that the advances of defaultFederate
		// no long affect any constrained federates
		defaultFederate.quickDisableRegulating();
		secondFederate.quickAdvanceAndWait( 1000.0 );
		
		// restore and check
		defaultFederate.quickRestoreToCompletion( saveLabel );
		Assert.assertEquals( defaultFederate.quickQueryCurrentTime(), 100.0 );
		Assert.assertEquals( secondFederate.quickQueryCurrentTime(), 100.0 );
		secondFederate.quickAdvanceRequest( 500.0 );
		secondFederate.fedamb.waitForTimeAdvanceTimeout( 500.0 );
	}
	
	////////////////////////////////////////////////////////////
	// TEST: (valid) testConstrainedEnabledAfterSaveRestore() //
	////////////////////////////////////////////////////////////
	@Test
	public void testConstrainedEnabledAfterSaveRestore()
	{
		// disable constrained and make sure that the federate isn't dependent on regulating feds
		defaultFederate.quickDisableConstrained();
		defaultFederate.quickAdvanceAndWait( 1000.0 );
		
		// restore and check
		defaultFederate.quickRestoreToCompletion( saveLabel );
		Assert.assertEquals( defaultFederate.quickQueryCurrentTime(), 100.0 );
		Assert.assertEquals( secondFederate.quickQueryCurrentTime(), 100.0 );
		defaultFederate.quickAdvanceRequest( 500.0 );
		defaultFederate.fedamb.waitForTimeAdvanceTimeout( 500.0 );
	}
	
	//////////////////////////////////////////////////////
	// TEST: (valid) testAsyncEnabledAfterSaveRestore() //
	//////////////////////////////////////////////////////
	@Test
	public void testAsyncEnabledAfterSaveRestore()
	{
		// disable async delivery
		defaultFederate.quickDisableAsyncDelivery();
		
		// restore and then try to enable it, if the restore works we should get
		// an exception telling us that it is already enabled
		defaultFederate.quickRestoreToCompletion( saveLabel );
		try
		{
			defaultFederate.rtiamb.enableAsynchronousDelivery();
			expectedException( AsynchronousDeliveryAlreadyEnabled.class );
		}
		catch( AsynchronousDeliveryAlreadyEnabled adae )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AsynchronousDeliveryAlreadyEnabled.class );
		}
	}

	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
