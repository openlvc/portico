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

///////////////////////////////////
// Ownership Management Services //
///////////////////////////////////
// 7.2
void PorticoRtiAmbassador::unconditionalAttributeOwnershipDivestiture(
		ObjectInstanceHandle theObject,
		const AttributeHandleSet& attributes )
	throw( AttributeNotOwned,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] unconditionalAttributeOwnershipDivestiture(): object=%ls, attributes=%s",
		               theObject.toString().c_str(),
		               Logger::toString(attributes).c_str() );
	}

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	jintArray jattributes = JniUtils::fromSet( jnienv, attributes );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->UNCONDITIONAL_DIVEST,
	                        jobjectHandle,
	                        jattributes );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jattributes );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] unconditionalAttributeOwnershipDivestiture(): object=%d, attributes=%s",
		              jobjectHandle,
		              Logger::toString(attributes).c_str() );
	}
}

// 7.3
void PorticoRtiAmbassador::negotiatedAttributeOwnershipDivestiture(
		ObjectInstanceHandle theObject,
		const AttributeHandleSet& attributes,
		const VariableLengthData& tag )
	throw( AttributeAlreadyBeingDivested,
	       AttributeNotOwned,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] negotiatedAttributeOwnershipDivestiture(): object=%ls, attributes=%s",
		               theObject.toString().c_str(),
		               Logger::toString(attributes).c_str() );
	}

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	jintArray jattributes = JniUtils::fromSet( jnienv, attributes );
	jbyteArray jtag = JniUtils::fromTag( jnienv, tag );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->NEGOTIATED_DIVEST,
	                        jobjectHandle,
	                        jattributes,
	                        jtag );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jtag );
	jnienv->DeleteLocalRef( jattributes );
	javarti->exceptionCheck();

	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] negotiatedAttributeOwnershipDivestiture(): object=%d, attributes=%s",
		              jobjectHandle,
		              Logger::toString(attributes).c_str() );
	}
}

// 7.6
void PorticoRtiAmbassador::confirmDivestiture( ObjectInstanceHandle theObject,
                                               const AttributeHandleSet& attributes,
                                               const VariableLengthData& tag )
	throw( NoAcquisitionPending,
	       AttributeDivestitureWasNotRequested,
	       AttributeNotOwned,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] confirmDivestiture(): object=%ls, attributes=%s",
		               theObject.toString().c_str(),
		               Logger::toString(attributes).c_str() );
	}

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	jintArray jattributes = JniUtils::fromSet( jnienv, attributes );
	jbyteArray jtag = JniUtils::fromTag( jnienv, tag );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->CONFIRM_DIVEST,
	                        jobjectHandle,
	                        jattributes,
	                        jtag );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jtag );
	jnienv->DeleteLocalRef( jattributes );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] confirmDivestiture(): object=%d, attributes=%s",
		              jobjectHandle,
		              Logger::toString(attributes).c_str() );
	}
}

// 7.8
void PorticoRtiAmbassador::attributeOwnershipAcquisition( ObjectInstanceHandle theObject,
                                                          const AttributeHandleSet& desiredAttributes,
                                                          const VariableLengthData& tag )
	throw( AttributeNotPublished,
	       ObjectClassNotPublished,
	       FederateOwnsAttributes,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] attributeOwnershipAcquisition(): object=%ls, attributes=%s",
		               theObject.toString().c_str(),
		               Logger::toString(desiredAttributes).c_str() );
	}

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	jintArray jattributes = JniUtils::fromSet( jnienv, desiredAttributes );
	jbyteArray jtag = JniUtils::fromTag( jnienv, tag );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->ATTRIBUTE_ACQUISITION,
	                        jobjectHandle,
	                        jattributes,
	                        jtag );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jtag );
	jnienv->DeleteLocalRef( jattributes );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] attributeOwnershipAcquisition(): object=%d, attributes=%s",
		              jobjectHandle,
		              Logger::toString(desiredAttributes).c_str() );
	}
}

// 7.9
void PorticoRtiAmbassador::attributeOwnershipAcquisitionIfAvailable(
		ObjectInstanceHandle theObject,
		const AttributeHandleSet& desiredAttributes )
	throw( AttributeAlreadyBeingAcquired,
	       AttributeNotPublished,
	       ObjectClassNotPublished,
	       FederateOwnsAttributes,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] attributeOwnershipAcquisitionIfAvailable(): object=%ls, attributes=%s",
		               theObject.toString().c_str(),
		               Logger::toString(desiredAttributes).c_str() );
	}

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	jintArray jattributes = JniUtils::fromSet( jnienv, desiredAttributes );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->ATTRIBUTE_ACQUISITION_AVAILABLE,
	                        jobjectHandle,
	                        jattributes );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jattributes );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] attributeOwnershipAcquisitionIfAvailable(): object=%d, attributes=%s",
		              jobjectHandle,
		              Logger::toString(desiredAttributes).c_str() );
	}
}

// 7.12
void PorticoRtiAmbassador::attributeOwnershipReleaseDenied( ObjectInstanceHandle theObject,
                                                            const AttributeHandleSet& theAttributes )
	throw( AttributeNotOwned,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] attributeOwnershipReleaseDenied(): object=%ls, attributes=%s",
		               theObject.toString().c_str(),
		               Logger::toString(theAttributes).c_str() );
	}

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	jintArray jattributes = JniUtils::fromSet( jnienv, theAttributes );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->ATTRIBUTE_OWNERSHIP_RELEASE_DENIED,
	                        jobjectHandle,
	                        jattributes );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jattributes );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] attributeOwnershipReleaseDenied(): object=%d, attributes=%s",
		              jobjectHandle,
		              Logger::toString(theAttributes).c_str() );
	}
}

// 7.13
void PorticoRtiAmbassador::attributeOwnershipDivestitureIfWanted(
		ObjectInstanceHandle theObject,
		const AttributeHandleSet& theAttributes,
		AttributeHandleSet& theDivestedAttributes ) // filled by RTI
	throw( AttributeNotOwned,
		   AttributeNotDefined,
		   ObjectInstanceNotKnown,
		   SaveInProgress,
		   RestoreInProgress,
		   FederateNotExecutionMember,
		   NotConnected,
		   RTIinternalError )
{
	logger->error( "[Not Implemented] attributeOwnershipDivestitureIfWanted()" );
}

// 7.14
void PorticoRtiAmbassador::cancelNegotiatedAttributeOwnershipDivestiture(
		ObjectInstanceHandle theObject,
		const AttributeHandleSet& theAttributes )
	throw( AttributeDivestitureWasNotRequested,
	       AttributeNotOwned,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] cancelNegotiatedAttributeOwnershipDivestiture(): object=%ls, attributes=%s",
		               theObject.toString().c_str(),
		               Logger::toString(theAttributes).c_str() );
	}

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	jintArray jattributes = JniUtils::fromSet( jnienv, theAttributes );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->CANCEL_NEGOTIATED_DIVEST,
	                        jobjectHandle,
	                        jattributes );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jattributes );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] cancelNegotiatedAttributeOwnershipDivestiture(): object=%d, attributes=%s",
		              jobjectHandle,
		              Logger::toString(theAttributes).c_str() );
	}
}

// 7.15
void PorticoRtiAmbassador::cancelAttributeOwnershipAcquisition(
		ObjectInstanceHandle theObject,
		const AttributeHandleSet& theAttributes )
	throw( AttributeAcquisitionWasNotRequested,
	       AttributeAlreadyOwned,
	       AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] cancelAttributeOwnershipAcquisition(): object=%ls, attributes=%s",
		               theObject.toString().c_str(),
		               Logger::toString(theAttributes).c_str() );
	}

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	jintArray jattributes = JniUtils::fromSet( jnienv, theAttributes );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->CANCEL_OWNERSHIP_ACQUISITION,
	                        jobjectHandle,
	                        jattributes );

	// clean up and run the exception check
	jnienv->DeleteLocalRef( jattributes );
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] cancelAttributeOwnershipAcquisition(): object=%d, attributes=%s",
		              jobjectHandle,
		              Logger::toString(theAttributes).c_str() );
	}
}

// 7.17
void PorticoRtiAmbassador::queryAttributeOwnership( ObjectInstanceHandle theObject,
                                                    AttributeHandle theAttribute )
	throw( AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] queryAttributeOwnership(): object=%ls, attributes=%ls",
		               theObject.toString().c_str(),
		               theAttribute.toString().c_str() );
	}

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	jint jattribute = JniUtils::fromHandle( theAttribute );

	// call the method
	jnienv->CallVoidMethod( javarti->jproxy,
	                        javarti->QUERY_ATTRIBUTE_OWNERSHIP,
	                        jobjectHandle,
	                        jattribute );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] queryAttributeOwnership(): object=%d, attributes=%d",
		              jobjectHandle,
		              jattribute );
	}
}

// 7.19
bool PorticoRtiAmbassador::isAttributeOwnedByFederate( ObjectInstanceHandle theObject,
                                                       AttributeHandle theAttribute )
	throw( AttributeNotDefined,
	       ObjectInstanceNotKnown,
	       SaveInProgress,
	       RestoreInProgress,
	       FederateNotExecutionMember,
	       NotConnected,
	       RTIinternalError )
{
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Starting] isAttributeOwnedByFederate(): object=%ls, attributes=%ls",
		               theObject.toString().c_str(),
		               theAttribute.toString().c_str() );
	}

	// get java versions of the parameters
	jint jobjectHandle = JniUtils::fromHandle( theObject );
	jint jattribute = JniUtils::fromHandle( theAttribute );

	// call the method
	jboolean result = jnienv->CallBooleanMethod( javarti->jproxy,
	                                             javarti->IS_ATTRIBUTE_OWNED_BY_FEDERATE,
	                                             jobjectHandle,
	                                             jattribute );

	// clean up and run the exception check
	javarti->exceptionCheck();
	
	if( logger->isTraceEnabled() )
	{
		logger->trace( "[Finished] isAttributeOwnedByFederate(): object=%d, attributes=%d",
		              jobjectHandle,
		              jattribute );
	}

	return result;
}

PORTICO1516E_NS_END
