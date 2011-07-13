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
#include "SubscribeObjectTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( SubscribeObjectTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SubscribeObjectTest, "SubscribeObjectTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SubscribeObjectTest, "subscribeObject" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SubscribeObjectTest, "declarationManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
SubscribeObjectTest::SubscribeObjectTest()
{
	this->defaultFederate = new TestNG6Federate( "defaultFederate" );
	this->listener = new TestNG6Federate( "listener" );
}

SubscribeObjectTest::~SubscribeObjectTest()
{
	delete this->listener;
	delete this->defaultFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void SubscribeObjectTest::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->listener->quickJoin();
	
	// get the handle information
	this->aaHandle = defaultFederate->quickACHandle( "ObjectRoot.A", "aa" );
	this->bHandle  = defaultFederate->quickOCHandle( "ObjectRoot.A.B" );
	this->baHandle = defaultFederate->quickACHandle( "ObjectRoot.A.B", "ba" );
	
	// subscribe in the listener federates
	defaultFederate->quickPublish( "ObjectRoot.A.B", 6, "aa", "ab", "ac", "ba", "bb", "bc" );
	this->theSet = defaultFederate->populatedAHS( 2, aaHandle, baHandle );
}

void SubscribeObjectTest::tearDown()
{
	delete theSet;
	this->listener->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Subscribe Test Methods //////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

void SubscribeObjectTest::validateSubscribed()
{
	// register an instance of the provided object class
	RTI::ObjectHandle theObject = defaultFederate->quickRegister( bHandle );
	
	// update the values for the newly created instance
	defaultFederate->quickReflect( theObject, 2, "aa", "ba" );
	
	// wait for the update in the listener
	TestNG6Object *ng6Object = listener->fedamb->waitForROUpdate( theObject );
	
	// validate the attribute values
	int result = strcmp( "aa", ng6Object->getAttribute(aaHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "ba", ng6Object->getAttribute(baHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
}

/////////////////////////////////////
// TEST: (valid) testOCSubscribe() //
/////////////////////////////////////
void SubscribeObjectTest::testOCSubscribe()
{
	try
	{
		listener->rtiamb->subscribeObjectClassAttributes( bHandle, *theSet );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while subscribing to object attributes: %s", e._reason );
	}
	
	validateSubscribed();
}

//////////////////////////////////////////////////////////
// TEST: (valid) testOCSubscribeWhenAlreadySubscribed() //
//////////////////////////////////////////////////////////
void SubscribeObjectTest::testOCSubscribeWhenAlreadySubscribed()
{
	//////////////////////////////////////////////
	// subscribe with just "aa" and validate it //
	//////////////////////////////////////////////
	listener->quickSubscribe( "ObjectRoot.A.B", 1, "aa" );
	// validate it, but just for aa (validateSubscribed() expected aa and ba)
	RTI::ObjectHandle theObject = defaultFederate->quickRegister( bHandle );
	defaultFederate->quickReflect( theObject, 2, "aa", "ba" );
	TestNG6Object *ng6Object = listener->fedamb->waitForROUpdate( theObject );
	int result = strcmp( "aa", ng6Object->getAttribute(aaHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	CPPUNIT_ASSERT( ng6Object->getAttribute(baHandle) == NULL );

	////////////////////////////////////////////////////////////
	// resubscribe, this time with "aa" and "ba" and validate //
	////////////////////////////////////////////////////////////
	listener->quickSubscribe( "ObjectRoot.A.B", 2, "aa", "ba" );
	defaultFederate->quickReflect( theObject, 2, "aa", "ba" );
	listener->fedamb->waitForROUpdate( theObject );
	result = strcmp( "aa", ng6Object->getAttribute(aaHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "ba", ng6Object->getAttribute(baHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
}

///////////////////////////////////////////////////////
// TEST: (valid) testOCSubscribeWithEmptyHandleSet() //
///////////////////////////////////////////////////////
void SubscribeObjectTest::testOCSubscribeWithEmptyHandleSet()
{
	// subscribe and validate that we are subscribed
	listener->quickSubscribe( "ObjectRoot.A.B", 2, "aa", "ba" );
	validateSubscribed();
	
	// subscribe with an empty handle set, this should be an IMPLICIT UNSUBSCRIBE
	listener->quickSubscribe( "ObjectRoot.A.B", 0 );
	validateNotSubscribed();
}

///////////////////////////////////////////////////
// TEST: testOCSubscribeWithInvalidClassHandle() //
///////////////////////////////////////////////////
void SubscribeObjectTest::testOCSubscribeWithInvalidClassHandle()
{
	try
	{
		listener->rtiamb->subscribeObjectClassAttributes( 100000, *theSet );
		failTestMissingException( "ObjectClassNotDefined", "subscribing w/ invalid class handle" );
	}
	catch( RTI::ObjectClassNotDefined& ocnd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectClassNotDefined", e, "subscribing w/ invalid class handle" );
	}
}

///////////////////////////////////////////////////////
// TEST: testOCSubscribeWithInvalidAttributeHandle() //
///////////////////////////////////////////////////////
void SubscribeObjectTest::testOCSubscribeWithInvalidAttributeHandle()
{
	RTI::AttributeHandleSet *handleSet = listener->populatedAHS( 3, aaHandle, baHandle, 11111111 );
	
	try
	{
		listener->rtiamb->subscribeObjectClassAttributes( bHandle, *handleSet );
		delete handleSet;
		failTestMissingException( "AttributeNotDefined", "subscribing w/ invalid attribute" );
	}
	catch( RTI::AttributeNotDefined& attnd )
	{
		// success!
		delete handleSet;
	}
	catch( RTI::Exception& e )
	{
		delete handleSet;
		failTestWrongException( "AttributeNotDefined", e, "subscribing w/ invalid attribute" );
	}
}

//////////////////////////////////////////
// TEST: testOCSubscribeWhenNotJoined() //
//////////////////////////////////////////
void SubscribeObjectTest::testOCSubscribeWhenNotJoined()
{
	listener->quickResign();

	try
	{
		listener->rtiamb->subscribeObjectClassAttributes( bHandle, *theSet );
		failTestMissingException( "FederateNotExecutionMember", "subscribing when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e, "subscribing when not joined" );
	}
}

////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////// Unsubscribe Test Methods /////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

void SubscribeObjectTest::validateNotSubscribed()
{
	// register an instance of the provided object class
	RTI::ObjectHandle theObject = defaultFederate->quickRegister( bHandle );
	
	// wait for a timeout on the discovery
	listener->fedamb->waitForDiscoveryTimeout( theObject );
}

///////////////////////////////////////
// TEST: (valid) testOCUnsubscribe() //
///////////////////////////////////////
void SubscribeObjectTest::testOCUnsubscribe()
{
	listener->quickSubscribe( "ObjectRoot.A.B", 2, "aa", "ba" );
	validateSubscribed();
	
	try
	{
		listener->rtiamb->unsubscribeObjectClass( bHandle );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception unsubscribing from object class: %s", e._reason );
	}
	
	validateNotSubscribed();
}

/////////////////////////////////////////////////////
// TEST: testOCUnsubscribeWithInvalidClassHandle() //
/////////////////////////////////////////////////////
void SubscribeObjectTest::testOCUnsubscribeWithInvalidClassHandle()
{
	try
	{
		listener->rtiamb->unsubscribeObjectClass( 100000 );
		failTestMissingException( "ObjectClassNotDefined","unsubscribing w/ invalid class handle" );
	}
	catch( RTI::ObjectClassNotDefined& ocnd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectClassNotDefined", e,
		                        "unsubscribing w/ invalid class handle" );
	}
}

///////////////////////////////////////////////
// TEST: testOCUnsubscribeWhenNotPublished() //
///////////////////////////////////////////////
void SubscribeObjectTest::testOCUnsubscribeWhenNotPublished()
{
	try
	{
		listener->rtiamb->unsubscribeObjectClass( bHandle );
		failTestMissingException( "ObjectClassNotSubscribed", "unsubscribing when not subscribed" );
	}
	catch( RTI::ObjectClassNotSubscribed& ocns )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectClassNotSubscribed",e,"unsubscribing when not subscribed" );
	}
}

////////////////////////////////////////////
// TEST: testOCUnsubscribeWhenNotJoined() //
////////////////////////////////////////////
void SubscribeObjectTest::testOCUnsubscribeWhenNotJoined()
{
	listener->quickResign();

	try
	{
		listener->rtiamb->unsubscribeObjectClass( bHandle );
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
