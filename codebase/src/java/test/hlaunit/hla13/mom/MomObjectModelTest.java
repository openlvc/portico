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

import org.portico.impl.HLAVersion;
import org.portico.lrc.model.Mom;

@Test(singleThreaded=true, groups={"MomObjectModelTest", "mom"})
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

	private int getMomOCHandle( String name )
	{
		return Mom.getMomObjectClassHandle( HLAVersion.HLA13, name );
	}
	
	private int getMomACHandle( int classHandle, String name )
	{
		return Mom.getMomAttributeHandle( HLAVersion.HLA13, classHandle, name );
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	
	/////////////////////////////////
	// TEST: testHla13MomHandles() //
	/////////////////////////////////
	/**
	 * This method will ensure that MOM class and attribute handles can be processed for the
	 * standard HLA 1.3 names.
	 */
	@Test
	public void testHla13MomHandles()
	{
		// get the handles for each type //
		try
		{
			int hManager    = getOCHandle( "ObjectRoot.Manager" );
			Assert.assertEquals( hManager, getMomOCHandle("Manager") );
			
			int hFederate   = getOCHandle( "ObjectRoot.Manager.Federate" );
			Assert.assertEquals( hFederate, getMomOCHandle("Manager.Federate") );

			int hFederation = getOCHandle( "ObjectRoot.Manager.Federation" );
			Assert.assertEquals( hFederation, getMomOCHandle("Manager.Federation") );

			///////////////////////////
			// FEDERATION ATTRIBUTES //
			///////////////////////////
			int handle = -1;
			handle = getACHandle( hFederation, "FederationName" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederation, "FederationName" ),
			                     "Federation.FederationName" );
			
			handle = getACHandle( hFederation, 
			                      "FederatesInFederation" );
			Assert.assertEquals( handle,
			                     getMomACHandle( hFederation, "FederatesInFederation" ),
			                     "Federation.FederatesInFederation" );
			
			handle = getACHandle( hFederation, "RTIversion" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederation, "RTIversion" ),
			                     "Federation.RTIversion" );
			
			handle = getACHandle( hFederation, "FEDid" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederation, "FEDid" ),
			                     "Federation.FEDid" );
			
			handle = getACHandle( hFederation, "LastSaveName" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederation, "LastSaveName" ),
			                     "Federation.LastSaveName" );
			
			handle = getACHandle( hFederation, "LastSaveTime" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederation, "LastSaveTime" ),
			                     "Federation.LastSaveTime" );
			
			handle = getACHandle( hFederation, "NextSaveName" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederation, "NextSaveName" ),
			                     "Federation.NextSaveName" );
			
			handle = getACHandle( hFederation, "NextSaveTime" );
			Assert.assertEquals( handle,
			                     getMomACHandle( hFederation, "NextSaveTime" ),
			                     "Federation.NextSaveTime" );

			/////////////////////////
			// FEDERATE ATTRIBUTES //
			/////////////////////////
			handle = getACHandle( hFederate, "FederateHandle" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "FederateHandle" ),
			                     "Federate.FederateHandle" );
			
			handle = getACHandle( hFederate, "FederateType" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "FederateType" ),
			                     "Federate.FederateType" );
			
			handle = getACHandle( hFederate, "FederateHost" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "FederateHost" ),
			                     "Federate.FederateHost" );
			
			handle = getACHandle( hFederate, "RTIversion" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "RTIversion" ),
			                     "Federate.RTIversion" );
			
			handle = getACHandle( hFederate, "FEDid" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "FEDid" ),
			                     "Federate.FEDid" );
			
			handle = getACHandle( hFederate, "TimeConstrained" );
			Assert.assertEquals( handle,
			                     getMomACHandle( hFederate, "TimeConstrained" ),
			                     "Federate.TimeConstrained" );
			
			handle = getACHandle( hFederate, "TimeRegulating" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "TimeRegulating" ),
			                     "Federate.TimeRegulating" );
			
			handle = getACHandle( hFederate, "AsynchronousDelivery" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "AsynchronousDelivery" ),
			                     "Federate.AsynchronousDelivery" );
			
			handle = getACHandle( hFederate, "FederateState" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "FederateState" ),
			                     "Federate.FederateState" );
			
			handle = getACHandle( hFederate, "TimeManagerState" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "TimeManagerState" ),
			                     "Federate.TimeManagerState" );
			
			handle = getACHandle( hFederate, "FederateTime" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "FederateTime" ),
			                     "Federate.FederateTime" );
			
			handle = getACHandle( hFederate, "Lookahead" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "Lookahead" ),
			                     "Federate.Lookahead" );
			
			handle = getACHandle( hFederate, "LBTS" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "LBTS" ),
			                     "Federate.LBTS" );
			
			handle = getACHandle( hFederate, "MinNextEventTime" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "MinNextEventTime" ),
			                     "Federate.MinNextEventTime" );
			
			handle = getACHandle( hFederate, "ROlength" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "ROlength" ),
			                     "Federate.ROlength" );
			
			handle = getACHandle( hFederate, "TSOlength" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "TSOlength" ),
			                     "Federate.TSOlength" );
			
			handle = getACHandle( hFederate, "ReflectionsReceived" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "ReflectionsReceived" ),
			                     "Federate.ReflectionsReceived" );
			
			handle = getACHandle( hFederate, "UpdatesSent" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "UpdatesSent" ),
			                     "Federate.UpdatesSent" );
			
			handle = getACHandle( hFederate, "InteractionsReceived" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "InteractionsReceived" ),
			                     "Federate.InteractionsReceived" );
			
			handle = getACHandle( hFederate, "InteractionsSent" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "InteractionsSent" ),
			                     "Federate.InteractionsSent" );
			
			handle = getACHandle( hFederate, "ObjectsOwned" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "ObjectsOwned" ),
			                     "Federate.ObjectsOwned" );
			
			handle = getACHandle( hFederate, "ObjectsUpdated" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "ObjectsUpdated" ),
			                     "Federate.ObjectsUpdated" );
			
			handle = getACHandle( hFederate, "ObjectsReflected" );
			Assert.assertEquals( handle, 
			                     getMomACHandle( hFederate, "ObjectsReflected" ),
			                     "Federate.ObjectsReflected" );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching MOM handles: " + e.getMessage(), e );
		}
	}
	
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
