/*
 *   Copyright 2006 The Portico Project
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
package hlaunit.hla13.mom;

import hla.rti.jlc.EncodingHelpers;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.portico.lrc.PorticoConstants;

import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;
import hlaunit.hla13.common.Test13Instance;
import hlaunit.hla13.common.TimeoutException;

@Test(singleThreaded=true, groups={"MomFederateLifecycleTest", "mom"})
public class MomFederateLifecycleTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate secondFederate;
	
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
		this.secondFederate = new Test13Federate( "secondFederate", this );
	}
	
	@Override
	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		//secondFederate.quickJoin();
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

	//////////////////////////////////////////////////////
	// TEST: (valid) testMomFederateInstanceLifecycle() //
	//////////////////////////////////////////////////////
	@Test
	public void testMomFederateInstanceLifecycle() throws Exception
	{
		// subscribe to the MOM information //
		int momHandle = defaultFederate.rtiamb.getObjectClassHandle("ObjectRoot.Manager.Federate");
		int nameHandle = defaultFederate.rtiamb.getAttributeHandle( "FederateType", momHandle );
		
		defaultFederate.quickSubscribe( momHandle, nameHandle );
		
		////////////////////////////
		// MOM instances creation //
		////////////////////////////
		// wait for a discover for both federates //
		Test13Instance one = defaultFederate.fedamb.waitForLatestDiscovery( momHandle );
		
		// join the second federate and wait for the MOM instance to be discovered //
		secondFederate.quickJoin();
		Test13Instance two = defaultFederate.fedamb.waitForLatestDiscovery( momHandle );
		
		// get updates for the attributes //
		defaultFederate.quickProvide( one.getHandle(), nameHandle );
		defaultFederate.quickProvide( two.getHandle(), nameHandle );
		
		// give the other federate a chance to process the provide-request
		quickSleep();
		secondFederate.quickTick( 0.1, 1.0 );
		
		// check the names //
		defaultFederate.fedamb.waitForUpdate( one.getHandle() );
		defaultFederate.fedamb.waitForUpdate( two.getHandle() );
		String oneName = EncodingHelpers.decodeString( one.getAttributes().get(nameHandle) );
		String twoName = EncodingHelpers.decodeString( two.getAttributes().get(nameHandle) );
		Assert.assertEquals( oneName, "defaultFederate" );
		Assert.assertEquals( twoName, "secondFederate" );
		
		//////////////////////////
		// MOM instance removal //
		//////////////////////////
		// resign the second federate and wait for the removal callback
		secondFederate.quickResign();
		defaultFederate.fedamb.waitForRORemoval( two.getHandle() );
	}
	
	////////////////////////////////////
	// TEST: (valid) testDisableMom() //
	////////////////////////////////////
	@Test
	public void testDisabledMom() throws Exception
	{
		// turn the mom off for new federations //
		PorticoConstants.disableMom();
		// create a separate federation //
		Test13Federate federate = new Test13Federate( "disabledMom", this );
		federate.quickCreate( "DisabledMomFederation" );
		federate.quickJoin( "DisabledMomFederation" );
		// turn the mom back on so it doesn't affect other tests //
		PorticoConstants.enableMom();
		
		// subscribe to the MOM information //
		int momHandle = federate.rtiamb.getObjectClassHandle("ObjectRoot.Manager.Federate");
		int nameHandle = federate.rtiamb.getAttributeHandle( "FederateType", momHandle );
		
		federate.quickSubscribe( momHandle, nameHandle );
		
		////////////////////////////
		// MOM instances creation //
		////////////////////////////
		// wait for a discover for both federates //
		try
		{
			federate.fedamb.waitForLatestDiscovery( momHandle );
			
			// if we get here, we have failed //
			federate.quickResign();
			federate.quickDestroy( "DisabledMomFederation" );
			Assert.fail( "Discovered MOM instance when MOM is meant to be disabled" );
		}
		catch( TimeoutException e )
		{
			// SUCCESS
			federate.quickResign();
			federate.quickDestroy( "DisabledMomFederation" );
		}
	}

	//////////////////////////////////////////////////////////////
	// TEST: (valid) testMomDoesntNotifyNonSubscribedOnResign() //
	//////////////////////////////////////////////////////////////
	/**
	 * This test makes sure that federates who are not subscribed to MOM data don't get notified
	 * of an object deletion when a federate resigns. 
	 */
	@Test
	public void testMomDoesntNotifyNonSubscribedOnResign()
	{
		// join the second
		int secondHandle = secondFederate.quickJoin();
		// get the MOM Federate handle the way Portico generates it
		int expectedHandle = (secondHandle-1) * PorticoConstants.MAX_OBJECTS;

		// resign the second federate and make sure the an object deletion for the
		// MOM object associated with it doesn't get through to the default federate
		secondFederate.quickResign();
		defaultFederate.fedamb.waitForRORemovalTimeout( expectedHandle );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
