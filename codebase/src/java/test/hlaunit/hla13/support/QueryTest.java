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
package hlaunit.hla13.support;

import hlaunit.hla13.common.Abstract13Test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;

/**
 * This test covers the basics of state information that can be obtained from the RTIambassador
 * through the queryXxx methods (we're talking, time, lbts, etc...)
 */
@Test(singleThreaded=true, groups={"QueryTest", "state", "query", "timeManagement", "supportServices"})
public class QueryTest extends Abstract13Test
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

	///////////////////////////////////
	// TEST: testQueryFederateTime() //
	///////////////////////////////////
	@Test
	public void testQueryFederateTime() throws Exception
	{
		// check the time before we have any values //
		double time = ((DoubleTime)defaultFederate.rtiamb.queryFederateTime()).getTime();
		Assert.assertEquals( time, 0.0 );
		
		// advance time somewhat and check again //
		defaultFederate.quickAdvanceAndWait( 10.0 );
		time = ((DoubleTime)defaultFederate.rtiamb.queryFederateTime()).getTime();
		Assert.assertEquals( time, 10.0 );
	}

	///////////////////////////
	// TEST: testQueryLBTS() //
	///////////////////////////
	@Test
	public void testQueryLBTS() throws Exception
	{
		// query the LBTS while not regulating //
		double time = ((DoubleTime)defaultFederate.rtiamb.queryLBTS()).getTime();
		Assert.assertEquals( time, 0.0 );
		
		// advance time and query LBTS again //
		defaultFederate.quickAdvanceAndWait( 10.0 );
		time = ((DoubleTime)defaultFederate.rtiamb.queryLBTS()).getTime();
		Assert.assertEquals( time, 10.0 );
		
		// enable regulating and query LBTS again //
		defaultFederate.quickEnableRegulating( 5.0 );
		time = ((DoubleTime)defaultFederate.rtiamb.queryLBTS()).getTime();
		Assert.assertEquals( time, 15.0 );
		
		// modify lookahead and query LBTS again //
		defaultFederate.quickModifyLookahead( 2.0 );
		time = ((DoubleTime)defaultFederate.rtiamb.queryLBTS()).getTime();
		Assert.assertEquals( time, 12.0 );
	}

	////////////////////////////////
	// TEST: testQueryLookahead() //
	////////////////////////////////
	@Test
	public void testQueryLookahead() throws Exception
	{
		// query the default lookahead //
		double time = ((DoubleTimeInterval)defaultFederate.rtiamb.queryLookahead()).getInterval();
		Assert.assertEquals( time, 0.0 );
		
		// enable time regulation and query the lookahead //
		defaultFederate.quickEnableRegulating( 5.0 );
		time = ((DoubleTimeInterval)defaultFederate.rtiamb.queryLookahead()).getInterval();
		Assert.assertEquals( time, 5.0 );
		
		// modify the lookahead and query it again //
		defaultFederate.quickModifyLookahead( 2.0 );
		time = ((DoubleTimeInterval)defaultFederate.rtiamb.queryLookahead()).getInterval();
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
