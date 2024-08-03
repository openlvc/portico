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
package hlaunit.hla13.declaration;

import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"DeclarationManagementSaveRestoreTest",
                                   "declarationManagement",
                                   "SaveRestore"})
public class DeclarationManagementSaveRestoreTest extends Abstract13Test
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
		this.saveLabel = "DeclarationManagementSaveRestoreTest";
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
		
		// set up pub/sub and register a test object
		defaultFederate.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		defaultFederate.quickPublish( "InteractionRoot.X" );
		secondFederate.quickSubscribe( "ObjectRoot.A", "aa", "ab", "ac" );
		secondFederate.quickSubscribe( "InteractionRoot.X" );
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
	//////////////////////////// Publication Save/Restore Tests /////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////////////////
	// TEST: (valid) testAttributePublicationRestoredAfterSaveRestore() //
	//////////////////////////////////////////////////////////////////////
	@Test
	public void testAttributePublicationRestoredAfterSaveRestore()
	{
		// unpublish ObjectRoot.A in defaultFederate
		defaultFederate.quickUnpublishOC( "ObjectRoot.A" );
		defaultFederate.quickReflectFail( objectHandle, "aa" );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// make sure the publication was restored
		defaultFederate.quickReflect( objectHandle, "aa" );
		secondFederate.fedamb.waitForUpdate( objectHandle );
	}

	/////////////////////////////////////////////////////////////////////
	// TEST: (valid) testAttributePublicationRemovedAfterSaveRestore() //
	/////////////////////////////////////////////////////////////////////
	@Test
	public void testAttributePublicationRemovedAfterSaveRestore()
	{
		// publish ObjectRoot.A in the second federate
		secondFederate.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		secondFederate.quickRegister( "ObjectRoot.A" );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// make sure the publication is gone
		secondFederate.quickRegisterFail( "ObjectRoot.A" );
	}

	////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testInteractionPublicationRestoredAfterSaveRestore() //
	////////////////////////////////////////////////////////////////////////
	@Test
	public void testInteractionPublicationRestoredAfterSaveRestore()
	{
		// unpublish InteractionRoot.X in defaultFederate
		defaultFederate.quickUnpublishIC( "InteractionRoot.X" );
		defaultFederate.quickSendFail( "InteractionRoot.X", "xa", "xb", "xc" );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// make sure the publication was restored
		defaultFederate.quickSend( "InteractionRoot.X", "xa", "xb", "xc" );
		secondFederate.fedamb.waitForROInteraction( "InteractionRoot.X" );
	}

	///////////////////////////////////////////////////////////////////////
	// TEST: (valid) testInteractionPublicationRemovedAfterSaveRestore() //
	///////////////////////////////////////////////////////////////////////
	@Test
	public void testInteractionPublicationRemovedAfterSaveRestore()
	{
		// publish InteractionRoot.X in the second federate
		secondFederate.quickPublish( "InteractionRoot.X" );
		secondFederate.quickSend( "InteractionRoot.X" );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// make sure the publication is gone
		secondFederate.quickSendFail( "InteractionRoot.X" );
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// Subscription Save/Restore Tests ////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////
	// TEST: (valid) testAttributeSubscriptionRestoredAfterSaveRestore() //
	///////////////////////////////////////////////////////////////////////
	@Test
	public void testAttributeSubscriptionRestoredAfterSaveRestore()
	{
		// unsubscribe from ObjectRoot.A in second federate and confirm that it is gone
		secondFederate.quickUnsubscribe( "ObjectRoot.A" );
		defaultFederate.quickReflect( objectHandle, "aa", "ab", "ac" );
		secondFederate.fedamb.waitForUpdateTimeout( objectHandle );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// validate that the subscription is back
		defaultFederate.quickReflect( objectHandle, "aa", "ab", "ac" );
		secondFederate.fedamb.waitForROUpdate( objectHandle );
	}

	//////////////////////////////////////////////////////////////////////
	// TEST: (valid) testAttributeSubscriptionRemovedAfterSaveRestore() //
	//////////////////////////////////////////////////////////////////////
	@Test
	public void testAttributeSubscriptionRemovedAfterSaveRestore()
	{
		// publish ObjectRoot.A in the second federate and subscribe in the default
		// federate, then send an update from the second to ensure subscribe worked in default
		defaultFederate.quickSubscribe( "ObjectRoot.A", "aa", "ab", "ac" );
		secondFederate.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		int otherObject = secondFederate.quickRegister( "ObjectRoot.A" );
		secondFederate.quickReflect( otherObject, "aa" );
		defaultFederate.fedamb.waitForDiscovery( otherObject );
		defaultFederate.fedamb.waitForROUpdate( otherObject );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );

		// publish in secondFederate again (because we restored) then regsiter an object
		// that should not be discovered by the default federate because the subscription
		// is no longer valid following the restore
		secondFederate.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		otherObject = secondFederate.quickRegister( "ObjectRoot.A" );
		defaultFederate.fedamb.waitForDiscoveryTimeout( otherObject );
	}

	/////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testInteractionSubscriptionRestoredAfterSaveRestore() //
	/////////////////////////////////////////////////////////////////////////
	@Test
	public void testInteractionSubscriptionRestoredAfterSaveRestore()
	{
		// unsubscribe from ObjectRoot.A in second federate and confirm that it is gone
		secondFederate.quickUnsubscribeIC( "InteractionRoot.X" );
		defaultFederate.quickSend( "InteractionRoot.X" );
		secondFederate.fedamb.waitForROInteractionTimeout( "InteractionRoot.X" );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// validate that the subscription is back
		defaultFederate.quickSend( "InteractionRoot.X" );
		secondFederate.fedamb.waitForROInteraction( "InteractionRoot.X" );
	}

	////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testInteractionSubscriptionRemovedAfterSaveRestore() //
	////////////////////////////////////////////////////////////////////////
	@Test
	public void testInteractionSubscriptionRemovedAfterSaveRestore()
	{
		// publish InteractionRoot.X in the second federate
		defaultFederate.quickSubscribe( "InteractionRoot.X" );
		secondFederate.quickPublish( "InteractionRoot.X" );
		secondFederate.quickSend( "InteractionRoot.X" );
		defaultFederate.fedamb.waitForROInteraction( "InteractionRoot.X" );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// make sure the publication is gone
		secondFederate.quickPublish( "InteractionRoot.X" );
		secondFederate.quickSend( "InteractionRoot.X" );
		defaultFederate.fedamb.waitForROInteractionTimeout( "InteractionRoot.X" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
