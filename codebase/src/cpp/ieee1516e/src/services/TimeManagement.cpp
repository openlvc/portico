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
// Time Management Services /////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
// 8.2
void PorticoRtiAmbassador::enableTimeRegulation( const LogicalTimeInterval& theLookahead )
	throw( InvalidLookahead,
	       InTimeAdvancingState,
	       RequestForTimeRegulationPending,
	       TimeRegulationAlreadyEnabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 8.4
void PorticoRtiAmbassador::disableTimeRegulation()
	throw( TimeRegulationIsNotEnabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 8.5
void PorticoRtiAmbassador::enableTimeConstrained()
	throw( InTimeAdvancingState,
	       RequestForTimeConstrainedPending,
	       TimeConstrainedAlreadyEnabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 8.7
void PorticoRtiAmbassador::disableTimeConstrained()
	throw( TimeConstrainedIsNotEnabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 8.8
void PorticoRtiAmbassador::timeAdvanceRequest( const LogicalTime& theTime )
	throw( LogicalTimeAlreadyPassed,
	       InvalidLogicalTime,
	       InTimeAdvancingState,
	       RequestForTimeRegulationPending,
	       RequestForTimeConstrainedPending,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 8.9
void PorticoRtiAmbassador::timeAdvanceRequestAvailable( const LogicalTime& theTime )
	throw( LogicalTimeAlreadyPassed,
	       InvalidLogicalTime,
	       InTimeAdvancingState,
	       RequestForTimeRegulationPending,
	       RequestForTimeConstrainedPending,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 8.10
void PorticoRtiAmbassador::nextMessageRequest( const LogicalTime& theTime )
	throw( LogicalTimeAlreadyPassed,
	       InvalidLogicalTime,
	       InTimeAdvancingState,
	       RequestForTimeRegulationPending,
	       RequestForTimeConstrainedPending,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 8.11
void PorticoRtiAmbassador::nextMessageRequestAvailable( const LogicalTime& theTime )
	throw( LogicalTimeAlreadyPassed,
	       InvalidLogicalTime,
	       InTimeAdvancingState,
	       RequestForTimeRegulationPending,
	       RequestForTimeConstrainedPending,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 8.12
void PorticoRtiAmbassador::flushQueueRequest( const LogicalTime& theTime )
	throw( LogicalTimeAlreadyPassed,
	       InvalidLogicalTime,
	       InTimeAdvancingState,
	       RequestForTimeRegulationPending,
	       RequestForTimeConstrainedPending,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 8.14
void PorticoRtiAmbassador::enableAsynchronousDelivery()
	throw( AsynchronousDeliveryAlreadyEnabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 8.15
void PorticoRtiAmbassador::disableAsynchronousDelivery()
	throw( AsynchronousDeliveryAlreadyDisabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 8.16
bool PorticoRtiAmbassador::queryGALT( LogicalTime& theTime )
	throw( SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	return false;
}

// 8.17
void PorticoRtiAmbassador::queryLogicalTime( LogicalTime& theTime )
	throw( SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 8.18
bool PorticoRtiAmbassador::queryLITS( LogicalTime& theTime )
	throw( SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	return false;
}

// 8.19
void PorticoRtiAmbassador::modifyLookahead( const LogicalTimeInterval& theLookahead )
	throw( InvalidLookahead,
	       InTimeAdvancingState,
	       TimeRegulationIsNotEnabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 8.20
void PorticoRtiAmbassador::queryLookahead( LogicalTimeInterval& interval )
	throw( TimeRegulationIsNotEnabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 8.21
void PorticoRtiAmbassador::retract( MessageRetractionHandle theHandle )
	throw( MessageCanNoLongerBeRetracted,
	       InvalidMessageRetractionHandle,
	       TimeRegulationIsNotEnabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 8.23
void PorticoRtiAmbassador::changeAttributeOrderType( ObjectInstanceHandle theObject,
                               const AttributeHandleSet& theAttributes,
                               OrderType theType )
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

// 8.24
void PorticoRtiAmbassador::changeInteractionOrderType( InteractionClassHandle theClass,
                                 OrderType theType )
	throw( InteractionClassNotPublished,
	       InteractionClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

PORTICO1516E_NS_END
