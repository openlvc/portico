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
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.encoding.DataElementFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.NameNotFound;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;
import hlaunit.ieee1516e.common.TestInteraction;

@Test(singleThreaded=true, groups={"MomExceptionReportingTest","mom"})
public class MomExceptionReportingTest extends Abstract1516eTest
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

	//////////////////////////////////////////
	// TEST: testEnableExceptionReporting() //
	//////////////////////////////////////////
	@Test(enabled=true)
	public void testEnableExceptionReporting() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederate.HLAadjust.HLAsetExceptionReporting" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportException" );
		
		int secondHandle = secondFederate.federateHandle;
		Map<String,byte[]> params = new HashMap<>();
		ByteWrapper handleWrapper = new ByteWrapper( HLA1516eHandle.EncodedLength );
		new HLA1516eHandle( secondHandle ).encode( handleWrapper );
		params.put( "HLAfederate", handleWrapper.array() );
		params.put( "HLAreportingState", new HLA1516eBoolean(true).toByteArray() );
		defaultFederate.quickSend( "HLAmanager.HLAfederate.HLAadjust.HLAsetExceptionReporting", 
		                           params, 
		                           null );
		
		// Sleep for a bit to let the switch turn on
		Thread.sleep( 100 );
		RTIambassador rtiamb2 = secondFederate.getRtiAmb();
		
		try
		{
			secondFederate.rtiamb.getObjectClassHandle( "BogusClass" );
		}
		catch( NameNotFound nnf )
		{
			//
		}
		
		TestInteraction received =
		    defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederate.HLAreport.HLAreportException" );
		Map<String,byte[]> paramMap = received.getParametersNamed( defaultFederate.rtiamb );

		FederateHandle federateHandle = 
			defaultFederate.rtiamb.getFederateHandleFactory().decode( paramMap.get("HLAfederate"), 0 );
		Assert.assertEquals( federateHandle, new HLA1516eHandle(secondHandle) );
		
		HLAunicodeString service = encoders.createHLAunicodeString();
		service.decode( paramMap.get("HLAservice") );
		
		Assert.assertEquals( service.getValue(), "getObjectClassHandle" );
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
