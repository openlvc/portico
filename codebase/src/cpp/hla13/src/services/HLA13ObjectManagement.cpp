/*
 *   Copyright 2009 The Portico Project
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

////////////////////////////////
// Object Management Services //
////////////////////////////////
// 6.2
RTI::ObjectHandle RTI::RTIambassador::registerObjectInstance( RTI::ObjectClassHandle theClass,
                                                              const char *theObject )
	throw( RTI::ObjectClassNotDefined,
	       RTI::ObjectClassNotPublished,
	       RTI::ObjectAlreadyRegistered,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] registerObjectInstance(): classHandle=%d, name=%s", theClass, theObject );
	
	// get java versions of the parameters
	jstring jObjectName = privateRefs->env->NewStringUTF( theObject );
	
	// call the method
	jint handle = privateRefs->env->CallIntMethod( privateRefs->rti->jproxy,
	                                               privateRefs->rti->REGISTER_OBJECT_INSTANCE_WITH_NAME,
	                                               theClass,
	                                               jObjectName );
	
	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jObjectName );
	privateRefs->rti->exceptionCheck();

	logger->debug( "[Finished] registerObjectInstance(): classHandle=%d, name=%s (return: %d)",
	               theClass, theObject, handle );

	// return the handle
	return handle;
}

RTI::ObjectHandle RTI::RTIambassador::registerObjectInstance( RTI::ObjectClassHandle theClass )
	throw( RTI::ObjectClassNotDefined,
	       RTI::ObjectClassNotPublished,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] registerObjectInstance(): classHandle=%d", theClass );

	// call the method
	jint handle = privateRefs->env->CallIntMethod( privateRefs->rti->jproxy,
	                                               privateRefs->rti->REGISTER_OBJECT_INSTANCE,
	                                               theClass );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();

	logger->debug( "[Finished] registerObjectInstance(): classHandle=%d (return: %d)",
	               theClass, handle );

	// return the handle
	return handle;
}

// 6.4
RTI::EventRetractionHandle
RTI::RTIambassador::updateAttributeValues( RTI::ObjectHandle theObject,
                                           const RTI::AttributeHandleValuePairSet& theAttributes,
                                           const RTI::FedTime& theTime,
                                           const char *theTag )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotDefined,
	       RTI::AttributeNotOwned,
	       RTI::InvalidFederationTime,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// get java versions of the parameters
	jbyteArray jTag = privateRefs->rti->convertTag( theTag );
	jdouble jTime = privateRefs->rti->convertTime( theTime );
	HVPS values = privateRefs->rti->convertAHVPS( theAttributes );

	// log the request
	if( logger->isTraceEnabled() )
	{
		char* attributeString = mapToString( theAttributes );
		logger->trace( "[Starting] updateAttributeValues(TSO): objectHandle=%d, attributes=%s, time=%f",
		               theObject, attributeString, jTime );
		delete attributeString;
	}
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->UPDATE_ATTRIBUTE_VALUES_WITH_TIME,
	                                  theObject,
	                                  values.handles,
	                                  values.values,
	                                  jTag,
	                                  jTime );
	
	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jTag );
	privateRefs->env->DeleteLocalRef( values.handles );
	privateRefs->env->DeleteLocalRef( values.values );
	privateRefs->rti->exceptionCheck();

	logger->debug( "[Finished] updateAttributeValues(TSO): objectHandle=%d", theObject );

	// return an empty retraction handle
	return RTI::EventRetractionHandle();
}

void RTI::RTIambassador::updateAttributeValues( RTI::ObjectHandle theObject,
                                                const RTI::AttributeHandleValuePairSet& attributes,
                                                const char *theTag )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotDefined,
	       RTI::AttributeNotOwned,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		char* attributeString = mapToString( attributes );
		logger->trace( "[Starting] updateAttributeValues(RO): objectHandle=%d, attributes=%s",
		               theObject, attributeString );
		delete attributeString;
	}

	// get java versions of the parameters
	jbyteArray jTag = privateRefs->rti->convertTag( theTag );
	HVPS values = privateRefs->rti->convertAHVPS( attributes );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->UPDATE_ATTRIBUTE_VALUES,
	                                  theObject,
	                                  values.handles,
	                                  values.values,
	                                  jTag );
	
	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jTag );
	privateRefs->env->DeleteLocalRef( values.handles );
	privateRefs->env->DeleteLocalRef( values.values );
	privateRefs->rti->exceptionCheck();
	
	logger->debug( "[Finished] updateAttributeValues(RO): objectHandle=%d", theObject );
}

// 6.6
RTI::EventRetractionHandle
RTI::RTIambassador::sendInteraction( RTI::InteractionClassHandle theInteraction,
                                     const RTI::ParameterHandleValuePairSet& theParameters,
                                     const RTI::FedTime& theTime,
                                     const char *theTag )
	throw( RTI::InteractionClassNotDefined,
	       RTI::InteractionClassNotPublished,
	       RTI::InteractionParameterNotDefined,
	       RTI::InvalidFederationTime,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// get java versions of the parameters
	jbyteArray jTag = privateRefs->rti->convertTag( theTag );
	jdouble jTime = privateRefs->rti->convertTime( theTime );
	HVPS values = privateRefs->rti->convertPHVPS( theParameters );
	
	// log the request
	if( logger->isTraceEnabled() )
	{
		char* parameterString = mapToString( theParameters );
		logger->trace( "[Starting] sendInteraction(TSO): classHandle=%d, parameters=%s, time=%f",
		               theInteraction, parameterString, jTime );
		delete parameterString;
	}
		
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->SEND_INTERACTION_WITH_TIME,
	                                  theInteraction,
	                                  values.handles,
	                                  values.values,
	                                  jTag,
	                                  jTime );
	
	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jTag );
	privateRefs->env->DeleteLocalRef( values.handles );
	privateRefs->env->DeleteLocalRef( values.values );
	privateRefs->rti->exceptionCheck();

	logger->debug( "[Finished] sendInteraction(TSO): classHandle=%d", theInteraction );

	// return an empty retraction handle
	return RTI::EventRetractionHandle();
}

void RTI::RTIambassador::sendInteraction( RTI::InteractionClassHandle theInteraction,
                                          const RTI::ParameterHandleValuePairSet& theParameters,
                                          const char *theTag )
	throw( RTI::InteractionClassNotDefined,
	       RTI::InteractionClassNotPublished,
	       RTI::InteractionParameterNotDefined,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		char* parameterString = mapToString( theParameters );
		logger->trace( "[Starting] sendInteraction(RO): classHandle=%d, parameters=%s",
		               theInteraction, parameterString );
		delete parameterString;
	}

	// get java versions of the parameters
	jbyteArray jTag = privateRefs->rti->convertTag( theTag );
	HVPS values = privateRefs->rti->convertPHVPS( theParameters );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->SEND_INTERACTION,
	                                  theInteraction,
	                                  values.handles,
	                                  values.values,
	                                  jTag );
	
	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jTag );
	privateRefs->env->DeleteLocalRef( values.handles );
	privateRefs->env->DeleteLocalRef( values.values );
	privateRefs->rti->exceptionCheck();
	
	logger->debug( "[Finished] sendInteraction(RO): classHandle=%d", theInteraction );
}

// 6.8
RTI::EventRetractionHandle
RTI::RTIambassador::deleteObjectInstance( RTI::ObjectHandle theObject,
                                          const RTI::FedTime& theTime,
                                          const char *theTag )
	throw( RTI::ObjectNotKnown,
	       RTI::DeletePrivilegeNotHeld,
	       RTI::InvalidFederationTime,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// get java versions of the parameters
	jbyteArray jTag = privateRefs->rti->convertTag( theTag );
	jdouble jTime   = privateRefs->rti->convertTime( theTime );

	// log the request
	logger->trace( "[Starting] deleteObjectInstance(TSO): objectHandle=%d, time=%f",
	               theObject, jTime );
	
	// call the method
	//int handle = 
		privateRefs->env->CallIntMethod( privateRefs->rti->jproxy,
		                                 privateRefs->rti->DELETE_OBJECT_INSTANCE_WITH_TIME,
		                                 theObject,
		                                 jTag,
		                                 jTime );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jTag );
	privateRefs->rti->exceptionCheck();
	
	logger->debug( "[Finished] deleteObjectInstance(TSO): objectHandle=%d", theObject );

	// return the handle
	return RTI::EventRetractionHandle();
	//return handle;
}

void RTI::RTIambassador::deleteObjectInstance( RTI::ObjectHandle theObject, const char *theTag )
	throw( RTI::ObjectNotKnown,
	       RTI::DeletePrivilegeNotHeld,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] deleteObjectInstance(RO): objectHandle=%d" );
	
	// get java versions of the parameters
	jbyteArray jTag = privateRefs->rti->convertTag( theTag );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->DELETE_OBJECT_INSTANCE,
	                                  theObject,
	                                  jTag );
	
	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jTag );
	privateRefs->rti->exceptionCheck();

	logger->debug( "[Finished] deleteObjectInstance(RO): objectHandle=%d" );
}

// 6.10
void RTI::RTIambassador::localDeleteObjectInstance( RTI::ObjectHandle theObject )
	throw( RTI::ObjectNotKnown,
	       RTI::FederateOwnsAttributes,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] localDeleteObjectInstance(): objectHandle=%d", theObject );
	
	// call the method
	privateRefs->env->CallIntMethod( privateRefs->rti->jproxy,
	                                 privateRefs->rti->LOCAL_DELETE_OBJECT_INSTANCE,
	                                 theObject );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();

	logger->debug( "[Finished] localDeleteObjectInstance(): objectHandle=%d", theObject );
}

// 6.11
void
RTI::RTIambassador::changeAttributeTransportationType( RTI::ObjectHandle theObject,
                                                       const RTI::AttributeHandleSet& attributes,
                                                       RTI::TransportationHandle theType )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotDefined,
	       RTI::AttributeNotOwned,
	       RTI::InvalidTransportationHandle,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		char* attributeString = setToString( attributes );
		logger->trace( "[Starting] changeAttributeTransportationType(): objectHandle=%d, attributes=%s, transportHandle=%d",
		               theObject, attributeString, theType );
		delete attributeString;
	}
	
	// get java versions of the parameters
	jintArray jAttributes = privateRefs->rti->convertAHS( attributes );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->CHANGE_ATTRIBUTE_TRANSPORTATION_TYPE,
	                                  theObject,
	                                  jAttributes,
	                                  theType );
	
	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jAttributes );
	privateRefs->rti->exceptionCheck();
	
	logger->debug( "[Finished] changeAttributeTransportationType(): objectHandle=%d, transportHandle=%d",
	               theObject, theType );
}

// 6.12
void RTI::RTIambassador::changeInteractionTransportationType( RTI::InteractionClassHandle theClass,
                                                              RTI::TransportationHandle theType )
	throw( RTI::InteractionClassNotDefined,
	       RTI::InteractionClassNotPublished,
	       RTI::InvalidTransportationHandle,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] changeInteractionTransportationType(): classHandle=%d, transportHandle=%d",
	               theClass, theType );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->CHANGE_INTERACTION_TRANSPORTATION_TYPE,
	                                  theClass,
	                                  theType );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();

	logger->debug( "[Finished] changeInteractionTransportationType(): classHandle=%d, transportHandle=%d",
	               theClass, theType );
}

// 6.15
void
RTI::RTIambassador::requestObjectAttributeValueUpdate( RTI::ObjectHandle theObject,
                                                       const RTI::AttributeHandleSet& attributes )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotDefined,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		char* attributeString = setToString( attributes );
		logger->trace( "[Starting] requestObjectAttributeValueUpdate(): objectHandle=%d, attributes=%s",
		               theObject, attributeString );
		delete attributeString;
	}
	
	// get java versions of the parameters
	jintArray jAttributes = privateRefs->rti->convertAHS( attributes );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->REQUEST_OBJECT_ATTRIBUTE_VALUE_UPDATE,
	                                  theObject,
	                                  jAttributes );
	
	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jAttributes );
	privateRefs->rti->exceptionCheck();
	
	logger->debug( "[Finished] requestObjectAttributeValueUpdate(): objectHandle=%d", theObject );
}

void
RTI::RTIambassador::requestClassAttributeValueUpdate( RTI::ObjectClassHandle theClass,
                                                      const RTI::AttributeHandleSet& attributes )
	throw( RTI::ObjectClassNotDefined,
	       RTI::AttributeNotDefined,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		char* attributeString = setToString( attributes );
		logger->trace( "[Starting] requestClassAttributeValueUpdate(): classHandle=%d, attributes=%s",
		               theClass, attributeString );
		delete attributeString;
	}
	
	// get java versions of the parameters
	jintArray jAttributes = privateRefs->rti->convertAHS( attributes );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->REQUEST_CLASS_ATTRIBUTE_VALUE_UPDATE,
	                                  theClass,
	                                  jAttributes );
	
	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jAttributes );
	privateRefs->rti->exceptionCheck();

	logger->debug( "[Finished] requestClassAttributeValueUpdate(): classHandle=%d", theClass );
}
