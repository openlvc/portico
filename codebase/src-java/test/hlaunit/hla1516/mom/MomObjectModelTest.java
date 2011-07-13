/*
 *   Copyright 2007 The Portico Project
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
package hlaunit.hla1516.mom;


import hlaunit.hla1516.common.Abstract1516Test;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.portico.lrc.model.Mom;

@Test(sequential=true, groups={"MomObjectModelTest", "mom"})
public class MomObjectModelTest extends Abstract1516Test
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

		// create a federation that we can test with //
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
	}
	
	@Override
	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();

		// destroy the federation that we are working in //
		defaultFederate.quickDestroy();
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		defaultFederate.quickResign();
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
	public void testHla1516MomHandles()
	{
		// get the handles for each type //
		try
		{
			int hManager    = getOCHandle( "HLAobjectRoot.HLAmanager" );
			Assert.assertEquals( hManager, Mom.ManagerClass );
			
			int hFederate   = getOCHandle( "HLAobjectRoot.HLAmanager.Federate" );
			Assert.assertEquals( hFederate, Mom.FederateClass );

			int hFederation = getOCHandle( "HLAobjectRoot.HLAmanager.Federation" );
			Assert.assertEquals( hFederation, Mom.FederationClass );

			///////////////////////////
			// FEDERATION ATTRIBUTES //
			///////////////////////////
			int handle = -1;
			handle = getACHandle( hFederation, "HLAfederationName" );
			Assert.assertEquals( handle, Mom.Federation.FederationName.handle,
			                     "Federation.HLAfederationName" );
			
			handle = getACHandle( hFederation, "HLAfederatesInFederation" );
			Assert.assertEquals( handle, Mom.Federation.FederatesInFederation.handle,
			                     "Federation.HLAfederatesInFederation" );
			
			handle = getACHandle( hFederation, "HLARTIversion" );
			Assert.assertEquals( handle, Mom.Federation.RtiVersion.handle,
			                     "Federation.HLARTIversion" );
			
			handle = getACHandle( hFederation, "HLAFDDID" );
			Assert.assertEquals( handle, Mom.Federation.FedID.handle,
			                     "Federation.HLAFDDID" );
			
			handle = getACHandle( hFederation, "HLAlastSaveName" );
			Assert.assertEquals( handle, Mom.Federation.LastSaveName.handle,
			                     "Federation.HLAlastSaveName" );
			
			handle = getACHandle( hFederation, "HLAlastSaveTime" );
			Assert.assertEquals( handle, Mom.Federation.LastSaveTime.handle,
			                     "Federation.HLAlastSaveTime" );
			
			handle = getACHandle( hFederation, "HLAnextSaveName" );
			Assert.assertEquals( handle, Mom.Federation.NextSaveName.handle,
			                     "Federation.HLAnextSaveName" );
			
			handle = getACHandle( hFederation, "HLAnextSaveTime" );
			Assert.assertEquals( handle, Mom.Federation.NextSaveTime.handle,
			                     "Federation.HLAnextSaveTime" );

			/////////////////////////
			// FEDERATE ATTRIBUTES //
			/////////////////////////
			handle = getACHandle( hFederate, "HLAfederateHandle" );
			Assert.assertEquals( handle, Mom.Federate.FederateHandle.handle,
			                     "Federate.HLAfederateHandle" );
			
			handle = getACHandle( hFederate, "HLAfederateType" );
			Assert.assertEquals( handle, Mom.Federate.FederateType.handle,
			                     "Federate.HLAfederateType" );
			
			handle = getACHandle( hFederate, "HLARTIversion" );
			Assert.assertEquals( handle, Mom.Federate.RtiVersion.handle,
			                     "Federate.HLARTIversion" );
			
			handle = getACHandle( hFederate, "HLAFDDID" );
			Assert.assertEquals( handle, Mom.Federate.FedID.handle,
			                     "Federate.HLAFDDID" );
			
			handle = getACHandle( hFederate, "HLAtimeConstrained" );
			Assert.assertEquals( handle, Mom.Federate.TimeConstrained.handle,
			                     "Federate.HLAtimeConstrained" );
			
			handle = getACHandle( hFederate, "HLAtimeRegulating" );
			Assert.assertEquals( handle, Mom.Federate.TimeRegulating.handle,
			                     "Federate.HLAtimeRegulating" );
			
			handle = getACHandle( hFederate, "HLAasynchronousDelivery" );
			Assert.assertEquals( handle, Mom.Federate.AsynchronousDelivery.handle,
			                     "Federate.HLAasynchronousDelivery" );
			
			handle = getACHandle( hFederate, "HLAfederateState" );
			Assert.assertEquals( handle, Mom.Federate.FederateState.handle,
			                     "Federate.HLAfederateState" );
			
			handle = getACHandle( hFederate, "HLAtimeManagerState" );
			Assert.assertEquals( handle, Mom.Federate.TimeManagerState.handle,
			                     "Federate.HLAtimeManagerState" );
			
			handle = getACHandle( hFederate, "HLAlogicalTime" );
			Assert.assertEquals( handle, Mom.Federate.LogicalTime.handle,
			                     "Federate.HLAlogicalTime" );
			
			handle = getACHandle( hFederate, "HLAlookahead" );
			Assert.assertEquals( handle, Mom.Federate.Lookahead.handle,
			                     "Federate.Lookahead" );
			
			handle = getACHandle( hFederate, "HLAROlength" );
			Assert.assertEquals( handle, Mom.Federate.ROlength.handle,
			                     "Federate.HLAROlength" );
			
			handle = getACHandle( hFederate, "HLATSOlength" );
			Assert.assertEquals( handle, Mom.Federate.TSOlength.handle,
			                     "Federate.HLATSOlength" );
			
			handle = getACHandle( hFederate, "HLAreflectionsReceived" );
			Assert.assertEquals( handle, Mom.Federate.ReflectionsReceived.handle,
			                     "Federate.HLAreflectionsReceived" );
			
			handle = getACHandle( hFederate, "HLAupdatesSent" );
			Assert.assertEquals( handle, Mom.Federate.UpdatesSent.handle,
			                     "Federate.HLAupdatesSent" );
			
			handle = getACHandle( hFederate, "HLAinteractionsReceived" );
			Assert.assertEquals( handle, Mom.Federate.InteractionsReceived.handle,
			                     "Federate.HLAinteractionsReceived" );
			
			handle = getACHandle( hFederate, "HLAinteractionsSent" );
			Assert.assertEquals( handle, Mom.Federate.InteractionsSent.handle,
			                     "Federate.HLAinteractionsSent" );
			
			handle = getACHandle( hFederate, "HLAobjectInstancesThatCanBeDeleted" );
			Assert.assertEquals( handle, Mom.Federate.ObjectInstancesThatCanBeDeleted.handle,
			                     "Federate.HLAobjectInstancesThatCanBeDeleted" );
			
			handle = getACHandle( hFederate, "HLAobjectInstancesUpdated" );
			Assert.assertEquals( handle, Mom.Federate.ObjectInstancesUpdated.handle,
			                     "Federate.HLAobjectInstancessUpdated" );
			
			handle = getACHandle( hFederate, "HLAobjectInstancesReflected" );
			Assert.assertEquals( handle, Mom.Federate.ObjectInstancesReflected.handle,
			                     "Federate.HLAobjectInstancesReflected" );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching MOM handles: " + e.getMessage(), e );
		}
	}
	
	private int getOCHandle( String name ) throws Exception
	{
		return defaultFederate.quickOCHandle( name );
	}
	
	private int getACHandle( int whichClass, String name ) throws Exception
	{
		return defaultFederate.quickACHandle( whichClass, name );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
