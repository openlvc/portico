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
#include "utils/StringUtils.h"

PORTICO1516E_NS_START

/////////////////////////////////////////////////////////////////////////////////
// Federation Management Services ///////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
// 4.2
void PorticoRtiAmbassador::connect( FederateAmbassador& federateAmbassador,
                                    CallbackModel theCallbackModel,
                                    const std::wstring& localSettingsDesignator )
	throw( ConnectionFailed,
		   InvalidLocalSettingsDesignator,
		   UnsupportedCallbackModel,
		   AlreadyConnected,
		   CallNotAllowedFromWithinCallback,
		   RTIinternalError )
{
	logger->trace( "[Starting] connect(): callback=%d", theCallbackModel );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// get java versions of the parameters
	jstring jmodel = JniUtils::fromCallbackModel( jnienv, theCallbackModel );

	// save the fedamb reference
	javarti->fedamb = &federateAmbassador;
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->CONNECT, jmodel );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jmodel );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] connect(): callback=%d", theCallbackModel );
}

// 4.3
void PorticoRtiAmbassador::disconnect() throw( FederateIsExecutionMember,
                                               CallNotAllowedFromWithinCallback,
                                               RTIinternalError )
{
	logger->trace( "[Starting] disconnect()" );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->DISCONNECT );
	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] disconnect()" );
}

// 4.5
void PorticoRtiAmbassador::createFederationExecution( const std::wstring& federationName,
								                      const std::wstring& fomModule,
								                      const std::wstring& timeImplementation )
	throw( CouldNotCreateLogicalTimeFactory,
		   InconsistentFDD,
		   ErrorReadingFDD,
		   CouldNotOpenFDD,
		   FederationExecutionAlreadyExists,
		   NotConnected,
		   RTIinternalError )
{
	logger->trace( "[Starting] createFederationExecution(federation,fom): name=%ls, fedfile=%ls, time=%ls",
	               federationName.c_str(),
	               fomModule.c_str(),
	               timeImplementation.c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jstring jfederation = JniUtils::fromWideString( jnienv, federationName );
	jstring jfom = JniUtils::fromWideString( jnienv, fomModule );
	jstring jtime = JniUtils::fromWideString( jnienv, timeImplementation );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->CREATE_FEDERATION,
	                        jfederation,
	                        jfom,
	                        jtime );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jfederation );
	jnienv->DeleteLocalRef( jfom );
	jnienv->DeleteLocalRef( jtime );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] createFederationExecution(federation,fom): name=%ls", federationName.c_str() );
}

void PorticoRtiAmbassador::createFederationExecution( const std::wstring& federationName,
                                                      const std::vector<std::wstring>& fomModules,
                                                      const std::wstring& timeImplementation )
	throw( CouldNotCreateLogicalTimeFactory,
		   InconsistentFDD,
		   ErrorReadingFDD,
		   CouldNotOpenFDD,
		   FederationExecutionAlreadyExists,
		   NotConnected,
		   RTIinternalError )
{
	logger->trace( "[Starting] createFederationExecution(federation,modules): name=%ls, fedfiles=%ls, time=%ls",
	               federationName.c_str(),
	               Logger::toWString(fomModules).c_str(),
	               timeImplementation.c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jstring jfederation = JniUtils::fromWideString( jnienv, federationName );
	jobjectArray jfoms = JniUtils::fromVector( jnienv, fomModules );
	jstring jtime = JniUtils::fromWideString( jnienv, timeImplementation );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->CREATE_FEDERATION_WITH_MODULES,
	                        jfederation,
	                        jfoms,
	                        jtime );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jfederation );
	jnienv->DeleteLocalRef( jtime );
	JniUtils::deleteJniArray( jnienv, jfoms );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] createFederationExecution(federation,modules) name=%ls", federationName.c_str() );
}

void PorticoRtiAmbassador::createFederationExecutionWithMIM(
	const std::wstring& federationName,
    const std::vector<std::wstring>& fomModules,
    const std::wstring& mimModule,
    const std::wstring& timeImplementation )
	throw( CouldNotCreateLogicalTimeFactory,
		   InconsistentFDD,
		   ErrorReadingFDD,
		   CouldNotOpenFDD,
		   DesignatorIsHLAstandardMIM,
		   ErrorReadingMIM,
		   CouldNotOpenMIM,
		   FederationExecutionAlreadyExists,
		   NotConnected,
		   RTIinternalError )
{
	logger->trace( "[Starting] createFederationExecution(federation,modules,mim): name=%ls, fedfiles=%ls, mim=%ls, time=%ls",
	               federationName.c_str(),
	               Logger::toWString(fomModules).c_str(),
	               mimModule.c_str(),
	               timeImplementation.c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jstring jfederation = JniUtils::fromWideString( jnienv, federationName );
	jobjectArray jfoms = JniUtils::fromVector( jnienv, fomModules );
	jstring jmim = JniUtils::fromWideString( jnienv, mimModule );
	jstring jtime = JniUtils::fromWideString( jnienv, timeImplementation );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->CREATE_FEDERATION_WITH_MIM,
	                        jfederation,
	                        jfoms,
	                        jmim,
	                        jtime );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jfederation );
	JniUtils::deleteJniArray( jnienv, jfoms );
	jnienv->DeleteLocalRef( jmim );
	jnienv->DeleteLocalRef( jtime );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] createFederationExecution(federation,modules,mim) name=%ls", federationName.c_str() );
}

// 4.6
void PorticoRtiAmbassador::destroyFederationExecution( const std::wstring& federationName )
	throw( FederatesCurrentlyJoined,
		   FederationExecutionDoesNotExist,
		   NotConnected,
		   RTIinternalError )
{
	logger->trace( "[Starting] destroyFederationExecution(): name=%ls", federationName.c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jstring jfederation = JniUtils::fromWideString( jnienv, federationName );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->DESTROY_FEDERATION,
	                        jfederation );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jfederation );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] destroyFederationExecution(): name=%ls", federationName.c_str() );
}

// 4.7
void PorticoRtiAmbassador::listFederationExecutions() throw( NotConnected, RTIinternalError )
{
	logger->trace( "[Starting] listFederationExecutions()" );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->LIST_FEDERATIONS );
	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] listFederationExecutions()" );
}

// 4.9
FederateHandle PorticoRtiAmbassador::joinFederationExecution(
	const std::wstring& federateType,
	const std::wstring& federationName,
	const std::vector<std::wstring>& fomModules )
	throw( CouldNotCreateLogicalTimeFactory,
		   FederationExecutionDoesNotExist,
		   InconsistentFDD,
		   ErrorReadingFDD, 
		   CouldNotOpenFDD,
		   SaveInProgress,
		   RestoreInProgress,
		   FederateAlreadyExecutionMember,
		   NotConnected,
		   CallNotAllowedFromWithinCallback,
		   RTIinternalError )
{
	logger->trace( "[Starting] joinFederationExecution(): federateType=%ls, federation=%ls",
	               federateType.c_str(),
	               federationName.c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// get java versions of the parameters
	jstring jtype = JniUtils::fromWideString( jnienv, federateType );
	jstring jfederation = JniUtils::fromWideString( jnienv, federationName );
	jobjectArray jfoms = JniUtils::fromVector( jnienv, fomModules );

	// call the method
	jint handle = jnienv->CallIntMethod( javarti->jproxy,
	                                     javarti->JOIN_FEDERATION,
	                                     jtype,
	                                     jfederation,
	                                     jfoms );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jtype );
	jnienv->DeleteLocalRef( jfederation );
	JniUtils::deleteJniArray( jnienv, jfoms );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] joinFederationExecution(): federation=%ls", federationName.c_str() );
	
	// return the handle
	return JniUtils::toFederateHandle( handle );
}

FederateHandle PorticoRtiAmbassador::joinFederationExecution(
	const std::wstring& federateName,
	const std::wstring& federateType,
	const std::wstring& federationName,
	const std::vector<std::wstring>& additionalFomModules )
	throw( CouldNotCreateLogicalTimeFactory,
		   FederateNameAlreadyInUse,
		   FederationExecutionDoesNotExist,
		   InconsistentFDD,
		   ErrorReadingFDD,
		   CouldNotOpenFDD,
		   SaveInProgress,
		   RestoreInProgress,
		   FederateAlreadyExecutionMember,
		   NotConnected,
		   CallNotAllowedFromWithinCallback,
		   RTIinternalError )
{
	logger->trace( "[Starting] joinFederationExecution(): federateType=%ls, federation=%ls",
	               federateType.c_str(),
	               federationName.c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// get java versions of the parameters
	jstring jname = JniUtils::fromWideString( jnienv, federateName );
	jstring jtype = JniUtils::fromWideString( jnienv, federateType );
	jstring jfederation = JniUtils::fromWideString( jnienv, federationName );
	jobjectArray jfoms = JniUtils::fromVector( jnienv, additionalFomModules );

	// call the method
	jint handle = jnienv->CallIntMethod( javarti->jproxy,
	                                     javarti->JOIN_FEDERATION_WITH_NAME,
	                                     jname,
	                                     jtype,
	                                     jfederation,
	                                     jfoms );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jname );
	jnienv->DeleteLocalRef( jtype );
	jnienv->DeleteLocalRef( jfederation );
	JniUtils::deleteJniArray( jnienv, jfoms );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] joinFederationExecution(): federation=%ls", federationName.c_str() );
	
	// return the handle
	return JniUtils::toFederateHandle( handle );
}

// 4.10
void PorticoRtiAmbassador::resignFederationExecution( ResignAction resignAction )
	throw( InvalidResignAction,
		   OwnershipAcquisitionPending,
		   FederateOwnsAttributes,
		   FederateNotExecutionMember,
		   NotConnected,
		   CallNotAllowedFromWithinCallback,
		   RTIinternalError )
{
	logger->trace( "[Starting] resignFederationExecution(): action=%d", resignAction );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// get java versions of the parameters
	jstring jaction = JniUtils::fromResignAction( jnienv, resignAction );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->RESIGN_FEDERATION,
	                        jaction );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jaction );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] resignFederationExecution(): action=%d", resignAction );
}

PORTICO1516E_NS_END
