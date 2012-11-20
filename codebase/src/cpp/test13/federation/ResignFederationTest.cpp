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
#include "ResignFederationTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( ResignFederationTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( ResignFederationTest, "ResignFederationTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( ResignFederationTest, "resign" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( ResignFederationTest, "federationManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
ResignFederationTest::ResignFederationTest()
{
}

ResignFederationTest::~ResignFederationTest()
{
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void ResignFederationTest::setUp()
{
    this->defaultFederate = new Test13Federate( "defaultFederate" );
    defaultFederate->quickCreate();
    defaultFederate->quickJoin();
}

void ResignFederationTest::tearDown()
{
	defaultFederate->quickResign();
	defaultFederate->quickDestroy();
	delete this->defaultFederate;
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////
// TEST: (valid) testResignWithNoAction() //
////////////////////////////////////////////
void ResignFederationTest::testResignWithNoAction()
{
	try
	{
		defaultFederate->rtiamb->resignFederationExecution( RTI::NO_ACTION );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Exception during valid resign with NO_ACTION: %s", e._reason );
	}
}

/////////////////////////////////////////////////
// TEST: (valid) testResignWithDeleteObjects() //
/////////////////////////////////////////////////
void ResignFederationTest::testResignWithDeleteObjects()
{
	try
	{
		defaultFederate->rtiamb->resignFederationExecution( RTI::DELETE_OBJECTS );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Exception during valid resign with DELETE_OBJECTS: %s", e._reason );
	}
}

/////////////////////////////////////////////////////
// TEST: (valid) testResignWithReleaseAttributes() //
/////////////////////////////////////////////////////
void ResignFederationTest::testResignWithReleaseAttributes()
{
	try
	{
		defaultFederate->rtiamb->resignFederationExecution( RTI::RELEASE_ATTRIBUTES );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Exception during valid resign with RELEASE_ATTRIBUTES: %s", e._reason );
	}

}

/////////////////////////////////////////////////////////////////////
// TEST: (valid) testResignWithDeleteObjectsAndReleaseAttributes() //
/////////////////////////////////////////////////////////////////////
void ResignFederationTest::testResignWithDeleteObjectsAndReleaseAttributes()
{
	try
	{
		defaultFederate->rtiamb->resignFederationExecution( RTI::DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Exception during valid resign with DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES: %s",
		          e._reason );
	}
}

///////////////////////////////////////////////////////////
// TEST: testResignWithNoActionWhileAttributesAreOwned() //
///////////////////////////////////////////////////////////
void ResignFederationTest::testResignWithNoActionWhileAttributesAreOwned()
{
	// publish an object class and register an object
	defaultFederate->quickPublish( "ObjectRoot.A", 1, "aa" );
	defaultFederate->quickRegister( "ObjectRoot.A" );

	try
	{
		defaultFederate->rtiamb->resignFederationExecution( RTI::NO_ACTION );
		failTestMissingException( "FederateOwnsAttributes",
		                          "resigning with NO_ACTION while owning attributes" );
	}
	catch( RTI::FederateOwnsAttributes &foa )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "FederateOwnsAttributes", e,
		                        "resigning with NO_ACTION while owning attributes" );
	}
}

/////////////////////////////////////////////////
// TEST: testResignFromFederationNotJoinedTo() //
/////////////////////////////////////////////////
void ResignFederationTest::testResignFromFederationNotJoinedTo()
{
	defaultFederate->quickResign();
	try
	{
		defaultFederate->rtiamb->resignFederationExecution( RTI::NO_ACTION );
		failTestMissingException( "FederateNotExecutionMember",
		                          "resigning from federation not joined to" );
	}
	catch( RTI::FederateNotExecutionMember &fnem )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "resigning from federation not joined to" );
	}
}
