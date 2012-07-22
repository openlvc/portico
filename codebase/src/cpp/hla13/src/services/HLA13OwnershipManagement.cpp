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

///////////////////////////////////
// Ownership Management Services //
///////////////////////////////////
// 7.2
void RTI::RTIambassador::unconditionalAttributeOwnershipDivestiture(
		RTI::ObjectHandle theObject,
		const RTI::AttributeHandleSet& theAttributes )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotDefined,
	       RTI::AttributeNotOwned,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// get java versions of the parameters
	jintArray jAttSet = privateRefs->rti->convertAHS( theAttributes );

	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->UNCONDITIONAL_DIVEST,
	                                  theObject,
	                                  jAttSet );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jAttSet );
	privateRefs->rti->exceptionCheck();
}

// 7.3
void RTI::RTIambassador::negotiatedAttributeOwnershipDivestiture(
		RTI::ObjectHandle theObject,
		const RTI::AttributeHandleSet& theAttributes,
		const char *theTag )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotDefined,
	       RTI::AttributeNotOwned,
	       RTI::AttributeAlreadyBeingDivested,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// get java versions of the parameters
	jintArray jAttSet = privateRefs->rti->convertAHS( theAttributes );
	jbyteArray jTag = privateRefs->rti->convertTag( theTag );

	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->NEGOTIATED_DIVEST,
	                                  theObject,
	                                  jAttSet,
	                                  jTag );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jAttSet );
	privateRefs->env->DeleteLocalRef( jTag );
	privateRefs->rti->exceptionCheck();
}

// 7.7
void
RTI::RTIambassador::attributeOwnershipAcquisition( RTI::ObjectHandle theObject,
                                                   const RTI::AttributeHandleSet& theAttributes,
                                                   const char *theTag )
	throw( RTI::ObjectNotKnown,
	       RTI::ObjectClassNotPublished,
	       RTI::AttributeNotDefined,
	       RTI::AttributeNotPublished,
	       RTI::FederateOwnsAttributes,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// get java versions of the parameters
	jintArray jAttSet = privateRefs->rti->convertAHS( theAttributes );
	jbyteArray jTag = privateRefs->rti->convertTag( theTag );

	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->ATTRIBUTE_ACQUISITION,
	                                  theObject,
	                                  jAttSet,
	                                  jTag );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jAttSet );
	privateRefs->env->DeleteLocalRef( jTag );
	privateRefs->rti->exceptionCheck();
}

// 7.8
void RTI::RTIambassador::attributeOwnershipAcquisitionIfAvailable(
		RTI::ObjectHandle theObject,
		const RTI::AttributeHandleSet& theAttributes )
	throw( RTI::ObjectNotKnown,
	       RTI::ObjectClassNotPublished,
	       RTI::AttributeNotDefined,
	       RTI::AttributeNotPublished,
	       RTI::FederateOwnsAttributes,
	       RTI::AttributeAlreadyBeingAcquired,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// get java versions of the parameters
	jintArray jAttSet = privateRefs->rti->convertAHS( theAttributes );

	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->ATTRIBUTE_ACQUISITION_AVAILABLE,
	                                  theObject,
	                                  jAttSet );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jAttSet );
	privateRefs->rti->exceptionCheck();
}

// 7.11
RTI::AttributeHandleSet* RTI::RTIambassador::attributeOwnershipReleaseResponse(
		RTI::ObjectHandle theObject,
		const RTI::AttributeHandleSet& theAttributes )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotDefined,
	       RTI::AttributeNotOwned,
	       RTI::FederateWasNotAskedToReleaseAttribute,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// get java versions of the parameters
	jintArray jAttSet = privateRefs->rti->convertAHS( theAttributes );

	// call the method
	jobject ret = 
		privateRefs->env->CallObjectMethod( privateRefs->rti->jproxy,
	                                        privateRefs->rti->ATTRIBUTE_OWNERSHIP_RELEASE_RESPOSE,
	                                        theObject,
	                                        jAttSet );
	
	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jAttSet );
	privateRefs->rti->exceptionCheck();
	
	return privateRefs->rti->convertToAHS( (jintArray)ret );
}

// 7.12
void RTI::RTIambassador::cancelNegotiatedAttributeOwnershipDivestiture(
		RTI::ObjectHandle theObject,
		const RTI::AttributeHandleSet& theAttributes )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotDefined,
	       RTI::AttributeNotOwned,
	       RTI::AttributeDivestitureWasNotRequested,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// get java versions of the parameters
	jintArray jAttSet = privateRefs->rti->convertAHS( theAttributes );

	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->CANCEL_NEGOTIATED_DIVEST,
	                                  theObject,
	                                  jAttSet );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jAttSet );
	privateRefs->rti->exceptionCheck();
}

// 7.13
void
RTI::RTIambassador::cancelAttributeOwnershipAcquisition( RTI::ObjectHandle theObject,
                                                         const RTI::AttributeHandleSet& attributes )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotDefined,
	       RTI::AttributeAlreadyOwned,
	       RTI::AttributeAcquisitionWasNotRequested,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// get java versions of the parameters
	jintArray jAttSet = privateRefs->rti->convertAHS( attributes );

	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->CANCEL_OWNERSHIP_ACQUISITION,
	                                  theObject,
	                                  jAttSet );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jAttSet );
	privateRefs->rti->exceptionCheck();
}

// 7.15
void RTI::RTIambassador::queryAttributeOwnership( RTI::ObjectHandle theObject,
                                                  RTI::AttributeHandle theAttribute )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotDefined,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->QUERY_ATTRIBUTE_OWNERSHIP,
	                                  theObject,
	                                  theAttribute );
	
	// run exception check
	privateRefs->rti->exceptionCheck();
}

// 7.17
RTI::Boolean RTI::RTIambassador::isAttributeOwnedByFederate( RTI::ObjectHandle theObject,
                                                             RTI::AttributeHandle theAttribute )
	throw( RTI::ObjectNotKnown,
	       RTI::AttributeNotDefined,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	// call the method
	jboolean retval =
		privateRefs->env->CallBooleanMethod( privateRefs->rti->jproxy,
	                                         privateRefs->rti->IS_ATTRIBUTE_OWNED_BY_FEDERATE,
	                                         theObject,
	                                         theAttribute );

	// run the exception check
	privateRefs->rti->exceptionCheck();

	if( retval == true )
		return RTI::RTI_TRUE;
	else
		return RTI::RTI_FALSE;
}

