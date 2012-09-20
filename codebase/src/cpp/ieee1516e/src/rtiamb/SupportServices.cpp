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
#include "jni/JniUtils.h"
#include "utils/Logger.h"

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
	// call the method
	jstring result = (jstring)jnienv->CallObjectMethod( javarti->jproxy,
	                                                    javarti->GET_AUTO_RESIGN_DIRECTIVE );

	// clean up and run the exception check
	ResignAction action = JniUtils::toResignAction( jnienv, result );
	jnienv->DeleteLocalRef( result );
	javarti->exceptionCheck();
	
	return action;
}

// 10.3
void PorticoRtiAmbassador::setAutomaticResignDirective( ResignAction resignAction )
    throw( InvalidResignAction,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	// convert the parameters to java types
	jstring jaction = JniUtils::fromResignAction( jnienv, resignAction );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->SET_AUTO_RESIGN_DIRECTIVE,
	                        jaction );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jaction );
	javarti->exceptionCheck();
}

// 10.4
FederateHandle PorticoRtiAmbassador::getFederateHandle( const std::wstring& theName )
    throw( NameNotFound,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	// convert the parameters to java types
	jstring jname = JniUtils::fromWideString( jnienv, theName );
	
	// call the method
	jint result = jnienv->CallIntMethod( javarti->jproxy,
	                                     javarti->GET_FEDERATE_HANDLE,
	                                     jname );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jname );
	javarti->exceptionCheck();
	return JniUtils::toFederateHandle( result );
}

// 10.5
std::wstring PorticoRtiAmbassador::getFederateName( FederateHandle theHandle )
    throw( InvalidFederateHandle,
           FederateHandleNotKnown,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	// convert the parameters to java types
	jint jhandle = JniUtils::fromHandle( theHandle );
	
	// call the method
	jstring result = (jstring)jnienv->CallObjectMethod( javarti->jproxy,
	                                                    javarti->GET_FEDERATE_NAME,
	                                                    jhandle );

	// clean up and run the exception check
	wstring name = JniUtils::toWideString( jnienv, result );
	jnienv->DeleteLocalRef( result );
	javarti->exceptionCheck();
	return name;
}

// 10.6
ObjectClassHandle PorticoRtiAmbassador::getObjectClassHandle( const std::wstring& theName )
    throw( NameNotFound,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	// convert the parameters to java types
	jstring jname = JniUtils::fromWideString( jnienv, theName );
	
	// call the method
	jint result = jnienv->CallIntMethod( javarti->jproxy,
	                                     javarti->GET_OBJECT_CLASS_HANDLE,
	                                     jname );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jname );
	javarti->exceptionCheck();
	return JniUtils::toObjectClassHandle( result );
}

// 10.7
std::wstring PorticoRtiAmbassador::getObjectClassName( ObjectClassHandle theHandle )
    throw( InvalidObjectClassHandle,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	// convert the parameters to java types
	jint jhandle = JniUtils::fromHandle( theHandle );
	
	// call the method
	jstring result = (jstring)jnienv->CallObjectMethod( javarti->jproxy,
	                                                    javarti->GET_OBJECT_CLASS_NAME,
	                                                    jhandle );

	// clean up and run the exception check
	wstring name = JniUtils::toWideString( jnienv, result );
	jnienv->DeleteLocalRef( result );
	javarti->exceptionCheck();
	return name;
}

// 10.8
ObjectClassHandle PorticoRtiAmbassador::getKnownObjectClassHandle( ObjectInstanceHandle theObject )
    throw( ObjectInstanceNotKnown,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	// call the method
	jint result = jnienv->CallIntMethod( javarti->jproxy,
	                                     javarti->GET_KNOWN_OBJECT_CLASS_HANDLE,
	                                     JniUtils::fromHandle(theObject) );

	// clean up and run the exception check
	javarti->exceptionCheck();
	return JniUtils::toObjectClassHandle( result );
}

// 10.9
ObjectInstanceHandle PorticoRtiAmbassador::getObjectInstanceHandle( const std::wstring& theName )
    throw( ObjectInstanceNotKnown,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	// convert the parameters to java types
	jstring jname = JniUtils::fromWideString( jnienv, theName );
	
	// call the method
	jint result = jnienv->CallIntMethod( javarti->jproxy,
	                                     javarti->GET_OBJECT_INSTANCE_HANDLE,
	                                     jname );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jname );
	javarti->exceptionCheck();
	return JniUtils::toObjectHandle( result );
}

// 10.10
std::wstring PorticoRtiAmbassador::getObjectInstanceName( ObjectInstanceHandle theHandle )
    throw( ObjectInstanceNotKnown,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	// convert the parameters to java types
	jint jhandle = JniUtils::fromHandle( theHandle );
	
	// call the method
	jstring result = (jstring)jnienv->CallObjectMethod( javarti->jproxy,
	                                                    javarti->GET_OBJECT_INSTANCE_NAME,
	                                                    jhandle );

	// clean up and run the exception check
	wstring name = JniUtils::toWideString( jnienv, result );
	jnienv->DeleteLocalRef( result );
	javarti->exceptionCheck();
	return name;
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
	// convert the parameters to java types
	jint jclassHandle = JniUtils::fromHandle( whichClass );
	jstring jname = JniUtils::fromWideString( jnienv, name );
	
	// call the method
	jint result = jnienv->CallIntMethod( javarti->jproxy,
	                                     javarti->GET_ATTRIBUTE_HANDLE,
	                                     jclassHandle,
	                                     jname );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jname );
	javarti->exceptionCheck();
	return JniUtils::toAttributeHandle( result );
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
	// convert the parameters to java types
	jint jclassHandle = JniUtils::fromHandle( whichClass );
	jint jhandle = JniUtils::fromHandle( theHandle );
	
	// call the method
	jstring result = (jstring)jnienv->CallObjectMethod( javarti->jproxy,
	                                                    javarti->GET_ATTRIBUTE_NAME,
	                                                    jclassHandle,
	                                                    jhandle );

	// clean up and run the exception check
	wstring name = JniUtils::toWideString( jnienv, result );
	jnienv->DeleteLocalRef( result );
	javarti->exceptionCheck();
	return name;
}

// 10.13
double PorticoRtiAmbassador::getUpdateRateValue( const std::wstring& updateRateDesignator )
    throw( InvalidUpdateRateDesignator,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	logger->error( "[Not Implemented] getUpdateRateValue()" );
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
	logger->error( "[Not Implemented] getUpdateRateValueForAttribute()" );
	return 0.0;
}

// 10.15
InteractionClassHandle PorticoRtiAmbassador::getInteractionClassHandle( const std::wstring& theName )
    throw( NameNotFound,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	// convert the parameters to java types
	jstring jname = JniUtils::fromWideString( jnienv, theName );
	
	// call the method
	jint result = jnienv->CallIntMethod( javarti->jproxy,
	                                     javarti->GET_INTERACTION_CLASS_HANDLE,
	                                     jname );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jname );
	javarti->exceptionCheck();
	return JniUtils::toInteractionClassHandle( result );
}

// 10.16
std::wstring PorticoRtiAmbassador::getInteractionClassName( InteractionClassHandle theHandle )
    throw( InvalidInteractionClassHandle,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	// convert the parameters to java types
	jint jhandle = JniUtils::fromHandle( theHandle );
	
	// call the method
	jstring result = (jstring)jnienv->CallObjectMethod( javarti->jproxy,
	                                                    javarti->GET_INTERACTION_CLASS_NAME,
	                                                    jhandle );

	// clean up and run the exception check
	wstring name = JniUtils::toWideString( jnienv, result );
	jnienv->DeleteLocalRef( result );
	javarti->exceptionCheck();
	return name;
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
	// convert the parameters to java types
	jint jclassHandle = JniUtils::fromHandle( whichClass );
	jstring jname = JniUtils::fromWideString( jnienv, theName );
	
	// call the method
	jint result = jnienv->CallIntMethod( javarti->jproxy,
	                                     javarti->GET_PARAMETER_HANDLE,
	                                     jclassHandle,
	                                     jname );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jname );
	javarti->exceptionCheck();
	return JniUtils::toParameterHandle( result );
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
	// convert the parameters to java types
	jint jclassHandle = JniUtils::fromHandle( whichClass );
	jint jhandle = JniUtils::fromHandle( theHandle );
	
	// call the method
	jstring result = (jstring)jnienv->CallObjectMethod( javarti->jproxy,
	                                                    javarti->GET_PARAMETER_NAME,
	                                                    jclassHandle,
	                                                    jhandle );

	// clean up and run the exception check
	wstring name = JniUtils::toWideString( jnienv, result );
	jnienv->DeleteLocalRef( result );
	javarti->exceptionCheck();
	return name;
}

// 10.19
OrderType PorticoRtiAmbassador::getOrderType( const std::wstring& orderName )
    throw( InvalidOrderName,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	if( orderName.compare(L"RECEIVE") == 0 )
		return RECEIVE;
	else if( orderName.compare(L"TIMESTAMP") == 0 )
		return TIMESTAMP;
	else
		return RECEIVE; // default to receive		
}

// 10.20
std::wstring PorticoRtiAmbassador::getOrderName( OrderType orderType )
    throw( InvalidOrderType,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	switch( orderType )
	{
		case RECEIVE:
			return L"RECEIVE";
		case TIMESTAMP:
			return L"TIMESTAMP";
		default:
			return L"RECEIVE";
	}
}

// 10.21
TransportationType PorticoRtiAmbassador::getTransportationType( const std::wstring& transportName )
    throw( InvalidTransportationName,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	if( transportName.compare(L"BEST_EFFORT") == 0 )
		return BEST_EFFORT;
	else if( transportName.compare(L"RELIABLE") == 0 )
		return RELIABLE;
	else
		return BEST_EFFORT; // default to best effort
}

// 10.22
std::wstring PorticoRtiAmbassador::getTransportationName( TransportationType transportType )
    throw( InvalidTransportationType,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	switch( transportType )
	{
		case BEST_EFFORT:
			return L"BEST_EFFORT";
		case RELIABLE:
			return L"RELIABLE";
		default:
			return L"BEST_EFFORT";
	}
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
	logger->error( "[Not Implemented] getAvailableDimensionsForClassAttribute()" );
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
	logger->error( "[Not Implemented] getAvailableDimensionsForInteractionClass()" );
	return DimensionHandleSet();
}

// 10.25
DimensionHandle PorticoRtiAmbassador::getDimensionHandle( const std::wstring& theName )
    throw( NameNotFound,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	logger->error( "[Not Implemented] getDimensionHandle()" );
	return DimensionHandle();
}

// 10.26
std::wstring PorticoRtiAmbassador::getDimensionName( DimensionHandle theHandle )
    throw( InvalidDimensionHandle,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	logger->error( "[Not Implemented] getDimensionHandle()" );
	return L"";
}

// 10.27
unsigned long PorticoRtiAmbassador::getDimensionUpperBound( DimensionHandle theHandle )
    throw( InvalidDimensionHandle,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	logger->error( "[Not Implemented] getDimensionHandle()" );
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
	logger->error( "[Not Implemented] getDimensionHandleSet()" );
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
	logger->error( "[Not Implemented] getRangeBounds()" );
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
	logger->error( "[Not Implemented] getRangeBounds()" );
}

// 10.31
unsigned long PorticoRtiAmbassador::normalizeFederateHandle( FederateHandle theFederateHandle )
    throw( InvalidFederateHandle,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return JniUtils::fromHandle( theFederateHandle );
}

// 10.32
unsigned long PorticoRtiAmbassador::normalizeServiceGroup( ServiceGroup theServiceGroup )
    throw( InvalidServiceGroup,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	switch( theServiceGroup )
	{
		case FEDERATION_MANAGEMENT: return 1;
		case DECLARATION_MANAGEMENT: return 2;
		case OBJECT_MANAGEMENT: return 3;
		case OWNERSHIP_MANAGEMENT: return 4;
		case TIME_MANAGEMENT: return 5;
		case DATA_DISTRIBUTION_MANAGEMENT: return 6;
		case SUPPORT_SERVICES: return 7;
		default: return 100;
	}
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
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->ENABLE_CLASS_RELEVANCE_ADVISORY_SWITCH );
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
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->DISABLE_CLASS_RELEVANCE_ADVISORY_SWITCH );
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
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->ENABLE_ATTRIBUTE_RELEVANCE_ADVISORY_SWITCH );

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
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->DISABLE_ATTRIBUTE_RELEVANCE_ADVISORY_SWITCH );

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
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->ENABLE_ATTRIBUTE_SCOPE_ADVISORY_SWITCH );

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
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->DISABLE_ATTRIBUTE_SCOPE_ADVISORY_SWITCH );
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
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->ENABLE_INTERACTION_RELEVANCE_ADVISORY_SWITCH );
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
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->DISABLE_INTERACTION_RELEVANCE_ADVISORY_SWITCH );
}

// 10.41
bool PorticoRtiAmbassador::evokeCallback( double minSeconds )
    throw( CallNotAllowedFromWithinCallback, RTIinternalError )
{
	return jnienv->CallBooleanMethod( javarti->jproxy,
	                                  javarti->EVOKE_CALLBACK,
	                                  minSeconds );
}

// 10.42
bool PorticoRtiAmbassador::evokeMultipleCallbacks( double minSeconds, double maxSeconds )
	throw( CallNotAllowedFromWithinCallback, RTIinternalError )
{
	return jnienv->CallBooleanMethod( javarti->jproxy,
	                                  javarti->EVOKE_MULTIPLE_CALLBACKS,
	                                  minSeconds,
	                                  maxSeconds );
}

// 10.43
void PorticoRtiAmbassador::enableCallbacks()
	throw( SaveInProgress,
	       RestoreInProgress,
	       RTIinternalError )
{
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->ENABLE_CALLBACKS );
}

// 10.44
void PorticoRtiAmbassador::disableCallbacks()
	throw( SaveInProgress,
	       RestoreInProgress,
	       RTIinternalError )
{
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->DISABLE_CALLBACKS );
}

PORTICO1516E_NS_END
