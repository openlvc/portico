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
#include "rtiamb/PorticoRtiAmbassador.h"

PORTICO1516E_NS_START

///////////////////////////////////
// Ownership Management Services //
///////////////////////////////////
// 7.2
void PorticoRtiAmbassador::unconditionalAttributeOwnershipDivestiture(
	ObjectInstanceHandle theObject,
	const AttributeHandleSet& theAttributes )
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

// 7.3
void PorticoRtiAmbassador::negotiatedAttributeOwnershipDivestiture(
	ObjectInstanceHandle theObject,
	const AttributeHandleSet& theAttributes,
	const VariableLengthData& tag )
	throw( AttributeAlreadyBeingDivested,
	       AttributeNotOwned,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 7.6
void PorticoRtiAmbassador::confirmDivestiture( ObjectInstanceHandle theObject,
                                               const AttributeHandleSet& confirmedAttributes,
                                               const VariableLengthData& tag )
	throw( NoAcquisitionPending,
	       AttributeDivestitureWasNotRequested,
	       AttributeNotOwned,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 7.8
void PorticoRtiAmbassador::attributeOwnershipAcquisition( ObjectInstanceHandle theObject,
                                                          const AttributeHandleSet& desiredAttributes,
                                                          const VariableLengthData& tag )
	throw( AttributeNotPublished,
	       ObjectClassNotPublished,
	       FederateOwnsAttributes,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 7.9
void PorticoRtiAmbassador::attributeOwnershipAcquisitionIfAvailable(
	ObjectInstanceHandle theObject,
	const AttributeHandleSet& desiredAttributes )
	throw( AttributeAlreadyBeingAcquired,
	       AttributeNotPublished,
	       ObjectClassNotPublished,
	       FederateOwnsAttributes,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 7.12
void PorticoRtiAmbassador::attributeOwnershipReleaseDenied( ObjectInstanceHandle theObject,
                                                            const AttributeHandleSet& theAttributes )
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

// 7.13
void PorticoRtiAmbassador::attributeOwnershipDivestitureIfWanted(
	ObjectInstanceHandle theObject,
	const AttributeHandleSet& theAttributes,
	AttributeHandleSet& theDivestedAttributes ) // filled by RTI
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

// 7.14
void PorticoRtiAmbassador::cancelNegotiatedAttributeOwnershipDivestiture(
	ObjectInstanceHandle theObject,
	const AttributeHandleSet& theAttributes )
	throw( AttributeDivestitureWasNotRequested,
	       AttributeNotOwned,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 7.15
void PorticoRtiAmbassador::cancelAttributeOwnershipAcquisition(
	ObjectInstanceHandle theObject,
	const AttributeHandleSet& theAttributes )
	throw( AttributeAcquisitionWasNotRequested,
	       AttributeAlreadyOwned,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 7.17
void PorticoRtiAmbassador::queryAttributeOwnership( ObjectInstanceHandle theObject,
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

// 7.19
bool PorticoRtiAmbassador::isAttributeOwnedByFederate( ObjectInstanceHandle theObject,
                                                       AttributeHandle theAttribute )
	throw( AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	return false;
}

PORTICO1516E_NS_END
