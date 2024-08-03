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
import hla.rti.FederateNotExecutionMember;
import hla.rti.InvalidExtents;
import hla.rti.Region;
import hla.rti.RegionNotKnown;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true,groups={"modifyRegion", "ModifyRegionTest", "ddm"})
public class ModifyRegionTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate listenerFederate;
	
	private Region testRegion;
	private Region listenerRegion;
	private int testObject;
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
		this.listenerFederate = new Test13Federate( "listenerFederate", this );
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		listenerFederate.quickJoin();
		
		// get the handle information and set up the regions
		this.dimensionHandle = defaultFederate.quickDimensionHandle( "TestSpace", "TestDimension" );
		this.testRegion = defaultFederate.quickCreateTestRegion( 100, 200 );
		this.listenerRegion = listenerFederate.quickCreateTestRegion( 1000, 1100 );

		// do publication and subscription
		defaultFederate.quickPublish( "ObjectRoot.A", "aa" );
		listenerFederate.quickSubscribeWithRegion( "ObjectRoot.A", listenerRegion, "aa" );
		
		// register an object to test with
		testObject = defaultFederate.quickRegisterWithRegion( "ObjectRoot.A", testRegion, "aa" );
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		listenerFederate.quickResign();
		defaultFederate.quickResign();
		defaultFederate.quickDestroy();
	}
	
	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Validation Methods ///////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Will send an update from the defaultFederate and make sure it IS received in the
	 * listener federate (as the associated region for the attribute being updated SHOULD
	 * overlap with the listeners subscription region.
	 */
	private void validateRegionsOverlap()
	{
		defaultFederate.quickReflect( testObject, "aa" );
		listenerFederate.fedamb.waitForROUpdate( testObject );		
	}

	/**
	 * Will send an update from the defaultFederate and make sure it ISN'T received in the
	 * listener federate (as the associated region for the attribute being updated SHOULDN'T
	 * overlap with the listeners subscription region.
	 */
	private void validateRegionsDontOverlap()
	{
		defaultFederate.quickReflect( testObject, "aa" );
		listenerFederate.fedamb.waitForROUpdateTimeout( testObject );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Modify Region Test Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void notifyOfRegionModification( Region modifiedRegionInstance )
	//        throws RegionNotKnown,
	//               InvalidExtents,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	//////////////////////////////////////
	// TEST: (valid) testModifyRegion() //
	//////////////////////////////////////
	@Test
	public void testModifyRegion() throws ArrayIndexOutOfBounds
	{
		// make sure that there is no overlap yet
		validateRegionsDontOverlap();
		
		// change the region values locally then validate that there is still
		// no overlap yet (as we haven't notified the RTI of the change)
		testRegion.setRangeLowerBound( 0, dimensionHandle, 1000 );
		testRegion.setRangeUpperBound( 0, dimensionHandle, 2000 );
		validateRegionsDontOverlap();
		
		// attempt to modify the region with the RTI
		try
		{
			defaultFederate.rtiamb.notifyOfRegionModification( testRegion );
		}
		catch( Exception e )
		{
			unexpectedException( "modifying region", e );
		}
		
		// validate that the modification is good (get the region from the RTI)
		validateRegionsOverlap();
	}

	//////////////////////////////////////////////////////////
	// TEST: (valid) testModifyRegionWithEqualRangeBounds() //
	//////////////////////////////////////////////////////////
	/**
	 * This test will set the upper/lower bounds of a range for an extent to be equals and
	 * then attempt to modify the region. This is VALID behaviour.
	 */
	@Test
	public void testModifyRegionWithEqualRangeBounds() throws ArrayIndexOutOfBounds
	{
		// change the region values
		testRegion.setRangeLowerBound( 0, dimensionHandle, 1000 );
		testRegion.setRangeUpperBound( 0, dimensionHandle, 1000 );
		
		// attempt to modify the region with the RTI
		try
		{
			defaultFederate.rtiamb.notifyOfRegionModification( testRegion );
		}
		catch( Exception e )
		{
			unexpectedException( "modifying region with equal range bounds", e );
		}
	}

	////////////////////////////////////////////////
	// TEST: testModifyRegionWithInvalidExtents() //
	////////////////////////////////////////////////
	/**
	 * This method tests that invalid extent values cannot be set, such as having the
	 * minimum value for an extent be greater than the maximum, or that the value for
	 * a range in an extent doesn't fall beyond the permissible range.
	 */
	@Test
	public void testModifyRegionWithInvalidExtents() throws ArrayIndexOutOfBounds
	{
		// change the region values (lower greater than upper)
		testRegion.setRangeLowerBound( 0, dimensionHandle, 2000 );
		testRegion.setRangeUpperBound( 0, dimensionHandle, 1000 );
		
		// try and modify the region
		try
		{
			defaultFederate.rtiamb.notifyOfRegionModification( testRegion );
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

		// validate that the modification did not make its way through to the RTI
		validateRegionsDontOverlap();
	}

	///////////////////////////////////////////
	// TEST: testModifyRegionWhenNotJoined() //
	///////////////////////////////////////////
	@Test
	public void testModifyRegionWhenNotJoined() throws ArrayIndexOutOfBounds
	{
		// resign from the federate
		defaultFederate.quickResign();
		
		// try and modify the region
		try
		{
			defaultFederate.rtiamb.notifyOfRegionModification( testRegion );
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
	// TEST: testModifyRegionWhenSaveInProgress() //
	////////////////////////////////////////////////
	@Test
	public void testModifyRegionWhenSaveInProgress()
	{
		// resign from the federate
		defaultFederate.quickSaveInProgress( "save" );
		
		// try and modify the region
		try
		{
			defaultFederate.rtiamb.notifyOfRegionModification( testRegion );
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

	////////////////////////////////////////////////
	// TEST: testModifyRegionWhenSaveInProgress() //
	////////////////////////////////////////////////
	@Test
	public void testModifyRegionWhenRestoreInProgress()
	{
		// resign from the federate
		defaultFederate.quickRestoreInProgress( "save" );
		
		// try and modify the region
		try
		{
			defaultFederate.rtiamb.notifyOfRegionModification( testRegion );
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

	///////////////////////////////////////////////////
	// TEST: testModifyRegionFromDifferentFederate() //
	///////////////////////////////////////////////////
	/**
	 * This test will take a region created by a different federate and modify it then
	 * attempt to update it. As the region was created in a different federate, this
	 * should fail.
	 */
	@Test
	public void testModifyRegionFromDifferentFederate() throws ArrayIndexOutOfBounds
	{
		// change the region values
		testRegion.setRangeLowerBound( 0, dimensionHandle, 1000 );
		testRegion.setRangeUpperBound( 0, dimensionHandle, 2000 );
		
		// try and modify the region
		try
		{
			listenerFederate.rtiamb.notifyOfRegionModification( testRegion );
			expectedException( RegionNotKnown.class );
		}
		catch( RegionNotKnown rnn )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RegionNotKnown.class );
		}
	}

	//////////////////////////////////////////////
	// TEST: testModifyRegionWithNullInstance() //
	//////////////////////////////////////////////
	@Test
	public void testModifyRegionWithNullInstance()
	{
		try
		{
			defaultFederate.rtiamb.notifyOfRegionModification( null );
			expectedException( RegionNotKnown.class );
		}
		catch( RegionNotKnown rnn )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RegionNotKnown.class );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
