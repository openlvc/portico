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
#include "FlushQueueRequestTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( FlushQueueRequestTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( FlushQueueRequestTest, "FlushQueueRequestTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( FlushQueueRequestTest, "flushQueueRequest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( FlushQueueRequestTest, "timeManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
FlushQueueRequestTest::FlushQueueRequestTest()
{
	this->defaultFederate = new Test13Federate( "defaultFederate" );
	this->secondFederate = new Test13Federate( "secondFederate" );
}

FlushQueueRequestTest::~FlushQueueRequestTest()
{
	delete this->defaultFederate;
	delete this->secondFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void FlushQueueRequestTest::setUp()
{
    this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->secondFederate->quickJoin();

	this->xHandle = defaultFederate->quickICHandle( "InteractionRoot.X" );
	
	// publication and subscription
	defaultFederate->quickPublish( "InteractionRoot.X" );
	secondFederate->quickSubscribe( "InteractionRoot.X" );

	// time management
	defaultFederate->quickEnableAsync();
	secondFederate->quickEnableAsync();
	defaultFederate->quickEnableRegulating( 5.0 );
	secondFederate->quickEnableConstrained();

	// advance time a little
	defaultFederate->quickAdvanceAndWait( 10.0 );
	secondFederate->quickAdvanceAndWait( 10.0 );
}

void FlushQueueRequestTest::tearDown()
{
	this->secondFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

/////////////////////////////
// TEST: (valid) testFqr() //
/////////////////////////////
void FlushQueueRequestTest::testFqr()
{
	// send some interactions
	defaultFederate->quickSend( xHandle, 100.0, 0 );
	
	// issue the request in the second federate
	// normally this advance would be constrained by the regulating defaultFederate, but
	// with a flush queue request, the current RO/TSO messages will be delivered regardless
	// of the federation-wide LBTS and an advance will be granted to the LESSER or the
	// requested time -OR- the timestamp of the highest TSO message
	secondFederate->quickFlushQueueRequest( 200.0 );
	Test13Interaction *received = secondFederate->fedamb->waitForTSOInteraction( xHandle );
	double timestamp = received->getTime();
	CPPUNIT_ASSERT_EQUAL( 100.0, timestamp );

	secondFederate->fedamb->waitForTimeAdvance( 200.0 );
	
	// send an event in the past by the regulating federate
	defaultFederate->quickSend( xHandle, 50.0, 0 );
	secondFederate->fedamb->waitForTSOInteractionTimeout( xHandle );
}

///////////////////////////////////
// TEST: testFqrWithTimeInPast() //
///////////////////////////////////
void FlushQueueRequestTest::testFqrWithTimeInPast()
{
	try
	{
		RTIfedTime theTime = 5.0;
		defaultFederate->rtiamb->flushQueueRequest( theTime );
		failTestMissingException( "FederationTimeAlreadyPassed",
		                          "flushQueueRequest with time in past" );
	}
	catch( RTI::FederationTimeAlreadyPassed& ftap )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederationTimeAlreadyPassed", e, 
		                        "flushQueueRequest with time in past" );
	}
}

//////////////////////////////////////////
// TEST: testFqrWhenAdvanceInProgress() //
//////////////////////////////////////////
void FlushQueueRequestTest::testFqrWhenAdvanceInProgress()
{
	// request an advance to get us into the right state
	defaultFederate->quickAdvanceRequest( 100.0 );
	
	// issue another time advance request
	try
	{
		RTIfedTime theTime = 100.0;
		defaultFederate->rtiamb->flushQueueRequest( theTime );
		failTestMissingException( "TimeAdvanceAlreadyInProgress",
		                          "flushQueueRequest while advance in progress" );
	}
	catch( RTI::TimeAdvanceAlreadyInProgress& taaip )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "TimeAdvanceAlreadyInProgress", e, 
		                        "flushQueueRequest while advance in progress" );
	}
}

///////////////////////////////////////////
// TEST: testFqrWhenConstrainedPending() //
///////////////////////////////////////////
void FlushQueueRequestTest::testFqrWhenConstrainedPending()
{
	// request an advance to get us into the right state
	defaultFederate->quickEnabledConstrainedRequest();
	
	// issue another time advance request
	try
	{
		RTIfedTime theTime = 100.0;
		defaultFederate->rtiamb->flushQueueRequest( theTime );
		failTestMissingException( "EnableTimeConstrainedPending",
		                          "flushQueueRequest while constrained request is pending" );
	}
	catch( RTI::EnableTimeConstrainedPending& etcp )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "EnableTimeConstrainedPending", e, 
		                        "flushQueueRequest while constrained request is pending" );
	}
}

//////////////////////////////////////////
// TEST: testFqrWhenRegulatingPending() //
//////////////////////////////////////////
void FlushQueueRequestTest::testFqrWhenRegulatingPending()
{
	// request an advance to get us into the right state
	secondFederate->quickEnableRegulatingRequest( 5.0 );
	
	// issue another time advance request
	try
	{
		RTIfedTime theTime = 100.0;
		secondFederate->rtiamb->flushQueueRequest( theTime );
		failTestMissingException( "EnableTimeRegulationPending",
		                          "flushQueueRequest while regulation request is pending" );
	}
	catch( RTI::EnableTimeRegulationPending& etrp )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "EnableTimeRegulationPending", e, 
		                        "flushQueueRequest while regulation request is pending" );
	}
}

//////////////////////////////////
// TEST: testFqrWhenNotJoined() //
//////////////////////////////////
void FlushQueueRequestTest::testFqrWhenNotJoined()
{
	// resign the default federate
	defaultFederate->quickResign();
	
	try
	{
		RTIfedTime theTime = 100.0;
		defaultFederate->rtiamb->flushQueueRequest( theTime );
		failTestMissingException( "EnableTimeRegulationPending",
		                          "requesting advance while regulation request is pending" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e, 
		                        "flushQueueRequest when not joined" );
	}
}
