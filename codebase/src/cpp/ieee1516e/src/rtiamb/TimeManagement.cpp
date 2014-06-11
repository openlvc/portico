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
// Time Management Services /////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////
// 8.2
void PorticoRtiAmbassador::enableTimeRegulation( const LogicalTimeInterval& theLookahead )
	throw( InvalidLookahead,
	       InTimeAdvancingState,
	       RequestForTimeRegulationPending,
	       TimeRegulationAlreadyEnabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] enableTimeRegulation(): lookahead=%ls",
	               theLookahead.toString().c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// get java versions of the parameters
	jdouble jlookahead = JniUtils::fromInterval( theLookahead );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->ENABLE_TIME_REGULATION,
	                        jlookahead );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] enableTimeRegulation(): lookahead=%ls",
	               theLookahead.toString().c_str() );
}

// 8.4
void PorticoRtiAmbassador::disableTimeRegulation()
	throw( TimeRegulationIsNotEnabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] disableTimeRegulation()" );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->DISABLE_TIME_REGULATION );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] disableTimeRegulation()" );

}

// 8.5
void PorticoRtiAmbassador::enableTimeConstrained()
	throw( InTimeAdvancingState,
	       RequestForTimeConstrainedPending,
	       TimeConstrainedAlreadyEnabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] enableTimeConstrained()" );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->ENABLE_TIME_CONSTRAINED );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] enableTimeConstrained()" );
}

// 8.7
void PorticoRtiAmbassador::disableTimeConstrained()
	throw( TimeConstrainedIsNotEnabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] disableTimeConstrained()" );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->DISABLE_TIME_CONSTRAINED );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] disableTimeConstrained()" );
}

// 8.8
void PorticoRtiAmbassador::timeAdvanceRequest( const LogicalTime& theTime )
	throw( LogicalTimeAlreadyPassed,
	       InvalidLogicalTime,
	       InTimeAdvancingState,
	       RequestForTimeRegulationPending,
	       RequestForTimeConstrainedPending,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] timeAdvanceRequest(): time=%ls", theTime.toString().c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jdouble jtime = JniUtils::fromTime( theTime );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->TIME_ADVANCE_REQUEST,
	                        jtime );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] timeAdvanceRequest(): time=%ls", theTime.toString().c_str() );
}

// 8.9
void PorticoRtiAmbassador::timeAdvanceRequestAvailable( const LogicalTime& theTime )
	throw( LogicalTimeAlreadyPassed,
	       InvalidLogicalTime,
	       InTimeAdvancingState,
	       RequestForTimeRegulationPending,
	       RequestForTimeConstrainedPending,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] timeAdvanceRequestAvailable(): time=%ls",
	               theTime.toString().c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// get java versions of the parameters
	jdouble jtime = JniUtils::fromTime( theTime );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->TIME_ADVANCE_REQUEST_AVAILABLE,
	                        jtime );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] timeAdvanceRequestAvailable(): time=%ls",
	               theTime.toString().c_str() );
}

// 8.10
void PorticoRtiAmbassador::nextMessageRequest( const LogicalTime& theTime )
	throw( LogicalTimeAlreadyPassed,
	       InvalidLogicalTime,
	       InTimeAdvancingState,
	       RequestForTimeRegulationPending,
	       RequestForTimeConstrainedPending,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] nextMessageRequest(): time=%ls", theTime.toString().c_str() );
	
	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jdouble jtime = JniUtils::fromTime( theTime );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->NEXT_EVENT_REQUEST,
	                        jtime );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] nextMessageRequest(): time=%ls", theTime.toString().c_str() );
}

// 8.11
void PorticoRtiAmbassador::nextMessageRequestAvailable( const LogicalTime& theTime )
	throw( LogicalTimeAlreadyPassed,
	       InvalidLogicalTime,
	       InTimeAdvancingState,
	       RequestForTimeRegulationPending,
	       RequestForTimeConstrainedPending,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] nextMessageRequestAvailable(): time=%ls",
	               theTime.toString().c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// get java versions of the parameters
	jdouble jtime = JniUtils::fromTime( theTime );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->NEXT_EVENT_REQUEST_AVAILABLE,
	                        jtime );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] nextMessageRequestAvailable(): time=%ls",
	               theTime.toString().c_str() );
}

// 8.12
void PorticoRtiAmbassador::flushQueueRequest( const LogicalTime& theTime )
	throw( LogicalTimeAlreadyPassed,
	       InvalidLogicalTime,
	       InTimeAdvancingState,
	       RequestForTimeRegulationPending,
	       RequestForTimeConstrainedPending,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] flushQueueRequest(): time=%ls", theTime.toString().c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jdouble jtime = JniUtils::fromTime( theTime );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->FLUSH_QUEUE_REQUEST,
	                        jtime );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] flushQueueRequest(): time=%ls", theTime.toString().c_str() );
}

// 8.14
void PorticoRtiAmbassador::enableAsynchronousDelivery()
	throw( AsynchronousDeliveryAlreadyEnabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] enableAsynchronousDelivery()" );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->ENABLE_ASYNCHRONOUS_DELIVERY );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] enableAsynchronousDelivery()" );
}

// 8.15
void PorticoRtiAmbassador::disableAsynchronousDelivery()
	throw( AsynchronousDeliveryAlreadyDisabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] disableAsynchronousDelivery()" );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->DISABLE_ASYNCHRONOUS_DELIVERY );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] disableAsynchronousDelivery()" );
}

// 8.16
bool PorticoRtiAmbassador::queryGALT( LogicalTime& theTime )
	throw( SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] queryGALT()" );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// call the method
	jdouble jtime = jnienv->CallDoubleMethod( javarti->jproxy, javarti->QUERY_GALT );
	// clean up and run the exception check
	javarti->exceptionCheck();

	if( jtime == -1 )
	{
		logger->trace( "[Finished] queryGALT()" );
		return false;
	}
	else
	{
		logger->trace( "[Finished] queryGALT()" );
		theTime = HLAfloat64Time( jtime );
		return true;
	}
}

// 8.17
void PorticoRtiAmbassador::queryLogicalTime( LogicalTime& theTime )
	throw( SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] queryLogicalTime()" );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// call the method
	jdouble jtime = jnienv->CallDoubleMethod( javarti->jproxy, javarti->QUERY_TIME );
	// clean up and run the exception check
	javarti->exceptionCheck();

	theTime = HLAfloat64Time( jtime );
	logger->trace( "[Finished] queryLogicalTime()" );
}

// 8.18
bool PorticoRtiAmbassador::queryLITS( LogicalTime& theTime )
	throw( SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] queryLITS()" );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// call the method
	jdouble jtime = jnienv->CallDoubleMethod( javarti->jproxy, javarti->QUERY_LITS );
	// clean up and run the exception check
	javarti->exceptionCheck();

	if( jtime == -1 )
	{
		logger->trace( "[Finished] queryLITS()" );
		return false;
	}
	else
	{
		logger->trace( "[Finished] queryLITS()" );
		theTime = HLAfloat64Time( jtime );
		return true;
	}
}

// 8.19
void PorticoRtiAmbassador::modifyLookahead( const LogicalTimeInterval& theLookahead )
	throw( InvalidLookahead,
	       InTimeAdvancingState,
	       TimeRegulationIsNotEnabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] modifyLookahead(): lookahead=%ls", theLookahead.toString().c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// get java versions of the parameters
	jdouble jlookahead = JniUtils::fromInterval( theLookahead );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->MODIFY_LOOKAHEAD,
	                        jlookahead );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] modifyLookahead(): lookahead=%ls", theLookahead.toString().c_str() );
}

// 8.20
void PorticoRtiAmbassador::queryLookahead( LogicalTimeInterval& interval )
	throw( TimeRegulationIsNotEnabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] queryLookahead()" );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// call the method
	jdouble jtime = jnienv->CallDoubleMethod( javarti->jproxy, javarti->QUERY_LOOKAHEAD );
	// clean up and run the exception check
	javarti->exceptionCheck();

	interval = HLAfloat64Interval( jtime );
	logger->trace( "[Finished] queryLookahead()" );
}

// 8.21
void PorticoRtiAmbassador::retract( MessageRetractionHandle theHandle )
	throw( MessageCanNoLongerBeRetracted,
	       InvalidMessageRetractionHandle,
	       TimeRegulationIsNotEnabled,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] retract() handle=%ls", theHandle.toString().c_str() );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->RETRACT,
	                        JniUtils::fromHandle(theHandle) );

	// clean up and run the exception check
	javarti->exceptionCheck();
	logger->trace( "[Finished] retract() handle=%ls", theHandle.toString().c_str() );
}

// 8.23
void PorticoRtiAmbassador::changeAttributeOrderType( ObjectInstanceHandle theObject,
                                                     const AttributeHandleSet& theAttributes,
                                                     OrderType theType )
	throw( AttributeNotOwned,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] changeAttributeOrderType(): theObject=%ls, theAttributes=%s, order=%d",
	               theObject.toString().c_str(),
	               Logger::toString(theAttributes).c_str(),
	               theType );

	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();
	
	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	jintArray jattributes = JniUtils::fromSet( jnienv, theAttributes );
	jstring jorder = JniUtils::fromOrder( jnienv, theType );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->CHANGE_ATTRIBUTE_ORDER_TYPE,
	                        jobjectHandle,
	                        jattributes,
	                        jorder );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jattributes );
	jnienv->DeleteLocalRef( jorder );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] changeAttributeOrderType(): theObject=%ls, theAttributes=%s, order=%d",
	               theObject.toString().c_str(),
	               Logger::toString(theAttributes).c_str(),
	               theType );
}

// 8.24
void PorticoRtiAmbassador::changeInteractionOrderType( InteractionClassHandle theClass,
                                                       OrderType theType )
	throw( InteractionClassNotPublished,
	       InteractionClassNotDefined,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->trace( "[Starting] changeInteractionOrderType(): theClass=%ls, order=%d",
	               theClass.toString().c_str(), theType );
	
	// Get active environment
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// get java versions of the parameters
	jint jclassHandle = JniUtils::fromHandle( theClass );
	jstring jorder = JniUtils::fromOrder( jnienv, theType );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->CHANGE_INTERACTION_ORDER_TYPE,
	                        jclassHandle,
	                        jorder );


	// clean up and run the exception check
	jnienv->DeleteLocalRef( jorder );
	javarti->exceptionCheck();
	
	logger->trace( "[Finished] changeInteractionOrderType(): theClass=%ls, order=%d",
	               theClass.toString().c_str(), theType );
}

PORTICO1516E_NS_END
