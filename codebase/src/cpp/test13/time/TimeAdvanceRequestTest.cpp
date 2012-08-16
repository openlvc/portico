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
#include "TimeAdvanceRequestTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( TimeAdvanceRequestTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( TimeAdvanceRequestTest, "TimeAdvanceRequestTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( TimeAdvanceRequestTest, "timeAdvanceRequest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( TimeAdvanceRequestTest, "timeManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
TimeAdvanceRequestTest::TimeAdvanceRequestTest()
{
	this->defaultFederate = new TestNG6Federate( "defaultFederate" );
}

TimeAdvanceRequestTest::~TimeAdvanceRequestTest()
{
	delete this->defaultFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void TimeAdvanceRequestTest::setUp()
{
    this->defaultFederate->quickCreate();
    this->defaultFederate->quickJoin();
}

void TimeAdvanceRequestTest::tearDown()
{
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

/////////////////////////////
// TEST: (valid) testTar() //
/////////////////////////////
/*
 * This test will just ensure that a federate can get a time advance when it requests it. As the
 * main goal of the C++ interface unit testing is to test that things can get back and forth over
 * the JNI boundary without any major problems, this is sufficient.
 */
void TimeAdvanceRequestTest::testTar()
{
	try
	{
		// request the time advance
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->timeAdvanceRequest( theTime );
		
		// wait for the advance to be granted
		defaultFederate->fedamb->waitForTimeAdvance( 10.0 );
	}
	catch ( RTI::Exception& e )
	{
		failTest( "Unexpected exception while requesting valid time advance grant: %s", e._reason );
	}
}

///////////////////////////////////////////
// TEST: (valid) testTarWhenInProgress() //
///////////////////////////////////////////
void TimeAdvanceRequestTest::testTarWhenInProgress()
{
	// request an advance to get us into the right state
	defaultFederate->quickAdvanceRequest( 10.0 );
	
	// issue another time advance request
	try
	{
		RTIfedTime theTime = 11.0;
		defaultFederate->rtiamb->timeAdvanceRequest( theTime );
		failTestMissingException( "TimeAdvanceAlreadyInProgress",
		                          "requesting advance while one is already outstanding" );
	}
	catch( RTI::TimeAdvanceAlreadyInProgress& taaip )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "TimeAdvanceAlreadyInProgress", e, 
		                        "requesting advance while one is already outstanding" );
	}
}

///////////////////////////////////////////////////
// TEST: (valid) testTarWhenConstrainedPending() //
///////////////////////////////////////////////////
void TimeAdvanceRequestTest::testTarWhenConstrainedPending()
{
	// request an advance to get us into the right state
	defaultFederate->quickEnabledConstrainedRequest();
	
	// issue another time advance request
	try
	{
		RTIfedTime theTime = 11.0;
		defaultFederate->rtiamb->timeAdvanceRequest( theTime );
		failTestMissingException( "EnableTimeConstrainedPending",
		                          "requesting advance while constrained request is pending" );
	}
	catch( RTI::EnableTimeConstrainedPending& etcp )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "EnableTimeConstrainedPending", e, 
		                        "requesting advance while constrained request is pending" );
	}
}

//////////////////////////////////////////////////
// TEST: (valid) testTarWhenRegulatingPending() //
//////////////////////////////////////////////////
void TimeAdvanceRequestTest::testTarWhenRegulatingPending()
{
	// request an advance to get us into the right state
	defaultFederate->quickEnableRegulatingRequest( 5.0 );
	
	// issue another time advance request
	try
	{
		RTIfedTime theTime = 11.0;
		defaultFederate->rtiamb->timeAdvanceRequest( theTime );
		failTestMissingException( "EnableTimeRegulationPending",
		                          "requesting advance while regulation request is pending" );
	}
	catch( RTI::EnableTimeRegulationPending& etrp )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "EnableTimeRegulationPending", e, 
		                        "requesting advance while regulation request is pending" );
	}
}

