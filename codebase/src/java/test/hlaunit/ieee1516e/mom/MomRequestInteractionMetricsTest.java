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
import java.util.Map;

import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.utils.bithelpers.BitHelpers;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.InteractionClassHandleFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAunicodeString;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;
import hlaunit.ieee1516e.common.TestInteraction;

@Test(singleThreaded=true, groups={"MomRequestInteractionMetricsTest","mom"})
public class MomRequestInteractionMetricsTest extends Abstract1516eTest
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
	public void testRequestInteractionsSent() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederate.HLArequest.HLArequestInteractionsSent" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportInteractionsSent" );
		
		defaultFederate.quickSubscribe( "HLAinteractionRoot.X" );
		defaultFederate.quickSubscribe( "HLAinteractionRoot.X.Y" );
		secondFederate.quickPublish( "HLAinteractionRoot.X" );
		secondFederate.quickPublish( "HLAinteractionRoot.X.Y" );
		
		InteractionClassHandle xHandle = defaultFederate.rtiamb.getInteractionClassHandle( "HLAinteractionRoot.X" );
		InteractionClassHandle yHandle = defaultFederate.rtiamb.getInteractionClassHandle( "HLAinteractionRoot.X.Y" );
		
		Map<String,byte[]> xParams = new HashMap<>();
		xParams.put( "xa", encoders.createHLAinteger32BE(1).toByteArray() );
		xParams.put( "xb", encoders.createHLAinteger32BE(2).toByteArray() );
		xParams.put( "xc", encoders.createHLAinteger32BE(3).toByteArray() );
		
		// Send X 3 times
		for( int i = 0 ; i < 3 ; ++i )
		{
			secondFederate.quickSend( "HLAinteractionRoot.X", xParams, null );
			defaultFederate.fedamb.waitForROInteraction( "HLAinteractionRoot.X" );
		}
		
		Map<String,byte[]> yParams = new HashMap<>( xParams );
		yParams.put( "ya", encoders.createHLAinteger32BE(4).toByteArray() );
		yParams.put( "yb", encoders.createHLAinteger32BE(5).toByteArray() );
		yParams.put( "yc", encoders.createHLAinteger32BE(6).toByteArray() );
		
		// Send Y 2 times
		for( int i = 0 ; i < 2 ; ++i )
		{
			secondFederate.quickSend( "HLAinteractionRoot.X.Y", yParams, null );
			defaultFederate.fedamb.waitForROInteraction( "HLAinteractionRoot.X.Y" );
		}
		
		// Request interactions sent by the second federate
		Map<String,byte[]> requestParams = new HashMap<>();
		FederateHandle secondHandle = new HLA1516eHandle( secondFederate.federateHandle );
		byte[] handleBuffer = new byte[secondHandle.encodedLength()];
		secondHandle.encode( handleBuffer, 0 );
		requestParams.put( "HLAfederate", handleBuffer );
		
		defaultFederate.quickSend( "HLAmanager.HLAfederate.HLArequest.HLArequestInteractionsSent", 
		                           requestParams, 
		                           null );
		TestInteraction response = 
			defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederate.HLAreport.HLAreportInteractionsSent" );
		
		Map<String,byte[]> responseParams = response.getParametersNamed( defaultFederate.rtiamb );
		
		// HLAtransportation is unsupported at the moment, always HLAreliable
		HLAunicodeString transportation = encoders.createHLAunicodeString();
		transportation.decode( responseParams.get("HLAtransportation") );
		Assert.assertEquals( transportation.getValue(), "HLAreliable" );
		
		byte[] interactionCounts = responseParams.get( "HLAinteractionCounts" );
		
		// Should be two counts (X and X.Y)
		int offset = 0;
		int size = BitHelpers.readIntBE( interactionCounts, offset );
		offset += 4;
		Assert.assertEquals( size, 2 );
		
		InteractionClassHandleFactory icHandleFactory = 
			defaultFederate.rtiamb.getInteractionClassHandleFactory();
		for( int i = 0 ; i < size ; ++i )
		{
			InteractionClassHandle icHandle = icHandleFactory.decode( interactionCounts, offset );
			offset += icHandle.encodedLength();
			
			int count = BitHelpers.readIntBE( interactionCounts, offset );
			offset += 4;
			
			if( icHandle.equals(xHandle) )
				Assert.assertEquals( count, 3 );
			else if( icHandle.equals(yHandle) )
				Assert.assertEquals( count, 2 );
			else
				Assert.fail( "Unexpected ic handle: " + icHandle );
		}
	}
	
	@Test(enabled=true)
	public void testRequestInteractionsReceived() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederate.HLArequest.HLArequestInteractionsReceived" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportInteractionsReceived" );
		
		defaultFederate.quickPublish( "HLAinteractionRoot.X" );
		defaultFederate.quickPublish( "HLAinteractionRoot.X.Y" );
		secondFederate.quickSubscribe( "HLAinteractionRoot.X" );
		secondFederate.quickSubscribe( "HLAinteractionRoot.X.Y" );
		
		InteractionClassHandle xHandle = defaultFederate.rtiamb.getInteractionClassHandle( "HLAinteractionRoot.X" );
		InteractionClassHandle yHandle = defaultFederate.rtiamb.getInteractionClassHandle( "HLAinteractionRoot.X.Y" );
		
		Map<String,byte[]> xParams = new HashMap<>();
		xParams.put( "xa", encoders.createHLAinteger32BE(1).toByteArray() );
		xParams.put( "xb", encoders.createHLAinteger32BE(2).toByteArray() );
		xParams.put( "xc", encoders.createHLAinteger32BE(3).toByteArray() );
		
		// Send X 3 times
		for( int i = 0 ; i < 3 ; ++i )
		{
			defaultFederate.quickSend( "HLAinteractionRoot.X", xParams, null );
			secondFederate.fedamb.waitForROInteraction( "HLAinteractionRoot.X" );
		}
		
		Map<String,byte[]> yParams = new HashMap<>( xParams );
		yParams.put( "ya", encoders.createHLAinteger32BE(4).toByteArray() );
		yParams.put( "yb", encoders.createHLAinteger32BE(5).toByteArray() );
		yParams.put( "yc", encoders.createHLAinteger32BE(6).toByteArray() );
		
		// Send Y 2 times
		for( int i = 0 ; i < 2 ; ++i )
		{
			defaultFederate.quickSend( "HLAinteractionRoot.X.Y", yParams, null );
			secondFederate.fedamb.waitForROInteraction( "HLAinteractionRoot.X.Y" );
		}
		
		// Request interactions received by the second federate
		Map<String,byte[]> requestParams = new HashMap<>();
		FederateHandle secondHandle = new HLA1516eHandle( secondFederate.federateHandle );
		byte[] handleBuffer = new byte[secondHandle.encodedLength()];
		secondHandle.encode( handleBuffer, 0 );
		requestParams.put( "HLAfederate", handleBuffer );
		
		defaultFederate.quickSend( "HLAmanager.HLAfederate.HLArequest.HLArequestInteractionsReceived", 
		                           requestParams, 
		                           null );
		TestInteraction response = 
			defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederate.HLAreport.HLAreportInteractionsReceived" );
		
		Map<String,byte[]> responseParams = response.getParametersNamed( defaultFederate.rtiamb );
		
		// HLAtransportation is unsupported at the moment, always HLAreliable
		HLAunicodeString transportation = encoders.createHLAunicodeString();
		transportation.decode( responseParams.get("HLAtransportation") );
		Assert.assertEquals( transportation.getValue(), "HLAreliable" );
		
		byte[] interactionCounts = responseParams.get( "HLAinteractionCounts" );
		
		// Should be two counts (X and X.Y)
		int offset = 0;
		int size = BitHelpers.readIntBE( interactionCounts, offset );
		offset += 4;
		Assert.assertEquals( size, 2 );
		
		InteractionClassHandleFactory icHandleFactory = 
			defaultFederate.rtiamb.getInteractionClassHandleFactory();
		for( int i = 0 ; i < size ; ++i )
		{
			InteractionClassHandle icHandle = icHandleFactory.decode( interactionCounts, offset );
			offset += icHandle.encodedLength();
			
			int count = BitHelpers.readIntBE( interactionCounts, offset );
			offset += 4;
			
			if( icHandle.equals(xHandle) )
				Assert.assertEquals( count, 3 );
			else if( icHandle.equals(yHandle) )
				Assert.assertEquals( count, 2 );
			else
				Assert.fail( "Unexpected ic handle: " + icHandle );
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void main( String[] args )
	{
		MomRequestInteractionMetricsTest test = new MomRequestInteractionMetricsTest();
		test.commonBeforeClass();
		test.beforeClass();
		test.commonBeforeMethod();
		test.beforeMethod();
		
		try
		{
			test.testRequestInteractionsSent();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		test.afterMethod();
		test.commonAfterMethod();
		test.afterClass();
		test.commonAfterClass();
	}
}
