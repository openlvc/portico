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
#include "SynchronizationTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( SynchronizationTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SynchronizationTest, "SynchronizationTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SynchronizationTest, "syncPointAchieved" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SynchronizationTest, "synchronization" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( SynchronizationTest, "federationManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
SynchronizationTest::SynchronizationTest()
{
	this->federateOne = new Test13Federate( "federateOne" );
	this->federateTwo = new Test13Federate( "federateTwo" );
	this->pointOne = new char[32];
	sprintf( this->pointOne, "SynchronizationPoint" );
	this->tag = new char[8];
	sprintf( this->tag, "letag" );
}

SynchronizationTest::~SynchronizationTest()
{
	delete this->federateOne;
	delete this->federateTwo;
	delete [] this->pointOne;
	delete [] this->tag;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void SynchronizationTest::setUp()
{
    this->federateOne->quickCreate();
    this->federateOne->quickJoin();
    this->federateTwo->quickJoin();
}

void SynchronizationTest::tearDown()
{
	this->federateTwo->quickResign();
	this->federateOne->quickResign();
	this->federateOne->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////
// TEST: (valid) testAchievedSyncPoint() //
///////////////////////////////////////////
void SynchronizationTest::testAchievedSyncPoint()
{
	// announce the point
	federateOne->quickAnnounce( pointOne, tag );
	federateOne->fedamb->waitForSyncRegResult( pointOne, RTI::RTI_TRUE );
	
	// wait for the point to be announced to the second federate
	federateTwo->fedamb->waitForSyncAnnounce( pointOne );
	
	// achieve the point and wait for it to be synchronized on
	federateOne->quickAchieved( pointOne );
	federateTwo->quickAchieved( pointOne );
	federateOne->fedamb->waitForSynchronized( pointOne );
	federateTwo->fedamb->waitForSynchronized( pointOne );
}

///////////////////////////////////////////////////////////////
// TEST: (valid) testAcheivedSyncPointFromUnjoinedFederate() //
///////////////////////////////////////////////////////////////
void SynchronizationTest::testAcheivedSyncPointFromUnjoinedFederate()
{
	federateOne->quickResign();

	try
	{
		federateOne->rtiamb->synchronizationPointAchieved( pointOne );
		failTestMissingException( "FederateNotExecutionMember", "achieve sync when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e, "achieve sync when not joined" );
	}
}

////////////////////////////////////////////////////////
// TEST: (valid) testAchievedSyncPointWithNullLabel() //
////////////////////////////////////////////////////////
void SynchronizationTest::testAchievedSyncPointWithNullLabel()
{
	try
	{
		federateOne->rtiamb->synchronizationPointAchieved( NULL );
		failTestMissingException( "SynchronizationPointLabelWasNotAnnounced or RTIinternalError",
		                          "Attempting to achieve sync point with NULL label" );
	}
	catch( RTI::SynchronizationPointLabelWasNotAnnounced& slna )
	{
		// success!
	}
	catch( RTI::RTIinternalError& rie )
	{
		// also valid
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "SynchronizationPointLabelWasNotAnnounced or RTIinternalError", e,
		                        "Attempting to achieve sync point with NULL label" );
	}
}

/////////////////////////////////////////////////////////
// TEST: (valid) testAchievedSyncPointWithEmptyLabel() //
/////////////////////////////////////////////////////////
void SynchronizationTest::testAchievedSyncPointWithEmptyLabel()
{
	try
	{
		federateOne->rtiamb->synchronizationPointAchieved( "" );
		failTestMissingException( "SynchronizationPointLabelWasNotAnnounced",
		                          "Attempting to achieve sync point with empty label" );
	}
	catch( RTI::SynchronizationPointLabelWasNotAnnounced& slna )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "SynchronizationPointLabelWasNotAnnounced", e, 
		                        "Attempting to achieve sync point with empty label" );
	}
}

//////////////////////////////////////////////////////////////
// TEST: (valid) testAchievedSyncPointWithWhitespaceLabel() //
//////////////////////////////////////////////////////////////
void SynchronizationTest::testAchievedSyncPointWithWhitespaceLabel()
{
	try
	{
		federateOne->rtiamb->synchronizationPointAchieved( "    " );
		failTestMissingException( "SynchronizationPointLabelWasNotAnnounced",
		                          "Attempting to achieve sync point with whitespace label" );
	}
	catch( RTI::SynchronizationPointLabelWasNotAnnounced& slna )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "SynchronizationPointLabelWasNotAnnounced", e, 
		                        "Attempting to achieve sync point with whitespace label" );
	}
}

////////////////////////////////////////////////////////////////
// TEST: (valid) testAchievedSyncPointWithNonAnnouncedLabel() //
////////////////////////////////////////////////////////////////
void SynchronizationTest::testAchievedSyncPointWithNonAnnouncedLabel()
{
	try
	{
		federateOne->rtiamb->synchronizationPointAchieved( "canHasAchieved?" );
		failTestMissingException( "SynchronizationPointLabelWasNotAnnounced",
		                          "Attempting to achieve sync point with non-announced label" );
	}
	catch( RTI::SynchronizationPointLabelWasNotAnnounced& slna )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "SynchronizationPointLabelWasNotAnnounced", e, 
		                        "Attempting to achieve sync point with non-announced label" );
	}
}

//////////////////////////////////////////////////////////////////////////
// TEST: (valid) testAchievedSyncPointWithPreviouslySynchronizedLabel() //
//////////////////////////////////////////////////////////////////////////
void SynchronizationTest::testAchievedSyncPointWithPreviouslySynchronizedLabel()
{
	// announce and synchronize on a point
	federateOne->quickAnnounce( pointOne, tag );
	federateOne->fedamb->waitForSyncRegResult( pointOne, RTI::RTI_TRUE );
	federateTwo->fedamb->waitForSyncAnnounce( pointOne );
	federateOne->quickAchieved( pointOne );
	federateTwo->quickAchieved( pointOne );
	federateOne->fedamb->waitForSynchronized( pointOne );
	federateTwo->fedamb->waitForSynchronized( pointOne );
	
	try
	{
		federateOne->rtiamb->synchronizationPointAchieved( pointOne );
		failTestMissingException( "SynchronizationPointLabelWasNotAnnounced",
		                          "Attempting to achieve sync point for previously sync'd point" );
	}
	catch( RTI::SynchronizationPointLabelWasNotAnnounced& slna )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "SynchronizationPointLabelWasNotAnnounced", e, 
		                        "Attempting to achieve sync point for previously sync'd point" );
	}
}
