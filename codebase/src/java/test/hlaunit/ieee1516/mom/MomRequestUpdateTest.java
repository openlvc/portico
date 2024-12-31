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
package hlaunit.ieee1516.mom;

import hlaunit.ieee1516.common.Abstract1516Test;
import hlaunit.ieee1516.common.TestFederate;
import hlaunit.ieee1516.common.TestObject;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * This class tests that all "provide update" tests work for MOM related attributes and objects.
 * These requests, when made, should not be delivered to the user, rather, the LRC should intercept
 * them and provide the right information.
 */
@Test(singleThreaded=true, groups={"MomRequestUpdateTest", "mom"})
public class MomRequestUpdateTest extends Abstract1516Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate secondFederate;
	private int federateMomHandle;
	private int federateTypeMomHandle;
	private int federationMomHandle;
	private int federationNameMomHandle;

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
		secondFederate.quickJoin();

		this.federateMomHandle = defaultFederate.quickOCHandle( "HLAmanager.HLAfederate" );
		this.federateTypeMomHandle =
		    defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAfederateType" );

		this.federationMomHandle = defaultFederate.quickOCHandle( "HLAmanager.HLAfederation" );
		this.federationNameMomHandle =
		    defaultFederate.quickACHandle( "HLAmanager.HLAfederation", "HLAfederationName" );
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

	////////////////////////////////////////////////
	// TEST: testRequestObjectUpdateForFederate() //
	////////////////////////////////////////////////
	@Test
	public void testRequestObjectUpdateForFederate()
	{
		// have the second federate discover the federate objects through subscription
		secondFederate.quickSubscribe( "Manager.Federate", "HLAfederateType" );
		TestObject one = secondFederate.fedamb.waitForLatestDiscovery( federateMomHandle );
		TestObject two = secondFederate.fedamb.waitForLatestDiscovery( federateMomHandle );

		// have the second federate request an object update
		secondFederate.quickProvide( one.getHandle(), federateTypeMomHandle );
		secondFederate.quickProvide( two.getHandle(), federateTypeMomHandle );

		// give the default federate time to respond
		defaultFederate.quickTick( 0.1, 1.0 );

		// make sure the updates come through
		secondFederate.fedamb.waitForUpdate( one.getHandle() );
		secondFederate.fedamb.waitForUpdate( two.getHandle() );
		String oneName = decodeString( one.getAttributes().get( federateTypeMomHandle ) );
		String twoName = decodeString( two.getAttributes().get( federateTypeMomHandle ) );
		if( !oneName.equals( "defaultFederate" ) && !oneName.equals( "secondFederate" ) )
			Assert.fail( "Expected name to be defaultFederate or secondFedeate, was: " + oneName );
		if( !twoName.equals( "defaultFederate" ) && !twoName.equals( "secondFederate" ) )
			Assert.fail( "Expected name to be defaultFederate or secondFedeate, was: " + twoName );
	}

	//////////////////////////////////////////////////
	// TEST: testRequestObjectUpdateForFederation() //
	//////////////////////////////////////////////////
	@Test
	public void testRequestObjectUpdateForFederation()
	{
		defaultFederate.quickSubscribe( "Manager.Federation", "HLAfederationName" );
		TestObject one = defaultFederate.fedamb.waitForLatestDiscovery( federationMomHandle );
		defaultFederate.quickProvide( one.getHandle(), federationNameMomHandle );
		defaultFederate.fedamb.waitForUpdate( one.getHandle() );
		String name = decodeString( one.getAttributes().get( federationNameMomHandle ) );
		Assert.assertEquals( name, "MomRequestUpdateTest" );
	}

	////////////////////////////////////////////////
	// TEST: testRequestClassUpdateForFederates() //
	////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateForFederates()
	{
		// have the second federate discover the federate objects through subscription
		secondFederate.quickSubscribe( "Manager.Federate", "HLAfederateType" );
		TestObject one = secondFederate.fedamb.waitForLatestDiscovery( federateMomHandle );
		TestObject two = secondFederate.fedamb.waitForLatestDiscovery( federateMomHandle );

		// have the second federate request an object update
		secondFederate.quickProvideClass( federateMomHandle, federateTypeMomHandle );

		// give the default federate time to respond
		defaultFederate.quickTick( 0.1, 1.0 );

		// make sure the updates come through
		secondFederate.fedamb.waitForUpdate( one.getHandle() );
		secondFederate.fedamb.waitForUpdate( two.getHandle() );
		String oneName = decodeString( one.getAttributes().get( federateTypeMomHandle ) );
		String twoName = decodeString( two.getAttributes().get( federateTypeMomHandle ) );
		if( !oneName.equals( "defaultFederate" ) && !oneName.equals( "secondFederate" ) )
			Assert.fail( "Expected name to be defaultFederate or secondFedeate, was: " + oneName );
		if( !twoName.equals( "defaultFederate" ) && !twoName.equals( "secondFederate" ) )
			Assert.fail( "Expected name to be defaultFederate or secondFedeate, was: " + twoName );
	}

	/////////////////////////////////////////////////
	// TEST: testRequestClassUpdateForFederation() //
	/////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateForFederation()
	{
		defaultFederate.quickSubscribe( "Manager.Federation", "HLAfederationName" );
		TestObject one = defaultFederate.fedamb.waitForLatestDiscovery( federationMomHandle );
		defaultFederate.quickProvideClass( federationMomHandle, federationNameMomHandle );
		defaultFederate.fedamb.waitForUpdate( one.getHandle() );
		String name = decodeString( one.getAttributes().get( federationNameMomHandle ) );
		Assert.assertEquals( name, "MomRequestUpdateTest" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
