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
package hlaunit.hla13.object;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import hla.rti.FederateNotExecutionMember;
import hla.rti.InteractionClassNotDefined;
import hla.rti.InteractionClassNotPublished;
import hla.rti.InteractionParameterNotDefined;
import hla.rti.InvalidFederationTime;
import hla.rti.LogicalTime;
import hla.rti.RTIinternalError;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hla.rti.SuppliedParameters;
import hlaunit.hla13.TestSetup;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;
import hlaunit.hla13.common.Test13Interaction;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"SendInteractionTest", "sendInteraction", "objectManagement"})
public class SendInteractionTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate secondFederate;
	private Test13Federate thirdFederate;

	private byte[] tag;
	private SuppliedParameters params;

	private int xHandle, xaHandle, xbHandle, xcHandle;
	private int yHandle, yaHandle, ybHandle, ycHandle;
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@BeforeClass(alwaysRun=true)
	public void beforeClass()
	{
		super.beforeClass();
		secondFederate = new Test13Federate( "secondFederate", this );
		thirdFederate = new Test13Federate( "thirdFederate", this );
		
		this.tag = "letag".getBytes();
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
		thirdFederate.quickJoin();
		
		// update the handle information //
		xHandle  = defaultFederate.quickICHandle( "InteractionRoot.X" );
		xaHandle = defaultFederate.quickPCHandle( "InteractionRoot.X", "xa" );
		xbHandle = defaultFederate.quickPCHandle( "InteractionRoot.X", "xb" );
		xcHandle = defaultFederate.quickPCHandle( "InteractionRoot.X", "xc" );
		yHandle  = defaultFederate.quickICHandle( "InteractionRoot.X.Y" );
		yaHandle = defaultFederate.quickPCHandle( "InteractionRoot.X.Y", "ya" );
		ybHandle = defaultFederate.quickPCHandle( "InteractionRoot.X.Y", "yb" );
		ycHandle = defaultFederate.quickPCHandle( "InteractionRoot.X.Y", "yc" );

		// create the default set of parameters to use
		this.params = TestSetup.getRTIFactory().createSuppliedParameters();
		this.params.add( xaHandle, "xa".getBytes() );
		this.params.add( xbHandle, "xb".getBytes() );
		this.params.add( yaHandle, "ya".getBytes() );
		this.params.add( ybHandle, "yb".getBytes() );

		// do publish and subscribe //
		defaultFederate.quickPublish( "InteractionRoot.X.Y" );
		secondFederate.quickSubscribe( "InteractionRoot.X" );
		thirdFederate.quickSubscribe( "InteractionRoot.X.Y" );
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		thirdFederate.quickResign();
		secondFederate.quickResign();
		defaultFederate.quickResign();
		defaultFederate.quickDestroy();
	}
	
	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// Helper Methods /////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will check the values in the given {@link Test13Interaction} and ensure that they
	 * are consistent with those that are expected given the subscription interests of the second
	 * federate.
	 */
	private void checkSecondFederateParameters( Test13Interaction instance )
	{
		Assert.assertEquals( xHandle, instance.getClassHandle(),
		                     "Second federate received wrong interaction class type" );
		Assert.assertEquals( "xa".getBytes(), instance.getParameterValue( xaHandle ),
		                     "Second federate has wrong value for parameter xa" );
		Assert.assertEquals( "xb".getBytes(), instance.getParameterValue( xbHandle ),
		                     "Second federate has wrong value for parameter xb" );
		Assert.assertNull( instance.getParameterValue(xcHandle),
		                   "Second federate has wrong value for attribute xc, not null" );
		Assert.assertNull( instance.getParameterValue(yaHandle), 
		                   "Second federate has wrong value for attribute ya, not null" );
		Assert.assertNull( instance.getParameterValue(ybHandle),
		                   "Second federate has wrong value for attribute yb, not null" );
		Assert.assertNull( instance.getParameterValue(ycHandle),
		                   "Second federate has wrong value for attribute yc, not null" );
	}
	
	/**
	 * This method will check the values in the given {@link Test13Interaction} and ensure that they
	 * are consistent with those that are expected given the subscription interests of the third
	 * federate.
	 */
	private void checkThirdFederateParameters( Test13Interaction instance )
	{
		// ensure that it has all the appropriate values //
		Assert.assertEquals( yHandle, instance.getClassHandle(),
		                     "Third federate received wrong interaction class type" );
		Assert.assertEquals( "xa".getBytes(), instance.getParameterValue( xaHandle ),
		                     "Third federate has wrong value for parameter xa" );
		Assert.assertEquals( "xb".getBytes(), instance.getParameterValue( xbHandle ),
		                     "Third federate has wrong value for parameter xb" );
		Assert.assertEquals( "ya".getBytes(), instance.getParameterValue( yaHandle ),
		                     "Third federate has wrong value for parameter ya" );
		Assert.assertEquals( "yb".getBytes(), instance.getParameterValue( ybHandle ),
		                     "Third federate has wrong value for parameter yb" );
		Assert.assertNull( instance.getParameterValue(xcHandle),
		                   "Third federate has wrong value for parameter xc" );
		Assert.assertNull( instance.getParameterValue(ycHandle),
		                   "Third federate has wrong value for parameter yc" );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// General Update Methods /////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * According to a bug report filed against Portico (using JGroups), there are problems with
	 * bytes being zero'd out after a certain number when sending large updates (GH #65). This
	 * method generates an interaction with a parameter of size 1MB to test for this behaviour.
	 * Random values are used to fill the byte[]'s sent.
	 */
	@Test(groups="jgroups")
	public void testROInteractionWithLargeParameterValue()
	{
		// let's just specify how large a message we want to use for testing in one
		// place so that we can change it quickly.
		int payloadSize = 1048576; // 1MiB

		byte[] sentArray = new byte[payloadSize];
		Random random = new Random();
		random.nextBytes( sentArray );
		
		// package this into an interaction and sent it from the sender to the receiver
		Map<String,byte[]> parameters = new HashMap<String,byte[]>();
		parameters.put( "xa", sentArray );
		defaultFederate.quickSend( "InteractionRoot.X.Y", parameters, null );
		
		// validate that the values reach the other side ok
		Test13Interaction temp = secondFederate.fedamb.waitForROInteraction( "InteractionRoot.X" );
		
		// ensure that it has all the appropriate values
		byte[] receivedArray = temp.getParameterValue( xaHandle );
		assertNotNull( receivedArray, "did not receive update for correct parameter" );
		assertEquals( receivedArray.length, payloadSize, "received wrong amount of data" );
		for( int i = 0; i < 1024; i++ )
		{
			assertEquals( receivedArray[i], sentArray[i], "byte at ["+i+"] was incorrect" );
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Receive Order Test Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void sendInteraction( int theInteraction,
	//                              SuppliedParameters theParameters,
	//                              byte[] userSuppliedTag )
	//        throws InteractionClassNotDefined,
	//               InteractionClassNotPublished,
	//               InteractionParameterNotDefined,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	///////////////////////////////////////
	// TEST: (valid) testROInteraction() //
	///////////////////////////////////////
	@Test
	public void testROInteraction()
	{
		// prepare and send the interaction //
		try
		{
			defaultFederate.rtiamb.sendInteraction( yHandle, params, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid RO interacion", e );
		}
		
		// wait for the interaction in the other federates //
		Test13Interaction temp = secondFederate.fedamb.waitForROInteraction( xHandle );
		// ensure that it has all the appropriate values //
		checkSecondFederateParameters( temp );
		
		temp = thirdFederate.fedamb.waitForROInteraction( yHandle );
		// ensure that it has all the appropriate values //
		checkThirdFederateParameters( temp );
	}
	
	////////////////////////////////////////////////////
	// TEST: (valid) testROInteractionWithTimestamp() //
	////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithTimestamp()
	{
		// setup
		thirdFederate.quickEnableAsyncDelivery();
		thirdFederate.quickEnableConstrained();
		
		// prepare and send the interaction //
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.sendInteraction( yHandle, params, tag, time );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid RO interacion with timestamp", e );
		}
		
		// wait for the interaction in the other federates //
		Test13Interaction temp = secondFederate.fedamb.waitForROInteraction( xHandle );
		// ensure that it has all the appropriate values //
		checkSecondFederateParameters( temp );
		
		temp = thirdFederate.fedamb.waitForROInteraction( yHandle );
		// ensure that it has all the appropriate values //
		checkThirdFederateParameters( temp );
	}
	
	////////////////////////////////////////////////////////
	// TEST: (valid) testROInteractionWithNullTimestamp() //
	////////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithNullTimestamp()
	{
		// setup
		thirdFederate.quickEnableAsyncDelivery();
		thirdFederate.quickEnableConstrained();
		
		// prepare and send the interaction //
		try
		{
			defaultFederate.rtiamb.sendInteraction( yHandle, params, tag, null );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid RO interacion with null timestamp", e );
		}
		
		// wait for the interaction in the other federates //
		Test13Interaction temp = secondFederate.fedamb.waitForROInteraction( xHandle );
		// ensure that it has all the appropriate values //
		checkSecondFederateParameters( temp );
		
		temp = thirdFederate.fedamb.waitForROInteraction( yHandle );
		// ensure that it has all the appropriate values //
		checkThirdFederateParameters( temp );
	}

	////////////////////////////////////////////////////////////
	// TEST: (valid) testROInteractionWithEmptyParameterSet() //
	////////////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithEmptyParameterSet()
	{
		// prepare and send the interaction //
		try
		{
			SuppliedParameters empty = TestSetup.getRTIFactory().createSuppliedParameters();
			defaultFederate.rtiamb.sendInteraction( yHandle, empty, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during valid RO interacion with empty parameter set", e );
		}
		
		// make sure the subscribers got no parameters
		Test13Interaction temp = secondFederate.fedamb.waitForROInteraction( xHandle );
		Assert.assertEquals( temp.getParameters().size(), 0,
		                     "Received parameter values when null set was sent for interaction" );

		temp = null;
		temp = thirdFederate.fedamb.waitForROInteraction( yHandle );
		Assert.assertEquals( temp.getParameters().size(), 0,
		                     "Received parameter values when null set was sent for interaction" );
	}
	
	///////////////////////////////////////////////////
	// TEST: testROInteractionWithNullParameterSet() //
	///////////////////////////////////////////////////
	@Test
	public void testROInteractionWithNullParameterSet()
	{
		// try and send an interaction with null as the parameters
		try
		{
			defaultFederate.rtiamb.sendInteraction( yHandle, null, tag );
			expectedException( RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RTIinternalError.class );
		}
	}

	////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testROInteractionReceivedWithNoParamsDueToSubscription() //
	////////////////////////////////////////////////////////////////////////////
	/**
	 * this will involve sending an attribute of type InteractionRoot.X.Y that
	 * only contains parameters for ya, yb and/or yc (but none for those parameters
	 * declared in InteractionRoot.X). This should result in the second federate
	 * receiving no parameters, while the third receives all those sent
	 */
	@Test
	public void testROInteractionReceivedWithNoParamsDueToSubscription()
	{
		// send the interactions
		try
		{
			SuppliedParameters set = TestSetup.getRTIFactory().createSuppliedParameters();
			set.add( yaHandle, "ya".getBytes() );
			set.add( ybHandle, "yb".getBytes() );
			set.add( ycHandle, "yc".getBytes() );
			defaultFederate.rtiamb.sendInteraction( yHandle, set, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected Exception while sending RO interaction", e );
		}
		
		// make sure the second federate gets the interaction, but with no parameters
		Test13Interaction interaction = secondFederate.fedamb.waitForROInteraction( xHandle );
		Assert.assertEquals( interaction.getParameters().size(), 0,
		                     "Received parameters when subscription status says we shouldn't" );
		
		// make sure the third federate gets all the parameters
		interaction = null;
		interaction = thirdFederate.fedamb.waitForROInteraction( yHandle );
		Assert.assertEquals( interaction.getParameters().size(), 3,
		                     "Received 0 parameters when subscription says we should get 3" );
	}

	/////////////////////////////////////////////////////////
	// TEST: testROInteractionWithNonExistentClassHandle() //
	/////////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithNonExistentClassHandle()
	{
		// try and send an interaction that doesn't exist
		try
		{
			defaultFederate.rtiamb.sendInteraction( 1111111111, params, tag );
			expectedException( InteractionClassNotDefined.class );
		}
		catch( InteractionClassNotDefined icnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InteractionClassNotDefined.class );
		}
	}
	
	//////////////////////////////////////////////////////
	// TEST: testROInteractionWithNegativeClassHandle() //
	//////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithNegativeClassHandle()
	{
		// try and send with a negative interaction handle
		try
		{
			defaultFederate.rtiamb.sendInteraction( -1111111111, params, tag );
			expectedException( InteractionClassNotDefined.class );
		}
		catch( InteractionClassNotDefined icnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InteractionClassNotDefined.class );
		}
	}
	
	///////////////////////////////////////////////
	// TEST: testROInteractionWhenNotPublished() //
	///////////////////////////////////////////////
	@Test
	public void testROInteractionWhenNotPublished()
	{
		// try and send an unpublished interaction
		try
		{
			defaultFederate.rtiamb.sendInteraction( xHandle, params, tag );
			expectedException( InteractionClassNotPublished.class );
		}
		catch( InteractionClassNotPublished icnp )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InteractionClassNotPublished.class );
		}
	}
	
	///////////////////////////////////////////////////////////
	// TEST: testROInteractionWithUndefinedParameterHandle() //
	///////////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithUndefinedParameterHandle()
	{
		// try and send an interaction with an unknown parameter handle
		try
		{
			params.add( 123456, "bad".getBytes() );
			defaultFederate.rtiamb.sendInteraction( yHandle, params, tag );
			expectedException( InteractionParameterNotDefined.class );
		}
		catch( InteractionParameterNotDefined ipnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InteractionParameterNotDefined.class );
		}
	}

	////////////////////////////////////////////
	// TEST: testROInteractionWhenNotJoined() //
	////////////////////////////////////////////
	@Test
	public void testROInteractionWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			defaultFederate.rtiamb.sendInteraction( yHandle, params, tag );
			expectedException( FederateNotExecutionMember.class );
		}
		catch( FederateNotExecutionMember fnem )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, FederateNotExecutionMember.class );
		}
	}

	/////////////////////////////////////////////////
	// TEST: testROInteractionWhenSaveInProgress() //
	/////////////////////////////////////////////////
	@Test
	public void testROInteractionWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.sendInteraction( yHandle, params, tag );
			expectedException( SaveInProgress.class );
		}
		catch( SaveInProgress sip )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, SaveInProgress.class );
		}
	}
	
	////////////////////////////////////////////////////
	// TEST: testROInteractionWhenRestoreInProgress() //
	////////////////////////////////////////////////////
	@Test
	public void testROInteractionWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.sendInteraction( yHandle, params, tag );
			expectedException( RestoreInProgress.class );
		}
		catch( RestoreInProgress rip )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RestoreInProgress.class );
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Timestamp Order Test Methods //////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public EventRetractionHandle sendInteraction( int theInteraction,
	//                                               SuppliedParameters theParameters,
	//                                               byte[] userSuppliedTag,
	//                                               LogicalTime theTime )
	//        throws InteractionClassNotDefined,
	//               InteractionClassNotPublished,
	//               InteractionParameterNotDefined,
	//               InvalidFederationTime,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	////////////////////////////////////////
	// TEST: (valid) testTSOInteraction() //
	////////////////////////////////////////
	@Test
	public void testTSOInteraction()
	{
		/////////////////////////
		// initialize the test //
		/////////////////////////
		// enable regulation and constrained as required for the federates //
		defaultFederate.quickEnableRegulating( 5.0 );
		secondFederate.quickEnableConstrained();
		secondFederate.quickEnableAsyncDelivery();
		// third federate is neither regulating nor constrained
		
		//////////////////////////////////////////////
		// send the interaction with a time of 10.0 //
		//////////////////////////////////////////////
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.sendInteraction( yHandle, params, tag, time );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid TSO interaction send", e );
		}
		
		//////////////////////////////////////////////////////////////
		// receive the interaction in the third federate right away //
		//////////////////////////////////////////////////////////////
		Test13Interaction interaction = thirdFederate.fedamb.waitForROInteraction( yHandle );
		// ensure it has the appropriate values //
		Assert.assertEquals( interaction.getTimestamp(), -1.0,
		                     "TSO interaction received as TSO by non-constrained" );
		checkThirdFederateParameters( interaction );
		
		/////////////////////////////////////////////////////////////////////
		// ensure the constrained federate hasn't got any premature update //
		/////////////////////////////////////////////////////////////////////
		secondFederate.fedamb.waitForTSOInteractionTimeout( xHandle );
		// no interaction, set the advance process in motion
		defaultFederate.quickAdvanceAndWait( 100.0 ); // that should be plenty :P
		// request the time advance
		secondFederate.quickAdvanceRequest( 10.0 );
		
		// wait for the interaction in the other federates //
		interaction = secondFederate.fedamb.waitForTSOInteraction( xHandle );
		// ensure that it has all the appropriate values //
		Assert.assertEquals( interaction.getTimestamp(), 10.0,
		                     "Wrong timestamp for received TSO interaction" );
		checkSecondFederateParameters( interaction );
		
		// wait for the time advance to be granted //
		secondFederate.fedamb.waitForTimeAdvance( 10.0 );
	}
	
	/////////////////////////////////////////////////////////////
	// TEST: (valid) testTSOInteractionWithEmptyParameterSet() //
	/////////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithEmptyParameterSet()
	{
		// setup
		defaultFederate.quickEnableRegulating( 5.0 );
		secondFederate.quickEnableAsyncDelivery();
		secondFederate.quickEnableConstrained();
		// third federate is not constraineed
		
		// prepare and send the interaction //
		try
		{
			SuppliedParameters empty = TestSetup.getRTIFactory().createSuppliedParameters();
			defaultFederate.rtiamb.sendInteraction( yHandle, empty, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during valid RO interacion with empty parameter set", e );
		}
		
		// make sure the subscribers got no parameters
		Test13Interaction temp = secondFederate.fedamb.waitForROInteraction( xHandle );
		Assert.assertEquals( temp.getParameters().size(), 0,
		                     "Received parameter values when null set was sent for interaction" );

		temp = null;
		temp = thirdFederate.fedamb.waitForROInteraction( yHandle );
		Assert.assertEquals( temp.getParameters().size(), 0,
		                     "Received parameter values when null set was sent for interaction" );

	}
	
	/////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testTSOInteractionReceivedWithNoParamsDueToSubscription() //
	/////////////////////////////////////////////////////////////////////////////
	/**
	 * Same as {@link #testROInteractionReceivedWithNoParamsDueToSubscription()} except that both
	 * subscribing federates should received the interaction as TSO
	 */
	@Test
	public void testTSOInteractionReceivedWithNoParamsDueToSubscription()
	{
		// setup
		defaultFederate.quickEnableRegulating( 5.0 );
		secondFederate.quickEnableAsyncDelivery();
		secondFederate.quickEnableConstrained();
		thirdFederate.quickEnableAsyncDelivery();
		thirdFederate.quickEnableConstrained();
		
		// send the interactions
		try
		{
			SuppliedParameters set = TestSetup.getRTIFactory().createSuppliedParameters();
			LogicalTime time = defaultFederate.createTime( 6.0 );
			set.add( yaHandle, "ya".getBytes() );
			set.add( ybHandle, "yb".getBytes() );
			set.add( ycHandle, "yc".getBytes() );
			defaultFederate.rtiamb.sendInteraction( yHandle, set, tag, time );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected Exception while sending RO interaction", e );
		}
		
		// make sure the second federate gets the interaction, but with no parameters
		secondFederate.fedamb.waitForTSOInteractionTimeout( xHandle ); // we haven't advanced yet
		secondFederate.quickAdvanceRequest( 10.0 );

		// we still shouldn't get it yet, because the RTI can't guarantee messages with
		// a timestamp pre-6.0 won't come until we advance the default federate
		secondFederate.fedamb.waitForTSOInteractionTimeout( xHandle ); // we haven't advanced yet

		// advance the regulating federate and wait for the callbacks.
		defaultFederate.quickAdvanceAndWait( 10.0 );
		Test13Interaction interaction = secondFederate.fedamb.waitForTSOInteraction( xHandle );
		Assert.assertEquals( interaction.getTimestamp(), 6.0,
		                     "Wrong timestamp on received interaction" );
		Assert.assertEquals( interaction.getParameters().size(), 0,
		                     "Received parameters when subscription status says we shouldn't" );
		
		// make sure the third federate gets all the parameters
		interaction = null;
		thirdFederate.fedamb.waitForTSOInteractionTimeout( yHandle ); // we haven't advanced yet
		thirdFederate.quickAdvanceRequest( 10.0 );
		interaction = thirdFederate.fedamb.waitForTSOInteraction( yHandle );
		Assert.assertEquals( interaction.getTimestamp(), 6.0,
		                     "Wrong timestamp on received interaction" );
		Assert.assertEquals( interaction.getParameters().size(), 3,
		                     "Received 0 parameters when subscription says we should get 3" );
		
		// make sure we get the time advances, just to be sure everything is going well ;)
		secondFederate.fedamb.waitForTimeAdvance( 10.0 );
		thirdFederate.fedamb.waitForTimeAdvance( 10.0 );
	}

	////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithNullParameterSet() //
	////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithNullParameterSet()
	{
		// setup
		defaultFederate.quickEnableRegulating( 5.0 );
		LogicalTime time = defaultFederate.createTime( 10.0 );

		// try and send an interaction with null as the parameters
		try
		{
			defaultFederate.rtiamb.sendInteraction( yHandle, null, tag, time );
			expectedException( RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RTIinternalError.class );
		}

	}

	/////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithNegativeTimestamp() //
	/////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithNegativeTimestamp()
	{
		defaultFederate.quickEnableRegulating( 5.0 );
		
		// try and send an interaction with a negative time
		try
		{
			LogicalTime time = defaultFederate.createTime( -11.0 );
			defaultFederate.rtiamb.sendInteraction( yHandle, params, tag, time );
			expectedException( InvalidFederationTime.class );
		}
		catch( InvalidFederationTime ift )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidFederationTime.class );
		}
	}
	
	/////////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithTimestampLessThanLBTS() //
	/////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithTimestampLessThanLBTS()
	{
		defaultFederate.quickEnableRegulating( 5.0 );

		// send an interaction  with a time that is less than our LBTS
		try
		{
			LogicalTime time = defaultFederate.createTime( 1.0 );
			defaultFederate.rtiamb.sendInteraction( yHandle, params, tag, time );
			expectedException( InvalidFederationTime.class );
		}
		catch( InvalidFederationTime ift )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidFederationTime.class );
		}
	}

	///////////////////////////////////////////////////
	// TEST: testTSOInteractionWithTimestampInPast() //
	///////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithTimestampInPast()
	{
		// setup
		defaultFederate.quickEnableRegulating( 5.0 );
		defaultFederate.quickAdvanceAndWait( 50.0 );
		
		// send an interaction  with a time that is less than our LBTS
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.sendInteraction( yHandle, params, tag, time );
			expectedException( InvalidFederationTime.class );
		}
		catch( InvalidFederationTime ift )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidFederationTime.class );
		}
	}

	//////////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithNonExistentClassHandle() //
	//////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithNonExistentClassHandle()
	{
		defaultFederate.quickEnableRegulating( 5.0 );
		LogicalTime time = defaultFederate.createTime( 5.0 );

		// try and send an interaction that doesn't exist
		try
		{
			defaultFederate.rtiamb.sendInteraction( 1111111111, params, tag, time );
			expectedException( InteractionClassNotDefined.class );
		}
		catch( InteractionClassNotDefined icnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InteractionClassNotDefined.class );
		}
	}
	
	///////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithNegativeClassHandle() //
	///////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithNegativeClassHandle()
	{
		defaultFederate.quickEnableRegulating( 5.0 );
		LogicalTime time = defaultFederate.createTime( 5.0 );
		
		// try and send with a negative interaction handle
		try
		{
			defaultFederate.rtiamb.sendInteraction( -1111111111, params, tag, time );
			expectedException( InteractionClassNotDefined.class );
		}
		catch( InteractionClassNotDefined icnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InteractionClassNotDefined.class );
		}
	}
	
	////////////////////////////////////////////////
	// TEST: testTSOInteractionWhenNotPublished() //
	////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWhenNotPublished()
	{
		defaultFederate.quickEnableRegulating( 5.0 );
		LogicalTime time = defaultFederate.createTime( 5.0 );

		// try and send an unpublished interaction
		try
		{
			defaultFederate.rtiamb.sendInteraction( xHandle, params, tag, time );
			expectedException( InteractionClassNotPublished.class );
		}
		catch( InteractionClassNotPublished icnp )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InteractionClassNotPublished.class );
		}
	}
	
	////////////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithUndefinedParameterHandle() //
	////////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithUndefinedParameterHandle()
	{
		defaultFederate.quickEnableRegulating( 5.0 );
		LogicalTime time = defaultFederate.createTime( 5.0 );

		// try and send an interaction with an unknown parameter handle
		try
		{
			params.add( 123456, "bad".getBytes() );
			defaultFederate.rtiamb.sendInteraction( yHandle, params, tag, time );
			expectedException( InteractionParameterNotDefined.class );
		}
		catch( InteractionParameterNotDefined ipnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InteractionParameterNotDefined.class );
		}
	}

	/////////////////////////////////////////////
	// TEST: testTSOInteractionWhenNotJoined() //
	/////////////////////////////////////////////
	@Test
	public void testTSOInteractionWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.sendInteraction( yHandle, params, tag, time );
			expectedException( FederateNotExecutionMember.class );
		}
		catch( FederateNotExecutionMember fnem )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, FederateNotExecutionMember.class );
		}
	}

	//////////////////////////////////////////////////
	// TEST: testTSOInteractionWhenSaveInProgress() //
	//////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.sendInteraction( yHandle, params, tag, time );
			expectedException( SaveInProgress.class );
		}
		catch( SaveInProgress sip )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, SaveInProgress.class );
		}
	}
	
	/////////////////////////////////////////////////////
	// TEST: testTSOInteractionWhenRestoreInProgress() //
	/////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.sendInteraction( yHandle, params, tag, time );
			expectedException( RestoreInProgress.class );
		}
		catch( RestoreInProgress rip )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RestoreInProgress.class );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
