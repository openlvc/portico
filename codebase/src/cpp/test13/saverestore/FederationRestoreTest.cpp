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
#include "FederationRestoreTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( FederationRestoreTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( FederationRestoreTest, "FederationRestoreTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( FederationRestoreTest, "SaveRestore" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
FederationRestoreTest::FederationRestoreTest()
{
	this->defaultFederate = new TestNG6Federate( "defaultFederate" );
	this->secondFederate = new TestNG6Federate( "secondFederate" );
	this->saveLabel = "FederationSaveTest";
}

FederationRestoreTest::~FederationRestoreTest()
{
	delete this->defaultFederate;
	delete this->secondFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void FederationRestoreTest::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->secondFederate->quickJoin();
	
	defaultFederate->quickSaveToCompletion( saveLabel, 2, defaultFederate, secondFederate );
}

void FederationRestoreTest::tearDown()
{
	this->secondFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Request Restore Test Methods ///////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////
// TEST: (valid) testRequestFederationRestore() //
//////////////////////////////////////////////////
void FederationRestoreTest::testRequestFederationRestore()
{
	try
	{
		defaultFederate->rtiamb->requestFederationRestore( saveLabel );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while requesting federation restore: %s", e._reason );
	}
	
	defaultFederate->fedamb->waitForRestoreRequestSuccess( saveLabel );
}

///////////////////////////////////////////////////////
// TEST: testRequestFederationRestoreWhenNotJoined() //
///////////////////////////////////////////////////////
void FederationRestoreTest::testRequestFederationRestoreWhenNotJoined()
{
	defaultFederate->quickResign();
	try
	{
		defaultFederate->rtiamb->requestFederationRestore( saveLabel );
		failTestMissingException( "FederateNotExecutionMember",
		                          "requesting federation restore when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "requesting federation restore when not joined" );
	}
}

////////////////////////////////////////////////////////////
// TEST: testRequestFederationRestoreWhenSaveInProgress() //
////////////////////////////////////////////////////////////
void FederationRestoreTest::testRequestFederationRestoreWhenSaveInProgress()
{
	defaultFederate->quickSaveInProgress( saveLabel );
	try
	{
		defaultFederate->rtiamb->requestFederationRestore( saveLabel );
		failTestMissingException( "SaveInProgress",
		                          "requesting federation restore when save in progress" );
	}
	catch( RTI::SaveInProgress& sip )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "SaveInProgress", e,
		                        "requesting federation restore when save in progress" );
	}
}

///////////////////////////////////////////////////////////////
// TEST: testRequestFederationRestoreWhenRestoreInProgress() //
///////////////////////////////////////////////////////////////
void FederationRestoreTest::testRequestFederationRestoreWhenRestoreInProgress()
{
	defaultFederate->quickRestoreInProgress( saveLabel, 2, defaultFederate, secondFederate );
	try
	{
		defaultFederate->rtiamb->requestFederationRestore( saveLabel );
		failTestMissingException( "RestoreInProgress",
		                          "requesting federation restore when restore in progress" );
	}
	catch( RTI::RestoreInProgress& rip )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "RestoreInProgress", e,
		                        "requesting federation restore when restore in progress" );
	}
}


////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////// Restore Complete Test Methods ///////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////
// TEST: (valid) testFederationRestoreComplete() //
///////////////////////////////////////////////////
void FederationRestoreTest::testFederationRestoreComplete()
{
	defaultFederate->quickRestoreRequest( saveLabel );
	defaultFederate->fedamb->waitForRestoreRequestSuccess( saveLabel );
	defaultFederate->fedamb->waitForFederationRestoreBegun();
	secondFederate->fedamb->waitForFederationRestoreBegun();
	defaultFederate->fedamb->waitForFederateRestoreInitiated( saveLabel );
	secondFederate->fedamb->waitForFederateRestoreInitiated( saveLabel );
	
	try
	{
		defaultFederate->rtiamb->federateRestoreComplete();
		secondFederate->rtiamb->federateRestoreComplete();
	}
	catch( RTI::Exception& e )
	{
		failTest( "Informing the RTI that federate has completed restoring: %s", e._reason );
	}

	defaultFederate->fedamb->waitForFederationRestored();
	secondFederate->fedamb->waitForFederationRestored();
}

///////////////////////////////////////////////////////////////
// TEST: testFederationRestoreCompleteWithoutActiveRestore() //
///////////////////////////////////////////////////////////////
void FederationRestoreTest::testFederationRestoreCompleteWithoutActiveRestore()
{
	try
	{
		defaultFederate->rtiamb->federateRestoreComplete();
		failTestMissingException( "RestoreNotRequested",
		                          "informing federate restore complete without active restore" );
	}
	catch( RTI::RestoreNotRequested& rnr )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "RestoreNotRequested", e,
		                        "informing federate restore complete without active restore" );
	}
}

////////////////////////////////////////////////////////
// TEST: testFederationRestoreCompleteWhenNotJoined() //
////////////////////////////////////////////////////////
void FederationRestoreTest::testFederationRestoreCompleteWhenNotJoined()
{
	defaultFederate->quickResign();
	try
	{
		defaultFederate->rtiamb->federateRestoreComplete();
		failTestMissingException( "FederateNotExecutionMember",
		                          "informing federate restore complete when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "informing federate restore complete when not joined" );
	}
}

/////////////////////////////////////////////////////////////
// TEST: testFederationRestoreCompleteWhenSaveInProgress() //
/////////////////////////////////////////////////////////////
void FederationRestoreTest::testFederationRestoreCompleteWhenSaveInProgress()
{
	defaultFederate->quickSaveInProgress( saveLabel );
	try
	{
		defaultFederate->rtiamb->federateRestoreComplete();
		failTestMissingException( "SaveInProgress",
		                          "informing federate restore complete when save in progress" );
	}
	catch( RTI::SaveInProgress& sip )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "SaveInProgress", e,
		                        "informing federate restore complete when save in progress" );
	}
}

////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// Restore Not Complete Test Methods ////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////
// TEST: (valid) testFederationRestoreNotComplete() //
//////////////////////////////////////////////////////
void FederationRestoreTest::testFederationRestoreNotComplete()
{
	defaultFederate->quickRestoreRequest( saveLabel );
	defaultFederate->fedamb->waitForRestoreRequestSuccess( saveLabel );
	defaultFederate->fedamb->waitForFederationRestoreBegun();
	secondFederate->fedamb->waitForFederationRestoreBegun();
	defaultFederate->fedamb->waitForFederateRestoreInitiated( saveLabel );
	secondFederate->fedamb->waitForFederateRestoreInitiated( saveLabel );
	defaultFederate->quickRestoreComplete();
	
	try
	{
		secondFederate->rtiamb->federateRestoreNotComplete();
	}
	catch( RTI::Exception& e )
	{
		failTest( "Informing the RTI that federate has unsuccessfully completed restoring: %s", e._reason );
	}

	defaultFederate->fedamb->waitForFederationNotRestored();
	secondFederate->fedamb->waitForFederationNotRestored();
}

//////////////////////////////////////////////////////////////////
// TEST: testFederationRestoreNotCompleteWithoutActiveRestore() //
//////////////////////////////////////////////////////////////////
void FederationRestoreTest::testFederationRestoreNotCompleteWithoutActiveRestore()
{
	try
	{
		defaultFederate->rtiamb->federateRestoreNotComplete();
		failTestMissingException( "RestoreNotRequested",
		                          "informing federate restore not complete without active restore" );
	}
	catch( RTI::RestoreNotRequested& rnr )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "RestoreNotRequested", e,
		                        "informing federate restore not complete without active restore" );
	}
}

///////////////////////////////////////////////////////////
// TEST: testFederationRestoreNotCompleteWhenNotJoined() //
///////////////////////////////////////////////////////////
void FederationRestoreTest::testFederationRestoreNotCompleteWhenNotJoined()
{
	defaultFederate->quickResign();
	try
	{
		defaultFederate->rtiamb->federateRestoreNotComplete();
		failTestMissingException( "FederateNotExecutionMember",
		                          "informing federate restore not complete when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "informing federate restore not complete when not joined" );
	}
}

////////////////////////////////////////////////////////////////
// TEST: testFederationRestoreNotCompleteWhenSaveInProgress() //
////////////////////////////////////////////////////////////////
void FederationRestoreTest::testFederationRestoreNotCompleteWhenSaveInProgress()
{
	defaultFederate->quickSaveInProgress( saveLabel );
	try
	{
		defaultFederate->rtiamb->federateRestoreNotComplete();
		failTestMissingException( "SaveInProgress",
		                          "informing federate restore not complete when save in progress" );
	}
	catch( RTI::SaveInProgress& sip )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "SaveInProgress", e,
		                        "informing federate restore not complete when save in progress" );
	}
}
