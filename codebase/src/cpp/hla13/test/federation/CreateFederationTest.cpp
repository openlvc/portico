/*
 *   Copyright 2007 The Portico Project
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
#include "CreateFederationTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( CreateFederationTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( CreateFederationTest, "CreateFederationTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( CreateFederationTest, "create" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( CreateFederationTest, "federationManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
CreateFederationTest::CreateFederationTest()
{
}

CreateFederationTest::~CreateFederationTest()
{
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void CreateFederationTest::setUp()
{
    this->defaultFederate = new Test13Federate( "defaultFederate" );
}

void CreateFederationTest::tearDown()
{
	delete this->defaultFederate;
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////
// TEST: (valid) testCreateFederation() //
//////////////////////////////////////////
void CreateFederationTest::testCreateFederation()
{
	// can we create the federation?
	try
	{
		defaultFederate->rtiamb->createFederationExecution( Test13Federate::SIMPLE_NAME,
		                                                    "etc/testfom.fed" );
		// clean up
		defaultFederate->quickDestroy();
	}
	catch( RTI::Exception &e )
	{
		// failure!
		failTest( "Exception creating valid fom: %s", e._reason );
	}
}

//////////////////////////////////////////////////
// TEST: testCreateFederationWithExistingName() //
//////////////////////////////////////////////////
void CreateFederationTest::testCreateFederationThatAlreadyExists()
{
	// create a federation
	defaultFederate->quickCreate();
	
	// can we create the federation?
	try
	{
		defaultFederate->rtiamb->createFederationExecution( Test13Federate::SIMPLE_NAME,
		                                                    "etc/testfom.fed" );
		// should have received an exception
		defaultFederate->quickDestroy();
		failTestMissingException( "FederationExecutionAlreadyExists",
		                          "recreating existing federation" );
	}
	catch( RTI::FederationExecutionAlreadyExists &feae )
	{
		// success! clean up
		defaultFederate->quickDestroy();
	}
	catch( RTI::Exception &e )
	{
		// failure!
		defaultFederate->quickDestroy();
		failTestWrongException( "FederationExecutionAlreadyExists", e, "recreating existing federation" );
	}
}

//////////////////////////////////////////////
// TEST: testCreateFederationWithNullName() //
//////////////////////////////////////////////
void CreateFederationTest::testCreateFederationWithInvalidFom()
{
	try
	{
		defaultFederate->rtiamb->createFederationExecution( Test13Federate::SIMPLE_NAME,
		                                                    "etc/testfom-invalid.fed" );
		
		// should have exceptioned all up out of here
		failTestMissingException( "ErrorReadingFED", "creating federation with invalid fed" );
	}
	catch( RTI::ErrorReadingFED &erf )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		// failure!
		failTestWrongException( "ErrorReadingFED", e, "creating federation with invalid fed" );
	}
}

//////////////////////////////////////////////
// TEST: testCreateFederationWithNullName() //
//////////////////////////////////////////////
void CreateFederationTest::testCreateFederationWithInvalidFomLocation()
{
	try
	{
		defaultFederate->rtiamb->createFederationExecution( Test13Federate::SIMPLE_NAME,
		                                                    "etc/noSuchFom.fed" );
		
		failTestMissingException( "CouldNotOpenFED", "creating federation with invalid fed location" );
	}
	catch( RTI::CouldNotOpenFED &cnof )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		// failure!
		failTestWrongException( "CouldNotOpenFED", e, "creating federation with invalid fed location" );
	}
}

//////////////////////////////////////////////
// TEST: testCreateFederationWithNullName() //
//////////////////////////////////////////////
void CreateFederationTest::testCreateFederationWithNullFom()
{
	try
	{
		defaultFederate->rtiamb->createFederationExecution( Test13Federate::SIMPLE_NAME, NULL );
		failTestMissingException( "CouldNotOpenFED", "creating federation with null fed file" );
	}
	catch( RTI::CouldNotOpenFED &cnof )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		// failure!
		failTestWrongException( "CouldNotOpenFED", e, "creating federation with null fed file" );
	}
}

//////////////////////////////////////////////
// TEST: testCreateFederationWithNullName() //
//////////////////////////////////////////////
void CreateFederationTest::testCreateFederationWithNullName()
{
	try
	{
		defaultFederate->rtiamb->createFederationExecution( NULL, "etc/testfom.fed" );
		failTestMissingException( "RTIinternalError", "creating federation with null name" );
	}
	catch( RTI::RTIinternalError &rtie )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		// failure!
		failTestWrongException( "RTIinternalError", e, "creating federation with null name" );
	}
}

