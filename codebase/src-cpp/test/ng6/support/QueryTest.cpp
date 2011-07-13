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
#include "QueryTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( QueryTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( QueryTest, "QueryTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( QueryTest, "supportServices" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
QueryTest::QueryTest()
{
	this->defaultFederate = new TestNG6Federate( "defaultFederate" );
}

QueryTest::~QueryTest()
{
	delete this->defaultFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void QueryTest::setUp()
{
    this->defaultFederate->quickCreate();
    this->defaultFederate->quickJoin();
}

void QueryTest::tearDown()
{
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////
// TEST: (valid) testQueryFederateTime() //
///////////////////////////////////////////
void QueryTest::testQueryFederateTime()
{
	// check the time before we have any values //
	RTIfedTime theTime = 0.0;
	
	try
	{
		defaultFederate->rtiamb->queryFederateTime( theTime );
		CPPUNIT_ASSERT_DOUBLES_EQUAL( 0.0, theTime.getTime(), 0.000001 );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception querying federate time: %s", e._reason );
	}

	// advance time somewhat and check again //
	try
	{
		defaultFederate->quickAdvanceAndWait( 10.0 );
		defaultFederate->rtiamb->queryFederateTime( theTime );
		CPPUNIT_ASSERT_DOUBLES_EQUAL( 10.0, theTime.getTime(), 0.000001 );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception querying federate time: %s", e._reason );
	}
}

///////////////////////////////////
// TEST: (valid) testQueryLBTS() //
///////////////////////////////////
void QueryTest::testQueryLBTS()
{
	// query the LBTS while not regulating //
	RTIfedTime theTime = 0.0;
	try
	{
		defaultFederate->rtiamb->queryLBTS( theTime );
		CPPUNIT_ASSERT_DOUBLES_EQUAL( 0.0, theTime.getTime(), 0.000001 );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception querying lbts: %s", e._reason );
	}

	// advance time and query LBTS again //
	try
	{
		defaultFederate->quickAdvanceAndWait( 10.0 );
		defaultFederate->rtiamb->queryLBTS( theTime );
		CPPUNIT_ASSERT_DOUBLES_EQUAL( 10.0, theTime.getTime(), 0.000001 );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception querying lbts: %s", e._reason );
	}

	// enable regulating and query LBTS again //
	try
	{
		defaultFederate->quickEnableRegulating( 5.0 );
		defaultFederate->rtiamb->queryLBTS( theTime );
		CPPUNIT_ASSERT_DOUBLES_EQUAL( 15.0, theTime.getTime(), 0.000001 );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception querying lbts: %s", e._reason );
	}

	// modify lookahead and query LBTS again //
	try
	{
		defaultFederate->quickModifyLookahead( 2.0 );
		defaultFederate->rtiamb->queryLBTS( theTime );
		CPPUNIT_ASSERT_DOUBLES_EQUAL( 12.0, theTime.getTime(), 0.000001 );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception querying lbts: %s", e._reason );
	}
}

////////////////////////////////////////
// TEST: (valid) testQueryLookahead() //
////////////////////////////////////////
void QueryTest::testQueryLookahead()
{
	// query the default lookahead //
	RTIfedTime theTime = 0.0;
	try
	{
		defaultFederate->rtiamb->queryLookahead( theTime );
		CPPUNIT_ASSERT_DOUBLES_EQUAL( 0.0, theTime.getTime(), 0.000001 );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception querying lookahead: %s", e._reason );
	}
	
	// enable regulating and query lookahead again //
	try
	{
		defaultFederate->quickEnableRegulating( 5.0 );
		defaultFederate->rtiamb->queryLookahead( theTime );
		CPPUNIT_ASSERT_DOUBLES_EQUAL( 5.0, theTime.getTime(), 0.000001 );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception querying lookahead: %s", e._reason );
	}

	// modify lookahead and query lookahead again //
	try
	{
		defaultFederate->quickModifyLookahead( 2.0 );
		defaultFederate->rtiamb->queryLookahead( theTime );
		CPPUNIT_ASSERT_DOUBLES_EQUAL( 2.0, theTime.getTime(), 0.000001 );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception querying lookahead: %s", e._reason );
	}
}

