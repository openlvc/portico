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
#include "ModifyRegionTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( ModifyRegionTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( ModifyRegionTest, "ModifyRegionTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( ModifyRegionTest, "modifyRegion" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( ModifyRegionTest, "ddm" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
ModifyRegionTest::ModifyRegionTest()
{
	this->defaultFederate = new Test13Federate( "defaultFederate" );
	this->listenerFederate = new Test13Federate( "listenerFederate" );
}

ModifyRegionTest::~ModifyRegionTest()
{
	delete this->defaultFederate;
	delete this->listenerFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void ModifyRegionTest::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->listenerFederate->quickJoin();
	
	// fetch the handles
	this->testSpace = defaultFederate->quickSpaceHandle( "TestSpace" );
	this->testDimension = defaultFederate->quickDimensionHandle( "TestSpace", "TestDimension" );

	// create the regions
	this->testRegion = defaultFederate->quickCreateTestRegion( 100, 200 );
	this->testToken = defaultFederate->quickGetRegionToken( this->testRegion );
	this->listenerRegion = listenerFederate->quickCreateTestRegion( 1000, 1100 );

	// do publication and subscription
	defaultFederate->quickPublish( "ObjectRoot.A", 1, "aa" );
	listenerFederate->quickSubscribeWithRegion( "ObjectRoot.A", listenerRegion, 1, "aa" );
	
	// register the test object
	this->testObject = defaultFederate->quickRegisterWithRegion( "ObjectRoot.A", testRegion, 1, "aa" );
	listenerFederate->fedamb->waitForDiscovery( this->testObject );
}

void ModifyRegionTest::tearDown()
{
	this->listenerFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// Helper Methods //////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
void ModifyRegionTest::validateRegionOverlap()
{
	// send an update from the default federate
	defaultFederate->quickReflect( this->testObject, 1, "aa" );
	// associated region and subscription region should overlap, reflection should be received
	listenerFederate->fedamb->waitForROUpdate( this->testObject );
}

void ModifyRegionTest::validateRegionNoOverlap()
{
	// send an update from the default federate
	defaultFederate->quickReflect( this->testObject, 1, "aa" );
	// associated region and subscription region should not overlap, no reflection should be received
	listenerFederate->fedamb->waitForROUpdateTimeout( this->testObject );
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

// void notifyAboutRegionModification( RTI::Region &theRegion )
//     throw( RTI::RegionNotKnown,
//            RTI::InvalidExtents,
//            RTI::FederateNotExecutionMember,
//            RTI::ConcurrentAccessAttempted,
//            RTI::SaveInProgress,
//            RTI::RestoreInProgress,
//            RTI::RTIinternalError )

//////////////////////////////////////
// TEST: (valid) testModifyRegion() //
//////////////////////////////////////
void ModifyRegionTest::testModifyRegion()
{
	// validate that we don't overlap yet
	validateRegionNoOverlap();
	
	// change the values and validate that they are ONLY changed locally
	testRegion->setRangeLowerBound( 0, testDimension, 1000 );
	testRegion->setRangeUpperBound( 0, testDimension, 2000 );
	validateRegionNoOverlap();
	
	// notify the RTI of the region change and validate that it occurs
	try
	{
		defaultFederate->rtiamb->notifyAboutRegionModification( *testRegion );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception notifying RTI about region modification: %s", e._reason );
	}
	
	// validate that the regions now overlap
	validateRegionOverlap();
}

///////////////////////////////////////////////
// TEST: testModifyRegionWithUnknownRegion() //
///////////////////////////////////////////////
void ModifyRegionTest::testModifyRegionWithUnknownRegion()
{
	try
	{
		listenerFederate->rtiamb->notifyAboutRegionModification( *testRegion );
		failTestMissingException( "RegionNotKnown", "modifying region created in other federate" );
	}
	catch( RTI::RegionNotKnown &rnk )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "RegionNotKnown", e, "modifying region created in other federate" );
	}
}

////////////////////////////////////////////////
// TEST: testModifyRegionWithInvalidExtents() //
////////////////////////////////////////////////
void ModifyRegionTest::testModifyRegionWithInvalidExtents()
{
	// set some invalid extents (lower bound > upper bound). this should cause an
	// exception when we attempt to notify the RTI of the region modification
	testRegion->setRangeLowerBound( 0, testDimension, 2000 );
	testRegion->setRangeUpperBound( 0, testDimension, 1000 );
	validateRegionNoOverlap();
	
	try
	{
		defaultFederate->rtiamb->notifyAboutRegionModification( *testRegion );
		failTestMissingException( "InvalidExtents", "Notify region change with invalid extents" );
	}
	catch( RTI::InvalidExtents &ie )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "InvalidExtents", e, "Notify region change with invalid extents" );
	}
}

///////////////////////////////////////////
// TEST: testModifyRegionWhenNotJoined() //
///////////////////////////////////////////
void ModifyRegionTest::testModifyRegionWhenNotJoined()
{
	defaultFederate->quickResign();
	try
	{
		listenerFederate->rtiamb->notifyAboutRegionModification( *testRegion );
		failTestMissingException( "RegionNotKnown", "modifying region created in other federate" );
	}
	catch( RTI::RegionNotKnown &rnk )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "RegionNotKnown", e, "modifying region created in other federate" );
	}
}

