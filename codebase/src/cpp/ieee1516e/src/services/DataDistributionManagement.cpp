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

//////////////////////////////////
// Data Distribution Management //
//////////////////////////////////
// 9.2
RegionHandle PorticoRtiAmbassador::createRegion( const DimensionHandleSet& theDimensions )
	throw( InvalidDimensionHandle,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	return RegionHandle();
}

// 9.3
void PorticoRtiAmbassador::commitRegionModifications( const RegionHandleSet& regionHandleSet )
	throw( RegionNotCreatedByThisFederate,
	       InvalidRegion,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 9.4
void PorticoRtiAmbassador::deleteRegion( const RegionHandle& theRegion )
	throw( RegionInUseForUpdateOrSubscription,
	       RegionNotCreatedByThisFederate,
	       InvalidRegion,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 9.5
ObjectInstanceHandle
PorticoRtiAmbassador::registerObjectInstanceWithRegions(
	ObjectClassHandle theClass,
	const AttributeHandleSetRegionHandleSetPairVector& theVector )
	throw( InvalidRegionContext,
	       RegionNotCreatedByThisFederate,
	       InvalidRegion,
	       AttributeNotPublished,
	       ObjectClassNotPublished,
	       AttributeNotDefined,
	       ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	return ObjectInstanceHandle();
}

ObjectInstanceHandle PorticoRtiAmbassador::registerObjectInstanceWithRegions(
	ObjectClassHandle theClass,
	const AttributeHandleSetRegionHandleSetPairVector& theVector,
	const std::wstring& objectName )
	throw( ObjectInstanceNameInUse,
	       ObjectInstanceNameNotReserved,
	       InvalidRegionContext,
	       RegionNotCreatedByThisFederate,
	       InvalidRegion,
	       AttributeNotPublished,
	       ObjectClassNotPublished,
	       AttributeNotDefined,
	       ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	return ObjectInstanceHandle();
}

// 9.6
void PorticoRtiAmbassador::associateRegionsForUpdates(
	ObjectInstanceHandle theObject,
	const AttributeHandleSetRegionHandleSetPairVector& theVector )
	throw( InvalidRegionContext,
	       RegionNotCreatedByThisFederate,
	       InvalidRegion,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 9.7
void PorticoRtiAmbassador::unassociateRegionsForUpdates(
	ObjectInstanceHandle theObject,
	const AttributeHandleSetRegionHandleSetPairVector& theVector )
	throw( RegionNotCreatedByThisFederate,
	       InvalidRegion,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 9.8
void PorticoRtiAmbassador::subscribeObjectClassAttributesWithRegions(
	     ObjectClassHandle theClass,
         const AttributeHandleSetRegionHandleSetPairVector& theVector,
         bool active,
         const std::wstring& updateRateDesignator )
	throw( InvalidRegionContext,
	       RegionNotCreatedByThisFederate,
	       InvalidRegion,
	       AttributeNotDefined,
	       ObjectClassNotDefined,
	       InvalidUpdateRateDesignator,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 9.9
void PorticoRtiAmbassador::unsubscribeObjectClassAttributesWithRegions(
         ObjectClassHandle theClass,
         const AttributeHandleSetRegionHandleSetPairVector& theVector )
	throw( RegionNotCreatedByThisFederate,
	       InvalidRegion,
	       AttributeNotDefined,
	       ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 9.10
void PorticoRtiAmbassador::subscribeInteractionClassWithRegions(
	InteractionClassHandle theClass,
	const RegionHandleSet& regions,
	bool active )
	throw( FederateServiceInvocationsAreBeingReportedViaMOM,
	       InvalidRegionContext,
	       RegionNotCreatedByThisFederate,
	       InvalidRegion,
	       InteractionClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 9.11
void PorticoRtiAmbassador::unsubscribeInteractionClassWithRegions( InteractionClassHandle theClass,
                                                                   const RegionHandleSet& regions )
	throw( RegionNotCreatedByThisFederate,
	       InvalidRegion,
	       InteractionClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 9.12
void PorticoRtiAmbassador::sendInteractionWithRegions( InteractionClassHandle theClass,
                                                       const ParameterHandleValueMap& parameters,
                                                       const RegionHandleSet& regions,
                                                       const VariableLengthData& tag )
	throw( InvalidRegionContext,
	       RegionNotCreatedByThisFederate,
	       InvalidRegion,
	       InteractionClassNotPublished,
	       InteractionParameterNotDefined,
	       InteractionClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

MessageRetractionHandle
PorticoRtiAmbassador::sendInteractionWithRegions( InteractionClassHandle theClass,
                                                  const ParameterHandleValueMap& parameters,
                                                  const RegionHandleSet& regions,
                                                  const VariableLengthData& tag,
                                                  const LogicalTime& theTime )
	throw( InvalidLogicalTime,
	       InvalidRegionContext,
	       RegionNotCreatedByThisFederate,
	       InvalidRegion,
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

// 9.13
void PorticoRtiAmbassador::requestAttributeValueUpdateWithRegions(
	ObjectClassHandle theClass,
	const AttributeHandleSetRegionHandleSetPairVector& theSet,
	const VariableLengthData& tag )
	throw( InvalidRegionContext,
	       RegionNotCreatedByThisFederate,
	       InvalidRegion,
	       AttributeNotDefined,
	       ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

PORTICO1516E_NS_END
