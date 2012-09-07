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
// Declaration Management Services //////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
// 5.2
void PorticoRtiAmbassador::publishObjectClassAttributes( ObjectClassHandle theClass,
                                                         const AttributeHandleSet& attributeList )
	throw( AttributeNotDefined,
	       ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 5.3
void PorticoRtiAmbassador::unpublishObjectClass( ObjectClassHandle theClass )
	throw( OwnershipAcquisitionPending,
	       ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{

}


void PorticoRtiAmbassador::unpublishObjectClassAttributes( ObjectClassHandle theClass,
                                                           const AttributeHandleSet& attributeList )
	throw( OwnershipAcquisitionPending,
	       AttributeNotDefined,
	       ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{

}

// 5.4
void PorticoRtiAmbassador::publishInteractionClass( InteractionClassHandle theInteraction )
	throw( InteractionClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{

}

// 5.5
void PorticoRtiAmbassador::unpublishInteractionClass( InteractionClassHandle theInteraction )
	throw( InteractionClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{

}

// 5.6
void PorticoRtiAmbassador::subscribeObjectClassAttributes(
	ObjectClassHandle theClass,
	const AttributeHandleSet& attributeList,
	bool active,
	const std::wstring& updateRateDesignator )
	throw( AttributeNotDefined,
	       ObjectClassNotDefined,
	       InvalidUpdateRateDesignator,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{

}

// 5.7
void PorticoRtiAmbassador::unsubscribeObjectClass( ObjectClassHandle theClass )
	throw( ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{

}

void PorticoRtiAmbassador::unsubscribeObjectClassAttributes( ObjectClassHandle theClass,
                                                             const AttributeHandleSet& attributeList )
	throw( AttributeNotDefined,
	       ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{

}

// 5.8
void PorticoRtiAmbassador::subscribeInteractionClass( InteractionClassHandle theClass,
                                                      bool active )
	throw( FederateServiceInvocationsAreBeingReportedViaMOM,
	       InteractionClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{

}

// 5.9
void PorticoRtiAmbassador::unsubscribeInteractionClass( InteractionClassHandle theClass )
	throw( InteractionClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{

}

PORTICO1516E_NS_END
