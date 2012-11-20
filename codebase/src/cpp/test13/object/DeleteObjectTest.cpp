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
#include "DeleteObjectTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( DeleteObjectTest );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( DeleteObjectTest, "DeleteObjectTest" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( DeleteObjectTest, "deleteObject" );
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION( DeleteObjectTest, "objectManagement" );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
DeleteObjectTest::DeleteObjectTest()
{
	this->defaultFederate = new Test13Federate( "defaultFederate" );
	this->listenerFederate = new Test13Federate( "listenerFederate" );
	this->tag = new char[8];
	strcpy( this->tag, "eltaggo" );
}

DeleteObjectTest::~DeleteObjectTest()
{
	delete this->defaultFederate;
	delete this->listenerFederate;
	delete this->tag;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void DeleteObjectTest::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->listenerFederate->quickJoin();

	// publish and subscribe
	defaultFederate->quickPublish( "ObjectRoot.A.B", 2, "aa", "bb" );
	listenerFederate->quickSubscribe( "ObjectRoot.A", 2, "aa", "ab" );

	// set time up
	defaultFederate->quickEnableRegulating( 5.0 );
	listenerFederate->quickEnableAsync();
	listenerFederate->quickEnableConstrained();
	
	// register and discover the object
	theObject = defaultFederate->quickRegister( "ObjectRoot.A.B" );
	listenerFederate->fedamb->waitForDiscovery( theObject );
}

void DeleteObjectTest::tearDown()
{
	this->listenerFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////
// TEST: (valid) testRODeleteObject() //
////////////////////////////////////////
void DeleteObjectTest::testRODeleteObject()
{
	try
	{
		defaultFederate->rtiamb->deleteObjectInstance( theObject, tag );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while deleting object: %s", e._reason );
	}
	
	// make sure the listener gets the remove message
	listenerFederate->fedamb->waitForRORemoval( theObject );
	defaultFederate->fedamb->waitForRORemovalTimeout( theObject ); // shouldn't get its own delete
}

/////////////////////////////////////////////////
// TEST: testRODeleteObjectWithInvalidHandle() //
/////////////////////////////////////////////////
void DeleteObjectTest::testRODeleteObjectWithInvalidHandle()
{
	try
	{
		defaultFederate->rtiamb->deleteObjectInstance( 123, tag );
		failTestMissingException( "ObjectNotKnown", "deleteing object that doesn't exist" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown", e, "deleteing object that doesn't exist" );
	}
}

////////////////////////////////////////
// TEST: testRODeleteObjectNotOwned() //
////////////////////////////////////////
void DeleteObjectTest::testRODeleteObjectNotOwned()
{
	try
	{
		listenerFederate->rtiamb->deleteObjectInstance( theObject, tag );
		failTestMissingException( "DeletePrivilegeNotHeld",
		                          "deleteing object that is owned by someone else" );
	}
	catch( RTI::DeletePrivilegeNotHeld& dpnh )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "DeletePrivilegeNotHeld", e,
		                        "deleteing object that is owned by someone else" );
	}
}

/////////////////////////////////////////////
// TEST: testRODeleteObjectWhenNotJoined() //
/////////////////////////////////////////////
void DeleteObjectTest::testRODeleteObjectWhenNotJoined()
{
	defaultFederate->quickResign();
	
	try
	{
		defaultFederate->rtiamb->deleteObjectInstance( theObject, tag );
		failTestMissingException( "FederateNotExecutionMember","deleteing object when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "deleteing object when not joined" );
	}
}

////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////// TSO Test Methods /////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////
// TEST: (valid) testTSODeleteObject() //
/////////////////////////////////////////
void DeleteObjectTest::testTSODeleteObject()
{
	try
	{
		RTIfedTime theTime = 5.0;
		defaultFederate->rtiamb->deleteObjectInstance( theObject, theTime, tag );
	}
	catch( RTI::Exception& e )
	{
		failTest( "Unexpected exception while deleting object: %s", e._reason );
	}
	
	// make sure the listener doesn't get the removal YET
	listenerFederate->fedamb->waitForTSORemovalTimeout( theObject );
	
	// advance time such that the removal can be delivered
	defaultFederate->quickAdvanceAndWait( 10.0 );
	listenerFederate->quickAdvanceAndWait( 10.0 );
	listenerFederate->fedamb->waitForTSORemoval( theObject );
}

//////////////////////////////////////////////////
// TEST: testTSODeleteObjectWithInvalidHandle() //
//////////////////////////////////////////////////
void DeleteObjectTest::testTSODeleteObjectWithInvalidHandle()
{
	try
	{
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->deleteObjectInstance( 123, theTime, tag );
		failTestMissingException( "ObjectNotKnown", "deleteing object that doesn't exist" );
	}
	catch( RTI::ObjectNotKnown& onk )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "ObjectNotKnown", e, "deleteing object that doesn't exist" );
	}
}

///////////////////////////////////////////////
// TEST: testTSODeleteObjectWithTimeInPast() //
///////////////////////////////////////////////
void DeleteObjectTest::testTSODeleteObjectWithTimeInPast()
{
	try
	{
		RTIfedTime theTime = 1.0;
		defaultFederate->rtiamb->deleteObjectInstance( theObject, theTime, tag );
		failTestMissingException( "InvalidFederationTime", "deleteing object in the past" );
	}
	catch( RTI::InvalidFederationTime& ift )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "InvalidFederationTime", e, "deleteing object in the past" );
	}
}

//////////////////////////////////////////
// TEST: testTSODeleteObjectNotOwned() //
/////////////////////////////////////////
void DeleteObjectTest::testTSODeleteObjectNotOwned()
{
	try
	{
		RTIfedTime theTime = 10.0;
		listenerFederate->rtiamb->deleteObjectInstance( theObject, theTime, tag );
		failTestMissingException( "DeletePrivilegeNotHeld",
		                          "deleteing object that is owned by someone else" );
	}
	catch( RTI::DeletePrivilegeNotHeld& dpnh )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "DeletePrivilegeNotHeld", e,
		                        "deleteing object that is owned by someone else" );
	}
}

//////////////////////////////////////////////
// TEST: testTSODeleteObjectWhenNotJoined() //
//////////////////////////////////////////////
void DeleteObjectTest::testTSODeleteObjectWhenNotJoined()
{
	defaultFederate->quickResign();
	
	try
	{
		RTIfedTime theTime = 10.0;
		defaultFederate->rtiamb->deleteObjectInstance( theObject, theTime, tag );
		failTestMissingException( "FederateNotExecutionMember","deleteing object when not joined" );
	}
	catch( RTI::FederateNotExecutionMember& fnem )
	{
		// success!
	}
	catch( RTI::Exception& e )
	{
		failTestWrongException( "FederateNotExecutionMember", e,
		                        "deleteing object when not joined" );
	}
}

