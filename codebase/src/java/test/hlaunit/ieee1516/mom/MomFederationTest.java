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
package hlaunit.ieee1516.mom;


import java.util.HashMap;

import org.portico.impl.HLAVersion;
import org.portico.lrc.model.Mom;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hlaunit.ieee1516.common.Abstract1516Test;
import hlaunit.ieee1516.common.TestObject;

@Test(singleThreaded=true, groups={"MomFederationTest", "mom"})
public class MomFederationTest extends Abstract1516Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
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
	private int bhRTIversion;
	private int bhFEDid;
	private int bhTimeConstrained;
	private int bhTimeRegulating;
	private int bhAsynchronousDelivery;
	private int bhFederateState;
	private int bhTimeManagerState;
	private int bhFederateTime;
	private int bhLookahead;
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
	public void beforeMethod() throws Exception
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		// get federation handles
		//chFederation = defaultFederate.quickOCHandle( "HLAobjectRoot.HLAmanager.HLAfederation" );
		ahFederationName = defaultFederate.quickACHandle( "HLAmanager.HLAfederation", "HLAfederationName" );
		ahFederationName = defaultFederate.quickACHandle( "HLAmanager.HLAfederation", "HLAfederationName" );
		ahFederatesInFederation = defaultFederate.quickACHandle( "HLAmanager.HLAfederation", "HLAfederatesInFederation" );
		ahRTIversion = defaultFederate.quickACHandle( "HLAmanager.HLAfederation", "HLARTIversion" );
		ahFEDid = defaultFederate.quickACHandle( "HLAmanager.HLAfederation", "HLAFDDID" );
		ahLastSaveName = defaultFederate.quickACHandle( "HLAmanager.HLAfederation", "HLAlastSaveName" );
		ahLastSaveTime = defaultFederate.quickACHandle( "HLAmanager.HLAfederation", "HLAlastSaveTime" );
		ahNextSaveName = defaultFederate.quickACHandle( "HLAmanager.HLAfederation", "HLAnextSaveName" );
		ahNextSaveTime = defaultFederate.quickACHandle( "HLAmanager.HLAfederation", "HLAnextSaveTime" );
		aHandles = new int[] { ahFederationName, 
			                   ahFederatesInFederation,
			                   ahRTIversion,
			                   ahFEDid,
			                   ahLastSaveName,
			                   ahLastSaveTime,
			                   ahNextSaveName,
			                   ahNextSaveTime };
		
		// get federate handles
		chFederate             = defaultFederate.quickOCHandle( "HLAobjectRoot.Manager.Federate" );
		bhFederateHandle       = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAfederateHandle" );
		bhFederateHandle       = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAfederateHandle" );
		bhFederateType         = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAfederateType" );
		bhRTIversion           = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLARTIversion" );
		bhFEDid                = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAFDDID" );
		bhTimeConstrained      = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAtimeConstrained" );
		bhTimeRegulating       = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAtimeRegulating" );
		bhAsynchronousDelivery = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAasynchronousDelivery" );
		bhFederateState        = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAfederateState" );
		bhTimeManagerState     = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAtimeManagerState" );
		bhFederateTime         = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAlogicalTime" );
		bhLookahead            = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAlookahead" );
		bhROlength             = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAROlength" );
		bhTSOlength            = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLATSOlength" );
		bhReflectionsReceived  = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAreflectionsReceived" );
		bhUpdatesSent          = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAupdatesSent" );
		bhInteractionsReceived = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAinteractionsReceived" );
		bhInteractionsSent     = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAinteractionsSent" );
		bhObjectsOwned         = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAobjectInstancesThatCanBeDeleted" );
		bhObjectsUpdated       = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAobjectInstancesUpdated" );
		bhObjectsReflected     = defaultFederate.quickACHandle( "HLAmanager.HLAfederate", "HLAobjectInstancesReflected" );
		bHandles = new int[] { bhFederateHandle, 
		                       bhFederateType,
		                       bhRTIversion,
		                       bhFEDid,
		                       bhTimeConstrained,
		                       bhTimeRegulating,
		                       bhAsynchronousDelivery,
		                       bhFederateState,
		                       bhTimeManagerState,
		                       bhFederateTime,
		                       bhLookahead,
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
		defaultFederate.quickSubscribe( "HLAobjectRoot.HLAmanager.HLAfederation",
		                                "HLAfederationName",
		                                "HLAfederatesInFederation",
		                                "HLARTIversion",
		                                "HLAFDDID",
		                                "HLAlastSaveName",
		                                "HLAlastSaveTime",
		                                "HLAnextSaveName",
		                                "HLAnextSaveTime" );

		// wait for a discovery of an instance //
		// need a better way than to assume specific info about handles
		int objectHandle = Mom.getMomObjectClassHandle( HLAVersion.IEEE1516, "HLAmanager.HLAfederation" );
		defaultFederate.fedamb.waitForDiscovery( objectHandle );
		
		// ask for the MOM to provide an update //
		defaultFederate.quickProvide( objectHandle, aHandles );
		
		// wait for the update to come through //
		defaultFederate.fedamb.waitForUpdate( objectHandle );
		
		// get the instance and check the values //
		TestObject instance = defaultFederate.fedamb.getInstances().get( objectHandle );
		HashMap<Integer,byte[]> values = instance.getAttributes();

		// check a value //
		Assert.assertEquals( decodeString(values.get(ahFederationName)),
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
		defaultFederate.quickSubscribe( "HLAobjectRoot.Manager.Federate",
		                                "HLAfederateHandle", 
		                                "HLAfederateType", 
		                                "HLARTIversion", 
		                                "HLAFDDID", 
		                                "HLAtimeConstrained", 
		                                "HLAtimeRegulating", 
		                                "HLAasynchronousDelivery", 
		                                "HLAfederateState", 
		                                "HLAtimeManagerState", 
		                                "HLAlogicalTime", 
		                                "HLAlookahead", 
		                                "HLAROlength", 
		                                "HLATSOlength", 
		                                "HLAreflectionsReceived", 
		                                "HLAupdatesSent", 
		                                "HLAinteractionsReceived", 
		                                "HLAinteractionsSent", 
		                                "HLAobjectInstancesThatCanBeDeleted", 
		                                "HLAobjectInstancesUpdated", 
		                                "HLAobjectInstancesReflected" );
		
		// wait for a discovery of an instance that represents the default federate //
		TestObject fedInstance = defaultFederate.fedamb.waitForLatestDiscovery( chFederate );
		int objectHandle = fedInstance.getHandle();
		
		// ask for an update //
		defaultFederate.quickProvide( objectHandle, bHandles );
		
		// watif rot eh update //
		defaultFederate.fedamb.waitForUpdate( objectHandle );
		
		// check the values //
		HashMap<Integer,byte[]> values = fedInstance.getAttributes();
		Assert.assertEquals( decodeString(values.get(bhFederateType)),
		                     defaultFederate.federateName,
		                     "Value returned by MOM did not match federate name" );
		Assert.assertEquals( decodeHandle(values.get(bhFederateHandle)),
		                     defaultFederate.federateHandle,
		                     "Value returned by MOM did not match federate handle" );
		
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
