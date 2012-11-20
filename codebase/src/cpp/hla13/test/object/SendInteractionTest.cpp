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
#include "SendInteractionTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( SendInteractionTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SendInteractionTest, "SendInteractionTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SendInteractionTest, "sendInteraction" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SendInteractionTest, "objectManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
SendInteractionTest::SendInteractionTest()
{
	this->defaultFederate = new Test13Federate( "defaultFederate" );
	this->listenerFederate = new Test13Federate( "listenerFederate" );
}

SendInteractionTest::~SendInteractionTest()
{
	delete this->listenerFederate;
	delete this->defaultFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void SendInteractionTest::setUp()
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
	this->defaultFederate->quickPublish( "InteractionRoot.X.Y" );
	this->listenerFederate->quickSubscribe( "InteractionRoot.X.Y" );
	
	// enable the time profiles
	defaultFederate->quickEnableRegulating( 5.0 );
	listenerFederate->quickEnableAsync();
	listenerFederate->quickEnableConstrained();
	
	// fill out the phvps
	this->phvps = defaultFederate->createPHVPS( 6 );
	this->phvps->add( xaHandle, "xa", 3 );
	this->phvps->add( xbHandle, "xb", 3 );
	this->phvps->add( xcHandle, "xc", 3 );
	this->phvps->add( yaHandle, "ya", 3 );
	this->phvps->add( ybHandle, "yb", 3 );
	this->phvps->add( ycHandle, "yc", 3 );
}

void SendInteractionTest::tearDown()
{
	delete phvps;
	this->listenerFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////// Test Helper Methods ///////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

void SendInteractionTest::validateReceivedRO()
{
	// wait for the listener to receive it
	Test13Interaction *received = listenerFederate->fedamb->waitForROInteraction( yHandle );
	
	// validate the contents of the interaction
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
	
	// clean up
	delete received;
}

void SendInteractionTest::validateReceivedTSO()
{
	// wait for the listener to receive it
	Test13Interaction *received = listenerFederate->fedamb->waitForTSOInteraction( yHandle );
	
	// validate the contents of the interaction
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
	
	// clean up
	delete received;
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// RO Test Send Methods ///////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////
// TEST: (valid) testSendROInteraction() //
///////////////////////////////////////////
void SendInteractionTest::testSendROInteraction()
{
	try
	{
		defaultFederate->rtiamb->sendInteraction( yHandle, *phvps, "NA" );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while sending RO interaction: %s", e._reason );
	}
	
	validateReceivedRO();
}

///////////////////////////////////////////////////////////
// TEST: (valid) testSendROInteractionWithNoParameters() //
///////////////////////////////////////////////////////////
void SendInteractionTest::testSendROInteractionWithNoParameters()
{
	RTI::ParameterHandleValuePairSet *emptySet = defaultFederate->createPHVPS(0);
	
	try
	{
		defaultFederate->rtiamb->sendInteraction( yHandle, *emptySet, "NA" );
		delete emptySet;
	}
	catch( RTI::Exception &e )
	{
		delete emptySet;
		failTest( "Unexpected exception sending RO interaction with empty params: %s", e._reason );
	}
	
	// wait for the interaciton and validate that it has no parameters
	Test13Interaction *interaction = listenerFederate->fedamb->waitForROInteraction( yHandle );
	int size = interaction->getSize();
	delete interaction;
	CPPUNIT_ASSERT_EQUAL( 0, size );
}

///////////////////////////////////////////////////
// TEST: testSendROInteractionWithInvalidClass() //
///////////////////////////////////////////////////
void SendInteractionTest::testSendROInteractionWithInvalidClass()
{
	try
	{
		defaultFederate->rtiamb->sendInteraction( 10000000, *phvps, "NA" );
		failTestMissingException( "InteractionClassNotDefined", 
		                          "sending interaction with wrong class handle" );
	}
	catch( RTI::InteractionClassNotDefined &icnd )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "InteractionClassNotDefined", e,
		                        "sending interaction with wrong class handle" );
	}
}

///////////////////////////////////////////////////////
// TEST: testSendROInteractionWithInvalidParameter() //
///////////////////////////////////////////////////////
void SendInteractionTest::testSendROInteractionWithInvalidParameter()
{
	RTI::ParameterHandleValuePairSet *params = defaultFederate->createPHVPS( 2 );
	params->add( xaHandle, "xa", 3 );
	params->add( 10000000, "no", 3 );
	
	try
	{
		defaultFederate->rtiamb->sendInteraction( yHandle, *params, "NA" );
		delete params;
		failTestMissingException( "InteractionParameterNotDefined", 
		                          "sending interaction with invalid param handle" );
	}
	catch( RTI::InteractionParameterNotDefined &ipnd )
	{
		// success!
		delete params;
	}
	catch( RTI::Exception &e )
	{
		delete params;
		failTestWrongException( "InteractionParameterNotDefined", e,
		                        "sending interaction with invalid param handle" );
	}
}

///////////////////////////////////////////////////
// TEST: testSendROInteractionWhenNotPublished() //
///////////////////////////////////////////////////
void SendInteractionTest::testSendROInteractionWhenNotPublished()
{
	try
	{
		defaultFederate->rtiamb->sendInteraction( xHandle, *phvps, "NA" );
		failTestMissingException( "InteractionClassNotPublished", 
		                          "sending interaction of class we don't publish" );
	}
	catch( RTI::InteractionClassNotPublished &icnp )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "InteractionClassNotPublished", e,
		                        "sending interaction of class we don't publish" );
	}
}

////////////////////////////////////////////////
// TEST: testSendROInteractionWhenNotJoined() //
////////////////////////////////////////////////
void SendInteractionTest::testSendROInteractionWhenNotJoined()
{
	defaultFederate->quickResign();
	
	try
	{
		defaultFederate->rtiamb->sendInteraction( yHandle, *phvps, "NA" );
		failTestMissingException( "FederateNotExecutionMember",
		                          "sending interaction when not joined" );
	}
	catch( RTI::FederateNotExecutionMember &fnem )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "sending interaction when not joined" );
	}
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// TSO Test Send Methods //////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////
// TEST: (valid) testSendTSOInteraction() //
////////////////////////////////////////////
void SendInteractionTest::testSendTSOInteraction()
{
	try
	{
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->sendInteraction( yHandle, *phvps, theTime, "NA" );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while sending TSO interaction: %s", e._reason );
	}
	
	// shouldn't receive the reflection yet
	listenerFederate->fedamb->waitForTSOInteractionTimeout( yHandle );
	defaultFederate->quickAdvanceAndWait( 10.0 );
	listenerFederate->quickAdvanceAndWait( 10.0 );
	validateReceivedTSO();
}

////////////////////////////////////////////////////////////
// TEST: (valid) testSendTSOInteractionWithNoParameters() //
////////////////////////////////////////////////////////////
void SendInteractionTest::testSendTSOInteractionWithNoParameters()
{
	RTI::ParameterHandleValuePairSet *emptySet = defaultFederate->createPHVPS(0);
	
	try
	{
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->sendInteraction( yHandle, *emptySet, theTime, "NA" );
		delete emptySet;
	}
	catch( RTI::Exception &e )
	{
		delete emptySet;
		failTest( "Unexpected exception sending TSO interaction with empty params: %s", e._reason );
	}
	
	// wait for the interaciton and validate that it has no parameters
	listenerFederate->fedamb->waitForTSOInteractionTimeout( yHandle );
	defaultFederate->quickAdvanceAndWait( 10.0 );
	listenerFederate->quickAdvanceAndWait( 10.0 );
	// should be able to get it now
	Test13Interaction *interaction = listenerFederate->fedamb->waitForTSOInteraction( yHandle );
	int size = interaction->getSize();
	delete interaction;
	CPPUNIT_ASSERT_EQUAL( 0, size );
}

////////////////////////////////////////////////////
// TEST: testSendTSOInteractionWithInvalidClass() //
////////////////////////////////////////////////////
void SendInteractionTest::testSendTSOInteractionWithInvalidClass()
{
	try
	{
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->sendInteraction( 10000000, *phvps, theTime, "NA" );
		failTestMissingException( "InteractionClassNotDefined", 
		                          "sending interaction with wrong class handle" );
	}
	catch( RTI::InteractionClassNotDefined &icnd )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "InteractionClassNotDefined", e,
		                        "sending interaction with wrong class handle" );
	}

}

////////////////////////////////////////////////////////
// TEST: testSendTSOInteractionWithInvalidParameter() //
////////////////////////////////////////////////////////
void SendInteractionTest::testSendTSOInteractionWithInvalidParameter()
{
	RTI::ParameterHandleValuePairSet *params = defaultFederate->createPHVPS( 2 );
	params->add( xaHandle, "xa", 3 );
	params->add( 10000000, "no", 3 );
	
	try
	{
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->sendInteraction( yHandle, *params, theTime, "NA" );
		delete params;
		failTestMissingException( "InteractionParameterNotDefined", 
		                          "sending interaction with invalid param handle" );
	}
	catch( RTI::InteractionParameterNotDefined &ipnd )
	{
		// success!
		delete params;
	}
	catch( RTI::Exception &e )
	{
		delete params;
		failTestWrongException( "InteractionParameterNotDefined", e,
		                        "sending interaction with invalid param handle" );
	}
}

////////////////////////////////////////////////////
// TEST: testSendTSOInteractionWhenNotPublished() //
////////////////////////////////////////////////////
void SendInteractionTest::testSendTSOInteractionWhenNotPublished()
{
	try
	{
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->sendInteraction( xHandle, *phvps, theTime, "NA" );
		failTestMissingException( "InteractionClassNotPublished", 
		                          "sending interaction of class we don't publish" );
	}
	catch( RTI::InteractionClassNotPublished &icnp )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "InteractionClassNotPublished", e,
		                        "sending interaction of class we don't publish" );
	}
}

///////////////////////////////////////////////////
// TEST: testSendTSOInteractionWithInvalidTime() //
///////////////////////////////////////////////////
void SendInteractionTest::testSendTSOInteractionWithInvalidTime()
{
	try
	{
		// can't be -1.0 as that is the time used by Portico to represent no timestamp
		// thus, the LRC/RTI will process the request as an RO one, not a TSO with timestamp of -1
		RTIfedTime theTime = -11.0;
		defaultFederate->rtiamb->sendInteraction( yHandle, *phvps, theTime, "NA" );
		failTestMissingException( "InvalidFederationTime",
		                          "sending interaction with negative time" );
	}
	catch( RTI::InvalidFederationTime &fnem )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "InvalidFederationTime", e,
		                        "sending interaction with negative time" );
	}
}

//////////////////////////////////////////////////////////
// TEST: testSendTSOInteractionWithTimeBelowLookahead() //
//////////////////////////////////////////////////////////
void SendInteractionTest::testSendTSOInteractionWithTimeBelowLookahead()
{
	try
	{
		RTIfedTime theTime = 3.0;
		defaultFederate->rtiamb->sendInteraction( yHandle, *phvps, theTime, "NA" );
		failTestMissingException( "InvalidFederationTime",
		                          "sending interaction with time below lookahead" );
	}
	catch( RTI::InvalidFederationTime &fnem )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "InvalidFederationTime", e,
		                        "sending interaction with time below lookahead" );
	}
}

/////////////////////////////////////////////////
// TEST: testSendTSOInteractionWhenNotJoined() //
/////////////////////////////////////////////////
void SendInteractionTest::testSendTSOInteractionWhenNotJoined()
{
	defaultFederate->quickResign();
	
	try
	{
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->sendInteraction( yHandle, *phvps, theTime, "NA" );
		failTestMissingException( "FederateNotExecutionMember",
		                          "sending interaction when not joined" );
	}
	catch( RTI::FederateNotExecutionMember &fnem )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "sending interaction when not joined" );
	}
}
