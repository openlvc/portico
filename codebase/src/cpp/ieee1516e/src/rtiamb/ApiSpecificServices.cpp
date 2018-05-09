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
#include "portico/DatatypeRetrieval.h"

PORTICO1516E_NS_START

/////////////////////////////////////////////////////////////////////////////////
// API-specific services ////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
// Return instance of time factory being used by the federation
std::auto_ptr<LogicalTimeFactory> PorticoRtiAmbassador::getTimeFactory() const
    throw( FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	// TODO fix this so that it returns the stored factory, not just a hard coded one
	return std::auto_ptr<LogicalTimeFactory>( new HLAfloat64TimeFactory() );
}

// Decode handles
FederateHandle PorticoRtiAmbassador::decodeFederateHandle( const VariableLengthData& encodedValue ) const
    throw( CouldNotDecode,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return FederateHandle();
}

ObjectClassHandle
PorticoRtiAmbassador::decodeObjectClassHandle( const VariableLengthData& encodedValue ) const
    throw( CouldNotDecode,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return ObjectClassHandle();
}

InteractionClassHandle
PorticoRtiAmbassador::decodeInteractionClassHandle( const VariableLengthData& encodedValue ) const
    throw( CouldNotDecode,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return InteractionClassHandle();
}

ObjectInstanceHandle
PorticoRtiAmbassador::decodeObjectInstanceHandle( const VariableLengthData& encodedValue ) const
    throw( CouldNotDecode,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return ObjectInstanceHandle();
}

AttributeHandle PorticoRtiAmbassador::decodeAttributeHandle( const VariableLengthData& encodedValue ) const
    throw( CouldNotDecode,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return AttributeHandle();
}

ParameterHandle PorticoRtiAmbassador::decodeParameterHandle( const VariableLengthData& encodedValue ) const
    throw( CouldNotDecode,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return ParameterHandle();
}

DimensionHandle PorticoRtiAmbassador::decodeDimensionHandle( const VariableLengthData& encodedValue ) const
    throw( CouldNotDecode,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return DimensionHandle();
}

MessageRetractionHandle
PorticoRtiAmbassador::decodeMessageRetractionHandle( const VariableLengthData& encodedValue ) const
    throw( CouldNotDecode,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return MessageRetractionHandle();
}

RegionHandle PorticoRtiAmbassador::decodeRegionHandle( const VariableLengthData& encodedValue ) const
    throw( CouldNotDecode,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	return RegionHandle();
}

/////////////////////////////////////////////////////////////////////////////////
// Portico Dataatype services ///////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
IDatatype* PorticoRtiAmbassador::getAttributeDatatype( ObjectClassHandle whichClass,
                                                       AttributeHandle theHandle)
	throw( InteractionParameterNotDefined,
           InvalidParameterHandle,
           InvalidInteractionClassHandle,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{

	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// Convert handles for call
	jint classHandle = JniUtils::fromHandle(whichClass);
	jint attributeHandle = JniUtils::fromHandle(theHandle);

	// Get the datatype name as a jstring, and convert to wstring
	jstring datatypeName = (jstring)jnienv->CallObjectMethod( javarti->jproxy,
	                                                          javarti->GET_ATTRIBUTE_DATATYPE,
	                                                          classHandle,
	                                                          attributeHandle );
	javarti->exceptionCheck();

	std::wstring className = JniUtils::toWideString( jnienv, datatypeName );
	return this->datatypeRetriever->getDatatype( className );
}


IDatatype* PorticoRtiAmbassador::getParameterDatatype( InteractionClassHandle whichClass,
                                                       ParameterHandle theHandle)
	throw( InteractionParameterNotDefined,
	       InvalidParameterHandle,
           InvalidInteractionClassHandle,
           FederateNotExecutionMember,
           NotConnected,
           RTIinternalError )
{
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// Convert handles for call
	jint classHandle = JniUtils::fromHandle(whichClass);
	jint parameterHandle = JniUtils::fromHandle(theHandle);

	// Get the class type / name token pair
	jstring datatypeName = (jstring)jnienv->CallObjectMethod( javarti->jproxy,
	                                                          javarti->GET_PARAMETER_DATATYPE,
	                                                          classHandle,
	                                                          parameterHandle );
	javarti->exceptionCheck();

	// Create the string set from the 
	std::wstring className = JniUtils::toWideString( jnienv, datatypeName );
	return this->datatypeRetriever->getDatatype( className );
}

PORTICO1516E_NS_END
