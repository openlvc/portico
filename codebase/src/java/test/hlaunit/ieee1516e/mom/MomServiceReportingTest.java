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
import org.portico.impl.hla1516e.types.encoding.HLA1516eBoolean;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.FederateHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.encoding.DataElementFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAboolean;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.encoding.HLAvariableArray;
import hla.rti1516e.exceptions.NameNotFound;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;
import hlaunit.ieee1516e.common.TestInteraction;

@Test(singleThreaded=true, groups={"MomServiceReportingTest","mom"})
public class MomServiceReportingTest extends Abstract1516eTest
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
		this.secondFederate.quickConnectWithImmediateCallbacks();
		
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

	///////////////////////////////////////////////////////////
	// TEST: testEnableServiceReportingWithFedAmbCallbacks() //
	///////////////////////////////////////////////////////////
	@Test(enabled=true)
	public void testEnableServiceReportingWithFedAmbCallbacks() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederate.HLAadjust.HLAsetServiceReporting" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportServiceInvocation" );
		int secondHandle = secondFederate.federateHandle;
		Map<String,byte[]> params = new HashMap<>();
		ByteWrapper handleWrapper = new ByteWrapper( HLA1516eHandle.EncodedLength );
		new HLA1516eHandle( secondHandle ).encode( handleWrapper );
		params.put( "HLAfederate", handleWrapper.array() );
		params.put( "HLAreportingState", new HLA1516eBoolean(true).toByteArray() );
		defaultFederate.quickSend( "HLAmanager.HLAfederate.HLAadjust.HLAsetServiceReporting", 
		                           params, 
		                           null );
		
		// Sleep for a bit to let the switch turn on
		Thread.sleep( 100 );
		RTIambassador rtiamb2 = secondFederate.getRtiAmb();
		
		// Registering a sync point should result in 3 service invocations, however their order is
		// not guaranteed. All should be a success result though
		rtiamb2.registerFederationSynchronizationPoint( "test",
		                                                new String("Hello").getBytes() );
		for( int i = 0 ; i < 3 ; ++i )
		{
			TestInteraction received =
			    defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederate.HLAreport.HLAreportServiceInvocation" );
			Map<String,byte[]> paramMap = received.getParametersNamed( defaultFederate.rtiamb );

			FederateHandle federateHandle = 
				defaultFederate.rtiamb.getFederateHandleFactory().decode( paramMap.get("HLAfederate"), 0 );
			Assert.assertEquals( federateHandle, 
			                     new HLA1516eHandle(secondFederate.federateHandle) );
			
			HLAunicodeString hlaService = encoders.createHLAunicodeString();
			hlaService.decode( paramMap.get( "HLAservice" ) );
			String service = hlaService.getValue();
			
			HLAboolean hlaSuccessIndicator = encoders.createHLAboolean();
			hlaSuccessIndicator.decode( paramMap.get( "HLAsuccessIndicator" ) );
			boolean successIndicator = hlaSuccessIndicator.getValue();
			Assert.assertEquals( hlaSuccessIndicator.getValue(), true );

			HLAunicodeString hlaException = encoders.createHLAunicodeString();
			hlaException.decode( paramMap.get("HLAexception") );
			String exception = hlaException.getValue().trim();
			Assert.assertTrue( exception.isEmpty() );
			
			HLAvariableArray<HLAunicodeString> suppliedArgs =
			    encoders.createHLAvariableArray( new UnicodeStringFactory() );
			suppliedArgs.decode( paramMap.get( "HLAsuppliedArguments" ) );
			
			HLAvariableArray<HLAunicodeString> returnedArgs =
			    encoders.createHLAvariableArray( new UnicodeStringFactory() );
			returnedArgs.decode( paramMap.get( "HLAreturnedArguments" ) );
			
			if( service.equals("registerFederationSynchronizationPoint") )
			{
				//
				// rtiamb.registerFederationSynchronizationPoint( "test", {0x48,0x65,0x6c,0x6c,0x6f} );
				//
				Assert.assertEquals( suppliedArgs.size(), 2 );
				HLAunicodeString syncpointName = suppliedArgs.get( 0 );
				Assert.assertEquals( syncpointName.getValue(), "test" );
				HLAunicodeString tag = suppliedArgs.get( 1 );
				Assert.assertEquals( tag.getValue(), "[0x48,0x65,0x6c,0x6c,0x6f]" );
				
				Assert.assertEquals( returnedArgs.size(), 0 );
			}
			else if( service.equals( "synchronizationPointRegistrationSucceeded") )
			{
				//
				// fedamb.synchronizationPointRegistrationSucceeded( "test" );
				//
				Assert.assertEquals( successIndicator, true );
				
				Assert.assertEquals( suppliedArgs.size(), 1 );
				HLAunicodeString label = suppliedArgs.get( 0 );
				Assert.assertEquals( label.getValue(), "test" );
				
				Assert.assertEquals( returnedArgs.size(), 0 );
				
				Assert.assertTrue( exception.isEmpty() );
			}
			else if( service.equals( "announceSynchronizationPoint") )
			{
				//
				// fedamb.announceSynchronizationPoint( "test", {0x48,0x65,0x6c,0x6c,0x6f} );
				//
				Assert.assertEquals( successIndicator, true );
				
				Assert.assertEquals( suppliedArgs.size(), 2 );
				HLAunicodeString syncpointName = suppliedArgs.get( 0 );
				Assert.assertEquals( syncpointName.getValue(), "test" );
				HLAunicodeString tag = suppliedArgs.get( 1 );
				Assert.assertEquals( tag.getValue(), "[0x48,0x65,0x6c,0x6c,0x6f]" );
				
				Assert.assertEquals( returnedArgs.size(), 0 );
				
				Assert.assertTrue( exception.isEmpty() );
			}
			else
			{
				Assert.fail( "Received unexpceted service invocation report for " + service  );
			}
		}
	}
	
	///////////////////////////////////////////////////////////
	// TEST: testEnableServiceReportingWithReturnArguments() //
	///////////////////////////////////////////////////////////
	@Test(enabled=true)
	public void testEnableServiceReportingWithReturnArguments() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederate.HLAadjust.HLAsetServiceReporting" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportServiceInvocation" );
		int secondHandle = secondFederate.federateHandle;
		Map<String,byte[]> params = new HashMap<>();
		ByteWrapper handleWrapper = new ByteWrapper( HLA1516eHandle.EncodedLength );
		new HLA1516eHandle( secondHandle ).encode( handleWrapper );
		params.put( "HLAfederate", handleWrapper.array() );
		params.put( "HLAreportingState", new HLA1516eBoolean(true).toByteArray() );
		defaultFederate.quickSend( "HLAmanager.HLAfederate.HLAadjust.HLAsetServiceReporting", 
		                           params, 
		                           null );
		
		// Sleep for a bit to let the switch turn on
		Thread.sleep( 100 );
		RTIambassador rtiamb2 = secondFederate.getRtiAmb();
		
		ObjectClassHandle ocHandle = rtiamb2.getObjectClassHandle( "HLAmanager.HLAfederation" );

		TestInteraction received =
		    defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederate.HLAreport.HLAreportServiceInvocation" );
		Map<String,byte[]> paramMap = received.getParametersNamed( defaultFederate.rtiamb );

		FederateHandle federateHandle = 
			defaultFederate.rtiamb.getFederateHandleFactory().decode( paramMap.get("HLAfederate"), 0 );
		Assert.assertEquals( federateHandle, 
		                     new HLA1516eHandle(secondFederate.federateHandle) );
		
		// Service should be getObjectClassHandle
		HLAunicodeString hlaService = encoders.createHLAunicodeString();
		hlaService.decode( paramMap.get( "HLAservice" ) );
		String service = hlaService.getValue();
		Assert.assertEquals( service, "getObjectClassHandle" );
		
		// Success should be true
		HLAboolean hlaSuccessIndicator = encoders.createHLAboolean();
		hlaSuccessIndicator.decode( paramMap.get( "HLAsuccessIndicator" ) );
		boolean successIndicator = hlaSuccessIndicator.getValue();
		Assert.assertEquals( hlaSuccessIndicator.getValue(), true );

		// Exception should be empty
		HLAunicodeString hlaException = encoders.createHLAunicodeString();
		hlaException.decode( paramMap.get("HLAexception") );
		String exception = hlaException.getValue().trim();
		Assert.assertTrue( exception.isEmpty() );
		
		// Should be one supplied argument - "HLAmanager.HLAfederation"
		HLAvariableArray<HLAunicodeString> suppliedArgs =
		    encoders.createHLAvariableArray( new UnicodeStringFactory() );
		suppliedArgs.decode( paramMap.get( "HLAsuppliedArguments" ) );
		Assert.assertEquals( suppliedArgs.size(), 1 );
		Assert.assertEquals( suppliedArgs.get(0).getValue(), "HLAmanager.HLAfederation" );
		
		// Should be on returned argument which matches the return from the function call
		HLAvariableArray<HLAunicodeString> returnedArgs =
		    encoders.createHLAvariableArray( new UnicodeStringFactory() );
		returnedArgs.decode( paramMap.get( "HLAreturnedArguments" ) );
		Assert.assertEquals( returnedArgs.size(), 1 );
		Assert.assertEquals( returnedArgs.get(0).getValue(), ocHandle.toString() );
	}
	
	//////////////////////////////////////////////////////
	// TEST: testEnableServiceReportingWithFailedCall() //
	//////////////////////////////////////////////////////
	@Test(enabled=true)
	public void testEnableServiceReportingWithFailedCall() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederate.HLAadjust.HLAsetServiceReporting" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportServiceInvocation" );
		
		int secondHandle = secondFederate.federateHandle;
		Map<String,byte[]> params = new HashMap<>();
		ByteWrapper handleWrapper = new ByteWrapper( HLA1516eHandle.EncodedLength );
		new HLA1516eHandle( secondHandle ).encode( handleWrapper );
		params.put( "HLAfederate", handleWrapper.array() );
		params.put( "HLAreportingState", new HLA1516eBoolean(true).toByteArray() );
		defaultFederate.quickSend( "HLAmanager.HLAfederate.HLAadjust.HLAsetServiceReporting", 
		                           params, 
		                           null );
		
		// Sleep for a bit to let the switch turn on
		Thread.sleep( 100 );
		
		try
		{
			secondFederate.rtiamb.getObjectClassHandle( "BogusClass" );
		}
		catch( NameNotFound nnf )
		{
			//
		}
		
		TestInteraction received =
		    defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederate.HLAreport.HLAreportServiceInvocation" );
		Map<String,byte[]> paramMap = received.getParametersNamed( defaultFederate.rtiamb );

		FederateHandle federateHandle = 
			defaultFederate.rtiamb.getFederateHandleFactory().decode( paramMap.get("HLAfederate"), 0 );
		Assert.assertEquals( federateHandle, 
		                     new HLA1516eHandle(secondFederate.federateHandle) );
		
		// Service should be getObjectClassHandle
		HLAunicodeString hlaService = encoders.createHLAunicodeString();
		hlaService.decode( paramMap.get( "HLAservice" ) );
		String service = hlaService.getValue();
		Assert.assertEquals( service, "getObjectClassHandle" );
		
		// Success should be false
		HLAboolean hlaSuccessIndicator = encoders.createHLAboolean();
		hlaSuccessIndicator.decode( paramMap.get( "HLAsuccessIndicator" ) );
		boolean successIndicator = hlaSuccessIndicator.getValue();
		Assert.assertEquals( hlaSuccessIndicator.getValue(), false );

		// Exception should not be empty!
		HLAunicodeString hlaException = encoders.createHLAunicodeString();
		hlaException.decode( paramMap.get("HLAexception") );
		String exception = hlaException.getValue().trim();
		Assert.assertFalse( exception.isEmpty() );
		Assert.assertEquals( exception, "name not found" );
		
		// Should be one supplied argument - "BogusClass"
		HLAvariableArray<HLAunicodeString> suppliedArgs =
		    encoders.createHLAvariableArray( new UnicodeStringFactory() );
		suppliedArgs.decode( paramMap.get( "HLAsuppliedArguments" ) );
		Assert.assertEquals( suppliedArgs.size(), 1 );
		Assert.assertEquals( suppliedArgs.get(0).getValue(), "BogusClass" );
		
		// Should be on returned argument which matches the return from the function call
		HLAvariableArray<HLAunicodeString> returnedArgs =
		    encoders.createHLAvariableArray( new UnicodeStringFactory() );
		returnedArgs.decode( paramMap.get( "HLAreturnedArguments" ) );
		Assert.assertEquals( returnedArgs.size(), 0 );
	}
	
	@Test(enabled=true)
	public void testEnableServiceReportingOnRSISubscriber() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederate.HLAadjust.HLAsetServiceReporting" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportServiceInvocation" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportMOMexception" );
		
		// Have the second federate subscribe to HLAreportServiceInvocation - this should cause the
		// subsequent request to enable service reporting on the federate to fail
		secondFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportServiceInvocation" );
		
		// Request that service reporting be enabled on the second federate
		int secondHandle = secondFederate.federateHandle;
		Map<String,byte[]> params = new HashMap<>();
		ByteWrapper handleWrapper = new ByteWrapper( HLA1516eHandle.EncodedLength );
		new HLA1516eHandle( secondHandle ).encode( handleWrapper );
		params.put( "HLAfederate", handleWrapper.array() );
		params.put( "HLAreportingState", new HLA1516eBoolean(true).toByteArray() );
		defaultFederate.quickSend( "HLAmanager.HLAfederate.HLAadjust.HLAsetServiceReporting", 
		                           params, 
		                           null );
		
		// Sleep for a bit to let the call go through
		Thread.sleep( 100 );
		
		// A MOM Exception should be reported for the second federate
		TestInteraction received =
		    defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederate.HLAreport.HLAreportMOMexception" );
		Map<String,byte[]> paramMap = received.getParametersNamed( defaultFederate.rtiamb );

		// Exception should be for the HLAsetServiceReporting service
		HLAunicodeString service = encoders.createHLAunicodeString();
		service.decode( paramMap.get("HLAservice") );
		Assert.assertEquals( service.getValue(), 
		                     "HLAinteractionRoot.HLAmanager.HLAfederate.HLAadjust.HLAsetServiceReporting" );
		
		// Exception should be raised against the requestor
		FederateHandle defaultHandle = new HLA1516eHandle( defaultFederate.federateHandle );
		FederateHandle federateHandle = 
			defaultFederate.rtiamb.getFederateHandleFactory().decode( paramMap.get("HLAfederate"), 0 );
		Assert.assertEquals( federateHandle, 
		                     defaultHandle );
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
