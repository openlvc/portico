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
#include "RegisterObjectTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( RegisterObjectTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( RegisterObjectTest, "RegisterObjectTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( RegisterObjectTest, "registerObject" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( RegisterObjectTest, "objectManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
RegisterObjectTest::RegisterObjectTest()
{
	this->defaultFederate = new TestNG6Federate( "defaultFederate" );
	this->federateA = new TestNG6Federate( "federateA" );
	this->federateB = new TestNG6Federate( "federateB" );
	this->federateNone = new TestNG6Federate( "federateNone" );
}

RegisterObjectTest::~RegisterObjectTest()
{
	delete this->defaultFederate;
	delete this->federateA;
	delete this->federateB;
	delete this->federateNone;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void RegisterObjectTest::setUp()
{
    this->defaultFederate->quickCreate();
    this->defaultFederate->quickJoin();
    this->federateA->quickJoin();
    this->federateB->quickJoin();
    this->federateNone->quickJoin();
    
    // get the handle information
    this->aHandle  = defaultFederate->quickOCHandle( "ObjectRoot.A" );
    this->bHandle  = defaultFederate->quickOCHandle( "ObjectRoot.A.B" );
    
    // publish and subscribe
    this->defaultFederate->quickPublish( "ObjectRoot.A.B", 1, "aa" );
    this->federateA->quickSubscribe( "ObjectRoot.A", 1, "aa" );
    this->federateB->quickSubscribe( "ObjectRoot.A.B", 1, "aa" );
}

void RegisterObjectTest::tearDown()
{
	this->federateNone->quickResign();
	this->federateB->quickResign();
	this->federateA->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////
// TEST: (valid) testRegisterObject() //
////////////////////////////////////////
void RegisterObjectTest::testRegisterObject()
{
	RTI::ObjectHandle theObject = 0;
	try
	{
		theObject = defaultFederate->rtiamb->registerObjectInstance( bHandle );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while registering object instance: %s", e._reason );
	}
	
	// make sure it is discovered by the other federates
	federateA->fedamb->waitForDiscoveryAs( theObject, aHandle );
	federateB->fedamb->waitForDiscoveryAs( theObject, bHandle );
	federateNone->fedamb->waitForDiscoveryTimeout( theObject );
}

//////////////////////////////////////////////////////
// TEST: testRegisterObjectWithInvalidClassHandle() //
//////////////////////////////////////////////////////
void RegisterObjectTest::testRegisterObjectWithInvalidClassHandle()
{
	try
	{
		defaultFederate->rtiamb->registerObjectInstance( (RTI::ObjectClassHandle)100000 );
		failTestMissingException( "ObjectClassNotDefined",
		                          "registering instance with invalid class handle" );
	}
	catch( RTI::ObjectClassNotDefined& ocnd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectClassNotDefined", e,
		                        "registering instance with invalid class handle" );
	}
}

///////////////////////////////////////////////////////////
// TEST: testRegisterObjectWithNonPublishedClassHandle() //
///////////////////////////////////////////////////////////
void RegisterObjectTest::testRegisterObjectWithNonPublishedClassHandle()
{
	try
	{
		defaultFederate->rtiamb->registerObjectInstance( aHandle );
		failTestMissingException( "ObjectClassNotPublished",
		                          "registering instance with unpublished class handle" );
	}
	catch( RTI::ObjectClassNotPublished& ocnp )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectClassNotPublished", e,
		                        "registering instance with unpublished class handle" );
	}
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Register Named Object Tests ////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////
// TEST: (valid) testRegisterNamedObject() //
/////////////////////////////////////////////
void RegisterObjectTest::testRegisterNamedObject()
{
	RTI::ObjectHandle theObject = 0;
	try
	{
		theObject = defaultFederate->rtiamb->registerObjectInstance( bHandle, "MyObject" );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while registering object instance: %s", e._reason );
	}
	
	// make sure it is discovered by the other federates
	federateA->fedamb->waitForDiscoveryAsWithName( theObject, aHandle, "MyObject" );
	federateB->fedamb->waitForDiscoveryAsWithName( theObject, bHandle, "MyObject" );
	federateNone->fedamb->waitForDiscoveryTimeout( theObject );
}

/////////////////////////////////////////////////////////
// TEST: (valid) testRegisterNamedObjectWithNullName() //
/////////////////////////////////////////////////////////
void RegisterObjectTest::testRegisterNamedObjectWithNullName()
{
	RTI::ObjectHandle theObject = 0;
	try
	{
		theObject = defaultFederate->rtiamb->registerObjectInstance( bHandle, NULL );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while registering object instance: %s", e._reason );
	}
	
	// make sure it is discovered by the other federates
	char *expectedName = new char[8];
	sprintf( expectedName, "HLA%lo", theObject );
	federateA->fedamb->waitForDiscoveryAsWithName( theObject, aHandle, expectedName );
	federateB->fedamb->waitForDiscoveryAsWithName( theObject, bHandle, expectedName );
	delete expectedName; // the waitForDiscoveryAsWithName() will delete if the test fails
	federateNone->fedamb->waitForDiscoveryTimeout( theObject );
}

///////////////////////////////////////////////////////////////
// TEST: (valid) testRegisterNamedObjectWithWhitespaceName() //
///////////////////////////////////////////////////////////////
void RegisterObjectTest::testRegisterNamedObjectWithWhitespaceName()
{
	RTI::ObjectHandle theObject = 0;
	try
	{
		theObject = defaultFederate->rtiamb->registerObjectInstance( bHandle, "   " );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while registering object instance: %s", e._reason );
	}
	
	// make sure it is discovered by the other federates
	char *expectedName = new char[4];
	sprintf( expectedName, "   " );
	federateA->fedamb->waitForDiscoveryAsWithName( theObject, aHandle, expectedName );
	federateB->fedamb->waitForDiscoveryAsWithName( theObject, bHandle, expectedName );
	delete expectedName; // the waitForDiscoveryAsWithName() will delete if the test fails
	federateNone->fedamb->waitForDiscoveryTimeout( theObject );
}

///////////////////////////////////////////////////////////
// TEST: testRegisterNamedObjectWithInvalidClassHandle() //
///////////////////////////////////////////////////////////
void RegisterObjectTest::testRegisterNamedObjectWithInvalidClassHandle()
{
	try
	{
		defaultFederate->rtiamb->registerObjectInstance( (RTI::ObjectClassHandle)100000, "MyName" );
		failTestMissingException( "ObjectClassNotDefined",
		                          "registering instance with invalid class handle" );
	}
	catch( RTI::ObjectClassNotDefined& ocnd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectClassNotDefined", e,
		                        "registering instance with invalid class handle" );
	}
}

/////////////////////////////////////////////////////
// TEST: testRegisterNamedObjectWithExistingName() //
/////////////////////////////////////////////////////
void RegisterObjectTest::testRegisterNamedObjectWithExistingName()
{
	RTI::ObjectClassHandle firstObject = defaultFederate->quickRegister( bHandle, "MyObject" );
	federateA->fedamb->waitForDiscoveryAsWithName( firstObject, aHandle, "MyObject" );
	federateB->fedamb->waitForDiscoveryAsWithName( firstObject, bHandle, "MyObject" );
	
	try
	{
		defaultFederate->rtiamb->registerObjectInstance( bHandle, "MyObject" );
		failTestMissingException( "ObjectAlreadyRegistered",
		                          "registering instance with name that has already been taken" );
	}
	catch( RTI::ObjectAlreadyRegistered& ocnd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectAlreadyRegistered", e,
		                        "registering instance with name that has already been taken" );
	}
}

////////////////////////////////////////////////////////////////
// TEST: testRegisterNamedObjectWithNonPublishedClassHandle() //
////////////////////////////////////////////////////////////////
void RegisterObjectTest::testRegisterNamedObjectWithNonPublishedClassHandle()
{
	try
	{
		defaultFederate->rtiamb->registerObjectInstance( aHandle, "MyObject" );
		failTestMissingException( "ObjectClassNotPublished",
		                          "registering instance with unpublished class handle" );
	}
	catch( RTI::ObjectClassNotPublished& ocnp )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectClassNotPublished", e,
		                        "registering instance with unpublished class handle" );
	}
}
