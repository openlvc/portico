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
#include "ReflectAttributesTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( ReflectAttributesTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( ReflectAttributesTest, "ReflectAttributesTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( ReflectAttributesTest, "reflectAttributes" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( ReflectAttributesTest, "objectManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
ReflectAttributesTest::ReflectAttributesTest()
{
	this->defaultFederate = new Test13Federate( "defaultFederate" );
	this->listenerFederate = new Test13Federate( "listenerFederate" );
}

ReflectAttributesTest::~ReflectAttributesTest()
{
	delete this->listenerFederate;
	delete this->defaultFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void ReflectAttributesTest::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->listenerFederate->quickJoin();
	
	// cache the handles
	this->aHandle  = defaultFederate->quickOCHandle( "ObjectRoot.A" );
	this->aaHandle = defaultFederate->quickACHandle( "ObjectRoot.A", "aa" );
	this->abHandle = defaultFederate->quickACHandle( "ObjectRoot.A", "ab" );
	this->acHandle = defaultFederate->quickACHandle( "ObjectRoot.A", "ac" );
	this->bHandle  = defaultFederate->quickOCHandle( "ObjectRoot.A.B" );
	this->baHandle = defaultFederate->quickACHandle( "ObjectRoot.A.B", "ba" );
	this->bbHandle = defaultFederate->quickACHandle( "ObjectRoot.A.B", "bb" );
	this->bcHandle = defaultFederate->quickACHandle( "ObjectRoot.A.B", "bc" );

	// create the ahvps
	this->ahvps = defaultFederate->createAHVPS( 6 );
	this->ahvps->add( aaHandle, "aa", 3 );
	this->ahvps->add( abHandle, "ab", 3 );
	this->ahvps->add( acHandle, "ac", 3 );
	this->ahvps->add( baHandle, "ba", 3 );
	this->ahvps->add( bbHandle, "bb", 3 );
	this->ahvps->add( bcHandle, "bc", 3 );
	
	// do the publish and subscribe
	defaultFederate->quickPublish( "ObjectRoot.A.B", 6, "aa", "ab", "ac", "ba", "bb", "bc" );
	listenerFederate->quickSubscribe( "ObjectRoot.A.B", 6, "aa", "ab", "ac", "ba", "bb", "bc" );
	
	// enable the time profiles
	defaultFederate->quickEnableRegulating( 5.0 );
	listenerFederate->quickEnableAsync();
	listenerFederate->quickEnableConstrained();

	// register and discover the object to update
	this->theObject = this->defaultFederate->quickRegister( "ObjectRoot.A.B" );
	this->ng6Object = this->listenerFederate->fedamb->waitForDiscoveryAs( theObject, bHandle );
}

void ReflectAttributesTest::tearDown()
{
	delete ahvps;
	this->listenerFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// Test Helper Methods ////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

void ReflectAttributesTest::validateReflectedRO()
{
	// wait for the reflection to be received
	listenerFederate->fedamb->waitForROUpdate( theObject );
	
	// validate the values
	int result = strcmp( "aa", ng6Object->getAttribute(aaHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "ab", ng6Object->getAttribute(abHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "ac", ng6Object->getAttribute(acHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "ba", ng6Object->getAttribute(baHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "bb", ng6Object->getAttribute(bbHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "bc", ng6Object->getAttribute(bcHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
}

void ReflectAttributesTest::validateReflectedTSO()
{
	// wait for the reflection to be received
	listenerFederate->fedamb->waitForTSOUpdate( theObject );
	
	// validate the values
	int result = strcmp( "aa", ng6Object->getAttribute(aaHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "ab", ng6Object->getAttribute(abHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "ac", ng6Object->getAttribute(acHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "ba", ng6Object->getAttribute(baHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "bb", ng6Object->getAttribute(bbHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "bc", ng6Object->getAttribute(bcHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
}

////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////// RO Reflection Test Methods ////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////
// TEST: (valid) testReflectRO() //
///////////////////////////////////
void ReflectAttributesTest::testReflectRO()
{
	try
	{
		defaultFederate->rtiamb->updateAttributeValues( theObject, *ahvps, "NA" );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while updating attribute values (RO): %s", e._reason );
	}
	
	validateReflectedRO();
}

////////////////////////////////////////////
// TEST: testReflectROWithUnknownObject() //
////////////////////////////////////////////
void ReflectAttributesTest::testReflectROWithUnknownObject()
{
	try
	{
		defaultFederate->rtiamb->updateAttributeValues( 100000, *ahvps, "NA" );
		failTestMissingException( "ObjectNotKnown", "updating attributes for unknown object" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown", e, "updating attributes for unknown object" );
	}
}

///////////////////////////////////////////////
// TEST: testReflectROWithInvalidAttribute() //
///////////////////////////////////////////////
void ReflectAttributesTest::testReflectROWithInvalidAttribute()
{
	RTI::AttributeHandleValuePairSet *set = defaultFederate->createAHVPS( 2 );
	set->add( aaHandle, "aa", 3 );
	set->add( 10000000, "na", 3 );
	
	try
	{
		defaultFederate->rtiamb->updateAttributeValues( theObject, *set, "NA" );
		delete set;
		failTestMissingException( "AttributeNotDefined",
		                          "updating attributes with invalid attribute" );
	}
	catch( RTI::AttributeNotDefined& attnd )
	{
		// success
		delete set;
	}
	catch( RTI::Exception& e )
	{
		delete set;
		failTestWrongException( "AttributeNotDefined", e,
		                        "updating attributes with invalid attribute" );
	}
}

///////////////////////////////////////////////
// TEST: testReflectROWithUnownedAttribute() //
///////////////////////////////////////////////
void ReflectAttributesTest::testReflectROWithUnownedAttribute()
{
	RTI::AttributeHandleValuePairSet *set = defaultFederate->createAHVPS( 1 );
	set->add( aaHandle, "aa", 3 );
	
	try
	{
		listenerFederate->rtiamb->updateAttributeValues( theObject, *set, "NA" );
		delete set;
		failTestMissingException( "AttributeNotOwned", "updating attributes not owned" );
	}
	catch( RTI::AttributeNotOwned& ano )
	{
		// success
		delete set;
	}
	catch( RTI::Exception& e )
	{
		delete set;
		failTestWrongException( "AttributeNotOwned", e, "updating attributes not owned" );
	}
}

////////////////////////////////////////
// TEST: testReflectROWhenNotJoined() //
////////////////////////////////////////
void ReflectAttributesTest::testReflectROWhenNotJoined()
{
	defaultFederate->quickResign();
	
	try
	{
		defaultFederate->rtiamb->updateAttributeValues( theObject, *ahvps, "NA" );
		failTestMissingException( "FederateNotExecutionMember", "updating attributes while not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "updating attributes while not joined" );
	}
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// TSO Reflection Test Methods ////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////
// TEST: (valid) testReflectTSO() //
////////////////////////////////////
void ReflectAttributesTest::testReflectTSO()
{	
	try
	{
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->updateAttributeValues( theObject, *ahvps, theTime, "NA" );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while updating attribute values (TSO): %s", e._reason );
	}
	
	// shouldn't receive the reflection yet
	listenerFederate->fedamb->waitForTSOUpdateTimeout( theObject );
	defaultFederate->quickAdvanceAndWait( 10.0 );
	listenerFederate->quickAdvanceAndWait( 10.0 );
	validateReflectedTSO();
}

/////////////////////////////////////////////
// TEST: testReflectTSOWithUnknownObject() //
/////////////////////////////////////////////
void ReflectAttributesTest::testReflectTSOWithUnknownObject()
{
	try
	{
		RTIfedTime time = 10.0;
		defaultFederate->rtiamb->updateAttributeValues( 100000, *ahvps, time, "NA" );
		failTestMissingException( "ObjectNotKnown", "updating attributes for unknown object" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown", e, "updating attributes for unknown object" );
	}

}

////////////////////////////////////////////////
// TEST: testReflectTSOWithInvalidAttribute() //
////////////////////////////////////////////////
void ReflectAttributesTest::testReflectTSOWithInvalidAttribute()
{
	RTI::AttributeHandleValuePairSet *set = defaultFederate->createAHVPS( 2 );
	set->add( aaHandle, "aa", 3 );
	set->add( 10000000, "na", 3 );
	
	try
	{
		RTIfedTime time = 10.0;
		defaultFederate->rtiamb->updateAttributeValues( theObject, *set, time, "NA" );
		delete set;
		failTestMissingException( "AttributeNotDefined",
		                          "updating attributes with invalid attribute" );
	}
	catch( RTI::AttributeNotDefined& attnd )
	{
		// success
		delete set;
	}
	catch( RTI::Exception& e )
	{
		delete set;
		failTestWrongException( "AttributeNotDefined", e,
		                        "updating attributes with invalid attribute" );
	}
}

////////////////////////////////////////////////
// TEST: testReflectTSOWithUnownedAttribute() //
////////////////////////////////////////////////
void ReflectAttributesTest::testReflectTSOWithUnownedAttribute()
{
	RTI::AttributeHandleValuePairSet *set = defaultFederate->createAHVPS( 1 );
	set->add( aaHandle, "aa", 3 );
	
	try
	{
		RTIfedTime time = 10.0;
		listenerFederate->rtiamb->updateAttributeValues( theObject, *set, time, "NA" );
		delete set;
		failTestMissingException( "AttributeNotOwned", "updating attributes not owned" );
	}
	catch( RTI::AttributeNotOwned& ano )
	{
		// success
		delete set;
	}
	catch( RTI::Exception& e )
	{
		delete set;
		failTestWrongException( "AttributeNotOwned", e, "updating attributes not owned" );
	}
}

///////////////////////////////////////////
// TEST: testReflectTSOWithInvalidTime() //
///////////////////////////////////////////
void ReflectAttributesTest::testReflectTSOWithInvalidTime()
{
	try
	{
		// can't be -1.0 as that is the time used by Portico to represent no timestamp
		// thus, the LRC/RTI will process the request as an RO one, not a TSO with timestamp of -1
		RTIfedTime time = -11.0;
		defaultFederate->rtiamb->updateAttributeValues( theObject, *ahvps, time, "NA" );
		failTestMissingException( "InvalidFederationTime",
		                          "updating attributes with negative time" );
	}
	catch( RTI::InvalidFederationTime& ift )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "InvalidFederationTime", e,
		                        "updating attributes with negative time" );
	}
}

///////////////////////////////////////////////////
// TEST: testReflectTSOWithTimeBeforeLookahead() //
///////////////////////////////////////////////////
void ReflectAttributesTest::testReflectTSOWithTimeBeforeLookahead()
{
	try
	{
		RTIfedTime time = 3.0;
		defaultFederate->rtiamb->updateAttributeValues( theObject, *ahvps, time, "NA" );
		failTestMissingException( "InvalidFederationTime",
		                          "updating attributes with time before lookahead" );
	}
	catch( RTI::InvalidFederationTime& ift )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "InvalidFederationTime", e,
		                        "updating attributes with time before lookahead" );
	}
}

/////////////////////////////////////////
// TEST: testReflectTSOWhenNotJoined() //
/////////////////////////////////////////
void ReflectAttributesTest::testReflectTSOWhenNotJoined()
{
	defaultFederate->quickResign();
	
	try
	{
		RTIfedTime time = 10.0;
		defaultFederate->rtiamb->updateAttributeValues( theObject, *ahvps, time, "NA" );
		failTestMissingException( "FederateNotExecutionMember", "updating attributes while not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "updating attributes while not joined" );
	}
}
