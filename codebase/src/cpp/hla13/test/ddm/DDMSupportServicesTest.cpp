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
#include "DDMSupportServicesTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( DDMSupportServicesTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( DDMSupportServicesTest, "DDMSupportServicesTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( DDMSupportServicesTest, "ddm" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( DDMSupportServicesTest, "supportServices" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
DDMSupportServicesTest::DDMSupportServicesTest()
{
	this->defaultFederate = new Test13Federate( "defaultFederate" );
}

DDMSupportServicesTest::~DDMSupportServicesTest()
{
	delete this->defaultFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void DDMSupportServicesTest::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
}

void DDMSupportServicesTest::tearDown()
{
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////
// TEST: (valid) testGetRegion() //
///////////////////////////////////
// RTI::Region* getRegion( RTI::RegionToken token )
//     throw( RTI::FederateNotExecutionMember,
//            RTI::ConcurrentAccessAttempted,
//            RTI::RegionNotKnown,
//            RTI::RTIinternalError )
void DDMSupportServicesTest::testGetRegion()
{
	// register a region
	RTI::Region *region = defaultFederate->quickCreateTestRegion( 100, 150 );
	RTI::RegionToken token = defaultFederate->quickGetRegionToken( region );
	RTI::Region *otherRegion = NULL; 
	
	// get the region for the token and make sure they are the same
	try
	{
		otherRegion = defaultFederate->rtiamb->getRegion( token );
	}
	catch( RTI::Exception &e )
	{
		delete region;
		failTest( "Fetching region for valid token: %s", e._reason );
	}
	
	if( otherRegion == NULL )
	{
		delete region;
		failTest( "Failed to fetch region, null found" );
	}
	
	// make sure the values are the same
	CPPUNIT_ASSERT_EQUAL( region->getSpaceHandle(), otherRegion->getSpaceHandle() );
	CPPUNIT_ASSERT_EQUAL( region->getNumberOfExtents(), otherRegion->getNumberOfExtents() );
	
	delete region;
	delete otherRegion;
}

////////////////////////////////////////
// TEST: (valid) testGetRegionToken() //
////////////////////////////////////////
// RTI::RegionToken getRegionToken( RTI::Region *theRegion )
//     throw( RTI::FederateNotExecutionMember, 
//            RTI::ConcurrentAccessAttempted,
//            RTI::RegionNotKnown,
//            RTI::RTIinternalError )
void DDMSupportServicesTest::testGetRegionToken()
{
	RTI::Region *region = defaultFederate->quickCreateTestRegion( 100, 150 );
	RTI::RegionToken token = defaultFederate->rtiamb->getRegionToken( region );
	RTI::Region *other = defaultFederate->rtiamb->getRegion( token );
	
	CPPUNIT_ASSERT_EQUAL( region->getNumberOfExtents(), other->getNumberOfExtents() );
	CPPUNIT_ASSERT_EQUAL( region->getSpaceHandle(), other->getSpaceHandle() );
	
	delete other;
	delete region;
}

////////////////////////////////////////////////////////
// TEST: (valid) testGetAttributeRoutingSpaceHandle() //
////////////////////////////////////////////////////////
// RTI::SpaceHandle
// getAttributeRoutingSpaceHandle( RTI::AttributeHandle theHandle, 
//                                 RTI::ObjectClassHandle whichClass )
//     throw( RTI::ObjectClassNotDefined,
//            RTI::AttributeNotDefined,
//            RTI::FederateNotExecutionMember,
//            RTI::ConcurrentAccessAttempted,
//            RTI::RTIinternalError )
void DDMSupportServicesTest::testGetAttributeRoutingSpaceHandle()
{
	RTI::SpaceHandle expected = defaultFederate->quickSpaceHandle( "TestSpace" );
	RTI::ObjectClassHandle classHandle = defaultFederate->quickOCHandle( "ObjectRoot.A" );
	RTI::AttributeHandle attributeHandle = defaultFederate->quickACHandle( "ObjectRoot.A", "aa" );
	
	RTI::SpaceHandle actual =
		defaultFederate->rtiamb->getAttributeRoutingSpaceHandle( attributeHandle, classHandle );
	
	CPPUNIT_ASSERT_EQUAL( expected, actual );
}

////////////////////////////////////////////
// TEST: (valid) testGetDimensionHandle() //
////////////////////////////////////////////
// RTI::DimensionHandle
// getDimensionHandle( const char *theName, RTI::SpaceHandle whichSpace )
//     throw( RTI::SpaceNotDefined,
//            RTI::NameNotFound,
//            RTI::FederateNotExecutionMember,
//            RTI::ConcurrentAccessAttempted,
//            RTI::RTIinternalError )
void DDMSupportServicesTest::testGetDimensionHandle()
{
	RTI::SpaceHandle spaceHandle = defaultFederate->quickSpaceHandle( "TestSpace" );
	RTI::DimensionHandle dimensionHandle =
		defaultFederate->rtiamb->getDimensionHandle( "TestDimension", spaceHandle );
	
	CPPUNIT_ASSERT_EQUAL( (RTI::ULong)(spaceHandle+1), (RTI::ULong)dimensionHandle );
}

//////////////////////////////////////////
// TEST: (valid) testGetDimensionName() //
//////////////////////////////////////////
// char*
// getDimensionName( RTI::DimensionHandle theHandle, RTI::SpaceHandle whichSpace )
//     throw( RTI::SpaceNotDefined,
//            RTI::DimensionNotDefined,
//            RTI::FederateNotExecutionMember,
//            RTI::ConcurrentAccessAttempted,
//            RTI::RTIinternalError )
void DDMSupportServicesTest::testGetDimensionName()
{
	RTI::SpaceHandle spaceHandle = defaultFederate->quickSpaceHandle( "TestSpace" );
	RTI::DimensionHandle dimensionHandle =
		defaultFederate->quickDimensionHandle( "TestSpace", "TestDimension" );
	
	char *dimensionName = defaultFederate->rtiamb->getDimensionName( dimensionHandle, spaceHandle );
	int result = strcmp( "TestDimension", dimensionName );
	
	CPPUNIT_ASSERT_EQUAL( 0, result );
}

//////////////////////////////////////////////////////////
// TEST: (valid) testGetInteractionRoutingSpaceHandle() //
//////////////////////////////////////////////////////////
// RTI::SpaceHandle
// getInteractionRoutingSpaceHandle( RTI::InteractionClassHandle theHandle )
//     throw( RTI::InteractionClassNotDefined,
//            RTI::FederateNotExecutionMember,
//            RTI::ConcurrentAccessAttempted,
//            RTI::RTIinternalError )
void DDMSupportServicesTest::testGetInteractionRoutingSpaceHandle()
{
	RTI::SpaceHandle expected = defaultFederate->quickSpaceHandle( "TestSpace" );
	RTI::ObjectClassHandle classHandle = defaultFederate->quickICHandle( "InteractionRoot.X" );

	RTI::SpaceHandle actual = defaultFederate->rtiamb->getInteractionRoutingSpaceHandle( classHandle );

	CPPUNIT_ASSERT_EQUAL( expected, actual );
}

///////////////////////////////////////////////
// TEST: (valid) testGetRoutingSpaceHandle() //
///////////////////////////////////////////////
// RTI::SpaceHandle getRoutingSpaceHandle( const char *theName )
//     throw( RTI::NameNotFound,
//            RTI::FederateNotExecutionMember,
//            RTI::ConcurrentAccessAttempted,
//            RTI::RTIinternalError )
void DDMSupportServicesTest::testGetRoutingSpaceHandle()
{
	// not really sure how to test this. just same as getSpaceName() until I figure something out
	RTI::SpaceHandle spaceHandle = defaultFederate->quickSpaceHandle( "TestSpace" );
	char *spaceName = defaultFederate->rtiamb->getRoutingSpaceName( spaceHandle );
	int result = strcmp( "TestSpace", spaceName );
	
	CPPUNIT_ASSERT_EQUAL( 0, result );
}

/////////////////////////////////////////////
// TEST: (valid) testGetRoutingSpaceName() //
/////////////////////////////////////////////
// char* getRoutingSpaceName( RTI::SpaceHandle theHandle )
//     throw( RTI::SpaceNotDefined,
//            RTI::FederateNotExecutionMember,
//            RTI::ConcurrentAccessAttempted,
//            RTI::RTIinternalError )
void DDMSupportServicesTest::testGetRoutingSpaceName()
{
	RTI::SpaceHandle spaceHandle = defaultFederate->quickSpaceHandle( "TestSpace" );
	
	char *spaceName = defaultFederate->rtiamb->getRoutingSpaceName( spaceHandle );
	int result = strcmp( "TestSpace", spaceName );
	
	CPPUNIT_ASSERT_EQUAL( 0, result );
}

