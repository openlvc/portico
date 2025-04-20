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
package hlaunit.hla13.object;

import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"ObjectManagementSaveRestoreTest", "objectManagement", "SaveRestore"})
public class ObjectManagementSaveRestoreTest extends Abstract13Test
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
		this.saveLabel = "ObjectManagementSaveRestoreTest";
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

		// set up pub/sub and register a test object
		defaultFederate.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		defaultFederate.quickPublish( "InteractionRoot.X" );
		secondFederate.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		secondFederate.quickSubscribe( "ObjectRoot.A", "aa", "ab", "ac" );
		secondFederate.quickSubscribe( "InteractionRoot.X" );
		objectHandle = defaultFederate.quickRegister( "ObjectRoot.A", "myObject" );
		secondFederate.fedamb.waitForDiscovery( objectHandle );
		
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
	///////////////////////// Object Management Save/Restore Tests //////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////
	// TEST: (valid) testRegisterObjectAfterSaveRestore() //
	////////////////////////////////////////////////////////
	/**
	 * Register an object with a name (in setup), save, delete that object, restore, try to
	 * register with that name again. Should get an error saying the name is taken because the
	 * object will come back into existence when we restore.
	 */
	@Test
	public void testRegisterObjectAfterSaveRestore()
	{
		// setup registered the object with the name "myObject"
		// delete the object
		secondFederate.quickRegisterFail( "ObjectRoot.A", "myObject" );
		defaultFederate.quickDelete( objectHandle );
		secondFederate.fedamb.waitForRORemoval( objectHandle );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// now try and register the object again, should fail
		secondFederate.quickRegisterFail( "ObjectRoot.A", "myObject" );
	}
	
	/////////////////////////////////////////////////////////////
	// TEST: (valid) testUpdateAttributesTSOAfterSaveRestore() //
	/////////////////////////////////////////////////////////////
	/**
	 * Advance to time 200, try to update attributes @ 150 and fail, restore, try to update
	 * attributes @ 150 again only it should succeed.
	 */
	@Test
	public void testUpdateAttributesTSOAfterSaveRestore()
	{
		// advance federation to 200
		defaultFederate.quickAdvanceFederation( 200.0 );
		
		// try and update attributes @ 150 for failure
		defaultFederate.quickReflectFail( objectHandle, 150, "aa", "ab", "ac" );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// try and update attributes @ 150, should work now
		defaultFederate.quickReflect( objectHandle, 150, "aa", "ab", "ac" );
	}
	
	////////////////////////////////////////////////////////////
	// TEST: (valid) testUpdateAttributesROAfterSaveRestore() //
	////////////////////////////////////////////////////////////
	/**
	 * Save, delete and object, restore, try and update the attributes of the object. This
	 * should work fine because the restore puts the object back into the federation.
	 */
	@Test
	public void testUpdateAttributesROAfterSaveRestore()
	{
		// delete the object
		defaultFederate.quickDelete( objectHandle );
		secondFederate.fedamb.waitForRORemoval( objectHandle );
		defaultFederate.quickReflectFail( objectHandle, "aa", "ab", "ac" );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// update the attributes of the object we deleted before we restored
		defaultFederate.quickReflect( objectHandle, "aa", "ab", "ac" );
	}
	
	//////////////////////////////////////////////////////
	// TEST: (valid) testDeleteObjectAfterSaveRestore() //
	//////////////////////////////////////////////////////
	/**
	 * Save, delete and object, restore, delete it again. This should work because the restore
	 * should put the object back into the federation.
	 */
	@Test
	public void testDeleteObjectAfterSaveRestore()
	{
		// delete the object
		defaultFederate.quickDelete( objectHandle );
		secondFederate.fedamb.waitForRORemoval( objectHandle );
		defaultFederate.quickDeleteFail( objectHandle );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// try and delete again, this should not fail
		defaultFederate.quickDelete( objectHandle );
	}
	
	///////////////////////////////////////////////////////////
	// TEST: (valid) testSendInteractionROAfterSaveRestore() //
	///////////////////////////////////////////////////////////
	/**
	 * Advance to time 200, try to send an interaction @ 150 and fail, restore, try to send
	 * an interaction @ 150 again only it should succeed.
	 */
	@Test
	public void testSendInteractionTSOAfterSaveRestore()
	{
		// make sure we can send an interaction at this time
		defaultFederate.quickSend( "InteractionRoot.X", 150, "xa", "xb", "xc" );
		
		// advance federation to 200
		defaultFederate.quickAdvanceFederation( 200.0 );
		
		// try and update attributes @ 150 for failure
		defaultFederate.quickSendFail( "InteractionRoot.X", 150, "xa", "xb", "xc" );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// try and update attributes @ 150, should work now
		defaultFederate.quickSend( "InteractionRoot.X", 150, "xa", "xb", "xc" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
