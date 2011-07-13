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
#include "DestroyFederationTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( DestroyFederationTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( DestroyFederationTest, "DestroyFederationTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( DestroyFederationTest, "destroy" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( DestroyFederationTest, "federationManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
DestroyFederationTest::DestroyFederationTest()
{
}

DestroyFederationTest::~DestroyFederationTest()
{
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void DestroyFederationTest::setUp()
{
    this->defaultFederate = new TestNG6Federate( "defaultFederate" );
    this->defaultFederate->quickCreate();
}

void DestroyFederationTest::tearDown()
{
	this->defaultFederate->quickDestroyNoFail();
	delete this->defaultFederate;
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////
// TEST: (valid) testDestroyFederation() //
///////////////////////////////////////////
void DestroyFederationTest::testDestroyFederation()
{
	// try and destroy the federation
	try
	{
		defaultFederate->rtiamb->destroyFederationExecution( TestNG6Federate::SIMPLE_NAME );
	}
	catch( RTI::Exception &e )
	{
		// failure
		failTest( "Unexpected exception while destroying federation: (%s) %s", e._name, e._reason );
	}
}

////////////////////////////////////////////////////
// TEST: testDestroyFederationWithJoinedMembers() //
////////////////////////////////////////////////////
void DestroyFederationTest::testDestroyFederationWithJoinedMembers()
{
	// join the federation so that there are still federates joined when we
	// try to destroy it
	defaultFederate->quickJoin();
	
	// try and destroy the federation
	try
	{
		defaultFederate->rtiamb->destroyFederationExecution( TestNG6Federate::SIMPLE_NAME );
		defaultFederate->quickResign();
		failTestMissingException( "FederatesCurrentlyJoined",
		                          "destroying fderation with joined members" );
	}
	catch( RTI::FederatesCurrentlyJoined &fcj )
	{
		// success!
		defaultFederate->quickResign();
	}
	catch( RTI::Exception &e )
	{
		// failure
		defaultFederate->quickResign();
		failTestWrongException( "FederatesCurrentlyJoined", e,
		                        "destroying federation with joined members" );
	}
}

///////////////////////////////////////////////////
// TEST: testDestroyFederationThatDoesNotExist() //
///////////////////////////////////////////////////
void DestroyFederationTest::testDestroyFederationThatDoesNotExist()
{
	// try and destroy the federation
	try
	{
		defaultFederate->rtiamb->destroyFederationExecution( "NoSuchFederation" );
		failTestMissingException( "FederationExecutionDoesNotExist",
		                          "destroying federation that doesn't exist" );
	}
	catch( RTI::FederationExecutionDoesNotExist &fedne )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		// failure
		failTestWrongException( "FederationExecutionDoesNotExist", e,
		                        "destroying federation that doesn't exist" );
	}
}

///////////////////////////////////////////////
// TEST: testDestroyFederationWithNullName() //
///////////////////////////////////////////////
void DestroyFederationTest::testDestroyFederationWithNullName()
{
	// try and destroy the federation
	try
	{
		defaultFederate->rtiamb->destroyFederationExecution( NULL );
		failTestMissingException( "FederationExecutionDoesNotExist",
		                          "destroying federation with null name" );
	}
	catch( RTI::FederationExecutionDoesNotExist &fedne )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		// failure
		failTestWrongException( "FederationExecutionDoesNotExist", e,
		                        "destroying federation with null name" );
	}
}

