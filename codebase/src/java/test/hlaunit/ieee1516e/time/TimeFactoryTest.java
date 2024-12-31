/*
 *   Copyright 2015 The Portico Project
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
import hla.rti1516e.LogicalTimeFactory;
import hla.rti1516e.LogicalTimeFactoryFactory;
import hla.rti1516e.LogicalTimeInterval;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAinteger64Interval;
import hla.rti1516e.time.HLAinteger64Time;
import hlaunit.ieee1516e.common.Abstract1516eTest;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"TimeFactoryTest", "timeManagement"})
public class TimeFactoryTest extends Abstract1516eTest
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

	@BeforeClass(alwaysRun=true)
	public void beforeClass()
	{
		super.beforeClass();
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		
	}
	
	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////
	// TEST: testCreateHLAfloat64Time() //
	//////////////////////////////////////
	/**
	 * Tests that the values that the LogicalTimeFactory creates for the HLAfloat64Time class
	 * are as expected
	 */
	@Test
	public void testCreateHLAfloat64Time()
	{
		LogicalTimeFactory factory = 
			LogicalTimeFactoryFactory.getLogicalTimeFactory( "HLAfloat64Time" );
		
		LogicalTime time = factory.makeInitial();
		Assert.assertTrue( HLAfloat64Time.class.isInstance(time) );
		
		LogicalTimeInterval interval = factory.makeEpsilon();
		Assert.assertTrue( HLAfloat64Interval.class.isInstance(interval) );
	}
	
	////////////////////////////////////////
	// TEST: testCreateHLAinteger64Time() //
	////////////////////////////////////////
	/**
	 * Tests that the values that the LogicalTimeFactory creates for the HLAinteger64Time class
	 * are as expected
	 */
	@Test
	public void testCreateHLAinteger64Time()
	{
		LogicalTimeFactory factory = 
			LogicalTimeFactoryFactory.getLogicalTimeFactory( "HLAinteger64Time" );
		
		LogicalTime time = factory.makeInitial();
		Assert.assertTrue( HLAinteger64Time.class.isInstance(time) );
		
		LogicalTimeInterval interval = factory.makeEpsilon();
		Assert.assertTrue( HLAinteger64Interval.class.isInstance(interval) );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
