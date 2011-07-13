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

extern portico13::Logger* logger;

/////////////////////////////////////
// Declaration Management Services //
/////////////////////////////////////
// 5.2
void RTI::RTIambassador::publishObjectClass( RTI::ObjectClassHandle theClass,
                                             const RTI::AttributeHandleSet& attributeList )
	throw( RTI::ObjectClassNotDefined,
	       RTI::AttributeNotDefined,
	       RTI::OwnershipAcquisitionPending,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		char* theAttributeList = setToString( attributeList );
		logger->trace( "[Starting] publishObjectClass(): classHandle=%d, attributes=%s",
		               theClass, theAttributeList );
		delete theAttributeList;
	}
	
	// get java versions of the parameters
	jintArray jAttSet = privateRefs->rti->convertAHS( attributeList );

	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->PUBLISH_OBJECT_CLASS,
	                                  theClass,
	                                  jAttSet );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jAttSet );
	privateRefs->rti->exceptionCheck();
	
	logger->debug( "[Finished] publishObjectClass(): objectClass=%d", theClass ); 
}

// 5.3
void RTI::RTIambassador::unpublishObjectClass( RTI::ObjectClassHandle theClass )
	throw( RTI::ObjectClassNotDefined,
	       RTI::ObjectClassNotPublished,
	       RTI::OwnershipAcquisitionPending,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] unpublishObjectClass(): classHandle=%d", theClass );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->UNPUBLISH_OBJECT_CLASS,
	                                  theClass );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->debug( "[Finished] unpublishObjectClass(): classHandle=%d", theClass );
}

// 5.4
void RTI::RTIambassador::publishInteractionClass( RTI::InteractionClassHandle theClass )
	throw( RTI::InteractionClassNotDefined,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] publishInteractionClass(): classHandle=%d", theClass );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->PUBLISH_INTERACTION_CLASS,
	                                  theClass );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->debug( "[Finished] publishInteractionClass(): classHandle=%d", theClass );
}

// 5.5
void RTI::RTIambassador::unpublishInteractionClass( RTI::InteractionClassHandle theClass )
	throw( RTI::InteractionClassNotDefined,
	       RTI::InteractionClassNotPublished,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] unpublishInteractionClass(): classHandle=%d", theClass );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->UNPUBLISH_INTERACTION_CLASS,
	                                  theClass );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();

	logger->debug( "[Finished] unpublishInteractionClass(): classHandle=%d", theClass );
}

// 5.6
void RTI::RTIambassador::subscribeObjectClassAttributes( RTI::ObjectClassHandle theClass,
                                                         const RTI::AttributeHandleSet& attributes,
                                                         RTI::Boolean active )
	throw( RTI::ObjectClassNotDefined,
	       RTI::AttributeNotDefined,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	/////////////////////
	// log the request //
	/////////////////////
	if( logger->isTraceEnabled() )
	{
		char* attributeList = setToString( attributes );
		if( active == RTI:: RTI_TRUE )
		{
			logger->trace( "[Starting] subscribeObjectClassAttributes(active): classHandle=%d, attributes=%s",
			               theClass, attributeList );
		}
		else
		{
			logger->trace( "[Starting] subscribeObjectClassAttributes(passive): classHandle=%d, attributes=%s",
			               theClass, attributeList );
		}
		delete attributeList;
	}
	
	/////////////////////////
	// do the subscription //
	/////////////////////////
	// get java versions of the parameters
	jintArray jAttSet = privateRefs->rti->convertAHS( attributes );

	if( active == RTI::RTI_TRUE )
	{
		// call the method
		privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
		                                  privateRefs->rti->SUBSCRIBE_OBJECT_CLASS_ATTRIBUTES_ACTIVELY,
		                                  theClass,
		                                  jAttSet );
	}
	else
	{
		// call the method
		privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
		                                  privateRefs->rti->SUBSCRIBE_OBJECT_CLASS_ATTRIBUTES_PASSIVELY,
		                                  theClass,
		                                  jAttSet );
	}

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jAttSet );
	privateRefs->rti->exceptionCheck();
	
	logger->debug( "[Finished] subscribeObjectClassAttributes(): classHandle=%d", theClass );
}

// 5.7
void RTI::RTIambassador::unsubscribeObjectClass( RTI::ObjectClassHandle theClass )
	throw( RTI::ObjectClassNotDefined,
	       RTI::ObjectClassNotSubscribed,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] unsubscribeObjectClass(): classHandle=%d", theClass );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->UNSUBSCRIBE_OBJECT_CLASS,
	                                  theClass );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();

	logger->debug( "[Finished] unsubscribeObjectClass(): classHandle=%d", theClass );
}

// 5.8
void RTI::RTIambassador::subscribeInteractionClass( RTI::InteractionClassHandle theClass,
                                                    RTI::Boolean active )
	throw( RTI::InteractionClassNotDefined,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::FederateLoggingServiceCalls,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	/////////////////////
	// log the request //
	/////////////////////
	if( logger->isTraceEnabled() )
	{
		if( active == RTI:: RTI_TRUE )
			logger->trace( "[Starting] subscribeInteractionClass(active): classHandle=%d", theClass );
		else
			logger->trace( "[Starting] subscribeInteractionClass(passive): classHandle=%d", theClass );
	}

	/////////////////////////
	// do the subscription //
	/////////////////////////
	if( active == RTI::RTI_TRUE )
	{
		// call the method
		privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
		                                  privateRefs->rti->SUBSCRIBE_INTERACTION_CLASS,
		                                  theClass );
	}
	else
	{
		// call the method
		privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
		                                  privateRefs->rti->SUBSCRIBE_INTERACTION_CLASS_PASSIVELY,
		                                  theClass );
	}

	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->debug( "[Finished] subscribeInteractionClass(): classHandle=%d", theClass );
}

// 5.9
void RTI::RTIambassador::unsubscribeInteractionClass( RTI::InteractionClassHandle theClass )
	throw( RTI::InteractionClassNotDefined,
	       RTI::InteractionClassNotSubscribed,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] unsubscribeInteractionClass(): classHandle=%d", theClass );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->UNSUBSCRIBE_INTERACTION_CLASS,
	                                  theClass );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();

	logger->debug( "[Finished] unsubscribeInteractionClass(): classHandle=%d", theClass );
}
