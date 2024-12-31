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


import java.util.HashMap;

import hla.rti.AttributeHandleSet;
import hla.rti.jlc.EncodingHelpers;

import org.portico.impl.HLAVersion;
import org.portico.lrc.model.Mom;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Instance;

@Test(singleThreaded=true, groups={"MomFederationTest", "mom"})
public class MomFederationTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int chFederation;
	private int ahFederationName;
	private int ahFederatesInFederation;
	private int ahRTIversion;
	private int ahFEDid;
	private int ahLastSaveName;
	private int ahLastSaveTime;
	private int ahNextSaveName;
	private int ahNextSaveTime;
	private int[] aHandles;
	
	private int chFederate;
	private int bhFederateHandle;
	private int bhFederateType;
	private int bhFederateHost;
	private int bhRTIversion;
	private int bhFEDid;
	private int bhTimeConstrained;
	private int bhTimeRegulating;
	private int bhAsynchronousDelivery;
	private int bhFederateState;
	private int bhTimeManagerState;
	private int bhFederateTime;
	private int bhLookahead;
	private int bhLBTS;
	private int bhMinNextEventTime;
	private int bhROlength;
	private int bhTSOlength;
	private int bhReflectionsReceived;
	private int bhUpdatesSent;
	private int bhInteractionsReceived;
	private int bhInteractionsSent;
	private int bhObjectsOwned;
	private int bhObjectsUpdated;
	private int bhObjectsReflected;
	private int[] bHandles;
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
		try
		{
			// get federation handles
			chFederation = defaultFederate.rtiamb.getObjectClassHandle( "ObjectRoot.Manager.Federation" );
			ahFederationName = defaultFederate.rtiamb.getAttributeHandle( "FederationName", chFederation );
			ahFederatesInFederation = defaultFederate.rtiamb.getAttributeHandle( "FederatesInFederation", chFederation );
			ahRTIversion = defaultFederate.rtiamb.getAttributeHandle( "RTIversion", chFederation );
			ahFEDid = defaultFederate.rtiamb.getAttributeHandle( "FEDid", chFederation );
			ahLastSaveName = defaultFederate.rtiamb.getAttributeHandle( "LastSaveName", chFederation );
			ahLastSaveTime = defaultFederate.rtiamb.getAttributeHandle( "LastSaveTime", chFederation );
			ahNextSaveName = defaultFederate.rtiamb.getAttributeHandle( "NextSaveName", chFederation );
			ahNextSaveTime = defaultFederate.rtiamb.getAttributeHandle( "NextSaveTime", chFederation );
			aHandles = new int[] { ahFederationName, 
				                   ahFederatesInFederation,
				                   ahRTIversion,
				                   ahFEDid,
				                   ahLastSaveName,
				                   ahLastSaveTime,
				                   ahNextSaveName,
				                   ahNextSaveTime };
			
			// get federate handles
			chFederate = defaultFederate.rtiamb.getObjectClassHandle( "ObjectRoot.Manager.Federate" );
			bhFederateHandle = defaultFederate.rtiamb.getAttributeHandle( "FederateHandle", chFederate );
			bhFederateType = defaultFederate.rtiamb.getAttributeHandle( "FederateType", chFederate );
			bhFederateHost = defaultFederate.rtiamb.getAttributeHandle( "FederateHost", chFederate );
			bhRTIversion = defaultFederate.rtiamb.getAttributeHandle( "RTIversion", chFederate );
			bhFEDid = defaultFederate.rtiamb.getAttributeHandle( "FEDid", chFederate );
			bhTimeConstrained = defaultFederate.rtiamb.getAttributeHandle( "TimeConstrained", chFederate );
			bhTimeRegulating = defaultFederate.rtiamb.getAttributeHandle( "TimeRegulating", chFederate );
			bhAsynchronousDelivery = defaultFederate.rtiamb.getAttributeHandle( "AsynchronousDelivery", chFederate );
			bhFederateState = defaultFederate.rtiamb.getAttributeHandle( "FederateState", chFederate );
			bhTimeManagerState = defaultFederate.rtiamb.getAttributeHandle( "TimeManagerState", chFederate );
			bhFederateTime = defaultFederate.rtiamb.getAttributeHandle( "FederateTime", chFederate );
			bhLookahead = defaultFederate.rtiamb.getAttributeHandle( "Lookahead", chFederate );
			bhLBTS = defaultFederate.rtiamb.getAttributeHandle( "LBTS", chFederate );
			bhMinNextEventTime = defaultFederate.rtiamb.getAttributeHandle( "MinNextEventTime", chFederate );
			bhROlength = defaultFederate.rtiamb.getAttributeHandle( "ROlength", chFederate );
			bhTSOlength = defaultFederate.rtiamb.getAttributeHandle( "TSOlength", chFederate );
			bhReflectionsReceived = defaultFederate.rtiamb.getAttributeHandle( "ReflectionsReceived", chFederate );
			bhUpdatesSent = defaultFederate.rtiamb.getAttributeHandle( "UpdatesSent", chFederate );
			bhInteractionsReceived = defaultFederate.rtiamb.getAttributeHandle( "InteractionsReceived", chFederate );
			bhInteractionsSent = defaultFederate.rtiamb.getAttributeHandle( "InteractionsSent", chFederate );
			bhObjectsOwned = defaultFederate.rtiamb.getAttributeHandle( "ObjectsOwned", chFederate );
			bhObjectsUpdated = defaultFederate.rtiamb.getAttributeHandle( "ObjectsUpdated", chFederate );
			bhObjectsReflected = defaultFederate.rtiamb.getAttributeHandle( "ObjectsReflected", chFederate );
			bHandles = new int[] { bhFederateHandle, 
			                       bhFederateType,
			                       bhFederateHost,
			                       bhRTIversion,
			                       bhFEDid,
			                       bhTimeConstrained,
			                       bhTimeRegulating,
			                       bhAsynchronousDelivery,
			                       bhFederateState,
			                       bhTimeManagerState,
			                       bhFederateTime,
			                       bhLookahead,
			                       bhLBTS,
			                       bhMinNextEventTime,
			                       bhROlength,
			                       bhTSOlength,
			                       bhReflectionsReceived,
			                       bhUpdatesSent,
			                       bhInteractionsReceived,
			                       bhInteractionsSent,
			                       bhObjectsOwned,
			                       bhObjectsUpdated,
			                       bhObjectsReflected };
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching MOM handles" );
		}
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		defaultFederate.quickResign();
		defaultFederate.quickDestroy();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////
	// TEST: testMomFederationInstance() //
	///////////////////////////////////////
	@Test
	public void testMomFederationInstance()
	{
		// subscribe to the MOM Federation class attributes //
		defaultFederate.quickSubscribe( "ObjectRoot.Manager.Federation",
		                                "FederationName",
		                                "FederatesInFederation",
		                                "RTIversion",
		                                "FEDid",
		                                "LastSaveName",
		                                "LastSaveTime",
		                                "NextSaveName",
		                                "NextSaveTime" );

		// wait for a discovery of an instance //
		// need a better way than to assume specific info about handles
		int objectHandle = Mom.getMomObjectClassHandle( HLAVersion.HLA13, "Manager.Federation" );
		defaultFederate.fedamb.waitForDiscovery( objectHandle );
		
		// ask for the MOM to provide an update //
		try
		{
			AttributeHandleSet set = defaultFederate.createAHS( aHandles );
			defaultFederate.rtiamb.requestObjectAttributeValueUpdate( objectHandle, set );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while requesting update: " + e.getMessage(), e );
		}
		
		// wait for the update to come through //
		defaultFederate.fedamb.waitForUpdate( objectHandle );
		
		// get the instance and check the values //
		Test13Instance instance = defaultFederate.fedamb.getInstances().get( objectHandle );
		HashMap<Integer,byte[]> values = instance.getAttributes();

		// check a value //
		Assert.assertEquals( EncodingHelpers.decodeString(values.get(ahFederationName)),
		                     defaultFederate.simpleName,
		                     "Value returned by MOM did not match federation name" );
	}
	
	/////////////////////////////////////
	// TEST: testMomFederateInstance() //
	/////////////////////////////////////
	@Test
	public void testMomFederateInstance()
	{
		// subscirbe to the MOM Federate class attributes //
		defaultFederate.quickSubscribe( "ObjectRoot.Manager.Federate",
		                                "FederateHandle", 
		                                "FederateType", 
		                                "FederateHost", 
		                                "RTIversion", 
		                                "FEDid", 
		                                "TimeConstrained", 
		                                "TimeRegulating", 
		                                "AsynchronousDelivery", 
		                                "FederateState", 
		                                "TimeManagerState", 
		                                "FederateTime", 
		                                "Lookahead", 
		                                "LBTS", 
		                                "MinNextEventTime", 
		                                "ROlength", 
		                                "TSOlength", 
		                                "ReflectionsReceived", 
		                                "UpdatesSent", 
		                                "InteractionsReceived", 
		                                "InteractionsSent", 
		                                "ObjectsOwned", 
		                                "ObjectsUpdated", 
		                                "ObjectsReflected" );
		
		// wait for a discovery of an instance that represents the default federate //
		Test13Instance fedInstance = defaultFederate.fedamb.waitForLatestDiscovery( chFederate );
		int objectHandle = fedInstance.getHandle();
		
		// ask for an update //
		try
		{
			AttributeHandleSet set = defaultFederate.createAHS( bHandles );
			defaultFederate.rtiamb.requestObjectAttributeValueUpdate( objectHandle, set );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while requesting update: " + e.getMessage(), e );
		}
		
		// watif rot eh update //
		defaultFederate.fedamb.waitForUpdate( objectHandle );
		
		// check the values //
		HashMap<Integer,byte[]> values = fedInstance.getAttributes();
		Assert.assertEquals( EncodingHelpers.decodeString(values.get(bhFederateType)),
		                     defaultFederate.federateName,
		                     "Value returned by MOM did not match federate name" );
		String sFederateHandle = EncodingHelpers.decodeString( values.get(bhFederateHandle) );
		Assert.assertEquals( Integer.parseInt(sFederateHandle),
		                     defaultFederate.federateHandle,
		                     "Value returned by MOM did not match federate handle" );
		
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
