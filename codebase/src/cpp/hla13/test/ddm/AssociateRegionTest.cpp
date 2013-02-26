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
#include "AssociateRegionTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( AssociateRegionTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( AssociateRegionTest, "AssociateRegionTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( AssociateRegionTest, "associateRegion" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( AssociateRegionTest, "ddm" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
AssociateRegionTest::AssociateRegionTest()
{
	this->defaultFederate = new Test13Federate( "defaultFederate" );
	this->federateA = new Test13Federate( "federateA" );
	this->federateB = new Test13Federate( "federateB" );
	this->federateC = new Test13Federate( "federateC" );
}

AssociateRegionTest::~AssociateRegionTest()
{
	delete this->defaultFederate;
	delete this->federateA;
	delete this->federateB;
	delete this->federateC;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void AssociateRegionTest::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->federateA->quickJoin();
	this->federateB->quickJoin();
	this->federateC->quickJoin();

	// get the handle information
	this->aHandle  = defaultFederate->quickOCHandle( "ObjectRoot.A" );
	this->aaHandle = defaultFederate->quickACHandle( "ObjectRoot.A", "aa" );
	this->abHandle = defaultFederate->quickACHandle( "ObjectRoot.A", "ab" );
	this->acHandle = defaultFederate->quickACHandle( "ObjectRoot.A", "ac" );
	this->bHandle  = defaultFederate->quickOCHandle( "ObjectRoot.A.B" );
	this->baHandle = defaultFederate->quickACHandle( "ObjectRoot.A.B", "ba" );
	this->bbHandle = defaultFederate->quickACHandle( "ObjectRoot.A.B", "bb" );
	this->bcHandle = defaultFederate->quickACHandle( "ObjectRoot.A.B", "bc" );
	
	// create the regions
	this->senderRegion = defaultFederate->quickCreateTestRegion( 100, 200 );
	this->federateARegion = federateA->quickCreateTestRegion( 150, 250 );
	this->federateBRegion = federateB->quickCreateTestRegion( 450, 550 );

	// publish and subscribe
	this->defaultFederate->quickPublish( "ObjectRoot.A.B", 6, "aa", "ab", "ac", "ba", "bb", "bc" );
	this->federateA->quickSubscribeWithRegion( "ObjectRoot.A.B", federateARegion, 6,
	                                           "aa", "ab", "ac", "ba", "bb", "bc" );
	this->federateB->quickSubscribeWithRegion( "ObjectRoot.A.B", federateBRegion, 6, 
	                                           "aa", "ab", "ac", "ba", "bb", "bc" );
	this->federateC->quickSubscribe( "ObjectRoot.A.B", 6, "aa", "ab", "ac", "ba", "bb", "bc" );
	
	// create the handle sets that are used throughout
	this->allHandles = defaultFederate->populatedAHS( 6, aaHandle, abHandle, acHandle,
	                                                     baHandle, bbHandle, bcHandle );
	
	this->theObject = defaultFederate->quickRegister( "ObjectRoot.A.B" );
}

void AssociateRegionTest::tearDown()
{
	delete this->allHandles;
	delete this->senderRegion;
	delete this->federateARegion;
	delete this->federateBRegion;
	
	this->federateC->quickResign();
	this->federateB->quickResign();
	this->federateA->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////// Association Test Methods /////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
// associateRegionForUpdates( RTI::Region &theRegion,
//                            RTI::ObjectHandle theObject,
//                            const RTI::AttributeHandleSet &theAttributes )
//     throw( RTI::ObjectNotKnown,
//            RTI::AttributeNotDefined,
//            RTI::InvalidRegionContext,
//            RTI::RegionNotKnown,
//            RTI::FederateNotExecutionMember,
//            RTI::ConcurrentAccessAttempted,
//            RTI::SaveInProgress,
//            RTI::RestoreInProgress,
//            RTI::RTIinternalError )

/*
 * Validates that the proper association has been made between the attributes of the given object
 * and the defaultFederate. An attribute update will be sent for the defaultFederate and if the
 * association is working properly, only federateA (which has an overlapping region) and federateC
 * (which isn't using DDM) should get the reflection.
 */
void AssociateRegionTest::validateAssociated( RTI::ObjectHandle theObject )
{
	// send the update from the default federate
	defaultFederate->quickReflect( theObject, 6, "aa", "ab", "ac", "ba", "bb", "bc" );
	
	// make sure the reflection is received by federateA and that it contains the right values
	Test13Object *ng6Object = federateA->fedamb->waitForROUpdate( theObject );
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

	// make sure reflections NOT sent to federateB
	federateB->fedamb->waitForROUpdateTimeout( theObject );

	// make sure reflections sent to federateA
	ng6Object = federateC->fedamb->waitForROUpdate( theObject );
	result = strcmp( "aa", ng6Object->getAttribute(aaHandle) );
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

/////////////////////////////////////////////
// TEST: (valid) testAssociateForUpdates() //
/////////////////////////////////////////////
void AssociateRegionTest::testAssociateForUpdates()
{
	// make sure we aren't associated yet
	validateNotAssociated( theObject );
	
	// do the association
	try
	{
		defaultFederate->rtiamb->associateRegionForUpdates( *senderRegion, theObject, *allHandles );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while associating region for updates: %s", e._reason );
	}
	
	validateAssociated( theObject );
}

/////////////////////////////////////////////////////////
// TEST: (valid) testAssociateForUpdatesWithEmptySet() //
/////////////////////////////////////////////////////////
/*
 * This is the same as an implicit unsubscription. First associate and validate, then call
 * associate with 0-attributes in the set and make sure that the association no longer exists.
 */
void AssociateRegionTest::testAssociateForUpdatesWithEmptySet()
{
	// make the association and validate it
	defaultFederate->quickAssociateWithRegion( theObject, senderRegion, 6, "aa", "ab", "ac",
	                                                                       "ba", "bb", "bc" );
	validateAssociated( theObject );
	
	// call associate with an empty set and validate that the association no longer exists
	RTI::AttributeHandleSet *ahs = defaultFederate->createAHS(0);
	try
	{
		defaultFederate->rtiamb->associateRegionForUpdates( *senderRegion, theObject, *ahs );
		delete ahs;
	}
	catch( RTI::Exception &e )
	{
		delete ahs;
		failTest( "Unexpected exception while associating region for updates: %s", e._reason );
	}
	
	validateNotAssociated( theObject );
}

//////////////////////////////////////////////////////
// TEST: testAssociateForUpdatesWithUnknownObject() //
//////////////////////////////////////////////////////
void AssociateRegionTest::testAssociateForUpdatesWithUnknownObject()
{
	try
	{
		defaultFederate->rtiamb->associateRegionForUpdates( *senderRegion, 1000000, *allHandles );
		failTestMissingException( "ObjectNotKnown", "associating for updates with unknown object" );
	}
	catch( RTI::ObjectNotKnown &onk )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "ObjectNotKnown", e, "associating for updates with unknown object" );
	}
}

/////////////////////////////////////////////////////////
// TEST: testAssociateForUpdatedWithInvalidAttribute() //
/////////////////////////////////////////////////////////
void AssociateRegionTest::testAssociateForUpdatedWithInvalidAttribute()
{
	// create an ahs with an invalid handle
	RTI::AttributeHandleSet *ahs = defaultFederate->populatedAHS( 2, aaHandle, 100000000 );
	
	// try and register using the dodgy set
	try
	{
		defaultFederate->rtiamb->associateRegionForUpdates( *senderRegion, theObject, *ahs );
		delete ahs;
		failTestMissingException( "AttributeNotDefined",
		                          "associateForUpdates with invalid attribute" );
	}
	catch( RTI::AttributeNotDefined &attnd )
	{
		// success!
		delete ahs;
	}
	catch( RTI::Exception &e )
	{
		delete ahs;
		failTestWrongException( "AttributeNotDefined", e, 
		                        "associateForUpdates with invalid attribute" );
	}
}

//////////////////////////////////////////////////////
// TEST: testAssociateForUpdatesWithUnknownRegion() //
//////////////////////////////////////////////////////
void AssociateRegionTest::testAssociateForUpdatesWithUnknownRegion()
{
	try
	{
		defaultFederate->rtiamb->associateRegionForUpdates( *federateARegion, theObject, *allHandles );
		failTestMissingException( "RegionNotKnown", "associateForUpdates with unknown region" );
	}
	catch( RTI::RegionNotKnown &rnk )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "RegionNotKnown", e, "associateForUpdates with unknown region" );
	}
}

//////////////////////////////////////////////////////////////////
// TEST: testAssociateForUpdatesWithInvalidRegionForAttribute() //
//////////////////////////////////////////////////////////////////
void AssociateRegionTest::testAssociateForUpdatesWithInvalidRegionForAttribute()
{
	// create a new region of a space that can't be associated with any of the
	// attributes from ObjectRoot.A.B and then try and make the association
	RTI::Region *otherRegion = defaultFederate->quickCreateOtherRegion( 100, 300 );
	
	try
	{
		defaultFederate->rtiamb->associateRegionForUpdates( *otherRegion, theObject, *allHandles );
		delete otherRegion;
		failTestMissingException( "InvalidRegionContext", "associateForUpdates with invalid region" );
	}
	catch( RTI::InvalidRegionContext &irc )
	{
		// success!
		delete otherRegion;
	}
	catch( RTI::Exception &e )
	{
		delete otherRegion;
		failTestWrongException( "InvalidRegionContext", e, "associateForUpdates with invalid region" );
	}
}

//////////////////////////////////////////////////
// TEST: testAssociateForUpdatesWhenNotJoined() //
//////////////////////////////////////////////////
void AssociateRegionTest::testAssociateForUpdatesWhenNotJoined()
{
	defaultFederate->quickResign();
	try
	{
		defaultFederate->rtiamb->associateRegionForUpdates( *senderRegion, theObject, *allHandles );
		failTestMissingException( "FederateNotExecutionMember",
		                          "associateForUpdates when not joined" );
	}
	catch( RTI::FederateNotExecutionMember &fnem )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "associateForUpdates when not joined" );
	}
}

////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////// Unassociation Test Methods ////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

// unassociateRegionForUpdates( RTI::Region &theRegion, RTI::ObjectHandle theObject )
//     throw( RTI::ObjectNotKnown,
//            RTI::InvalidRegionContext,
//            RTI::RegionNotKnown,
//            RTI::FederateNotExecutionMember,
//            RTI::ConcurrentAccessAttempted,
//            RTI::SaveInProgress,
//            RTI::RestoreInProgress,
//            RTI::RTIinternalError )

/*
 * The provided object (registered by the defaultFederate) should no longer have attributes that
 * are associated with any regions. When a reflect is sent, all federates should receive it.
 */
void AssociateRegionTest::validateNotAssociated( RTI::ObjectHandle theObject )
{
	// send the update from the default federate
	defaultFederate->quickReflect( theObject, 6, "aa", "ab", "ac", "ba", "bb", "bc" );
	
	// make sure the reflection is received by federateA and that it contains the right values
	Test13Object *ng6Object = federateA->fedamb->waitForROUpdate( theObject );
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

	// make sure reflections sent to federateB
	ng6Object = federateB->fedamb->waitForROUpdate( theObject );
	result = strcmp( "aa", ng6Object->getAttribute(aaHandle) );
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

	// make sure reflections sent to federateC
	ng6Object = federateC->fedamb->waitForROUpdate( theObject );
	result = strcmp( "aa", ng6Object->getAttribute(aaHandle) );
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

///////////////////////////////////////////////
// TEST: (valid) testUnassociateForUpdates() //
///////////////////////////////////////////////
void AssociateRegionTest::testUnassociateForUpdates()
{
	// make the association and validate it, we'll test at the end to make sure that the
	// association still stands, regardless of the call below
	defaultFederate->quickAssociateWithRegion( theObject, senderRegion, 6, "aa", "ab", "ac",
	                                                                       "ba", "bb", "bc" );
	validateAssociated( theObject );

	// break the association
	try
	{
		defaultFederate->rtiamb->unassociateRegionForUpdates( *senderRegion, theObject );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception unassociating with region: %s", e._reason );
	}

	// was the association removed?
	validateNotAssociated( theObject );
}

//////////////////////////////////////////////////////
// TEST: (valid) testUnassociateWhenNotAssociated() //
//////////////////////////////////////////////////////
void AssociateRegionTest::testUnassociateWhenNotAssociated()
{
	try
	{
		defaultFederate->rtiamb->unassociateRegionForUpdates( *senderRegion, theObject );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while assocating with region not associated with: %s",
		          e._reason );
	}
}

////////////////////////////////////////////////////////////////////////////////
// TEST: (valid) testUnassociateForUpdatesWithInvalidRegionForAnyAttributes() //
////////////////////////////////////////////////////////////////////////////////
/*
 * This will have no lasting effect as regions of the other space can never be associated with
 * attributes of ObjectRootA.B in the first place. However, it should still be allowed to proceed
 * without causing an exception or changing any status inside the RTI.
 */
void AssociateRegionTest::testUnassociateForUpdatesWithInvalidRegionForAnyAttributes()
{
	// make the association and validate it, we'll test at the end to make sure that the
	// association still stands, regardless of the call below
	defaultFederate->quickAssociateWithRegion( theObject, senderRegion, 6, "aa", "ab", "ac",
	                                                                       "ba", "bb", "bc" );
	validateAssociated( theObject );

	// create a new region of a space that can't be associated with any of the
	// attributes from ObjectRoot.A.B and then try and make the association
	RTI::Region *otherRegion = defaultFederate->quickCreateOtherRegion( 100, 300 );
	
	try
	{
		defaultFederate->rtiamb->unassociateRegionForUpdates( *otherRegion, theObject );
		delete otherRegion;
	}
	catch( RTI::Exception &e )
	{
		delete otherRegion;
		failTest( "Unexpected exception unassociating with region that could never be associated: %s",
		          e._reason );
	}

	// do the association still stand? it should because the call above shouldn't change
	// the association with the senderRegion
	validateAssociated( theObject );
}

////////////////////////////////////////////////////////
// TEST: testUnassociateForUpdatesWithUnknownObject() //
////////////////////////////////////////////////////////
void AssociateRegionTest::testUnassociateForUpdatesWithUnknownObject()
{
	try
	{
		defaultFederate->rtiamb->unassociateRegionForUpdates( *senderRegion, 1000000 );
		failTestMissingException( "ObjectNotKnown",
		                          "unassociating for updates with unknown object" );
	}
	catch( RTI::ObjectNotKnown &onk )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "ObjectNotKnown", e,
		                        "unassociating for updates with unknown object" );
	}
}

////////////////////////////////////////////////////////
// TEST: testUnassociateForUpdatesWithUnknownRegion() //
////////////////////////////////////////////////////////
void AssociateRegionTest::testUnassociateForUpdatesWithUnknownRegion()
{
	try
	{
		defaultFederate->rtiamb->unassociateRegionForUpdates( *federateARegion, theObject );
		failTestMissingException( "RegionNotKnown", "unassociateForUpdates with unknown region" );
	}
	catch( RTI::RegionNotKnown &rnk )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "RegionNotKnown", e, "unassociateForUpdates with unknown region" );
	}
}

////////////////////////////////////////////////////
// TEST: testUnassociateForUpdatesWhenNotJoined() //
////////////////////////////////////////////////////
void AssociateRegionTest::testUnassociateForUpdatesWhenNotJoined()
{
	defaultFederate->quickResign();
	try
	{
		defaultFederate->rtiamb->unassociateRegionForUpdates( *senderRegion, theObject );
		failTestMissingException( "FederateNotExecutionMember",
		                          "unassociateForUpdates when not joined" );
	}
	catch( RTI::FederateNotExecutionMember &fnem )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "unassociateForUpdates when not joined" );
	}
}

