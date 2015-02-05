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
#include "ParameterHandleValuePairSetTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( ParameterHandleValuePairSetTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( ParameterHandleValuePairSetTest, "ParameterHandleValuePairSetTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( ParameterHandleValuePairSetTest, "types" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
ParameterHandleValuePairSetTest::ParameterHandleValuePairSetTest()
{
	this->defaultFederate = new Test13Federate( "defaultFederate" );
}

ParameterHandleValuePairSetTest::~ParameterHandleValuePairSetTest()
{
	delete this->defaultFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void ParameterHandleValuePairSetTest::setUp()
{
    this->defaultFederate->quickCreate();
    this->defaultFederate->quickJoin();
    
    this->theSet = RTI::ParameterSetFactory::create(0);
}

void ParameterHandleValuePairSetTest::tearDown()
{
	this->theSet->empty();
	delete this->theSet;

	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

//////////////////////////////
// TEST: (valid) testSize() //
//////////////////////////////
void ParameterHandleValuePairSetTest::testSize()
{
	CPPUNIT_ASSERT_EQUAL( this->theSet->size() , (RTI::ULong)0 );
	
	this->theSet->add( 501, "aa", 3 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->size() , (RTI::ULong)1 );
	
	this->theSet->add( 502, "ab", 3 );
	this->theSet->add( 503, "ac", 3 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->size() , (RTI::ULong)3 );
}

///////////////////////////////////
// TEST: (valid) testGetHandle() //
///////////////////////////////////
void ParameterHandleValuePairSetTest::testGetHandle()
{
	this->theSet->add( 501, "aa", 3 );
	this->theSet->add( 502, "ab", 3 );
	this->theSet->add( 503, "ac", 3 );

	// Test valid getHandle scenarios
	try
	{
		CPPUNIT_ASSERT_EQUAL( this->theSet->getHandle(0), (RTI::ParameterHandle)501 );
		CPPUNIT_ASSERT_EQUAL( this->theSet->getHandle(1), (RTI::ParameterHandle)502 );
		CPPUNIT_ASSERT_EQUAL( this->theSet->getHandle(2), (RTI::ParameterHandle)503 );
	}
	catch ( RTI::Exception& e )
	{
		failTest( "Unexpected exception while calling valid getHandle: %s", e._reason );
	}
}

///////////////////////////////////////////
// TEST: testGetHandleWithInvalidIndex() //
///////////////////////////////////////////
void ParameterHandleValuePairSetTest::testGetHandleWithInvalidIndex()
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
void ParameterHandleValuePairSetTest::testAdd()
{
	this->theSet->add( 501, "one", 4 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->size() , (RTI::ULong)1 );
	
	this->theSet->add( 502, "two", 4 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->size() , (RTI::ULong)2 );

	// re-adding existing handle, size should still be the same
	this->theSet->add( 501, "three", 6 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->size() , (RTI::ULong)2 );
}

////////////////////////////////////////
// TEST: (valid) testGetValueLength() //
////////////////////////////////////////
void ParameterHandleValuePairSetTest::testGetValueLength()
{
	this->theSet->add( 501, "one", 4 );
	this->theSet->add( 502, "theDeuce", 9 );
	this->theSet->add( 503, "trois", 6);

	// Test valid getValueLength scenarios
	try
	{
		CPPUNIT_ASSERT_EQUAL( this->theSet->getValueLength(0), (RTI::ULong)4 );
		CPPUNIT_ASSERT_EQUAL( this->theSet->getValueLength(1), (RTI::ULong)9 );
		CPPUNIT_ASSERT_EQUAL( this->theSet->getValueLength(2), (RTI::ULong)6 );
	}
	catch ( RTI::Exception& e )
	{
		failTest( "Unexpected exception while getting value length: %s", e._reason );
	}
}

////////////////////////////////////////////////
// TEST: testGetValueLengthWithInvalidIndex() //
////////////////////////////////////////////////
void ParameterHandleValuePairSetTest::testGetValueLengthWithInvalidIndex()
{
	try
	{
		this->theSet->getHandle( 100 );
		failTestMissingException( "ArrayIndexOutOfBounds", 
		                          "getting value length with invalid index" );
	}
	catch ( RTI::ArrayIndexOutOfBounds& aioob )
	{
		// success
	}
	catch ( RTI::Exception& e )
	{
		failTestWrongException( "ArrayIndexOutOfBounds", e, 
		                        "getting value length with invalid index" );
	}
}

//////////////////////////////////
// TEST: (valid) testGetValue() //
//////////////////////////////////
void ParameterHandleValuePairSetTest::testGetValue()
{
	this->theSet->add( 501, "one", 4 );
	this->theSet->add( 502, "theDeuce", 9 );
	this->theSet->add( 503, "trois", 6 );

	// Test valid getValue scenarios
	RTI::ULong valueSize;
	char *value = new char[16];
	int equals = 1;

	try
	{
		// check aa
		theSet->getValue( 0, value, valueSize );
		CPPUNIT_ASSERT_EQUAL( (RTI::ULong)4, valueSize );
		equals = strcmp( value,"one" );
		CPPUNIT_ASSERT_EQUAL( 0, equals );
		
		// check ab
		theSet->getValue( 1, value, valueSize );
		CPPUNIT_ASSERT_EQUAL( (RTI::ULong)9, valueSize );
		equals = strcmp( value,"theDeuce" );
		CPPUNIT_ASSERT_EQUAL( 0, equals );

		// check ac
		theSet->getValue( 2, value, valueSize );
		CPPUNIT_ASSERT_EQUAL( (RTI::ULong)6, valueSize );
		equals = strcmp( value,"trois" );
		CPPUNIT_ASSERT_EQUAL( 0, equals );
		
		// clean up
		delete [] value;
	}
	catch ( RTI::Exception& e )
	{
		delete [] value;
		failTest( "Unexpected exception during getValue(): %s", e._reason );
	}
}

//////////////////////////////////////////
// TEST: testGetValueWithInvalidIndex() //
//////////////////////////////////////////
void ParameterHandleValuePairSetTest::testGetValueWithInvalidIndex()
{
	try
	{
		char tempBuffer[8];
		RTI::ULong tempSize;
		this->theSet->getValue( 100, tempBuffer, tempSize );
		failTestMissingException( "ArrayIndexOutOfBounds", "getting value with invalid index" );
	}
	catch ( RTI::ArrayIndexOutOfBounds& aioob )
	{
		// success
	}
	catch ( RTI::Exception& e )
	{
		failTestWrongException( "ArrayIndexOutOfBounds", e, "getting value with invalid index" );
	}	
}

/////////////////////////////////////////
// TEST: (valid) testGetValuePointer() //
/////////////////////////////////////////
void ParameterHandleValuePairSetTest::testGetValuePointer()
{
	const char *aaValue = "one";
	const char *abValue = "two";
	const char *acValue = "three";
	this->theSet->add( 501, aaValue, 4 );
	this->theSet->add( 502, abValue, 4 );
	this->theSet->add( 503, acValue, 6 );

	// Test valid getValuePointer scenarios
	try
	{
		// check aa
		char *value = NULL;
		RTI::ULong valueSize;
		int equals = 1;

		value = theSet->getValuePointer( 0, valueSize );
		CPPUNIT_ASSERT_EQUAL( (RTI::ULong)4, valueSize );
		equals = strcmp( aaValue,value );
		CPPUNIT_ASSERT_EQUAL( 0, equals );
		
		// check ab
		value = theSet->getValuePointer( 1, valueSize );
		CPPUNIT_ASSERT_EQUAL( (RTI::ULong)4, valueSize );
		equals = strcmp( abValue,value );
		CPPUNIT_ASSERT_EQUAL( 0, equals );
		
		// check ac
		value = theSet->getValuePointer( 2, valueSize );
		CPPUNIT_ASSERT_EQUAL( (RTI::ULong)6, valueSize );
		equals = strcmp( acValue,value );
		CPPUNIT_ASSERT_EQUAL( 0, equals );
	}
	catch ( RTI::Exception& e )
	{
		failTest( "Unexpected exception during getValuePointer(): %s", e._reason );
	}
}

/////////////////////////////////////////////////
// TEST: testGetValuePointerWithInvalidIndex() //
/////////////////////////////////////////////////
void ParameterHandleValuePairSetTest::testGetValuePointerWithInvalidIndex()
{
	// Try getValuePointer on an array index that does not exist
	try
	{
		RTI::ULong tempSize;
		this->theSet->getValuePointer( 100, tempSize );
		failTestMissingException( "ArrayIndexOutOfBounds", "getValuePointer() with invalid index" );
	}
	catch ( RTI::ArrayIndexOutOfBounds& aioob )
	{
		// success
	}
	catch ( RTI::Exception& e )
	{
		failTestWrongException( "ArrayIndexOutOfBounds", e, "getValuePointer() with invalid index" );
	}
}

////////////////////////////////
// TEST: (valid) testRemove() //
////////////////////////////////
void ParameterHandleValuePairSetTest::testRemove()
{
	this->theSet->add( 501, "one", 4 );
	this->theSet->add( 502, "two", 4 );

	// valid remove
	this->theSet->remove( 501 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->size(), (RTI::ULong)1 );

	// removing a handle that doesn't exist. Will fail silently (I think this is ok), but won't
	// affect the state of the AHS
	this->theSet->remove( 600 );
	CPPUNIT_ASSERT_EQUAL( this->theSet->size(), (RTI::ULong)1 );
}

///////////////////////////////
// TEST: (valid) testEmpty() //
///////////////////////////////
void ParameterHandleValuePairSetTest::testEmpty()
{
	this->theSet->add( 501, "one", 4 );
	this->theSet->add( 502, "two", 4 );
	this->theSet->add( 503, "three", 6 );

	this->theSet->empty();
	CPPUNIT_ASSERT_EQUAL( this->theSet->size() , (RTI::ULong)0 );
}
