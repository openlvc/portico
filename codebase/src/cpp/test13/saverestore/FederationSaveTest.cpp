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
#include "FederationSaveTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( FederationSaveTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( FederationSaveTest, "FederationSaveTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( FederationSaveTest, "SaveRestore" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
FederationSaveTest::FederationSaveTest()
{
	this->defaultFederate = new TestNG6Federate( "defaultFederate" );
	this->secondFederate = new TestNG6Federate( "secondFederate" );
	this->saveLabel = "FederationSaveTest";
}

FederationSaveTest::~FederationSaveTest()
{
	delete this->defaultFederate;
	delete this->secondFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void FederationSaveTest::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->secondFederate->quickJoin();
	
	// initialize time settings in the default federate
	defaultFederate->quickEnableAsync();
	defaultFederate->quickEnableRegulating( 1.0 );
	defaultFederate->quickEnableConstrained();
	// initialize time settings in the second federate
	secondFederate->quickEnableAsync();
	secondFederate->quickEnableRegulating( 1.0 );
	secondFederate->quickEnableConstrained();
	
	// advance time a little
	defaultFederate->quickAdvanceRequest( 5.0 );
	secondFederate->quickAdvanceAndWait( 5.0 );
	defaultFederate->fedamb->waitForTimeAdvance( 5.0 );
}

void FederationSaveTest::tearDown()
{
	this->secondFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

///////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////// Request Save Test Methods ////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////
// TEST: (valid) testRequestFederationSave() //
///////////////////////////////////////////////
void FederationSaveTest::testRequestFederationSave()
{
	try
	{
		defaultFederate->rtiamb->requestFederationSave( saveLabel );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while requesting federation save: %s", e._reason );
	}

	// make sure the proper callbacks are sent out
	defaultFederate->fedamb->waitForSaveInitiated( saveLabel );
	secondFederate->fedamb->waitForSaveInitiated( saveLabel );
}

////////////////////////////////////////////////////
// TEST: testRequestFederationSaveWhenNotJoined() //
////////////////////////////////////////////////////
void FederationSaveTest::testRequestFederationSaveWhenNotJoined()
{
	defaultFederate->quickResign();
	try
	{
		defaultFederate->rtiamb->requestFederationSave( this->saveLabel );
		failTestMissingException( "FederateNotExecutionMember",
		                          "requesting federation save when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "requesting federation save when not joined" );
	}
}

/////////////////////////////////////////////////////////
// TEST: testRequestFederationSaveWhenSaveInProgress() //
/////////////////////////////////////////////////////////
void FederationSaveTest::testRequestFederationSaveWhenSaveInProgress()
{
	defaultFederate->quickSaveInProgress( saveLabel );
	try
	{
		defaultFederate->rtiamb->requestFederationSave( "otherLabel" );
		failTestMissingException( "SaveInProgress", "requesting save while save in progress" );
	}
	catch( RTI::SaveInProgress& sip )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "SaveInProgress", e, "requesting save while save in progress" );
	}
}

////////////////////////////////////////////////////////////
// TEST: testRequestFederationSaveWhenRestoreInProgress() //
////////////////////////////////////////////////////////////
void FederationSaveTest::testRequestFederationSaveWhenRestoreInProgress()
{
	defaultFederate->quickRestoreInProgress( saveLabel, 2, defaultFederate, secondFederate );
	try
	{
		defaultFederate->rtiamb->requestFederationSave( saveLabel );
		failTestMissingException( "RestoreInProgress", "requesting save while restore in progress" );
	}
	catch( RTI::RestoreInProgress& rip )
	{
		// success
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException("RestoreInProgress", e, "requesting save while restore in progress");
	}
}

///////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////// Request Save With Time Test Methods ///////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////
// TEST: (valid) testTimestampedRequestFederationSave() //
//////////////////////////////////////////////////////////
void FederationSaveTest::testTimestampedRequestFederationSave()
{
	try
	{
		RTIfedTime time = 10.0;
		defaultFederate->rtiamb->requestFederationSave( saveLabel, time );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception requesting federation save @ time 10.0: %s", e._reason );
	}
	
	// we shouldn't get any callbacks yet, as neither federate is at 10.0
	defaultFederate->fedamb->waitForSaveInitiatedTimeout( saveLabel );
	secondFederate->fedamb->waitForSaveInitiatedTimeout( saveLabel );

	// advance the federates to 10.0
	defaultFederate->quickAdvanceRequest( 10.0 );
	secondFederate->quickAdvanceAndWait( 10.0 );
	defaultFederate->fedamb->waitForTimeAdvance( 10.0 );

	// now we should get the save notification
	defaultFederate->fedamb->waitForSaveInitiated( saveLabel );
	secondFederate->fedamb->waitForSaveInitiated( saveLabel );
}

////////////////////////////////////////////////////////////////
// TEST: testTimestampedRequestFederationSaveWithTimeInPast() //
////////////////////////////////////////////////////////////////
void FederationSaveTest::testTimestampedRequestFederationSaveWithTimeInPast()
{
	try
	{
		RTIfedTime theTime = 1.0;
		defaultFederate->rtiamb->requestFederationSave( this->saveLabel, theTime );
		failTestMissingException( "FederateNotExecutionMember",
		                          "requesting federation save with time in past" );
	}
	catch( RTI::FederationTimeAlreadyPassed& ftap )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederationTimeAlreadyPassed", e,
		                        "requesting federation save with time in past" );
	}
}

///////////////////////////////////////////////////////////////
// TEST: testTimestampedRequestFederationSaveWhenNotJoined() //
///////////////////////////////////////////////////////////////
void FederationSaveTest::testTimestampedRequestFederationSaveWhenNotJoined()
{
	defaultFederate->quickResign();
	try
	{
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->requestFederationSave( this->saveLabel, theTime );
		failTestMissingException( "FederateNotExecutionMember",
		                          "requesting federation save when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "requesting federation save when not joined" );
	}
}

////////////////////////////////////////////////////////////////////
// TEST: testTimestampedRequestFederationSaveWhenSaveInProgress() //
////////////////////////////////////////////////////////////////////
void FederationSaveTest::testTimestampedRequestFederationSaveWhenSaveInProgress()
{
	defaultFederate->quickSaveInProgress( saveLabel );
	try
	{
		RTIfedTime time = 10.0;
		defaultFederate->rtiamb->requestFederationSave( saveLabel, time );
		failTestMissingException( "SaveInProgress",
		                          "requesting timestampped save while save in progress" );
	}
	catch( RTI::SaveInProgress& sip )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "SaveInProgress", e,
		                        "requesting timestampped save while save in progress" );
	}
}

///////////////////////////////////////////////////////////////////////
// TEST: testTimestampedRequestFederationSaveWhenRestoreInProgress() //
///////////////////////////////////////////////////////////////////////
void FederationSaveTest::testTimestampedRequestFederationSaveWhenRestoreInProgress()
{
	defaultFederate->quickRestoreInProgress( saveLabel, 2, defaultFederate, secondFederate );
	try
	{
		RTIfedTime time = 100.0;
		defaultFederate->rtiamb->requestFederationSave( saveLabel, time );
		failTestMissingException( "RestoreInProgress",
		                          "requesting timestamped save while restore in progress" );
	}
	catch( RTI::RestoreInProgress& rip )
	{
		// success
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException("RestoreInProgress", e,
		                       "requesting timestamped save while restore in progress");
	}
}

///////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////// Save Begun Test Methods /////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////
// TEST: (valid) testFederateSaveBegun() //
///////////////////////////////////////////
void FederationSaveTest::testFederateSaveBegun()
{
	defaultFederate->quickSaveRequest( saveLabel );
	defaultFederate->fedamb->waitForSaveInitiated( saveLabel );
	secondFederate->fedamb->waitForSaveInitiated( saveLabel );
	
	try
	{
		defaultFederate->rtiamb->federateSaveBegun();
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception informing RTI that federate has begun saving: %s", e._reason );
	}
	
	// the call didn't result in an error, so we must be good.
}

////////////////////////////////////////////////////
// TEST: testFederateSaveBegunWithoutActiveSave() //
////////////////////////////////////////////////////
void FederationSaveTest::testFederateSaveBegunWithoutActiveSave()
{
	try
	{
		defaultFederate->rtiamb->federateSaveBegun();
		failTestMissingException( "SaveNotInitiated",
		                          "informing federate save begun without active save" );
	}
	catch( RTI::SaveNotInitiated& sni )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "SaveNotInitiated", e,
		                        "informing federate save begun without active save" );
	}
}

////////////////////////////////////////////////
// TEST: testFederateSaveBegunWhenNotJoined() //
////////////////////////////////////////////////
void FederationSaveTest::testFederateSaveBegunWhenNotJoined()
{
	defaultFederate->quickResign();
	try
	{
		defaultFederate->rtiamb->federateSaveBegun();
		failTestMissingException( "FederateNotExecutionMember",
		                          "informing federate save begun" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "informing federate save begun" );
	}
}

////////////////////////////////////////////////////////
// TEST: testFederateSaveBegunWhenRestoreInProgress() //
////////////////////////////////////////////////////////
void FederationSaveTest::testFederateSaveBegunWhenRestoreInProgress()
{
	defaultFederate->quickRestoreInProgress( saveLabel, 2, defaultFederate, secondFederate );
	try
	{
		defaultFederate->rtiamb->federateSaveBegun();
		failTestMissingException( "RestoreInProgress",
		                          "informing federate save begun while restore in progress" );
	}
	catch( RTI::RestoreInProgress& rip )
	{
		// success
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException("RestoreInProgress", e,
		                       "informing federate save begun while restore in progress");
	}
}

///////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////// Save Complete Test Methods ///////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////
// TEST: (valid) testFederateSaveComplete() //
//////////////////////////////////////////////
void FederationSaveTest::testFederateSaveComplete()
{
	defaultFederate->quickSaveRequest( saveLabel );
	defaultFederate->fedamb->waitForSaveInitiated( saveLabel );
	secondFederate->fedamb->waitForSaveInitiated( saveLabel );
	defaultFederate->quickSaveBegun();
	secondFederate->quickSaveBegun();
	
	try
	{
		defaultFederate->rtiamb->federateSaveComplete();
		secondFederate->rtiamb->federateSaveComplete();
	}
	catch( RTI::Exception& e )
	{
		failTest( "Informing RTI that a save has been completed: %s", e._reason );
	}

	// all are complete, wait for notification that federation has saved happily
	defaultFederate->fedamb->waitForFederationSaved();
	secondFederate->fedamb->waitForFederationSaved();	
}

///////////////////////////////////////////////////////
// TEST: testFederateSaveCompleteWithoutActiveSave() //
///////////////////////////////////////////////////////
void FederationSaveTest::testFederateSaveCompleteWithoutActiveSave()
{
	try
	{
		defaultFederate->rtiamb->federateSaveComplete();
		failTestMissingException( "SaveNotInitiated",
		                          "informing federate save complete without active save" );
	}
	catch( RTI::SaveNotInitiated& sni )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "SaveNotInitiated", e,
		                        "informing federate save complete without active save" );
	}
}

///////////////////////////////////////////////////
// TEST: testFederateSaveCompleteWhenNotJoined() //
///////////////////////////////////////////////////
void FederationSaveTest::testFederateSaveCompleteWhenNotJoined()
{
	defaultFederate->quickResign();
	try
	{
		defaultFederate->rtiamb->federateSaveComplete();
		failTestMissingException( "FederateNotExecutionMember","informing federate save complete" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "informing federate save complete" );
	}
}

///////////////////////////////////////////////////////////
// TEST: testFederateSaveCompleteWhenRestoreInProgress() //
///////////////////////////////////////////////////////////
void FederationSaveTest::testFederateSaveCompleteWhenRestoreInProgress()
{
	defaultFederate->quickRestoreInProgress( saveLabel, 2, defaultFederate, secondFederate );
	try
	{
		defaultFederate->rtiamb->federateSaveComplete();
		failTestMissingException( "RestoreInProgress",
		                          "informing federate save complete while restore in progress" );
	}
	catch( RTI::RestoreInProgress& rip )
	{
		// success
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException("RestoreInProgress", e,
		                       "informing federate save complete while restore in progress");
	}
}

///////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////// Save Not Complete Test Methods /////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////
// TEST: (valid) testFederateSaveNotCompleted() //
//////////////////////////////////////////////////
void FederationSaveTest::testFederateSaveNotCompleted()
{
	defaultFederate->quickSaveRequest( saveLabel );
	defaultFederate->fedamb->waitForSaveInitiated( saveLabel );
	secondFederate->fedamb->waitForSaveInitiated( saveLabel );
	defaultFederate->quickSaveBegun();
	secondFederate->quickSaveBegun();
	defaultFederate->quickSaveComplete();
	
	try
	{
		secondFederate->rtiamb->federateSaveNotComplete();
	}
	catch( RTI::Exception& e )
	{
		failTest( "Informing RTI that a save has been not completed: %s", e._reason );
	}

	// all are complete, wait for notification that federation has saved happily
	defaultFederate->fedamb->waitForFederationNotSaved();
	secondFederate->fedamb->waitForFederationNotSaved();
}

///////////////////////////////////////////////////////////
// TEST: testFederateSaveNotCompletedWithoutActiveSave() //
///////////////////////////////////////////////////////////
void FederationSaveTest::testFederateSaveNotCompletedWithoutActiveSave()
{
	try
	{
		defaultFederate->rtiamb->federateSaveNotComplete();
		failTestMissingException( "SaveNotInitiated",
		                          "informing federate save not complete without active save" );
	}
	catch( RTI::SaveNotInitiated& sni )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "SaveNotInitiated", e,
		                        "informing federate save not complete without active save" );
	}
}

///////////////////////////////////////////////////////
// TEST: testFederateSaveNotCompletedWhenNotJoined() //
///////////////////////////////////////////////////////
void FederationSaveTest::testFederateSaveNotCompletedWhenNotJoined()
{
	defaultFederate->quickResign();
	try
	{
		defaultFederate->rtiamb->federateSaveNotComplete();
		failTestMissingException( "FederateNotExecutionMember",
		                          "informing federate save not complete" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "informing federate save not complete" );
	}
}

///////////////////////////////////////////////////////////////
// TEST: testFederateSaveNotCompletedWhenRestoreInProgress() //
///////////////////////////////////////////////////////////////
void FederationSaveTest::testFederateSaveNotCompletedWhenRestoreInProgress()
{
	defaultFederate->quickRestoreInProgress( saveLabel, 2, defaultFederate, secondFederate );
	try
	{
		defaultFederate->rtiamb->federateSaveNotComplete();
		failTestMissingException( "RestoreInProgress",
		                          "informing federate save not complete while restore in progress" );
	}
	catch( RTI::RestoreInProgress& rip )
	{
		// success
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException("RestoreInProgress", e,
		                       "informing federate save not complete while restore in progress");
	}
}
