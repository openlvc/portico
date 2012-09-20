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
	logger->debug( "[Starting] enableTimeRegulation(): lookahead=%ls",
	               theLookahead.toString().c_str() );
	
	// get java versions of the parameters
	jdouble jlookahead = JniUtils::fromInterval( theLookahead );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->ENABLE_TIME_REGULATION,
	                        jlookahead );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->info( "[Finished] enableTimeRegulation(): lookahead=%ls",
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
	logger->debug( "[Starting] disableTimeRegulation()" );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->DISABLE_TIME_REGULATION );
	javarti->exceptionCheck();
	
	logger->info( "[Finished] disableTimeRegulation()" );

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
	logger->debug( "[Starting] enableTimeConstrained()" );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->ENABLE_TIME_CONSTRAINED );
	javarti->exceptionCheck();
	
	logger->info( "[Finished] enableTimeConstrained()" );
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
	logger->debug( "[Starting] disableTimeConstrained()" );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->DISABLE_TIME_CONSTRAINED );
	javarti->exceptionCheck();
	
	logger->info( "[Finished] disableTimeConstrained()" );
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
	logger->debug( "[Starting] timeAdvanceRequest(): time=%ls", theTime.toString().c_str() );
	
	// get java versions of the parameters
	jdouble jtime = JniUtils::fromTime( theTime );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->TIME_ADVANCE_REQUEST,
	                        jtime );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->info( "[Finished] timeAdvanceRequest(): time=%ls", theTime.toString().c_str() );
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
	logger->debug( "[Starting] timeAdvanceRequestAvailable(): time=%ls",
	               theTime.toString().c_str() );
	
	// get java versions of the parameters
	jdouble jtime = JniUtils::fromTime( theTime );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->TIME_ADVANCE_REQUEST_AVAILABLE,
	                        jtime );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->info( "[Finished] timeAdvanceRequestAvailable(): time=%ls",
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
	logger->debug( "[Starting] nextMessageRequest(): time=%ls", theTime.toString().c_str() );
	
	// get java versions of the parameters
	jdouble jtime = JniUtils::fromTime( theTime );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->NEXT_EVENT_REQUEST,
	                        jtime );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->info( "[Finished] nextMessageRequest(): time=%ls", theTime.toString().c_str() );
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
	logger->debug( "[Starting] nextMessageRequestAvailable(): time=%ls",
	               theTime.toString().c_str() );
	
	// get java versions of the parameters
	jdouble jtime = JniUtils::fromTime( theTime );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->NEXT_EVENT_REQUEST_AVAILABLE,
	                        jtime );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->info( "[Finished] nextMessageRequestAvailable(): time=%ls",
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
	logger->debug( "[Starting] flushQueueRequest(): time=%ls", theTime.toString().c_str() );
	
	// get java versions of the parameters
	jdouble jtime = JniUtils::fromTime( theTime );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->FLUSH_QUEUE_REQUEST,
	                        jtime );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->info( "[Finished] flushQueueRequest(): time=%ls", theTime.toString().c_str() );
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
	logger->debug( "[Starting] enableAsynchronousDelivery()" );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->ENABLE_ASYNCHRONOUS_DELIVERY );
	javarti->exceptionCheck();
	
	logger->info( "[Finished] enableAsynchronousDelivery()" );
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
	logger->debug( "[Starting] disableAsynchronousDelivery()" );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy, javarti->DISABLE_ASYNCHRONOUS_DELIVERY );
	javarti->exceptionCheck();
	
	logger->info( "[Finished] disableAsynchronousDelivery()" );
}

// 8.16
bool PorticoRtiAmbassador::queryGALT( LogicalTime& theTime )
	throw( SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->debug( "[Starting] queryGALT()" );
	
	// call the method
	jdouble jtime = jnienv->CallDoubleMethod( javarti->jproxy, javarti->QUERY_GALT );
	// clean up and run the exception check
	javarti->exceptionCheck();

	if( jtime == -1 )
	{
		logger->info( "[Finished] queryGALT()" );
		return false;
	}
	else
	{
		logger->info( "[Finished] queryGALT()" );
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
	logger->debug( "[Starting] queryLogicalTime()" );
	
	// call the method
	jdouble jtime = jnienv->CallDoubleMethod( javarti->jproxy, javarti->QUERY_TIME );
	// clean up and run the exception check
	javarti->exceptionCheck();

	theTime = HLAfloat64Time( jtime );
	logger->info( "[Finished] queryLogicalTime()" );
}

// 8.18
bool PorticoRtiAmbassador::queryLITS( LogicalTime& theTime )
	throw( SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	logger->debug( "[Starting] queryLITS()" );
	
	// call the method
	jdouble jtime = jnienv->CallDoubleMethod( javarti->jproxy, javarti->QUERY_LITS );
	// clean up and run the exception check
	javarti->exceptionCheck();

	if( jtime == -1 )
	{
		logger->info( "[Finished] queryLITS()" );
		return false;
	}
	else
	{
		logger->info( "[Finished] queryLITS()" );
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
	logger->debug( "[Starting] modifyLookahead(): lookahead=%ls", theLookahead.toString().c_str() );
	
	// get java versions of the parameters
	jdouble jlookahead = JniUtils::fromInterval( theLookahead );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->MODIFY_LOOKAHEAD,
	                        jlookahead );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	logger->info( "[Finished] modifyLookahead(): lookahead=%ls", theLookahead.toString().c_str() );
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
	logger->debug( "[Starting] queryLookahead()" );
	
	// call the method
	jdouble jtime = jnienv->CallDoubleMethod( javarti->jproxy, javarti->QUERY_LOOKAHEAD );
	// clean up and run the exception check
	javarti->exceptionCheck();

	interval = HLAfloat64Interval( jtime );
	logger->info( "[Finished] queryLookahead()" );
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
	logger->debug( "[Starting] retract() handle=%ls", theHandle.toString().c_str() );
	
	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->RETRACT,
	                        JniUtils::fromHandle(theHandle) );

	// clean up and run the exception check
	javarti->exceptionCheck();
	logger->debug( "[Finished] retract() handle=%ls", theHandle.toString().c_str() );
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
	logger->debug( "[Starting] changeAttributeOrderType(): theObject=%ls, theAttributes=%s, order=%d",
	               theObject.toString().c_str(),
	               Logger::toString(theAttributes).c_str(),
	               theType );
	
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
	
	logger->info( "[Finished] changeAttributeOrderType(): theObject=%ls, theAttributes=%s, order=%d",
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
	logger->debug( "[Starting] changeInteractionOrderType(): theClass=%ls, order=%d",
	               theClass.toString().c_str(), theType );
	
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
	
	logger->info( "[Finished] changeInteractionOrderType(): theClass=%ls, order=%d",
	              theClass.toString().c_str(), theType );
}

PORTICO1516E_NS_END
