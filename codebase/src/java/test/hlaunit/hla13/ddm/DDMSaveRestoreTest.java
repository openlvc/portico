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
package hlaunit.hla13.ddm;

import hla.rti.Region;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"ObjectManagementSaveRestoreTest", "objectManagement", "SaveRestore"})
public class DDMSaveRestoreTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate secondFederate;
	private String saveLabel;
	private int objectHandle;
	private Region publicationRegion;
	private Region subscriptionRegion;
	
	private int aaHandle, abHandle, acHandle;
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
		
		this.secondFederate = new Test13Federate( "secondFederate", this );
		this.saveLabel = "DDMSaveRestoreTest";
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception
	{
		// create and join the federation
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
		
		// cache the handles
		aaHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "aa" );
		abHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ab" );
		acHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ac" );
		spaceHandle = defaultFederate.quickSpaceHandle( "TestSpace" );
		dimensionHandle = defaultFederate.quickDimensionHandle( "TestSpace", "TestDimension" );
		
		// publish and subscribe
		defaultFederate.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		defaultFederate.quickPublish( "InteractionRoot.X" );
		publicationRegion = defaultFederate.quickCreateRegion( spaceHandle, 1 );
		publicationRegion.setRangeLowerBound( 0, dimensionHandle, 0 );
		publicationRegion.setRangeUpperBound( 0, dimensionHandle, 1000 );
		defaultFederate.quickModifyRegion( publicationRegion );
		
		subscriptionRegion = secondFederate.quickCreateRegion( spaceHandle, 1 );
		subscriptionRegion.setRangeLowerBound( 0, dimensionHandle, 0 );
		subscriptionRegion.setRangeUpperBound( 0, dimensionHandle, 1000 );
		secondFederate.quickModifyRegion( subscriptionRegion );
		secondFederate.quickSubscribeWithRegion( "ObjectRoot.A", subscriptionRegion, "aa", "ab", "ac" );
		secondFederate.quickSubscribeWithRegion( "InteractionRoot.X", subscriptionRegion );
		
		// register the object
		objectHandle = defaultFederate.quickRegisterWithRegion( "ObjectRoot.A", 
		                                                        publicationRegion,
		                                                        "aa",
		                                                        "ab",
		                                                        "ac" );
		secondFederate.fedamb.waitForDiscovery( objectHandle );
		
		// send an update to make sure things work
		defaultFederate.quickReflect( objectHandle, "aa", "ab", "ac" );
		secondFederate.fedamb.waitForUpdate( objectHandle );
		
		// save
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

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// Helper Methods /////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////
	// TEST: (valid) testRegionModifyResetAfterSaveRestore() //
	///////////////////////////////////////////////////////////
	@Test
	public void testRegionModifyResetAfterSaveRestore() throws Exception
	{
		// modify the publication region so that it is outside the subscription region
		publicationRegion.setRangeLowerBound( 0, dimensionHandle, 2000 );
		publicationRegion.setRangeUpperBound( 0, dimensionHandle, 3000 );
		defaultFederate.quickModifyRegion( publicationRegion );
		
		// update and make sure the second federate doesn't get it
		defaultFederate.quickReflect( objectHandle, "aa", "ab", "ac" );
		secondFederate.fedamb.waitForUpdateTimeout( objectHandle );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// update and make sure the second federate DOES get it now that the region has reverted
		defaultFederate.quickReflect( objectHandle, "aa", "ab", "ac" );
		secondFederate.fedamb.waitForUpdate( objectHandle );
	}

	////////////////////////////////////////////////////////////////
	// TEST: (valid) testRegionAssociationResetAfterSaveRestore() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testRegionAssociationForAttributesRestoredAfterSaveRestore() throws Exception
	{
		// modify publication region so that there is no overlap
		publicationRegion.setRangeLowerBound( 0, dimensionHandle, 2000 );
		publicationRegion.setRangeUpperBound( 0, dimensionHandle, 3000 );
		defaultFederate.quickModifyRegion( publicationRegion );
		
		// save so that this is our default state, this way, when we later unassociate the
		// region we should get updates, but when we revert to this state we shouldn't
		defaultFederate.quickReflect( objectHandle, "aa", "ab", "ac" );
		secondFederate.fedamb.waitForUpdateTimeout( objectHandle );
		defaultFederate.quickSaveToCompletion( saveLabel );
		
		// unassociate the region with the attributes and then update, which the second
		// federate should now receive
		defaultFederate.quickUnassociateWithRegion( objectHandle, publicationRegion );
		defaultFederate.quickReflect( objectHandle, "aa", "ab", "ac" );
		secondFederate.fedamb.waitForUpdate( objectHandle );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// update again, due to the region association that has been restored, the update
		// should not filter through this time
		defaultFederate.quickReflect( objectHandle, "aa", "ab", "ac" );
		secondFederate.fedamb.waitForUpdateTimeout( objectHandle );
	}

	///////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testRegionAssociationForAttributesRemovedAfterSaveRestore() //
	///////////////////////////////////////////////////////////////////////////////
	@Test
	public void testRegionAssociationForAttributesRemovedAfterSaveRestore() throws Exception
	{
		// modify the subscription region so there is no overlap (thus we shouldn't get
		// any updates for attribute associated with the publication region).
		subscriptionRegion.setRangeLowerBound( 0, dimensionHandle, 2000 );
		subscriptionRegion.setRangeUpperBound( 0, dimensionHandle, 3000 );
		secondFederate.quickModifyRegion( subscriptionRegion );
		defaultFederate.quickReflect( objectHandle, "aa", "ab", "ac" );
		secondFederate.fedamb.waitForUpdateTimeout( objectHandle );
		
		// remove the region association and save so that is our default state
		defaultFederate.quickUnassociateWithRegion( objectHandle, publicationRegion );
		defaultFederate.quickReflect( objectHandle, "aa", "ab", "ac" );
		secondFederate.fedamb.waitForUpdate( objectHandle );
		
		defaultFederate.quickSaveToCompletion( saveLabel );
		
		// associate the publication region with the attributes again and make sure we
		// don't receive updates because the regions don't overlap
		defaultFederate.quickAssociateWithRegion( objectHandle,
		                                          publicationRegion,
		                                          aaHandle,
		                                          abHandle,
		                                          acHandle );
		
		// make sure we don't get the update
		defaultFederate.quickReflect( objectHandle, "aa", "ab", "ac" );
		secondFederate.fedamb.waitForUpdateTimeout( objectHandle );
		
		// restore, so the region association should be over
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// make sure we do get the update now the region association is gone
		defaultFederate.quickReflect( objectHandle, "aa", "ab", "ac" );
		secondFederate.fedamb.waitForUpdate( objectHandle );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
