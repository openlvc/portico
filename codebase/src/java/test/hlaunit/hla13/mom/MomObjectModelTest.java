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

import org.portico.lrc.model.Mom;

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
			Assert.assertEquals( hManager, Mom.ManagerClass );
			
			int hFederate   = getOCHandle( "ObjectRoot.Manager.Federate" );
			Assert.assertEquals( hFederate, Mom.FederateClass );

			int hFederation = getOCHandle( "ObjectRoot.Manager.Federation" );
			Assert.assertEquals( hFederation, Mom.FederationClass );

			///////////////////////////
			// FEDERATION ATTRIBUTES //
			///////////////////////////
			int handle = -1;
			handle = getACHandle( hFederation, "FederationName" );
			Assert.assertEquals( handle, Mom.Federation.FederationName.handle,
			                     "Federation.FederationName" );
			
			handle = getACHandle( hFederation, "FederatesInFederation" );
			Assert.assertEquals( handle, Mom.Federation.FederatesInFederation.handle,
			                     "Federation.FederatesInFederation" );
			
			handle = getACHandle( hFederation, "RTIversion" );
			Assert.assertEquals( handle, Mom.Federation.RtiVersion.handle,
			                     "Federation.RTIversion" );
			
			handle = getACHandle( hFederation, "FEDid" );
			Assert.assertEquals( handle, Mom.Federation.FedID.handle,
			                     "Federation.FEDid" );
			
			handle = getACHandle( hFederation, "LastSaveName" );
			Assert.assertEquals( handle, Mom.Federation.LastSaveName.handle,
			                     "Federation.LastSaveName" );
			
			handle = getACHandle( hFederation, "LastSaveTime" );
			Assert.assertEquals( handle, Mom.Federation.LastSaveTime.handle,
			                     "Federation.LastSaveTime" );
			
			handle = getACHandle( hFederation, "NextSaveName" );
			Assert.assertEquals( handle, Mom.Federation.NextSaveName.handle,
			                     "Federation.NextSaveName" );
			
			handle = getACHandle( hFederation, "NextSaveTime" );
			Assert.assertEquals( handle, Mom.Federation.NextSaveTime.handle,
			                     "Federation.NextSaveTime" );

			/////////////////////////
			// FEDERATE ATTRIBUTES //
			/////////////////////////
			handle = getACHandle( hFederate, "FederateHandle" );
			Assert.assertEquals( handle, Mom.Federate.FederateHandle.handle,
			                     "Federate.FederateHandle" );
			
			handle = getACHandle( hFederate, "FederateType" );
			Assert.assertEquals( handle, Mom.Federate.FederateType.handle,
			                     "Federate.FederateType" );
			
			handle = getACHandle( hFederate, "FederateHost" );
			Assert.assertEquals( handle, Mom.Federate.FederateHost.handle,
			                     "Federate.FederateHost" );
			
			handle = getACHandle( hFederate, "RTIversion" );
			Assert.assertEquals( handle, Mom.Federate.RtiVersion.handle,
			                     "Federate.RTIversion" );
			
			handle = getACHandle( hFederate, "FEDid" );
			Assert.assertEquals( handle, Mom.Federate.FedID.handle,
			                     "Federate.FEDid" );
			
			handle = getACHandle( hFederate, "TimeConstrained" );
			Assert.assertEquals( handle, Mom.Federate.TimeConstrained.handle,
			                     "Federate.TimeConstrained" );
			
			handle = getACHandle( hFederate, "TimeRegulating" );
			Assert.assertEquals( handle, Mom.Federate.TimeRegulating.handle,
			                     "Federate.TimeRegulating" );
			
			handle = getACHandle( hFederate, "AsynchronousDelivery" );
			Assert.assertEquals( handle, Mom.Federate.AsynchronousDelivery.handle,
			                     "Federate.AsynchronousDelivery" );
			
			handle = getACHandle( hFederate, "FederateState" );
			Assert.assertEquals( handle, Mom.Federate.FederateState.handle,
			                     "Federate.FederateState" );
			
			handle = getACHandle( hFederate, "TimeManagerState" );
			Assert.assertEquals( handle, Mom.Federate.TimeManagerState.handle,
			                     "Federate.TimeManagerState" );
			
			handle = getACHandle( hFederate, "FederateTime" );
			Assert.assertEquals( handle, Mom.Federate.LogicalTime.handle,
			                     "Federate.FederateTime" );
			
			handle = getACHandle( hFederate, "Lookahead" );
			Assert.assertEquals( handle, Mom.Federate.Lookahead.handle,
			                     "Federate.Lookahead" );
			
			handle = getACHandle( hFederate, "LBTS" );
			Assert.assertEquals( handle, Mom.Federate.LBTS.handle,
			                     "Federate.LBTS" );
			
			handle = getACHandle( hFederate, "MinNextEventTime" );
			Assert.assertEquals( handle, Mom.Federate.LITS.handle,
			                     "Federate.MinNextEventTime" );
			
			handle = getACHandle( hFederate, "ROlength" );
			Assert.assertEquals( handle, Mom.Federate.ROlength.handle,
			                     "Federate.ROlength" );
			
			handle = getACHandle( hFederate, "TSOlength" );
			Assert.assertEquals( handle, Mom.Federate.TSOlength.handle,
			                     "Federate.TSOlength" );
			
			handle = getACHandle( hFederate, "ReflectionsReceived" );
			Assert.assertEquals( handle, Mom.Federate.ReflectionsReceived.handle,
			                     "Federate.ReflectionsReceived" );
			
			handle = getACHandle( hFederate, "UpdatesSent" );
			Assert.assertEquals( handle, Mom.Federate.UpdatesSent.handle,
			                     "Federate.UpdatesSent" );
			
			handle = getACHandle( hFederate, "InteractionsReceived" );
			Assert.assertEquals( handle, Mom.Federate.InteractionsReceived.handle,
			                     "Federate.InteractionsReceived" );
			
			handle = getACHandle( hFederate, "InteractionsSent" );
			Assert.assertEquals( handle, Mom.Federate.InteractionsSent.handle,
			                     "Federate.InteractionsSent" );
			
			handle = getACHandle( hFederate, "ObjectsOwned" );
			Assert.assertEquals( handle, Mom.Federate.ObjectInstancesThatCanBeDeleted.handle,
			                     "Federate.ObjectsOwned" );
			
			handle = getACHandle( hFederate, "ObjectsUpdated" );
			Assert.assertEquals( handle, Mom.Federate.ObjectInstancesUpdated.handle,
			                     "Federate.ObjectsUpdated" );
			
			handle = getACHandle( hFederate, "ObjectsReflected" );
			Assert.assertEquals( handle, Mom.Federate.ObjectInstancesReflected.handle,
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
