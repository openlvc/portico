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
	// The factory is the one of the federation we are joined to, not a fixed choice of ours: a
	// federation keeps the logical time implementation it was created with, and every federate
	// in it exchanges its times in that implementation. Ask the Java side, which is where that
	// is recorded.
	if( isIntegerTimeFederation() )
		return std::auto_ptr<LogicalTimeFactory>( new HLAinteger64TimeFactory() );
	else
		return std::auto_ptr<LogicalTimeFactory>( new HLAfloat64TimeFactory() );
}

/*
 * The name of the logical time implementation the federation works with, straight from the Java
 * side. Anything we cannot resolve - not joined yet, no answer from Java - is reported as the
 * default implementation, which is what a federation gets when it does not ask for one.
 */
std::wstring PorticoRtiAmbassador::getTimeImplementationName() const
{
	// A portico.jar older than this binding does not report one. Every federation it can host
	// works with the default implementation, so that is the honest answer for it.
	if( javarti->GET_TIME_IMPLEMENTATION == NULL )
		return std::wstring( L"HLAfloat64Time" );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	jstring jname = (jstring)jnienv->CallObjectMethod( javarti->jproxy,
	                                                   javarti->GET_TIME_IMPLEMENTATION );
	if( jname == NULL )
		return std::wstring( L"HLAfloat64Time" );

	std::wstring name = JniUtils::toWideString( jnienv, jname );
	jnienv->DeleteLocalRef( jname );

	if( name.empty() )
		return std::wstring( L"HLAfloat64Time" );
	else
		return name;
}

/*
 * True when the federation works with HLAinteger64Time.
 */
bool PorticoRtiAmbassador::isIntegerTimeFederation() const
{
	return getTimeImplementationName().compare( L"HLAinteger64Time" ) == 0;
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

PORTICO1516E_NS_END
