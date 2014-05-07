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
// Synchronization Services /////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
// 4.11
void PorticoRtiAmbassador::registerFederationSynchronizationPoint( const std::wstring& label,
                                                                   const VariableLengthData& tag )
	throw( SaveInProgress,
		   RestoreInProgress,
		   FederateNotExecutionMember,
		   NotConnected,
		   RTIinternalError )
{
	logger->trace( "[Starting] registerFederationSynchronizationPoint(): label=%ls",label.c_str() );
	
	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jstring jlabel = JniUtils::fromWideString( jnienv, label );
	jbyteArray jtag = JniUtils::fromTag( jnienv, tag );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->REGISTER_FEDERATION_SYNC,
	                        jlabel,
	                        jtag );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jlabel );
	jnienv->DeleteLocalRef( jtag );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] registerFederationSynchronizationPoint(): label=%ls", label.c_str() );
}

void PorticoRtiAmbassador::registerFederationSynchronizationPoint( const std::wstring& label,
                                                                   const VariableLengthData& tag,
                                                                   const FederateHandleSet& syncset )
	throw( InvalidFederateHandle,
		   SaveInProgress,
		   RestoreInProgress,
		   FederateNotExecutionMember,
		   NotConnected,
		   RTIinternalError )
{
	logger->trace( "[Starting] registerFederationSynchronizationPoint(): label=%ls, set=%s",
	               label.c_str(),
	               Logger::toString(syncset).c_str() );
	
	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jstring jlabel = JniUtils::fromWideString( jnienv, label );
	jbyteArray jtag = JniUtils::fromTag( jnienv, tag );
	jintArray jfederates = JniUtils::fromSet( jnienv, syncset );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->REGISTER_FEDERATION_SYNC_FEDSET,
	                        jlabel,
	                        jtag,
	                        jfederates );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jlabel );
	jnienv->DeleteLocalRef( jtag );
	jnienv->DeleteLocalRef( jfederates );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] registerFederationSynchronizationPoint(): label=%ls, set=%s",
	               label.c_str(),
	               Logger::toString(syncset).c_str() );
}

// 4.14
void PorticoRtiAmbassador::synchronizationPointAchieved( const std::wstring& label,
                                                         bool successfully )
	throw( SynchronizationPointLabelNotAnnounced,
		   SaveInProgress,
		   RestoreInProgress,
		   FederateNotExecutionMember,
		   NotConnected,
		   RTIinternalError )
{
	logger->trace( "[Starting] synchronizationPointAchieved(): label=%ls, success=%d",
	               label.c_str(),
	               successfully );
	
	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jstring jlabel = JniUtils::fromWideString( jnienv, label );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->SYNC_POINT_ACHIEVED_WITH_INDICATOR,
	                        jlabel,
	                        successfully );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jlabel );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] synchronizationPointAchieved(): label=%ls, success=%d",
	               label.c_str(),
	               successfully );
}

PORTICO1516E_NS_END
