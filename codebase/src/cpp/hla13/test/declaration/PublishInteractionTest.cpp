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
#include "PublishInteractionTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( PublishInteractionTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( PublishInteractionTest, "PublishInteractionTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( PublishInteractionTest, "publishInteraction" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( PublishInteractionTest, "declarationManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
PublishInteractionTest::PublishInteractionTest()
{
	this->defaultFederate = new Test13Federate( "defaultFederate" );
	this->listenerFederate = new Test13Federate( "listenerFederate" );
}

PublishInteractionTest::~PublishInteractionTest()
{
	delete this->defaultFederate;
	delete this->listenerFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void PublishInteractionTest::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->listenerFederate->quickJoin();
	
	// get the class handles
	this->xHandle = defaultFederate->quickICHandle( "InteractionRoot.X" );
	this->yHandle = defaultFederate->quickICHandle( "InteractionRoot.X.Y" );
	this->xaHandle = defaultFederate->quickPCHandle( "InteractionRoot.X", "xa" );
	this->xbHandle = defaultFederate->quickPCHandle( "InteractionRoot.X", "xb" );
	this->xcHandle = defaultFederate->quickPCHandle( "InteractionRoot.X", "xc" );
	this->yaHandle = defaultFederate->quickPCHandle( "InteractionRoot.X.Y", "ya" );
	this->ybHandle = defaultFederate->quickPCHandle( "InteractionRoot.X.Y", "yb" );
	this->ycHandle = defaultFederate->quickPCHandle( "InteractionRoot.X.Y", "yc" );
	
	// do pub/sub
	this->listenerFederate->quickSubscribe( "InteractionRoot.X.Y" );
}

void PublishInteractionTest::tearDown()
{
	this->listenerFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

void PublishInteractionTest::validatePublished()
{
	// send an interaction from the defaultFederate
	defaultFederate->quickSend( yHandle, 6, "xa", "xb", "xc", "ya", "yb", "yc" );
	
	// receive the interaction in the listener
	Test13Interaction *received = listenerFederate->fedamb->waitForROInteraction( yHandle );
	
	// validate the parameters for the interaction
	int result = strcmp( "xa", received->getParameter(xaHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "xb", received->getParameter(xbHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "xc", received->getParameter(xcHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "ya", received->getParameter(yaHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "yb", received->getParameter(ybHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "yc", received->getParameter(ycHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );

	// clean the interaction up
	delete received;
}

///////////////////////////////////
// TEST: (valid) testICPublish() //
///////////////////////////////////
void PublishInteractionTest::testICPublish()
{
	try
	{
		defaultFederate->rtiamb->publishInteractionClass( yHandle );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while publishing interaction class: %s", e._reason );
	}
	
	validatePublished();
}

////////////////////////////////////////////
// TEST: testICPublishWithInvalidHandle() //
////////////////////////////////////////////
void PublishInteractionTest::testICPublishWithInvalidHandle()
{
	try
	{
		defaultFederate->rtiamb->publishInteractionClass( 1111111 );
		failTestMissingException( "InteractionClassNotDefined", 
		                          "publishing interaction class with invalid handle" );
	}
	catch( RTI::InteractionClassNotDefined& icnd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "InteractionClassNotDefined", e,
		                        "publishing interaction class with invalid handle" );
	}
}

/////////////////////////////////////////
// TEST: testICPublishWhileNotJoined() //
/////////////////////////////////////////
void PublishInteractionTest::testICPublishWhileNotJoined()
{
	defaultFederate->quickResign();

	try
	{
		defaultFederate->rtiamb->publishInteractionClass( yHandle );
		failTestMissingException( "FederateNotExecutionMember", 
		                          "publishing interaction class when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "publishing interaction class when not joined" );
	}
}

////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Unpublish Test Methods //////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

void PublishInteractionTest::validateNotPublished()
{
	RTI::ParameterHandleValuePairSet *phvps = defaultFederate->createPHVPS( 0 );
	try
	{
		defaultFederate->rtiamb->sendInteraction( yHandle, *phvps, "NA" );
		delete phvps;
		failTestMissingException( "InteractionClassNotPublished",
		                          "Sending interaction of class that isn't meant to be published" );

	}
	catch( RTI::InteractionClassNotPublished& icnp )
	{
		// success
		delete phvps;
	}
	catch( RTI::Exception& e )
	{
		delete phvps;
		failTestWrongException( "InteractionClassNotPublished", e,
		                        "Sending interaction of class that isn't meant to be published" );
	}
}

/////////////////////////////////////
// TEST: (valid) testICUnpublish() //
/////////////////////////////////////
void PublishInteractionTest::testICUnpublish()
{
	defaultFederate->quickPublish( "InteractionRoot.X.Y" );
	validatePublished();
	
	try
	{
		defaultFederate->rtiamb->unpublishInteractionClass( yHandle );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while unpublishing interaction: %s", e._reason );
	}
	
	validateNotPublished();
}

//////////////////////////////////////////////
// TEST: testICUnpublishWithInvalidHandle() //
//////////////////////////////////////////////
void PublishInteractionTest::testICUnpublishWithInvalidHandle()
{
	try
	{
		defaultFederate->rtiamb->unpublishInteractionClass( 1111111 );
		failTestMissingException( "InteractionClassNotDefined", 
		                          "unpublishing interaction class with invalid handle" );
	}
	catch( RTI::InteractionClassNotDefined& icnd )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "InteractionClassNotDefined", e,
		                        "unpublishing interaction class with invalid handle" );
	}
}

/////////////////////////////////////////////
// TEST: testICUnpublishWhenNotPublished() //
/////////////////////////////////////////////
void PublishInteractionTest::testICUnpublishWhenNotPublished()
{
	try
	{
		defaultFederate->rtiamb->unpublishInteractionClass( yHandle );
		failTestMissingException( "InteractionClassNotPublished", 
		                          "unpublishing interaction class when not published" );
	}
	catch( RTI::InteractionClassNotPublished& icnp )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "InteractionClassNotPublished", e,
		                        "unpublishing interaction class when not published" );
	}
}

///////////////////////////////////////////
// TEST: testICUnpublishWhileNotJoined() //
///////////////////////////////////////////
void PublishInteractionTest::testICUnpublishWhileNotJoined()
{
	defaultFederate->quickResign();

	try
	{
		defaultFederate->rtiamb->unpublishInteractionClass( yHandle );
		failTestMissingException( "FederateNotExecutionMember", 
		                          "unpublishing interaction class when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "unpublishing interaction class when not joined" );
	}
}
