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

import java.util.HashMap;

import org.portico.impl.HLAVersion;
import org.portico.lrc.model.Mom;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestObject;

/**
 * These tests look at the `Federate` and `Federation` MOM classes and ensure that the data
 * returned through reflections for their objects can be deserlialized as expected.
 */
@Test(singleThreaded=true, groups={"MomFederationTest", "mom"})
public class MomFederationTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int ahHLAfederateHandle;
	private int ahHLAfederateName;
	private int ahHLAfederateType;
	private int ahHLAfederateHost;
	private int ahHLARTIversion;
	private int ahHLAFOMmoduleDesignatorList;
	private int ahHLAtimeConstrained;
	private int ahHLAtimeRegulating;
	private int ahHLAasynchronousDelivery;
	private int ahHLAfederateState;
	private int ahHLAtimeManagerState;
	private int ahHLAlogicalTime;
	private int ahHLAlookahead;
	private int ahHLAGALT;
	private int ahHLALITS;
	private int ahHLAROlength;
	private int ahHLATSOlength;
	private int ahHLAreflectionsReceived;
	private int ahHLAupdatesSent;
	private int ahHLAinteractionsReceived;
	private int ahHLAinteractionsSent;
	private int ahHLAobjectInstancesThatCanBeDeleted;
	private int ahHLAobjectInstancesUpdated;
	private int ahHLAobjectInstancesReflected;
	private int ahHLAobjectInstancesDeleted;
	private int ahHLAobjectInstancesRemoved;
	private int ahHLAobjectInstancesRegistered;
	private int ahHLAobjectInstancesDiscovered;
	private int ahHLAtimeGrantedTime;
	private int ahHLAtimeAdvancingTime;
	private int[] federateHandles;

	private int ahHLAfederationName;
	private int ahHLAfederatesInFederation;
	private int ahFederationRtiVersion;
	private int ahHLAMIMdesignator;
	private int ahFederationFomModuleDesigantorList;
	private int ahHLAcurrentFDD;
	private int ahHLAtimeImplementationName;
	private int ahHLAlastSaveName;
	private int ahHLAlastSaveTime;
	private int ahHLAnextSaveName;
	private int ahHLAnextSaveTime;
	private int ahHLAautoProvide;
	private int[] federationHandles;

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
	public void beforeMethod() //throws Exception
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();

		// get federation handles
		ahHLAfederationName         = federationHandle( "HLAfederationName" );
		ahHLAfederatesInFederation  = federationHandle( "HLAfederatesInFederation" );
		ahFederationRtiVersion      = federationHandle( "HLARTIversion" );
		ahHLAMIMdesignator          = federationHandle( "HLAMIMdesignator" );
		ahFederationFomModuleDesigantorList = federationHandle( "HLAFOMmoduleDesignatorList" );
		ahHLAcurrentFDD             = federationHandle( "HLAcurrentFDD" );
		ahHLAtimeImplementationName = federationHandle( "HLAtimeImplementationName" );
		ahHLAlastSaveName           = federationHandle( "HLAlastSaveName" );
		ahHLAlastSaveTime           = federationHandle( "HLAlastSaveTime" );
		ahHLAnextSaveName           = federationHandle( "HLAnextSaveName" );
		ahHLAnextSaveTime           = federationHandle( "HLAnextSaveTime" );
		ahHLAautoProvide            = federationHandle( "HLAautoProvide" );
		federationHandles = new int[]
		{
		 	ahHLAfederationName,
		 	ahHLAfederatesInFederation,
		 	ahFederationRtiVersion,
		 	ahHLAMIMdesignator,
		 	ahFederationFomModuleDesigantorList,
		 	ahHLAcurrentFDD,
		 	ahHLAtimeImplementationName,
		 	ahHLAlastSaveName,
		 	ahHLAlastSaveTime,
		 	ahHLAnextSaveName,
		 	ahHLAnextSaveTime,
		 	ahHLAautoProvide
		};

		// get federate handles
		ahHLAfederateHandle            = federateHandle( "HLAfederateHandle" );
		ahHLAfederateName              = federateHandle( "HLAfederateName" );
		ahHLAfederateType              = federateHandle( "HLAfederateType" );
		ahHLAfederateHost              = federateHandle( "HLAfederateHost" );
		ahHLARTIversion                = federateHandle( "HLARTIversion" );
		ahHLAFOMmoduleDesignatorList   = federateHandle( "HLAFOMmoduleDesignatorList" );
		ahHLAtimeConstrained           = federateHandle( "HLAtimeConstrained" );
		ahHLAtimeRegulating            = federateHandle( "HLAtimeRegulating" );
		ahHLAasynchronousDelivery      = federateHandle( "HLAasynchronousDelivery" );
		ahHLAfederateState             = federateHandle( "HLAfederateState" );
		ahHLAtimeManagerState          = federateHandle( "HLAtimeManagerState" );
		ahHLAlogicalTime               = federateHandle( "HLAlogicalTime" );
		ahHLAlookahead                 = federateHandle( "HLAlookahead" );
		ahHLAGALT                      = federateHandle( "HLAGALT" );
		ahHLALITS                      = federateHandle( "HLALITS" );
		ahHLAROlength                  = federateHandle( "HLAROlength" );
		ahHLATSOlength                 = federateHandle( "HLATSOlength" );
		ahHLAreflectionsReceived       = federateHandle( "HLAreflectionsReceived" );
		ahHLAupdatesSent               = federateHandle( "HLAupdatesSent" );
		ahHLAinteractionsReceived      = federateHandle( "HLAinteractionsReceived" );
		ahHLAinteractionsSent          = federateHandle( "HLAinteractionsSent" );
		ahHLAobjectInstancesThatCanBeDeleted = federateHandle( "HLAobjectInstancesThatCanBeDeleted" );
		ahHLAobjectInstancesUpdated    = federateHandle( "HLAobjectInstancesUpdated" );
		ahHLAobjectInstancesReflected  = federateHandle( "HLAobjectInstancesReflected" );
		ahHLAobjectInstancesDeleted    = federateHandle( "HLAobjectInstancesDeleted" );
		ahHLAobjectInstancesRemoved    = federateHandle( "HLAobjectInstancesRemoved" );
		ahHLAobjectInstancesRegistered = federateHandle( "HLAobjectInstancesRegistered" );
		ahHLAobjectInstancesDiscovered = federateHandle( "HLAobjectInstancesDiscovered" );
		ahHLAtimeGrantedTime           = federateHandle( "HLAtimeGrantedTime" );
		ahHLAtimeAdvancingTime         = federateHandle( "HLAtimeAdvancingTime" );
		federateHandles = new int[]
		{
		 	ahHLAfederateHandle,
		 	ahHLAfederateName,
		 	ahHLAfederateType,
		 	ahHLAfederateHost,
		 	ahHLARTIversion,
		 	ahHLAFOMmoduleDesignatorList,
		 	ahHLAtimeConstrained,
		 	ahHLAtimeRegulating,
		 	ahHLAasynchronousDelivery,
		 	ahHLAfederateState,
		 	ahHLAtimeManagerState,
		 	ahHLAlogicalTime,
		 	ahHLAlookahead,
		 	ahHLAGALT,
		 	ahHLALITS,
		 	ahHLAROlength,
		 	ahHLATSOlength,
		 	ahHLAreflectionsReceived,
		 	ahHLAupdatesSent,
		 	ahHLAinteractionsReceived,
		 	ahHLAinteractionsSent,
		 	ahHLAobjectInstancesThatCanBeDeleted,
		 	ahHLAobjectInstancesUpdated,
		 	ahHLAobjectInstancesReflected,
		 	ahHLAobjectInstancesDeleted,
		 	ahHLAobjectInstancesRemoved,
		 	ahHLAobjectInstancesRegistered,
		 	ahHLAobjectInstancesDiscovered,
		 	ahHLAtimeGrantedTime,
		 	ahHLAtimeAdvancingTime
		};
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		defaultFederate.quickResign();
		defaultFederate.quickDestroy();
	}

	
	private int federationHandle( String name )
	{
		return defaultFederate.quickACHandle( "HLAmanager.HLAfederation", name );
	}

	private int federateHandle( String name )
	{
		return defaultFederate.quickACHandle( "HLAmanager.HLAfederate", name );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////
	// TEST: testMomFederationInstance() //
	///////////////////////////////////////
	@Test(enabled=true)
	public void testMomFederationInstance()
	{
		// subscribe to the MOM Federation class attributes //
		defaultFederate.quickSubscribe( "HLAobjectRoot.HLAmanager.HLAfederation",
		                                "HLAfederationName",
		                                "HLAfederatesInFederation",
		                                "HLARTIversion",
		                                "HLAMIMdesignator",
		                                "HLAFOMmoduleDesignatorList",
		                                "HLAcurrentFDD",
		                                "HLAtimeImplementationName",
		                                "HLAlastSaveName",
		                                "HLAlastSaveTime",
		                                "HLAnextSaveName",
		                                "HLAnextSaveTime",
		                                "HLAautoProvide" );
		
		// wait for a discovery of an instance //
		int classHandle = Mom.getMomObjectClassHandle( HLAVersion.IEEE1516e, "HLAmanager.HLAfederation" );
		int objectHandle = defaultFederate.fedamb.waitForClassDiscovery( classHandle );
		
		// ask for the MOM to provide an update //
		defaultFederate.quickProvide( objectHandle, federationHandles );
		
		// wait for the update to come through //
		defaultFederate.fedamb.waitForUpdate( objectHandle );
		
		// get the instance and check the values //
		TestObject instance = defaultFederate.fedamb.getInstances().get( objectHandle );
		HashMap<Integer,byte[]> values = instance.getAttributes();

		// check a value //
		Assert.assertEquals( decodeString(values.get(ahHLAfederationName)),
		                     defaultFederate.simpleName,
		                     "Value returned by MOM did not match federation name" );
	}
	
	/////////////////////////////////////
	// TEST: testMomFederateInstance() //
	/////////////////////////////////////
	@Test(enabled=true)
	public void testMomFederateInstance()
	{
		// subscirbe to the MOM Federate class attributes //
		defaultFederate.quickSubscribe( "HLAobjectRoot.HLAmanager.HLAfederate",
		                                "HLAfederateHandle", 
		                                "HLAfederateName",
		                                "HLAfederateType",
		                                "HLAfederateHost",
		                                "HLARTIversion", 
		                                "HLAFOMmoduleDesignatorList", 
		                                "HLAtimeConstrained", 
		                                "HLAtimeRegulating", 
		                                "HLAasynchronousDelivery", 
		                                "HLAfederateState", 
		                                "HLAtimeManagerState", 
		                                "HLAlogicalTime", 
		                                "HLAlookahead", 
		                                "HLAGALT",
		                                "HLALITS",
		                                "HLAROlength", 
		                                "HLATSOlength", 
		                                "HLAreflectionsReceived", 
		                                "HLAupdatesSent", 
		                                "HLAinteractionsReceived", 
		                                "HLAinteractionsSent", 
		                                "HLAobjectInstancesThatCanBeDeleted", 
		                                "HLAobjectInstancesUpdated", 
		                                "HLAobjectInstancesReflected",
		                                "HLAobjectInstancesDeleted",
		                                "HLAobjectInstancesRemoved",
		                                "HLAobjectInstancesRegistered",
		                                "HLAobjectInstancesDiscovered",
		                                "HLAtimeGrantedTime",
		                                "HLAtimeAdvancingTime" );

		// wait for a discovery of an instance that represents the default federate //
		int classHandle = defaultFederate.quickOCHandle( "HLAmanager.HLAfederate" );
		TestObject fedInstance = defaultFederate.fedamb.waitForLatestDiscovery( classHandle );
		int objectHandle = fedInstance.getHandle();
		
		// ask for an update //
		defaultFederate.quickProvide( objectHandle, federateHandles );
		
		// watif rot eh update //
		defaultFederate.fedamb.waitForUpdate( objectHandle );

		// check the values //
		HashMap<Integer,byte[]> values = fedInstance.getAttributes();
		Assert.assertEquals( decodeString(values.get(ahHLAfederateName)),
		                     defaultFederate.federateName,
							"Value returned by MOM did not match federate name" );
		Assert.assertEquals( decodeString(values.get(ahHLAfederateType)),
		                     defaultFederate.federateType,
		                     "Value returned by MOM did not match federate type" );
		Assert.assertEquals( decodeHandle(values.get(ahHLAfederateHandle)),
		                     defaultFederate.federateHandle,
		                     "Value returned by MOM did not match federate handle" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
