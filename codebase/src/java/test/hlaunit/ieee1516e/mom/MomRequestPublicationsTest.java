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
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleFactory;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.InteractionClassHandleFactory;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectClassHandleFactory;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.encoding.DataElementFactory;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.CouldNotDecode;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;
import hlaunit.ieee1516e.common.TestInteraction;

@Test(singleThreaded=true, groups={"MomRequestPublicationsTest","mom"})
public class MomRequestPublicationsTest extends Abstract1516eTest
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
	/**
	 * The spec says that HLAattributeList should be a HLAvariableArray of HLAhandle, however HLAhandle
	 * doesn't implement the DataElement interface. Thus we can't create/decode the list using the
	 * encoder factory :(
	 */
	private Set<AttributeHandle> decodeHLAattributeList( RTIambassador rtiamb, byte[] data )
		throws DecoderException, 
		       RTIinternalError, 
		       NotConnected, 
		       FederateNotExecutionMember, 
		       CouldNotDecode
	{
		HLAinteger32BE hlaSize = encoders.createHLAinteger32BE();
		hlaSize.decode( data );
		int size = hlaSize.getValue();
		
		int offset = hlaSize.getEncodedLength();
		Set<AttributeHandle> results = new HashSet<>( size );
		for( int i = 0 ; i < size ; ++i )
		{
			AttributeHandle handle = rtiamb.getAttributeHandleFactory().decode( data, offset );
			offset += handle.encodedLength();
			results.add( handle );
		}
		
		return results;
	}
	
	/**
	 * The spec says that HLAinteractionClassList should be a HLAvariableArray of HLAhandle, however 
	 * HLAhandle doesn't implement the DataElement interface. Thus we can't create/decode the list using 
	 * the encoder factory :(
	 */
	private Set<InteractionClassHandle> decodeHLAinteractionClassList( RTIambassador rtiamb, byte[] data )
		throws DecoderException, 
		       RTIinternalError, 
		       NotConnected, 
		       FederateNotExecutionMember, 
		       CouldNotDecode
	{
		HLAinteger32BE hlaSize = encoders.createHLAinteger32BE();
		hlaSize.decode( data );
		int size = hlaSize.getValue();
		
		int offset = hlaSize.getEncodedLength();
		Set<InteractionClassHandle> results = new HashSet<>( size );
		for( int i = 0 ; i < size ; ++i )
		{
			InteractionClassHandle handle = rtiamb.getInteractionClassHandleFactory().decode( data, 
			                                                                                  offset );
			offset += handle.encodedLength();
			results.add( handle );
		}
		
		return results;
	}
	
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

	///////////////////////////////////////////
	// TEST: testRequestObjectPublications() //
	///////////////////////////////////////////
	@Test(enabled=true)
	public void testRequestObjectPublications() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederate.HLArequest.HLArequestPublications" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectClassPublication" );
		
		secondFederate.quickPublish( "HLAobjectRoot.A", "aa", "ac" );
		secondFederate.quickPublish( "HLAobjectRoot.A.B", "bb" );
		
		int secondHandle = secondFederate.federateHandle;
		Map<String,byte[]> params = new HashMap<>();
		ByteWrapper handleWrapper = new ByteWrapper( HLA1516eHandle.EncodedLength );
		new HLA1516eHandle( secondHandle ).encode( handleWrapper );
		params.put( "HLAfederate", handleWrapper.array() );
		defaultFederate.quickSend( "HLAmanager.HLAfederate.HLArequest.HLArequestPublications", 
		                           params, 
		                           null );
		
		FederateHandleFactory fedHandleFactory = defaultFederate.rtiamb.getFederateHandleFactory();
		ObjectClassHandleFactory ocHandleFactory = defaultFederate.rtiamb.getObjectClassHandleFactory();
		ObjectClassHandle aHandle = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A" );
		ObjectClassHandle bHandle = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A.B" );
		
		// Should receive 2x HLAreportPublications, one for each object class
		for( int i = 0 ; i < 2 ; ++i )
		{
			TestInteraction received =
			    defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectClassPublication" );
			
			Map<String,byte[]> recvParams = received.getParametersNamed( defaultFederate.rtiamb );
			
			// Handle should be the second federate
			FederateHandle handle = fedHandleFactory.decode( recvParams.get("HLAfederate"), 0 );
			Assert.assertEquals( handle, new HLA1516eHandle(secondHandle) );
			
			// Should be a total of 2 classes being reported
			HLAinteger32BE classCount = encoders.createHLAinteger32BE();
			classCount.decode( recvParams.get("HLAnumberOfClasses") );
			Assert.assertEquals( classCount.getValue(), 2 );
			
			ObjectClassHandle ocHandle = ocHandleFactory.decode( recvParams.get("HLAobjectClass"), 0 );
			Set<AttributeHandle> attHandles = decodeHLAattributeList( defaultFederate.rtiamb, 
			                                                          recvParams.get("HLAattributeList") );
			
			if( ocHandle.equals(aHandle) )
			{
				Assert.assertEquals( attHandles.size(), 3 );
				Set<String> expecting = new HashSet<>();
				expecting.add( "HLAprivilegeToDeleteObject" );
				expecting.add( "aa" );
				expecting.add( "ac" );
				
				for( AttributeHandle atthandle : attHandles )
				{
					String name = defaultFederate.rtiamb.getAttributeName( ocHandle, atthandle );
					expecting.remove( name );
				}
				
				// All expected attributes should have been processed, and none should remain in the
				// "existing" set
				Assert.assertEquals( expecting.size(), 0 );
			}
			else if( ocHandle.equals(bHandle) )
			{
				Assert.assertEquals( attHandles.size(), 2 );
				Set<String> expecting = new HashSet<>();
				expecting.add( "HLAprivilegeToDeleteObject" );
				expecting.add( "bb" );
				
				for( AttributeHandle atthandle : attHandles )
				{
					String name = defaultFederate.rtiamb.getAttributeName( ocHandle, atthandle );
					expecting.remove( name );
				}
				
				// All expected attributes should have been processed, and none should remain in the
				// "existing" set
				Assert.assertEquals( expecting.size(), 0 );
			}
			else
			{
				Assert.fail( "Unexpected HLAreportObjectClassPublication received for OC " + ocHandle );
			}
		}
	}
	
	////////////////////////////////////////////////
	// TEST: testRequestInteractionPublications() //
	////////////////////////////////////////////////
	@Test(enabled=true)
	public void testRequestInteractionPublications() throws Exception
	{
		defaultFederate.quickPublish( "HLAmanager.HLAfederate.HLArequest.HLArequestPublications" );
		defaultFederate.quickSubscribe( "HLAmanager.HLAfederate.HLAreport.HLAreportInteractionPublication" );
		
		secondFederate.quickPublish( "HLAinteractionRoot.X" );
		secondFederate.quickPublish( "HLAinteractionRoot.X.Y" );
		
		int secondHandle = secondFederate.federateHandle;
		Map<String,byte[]> params = new HashMap<>();
		ByteWrapper handleWrapper = new ByteWrapper( HLA1516eHandle.EncodedLength );
		new HLA1516eHandle( secondHandle ).encode( handleWrapper );
		params.put( "HLAfederate", handleWrapper.array() );
		defaultFederate.quickSend( "HLAmanager.HLAfederate.HLArequest.HLArequestPublications", 
		                           params, 
		                           null );
		
		FederateHandleFactory fedHandleFactory = defaultFederate.rtiamb.getFederateHandleFactory();
		InteractionClassHandleFactory icHandleFactory = defaultFederate.rtiamb.getInteractionClassHandleFactory();
		InteractionClassHandle xHandle = defaultFederate.rtiamb.getInteractionClassHandle( "HLAinteractionRoot.X" );
		InteractionClassHandle yHandle = defaultFederate.rtiamb.getInteractionClassHandle( "HLAinteractionRoot.X.Y" );
		
		TestInteraction received =
		    defaultFederate.fedamb.waitForROInteraction( "HLAmanager.HLAfederate.HLAreport.HLAreportInteractionPublication" );
		
		Map<String,byte[]> recvParams = received.getParametersNamed( defaultFederate.rtiamb );
		
		// Handle should be the second federate
		FederateHandle handle = fedHandleFactory.decode( recvParams.get("HLAfederate"), 0 );
		Assert.assertEquals( handle, new HLA1516eHandle(secondHandle) );
		
		Set<InteractionClassHandle> interactions = 
			decodeHLAinteractionClassList( defaultFederate.rtiamb, 
			                               recvParams.get("HLAinteractionClassList") );
		Assert.assertEquals( interactions.size(), 2 );
		Assert.assertTrue( interactions.contains(xHandle) );
		Assert.assertTrue( interactions.contains(yHandle) );
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
