/*
 *   Copyright 2012 The Portico Project
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
#include <iostream>
#include "RTI/RTI1516.h"
#include "RTI/time/HLAfloat64Interval.h"
#include "RTI/time/HLAfloat64Time.h"

#include "ExampleFedAmb.h"
#include "ExampleCPPFederate.h"
#include <string>

#if __linux__
	#include <string.h>
	#include <stdio.h>
#endif

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
ExampleCPPFederate::ExampleCPPFederate()
{
}

ExampleCPPFederate::~ExampleCPPFederate()
{
	if( this->fedamb )
		delete this->fedamb;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
///////////////////////////////////////////////////////////////////////////
////////////////////////// Main Simulation Method /////////////////////////
///////////////////////////////////////////////////////////////////////////
void ExampleCPPFederate::runFederate( std::wstring federateName )
{
	/////////////////////////////////
	// 1. create the RTIambassador //
	/////////////////////////////////
	RTIambassadorFactory factory = RTIambassadorFactory();
	this->rtiamb = factory.createRTIambassador().release();

	///////////////////////////
	// 2. connect to the RTI //
	///////////////////////////
	// we need the federate ambassador set up before we can connect
	this->fedamb = new ExampleFedAmb();
	try
	{
		rtiamb->connect( *this->fedamb, HLA_EVOKED );
	}
	catch( ConnectionFailed& connectionFailed )
	{
		wcout << L"Connection failed: " << connectionFailed.what() << endl;
	}
	catch( InvalidLocalSettingsDesignator& settings )
	{
		wcout << L"Connection failed, InvalidLocalSettingsDesignator: " << settings.what() << endl;
	}
	catch( UnsupportedCallbackModel& callbackModel )
	{
		wcout << L"Connection failed, UnsupportedCallbackModel: " << callbackModel.what() << endl;
	}
	catch( AlreadyConnected& connected )
	{
		wcout << L"Connection failed, AlreadyConnected: " << connected.what() << endl;
	}
	catch( RTIinternalError& error )
	{
		wcout << L"Connection failed, Generic Error: " << error.what() << endl;
	}
	
	//////////////////////////////////////////
	// 3. create and join to the federation //
	//////////////////////////////////////////
	// create
	// NOTE: some other federate may have already created the federation,
	//       in that case, we'll just try and join it
	try
	{
		vector<wstring> foms;
		foms.push_back( L"testfom.fed" );
		
		//rtiamb->createFederationExecution( L"ExampleFederation", L"testfom.fed" );
		rtiamb->createFederationExecution( L"ExampleFederation", foms );
		wcout << L"Created Federation" << endl;
	}
	catch( FederationExecutionAlreadyExists& exists )
	{
		wcout << L"Didn't create federation, it already existed" << endl;
	}

	////////////////////////////
	// 4. join the federation //
	////////////////////////////
	rtiamb->joinFederationExecution( federateName, L"Example Federate", L"ExampleFederation" );
	wcout << L"Joined Federation as " << federateName << endl;

	// initialize the handles - have to wait until we are joined
	initializeHandles();

	////////////////////////////////
	// 5. announce the sync point //
	////////////////////////////////
	// announce a sync point to get everyone on the same page. if the point
	// has already been registered, we'll get a callback saying it failed,
	// but we don't care about that, as long as someone registered it
	VariableLengthData tag( (void*)"", 1 );
	rtiamb->registerFederationSynchronizationPoint( READY_TO_RUN, tag );
	while( fedamb->isAnnounced == false )
	{
		rtiamb->evokeMultipleCallbacks( 0.1, 1.0 );
	}

	// WAIT FOR USER TO KICK US OFF
	// So that there is time to add other federates, we will wait until the
	// user hits enter before proceeding. That was, you have time to start
	// other federates.
	waitForUser();

	///////////////////////////////////////////////////////
	// 6. achieve the point and wait for synchronization //
	///////////////////////////////////////////////////////
	// tell the RTI we are ready to move past the sync point and then wait
	// until the federation has synchronized on
	rtiamb->synchronizationPointAchieved( READY_TO_RUN );
	wcout << L"Achieved sync point: " << READY_TO_RUN << L", waiting for federation..." << endl;
	while( fedamb->isReadyToRun == false )
	{
		rtiamb->evokeMultipleCallbacks( 0.1, 1.0 );
	}

	/////////////////////////////
	// 7. enable time policies //
	/////////////////////////////
	// in this section we enable/disable all time policies
	// note that this step is optional!
	enableTimePolicy();
	wcout << L"Time Policy Enabled" << endl;

	//////////////////////////////
	// 8. publish and subscribe //
	//////////////////////////////
	// in this section we tell the RTI of all the data we are going to
	// produce, and all the data we want to know about
	publishAndSubscribe();
	wcout << L"Published and Subscribed" << endl;

	/////////////////////////////////////
	// 9. register an object to update //
	/////////////////////////////////////
	ObjectInstanceHandle objectHandle = registerObject();
	wcout << L"Registered Object, handle=" << objectHandle << endl;

	/////////////////////////////////////
	// 10. do the main simulation loop //
	/////////////////////////////////////
	// here is where we do the meat of our work. in each iteration, we will
	// update the attribute values of the object we registered, and will
	// send an interaction.
	int i;
	for( i = 0; i < 20; i++ )
	{
		// 9.1 update the attribute values of the instance //
		updateAttributeValues( objectHandle );

		// 9.2 send an interaction
		sendInteraction();

		// 9.3 request a time advance and wait until we get it
		advanceTime( 1.0 );
		wcout << L"Time Advanced to " << fedamb->federateTime << endl;
	}

	//////////////////////////////////////
	// 11. delete the object we created //
	//////////////////////////////////////
	deleteObject( objectHandle );
	wcout << L"Deleted Object, handle=" << objectHandle << endl;

	////////////////////////////////////
	// 12. resign from the federation //
	////////////////////////////////////
	rtiamb->resignFederationExecution( NO_ACTION );
	wcout << L"Resigned from Federation" << endl;

	////////////////////////////////////////
	// 13. try and destroy the federation //
	////////////////////////////////////////
	// NOTE: we won't die if we can't do this because other federates
	//       remain. in that case we'll leave it for them to clean up
	try
	{
		rtiamb->destroyFederationExecution( L"ExampleFederation" );
		wcout << L"Destroyed Federation" << endl;
	}
	catch( FederationExecutionDoesNotExist& dne )
	{
		wcout << L"No need to destroy federation, it doesn't exist" << endl;
	}
	catch( FederatesCurrentlyJoined& fcj )
	{
		wcout << L"Didn't destroy federation, federates still joined" << endl;
	}

	/////////////////////////////////
	// 14. disconnect from the RTI //
	/////////////////////////////////
	// disconnect from the RTI
	this->rtiamb->disconnect();

	//////////////////
	// 15. clean up //
	//////////////////
	delete this->rtiamb;
}

///////////////////////////////////////////////////////////////////////////////
//////////////////////////////// Helper Methods ///////////////////////////////
///////////////////////////////////////////////////////////////////////////////
/*
 * This method will get all the relevant handle information from the RTIambassador
 */
void ExampleCPPFederate::initializeHandles()
{
	this->aHandle  = rtiamb->getObjectClassHandle( L"ObjectRoot.A" );
	this->aaHandle = rtiamb->getAttributeHandle( aHandle, L"aa" );
	this->abHandle = rtiamb->getAttributeHandle( aHandle, L"ab" );
	this->acHandle = rtiamb->getAttributeHandle( aHandle, L"ac" );

	this->xHandle  = rtiamb->getInteractionClassHandle( L"InteractionRoot.X" );
	this->xaHandle = rtiamb->getParameterHandle( xHandle, L"xa" );
	this->xbHandle = rtiamb->getParameterHandle( xHandle, L"xb" );
}

/*
 * Blocks until the user hits enter
 */
void ExampleCPPFederate::waitForUser()
{
	wcout << L" >>>>>>>>>> Press Enter to Continue <<<<<<<<<<" << endl;
	string line;
	getline( cin, line );
}

/*
 * This method will attempt to enable the various time related properties for
 * the federate
 */
void ExampleCPPFederate::enableTimePolicy()
{
	////////////////////////////
	// enable time regulation //
	////////////////////////////
	double lookahead = fedamb->federateLookahead;
	auto_ptr<HLAfloat64Interval> interval( new HLAfloat64Interval(lookahead) );
	rtiamb->enableTimeRegulation( *interval );

	// tick until we get the callback
	while( fedamb->isRegulating == false )
	{
		rtiamb->evokeMultipleCallbacks( 0.1, 1.0 );
	}

	/////////////////////////////
	// enable time constrained //
	/////////////////////////////
	rtiamb->enableTimeConstrained();

	// tick until we get the callback
	while( fedamb->isConstrained == false )
	{
		rtiamb->evokeMultipleCallbacks( 0.1, 1.0 );
	}
}

/*
 * This method will inform the RTI about the types of data that the federate will
 * be creating, and the types of data we are interested in hearing about as other
 * federates produce it.
 */
void ExampleCPPFederate::publishAndSubscribe()
{
	////////////////////////////////////////////
	// publish all attributes of ObjectRoot.A //
	////////////////////////////////////////////
	// before we can register instance of the object class ObjectRoot.A and
	// update the values of the various attributes, we need to tell the RTI
	// that we intend to publish this information

	// package the information into a handle set
	AttributeHandleSet attributes;// = AttributeHandleSet();
	attributes.insert( this->aaHandle );
	attributes.insert( this->abHandle );
	attributes.insert( this->acHandle );

	// do the actual publication
	rtiamb->publishObjectClassAttributes( this->aHandle, attributes );

	/////////////////////////////////////////////////
	// subscribe to all attributes of ObjectRoot.A //
	/////////////////////////////////////////////////
	// we also want to hear about the same sort of information as it is
	// created and altered in other federates, so we need to subscribe to it
	rtiamb->subscribeObjectClassAttributes( this->aHandle, attributes );

	/////////////////////////////////////////////////////
	// publish the interaction class InteractionRoot.X //
	/////////////////////////////////////////////////////
	// we want to send interactions of type InteractionRoot.X, so we need
	// to tell the RTI that we're publishing it first. We don't need to
	// inform it of the parameters, only the class, making it much simpler

	// do the publication
	rtiamb->publishInteractionClass( this->xHandle );

	////////////////////////////////////////////////////
	// subscribe to the InteractionRoot.X interaction //
	////////////////////////////////////////////////////
	// we also want to receive other interaction of the same type that are
	// sent out by other federates, so we have to subscribe to it first
	rtiamb->subscribeInteractionClass( this->xHandle );
}

/*
 * This method will register an instance of the class ObjectRoot.A and will
 * return the federation-wide unique handle for that instance. Later in the
 * simulation, we will update the attribute values for this instance
 */
ObjectInstanceHandle ExampleCPPFederate::registerObject()
{
	return rtiamb->registerObjectInstance( rtiamb->getObjectClassHandle(L"ObjectRoot.A") );
}

/*
 * This method will update all the values of the given object instance. It will
 * set each of the values to be a string which is equal to the name of the
 * attribute plus the current time. eg "aa:10.0" if the time is 10.0.
 * <p/>
 * Note that we don't actually have to update all the attributes at once, we
 * could update them individually, in groups or not at all!
 */
void ExampleCPPFederate::updateAttributeValues( ObjectInstanceHandle objectHandle )
{
	///////////////////////////////////////////////
	// create the necessary container and values //
	///////////////////////////////////////////////
	// create the collection to store the values in, as you can see
	// this is quite a lot of work
	AttributeHandleValueMap attributes;
	
	// generate the new values
	// we use EncodingHelpers to make things nice friendly for both Java and C++
	char aaValue[16], abValue[16], acValue[16];
	sprintf( aaValue, "aa:%f", getLbts() );
	sprintf( abValue, "ab:%f", getLbts() );
	sprintf( acValue, "ac:%f", getLbts() );
	
	VariableLengthData aaData( (void*)aaValue, strlen(aaValue)+1 );
	VariableLengthData abData( (void*)abValue, strlen(abValue)+1 );
	VariableLengthData acData( (void*)acValue, strlen(acValue)+1 );
	attributes[aaHandle] = aaData;
	attributes[abHandle] = abData;
	attributes[acHandle] = acData;

	//////////////////////////
	// do the actual update //
	//////////////////////////
	VariableLengthData tag( (void*)"Hi!", 4 );
	rtiamb->updateAttributeValues( objectHandle, attributes, tag );

	// note that if you want to associate a particular timestamp with the
	// update. here we send another update, this time with a timestamp:
	auto_ptr<HLAfloat64Time> time( new HLAfloat64Time(fedamb->federateTime+
	                                                  fedamb->federateLookahead) );
	rtiamb->updateAttributeValues( objectHandle, attributes, tag, *time );
}

/*
 * This method will send out an interaction of the type InteractionRoot.X. Any
 * federates which are subscribed to it will receive a notification the next time
 * they tick(). Here we are passing only two of the three parameters we could be
 * passing, but we don't actually have to pass any at all!
 */
void ExampleCPPFederate::sendInteraction()
{
	///////////////////////////////////////////////
	// create the necessary container and values //
	///////////////////////////////////////////////
	// create the collection to store the values in
	ParameterHandleValueMap parameters;

	// generate the new values
	char xaValue[16], xbValue[16];
	sprintf( xaValue, "xa:%f", getLbts() );
	sprintf( xbValue, "xb:%f", getLbts() );
	VariableLengthData xaData( (void*)xaValue, strlen(xaValue)+1 );
	VariableLengthData xbData( (void*)xaValue, strlen(xbValue)+1 );
	parameters[xaHandle] = xaData;
	parameters[xbHandle] = xbData;

	//////////////////////////
	// send the interaction //
	//////////////////////////
	VariableLengthData tag( (void*)"Hi!", 4 );
	rtiamb->sendInteraction( xHandle, parameters, tag );

	// if you want to associate a particular timestamp with the
	// interaction, you will have to supply it to the RTI. Here
	// we send another interaction, this time with a timestamp:
	auto_ptr<HLAfloat64Time> time( new HLAfloat64Time(fedamb->federateTime+
	                                                  fedamb->federateLookahead) );
	rtiamb->sendInteraction( xHandle, parameters, tag, *time );
}

/*
 * This method will request a time advance to the current time, plus the given
 * timestep. It will then wait until a notification of the time advance grant
 * has been received.
 */
void ExampleCPPFederate::advanceTime( double timestep )
{
	// request the advance
	fedamb->isAdvancing = true;
	auto_ptr<HLAfloat64Time> newTime( new HLAfloat64Time(fedamb->federateTime+timestep) );
	rtiamb->timeAdvanceRequest( *newTime );

	// wait for the time advance to be granted. ticking will tell the
	// LRC to start delivering callbacks to the federate
	while( fedamb->isAdvancing )
	{
		rtiamb->evokeMultipleCallbacks( 0.1, 1.0 );
	}
}

/*
 * This method will attempt to delete the object instance of the given
 * handle. We can only delete objects we created, or for which we own the
 * privilegeToDelete attribute.
 */
void ExampleCPPFederate::deleteObject( ObjectInstanceHandle objectHandle )
{
	VariableLengthData tag( (void*)"Hi!", 4 );
	rtiamb->deleteObjectInstance( objectHandle, tag );
}

double ExampleCPPFederate::getLbts()
{
	return (fedamb->federateTime + fedamb->federateLookahead);
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

