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
// Declaration Management Services //////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
// 5.2
void PorticoRtiAmbassador::publishObjectClassAttributes( ObjectClassHandle theClass,
                                                         const AttributeHandleSet& attributes )
	throw( AttributeNotDefined,
	       ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] publishObjectClassAttributes(): class=%ls, attributes=%s",
	               theClass.toString().c_str(),
	               Logger::toString(attributes).c_str() );
	
	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jclassHandle = JniUtils::fromHandle( theClass );
	jintArray jattributes = JniUtils::fromSet( jnienv, attributes );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->PUBLISH_OBJECT_CLASS,
	                        jclassHandle,
	                        jattributes );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jattributes );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] publishObjectClassAttributes(): class=%ls, attributes=%s",
	                theClass.toString().c_str(),
	                Logger::toString(attributes).c_str() );
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
	logger->trace( "[Starting] unpublishObjectClass(): class=%ls", theClass.toString().c_str() );
	
	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jclassHandle = JniUtils::fromHandle( theClass );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->UNPUBLISH_OBJECT_CLASS,
	                        jclassHandle );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] unpublishObjectClass(): class=%ls", theClass.toString().c_str() );
}


void PorticoRtiAmbassador::unpublishObjectClassAttributes( ObjectClassHandle theClass,
                                                           const AttributeHandleSet& attributes )
	throw( OwnershipAcquisitionPending,
	       AttributeNotDefined,
	       ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] unpublishObjectClassAttributes(): class=%ls, attributes=%s",
	               theClass.toString().c_str(),
	               Logger::toString(attributes).c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jclassHandle = JniUtils::fromHandle( theClass );
	jintArray jattributes = JniUtils::fromSet( jnienv, attributes );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->UNPUBLISH_OBJECT_CLASS_WITH_ATTRIBUTES,
	                        jclassHandle,
	                        jattributes );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jattributes );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] unpublishObjectClassAttributes(): class=%ls, attributes=%s",
	                theClass.toString().c_str(),
	                Logger::toString(attributes).c_str() );
}

// 5.4
void PorticoRtiAmbassador::publishInteractionClass( InteractionClassHandle theClass )
	throw( InteractionClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] publishInteractionClass(): class=%ls", theClass.toString().c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jclassHandle = JniUtils::fromHandle( theClass );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->PUBLISH_INTERACTION_CLASS,
	                        jclassHandle );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] publishInteractionClass(): class=%ls", theClass.toString().c_str() );
}

// 5.5
void PorticoRtiAmbassador::unpublishInteractionClass( InteractionClassHandle theClass )
	throw( InteractionClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] unpublishInteractionClass(): class=%ls", theClass.toString().c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jclassHandle = JniUtils::fromHandle( theClass );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->UNPUBLISH_INTERACTION_CLASS,
	                        jclassHandle );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] unpublishInteractionClass(): class=%ls", theClass.toString().c_str() );
}

// 5.6
void PorticoRtiAmbassador::subscribeObjectClassAttributes( ObjectClassHandle theClass,
                                                           const AttributeHandleSet& attributes,
                                                           bool active,
                                                           const std::wstring& updateRate )
	throw( AttributeNotDefined,
	       ObjectClassNotDefined,
	       InvalidUpdateRateDesignator,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] subscribeObjectClassAttributes(): class=%ls, attributes=%s, active=%d, rate=%ls",
	                theClass.toString().c_str(),
	                Logger::toString(attributes).c_str(),
	                active,
	                updateRate.c_str() );
	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// get java versions of the parameters
	jint jclassHandle = JniUtils::fromHandle( theClass );
	jintArray jattributes = JniUtils::fromSet( jnienv, attributes );
	jstring jrate = JniUtils::fromWideString( jnienv, updateRate );

	// which method do we want to call!?
	if( active )
	{
		jnienv->CallVoidMethod( javarti->jproxy,
								javarti->SUBSCRIBE_ATTRIBUTES,
								jclassHandle,
								jattributes,
								jrate );
	}
	else
	{
		jnienv->CallVoidMethod( javarti->jproxy,
								javarti->SUBSCRIBE_ATTRIBUTES_PASSIVE,
								jclassHandle,
								jattributes,
								jrate );
	}

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jattributes );
	jnienv->DeleteLocalRef( jrate );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] subscribeObjectClassAttributes(): class=%ls, attributes=%s, active=%d, rate=%ls",
	                theClass.toString().c_str(),
	                Logger::toString(attributes).c_str(),
	                active,
	                updateRate.c_str() );
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
	logger->trace( "[Starting] unsubscribeObjectClass(): class=%ls", theClass.toString().c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// get java versions of the parameters
	jint jclassHandle = JniUtils::fromHandle( theClass );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->UNSUBSCRIBE_OBJECT_CLASS,
	                        jclassHandle );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] unsubscribeObjectClass(): class=%ls", theClass.toString().c_str() );
}

void PorticoRtiAmbassador::unsubscribeObjectClassAttributes( ObjectClassHandle theClass,
                                                             const AttributeHandleSet& attributes )
	throw( AttributeNotDefined,
	       ObjectClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] unsubscribeObjectClassAttributes(): class=%ls, attributes=%s",
	               theClass.toString().c_str(),
	               Logger::toString(attributes).c_str() );
	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// get java versions of the parameters
	jint jclassHandle = JniUtils::fromHandle( theClass );
	jintArray jattributes = JniUtils::fromSet( jnienv, attributes );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->UNSUBSCRIBE_OBJECT_CLASS_WITH_ATTRIBUTES,
	                        jclassHandle,
	                        jattributes );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jattributes );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] unsubscribeObjectClassAttributes(): class=%ls, attributes=%s",
	                theClass.toString().c_str(),
	                Logger::toString(attributes).c_str() );
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
	logger->trace( "[Starting] subscribeInteractionClass(): class=%ls, active=%d",
	               theClass.toString().c_str(),
	               active );
	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// get java versions of the parameters
	jint jclassHandle = JniUtils::fromHandle( theClass );

	// call the method
	jmethodID method = javarti->SUBSCRIBE_INTERACTION_CLASS;
	if( active == false )
		method = javarti->SUBSCRIBE_INTERACTION_CLASS_PASSIVE;

	jnienv->CallVoidMethod( javarti->jproxy, method, jclassHandle );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] subscribeInteractionClass(): class=%ls, active=%d",
	               theClass.toString().c_str(),
	               active );
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
	logger->trace( "[Starting] unsubscribeInteractionClass(): class=%ls", theClass.toString().c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// get java versions of the parameters
	jint jclassHandle = JniUtils::fromHandle( theClass );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->UNSUBSCRIBE_INTERACTION_CLASS,
	                        jclassHandle );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] unsubscribeInteractionClass(): class=%ls", theClass.toString().c_str() );
}

PORTICO1516E_NS_END
