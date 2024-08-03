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
package hlaunit.ieee1516e.support;

import hlaunit.ieee1516e.common.Abstract1516eTest;

import org.portico.impl.hla1516e.types.time.DoubleTime;
import org.portico.impl.hla1516e.types.time.DoubleTimeInterval;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This test covers the basics of state information that can be obtained from the RTIambassador
 * through the queryXxx methods (we're talking, time, lbts, etc...)
 */
@Test(singleThreaded=true, groups={"QueryTest", "state", "query", "timeManagement", "supportServices"})
public class QueryTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

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
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
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
		defaultFederate.quickResign();
		defaultFederate.quickDestroy();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////
	// TEST: testQueryLogicalTime() //
	//////////////////////////////////
	@Test
	public void testQueryLogicalTime() throws Exception
	{
		// check the time before we have any values //
		double time = ((DoubleTime)defaultFederate.rtiamb.queryLogicalTime()).getTime();
		Assert.assertEquals( time, 0.0 );
		
		// advance time somewhat and check again //
		defaultFederate.quickAdvanceAndWait( 10.0 );
		time = ((DoubleTime)defaultFederate.rtiamb.queryLogicalTime()).getTime();
		Assert.assertEquals( time, 10.0 );
	}

	////////////////////////////////
	// TEST: testQueryLookahead() //
	////////////////////////////////
	@Test
	public void testQueryLookahead() throws Exception
	{
		// query the default lookahead //
		double time = ((DoubleTimeInterval)defaultFederate.rtiamb.queryLookahead()).getTime();
		Assert.assertEquals( time, 0.0 );
		
		// enable time regulation and query the lookahead //
		defaultFederate.quickEnableRegulating( 5.0 );
		time = ((DoubleTimeInterval)defaultFederate.rtiamb.queryLookahead()).getTime();
		Assert.assertEquals( time, 5.0 );
		
		// modify the lookahead and query it again //
		defaultFederate.quickModifyLookahead( 2.0 );
		time = ((DoubleTimeInterval)defaultFederate.rtiamb.queryLookahead()).getTime();
		Assert.assertEquals( time, 2.0 );
	}

	///////////////////////////////////////
	// TEST: testQueryMinNextEventTime() //
	///////////////////////////////////////
	//@Test
	//public void testQueryMinNextEventTime()
	//{
		
	//}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
