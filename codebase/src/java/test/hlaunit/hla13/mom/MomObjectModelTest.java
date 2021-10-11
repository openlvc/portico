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


import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import hlaunit.hla13.common.Abstract13Test;

@Test(sequential=true, groups={"MomObjectModelTest", "mom"})
public class MomObjectModelTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

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
		// create a federation that we can test with //
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
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

	///////////////////////////////////////////////
	// TEST: testHla13MomWithPrivilegeToDelete() //
	///////////////////////////////////////////////
	/**
	 * This tests that the privilegeToDelete handle is accessible through the MOM types
	 */
	@Test
	public void testHla13MomWithPrivilegeToDelete()
	{
		/*
			int hManager    = getOCHandle( "ObjectRoot.Manager" );
			Assert.assertEquals( hManager, MOMHelper.ManagerClass );
			
			int hFederate   = getOCHandle( "ObjectRoot.Manager.Federate" );
			Assert.assertEquals( hFederate, MOMHelper.FederateClass );

			int hFederation = getOCHandle( "ObjectRoot.Manager.Federation" );
			Assert.assertEquals( hFederation, MOMHelper.FederationClass );
		*/
		try
		{
			// make sure it doesn't error out
			int hManager = defaultFederate.rtiamb.getObjectClassHandle( "Manager" );
			defaultFederate.rtiamb.getAttributeHandle( "privilegeToDelete", hManager );
		}
		catch( Exception e )
		{
			unexpectedException( "Getting privToDelete handle through Manager", e );
		}

		try
		{
			// make sure it doesn't error out
			int hFederate = defaultFederate.rtiamb.getObjectClassHandle( "Manager.Federate" );
			defaultFederate.rtiamb.getAttributeHandle( "privilegeToDelete", hFederate );
		}
		catch( Exception e )
		{
			unexpectedException( "Getting privToDelete handle through Manager.Federate", e );
		}

		try
		{
			// make sure it doesn't error out
			int hFederation = defaultFederate.rtiamb.getObjectClassHandle( "Manager.Federation" );
			defaultFederate.rtiamb.getAttributeHandle( "privilegeToDelete", hFederation );
		}
		catch( Exception e )
		{
			unexpectedException( "Getting privToDelete handle through Manager.Federation", e );
		}
	}

	////////////////////////////////////////////////
	// TEST: (valid) testHla13MomClassNameFetch() //
	////////////////////////////////////////////////
	/**
	 * This method will validate that the proper names are returned when querying the RTI about
	 * object class names using the handles of MOM objects.
	 * <p/>
	 * This test is needed because internally, the MOM information is stored according to the 
	 * 1516 names.
	 */
	@Test
	public void testHla13MomClassNameFetch()
	{
		// test for ObjectRoot.Manager
		int managerHandle = defaultFederate.quickOCHandle( "Manager" );
		String managerName = defaultFederate.quickOCName( managerHandle );
		Assert.assertEquals( managerName, "ObjectRoot.Manager" );

		// test for ObjectRoot.Manager.Federate
		int federateHandle = defaultFederate.quickOCHandle( "Manager.Federate" );
		String federateName = defaultFederate.quickOCName( federateHandle );
		Assert.assertEquals( federateName, "ObjectRoot.Manager.Federate" );
		
		// test for ObjectRoot.Manager.Federation
		int federationHandle = defaultFederate.quickOCHandle( "Manager.Federation" );
		String federationName = defaultFederate.quickOCName( federationHandle );
		Assert.assertEquals( federationName, "ObjectRoot.Manager.Federation" );
	}
	
	////////////////////////////////////////////////////
	// TEST: (valid) testHla13MomAttributeNameFetch() //
	////////////////////////////////////////////////////
	/**
	 * This method will validate that the proper names are returned when querying the RTI about
	 * attribute names using the handles of MOM objects.
	 * <p/>
	 * This test is needed because internally, the MOM information is stored according to the 
	 * 1516 names.
	 */
	@Test
	public void testHla13MomAttributeNameFetch()
	{
		// test to make sure that the names of attributes are in the correct format
		// just check one for now
		int classHandle = defaultFederate.quickOCHandle( "Manager.Federation" );
		int attributeHandle = defaultFederate.quickACHandle( "Manager.Federation", "RTIversion" );
		String attributeName = defaultFederate.quickACName( attributeHandle, classHandle );
		Assert.assertEquals( attributeName, "RTIversion" );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Private Helper Methods /////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	private int getOCHandle( String name ) throws Exception
	{
		return defaultFederate.rtiamb.getObjectClassHandle( name );
	}
	
	private int getACHandle( int whichClass, String name ) throws Exception
	{
		return defaultFederate.rtiamb.getAttributeHandle( name, whichClass );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
