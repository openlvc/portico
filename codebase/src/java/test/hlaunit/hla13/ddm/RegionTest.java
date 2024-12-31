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
package hlaunit.hla13.ddm;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.Region;
import hlaunit.hla13.common.Abstract13Test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * The tests in this class validate the behaviour of a Region. They ensure that once a region has
 * been created, that it behaves as expected. The methods DO NOT explicitly test the process of
 * create/destroying a region etc...
 */
@Test(singleThreaded=true,groups={"RegionTest", "ddm","supportServices"})
public class RegionTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int spaceHandle;
	private int dimensionHandle;
	private int extentCount = 1;
	private Region region;

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
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		
		// get the handle information
		this.spaceHandle = defaultFederate.quickSpaceHandle( "TestSpace" );
		this.dimensionHandle = defaultFederate.quickDimensionHandle( "TestSpace", "TestDimension" );
		this.region = defaultFederate.quickCreateRegion( spaceHandle, extentCount );
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		defaultFederate.quickResign();
		defaultFederate.quickDestroy();
	}
	
	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Region Test Methods ///////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////
	// TEST: (valid) testRegionGetSpaceHandle() //
	//////////////////////////////////////////////
	@Test
	public void testRegionGetSpaceHandle()
	{
		Assert.assertEquals( region.getSpaceHandle(), spaceHandle, "invalid space handle" );
	}

	//////////////////////////////////////////////////
	// TEST: (valid) testRegionGetNumberOfExtents() //
	//////////////////////////////////////////////////
	@Test
	public void testRegionGetNumberOfExtents()
	{
		Assert.assertEquals( region.getNumberOfExtents(), extentCount, "invalid extent count" );
	}

	////////////////////////////////////////////////////
	// TEST: (valid) testRegionExtentInitialization() //
	////////////////////////////////////////////////////
	/**
	 * Validate that the initial values given to the extents of a region are as expected
	 */
	@Test
	public void testRegionExtentInitialization() throws Exception
	{
		// validate that the extents have the correct initial values
		Assert.assertEquals( region.getRangeLowerBound(0,dimensionHandle), 0,
		                     "region has invalid initial extent value" );
		Assert.assertEquals( region.getRangeUpperBound(0,dimensionHandle), Long.MAX_VALUE,
		                     "region has invalid initial extent value" );
	}
	
	////////////////////////////////////////////////
	// TEST: testRegionGetRangeForInvalidExtent() //
	////////////////////////////////////////////////
	@Test
	public void testRegionGetRangeForInvalidExtent()
	{
		// validate that we get an exception when fetching an invalid extent
		try
		{
			// lower bound
			region.getRangeLowerBound( 2, dimensionHandle );
			expectedException( ArrayIndexOutOfBounds.class );
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ArrayIndexOutOfBounds.class );
		}
		
		try
		{
			// upper bound
			region.getRangeUpperBound( 2, dimensionHandle );
			expectedException( ArrayIndexOutOfBounds.class );
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ArrayIndexOutOfBounds.class );
		}
	}
	
	/////////////////////////////////////////////////
	// TEST: testRegionGetRangeForNegativeExtent() //
	/////////////////////////////////////////////////
	@Test
	public void testRegionGetRangeForNegativeExtent()
	{
		// validate that we get an exception when fetching a negative extent
		try
		{
			// lower bound
			region.getRangeLowerBound( -1, dimensionHandle );
			expectedException( ArrayIndexOutOfBounds.class );
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ArrayIndexOutOfBounds.class );
		}
		
		try
		{
			// upper bound
			region.getRangeUpperBound( -1, dimensionHandle );
			expectedException( ArrayIndexOutOfBounds.class );
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ArrayIndexOutOfBounds.class );
		}
	}
	
	///////////////////////////////////////////////////
	// TEST: testRegionGetRangeForInvalidDimension() //
	///////////////////////////////////////////////////
	@Test
	public void testRegionGetRangeForInvalidDimension()
	{
		// validate that we get an exception when fetching an invalid dimension
		try
		{
			// lower bound
			region.getRangeLowerBound( 0, dimensionHandle+100 );
			expectedException( ArrayIndexOutOfBounds.class );
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ArrayIndexOutOfBounds.class );
		}
		
		try
		{
			// upper bound
			region.getRangeUpperBound( 0, dimensionHandle+100 );
			expectedException( ArrayIndexOutOfBounds.class );
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ArrayIndexOutOfBounds.class );
		}
	}
	
	////////////////////////////////////////////////////
	// TEST: testRegionGetRangeForNegativeDimension() //
	////////////////////////////////////////////////////
	@Test
	public void testRegionGetRangeForNegativeDimension()
	{
		// validate that we get an exception when fetching a negative dimension
		try
		{
			// lower bound
			region.getRangeLowerBound( 0, -1 );
			expectedException( ArrayIndexOutOfBounds.class );
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ArrayIndexOutOfBounds.class );
		}
		
		try
		{
			// upper bound
			region.getRangeUpperBound( 0, -1 );
			expectedException( ArrayIndexOutOfBounds.class );
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ArrayIndexOutOfBounds.class );
		}
	}
	
	/////////////////////////////////////////
	// TEST: testRegionSetRangeForExtent() //
	/////////////////////////////////////////
	/**
	 * Changes the ranges for a valid extent of the region and then tests to make sure the
	 * region state reflects those changes.
	 */
	@Test
	public void testRegionSetRangeForExtent() throws Exception
	{
		// change the region values
		try
		{
			region.setRangeLowerBound( 0, dimensionHandle, 100000 );
			region.setRangeUpperBound( 0, dimensionHandle, 100001 );
		}
		catch( Exception e )
		{
			unexpectedException( "setting range for valid extent", e );
		}
		
		// ensure that the values are correct
		Assert.assertEquals( region.getRangeLowerBound(0,dimensionHandle), 100000,
		                     "wrong value for range lower bound after change" );
		Assert.assertEquals( region.getRangeUpperBound(0,dimensionHandle), 100001,
		                     "wrong value for range upper bound after change" );
	}
	
	///////////////////////////////////////////////////////
	// TEST: testRegionSetRangeWithSillyYetValidValues() //
	///////////////////////////////////////////////////////
	/**
	 * Although it doesn't make much sense, it is valid to set* the values of a range to
	 * illogical settings (e.g. where lowerBound > upperBound). This test will ensure that
	 * this is possible.
	 * <p/>
	 * *Note that this is actually invalid. Although it is allowed now, it should cause an
	 * exception later on when attempting to use this invalid region
	 */
	@Test
	public void testRegionSetRangeWithSillyYetValidValues() throws Exception
	{
		// change the region values
		try
		{
			region.setRangeLowerBound( 0, dimensionHandle, 100001 );
			region.setRangeUpperBound( 0, dimensionHandle, 100000 );
		}
		catch( Exception e )
		{
			unexpectedException( "setting range for valid extent", e );
		}
		
		// ensure that the values are as expected
		Assert.assertEquals( region.getRangeLowerBound(0,dimensionHandle), 100001,
		                     "wrong value for range lower bound after change to illogical value" );
		Assert.assertEquals( region.getRangeUpperBound(0,dimensionHandle), 100000,
		                     "wrong value for range upper bound after change to illogical value" );
	}
	
	////////////////////////////////////////////////
	// TEST: testRegionSetRangeForInvalidExtent() //
	////////////////////////////////////////////////
	@Test
	public void testRegionSetRangeForInvalidExtent()
	{
		try
		{
			region.setRangeLowerBound( 2, dimensionHandle, 100000 );
			expectedException( ArrayIndexOutOfBounds.class );
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ArrayIndexOutOfBounds.class );
		}

		try
		{
			region.setRangeUpperBound( 2, dimensionHandle, 100000 );
			expectedException( ArrayIndexOutOfBounds.class );
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ArrayIndexOutOfBounds.class );
		}
	}
	
	/////////////////////////////////////////////////
	// TEST: testRegionSetRangeForNegativeExtent() //
	/////////////////////////////////////////////////
	@Test
	public void testRegionSetRangeForNegativeExtent()
	{
		try
		{
			region.setRangeLowerBound( -1, dimensionHandle, 100000 );
			expectedException( ArrayIndexOutOfBounds.class );
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ArrayIndexOutOfBounds.class );
		}

		try
		{
			region.setRangeUpperBound( -1, dimensionHandle, 100000 );
			expectedException( ArrayIndexOutOfBounds.class );
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ArrayIndexOutOfBounds.class );
		}
	}
	
	///////////////////////////////////////////////////
	// TEST: testRegionSetRangeForInvalidDimension() //
	///////////////////////////////////////////////////
	@Test
	public void testRegionSetRangeForInvalidDimension()
	{
		try
		{
			region.setRangeLowerBound( 0, dimensionHandle+100, 100000 );
			expectedException( ArrayIndexOutOfBounds.class );
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ArrayIndexOutOfBounds.class );
		}

		try
		{
			region.setRangeUpperBound( 0, dimensionHandle+100, 100000 );
			expectedException( ArrayIndexOutOfBounds.class );
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ArrayIndexOutOfBounds.class );
		}
	}
	
	////////////////////////////////////////////////////
	// TEST: testRegionSetRangeForNegativeDimension() //
	////////////////////////////////////////////////////
	@Test
	public void testRegionSetRangeForNegativeDimension()
	{
		try
		{
			region.setRangeLowerBound( 0, -1, 100000 );
			expectedException( ArrayIndexOutOfBounds.class );
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ArrayIndexOutOfBounds.class );
		}

		try
		{
			region.setRangeUpperBound( 0, -1, 100000 );
			expectedException( ArrayIndexOutOfBounds.class );
		}
		catch( ArrayIndexOutOfBounds aioob )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ArrayIndexOutOfBounds.class );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
