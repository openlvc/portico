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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.utils.bithelpers.BitHelpers;
import org.portico2.rti.services.mom.data.SynchPointFederate.SynchPointStatus;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.encoding.HLAunicodeString;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;
import hlaunit.ieee1516e.common.TestInteraction;

@Test(singleThreaded=true, groups={"MomRequestSynchronizationPoints","mom"})
public class MomRequestSynchronizationPoints extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate secondFederate;
	private EncoderFactory encoders;
	
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
		
		try
		{
			this.encoders = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
		}
		catch( Exception e )
		{
			Assert.fail( "Could not create encoder factory " + e.getMessage(), e );
		}
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		this.secondFederate = new TestFederate( "secondFederate", this );
		this.secondFederate.quickConnect();
		
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
		
		
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
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	@Test(enabled=true)
	public void testRequestSynchronizationPoints() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederation.HLArequest.HLArequestSynchronizationPoints" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederation.HLAreport.HLAreportSynchronizationPoints" );
		
		secondFederate.quickAnnounce( "achieved", null );
		secondFederate.quickAnnounce( "test1", null );
		secondFederate.quickAnnounce( "test2", null );
		secondFederate.quickAnnounce( "test3", null );
		
		defaultFederate.quickAchieved( "achieved" );
		secondFederate.quickAchieved( "achieved" );
		defaultFederate.fedamb.waitForSynchronized( "achieved" );
		
		defaultFederate.quickSend( "HLAmanager.HLAfederation.HLArequest.HLArequestSynchronizationPoints" );
		TestInteraction response = 
			defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederation.HLAreport.HLAreportSynchronizationPoints" );
		
		Map<String,byte[]> responseParams = response.getParametersNamed( defaultFederate.rtiamb );
		ByteWrapper syncpoints = new ByteWrapper( responseParams.get("HLAsyncPoints") );
		HLAinteger32BE recordCount = encoders.createHLAinteger32BE();
		recordCount.decode( syncpoints );
		
		// HLAreportSynchronizationPoints should only report "in-progress" syncpoints, therefore we
		// should only have 3 in our response
		Assert.assertEquals( recordCount.getValue(), 3 );
		
		// Are they the syncpoints we were expecting?
		Set<String> expectedLabels = new HashSet<>();
		expectedLabels.add( "test1" );
		expectedLabels.add( "test2" );
		expectedLabels.add( "test3" );
		for( int i = 0 ; i < recordCount.getValue() ; ++i )
		{
			HLAunicodeString label = encoders.createHLAunicodeString();
			label.decode( syncpoints );
			String labelValue = label.getValue();
			
			Assert.assertTrue( expectedLabels.contains(labelValue) );
			expectedLabels.remove( labelValue );
		}
		
		Assert.assertTrue( expectedLabels.isEmpty() );
	}
	
	@Test(enabled=true)
	public void testRequestSynchronizationPointStatus() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederation.HLArequest.HLArequestSynchronizationPointStatus" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederation.HLAreport.HLAreportSynchronizationPointStatus" );
	
		FederateHandleFactory fedHandleFactory = defaultFederate.rtiamb.getFederateHandleFactory();
		FederateHandle defaultFedHandle = new HLA1516eHandle( defaultFederate.federateHandle );
		FederateHandle secondFedHandle = new HLA1516eHandle( secondFederate.federateHandle );
		
		secondFederate.quickAnnounce( "test1", null );
		secondFederate.quickAnnounce( "test2", null );
		
		// Have the default federate achieve test1
		defaultFederate.quickAchieved( "test1" );
		
		Map<String,byte[]> requestParams = new HashMap<>();
		requestParams.put( "HLAsyncPointName", encoders.createHLAunicodeString("test1").toByteArray() );
		defaultFederate.quickSend( "HLAmanager.HLAfederation.HLArequest.HLArequestSynchronizationPointStatus",
		                           requestParams,
		                           null );
		TestInteraction response = 
			defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederation.HLAreport.HLAreportSynchronizationPointStatus" );
		
		Map<String,byte[]> responseParams = response.getParametersNamed( defaultFederate.rtiamb );
		HLAunicodeString syncpointName = encoders.createHLAunicodeString();
		syncpointName.decode( responseParams.get("HLAsyncPointName") );
		Assert.assertEquals( syncpointName.getValue(), "test1" );
		
		byte[] syncpointFederates = responseParams.get( "HLAsyncPointFederates" );
		int offset = 0;
		int recordCount = BitHelpers.readIntBE( syncpointFederates, offset );
		offset += 4;
		
		// Expecting two records, one for each federate
		Assert.assertEquals( recordCount, 2 );
		
		for( int i = 0 ; i < recordCount ; ++i )
		{
			FederateHandle fedHandle = fedHandleFactory.decode( syncpointFederates, offset );
			offset += fedHandle.encodedLength();
			
			int statusRaw = BitHelpers.readIntBE( syncpointFederates, offset );
			SynchPointStatus status = SynchPointStatus.values()[statusRaw];
			offset += 4;
			
			if( fedHandle.equals(defaultFedHandle) )
			{
				// Default federate has achieved the syncpoint, and is waiting for the other federate
				Assert.assertEquals( status, SynchPointStatus.WaitingForRestOfFederation );
			}
			else if( fedHandle.equals(secondFedHandle) )
			{
				// Second federate has not achieved the syncpoint
				Assert.assertEquals( status, SynchPointStatus.NoActivity );
			}
			else
			{
				Assert.fail( "Unexpected fed handle: " + fedHandle );
			}
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
