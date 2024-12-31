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

@Test(singleThreaded=true,groups={"deleteRegion", "DeleteRegionTest", "ddm"})
public class DeleteRegionTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
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
		
		// get the handle information and create a region
		this.region = defaultFederate.quickCreateRegion( "TestSpace", 1 );
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
	/////////////////////////////// Delete Region Test Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void deleteRegion( Region theRegion )
	//        throws RegionNotKnown,
	//               RegionInUse,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	//////////////////////////////////////
	// TEST: (valid) testDeleteRegion() //
	//////////////////////////////////////
	@Test
	public void testDeleteRegion()
	{
		// delete the region, making sure it occurs happily
		try
		{
			defaultFederate.rtiamb.deleteRegion( region );
		}
		catch( Exception e )
		{
			unexpectedException( "deleting valid region", e );
		}
		
		// validate that the region has been deleted by attempting to modify it
		try
		{
			defaultFederate.rtiamb.notifyOfRegionModification( region );
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

	///////////////////////////////////////////
	// TEST: testDeleteRegionWhenNotJoined() //
	///////////////////////////////////////////
	@Test
	public void testDeleteRegionWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			defaultFederate.rtiamb.deleteRegion( region );
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
	// TEST: testDeleteRegionWhenSaveInProgress() //
	////////////////////////////////////////////////
	@Test
	public void testDeleteRegionWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.deleteRegion( region );
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
	// TEST: testDeleteRegionWhenRestoreInProgress() //
	///////////////////////////////////////////////////
	@Test
	public void testDeleteRegionWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.deleteRegion( region );
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
	// TEST: testDeleteRegionFromDifferentFederate() //
	///////////////////////////////////////////////////
	@Test
	public void testDeleteRegionFromDifferentFederate()
	{
		// create a second federate to test with
		Test13Federate secondFederate = new Test13Federate( "secondFederate", this );
		secondFederate.quickJoin();
		
		// try and delete the region that the federate didn't create
		try
		{
			secondFederate.rtiamb.deleteRegion( region );
			secondFederate.quickResign();
			expectedException( RegionNotKnown.class );
		}
		catch( RegionNotKnown rnn )
		{
			// success!
			secondFederate.quickResign();
		}
		catch( Exception e )
		{
			secondFederate.quickResign();
			wrongException( e, RegionNotKnown.class );
		}
	}
	
	//////////////////////////////////////////////
	// TEST: testDeleteRegionWithNullInstance() //
	//////////////////////////////////////////////
	@Test
	public void testDeleteRegionWithNullInstance()
	{
		try
		{
			defaultFederate.rtiamb.deleteRegion( null );
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

	/////////////////////////////////////////
	// TEST: testDeleteRegionThatIsInUse() //
	/////////////////////////////////////////
	@Test
	public void testDeleteRegionThatIsInUse()
	{
		log( "Region in use testing not yet implemented" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
