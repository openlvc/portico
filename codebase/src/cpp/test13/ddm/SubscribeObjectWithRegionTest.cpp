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
#include "SubscribeObjectWithRegionTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( SubscribeObjectWithRegionTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SubscribeObjectWithRegionTest, "SubscribeObjectWithRegionTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SubscribeObjectWithRegionTest, "subscribeObjectWithRegion" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SubscribeObjectWithRegionTest, "ddm" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
SubscribeObjectWithRegionTest::SubscribeObjectWithRegionTest()
{
	this->defaultFederate = new Test13Federate( "defaultFederate" );
	this->listenerFederate = new Test13Federate( "listenerFederate" );
}

SubscribeObjectWithRegionTest::~SubscribeObjectWithRegionTest()
{
	delete this->listenerFederate;
	delete this->defaultFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void SubscribeObjectWithRegionTest::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->listenerFederate->quickJoin();
	
	// get the handle information
	this->aHandle  = defaultFederate->quickOCHandle( "ObjectRoot.A" );
	this->aaHandle = defaultFederate->quickACHandle( "ObjectRoot.A", "aa" );
	this->abHandle = defaultFederate->quickACHandle( "ObjectRoot.A", "ab" );
	
	// subscribe in the listener federates
	defaultFederate->quickPublish( "ObjectRoot.A", 2, "aa", "ab" );
	this->theSet = defaultFederate->populatedAHS( 2, aaHandle, abHandle );
	
	// create the regions
	this->senderRegion = defaultFederate->quickCreateTestRegion( 100, 200 );
	this->senderRegionOOB = defaultFederate->quickCreateTestRegion( 1000, 2000 );
	this->listenerRegion = listenerFederate->quickCreateTestRegion( 150, 250 );
	this->listenerRegionOOB = listenerFederate->quickCreateTestRegion( 1500, 2500 );
	this->otherRegion = listenerFederate->quickCreateOtherRegion( 150, 250 );
	
	// gonna create me some objects
	this->ibObject =
		defaultFederate->quickRegisterWithRegion( "ObjectRoot.A", senderRegion, 2, "aa", "ab" ); 
	this->oobObject = 
		defaultFederate->quickRegisterWithRegion( "ObjectRoot.A", senderRegionOOB, 2, "aa", "ab" ); 
}

void SubscribeObjectWithRegionTest::tearDown()
{
	delete theSet;
	delete senderRegion;
	delete senderRegionOOB;
	delete listenerRegion;
	delete listenerRegionOOB;
	delete otherRegion;
	
	this->listenerFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Subscribe Test Methods //////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

void SubscribeObjectWithRegionTest::validateSubscribed( RTI::ObjectHandle testObject )
{
	// update the values for the provided object
	defaultFederate->quickReflect( testObject, 2, "aa", "ab" );
	
	// wait for update in listener (discovery should happen while ticking if not already discovered)
	Test13Object *ng6Object = listenerFederate->fedamb->waitForROUpdate( testObject );
	
	// validate the attribute values
	int result = strcmp( "aa", ng6Object->getAttribute(aaHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "ab", ng6Object->getAttribute(abHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
}

void SubscribeObjectWithRegionTest::validateNotSubscribed( RTI::ObjectHandle testObject )
{
	// update the values for the provided object
	defaultFederate->quickReflect( testObject, 2, "aa", "ab" );
	
	// wait for the reflection that shouldn't arrive
	listenerFederate->fedamb->waitForROUpdateTimeout( testObject );
}

////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Subscribe Test Methods //////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
// subscribeObjectClassAttributesWithRegion( RTI::ObjectClassHandle theClass,
//                                           RTI::Region &theRegion,
//                                           const RTI::AttributeHandleSet &attributeList,
//                                           RTI::Boolean active )
//     throw( RTI::ObjectClassNotDefined,
//            RTI::AttributeNotDefined,
//            RTI::RegionNotKnown,
//            RTI::InvalidRegionContext,
//            RTI::FederateNotExecutionMember,
//            RTI::ConcurrentAccessAttempted,
//            RTI::SaveInProgress,
//            RTI::RestoreInProgress,
//            RTI::RTIinternalError )

///////////////////////////////////////////////
// TEST: (valid) testOCSubscribeWithRegion() //
///////////////////////////////////////////////
void SubscribeObjectWithRegionTest::testOCSubscribeWithRegion()
{
	// validate that we are subscribed in neither region
	validateNotSubscribed( ibObject );
	validateNotSubscribed( oobObject );
	
	// subscribe to the class in the OOB region only
	try
	{
		listenerFederate->rtiamb->subscribeObjectClassAttributesWithRegion( aHandle,
		                                                                    *listenerRegionOOB,
		                                                                    *theSet );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception subscribing to attributes with region: %s", e._reason );
	}
	
	// validate that we now get updates
	validateNotSubscribed( ibObject );
	validateSubscribed( oobObject );
	
	// subscribe in the inbound region now and make sure we get updates for both
	try
	{
		listenerFederate->rtiamb->subscribeObjectClassAttributesWithRegion( aHandle,
		                                                                    *listenerRegion,
		                                                                    *theSet );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception subscribing to attributes with region: %s", e._reason );
	}
	
	// validate subscribed in both
	validateSubscribed( ibObject );
	validateSubscribed( oobObject );
}

////////////////////////////////////////////////////////////////
// TEST: (valid) testOCSubscribeWithRegionAndEmptyHandleSet() //
////////////////////////////////////////////////////////////////
void SubscribeObjectWithRegionTest::testOCSubscribeWithRegionAndEmptyHandleSet()
{
	// make sure that if we subscribe with an empty handle set before we are subscribed
	// that it doesn't throw an exception because we are not yet subscribed
	listenerFederate->quickSubscribeWithRegion( "ObjectRoot.A", listenerRegion, 0 );
	validateNotSubscribed( ibObject );
	
	// subscribe and validate that we are subscribed
	listenerFederate->quickSubscribeWithRegion( "ObjectRoot.A", listenerRegion, 2, "aa", "ab" );
	validateSubscribed( ibObject );
	
	// subscribe with an empty handle set, this should be an IMPLICIT UNSUBSCRIBE
	listenerFederate->quickSubscribeWithRegion( "ObjectRoot.A", listenerRegion, 0 );
	validateNotSubscribed( ibObject );
}

/////////////////////////////////////////////////////////////////////
// TEST:  (valid) testOCSubscribeWithRegionWhenAlreadySubscribed() //
/////////////////////////////////////////////////////////////////////
void SubscribeObjectWithRegionTest::testOCSubscribeWithRegionWhenAlreadySubscribed()
{
	// do the same subscription twice, this shouldn't cause any problems
	listenerFederate->quickSubscribeWithRegion( "ObjectRoot.A", listenerRegion, 2, "aa", "ab" );
	validateSubscribed( ibObject );

	listenerFederate->quickSubscribeWithRegion( "ObjectRoot.A", listenerRegion, 2, "aa", "ab" );
	validateSubscribed( ibObject );
}

/////////////////////////////////////////////////////////
// TEST: testOCSubscribeWithRegionUsingUnknownRegion() //
/////////////////////////////////////////////////////////
void SubscribeObjectWithRegionTest::testOCSubscribeWithRegionUsingUnknownRegion()
{
	try
	{
		listenerFederate->rtiamb->subscribeObjectClassAttributesWithRegion( aHandle,
		                                                                    *senderRegion,
		                                                                    *theSet );
		failTestMissingException( "RegionNotKnown",
		                          "subscribing to OC with region created in other federate" );
	}
	catch( RTI::RegionNotKnown &rnk )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "RegionNotKnown", e,
		                        "subscribing to OC with region created in other federate" );
	}
}

/////////////////////////////////////////////////////////
// TEST: testOCSubscribeWithRegionUsingInvalidRegion() //
/////////////////////////////////////////////////////////
void SubscribeObjectWithRegionTest::testOCSubscribeWithRegionUsingInvalidRegion()
{
	try
	{
		listenerFederate->rtiamb->subscribeObjectClassAttributesWithRegion( aHandle,
		                                                                    *otherRegion,
		                                                                    *theSet );
		failTestMissingException( "InvalidRegionContext",
		                          "subscribing to OC using region not valid according to FOM" );
	}
	catch( RTI::InvalidRegionContext &irc )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "InvalidRegionContext", e,
		                        "subscribing to OC using region not valid according to FOM" );
	}
}

////////////////////////////////////////////////////////////
// TEST: testOCSubscribeWithRegionAndInvalidClassHandle() //
////////////////////////////////////////////////////////////
void SubscribeObjectWithRegionTest::testOCSubscribeWithRegionAndInvalidClassHandle()
{
	try
	{
		listenerFederate->rtiamb->subscribeObjectClassAttributesWithRegion( 100000,
		                                                                    *listenerRegion,
		                                                                    *theSet );
		failTestMissingException( "ObjectClassNotDefined", "subscribing w/ invalid class handle" );
	}
	catch( RTI::ObjectClassNotDefined &ocnd )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "ObjectClassNotDefined", e, "subscribing w/ invalid class handle" );
	}
}

////////////////////////////////////////////////////////////////
// TEST: testOCSubscribeWithRegionAndInvalidAttributeHandle() //
////////////////////////////////////////////////////////////////
void SubscribeObjectWithRegionTest::testOCSubscribeWithRegionAndInvalidAttributeHandle()
{
	RTI::AttributeHandleSet *handleSet =
		listenerFederate->populatedAHS( 3, aaHandle, abHandle, 11111111 );
	
	try
	{
		listenerFederate->rtiamb->subscribeObjectClassAttributesWithRegion( aHandle,
		                                                                    *listenerRegion,
		                                                                    *handleSet );
		delete handleSet;
		failTestMissingException( "AttributeNotDefined", "subscribing w/ invalid attribute" );
	}
	catch( RTI::AttributeNotDefined &attnd )
	{
		// success!
		delete handleSet;
	}
	catch( RTI::Exception &e )
	{
		delete handleSet;
		failTestWrongException( "AttributeNotDefined", e, "subscribing w/ invalid attribute" );
	}
}

////////////////////////////////////////////////////
// TEST: testOCSubscribeWithRegionWhenNotJoined() //
////////////////////////////////////////////////////
void SubscribeObjectWithRegionTest::testOCSubscribeWithRegionWhenNotJoined()
{
	listenerFederate->quickResign();

	try
	{
		listenerFederate->rtiamb->subscribeObjectClassAttributesWithRegion( aHandle,
		                                                                    *listenerRegion,
		                                                                    *theSet );
		failTestMissingException( "FederateNotExecutionMember", "subscribing when not joined" );
	}
	catch( RTI::FederateNotExecutionMember &fnem )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "FederateNotExecutionMember", e, "subscribing when not joined" );
	}
}

////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////// Unsubscribe Test Methods /////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
// unsubscribeObjectClassWithRegion( RTI::ObjectClassHandle theClass, RTI::Region &theRegion )
//     throw( RTI::ObjectClassNotDefined,
//            RTI::RegionNotKnown,
//            RTI::ObjectClassNotSubscribed,
//            RTI::FederateNotExecutionMember,
//            RTI::ConcurrentAccessAttempted,
//            RTI::SaveInProgress,
//            RTI::RestoreInProgress,
//            RTI::RTIinternalError )

/////////////////////////////////////////////////
// TEST: (valid) testOCUnsubscribeWithRegion() //
/////////////////////////////////////////////////
void SubscribeObjectWithRegionTest::testOCUnsubscribeWithRegion()
{
	// subscirbe in both regions and validate that we are
	listenerFederate->quickSubscribeWithRegion( aHandle, listenerRegion, 2, aaHandle, abHandle );
	listenerFederate->quickSubscribeWithRegion( aHandle, listenerRegionOOB, 2, aaHandle, abHandle );
	validateSubscribed( ibObject );
	validateSubscribed( oobObject );
	
	// unsubscribe from the OOB region
	try
	{
		listenerFederate->rtiamb->unsubscribeObjectClassWithRegion( aHandle, *listenerRegionOOB );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while unsubscribing from OC with region: %s", e._reason );
	}
	
	// validate that we are no longer subscribed for OOB
	validateSubscribed( ibObject );
	validateNotSubscribed( oobObject );
	
	// unsubscribe from the other region which should remove all subscriptions
	try
	{
		listenerFederate->rtiamb->unsubscribeObjectClassWithRegion( aHandle, *listenerRegion );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while unsubscribing from OC with region: %s", e._reason );
	}
	
	// validate that we are no longer subscribed for OOB
	validateNotSubscribed( ibObject );
	validateNotSubscribed( oobObject );
}

///////////////////////////////////////////////////////////
// TEST: testOCUnsubscribeWithRegionUsingUnknownRegion() //
///////////////////////////////////////////////////////////
void SubscribeObjectWithRegionTest::testOCUnsubscribeWithRegionUsingUnknownRegion()
{
	try
	{
		listenerFederate->rtiamb->unsubscribeObjectClassWithRegion( aHandle, *senderRegion );
		failTestMissingException( "RegionNotKnown",
		                          "unsubscribing to OC with region created in other federate" );
	}
	catch( RTI::RegionNotKnown &rnk )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "RegionNotKnown", e,
		                        "unsubscribing to OC with region created in other federate" );
	}
}

//////////////////////////////////////////////////////////////
// TEST: testOCUnsubscribeWithRegionAndInvalidClassHandle() //
//////////////////////////////////////////////////////////////
void SubscribeObjectWithRegionTest::testOCUnsubscribeWithRegionAndInvalidClassHandle()
{
	try
	{
		listenerFederate->rtiamb->unsubscribeObjectClassWithRegion( 100000, *listenerRegion );
		failTestMissingException( "ObjectClassNotDefined","unsubscribing w/ invalid class handle" );
	}
	catch( RTI::ObjectClassNotDefined& ocnd )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "ObjectClassNotDefined", e,
		                        "unsubscribing w/ invalid class handle" );
	}
}

/////////////////////////////////////////////////////////
// TEST: testOCUnsubscribeWithRegionWhenNotPublished() //
/////////////////////////////////////////////////////////
void SubscribeObjectWithRegionTest::testOCUnsubscribeWithRegionWhenNotPublished()
{
	try
	{
		listenerFederate->rtiamb->unsubscribeObjectClassWithRegion( aHandle, *listenerRegion );
		failTestMissingException( "ObjectClassNotSubscribed", "unsubscribing when not subscribed" );
	}
	catch( RTI::ObjectClassNotSubscribed &ocns )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "ObjectClassNotSubscribed",e,"unsubscribing when not subscribed" );
	}
}

//////////////////////////////////////////////////////
// TEST: testOCUnsubscribeWithRegionWhenNotJoined() //
//////////////////////////////////////////////////////
void SubscribeObjectWithRegionTest::testOCUnsubscribeWithRegionWhenNotJoined()
{
	listenerFederate->quickResign();

	try
	{
		listenerFederate->rtiamb->unsubscribeObjectClassWithRegion( aHandle, *listenerRegion );
		failTestMissingException( "FederateNotExecutionMember", "unsubscribing when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e, "unsubscribing when not joined" );
	}
}
