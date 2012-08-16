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
#include "RegisterObjectDDMTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( RegisterObjectDDMTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( RegisterObjectDDMTest, "RegisterObjectDDMTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( RegisterObjectDDMTest, "registerObjectWithRegion" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( RegisterObjectDDMTest, "ddm" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
RegisterObjectDDMTest::RegisterObjectDDMTest()
{
	this->defaultFederate = new TestNG6Federate( "defaultFederate" );
	this->federateA = new TestNG6Federate( "federateA" );
	this->federateB = new TestNG6Federate( "federateB" );
	this->federateC = new TestNG6Federate( "federateC" );
}

RegisterObjectDDMTest::~RegisterObjectDDMTest()
{
	delete this->defaultFederate;
	delete this->federateA;
	delete this->federateB;
	delete this->federateC;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void RegisterObjectDDMTest::setUp()
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
	this->regionOne = defaultFederate->quickCreateTestRegion( 100, 200 );
	this->regionTwo = defaultFederate->quickCreateTestRegion( 400, 500 );
	this->federateARegion = federateA->quickCreateTestRegion( 150, 250 );
	this->federateBRegion = federateB->quickCreateTestRegion( 450, 550 );

	// publish and subscribe
	this->defaultFederate->quickPublish( "ObjectRoot.A.B", 6, "aa", "ab", "ac", "ba", "bb", "bc" );
	this->federateA->quickSubscribeWithRegion( "ObjectRoot.A.B", federateARegion, 6,
	                                           "aa", "ab", "ac", "ba", "bb", "bc" );
	this->federateB->quickSubscribeWithRegion( "ObjectRoot.A.B", federateBRegion, 6, 
	                                           "aa", "ab", "ac", "ba", "bb", "bc" );
	this->federateC->quickSubscribe( "ObjectRoot.A.B", 6, "aa", "ab", "ac", "ba", "bb", "bc" );

	// create the helper arrays
	//this->allHandles = RTI::AttributeHandle[6];
	this->allHandles[0] = aaHandle;
	this->allHandles[1] = abHandle;
	this->allHandles[2] = acHandle;
	this->allHandles[3] = baHandle;
	this->allHandles[4] = bbHandle;
	this->allHandles[5] = bcHandle;
	
	//this->regions = new RTI::Region*[6];
	this->regions[0] = regionOne;
	this->regions[1] = regionOne;
	this->regions[2] = regionOne;
	this->regions[3] = regionOne;
	this->regions[4] = regionOne;
	this->regions[5] = regionOne;
}

void RegisterObjectDDMTest::tearDown()
{
	//delete [] this->allHandles(); -- auto deleted
	//delete [] this->regions();    -- auto deleted

	delete this->regionOne;
	delete this->regionTwo;
	delete this->federateARegion;
	delete this->federateBRegion;
	
	this->federateC->quickResign();
	this->federateB->quickResign();
	this->federateA->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

/*
 * Update the object in region one and make sure only the attributes associated with that region
 * are updated.
 */
void RegisterObjectDDMTest::validateRegistration( RTI::ObjectHandle theObject )
{
	// make sure the other federates discover the object
	federateA->fedamb->waitForDiscoveryAs( theObject, bHandle );
	federateB->fedamb->waitForDiscoveryAs( theObject, bHandle );
	federateC->fedamb->waitForDiscoveryAs( theObject, bHandle );

	// update all the attributes
	defaultFederate->quickReflect( theObject, 6, "aa", "ab", "ac", "ba", "bb", "bc" );
	
	// make sure reflections sent to federateA
	TestNG6Object *ng6Object = federateA->fedamb->waitForROUpdate( theObject );
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

////////////////////////////////////////
// TEST: (valid) testRegisterObject() //
////////////////////////////////////////
void RegisterObjectDDMTest::testRegisterObjectDDM()
{
	RTI::ObjectHandle theObject = 0;
	try
	{
		theObject = defaultFederate->rtiamb->registerObjectInstanceWithRegion( bHandle,
		                                                                       allHandles,
		                                                                       regions,
		                                                                       6 );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while registering object instance with region: %s",
		          e._reason );
	}
	
	// make sure it is discovered by the other federates
	validateRegistration( theObject );
}

//////////////////////////////////////////////////////
// TEST: testRegisterObjectWithInvalidClassHandle() //
//////////////////////////////////////////////////////
void RegisterObjectDDMTest::testRegisterObjectDDMWithInvalidClassHandle()
{
	try
	{
		defaultFederate->rtiamb->registerObjectInstanceWithRegion( (RTI::ObjectClassHandle)100000,
		                                                           allHandles,
		                                                           regions,
		                                                           6 );
		failTestMissingException( "ObjectClassNotDefined",
		                          "registering instance with invalid class handle" );
	}
	catch( RTI::ObjectClassNotDefined& ocnd )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "ObjectClassNotDefined", e,
		                        "registering instance with invalid class handle" );
	}
}

///////////////////////////////////////////////////////////
// TEST: testRegisterObjectWithNonPublishedClassHandle() //
///////////////////////////////////////////////////////////
void RegisterObjectDDMTest::testRegisterObjectDDMWithNonPublishedClassHandle()
{
	try
	{
		defaultFederate->rtiamb->registerObjectInstanceWithRegion( aHandle, allHandles, regions, 6 );
		failTestMissingException( "ObjectClassNotPublished",
		                          "registering instance with unpublished class handle" );
	}
	catch( RTI::ObjectClassNotPublished& ocnp )
	{
		// success!
	}
	catch( RTI::Exception &e )
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
void RegisterObjectDDMTest::testRegisterNamedObjectDDM()
{
	RTI::ObjectHandle theObject = 0;
	try
	{
		theObject = defaultFederate->rtiamb->registerObjectInstanceWithRegion( bHandle,
		                                                                       "MyObject",
		                                                                       allHandles,
		                                                                       regions,
		                                                                       6 );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while registering object instance: %s", e._reason );
	}
	
	// make sure it is discovered by the other federates
	validateRegistration( theObject );
}

/////////////////////////////////////////////////////////
// TEST: (valid) testRegisterNamedObjectWithNullName() //
/////////////////////////////////////////////////////////
void RegisterObjectDDMTest::testRegisterNamedObjectDDMWithNullName()
{
	RTI::ObjectHandle theObject = 0;
	try
	{
		theObject = defaultFederate->rtiamb->registerObjectInstance( bHandle, NULL );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while registering object instance: %s", e._reason );
	}
	
	// make sure it is discovered by the other federates
	char *expectedName = new char[8];
	sprintf( expectedName, "HLA%lo", theObject );
	federateA->fedamb->waitForDiscoveryAsWithName( theObject, bHandle, expectedName );
	federateB->fedamb->waitForDiscoveryAsWithName( theObject, bHandle, expectedName );
	delete expectedName; // the waitForDiscoveryAsWithName() will delete if the test fails
}

///////////////////////////////////////////////////////////////
// TEST: (valid) testRegisterNamedObjectWithWhitespaceName() //
///////////////////////////////////////////////////////////////
void RegisterObjectDDMTest::testRegisterNamedObjectDDMWithWhitespaceName()
{
	RTI::ObjectHandle theObject = 0;
	try
	{
		theObject = defaultFederate->rtiamb->registerObjectInstance( bHandle, "   " );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while registering object instance: %s", e._reason );
	}
	
	// make sure it is discovered by the other federates
	char *expectedName = new char[4];
	sprintf( expectedName, "   " );
	federateA->fedamb->waitForDiscoveryAsWithName( theObject, bHandle, expectedName );
	federateB->fedamb->waitForDiscoveryAsWithName( theObject, bHandle, expectedName );
	delete expectedName;
}

///////////////////////////////////////////////////////////
// TEST: testRegisterNamedObjectWithInvalidClassHandle() //
///////////////////////////////////////////////////////////
void RegisterObjectDDMTest::testRegisterNamedObjectDDMWithInvalidClassHandle()
{
	try
	{
		defaultFederate->rtiamb->registerObjectInstanceWithRegion( (RTI::ObjectClassHandle)100000,
		                                                           "MyName",
		                                                           allHandles, 
		                                                           regions,
		                                                           6 );
		failTestMissingException( "ObjectClassNotDefined",
		                          "registering instance with invalid class handle" );
	}
	catch( RTI::ObjectClassNotDefined& ocnd )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "ObjectClassNotDefined", e,
		                        "registering instance with invalid class handle" );
	}
}

/////////////////////////////////////////////////////
// TEST: testRegisterNamedObjectWithExistingName() //
/////////////////////////////////////////////////////
void RegisterObjectDDMTest::testRegisterNamedObjectDDMWithExistingName()
{
	RTI::ObjectClassHandle firstObject = defaultFederate->quickRegister( bHandle, "MyObject" );
	federateA->fedamb->waitForDiscoveryAsWithName( firstObject, bHandle, "MyObject" );
	federateB->fedamb->waitForDiscoveryAsWithName( firstObject, bHandle, "MyObject" );
	federateC->fedamb->waitForDiscoveryAsWithName( firstObject, bHandle, "MyObject" );
	
	try
	{
		defaultFederate->rtiamb->registerObjectInstanceWithRegion( bHandle,
		                                                           "MyObject",
		                                                           allHandles,
		                                                           regions,
		                                                           6 );
		failTestMissingException( "ObjectAlreadyRegistered",
		                          "registering instance with name that has already been taken" );
	}
	catch( RTI::ObjectAlreadyRegistered& ocnd )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "ObjectAlreadyRegistered", e,
		                        "registering instance with name that has already been taken" );
	}
}

////////////////////////////////////////////////////////////////
// TEST: testRegisterNamedObjectWithNonPublishedClassHandle() //
////////////////////////////////////////////////////////////////
void RegisterObjectDDMTest::testRegisterNamedObjectDDMWithNonPublishedClassHandle()
{
	try
	{
		defaultFederate->rtiamb->registerObjectInstanceWithRegion( aHandle,
		                                                           "MyObject",
		                                                           allHandles,
		                                                           regions,
		                                                           6 );
		failTestMissingException( "ObjectClassNotPublished",
		                          "registering instance with unpublished class handle" );
	}
	catch( RTI::ObjectClassNotPublished& ocnp )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "ObjectClassNotPublished", e,
		                        "registering instance with unpublished class handle" );
	}
}
