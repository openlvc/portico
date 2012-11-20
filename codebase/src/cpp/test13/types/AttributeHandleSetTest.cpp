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
#include "AttributeHandleSetTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( AttributeHandleSetTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( AttributeHandleSetTest, "AttributeHandleSetTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( AttributeHandleSetTest, "types" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
AttributeHandleSetTest::AttributeHandleSetTest()
{
	this->defaultFederate = new Test13Federate( "defaultFederate" );
}

AttributeHandleSetTest::~AttributeHandleSetTest()
{
	delete this->defaultFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void AttributeHandleSetTest::setUp()
{
    this->defaultFederate->quickCreate();
    this->defaultFederate->quickJoin();
    
    this->theSet = RTI::AttributeHandleSetFactory::create(0);
}

void AttributeHandleSetTest::tearDown()
{
	this->theSet->empty();
	delete this->theSet;
	
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////
// TEST: (valid) testCreateFederation() //
//////////////////////////////////////////
void AttributeHandleSetTest::testSize()
{
	CPPUNIT_ASSERT_EQUAL( this->theSet->size() , (RTI::ULong)0 );
	
	this->theSet->add( 501 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->size() , (RTI::ULong)1 );
	
	this->theSet->add( 502 );
	this->theSet->add( 503 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->size() , (RTI::ULong)3 );
}

///////////////////////////////////
// TEST: (valid) testGetHandle() //
///////////////////////////////////
void AttributeHandleSetTest::testGetHandle()
{
	try
	{
		this->theSet->add( 501 );
		this->theSet->add( 502 );
		this->theSet->add( 503 );

		// Test valid getHandle scenarios
		CPPUNIT_ASSERT_EQUAL( this->theSet->getHandle(0), (RTI::AttributeHandle)501 );
		CPPUNIT_ASSERT_EQUAL( this->theSet->getHandle(1), (RTI::AttributeHandle)502 );
		CPPUNIT_ASSERT_EQUAL( this->theSet->getHandle(2), (RTI::AttributeHandle)503 );
	}
	catch ( RTI::Exception& e )
	{
		failTest( "Unexpected exception while calling valid getHandle: %s", e._reason );
	}
}

///////////////////////////////////////////
// TEST: testGetHandleWithInvalidIndex() //
///////////////////////////////////////////
void AttributeHandleSetTest::testGetHandleWithInvalidIndex()
{
	// Try getHandle on an array index that does not exist
	try
	{
		this->theSet->getHandle( 100 );
		failTestMissingException( "ArrayIndexOutOfBounds", "getting handle with invalid index" );
	}
	catch ( RTI::ArrayIndexOutOfBounds& aioob )
	{
		// success
	}
	catch ( RTI::Exception& e )
	{
		failTestWrongException( "ArrayIndexOutOfBounds", e, "getting handle with invalid index" );
	}
}

/////////////////////////////
// TEST: (valid) testAdd() //
/////////////////////////////
void AttributeHandleSetTest::testAdd()
{
	this->theSet->add( 501 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->size() , (RTI::ULong)1 );
	
	this->theSet->add( 502 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->size() , (RTI::ULong)2 );

	// re-adding existing handle, size should still be the same
	this->theSet->add( 501 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->size() , (RTI::ULong)2 );	
}

////////////////////////////////
// TEST: (valid) testRemove() //
////////////////////////////////
void AttributeHandleSetTest::testRemove()
{
	this->theSet->add( 501 );
	this->theSet->add( 502 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->size() , (RTI::ULong)2 );

	// valid remove
	this->theSet->remove( 501 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->size(), (RTI::ULong)1 );

	// removing a handle that doesn't exist.
	try
	{
		this->theSet->remove( 600 );
		failTest( "Expected exception when removing handle that doesn't exist from set" );
	}
	catch( RTI::AttributeNotDefined& e )
	{
		// success!
	}
}

///////////////////////////////
// TEST: (valid) testEmpty() //
///////////////////////////////
void AttributeHandleSetTest::testEmpty()
{
	this->theSet->add( 501 );
	this->theSet->add( 502 );
	this->theSet->add( 503 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->size() , (RTI::ULong)3 );

	this->theSet->empty();
	CPPUNIT_ASSERT_EQUAL( this->theSet->size() , (RTI::ULong)0 );
	
}

/////////////////////////////////
// TEST: (valid) testIsEmpty() //
/////////////////////////////////
void AttributeHandleSetTest::testIsEmpty()
{
	this->theSet->add( 501 );
	this->theSet->add( 502 );
	this->theSet->add( 503 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->isEmpty(), RTI::RTI_FALSE );

	this->theSet->empty();
	CPPUNIT_ASSERT_EQUAL( this->theSet->isEmpty() , RTI::RTI_TRUE );

	// make sure that if we empty the set with remove() that is comes up as empty
	this->theSet->add( 501 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->isEmpty(), RTI::RTI_FALSE );
	this->theSet->remove( 501 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->isEmpty(), RTI::RTI_TRUE );
}

//////////////////////////////////
// TEST: (valid) testIsMember() //
//////////////////////////////////
void AttributeHandleSetTest::testIsMember()
{
	this->theSet->add( 501 );
	this->theSet->add( 502 );
	this->theSet->add( 503 );

	CPPUNIT_ASSERT_EQUAL( this->theSet->isMember(501), RTI::RTI_TRUE );
	CPPUNIT_ASSERT_EQUAL( this->theSet->isMember(502), RTI::RTI_TRUE );
	CPPUNIT_ASSERT_EQUAL( this->theSet->isMember(503), RTI::RTI_TRUE );
	CPPUNIT_ASSERT_EQUAL( this->theSet->isMember(600), RTI::RTI_FALSE );
}

