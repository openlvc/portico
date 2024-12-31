/*
 *   Copyright 2016 The Portico Project
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
package hlaunit.ieee1516e.mom;

import org.portico.lrc.PorticoConstants;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;
import hlaunit.ieee1516e.common.TestObject;
import hlaunit.ieee1516e.common.TimeoutException;

@Test(singleThreaded=true, groups={"MomFederateLifecycleTest","mom"})
public class MomFederateLifecycleTest extends Abstract1516eTest
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
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		this.secondFederate = new TestFederate( "secondFederate", "secondFederateType", this );
		this.secondFederate.quickConnect();
		
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
	
	@Override
	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////
	// TEST: testMomFederateInstanceLifecycle() //
	//////////////////////////////////////////////
	@Test(enabled=true)
	public void testMomFederateInstanceLifecycle() throws Exception
	{
		// subscribe to the MOM information //
		int momHandle = defaultFederate.quickOCHandle( "HLAobjectRoot.HLAmanager.HLAfederate" );
		int nameHandle = defaultFederate.quickACHandle( momHandle, "HLAfederateName" );
		int typeHandle = defaultFederate.quickACHandle( momHandle, "HLAfederateType" );
		int hostHandle = defaultFederate.quickACHandle( momHandle, "HLAfederateHost" );
		int handleHandle = defaultFederate.quickACHandle( momHandle, "HLAfederateHandle" );
		defaultFederate.quickSubscribe( momHandle, nameHandle, typeHandle, hostHandle, handleHandle );

		////////////////////////////
		// MOM instances creation //
		////////////////////////////
		// wait for a discover for both default and second federates //
		TestObject one = defaultFederate.fedamb.waitForLatestDiscovery( momHandle );
		
		// join the second federate and wait for the MOM instance to be discovered //
		secondFederate.quickJoin();
		TestObject two = defaultFederate.fedamb.waitForLatestDiscovery( momHandle );
		
		// get updates for the attributes //
		defaultFederate.quickProvide( one.getHandle(), nameHandle, typeHandle, hostHandle, handleHandle );
		defaultFederate.quickProvide( two.getHandle(), nameHandle, typeHandle, hostHandle, handleHandle );
		
		// give the other federate a chance to process the provide-request
		secondFederate.quickTick( 0.1, 1.0 );
		
		defaultFederate.fedamb.waitForUpdate( one.getHandle() );
		defaultFederate.fedamb.waitForUpdate( two.getHandle() );
		
		// check the names //
		String oneName = decodeString( one.getAttributes().get(nameHandle) );
		String twoName = decodeString( two.getAttributes().get(nameHandle) );
		Assert.assertEquals( oneName, "defaultFederate" );
		Assert.assertEquals( twoName, "secondFederate" );
		
		// check the types //
		String oneType = decodeString( one.getAttributes().get(typeHandle) );
		String twoType = decodeString( two.getAttributes().get(typeHandle) );
		Assert.assertEquals( oneType, "defaultFederateType" );
		Assert.assertEquals( twoType, "secondFederateType" );
		
		//////////////////////////
		// MOM instance removal //
		//////////////////////////
		// resign the second federate and wait for the removal callback
		secondFederate.quickResign();
		defaultFederate.fedamb.waitForRORemoval( two.getHandle() );
	}
	
	@Test(enabled=true)
	public void testDisabledMom() throws Exception
	{
		// turn the mom off for new federations //
		PorticoConstants.disableMom();
		// create a separate federation //
		TestFederate federate = new TestFederate( "disabledMom", this );
		federate.quickCreate( "DisabledMomFederation" );
		federate.quickJoin( "DisabledMomFederation" );
		// turn the mom back on so it doesn't affect other tests //
		PorticoConstants.enableMom();
		
		// subscribe to the MOM information //
		int momHandle = defaultFederate.quickOCHandle( "HLAobjectRoot.HLAmanager.HLAfederate" );
		int nameHandle = defaultFederate.quickACHandle( "HLAobjectRoot.HLAmanager.HLAfederate",
														"HLAfederateName" );
		int typeHandle = defaultFederate.quickACHandle( "HLAobjectRoot.HLAmanager.HLAfederate",
														"HLAfederateType" );
		
		federate.quickSubscribe( momHandle, nameHandle, typeHandle );
		
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
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
