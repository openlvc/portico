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
package hlaunit.hla13.ownership;

import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"OwnershipManagementAfterSaveRestoreTest",
                                   "ownershipManagement",
                                   "SaveRestore"})
public class OwnershipManagementAfterSaveRestoreTest extends Abstract13Test
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
	private int aaHandle;

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
		this.saveLabel = "OwnershipManagementAfterSaveRestoreTest";
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
		
		this.aaHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "aa" );
		
		// set up pub/sub and register a test object
		defaultFederate.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		secondFederate.quickSubscribe( "ObjectRoot.A", "aa", "ab", "ac" );
		secondFederate.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		secondFederate.quickSubscribe( "ObjectRoot.A", "aa", "ab", "ac" );
		objectHandle = defaultFederate.quickRegister( "ObjectRoot.A" );
		secondFederate.fedamb.waitForDiscovery( objectHandle );
		
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
	//////////////////////// Ownership Management Save/Restore Tests ////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////
	// TEST: (valid) testOwnershipExchangeResetAfterSaveRestore() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testOwnershipExchangeResetAfterSaveRestore()
	{
		// exhange ownership of aa
		secondFederate.quickExchangeOwnership( defaultFederate, objectHandle, aaHandle );
		// update aa now that second federate owns it
		defaultFederate.quickReflectFail( objectHandle, "aa" );
		secondFederate.quickReflect( objectHandle, "aa" );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// make sure only the default federate can update now
		defaultFederate.quickReflect( objectHandle, "aa" );
		secondFederate.quickReflectFail( objectHandle, "aa" );
	}

	///////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testUnconditionalOwnershipDivestResetAfterSaveRestore() //
	///////////////////////////////////////////////////////////////////////////
	@Test
	public void testUnconditionalOwnershipDivestResetAfterSaveRestore()
	{
		// unconditionally divest the attribute aa
		defaultFederate.quickUnconditionalDivest( objectHandle, aaHandle );
		defaultFederate.quickReflectFail( objectHandle, "aa" );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// make sure we own aa again
		defaultFederate.quickReflect( objectHandle, "aa" );
	}

	//////////////////////////////////////////////////////////////
	// TEST: (valid) testReleaseResponseResetAfterSaveRestore() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testReleaseResponseResetAfterSaveRestore()
	{
		// request divestiture
		secondFederate.quickAcquireRequest( objectHandle, aaHandle );
		defaultFederate.fedamb.waitForOwnershipReleaseRequest( objectHandle, aaHandle );
		
		// save again, we want an active transfer in the save so we can test that we
		// roll back to this state after the trasnfer is finalized
		defaultFederate.quickSaveToCompletion( saveLabel );

		defaultFederate.quickReleaseResponse( objectHandle, aaHandle );
		secondFederate.fedamb.waitForOwnershipAcquisition( objectHandle, aaHandle );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// try and complete the 
		defaultFederate.quickReleaseResponse( objectHandle, aaHandle );
		secondFederate.fedamb.waitForOwnershipAcquisition( objectHandle, aaHandle );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
