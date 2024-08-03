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
package hlaunit.hla13.ddm;

import hla.rti.FederateNotExecutionMember;
import hla.rti.InteractionClassNotDefined;
import hla.rti.InteractionClassNotSubscribed;
import hla.rti.InvalidRegionContext;
import hla.rti.Region;
import hla.rti.RegionNotKnown;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"SubscribeInteractionClassWithRegionTest",
                                   "subscribeInteractionWithRegion",
                                   "subscribe",
                                   "pubsub",
                                   "ddm"})
public class SubscribeInteractionClassWithRegionTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int xHandle;
	private int yHandle;
	private int zHandle;
	private int spaceHandle;
	private int dimensionHandle;
	private Test13Federate senderFederate;
	private Region sendingRegion;
	private Region sendingRegionOOB;
	private Region receivingRegion;
	private Region receivingRegionOOB;

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
		this.senderFederate = new Test13Federate( "SenderFederate", this );
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		senderFederate.quickJoin();
		senderFederate.quickPublish( "InteractionRoot.X" );     // associated with TestSpace
		senderFederate.quickPublish( "InteractionRoot.X.Y" );   // no space association
		senderFederate.quickPublish( "InteractionRoot.X.Y.Z" ); // associated with TestSpace
		
		// cache the handles
		xHandle = defaultFederate.quickICHandle( "InteractionRoot.X" );
		yHandle = defaultFederate.quickICHandle( "InteractionRoot.X.Y" );
		zHandle = defaultFederate.quickICHandle( "InteractionRoot.X.Y.Z" );
		spaceHandle = defaultFederate.quickSpaceHandle( "TestSpace" );
		dimensionHandle = defaultFederate.quickDimensionHandle( "TestSpace", "TestDimension" );

		///////////////////////////////
		// set up the regions to use //
		///////////////////////////////
		// sending region is used by the sender federate when sending an interaction
		sendingRegion = senderFederate.quickCreateRegion( spaceHandle, 1 );
		sendingRegion.setRangeLowerBound( 0, dimensionHandle, 100 );
		sendingRegion.setRangeUpperBound( 0, dimensionHandle, 200 );
		senderFederate.quickModifyRegion( sendingRegion );
		
		// sendingRegionOOB is used by the sender federate and is beyond the bounds of the
		// receivingRegion. That said, it does overlap with receivingRegionOOB
		sendingRegionOOB = senderFederate.quickCreateRegion( spaceHandle, 1 );
		sendingRegionOOB.setRangeLowerBound( 0, dimensionHandle, 250 );
		sendingRegionOOB.setRangeUpperBound( 0, dimensionHandle, 350 );
		senderFederate.quickModifyRegion( sendingRegionOOB );
		
		// receiving region is used by the receiving federate, it overlaps with sendingRegion
		receivingRegion = defaultFederate.quickCreateRegion( spaceHandle, 1 );
		receivingRegion.setRangeLowerBound( 0, dimensionHandle, 150 );
		receivingRegion.setRangeUpperBound( 0, dimensionHandle, 250 );
		defaultFederate.quickModifyRegion( receivingRegion );
		
		// receivingRegionOOB is also used by the receiving federate, except that it doesn't
		// overlap with the sending region (and thus, shouldn't result in updates)
		receivingRegionOOB = defaultFederate.quickCreateRegion( spaceHandle, 1 );
		receivingRegionOOB.setRangeLowerBound( 0, dimensionHandle, 200 );
		receivingRegionOOB.setRangeUpperBound( 0, dimensionHandle, 300 );
		defaultFederate.quickModifyRegion( receivingRegionOOB );
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
	private void validateSubscribed( Test13Federate federate, int classHandle )
	{
		// send the interaction
		senderFederate.quickSend( classHandle, null, "tag".getBytes() );
		// wait for the interaction
		federate.fedamb.waitForROInteraction( classHandle );
	}

	/**
	 * Same as {@link #validateSubscribed(Test13Federate, int)} except that the sending
	 * federate will send the interaction with region data.
	 * 
	 * @param region This is the region that should be used by the senderFederate
	 */
	private void validateSubscribedWithRegion( Test13Federate federate,
	                                           int classHandle,
	                                           Region region )
	{
		// send the interaction
		senderFederate.quickSendWithRegion( classHandle, null, "tag".getBytes(), region );
		// wait for the interaction
		federate.fedamb.waitForROInteraction( classHandle );
	}
	
	/**
	 * This is the opposite of {@link #validateSubscribed(Test13Federate, int)}. It will validate
	 * that the given federate is NOT subscribed to the interaction class of the provided handle.
	 * Once again, an interaction will be sent, however, this time, if the federate does NOT
	 * time-out waiting for it, Assert.fail will be used to kill the test.
	 */
	private void validateNotSubscribed( Test13Federate federate, int classHandle )
	{
		// send the interaction
		senderFederate.quickSend( classHandle, null, "tag".getBytes() );
		// wait for the interaction
		federate.fedamb.waitForROInteractionTimeout( classHandle );
	}

	/**
	 * Same as {@link #validateNotSubscribed(Test13Federate, int)} except that the sending
	 * federate will send the interaction with region information.
	 * 
	 * @param region This is the region that should be used by the senderFederate
	 */
	private void validateNotSubscribedWithRegion( Test13Federate federate,
	                                              int classHandle,
	                                              Region region )
	{
		// send the interaction
		senderFederate.quickSendWithRegion( classHandle, null, "tag".getBytes(), region );
		// wait for the interaction
		federate.fedamb.waitForROInteractionTimeout( classHandle );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Active Subscription Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void subscribeInteractionClassWithRegion( int theClass, Region theRegion )
	//        throws InteractionClassNotDefined,
	//               RegionNotKnown,
	//               InvalidRegionContext,
	//               FederateLoggingServiceCalls,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted

	///////////////////////////////////////////////
	// TEST: (valid) testICSubscribeWithRegion() //
	///////////////////////////////////////////////
	@Test
	public void testICSubscribeWithRegion()
	{
		// subscribe to the interaction
		try
		{
			defaultFederate.rtiamb.subscribeInteractionClassWithRegion( xHandle, receivingRegion );
		}
		catch( Exception e )
		{
			unexpectedException( "subscribing to interaction class with region", e );
		}
		
		// validate the subscription
		validateSubscribedWithRegion( defaultFederate, xHandle, sendingRegion );
	}

	///////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testICSubscribeWithRegionReceivesInteractionSentWithoutRegion() //
	///////////////////////////////////////////////////////////////////////////////////
	/**
	 * This test makes sure that a federate subscribed with a region will receive an
	 * interaction if it is sent without region data.
	 */
	@Test
	public void testICSubscribeWithRegionReceivesInteractionSentWithoutRegion()
	{
		// validate that we do no receive interactions for InteractionRoot.X
		validateNotSubscribed( defaultFederate, xHandle );
		
		// subscribe to InteractionRoot.X using region data
		defaultFederate.quickSubscribeWithRegion( xHandle, receivingRegion );
		
		// make sure we now receive an interaction sent WITHOUT a region
		validateSubscribed( defaultFederate, xHandle );
	}

	//////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testICSubscribeWithRegionWhereSendingClassIsSubclass() //
	//////////////////////////////////////////////////////////////////////////
	/**
	 * This test ensures that interactions sent whose type is a child class of the actual
	 * subscribed type, and who has a region that overlaps the subscription region, isn't
	 * filtered out by the federate. The following is a description of the steps it takes:
	 * <ol>
	 *   <li>defaultFederate subscribes to InteractionRoot.X with receivingRegion</li>
	 *   <li>senderFederate sends InteractionRoot.X.Y.Z with sendingRegion</li>
	 *   <li>defaultFederate SHOULD receive interaction as InteractionRoot.X</li>
	 * </ol>
	 */
	@Test
	public void testICSubscribeWithRegionWhereSendingClassIsSubclass()
	{
		// subscribe to InteractionRoot.X
		defaultFederate.quickSubscribeWithRegion( xHandle, receivingRegion );

		// ensure that it DOES NOT receive interactions that are sent of X.Y.Z
		// with a region where the region doesn't overlap
		senderFederate.quickSendWithRegion( zHandle, null, "tag".getBytes(), sendingRegionOOB );
		defaultFederate.fedamb.waitForROInteractionTimeout( xHandle );

		// ensure that is DOES receive interactions that are sent of X.Y.Z
		// with a region where that region does overlap, and that the received
		// interaction should be of type X
		senderFederate.quickSendWithRegion( zHandle, null, "tag".getBytes(), sendingRegion );
		defaultFederate.fedamb.waitForROInteraction( xHandle );
	}

	///////////////////////////////////////////////////////////////
	// TEST: (valid) testICSubscribeWithRegionThenModifyRegion() //
	///////////////////////////////////////////////////////////////
	/**
	 * This test will ensure that the modification of a region that was used by a federate
	 * to subscribe to an interaction class affects whether or not a federate receives the
	 * interaction.
	 */
	@Test
	public void testICSubscribeWithRegionThenModifyRegion() throws Exception
	{
		// subscribe to the interaction with the default region
		defaultFederate.quickSubscribeWithRegion( xHandle, receivingRegion );
		validateSubscribedWithRegion( defaultFederate, xHandle, sendingRegion );
		validateNotSubscribedWithRegion( defaultFederate, xHandle, sendingRegionOOB );
		
		// modify the region so that it is no longer overlaps with the sending region
		// make the values the same as that of receivingRegionOOB so that if the sender
		// sends an interaction with sendingRegionOOB, we will get it
		long newLower = receivingRegionOOB.getRangeLowerBound( 0, dimensionHandle );
		long newUpper = receivingRegionOOB.getRangeUpperBound( 0, dimensionHandle );
		receivingRegion.setRangeLowerBound( 0, dimensionHandle, newLower );
		receivingRegion.setRangeUpperBound( 0, dimensionHandle, newUpper );
		defaultFederate.quickModifyRegion( receivingRegion );
		
		// make sure we don't get the interaction when sent to the original region
		validateNotSubscribedWithRegion( defaultFederate, xHandle, sendingRegion );
		// make sure we DO get the interaction when it overlaps with the new values
		validateSubscribedWithRegion( defaultFederate, xHandle, sendingRegionOOB );
	}

	///////////////////////////////////////////////////////////////////
	// TEST: (valid) testICSubscribeWithRegionUsingMultipleRegions() //
	///////////////////////////////////////////////////////////////////
	/**
	 * When subscribing to a class with regions, multiple calls should just cause the region
	 * information to be extended (cumulative), rather than error out. In this way, a federate
	 * can be subscribed to an interaction with multiple regions.
	 * <p/>
	 * This test will subscribe to a second region and then validate that interactions send to
	 * that region (that are beyond the bounds of the original region) are received.
	 */
	@Test
	public void testICSubscribeWithRegionUsingMultipleRegions()
	{
		// subscribe to an interaction twice, each time using a different region
		defaultFederate.quickSubscribeWithRegion( xHandle, receivingRegion );
		defaultFederate.quickSubscribeWithRegion( xHandle, receivingRegionOOB );
		
		// validate those subscriptions
		validateSubscribedWithRegion( defaultFederate, xHandle, sendingRegion );
		validateSubscribedWithRegion( defaultFederate, xHandle, sendingRegionOOB );
	}

	/////////////////////////////////////////////////////////
	// TEST: testICSubscribeWithRegionUsingUnknownRegion() //
	/////////////////////////////////////////////////////////
	@Test
	public void testICSubscribeWithRegionUsingUnknownRegion()
	{
		// subscirbe to the interaction using the region from the other federate
		try
		{
			defaultFederate.rtiamb.subscribeInteractionClassWithRegion( xHandle, sendingRegion );
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

	//////////////////////////////////////////////////////
	// TEST: testICSubscribeWithRegionUsingNullRegion() //
	//////////////////////////////////////////////////////
	@Test
	public void testICSubscribeWithRegionUsingNullRegion()
	{
		try
		{
			defaultFederate.rtiamb.subscribeInteractionClassWithRegion( xHandle, null );
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

	/////////////////////////////////////////////////////////////////
	// TEST: testICSubscribeWithRegionUsingInvalidRegionForClass() //
	/////////////////////////////////////////////////////////////////
	@Test
	public void testICSubscribeWithRegionUsingInvalidRegionForClass()
	{
		// subscribe to the interaction InteractionRoot.X.Y using a valid region instance.
		// in the FOM, InteractionRoot.X.Y isn't associated with that region, should error out.
		try
		{
			defaultFederate.rtiamb.subscribeInteractionClassWithRegion( yHandle, receivingRegion );
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

	//////////////////////////////////////////////////////////
	// TEST: testICSubscribeWithRegionUsingUndefinedClass() //
	//////////////////////////////////////////////////////////
	@Test
	public void testICSubscribeWithRegionUsingUndefinedClass()
	{
		try
		{
			defaultFederate.rtiamb.subscribeInteractionClassWithRegion( 11111111, receivingRegion );
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

	////////////////////////////////////////////////////
	// TEST: testICSubscribeWithRegionWhenNotJoined() //
	////////////////////////////////////////////////////
	@Test
	public void testICSubscribeWithRegionWhenNotJoined()
	{
		// resign so we can run the test
		defaultFederate.quickResign();
		
		try
		{
			defaultFederate.rtiamb.subscribeInteractionClassWithRegion( xHandle, receivingRegion );
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
	// TEST: testICSubscribeWithRegionWhenSaveInProgress() //
	/////////////////////////////////////////////////////////
	@Test
	public void testICSubscribeWithRegionWhenSaveInProgress()
	{
		// resign so we can run the test
		defaultFederate.quickSaveInProgress( "save" );
		
		try
		{
			defaultFederate.rtiamb.subscribeInteractionClassWithRegion( xHandle, receivingRegion );
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
	// TEST: testICSubscribeWithRegionWhenRestoreInProgress() //
	////////////////////////////////////////////////////////////
	@Test
	public void testICSubscribeWithRegionWhenRestoreInProgress()
	{
		// resign so we can run the test
		defaultFederate.quickRestoreInProgress( "save" );
		
		try
		{
			defaultFederate.rtiamb.subscribeInteractionClassWithRegion( xHandle, receivingRegion );
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
	
	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Passive Subscription Methods //////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void subscribeInteractionClassPassivelyWithRegion( int theClass, Region theRegion )
	//        throws InteractionClassNotDefined,
	//               RegionNotKnown,
	//               InvalidRegionContext,
	//               FederateLoggingServiceCalls,
	//               FederateNotExecutionMember,
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

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Unsubscription Methods /////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void unsubscribeInteractionClassWithRegion( int theClass, Region theRegion )
	//        throws InteractionClassNotDefined,
	//               InteractionClassNotSubscribed,
	//               RegionNotKnown,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	/////////////////////////////////////////////////
	// TEST: (valid) testICUnsubscribeWithRegion() //
	/////////////////////////////////////////////////
	@Test
	public void testICUnsubscribeWithRegion()
	{
		// setup
		defaultFederate.quickSubscribeWithRegion( xHandle, receivingRegion );
		validateSubscribedWithRegion( defaultFederate, xHandle, sendingRegion );

		try
		{
			defaultFederate.rtiamb.unsubscribeInteractionClassWithRegion( xHandle,
			                                                              receivingRegion );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing from interaction class with region", e );
		}
		
		// validate that we are no longer subscribed
		validateNotSubscribedWithRegion( defaultFederate, xHandle, sendingRegion );
	}

	/////////////////////////////////////////////////////////////////////
	// TEST: (valid) testICUnsubscribeWithRegionUsingMultipleRegions() //
	/////////////////////////////////////////////////////////////////////
	/**
	 * This test will see the federate subscribe to two regions and validate each. It will then
	 * have the subscribing federate unsubscribe using a single region, ensuring that interactions
	 * sent to that region are no longer recevied, but ones sent to the first region are still
	 * received.
	 */
	@Test
	public void testICUnsubscribeWithRegionUsingMultipleRegions()
	{
		// subscribe to an interaction twice, each time using a different region
		defaultFederate.quickSubscribeWithRegion( xHandle, receivingRegion );
		defaultFederate.quickSubscribeWithRegion( xHandle, receivingRegionOOB );
		
		// validate those subscriptions
		validateSubscribedWithRegion( defaultFederate, xHandle, sendingRegion );
		validateSubscribedWithRegion( defaultFederate, xHandle, sendingRegionOOB );
		
		// NOTE: The receivingRegion upperBound is 250 and the sendingRegionOOB
		//       lowerBound is 250. Because the upperBound is non-inclusive, this
		//       should put it out of range.
		
		// unsubscribe to one of the regions
		defaultFederate.quickUnsubscribeICWithRegion( xHandle, receivingRegion );
		
		// make sure we still get interactions to the subscribed region, but not to the other
		validateNotSubscribedWithRegion( defaultFederate, xHandle, sendingRegion );
		validateSubscribedWithRegion( defaultFederate, xHandle, sendingRegionOOB );
		
		// unsubscribe from the other region now
		defaultFederate.quickUnsubscribeICWithRegion( xHandle, receivingRegionOOB );
		
		// make sure we are no longer get any interactions
		validateNotSubscribedWithRegion( defaultFederate, xHandle, sendingRegion );
		validateNotSubscribedWithRegion( defaultFederate, xHandle, sendingRegionOOB );
	}

	////////////////////////////////////////////////////////////////
	// TEST: testICUnsubscribeWithRegionUsingNonSubscribedClass() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testICUnsubscribeWithRegionUsingNonSubscribedClass()
	{
		try
		{
			// haven't yet subscribed to InteractionRoot.X, so this should error out
			defaultFederate.rtiamb.unsubscribeInteractionClassWithRegion( xHandle,
			                                                              receivingRegion );
			expectedException( InteractionClassNotSubscribed.class );
		}
		catch( InteractionClassNotSubscribed icns )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InteractionClassNotSubscribed.class );
		}
	}

	/////////////////////////////////////////////////////////////////////////////
	// TEST: testICUnsubscribeWithRegionUsingRegionThatIsntInSubscriptionSet() //
	/////////////////////////////////////////////////////////////////////////////
	/**
	 * According to the HLA1.3 spec, there is no error condition when this happens. So it
	 * should be allowed to proceed, but it shouldn't cause any actual change.
	 */
	@Test
	public void testICUnsubscribeWithRegionUsingRegionThatIsntInSubscriptionSet()
	{
		// subscribe to InteractionRoot.X with receivingRegion, but NOT receivingRegionOOB
		defaultFederate.quickSubscribeWithRegion( xHandle, receivingRegion );
		validateSubscribedWithRegion( defaultFederate, xHandle, sendingRegion );

		try
		{
			defaultFederate.rtiamb.unsubscribeInteractionClassWithRegion( xHandle,
			                                                              receivingRegionOOB );
		}
		catch( Exception e )
		{
			unexpectedException( "unsubscribing with region that wasn't in subscription set", e );
		}
	}

	///////////////////////////////////////////////////////////
	// TEST: testICUnsubscribeWithRegionUsingUnknownRegion() //
	///////////////////////////////////////////////////////////
	@Test
	public void testICUnsubscribeWithRegionUsingUnknownRegion()
	{
		// make sure we are subscribed so that non-subscription can't be a source of error
		defaultFederate.quickSubscribeWithRegion( xHandle, receivingRegion );
		validateSubscribedWithRegion( defaultFederate, xHandle, sendingRegion );

		try
		{
			defaultFederate.rtiamb.unsubscribeInteractionClassWithRegion( xHandle, sendingRegion );
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
	// TEST: testICUnsubscribeWithRegionUsingNullRegion() //
	////////////////////////////////////////////////////////
	@Test
	public void testICUnsubscribeWithRegionUsingNullRegion()
	{
		// make sure we are subscribed so that non-subscription can't be a source of error
		defaultFederate.quickSubscribeWithRegion( xHandle, receivingRegion );
		validateSubscribedWithRegion( defaultFederate, xHandle, sendingRegion );

		try
		{
			defaultFederate.rtiamb.unsubscribeInteractionClassWithRegion( xHandle, null );
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

	//////////////////////////////////////////////////////////////////
	// TEST: testICUnsubscribeWithRegionUsingUndefinedClassHandle() //
	//////////////////////////////////////////////////////////////////
	@Test
	public void testICUnsubscribeWithRegionUsingUndefinedClassHandle()
	{
		try
		{
			defaultFederate.rtiamb.unsubscribeInteractionClassWithRegion( 11111111, 
			                                                              receivingRegion );
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
	// TEST: testICUnsubscribeWithRegionWhenNotJoined() //
	//////////////////////////////////////////////////////
	@Test
	public void testICUnsubscribeWithRegionWhenNotJoined()
	{
		// resign so we can run the test
		defaultFederate.quickResign();

		try
		{
			defaultFederate.rtiamb.unsubscribeInteractionClassWithRegion( xHandle,
			                                                              receivingRegion );
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

	///////////////////////////////////////////////////////////
	// TEST: testICUnsubscribeWithRegionWhenSaveInProgress() //
	///////////////////////////////////////////////////////////
	@Test
	public void testICUnsubscribeWithRegionWhenSaveInProgress()
	{
		// resign so we can run the test
		defaultFederate.quickSaveInProgress( "save" );

		try
		{
			defaultFederate.rtiamb.unsubscribeInteractionClassWithRegion( xHandle,
			                                                              receivingRegion );
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

	//////////////////////////////////////////////////////////////
	// TEST: testICUnsubscribeWithRegionWhenRestoreInProgress() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testICUnsubscribeWithRegionWhenRestoreInProgress()
	{
		// resign so we can run the test
		defaultFederate.quickRestoreInProgress( "save" );

		try
		{
			defaultFederate.rtiamb.unsubscribeInteractionClassWithRegion( xHandle,
			                                                              receivingRegion );
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
