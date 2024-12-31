/*
 *   Copyright 2012 The Portico Project
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
package hlaunit.ieee1516e.federation;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.exceptions.AlreadyConnected;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;

@Test(singleThreaded=true, groups={"ConnectTest", "basic", "connect", "federationManagement"})
public class ConnectTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate federateOne;
	private TestFederate federateTwo;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@BeforeClass(alwaysRun=true)
	public void beforeClass()
	{
		this.federateOne = new TestFederate( "federateOne", this );
		this.federateTwo = new TestFederate( "federateTwo", this );
	}

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		federateOne.quickResignTolerant();
		federateTwo.quickResignTolerant();
		federateOne.quickDestroyTolerant();
		federateOne.quickDisconnect();
		federateTwo.quickDisconnect();
	}

	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	// connect() ///////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	/////////////////////////////////
	// TEST: (valid) testConnect() //
	/////////////////////////////////
	@Test
	public void testConnect() throws Exception
	{
		// connect and then disconnect
		federateOne.rtiamb.connect( federateOne.fedamb, CallbackModel.HLA_EVOKED );
		federateOne.rtiamb.disconnect();

		// connect and disconnect again, but with a different callback model
		federateOne.rtiamb.connect( federateOne.fedamb, CallbackModel.HLA_IMMEDIATE );
		federateOne.rtiamb.disconnect();
	}
	
	/////////////////////////////////////////////
	// TEST: testConnectWhenAlreadyConnected() //
	/////////////////////////////////////////////
	@Test
	public void testConnectWhenAlreadyConnected()
	{
		federateOne.quickConnectWithImmediateCallbacks();
		
		try
		{
			federateOne.rtiamb.connect( federateOne.fedamb, CallbackModel.HLA_EVOKED );
			expectedException( AlreadyConnected.class );
			Assert.fail( "Expected AlreadyConnected exception, but received no exception" );
		}
		catch( AlreadyConnected ac )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AlreadyConnected.class );
		}
	}
	

	////////////////////////////////////////////////////////////////////////////////////////////
	// connect(ImmediateCallback) //////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testConnectWithImmediateCallbackMode()
	{
		// connect with the first federate, create a federation and join it
		federateOne.quickConnect();
		federateOne.quickCreate();
		federateOne.quickJoin();
		federateOne.quickPublish( "InteractionRoot.X" );
		
		// connect with the second federate, using immediate callback mode, and join
		federateTwo.quickConnectWithImmediateCallbacks();
		federateTwo.quickJoin();
		federateTwo.quickSubscribe( "InteractionRoot.X" );
		
		federateOne.quickSend( "InteractionRoot.X" );
		federateTwo.fedamb.waitForROInteraction( "InteractionRoot.X" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
