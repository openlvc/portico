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

import hla.rti.FederateNotExecutionMember;
import hla.rti.InvalidExtents;
import hla.rti.Region;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hla.rti.SpaceNotDefined;
import hlaunit.hla13.common.Abstract13Test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * The tests in this class validate the RTI behaviour when attempting to create a new region.
 * They do not explicitly test the implementation of the region class itself (that is left to
 * the {@link RegionTest} class).
 */
@Test(singleThreaded=true,groups={"createRegion", "CreateRegionTest", "ddm"})
public class CreateRegionTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int spaceHandle;
	private int dimensionHandle;

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
	/////////////////////////////// Create Region Test Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public Region createRegion( int spaceHandle, int numberOfExtents )
	//        throws SpaceNotDefined,
	//               InvalidExtents,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	//////////////////////////////////////
	// TEST: (valid) testCreateRegion() //
	//////////////////////////////////////
	@Test
	public void testCreateRegion() throws Exception
	{
		Region region = null;
		try
		{
			region = defaultFederate.rtiamb.createRegion( spaceHandle, 1 );
		}
		catch( Exception e )
		{
			unexpectedException( "creating region", e );
		}
		
		// check the region for valid data
		Assert.assertEquals( region.getSpaceHandle(), spaceHandle );
		Assert.assertEquals( region.getNumberOfExtents(), 1 );
		Assert.assertEquals( region.getRangeLowerBound(0,dimensionHandle), 0 );
		Assert.assertEquals( region.getRangeUpperBound(0,dimensionHandle), Long.MAX_VALUE );
	}

	///////////////////////////////////////////
	// TEST: testCreateRegionWhenNotJoined() //
	///////////////////////////////////////////
	@Test
	public void testCreateRegionWhenNotJoined()
	{
		defaultFederate.quickResign();

		try
		{
			defaultFederate.rtiamb.createRegion( spaceHandle, 1 );
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

	////////////////////////////////////////////////
	// TEST: testCreateRegionWhenSaveInProgress() //
	////////////////////////////////////////////////
	@Test
	public void testCreateRegionWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.createRegion( spaceHandle, 1 );
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

	///////////////////////////////////////////////////
	// TEST: testCreateRegionWhenRestoreInProgress() //
	///////////////////////////////////////////////////
	@Test(enabled=false)
	public void testCreateRegionWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.createRegion( spaceHandle, 1 );
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

	////////////////////////////////////////////////////////
	// TEST: testCreateRegionWithNonExistentSpaceHandle() //
	////////////////////////////////////////////////////////
	@Test
	public void testCreateRegionWithNonExistentSpaceHandle()
	{
		try
		{
			defaultFederate.rtiamb.createRegion( 100000000, 1 );
			expectedException( SpaceNotDefined.class );
		}
		catch( SpaceNotDefined snd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, SpaceNotDefined.class );
		}
	}

	/////////////////////////////////////////////////////
	// TEST: testCreateRegionWithNegativeSpaceHandle() //
	/////////////////////////////////////////////////////
	@Test
	public void testCreateRegionWithNegativeSpaceHandle()
	{
		try
		{
			defaultFederate.rtiamb.createRegion( -1, 1 );
			expectedException( SpaceNotDefined.class );
		}
		catch( SpaceNotDefined snd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, SpaceNotDefined.class );
		}
	}
	
	/////////////////////////////////////////////////
	// TEST: testCreateRegionWithNegativeExtents() //
	/////////////////////////////////////////////////
	@Test
	public void testCreateRegionWithNegativeExtents()
	{
		try
		{
			defaultFederate.rtiamb.createRegion( spaceHandle, -1 );
			expectedException( InvalidExtents.class );
		}
		catch( InvalidExtents ie )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidExtents.class );
		}
	}
	
	//////////////////////////////////////////////////
	// TEST: testCreateRegionWithExcessiveExtents() //
	//////////////////////////////////////////////////
	@Test
	public void testCreateRegionWithExcessiveExtents()
	{
		try
		{
			defaultFederate.rtiamb.createRegion( spaceHandle, Integer.MAX_VALUE );
			expectedException( InvalidExtents.class );
		}
		catch( InvalidExtents ie )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidExtents.class );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
