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
#include "RequestUpdateWithRegionTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( RequestUpdateWithRegionTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( RequestUpdateWithRegionTest, "RequestUpdateWithRegionTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( RequestUpdateWithRegionTest, "requestUpdateWithRegion" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( RequestUpdateWithRegionTest, "ddm" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
RequestUpdateWithRegionTest::RequestUpdateWithRegionTest()
{
	this->defaultFederate = new Test13Federate( "defaultFederate" );
	this->provokerFederate = new Test13Federate( "provokerFederate" );
}

RequestUpdateWithRegionTest::~RequestUpdateWithRegionTest()
{
	delete this->provokerFederate;
	delete this->defaultFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void RequestUpdateWithRegionTest::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->provokerFederate->quickJoin();

	// get the handle information
	this->aHandle  = defaultFederate->quickOCHandle( "ObjectRoot.A" );
	this->aaHandle = defaultFederate->quickACHandle( "ObjectRoot.A", "aa" );
	this->abHandle = defaultFederate->quickACHandle( "ObjectRoot.A", "ab" );
	this->acHandle = defaultFederate->quickACHandle( "ObjectRoot.A", "ac" );
	this->bHandle  = defaultFederate->quickOCHandle( "ObjectRoot.A.B" );
	
	// publish and subscribe
	this->defaultFederate->quickPublish( "ObjectRoot.A", 2, "aa", "ab" );
	this->defaultFederate->quickPublish( "ObjectRoot.A.B", 3, "aa", "ab", "ac" );
	
	// create the regions
	this->senderRegion = defaultFederate->quickCreateTestRegion( 100, 200 );
	this->updateRegion = provokerFederate->quickCreateTestRegion( 150, 250 );
	this->updateRegionOOB = provokerFederate->quickCreateTestRegion( 1000, 2000 );
	
	// register some objects to work with
	this->firstObject = defaultFederate->quickRegisterWithRegion( "ObjectRoot.A", senderRegion,
	                                                              2, "aa", "ab" );
	this->secondObject = defaultFederate->quickRegisterWithRegion( "ObjectRoot.A.B", senderRegion,
	                                                              3, "aa", "ab", "ac" );
	
	// create the attribute handle set that is used for convenience
	this->ahs = defaultFederate->populatedAHS( 3, aaHandle, abHandle, acHandle );
}

void RequestUpdateWithRegionTest::tearDown()
{
	delete this->senderRegion;
	delete this->updateRegion;
	delete this->updateRegionOOB;
	delete this->ahs;
	
	this->provokerFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
// void requestClassAttributeValueUpdateWithRegion( RTI::ObjectClassHandle theClass,
//                                                  const RTI::AttributeHandleSet &theAttributes,
//                                                  const RTI::Region &theRegion )
//     throw( RTI::ObjectClassNotDefined,
//            RTI::AttributeNotDefined,
//            RTI::RegionNotKnown,
//            RTI::FederateNotExecutionMember,
//            RTI::ConcurrentAccessAttempted,
//            RTI::SaveInProgress,
//            RTI::RestoreInProgress,
//            RTI::RTIinternalError )

/////////////////////////////////////////////////
// TEST: (valid) testRequestUpdateWithRegion() //
/////////////////////////////////////////////////
void RequestUpdateWithRegionTest::testRequestUpdateWithRegion()
{
	try
	{
		provokerFederate->rtiamb->requestClassAttributeValueUpdateWithRegion( aHandle,
		                                                                      *ahs,
		                                                                      *updateRegion );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while requesting class update with ddm: %s", e._reason );
	}

	// make sure the callbacks get through to the federate that owns the attributes
	set<RTI::AttributeHandle>* firstAttributes =
		defaultFederate->fedamb->waitForProvideRequest( firstObject );
	CPPUNIT_ASSERT( 2 == firstAttributes->size() );
	CPPUNIT_ASSERT( firstAttributes->find(aaHandle) != firstAttributes->end() );
	CPPUNIT_ASSERT( firstAttributes->find(abHandle) != firstAttributes->end() );
	CPPUNIT_ASSERT( firstAttributes->find(acHandle) == firstAttributes->end() );
	delete firstAttributes;

	set<RTI::AttributeHandle>* secondAttributes =
		defaultFederate->fedamb->waitForProvideRequest( secondObject );
	CPPUNIT_ASSERT( secondAttributes->find(aaHandle) != firstAttributes->end() );
	CPPUNIT_ASSERT( secondAttributes->find(abHandle) != firstAttributes->end() );
	CPPUNIT_ASSERT( secondAttributes->find(acHandle) != firstAttributes->end() );
	delete secondAttributes;
}

//////////////////////////////////////////////////////////////////////////
// TEST: (valid) testRequestUpdateWithRegionUsingNonOverlappingRegion() //
//////////////////////////////////////////////////////////////////////////
void RequestUpdateWithRegionTest::testRequestUpdateWithRegionUsingNonOverlappingRegion()
{
	try
	{
		provokerFederate->rtiamb->requestClassAttributeValueUpdateWithRegion( aHandle,
		                                                                      *ahs,
		                                                                      *updateRegionOOB );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while requesting class update with ddm: %s", e._reason );
	}

	// make sure the callbacks DON'T get through to the federate (as the regions don't overlap)
	defaultFederate->fedamb->waitForProvideRequestTimeout( firstObject );
	defaultFederate->fedamb->waitForProvideRequestTimeout( secondObject );
}

////////////////////////////////////////////////////////////////
// TEST: testRequestUpdateWithRegionUsingInvalidObjectClass() //
////////////////////////////////////////////////////////////////
void RequestUpdateWithRegionTest::testRequestUpdateWithRegionUsingInvalidObjectClass()
{
	try
	{
		provokerFederate->rtiamb->requestClassAttributeValueUpdateWithRegion( 1000000,
		                                                                      *ahs,
		                                                                      *updateRegion );
		failTestMissingException( "ObjectClassNotDefined",
		                          "requesting update with invalid object class" );
	}
	catch( RTI::ObjectClassNotDefined &ocnd )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "ObjectClassNotDefined", e,
		                        "requesting update with invalid object class" );
	}
}

///////////////////////////////////////////////////////////////////
// TEST: testRequestUpdateWithRegionUsingInvalidAttributeClass() //
///////////////////////////////////////////////////////////////////
void RequestUpdateWithRegionTest::testRequestUpdateWithRegionUsingInvalidAttributeClass()
{
	RTI::AttributeHandleSet *badset = defaultFederate->populatedAHS( 100, aaHandle, abHandle );
	try
	{
		provokerFederate->rtiamb->requestClassAttributeValueUpdateWithRegion( aHandle,
		                                                                      *badset,
		                                                                      *updateRegion );
		delete badset;
		failTestMissingException( "AttributeNotDefined",
		                          "requesting update with invalid attribute handle" );
	}
	catch( RTI::AttributeNotDefined &attnd )
	{
		// success!
		delete badset;
	}
	catch( RTI::Exception &e )
	{
		delete badset;
		failTestWrongException( "AttributeNotDefined", e,
		                        "requesting update with invalid attribute handle" );
	}
}

///////////////////////////////////////////////////////////
// TEST: testRequestUpdateWithRegionUsingUnknownRegion() //
///////////////////////////////////////////////////////////
void RequestUpdateWithRegionTest::testRequestUpdateWithRegionUsingUnknownRegion()
{
	try
	{
		provokerFederate->rtiamb->requestClassAttributeValueUpdateWithRegion( aHandle,
		                                                                      *ahs,
		                                                                      *senderRegion );
		failTestMissingException( "RegionNotKnown",
		                          "requesting update with region created in other federate" );
	}
	catch( RTI::RegionNotKnown &rnk )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "RegionNotKnown", e, 
		                        "requesting update with region created in other federate" );
	}
}

//////////////////////////////////////////////////////
// TEST: testRequestUpdateWithRegionWhenNotJoined() //
//////////////////////////////////////////////////////
void RequestUpdateWithRegionTest::testRequestUpdateWithRegionWhenNotJoined()
{
	provokerFederate->quickResign();
	
	try
	{
		provokerFederate->rtiamb->requestClassAttributeValueUpdateWithRegion( aHandle,
		                                                                      *ahs,
		                                                                      *updateRegion );
		failTestMissingException( "FederateNotExecutionMember",
		                          "requesting update with region when not joined" );
	}
	catch( RTI::FederateNotExecutionMember &fnem )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "FederateNotExecutionMember", e, 
		                        "requesting update with region when not joined" );
	}
}
