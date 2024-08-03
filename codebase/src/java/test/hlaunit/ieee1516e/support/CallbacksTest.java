/*
 *   Copyright 2013 The Portico Project
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
package hlaunit.ieee1516e.support;

import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This test covers the validation of the enableCallbacks() and disableCallbacks() services.
 * <p/>
 * This will be achieved by having a federate fire object update events that the default federate
 * is interested in, along with validation that these are/aren't processed as appropriate.
 */
@Test(singleThreaded=true, groups={"CallbacksTest", "callbacks", "supportServices"})
public class CallbacksTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate secondFederate;

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
		this.secondFederate = new TestFederate( "secondFederate", this );
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();		
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
		secondFederate.quickResign();
		defaultFederate.quickResign();
		defaultFederate.quickDestroy();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////
	// TEST: testEnableDisableCallbacksWithAttributeUpdates() //
	////////////////////////////////////////////////////////////
	@Test
	public void testEnableDisableCallbacksWithAttributeUpdates() throws Exception
	{
		// set up the publication and subscription interests
		defaultFederate.quickPublish( "ObjectRoot.A", "aa" );
		secondFederate.quickSubscribe( "ObjectRoot.A", "aa" );
		int objectHandle = defaultFederate.quickRegister( "ObjectRoot.A" );
		secondFederate.fedamb.waitForDiscovery( objectHandle );

		// have the default federate update the object and ensure second federate receives
		defaultFederate.quickReflect( objectHandle, "aa" );
		secondFederate.fedamb.waitForROUpdate( objectHandle );
		
		// disable callbacks and ensure updates don't get through
		secondFederate.rtiamb.disableCallbacks();
		defaultFederate.quickReflect( objectHandle, "aa" );
		secondFederate.fedamb.waitForROUpdateTimeout( objectHandle );
		
		// re-enable callback and make sure they get through
		secondFederate.rtiamb.enableCallbacks();
		defaultFederate.quickReflect( objectHandle, "aa" );
		secondFederate.fedamb.waitForROUpdate( objectHandle );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}