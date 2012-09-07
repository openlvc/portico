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
// RTI Support Services /////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
// 10.2
ResignAction PorticoRtiAmbassador::getAutomaticResignDirective()
    throw( FederateNotExecutionMember,
    	   NotConnected,
    	   RTIinternalError )
{
	return NO_ACTION;
}

// 10.3
void PorticoRtiAmbassador::setAutomaticResignDirective( ResignAction resignAction )
    throw( InvalidResignAction,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	
}

// 10.4
FederateHandle PorticoRtiAmbassador::getFederateHandle( const std::wstring& theName )
    throw( NameNotFound,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return FederateHandle();
}

// 10.5
std::wstring PorticoRtiAmbassador::getFederateName( FederateHandle theHandle )
    throw( InvalidFederateHandle,
           FederateHandleNotKnown,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return L"";
}

// 10.6
ObjectClassHandle PorticoRtiAmbassador::getObjectClassHandle( const std::wstring& theName )
    throw( NameNotFound,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return ObjectClassHandle();
}

// 10.7
std::wstring PorticoRtiAmbassador::getObjectClassName( ObjectClassHandle theHandle )
    throw( InvalidObjectClassHandle,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return L"";
}

// 10.8
ObjectClassHandle PorticoRtiAmbassador::getKnownObjectClassHandle( ObjectInstanceHandle theObject )
    throw( ObjectInstanceNotKnown,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return ObjectClassHandle();
}

// 10.9
ObjectInstanceHandle PorticoRtiAmbassador::getObjectInstanceHandle( const std::wstring& theName )
    throw( ObjectInstanceNotKnown,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return ObjectInstanceHandle();
}

// 10.10
std::wstring PorticoRtiAmbassador::getObjectInstanceName( ObjectInstanceHandle theHandle )
    throw( ObjectInstanceNotKnown,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return L"";
}

// 10.11
AttributeHandle PorticoRtiAmbassador::getAttributeHandle( ObjectClassHandle whichClass,
                                                          const std::wstring& name )
	throw( NameNotFound,
	       InvalidObjectClassHandle,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	return AttributeHandle();
}

// 10.12
std::wstring PorticoRtiAmbassador::getAttributeName( ObjectClassHandle whichClass,
                                                     AttributeHandle theHandle )
	throw( AttributeNotDefined,
	       InvalidAttributeHandle,
	       InvalidObjectClassHandle,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	return L"";
}

// 10.13
double PorticoRtiAmbassador::getUpdateRateValue( const std::wstring& updateRateDesignator )
    throw( InvalidUpdateRateDesignator,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return 0.0;
}

// 10.14
double PorticoRtiAmbassador::getUpdateRateValueForAttribute( ObjectInstanceHandle theObject,
                                                             AttributeHandle theAttribute )
	throw( ObjectInstanceNotKnown,
	       AttributeNotDefined,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	return 0.0;
}

// 10.15
InteractionClassHandle PorticoRtiAmbassador::getInteractionClassHandle( const std::wstring& theName )
    throw( NameNotFound,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return InteractionClassHandle();
}

// 10.16
std::wstring PorticoRtiAmbassador::getInteractionClassName( InteractionClassHandle theHandle )
    throw( InvalidInteractionClassHandle,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return L"";
}

// 10.17
ParameterHandle PorticoRtiAmbassador::getParameterHandle( InteractionClassHandle whichClass,
                                                          const std::wstring& theName )
	throw( NameNotFound,
	       InvalidInteractionClassHandle,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	return ParameterHandle();
}

// 10.18
std::wstring PorticoRtiAmbassador::getParameterName( InteractionClassHandle whichClass,
                                                     ParameterHandle theHandle )
	throw( InteractionParameterNotDefined,
	       InvalidParameterHandle,
	       InvalidInteractionClassHandle,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	return L"";
}

// 10.19
OrderType PorticoRtiAmbassador::getOrderType( const std::wstring& orderName )
    throw( InvalidOrderName,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return RECEIVE;
}

// 10.20
std::wstring PorticoRtiAmbassador::getOrderName( OrderType orderType )
    throw( InvalidOrderType,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return L"";
}

// 10.21
TransportationType PorticoRtiAmbassador::getTransportationType( const std::wstring& transportName )
    throw( InvalidTransportationName,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return BEST_EFFORT;
}

// 10.22
std::wstring PorticoRtiAmbassador::getTransportationName( TransportationType transportType )
    throw( InvalidTransportationType,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return L"";
}

// 10.23
DimensionHandleSet
PorticoRtiAmbassador::getAvailableDimensionsForClassAttribute( ObjectClassHandle theClass,
                                                               AttributeHandle theHandle )
throw( AttributeNotDefined,
	   InvalidAttributeHandle,
	   InvalidObjectClassHandle,
	   FederateNotExecutionMember,
	   NotConnected,
	   RTIinternalError )
{
	return DimensionHandleSet();
}

// 10.24
DimensionHandleSet
PorticoRtiAmbassador::getAvailableDimensionsForInteractionClass( InteractionClassHandle theClass )
    throw( InvalidInteractionClassHandle,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return DimensionHandleSet();
}

// 10.25
DimensionHandle PorticoRtiAmbassador::getDimensionHandle( const std::wstring& theName )
    throw( NameNotFound,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return DimensionHandle();
}

// 10.26
std::wstring PorticoRtiAmbassador::getDimensionName( DimensionHandle theHandle )
    throw( InvalidDimensionHandle,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return L"";
}

// 10.27
unsigned long PorticoRtiAmbassador::getDimensionUpperBound( DimensionHandle theHandle )
    throw( InvalidDimensionHandle,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return 1;
}

// 10.28
DimensionHandleSet PorticoRtiAmbassador::getDimensionHandleSet( RegionHandle regionHandle )
    throw( InvalidRegion,
           SaveInProgress,
           RestoreInProgress,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return DimensionHandleSet();
}

// 10.29
RangeBounds PorticoRtiAmbassador::getRangeBounds( RegionHandle regionHandle,
                                                  DimensionHandle dimensionHandle )
	throw( RegionDoesNotContainSpecifiedDimension,
		   InvalidRegion,
		   SaveInProgress,
		   RestoreInProgress,
		   FederateNotExecutionMember,
		   NotConnected,
		   RTIinternalError )
{
	return RangeBounds();
}

// 10.30
void PorticoRtiAmbassador::setRangeBounds( RegionHandle regionHandle,
                                           DimensionHandle dimensionHandle,
                                           const RangeBounds& theRangeBounds )
	throw( InvalidRangeBound,
	       RegionDoesNotContainSpecifiedDimension,
	       RegionNotCreatedByThisFederate,
	       InvalidRegion,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 10.31
unsigned long PorticoRtiAmbassador::normalizeFederateHandle( FederateHandle theFederateHandle )
    throw( InvalidFederateHandle,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return 1;
}

// 10.32
unsigned long PorticoRtiAmbassador::normalizeServiceGroup( ServiceGroup theServiceGroup )
    throw( InvalidServiceGroup,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return 1;
}

// 10.33
void PorticoRtiAmbassador::enableObjectClassRelevanceAdvisorySwitch()
	throw( ObjectClassRelevanceAdvisorySwitchIsOn,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	
}

// 10.34
void PorticoRtiAmbassador::disableObjectClassRelevanceAdvisorySwitch()
    throw( ObjectClassRelevanceAdvisorySwitchIsOff,
           SaveInProgress,
           RestoreInProgress,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	
}

// 10.35
void PorticoRtiAmbassador::enableAttributeRelevanceAdvisorySwitch()
    throw( AttributeRelevanceAdvisorySwitchIsOn,
           SaveInProgress,
           RestoreInProgress,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	
}

// 10.36
void PorticoRtiAmbassador::disableAttributeRelevanceAdvisorySwitch()
    throw( AttributeRelevanceAdvisorySwitchIsOff,
           SaveInProgress,
           RestoreInProgress,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	
}

// 10.37
void PorticoRtiAmbassador::enableAttributeScopeAdvisorySwitch()
    throw( AttributeScopeAdvisorySwitchIsOn,
           SaveInProgress,
           RestoreInProgress,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	
}

// 10.38
void PorticoRtiAmbassador::disableAttributeScopeAdvisorySwitch()
    throw( AttributeScopeAdvisorySwitchIsOff,
           SaveInProgress,
           RestoreInProgress,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	
}

// 10.39
void PorticoRtiAmbassador::enableInteractionRelevanceAdvisorySwitch()
    throw( InteractionRelevanceAdvisorySwitchIsOn,
           SaveInProgress,
           RestoreInProgress,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	
}

// 10.40
void PorticoRtiAmbassador::disableInteractionRelevanceAdvisorySwitch()
    throw( InteractionRelevanceAdvisorySwitchIsOff,
           SaveInProgress,
           RestoreInProgress,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	
}

// 10.41
bool PorticoRtiAmbassador::evokeCallback( double minSeconds )
    throw( CallNotAllowedFromWithinCallback, RTIinternalError )
{
	return false;
}

// 10.42
bool PorticoRtiAmbassador::evokeMultipleCallbacks( double minSeconds, double maxSeconds )
	throw( CallNotAllowedFromWithinCallback, RTIinternalError )
{
	return false;
}

// 10.43
void PorticoRtiAmbassador::enableCallbacks()
	throw( SaveInProgress,
	       RestoreInProgress,
	       RTIinternalError )
{
	
}

// 10.44
void PorticoRtiAmbassador::disableCallbacks()
	throw( SaveInProgress,
	       RestoreInProgress,
	       RTIinternalError )
{
	
}

PORTICO1516E_NS_END
