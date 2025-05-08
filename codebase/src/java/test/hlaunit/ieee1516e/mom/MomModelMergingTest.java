/*
 *   Copyright 2021 The Portico Project
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

import java.net.URL;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;

@Test(singleThreaded=true, groups={"MomModelMergingTest","mom"})
public class MomModelMergingTest extends Abstract1516eTest
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

	//////////////////////////////////////////////////////////////////////////////////////////
	///   Lifecycle Methods   ////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeClass(alwaysRun=true)
	public void beforeClass()
	{
		super.beforeClass();
		this.secondFederate = new TestFederate( "secondFederate", this );
		this.secondFederate.quickConnect();
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		//defaultFederate.quickCreate();
		//defaultFederate.quickJoin();
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
	///   Test Methods   /////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	/////////////////////////////////////////////
	// TEST: testMultipleFederatesMergingMim() //
	/////////////////////////////////////////////
	/**
	 * We want to make sure that multiple federates can specify and merge the MIM as part of
	 * their setup process.
	 */
	@Test
	public void testMultipleFederatesMergingMim()
	{
		// Testing options
		//URL[] createModules = toUrlArray( "etc/ieee1516e/HLAstandardMIM.xml" );
		//URL[] joinModules = toUrlArray( "etc/ieee1516e/HLAstandardMIM.xml",
		//                                "fom/ieee1516e/testfom.xml" );
		//URL[] joinModules = toUrlArray( "fom/ieee1516e/testfom.xml" );
		
		// Create with a base FOM, noting that MIM should be automatically added
		// When joining, we shouldn't get merge errors
		URL[] createModules = toUrlArray( "fom/ieee1516e/testfom.xml" );
		URL[] joinModules = toUrlArray( "etc/ieee1516e/HLAstandardMIM.xml",
		                                "fom/ieee1516e/testfom.xml" );
		
		// Create and join
		defaultFederate.quickCreateWithModules( createModules );
		defaultFederate.quickJoin();
		//defaultFederate.quickJoin();
		int handleA = defaultFederate.quickICHandle( "HLAmanager.HLAfederate.HLAadjust.HLAsetSwitches" );
		
		// Try to join the second federate the with the default MIM
		secondFederate.quickJoinWithModules( joinModules );
		int handleB = secondFederate.quickICHandle( "HLAmanager.HLAfederate.HLAadjust.HLAsetSwitches" );
		
		//System.out.println( "handleA="+handleA+", handleB="+handleB );
	}
	
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
//	public static void main( String[] args ) throws Exception
//	{
//		CommonSetup.commonBeforeSuiteSetup();
//
//		MomModelMergingTest test = new MomModelMergingTest();
//		test.beforeClass();
//		test.beforeMethod();
//		test.testMultipleFederatesMergingMim();
//		test.afterMethod();
//		test.afterClass();
//		
//		CommonSetup.commonAfterSuiteCleanup();
//	}
}
