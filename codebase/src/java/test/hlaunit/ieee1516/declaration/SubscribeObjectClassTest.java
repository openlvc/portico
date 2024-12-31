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
package hlaunit.ieee1516.declaration;

import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeNotDefined;
import hla.rti1516.FederateNotExecutionMember;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectClassNotDefined;
import hla.rti1516.RTIinternalError;
import hlaunit.ieee1516.common.Abstract1516Test;
import hlaunit.ieee1516.common.TestFederate;
import hlaunit.ieee1516.common.TestObject;

import java.util.Set;

import static hlaunit.ieee1516.common.TypeFactory.*;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"SubscribeObjectClassTest",
                                   "subscribeObject",
                                   "subscribe",
                                   "pubsub",
                                   "declarationManagement"})
public class SubscribeObjectClassTest extends Abstract1516Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int aHandle, aaHandle, abHandle;
	private int bHandle, baHandle, bbHandle;
	
	private ObjectClassHandle aClass;
	private ObjectClassHandle bClass;

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
		this.senderFederate = new TestFederate( "senderFederate", this );
	}

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		senderFederate.quickJoin();
		
		// make sure the sending federate published everything we might need
		//  => both classes with all attributes except *c
		senderFederate.quickPublish( "ObjectRoot.A", "aa", "ab" );
		senderFederate.quickPublish( "ObjectRoot.A.B", "aa", "ab", "ba", "bb" );
		
		// cache the handle information //
		aHandle  = defaultFederate.quickOCHandle( "ObjectRoot.A" );
		aaHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "aa" );
		abHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ab" );
		bHandle  = defaultFederate.quickOCHandle( "ObjectRoot.A.B" );
		baHandle = defaultFederate.quickACHandle( "ObjectRoot.A.B", "ba" );
		bbHandle = defaultFederate.quickACHandle( "ObjectRoot.A.B", "bb" );
		
		aClass = getObjectClassHandle( aHandle );
		bClass = getObjectClassHandle( bHandle );
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
	 * Validates that the given federate is subscribed to the given class with the given set
	 * of attributes.
	 * 
	 * If the <code>strict</code> parameter is <code>true</code>, then this method will make
	 * sure that *ONLY* those attributes provided in the subscribed attributes list are received.
	 * If it isn't, it will just make sure that *at a minimum* the given attributes are subscribed
	 * to.
	 */
	private void validateSubscribed( boolean strict,
	                                 TestFederate sendingFederate,
	                                 int sendingClass,
	                                 int[] sendingAttributes,
	                                 TestFederate subscribingFederate,
	                                 int subscribedClass,
	                                 int[] subscribedAttributes )
	{
		// register an instance to test with
		int oHandle = sendingFederate.quickRegister( sendingClass );
		subscribingFederate.fedamb.waitForDiscovery( oHandle );

		// reflect some values
		sendingFederate.quickReflect( oHandle, sendingAttributes );
		subscribingFederate.fedamb.waitForROUpdate( oHandle );
		
		// validate the reflection
		TestObject instance = subscribingFederate.fedamb.getInstances().get( oHandle );
		Set<Integer> foundAttributes = instance.getAttributes().keySet();

		// make sure all the expected attributes are there
		for( int expectedHandle : subscribedAttributes )
		{
			// is the expected handle in the found set?
			if( foundAttributes.remove(expectedHandle) == false )
				Assert.fail( "Missing expected attribute: must not be subscribed to" );
		}
		
		// if we are doing strict checking, the number of expected attributes
		// should equal the number of found attributes
		if( strict && !foundAttributes.isEmpty() )
		{
			// we are strict and there were found attributes that we did not expect
			Assert.fail( "Found unexpected attribute" );
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Active Subscription Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void subscribeObjectClassAttributes( ObjectClassHandle theClass,
	//                                             AttributeHandleSet attributeList )
	//        throws ObjectClassNotDefined,
	//               AttributeNotDefined,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

	/////////////////////////////////////
	// TEST: (valid) testOCSubscribe() //
	/////////////////////////////////////
	@Test
	public void testOCSubscribe()
	{
		// do the subscription
		int[] subscribedTo = new int[]{ aaHandle };
		AttributeHandleSet ahs = newAttributeSet( subscribedTo );
		try
		{
			defaultFederate.rtiamb.subscribeObjectClassAttributes( aClass, ahs );
		}
		catch( Exception e )
		{
			unexpectedException( "subscribing to object attributes", e );
		}
		
		// validate the subscription
		validateSubscribed( true,
		                    senderFederate,
		                    aHandle,
		                    new int[]{ aaHandle, abHandle },
		                    defaultFederate,
		                    aHandle,
		                    subscribedTo );
	}

	//////////////////////////////////////////////////
	// TEST: (valid) testOCSubscribeToParentClass() //
	//////////////////////////////////////////////////
	/**
	 * This test will ensure that a subscription to a parent class will cause objects to
	 * be discovered at that class, rather than the class they are actually of (which is
	 * not subscribed to) or not at all.
	 */
	@Test
	public void testOCSubscribeToParentClass()
	{
		// do the subscription
		int[] subscribedTo = new int[]{ aaHandle };
		AttributeHandleSet ahs = newAttributeSet( subscribedTo );
		try
		{
			defaultFederate.rtiamb.subscribeObjectClassAttributes( aClass, ahs );
		}
		catch( Exception e )
		{
			unexpectedException( "subscribing to object attributes", e );
		}
		
		// validate the subscription
		validateSubscribed( true,
		                    senderFederate,
		                    bHandle,
		                    new int[]{ aaHandle, abHandle, baHandle, bbHandle },
		                    defaultFederate,
		                    aHandle,
		                    subscribedTo );
	}

	/////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCSubscribeToPreviouslySubscribedAttributes() //
	/////////////////////////////////////////////////////////////////////
	/**
	 * This will subscribe to the same set of attributes twice, ensuring that there is no
	 * change triggered. The reponse of the RTI in this cause would be the same for 1.3 as
	 * it is for 1516: no change. In 1.3, this is because the new subscription replaces the
	 * older one (but they're the same set, so no appreciable change occurs). In 1516, this
	 * is because multiple subscription requests "add" to previous ones, and as these are
	 * the same attributes, there is nothing to add.
	 */
	@Test
	public void testOCSubscribeToPreviouslySubscribedAttributes()
	{
		////////////////////////////////////////
		// do the subscription the first time //
		////////////////////////////////////////
		int[] subscribedTo = new int[]{ aaHandle };
		AttributeHandleSet ahs = newAttributeSet( subscribedTo );
		try
		{
			defaultFederate.rtiamb.subscribeObjectClassAttributes( aClass, ahs );
		}
		catch( Exception e )
		{
			unexpectedException( "subscribing to object attributes", e );
		}
		
		// validate the subscription
		validateSubscribed( true,
		                    senderFederate,
		                    bHandle,
		                    new int[]{ aaHandle, abHandle, baHandle, bbHandle },
		                    defaultFederate,
		                    aHandle,
		                    subscribedTo );

		///////////////////////////////
		// do the subscription again //
		///////////////////////////////
		AttributeHandleSet ahs2 = newAttributeSet( subscribedTo );
		try
		{
			defaultFederate.rtiamb.subscribeObjectClassAttributes( aClass, ahs2 );
		}
		catch( Exception e )
		{
			unexpectedException( "subscribing to object attributes", e );
		}
		
		// validate the subscription
		validateSubscribed( true,
		                    senderFederate,
		                    bHandle,
		                    new int[]{ aaHandle, abHandle, baHandle, bbHandle },
		                    defaultFederate,
		                    aHandle,
		                    subscribedTo );
	}

	////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCSubscribeToPartialPreviouslySubscribedAttributes() //
	////////////////////////////////////////////////////////////////////////////
	/**
	 * This test is like {@link #testOCSubscribeToPreviouslySubscribedAttributes()}, except
	 * that the set of attributes used will NOT be the same for each subscribe. That is, it
	 * will contain some (but not all) attributes that existing in the first subscribe, and
	 * it will also contain some attributes that were not in the original subscription set.
	 * The effects of this call are different in 1.3 to 1516.
	 * <p/>
	 * In 1.3, a later subscription replaces an earlier one for a specific object class. Thus,
	 * the attributes missing from the second set should be implicitly unsubscribed to. Further,
	 * the new attributes should be subscribed to.
	 * <p/>
	 * In 1516, a later subsription adds to the earlier one for a specific object class. Thus,
	 * all the attributes from the initial subscription should remain subscribed to, while any
	 * new attributes specified in the second subscription should also be added to the set of
	 * subscribed attributes.
	 */
	@Test
	public void testOCSubscribeToPartialPreviouslySubscribedAttributes()
	{
		// set up the handle sets
		int[] initialSet = new int[]{ aaHandle, baHandle };
		int[] secondSet  = new int[]{ aaHandle, abHandle }; 
		
		////////////////////////////////////////
		// do the subscription the first time //
		////////////////////////////////////////
		AttributeHandleSet ahs = newAttributeSet( initialSet );
		try
		{
			defaultFederate.rtiamb.subscribeObjectClassAttributes( bClass, ahs );
		}
		catch( Exception e )
		{
			unexpectedException( "subscribing to object attributes", e );
		}
		
		// validate the subscription
		validateSubscribed( true,
		                    senderFederate,
		                    bHandle,
		                    new int[]{ aaHandle, abHandle, baHandle, bbHandle },
		                    defaultFederate,
		                    aHandle,
		                    initialSet );

		///////////////////////////////
		// do the subscription again //
		///////////////////////////////
		AttributeHandleSet ahs2 = newAttributeSet( secondSet );
		try
		{
			defaultFederate.rtiamb.subscribeObjectClassAttributes( bClass, ahs2 );
		}
		catch( Exception e )
		{
			unexpectedException( "subscribing to object attributes", e );
		}
		
		// validate the subscription
		validateSubscribed( true,
		                    senderFederate,
		                    bHandle,
		                    new int[]{ aaHandle, abHandle, baHandle, bbHandle },
		                    defaultFederate,
		                    aHandle,
		                    secondSet );
	}

	///////////////////////////////////////////////////////
	// TEST: (valid) testOCSubscribeWithEmptyHandleSet() //
	///////////////////////////////////////////////////////
	/**
	 * The result of this action should differ from 1.3 to 1516. In 1.3, this is the same as
	 * an implicit unsubscribe (as later subscriptions overwrite earlier ones). However, in
	 * 1516, this should cause no change, as later subscriptions are only meant to add to
	 * existing ones (and an empty set has nothing to add).
	 */
	@Test
	public void testOCSubscribeWithEmptyHandleSet()
	{
		// set up the handle sets
		int[] initialSet = new int[]{ aaHandle, baHandle };
		int[] emptySet   = new int[]{}; 
		
		////////////////////////////////////////
		// do the subscription the first time //
		////////////////////////////////////////
		AttributeHandleSet ahs = newAttributeSet( initialSet );
		try
		{
			defaultFederate.rtiamb.subscribeObjectClassAttributes( bClass, ahs );
		}
		catch( Exception e )
		{
			unexpectedException( "subscribing to object attributes", e );
		}
		
		// validate the subscription
		validateSubscribed( true,
		                    senderFederate,
		                    bHandle,
		                    new int[]{ aaHandle, abHandle, baHandle, bbHandle },
		                    defaultFederate,
		                    aHandle,
		                    initialSet );

		///////////////////////////////
		// do the subscription again //
		///////////////////////////////
		AttributeHandleSet ahs2 = newAttributeSet( emptySet );
		try
		{
			defaultFederate.rtiamb.subscribeObjectClassAttributes( bClass, ahs2 );
		}
		catch( Exception e )
		{
			unexpectedException( "subscribing to object attributes with empty set", e );
		}
		
		// validate the subscription
		int oHandle = senderFederate.quickRegister( bHandle );
		// seeing as we are no longer subscribed to anything, we should never discover this
		defaultFederate.fedamb.waitForDiscoveryTimeout( oHandle );
	}

	/////////////////////////////////////////////////////
	// TEST: testOCSubscribeWithUndefinedClassHandle() //
	/////////////////////////////////////////////////////
	@Test
	public void testOCSubscribeWithUndefinedClassHandle()
	{
		try
		{
			AttributeHandleSet ahs = newAttributeSet( aaHandle );
			defaultFederate.rtiamb.subscribeObjectClassAttributes( getObjectClassHandle(11111111),
			                                                       ahs );
			expectedException( ObjectClassNotDefined.class );
		}
		catch( ObjectClassNotDefined ocnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectClassNotDefined.class );
		}
	}
	
	/////////////////////////////////////////////////////////
	// TEST: testOCSubscribeWithUndefinedAttributeHandle() //
	/////////////////////////////////////////////////////////
	/**
	 * Attempts to subscribe to an attribute handle that doesn't exist in the FOM
	 */
	@Test
	public void testOCSubscribeWithUndefinedAttributeHandle()
	{
		try
		{
			AttributeHandleSet ahs = newAttributeSet( aaHandle, 11111111 );
			defaultFederate.rtiamb.subscribeObjectClassAttributes( aClass, ahs );
			expectedException( AttributeNotDefined.class );
		}
		catch( AttributeNotDefined and )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeNotDefined.class );
		}
	}
	
	///////////////////////////////////////////////////////
	// TEST: testOCSubscribeWithInvalidAttributeHandle() //
	///////////////////////////////////////////////////////
	/**
	 * Attempts to subscribe to an attribute handle that exists in the FOM, but isn't valid for
	 * the object class specified.
	 */
	@Test
	public void testOCSubscribeWithInvalidAttributeHandle()
	{
		try
		{
			AttributeHandleSet ahs = newAttributeSet( aaHandle, baHandle );
			defaultFederate.rtiamb.subscribeObjectClassAttributes( aClass, ahs );
			expectedException( AttributeNotDefined.class );
		}
		catch( AttributeNotDefined and )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeNotDefined.class );
		}
	}
	
	//////////////////////////////////////////////
	// TEST: testOCSubscribeWithNullHandleSet() //
	//////////////////////////////////////////////
	@Test
	public void testOCSubscribeWithNullHandleSet()
	{
		try
		{
			defaultFederate.rtiamb.subscribeObjectClassAttributes( aClass, null );
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
	
	//////////////////////////////////////////
	// TEST: testOCSubscribeWhenNotJoined() //
	//////////////////////////////////////////
	@Test
	public void testOCSubscribeWhenNotJoined()
	{
		// resign so we can run the test
		defaultFederate.quickResign();
		
		try
		{
			AttributeHandleSet ahs = newAttributeSet( aaHandle );
			defaultFederate.rtiamb.subscribeObjectClassAttributes( aClass, ahs );
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
	// TEST: (valid) testOCSubscribeForRegistrationEffects() //
	///////////////////////////////////////////////////////////
	@Test
	public void testOCSubscribeForRegistrationEffects()
	{
		log( "registration notification not yet supported" );
	}

	////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCSubscribeToParentClassForRegistrationEffects() //
	////////////////////////////////////////////////////////////////////////
	/**
	 * Test that the subscription to a parent class triggers the proper registration callbacks
	 * in federates publishing instances of child classes.
	 */
	@Test
	public void testOCSubscribeToParentClassForRegistrationEffects()
	{
		log( "registration notification not yet supported" );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCSubscribeToPreviouslySubscribedAttributesForRegistrationEffects() //
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that the follow-up subscription has the appropriate affects on the registration
	 * notice for publishing federates.
	 */
	@Test
	public void testOCSubscribeToPreviouslySubscribedAttributesForRegistrationEffects()
	{
		log( "registration notification not yet supported" );
	}

	/////////////////////////////////////////////////////////////
	// TEST: (valid) testOCSubscribeToPartialPreviouslySu..... //
	/////////////////////////////////////////////////////////////
	@Test
	public void testOCSubscribeToPartialPreviouslySubscribedAttributesForRegistrationEffects()
	{
		log( "registration notification not yet supported" );
	}

	/////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCSubscribeWithEmptyHandleSetForRegistrationEffects() //
	/////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that the empty subscription has the appropriate affects on the registration
	 * notice for publishing federates.
	 */
	@Test
	public void testOCSubscribeWithEmptyHandleSetForRegistrationEffects()
	{
		log( "registration notification not yet supported" );
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Passive Subscription Methods //////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void subscribeObjectClassAttributesPassively( ObjectClassHandle theClass,
	//                                                      AttributeHandleSet attributeList )
	//        throws ObjectClassNotDefined,
	//               AttributeNotDefined,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

	////////////
	// NOTICE //
	//////////////////////////////////////////////////////////////
	// One of the main points of these tests is to also ensure  //
	// that the appropriate start/stop registration methods are //
	// not called as a result of the subscription requests      //
	//                                                          //
	// The main subscription-behaviour is tested in other tests //
	//////////////////////////////////////////////////////////////

	////////////////////////////////////////////
	// TEST: (valid) testOCPassiveSubscribe() //
	////////////////////////////////////////////
	@Test
	public void testOCPassiveSubscribe()
	{
		log( "passive subscription not yet supported" );
	}

	//////////////////////////////////////////////////////
	// TEST: (valid) testOCPassiveSubscribeToSubclass() //
	//////////////////////////////////////////////////////
	/**
	 * Passively subscribe to a subclass of that being published by the other federate
	 */
	@Test
	public void testOCPassiveSubscribeToSubclass()
	{
		log( "passive subscription not yet supported" );
	}

	////////////////////////////////////////////////////////////
	// TEST: testOCPassiveSubscribeWithUndefinedClassHandle() //
	////////////////////////////////////////////////////////////
	@Test
	public void testOCPassiveSubscribeWithUndefinedClassHandle()
	{
		log( "passive subscription not yet supported" );
	}

	////////////////////////////////////////////////////////////////
	// TEST: testOCPassiveSubscribeWithUndefinedAttributeHandle() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testOCPassiveSubscribeWithUndefinedAttributeHandle()
	{
		log( "passive subscription not yet supported" );
	}

	//////////////////////////////////////////////////////////////
	// TEST: testOCPassiveSubscribeWithInvalidAttributeHandle() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testOCPassiveSubscribeWithInvalidAttributeHandle()
	{
		log( "passive subscription not yet supported" );
	}

	/////////////////////////////////////////////////////
	// TEST: testOCPassiveSubscribeWithNullHandleSet() //
	/////////////////////////////////////////////////////
	@Test
	public void testOCPassiveSubscribeWithNullHandleSet()
	{
		log( "passive subscription not yet supported" );
	}

	/////////////////////////////////////////////////
	// TEST: testOCPassiveSubscribeWhenNotJoined() //
	/////////////////////////////////////////////////
	@Test
	public void testOCPassiveSubscribeWhenNotJoined()
	{
		log( "passive subscription not yet supported" );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Unsubscription Methods /////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void unsubscribeObjectClass( ObjectClassHandle theClass )
	//        throws ObjectClassNotDefined,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

	///////////////////////////////////////
	// TEST: (valid) testOCUnsubscribe() //
	///////////////////////////////////////
	@Test
	public void testOCUnsubscribe()
	{
		// quickly subscribe in the default federate
		defaultFederate.quickSubscribe( aHandle, aaHandle );
		validateSubscribed( true,
		                    senderFederate,
		                    aHandle,
		                    new int[]{aaHandle},
		                    defaultFederate,
		                    aHandle,
		                    new int[]{aaHandle} );
		
		// unsubscribe
		try
		{
			defaultFederate.rtiamb.unsubscribeObjectClass( aClass );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing object class", e );
		}
		
		// validate that we no longer subscribe to the class
		int oHandle = senderFederate.quickRegister( aHandle );
		defaultFederate.fedamb.waitForDiscoveryTimeout( oHandle );
	}

	///////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCUnsubscribeChildClassDoesntAffectSuperClassSubscription() //
	///////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testOCUnsubscribeChildClassDoesntAffectSuperClassSubscription()
	{
		// quickly subscribe in the default federate to BOTH classes
		defaultFederate.quickSubscribe( aHandle, aaHandle );
		validateSubscribed( true,
		                    senderFederate,
		                    aHandle,
		                    new int[]{aaHandle},
		                    defaultFederate,
		                    aHandle,
		                    new int[]{aaHandle} );

		defaultFederate.quickSubscribe( bHandle, aaHandle, baHandle );
		validateSubscribed( true,
		                    senderFederate,
		                    bHandle,
		                    new int[]{aaHandle,baHandle},
		                    defaultFederate,
		                    bHandle,
		                    new int[]{aaHandle,baHandle} );

		// unsubscribe from the child class
		try
		{
			defaultFederate.rtiamb.unsubscribeObjectClass( bClass );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing object class", e );
		}
		
		// validate that we no longer subscribe to the child class, but that we
		// subscribe only to the parent class. if we register a child class, we
		// should discover it as the parent type
		int oHandle = senderFederate.quickRegister( bHandle );
		defaultFederate.fedamb.waitForDiscovery( oHandle );
		// assert that we have discovered it as the right type
		TestObject instance = defaultFederate.fedamb.getInstances().get( oHandle );
		Assert.assertEquals( instance.getClassHandle(), aHandle,
		                     "Discovered class as wrong type following child class unsubscribe" );
	}

	///////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCUnsubscribeSuperClassDoesntAffectChildClassSubscription() //
	///////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testOCUnsubscribeSuperClassDoesntAffectChildClassSubscription()
	{
		// quickly subscribe in the default federate to BOTH classes
		defaultFederate.quickSubscribe( aHandle, aaHandle );
		validateSubscribed( true,
		                    senderFederate,
		                    aHandle,
		                    new int[]{aaHandle},
		                    defaultFederate,
		                    aHandle,
		                    new int[]{aaHandle} );

		defaultFederate.quickSubscribe( bHandle, aaHandle, baHandle );
		validateSubscribed( true,
		                    senderFederate,
		                    bHandle,
		                    new int[]{aaHandle,baHandle},
		                    defaultFederate,
		                    bHandle,
		                    new int[]{aaHandle,baHandle} );

		// unsubscribe from the parent class
		try
		{
			defaultFederate.rtiamb.unsubscribeObjectClass( aClass );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing object class", e );
		}

		// validate that we no longer subscribe to the class
		int oHandle = senderFederate.quickRegister( aHandle );
		defaultFederate.fedamb.waitForDiscoveryTimeout( oHandle );
		
		// validate that we still subscribe to the child class
		validateSubscribed( true,
		                    senderFederate,
		                    bHandle,
		                    new int[]{aaHandle,baHandle},
		                    defaultFederate,
		                    bHandle,
		                    new int[]{aaHandle,baHandle} );
	}

	/////////////////////////////////////////////////////////
	// TEST: (valid) testOCUnsubscribeNonSubscribedClass() //
	/////////////////////////////////////////////////////////
	@Test
	public void testOCUnsubscribeNonSubscribedClass()
	{
		try
		{
			defaultFederate.rtiamb.unsubscribeObjectClass( aClass );
		}
		catch( Exception e )
		{
			unexpectedException( "Unsubscribing non-subscribed object class (valid in 1516)", e );
		}
	}

	//////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCUnsubscribeSuperClassOfSubscribedClass() //
	//////////////////////////////////////////////////////////////////
	@Test
	public void testOCUnsubscribeSuperClassOfSubscribedClass()
	{
		// subscribe to ObjectRoot.A.B
		defaultFederate.quickSubscribe( bHandle, aaHandle, baHandle );
		validateSubscribed( true,
		                    senderFederate,
		                    bHandle,
		                    new int[]{aaHandle,baHandle},
		                    defaultFederate,
		                    bHandle,
		                    new int[]{aaHandle,baHandle} );
		
		// try and unsubscribe ObjectRoot.A
		try
		{
			defaultFederate.rtiamb.unsubscribeObjectClass( aClass );
		}
		catch( Exception e )
		{
			unexpectedException( "Unsubscribing super-class of non-subscribed object class" +
			                     "(valid in 1516, should be ignored)", e );
		}
	}

	///////////////////////////////////////////////////////
	// TEST: testOCUnsubscribeWithUndefinedClassHandle() //
	///////////////////////////////////////////////////////
	@Test
	public void testOCUnsubscribeWithUndefinedClassHandle()
	{
		try
		{
			defaultFederate.rtiamb.unsubscribeObjectClass( getObjectClassHandle(11111111) );
			expectedException( ObjectClassNotDefined.class );
		}
		catch( ObjectClassNotDefined ocnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectClassNotDefined.class );
		}
	}

	////////////////////////////////////////////
	// TEST: testOCUnsubscribeWhenNotJoined() //
	////////////////////////////////////////////
	@Test
	public void testOCUnsubscribeWhenNotJoined()
	{
		// resign so we can run the test
		defaultFederate.quickResign();
		
		try
		{
			defaultFederate.rtiamb.unsubscribeObjectClass( aClass );
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
