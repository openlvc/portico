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
#include "Test13FederateAmbassador.h"

using namespace std;

int    Test13FederateAmbassador::STRING_SIZE = 256;
double Test13FederateAmbassador::TIMEOUT = 0.7;
int    Test13FederateAmbassador::BUFFER_SIZE = 64;

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////// Constructors ////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
Test13FederateAmbassador::Test13FederateAmbassador( Test13Federate *testFederate )
{
	this->testFederate = testFederate;
	
	// initialize the instance vars
	this->constrained        = RTI::RTI_FALSE;
	this->regulating         = RTI::RTI_FALSE;
	this->announced          = new map<char*, char*, ltstr>();
	this->synchronized       = new set<char*, ltstr>();
	this->logicalTime        = 0.0;
	this->syncRegResult      = WAITING;
	
	// object/interaction containers
	this->discovered         = new map<RTI::ObjectHandle,Test13Object*>();
	this->roRemoved          = new map<RTI::ObjectHandle,Test13Object*>();
	this->tsoRemoved         = new map<RTI::ObjectHandle,Test13Object*>();
	this->roUpdated          = new set<RTI::ObjectHandle>();
	this->tsoUpdated         = new set<RTI::ObjectHandle>();
	
	this->roInteractions     = new vector<Test13Interaction*>();
	this->tsoInteractions    = new vector<Test13Interaction*>();
	
	this->requestedUpdates   = new map<RTI::ObjectHandle,set<RTI::AttributeHandle>*>();
	
	// ownership management containers
	this->attributesOffered     = new map<RTI::ObjectHandle,set<RTI::AttributeHandle>*>();
	this->attributesDivested    = new map<RTI::ObjectHandle,set<RTI::AttributeHandle>*>();
	this->attributesAcquired    = new map<RTI::ObjectHandle,set<RTI::AttributeHandle>*>();
	this->attributesUnavailable = new map<RTI::ObjectHandle,set<RTI::AttributeHandle>*>();
	this->attributesRequested   = new map<RTI::ObjectHandle,set<RTI::AttributeHandle>*>();
	this->attributeOwnershipInfo = new map<RTI::AttributeHandle,int>();
	this->attributesCancelled   = new map<RTI::ObjectHandle,set<RTI::AttributeHandle>*>();
	
	// save/restore stuff
	this->currentSave              = new ActiveSR();
	this->currentRestore           = new ActiveSR();
	this->successfulRestoreRequest = NULL;
	this->failedRestoreRequest     = NULL;
}

Test13FederateAmbassador::~Test13FederateAmbassador() throw( RTI::FederateInternalError )
{
	// clean up all the maps
	SET_LTSTR_CLEANUP( synchronized );
	MAP_LTSTR_CLEANUP( announced );
	MAP_CLEANUP( RTI::ObjectHandle, Test13Object*, discovered );
	MAP_CLEANUP( RTI::ObjectHandle, Test13Object*, roRemoved );
	MAP_CLEANUP( RTI::ObjectHandle, Test13Object*, tsoRemoved );
	VECTOR_CLEANUP( Test13Interaction*, roInteractions );
	VECTOR_CLEANUP( Test13Interaction*, tsoInteractions );
	MAP_CLEANUP( RTI::ObjectHandle, set<RTI::AttributeHandle>*, requestedUpdates );
	
	MAP_CLEANUP( RTI::ObjectHandle, set<RTI::AttributeHandle>*, attributesOffered );
	MAP_CLEANUP( RTI::ObjectHandle, set<RTI::AttributeHandle>*, attributesDivested );
	MAP_CLEANUP( RTI::ObjectHandle, set<RTI::AttributeHandle>*, attributesAcquired );
	MAP_CLEANUP( RTI::ObjectHandle, set<RTI::AttributeHandle>*, attributesUnavailable );
	MAP_CLEANUP( RTI::ObjectHandle, set<RTI::AttributeHandle>*, attributesRequested );
	//MAP_CLEANUP( RTI::AttributeHandle, int, attributeOwnershipInfo );
	MAP_CLEANUP( RTI::ObjectHandle, set<RTI::AttributeHandle>*, attributesCancelled );

	delete this->announced;
	delete this->synchronized;
	delete this->discovered;
	delete this->roRemoved;
	delete this->tsoRemoved;
	delete this->roUpdated;
	delete this->tsoUpdated;
	delete this->roInteractions;
	delete this->tsoInteractions;
	delete this->requestedUpdates;
	delete this->attributesOffered;
	delete this->attributesDivested;
	delete this->attributesAcquired;
	delete this->attributesUnavailable;
	delete this->attributesRequested;
	delete this->attributeOwnershipInfo;
	delete this->attributesCancelled;
	delete this->currentSave;
	delete this->currentRestore;
}

////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////// Testing Convenience Methods ///////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
void Test13FederateAmbassador::tick()
{
	this->testFederate->getRtiAmb()->tick();
}

time_t Test13FederateAmbassador::getCurrentTime()
{
	return time(NULL);
}

/*
 * Checks to see if more than the allowed period of time has passed since startTime. If it
 * has, the CppUnit macros will be used to kill the current test, and the currentMethod will
 * be identified in the failure message.
 */
void Test13FederateAmbassador::checkTimeout( time_t startTime, char *currentMethod )
{
	double difference = difftime( getCurrentTime(), startTime );
	if( difference > TIMEOUT )
	{
		char *message = new char[512];
		sprintf( message,
		         "(%s) Timeout occurred during %s",
		         testFederate->getFederateName(),
		         currentMethod );
		
		// kill the test
		this->testFederate->killTest( message );
		delete [] message; // don't think this ever gets called
	}
}

/*
 * This method will check to see if, given the startTime, a timeout has occurred. Unlike
 * checkTimeout(time_t,char*), it will not fail the current test if a timeout has occurred.
 * Rather, it will just return RTI_TRUE.
 */ 
RTI::Boolean Test13FederateAmbassador::checkTimeoutNonFatal( time_t startTime )
{
	double difference = difftime( getCurrentTime(), startTime );
	if( difference > TIMEOUT )
	{
		return RTI::RTI_TRUE;
	}
	else
	{
		return RTI::RTI_FALSE;
	}
}

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////// Synchronization Point Methods ///////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
/*
 * Check to see if the sync point with the given label has been announced or not
 */
RTI::Boolean Test13FederateAmbassador::isAnnounced( char* label )
{
	if( this->announced->count(label) == 0 )
		return RTI::RTI_FALSE;
	else
		return RTI::RTI_TRUE;
}

/*
 * Check to see if the federation has synchronized on the given sync point or not
 */
RTI::Boolean Test13FederateAmbassador::isSynchronized( char* label )
{
	if( this->synchronized->count(label) == 0 )
		return RTI::RTI_FALSE;
	else
		return RTI::RTI_TRUE;
}

/*
 * Wait for the given sync point to be announced. If it doesn't happen before a timeout
 * occurs, CppUnit will be used to fail the current test.
 */
char* Test13FederateAmbassador::waitForSyncAnnounce( char* label )
{
	// wait for the point to be announced
	time_t startTime = this->getCurrentTime();
	while( this->isAnnounced(label) == RTI::RTI_FALSE )
	{
		// check for a timeout
		this->checkTimeout( startTime, "waitForSyncAnnounce()" );
		
		// tick to get some callbacks
		this->tick();
	}

	// if we get here, the point must have been announced, return the tag that came with it
	return (*this->announced)[label];
}

/*
 * Wait for the given sync point to be announced. This is the OPPOSITE of waitForSyncAnnounce()
 * in that it EXPECTS there to be a timeout. If the request doesn't timeout, it will fail the
 * current test.
 */
void Test13FederateAmbassador::waitForSyncAnnounceTimeout( char* label )
{
	// wait for the point to be announced
	time_t startTime = this->getCurrentTime();
	while( this->isAnnounced(label) == RTI::RTI_FALSE )
	{
		// check for a timeout
		if( checkTimeoutNonFatal(startTime) == RTI::RTI_TRUE )
		{
			// this is what we were waiting for, let's blow this popsicle stand!
			return;
		}
		
		// tick for callbacks
		this->tick();
	}
	
	// if we get here, it means we got the announcement before the timeout, grave news indeed
	this->testFederate->killTest( "Expected timeout waiting for sync point announce" );
}

/*
 * Wait for the result of the registration request for the given sync point has been received.
 * If the registration request is a success, RTI_TRUE is returned, if not, RTI_FALSE. If there
 * is no response before a timeout occurs, the current test will be failed.
 */
RTI::Boolean Test13FederateAmbassador::waitForSyncRegResult( char* label )
{
	// clear the current values
	this->syncRegResult = WAITING;
	
	// wait for the callback to come through
	time_t startTime = this->getCurrentTime();
	while( this->syncRegResult == WAITING )
	{
		// check for a timeout
		this->checkTimeout( startTime, "waitForSyncRegResult()" );
		
		// tick to get some callbacks
		this->tick();
	}

	// return the result
	if( this->syncRegResult == SUCCESS )
		return RTI::RTI_TRUE;
	else
		return RTI::RTI_FALSE;
}

/*
 * Waits for the result of the sync point registration request with the given label. This is
 * ostensibly the same as waitForSyncRegResult(char*) except that you can provide the expected
 * result. All the conditions of the previous method apply. If the expected result ends up being
 * the actual result, this method will return normally. If the result differs, the current
 * test will be failed.
 */
void Test13FederateAmbassador::waitForSyncRegResult( char* label, RTI::Boolean expectedResult )
{
	// wait for the result
	RTI::Boolean result = waitForSyncRegResult( label );
	
	// check to see if it is the result we want
	if( result != expectedResult )
	{
		this->testFederate->killTest( "waitForSyncRegResult(): Result was not what was expected" );
	}
}

/*
 * This method will wait until the federation has synchronized on the given sync point. If the
 * notification doesn't come through before a timeout, the current unit test will be failed.
 */
void Test13FederateAmbassador::waitForSynchronized( char* label )
{
	// wait for us to feel that funky synchronized love
	time_t startTime = this->getCurrentTime();
	while( this->isSynchronized(label) == RTI::RTI_FALSE )
	{
		// check for a timeout
		this->checkTimeout( startTime, "waitForSynchronized()" );
		
		// tick, tock
		this->tick();
	}
}

/*
 * This is the opposite of waitForSynchronized(char*). It will wait for the federation to
 * synchronized on the point with the given label. If this happens BEFORE a timeout occurs,
 * the current test will be failed. If a timeout occurs, this method will return normally.
 */
void Test13FederateAmbassador::waitForSynchornizedTimeout( char* label )
{
	// wait for the federation to synchronize
	time_t startTime = this->getCurrentTime();
	while( this->isSynchronized(label) == RTI::RTI_FALSE )
	{
		// check for a timeout (which is what we want to happen)
		if( this->checkTimeoutNonFatal(startTime) == RTI::RTI_TRUE )
		{
			// w00t!
			return;
		}
		
		// get some callbacks
		this->tick();
	}
	
	// if we get here, then we synchronized before the timeout, not what we wanted
	this->testFederate->killTest( "Expected timeout waiting for federation to synchronize" );
}

//////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Time Management Methods ///////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
/*
 * This method will wait until the federate has become time constrained. If the notification
 * doesn't come through before a timeout, the current unit test will be failed.
 */
void Test13FederateAmbassador::waitForConstrainedEnabled()
{
	time_t startTime = this->getCurrentTime();
	while( this->constrained != RTI::RTI_TRUE )
	{
		this->checkTimeout( startTime, "waitForConstrainedEnabled()" );
		this->tick();
	}
}

/*
 * This method will wait until the federate has become time regulating. If the notification
 * doesn't come through before a timeout, the current unit test will be failed.
 */
void Test13FederateAmbassador::waitForRegulatingEnabled()
{
	time_t startTime = this->getCurrentTime();
	while( this->regulating != RTI::RTI_TRUE )
	{
		this->checkTimeout( startTime, "waitForRegulatingEnabled()" );
		this->tick();
	}
}

/*
 * This method will wait until the federate has been granted a time advance to AT LEAST the given
 * time. If the notification doesn't come through before a timeout, the current unit test will be
 * failed.
 */
void Test13FederateAmbassador::waitForTimeAdvance( double newTime )
{
	time_t startTime = this->getCurrentTime();
	while( this->logicalTime < newTime )
	{
		this->checkTimeout( startTime, "waitForTimeAdvance()" );
		this->tick();
	}
}

//////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Object Management Methods /////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////

///// Object Discovery Methods ///////////////

/*
 * This method will wait until the federate has discovered an object instance with the given
 * handle, or a timeout has occurred. If the timeout runs up before the discovery, the current
 * test will be killed.
 */
Test13Object* Test13FederateAmbassador::waitForDiscovery( RTI::ObjectHandle objectHandle )
{
	time_t startTime = this->getCurrentTime();
	while( (*discovered)[objectHandle] == NULL )
	{
		this->checkTimeout( startTime, "waitForDiscovery()" );
		this->tick();
	}
	
	return (*discovered)[objectHandle];
}

/*
 * This method will wait until the federate has discovered an object instance with the given
 * handle, or a timeout has occurred. Of the timeout runs up before the discovery, the current
 * test will be killed. Further, if the discovered object instance IS NOT of the class identified
 * in the "asClass" argument, the test will also be killed.
 */
Test13Object* Test13FederateAmbassador::waitForDiscoveryAs( RTI::ObjectHandle objectHandle,
                                                              RTI::ObjectClassHandle asClass )
{
	time_t startTime = this->getCurrentTime();
	while( (*discovered)[objectHandle] == NULL )
	{
		this->checkTimeout( startTime, "waitForDiscovery()" );
		this->tick();
	}
	
	// we got the discovery before a timeout, but was it of the right type?
	RTI::ObjectClassHandle discoveredAs = (*discovered)[objectHandle]->getClassHandle();
	if( discoveredAs != asClass )
	{
		testFederate->killTest( "Discovered instance [%u] as wrong type: expected=%u, found=%u",
		                        objectHandle, asClass, discoveredAs );
	}
	
	return (*discovered)[objectHandle];
}

/*
 * Same as waitForDiscoveryAs() except that it also checks that the name is the same as that which
 * was expected.
 */
Test13Object* Test13FederateAmbassador::waitForDiscoveryAsWithName( RTI::ObjectHandle objectHandle,
                                                                      RTI::ObjectClassHandle asClass,
                                                                      char *expectedName )
{
	time_t startTime = this->getCurrentTime();
	while( (*discovered)[objectHandle] == NULL )
	{
		this->checkTimeout( startTime, "waitForDiscovery()" );
		this->tick();
	}
	
	// we got the discovery before a timeout, but was it of the right type?
	RTI::ObjectClassHandle discoveredAs = (*discovered)[objectHandle]->getClassHandle();
	if( discoveredAs != asClass )
	{
		testFederate->killTest( "Discovered instance [%u] as wrong type: expected=%u, found=%u",
		                        objectHandle, asClass, discoveredAs );
	}
	
	// check the name
	char *actualName = (*discovered)[objectHandle]->getName();
	if( strcmp(expectedName,actualName) != 0 )
	{
		delete expectedName; // the test won't be able to clean this up once I fail it
		testFederate->killTest( "Discovered instance [%u] with wrong name: expected=[%s], found=[%s]",
		                        objectHandle, expectedName, actualName );
	}

	return (*discovered)[objectHandle];
}

/*
 * This method is the inverse of the waitForDiscovery() method. It will wait for an object instance
 * with the given handle to be discovered, but if a time out DOESN'T occur, the current test will
 * be killed. This should be used in situations where you want to validate that a federate does not
 * receive a discovery.
 */
void Test13FederateAmbassador::waitForDiscoveryTimeout( RTI::ObjectHandle objectHandle )
{
	// wait for the federation to synchronize
	time_t startTime = this->getCurrentTime();
	while( (*discovered)[objectHandle] == NULL )
	{
		// check for a timeout (which is what we want to happen)
		if( this->checkTimeoutNonFatal(startTime) == RTI::RTI_TRUE )
		{
			// w00t!
			return;
		}
		
		// get some callbacks
		this->tick();
	}
	
	// if we get here, then we got a discovery we didn't want
	testFederate->killTest( "Expected timeout waiting for discovery of object instance: %u",
	                        objectHandle );
}

///// Object Removal Methods ///////////////
/*
 * Method will wait until a removal notification for the identified object has been received by
 * the FederateAmbassador (in receive order). If the removal request takes too long to come through
 * (the timeout period expires) then the current test will be failed.
 */
void Test13FederateAmbassador::waitForRORemoval( RTI::ObjectHandle objectHandle )
{
	time_t startTime = this->getCurrentTime();
	while( (*roRemoved)[objectHandle] == NULL )
	{
		this->checkTimeout( startTime, "waitForRORemoval()" );
		this->tick();
	}
	
	// if we get here, the object was in the store, remove it that its out of the way
	delete (*roRemoved)[objectHandle];
	roRemoved->erase( objectHandle );
}

/*
 * This is the inverse of waitForRORemoval(). It will wait for a receive-order removal notification,
 * but it expects this NOT to arrive. It will wait for the timeout period to expire and then return
 * happily. If the notification is received, the current test will be failed.
 */
void Test13FederateAmbassador::waitForRORemovalTimeout( RTI::ObjectHandle objectHandle )
{
	// wait for the federation to synchronize
	time_t startTime = this->getCurrentTime();
	while( (*roRemoved)[objectHandle] == NULL )
	{
		// check for a timeout (which is what we want to happen)
		if( this->checkTimeoutNonFatal(startTime) == RTI::RTI_TRUE )
		{
			// w00t!
			return;
		}
		
		// get some callbacks
		this->tick();
	}
	
	// if we get here, then we got a removal we didn't want
	testFederate->killTest( "Expected timeout waiting for RO removal of object instance: %u",
	                        objectHandle );
}

/*
 * Method will wait until a removal notification for the identified object has been received by
 * the FederateAmbassador (in timestamp order). If the removal request takes too long to come
 * through (the timeout period expires) then the current test will be failed.
 */
void Test13FederateAmbassador::waitForTSORemoval( RTI::ObjectHandle objectHandle )
{
	time_t startTime = this->getCurrentTime();
	while( (*tsoRemoved)[objectHandle] == NULL )
	{
		this->checkTimeout( startTime, "waitForTSORemoval()" );
		this->tick();
	}
	
	// if we get here, the object was in the store, remove it that its out of the way
	delete (*tsoRemoved)[objectHandle];
	tsoRemoved->erase( objectHandle );
}

/*
 * This is the inverse of waitForTSORemoval(). It will wait for a timestamp-order removal
 * notification, but it expects this NOT to arrive. It will wait for the timeout period to expire
 * and then return happily. If the notification is received, the current test will be failed.
 */
void Test13FederateAmbassador::waitForTSORemovalTimeout( RTI::ObjectHandle objectHandle )
{
	// wait for the federation to synchronize
	time_t startTime = this->getCurrentTime();
	while( (*tsoRemoved)[objectHandle] == NULL )
	{
		// check for a timeout (which is what we want to happen)
		if( this->checkTimeoutNonFatal(startTime) == RTI::RTI_TRUE )
		{
			// w00t!
			return;
		}
		
		// get some callbacks
		this->tick();
	}
	
	// if we get here, then we got a removal we didn't want
	testFederate->killTest( "Expected timeout waiting for TSO removal of object instance: %u",
	                        objectHandle );
}

///// Object Reflection Methods ///////////////
/*
 * This method will wait for the object of the given handle to be updated (receive order) for a
 * certain period of time. If the object is updated before the timeout, this method will return
 * happily. If the timeout expires, the current test will be failed. Note that updates may have
 * been queued up due to prior processing by the federate ambassador. In this case, this method
 * will return right away. Note that multiple reflections for a particular object instance are
 * not queued up. When you receive an update, it is taken as an indication of acceptance of all
 * the outstanding updates to that point (the updated object handles are stored in a set, so no
 * duplicates are kept).
 */
Test13Object* Test13FederateAmbassador::waitForROUpdate( RTI::ObjectHandle objectHandle )
{
	time_t startTime = this->getCurrentTime();
	while( roUpdated->find(objectHandle) == roUpdated->end() )
	{
		this->tick();
		this->checkTimeout( startTime, "waitForROUpdate()" );
	}
	
	// we found an update for the instance, remove it from the set and return
	roUpdated->erase( objectHandle );
	return (*discovered)[objectHandle];
}

/*
 * This method is like waitForROUpdate(RTI::ObjectHandle) except that it expects NOT to receive or
 * find an update before the timeout expires. If an updated does come in before the timeout, the
 * current test will be failed. If no update is received before the timeout expires, this method
 * will return happily.
 */
void Test13FederateAmbassador::waitForROUpdateTimeout( RTI::ObjectHandle objectHandle )
{
	time_t startTime = this->getCurrentTime();
	while( roUpdated->find(objectHandle) == roUpdated->end() )
	{
		// check for a timeout (which is what we want to happen)
		if( this->checkTimeoutNonFatal(startTime) == RTI::RTI_TRUE )
		{
			// w00t!
			return;
		}
		
		// get some callbacks
		this->tick();
	}
	
	// if we get here, then we got a reflection we didn't want
	testFederate->killTest( "Expected timeout waiting for RO reflection of object instance: %u",
	                        objectHandle );
}

/*
 * This is the same as waitForROUpdate(RTI::ObjectHandle), except that it will wait for TSO events.
 * For more information, read the documentation for that method (it's just up a little bit, don't
 * worry, you don't have to scroll very far).
 */
Test13Object* Test13FederateAmbassador::waitForTSOUpdate( RTI::ObjectHandle objectHandle )
{
	time_t startTime = this->getCurrentTime();
	while( tsoUpdated->find(objectHandle) == tsoUpdated->end() )
	{
		this->tick();
		this->checkTimeout( startTime, "waitForTSOUpdate()" );
	}
	
	// we found an update for the instance, remove it from the set and return
	tsoUpdated->erase( objectHandle );
	return (*discovered)[objectHandle];
}

/*
 * This method is like waitForTSOUpdate(RTI::ObjectHandle) except that it expects NOT to receive or
 * find an update before the timeout expires. If an updated does come in before the timeout, the
 * current test will be failed. If no update is received before the timeout expires, this method
 * will return happily.
 */
void Test13FederateAmbassador::waitForTSOUpdateTimeout( RTI::ObjectHandle objectHandle )
{
	time_t startTime = this->getCurrentTime();
	while( tsoUpdated->find(objectHandle) == tsoUpdated->end() )
	{
		// check for a timeout (which is what we want to happen)
		if( this->checkTimeoutNonFatal(startTime) == RTI::RTI_TRUE )
		{
			// w00t!
			return;
		}
		
		// get some callbacks
		this->tick();
	}
	
	// if we get here, then we got a reflection we didn't want
	testFederate->killTest( "Expected timeout waiting for TSO reflection of object instance: %u",
	                        objectHandle );
}

///// Interaction Received Methods ///////////////
/*
 * This method will wait for an RO interaction of the given class handle to be received. If such
 * an interaction isn't received before a timeout expires, the current test will be failed. Note
 * that interacitons are stored up, so if there is a pending interaction of the expected class
 * waiting for processing, it will be removed from the list and returned right away.
 */
Test13Interaction*
Test13FederateAmbassador::waitForROInteraction( RTI::InteractionClassHandle expected )
{
	time_t startTime = this->getCurrentTime();
	Test13Interaction *ng6Interaction = NULL;
	while( (ng6Interaction=fetchInteraction(expected,roInteractions)) == NULL )
	{
		this->tick();
		this->checkTimeout( startTime, "waitForROInteraction()" );
	}
	
	return ng6Interaction;
}

/*
 * This method is like waitForROInteraction(RTI::InteractionClassHandle) except that it expects
 * no interaction to be received before the timeout expires. If there is an interaction of the
 * identified class waiting for processing, or one is received before the timeout expires, the
 * current test will be failed. Otherwise, the method will return happily.
 */
void Test13FederateAmbassador::waitForROInteractionTimeout( RTI::InteractionClassHandle expected )
{
	time_t startTime = this->getCurrentTime();
	Test13Interaction *ng6Interaction = NULL;
	while( (ng6Interaction=fetchInteraction(expected,roInteractions)) == NULL )
	{
		// check for a timeout (which is what we want to happen)
		if( this->checkTimeoutNonFatal(startTime) == RTI::RTI_TRUE )
		{
			// w00t!
			return;
		}

		// get some callbacks
		this->tick();
	}

	// if we get here, then we got an interaction we didn't want
	delete ng6Interaction;
	testFederate->killTest( "Expected timeout waiting for RO interaction of class: %u", expected );
}

/*
 * This method will wait for an TSO interaction of the given class handle to be received. If such
 * an interaction isn't received before a timeout expires, the current test will be failed. Note
 * that interacitons are stored up, so if there is a pending interaction of the expected class
 * waiting for processing, it will be removed from the list and returned right away.
 */
Test13Interaction*
Test13FederateAmbassador::waitForTSOInteraction( RTI::InteractionClassHandle expected )
{
	time_t startTime = this->getCurrentTime();
	Test13Interaction *ng6Interaction = NULL;
	while( (ng6Interaction=fetchInteraction(expected,tsoInteractions)) == NULL )
	{
		this->tick();
		this->checkTimeout( startTime, "waitForTSOInteraction()" );
	}
	
	return ng6Interaction;
}

/*
 * This method is like waitForROInteraction(RTI::InteractionClassHandle) except that it expects
 * no interaction to be received before the timeout expires. If there is an interaction of the
 * identified class waiting for processing, or one is received before the timeout expires, the
 * current test will be failed. Otherwise, the method will return happily.
 */
void Test13FederateAmbassador::waitForTSOInteractionTimeout( RTI::InteractionClassHandle expected )
{
	time_t startTime = this->getCurrentTime();
	Test13Interaction *ng6Interaction = NULL;
	while( (ng6Interaction=fetchInteraction(expected,tsoInteractions)) == NULL )
	{
		// check for a timeout (which is what we want to happen)
		if( this->checkTimeoutNonFatal(startTime) == RTI::RTI_TRUE )
		{
			// w00t!
			return;
		}

		// get some callbacks
		this->tick();
	}

	// if we get here, then we got an interaction we didn't want
	delete ng6Interaction;
	testFederate->killTest( "Expected timeout waiting for TSO interaction of class: %u", expected );
}

///// Provide Update Methods ///////////////
/*
 * This method will wait until a provideAttributeValueUpdate() request for the identified object
 * is receieved and will then return the set of attribute handles that was provided with it. If
 * the update has already been received, the handles will be returned right away. If the update
 * isn't received before a timeout occurs, the current test will be failed.
 * 
 * MEMORY MANAGEMENT: The caller is responsible for deleting the provided set.
 */
set<RTI::AttributeHandle>*
Test13FederateAmbassador::waitForProvideRequest( RTI::ObjectHandle theObject )
{
	time_t startTime = this->getCurrentTime();
	set<RTI::AttributeHandle> *attributesRequested = NULL;
	while( (attributesRequested=(*requestedUpdates)[theObject]) == NULL )
	{
		this->tick();
		this->checkTimeout( startTime, "waitForProvideRequest()" );
	}
	
	requestedUpdates->erase( theObject );
	return attributesRequested;
}

/*
 * This method is the same as waitForProvideRequest(RTI::ObjectHandle) above, except that it
 * expects NOT to receive the request before a timeout occurs. If a request has already been
 * received, or turns up before the timeout occurs, the current test will be failed.
 */
void Test13FederateAmbassador::waitForProvideRequestTimeout( RTI::ObjectHandle theObject )
{
	time_t startTime = this->getCurrentTime();
	set<RTI::AttributeHandle> *attributesRequested = NULL;
	while( (attributesRequested=(*requestedUpdates)[theObject]) == NULL )
	{
		// check for a timeout (which is what we want to happen)
		if( this->checkTimeoutNonFatal(startTime) == RTI::RTI_TRUE )
		{
			// w00t!
			return;
		}

		// get some callbacks
		this->tick();
	}

	// if we get here, then we got an interaction we didn't want
	delete attributesRequested;
	testFederate->killTest( "Expected timeout waiting for provide update request: object=%d",
	                        theObject );
}

/////////// Ownership Management Helpers ///////////
/*
 * This method will wait for a callback informing the federate that it has acquired the attributes
 * of the given object. If the callback isn't received in time, the current test will be failed. If
 * a call comes in and it doesn't contain all the given attributes, the current test will be failed.
 */
void Test13FederateAmbassador::waitForOwnershipAcquistion( RTI::ObjectHandle theObject,
                                                            int attributeCount,
                                                            ... )
{
	// turn the requested attributes into a set so we can compare them
	set<RTI::AttributeHandle> expected;
	va_list args;
	va_start( args, attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		expected.insert( va_arg(args,int) );
	}
	va_end( args );

	// wait for something to come in for that object
	time_t startTime = this->getCurrentTime();
	set<RTI::AttributeHandle> *acquired = NULL;
	while( (acquired=(*attributesAcquired)[theObject]) == NULL )
	{
		this->tick();
		this->checkTimeout( startTime, "waitForOwnershipAcquistion()" );
	}
	
	// remove the set from the acquired attributes map so we don't see this update later
	// check to see if all the attributes we were expecting as in what we received
	attributesAcquired->erase( theObject );
	RTI::Boolean success = this->setContainsAll( &expected, acquired ); 
	delete acquired;

	if( success == RTI::RTI_FALSE )
	{
		testFederate->killTest(
		    "Received ownership acquired callback but expected more/different attributes" );
	}
}

/*
 * This method will wait for a callback informing the federate that it has been offered ownership
 * of the given attributes of the given object (through the request assumption service). If the
 * callback isn't received in time, the current test will be failed. If a call comes in and it
 * doesn't contain all the given attributes, the current test will be failed.
 */
void Test13FederateAmbassador::waitForOwnershipOffered( RTI::ObjectHandle theObject,
                                                         int attributeCount,
                                                         ... )
{
	// turn the requested attributes into a set so we can compare them
	set<RTI::AttributeHandle> expected;
	va_list args;
	va_start( args, attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		expected.insert( va_arg(args,int) );
	}
	va_end( args );

	// wait for something to come in for that object
	time_t startTime = this->getCurrentTime();
	set<RTI::AttributeHandle> *offered = NULL;
	while( (offered=(*attributesOffered)[theObject]) == NULL )
	{
		this->tick();
		this->checkTimeout( startTime, "waitForOwnershipOffered()" );
	}
	
	// remove the set from the acquired attributes map so we don't see this update later
	// check to see if all the attributes we were expecting as in what we received
	attributesOffered->erase( theObject );
	RTI::Boolean success = this->setContainsAll( &expected, offered ); 
	delete offered;

	if( success == RTI::RTI_FALSE )
	{
		testFederate->killTest(
		    "Received ownership offering callback but expected more/different attributes" );
	}
}

/*
 * This method will wait for a callback informing the federate that it has successfully divested
 * the attributes of the given object. If the callback isn't received in time, the current test
 * will be failed. If a call comes in and it doesn't contain all the given attributes, the current
 * test will be failed.
 */
void Test13FederateAmbassador::waitForOwnershipDivested( RTI::ObjectHandle theObject,
                                                          int attributeCount,
                                                          ... )
{
	// turn the requested attributes into a set so we can compare them
	set<RTI::AttributeHandle> expected;
	va_list args;
	va_start( args, attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		expected.insert( va_arg(args,int) );
	}
	va_end( args );

	// wait for something to come in for that object
	time_t startTime = this->getCurrentTime();
	set<RTI::AttributeHandle> *divested = NULL;
	while( (divested=(*attributesDivested)[theObject]) == NULL )
	{
		this->tick();
		this->checkTimeout( startTime, "waitForOwnershipDivested()" );
	}
	
	// remove the set from the acquired attributes map so we don't see this update later
	// check to see if all the attributes we were expecting as in what we received
	attributesDivested->erase( theObject );
	RTI::Boolean success = this->setContainsAll( &expected, divested ); 
	delete divested;

	if( success == RTI::RTI_FALSE )
	{
		testFederate->killTest(
		    "Received ownership divested callback but expected more/different attributes" );
	}
}

/*
 * This method will wait for a callback informing the federate that it could not obtain the
 * attributes it requested. If the callback isn't received in time, the current test will be failed.
 * If a call comes in and it doesn't contain all the given attributes, the current test will be
 * failed.
 */
void Test13FederateAmbassador::waitForOwnershipUnavailable( RTI::ObjectHandle theObject,
                                                             int attributeCount,
                                                             ... )
{
	// turn the requested attributes into a set so we can compare them
	set<RTI::AttributeHandle> expected;
	va_list args;
	va_start( args, attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		expected.insert( va_arg(args,int) );
	}
	va_end( args );

	// wait for something to come in for that object
	time_t startTime = this->getCurrentTime();
	set<RTI::AttributeHandle> *unavailable = NULL;
	while( (unavailable=(*attributesUnavailable)[theObject]) == NULL )
	{
		this->tick();
		this->checkTimeout( startTime, "waitForOwnershipUnavailable()" );
	}
	
	// remove the set from the acquired attributes map so we don't see this update later
	// check to see if all the attributes we were expecting as in what we received
	attributesUnavailable->erase( theObject );
	RTI::Boolean success = this->setContainsAll( &expected, unavailable ); 
	delete unavailable;

	if( success == RTI::RTI_FALSE )
	{
		testFederate->killTest(
		    "Received ownership unavailable callback but expected more/different attributes" );
	}
}

/*
 * This method will wait for a callback informing the federate that the given attributes of the
 * given object have had an ownership transfer request made for them. If the callback isn't
 * received in time, the current test will be failed. If a call comes in and it doesn't contain 
 * all the given attributes, the current test will be failed.
 */
void Test13FederateAmbassador::waitForOwnershipRequest( RTI::ObjectHandle theObject,
                                                         int attributeCount,
                                                         ... )
{
	// turn the requested attributes into a set so we can compare them
	set<RTI::AttributeHandle> expected;
	va_list args;
	va_start( args, attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		expected.insert( va_arg(args,int) );
	}
	va_end( args );

	// wait for something to come in for that object
	time_t startTime = this->getCurrentTime();
	set<RTI::AttributeHandle> *requested = NULL;
	while( (requested=(*attributesRequested)[theObject]) == NULL )
	{
		this->tick();
		this->checkTimeout( startTime, "waitForOwnershipRequested()" );
	}
	
	// remove the set from the acquired attributes map so we don't see this update later
	// check to see if all the attributes we were expecting as in what we received
	attributesRequested->erase( theObject );
	RTI::Boolean success = this->setContainsAll( &expected, requested ); 
	delete requested;

	if( success == RTI::RTI_FALSE )
	{
		testFederate->killTest(
		    "Received ownership divest request callback but expected more/different attributes" );
	}
}

/**
 * This method waits for a callback informing the federate of the ownership of the given attribute
 * in the given object. If the callback isn't received in a timely manner, the current test will
 * be failed.
 */
int Test13FederateAmbassador::waitForOwnershipResponse( RTI::ObjectHandle theObject,
                                                         RTI::AttributeHandle theAttribute )
{
	time_t startTime = this->getCurrentTime();
	while( this->attributeOwnershipInfo->find(theAttribute) ==
	       this->attributeOwnershipInfo->end() )
	{
		this->checkTimeout( startTime, "waitForOwnershipResponse()" );
		this->tick();
	}
	
	int federateHandle = (*attributeOwnershipInfo)[theAttribute];
	this->attributeOwnershipInfo->erase( attributeOwnershipInfo->find(theAttribute) );
	return federateHandle;
}

/*
 * This method will wait for a callback informing the federate confirming that the acquisition
 * request cancellation it issued has been successful. If the callback isn't received in time,
 * the current test will be failed. If a call comes in and it doesn't contain all the given
 * attributes, the current test will be failed.
 */
void Test13FederateAmbassador::waitForOwnershipCancelConfirmation( RTI::ObjectHandle theObject,
                                                                    int attributeCount,
                                                                    ... )
{
	// turn the requested attributes into a set so we can compare them
	set<RTI::AttributeHandle> expected;
	va_list args;
	va_start( args, attributeCount );
	for( int i = 0; i < attributeCount; i++ )
	{
		expected.insert( va_arg(args,int) );
	}
	va_end( args );

	// wait for something to come in for that object
	time_t startTime = this->getCurrentTime();
	set<RTI::AttributeHandle> *cancalled = NULL;
	while( (cancalled=(*attributesCancelled)[theObject]) == NULL )
	{
		this->tick();
		this->checkTimeout( startTime, "waitForOwnershipCancelConfirmation()" );
	}
	
	// remove the set from the cancelled attributes map so we don't see this message later
	// check to see if all the attributes we were expecting as in what we received
	attributesCancelled->erase( theObject );
	RTI::Boolean success = this->setContainsAll( &expected, cancalled ); 
	delete cancalled;

	if( success == RTI::RTI_FALSE )
	{
		testFederate->killTest(
		    "Received acquire cancelled confirmation callback but expected more/different attributes" );
	}
}

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////// Save/Restore Methods ////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
/*
 * This method waits until a callback is delivered informing the federate that a save with the
 * given label is initiated. If no callback is received in time, the current test will be failed.
 */
void Test13FederateAmbassador::waitForSaveInitiated( char *saveLabel )
{
	time_t startTime = this->getCurrentTime();
	while( this->currentSave->isInitiated(saveLabel) == RTI::RTI_FALSE )
	{
		this->checkTimeout( startTime, "waitForSaveInitiated()" );
		this->tick();
	}
}

/*
 * This method waits to see if a callback telling the federate that a save with the given label
 * has been initiated occurs. It expects that one will not occur and should one be received before
 * a timeout has expired, the current test will be killed.
 */
void Test13FederateAmbassador::waitForSaveInitiatedTimeout( char *saveLabel )
{
	time_t startTime = this->getCurrentTime();
	while( currentSave->isInitiated(saveLabel) == RTI::RTI_FALSE )
	{
		// check for a timeout (which is what we want to happen)
		if( this->checkTimeoutNonFatal(startTime) == RTI::RTI_TRUE )
		{
			// w00t!
			return;
		}

		// get some callbacks
		this->tick();
	}

	// if we get here, then we got the callback we didn't want
	testFederate->killTest( "Expected timeout waiting for save initiated" );
}

/*
 * Waits for notification that the federation has successfully saved. If the notification doesn't
 * come through before a timeout occurs, the currnet test will be failed.
 */
void Test13FederateAmbassador::waitForFederationSaved()
{
	time_t startTime = this->getCurrentTime();
	while( currentSave->isComplete() == RTI::RTI_FALSE )
	{
		this->checkTimeout( startTime, "waitForFederationSaved()" );
		this->tick();
	}

	// check to see if the save was a success
	if( currentSave->isSuccess() == RTI::RTI_TRUE )
		return;
	else
		testFederate->killTest( "Timeout waiting for federation to save, got failure notice" );
}

/*
 * Waits for notification that the federation has successfully *NOT* saved. If the notification
 * doesn't come through before a timeout occurs, the currnet test will be failed.
 */
void Test13FederateAmbassador::waitForFederationNotSaved()
{
	time_t startTime = this->getCurrentTime();
	while( currentSave->isComplete() == RTI::RTI_FALSE )
	{
		this->checkTimeout( startTime, "waitForFederationSaved()" );
		this->tick();
	}

	// check to see if the save was a success
	if( currentSave->isFailure() == RTI::RTI_TRUE )
		return;
	else
		testFederate->killTest( "Timeout waiting for federation to save unsuccessfully, got success notice" );
}

/**
 * Wait for a notification that the federation restore has begun. If this isn't received in a
 * timely fashion, throw an exception.
 */
void Test13FederateAmbassador::waitForFederationRestoreBegun()
{
	time_t startTime = this->getCurrentTime();
	while( currentRestore->isBegun() == RTI::RTI_FALSE )
	{
		this->checkTimeout( startTime, "waitForFederationRestoreBegun()" );
		this->tick();
	}
}

/*
 * Wait for a notification that a federation restore has been initiated with the given label.
 * If this happens, return the federation handle we were given. If not, throw an exception.
 */
int Test13FederateAmbassador::waitForFederateRestoreInitiated( char* label )
{
	// wait for something to come in for that object
	time_t startTime = this->getCurrentTime();
	while( currentRestore->isInitiated(label) == RTI::RTI_FALSE )
	{
		this->checkTimeout( startTime, "waitForFederateRestoreInitiated()" );
		this->tick();
	}

	if( currentRestore->isInitiated(label) )
	{
		return currentRestore->getFederateHandle();
	}
	else
	{
		testFederate->killTest( "Timeout waiting for restore to be initiated with label [%s]",
		                        label );
		return -1; // to shut compiler up
	}
}

/**
 * The opposite of waitForFederateRestoreInitiated(char*)}, this method expects a
 * timeout to occur and will fail the current test if one doesn't happen.
 */
void Test13FederateAmbassador::waitForFederateRestoreInitiatedTimeout( char* saveLabel )
{
	time_t startTime = this->getCurrentTime();
	while( currentRestore->isInitiated(saveLabel) == RTI::RTI_FALSE )
	{
		// check for a timeout (which is what we want to happen)
		if( this->checkTimeoutNonFatal(startTime) == RTI::RTI_TRUE )
		{
			// w00t!
			return;
		}

		// get some callbacks
		this->tick();
	}

	// if we get here, then we got the callback we didn't want
	testFederate->killTest( "Expected timeout waiting for restore initiated" );
}

/*
 * Wait for a notification from the RTI that the federation restore has succeeded. If we
 * get a failure notice or no notice at all, the current test will be killed.
 */
void Test13FederateAmbassador::waitForFederationRestored()
{
	time_t startTime = this->getCurrentTime();
	while( currentRestore->isComplete() == RTI::RTI_FALSE )
	{
		this->checkTimeout( startTime, "waitForFederationRestored()" );
		this->tick();
	}

	// check to see if the save was a success
	if( currentRestore->isSuccess() == RTI::RTI_TRUE )
		return;
	else
		testFederate->killTest( "Timeout waiting for federation to restore, got failure notice" );
}

/*
 * Like waitForFederationRestored() except that it expects a timeout and will fail the test if the
 * notification is received.
 */
void Test13FederateAmbassador::waitForFederationRestoredTimeout( char* label )
{
	time_t startTime = this->getCurrentTime();
	while( currentRestore->isComplete() == RTI::RTI_FALSE )
	{
		// check for a timeout (which is what we want to happen)
		if( this->checkTimeoutNonFatal(startTime) == RTI::RTI_TRUE )
		{
			// w00t!
			return;
		}

		// get some callbacks
		this->tick();
	}

	// if we get here, then we got the callback we didn't want
	testFederate->killTest( "Expected timeout waiting for federation to restore successfully" );
}

/*
 * Wait for a notification from the RTI that the federation restore has completed *unsuccessfully*.
 * If we get a success notice or no notice at all, the current test will be killed.
 */
void Test13FederateAmbassador::waitForFederationNotRestored()
{
	time_t startTime = this->getCurrentTime();
	while( currentRestore->isComplete() == RTI::RTI_FALSE )
	{
		this->checkTimeout( startTime, "waitForFederationNotRestored()" );
		this->tick();
	}

	// check to see if the save was a success
	if( currentRestore->isFailure() == RTI::RTI_TRUE )
		return;
	else
		testFederate->killTest( "Timeout waiting for federation to restore unsuccessfully, got success notice" );
}

/*
 * Wait until the ambassador receives notification that the federation restore with the given
 * label that they attempted to initiate was successful. If it doesn't happen in a timely
 * fashion, the current test is failed.
 */
void Test13FederateAmbassador::waitForRestoreRequestSuccess( char *label )
{
	time_t startTime = this->getCurrentTime();
	while( successfulRestoreRequest != NULL && strcmp(successfulRestoreRequest,label) != 0 )
	{
		this->checkTimeout( startTime, "waitForRestoreRequestSuccess()" );
		this->tick();
	}
}

/*
 * Wait until the ambassador receives notification that the federation restore with the given
 * label that they attempted to initiate has FAILED. If it doesn't happen in a timely fashion,
 * the current test is failed.
 */
void Test13FederateAmbassador::waitForRestoreRequestFailure( char *label )
{
	time_t startTime = this->getCurrentTime();
	while( failedRestoreRequest != NULL && strcmp(failedRestoreRequest,label) != 0 )
	{
		this->checkTimeout( startTime, "waitForRestoreRequestFailure()" );
		this->tick();
	}
}

//////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// PRIVATE Helper Methods ///////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
/*
 * Will attempt to find and return an interaction of the given class in the provided store. If none
 * can be found, NULL will be returned.
 */
Test13Interaction*
Test13FederateAmbassador::fetchInteraction( RTI::InteractionClassHandle theClass,
                                             vector<Test13Interaction*> *theStore )
{
	vector<Test13Interaction*>::iterator it;
	for( it = theStore->begin(); it < theStore->end(); it++ )
	{
		if( theClass == (*it)->getClassHandle() )
		{
			// remove the interaction and return it
			theStore->erase( it );
			return (*it);
		}
	}
	
	return NULL;
}

/**
 * Checks the received set to make sure it contains all the attribute handles from the expected
 * set. If it doesn't, RTI_FALSE is returned, otherwise, RTI_TRUE is returend.
 */
RTI::Boolean Test13FederateAmbassador::setContainsAll( set<RTI::AttributeHandle> *expected,
                                                        set<RTI::AttributeHandle> *received )
{
	set<RTI::AttributeHandle>::iterator setIterator;
	for( setIterator = expected->begin();
	     setIterator != expected->end();
	     setIterator++ )
	{
		RTI::AttributeHandle lookingFor = *setIterator;
		if( received->find(lookingFor) == received->end() )
			return RTI::RTI_FALSE;
	}

	// we found them all, return true
	return RTI::RTI_TRUE;
}

/*
 * Does a string copy from source to target, but will delete the memory at target is it isn't
 * null and will allocate new memory. Really just a "cleanAndCopy"
 */
void Test13FederateAmbassador::copyString( char *target, const char *source )
{
	// delete the old target if there is data there we are going to overwrite
	if( target != NULL )
		delete [] target;
	
	// copy the data across
	char *buffer = new char[strlen(source)];
	strcpy( buffer, source );
	target = buffer;
}


////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////// Federate Ambassador Methods ///////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Helper Methods ///////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
double Test13FederateAmbassador::convertTime( const RTI::FedTime& time )
{
	RTIfedTime timePorticoImpl;

	try
	{
		timePorticoImpl = dynamic_cast<const RTIfedTime&>( time );
	}
	catch( std::bad_cast )
	{
		CPPUNIT_FAIL( "convertTime() was not provided with a portico time implementation" );
	}

	return timePorticoImpl.getTime();
}

//////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////// Synchornization Point Management //////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
void Test13FederateAmbassador::synchronizationPointRegistrationSucceeded( const char *label )
	throw( RTI::FederateInternalError )
{
	this->syncRegResult = SUCCESS;
}

void Test13FederateAmbassador::synchronizationPointRegistrationFailed( const char *label )
	throw( RTI::FederateInternalError )
{
	this->syncRegResult = FAILURE;
}

void Test13FederateAmbassador::announceSynchronizationPoint( const char *label, const char *tag )
	throw( RTI::FederateInternalError )
{
	// copy the strings and put them in the map
	char *labelCopy = new char[strlen(label)+1];	
	strcpy( labelCopy, label );

	char *tagCopy = NULL;
	if( tag != NULL )
	{
		tagCopy = new char[strlen(tag)+1];
		strcpy( tagCopy, tag );
	}

	// store the information
	(*this->announced)[labelCopy] = tagCopy;
}

void Test13FederateAmbassador::federationSynchronized( const char *label )
	throw( RTI::FederateInternalError )
{
	// copy the value into the set
	char *labelCopy = new char[strlen(label)+1];
	strcpy( labelCopy, label );
	this->synchronized->insert( labelCopy );
}

//////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////// Save and Restore Management ////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
void Test13FederateAmbassador::initiateFederateSave( const char *label )
	throw( RTI::UnableToPerformSave,
	       RTI::FederateInternalError )
{
	// the initiate method will copy the label
	this->currentSave->initiate( label );
}

void Test13FederateAmbassador::federationSaved()
	throw( RTI::FederateInternalError )
{
	this->currentSave->success();
}

void Test13FederateAmbassador::federationNotSaved()
	throw( RTI::FederateInternalError )
{
	this->currentSave->failure();
}

void Test13FederateAmbassador::requestFederationRestoreSucceeded( const char *label )
	throw( RTI::FederateInternalError )
{
	copyString( this->successfulRestoreRequest, label );
}

void Test13FederateAmbassador::requestFederationRestoreFailed( const char *label,
                                                                const char *reason )
	throw( RTI::FederateInternalError )
{
	copyString( this->failedRestoreRequest, label );
}

void Test13FederateAmbassador::federationRestoreBegun()
	throw( RTI::FederateInternalError )
{
	this->currentRestore->begun();
}

void Test13FederateAmbassador::initiateFederateRestore( const char *label,
                                                         RTI::FederateHandle handle )
	throw( RTI::SpecifiedSaveLabelDoesNotExist,
	       RTI::CouldNotRestore,
	       RTI::FederateInternalError )
{
	// the initiate method will copy the label
	this->currentRestore->initiate( label, handle );
}

void Test13FederateAmbassador::federationRestored()
	throw( RTI::FederateInternalError )
{
	this->currentRestore->success();
}

void Test13FederateAmbassador::federationNotRestored()
	throw( RTI::FederateInternalError )
{
	this->currentRestore->failure();
}

//////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Declaration Management Services //////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////

void Test13FederateAmbassador::startRegistrationForObjectClass( RTI::ObjectClassHandle theClass )
	throw( RTI::ObjectClassNotPublished,
	       RTI::FederateInternalError )
{
}

void Test13FederateAmbassador::stopRegistrationForObjectClass( RTI::ObjectClassHandle theClass )
	throw( RTI::ObjectClassNotPublished,
	       RTI::FederateInternalError )
{
}

void Test13FederateAmbassador::turnInteractionsOn( RTI::InteractionClassHandle theHandle )
	throw( RTI::InteractionClassNotPublished,
	       RTI::FederateInternalError )
{
}

void Test13FederateAmbassador::turnInteractionsOff( RTI::InteractionClassHandle theHandle )
	throw( RTI::InteractionClassNotPublished,
	       RTI::FederateInternalError )
{
}

//////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////// Object Management Services /////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
void Test13FederateAmbassador::discoverObjectInstance( RTI::ObjectHandle theObject,
                                                        RTI::ObjectClassHandle objectClass,
                                                        const char* theObjectName )
	throw( RTI::CouldNotDiscover,
	       RTI::ObjectClassNotKnown,
	       RTI::FederateInternalError )
{
	Test13Object *ng6Object = new Test13Object( theObject, objectClass, theObjectName );
	(*discovered)[theObject] = ng6Object;
}

void Test13FederateAmbassador::reflectAttributeValues(
                 RTI::ObjectHandle theObject,
                 const RTI::AttributeHandleValuePairSet& theAttributes,
                 const RTI::FedTime& theTime,
                 const char *theTag,
                 RTI::EventRetractionHandle theHandle )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotKnown,
	       RTI::FederateOwnsAttributes,
	       RTI::InvalidFederationTime,
	       RTI::FederateInternalError )
{
	// find the object that is being updated
	Test13Object *ng6Object = (*discovered)[theObject];
	if( ng6Object == NULL )
		testFederate->killTest( "Couldn't find object [%d] to update in reflect", theObject );
	
	// update the attribute values
	ng6Object->updateAttributes( theAttributes );
	
	// place the object handle on the set of those that have been updated
	tsoUpdated->insert( theObject );
}

void Test13FederateAmbassador::reflectAttributeValues(
                 RTI::ObjectHandle theObject,
                 const RTI::AttributeHandleValuePairSet& theAttributes,
                 const char *theTag )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotKnown,
	       RTI::FederateOwnsAttributes,
	       RTI::FederateInternalError )
{
	// find the object that is being updated
	Test13Object *ng6Object = (*discovered)[theObject];
	if( ng6Object == NULL )
		testFederate->killTest( "Couldn't find object [%d] to update in reflect", theObject );
	
	// update the attribute values
	ng6Object->updateAttributes( theAttributes );
	
	// place the object handle on the set of those that have been updated
	roUpdated->insert( theObject );
}

// 4.6
void Test13FederateAmbassador::receiveInteraction(
                 RTI::InteractionClassHandle theInteraction,
                 const RTI::ParameterHandleValuePairSet& theParameters,
                 const RTI::FedTime& theTime,
                 const char *theTag,
                 RTI::EventRetractionHandle theHandle )
	throw( RTI::InteractionClassNotKnown,
	       RTI::InteractionParameterNotKnown,
	       RTI::InvalidFederationTime,
	       RTI::FederateInternalError )
{
	// create a new interaction to store
	Test13Interaction *ng6Interaction = new Test13Interaction( theInteraction, theTag );
	ng6Interaction->updateParameters( theParameters );
	ng6Interaction->setTime( convertTime(theTime) );
	
	// store the interaction
	tsoInteractions->push_back( ng6Interaction );
}

void Test13FederateAmbassador::receiveInteraction(
                 RTI::InteractionClassHandle theInteraction,
                 const RTI::ParameterHandleValuePairSet& theParameters,
                 const char *theTag )
	throw( RTI::InteractionClassNotKnown,
	       RTI::InteractionParameterNotKnown,
	       RTI::FederateInternalError )
{
	// create a new interaction to store
	Test13Interaction *ng6Interaction = new Test13Interaction( theInteraction, theTag );
	ng6Interaction->updateParameters( theParameters );
	
	// store the interaction
	roInteractions->push_back( ng6Interaction );
}

void Test13FederateAmbassador::removeObjectInstance( RTI::ObjectHandle theObject,
                                                      const RTI::FedTime& theTime,
                                                      const char *theTag,
                                                      RTI::EventRetractionHandle theHandle )
	throw( RTI::ObjectNotKnown,
	       RTI::InvalidFederationTime,
	       RTI::FederateInternalError )
{
	Test13Object *ng6Object = (*discovered)[theObject];
	if( ng6Object == NULL )
	{
		// something went wrong, kill any tests
		failTest( "Received TSO removal for object we hadn't discovered: %lo", theObject );
	}
	
	// remove the object from the discovered pool
	discovered->erase( theObject );
	
	// add it to the roRemoved pool
	(*tsoRemoved)[theObject] = ng6Object;
}

void Test13FederateAmbassador::removeObjectInstance( RTI::ObjectHandle theObject, const char *theTag )
	throw( RTI::ObjectNotKnown,
	       RTI::FederateInternalError )
{
	Test13Object *ng6Object = (*discovered)[theObject];
	if( ng6Object == NULL )
	{
		// something went wrong, kill any tests
		failTest( "Received RO removal for object we hadn't discovered: %lo", theObject );
	}
	
	// remove the object from the discovered pool
	discovered->erase( theObject );
	
	// add it to the roRemoved pool
	(*roRemoved)[theObject] = ng6Object;
}

void Test13FederateAmbassador::attributesInScope( RTI::ObjectHandle theObject,
                                                   const RTI::AttributeHandleSet& theAttributes )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotKnown,
	       RTI::FederateInternalError )
{
}

void Test13FederateAmbassador::attributesOutOfScope( RTI::ObjectHandle theObject,
                                                      const RTI::AttributeHandleSet& theAttributes )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotKnown,
	       RTI::FederateInternalError )
{
}

void Test13FederateAmbassador::provideAttributeValueUpdate(
                 RTI::ObjectHandle theObject,
                 const RTI::AttributeHandleSet& theAttributes )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotKnown,
	       RTI::AttributeNotOwned,
	       RTI::FederateInternalError )
{
	set<RTI::AttributeHandle> *handleSet = new set<RTI::AttributeHandle>();
	for( RTI::ULong i = 0; i < theAttributes.size(); i++ )
	{
		handleSet->insert( theAttributes.getHandle(i) );
	}

	(*requestedUpdates)[theObject] = handleSet;
}

void Test13FederateAmbassador::turnUpdatesOnForObjectInstance(
                 RTI::ObjectHandle theObject,
                 const RTI::AttributeHandleSet& theAttributes )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotOwned,
	       RTI::FederateInternalError )
{
}

void Test13FederateAmbassador::turnUpdatesOffForObjectInstance(
                 RTI::ObjectHandle theObject,
                 const RTI::AttributeHandleSet& theAttributes )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotOwned,
	       RTI::FederateInternalError )
{
}

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////// Ownership Management Services ///////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////

void Test13FederateAmbassador::requestAttributeOwnershipAssumption(
                 RTI::ObjectHandle theObject,
                 const RTI::AttributeHandleSet& offeredAttributes,
                 const char *theTag )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotKnown,
	       RTI::AttributeAlreadyOwned,
	       RTI::AttributeNotPublished,
	       RTI::FederateInternalError )
{
	set<RTI::AttributeHandle> *handleSet = new set<RTI::AttributeHandle>();
	for( RTI::ULong i = 0; i < offeredAttributes.size(); i++ )
	{
		handleSet->insert( offeredAttributes.getHandle(i) );
	}

	(*attributesOffered)[theObject] = handleSet;
}

void Test13FederateAmbassador::attributeOwnershipDivestitureNotification(
                 RTI::ObjectHandle theObject,
                 const RTI::AttributeHandleSet& releasedAttributes )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotKnown,
	       RTI::AttributeNotOwned,
	       RTI::AttributeDivestitureWasNotRequested,
	       RTI::FederateInternalError )
{
	set<RTI::AttributeHandle> *handleSet = new set<RTI::AttributeHandle>();
	for( RTI::ULong i = 0; i < releasedAttributes.size(); i++ )
	{
		handleSet->insert( releasedAttributes.getHandle(i) );
	}

	(*attributesDivested)[theObject] = handleSet;
}

void Test13FederateAmbassador::attributeOwnershipAcquisitionNotification(
                 RTI::ObjectHandle theObject,
                 const RTI::AttributeHandleSet& securedAttributes )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotKnown,
	       RTI::AttributeAcquisitionWasNotRequested,
	       RTI::AttributeAlreadyOwned,
	       RTI::AttributeNotPublished,
	       RTI::FederateInternalError )
{
	set<RTI::AttributeHandle> *handleSet = new set<RTI::AttributeHandle>();
	for( RTI::ULong i = 0; i < securedAttributes.size(); i++ )
	{
		handleSet->insert( securedAttributes.getHandle(i) );
	}

	(*attributesAcquired)[theObject] = handleSet;
}

void Test13FederateAmbassador::attributeOwnershipUnavailable(
                 RTI::ObjectHandle theObject,
                 const RTI::AttributeHandleSet& theAttributes )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotKnown, 
	       RTI::AttributeAlreadyOwned,
	       RTI::AttributeAcquisitionWasNotRequested,
	       RTI::FederateInternalError )
{
	set<RTI::AttributeHandle> *handleSet = new set<RTI::AttributeHandle>();
	for( RTI::ULong i = 0; i < theAttributes.size(); i++ )
	{
		handleSet->insert( theAttributes.getHandle(i) );
	}

	(*attributesUnavailable)[theObject] = handleSet;
}

void Test13FederateAmbassador::requestAttributeOwnershipRelease(
                 RTI::ObjectHandle theObject,
                 const RTI::AttributeHandleSet& candidateAttributes,
                 const char *theTag )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotKnown,
	       RTI::AttributeNotOwned,
	       RTI::FederateInternalError )
{
	set<RTI::AttributeHandle> *handleSet = new set<RTI::AttributeHandle>();
	for( RTI::ULong i = 0; i < candidateAttributes.size(); i++ )
	{
		handleSet->insert( candidateAttributes.getHandle(i) );
	}

	(*attributesRequested)[theObject] = handleSet;
}

void Test13FederateAmbassador::confirmAttributeOwnershipAcquisitionCancellation(
                 RTI::ObjectHandle theObject,
                 const RTI::AttributeHandleSet& theAttributes )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotKnown,
	       RTI::AttributeAlreadyOwned,
	       RTI::AttributeAcquisitionWasNotCanceled,
	       RTI::FederateInternalError )
{
	set<RTI::AttributeHandle> *handleSet = new set<RTI::AttributeHandle>();
	for( RTI::ULong i = 0; i < theAttributes.size(); i++ )
	{
		handleSet->insert( theAttributes.getHandle(i) );
	}
	
	(*attributesCancelled)[theObject] = handleSet;
}

void Test13FederateAmbassador::informAttributeOwnership( RTI::ObjectHandle theObject,
                                                          RTI::AttributeHandle theAttribute,
                                                          RTI::FederateHandle theOwner )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotKnown,
	       RTI::FederateInternalError )
{
	// NOTE: This is a crappy hack because I don't want to create more infrastructure for
	//       dealing with this little scenario. We are going to IGNORE the object handle and
	//       only take note of the attribute handle when storing the owner information. Thus,
	//       if there is more than one outstanding request for the same attribute but in different
	//       objects, this has the potential to fail, with the second update overwriting the
	//       ownership information from the first.
	(*attributeOwnershipInfo)[theAttribute] = (int)theOwner;
}

void Test13FederateAmbassador::attributeIsNotOwned( RTI::ObjectHandle theObject,
                                                     RTI::AttributeHandle theAttribute )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotKnown,
	       RTI::FederateInternalError )
{
	// NOTE: This is a crappy hack because I don't want to create more infrastructure for
	//       dealing with this little scenario. We are going to IGNORE the object handle and
	//       only take note of the attribute handle when storing the owner information. Thus,
	//       if there is more than one outstanding request for the same attribute but in different
	//       objects, this has the potential to fail, with the second update overwriting the
	//       ownership information from the first.
	(*attributeOwnershipInfo)[theAttribute] = Test13Federate::OWNER_UNOWNED;
}

void Test13FederateAmbassador::attributeOwnedByRTI( RTI::ObjectHandle theObject,
                                                     RTI::AttributeHandle theAttribute )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotKnown,
	       RTI::FederateInternalError )
{
	// NOTE: This is a crappy hack because I don't want to create more infrastructure for
	//       dealing with this little scenario. We are going to IGNORE the object handle and
	//       only take note of the attribute handle when storing the owner information. Thus,
	//       if there is more than one outstanding request for the same attribute but in different
	//       objects, this has the potential to fail, with the second update overwriting the
	//       ownership information from the first.
	(*attributeOwnershipInfo)[theAttribute] = Test13Federate::OWNER_RTI;
}

//////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Time Management Services //////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////

void Test13FederateAmbassador::timeRegulationEnabled( const RTI::FedTime& theFederateTime )
	throw( RTI::InvalidFederationTime,
	       RTI::EnableTimeRegulationWasNotPending,
	       RTI::FederateInternalError )
{
	this->regulating = RTI::RTI_TRUE;
	this->logicalTime = this->convertTime( theFederateTime );
}

void Test13FederateAmbassador::timeConstrainedEnabled( const RTI::FedTime& theFederateTime )
	throw( RTI::InvalidFederationTime,
	       RTI::EnableTimeConstrainedWasNotPending,
	       RTI::FederateInternalError )
{
	this->constrained = RTI::RTI_TRUE;
	this->logicalTime = this->convertTime( theFederateTime );
}

void Test13FederateAmbassador::timeAdvanceGrant( const RTI::FedTime& theTime )
	throw( RTI::InvalidFederationTime,
	       RTI::TimeAdvanceWasNotInProgress,
	       RTI::FederateInternalError )
{
	this->logicalTime = this->convertTime( theTime );
}

void Test13FederateAmbassador::requestRetraction( RTI::EventRetractionHandle theHandle )
	throw( RTI::EventNotKnown,
	       RTI::FederateInternalError )
{
}

