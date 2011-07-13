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
#include "DivestOwnershipTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( DivestOwnershipTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( DivestOwnershipTest, "DivestOwnershipTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( DivestOwnershipTest, "ownershipManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
DivestOwnershipTest::DivestOwnershipTest()
{
	this->defaultFederate = new TestNG6Federate( "defaultFederate" );
	this->secondFederate = new TestNG6Federate( "secondFederate" );
	this->ahs = NULL;
	this->tag = new char[8];
	strcpy( this->tag, "eltaggo" );
}

DivestOwnershipTest::~DivestOwnershipTest()
{
	delete this->defaultFederate;
	delete this->secondFederate;
	delete this->tag;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void DivestOwnershipTest::setUp()
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

void DivestOwnershipTest::tearDown()
{
	if( this->ahs != NULL )
		delete this->ahs;
	
	this->secondFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

/////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// Unconditional Divest Test Methods /////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
// void unconditionalAttributeOwnershipDivestiture (
//        ObjectHandle theObject,                   // supplied C1
//        const AttributeHandleSet& theAttributes ) // supplied C4
//   throw ( ObjectNotKnown,
//           AttributeNotDefined,
//           AttributeNotOwned,
//           FederateNotExecutionMember,
//           ConcurrentAccessAttempted,
//           SaveInProgress,
//           RestoreInProgress,
//           RTIinternalError );

/////////////////////////////////////////////
// TEST: (valid) testUnconditionalDivest() //
/////////////////////////////////////////////
void DivestOwnershipTest::testUnconditionalDivest()
{
	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->unconditionalAttributeOwnershipDivestiture( theObject, *ahs );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while starting unconditional ownership release: %s",
		          e._reason );
	}
	
	// wait for the acquisition notification
	secondFederate->fedamb->waitForOwnershipOffered( theObject, 3, aa, ab, ac );
}

//////////////////////////////////////////////////////
// TEST: testUnconditionalDivestWithInvalidObject() //
//////////////////////////////////////////////////////
void DivestOwnershipTest::testUnconditionalDivestWithInvalidObject()
{
	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->unconditionalAttributeOwnershipDivestiture( 100000, *ahs );
		failTestMissingException( "ObjectNotKnown",
		                          "unconditional ownership release with unknown object" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown", e,
		                        "unconditional ownership release with unknown object" );
	}
}

//////////////////////////////////////////////////////
// TEST: testUnconditionalDivestWithUnknownObject() //
//////////////////////////////////////////////////////
void DivestOwnershipTest::testUnconditionalDivestWithUnknownObject()
{
	// create a second object that isn't discovered by the original federate, so it's a valid
	// handle in the federation, just not known by the seocnd federate
	RTI::ObjectHandle secondObject = secondFederate->quickRegister( "ObjectRoot.A.B" );
	
	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->unconditionalAttributeOwnershipDivestiture( secondObject, *ahs );
		failTestMissingException( "ObjectNotKnown",
		                          "unconditional ownership release with undiscovered object" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown", e,
		                        "unconditional ownership release with undiscovered object" );
	}
}

/////////////////////////////////////////////////////////
// TEST: testUnconditionalDivestWithInvalidAttribute() //
/////////////////////////////////////////////////////////
void DivestOwnershipTest::testUnconditionalDivestWithInvalidAttribute()
{
	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, 10000 );
		defaultFederate->rtiamb->unconditionalAttributeOwnershipDivestiture( theObject, *ahs );
		failTestMissingException( "AttributeNotDefined",
		                          "unconditional ownership release with invalid attribute" );
	}
	catch( RTI::AttributeNotDefined& anotd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeNotDefined", e,
		                        "unconditional ownership release with invalid attribute" );
	}	
}

/////////////////////////////////////////////////////////
// TEST: testUnconditionalDivestWithUnownedAttribute() //
/////////////////////////////////////////////////////////
void DivestOwnershipTest::testUnconditionalDivestWithUnownedAttribute()
{
	try
	{
		this->ahs = defaultFederate->populatedAHS( 1, ba );
		defaultFederate->rtiamb->unconditionalAttributeOwnershipDivestiture( theObject, *ahs );
		failTestMissingException( "AttributeNotOwned",
		                          "unconditional ownership release with unowned attribute" );
	}
	catch( RTI::AttributeNotOwned& ano )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeNotOwned", e,
		                        "unconditional ownership release with unowned attribute" );
	}
}

//////////////////////////////////////////////////
// TEST: testUnconditionalDivestWhenNotJoined() //
//////////////////////////////////////////////////
void DivestOwnershipTest::testUnconditionalDivestWhenNotJoined()
{
	defaultFederate->quickResign();

	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->unconditionalAttributeOwnershipDivestiture( theObject, *ahs );
		failTestMissingException( "FederateNotExecutionMember",
		                          "unconditional ownership release when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "unconditional ownership release when not joined" );
	}
}

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////// Negotiated Divest Test Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
// void negotiatedAttributeOwnershipDivestiture (
//        ObjectHandle theObject,                  // supplied C1
//        const AttributeHandleSet& theAttributes, // supplied C4
//        const char *theTag)                      // supplied C4
//   throw( ObjectNotKnown,
//          AttributeNotDefined,
//          AttributeNotOwned,
//          AttributeAlreadyBeingDivested,
//          FederateNotExecutionMember,
//          ConcurrentAccessAttempted,
//          SaveInProgress,
//          RestoreInProgress,
//          RTIinternalError );

//////////////////////////////////////////
// TEST: (valid) testNegotiatedDivest() //
//////////////////////////////////////////
void DivestOwnershipTest::testNegotiatedDivest()
{
	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->negotiatedAttributeOwnershipDivestiture( theObject, *ahs, "NA" );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while starting negotiated ownership release: %s",
		          e._reason );
	}
	
	// wait for the acquisition notification
	secondFederate->fedamb->waitForOwnershipOffered( theObject, 3, aa, ab, ac );
	secondFederate->quickAcquireRequest( theObject, 3, aa, ab, ac );
	defaultFederate->quickTick();
	secondFederate->fedamb->waitForOwnershipAcquistion( theObject, 3, aa, ab, ac );
	defaultFederate->fedamb->waitForOwnershipDivested( theObject, 3, aa, ab, ac );
}

///////////////////////////////////////////////////
// TEST: testNegotiatedDivestWithInvalidObject() //
///////////////////////////////////////////////////
void DivestOwnershipTest::testNegotiatedDivestWithInvalidObject()
{
	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->negotiatedAttributeOwnershipDivestiture( 100000, *ahs, tag );
		failTestMissingException( "ObjectNotKnown",
		                          "negotiated ownership release with invalid object" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown", e,
		                        "negotiated ownership release with invalid object" );
	}
}

///////////////////////////////////////////////////
// TEST: testNegotiatedDivestWithUnknownObject() //
///////////////////////////////////////////////////
void DivestOwnershipTest::testNegotiatedDivestWithUnknownObject()
{
	// create a second object that isn't discovered by the original federate, so it's a valid
	// handle in the federation, just not known by the seocnd federate
	RTI::ObjectHandle secondObject = secondFederate->quickRegister( "ObjectRoot.A.B" );
	
	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->negotiatedAttributeOwnershipDivestiture( secondObject, *ahs, tag );
		failTestMissingException( "ObjectNotKnown",
		                          "negotiated ownership release with undiscovered object" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown", e,
		                        "negotiated ownership release with undiscovered object" );
	}
}

//////////////////////////////////////////////////////
// TEST: testNegotiatedDivestWithInvalidAttribute() //
//////////////////////////////////////////////////////
void DivestOwnershipTest::testNegotiatedDivestWithInvalidAttribute()
{
	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, 10000 );
		defaultFederate->rtiamb->negotiatedAttributeOwnershipDivestiture( theObject, *ahs, tag );
		failTestMissingException( "AttributeNotDefined",
		                          "negotiated ownership release with invalid attribute" );
	}
	catch( RTI::AttributeNotDefined& anotd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeNotDefined", e,
		                        "negotiated ownership release with invalid attribute" );
	}
}

/////////////////////////////////////////////////////////
// TEST: testUnconditionalDivestWithUnownedAttribute() //
/////////////////////////////////////////////////////////
void DivestOwnershipTest::testNegotiatedDivestWithUnownedAttribute()
{
	try
	{
		this->ahs = defaultFederate->populatedAHS( 1, ba );
		defaultFederate->rtiamb->negotiatedAttributeOwnershipDivestiture( theObject, *ahs, tag );
		failTestMissingException( "AttributeNotOwned",
		                          "negotiated ownership release with unowned attribute" );
	}
	catch( RTI::AttributeNotOwned& ano )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeNotOwned", e,
		                        "negotiated ownership release with unowned attribute" );
	}
}

///////////////////////////////////////////////////////////////////
// TEST: testNegotiatedDivestWithAttributeAlreadyBeingDivested() //
///////////////////////////////////////////////////////////////////
void DivestOwnershipTest::testNegotiatedDivestWithAttributeAlreadyBeingDivested()
{
	// kick off the divest process
	defaultFederate->quickNegotiatedRelease( theObject, 3, aa, ab, ac );
	
	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->negotiatedAttributeOwnershipDivestiture( theObject, *ahs, tag );
		failTestMissingException( "AttributeAlreadyBeingDivested",
		                          "negotiated ownership release with unowned attribute" );
	}
	catch( RTI::AttributeAlreadyBeingDivested& aabd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeAlreadyBeingDivested", e,
		                        "negotiated ownership release with unowned attribute" );
	}
}

///////////////////////////////////////////////
// TEST: testNegotiatedDivestWhenNotJoined() //
///////////////////////////////////////////////
void DivestOwnershipTest::testNegotiatedDivestWhenNotJoined()
{
	defaultFederate->quickResign();

	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->negotiatedAttributeOwnershipDivestiture( theObject, *ahs, tag );
		failTestMissingException( "FederateNotExecutionMember",
		                          "negotiated ownership release when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "negotiated ownership release when not joined" );
	}
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////// Cancel Negoriated Divest Test Methods ///////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
// void cancelNegotiatedAttributeOwnershipDivestiture (
//          ObjectHandle theObject,                   // supplied C1
//          const AttributeHandleSet& theAttributes ) // supplied C4
//      throw( ObjectNotKnown,
//             AttributeNotDefined,
//             AttributeNotOwned,
//             AttributeDivestitureWasNotRequested,
//             FederateNotExecutionMember,
//             ConcurrentAccessAttempted,
//             SaveInProgress,
//             RestoreInProgress,
//             RTIinternalError );

////////////////////////////////////////////////
// TEST: (valid) testCancelNegotiatedDivest() //
////////////////////////////////////////////////
void DivestOwnershipTest::testCancelNegotiatedDivest()
{
	// kick off a divest
	defaultFederate->quickNegotiatedRelease( theObject, 3, aa, ab, ac );
	secondFederate->fedamb->waitForOwnershipOffered( theObject, 3, aa, ab, ac );

	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->cancelNegotiatedAttributeOwnershipDivestiture( theObject, *ahs );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while cancelling negotiated ownership release: %s",
		          e._reason );
	}

	// try and obtain the attributes in the second federate
	secondFederate->quickAcquireRequest( theObject, 3, aa, ab, ac );
	// if the divest was outstanding, defaultFederate would hand it off right away, but
	// as it isn't, this request should cause a callback requesting the default fed to release
	defaultFederate->fedamb->waitForOwnershipRequest( theObject, 3, aa, ab, ac );
}

/////////////////////////////////////////////////////////
// TEST: testCancelNegotiatedDivestWithInvalidObject() //
/////////////////////////////////////////////////////////
void DivestOwnershipTest::testCancelNegotiatedDivestWithInvalidObject()
{
	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->cancelNegotiatedAttributeOwnershipDivestiture( 100000, *ahs );
		failTestMissingException( "ObjectNotKnown",
		                          "cancel negotiated ownership divest with invalid object" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown", e,
		                        "cancel negotiated ownership divest with invalid object" );
	}
}

/////////////////////////////////////////////////////////
// TEST: testCancelNegotiatedDivestWithUnknownObject() //
/////////////////////////////////////////////////////////
void DivestOwnershipTest::testCancelNegotiatedDivestWithUnknownObject()
{
	// create a second object that isn't discovered by the original federate, so it's a valid
	// handle in the federation, just not known by the seocnd federate
	RTI::ObjectHandle secondObject = secondFederate->quickRegister( "ObjectRoot.A.B" );
	
	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->cancelNegotiatedAttributeOwnershipDivestiture( secondObject, *ahs );
		failTestMissingException( "ObjectNotKnown",
		                          "cancel negotiated ownership divest with unknown object" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown", e,
		                        "cancel negotiated ownership divest with unknown object" );
	}
}

////////////////////////////////////////////////////////////
// TEST: testCancelNegotiatedDivestWithInvalidAttribute() //
////////////////////////////////////////////////////////////
void DivestOwnershipTest::testCancelNegotiatedDivestWithInvalidAttribute()
{
	try
	{
		this->ahs = defaultFederate->populatedAHS( 1, 10000 );
		defaultFederate->rtiamb->cancelNegotiatedAttributeOwnershipDivestiture( theObject, *ahs );
		failTestMissingException( "AttributeNotDefined",
		                          "cancel negotiated ownership divest with invalid attribute" );
	}
	catch( RTI::AttributeNotDefined& anotd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeNotDefined", e,
		                        "cancel negotiated ownership divest with invalid attribute" );
	}
}

////////////////////////////////////////////////////////////
// TEST: testCancelNegotiatedDivestWithUnownedAttribute() //
////////////////////////////////////////////////////////////
void DivestOwnershipTest::testCancelNegotiatedDivestWithUnownedAttribute()
{
	// start the transfer off, have it transferred but not confirmed to the default federate,
	// otherwise we'd get an error saying there was no outstanding request
	defaultFederate->quickNegotiatedRelease( theObject, 3, aa, ab, ac );
	secondFederate->fedamb->waitForOwnershipOffered( theObject, 3, aa, ab, ac );
	secondFederate->quickAcquireRequest( theObject, 3, aa, ab, ac );
	defaultFederate->quickTick();
	secondFederate->fedamb->waitForOwnershipAcquistion( theObject, 3, aa, ab, ac );
	
	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->cancelNegotiatedAttributeOwnershipDivestiture( theObject, *ahs );
		failTestMissingException( "AttributeNotOwned",
		                          "cancel negotiated ownership divest after exchange completed" );
	}
	catch( RTI::AttributeNotOwned& ano )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeNotOwned", e,
		                        "cancel negotiated ownership divest after exchange completed" );
	}
}

/////////////////////////////////////////////////////////////////
// TEST: testCancelNegotiatedDivestWithoutOutstandingRequest() //
/////////////////////////////////////////////////////////////////
void DivestOwnershipTest::testCancelNegotiatedDivestWithoutOutstandingRequest()
{
	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->cancelNegotiatedAttributeOwnershipDivestiture( theObject, *ahs );
		failTestMissingException( "AttributeDivestitureWasNotRequested",
		                          "cancel negotiated ownership divest when none was requested" );
	}
	catch( RTI::AttributeDivestitureWasNotRequested& anotd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeDivestitureWasNotRequested", e,
		                        "cancel negotiated ownership divest when none was requested" );
	}
}

/////////////////////////////////////////////////////
// TEST: testCancelNegotiatedDivestWhenNotJoined() //
/////////////////////////////////////////////////////
void DivestOwnershipTest::testCancelNegotiatedDivestWhenNotJoined()
{
	defaultFederate->quickResign();

	try
	{
		this->ahs = defaultFederate->populatedAHS( 3, aa, ab, ac );
		defaultFederate->rtiamb->cancelNegotiatedAttributeOwnershipDivestiture( theObject, *ahs );
		failTestMissingException( "FederateNotExecutionMember",
		                          "cancel negotiated ownership divest when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "cancel negotiated ownership divest when not joined" );
	}
}
