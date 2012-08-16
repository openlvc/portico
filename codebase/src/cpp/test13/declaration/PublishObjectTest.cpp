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
#include "PublishObjectTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( PublishObjectTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( PublishObjectTest, "PublishObjectTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( PublishObjectTest, "publishObject" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( PublishObjectTest, "declarationManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
PublishObjectTest::PublishObjectTest()
{
	this->defaultFederate = new TestNG6Federate( "defaultFederate" );
	this->listener = new TestNG6Federate( "listener" );
}

PublishObjectTest::~PublishObjectTest()
{
	delete this->listener;
	delete this->defaultFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void PublishObjectTest::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->listener->quickJoin();
	
	// get the handle information
	this->aaHandle = defaultFederate->quickACHandle( "ObjectRoot.A", "aa" );
	this->bHandle  = defaultFederate->quickOCHandle( "ObjectRoot.A.B" );
	this->baHandle = defaultFederate->quickACHandle( "ObjectRoot.A.B", "ba" );
	
	// subscribe in the listener federates
	listener->quickSubscribe( "ObjectRoot.A.B", 6, "aa", "ab", "ac", "ba", "bb", "bc" );
	
	this->theSet = defaultFederate->populatedAHS( 2, aaHandle, baHandle );
}

void PublishObjectTest::tearDown()
{
	delete theSet;
	this->listener->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////// Helper Validation Methods ////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

void PublishObjectTest::validatePublished()
{
	// register an instance of the provided object class (assumes we are publishing it)
	RTI::ObjectHandle theObject = defaultFederate->quickRegister( bHandle );
	
	// update the values for the newly created instance
	defaultFederate->quickReflect( theObject, 2, "aa", "ba" );
	
	// wait for the update in the listener
	listener->fedamb->waitForROUpdate( theObject );
}

void PublishObjectTest::validateNotPublished()
{
	try
	{
		defaultFederate->rtiamb->registerObjectInstance( bHandle );
		failTestMissingException( "ObjectClassNotPublished",
		                          "Registering object for class we shouldn't be publishing" );
	}
	catch( RTI::ObjectClassNotPublished& ocnp )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectClassNotPublished", e,
		                        "Registering object for class we shouldn't be publishing" );
	}
}

////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////// Publication Test Methods /////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////
// TEST: (valid) testOCPublish() //
///////////////////////////////////
void PublishObjectTest::testOCPublish()
{
	try
	{
		defaultFederate->rtiamb->publishObjectClass( bHandle, *theSet );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception publishing object class: %s", e._reason );
	}
	
	// validate that we are publishing ObjectRoot.A, but not ObjectRoot.A.B
	validatePublished();
}

///////////////////////////////////////////////////////
// TEST: (valid) testOCPublishWhenAlreadyPublished() //
///////////////////////////////////////////////////////
void PublishObjectTest::testOCPublishWhenAlreadyPublished()
{
	// set the publication up
	defaultFederate->quickPublish( "ObjectRoot.A.B", 2, "aa", "ba" );
	validatePublished();

	// publish again, this is allowed and it should replace the set of attributes that
	// are already published. we're not checking to that level as that is up to the java
	// unit tests. we just want to check that the call goes through OK
	try
	{
		defaultFederate->rtiamb->publishObjectClass( bHandle, *theSet );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception republishing object class: %s", e._reason );
	}
	
	// validate that we are publishing ObjectRoot.A, but not ObjectRoot.A.B
	validatePublished();
}

/////////////////////////////////////////////////////
// TEST: (valid) testOCPublishWithEmptyHandleSet() //
/////////////////////////////////////////////////////
void PublishObjectTest::testOCPublishWithEmptyHandleSet()
{
	// set the publication up
	defaultFederate->quickPublish( "ObjectRoot.A.B", 2, "aa", "ba" );
	validatePublished();

	// publish again, but with empty handle set. this is an implicit unpublish
	RTI::AttributeHandleSet *emptySet = defaultFederate->createAHS(0);
	try
	{
		defaultFederate->rtiamb->publishObjectClass( bHandle, *emptySet );
		delete emptySet;
	}
	catch( RTI::Exception& e )
	{
		delete emptySet;
		failTest( "Unexpected exception publishing object class with empty set: %s", e._reason );
	}
	
	// validate that we are publishing ObjectRoot.A, but not ObjectRoot.A.B
	validateNotPublished();
}

/////////////////////////////////////////////////
// TEST: testOCPublishWithInvalidClassHandle() //
/////////////////////////////////////////////////
void PublishObjectTest::testOCPublishWithInvalidClassHandle()
{
	try
	{
		defaultFederate->rtiamb->publishObjectClass( 1000000, *theSet );
		failTestMissingException( "ObjectClassNotDefined",
		                          "publishing object class that doesn't exist" );
	}
	catch( RTI::ObjectClassNotDefined& ocnd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectClassNotDefined", e,
		                        "publishing object class that doesn't exist" );
	}
}

/////////////////////////////////////////////////////
// TEST: testOCPublishWithInvalidAttributeHandle() //
/////////////////////////////////////////////////////
void PublishObjectTest::testOCPublishWithInvalidAttributeHandle()
{
	RTI::AttributeHandleSet *theSet = defaultFederate->populatedAHS( 2, aaHandle, 1000000 );
	
	try
	{
		defaultFederate->rtiamb->publishObjectClass( bHandle, *theSet );
		failTestMissingException( "AttributeNotDefined",
		                          "publishing object class with invalid attribute handle" );
	}
	catch( RTI::AttributeNotDefined& ocnd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "AttributeNotDefined", e,
		                        "publishing object class with invalid attribute handle" );
	}
}

////////////////////////////////////////
// TEST: testOCPublishWhenNotJoined() //
////////////////////////////////////////
void PublishObjectTest::testOCPublishWhenNotJoined()
{
	defaultFederate->quickResign();
	
	try
	{
		defaultFederate->rtiamb->publishObjectClass( bHandle, *theSet );
		failTestMissingException( "FederateNotExecutionMember",
		                          "publishing object class while not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "publishing object class while not joined" );
	}
}

////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Unpublish Test Methods //////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////
// (valid) testOCUnpublish() //
///////////////////////////////
void PublishObjectTest::testOCUnpublish()
{
	// set the publication up
	defaultFederate->quickPublish( "ObjectRoot.A.B", 2, "aa", "ba" );
	validatePublished();
	
	// do the unpublish
	try
	{
		defaultFederate->rtiamb->unpublishObjectClass( bHandle );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while unpublishing object class: %s", e._reason );
	}
	
	validateNotPublished();
}

///////////////////////////////////////////////////
// TEST: testOCUnpublishWithInvalidClassHandle() //
///////////////////////////////////////////////////
void PublishObjectTest::testOCUnpublishWithInvalidClassHandle()
{
	try
	{
		defaultFederate->rtiamb->unpublishObjectClass( 1000000 );
		failTestMissingException( "ObjectClassNotDefined",
		                          "unpublishing object class that doesn't exist" );
	}
	catch( RTI::ObjectClassNotDefined& ocnd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectClassNotDefined", e,
		                        "unpublishing object class that doesn't exist" );
	}
}

/////////////////////////////////////////////
// TEST: testOCUnpublishWhenNotPublished() //
/////////////////////////////////////////////
void PublishObjectTest::testOCUnpublishWhenNotPublished()
{
	try
	{
		defaultFederate->rtiamb->unpublishObjectClass( bHandle );
		failTestMissingException( "ObjectClassNotPublished",
		                          "unpublishing object class that we dont' publish" );
	}
	catch( RTI::ObjectClassNotPublished& ocnp )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectClassNotPublished", e,
		                        "unpublishing object class that we dont' publish" );
	}
}

//////////////////////////////////////////
// TEST: testOCUnpublishWhenNotJoined() //
//////////////////////////////////////////
void PublishObjectTest::testOCUnpublishWhenNotJoined()
{
	defaultFederate->quickResign();
	
	try
	{
		defaultFederate->rtiamb->unpublishObjectClass( bHandle );
		failTestMissingException( "FederateNotExecutionMember",
		                          "unpublishing object class while not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "unpublishing object class while not joined" );
	}
}

