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
#include "services/PorticoRtiAmbassador.h"

PORTICO1516E_NS_START

/////////////////////////////////////////////////////////////////////////////////
// Object Management Services ///////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
// 6.2
void PorticoRtiAmbassador::reserveObjectInstanceName( const std::wstring& instanceName ) 
	throw( IllegalName,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 6.4
void PorticoRtiAmbassador::releaseObjectInstanceName( const std::wstring& instanceName )
	throw( ObjectInstanceNameNotReserved,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 6.5
void PorticoRtiAmbassador::reserveMultipleObjectInstanceName( const std::set<std::wstring>& names )
	throw( IllegalName,
	       NameSetWasEmpty,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 6.7
void PorticoRtiAmbassador::releaseMultipleObjectInstanceName( const std::set<std::wstring>& names )
	throw( ObjectInstanceNameNotReserved,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 6.8
ObjectInstanceHandle PorticoRtiAmbassador::registerObjectInstance( ObjectClassHandle theClass )
	throw( ObjectClassNotPublished,
	       ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	return ObjectInstanceHandle();
}

ObjectInstanceHandle PorticoRtiAmbassador::registerObjectInstance( ObjectClassHandle theClass,
                                                                   const std::wstring& name )
	throw( ObjectInstanceNameInUse,
	       ObjectInstanceNameNotReserved,
	       ObjectClassNotPublished,
	       ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	return ObjectInstanceHandle();
}

// 6.10
void PorticoRtiAmbassador::updateAttributeValues( ObjectInstanceHandle theObject,
                                                  const AttributeHandleValueMap& attributes,
                                                  const VariableLengthData& tag )
	throw( AttributeNotOwned,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

MessageRetractionHandle PorticoRtiAmbassador::updateAttributeValues(
	ObjectInstanceHandle theObject,
	const AttributeHandleValueMap& theAttributes,
	const VariableLengthData& tag,
	const LogicalTime& theTime )
	throw( InvalidLogicalTime,
		   AttributeNotOwned,
		   AttributeNotDefined,
		   ObjectInstanceNotKnown,
		   SaveInProgress,
		   RestoreInProgress,
		   FederateNotExecutionMember,
		   NotConnected,
		   RTIinternalError )
{
	return MessageRetractionHandle();
}

// 6.12
void PorticoRtiAmbassador::sendInteraction( InteractionClassHandle theInteraction,
                                            const ParameterHandleValueMap& parameters,
                                            const VariableLengthData& tag )
	throw( InteractionClassNotPublished,
	       InteractionParameterNotDefined,
	       InteractionClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

MessageRetractionHandle PorticoRtiAmbassador::sendInteraction(
	InteractionClassHandle theInteraction,
	const ParameterHandleValueMap& parameters,
	const VariableLengthData& tag,
	const LogicalTime& theTime )
	throw( InvalidLogicalTime,
	       InteractionClassNotPublished,
	       InteractionParameterNotDefined,
	       InteractionClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	return MessageRetractionHandle();
}

// 6.14
void PorticoRtiAmbassador::deleteObjectInstance( ObjectInstanceHandle theObject,
                                                 const VariableLengthData& tag )
	throw( DeletePrivilegeNotHeld,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

MessageRetractionHandle PorticoRtiAmbassador::deleteObjectInstance( ObjectInstanceHandle theObject,
                                                                    const VariableLengthData& tag,
                                                                    const LogicalTime& theTime )
	throw( InvalidLogicalTime,
	       DeletePrivilegeNotHeld,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	return MessageRetractionHandle();
}

// 6.16
void PorticoRtiAmbassador::localDeleteObjectInstance( ObjectInstanceHandle theObject )
	throw( OwnershipAcquisitionPending,
	       FederateOwnsAttributes,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 6.19
void PorticoRtiAmbassador::requestAttributeValueUpdate( ObjectInstanceHandle theObject,
                                                        const AttributeHandleSet& attributes,
                                                        const VariableLengthData& tag )
	throw( AttributeNotDefined,
		   ObjectInstanceNotKnown,
		   SaveInProgress,
		   RestoreInProgress,
		   FederateNotExecutionMember,
		   NotConnected,
		   RTIinternalError )
{
	
}

void PorticoRtiAmbassador::requestAttributeValueUpdate( ObjectClassHandle theClass,
                                                        const AttributeHandleSet& attributes,
                                                        const VariableLengthData& tag )
	throw( AttributeNotDefined,
	       ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 6.23
void PorticoRtiAmbassador::requestAttributeTransportationTypeChange(
	ObjectInstanceHandle theObject,
	const AttributeHandleSet& attributes,
	TransportationType theType )
	throw( AttributeAlreadyBeingChanged,
	       AttributeNotOwned,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       InvalidTransportationType,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 6.25
void PorticoRtiAmbassador::queryAttributeTransportationType( ObjectInstanceHandle theObject,
                                                             AttributeHandle theAttribute )
	throw( AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 6.27
void PorticoRtiAmbassador::requestInteractionTransportationTypeChange( InteractionClassHandle theClass,
                                                                       TransportationType theType )
	throw( InteractionClassAlreadyBeingChanged,
	       InteractionClassNotPublished,
	       InteractionClassNotDefined,
	       InvalidTransportationType,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 6.29
void PorticoRtiAmbassador::queryInteractionTransportationType( FederateHandle theFederate,
                                                               InteractionClassHandle theInteraction )
	throw( InteractionClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

PORTICO1516E_NS_END
