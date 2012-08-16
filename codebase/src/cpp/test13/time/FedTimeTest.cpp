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
#include "FedTimeTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( FedTimeTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( FedTimeTest, "FedTimeTest" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
FedTimeTest::FedTimeTest()
{
	this->defaultFederate = new TestNG6Federate( "defaultFederate" );
}

FedTimeTest::~FedTimeTest()
{
	delete this->defaultFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void FedTimeTest::setUp()
{
    this->defaultFederate->quickCreate();
    this->defaultFederate->quickJoin();
}

void FedTimeTest::tearDown()
{
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////
// TEST: (valid) testEquals() //
////////////////////////////////
void FedTimeTest::testEquals()
{
	RTIfedTime timeA = 5.0f;
	RTIfedTime timeB = 5.0f;
	RTIfedTime timeC = 10.0f;
	RTIfedTime timeD = 10.00000001f;
	RTIfedTime timeE = 10.0000009f;

	// == tests
	CPPUNIT_ASSERT_EQUAL( timeA == timeB, RTI::RTI_TRUE );
	CPPUNIT_ASSERT_EQUAL( timeA == timeA, RTI::RTI_TRUE );
	CPPUNIT_ASSERT_EQUAL( timeA == timeC, RTI::RTI_FALSE );
	CPPUNIT_ASSERT_EQUAL( timeC == timeD, RTI::RTI_TRUE );
	CPPUNIT_ASSERT_EQUAL( timeC == timeE, RTI::RTI_FALSE );
	CPPUNIT_ASSERT_EQUAL( timeA == 5.0, RTI::RTI_TRUE );
	CPPUNIT_ASSERT_EQUAL( timeC == 5.0, RTI::RTI_FALSE );
	CPPUNIT_ASSERT_EQUAL( timeC == 10.00000001f, RTI::RTI_TRUE );
	CPPUNIT_ASSERT_EQUAL( timeC == 10.0000009f, RTI::RTI_FALSE );
}

//////////////////////////////////////
// TEST: (valid) testDoesNotEqual() //
//////////////////////////////////////
void FedTimeTest::testDoesNotEqual()
{
	RTIfedTime timeA = 5.0f;
	RTIfedTime timeB = 5.0f;
	RTIfedTime timeC = 10.0f;
	RTIfedTime timeD = 10.00000001f;
	RTIfedTime timeE = 10.0000009f;

	// != tests
	CPPUNIT_ASSERT_EQUAL( timeA != timeB, RTI::RTI_FALSE );
	CPPUNIT_ASSERT_EQUAL( timeA != timeA, RTI::RTI_FALSE );
	CPPUNIT_ASSERT_EQUAL( timeA != timeC, RTI::RTI_TRUE );
	CPPUNIT_ASSERT_EQUAL( timeC != timeD, RTI::RTI_FALSE );
	CPPUNIT_ASSERT_EQUAL( timeC != timeE, RTI::RTI_TRUE );
	CPPUNIT_ASSERT_EQUAL( timeA != 5.0f, RTI::RTI_FALSE );
	CPPUNIT_ASSERT_EQUAL( timeC != 5.0f, RTI::RTI_TRUE );
	CPPUNIT_ASSERT_EQUAL( timeC != 10.00000001f, RTI::RTI_FALSE );
	CPPUNIT_ASSERT_EQUAL( timeC != 10.0000009f, RTI::RTI_TRUE );
}

//////////////////////////////////
// TEST: (valid) testLessThan() //
//////////////////////////////////
void FedTimeTest::testLessThan()
{
	RTIfedTime timeA = 5.0f;
	RTIfedTime timeB = 5.0f;
	RTIfedTime timeC = 10.0f;

	// < tests
	CPPUNIT_ASSERT_EQUAL( timeA < timeB, RTI::RTI_FALSE );
	CPPUNIT_ASSERT_EQUAL( timeA < timeA, RTI::RTI_FALSE );
	CPPUNIT_ASSERT_EQUAL( timeA < timeC, RTI::RTI_TRUE );
	CPPUNIT_ASSERT_EQUAL( timeC < timeA, RTI::RTI_FALSE );
}

/////////////////////////////////////
// TEST: (valid) testGreaterThan() //
/////////////////////////////////////
void FedTimeTest::testGreaterThan()
{
	RTIfedTime timeA = 5.0f;
	RTIfedTime timeB = 5.0f;
	RTIfedTime timeC = 10.0f;

	// != tests
	CPPUNIT_ASSERT_EQUAL( timeA > timeB, RTI::RTI_FALSE );
	CPPUNIT_ASSERT_EQUAL( timeA > timeA, RTI::RTI_FALSE );
	CPPUNIT_ASSERT_EQUAL( timeA > timeC, RTI::RTI_FALSE );
	CPPUNIT_ASSERT_EQUAL( timeC > timeA, RTI::RTI_TRUE );
}

////////////////////////////////////
// TEST: (valid) testPlusEquals() //
////////////////////////////////////
void FedTimeTest::testPlusEquals()
{
	RTIfedTime timeA = 5.0f;
	RTIfedTime timeB = 10.0f;
	RTIfedTime timeC = 15.0f;

	// += tests
	timeA += timeB;
	CPPUNIT_ASSERT_EQUAL( timeA , timeC );

	timeB += 5.0f;
	CPPUNIT_ASSERT_EQUAL( timeB , timeC );
}

/////////////////////////////////////
// TEST: (valid) testMinusEquals() //
/////////////////////////////////////
void FedTimeTest::testMinusEquals()
{
	RTIfedTime timeA = 15.0f;
	RTIfedTime timeB = 10.0f;
	RTIfedTime timeC = 5.0f;

	// -= tests
	timeA -= timeB;
	CPPUNIT_ASSERT_EQUAL( timeA , timeC );

	timeB -= 5.0f;
	CPPUNIT_ASSERT_EQUAL( timeB , timeC );
}

////////////////////////////////////////
// TEST: (valid) testMultiplyEquals() //
////////////////////////////////////////
void FedTimeTest::testMultiplyEquals()
{
	RTIfedTime timeA = 5.0f;
	RTIfedTime timeB = 10.0f;
	RTIfedTime timeC = 50.0f;

	// *= tests
	timeA *= timeB;
	CPPUNIT_ASSERT_EQUAL( timeA , timeC );

	timeB *= 5.0f;
	CPPUNIT_ASSERT_EQUAL( timeB , timeC );
}

//////////////////////////////////////
// TEST: (valid) testDivideEquals() //
//////////////////////////////////////
void FedTimeTest::testDivideEquals()
{
	RTIfedTime timeA = 50.0f;
	RTIfedTime timeB = 10.0f;
	RTIfedTime timeC = 5.0f;

	// /= tests
	timeA /= timeB;
	CPPUNIT_ASSERT_EQUAL( timeA , timeC );

	timeB /= 2.0f;
	CPPUNIT_ASSERT_EQUAL( timeB , timeC );
}

// Commenting these out, these functions aren't defined as FED_TIME_EXPORT in fedtime.hh, and
// therefore don't seem to be publicly accessable
/*
//////////////////////
// TEST: testPlus() //
//////////////////////
void FedTimeTest::testPlus()
{
	RTIfedTime timeA = 5.0f;
	RTIfedTime timeB = 10.0f;
	RTIfedTime timeC = 15.0f;

	CPPUNIT_ASSERT_EQUAL ( timeA + timeB, timeC );
	CPPUNIT_ASSERT_EQUAL ( timeA + 10.0f, timeC );
	CPPUNIT_ASSERT_EQUAL ( 5.0f + timeB, timeC );
}

///////////////////////
// TEST: testMinus() //
///////////////////////
void FedTimeTest::testMinus()
{
	RTIfedTime timeA = 15.0f;
	RTIfedTime timeB = 10.0f;
	RTIfedTime timeC = 5.0f;

	CPPUNIT_ASSERT_EQUAL ( timeA - timeB, timeC );
	CPPUNIT_ASSERT_EQUAL ( timeA - 10.0f, timeC );
	CPPUNIT_ASSERT_EQUAL ( 15.0f - timeB, timeC );
}

//////////////////////////
// TEST: testMultiply() //
//////////////////////////
void FedTimeTest::testMultiply()
{
	RTIfedTime timeA = 5.0f;
	RTIfedTime timeB = 10.0f;
	RTIfedTime timeC = 50.0f;

	CPPUNIT_ASSERT_EQUAL ( timeA * timeB, timeC );
	CPPUNIT_ASSERT_EQUAL ( timeA * 10.0f, timeC );
	CPPUNIT_ASSERT_EQUAL ( 5.0f * timeB, timeC );
}

////////////////////////
// TEST: testDivide() //
////////////////////////
void FedTimeTest::testDivide()
{
	RTIfedTime timeA = 50.0f;
	RTIfedTime timeB = 10.0f;
	RTIfedTime timeC = 5.0f;

	CPPUNIT_ASSERT_EQUAL ( timeA / timeB, timeC );
	CPPUNIT_ASSERT_EQUAL ( timeA / 10.0f, timeC );
	CPPUNIT_ASSERT_EQUAL ( 50.0f / timeB, timeC );
}
*/
