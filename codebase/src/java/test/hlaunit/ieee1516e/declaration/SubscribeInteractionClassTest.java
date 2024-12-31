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
package hlaunit.ieee1516e.declaration;

import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;
import hlaunit.ieee1516e.common.TypeFactory;

import static hlaunit.ieee1516e.common.TypeFactory.*;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"SubscribeInteractionClassTest",
                                   "subscribeInteraction",
                                   "subscribe",
                                   "pubsub",
                                   "declarationManagement"})
public class SubscribeInteractionClassTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int xHandle;
	private int yHandle;
	private InteractionClassHandle xClass;
	private InteractionClassHandle yClass;
	private TestFederate senderFederate;

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
		this.senderFederate = new TestFederate( "SenderFederate", this );
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		senderFederate.quickJoin();
		senderFederate.quickPublish( "InteractionRoot.X" );
		senderFederate.quickPublish( "InteractionRoot.X.Y" );
		
		// cache the interaction handles
		xHandle = defaultFederate.quickICHandle( "InteractionRoot.X" );
		xClass = TypeFactory.getInteractionHandle( xHandle );
		yHandle = defaultFederate.quickICHandle( "InteractionRoot.X.Y" );
		yClass = TypeFactory.getInteractionHandle( yHandle );
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		senderFederate.quickResign();
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
	 * This method validates that the given federate is subscribed to interactions of the
	 * given class handle by sending an interaction of that type. If the federate is subscribed,
	 * it should receive the interaction. If it isn't, it will time-out waiting for the interaction
	 * and Assert.fail() will be used to kill the test.
	 */
	private void validateSubscribed( TestFederate federate, int classHandle )
	{
		// send the interaction
		senderFederate.quickSend( classHandle, null, "tag".getBytes() );
		// wait for the interaction
		federate.fedamb.waitForROInteraction( classHandle );
	}
	
	/**
	 * This is the opposite of {@link #validateSubscribed(TestFederate, int)}. It will validate
	 * that the given federate is NOT subscribed to the interaction class of the provided handle.
	 * Once again, an interaction will be sent, however, this time, if the federate does NOT
	 * time-out waiting for it, Assert.fail will be used to kill the test.
	 */
	private void validateNotSubscribed( TestFederate federate, int classHandle )
	{
		// send the interaction
		senderFederate.quickSend( classHandle, null, "tag".getBytes() );
		// wait for the interaction
		federate.fedamb.waitForROInteractionTimeout( classHandle );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Active Subscription Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void subscribeInteractionClass( int theClass )
	//        throws InteractionClassNotDefined,
	//               FederateNotExecutionMember,
	//               FederateLoggingServiceCalls,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	/////////////////////////////////////
	// TEST: (valid) testICSubscribe() //
	/////////////////////////////////////
	@Test
	public void testICSubscribe()
	{
		// subscribe to the interaction
		try
		{
			defaultFederate.rtiamb.subscribeInteractionClass( xClass );
		}
		catch( Exception e )
		{
			unexpectedException( "subscribing to interaction class", e );
		}
		
		// validate the subscription
		validateSubscribed( defaultFederate, xHandle );
	}

	//////////////////////////////////////////////////
	// TEST: (valid) testICSubscribeToParentClass() //
	//////////////////////////////////////////////////
	@Test
	public void testICSubscribeToParentClass()
	{
		// subscribe to the interaction
		try
		{
			defaultFederate.rtiamb.subscribeInteractionClass( xClass );
		}
		catch( Exception e )
		{
			unexpectedException( "subscribing to interaction class", e );
		}
		
		// validate the subscription
		// have to do this by hand because we want to send an interaction of the child class
		// to make sure that our subscription to the parent gets it through to us
		senderFederate.quickSend( yHandle, null, "tag".getBytes() );
		defaultFederate.fedamb.waitForROInteraction( xHandle );
	}

	////////////////////////////////////////////////////////////////
	// TEST: (valid) testICSubscribeToPreviouslySubscribedClass() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testICSubscribeToPreviouslySubscribedClass()
	{
		// quickly subscribe to a class
		defaultFederate.quickSubscribe( xHandle );
		validateSubscribed( defaultFederate, xHandle );
		
		// resubscribe
		try
		{
			defaultFederate.rtiamb.subscribeInteractionClass( xClass );
		}
		catch( Exception e )
		{
			unexpectedException( "subscribing to interaction class", e );
		}
		
		// validate the subscription
		validateSubscribed( defaultFederate, xHandle );
	}

	///////////////////////////////////////////////
	// TEST: testICSubscribeWithUndefinedClass() //
	///////////////////////////////////////////////
	@Test
	public void testICSubscribeWithUndefinedClass()
	{
		try
		{
			defaultFederate.rtiamb.subscribeInteractionClass( getInteractionHandle(11111111) );
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

	//////////////////////////////////////////
	// TEST: testICSubscribeWhenNotJoined() //
	//////////////////////////////////////////
	@Test
	public void testICSubscribeWhenNotJoined()
	{
		// resign so we can run the test
		defaultFederate.quickResign();
		
		try
		{
			defaultFederate.rtiamb.subscribeInteractionClass( xClass );
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

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////// Subscription Methods For Registration Effects /////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	////////////
	// NOTICE //
	//////////////////////////////////////////////////////////////
	// One of the main points of these tests is to also ensure  //
	// that the appropriate start/stop registration methods are //
	// not called as a result of the subscription requests      //
	//                                                          //
	// The main subscription-behaviour is tested in other tests //
	//////////////////////////////////////////////////////////////
	
	///////////////////////////////////////////////////////////
	// TEST: (valid) testICSubscribeForRegistrationEffects() //
	///////////////////////////////////////////////////////////
	@Test
	public void testICSubscribeForRegistrationEffects()
	{
		log( "registration notification not yet supported" );
	}

	////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testICSubscribeToParentClassForRegistrationEffects() //
	////////////////////////////////////////////////////////////////////////
	@Test
	public void testICSubscribeToParentClassForRegistrationEffects()
	{
		log( "registration notification not yet supported" );
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testICSubscribeToPreviouslySubscribedClassForRegistrationEffects() //
	//////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testICSubscribeToPreviouslySubscribedClassForRegistrationEffects()
	{
		log( "registration notification not yet supported" );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Passive Subscription Methods //////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void subscribeInteractionClassPassively( int theClass )
	//        throws InteractionClassNotDefined,
	//               FederateNotExecutionMember,
	//               FederateLoggingServiceCalls,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	////////////
	// NOTICE //
	//////////////////////////////////////////////////////////////
	// One of the main points of these tests is to also ensure  //
	// that the appropriate start/stop registration methods are //
	// not called as a result of the subscription requests.     //
	//                                                          //
	// The main subscription-behaviour is tested in other tests //
	//////////////////////////////////////////////////////////////

	////////////////////////////////////////////
	// TEST: (valid) testICPassiveSubscribe() //
	////////////////////////////////////////////
	@Test
	public void testICPassiveSubscribe()
	{
		log( "passive subscription not yet supported" );
	}

	/////////////////////////////////////////////////////////
	// TEST: (valid) testICPassiveSubscribeToParentClass() //
	/////////////////////////////////////////////////////////
	@Test
	public void testICPassiveSubscribeToParentClass()
	{
		log( "passive subscription not yet supported" );
	}

	///////////////////////////////////////////////////////////////////////
	// TEST: (valid) testICPassiveSubscribeToPreviouslySubscribedClass() //
	///////////////////////////////////////////////////////////////////////
	@Test
	public void testICPassiveSubscribeToPreviouslySubscribedClass()
	{
		log( "passive subscription not yet supported" );
	}

	//////////////////////////////////////////////////////
	// TEST: testICPassiveSubscribeWithUndefinedClass() //
	//////////////////////////////////////////////////////
	@Test
	public void testICPassiveSubscribeWithUndefinedClass()
	{
		log( "passive subscription not yet supported" );
	}

	/////////////////////////////////////////////////
	// TEST: testICPassiveSubscribeWhenNotJoined() //
	/////////////////////////////////////////////////
	@Test
	public void testICPassiveSubscribeWhenNotJoined()
	{
		log( "passive subscription not yet supported" );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Unsubscription Methods /////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void unsubscribeInteractionClass( int theClass )
	//        throws InteractionClassNotDefined,
	//               InteractionClassNotSubscribed,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	///////////////////////////////////////
	// TEST: (valid) testICUnsubscribe() //
	///////////////////////////////////////
	@Test
	public void testICUnsubscribe()
	{
		// setup
		defaultFederate.quickSubscribe( xHandle );
		validateSubscribed( defaultFederate, xHandle );

		try
		{
			defaultFederate.rtiamb.unsubscribeInteractionClass( xClass );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing from interaction class", e );
		}
		
		// validate that we are no longer subscribed
		validateNotSubscribed( defaultFederate, xHandle );
	}

	///////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testICUnsubscribeChildClassDoesntAffectSuperClassSubscription() //
	///////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testICUnsubscribeChildClassDoesntAffectSuperClassSubscription()
	{
		// setup
		defaultFederate.quickSubscribe( xHandle );
		validateSubscribed( defaultFederate, xHandle );
		defaultFederate.quickSubscribe( yHandle );
		validateSubscribed( defaultFederate, yHandle );
		
		// unsubscribe from the child class
		try
		{
			defaultFederate.rtiamb.unsubscribeInteractionClass( yClass );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing from interaction class", e );
		}
		
		// we do the validation for the super-class by hand because we want to send
		// an interaction of the child class. as we are no longer subscribed to the
		// child, we should receive it as the parent
		senderFederate.quickSend( yHandle, null, "tag".getBytes() );
		// we wait twice, as there will be one queued from earlier
		defaultFederate.fedamb.waitForROInteraction( xHandle );
		
		// validate subscription remains for super class but not for child
		validateNotSubscribed( defaultFederate, yHandle );
	}
	
	///////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testICUnsubscribeSuperClassDoesntAffectChildClassSubscription() //
	///////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testICUnsubscribeSuperClassDoesntAffectChildClassSubscription()
	{
		// setup
		defaultFederate.quickSubscribe( xHandle );
		validateSubscribed( defaultFederate, xHandle );
		defaultFederate.quickSubscribe( yHandle );
		validateSubscribed( defaultFederate, yHandle );
		
		// unsubscribe from the child class
		try
		{
			defaultFederate.rtiamb.unsubscribeInteractionClass( xClass );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing from interaction class", e );
		}
		
		// validate subscription remains for super class but not for child
		validateSubscribed( defaultFederate, yHandle );
		validateNotSubscribed( defaultFederate, xHandle );
	}

	/////////////////////////////////////////////////////////
	// TEST: (valid) testICUnsubscribeNonSubscribedClass() //
	/////////////////////////////////////////////////////////
	@Test
	public void testICUnsubscribeNonSubscribedClass()
	{
		try
		{
			defaultFederate.rtiamb.unsubscribeInteractionClass( xClass );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing from unsubscribed interaction (valid in 1516)", e );
		}
	}

	//////////////////////////////////////////////////////////////////
	// TEST: (valid) testICUnsubscribeSuperClassOfSubscribedClass() //
	//////////////////////////////////////////////////////////////////
	@Test
	public void testICUnsubscribeSuperClassOfSubscribedClass()
	{
		// quick subscribe to a child class
		defaultFederate.quickSubscribe( yHandle );
		validateSubscribed( defaultFederate, yHandle );
		
		try
		{
			defaultFederate.rtiamb.unsubscribeInteractionClass( xClass );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing from super class of unsubscribed interaction" +
			                     "(valid in 1516)", e );
		}
	}

	///////////////////////////////////////////////////////
	// TEST: testICUnsubscribeWithUndefinedClassHandle() //
	///////////////////////////////////////////////////////
	@Test
	public void testICUnsubscribeWithUndefinedClassHandle()
	{
		try
		{
			defaultFederate.rtiamb.unsubscribeInteractionClass( getInteractionHandle(11111111) );
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

	////////////////////////////////////////////
	// TEST: testICUnsubscribeWhenNotJoined() //
	////////////////////////////////////////////
	@Test
	public void testICUnsubscribeWhenNotJoined()
	{
		// resign so we can run the test
		defaultFederate.quickResign();

		try
		{
			defaultFederate.rtiamb.unsubscribeInteractionClass( xClass );
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
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
