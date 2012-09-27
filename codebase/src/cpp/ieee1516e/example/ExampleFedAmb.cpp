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
#include "RTI/time/HLAfloat64Time.h"
#include "ExampleFedAmb.h"

using namespace std;

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
ExampleFedAmb::ExampleFedAmb()
{
	// initialize all the variable values
	this->federateTime      = 0.0;
	this->federateLookahead = 1.0;

	this->isRegulating  = false;
	this->isConstrained = false;
	this->isAdvancing   = false;
	this->isAnnounced   = false;
	this->isReadyToRun  = false;

}

ExampleFedAmb::~ExampleFedAmb() throw()
{
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
double ExampleFedAmb::convertTime( const LogicalTime& theTime )
{
	const HLAfloat64Time& castTime = dynamic_cast<const HLAfloat64Time&>(theTime);
	return castTime.getTime();
}

///////////////////////////////////////////////////////////////////////////////
/////////////////////// Synchronization Point Callbacks ///////////////////////
///////////////////////////////////////////////////////////////////////////////
void ExampleFedAmb::synchronizationPointRegistrationSucceeded( const std::wstring& label )
	throw( FederateInternalError )
{
	wcout << L"Successfully registered sync point: " << label << endl;
}

void
ExampleFedAmb::synchronizationPointRegistrationFailed( const std::wstring& label,
                                                       SynchronizationPointFailureReason reason )
	throw( FederateInternalError )
{
	wcout << L"Failed to register sync point: " << label << endl;
}

void ExampleFedAmb::announceSynchronizationPoint( const std::wstring& label,
                                                  const VariableLengthData& tag )
	throw( FederateInternalError )
{
	wcout << L"Synchronization point announced: " << label << endl;
	if( label.compare(L"ReadyToRun") == 0 )
		this->isAnnounced = true;
}

void ExampleFedAmb::federationSynchronized( const std::wstring& label,
                                            const FederateHandleSet& failedSet )
	throw( FederateInternalError )
{
	wcout << L"Federation Synchronized: " << label << endl;
	if( label.compare(L"ReadyToRun") == 0 )
		this->isReadyToRun = true;
}

///////////////////////////////////////////////////////////////////////////////
//////////////////////////////// Time Callbacks ///////////////////////////////
///////////////////////////////////////////////////////////////////////////////
void ExampleFedAmb::timeRegulationEnabled( const LogicalTime& theFederateTime )
	throw( FederateInternalError )
{
	this->isRegulating = true;
	this->federateTime = convertTime( theFederateTime );
}

void ExampleFedAmb::timeConstrainedEnabled( const LogicalTime& theFederateTime )
	throw( FederateInternalError )
{
	this->isConstrained = true;
	this->federateTime = convertTime( theFederateTime );
}

void ExampleFedAmb::timeAdvanceGrant( const LogicalTime& theFederateTime )
	throw( FederateInternalError )
{
	this->isAdvancing = false;
	this->federateTime = convertTime( theFederateTime );
}

///////////////////////////////////////////////////////////////////////////////
///////////////////////// Object Management Callbacks /////////////////////////
///////////////////////////////////////////////////////////////////////////////

//                         //
// Discover Object Methods //
//                         //
void ExampleFedAmb::discoverObjectInstance( ObjectInstanceHandle theObject,
                                            ObjectClassHandle theObjectClass,
                                            const std::wstring& theObjectName )
	throw( FederateInternalError )
{
	wcout << L"Discoverd Object: handle=" << theObject
	     << L", classHandle=" << theObjectClass
	     << L", name=" << theObjectName << endl;
}

void ExampleFedAmb::discoverObjectInstance( ObjectInstanceHandle theObject,
                                            ObjectClassHandle theObjectClass,
                                            const std::wstring& theObjectName,
                                            FederateHandle producingFederate )
	throw( FederateInternalError )
{
	wcout << L"Discoverd Object: handle=" << theObject
	     << L", classHandle=" << theObjectClass
	     << L", name=" << theObjectName
	     << L", createdBy=" << producingFederate << endl;
}

//                                 // 
// Reflect Attribute Value Methods //
//                                 // 
void ExampleFedAmb::reflectAttributeValues( ObjectInstanceHandle theObject,
                                            const AttributeHandleValueMap& theAttributes,
                                            const VariableLengthData& tag,
                                            OrderType sentOrder,
                                            TransportationType theType,
                                            SupplementalReflectInfo theReflectInfo )
       	throw( FederateInternalError )
{
	wcout << L"Reflection Received:";
	
	// print the handle
	wcout << L" object=" << theObject;
	
	// print the attribute information
	wcout << ", attributeCount=" << theAttributes.size() << endl;
	AttributeHandleValueMap::const_iterator iterator;
	for( iterator = theAttributes.begin(); iterator != theAttributes.end(); iterator++ )
	{
		// print the attribute handle
		wcout << L"\tattrHandle=" << (*iterator).first
		      << L", attrSize=" << (*iterator).second.size() << endl;
	}
}

void ExampleFedAmb::reflectAttributeValues( ObjectInstanceHandle theObject,
                                            const AttributeHandleValueMap& theAttributes,
                                            const VariableLengthData& tag,
                                            OrderType sentOrder,
                                            TransportationType theType,
                                            const LogicalTime& theTime,
                                            OrderType receivedOrder,
                                            SupplementalReflectInfo theReflectInfo )
	throw( FederateInternalError )
{
	wcout << L"Reflection Received:";
	
	// print the handle
	wcout << L" object=" << theObject;
	// print the time
	wcout << L", time=" << convertTime(theTime);
	
	// print the attribute information
	wcout << ", attributeCount=" << theAttributes.size() << endl;
	AttributeHandleValueMap::const_iterator iterator;
	for( iterator = theAttributes.begin(); iterator != theAttributes.end(); iterator++ )
	{
		// print the attribute handle
		wcout << L"\tattrHandle=" << (*iterator).first
		      << L", attrSize=" << (*iterator).second.size() << endl;
	}
}

void ExampleFedAmb::reflectAttributeValues( ObjectInstanceHandle theObject,
                                            const AttributeHandleValueMap& theAttributes,
                                            const VariableLengthData& tag,
                                            OrderType sentOrder,
                                            TransportationType theType,
                                            const LogicalTime& theTime,
                                            OrderType receivedOrder,
                                            MessageRetractionHandle theHandle,
                                            SupplementalReflectInfo theReflectInfo )
       	throw( FederateInternalError )
{
	wcout << L"Reflection Received:";
	
	// print the handle
	wcout << L" object=" << theObject;
	// print the time
	wcout << L", time=" << convertTime(theTime);
	
	// print the attribute information
	wcout << ", attributeCount=" << theAttributes.size() << endl;
	AttributeHandleValueMap::const_iterator iterator;
	for( iterator = theAttributes.begin(); iterator != theAttributes.end(); iterator++ )
	{
		// print the attribute handle
		wcout << L"\tattrHandle=" << (*iterator).first
		      << L", attrSize=" << (*iterator).second.size() << endl;
	}
}

//                             //
// Receive Interaction Methods //
//                             //
void ExampleFedAmb::receiveInteraction( InteractionClassHandle theInteraction,
                                        const ParameterHandleValueMap& theParameters,
                                        const VariableLengthData& tag,
                                        OrderType sentOrder,
                                        TransportationType theType,
                                        SupplementalReceiveInfo theReceiveInfo )
	throw( FederateInternalError )
{
	wcout << L"Interaction Received:";
	
	// print the handle
	wcout << L" handle=" << theInteraction;

	// print the parameter information
	wcout << L", parameterCount=" << theParameters.size() << endl;
	ParameterHandleValueMap::const_iterator iterator;
	for( iterator = theParameters.begin(); iterator != theParameters.end(); iterator++ )
	{
		// print the parameter handle
		wcout << L"\tparamHandle=" << (*iterator).first
		      << L", paramSize=" << (*iterator).second.size() << endl;
	}
}

void ExampleFedAmb::receiveInteraction( InteractionClassHandle theInteraction,
                                        const ParameterHandleValueMap& theParameters,
                                        const VariableLengthData& tag,
                                        OrderType sentOrder,
                                        TransportationType theType,
                                        const LogicalTime& theTime,
                                        OrderType receivedOrder,
                                        SupplementalReceiveInfo theReceiveInfo )
       	throw( FederateInternalError )
{
	wcout << L"Interaction Received:";
	
	// print the handle
	wcout << L" handle=" << theInteraction;
	// print the time
	wcout << L", time=" << convertTime(theTime);

	// print the parameter information
	wcout << L", parameterCount=" << theParameters.size() << endl;
	ParameterHandleValueMap::const_iterator iterator;
	for( iterator = theParameters.begin(); iterator != theParameters.end(); iterator++ )
	{
		// print the parameter handle
		wcout << L"\tparamHandle=" << (*iterator).first
		      << L", paramSize=" << (*iterator).second.size() << endl;
	}
}

void ExampleFedAmb::receiveInteraction( InteractionClassHandle theInteraction,
                                        const ParameterHandleValueMap& theParameters,
                                        const VariableLengthData& tag,
                                        OrderType sentOrder,
                                        TransportationType theType,
                                        const LogicalTime& theTime,
                                        OrderType receivedOrder,
                                        MessageRetractionHandle retractionHandle,
                                        SupplementalReceiveInfo theReceiveInfo )
       	throw( FederateInternalError )
{
	this->receiveInteraction( theInteraction,
	                          theParameters,
	                          tag,
	                          sentOrder,
	                          theType,
	                          theTime,
	                          receivedOrder,
	                          theReceiveInfo );
}


//                       //
// Remove Object Methods //
//                       //
void ExampleFedAmb::removeObjectInstance( ObjectInstanceHandle theObject,
                                          const VariableLengthData& tag,
                                          OrderType sentOrder,
                                          SupplementalRemoveInfo theRemoveInfo )
       	throw( FederateInternalError )
{
	wcout << L"Object Removed: handle=" << theObject << endl;
}

void ExampleFedAmb::removeObjectInstance( ObjectInstanceHandle theObject,
                                          const VariableLengthData& tag,
                                          OrderType sentOrder,
                                          const LogicalTime& theTime,
                                          OrderType receivedOrder,
                                          SupplementalRemoveInfo theRemoveInfo )
	throw( FederateInternalError )
{
	wcout << L"Object Removed: handle=" << theObject << endl;
}

void ExampleFedAmb::removeObjectInstance( ObjectInstanceHandle theObject,
                                          const VariableLengthData& tag,
                                          OrderType sentOrder,
                                          const LogicalTime& theTime,
                                          OrderType receivedOrder,
                                          MessageRetractionHandle theHandle,
                                          SupplementalRemoveInfo theRemoveInfo )
	throw( FederateInternalError )
{
	removeObjectInstance( theObject, tag, sentOrder, theTime, receivedOrder, theRemoveInfo );
}
