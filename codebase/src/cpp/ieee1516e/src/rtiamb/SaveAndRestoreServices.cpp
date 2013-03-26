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
// Save and Restore Services ////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
// 4.16
void PorticoRtiAmbassador::requestFederationSave( const std::wstring& label )
	throw( SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] requestFederationSave(): label=%ls", label.c_str() );
	
	// get java versions of the parameters
	jstring jlabel = JniUtils::fromWideString( jnienv, label );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->REQUEST_SAVE,
	                        jlabel );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jlabel );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] requestFederationSave(): label=%ls", label.c_str() );
}

void PorticoRtiAmbassador::requestFederationSave( const std::wstring& label,
                                                  const LogicalTime& theTime )
	throw( LogicalTimeAlreadyPassed,
	       InvalidLogicalTime,
	       FederateUnableToUseTime,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] requestFederationSave(time): label=%ls", label.c_str() );
	
	// get java versions of the parameters
	jstring jlabel = JniUtils::fromWideString( jnienv, label );
	jdouble jtime = JniUtils::fromTime( theTime );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->REQUEST_SAVE,
	                        jlabel,
	                        jtime );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jlabel );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] requestFederationSave(time): label=%ls", label.c_str() );
}


// 4.18
void PorticoRtiAmbassador::federateSaveBegun()
	throw( SaveNotInitiated,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] federateSaveBegun()" );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->SAVE_BEGUN );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] federateSaveBegun()" );
}


// 4.19
void PorticoRtiAmbassador::federateSaveComplete()
	throw( FederateHasNotBegunSave,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] federateSaveComplete()" );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->SAVE_COMPLETE );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] federateSaveComplete()" );
}


void PorticoRtiAmbassador::federateSaveNotComplete()
	throw( FederateHasNotBegunSave,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] federateSaveNotComplete()" );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->SAVE_NOT_COMPLETE );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] federateSaveNotComplete()" );
}


// 4.21
void PorticoRtiAmbassador::abortFederationSave()
	throw( SaveNotInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] abortFederationSave()" );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->SAVE_ABORT );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] abortFederationSave()" );
}


// 4.22
void PorticoRtiAmbassador::queryFederationSaveStatus()
	throw( RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] queryFederationSaveStatus()" );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->SAVE_QUERY );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] queryFederationSaveStatus()" );
}


// 4.24
void PorticoRtiAmbassador::requestFederationRestore( const std::wstring& label )
	throw( SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] requestFederationRestore(): label=%ls", label.c_str() );
	
	// get java versions of the parameters
	jstring jlabel = JniUtils::fromWideString( jnienv, label );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->REQUEST_RESTORE,
	                        jlabel );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jlabel );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] requestFederationRestore(): label=%ls", label.c_str() );
}


// 4.28
void PorticoRtiAmbassador::federateRestoreComplete()
	throw( RestoreNotRequested,
	       SaveInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] federateRestoreComplete()" );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->RESTORE_COMPLETE );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] federateRestoreComplete()" );
}


void PorticoRtiAmbassador::federateRestoreNotComplete()
	throw( RestoreNotRequested,
	       SaveInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] federateRestoreNotComplete()" );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->RESTORE_NOT_COMPLETE );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] federateRestoreNotComplete()" );
}


// 4.30
void PorticoRtiAmbassador::abortFederationRestore()
	throw( RestoreNotInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] abortFederationRestore()" );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->RESTORE_ABORT );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] abortFederationRestore()" );
}


// 4.31
void PorticoRtiAmbassador::queryFederationRestoreStatus()
	throw( SaveInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] queryFederationRestoreStatus()" );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->RESTORE_QUERY );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] queryFederationRestoreStatus()" );
}


PORTICO1516E_NS_END
