/*
 *   Copyright 2009 The Portico Project
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
#include "QueryAttributeOwnershipTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( QueryAttributeOwnershipTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( QueryAttributeOwnershipTest, "QueryAttributeOwnershipTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( QueryAttributeOwnershipTest, "ownershipManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
QueryAttributeOwnershipTest::QueryAttributeOwnershipTest()
{
	this->defaultFederate = new TestNG6Federate( "defaultFederate" );
	this->secondFederate = new TestNG6Federate( "secondFederate" );
}

QueryAttributeOwnershipTest::~QueryAttributeOwnershipTest()
{
	delete this->defaultFederate;
	delete this->secondFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void QueryAttributeOwnershipTest::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->secondFederate->quickJoin();
	
	// cache some handle information
	this->aa = defaultFederate->quickACHandle( "ObjectRoot.A", "aa" );
	this->ab = defaultFederate->quickACHandle( "ObjectRoot.A", "ab" );
	this->ac = defaultFederate->quickACHandle( "ObjectRoot.A", "ac" );
	this->ba = defaultFederate->quickACHandle( "ObjectRoot.A.B", "ba" );
	this->bb = defaultFederate->quickACHandle( "ObjectRoot.A.B", "bb" );
	this->bc = defaultFederate->quickACHandle( "ObjectRoot.A.B", "bc" );

	// publish and subscribe
	defaultFederate->quickPublish( "ObjectRoot.A.B", 3, "aa", "ab", "ac" );
	secondFederate->quickSubscribe( "ObjectRoot.A.B", 6, "aa", "ab", "ac", "ba", "bb", "bc" );
	secondFederate->quickPublish( "ObjectRoot.A.B", 6, "aa", "ab", "ac", "ba", "bb", "bc" );

	// register and discover the object
	this->theObject = defaultFederate->quickRegister( "ObjectRoot.A.B" );
	this->secondFederate->fedamb->waitForDiscovery( theObject );
}

void QueryAttributeOwnershipTest::tearDown()
{
	this->secondFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////// Query Attribute Ownership Test Methods //////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
// void queryAttributeOwnership( ObjectHandle theObject,        // supplied C1
//                               AttributeHandle theAttribute ) // supplied C1
//     throw ( ObjectNotKnown,
//             AttributeNotDefined,
//             FederateNotExecutionMember,
//             ConcurrentAccessAttempted,
//             SaveInProgress,
//             RestoreInProgress,
//             RTIinternalError );

////////////////////////////////////////////////////////////////////////
// TEST: (valid) testQueryAttributeOwnershipAfterObjectRegistration() //
////////////////////////////////////////////////////////////////////////
void QueryAttributeOwnershipTest::testQueryAttributeOwnershipAfterObjectRegistration()
{
	// check ownership as it current stands
	// should be: defaultFederate=>{aa,ab,ac}, unowned=>{ba,bb,bc}
	int defaultHandle = defaultFederate->getFederateHandle();
	int secondHandle = secondFederate->getFederateHandle();
	defaultFederate->quickAssertOwnedBy( defaultHandle, theObject, 3, aa, ab, ac );
	defaultFederate->quickAssertOwnedBy( TestNG6Federate::OWNER_UNOWNED, theObject, 3, ba, bb, bc );
	secondFederate->quickAssertOwnedBy( defaultHandle, theObject, 3, aa, ab, ac );
	secondFederate->quickAssertOwnedBy( TestNG6Federate::OWNER_UNOWNED, theObject, 3, ba, bb, bc );

	// pick up some attributes in the second federate
	// should be: defaultFederate=>{aa,ab,ac}, secondFederate=>{ba,bb,bc}
	secondFederate->quickAcquireIfAvailableRequest( theObject, 3, ba, bb, bc );
	
	defaultFederate->quickTick( 0.1, 1.0 );
	defaultFederate->quickAssertOwnedBy( defaultHandle, theObject, 3, aa, ab, ac );
	defaultFederate->quickAssertOwnedBy( secondHandle, theObject, 3, ba, bb, bc );
	secondFederate->quickTick( 0.1, 1.0 );
	secondFederate->quickAssertOwnedBy( defaultHandle, theObject, 3, aa, ab, ac );
	secondFederate->quickAssertOwnedBy( secondHandle, theObject, 3, ba, bb, bc );
}

///////////////////////////////////////////////////////////////////////
// TEST: (valid) testQueryAttributeOwnershipAfterOwnershipTransfer() //
///////////////////////////////////////////////////////////////////////
void QueryAttributeOwnershipTest::testQueryAttributeOwnershipAfterOwnershipTransfer()
{
	// check ownership as it current stands
	// should be: defaultFederate=>{aa,ab,ac}, unowned=>{ba,bb,bc}
	int defaultHandle = defaultFederate->getFederateHandle();
	int secondHandle = secondFederate->getFederateHandle();
	defaultFederate->quickAssertOwnedBy( defaultHandle, theObject, 3, aa, ab, ac );
	defaultFederate->quickAssertOwnedBy( TestNG6Federate::OWNER_UNOWNED, theObject, 3, ba, bb, bc );
	secondFederate->quickAssertOwnedBy( defaultHandle, theObject, 3, aa, ab, ac );
	secondFederate->quickAssertOwnedBy( TestNG6Federate::OWNER_UNOWNED, theObject, 3, ba, bb, bc );
	
	// transfer aa and ab from defaultFederate to secondFederate
	secondFederate->quickAcquireRequest( theObject, 2, aa, ab );
	defaultFederate->fedamb->waitForOwnershipRequest( theObject, 2, aa, ab );
	defaultFederate->quickReleaseResponse( theObject, 2, aa, ab );
	secondFederate->fedamb->waitForOwnershipAcquistion( theObject, 2, aa, ab );
	
	// check to make sure new ownership is good
	defaultFederate->quickAssertOwnedBy( defaultHandle, theObject, 1, ac );
	defaultFederate->quickAssertOwnedBy( secondHandle, theObject, 2, aa, ab );
	defaultFederate->quickAssertOwnedBy( TestNG6Federate::OWNER_UNOWNED, theObject, 3, ba, bb, bc );
	
	secondFederate->quickAssertOwnedBy( defaultHandle, theObject, 1, ac );
	secondFederate->quickAssertOwnedBy( secondHandle, theObject, 2, aa, ab );
	secondFederate->quickAssertOwnedBy( TestNG6Federate::OWNER_UNOWNED, theObject, 3, ba, bb, bc );
}

/////////////////////////////////////////////////////////////////
// TEST: (valid) testQueryAttributeOwnershipForMomAttributes() //
/////////////////////////////////////////////////////////////////
void QueryAttributeOwnershipTest::testQueryAttributeOwnershipForMomAttributes()
{
	// get the handle for a mom attribute
	RTI::AttributeHandle federationNameHandle =
		defaultFederate->quickACHandle( "Manager.Federation", "FederationName" );
	
	// subscribe to a MOM class and wait for discovery
	defaultFederate->quickSubscribe( "Manager.Federation", 1, "FederationName" );
	defaultFederate->fedamb->waitForDiscovery( 0 /*mom federation object*/ );
	
	// check ownership of a MOM attribute
	defaultFederate->quickAssertOwnedBy( TestNG6Federate::OWNER_RTI,
	                                     0 /*mom federation object*/,
	                                     1,
	                                     federationNameHandle );
}

//////////////////////////////////////////////////////////
// TEST: testQueryAttributeOwnershipWithInvalidObject() //
//////////////////////////////////////////////////////////
void QueryAttributeOwnershipTest::testQueryAttributeOwnershipWithInvalidObject()
{
	try
	{
		defaultFederate->rtiamb->queryAttributeOwnership( 100000, aa );
		failTestMissingException( "ObjectNotKnown", "query ownership with invalid object" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "ObjectNotKnown", e, "query ownership with invalid object" );
	}
}

//////////////////////////////////////////////////////////
// TEST: testQueryAttributeOwnershipWithUnknownObject() //
//////////////////////////////////////////////////////////
void QueryAttributeOwnershipTest::testQueryAttributeOwnershipWithUnknownObject()
{
	// register an object in the second federate, so we have a handle that is valid in the
	// federation, but don't have it discovered in the default federate, making it unknown
	RTI::ObjectHandle secondObject = secondFederate->quickRegister( "ObjectRoot.A.B" );
	
	try
	{
		defaultFederate->rtiamb->queryAttributeOwnership( secondObject, aa );
		failTestMissingException( "ObjectNotKnown", "query ownership with unknown object" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "ObjectNotKnown", e, "query ownership with unknown object" );
	}
}

///////////////////////////////////////////////////////////////////
// TEST: testQueryAttributeOwnershipWithWrongAttributeForClass() //
///////////////////////////////////////////////////////////////////
void QueryAttributeOwnershipTest::testQueryAttributeOwnershipWithWrongAttributeForClass()
{
	// register an object at ObjectRoot.A and then try and get ownership of attribute "ba"
	defaultFederate->quickPublish( "ObjectRoot.A", 3, "aa", "ab", "ac" );
	RTI::ObjectHandle secondObject = defaultFederate->quickRegister( "ObjectRoot.A" );
	
	try
	{
		defaultFederate->rtiamb->queryAttributeOwnership( secondObject, ba );
		failTestMissingException( "AttributeNotDefined",
		                          "query ownership with wrong attribute for class" );
	}
	catch( RTI::AttributeNotDefined& andef )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "AttributeNotDefined", e,
		                        "query ownership with wrong attribute for class" );
	}
}

/////////////////////////////////////////////////////////////
// TEST: testQueryAttributeOwnershipWithInvalidAttribute() //
/////////////////////////////////////////////////////////////
void QueryAttributeOwnershipTest::testQueryAttributeOwnershipWithInvalidAttribute()
{
	try
	{
		defaultFederate->rtiamb->queryAttributeOwnership( theObject, 10000 );
		failTestMissingException( "AttributeNotDefined", "query ownership with invalid attribute" );
	}
	catch( RTI::AttributeNotDefined& andef )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "AttributeNotDefined", e,
		                        "query ownership with wrong invalid attribute" );
	}
}

//////////////////////////////////////////////////////
// TEST: testQueryAttributeOwnershipWhenNotJoined() //
//////////////////////////////////////////////////////
void QueryAttributeOwnershipTest::testQueryAttributeOwnershipWhenNotJoined()
{
	defaultFederate->quickResign();

	try
	{
		defaultFederate->rtiamb->queryAttributeOwnership( theObject, aa );
		failTestMissingException( "FederateNotExecutionMember", "query ownership when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "FederateNotExecutionMember", e, "query ownership when not joined" );
	}
}
