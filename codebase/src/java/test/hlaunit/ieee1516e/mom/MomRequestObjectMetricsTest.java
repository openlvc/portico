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
import org.portico.lrc.model.ObjectModel;
import org.portico.utils.bithelpers.BitHelpers;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleFactory;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleFactory;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectClassHandleFactory;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ObjectInstanceHandleFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.encoding.DataElementFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.encoding.HLAunicodeString;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;
import hlaunit.ieee1516e.common.TestInteraction;

@Test(singleThreaded=true, groups={"MomRequestObjectMetricsTest","mom"})
public class MomRequestObjectMetricsTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate secondFederate;
	private EncoderFactory encoders;
	private int instanceHandle1;
	private int instanceHandle2;
	private int instanceHandle3;
	private int instanceHandle4;
	
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
		
		defaultFederate.quickSubscribe( "HLAobjectRoot.A", "aa", "ab", "ac" );
		secondFederate.quickPublish( "HLAobjectRoot.A", "aa", "ab", "ac" );
		secondFederate.quickPublish( "HLAobjectRoot.B", "ba", "bb", "bc" );
		
		instanceHandle1 = secondFederate.quickRegister( "HLAobjectRoot.A", "object1" );
		instanceHandle2 = secondFederate.quickRegister( "HLAobjectRoot.A", "object2" );
		instanceHandle3 = secondFederate.quickRegister( "HLAobjectRoot.A", "object3" );
		instanceHandle4 = secondFederate.quickRegister( "HLAobjectRoot.B", "object4" );
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

	////////////////////////////////////////////////////////
	// TEST: testRequestObjectInstancesThatCanBeDeleted() //
	////////////////////////////////////////////////////////
	@Test(enabled=true)
	public void testRequestObjectInstancesThatCanBeDeleted() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederate.HLArequest.HLArequestObjectInstancesThatCanBeDeleted" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectInstancesThatCanBeDeleted" );
		
		ObjectClassHandleFactory ocHandleFactory = defaultFederate.rtiamb.getObjectClassHandleFactory();
		FederateHandleFactory fedHandleFactory = defaultFederate.rtiamb.getFederateHandleFactory();
		
		ObjectClassHandle aHandle = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A" );
		ObjectClassHandle bHandle = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A.B" );
		
		int secondHandle = secondFederate.federateHandle;
		Map<String,byte[]> params = new HashMap<>();
		ByteWrapper handleWrapper = new ByteWrapper( HLA1516eHandle.EncodedLength );
		new HLA1516eHandle( secondHandle ).encode( handleWrapper );
		params.put( "HLAfederate", handleWrapper.array() );
		defaultFederate.quickSend( "HLAmanager.HLAfederate.HLArequest.HLArequestObjectInstancesThatCanBeDeleted", 
		                           params, 
		                           null );
		
		TestInteraction response = 
			defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectInstancesThatCanBeDeleted" );
		
		Map<String,byte[]> recvParams = response.getParametersNamed( defaultFederate.rtiamb );
		
		// Handle should be the second federate
		FederateHandle handle = fedHandleFactory.decode( recvParams.get("HLAfederate"), 0 );
		Assert.assertEquals( handle, new HLA1516eHandle(secondHandle) );
		
		// Should be a total of 2 entries (e.g. 2 classes)
		int offset = 0;
		byte[] data = recvParams.get("HLAobjectInstanceCounts");
		HLAinteger32BE count = encoders.createHLAinteger32BE();
		count.decode( data );
		offset += count.getEncodedLength();
		Assert.assertEquals( count.getValue(), 2 );
		
		for( int i = 0 ; i < count.getValue() ; ++i )
		{
			ObjectClassHandle oc = ocHandleFactory.decode( data, offset );
			offset += oc.encodedLength();
			int instanceCount = BitHelpers.readIntBE( data, offset );
			offset += 4;
			
			if( oc.equals(aHandle) )
				Assert.assertEquals( instanceCount, 3 );
			else if( oc.equals(bHandle) )
				Assert.assertEquals( instanceCount, 1 );
			else
				Assert.fail( "Unexpected class handle in HLAobjectInstanceCounts: " + oc );
		}
	}
	
	///////////////////////////////////////////////
	// TEST: testRequestObjectInstancesUpdated() //
	///////////////////////////////////////////////
	@Test(enabled=true)
	public void testRequestObjectInstancesUpdated() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederate.HLArequest.HLArequestObjectInstancesUpdated" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectInstancesUpdated" );
		
		ObjectClassHandleFactory ocHandleFactory = defaultFederate.rtiamb.getObjectClassHandleFactory();
		FederateHandleFactory fedHandleFactory = defaultFederate.rtiamb.getFederateHandleFactory();
		
		ObjectClassHandle aHandle = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A" );
		ObjectClassHandle bHandle = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A.B" );
		
		// Update some A instances
		AttributeHandleValueMap ahvmpA = secondFederate.rtiamb.getAttributeHandleValueMapFactory().create( 3 );
		ahvmpA.put( secondFederate.rtiamb.getAttributeHandle(aHandle, "aa"), new String("aa").getBytes() );
		ahvmpA.put( secondFederate.rtiamb.getAttributeHandle(aHandle, "ab"), new String("ab").getBytes() );
		ahvmpA.put( secondFederate.rtiamb.getAttributeHandle(aHandle, "ac"), new String("ac").getBytes() );
		secondFederate.rtiamb.updateAttributeValues( new HLA1516eHandle(instanceHandle1), 
		                                             ahvmpA, 
		                                             null );
		secondFederate.rtiamb.updateAttributeValues( new HLA1516eHandle(instanceHandle3), 
		                                             ahvmpA, 
		                                             null );
		
		// Update the B instance
		AttributeHandleValueMap ahvmpB = secondFederate.rtiamb.getAttributeHandleValueMapFactory().create( 1 );
		ahvmpB.put( secondFederate.rtiamb.getAttributeHandle(bHandle, "ba"), new String("ba").getBytes() );
		ahvmpB.put( secondFederate.rtiamb.getAttributeHandle(bHandle, "bb"), new String("bb").getBytes() );
		ahvmpB.put( secondFederate.rtiamb.getAttributeHandle(bHandle, "bc"), new String("bc").getBytes() );
		secondFederate.rtiamb.updateAttributeValues( new HLA1516eHandle(instanceHandle4), 
		                                             ahvmpB, 
		                                             null );
		
		// And update it again!
		secondFederate.rtiamb.updateAttributeValues( new HLA1516eHandle(instanceHandle4), 
		                                             ahvmpB, 
		                                             null );
		
		int secondHandle = secondFederate.federateHandle;
		Map<String,byte[]> params = new HashMap<>();
		ByteWrapper handleWrapper = new ByteWrapper( HLA1516eHandle.EncodedLength );
		new HLA1516eHandle( secondHandle ).encode( handleWrapper );
		params.put( "HLAfederate", handleWrapper.array() );
		defaultFederate.quickSend( "HLAmanager.HLAfederate.HLArequest.HLArequestObjectInstancesUpdated", 
		                           params, 
		                           null );
		
		TestInteraction response = 
			defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectInstancesUpdated" );
		
		Map<String,byte[]> recvParams = response.getParametersNamed( defaultFederate.rtiamb );
		
		// Handle should be the second federate
		FederateHandle handle = fedHandleFactory.decode( recvParams.get("HLAfederate"), 0 );
		Assert.assertEquals( handle, new HLA1516eHandle(secondHandle) );
		
		// Should be a total of 2 entries (e.g. 2 classes)
		int offset = 0;
		byte[] data = recvParams.get("HLAobjectInstanceCounts");
		HLAinteger32BE count = encoders.createHLAinteger32BE();
		count.decode( data );
		offset += count.getEncodedLength();
		Assert.assertEquals( count.getValue(), 2 );
		
		for( int i = 0 ; i < count.getValue() ; ++i )
		{
			ObjectClassHandle oc = ocHandleFactory.decode( data, offset );
			offset += oc.encodedLength();
			int instanceCount = BitHelpers.readIntBE( data, offset );
			offset += 4;
			
			if( oc.equals(aHandle) )
				Assert.assertEquals( instanceCount, 2 );
			else if( oc.equals(bHandle) )
				Assert.assertEquals( instanceCount, 1 ); // Only 1 because the metric is "unique object instances updated" 
			else
				Assert.fail( "Unexpected class handle in HLAobjectInstanceCounts: " + oc );
		}
	}
	
	/////////////////////////////////////////////////
	// TEST: testRequestObjectInstancesReflected() //
	/////////////////////////////////////////////////
	@Test(enabled=true)
	public void testRequestObjectInstancesReflected() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederate.HLArequest.HLArequestObjectInstancesReflected" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectInstancesReflected" );
		
		ObjectClassHandleFactory ocHandleFactory = defaultFederate.rtiamb.getObjectClassHandleFactory();
		FederateHandleFactory fedHandleFactory = defaultFederate.rtiamb.getFederateHandleFactory();
		
		ObjectClassHandle aHandle = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A" );
		ObjectClassHandle bHandle = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A.B" );
		
		// Update some A instances
		AttributeHandleValueMap ahvmpA = secondFederate.rtiamb.getAttributeHandleValueMapFactory().create( 3 );
		ahvmpA.put( secondFederate.rtiamb.getAttributeHandle(aHandle, "aa"), new String("aa").getBytes() );
		ahvmpA.put( secondFederate.rtiamb.getAttributeHandle(aHandle, "ab"), new String("ab").getBytes() );
		ahvmpA.put( secondFederate.rtiamb.getAttributeHandle(aHandle, "ac"), new String("ac").getBytes() );
		secondFederate.rtiamb.updateAttributeValues( new HLA1516eHandle(instanceHandle1), 
		                                             ahvmpA, 
		                                             null );
		secondFederate.rtiamb.updateAttributeValues( new HLA1516eHandle(instanceHandle3), 
		                                             ahvmpA, 
		                                             null );
		
		// Update the B instance
		AttributeHandleValueMap ahvmpB = secondFederate.rtiamb.getAttributeHandleValueMapFactory().create( 1 );
		ahvmpB.put( secondFederate.rtiamb.getAttributeHandle(bHandle, "ba"), new String("ba").getBytes() );
		ahvmpB.put( secondFederate.rtiamb.getAttributeHandle(bHandle, "bb"), new String("bb").getBytes() );
		ahvmpB.put( secondFederate.rtiamb.getAttributeHandle(bHandle, "bc"), new String("bc").getBytes() );
		secondFederate.rtiamb.updateAttributeValues( new HLA1516eHandle(instanceHandle4), 
		                                             ahvmpB, 
		                                             null );
		
		// And update it again!
		secondFederate.rtiamb.updateAttributeValues( new HLA1516eHandle(instanceHandle4), 
		                                             ahvmpB, 
		                                             null );
		
		int firstHandle = defaultFederate.federateHandle;
		Map<String,byte[]> params = new HashMap<>();
		ByteWrapper handleWrapper = new ByteWrapper( HLA1516eHandle.EncodedLength );
		new HLA1516eHandle( firstHandle ).encode( handleWrapper );
		params.put( "HLAfederate", handleWrapper.array() );
		defaultFederate.quickSend( "HLAmanager.HLAfederate.HLArequest.HLArequestObjectInstancesReflected", 
		                           params, 
		                           null );
		
		TestInteraction response = 
			defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectInstancesReflected" );
		
		Map<String,byte[]> recvParams = response.getParametersNamed( defaultFederate.rtiamb );
		
		// Handle should be the first federate
		FederateHandle handle = fedHandleFactory.decode( recvParams.get("HLAfederate"), 0 );
		Assert.assertEquals( handle, new HLA1516eHandle(firstHandle) );
		
		// Should be a total of 1 entry (e.g. defaultFederate is only subscribed to A)
		int offset = 0;
		byte[] data = recvParams.get("HLAobjectInstanceCounts");
		HLAinteger32BE count = encoders.createHLAinteger32BE();
		count.decode( data );
		offset += count.getEncodedLength();
		Assert.assertEquals( count.getValue(), 1 );
		
		ObjectClassHandle oc = ocHandleFactory.decode( data, offset );
		offset += oc.encodedLength();
		Assert.assertEquals( oc, aHandle );
		int instanceCount = BitHelpers.readIntBE( data, offset );
		offset += 4;
		Assert.assertEquals( instanceCount, 3 ); // The two actual instances of A, plus the one instance 
		                                         // of A.B (discovered as A)
	}
	
	////////////////////////////////////
	// TEST: testRequestUpdatesSent() //
	////////////////////////////////////
	@Test(enabled=true)
	public void testRequestUpdatesSent() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederate.HLArequest.HLArequestUpdatesSent" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportUpdatesSent" );
		
		ObjectClassHandleFactory ocHandleFactory = defaultFederate.rtiamb.getObjectClassHandleFactory();
		FederateHandleFactory fedHandleFactory = defaultFederate.rtiamb.getFederateHandleFactory();
		
		ObjectClassHandle aHandle = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A" );
		ObjectClassHandle bHandle = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A.B" );
		
		// Update some A instances
		AttributeHandleValueMap ahvmpA = secondFederate.rtiamb.getAttributeHandleValueMapFactory().create( 3 );
		ahvmpA.put( secondFederate.rtiamb.getAttributeHandle(aHandle, "aa"), new String("aa").getBytes() );
		ahvmpA.put( secondFederate.rtiamb.getAttributeHandle(aHandle, "ab"), new String("ab").getBytes() );
		ahvmpA.put( secondFederate.rtiamb.getAttributeHandle(aHandle, "ac"), new String("ac").getBytes() );
		secondFederate.rtiamb.updateAttributeValues( new HLA1516eHandle(instanceHandle1), 
		                                             ahvmpA, 
		                                             null );
		secondFederate.rtiamb.updateAttributeValues( new HLA1516eHandle(instanceHandle3), 
		                                             ahvmpA, 
		                                             null );
		
		// Update the B instance
		AttributeHandleValueMap ahvmpB = secondFederate.rtiamb.getAttributeHandleValueMapFactory().create( 1 );
		ahvmpB.put( secondFederate.rtiamb.getAttributeHandle(bHandle, "ba"), new String("ba").getBytes() );
		ahvmpB.put( secondFederate.rtiamb.getAttributeHandle(bHandle, "bb"), new String("bb").getBytes() );
		ahvmpB.put( secondFederate.rtiamb.getAttributeHandle(bHandle, "bc"), new String("bc").getBytes() );
		secondFederate.rtiamb.updateAttributeValues( new HLA1516eHandle(instanceHandle4), 
		                                             ahvmpB, 
		                                             null );
		
		// And update it again!
		secondFederate.rtiamb.updateAttributeValues( new HLA1516eHandle(instanceHandle4), 
		                                             ahvmpB, 
		                                             null );
		
		int secondHandle = secondFederate.federateHandle;
		Map<String,byte[]> params = new HashMap<>();
		ByteWrapper handleWrapper = new ByteWrapper( HLA1516eHandle.EncodedLength );
		new HLA1516eHandle( secondHandle ).encode( handleWrapper );
		params.put( "HLAfederate", handleWrapper.array() );
		defaultFederate.quickSend( "HLAmanager.HLAfederate.HLArequest.HLArequestUpdatesSent", 
		                           params, 
		                           null );
		
		TestInteraction response = 
			defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederate.HLAreport.HLAreportUpdatesSent" );
		
		Map<String,byte[]> recvParams = response.getParametersNamed( defaultFederate.rtiamb );
		
		// Handle should be the second federate
		FederateHandle handle = fedHandleFactory.decode( recvParams.get("HLAfederate"), 0 );
		Assert.assertEquals( handle, new HLA1516eHandle(secondHandle) );
		
		// HLAtransportation is unsupported at the moment, always HLAreliable
		HLAunicodeString transportation = encoders.createHLAunicodeString();
		transportation.decode( recvParams.get("HLAtransportation") );
		Assert.assertEquals( transportation.getValue(), "HLAreliable" );
		
		// Should be a total of 2 entries
		int offset = 0;
		byte[] data = recvParams.get("HLAupdateCounts");
		HLAinteger32BE count = encoders.createHLAinteger32BE();
		count.decode( data );
		offset += count.getEncodedLength();
		Assert.assertEquals( count.getValue(), 2 );
		
		for( int i = 0 ; i < count.getValue() ; ++i )
		{
			ObjectClassHandle oc = ocHandleFactory.decode( data, offset );
			offset += oc.encodedLength();
			int updateCount = BitHelpers.readIntBE( data, offset );
			offset += 4;
			
			if( oc.equals(aHandle) )
				Assert.assertEquals( updateCount, 2 );
			else if( oc.equals(bHandle) )
				Assert.assertEquals( updateCount, 2 ); // 2 as the metric is total updates for the OC
			else
				Assert.fail( "Unexpected class handle in HLAupdateCounts: " + oc );
		}
	}

	////////////////////////////////////////////
	// TEST: testRequestReflectionsReceived() //
	////////////////////////////////////////////
	@Test(enabled=true)
	public void testRequestReflectionsReceived() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederate.HLArequest.HLArequestReflectionsReceived" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportReflectionsReceived" );
		
		ObjectClassHandleFactory ocHandleFactory = defaultFederate.rtiamb.getObjectClassHandleFactory();
		FederateHandleFactory fedHandleFactory = defaultFederate.rtiamb.getFederateHandleFactory();
		
		ObjectClassHandle aHandle = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A" );
		ObjectClassHandle bHandle = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A.B" );
		
		// Update some A instances
		AttributeHandleValueMap ahvmpA = secondFederate.rtiamb.getAttributeHandleValueMapFactory().create( 3 );
		ahvmpA.put( secondFederate.rtiamb.getAttributeHandle(aHandle, "aa"), new String("aa").getBytes() );
		ahvmpA.put( secondFederate.rtiamb.getAttributeHandle(aHandle, "ab"), new String("ab").getBytes() );
		ahvmpA.put( secondFederate.rtiamb.getAttributeHandle(aHandle, "ac"), new String("ac").getBytes() );
		secondFederate.rtiamb.updateAttributeValues( new HLA1516eHandle(instanceHandle1), 
		                                             ahvmpA, 
		                                             null );
		secondFederate.rtiamb.updateAttributeValues( new HLA1516eHandle(instanceHandle3), 
		                                             ahvmpA, 
		                                             null );
		
		// Update the B instance
		AttributeHandleValueMap ahvmpB = secondFederate.rtiamb.getAttributeHandleValueMapFactory().create( 1 );
		ahvmpB.put( secondFederate.rtiamb.getAttributeHandle(bHandle, "ba"), new String("ba").getBytes() );
		ahvmpB.put( secondFederate.rtiamb.getAttributeHandle(bHandle, "bb"), new String("bb").getBytes() );
		ahvmpB.put( secondFederate.rtiamb.getAttributeHandle(bHandle, "bc"), new String("bc").getBytes() );
		secondFederate.rtiamb.updateAttributeValues( new HLA1516eHandle(instanceHandle4), 
		                                             ahvmpB, 
		                                             null );
		
		// And update it again!
		secondFederate.rtiamb.updateAttributeValues( new HLA1516eHandle(instanceHandle4), 
		                                             ahvmpB, 
		                                             null );
		
		int firstHandle = defaultFederate.federateHandle;
		Map<String,byte[]> params = new HashMap<>();
		ByteWrapper handleWrapper = new ByteWrapper( HLA1516eHandle.EncodedLength );
		new HLA1516eHandle( firstHandle ).encode( handleWrapper );
		params.put( "HLAfederate", handleWrapper.array() );
		defaultFederate.quickSend( "HLAmanager.HLAfederate.HLArequest.HLArequestReflectionsReceived", 
		                           params, 
		                           null );
		
		TestInteraction response = 
			defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederate.HLAreport.HLAreportReflectionsReceived" );
		
		Map<String,byte[]> recvParams = response.getParametersNamed( defaultFederate.rtiamb );
		
		// Handle should be the first federate
		FederateHandle handle = fedHandleFactory.decode( recvParams.get("HLAfederate"), 0 );
		Assert.assertEquals( handle, new HLA1516eHandle(firstHandle) );
		
		// HLAtransportation is unsupported at the moment, always HLAreliable
		HLAunicodeString transportation = encoders.createHLAunicodeString();
		transportation.decode( recvParams.get("HLAtransportation") );
		Assert.assertEquals( transportation.getValue(), "HLAreliable" );
		
		// Should be a total of 1 entry (defaultFederate only subscribes to A)
		int offset = 0;
		byte[] data = recvParams.get("HLAreflectCounts");
		HLAinteger32BE count = encoders.createHLAinteger32BE();
		count.decode( data );
		offset += count.getEncodedLength();
		Assert.assertEquals( count.getValue(), 1 );
		
		ObjectClassHandle oc = ocHandleFactory.decode( data, offset );
		offset += oc.encodedLength();
		Assert.assertEquals( oc, aHandle );
		int reflectCount = BitHelpers.readIntBE( data, offset );
		offset += 4;
			
		Assert.assertEquals( reflectCount, 4 ); // 4 as the metric is total updates for the OC
		                                         // 2 (A) + 2 (A.B discovered as A)
	}
	
	///////////////////////////////////////////////////////
	// TEST: testRequestObjectInstanceInformationOwned() //
	///////////////////////////////////////////////////////
	@Test(enabled=true)
	public void testRequestObjectInstanceInformationOwned() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederate.HLArequest.HLArequestObjectInstanceInformation" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectInstanceInformation" );
		
		ObjectClassHandleFactory ocHandleFactory = defaultFederate.rtiamb.getObjectClassHandleFactory();
		FederateHandleFactory fedHandleFactory = defaultFederate.rtiamb.getFederateHandleFactory();
		ObjectInstanceHandleFactory oiHandleFactory = defaultFederate.rtiamb.getObjectInstanceHandleFactory();
		AttributeHandleFactory aHandleFactory = defaultFederate.rtiamb.getAttributeHandleFactory();
		
		ObjectClassHandle aHandle = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A" );
		ObjectClassHandle bHandle = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A.B" );
		
		HLA1516eHandle i4Handle = new HLA1516eHandle( instanceHandle4 );
		
		// Request information from the perspective of the second federate
		int secondHandle = secondFederate.federateHandle;
		Map<String,byte[]> params = new HashMap<>();
		ByteWrapper handleWrapper = new ByteWrapper( HLA1516eHandle.EncodedLength );
		new HLA1516eHandle( secondHandle ).encode( handleWrapper );
		params.put( "HLAfederate", handleWrapper.array() );
		params.put( "HLAobjectInstance", i4Handle.getBytes() );
		defaultFederate.quickSend( "HLAmanager.HLAfederate.HLArequest.HLArequestObjectInstanceInformation", 
		                           params, 
		                           null );
		
		TestInteraction response = 
			defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectInstanceInformation" );
		
		Map<String,byte[]> recvParams = response.getParametersNamed( defaultFederate.rtiamb );
		
		// Handle should be the first federate
		FederateHandle handle = fedHandleFactory.decode( recvParams.get("HLAfederate"), 0 );
		Assert.assertEquals( handle, new HLA1516eHandle(secondHandle) );
		
		// Object Instance handle should match instance4
		ObjectInstanceHandle instanceHandle = 
			oiHandleFactory.decode( recvParams.get("HLAobjectInstance"), 0 );
		Assert.assertEquals( instanceHandle, i4Handle );
		
		// Was originally registered as A.B
		ObjectClassHandle registeredAs = ocHandleFactory.decode( recvParams.get("HLAregisteredClass"), 0 );
		Assert.assertEquals( registeredAs, bHandle );
		
		// Second Federate is the registrant of instance4, but does not subscribe to it, therefore the
		// known class should be invalid
		ObjectClassHandle knownHandle = ocHandleFactory.decode( recvParams.get("HLAknownClass"), 0 );
		Assert.assertEquals( knownHandle, new HLA1516eHandle(ObjectModel.INVALID_HANDLE) );
		
		// Second Federate owns all attributes of instance4
		byte[] ownedAttributes = recvParams.get( "HLAownedInstanceAttributeList" );
		int offset = 0;
		HLAinteger32BE ownedAttributeCount = encoders.createHLAinteger32BE();
		ownedAttributeCount.decode( ownedAttributes );
		offset += ownedAttributeCount.getEncodedLength();
		Assert.assertEquals( ownedAttributeCount.getValue(), 4 );
		
		// Are all expected attributes present? 
		Set<String> expectedAttributes = new HashSet<>();
		expectedAttributes.add( "ba" );
		expectedAttributes.add( "bb" );
		expectedAttributes.add( "bc" );
		expectedAttributes.add( "HLAprivilegeToDeleteObject" );
		
		for( int i = 0 ; i < ownedAttributeCount.getValue() ; ++i )
		{
			AttributeHandle attrHandle = aHandleFactory.decode( ownedAttributes, offset );
			offset += attrHandle.encodedLength();
			
			String attrName = defaultFederate.rtiamb.getAttributeName( bHandle, attrHandle );
			Assert.assertTrue( expectedAttributes.contains(attrName) );
			expectedAttributes.remove( attrName );
		}
		
		Assert.assertTrue( expectedAttributes.isEmpty() );
	}
	
	//////////////////////////////////////////////////////////
	// TEST: testRequestObjectInstanceInformationNotOwned() //
	//////////////////////////////////////////////////////////
	@Test(enabled=true)
	public void testRequestObjectInstanceInformationNotOwned() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederate.HLArequest.HLArequestObjectInstanceInformation" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectInstanceInformation" );
		
		ObjectClassHandleFactory ocHandleFactory = defaultFederate.rtiamb.getObjectClassHandleFactory();
		FederateHandleFactory fedHandleFactory = defaultFederate.rtiamb.getFederateHandleFactory();
		ObjectInstanceHandleFactory oiHandleFactory = defaultFederate.rtiamb.getObjectInstanceHandleFactory();
		ObjectClassHandle aHandle = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A" );
		ObjectClassHandle bHandle = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A.B" );
		
		HLA1516eHandle i4Handle = new HLA1516eHandle( instanceHandle4 );
		
		// First request information from the perspective of the default federate
		int firstHandle = defaultFederate.federateHandle;
		Map<String,byte[]> params = new HashMap<>();
		ByteWrapper handleWrapper = new ByteWrapper( HLA1516eHandle.EncodedLength );
		new HLA1516eHandle( firstHandle ).encode( handleWrapper );
		params.put( "HLAfederate", handleWrapper.array() );
		params.put( "HLAobjectInstance", i4Handle.getBytes() );
		defaultFederate.quickSend( "HLAmanager.HLAfederate.HLArequest.HLArequestObjectInstanceInformation", 
		                           params, 
		                           null );
		
		TestInteraction response = 
			defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectInstanceInformation" );
		
		Map<String,byte[]> recvParams = response.getParametersNamed( defaultFederate.rtiamb );
		
		// Handle should be the first federate
		FederateHandle handle = fedHandleFactory.decode( recvParams.get("HLAfederate"), 0 );
		Assert.assertEquals( handle, new HLA1516eHandle(firstHandle) );
		
		// Object Instance handle should match instance4
		ObjectInstanceHandle instanceHandle = 
			oiHandleFactory.decode( recvParams.get("HLAobjectInstance"), 0 );
		Assert.assertEquals( instanceHandle, i4Handle );
		
		// Was originally registered as A.B
		ObjectClassHandle registeredAs = ocHandleFactory.decode( recvParams.get("HLAregisteredClass"), 0 );
		Assert.assertEquals( registeredAs, bHandle );
		
		// However default federate is only subscribed to A, which is what it should be "known" as 
		ObjectClassHandle knownAs = ocHandleFactory.decode( recvParams.get("HLAknownClass"), 0 );
		Assert.assertEquals( knownAs, aHandle );
		
		// Default doesn't own any attributes of instance4
		ByteWrapper ownedAttributes = new ByteWrapper( recvParams.get( "HLAownedInstanceAttributeList" ) );
		HLAinteger32BE ownedAttributeCount = encoders.createHLAinteger32BE();
		ownedAttributeCount.decode( ownedAttributes );
		Assert.assertEquals( ownedAttributeCount.getValue(), 0 );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	private class UnicodeStringFactory implements DataElementFactory<HLAunicodeString>
	{
		@Override
		public HLAunicodeString createElement( int index )
		{
			return encoders.createHLAunicodeString();
		}
	}
}
