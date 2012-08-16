/*
 *   Copyright 2008 The Portico Project
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
#include "SubscribeInteractionWithRegionTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( SubscribeInteractionWithRegionTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SubscribeInteractionWithRegionTest,
                                       "SubscribeInteractionWithRegionTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SubscribeInteractionWithRegionTest,
                                       "subscribeInteractionWithRegion" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SubscribeInteractionWithRegionTest, "ddm" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
SubscribeInteractionWithRegionTest::SubscribeInteractionWithRegionTest()
{
	this->defaultFederate = new TestNG6Federate( "defaultFederate" );
	this->listenerFederate = new TestNG6Federate( "listenerFederate" );
}

SubscribeInteractionWithRegionTest::~SubscribeInteractionWithRegionTest()
{
	delete this->listenerFederate;
	delete this->defaultFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void SubscribeInteractionWithRegionTest::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->listenerFederate->quickJoin();
	
	// get the class handles
	this->xHandle = defaultFederate->quickICHandle( "InteractionRoot.X" );
	this->xaHandle = defaultFederate->quickPCHandle( "InteractionRoot.X", "xa" );
	this->xbHandle = defaultFederate->quickPCHandle( "InteractionRoot.X", "xb" );
	this->xcHandle = defaultFederate->quickPCHandle( "InteractionRoot.X", "xc" );
	
	// do pub/sub
	this->defaultFederate->quickPublish( "InteractionRoot.X" );
	
	// create the regions
	this->senderRegion = defaultFederate->quickCreateTestRegion( 100, 200 );
	this->senderRegionOOB = defaultFederate->quickCreateTestRegion( 1000, 2000 );
	this->listenerRegion = listenerFederate->quickCreateTestRegion( 150, 250 );
	this->listenerRegionOOB = listenerFederate->quickCreateTestRegion( 1500, 2500 );
	this->otherRegion = listenerFederate->quickCreateOtherRegion( 150, 250 );
}

void SubscribeInteractionWithRegionTest::tearDown()
{
	delete this->otherRegion;
	delete this->senderRegion;
	delete this->senderRegionOOB;
	delete this->listenerRegion;
	delete this->listenerRegionOOB;

	this->listenerFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Subscribe Test Methods //////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

void SubscribeInteractionWithRegionTest::validateSubscribed( RTI::Region *regionToSend )
{
	// send an interaction from the default federate
	defaultFederate->quickSendWithRegion( "InteractionRoot.X", regionToSend, 3, "xa", "xb", "xc" );
	
	// wait for it to be received in the listener
	TestNG6Interaction *received = listenerFederate->fedamb->waitForROInteraction( xHandle );

	// validate the parameters for the interaction
	int result = strcmp( "xa", received->getParameter(xaHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "xb", received->getParameter(xbHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "xc", received->getParameter(xcHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	
	// clean up
	delete received;
}

void SubscribeInteractionWithRegionTest::validateNotSubscribed( RTI::Region *regionToSend )
{
	// send an interaction from the default federate
	defaultFederate->quickSendWithRegion( "InteractionRoot.X", regionToSend, 3, "xa", "xb", "xc" );
	
	// make sure the received doesn't get anything
	listenerFederate->fedamb->waitForROInteractionTimeout( xHandle );
}

////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Subscribe Test Methods //////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
// subscribeInteractionClassWithRegion( RTI::InteractionClassHandle theClass,
//                                      RTI::Region &theRegion,
//                                      RTI::Boolean active )
//     throw( RTI::InteractionClassNotDefined,
//            RTI::RegionNotKnown,
//            RTI::InvalidRegionContext,
//            RTI::FederateLoggingServiceCalls,
//            RTI::FederateNotExecutionMember,
//            RTI::ConcurrentAccessAttempted,
//            RTI::SaveInProgress,
//            RTI::RestoreInProgress,
//            RTI::RTIinternalError )

/////////////////////////////////////
// TEST: (valid) testICSubscribe() //
/////////////////////////////////////
/*
 * 1. Validate that the listener isn't receiving interactions
 * 2. Subscribe, but with the OOB region
 * 3. Validate that the listener still isn't receiving interactions
 * 4. Subscribe with the normal region
 * 5. Validate that the listener does receive the interaction
 */ 
void SubscribeInteractionWithRegionTest::testICSubscribe()
{
	// 1. validate that we're not yet subscribed
	validateNotSubscribed( senderRegion );
	
	// 2. subscribe with OOB region
	try
	{
		listenerFederate->rtiamb->subscribeInteractionClassWithRegion( xHandle, *listenerRegionOOB );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while subscribing to interaction with region: %s",
		          e._reason );
	}
	
	// 3. validate that we're still not receiving interactions as they're being sent
	//    with a region that doesn't overlap the subscription region
	validateNotSubscribed( senderRegion );
	
	// 4. subscribe with the normal region, which should bring us into an overlap
	try
	{
		listenerFederate->rtiamb->subscribeInteractionClassWithRegion( xHandle, *listenerRegion );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while subscribing to interaction with region: %s",
		          e._reason );
	}
	
	// 5. validate that we do now receive the interaction
	validateSubscribed( senderRegion );
}

//////////////////////////////////////////////
// TEST: testICSubscribeWithUnknownRegion() //
//////////////////////////////////////////////
void SubscribeInteractionWithRegionTest::testICSubscribeWithUnknownRegion()
{
	try
	{
		listenerFederate->rtiamb->subscribeInteractionClassWithRegion( xHandle, *senderRegion );
		failTestMissingException( "RegionNotKnown", 
		                          "subscribing to interaction with region create by other federate" );
	}
	catch( RTI::RegionNotKnown &rnk )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "RegionNotKnown", e,
		                        "subscribing to interaction with region create by other federate" );
	}
}

//////////////////////////////////////////////
// TEST: testICSubscribeWithInvalidRegion() //
//////////////////////////////////////////////
void SubscribeInteractionWithRegionTest::testICSubscribeWithInvalidRegion()
{
	try
	{
		listenerFederate->rtiamb->subscribeInteractionClassWithRegion( xHandle, *otherRegion );
		failTestMissingException( "InvalidRegionContext", 
		                          "subscribing to interaction with region of wrong type for class" );
	}
	catch( RTI::InvalidRegionContext &irc )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "InvalidRegionContext", e,
		                        "subscribing to interaction with region of wrong type for class" );
	}
}

//////////////////////////////////////////////
// TEST: testICSubscribeWithInvalidHandle() //
//////////////////////////////////////////////
void SubscribeInteractionWithRegionTest::testICSubscribeWithInvalidHandle()
{
	try
	{
		listenerFederate->rtiamb->subscribeInteractionClassWithRegion( 10000000, *listenerRegion );
		failTestMissingException( "InteractionClassNotDefined", 
		                          "subscribing to interaction class with invalid handle" );
	}
	catch( RTI::InteractionClassNotDefined& icnd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "InteractionClassNotDefined", e,
		                        "subscribing to interaction class with invalid handle" );
	}
}

//////////////////////////////////////////
// TEST: testICSubscribeWhenNotJoined() //
//////////////////////////////////////////
void SubscribeInteractionWithRegionTest::testICSubscribeWhenNotJoined()
{
	listenerFederate->quickResign();

	try
	{
		listenerFederate->rtiamb->subscribeInteractionClassWithRegion( xHandle, *listenerRegion );
		failTestMissingException( "FederateNotExecutionMember", 
		                          "subscribing to interaction class when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "subscribing to interaction class when not joined" );
	}
}

////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////// Unsubscribe Test Methods /////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
// unsubscribeInteractionClassWithRegion( RTI::InteractionClassHandle theClass,
//                                        RTI::Region &theRegion )
//     throw( RTI::InteractionClassNotDefined,
//            RTI::InteractionClassNotSubscribed,
//            RTI::RegionNotKnown,
//            RTI::FederateNotExecutionMember,
//            RTI::ConcurrentAccessAttempted,
//            RTI::SaveInProgress,
//            RTI::RestoreInProgress,
//            RTI::RTIinternalError )

///////////////////////////////////////
// TEST: (valid) testICUnsubscribe() //
///////////////////////////////////////
/*
 * 1. Subscribe with both regular and OOB region
 * 2. Validate interactions sent to both regions are received
 * 3. Unsubscribe from OOB region
 * 4. Validate interactions to regular region get through, but not those to OOB region
 * 5. Unsubscribe from normal region
 * 6. Validate no interactions get through
 */ 
void SubscribeInteractionWithRegionTest::testICUnsubscribe()
{
	// 1. subscribe to interaction in both regions
	listenerFederate->quickSubscribeWithRegion( "InteractionRoot.X", listenerRegion );
	listenerFederate->quickSubscribeWithRegion( "InteractionRoot.X", listenerRegionOOB );
	
	// 2. validate interactions sent to both regions are received
	validateSubscribed( senderRegion );
	validateSubscribed( senderRegionOOB );
	
	// 3. unsubscribe from OOB region
	try
	{
		listenerFederate->rtiamb->unsubscribeInteractionClassWithRegion( xHandle,
		                                                                 *listenerRegionOOB );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception unsubscribing from interaction with region: %s", e._reason );
	}
	
	// 4. validate that interactions to OOB not received, while interactions to normal are
	validateSubscribed( senderRegion );
	validateNotSubscribed( senderRegionOOB );
	
	// 5. unsubscribe from the normal region
	try
	{
		listenerFederate->rtiamb->unsubscribeInteractionClassWithRegion( xHandle, *listenerRegion );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception unsubscribing from interaction with region: %s", e._reason );
	}
	
	// 6. validate no longer subscribed in either region
	validateNotSubscribed( senderRegion );
	validateNotSubscribed( senderRegionOOB );
}

///////////////////////////////////////////////////////////////////////////
// TEST: (valid) testICUnsubscribeWithoutRegionRemovesAllSubscriptions() //
///////////////////////////////////////////////////////////////////////////
/*
 * If we are subscribed with regions and then unsubscribe without providing region data, the
 * entire subscription (for all regions) should be removed.
 */ 
void SubscribeInteractionWithRegionTest::testICUnsubscribeWithoutRegionRemovesAllSubscriptions()
{
	// subscribe to interaction in both regions and validate
	listenerFederate->quickSubscribeWithRegion( "InteractionRoot.X", listenerRegion );
	listenerFederate->quickSubscribeWithRegion( "InteractionRoot.X", listenerRegionOOB );
	validateSubscribed( senderRegion );
	validateSubscribed( senderRegionOOB );

	// unsubscribe without region and validate that we are no longer subscribed in any way
	try
	{
		listenerFederate->rtiamb->unsubscribeInteractionClass( xHandle );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception unsubscribing from interaction: %s", e._reason );
	}
	
	// 6. validate no longer subscribed in either region
	validateNotSubscribed( senderRegion );
	validateNotSubscribed( senderRegionOOB );
}

////////////////////////////////////////////////
// TEST: testICUnsubscribeWithUnknownRegion() //
////////////////////////////////////////////////
void SubscribeInteractionWithRegionTest::testICUnsubscribeWithUnknownRegion()
{
	// subscribe so that this can't be a source of error below (we want to isolate a specific
	// error, so we have to remove the possibility of others)
	listenerFederate->quickSubscribeWithRegion( "InteractionRoot.X", listenerRegion );

	try
	{
		listenerFederate->rtiamb->unsubscribeInteractionClassWithRegion( xHandle, *senderRegion );
		failTestMissingException( "RegionNotKnown", 
		                          "unsubscribing interaction with region created in other federate" );
	}
	catch( RTI::RegionNotKnown &rnk )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "RegionNotKnown", e,
		                        "subscribing interaction with region created in other federate" );
	}
}

////////////////////////////////////////////////
// TEST: testICUnsubscribeWithInvalidHandle() //
////////////////////////////////////////////////
void SubscribeInteractionWithRegionTest::testICUnsubscribeWithInvalidHandle()
{
	try
	{
		listenerFederate->rtiamb->unsubscribeInteractionClassWithRegion( 1000000, *listenerRegion );
		failTestMissingException( "InteractionClassNotDefined", 
		                          "unsubscribing to interaction class with invalid handle" );
	}
	catch( RTI::InteractionClassNotDefined& icnd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "InteractionClassNotDefined", e,
		                        "unsubscribing to interaction class with invalid handle" );
	}
}

////////////////////////////////////////////////
// TEST: testICUnsubscribeWhenNotSubscribed() //
////////////////////////////////////////////////
void SubscribeInteractionWithRegionTest::testICUnsubscribeWhenNotSubscribed()
{
	try
	{
		listenerFederate->rtiamb->unsubscribeInteractionClassWithRegion( xHandle, *listenerRegion );
		failTestMissingException( "InteractionClassNotSubscribed", 
		                          "unsubscribing interaction when not subscribed" );
	}
	catch( RTI::InteractionClassNotSubscribed &icns )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "InteractionClassNotSubscribed", e,
		                        "unsubscribing interaction when not subscribed" );
	}
}

////////////////////////////////////////////
// TEST: testICUnsubscribeWhenNotJoined() //
////////////////////////////////////////////
void SubscribeInteractionWithRegionTest::testICUnsubscribeWhenNotJoined()
{
	listenerFederate->quickResign();

	try
	{
		listenerFederate->rtiamb->unsubscribeInteractionClassWithRegion( xHandle, *listenerRegion );
		failTestMissingException( "FederateNotExecutionMember", 
		                          "unsubscribing from interaction class when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "unsubscribing from interaction class when not joined" );
	}
}

