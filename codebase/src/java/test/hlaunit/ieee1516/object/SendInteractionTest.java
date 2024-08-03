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
package hlaunit.ieee1516.object;

import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InteractionClassNotDefined;
import hla.rti1516.InteractionClassNotPublished;
import hla.rti1516.InteractionParameterNotDefined;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.LogicalTime;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.RTIinternalError;
import hlaunit.ieee1516.common.Abstract1516Test;
import hlaunit.ieee1516.common.TestFederate;
import hlaunit.ieee1516.common.TestInteraction;
import hlaunit.ieee1516.common.TypeFactory;

import static hlaunit.ieee1516.common.TypeFactory.*;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"SendInteractionTest", "sendInteraction", "objectManagement"})
public class SendInteractionTest extends Abstract1516Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate secondFederate;
	private TestFederate thirdFederate;

	private byte[] tag;
	private ParameterHandleValueMap params;

	private int xHandle, xaHandle, xbHandle, xcHandle;
	private int yHandle, yaHandle, ybHandle, ycHandle;
	
	private InteractionClassHandle xClass, yClass;
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
		secondFederate = new TestFederate( "secondFederate", this );
		thirdFederate = new TestFederate( "thirdFederate", this );
		
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

		// get InteractionClassHandle versions of the handles //
		xClass = getInteractionHandle( xHandle );
		yClass = getInteractionHandle( yHandle );

		// create the default set of parameters to use
		this.params = TypeFactory.newParameterMap();
		this.params.put( getParameterHandle(xaHandle), "xa".getBytes() );
		this.params.put( getParameterHandle(xbHandle), "xb".getBytes() );
		this.params.put( getParameterHandle(yaHandle), "ya".getBytes() );
		this.params.put( getParameterHandle(ybHandle), "yb".getBytes() );

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
	 * This method will check the values in the given {@link TestInteraction} and ensure that they
	 * are consistent with those that are expected given the subscription interests of the second
	 * federate.
	 */
	private void checkSecondFederateParameters( TestInteraction instance )
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
	 * This method will check the values in the given {@link TestInteraction} and ensure that they
	 * are consistent with those that are expected given the subscription interests of the third
	 * federate.
	 */
	private void checkThirdFederateParameters( TestInteraction instance )
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
	/////////////////////////////// Receive Order Test Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void sendInteraction( InteractionClassHandle theInteraction,
	//                              ParameterHandleValueMap theParameters,
	//                              byte[] userSuppliedTag )
	//        throws InteractionClassNotPublished,
	//               InteractionClassNotDefined,
	//               InteractionParameterNotDefined,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

	///////////////////////////////////////
	// TEST: (valid) testROInteraction() //
	///////////////////////////////////////
	@Test
	public void testROInteraction()
	{
		// prepare and send the interaction //
		try
		{
			defaultFederate.rtiamb.sendInteraction( yClass, params, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid RO interacion", e );
		}
		
		// wait for the interaction in the other federates //
		TestInteraction temp = secondFederate.fedamb.waitForROInteraction( xHandle );
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
			LogicalTime time = TypeFactory.createTime( 10.0 );
			defaultFederate.rtiamb.sendInteraction( yClass, params, tag, time );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid RO interacion with timestamp", e );
		}
		
		// wait for the interaction in the other federates //
		TestInteraction temp = secondFederate.fedamb.waitForROInteraction( xHandle );
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
			defaultFederate.rtiamb.sendInteraction( yClass, params, tag, null );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid RO interacion with null timestamp", e );
		}
		
		// wait for the interaction in the other federates //
		TestInteraction temp = secondFederate.fedamb.waitForROInteraction( xHandle );
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
			ParameterHandleValueMap empty = TypeFactory.newParameterMap();
			defaultFederate.rtiamb.sendInteraction( yClass, empty, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during valid RO interacion with empty parameter set", e );
		}
		
		// make sure the subscribers got no parameters
		TestInteraction temp = secondFederate.fedamb.waitForROInteraction( xHandle );
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
			defaultFederate.rtiamb.sendInteraction( yClass, null, tag );
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
			ParameterHandleValueMap set = TypeFactory.newParameterMap();
			set.put( getParameterHandle(yaHandle), "ya".getBytes() );
			set.put( getParameterHandle(ybHandle), "yb".getBytes() );
			set.put( getParameterHandle(ycHandle), "yc".getBytes() );
			defaultFederate.rtiamb.sendInteraction( yClass, set, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected Exception while sending RO interaction", e );
		}
		
		// make sure the second federate gets the interaction, but with no parameters
		TestInteraction interaction = secondFederate.fedamb.waitForROInteraction( xHandle );
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
			defaultFederate.rtiamb.sendInteraction( getInteractionHandle(1111111111), params, tag );
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
			defaultFederate.rtiamb.sendInteraction( getInteractionHandle(-111111111), params, tag );
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
			defaultFederate.rtiamb.sendInteraction( xClass, params, tag );
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
			params.put( getParameterHandle(123456), "bad".getBytes() );
			defaultFederate.rtiamb.sendInteraction( yClass, params, tag );
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
	
	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Timestamp Order Test Methods //////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public MessageRetractionReturn sendInteraction( InteractionClassHandle theInteraction,
	//                                                 ParameterHandleValueMap theParameters,
	//                                                 byte[] userSuppliedTag,
	//                                                 LogicalTime theTime )
	//        throws InteractionClassNotPublished,
	//               InteractionClassNotDefined,
	//               InteractionParameterNotDefined,
	//               InvalidLogicalTime,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

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
			LogicalTime time = createTime( 10.0 );
			defaultFederate.rtiamb.sendInteraction( yClass, params, tag, time );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid TSO interaction send", e );
		}
		
		//////////////////////////////////////////////////////////////
		// receive the interaction in the third federate right away //
		//////////////////////////////////////////////////////////////
		TestInteraction interaction = thirdFederate.fedamb.waitForROInteraction( yHandle );
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
			ParameterHandleValueMap empty = TypeFactory.newParameterMap();
			defaultFederate.rtiamb.sendInteraction( yClass, empty, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during valid RO interacion with empty parameter set", e );
		}
		
		// make sure the subscribers got no parameters
		TestInteraction temp = secondFederate.fedamb.waitForROInteraction( xHandle );
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
			ParameterHandleValueMap set = TypeFactory.newParameterMap();
			LogicalTime time = createTime( 6.0 );
			set.put( getParameterHandle(yaHandle), "ya".getBytes() );
			set.put( getParameterHandle(ybHandle), "yb".getBytes() );
			set.put( getParameterHandle(ycHandle), "yc".getBytes() );
			defaultFederate.rtiamb.sendInteraction( yClass, set, tag, time );
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
		TestInteraction interaction = secondFederate.fedamb.waitForTSOInteraction( xHandle );
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
		LogicalTime time = createTime( 10.0 );

		// try and send an interaction with null as the parameters
		try
		{
			defaultFederate.rtiamb.sendInteraction( yClass, null, tag, time );
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
			LogicalTime time = createTime( -11.0 );
			defaultFederate.rtiamb.sendInteraction( yClass, params, tag, time );
			expectedException( InvalidLogicalTime.class );
		}
		catch( InvalidLogicalTime ift )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidLogicalTime.class );
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
			LogicalTime time = createTime( 1.0 );
			defaultFederate.rtiamb.sendInteraction( yClass, params, tag, time );
			expectedException( InvalidLogicalTime.class );
		}
		catch( InvalidLogicalTime ift )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidLogicalTime.class );
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
			LogicalTime time = createTime( 10.0 );
			defaultFederate.rtiamb.sendInteraction( yClass, params, tag, time );
			expectedException( InvalidLogicalTime.class );
		}
		catch( InvalidLogicalTime ift )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidLogicalTime.class );
		}
	}

	//////////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithNonExistentClassHandle() //
	//////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithNonExistentClassHandle()
	{
		defaultFederate.quickEnableRegulating( 5.0 );
		LogicalTime time = createTime( 5.0 );

		// try and send an interaction that doesn't exist
		try
		{
			defaultFederate.rtiamb.sendInteraction( getInteractionHandle(1111111111),
			                                        params,
			                                        tag,
			                                        time );
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
		LogicalTime time = createTime( 5.0 );
		
		// try and send with a negative interaction handle
		try
		{
			defaultFederate.rtiamb.sendInteraction( getInteractionHandle(-1111111111),
			                                        params,
			                                        tag,
			                                        time );
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
		LogicalTime time = createTime( 5.0 );

		// try and send an unpublished interaction
		try
		{
			defaultFederate.rtiamb.sendInteraction( xClass, params, tag, time );
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
		LogicalTime time = createTime( 5.0 );

		// try and send an interaction with an unknown parameter handle
		try
		{
			params.put( getParameterHandle(123456), "bad".getBytes() );
			defaultFederate.rtiamb.sendInteraction( yClass, params, tag, time );
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

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
