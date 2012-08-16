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
#include "SubscribeInteractionTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( SubscribeInteractionTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SubscribeInteractionTest, "SubscribeInteractionTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SubscribeInteractionTest, "subscribeInteraction" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SubscribeInteractionTest, "declarationManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
SubscribeInteractionTest::SubscribeInteractionTest()
{
	this->defaultFederate = new TestNG6Federate( "defaultFederate" );
	this->listenerFederate = new TestNG6Federate( "listenerFederate" );
}

SubscribeInteractionTest::~SubscribeInteractionTest()
{
	delete this->listenerFederate;
	delete this->defaultFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void SubscribeInteractionTest::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->listenerFederate->quickJoin();
	
	// get the class handles
	this->xHandle = defaultFederate->quickICHandle( "InteractionRoot.X" );
	this->yHandle = defaultFederate->quickICHandle( "InteractionRoot.X.Y" );
	this->xaHandle = defaultFederate->quickPCHandle( "InteractionRoot.X", "xa" );
	this->xbHandle = defaultFederate->quickPCHandle( "InteractionRoot.X", "xb" );
	this->xcHandle = defaultFederate->quickPCHandle( "InteractionRoot.X", "xc" );
	this->yaHandle = defaultFederate->quickPCHandle( "InteractionRoot.X.Y", "ya" );
	this->ybHandle = defaultFederate->quickPCHandle( "InteractionRoot.X.Y", "yb" );
	this->ycHandle = defaultFederate->quickPCHandle( "InteractionRoot.X.Y", "yc" );
	
	// do pub/sub
	this->defaultFederate->quickPublish( "InteractionRoot.X.Y" );
}

void SubscribeInteractionTest::tearDown()
{
	this->listenerFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Subscribe Test Methods //////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

void SubscribeInteractionTest::validateSubscribed()
{
	// send an interaction from the default federate
	defaultFederate->quickSend( yHandle, 6, "xa", "xb", "xc", "ya", "yb", "yc" );
	
	// wait for it to be received in the listener
	TestNG6Interaction *received = listenerFederate->fedamb->waitForROInteraction( yHandle );

	// validate the parameters for the interaction
	int result = strcmp( "xa", received->getParameter(xaHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "xb", received->getParameter(xbHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "xc", received->getParameter(xcHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "ya", received->getParameter(yaHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "yb", received->getParameter(ybHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "yc", received->getParameter(ycHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	
	// clean up
	delete received;
}

//////////////////////////////////////////
// TEST: (valid) testCreateFederation() //
//////////////////////////////////////////
void SubscribeInteractionTest::testICSubscribe()
{
	try
	{
		listenerFederate->rtiamb->subscribeInteractionClass( yHandle );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while subscribing to interaction: %s", e._reason );
	}
	
	validateSubscribed();
}

//////////////////////////////////////////////
// TEST: testICSubscribeWithInvalidHandle() //
//////////////////////////////////////////////
void SubscribeInteractionTest::testICSubscribeWithInvalidHandle()
{
	try
	{
		listenerFederate->rtiamb->subscribeInteractionClass( 10000000 );
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
void SubscribeInteractionTest::testICSubscribeWhenNotJoined()
{
	listenerFederate->quickResign();

	try
	{
		listenerFederate->rtiamb->subscribeInteractionClass( yHandle );
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

void SubscribeInteractionTest::validateNotSubscribed()
{
	// send an interaction from the default federate
	defaultFederate->quickSend( yHandle, 6, "xa", "xb", "xc", "ya", "yb", "yc" );
	
	// make sure the received doesn't get anything
	listenerFederate->fedamb->waitForROInteractionTimeout( yHandle );
}

///////////////////////////////////////
// TEST: (valid) testICUnsubscribe() //
///////////////////////////////////////
void SubscribeInteractionTest::testICUnsubscribe()
{
	listenerFederate->quickSubscribe( "InteractionRoot.X.Y" );
	validateSubscribed();
	
	try
	{
		listenerFederate->rtiamb->unsubscribeInteractionClass( yHandle );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception unsubscribing from interaction class: %s", e._reason );
	}
	
	validateNotSubscribed();
}

////////////////////////////////////////////////
// TEST: testICUnsubscribeWithInvalidHandle() //
////////////////////////////////////////////////
void SubscribeInteractionTest::testICUnsubscribeWithInvalidHandle()
{
	try
	{
		listenerFederate->rtiamb->unsubscribeInteractionClass( 10000000 );
		failTestMissingException( "InteractionClassNotDefined", 
		                          "unsubscribing from interaction class with invalid handle" );
	}
	catch( RTI::InteractionClassNotDefined& icnd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "InteractionClassNotDefined", e,
		                        "unsubscribing from interaction class with invalid handle" );
	}
}

////////////////////////////////////////////////
// TEST: testICUnsubscribeWhenNotSubscribed() //
////////////////////////////////////////////////
void SubscribeInteractionTest::testICUnsubscribeWhenNotSubscribed()
{
	try
	{
		listenerFederate->rtiamb->unsubscribeInteractionClass( xHandle );
		failTestMissingException( "InteractionClassNotSubscribed", 
		                          "unsubscribing from interaction class we don't subscribe to" );
	}
	catch( RTI::InteractionClassNotSubscribed& icns )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "InteractionClassNotSubscribed", e,
		                        "unsubscribing from interaction class we don't subscribe to" );
	}
}

////////////////////////////////////////////
// TEST: testICUnsubscribeWhenNotJoined() //
////////////////////////////////////////////
void SubscribeInteractionTest::testICUnsubscribeWhenNotJoined()
{
	listenerFederate->quickResign();

	try
	{
		listenerFederate->rtiamb->unsubscribeInteractionClass( yHandle );
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
