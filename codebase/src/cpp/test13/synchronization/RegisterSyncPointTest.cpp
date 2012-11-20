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
#include "RegisterSyncPointTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( RegisterSyncPointTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( RegisterSyncPointTest, "RegisterSyncPointTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( RegisterSyncPointTest, "registerSyncPoint" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( RegisterSyncPointTest, "synchronization" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( RegisterSyncPointTest, "federationManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
RegisterSyncPointTest::RegisterSyncPointTest()
{
	this->federateOne = new Test13Federate( "federateOne" );
	this->federateTwo = new Test13Federate( "federateTwo" );
	this->label = new char[32];
	sprintf( this->label, "SynchronizationPoint" );
	this->tag = new char[8];
	sprintf( this->tag, "letag" );
}

RegisterSyncPointTest::~RegisterSyncPointTest()
{
	delete this->federateOne;
	delete this->federateTwo;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void RegisterSyncPointTest::setUp()
{
    this->federateOne->quickCreate();
    this->federateOne->quickJoin();
    this->federateTwo->quickJoin();
}

void RegisterSyncPointTest::tearDown()
{
	this->federateTwo->quickResign();
	this->federateOne->quickResign();
	this->federateOne->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////
// TEST: (valid) testRegisterSyncPoint() //
///////////////////////////////////////////
void RegisterSyncPointTest::testRegisterSyncPoint()
{
	try
	{
		federateOne->rtiamb->registerFederationSynchronizationPoint( label, tag );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while registering synchronization point: %s", e._reason );
	}
	
	// make sure the registration is a success
	federateOne->fedamb->waitForSyncRegResult( label, RTI::RTI_TRUE );
	federateTwo->fedamb->waitForSyncAnnounce( label );
}

//////////////////////////////////////////////////
// TEST: (valid) testRegisterSyncPointWithFHS() //
//////////////////////////////////////////////////
void RegisterSyncPointTest::testRegisterSyncPointWithFHS()
{
	// create the FHS that defines the federates that are subject to the sync point
	RTI::FederateHandleSet *fhs = federateOne->populatedFHS( 1, federateOne->getFederateHandle() );
	
	try
	{
		federateOne->rtiamb->registerFederationSynchronizationPoint( label, tag, *fhs );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while registering synchronization point with FHS: %s",
		          e._reason );
	}
	
	// make sure the point is a success
	federateOne->fedamb->waitForSyncRegResult( label, RTI::RTI_TRUE );
	federateTwo->fedamb->waitForSyncAnnounceTimeout( label );
}

//////////////////////////////////////////////////////
// TEST: (valid) testRegisterSyncPointWithNullTag() //
//////////////////////////////////////////////////////
void RegisterSyncPointTest::testRegisterSyncPointWithNullTag()
{
	try
	{
		federateOne->rtiamb->registerFederationSynchronizationPoint( label, NULL );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while registering synchronization point with null tag: %s",
		          e._reason );
	}
	
	// make sure the registration is a success
	federateOne->fedamb->waitForSyncRegResult( label, RTI::RTI_TRUE );
	char *incomingTag = federateTwo->fedamb->waitForSyncAnnounce( label );
	if( incomingTag != NULL )
		failTest( "Expected sync point announcement tag to be NULL, but was [%s]", incomingTag );
}

///////////////////////////////////////////////////////
// TEST: (valid) testRegisterSyncPointWithEmptyTag() //
///////////////////////////////////////////////////////
void RegisterSyncPointTest::testRegisterSyncPointWithEmptyTag()
{
	try
	{
		federateOne->rtiamb->registerFederationSynchronizationPoint( label, "" );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while registering sync point with whitespace tag: %s",
		          e._reason );
	}
	
	// make sure the registration is a success
	federateOne->fedamb->waitForSyncRegResult( label, RTI::RTI_TRUE );
	char *incomingTag = federateTwo->fedamb->waitForSyncAnnounce( label );
	if( incomingTag == NULL || strcmp("", incomingTag) != 0 )
		failTest( "Expected sync point announcement tag to be \"\", but was [%s]", incomingTag );
}

////////////////////////////////////////////////////////////
// TEST: (valid) testRegisterSyncPointWithWhitespaceTag() //
////////////////////////////////////////////////////////////
void RegisterSyncPointTest::testRegisterSyncPointWithWhitespaceTag()
{
	try
	{
		federateOne->rtiamb->registerFederationSynchronizationPoint( label, "   " );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while registering synchronization point with empty tag: %s",
		          e._reason );
	}
	
	// make sure the registration is a success
	federateOne->fedamb->waitForSyncRegResult( label, RTI::RTI_TRUE );
	char *incomingTag = federateTwo->fedamb->waitForSyncAnnounce( label );
	if( incomingTag == NULL || strcmp("   ", incomingTag) != 0 )
		failTest( "Expected sync point announcement tag to be \"   \", but was [%s]", incomingTag );
}

///////////////////////////////////////////////////////
// TEST: testRegisterSyncPointFromUnjoinedFederate() //
///////////////////////////////////////////////////////
void RegisterSyncPointTest::testRegisterSyncPointFromUnjoinedFederate()
{
	federateOne->quickResign();
	try
	{
		federateOne->rtiamb->registerFederationSynchronizationPoint( label, tag );
		failTestMissingException( "FederateNotExecutionMember",
		                          "registering sync point while not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "FederateNotExecutionMember", e, 
		                        "registering sync point while not joined" );
	}
}

////////////////////////////////////////////////
// TEST: testRegisterSyncPointWithNullLabel() //
////////////////////////////////////////////////
void RegisterSyncPointTest::testRegisterSyncPointWithNullLabel()
{
	try
	{
		federateOne->rtiamb->registerFederationSynchronizationPoint( NULL, tag );
		failTestMissingException( "RTIinternalError",
		                          "registering sync point with NULL label" );
	}
	catch( RTI::RTIinternalError& rtie )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "RTIinternalError", e, 
		                        "registering sync point with NULL label" );
	}
}

/////////////////////////////////////////////////
// TEST: testRegisterSyncPointWithEmptyLabel() //
/////////////////////////////////////////////////
void RegisterSyncPointTest::testRegisterSyncPointWithEmptyLabel()
{
	try
	{
		federateOne->rtiamb->registerFederationSynchronizationPoint( "", tag );
		failTestMissingException( "RTIinternalError",
		                          "registering sync point with empty label" );
	}
	catch( RTI::RTIinternalError& rtie )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "RTIinternalError", e, 
		                        "registering sync point with empty label" );
	}
}

//////////////////////////////////////////////////////
// TEST: testRegisterSyncPointWithWhitespaceLabel() //
//////////////////////////////////////////////////////
void RegisterSyncPointTest::testRegisterSyncPointWithWhitespaceLabel()
{
	try
	{
		federateOne->rtiamb->registerFederationSynchronizationPoint( "   ", tag );
		failTestMissingException( "RTIinternalError",
		                          "registering sync point with whitespace label" );
	}
	catch( RTI::RTIinternalError& rtie )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "RTIinternalError", e, 
		                        "registering sync point with whitespace label" );
	}
}

//////////////////////////////////////////////////
// TEST: stRegisterSyncPointWithExistingLabel() //
//////////////////////////////////////////////////
void RegisterSyncPointTest::testRegisterSyncPointWithExistingLabel()
{
	federateOne->quickAnnounce( label, tag );
	
	try
	{
		federateOne->rtiamb->registerFederationSynchronizationPoint( label, tag );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while registering sync point that already exists: %s",
		          e._reason );
	}
	
	// make sure the registration is a success
	federateOne->fedamb->waitForSyncRegResult( label, RTI::RTI_FALSE );
}
