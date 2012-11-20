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
#include "AcquireOwnershipTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( AcquireOwnershipTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( AcquireOwnershipTest, "AcquireOwnershipTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( AcquireOwnershipTest, "ownershipManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
AcquireOwnershipTest::AcquireOwnershipTest()
{
	this->defaultFederate = new Test13Federate( "defaultFederate" );
	this->secondFederate = new Test13Federate( "secondFederate" );
	this->ahs = NULL;
	this->tag = new char[8];
	strcpy( this->tag, "eltaggo" );
}

AcquireOwnershipTest::~AcquireOwnershipTest()
{
	delete this->defaultFederate;
	delete this->secondFederate;
	delete this->tag;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void AcquireOwnershipTest::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->secondFederate->quickJoin();
	
	// cache some handle information
	this->ahs = NULL;
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

void AcquireOwnershipTest::tearDown()
{
	if( this->ahs != NULL )
		delete this->ahs;
	
	this->secondFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

/////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// Acquire If Available Test Methods /////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
// void attributeOwnershipAcquisitionIfAvailable(
//      ObjectHandle theObject,
//      const AttributeHandleSet& desiredAttributes ) // supplied C4
// throw ( ObjectNotKnown,
//         ObjectClassNotPublished,
//         AttributeNotDefined,
//         AttributeNotPublished,
//         FederateOwnsAttributes,
//         AttributeAlreadyBeingAcquired,
//         FederateNotExecutionMember,
//         ConcurrentAccessAttempted,
//         SaveInProgress,
//         RestoreInProgress,
//         RTIinternalError );

/////////////////////////////////////////////////////
// TEST: (valid) testAcquireOwnershipIfAvailable() //
/////////////////////////////////////////////////////
void AcquireOwnershipTest::testAcquireOwnershipIfAvailable()
{
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, ba, bb, bc );
		secondFederate->rtiamb->attributeOwnershipAcquisitionIfAvailable( theObject, *ahs );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while requesting ownership acquire (available): %s",
		          e._reason );
	}
	
	// wait for the acquisition notification
	secondFederate->fedamb->waitForOwnershipAcquistion( theObject, 3, ba, bb, bc );
	secondFederate->quickAssertOwnedBy( secondFederate->getFederateHandle(), theObject, 3, ba, bb, bc );
	secondFederate->quickAssertOwnedBy( defaultFederate->getFederateHandle(), theObject, 3, aa, ab, ac );
	
	// give the default federate some time to process callbacks before checking ownership
	defaultFederate->quickTick( 0.1, 1.0 );
	defaultFederate->quickAssertOwnedBy( secondFederate->getFederateHandle(), theObject, 3, ba, bb, bc );
	defaultFederate->quickAssertOwnedBy( defaultFederate->getFederateHandle(), theObject, 3, aa, ab, ac );
}

////////////////////////////////////////////////////////////////////
// TEST: (valid) testAcquireOwnershipIfAvailableWhenUnavailable() //
////////////////////////////////////////////////////////////////////
void AcquireOwnershipTest::testAcquireOwnershipIfAvailableWhenUnavailable()
{
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, aa, ab, ac );
		secondFederate->rtiamb->attributeOwnershipAcquisitionIfAvailable( theObject, *ahs );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while requesting ownership acquire (available): %s",
		          e._reason );
	}
	
	// wait for the acquisition notification
	secondFederate->fedamb->waitForOwnershipUnavailable( theObject, 3, aa, ab, ac );
	secondFederate->quickAssertOwnedBy( Test13Federate::OWNER_UNOWNED, theObject, 3, ba, bb, bc );
	secondFederate->quickAssertOwnedBy( defaultFederate->getFederateHandle(), theObject, 3, aa, ab, ac );
	
	// give the default federate some time to process callbacks before checking ownership
	defaultFederate->quickTick( 0.1, 1.0 );
	secondFederate->quickAssertOwnedBy( Test13Federate::OWNER_UNOWNED, theObject, 3, ba, bb, bc );
	defaultFederate->quickAssertOwnedBy( defaultFederate->getFederateHandle(), theObject, 3, aa, ab, ac );
}

//////////////////////////////////////////////////////////////
// TEST: testAcquireOwnershipIfAvailableWithUnknownObject() //
//////////////////////////////////////////////////////////////
void AcquireOwnershipTest::testAcquireOwnershipIfAvailableWithUnknownObject()
{
	// register a second object but one that isn't discovered by the second federate
	RTI::ObjectHandle secondObject = defaultFederate->quickRegister( "ObjectRoot.A.B" );
	
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, ba, bb, bc );
		secondFederate->rtiamb->attributeOwnershipAcquisitionIfAvailable( secondObject, *ahs );
		failTestMissingException( "ObjectNotKnown",
		                          "requesting attribute ownership with unknown object" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown",
		                        e,
		                        "requesting attribute ownership with unknown object" );
	}
}

//////////////////////////////////////////////////////////////
// TEST: testAcquireOwnershipIfAvailableWithInvalidObject() //
//////////////////////////////////////////////////////////////
void AcquireOwnershipTest::testAcquireOwnershipIfAvailableWithInvalidObject()
{
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, ba, bb, bc );
		secondFederate->rtiamb->attributeOwnershipAcquisitionIfAvailable( 100000, *ahs );
		failTestMissingException( "ObjectNotKnown",
		                          "requesting attribute ownership with invalid object" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown",
		                        e,
		                        "requesting attribute ownership with invalid object" );
	}
}

/////////////////////////////////////////////////////////////////
// TEST: testAcquireOwnershipIfAvailableWithInvalidAttribute() //
/////////////////////////////////////////////////////////////////
void AcquireOwnershipTest::testAcquireOwnershipIfAvailableWithInvalidAttribute()
{
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, ba, bb, 1000000 );
		secondFederate->rtiamb->attributeOwnershipAcquisitionIfAvailable( theObject, *ahs );
		failTestMissingException( "AttributeNotDefined",
		                          "requesting attribute ownership with invalid attribute" );
	}
	catch( RTI::AttributeNotDefined& anotd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeNotDefined",
		                        e,
		                        "requesting attribute ownership with invalid attribute" );
	}
}

/////////////////////////////////////////////////////////////////
// TEST: testAcquireOwnershipIfAvailableWithUnpublishedClass() //
/////////////////////////////////////////////////////////////////
void AcquireOwnershipTest::testAcquireOwnershipIfAvailableWithUnpublishedClass()
{
	// unpublish ObjectRoot.A.B
	secondFederate->quickUnpublishOC( "ObjectRoot.A.B" );

	try
	{
		this->ahs = secondFederate->populatedAHS( 3, ba, bb, bc );
		secondFederate->rtiamb->attributeOwnershipAcquisitionIfAvailable( theObject, *ahs );
		failTestMissingException( "ObjectClassNotPublished",
		                          "requesting attribute ownership with unpublished class" );
	}
	catch( RTI::ObjectClassNotPublished& ocnp )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectClassNotPublished",
		                        e,
		                        "requesting attribute ownership with unpublished class" );
	}
}

/////////////////////////////////////////////////////////////////////
// TEST: testAcquireOwnershipIfAvailableWithUnpublishedAttribute() //
/////////////////////////////////////////////////////////////////////
void AcquireOwnershipTest::testAcquireOwnershipIfAvailableWithUnpublishedAttribute()
{
	// republish ObjectRoot.A.B, but with different set, essentially unpublishing some atts
	secondFederate->quickPublish( "ObjectRoot.A.B", 4, "aa", "ab", "ac", "ba" );

	try
	{
		this->ahs = secondFederate->populatedAHS( 3, ba, bb, bc );
		secondFederate->rtiamb->attributeOwnershipAcquisitionIfAvailable( theObject, *ahs );
		failTestMissingException( "AttributeNotPublished",
		                          "requesting attribute ownership with unpublished attribute" );
	}
	catch( RTI::AttributeNotPublished& anp )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeNotPublished",
		                        e,
		                        "requesting attribute ownership with unpublished attribute" );
	}
}

//////////////////////////////////////////////////////////////////////
// TEST: testAcquireOwnershipIfAvailableWithAlreadyOwnedAttribute() //
//////////////////////////////////////////////////////////////////////
void AcquireOwnershipTest::testAcquireOwnershipIfAvailableWithAlreadyOwnedAttribute()
{
	// acquire the unowned attributes
	secondFederate->quickAcquireIfAvailableRequest( theObject, 3, ba, bb, bc );
	secondFederate->fedamb->waitForOwnershipAcquistion( theObject, 3, ba, bb, bc );
	
	// try and re-acquire the atts we now own
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, ba, bb, bc );
		secondFederate->rtiamb->attributeOwnershipAcquisitionIfAvailable( theObject, *ahs );
		failTestMissingException( "FederateOwnsAttributes",
		                          "requesting attribute ownership with attributes we already own" );
	}
	catch( RTI::FederateOwnsAttributes& foa )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateOwnsAttributes",
		                        e,
		                        "requesting attribute ownership with attributes we already own" );
	}
}

//////////////////////////////////////////////////////////
// TEST: testAcquireOwnershipIfAvailableWhenNotJoined() //
//////////////////////////////////////////////////////////
void AcquireOwnershipTest::testAcquireOwnershipIfAvailableWhenNotJoined()
{
	secondFederate->quickResign();
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, ba, bb, bc );
		secondFederate->rtiamb->attributeOwnershipAcquisitionIfAvailable( theObject, *ahs );
		failTestMissingException( "FederateNotExecutionMember",
		                          "requesting attribute ownership when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember",
		                        e,
		                        "requesting attribute ownership when not joined" );
	}
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// General Acquire Test Methods ///////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
//void attributeOwnershipAcquisition (
//     ObjectHandle theObject,                      // supplied C1
//     const AttributeHandleSet& desiredAttributes, // supplied C4
//     const char *theTag )                         // supplied C4
//  throw ( ObjectNotKnown,
//          ObjectClassNotPublished,
//          AttributeNotDefined,
//          AttributeNotPublished,
//          FederateOwnsAttributes,
//          FederateNotExecutionMember,
//          ConcurrentAccessAttempted,
//          SaveInProgress,
//          RestoreInProgress,
//          RTIinternalError );

/////////////////////////////////////////////////////
// TEST: (valid) testAcquireOwnershipIfAvailable() //
/////////////////////////////////////////////////////
void AcquireOwnershipTest::testAcquireOwnership()
{
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, aa, ab, ac );
		secondFederate->rtiamb->attributeOwnershipAcquisition( theObject, *ahs, "NA" );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while requesting ownership acquire: %s", e._reason );
	}
	
	// wait for the acquisition notification
	defaultFederate->fedamb->waitForOwnershipRequest( theObject, 3, aa, ab, ac );
}

///////////////////////////////////////////////////
// TEST: testAcquireOwnershipWithUnknownObject() //
///////////////////////////////////////////////////
void AcquireOwnershipTest::testAcquireOwnershipWithUnknownObject()
{
	// register a second object but one that isn't discovered by the second federate
	RTI::ObjectHandle secondObject = defaultFederate->quickRegister( "ObjectRoot.A.B" );
	
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, aa, ab, ac );
		secondFederate->rtiamb->attributeOwnershipAcquisition( secondObject, *ahs, "NA" );
		failTestMissingException( "ObjectNotKnown",
		                          "requesting attribute ownership with unknown object" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown",
		                        e,
		                        "requesting attribute ownership with unknown object" );
	}
}

///////////////////////////////////////////////////
// TEST: testAcquireOwnershipWithInvalidObject() //
///////////////////////////////////////////////////
void AcquireOwnershipTest::testAcquireOwnershipWithInvalidObject()
{
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, aa, ab, ac );
		secondFederate->rtiamb->attributeOwnershipAcquisition( 100000, *ahs, "NA" );
		failTestMissingException( "ObjectNotKnown",
		                          "requesting attribute ownership with invalid object" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown",
		                        e,
		                        "requesting attribute ownership with invalid object" );
	}
}

//////////////////////////////////////////////////////
// TEST: testAcquireOwnershipWithInvalidAttribute() //
//////////////////////////////////////////////////////
void AcquireOwnershipTest::testAcquireOwnershipWithInvalidAttribute()
{
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, aa, ab, 1000000 );
		secondFederate->rtiamb->attributeOwnershipAcquisition( theObject, *ahs, "NA" );
		failTestMissingException( "AttributeNotDefined",
		                          "requesting attribute ownership with invalid attribute" );
	}
	catch( RTI::AttributeNotDefined& anotd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeNotDefined",
		                        e,
		                        "requesting attribute ownership with invalid attribute" );
	}
}

//////////////////////////////////////////////////////////
// TEST: testAcquireOwnershipWithUnpublishedAttribute() //
//////////////////////////////////////////////////////////
void AcquireOwnershipTest::testAcquireOwnershipWithUnpublishedAttribute()
{
	// republish ObjectRoot.A.B, but with different set, essentially unpublishing some atts
	secondFederate->quickPublish( "ObjectRoot.A.B", 4, "aa", "ab", "ac", "ba" );

	try
	{
		this->ahs = secondFederate->populatedAHS( 3, ba, bb, bc );
		secondFederate->rtiamb->attributeOwnershipAcquisition( theObject, *ahs, this->tag );
		failTestMissingException( "AttributeNotPublished",
		                          "requesting attribute ownership with unpublished attribute" );
	}
	catch( RTI::AttributeNotPublished& anp )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeNotPublished",
		                        e,
		                        "requesting attribute ownership with unpublished attribute" );
	}
}

//////////////////////////////////////////////////////
// TEST: testAcquireOwnershipWithUnpublishedClass() //
//////////////////////////////////////////////////////
void AcquireOwnershipTest::testAcquireOwnershipWithUnpublishedClass()
{
	// unpublish ObjectRoot.A.B
	secondFederate->quickUnpublishOC( "ObjectRoot.A.B" );

	try
	{
		this->ahs = secondFederate->populatedAHS( 3, ba, bb, bc );
		secondFederate->rtiamb->attributeOwnershipAcquisition( theObject, *ahs, this->tag );
		failTestMissingException( "ObjectClassNotPublished",
		                          "requesting attribute ownership with unpublished class" );
	}
	catch( RTI::ObjectClassNotPublished& ocnp )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectClassNotPublished",
		                        e,
		                        "requesting attribute ownership with unpublished class" );
	}
}

///////////////////////////////////////////////////////////
// TEST: testAcquireOwnershipWithAlreadyOwnedAttribute() //
///////////////////////////////////////////////////////////
void AcquireOwnershipTest::testAcquireOwnershipWithAlreadyOwnedAttribute()
{
	// acquire the unowned attributes
	secondFederate->quickAcquireIfAvailableRequest( theObject, 3, ba, bb, bc );
	secondFederate->fedamb->waitForOwnershipAcquistion( theObject, 3, ba, bb, bc );
	
	// try and re-acquire the atts we now own
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, ba, bb, bc );
		secondFederate->rtiamb->attributeOwnershipAcquisition( theObject, *ahs, this->tag );
		failTestMissingException( "FederateOwnsAttributes",
		                          "requesting attribute ownership with attributes we already own" );
	}
	catch( RTI::FederateOwnsAttributes& foa )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateOwnsAttributes",
		                        e,
		                        "requesting attribute ownership with attributes we already own" );
	}
}

///////////////////////////////////////////////
// TEST: testAcquireOwnershipWhenNotJoined() //
///////////////////////////////////////////////
void AcquireOwnershipTest::testAcquireOwnershipWhenNotJoined()
{
	secondFederate->quickResign();
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, ba, bb, bc );
		secondFederate->rtiamb->attributeOwnershipAcquisition( theObject, *ahs, this->tag );
		failTestMissingException( "FederateNotExecutionMember",
		                          "requesting attribute ownership when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember",
		                        e,
		                        "requesting attribute ownership when not joined" );
	}
}


/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Release Response Test Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
// AttributeHandleSet* attributeOwnershipReleaseResponse(          // returned C6
//                       ObjectHandle theObject,                   // supplied C1
//                       const AttributeHandleSet& theAttributes ) // supplied C4
//     throw ( ObjectNotKnown,
//             AttributeNotDefined,
//             AttributeNotOwned,
//             FederateWasNotAskedToReleaseAttribute,
//             FederateNotExecutionMember,
//             ConcurrentAccessAttempted,
//             SaveInProgress,
//             RestoreInProgress,
//             RTIinternalError );

///////////////////////////////////////////////////////
// TEST: (valid) testAcquireOwnershipWhenNotJoined() //
///////////////////////////////////////////////////////
void AcquireOwnershipTest::testReleaseResponse()
{
	// request the release of attributes owned by the default federate
	secondFederate->quickAcquireRequest( theObject, 3, aa, ab, ac );
	defaultFederate->fedamb->waitForOwnershipRequest( theObject, 3, aa, ab, ac );
	
	// respond with a release response
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->attributeOwnershipReleaseResponse( theObject, *ahs );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while releasing attributes via a release response: %s",
		          e._reason );
	}

	// wait for them to be picked up in the second federate
	secondFederate->fedamb->waitForOwnershipAcquistion( theObject, 3, aa, ab, ac );
	secondFederate->quickAssertOwnedBy( secondFederate->getFederateHandle(), theObject, 3, aa, ab, ac );
	
	// give the default federate some time to process callbacks before checking ownership
	defaultFederate->quickTick( 0.1, 1.0 );
	defaultFederate->quickAssertOwnedBy( secondFederate->getFederateHandle(), theObject, 3, aa, ab, ac );
}


//////////////////////////////////////////////////
// TEST: testReleaseResponseWithUnknownObject() //
//////////////////////////////////////////////////
void AcquireOwnershipTest::testReleaseResponseWithUnknownObject()
{
	// register an object in the second federate that is not discovered by the first, and
	// thus is unknown to it
	RTI::ObjectHandle secondObject = secondFederate->quickRegister( "ObjectRoot.A.B" );

	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->attributeOwnershipReleaseResponse( secondObject, *ahs );
		failTestMissingException( "ObjectNotKnown",
		                          "calling attribute release response with undiscovered object" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown", e,
		                        "calling attribute release response with undiscovered object" );
	}	
}

//////////////////////////////////////////////////
// TEST: testReleaseResponseWithInvalidObject() //
//////////////////////////////////////////////////
void AcquireOwnershipTest::testReleaseResponseWithInvalidObject()
{
	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->attributeOwnershipReleaseResponse( 1000000, *ahs );
		failTestMissingException( "ObjectNotKnown",
		                          "calling attribute release response with invalid object handle" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown", e,
		                        "calling attribute release response with invalid object handle" );
	}
}

/////////////////////////////////////////////////////
// TEST: testReleaseResponseWithInvalidAttribute() //
/////////////////////////////////////////////////////
void AcquireOwnershipTest::testReleaseResponseWithInvalidAttribute()
{
	try
	{
		this->ahs = defaultFederate->populatedAHS( 1, 100000 );
		defaultFederate->rtiamb->attributeOwnershipReleaseResponse( theObject, *ahs );
		failTestMissingException( "AttributeNotDefined",
		                          "calling attribute release response with undefined attribute" );
	}
	catch( RTI::AttributeNotDefined& anotd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeNotDefined", e,
		                        "calling attribute release response with undefined attribute" );
	}
}

/////////////////////////////////////////////////////
// TEST: testReleaseResponseWithUnownedAttribute() //
/////////////////////////////////////////////////////
void AcquireOwnershipTest::testReleaseResponseWithUnownedAttribute()
{
	// create a second object, owned by the second federate so that we can play with it
	// we can't just use an attribute we don't publish (and thus don't own) because that'll
	// throw an AttributeNotPublished rather than the AttributeNotOwned we want
	RTI::ObjectHandle secondObject = secondFederate->quickRegister( "ObjectRoot.A.B" );
	defaultFederate->quickSubscribe( "ObjectRoot.A.B", 3, "aa", "ab", "ac" );
	defaultFederate->fedamb->waitForDiscovery( secondObject );
	
	try
	{
		this->ahs = defaultFederate->populatedAHS( 1, aa );
		defaultFederate->rtiamb->attributeOwnershipReleaseResponse( secondObject, *ahs );
		failTestMissingException( "AttributeNotOwned",
		                          "calling attribute release response with unowned attribute" );
	}
	catch( RTI::AttributeNotOwned& ano )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeNotOwned", e,
		                        "calling attribute release response with unowned attribute" );
	}
}

//////////////////////////////////////////////////////
// TEST: testReleaseResponseWhenNotAskedToRelease() //
//////////////////////////////////////////////////////
void AcquireOwnershipTest::testReleaseResponseWhenNotAskedToRelease()
{
	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->attributeOwnershipReleaseResponse( theObject, *ahs );
		failTestMissingException( "FederateWasNotAskedToReleaseAttribute",
		                          "calling attribute release response with no outstanding request" );
	}
	catch( RTI::FederateWasNotAskedToReleaseAttribute& fwnatra )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateWasNotAskedToReleaseAttribute", e,
		                        "calling attribute release response with no outstanding request" );
	}
}

//////////////////////////////////////////////
// TEST: testReleaseResponseWhenNotJoined() //
//////////////////////////////////////////////
void AcquireOwnershipTest::testReleaseResponseWhenNotJoined()
{
	defaultFederate->quickResign();
	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->attributeOwnershipReleaseResponse( theObject, *ahs );
		failTestMissingException( "FederateNotExecutionMember",
		                          "calling attribute release response when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e, 
		                        "calling attribute release response when not joined" );
	}
}

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////// Cancel Acquisition Test Methods //////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
// void cancelAttributeOwnershipAcquisition( ObjectHandle theObject,                // supplied C1
//                                           const AttributeHandleSet& attributes ) // supplied C4
//      throw ( ObjectNotKnown,
//              AttributeNotDefined,
//              AttributeAlreadyOwned,
//              AttributeAcquisitionWasNotRequested,
//              FederateNotExecutionMember,
//              ConcurrentAccessAttempted,
//              SaveInProgress,
//              RestoreInProgress,
//              RTIinternalError );

////////////////////////////////////////////////////
// TEST: (valid) testCancelOwnershipAcquisition() //
////////////////////////////////////////////////////
void AcquireOwnershipTest::testCancelOwnershipAcquisition()
{
	// request a transfer
	secondFederate->quickAcquireRequest( theObject, 3, aa, ab, ac );
	defaultFederate->fedamb->waitForOwnershipRequest( theObject, 3, aa, ab, ac );
	
	// cancel the transfer
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, aa, ab, ac );
		secondFederate->rtiamb->cancelAttributeOwnershipAcquisition( theObject, *ahs );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while cancelling attribute ownership acquisition: %s",
		          e._reason );
	}
	
	secondFederate->fedamb->waitForOwnershipCancelConfirmation( theObject, 3, aa, ab, ac );
}

/////////////////////////////////////////////////////////////
// TEST: testCancelOwnershipAcquisitionWithInvalidObject() //
/////////////////////////////////////////////////////////////
void AcquireOwnershipTest::testCancelOwnershipAcquisitionWithInvalidObject()
{
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, aa, ab, ac );
		secondFederate->rtiamb->cancelAttributeOwnershipAcquisition( 1000000, *ahs );
		failTestMissingException( "ObjectNotKnown",
		                          "cancelling attribute acquisition with invalid object handle" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown", e,
		                        "cancelling attribute acquisition with invalid object handle" );
	}
}

/////////////////////////////////////////////////////////////
// TEST: testCancelOwnershipAcquisitionWithUnknownObject() //
/////////////////////////////////////////////////////////////
void AcquireOwnershipTest::testCancelOwnershipAcquisitionWithUnknownObject()
{
	int secondObject = defaultFederate->quickRegister( "ObjectRoot.A.B" );

	try
	{
		this->ahs = secondFederate->populatedAHS( 3, aa, ab, ac );
		secondFederate->rtiamb->cancelAttributeOwnershipAcquisition( secondObject, *ahs );
		failTestMissingException( "ObjectNotKnown",
		                          "cancelling attribute acquisition with unknown object handle" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown", e,
		                        "cancelling attribute acquisition with unknown object handle" );
	}
}

////////////////////////////////////////////////////////////////
// TEST: testCancelOwnershipAcquisitionWithInvalidAttribute() //
////////////////////////////////////////////////////////////////
void AcquireOwnershipTest::testCancelOwnershipAcquisitionWithInvalidAttribute()
{
	try
	{
		this->ahs = secondFederate->populatedAHS( 1, 100000 );
		secondFederate->rtiamb->cancelAttributeOwnershipAcquisition( theObject, *ahs );
		failTestMissingException( "AttributeNotDefined",
		                          "cancelling attribute acquisition with undefined attribute" );
	}
	catch( RTI::AttributeNotDefined& anotd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeNotDefined", e,
		                        "cancelling attribute acquisition with undefined attribute" );
	}
}

//////////////////////////////////////////////////////////////////////////////
// TEST: testCancelOwnershipAcquisitionWithOwnershipWhereAcquiredFinished() //
//////////////////////////////////////////////////////////////////////////////
void AcquireOwnershipTest::testCancelOwnershipAcquisitionWithOwnershipWhereAcquiredFinished()
{
	secondFederate->quickAcquireRequest( theObject, 3, aa, ab, ac );
	defaultFederate->fedamb->waitForOwnershipRequest( theObject, 3, aa, ab, ac );
	defaultFederate->quickReleaseResponse( theObject, 3, aa, ab, ac );
	
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, aa, ab, ac );
		secondFederate->rtiamb->cancelAttributeOwnershipAcquisition( theObject, *ahs );
		failTestMissingException( "AttributeAlreadyOwned",
		                          "cancelling attribute acquisition with completed transfer" );
	}
	catch( RTI::AttributeAlreadyOwned& aao )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeAlreadyOwned", e,
		                        "cancelling attribute acquisition with completed transfer" );
	}
}

/////////////////////////////////////////////////////////////////////
// TEST: testCancelOwnershipAcquisitionWithUnrequestedAttributes() //
/////////////////////////////////////////////////////////////////////
void AcquireOwnershipTest::testCancelOwnershipAcquisitionWithUnrequestedAttributes()
{
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, aa, ab, ac );
		secondFederate->rtiamb->cancelAttributeOwnershipAcquisition( theObject, *ahs );
		failTestMissingException( "AttributeAcquisitionWasNotRequested",
		                          "cancelling attribute acquisition for unrequested attributes" );
	}
	catch( RTI::AttributeAcquisitionWasNotRequested& aao )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeAcquisitionWasNotRequested", e,
		                        "cancelling attribute acquisition for unrequested attributes" );
	}
}

/////////////////////////////////////////////////////////
// TEST: testCancelOwnershipAcquisitionWhenNotJoined() //
/////////////////////////////////////////////////////////
void AcquireOwnershipTest::testCancelOwnershipAcquisitionWhenNotJoined()
{
	secondFederate->quickResign();
	try
	{
		this->ahs = secondFederate->populatedAHS( 3, aa, ab, ac );
		secondFederate->rtiamb->cancelAttributeOwnershipAcquisition( theObject, *ahs );
		failTestMissingException( "FederateNotExecutionMember",
		                          "cancelling attribute acquisition when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e, 
		                        "cancelling attribute acquisition when not joined" );
	}
}
