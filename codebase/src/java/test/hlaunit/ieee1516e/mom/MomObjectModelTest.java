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

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import hlaunit.ieee1516e.common.Abstract1516eTest;

import org.portico.impl.HLAVersion;
import org.portico.lrc.model.Mom;

@Test(singleThreaded=true, groups={"MomObjectModelTest", "mom"})
public class MomObjectModelTest extends Abstract1516eTest
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
		// destroy the federation that we are working in //
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
		return Mom.getMomObjectClassHandle( HLAVersion.IEEE1516e, name );
	}
	
	private int getMomACHandle( int classHandle, String name )
	{
		return Mom.getMomAttributeHandle( HLAVersion.IEEE1516e, classHandle, name );
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////
	// TEST: testHla1516eMomHandles() //
	////////////////////////////////////
	/**
	 * Confirm that we can access MOM class and attribute handles.
	 */
	@Test(enabled=true)
	public void testHla1516eMomHandles()
	{
		// get the handles for each type //
		try
		{
			int hManager = getOCHandle( "HLAobjectRoot.HLAmanager" );
			Assert.assertEquals( hManager, getMomOCHandle("HLAmanager") );
			
			int hFederate = getOCHandle( "HLAobjectRoot.HLAmanager.HLAfederate" );
			Assert.assertEquals( hFederate, getMomOCHandle("HLAmanager.HLAfederate") );

			int hFederation = getOCHandle( "HLAobjectRoot.HLAmanager.HLAfederation" );
			Assert.assertEquals( hFederation, getMomOCHandle("HLAmanager.HLAfederation") );

			///////////////////////////
			// FEDERATION ATTRIBUTES //
			///////////////////////////
			int handle = -1;
			handle = getACHandle( hFederation, "HLAfederationName" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederation, "HLAfederationName"),
			                     "Federation.HLAfederationName" );
			
			handle = getACHandle( hFederation, "HLAfederatesInFederation" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederation, "HLAfederatesInFederation"),
			                     "Federation.HLAfederatesInFederation" );
			
			handle = getACHandle( hFederation, "HLARTIversion" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederation, "HLARTIversion"),
			                     "Federation.HLARTIversion" );
			
			handle = getACHandle( hFederation, "HLAcurrentFDD" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederation, "HLAcurrentFDD"),
			                     "Federation.HLAcurrentFDD" );
			
			handle = getACHandle( hFederation, "HLAlastSaveName" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederation, "HLAlastSaveName"),
			                     "Federation.HLAlastSaveName" );
			
			handle = getACHandle( hFederation, "HLAlastSaveTime" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederation, "HLAlastSaveTime"),
			                     "Federation.HLAlastSaveTime" );
			
			handle = getACHandle( hFederation, "HLAnextSaveName" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederation, "HLAnextSaveName"),
			                     "Federation.HLAnextSaveName" );
			
			handle = getACHandle( hFederation, "HLAnextSaveTime" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederation, "HLAnextSaveTime"),
			                     "Federation.HLAnextSaveTime" );

			/////////////////////////
			// FEDERATE ATTRIBUTES //
			/////////////////////////
			handle = getACHandle( hFederate, "HLAfederateHandle" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLAfederateHandle"),
			                     "Federate.HLAfederateHandle" );
			
			handle = getACHandle( hFederate, "HLAfederateType" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLAfederateType"),
			                     "Federate.HLAfederateType" );
			
			handle = getACHandle( hFederate, "HLARTIversion" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLARTIversion"),
			                     "Federate.HLARTIversion" );
			
			handle = getACHandle( hFederate, "HLAtimeConstrained" );
			Assert.assertEquals( handle,
			                     getMomACHandle(hFederate, "HLAtimeConstrained"),
			                     "Federate.HLAtimeConstrained" );
			
			handle = getACHandle( hFederate, "HLAtimeRegulating" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLAtimeRegulating"),
			                     "Federate.HLAtimeRegulating" );
			
			handle = getACHandle( hFederate, "HLAasynchronousDelivery" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLAasynchronousDelivery"),
			                     "Federate.HLAasynchronousDelivery" );
			
			handle = getACHandle( hFederate, "HLAfederateState" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLAfederateState"),
			                     "Federate.HLAfederateState" );
			
			handle = getACHandle( hFederate, "HLAtimeManagerState" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLAtimeManagerState"),
			                     "Federate.HLAtimeManagerState" );
			
			handle = getACHandle( hFederate, "HLAlogicalTime" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLAlogicalTime"),
			                     "Federate.HLAlogicalTime" );
			
			handle = getACHandle( hFederate, "HLAlookahead" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLAlookahead"),
			                     "Federate.HLAlookahead" );
			
			handle = getACHandle( hFederate, "HLAROlength" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLAROlength"),
			                     "Federate.HLAROlength" );
			
			handle = getACHandle( hFederate, "HLATSOlength" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLATSOlength"),
			                     "Federate.HLATSOlength" );
			
			handle = getACHandle( hFederate, "HLAreflectionsReceived" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLAreflectionsReceived"),
			                     "Federate.HLAreflectionsReceived" );
			
			handle = getACHandle( hFederate, "HLAupdatesSent" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLAupdatesSent"),
			                     "Federate.HLAupdatesSent" );
			
			handle = getACHandle( hFederate, "HLAinteractionsReceived" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLAinteractionsReceived"),
			                     "Federate.HLAinteractionsReceived" );
			
			handle = getACHandle( hFederate, "HLAinteractionsSent" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLAinteractionsSent"),
			                     "Federate.HLAinteractionsSent" );
			
			handle = getACHandle( hFederate, "HLAobjectInstancesThatCanBeDeleted" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLAobjectInstancesThatCanBeDeleted"),
			                     "Federate.HLAobjectInstancesThatCanBeDeleted" );
			
			handle = getACHandle( hFederate, "HLAobjectInstancesUpdated" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLAobjectInstancesUpdated"),
			                     "Federate.HLAobjectInstancessUpdated" );
			
			handle = getACHandle( hFederate, "HLAobjectInstancesReflected" );
			Assert.assertEquals( handle, 
			                     getMomACHandle(hFederate, "HLAobjectInstancesReflected"),
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
