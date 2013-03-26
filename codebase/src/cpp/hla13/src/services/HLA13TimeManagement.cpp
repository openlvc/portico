/*
 *   Copyright 2007 The Portico Project
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
#include "HLA13Common.h"

using namespace portico13;
extern Logger* logger;

//////////////////////////////
// Time Management Services //
//////////////////////////////
// 8.2
void RTI::RTIambassador::enableTimeRegulation( const RTI::FedTime& theFederateTime,
                                               const RTI::FedTime& theLookahead )
	throw( RTI::TimeRegulationAlreadyEnabled,
	       RTI::EnableTimeRegulationPending,
	       RTI::TimeAdvanceAlreadyInProgress,
	       RTI::InvalidFederationTime,
	       RTI::InvalidLookahead,
	       RTI::ConcurrentAccessAttempted,
	       RTI::FederateNotExecutionMember,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// get java versions of the parameters
	jdouble jTime      = privateRefs->rti->convertTime( theFederateTime );
	jdouble jLookahead = privateRefs->rti->convertTime( theLookahead );

	logger->trace( "[Starting] enableTimeRegulation(): federateTime=%f, lookahead=%f",
	               jTime, jLookahead );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->ENABLE_TIME_REGULATION,
	                                  jTime,
	                                  jLookahead );

	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] enableTimeRegulation(): federateTime=%f, lookahead=%f",
	               jTime, jLookahead );
}

// 8.4
void RTI::RTIambassador::disableTimeRegulation()
	throw( RTI::TimeRegulationWasNotEnabled,
	       RTI::ConcurrentAccessAttempted,
	       RTI::FederateNotExecutionMember,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] disableTimeRegulation()" );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->DISABLE_TIME_REGULATION );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] disableTimeRegulation()" );
}

// 8.5
void RTI::RTIambassador::enableTimeConstrained()
	throw( RTI::TimeConstrainedAlreadyEnabled,
	       RTI::EnableTimeConstrainedPending,
	       RTI::TimeAdvanceAlreadyInProgress,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] enableTimeConstrained()" );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->ENABLE_TIME_CONSTRAINED );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] enableTimeConstrained()" );
}

// 8.7
void RTI::RTIambassador::disableTimeConstrained()
	throw( RTI::TimeConstrainedWasNotEnabled,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] disableTimeConstrained()" );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->DISABLE_TIME_CONSTRAINED );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] disableTimeConstrained()" );
}

// 8.8
void RTI::RTIambassador::timeAdvanceRequest( const RTI::FedTime& theTime )
	throw( RTI::InvalidFederationTime,
	       RTI::FederationTimeAlreadyPassed,
	       RTI::TimeAdvanceAlreadyInProgress,
	       RTI::EnableTimeRegulationPending,
	       RTI::EnableTimeConstrainedPending,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// get java versions of the parameters
	jdouble jTime = privateRefs->rti->convertTime( theTime );

	logger->trace( "[Starting] timeAdvanceRequest(): time=%f", jTime );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->TIME_ADVANCE_REQUEST,
	                                  jTime );

	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] timeAdvanceRequest(): time=%f", jTime );
}

// 8.9
void RTI::RTIambassador::timeAdvanceRequestAvailable( const RTI::FedTime& theTime )
	throw( RTI::InvalidFederationTime,
	       RTI::FederationTimeAlreadyPassed,
	       RTI::TimeAdvanceAlreadyInProgress,
	       RTI::EnableTimeRegulationPending,
	       RTI::EnableTimeConstrainedPending,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// get java versions of the parameters
	jdouble jTime = privateRefs->rti->convertTime( theTime );

	logger->trace( "[Starting] timeAdvanceRequestAvailable(): time=%f", jTime );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->TIME_ADVANCE_REQUEST_AVAILABLE,
	                                  jTime );

	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] timeAdvanceRequestAvailable(): time=%f", jTime );
}

// 8.10
void RTI::RTIambassador::nextEventRequest( const RTI::FedTime& theTime )
	throw( RTI::InvalidFederationTime,
	       RTI::FederationTimeAlreadyPassed,
	       RTI::TimeAdvanceAlreadyInProgress,
	       RTI::EnableTimeRegulationPending,
	       RTI::EnableTimeConstrainedPending,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// get java versions of the parameters
	jdouble jTime = privateRefs->rti->convertTime( theTime );

	logger->trace( "[Starting] nextEventRequest(): time=%f", jTime );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->NEXT_EVENT_REQUEST,
	                                  jTime );

	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] nextEventRequest(): time=%f", jTime );
}

// 8.11
void RTI::RTIambassador::nextEventRequestAvailable( const RTI::FedTime& theTime )
	throw( RTI::InvalidFederationTime,
	       RTI::FederationTimeAlreadyPassed,
	       RTI::TimeAdvanceAlreadyInProgress,
	       RTI::EnableTimeRegulationPending,
	       RTI::EnableTimeConstrainedPending,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// get java versions of the parameters
	jdouble jTime = privateRefs->rti->convertTime( theTime );

	logger->trace( "[Starting] nextEventRequestAvailable(): time=%f", jTime );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->NEXT_EVENT_REQUEST_AVAILABLE,
	                                  jTime );

	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] nextEventRequestAvailable(): time=%f", jTime );
}

// 8.12
void RTI::RTIambassador::flushQueueRequest( const RTI::FedTime& theTime )
	throw( RTI::InvalidFederationTime,
	       RTI::FederationTimeAlreadyPassed,
	       RTI::TimeAdvanceAlreadyInProgress,
	       RTI::EnableTimeRegulationPending,
	       RTI::EnableTimeConstrainedPending,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// get java versions of the parameters
	jdouble jTime = privateRefs->rti->convertTime( theTime );

	logger->trace( "[Starting] flushQueueRequest(): time=%f", jTime );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->FLUSH_QUEUE_REQUEST,
	                                  jTime );

	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] flushQueueRequest(): time=%f", jTime );
}

// 8.14
void RTI::RTIambassador::enableAsynchronousDelivery()
	throw( RTI::AsynchronousDeliveryAlreadyEnabled,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] enableAsynchronousDelivery()" );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->ENABLE_ASYNCHRONOUS_DELIVERY );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] enableAsynchronousDelivery()" );
}

// 8.15
void RTI::RTIambassador::disableAsynchronousDelivery()
	throw( RTI::AsynchronousDeliveryAlreadyDisabled,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] disableAsynchronousDelivery()" );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->DISABLE_ASYNCHRONOUS_DELIVERY );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] disableAsynchronousDelivery()" );
}

// 8.16
void RTI::RTIambassador::queryLBTS( RTI::FedTime& theTime )
	throw( RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] queryLBTS()" );
	
	// call the method
	jdouble retval = privateRefs->env->CallDoubleMethod( privateRefs->rti->jproxy,
	                                                     privateRefs->rti->QUERY_LBTS );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	// store the result in the provided location
	privateRefs->rti->pushTime( retval, theTime );
	
	logger->trace( "[Finished] queryLBTS()" );
}

// 8.17
void RTI::RTIambassador::queryFederateTime( RTI::FedTime& theTime )
	throw( RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] queryFederateTime()" );
	
	// call the method
	jdouble retval = privateRefs->env->CallDoubleMethod( privateRefs->rti->jproxy,
	                                                     privateRefs->rti->QUERY_FEDERATE_TIME );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	// store the result in the provided location
	privateRefs->rti->pushTime( retval, theTime );
	
	logger->trace( "[Finished] queryFederateTime()" );
}

// 8.18
void RTI::RTIambassador::queryMinNextEventTime( RTI::FedTime& theTime )
	throw( RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] queryNextMinEventTime()" );

	// call the method
	jdouble retval =
		privateRefs->env->CallDoubleMethod( privateRefs->rti->jproxy,
	                                        privateRefs->rti->QUERY_MIN_NEXT_EVENT_TIME );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	// store the result in the provided location
	privateRefs->rti->pushTime( retval, theTime );

	logger->trace( "[Finished] queryNextMinEventTime()" );
}

// 8.19
void RTI::RTIambassador::modifyLookahead( const RTI::FedTime& theLookahead )
	throw( RTI::InvalidLookahead,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// get java versions of the parameters
	jdouble jTime = privateRefs->rti->convertTime( theLookahead );

	logger->trace( "[Starting] modifyLookahead(): lookahead=%f", jTime );

	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->MODIFY_LOOKAHEAD,
	                                  jTime );

	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] modifyLookahead(): lookahead=%f", jTime );
}

// 8.20
void RTI::RTIambassador::queryLookahead( RTI::FedTime& theTime )
	throw( RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] queryLookahead()" );

	// call the method
	jdouble retval = privateRefs->env->CallDoubleMethod( privateRefs->rti->jproxy,
	                                                     privateRefs->rti->QUERY_LOOKAHEAD );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	// store the result in the provided location
	privateRefs->rti->pushTime( retval, theTime );
	
	logger->trace( "[Finished] queryLookahead()" );
}

// 8.21
void RTI::RTIambassador::retract( RTI::EventRetractionHandle theHandle )
	throw( RTI::InvalidRetractionHandle,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] retract()" );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->RETRACT,
	                                  theHandle );

	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] retract()" );
}

// 8.23
void RTI::RTIambassador::changeAttributeOrderType( RTI::ObjectHandle theObject,
                                                   const RTI::AttributeHandleSet& theAttributes,
                                                   RTI::OrderingHandle theType )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotDefined,
	       RTI::AttributeNotOwned,
	       RTI::InvalidOrderingHandle,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] changeAttributeOrderType()" );

	// get java versions of the parameters
	jintArray jSyncSet = privateRefs->rti->convertAHS( theAttributes );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->CHANGE_ATTRIBUTE_ORDER_TYPE,
	                                  theObject,
	                                  jSyncSet,
	                                  theType );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jSyncSet );
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] changeAttributeOrderType()" );
}

// 8.24
void RTI::RTIambassador::changeInteractionOrderType( RTI::InteractionClassHandle theClass,
                                                     RTI::OrderingHandle theType )
	throw( RTI::InteractionClassNotDefined,
	       RTI::InteractionClassNotPublished,
	       RTI::InvalidOrderingHandle,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] changeInteractionOrderType(): class=%d, orderHandle=%d",
	               theClass, theType );

	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->CHANGE_INTERACTION_ORDER_TYPE,
	                                  theClass,
	                                  theType );

	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] changeInteractionOrderType(): class=%d, orderHandle=%d",
	               theClass, theType );
}

