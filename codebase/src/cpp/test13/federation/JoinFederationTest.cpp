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
#include "JoinFederationTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( JoinFederationTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( JoinFederationTest, "JoinFederationTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( JoinFederationTest, "join" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( JoinFederationTest, "federationManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
JoinFederationTest::JoinFederationTest()
{
}

JoinFederationTest::~JoinFederationTest()
{
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void JoinFederationTest::setUp()
{
    this->defaultFederate = new Test13Federate( "defaultFederate" );
    this->defaultFederate->quickCreate();
}

void JoinFederationTest::tearDown()
{
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
	delete this->defaultFederate;
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////
// TEST: (valid) testJoinFederation() //
////////////////////////////////////////
void JoinFederationTest::testJoinFederation()
{
	try
	{
		// create the FederateAmbassador
		defaultFederate->fedamb = new TestNG6FederateAmbassador( defaultFederate );

		// join the federation
		defaultFederate->rtiamb->joinFederationExecution( defaultFederate->getFederateName(),
		                                                  defaultFederate->SIMPLE_NAME,
		                                                  defaultFederate->fedamb );
	}
	catch( RTI::Exception &e )
	{
		// failure!
		failTest( "Exception joining valid federation: %s", e._reason );
	}
}

////////////////////////////////////////////////
// TEST: testJoinFederationThatDoesNotExist() //
////////////////////////////////////////////////
void JoinFederationTest::testJoinFederationThatDoesNotExist()
{
	try
	{
		// create the FederateAmbassador
		defaultFederate->fedamb = new TestNG6FederateAmbassador( defaultFederate );

		// try and join the federation
		defaultFederate->rtiamb->joinFederationExecution( defaultFederate->getFederateName(),
		                                                  "NoSuchFederation",
		                                                  defaultFederate->fedamb );
		failTestMissingException( "FederationExecutionDoesNotExist",
		                          "joining federation that does not exist" );
	}
	catch( RTI::FederationExecutionDoesNotExist &fedne )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "FederationExecutionDoesNotExist", e,
		                        "joining federation that does not exist" );
	}
}

//////////////////////////////////////////////////////
// TEST: testJoinFederationWithNullFederationName() //
//////////////////////////////////////////////////////
void JoinFederationTest::testJoinFederationWithNullFederationName()
{
	try
	{
		// create the FederateAmbassador
		defaultFederate->fedamb = new TestNG6FederateAmbassador( defaultFederate );

		// try and join the federation
		defaultFederate->rtiamb->joinFederationExecution( defaultFederate->getFederateName(),
		                                                  NULL,
		                                                  defaultFederate->fedamb );
		failTestMissingException( "FederationExecutionDoesNotExist",
		                          "joining federation with NULL execution name" );
	}
	catch( RTI::FederationExecutionDoesNotExist &fedne )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "FederationExecutionDoesNotExist", e,
		                        "joining federation with NULL execution name" );
	}
}

////////////////////////////////////////////////////
// TEST: testJoinFederationWithNullFederateName() //
////////////////////////////////////////////////////
void JoinFederationTest::testJoinFederationWithNullFederateName()
{
	try
	{
		defaultFederate->rtiamb->joinFederationExecution( NULL,
		                                                  defaultFederate->SIMPLE_NAME,
		                                                  defaultFederate->fedamb );
		failTestMissingException( "RTIinternalError","joining federation with NULL federate name" );
	}
	catch( RTI::RTIinternalError &rtie )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "RTIinternalError", e,
		                        "joining federation with NULL federate name" );
	}
}

/////////////////////////////////////////////////////
// TEST: testJoinFederationWithEmptyFederateName() //
/////////////////////////////////////////////////////
void JoinFederationTest::testJoinFederationWithEmptyFederateName()
{
	try
	{
		defaultFederate->rtiamb->joinFederationExecution( "",
		                                                  defaultFederate->SIMPLE_NAME,
		                                                  defaultFederate->fedamb );
		failTestMissingException( "RTIinternalError",
		                          "joining federation with empty federate name" );
	}
	catch( RTI::RTIinternalError &rtie )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "RTIinternalError", e,
		                        "joining federation with empty federate name" );
	}
}

//////////////////////////////////////////////////////////
// TEST: testJoinFederationWithNullFederateAmbassador() //
//////////////////////////////////////////////////////////
void JoinFederationTest::testJoinFederationWithNullFederateAmbassador()
{
	try
	{
		defaultFederate->rtiamb->joinFederationExecution( defaultFederate->getFederateName(),
		                                                  defaultFederate->SIMPLE_NAME,
		                                                  NULL );
		failTestMissingException( "RTIinternalError", "joining federation with NULL fedamb" );
	}
	catch( RTI::RTIinternalError &rtie )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "RTIinternalError", e, "joining federation with NULL fedamb" );
	}
}

/////////////////////////////////////////////////
// TEST: testJoinFederationWhenAlreadyJoined() //
/////////////////////////////////////////////////
void JoinFederationTest::testJoinFederationWhenAlreadyJoined()
{
	try
	{
		defaultFederate->quickJoin();
		defaultFederate->rtiamb->joinFederationExecution( defaultFederate->getFederateName(),
		                                                  defaultFederate->SIMPLE_NAME,
		                                                  defaultFederate->fedamb );
		failTestMissingException( "FederateAlreadyExecutionMember",
		                          "joining federation when already joined" );
	}
	catch( RTI::RTIinternalError &rtie )
	{
		// success!
	}
	catch( RTI::FederateAlreadyExecutionMember &faem )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "FederateAlreadyExecutionMember", e,
		                        "joining federation when already joined" );
	}
}
