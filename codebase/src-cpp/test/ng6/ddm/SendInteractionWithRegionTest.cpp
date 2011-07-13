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
#include "SendInteractionWithRegionTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( SendInteractionWithRegionTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SendInteractionWithRegionTest,
                                       "SendInteractionWithRegionTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SendInteractionWithRegionTest, "sendInteractionWithRegion" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SendInteractionWithRegionTest, "ddm" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
SendInteractionWithRegionTest::SendInteractionWithRegionTest()
{
	this->defaultFederate = new TestNG6Federate( "defaultFederate" );
	this->listenerFederate = new TestNG6Federate( "listenerFederate" );
}

SendInteractionWithRegionTest::~SendInteractionWithRegionTest()
{
	delete this->defaultFederate;
	delete this->listenerFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void SendInteractionWithRegionTest::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->listenerFederate->quickJoin();
	
	// get the class handles
	this->xHandle = defaultFederate->quickICHandle( "InteractionRoot.X" );
	this->xaHandle = defaultFederate->quickPCHandle( "InteractionRoot.X", "xa" );
	this->xbHandle = defaultFederate->quickPCHandle( "InteractionRoot.X", "xb" );
	this->xcHandle = defaultFederate->quickPCHandle( "InteractionRoot.X", "xc" );
	
	// create the regions
	this->senderRegion = defaultFederate->quickCreateTestRegion( 100, 200 );
	this->senderRegionOOB = defaultFederate->quickCreateTestRegion( 1000, 2000 );
	this->listenerRegion = listenerFederate->quickCreateTestRegion( 150, 250 );
	this->otherRegion = defaultFederate->quickCreateOtherRegion( 100, 200 );

	// do publication and subscription
	this->defaultFederate->quickPublish( "InteractionRoot.X" );
	this->listenerFederate->quickSubscribeWithRegion( "InteractionRoot.X", listenerRegion );
	
	// enable the time profiles
	defaultFederate->quickEnableRegulating( 5.0 );
	listenerFederate->quickEnableAsync();
	listenerFederate->quickEnableConstrained();
	
	// fill out the phvps
	this->phvps = defaultFederate->createPHVPS( 3 );
	this->phvps->add( xaHandle, "xa", 3 );
	this->phvps->add( xbHandle, "xb", 3 );
	this->phvps->add( xcHandle, "xc", 3 );
}

void SendInteractionWithRegionTest::tearDown()
{
	delete this->otherRegion;
	delete this->senderRegion;
	delete this->senderRegionOOB;
	delete this->listenerRegion;
	
	this->listenerFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////// Test Helper Methods ///////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

void SendInteractionWithRegionTest::validateReceivedRO()
{
	// wait for the listener to receive it
	TestNG6Interaction *received = listenerFederate->fedamb->waitForROInteraction( xHandle );
	
	// validate the contents of the interaction
	int result = strcmp( "xa", received->getParameter(xaHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "xb", received->getParameter(xbHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "xc", received->getParameter(xcHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	
	// clean up
	delete received;
}

void SendInteractionWithRegionTest::validateReceivedTSO()
{
	// wait for the listener to receive it
	TestNG6Interaction *received = listenerFederate->fedamb->waitForTSOInteraction( xHandle );
	
	// validate the contents of the interaction
	int result = strcmp( "xa", received->getParameter(xaHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "xb", received->getParameter(xbHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );
	result = strcmp( "xc", received->getParameter(xcHandle) );
	CPPUNIT_ASSERT_EQUAL( 0, result );

	// clean up
	delete received;
}

////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////// Receive Order Test Methods ////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
// sendInteractionWithRegion( RTI::InteractionClassHandle theInteraction,
//                            const RTI::ParameterHandleValuePairSet &parameters,
//                            const char *theTag,
//                            const RTI::Region &theRegion )
//     throw( RTI::InteractionClassNotDefined,
//            RTI::InteractionClassNotPublished,
//            RTI::InteractionParameterNotDefined,
//            RTI::RegionNotKnown,
//            RTI::InvalidRegionContext,
//            RTI::FederateNotExecutionMember,
//            RTI::ConcurrentAccessAttempted,
//            RTI::SaveInProgress,
//            RTI::RestoreInProgress,
//            RTI::RTIinternalError )                                               

/////////////////////////////////////////////////////
// TEST: (valid) testSendROInteractionWithRegion() //
/////////////////////////////////////////////////////
/*
 * This test will first send an instance of InteractionRoot.X with a region that overlaps with the
 * region that listener federate has subscribed to. It will then send another interaction with a
 * region that doesn't overlap with the listener (and validate that the interaction isnt' recevied)
 */
void SendInteractionWithRegionTest::testSendROInteractionWithRegion()
{
	// send an interaction with a region that overlaps with the subscribed region
	try
	{
		defaultFederate->rtiamb->sendInteractionWithRegion( xHandle, *phvps, "NA", *senderRegion );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while sending RO interaction: %s", e._reason );
	}
	
	// validate that the interaction is received
	validateReceivedRO();
	
	// send an interaction with a region that DOESN'T overlap
	try
	{
		defaultFederate->rtiamb->sendInteractionWithRegion( xHandle, *phvps, "NA", *senderRegionOOB );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while sending RO interaction: %s", e._reason );
	}
	
	// validate that the interaction is NOT received
	listenerFederate->fedamb->waitForROInteractionTimeout( xHandle );
}

////////////////////////////////////////////////////////////////////
// TEST: (valid) testSendROInteractionWithRegionAndNoParameters() //
////////////////////////////////////////////////////////////////////
void SendInteractionWithRegionTest::testSendROInteractionWithRegionAndNoParameters()
{
	RTI::ParameterHandleValuePairSet *emptySet = defaultFederate->createPHVPS(0);
	
	try
	{
		defaultFederate->rtiamb->sendInteractionWithRegion( xHandle, *emptySet, "NA", *senderRegion );
		delete emptySet;
	}
	catch( RTI::Exception &e )
	{
		delete emptySet;
		failTest( "Unexpected exception sending RO interaction with empty params: %s", e._reason );
	}
	
	// wait for the interaciton and validate that it has no parameters
	TestNG6Interaction *interaction = listenerFederate->fedamb->waitForROInteraction( xHandle );
	int size = interaction->getSize();
	delete interaction;
	CPPUNIT_ASSERT_EQUAL( 0, size );
}

////////////////////////////////////////////////////////////
// TEST: testSendROInteractionWithRegionAndInvalidClass() //
////////////////////////////////////////////////////////////
void SendInteractionWithRegionTest::testSendROInteractionWithRegionAndInvalidClass()
{
	try
	{
		defaultFederate->rtiamb->sendInteractionWithRegion( 10000000, *phvps, "NA", *senderRegion );
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

////////////////////////////////////////////////////////////////
// TEST: testSendROInteractionWithRegionAndInvalidParameter() //
////////////////////////////////////////////////////////////////
void SendInteractionWithRegionTest::testSendROInteractionWithRegionAndInvalidParameter()
{
	RTI::ParameterHandleValuePairSet *params = defaultFederate->createPHVPS( 2 );
	params->add( xaHandle, "xa", 3 );
	params->add( 10000000, "no", 3 );
	
	try
	{
		defaultFederate->rtiamb->sendInteractionWithRegion( xHandle, *params, "NA", *senderRegion );
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

///////////////////////////////////////////////////////////
// TEST: testSendROInteractionWithRegionThatIsNotKnown() //
///////////////////////////////////////////////////////////
void SendInteractionWithRegionTest::testSendROInteractionWithRegionThatIsNotKnown()
{
	try
	{
		defaultFederate->rtiamb->sendInteractionWithRegion( xHandle, *phvps, "NA", *listenerRegion );
		failTestMissingException( "RegionNotKnown",
		                          "sending interaction with region create in different federate" );
	}
	catch( RTI::RegionNotKnown &rnk )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "RegionNotKnown", e, 
		                        "sending interaction with region create in different federate" );
	}
}

///////////////////////////////////////////////////////////
// TEST: testSendROInteractionWithRegionThatIsNotValid() //
///////////////////////////////////////////////////////////
void SendInteractionWithRegionTest::testSendROInteractionWithRegionThatIsNotValid()
{
	try
	{
		defaultFederate->rtiamb->sendInteractionWithRegion( xHandle, *phvps, "NA", *otherRegion );
		failTestMissingException( "InvalidRegionContext",
		                          "sending interaction with invalid region for interaction class" );
	}
	catch( RTI::InvalidRegionContext &irc )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "InvalidRegionContext", e,
		                        "sending interaction with invalid region for interaction class" );
	}
}

/////////////////////////////////////////////////////////////
// TEST: testSendROInteractionWithRegionWhenNotPublished() //
/////////////////////////////////////////////////////////////
void SendInteractionWithRegionTest::testSendROInteractionWithRegionWhenNotPublished()
{
	try
	{
		RTI::InteractionClassHandle yHandle = defaultFederate->quickICHandle( "InteractionRoot.X.Y" );
		defaultFederate->rtiamb->sendInteractionWithRegion( yHandle, *phvps, "NA", *senderRegion );
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

//////////////////////////////////////////////////////////
// TEST: testSendROInteractionWithRegionWhenNotJoined() //
//////////////////////////////////////////////////////////
void SendInteractionWithRegionTest::testSendROInteractionWithRegionWhenNotJoined()
{
	defaultFederate->quickResign();
	
	try
	{
		defaultFederate->rtiamb->sendInteractionWithRegion( xHandle, *phvps, "NA", *senderRegion );
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
/////////////////////////////// Timestamp Order Test Methods ///////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
// sendInteractionWithRegion( RTI::InteractionClassHandle theInteraction,
//                            const RTI::ParameterHandleValuePairSet &parameters,
//                            const RTI::FedTime& theTime,
//                            const char *theTag,
//                            const RTI::Region &theRegion )
//     throw( RTI::InteractionClassNotDefined,
//            RTI::InteractionClassNotPublished,
//            RTI::InteractionParameterNotDefined,
//            RTI::InvalidFederationTime,
//            RTI::RegionNotKnown,
//            RTI::InvalidRegionContext,
//            RTI::FederateNotExecutionMember,
//            RTI::ConcurrentAccessAttempted,
//            RTI::SaveInProgress,
//            RTI::RestoreInProgress,
//            RTI::RTIinternalError )

/////////////////////////////////////////////////////
// TEST: (valid) testSendROInteractionWithRegion() //
/////////////////////////////////////////////////////
/*
 * This test will first send an instance of InteractionRoot.X with a region that overlaps with the
 * region that listener federate has subscribed to. It will then send another interaction with a
 * region that doesn't overlap with the listener (and validate that the interaction isnt' recevied)
 */
void SendInteractionWithRegionTest::testSendTSOInteractionWithRegion()
{
	try
	{
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->sendInteractionWithRegion(
		                             xHandle, *phvps, theTime, "NA", *senderRegion );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while sending TSO interaction: %s", e._reason );
	}
	
	// shouldn't receive the interaction until we advance enough
	listenerFederate->fedamb->waitForTSOInteractionTimeout( xHandle );
	defaultFederate->quickAdvanceAndWait( 10.0 );
	listenerFederate->quickAdvanceAndWait( 10.0 );
	validateReceivedTSO();

	try
	{
		RTIfedTime theTime = 20.0;
		defaultFederate->rtiamb->sendInteractionWithRegion(
		                             xHandle, *phvps, theTime, "NA", *senderRegionOOB );
	}
	catch( RTI::Exception &e )
	{
		failTest( "Unexpected exception while sending TSO interaction: %s", e._reason );
	}
	
	// shouldn't receive the interaction at all
	defaultFederate->quickAdvanceAndWait( 20.0 );
	listenerFederate->quickAdvanceAndWait( 20.0 );
	listenerFederate->fedamb->waitForTSOInteractionTimeout( xHandle );
}

/////////////////////////////////////////////////////
// TEST: (valid) testSendROInteractionWithRegion() //
/////////////////////////////////////////////////////
void SendInteractionWithRegionTest::testSendTSOInteractionWithRegionAndNoParameters()
{
	RTI::ParameterHandleValuePairSet *emptySet = defaultFederate->createPHVPS(0);
	
	try
	{
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->sendInteractionWithRegion(
		                             xHandle, *emptySet, theTime, "NA", *senderRegion );
		delete emptySet;
	}
	catch( RTI::Exception &e )
	{
		delete emptySet;
		failTest( "Unexpected exception sending TSO interaction with empty params: %s", e._reason );
	}
	
	// wait for the interaciton and validate that it has no parameters
	listenerFederate->fedamb->waitForTSOInteractionTimeout( xHandle );
	defaultFederate->quickAdvanceAndWait( 10.0 );
	listenerFederate->quickAdvanceAndWait( 10.0 );
	// should be able to get it now
	TestNG6Interaction *interaction = listenerFederate->fedamb->waitForTSOInteraction( xHandle );
	int size = interaction->getSize();
	delete interaction;
	CPPUNIT_ASSERT_EQUAL( 0, size );
}

/////////////////////////////////////////////////////////////
// TEST: testSendTSOInteractionWithRegionAndInvalidClass() //
/////////////////////////////////////////////////////////////
void SendInteractionWithRegionTest::testSendTSOInteractionWithRegionAndInvalidClass()
{
	try
	{
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->sendInteractionWithRegion(
		                             10000000, *phvps, theTime, "NA", *senderRegion );
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

/////////////////////////////////////////////////////////////////
// TEST: testSendTSOInteractionWithRegionAndInvalidParameter() //
/////////////////////////////////////////////////////////////////
void SendInteractionWithRegionTest::testSendTSOInteractionWithRegionAndInvalidParameter()
{
	RTI::ParameterHandleValuePairSet *params = defaultFederate->createPHVPS( 2 );
	params->add( xaHandle, "xa", 3 );
	params->add( 10000000, "no", 3 );
	
	try
	{
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->sendInteractionWithRegion(
		                             xHandle, *params, theTime, "NA", *senderRegion );
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

////////////////////////////////////////////////////////////
// TEST: testSendTSOInteractionWithRegionThatIsNotKnown() //
////////////////////////////////////////////////////////////
void SendInteractionWithRegionTest::testSendTSOInteractionWithRegionThatIsNotKnown()
{
	try
	{
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->sendInteractionWithRegion(
		                             xHandle, *phvps, theTime, "NA", *listenerRegion );
		failTestMissingException( "RegionNotKnown",
		                          "sending interaction with region create in different federate" );
	}
	catch( RTI::RegionNotKnown &rnk )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "RegionNotKnown", e, 
		                        "sending interaction with region create in different federate" );
	}
}

////////////////////////////////////////////////////////////
// TEST: testSendTSOInteractionWithRegionThatIsNotValid() //
////////////////////////////////////////////////////////////
void SendInteractionWithRegionTest::testSendTSOInteractionWithRegionThatIsNotValid()
{
	try
	{
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->sendInteractionWithRegion(
		                             xHandle, *phvps, theTime, "NA", *otherRegion );
		failTestMissingException( "InvalidRegionContext",
		                          "sending interaction with invalid region for interaction class" );
	}
	catch( RTI::InvalidRegionContext &irc )
	{
		// success!
	}
	catch( RTI::Exception &e )
	{
		failTestWrongException( "InvalidRegionContext", e,
		                        "sending interaction with invalid region for interaction class" );
	}
}

//////////////////////////////////////////////////////////////
// TEST: testSendTSOInteractionWithRegionWhenNotPublished() //
//////////////////////////////////////////////////////////////
void SendInteractionWithRegionTest::testSendTSOInteractionWithRegionWhenNotPublished()
{
	try
	{
		RTIfedTime theTime = 10.0;
		RTI::InteractionClassHandle yHandle =
			defaultFederate->quickICHandle( "InteractionRoot.X.Y" );
		defaultFederate->rtiamb->sendInteractionWithRegion(
		                             yHandle, *phvps, theTime, "NA", *senderRegion );
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

/////////////////////////////////////////////////////////////
// TEST: testSendTSOInteractionWithRegionWithInvalidTime() //
/////////////////////////////////////////////////////////////
void SendInteractionWithRegionTest::testSendTSOInteractionWithRegionWithInvalidTime()
{
	try
	{
		// can't be -1.0 as that is the time used by Portico to represent no timestamp
		// thus, the LRC/RTI will process the request as an RO one, not a TSO with timestamp of -1
		RTIfedTime theTime = -11.0;
		defaultFederate->rtiamb->sendInteractionWithRegion(
		                             xHandle, *phvps, theTime, "NA", *senderRegion );
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

////////////////////////////////////////////////////////////////////
// TEST: testSendTSOInteractionWithRegionWithTimeBelowLookahead() //
////////////////////////////////////////////////////////////////////
void SendInteractionWithRegionTest::testSendTSOInteractionWithRegionWithTimeBelowLookahead()
{
	try
	{
		RTIfedTime theTime = 3.0;
		defaultFederate->rtiamb->sendInteractionWithRegion(
		                             xHandle, *phvps, theTime, "NA", *senderRegion );
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

///////////////////////////////////////////////////////////
// TEST: testSendTSOInteractionWithRegionWhenNotJoined() //
///////////////////////////////////////////////////////////
void SendInteractionWithRegionTest::testSendTSOInteractionWithRegionWhenNotJoined()
{
	defaultFederate->quickResign();
	
	try
	{
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->sendInteractionWithRegion(
		                             xHandle, *phvps, theTime, "NA", *senderRegion );
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
