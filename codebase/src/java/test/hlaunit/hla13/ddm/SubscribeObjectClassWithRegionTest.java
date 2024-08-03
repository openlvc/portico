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
package hlaunit.hla13.ddm;

import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotDefined;
import hla.rti.FederateNotExecutionMember;
import hla.rti.FederateNotSubscribed;
import hla.rti.InvalidRegionContext;
import hla.rti.ObjectClassNotDefined;
import hla.rti.RTIinternalError;
import hla.rti.Region;
import hla.rti.RegionNotKnown;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;
import hlaunit.hla13.common.Test13Instance;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"SubscribeObjectClassWithRegionTest",
                                   "subscribeObjectWithRegion",
                                   "subscribe",
                                   "pubsub",
                                   "ddm"})
public class SubscribeObjectClassWithRegionTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate sender;
	private Test13Federate listener;

	private int aHandle, aaHandle, abHandle, acHandle;
	private int bHandle, baHandle, bbHandle, bcHandle;
	//private int spaceHandle;
	//private int dimensionHandle;

	private Region senderRegion;
	private Region senderRegionOOB;
	private Region listenerRegion;
	private Region listenerRegionOOB;
	
	private AttributeHandleSet fourAtts;
	private AttributeHandleSet aAttributes;
	private int[] allAttributes;

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
		
		this.sender = new Test13Federate( "sender", this );
		this.listener = new Test13Federate( "listener", this );
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception
	{
		sender.quickCreate();
		sender.quickJoin();
		listener.quickJoin();

		// cache the handle information //
		aHandle  = sender.quickOCHandle( "ObjectRoot.A" );
		aaHandle = sender.quickACHandle( "ObjectRoot.A", "aa" );
		abHandle = sender.quickACHandle( "ObjectRoot.A", "ab" );
		acHandle = sender.quickACHandle( "ObjectRoot.A", "ac" );
		bHandle  = sender.quickOCHandle( "ObjectRoot.A.B" );
		baHandle = sender.quickACHandle( "ObjectRoot.A.B", "ba" );
		bbHandle = sender.quickACHandle( "ObjectRoot.A.B", "bb" );
		bcHandle = sender.quickACHandle( "ObjectRoot.A.B", "bc" );
		//spaceHandle = sender.quickSpaceHandle( "TestSpace" );
		//dimensionHandle = sender.quickDimensionHandle( "TestSpace", "TestDimension" );
		
		// create that ahs
		fourAtts = sender.createAHS( aaHandle, abHandle, baHandle, bbHandle );
		aAttributes = sender.createAHS( aaHandle, abHandle, acHandle );
		allAttributes = new int[]{ aaHandle, abHandle, acHandle, baHandle, bbHandle, bcHandle };

		// do the publication and subscription and setup a basic instance //
		// sender federate
		sender.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		sender.quickPublish( "ObjectRoot.A.B", "aa", "ab", "ac", "ba", "bb", "bc" );
		senderRegion = sender.quickCreateTestRegion( 100, 200 );
		senderRegionOOB = sender.quickCreateTestRegion( 1050, 1150 );

		// alistener federate
		listenerRegion = listener.quickCreateTestRegion( 50, 150 );
		listenerRegionOOB = listener.quickCreateTestRegion( 1000, 1100 );
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		defaultFederate.quickResign(); // rarely joined
		listener.quickResign();
		sender.quickResign();
		sender.quickDestroy();
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
	 * Method will register an object of ObjectRoot.A.B with the sender, wait for it to be
	 * discovered by the <code>receiver</code> and validates that it is discovered as an instance
	 * of the <code>expectedClass</code>. Following this, the sender will update each of the
	 * attributes that it publishes and the method will wait to get an RO reflection for the 
	 * instance in the <code>receiver</code>. When this has completed, the method will validate
	 * that each of the <code>expectedAttributes</code> has a value.
	 * <p/>
	 * Note: the attributes will be associated with the <code>senderRegion</code> instance variable
	 * 
	 * @return The handle of the object that was registered during this process
	 */
	private int validateSubscribed( Test13Federate receiver,
	                                Region senderRegion,
	                                int expectedClass,
	                                int... expectedAttributes )
	{
		return validateSubscribedStrict( receiver,
		                                 senderRegion,
		                                 expectedClass,
		                                 expectedAttributes,
		                                 null );
	}

	/**
	 * The same as {@link #validateSubscribed(Test13Federate, Region, int, int...)} except you can
	 * also specify the attributes that you expect will NOT be provided as part of the update.
	 * This method will then ensure that they are not part of the reflect to the receiver (and
	 * will fail the test if they are). If the <code>missingAttributes</code> argument is null
	 * or empty, no missing attribute checks will be made and this call will be the same as a
	 * call to the other validate method.
	 * 
	 * @param receiver The federate that will be the recevier for this test
	 * @param expectedClass The object class that the federate is expected to be discovered as
	 * @param expectedAttributes The attributes that should be included in any reflection
	 * @param missingAttributes The attributes that should not be included in any reflection.
	 * 
	 * @return The handle of the object that was registered during this process
	 */
	private int validateSubscribedStrict( Test13Federate receiver,
	                                      Region regionForSender,
	                                      int expectedClass,
	                                      int[] expectedAttributes,
	                                      int[] missingAttributes )
	{
		// register an object instance for testing
		int object = sender.quickRegisterWithRegion( bHandle, regionForSender, allAttributes );
		
		// make sure it is discovered in the receiver as the right type
		receiver.fedamb.waitForDiscoveryAs( object, expectedClass );
		
		// update all the attribute values
		sender.quickReflect( object, allAttributes );
		
		// wait for the reflection in the receiver
		Test13Instance receivedInstance = receiver.fedamb.getInstances().get( object );
		receiver.fedamb.waitForROUpdate( object );

		// validate that we have values for each of the expected attributes
		for( int attribute : expectedAttributes )
		{
			Assert.assertNotNull( receivedInstance.getAttributeValue(attribute),
			                      "expected a reflection for attribute: " + attribute );
		}
		
		// validate that we don't have values for the expected missing attributes
		if( missingAttributes != null )
		{
			for( int attribute : missingAttributes )
			{
				Assert.assertNull( receivedInstance.getAttributeValue(attribute),
			                   "expected NO reflection for attribute (but got one): " + attribute );
			}
		}
		
		return object;
	}

	/**
	 * This method will validate that the receiver isn't subscribed to the expectedClass AT ALL.
	 * The sender will register an object of type ObjectRoot.A.B and the method will then make
	 * sure that this object isn't discovered by the receiver.
	 */
	private void validateNotSubscribed( Test13Federate receiver )
	{
		// register the instance and don't discover it in the receiver
		int instance = sender.quickRegister( bHandle );
		receiver.fedamb.waitForDiscoveryTimeout( instance );
	}

	/**
	 * Validate that the receiver isn't subscribed to any attributes of the identified object
	 * class. The sender will register a new instance of the given class. The listener should
	 * not discover an instance of this class because it isn't subscribed to any attributes.
	 */
	private void validateNotSubscribed( Test13Federate receiver, int classNotSubscribedTo )
	{
		// register the instance and don't discover it in the receiver
		int instance = sender.quickRegister( classNotSubscribedTo );
		receiver.fedamb.waitForDiscoveryAsTimeout( instance, classNotSubscribedTo );
	}

	/**
	 * This method assumes that the reveiver has previously been subscribed to the given attributes
	 * and that it has discovered the object with the given handle (that was registered by the
	 * sender federate). However, following an unsubscribe, the federate is no longer subscribed to
	 * those attributes attributes and thus should not receive a reflection of the attributes.
	 * This method will have the sender send a reflection with all of the provided attributes and
	 * will assert that the receiver does NOT receive it.
	 */
	private void validateNoLongerSubscribed( Test13Federate receiver,
	                                         int objectHandle,
	                                         int... attributes )
	{
		// update the values of the provided object
		sender.quickReflect( objectHandle, attributes );
		
		// wait for the update that should never arrive at the receiver
		receiver.fedamb.waitForROUpdateTimeout( objectHandle );
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Active Subscription Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void subscribeObjectClassAttributesWithRegion( int theClass,
	//                                                       Region theRegion,
	//                                                       AttributeHandleSet attributeList )
	//        throws ObjectClassNotDefined,
	//               AttributeNotDefined,
	//               RegionNotKnown,
	//               InvalidRegionContext,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	//
	// Unimplemented test ideas:
	//  -> subscribe with region covering full space (equiv. to subscribe without region)
	//  -> register object with region of A.B (using only B attributes) does A subscription get it?
	//  -> subscribe with empty attribute set is implicit unsubscribe with region for instance
	//

	///////////////////////////////////////////////
	// TEST: (valid) testOCSubscribeWithRegion() //
	///////////////////////////////////////////////
	@Test
	public void testOCSubscribeWithRegion()
	{
		// use defaultFederate as it isn't subscribed yet
		defaultFederate.quickJoin();
		Region theRegion = defaultFederate.quickCreateTestRegion( 100, 200 );

		// try the subscription
		try
		{
			defaultFederate.rtiamb.subscribeObjectClassAttributesWithRegion( bHandle,
			                                                                 theRegion, 
			                                                                 fourAtts );
		}
		catch( Exception e )
		{
			unexpectedException( "subscribing to object class with region", e );
		}
		
		// validate that the defaultFederate is now subscribed
		validateSubscribedStrict( defaultFederate,
		                          senderRegion,
		                          bHandle,
		                          new int[]{aaHandle, abHandle, baHandle, bbHandle},
		                          new int[]{acHandle, bcHandle} );
	}

	////////////////////////////////////////////////////////////
	// TEST: (valid) testOCSubscribeWithRegionToParentClass() //
	////////////////////////////////////////////////////////////
	/**
	 * Subscribe to ObjectRoot.A and make sure publications of ObjectRoot.A.B come through happily
	 */
	@Test
	public void testOCSubscribeWithRegionToParentClass()
	{
		try
		{
			listener.rtiamb.subscribeObjectClassAttributesWithRegion( aHandle,
			                                                          listenerRegion,
			                                                          aAttributes );
		}
		catch( Exception e )
		{
			unexpectedException( "subscribing to parent object class with region", e );
		}
		
		// validate that the federate is now subscribed
		validateSubscribedStrict( listener,
		                          senderRegion,
		                          aHandle,
		                          new int[]{aaHandle, abHandle, acHandle},
		                          new int[]{baHandle, bbHandle, bcHandle} );
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCSubscribeWithRegionToPreviouslySubscribedAttributesUsingDifferentRegion() //
	///////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Subscribe twice to the same attributes using two different regions. The first should be
	 * beyond the range of the sender federate so that updates are not received. The second
	 * should be within range of the sender so that reflections are received.
	 */
	@Test
	public void testOCSubscribeWithRegionToPreviouslySubscribedAttributesUsingDifferentRegion()
	{
		// subscribe with a region beyond the senders range
		listener.quickSubscribeWithRegion( aHandle, listenerRegionOOB, aaHandle );
		
		// register an instance we can test with
		int instance = sender.quickRegisterWithRegion( bHandle, senderRegion, aaHandle );
		
		// validate that we are not subscribed by ensuring we do not get updates for the aa
		validateNoLongerSubscribed( listener, instance, aaHandle );
		
		// subscribe to the same attributes with a region that overlaps with the sender
		listener.quickSubscribeWithRegion( aHandle, listenerRegion, aaHandle );

		// validate that we are subscribed by ensuring we do not get updates for the aa
		validateSubscribedStrict( listener, senderRegion, aHandle, new int[]{aaHandle}, new int[]{} );
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCSubscribeWithRegionAlreadySubscribedUsingSameRegionButFewerAttributes() //
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * According to the 1.3 spec, subscriptions are unique for a class/region pair, and that
	 * subsequent subscriptions for the same class/region pair will result in an implicit
	 * unsubscribe of the original and a subscribe of the new attributes. For example, consider
	 * the following scenario. A federate subscribes to ObjectRoot.A using Region1 for attributes
	 * aa, ab and ac. It then subscribes to ObjectRoot.A using Region1 (again), but this time only
	 * provides aa and ab. The result of this is that the class will only be subscribed to aa and
	 * ab for Region1.
	 * <p/>
	 * HOWEVER, remember that region subscriptions are cumulative, so if a different region was
	 * used in a separate subscribe for all the attributes, those attributes are still subscribed
	 * to, but aa and ab are subscribed for both regions while ac is subscribe only for the other
	 * region.
	 * <p/>
	 * This method will test that all the above guff is true.
	 */
	@Test
	public void testOCSubscribeWithRegionAlreadySubscribedUsingSameRegionButFewerAttributes()
	{
		// subscribe to ObjectRoot.A.B with all attributes and validate that subscription
		listener.quickSubscribeWithRegion( aHandle, listenerRegion, aaHandle, abHandle, acHandle );
		validateSubscribed( listener, senderRegion, aHandle, aaHandle, abHandle, acHandle );
		
		// reissue the subscription with the region, but only for aa and ab
		listener.quickSubscribeWithRegion( aHandle, listenerRegion, aaHandle, abHandle );
		validateSubscribedStrict( listener,
		                          senderRegion,
		                          aHandle,
		                          new int[]{aaHandle, abHandle},
		                          new int[]{acHandle} );
		
		// issue another subscription in a different region for ac
		listener.quickSubscribeWithRegion( aHandle, listenerRegionOOB, acHandle );
		
		// validate that we are still subscribe to ObjectRoot.A.B in the overlapping region
		// with aa and ab but NOT ac
		validateSubscribedStrict( listener,
		                          senderRegion,
		                          aHandle,
		                          new int[]{aaHandle, abHandle},
		                          new int[]{acHandle} );
	}

	////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCSubscribeWithRegionWithSomeAttributesInRangeAndOthersNot() //
	////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This test will do two subscriptions: some where the attributes are associated with a region
	 * that overlaps with that which the sender uses, and others which don't. It will assert that
	 * only those that overlap are reflected to the federate.
	 */
	@Test
	public void testOCSubscribeWithRegionWithSomeAttributesInRangeAndOthersNot()
	{
		// do the subscriptions
		listener.quickSubscribeWithRegion( bHandle, listenerRegion, aaHandle, baHandle );
		listener.quickSubscribeWithRegion( bHandle, listenerRegionOOB, acHandle, bcHandle );
		
		// validate the subscription
		validateSubscribedStrict( listener,
		                          senderRegion,
		                          bHandle,
		                          new int[]{aaHandle, baHandle},
		                          new int[]{acHandle, bcHandle} );
		
	}

	//////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCSubscribeWithRegionUsingEmptyHandleSet() //
	//////////////////////////////////////////////////////////////////
	/**
	 * Subscribing with an empty attribute set is equivalent to unsubscrbing with the region.
	 */
	@Test
	public void testOCSubscribeWithRegionUsingEmptyHandleSet()
	{
		// subscribe with an empty handle set, creating an implicit unsubscriptions, but
		// we are not yet subscribed. this should not throw an exception because DMSO doesn't
		listener.quickSubscribeWithRegion( bHandle, listenerRegion, new int[]{} );
		
		// subscribe to both ObjectRoot.A and ObjectRoot.A.B
		listener.quickSubscribeWithRegion( aHandle, listenerRegion, aaHandle );
		validateSubscribed( listener, senderRegion, aHandle, aaHandle );
		listener.quickSubscribeWithRegion( bHandle, listenerRegion, baHandle );
		validateSubscribed( listener, senderRegion, bHandle, baHandle );
		
		// now subscribe with an empty set, this should proceed, but should result in
		// the federate being unsubscribed for the identified region
		listener.quickSubscribeWithRegion( bHandle, listenerRegion, new int[]{} );
		validateSubscribed( listener, senderRegion, aHandle, aaHandle );
		validateNotSubscribed( listener, bHandle );
		listener.quickSubscribeWithRegion( aHandle, listenerRegion, new int[]{} );
		validateNotSubscribed( listener, aHandle );
		validateNotSubscribed( listener, bHandle );
	}
	
	////////////////////////////////////////////////////////////////
	// TEST: testOCSubscribeWithRegionUsingUndefinedObjectClass() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testOCSubscribeWithRegionUsingUndefinedObjectClass()
	{
		try
		{
			listener.rtiamb.subscribeObjectClassAttributesWithRegion( 11111111,
			                                                          listenerRegion,
			                                                          fourAtts );
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

	///////////////////////////////////////////////////////////////
	// TEST: testOCSubscribeWithRegionUsingNegativeObjectClass() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testOCSubscribeWithRegionUsingNegativeObjectClass()
	{
		try
		{
			listener.rtiamb.subscribeObjectClassAttributesWithRegion( -1,listenerRegion,fourAtts );
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

	//////////////////////////////////////////////////////////////
	// TEST: testOCSubscribeWithRegionUsingUndeinfedAttribute() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testOCSubscribeWithRegionUsingUndeinfedAttribute()
	{
		try
		{
			AttributeHandleSet localSet = listener.createAHS( aaHandle, 1111111 );
			listener.rtiamb.subscribeObjectClassAttributesWithRegion( bHandle,
			                                                          listenerRegion,
			                                                          localSet );
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

	/////////////////////////////////////////////////////////////
	// TEST: testOCSubscribeWithRegionUsingNegativeAttribute() //
	/////////////////////////////////////////////////////////////
	@Test
	public void testOCSubscribeWithRegionUsingNegativeAttribute()
	{
		try
		{
			AttributeHandleSet localSet = listener.createAHS( aaHandle, -1 );
			listener.rtiamb.subscribeObjectClassAttributesWithRegion( bHandle,
			                                                          listenerRegion,
			                                                          localSet );
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

	////////////////////////////////////////////////////////////
	// TEST: testOCSubscribeWithRegionUsingNullAttributeSet() //
	////////////////////////////////////////////////////////////
	@Test
	public void testOCSubscribeWithRegionUsingNullAttributeSet()
	{
		try
		{
			listener.rtiamb.subscribeObjectClassAttributesWithRegion( bHandle,
			                                                          listenerRegion,
			                                                          null );
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

	/////////////////////////////////////////////////////////
	// TEST: testOCSubscribeWithRegionUsingUnknownRegion() //
	/////////////////////////////////////////////////////////
	@Test
	public void testOCSubscribeWithRegionUsingUnknownRegion()
	{
		try
		{
			listener.rtiamb.subscribeObjectClassAttributesWithRegion( bHandle,
			                                                          senderRegion,
			                                                          fourAtts );
			expectedException( RegionNotKnown.class );
		}
		catch( RegionNotKnown rnk )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RegionNotKnown.class );
		}
	}

	/////////////////////////////////////////////////////////
	// TEST: testOCSubscribeWithRegionUsingInvalidRegion() //
	/////////////////////////////////////////////////////////
	@Test
	public void testOCSubscribeWithRegionUsingInvalidRegion()
	{
		try
		{
			// the ObjectRoot.BestEffortTest.blah attribute has no region data assoicated to
			// it in the FOM, so the following should be invalid
			int classHandle = listener.quickOCHandle( "ObjectRoot.BestEffortTest" );
			int attributeHandle = listener.quickACHandle( "ObjectRoot.BestEffortTest", "blah" );
			AttributeHandleSet ahs = listener.createAHS( attributeHandle );
			listener.rtiamb.subscribeObjectClassAttributesWithRegion( classHandle,
			                                                          listenerRegion,
			                                                          ahs );
			expectedException( InvalidRegionContext.class );
		}
		catch( InvalidRegionContext irc )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidRegionContext.class );
		}
	}

	//////////////////////////////////////////////////////
	// TEST: testOCSubscribeWithRegionUsingNullRegion() //
	//////////////////////////////////////////////////////
	@Test
	public void testOCSubscribeWithRegionUsingNullRegion()
	{
		try
		{
			listener.rtiamb.subscribeObjectClassAttributesWithRegion( bHandle, null, fourAtts );
			expectedException( RegionNotKnown.class );
		}
		catch( RegionNotKnown rnk )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RegionNotKnown.class );
		}
	}

	////////////////////////////////////////////////////////////
	// TEST: testOCSubscribeWithRegionWhenFederateNotJoined() //
	////////////////////////////////////////////////////////////
	@Test
	public void testOCSubscribeWithRegionWhenFederateNotJoined()
	{
		// resign for this test
		sender.quickResign();
		
		try
		{
			sender.rtiamb.subscribeObjectClassAttributesWithRegion( bHandle,
			                                                        senderRegion,
			                                                        fourAtts );
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

	/////////////////////////////////////////////////////////
	// TEST: testOCSubscribeWithRegionWhenSaveInProgress() //
	/////////////////////////////////////////////////////////
	@Test
	public void testOCSubscribeWithRegionWhenSaveInProgress()
	{
		// resign for this test
		sender.quickSaveInProgress( "save" );
		
		try
		{
			sender.rtiamb.subscribeObjectClassAttributesWithRegion( bHandle,
			                                                        senderRegion,
			                                                        fourAtts );
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

	////////////////////////////////////////////////////////////
	// TEST: testOCSubscribeWithRegionWhenRestoreInProgress() //
	////////////////////////////////////////////////////////////
	@Test
	public void testOCSubscribeWithRegionWhenRestoreInProgress()
	{
		// resign for this test
		sender.quickRestoreInProgress( "save" );
		
		try
		{
			sender.rtiamb.subscribeObjectClassAttributesWithRegion( bHandle,
			                                                        senderRegion,
			                                                        fourAtts );
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
	////////////////////////////// Passive Subscription Methods //////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void subscribeObjectClassAttributesPassivelyWithRegion( int theClass,
	//                                                                Region theRegion,
	//                                                                AttributeHandleSet atts )
	//        throws ObjectClassNotDefined,
	//               AttributeNotDefined,
	//               RegionNotKnown,
	//               InvalidRegionContext,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	////////////
	// NOTICE //
	//////////////////////////////////////////////////////////
	// Passive Subscription support has not yet been added  //
	//////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Unsubscribe Methods ///////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void unsubscribeObjectClassWithRegion( int theClass, Region theRegion )
	//        throws ObjectClassNotDefined,
	//               RegionNotKnown,
	//               FederateNotSubscribed,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	/////////////////////////////////////////////////
	// TEST: (valid) testOCUnsubscribeWithRegion() //
	/////////////////////////////////////////////////
	@Test
	public void testOCUnsubscribeWithRegion()
	{
		// set up the subscription
		listener.quickSubscribeWithRegion( aHandle, listenerRegion, aaHandle, abHandle, acHandle );
		validateSubscribed( listener, senderRegion, aHandle, aaHandle, abHandle, acHandle );
		
		// attempt the unsubscribe
		try
		{
			listener.rtiamb.unsubscribeObjectClassWithRegion( aHandle, listenerRegion );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing from object class with region", e );
		}
		
		// make sure we are no longer subscribed
		validateNotSubscribed( listener );
	}

	///////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCUnsubscribeWithRegionDoesntAffectSuperClassSubscription() //
	///////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testOCUnsubscribeWithRegionDoesntAffectSuperClassSubscription()
	{
		// subscribe to ObjectRoot.A and ObjectRoot.A.B
		listener.quickSubscribeWithRegion( aHandle, listenerRegion, aaHandle, abHandle, acHandle );
		validateSubscribed( listener, senderRegion, aHandle, aaHandle, abHandle, acHandle );
		listener.quickSubscribeWithRegion( bHandle, listenerRegion, allAttributes );
		validateSubscribed( listener, senderRegion, bHandle, allAttributes );
		
		// end subscription to the child class and make sure we are still
		// happily subscribed to the parent
		try
		{
			listener.rtiamb.unsubscribeObjectClassWithRegion( bHandle, listenerRegion );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing from object class with region", e );
		}
		
		// make sure we still discover instances registered of ObjectRoot.A.B as ObjectRoot.A
		int objectHandle = sender.quickRegisterWithRegion( bHandle, senderRegion, aaHandle );
		listener.fedamb.waitForDiscoveryAs( objectHandle, aHandle );
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCUnsubscribeWithRegionFromSuperclassDoesntAffectChildClassSubscription() //
	/////////////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testOCUnsubscribeWithRegionFromSuperclassDoesntAffectChildClassSubscription()
	{
		// subscribe to ObjectRoot.A and ObjectRoot.A.B
		listener.quickSubscribeWithRegion( aHandle, listenerRegion, aaHandle, abHandle, acHandle );
		validateSubscribed( listener, senderRegion, aHandle, aaHandle, abHandle, acHandle );
		listener.quickSubscribeWithRegion( bHandle, listenerRegion, allAttributes );
		validateSubscribed( listener, senderRegion, bHandle, allAttributes );
		
		// end subscription to the parent class and make sure we are still
		// happily subscribed to the child
		try
		{
			listener.rtiamb.unsubscribeObjectClassWithRegion( aHandle, listenerRegion );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing from object class with region", e );
		}
		
		// make sure we don't discover instances of ObjectRoot.A
		sender.quickPublish( aHandle, aaHandle, abHandle, acHandle );
		int aObject = sender.quickRegisterWithRegion( aHandle, senderRegion, aaHandle );
		listener.fedamb.waitForDiscoveryTimeout( aObject );
		
		// make sure we still discover instances registered of ObjectRoot.A.B
		int abObject = sender.quickRegisterWithRegion( bHandle, senderRegion, aaHandle, baHandle );
		listener.fedamb.waitForDiscoveryAs( abObject, bHandle );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCUnsubscribeWithRegionWhenMultipleDifferentRegionsAreSubscribed() //
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * As subscriptions with region data are meant to be cumulative, this test will validate that
	 * reversing a subscription for a given region doesn't affect the subscription with regard to
	 * the other region. It will subscribe to ObjectRoot.A with both <code>listenerRegion</code>
	 * and <code>listenerRegionOOB</code> and validate that registrations/updates in both regions
	 * result in notification. It will then unsubscribe from <code>listenerRegion</code> and make
	 * sure that updates sent to that region no longer make it through.
	 */
	@Test
	public void testOCUnsubscribeWithRegionWhenMultipleDifferentRegionsAreSubscribed()
	{
		// subscribe to the class twice, with different regions
		listener.quickSubscribeWithRegion( aHandle, listenerRegion, aaHandle, abHandle, acHandle );
		int firstObject = validateSubscribed( listener, senderRegion, aHandle,
		                                      aaHandle, abHandle, acHandle );
		
		// now with listenerRegionOOB
		listener.quickSubscribeWithRegion(aHandle, listenerRegionOOB, aaHandle, abHandle, acHandle);
		int secondObject = validateSubscribed( listener, senderRegionOOB, aHandle,
		                                       aaHandle, abHandle, acHandle );
		
		//////////////////////////////////////////
		// unsubscribe using the listenerRegion //
		//////////////////////////////////////////
		try
		{
			listener.rtiamb.unsubscribeObjectClassWithRegion( aHandle, listenerRegion );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing from object class using region", e );
		}
		
		// validate that we are no longer subscribed to with the listenerRegion overlap
		validateNoLongerSubscribed( listener, firstObject, aaHandle, abHandle, acHandle );
		
		// validate still subscribed for listenerRegionOOB object
		sender.quickReflect( secondObject, aaHandle, abHandle, acHandle );
		listener.fedamb.waitForROUpdate( secondObject );
		
		///////////////////////////////////////
		// unsubscribe from the other region //
		///////////////////////////////////////
		try
		{
			listener.rtiamb.unsubscribeObjectClassWithRegion( aHandle, listenerRegionOOB );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing from object class using region", e );
		}
		
		// validate that we are no longer subscribed to with the listenerRegion overlap
		validateNoLongerSubscribed( listener, secondObject, aaHandle, abHandle, acHandle );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCUnsubscribeWithRegionWhenSameRegionIsSubscribedTwiceWithSameAtts() //
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This test will subscribe to the same object class with the same region twice, using the
	 * same set of attributes. Once the subscription has been validated not to have changed, it
	 * will then unsubscribe using the class and that region. This should end all subscription
	 * interest in the class (even though it was subscribed to twice).
	 */
	@Test
	public void testOCUnsubscribeWithRegionWhenSameRegionIsSubscribedTwiceWithSameAtts()
	{
		// subscribe to ObjectRoot.A with listener region and validate it
		listener.quickSubscribeWithRegion( aHandle, listenerRegion, aaHandle, abHandle, acHandle );
		int objectHandle = validateSubscribed( listener, senderRegion, aHandle,
		                                       aaHandle, abHandle, acHandle );
		
		// issue the same subscription again and validate it
		listener.quickSubscribeWithRegion( aHandle, listenerRegion, aaHandle, abHandle, acHandle );
		sender.quickReflect( objectHandle, aaHandle, abHandle, acHandle );
		listener.fedamb.waitForROUpdate( objectHandle );

		// do the unsubscribe
		try
		{
			listener.rtiamb.unsubscribeObjectClassWithRegion( aHandle, listenerRegion );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing from object class using region", e );
		}
		
		// validate no longer subscribed
		sender.quickReflect( objectHandle, aaHandle, abHandle, acHandle );
		listener.fedamb.waitForROUpdateTimeout( objectHandle );
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCUnsubscribeWithRegionWhenSameRegionIsSubscribedTwiceWithDifferentAtts() //
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Although subscriptions with regions are cumulative, if you subscribe to a class with a region
	 * that you had already subscribe to the class with, the second subscription will have the
	 * effect of REPLACING the previous subscription (rather than augmenting it). This test will
	 * subscribe to ObjectRoot.A with aa, ab and ac, validate it, then issue the subscription but
	 * only include aa in the attributes. It will then make sure that aa is still subscribed to,
	 * but that ab and ac are not.
	 */
	@Test
	public void testOCUnsubscribeWithRegionWhenSameRegionIsSubscribedTwiceWithDifferentAtts()
	{
		// subscribe to ObjectRoot.A with listener region and validate it
		listener.quickSubscribeWithRegion( aHandle, listenerRegion, aaHandle, abHandle, acHandle );
		validateSubscribed( listener, senderRegion, aHandle, aaHandle, abHandle, acHandle );
		// issue the same subscription again and validate it
		listener.quickSubscribeWithRegion( aHandle, listenerRegion, aaHandle );
		validateSubscribedStrict( listener,
		                          senderRegion,
		                          aHandle,
		                          new int[]{aaHandle},
		                          new int[]{abHandle, acHandle} );

		try
		{
			listener.rtiamb.unsubscribeObjectClassWithRegion( aHandle, listenerRegion );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing from object class using region", e );
		}
		
		// validate no longer subscribed
		int instance = sender.quickRegisterWithRegion( bHandle, senderRegion, aaHandle );
		listener.fedamb.waitForDiscoveryTimeout( instance );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCUnsubscribeWithoutRegionWhenPreviouslySubscribedWithSingleRegion() //
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This test will issue an unsubscription request for an object class WITHOUT region data when
	 * the class has previously been subscribed to WITH region data. This should have the effect
	 * of removing the subscription interest totally.
	 */
	@Test
	public void testOCUnsubscribeWithoutRegionWhenPreviouslySubscribedWithSingleRegion()
	{
		// subscribe to the class twice, with different regions
		listener.quickSubscribeWithRegion( aHandle, listenerRegion, aaHandle, abHandle, acHandle );
		int object = validateSubscribed( listener, senderRegion, aHandle,
		                                 aaHandle, abHandle, acHandle );
		
		// unsubscribe using the listenerRegion
		try
		{
			listener.rtiamb.unsubscribeObjectClass( aHandle );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing from object class with region data", e );
		}
		
		// validate that we are no longer subscribed to with the listenerRegion overlap
		validateNoLongerSubscribed( listener, object, aaHandle, abHandle, acHandle );
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOCUnsubscribeWithoutRegionWhenPreviouslySubscribedWithMultipleRegions() //
	///////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This test will issue an unsubscription request for an object class WITHOUT region data when
	 * the class had previously been subscribed to WITH region data MULTIPLE times (different
	 * regions). Despite the multiple subscriptions, issuing the unsubscribe without region data
	 * should remove all subscription interests for the class. 
	 */
	@Test
	public void testOCUnsubscribeWithoutRegionWhenPreviouslySubscribedWithMultipleRegions()
	{
		// subscribe to the class twice, with different regions
		listener.quickSubscribeWithRegion( aHandle, listenerRegion, aaHandle, abHandle, acHandle );
		int firstObject = validateSubscribed( listener, senderRegion, aHandle,
		                                      aaHandle, abHandle, acHandle );
		
		// now with listenerRegionOOB
		listener.quickSubscribeWithRegion(aHandle, listenerRegionOOB, aaHandle, abHandle, acHandle);
		int secondObject = validateSubscribed( listener, senderRegionOOB, aHandle,
		                                       aaHandle, abHandle, acHandle );
		
		// unsubscribe using the listenerRegion
		try
		{
			listener.rtiamb.unsubscribeObjectClass( aHandle );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing from object class with region data", e );
		}
		
		// validate that we are no longer subscribed to with the listenerRegion overlap
		validateNoLongerSubscribed( listener, firstObject, aaHandle, abHandle, acHandle );
		validateNoLongerSubscribed( listener, secondObject, aaHandle, abHandle, acHandle );
	}

	//////////////////////////////////////////////////////////////////
	// TEST: testOCUnsubscribeWithRegionUsingUndefinedObjectClass() //
	//////////////////////////////////////////////////////////////////
	@Test
	public void testOCUnsubscribeWithRegionUsingUndefinedObjectClass()
	{
		try
		{
			listener.rtiamb.unsubscribeObjectClassWithRegion( 1111111, listenerRegion );
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

	/////////////////////////////////////////////////////////////////
	// TEST: testOCUnsubscribeWithRegionUsingNegativeObjectClass() //
	/////////////////////////////////////////////////////////////////
	@Test
	public void testOCUnsubscribeWithRegionUsingNegativeObjectClass()
	{
		try
		{
			listener.rtiamb.unsubscribeObjectClassWithRegion( -1, listenerRegion );
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

	///////////////////////////////////////////////////////////
	// TEST: testOCUnsubscribeWithRegionUsingUnknownRegion() //
	///////////////////////////////////////////////////////////
	@Test
	public void testOCUnsubscribeWithRegionUsingUnknownRegion()
	{
		try
		{
			listener.rtiamb.unsubscribeObjectClassWithRegion( aHandle, senderRegion );
			expectedException( RegionNotKnown.class );
		}
		catch( RegionNotKnown rnk )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RegionNotKnown.class );
		}
	}

	////////////////////////////////////////////////////////
	// TEST: testOCUnsubscribeWithRegionUsingNullRegion() //
	////////////////////////////////////////////////////////
	@Test
	public void testOCUnsubscribeWithRegionUsingNullRegion()
	{
		try
		{
			listener.rtiamb.unsubscribeObjectClassWithRegion( aHandle, null );
			expectedException( RegionNotKnown.class );
		}
		catch( RegionNotKnown rnk )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RegionNotKnown.class );
		}
	}

	//////////////////////////////////////////////////////////
	// TEST: testOCUnsubscribeWithRegionWhenNotSubscribed() //
	//////////////////////////////////////////////////////////
	@Test
	public void testOCUnsubscribeWithRegionWhenNotSubscribed()
	{
		defaultFederate.quickJoin();

		try
		{
			Region tempRegion = defaultFederate.quickCreateTestRegion( 100,400 );
			defaultFederate.rtiamb.unsubscribeObjectClassWithRegion( bHandle, tempRegion );
			expectedException( FederateNotSubscribed.class );
		}
		catch( FederateNotSubscribed fns )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, FederateNotSubscribed.class );
		}
	}

	///////////////////////////////////////////////////////////////
	// TEST:  testOCUnsubscribeWithRegionWhenFederateNotJoined() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testOCUnsubscribeWithRegionWhenFederateNotJoined()
	{
		listener.quickResign();
		
		try
		{
			listener.rtiamb.unsubscribeObjectClassWithRegion( bHandle, listenerRegion );
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

	////////////////////////////////////////////////////////////
	// TEST:  testOCUnsubscribeWithRegionWhenSaveInProgress() //
	////////////////////////////////////////////////////////////
	@Test
	public void testOCUnsubscribeWithRegionWhenSaveInProgress()
	{
		listener.quickSaveInProgress( "save" );
		
		try
		{
			listener.rtiamb.unsubscribeObjectClassWithRegion( bHandle, listenerRegion );
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

	///////////////////////////////////////////////////////////////
	// TEST:  testOCUnsubscribeWithRegionWhenRestoreInProgress() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testOCUnsubscribeWithRegionWhenRestoreInProgress()
	{
		listener.quickRestoreInProgress( "save" );
		
		try
		{
			listener.rtiamb.unsubscribeObjectClassWithRegion( bHandle, listenerRegion );
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
