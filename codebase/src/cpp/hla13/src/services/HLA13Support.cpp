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
#include "jni/Runtime.h"

using namespace portico13;
extern Logger* logger;

//////////////////////////
// RTI Support Services //
//////////////////////////
// 10.2
RTI::ObjectClassHandle RTI::RTIambassador::getObjectClassHandle( const char *theName )
	throw( RTI::NameNotFound,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getObjectClassHandle(): className=%s", theName );

	// get java versions of the parameters
	jstring jname = privateRefs->env->NewStringUTF( theName );

	// call the method
	jint classHandle = privateRefs->env->CallIntMethod( privateRefs->rti->jproxy,
	                                                    privateRefs->rti->GET_OBJECT_CLASS_HANDLE,
	                                                    jname );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jname );
	privateRefs->rti->exceptionCheck();

	logger->trace( "[Finished] getObjectClassHandle(): className=%s (return: %d)",
	               theName, classHandle );

	// return the handle
	return classHandle;
}

// 10.3
char* RTI::RTIambassador::getObjectClassName( RTI::ObjectClassHandle theHandle )
	throw( RTI::ObjectClassNotDefined,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getObjectClassName(): classHandle=%d", theHandle );
	
	// call the method
	jstring name =
		(jstring)privateRefs->env->CallObjectMethod( privateRefs->rti->jproxy,
	                                                 privateRefs->rti->GET_OBJECT_CLASS_NAME,
	                                                 theHandle );
	
	// check up and run the exception check
	privateRefs->rti->exceptionCheck();
	
	char* oName = privateRefs->rti->convertAndReleaseJString( name );
	logger->trace( "[Finished] getObjectClassName(): classHandle=%d (return: %s)",
	               theHandle, oName );
	return oName;
}

// 10.4
RTI::AttributeHandle RTI::RTIambassador::getAttributeHandle( const char *theName,
                                                             RTI::ObjectClassHandle whichClass )
	throw( RTI::ObjectClassNotDefined,
	       RTI::NameNotFound,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getAttributeHandle(): attribute=%s, whichClass=%d",
	               theName, whichClass );
	
	// get java versions of the parameters
	jstring jname = privateRefs->env->NewStringUTF( theName );

	// call the method
	jint handle = privateRefs->env->CallIntMethod( privateRefs->rti->jproxy,
	                                               privateRefs->rti->GET_ATTRIBUTE_HANDLE,
	                                               jname,
	                                               whichClass );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jname );
	privateRefs->rti->exceptionCheck();

	logger->trace( "[Finished] getAttributeHandle(): attribute=%s, whichClass=%d (return: %d)",
	               theName, whichClass, handle );

	// return the handle
	return handle;
}

// 10.5
char* RTI::RTIambassador::getAttributeName( RTI::AttributeHandle theHandle,
                                            RTI::ObjectClassHandle whichClass )
	throw( RTI::ObjectClassNotDefined,
	       RTI::AttributeNotDefined,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getAttributeName(): attribute=%d, whichClass=%d",
	               theHandle, whichClass );
	
	// call the method
	jstring name =
		(jstring)privateRefs->env->CallObjectMethod( privateRefs->rti->jproxy,
	                                                 privateRefs->rti->GET_ATTRIBUTE_NAME,
	                                                 theHandle,
	                                                 whichClass );
	
	// check up and run the exception check
	privateRefs->rti->exceptionCheck();
	
	char* oName = privateRefs->rti->convertAndReleaseJString( name );
	logger->trace( "[Finished] getAttributeName(): attribute=%d, whichClass=%d (return: %s)",
	               theHandle, whichClass, oName );
	return oName;
}

// 10.6
RTI::InteractionClassHandle RTI::RTIambassador::getInteractionClassHandle( const char *theName )
	throw( RTI::NameNotFound,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getInteractionClassHandle(): className=%s", theName );
	
	// get java versions of the parameters
	jstring jname = privateRefs->env->NewStringUTF( theName );

	// call the method
	jint handle = privateRefs->env->CallIntMethod( privateRefs->rti->jproxy,
	                                               privateRefs->rti->GET_INTERACTION_CLASS_HANDLE,
	                                               jname );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jname );
	privateRefs->rti->exceptionCheck();

	logger->trace( "[Finished] getInteractionClassHandle(): className=%s (return: %d)",
	               theName, handle );
	
	// return the handle
	return handle;
}

// 10.7
char* RTI::RTIambassador::getInteractionClassName( RTI::InteractionClassHandle theHandle )
	throw( RTI::InteractionClassNotDefined,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getInteractionClassName(): handle=%d", theHandle );
	
	// call the method
	jstring name =
		(jstring)privateRefs->env->CallObjectMethod( privateRefs->rti->jproxy,
	                                                 privateRefs->rti->GET_INTERACTION_CLASS_NAME,
	                                                 theHandle );
	
	// check up and run the exception check
	privateRefs->rti->exceptionCheck();
	
	char* cName = privateRefs->rti->convertAndReleaseJString( name );
	logger->trace( "[Finished] getInteractionClassName(): handle=%d (return: %s)",
	               theHandle, cName );
	return cName;
}

// 10.8
RTI::ParameterHandle
RTI::RTIambassador::getParameterHandle( const char *theName,
                                        RTI::InteractionClassHandle whichClass )
	throw( RTI::InteractionClassNotDefined,
	       RTI::NameNotFound,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getParameterHandle(): parameterName=%s, whichClass=%d",\
	               theName, whichClass );
	
	// get java versions of the parameters
	jstring jname = privateRefs->env->NewStringUTF( theName );

	// call the method
	jint handle = privateRefs->env->CallIntMethod( privateRefs->rti->jproxy,
	                                               privateRefs->rti->GET_PARAMETER_HANDLE,
	                                               jname,
	                                               whichClass );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jname );
	privateRefs->rti->exceptionCheck();

	logger->trace( "[Finished] getParameterHandle(): parameterName=%s, whichClass=%d (return: %d)", 
	               theName, whichClass, handle );

	// return the handle
	return handle;
}

// 10.9
char* RTI::RTIambassador::getParameterName( RTI::ParameterHandle theHandle,
                                            RTI::InteractionClassHandle whichClass )
	throw( RTI::InteractionClassNotDefined,
	       RTI::InteractionParameterNotDefined,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getParameterName(): parameterHandle=%d, whichClass=%d",
	               theHandle, whichClass );
	
	// call the method
	jstring name =
		(jstring)privateRefs->env->CallObjectMethod( privateRefs->rti->jproxy,
	                                                 privateRefs->rti->GET_PARAMETER_NAME,
	                                                 theHandle,
	                                                 whichClass );
	
	// check up and run the exception check
	privateRefs->rti->exceptionCheck();

	char* pName = privateRefs->rti->convertAndReleaseJString( name );
	logger->trace( "[Finished] getParameterName(): parameterHandle=%d, whichClass=%d",
	               theHandle, whichClass, pName );
	return pName;
}

// 10.10
RTI::ObjectHandle RTI::RTIambassador::getObjectInstanceHandle( const char *theName )
	throw( RTI::ObjectNotKnown,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getObjectInstanceHandle(): objectName=%s", theName );
	
	// get java versions of the parameters
	jstring jname = privateRefs->env->NewStringUTF( theName );

	// call the method
	jint handle = privateRefs->env->CallIntMethod( privateRefs->rti->jproxy,
	                                               privateRefs->rti->GET_OBJECT_INSTANCE_HANDLE,
	                                               jname );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jname );
	privateRefs->rti->exceptionCheck();

	logger->trace( "[Finished] getObjectInstanceHandle(): objectName=%s (return: %d)",
	               theName, handle );

	// return the handle
	return handle;
}

// 10.11
char* RTI::RTIambassador::getObjectInstanceName( RTI::ObjectHandle theHandle )
	throw( RTI::ObjectNotKnown,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getObjectInstanceName(): objectHandle=%d", theHandle );

	// call the method
	jstring name =
		(jstring)privateRefs->env->CallObjectMethod( privateRefs->rti->jproxy,
	                                                 privateRefs->rti->GET_OBJECT_INSTANCE_NAME,
	                                                 theHandle );
	
	// check up and run the exception check
	privateRefs->rti->exceptionCheck();
	
	char* oName = privateRefs->rti->convertAndReleaseJString( name );
	logger->trace( "[Finished] getObjectInstanceName(): objectHandle=%d (return: %s)",
	               theHandle, oName );
	return oName;
}

// 10.12
RTI::SpaceHandle RTI::RTIambassador::getRoutingSpaceHandle( const char *theName )
	throw( RTI::NameNotFound,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getRoutingSpaceHandle(): spaceName=%s", theName );
	
	// get java versions of the parameters
	jstring jname = privateRefs->env->NewStringUTF( theName );

	// call the method
	jint handle = privateRefs->env->CallIntMethod( privateRefs->rti->jproxy,
	                                               privateRefs->rti->GET_ROUTING_SPACE_HANDLE,
	                                               jname );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jname );
	privateRefs->rti->exceptionCheck();

	logger->trace( "[Finished] getRoutingSpaceHandle(): spaceName=%s (return: %d)",\
	               theName, handle );

	// return the handle
	return handle;
}

// 10.13
char* RTI::RTIambassador::getRoutingSpaceName( RTI::SpaceHandle theHandle )
	throw( RTI::SpaceNotDefined,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getRoutingSpaceName(): spaceHandle=%d", theHandle );
	
	// call the method
	jstring name =
		(jstring)privateRefs->env->CallObjectMethod( privateRefs->rti->jproxy,
	                                                 privateRefs->rti->GET_ROUTING_SPACE_NAME,
	                                                 theHandle );
	
	// check up and run the exception check
	privateRefs->rti->exceptionCheck();
	
	char* sName = privateRefs->rti->convertAndReleaseJString( name );
	logger->trace( "[Finished] getRoutingSpaceName(): spaceHandle=%d (return: %s)",
	               theHandle, sName );
	return sName;
}

// 10.14
RTI::DimensionHandle RTI::RTIambassador::getDimensionHandle( const char *theName,
                                                             RTI::SpaceHandle whichSpace )
	throw( RTI::SpaceNotDefined,
	       RTI::NameNotFound,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getDimensionHandle(): dimensionName=%s, whichSpace=%d",
	               theName, whichSpace );

	// get java versions of the parameters
	jstring jname = privateRefs->env->NewStringUTF( theName );

	// call the method
	jint handle = privateRefs->env->CallIntMethod( privateRefs->rti->jproxy,
	                                               privateRefs->rti->GET_DIMENSION_HANDLE,
	                                               jname,
	                                               whichSpace );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jname );
	privateRefs->rti->exceptionCheck();

	logger->trace( "[Finished] getDimensionHandle(): dimensionName=%s, whichSpace=%d (return: %d)",
	               theName, whichSpace, handle );
	
	// return the handle
	return handle;
}

// 10.15
char* RTI::RTIambassador::getDimensionName( RTI::DimensionHandle theHandle,
                                            RTI::SpaceHandle whichSpace )
	throw( RTI::SpaceNotDefined,
	       RTI::DimensionNotDefined,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getDimensionName(): dimensionHandle=%d, whichSpace=%d",
	               theHandle, whichSpace );

	// call the method
	jstring name =
		(jstring)privateRefs->env->CallObjectMethod( privateRefs->rti->jproxy,
	                                                 privateRefs->rti->GET_DIMENSION_NAME,
	                                                 theHandle,
	                                                 whichSpace );
	
	// check up and run the exception check
	privateRefs->rti->exceptionCheck();
	
	char* dName = privateRefs->rti->convertAndReleaseJString( name );
	logger->trace( "[Finished] getDimensionName(): dimensionHandle=%d, whichSpace=%d (return: %s)",
	               theHandle, whichSpace, dName );
	return dName;
}

// 10.16
RTI::SpaceHandle
RTI::RTIambassador::getAttributeRoutingSpaceHandle( RTI::AttributeHandle theHandle,
                                                    RTI::ObjectClassHandle whichClass )
	throw( RTI::ObjectClassNotDefined,
	       RTI::AttributeNotDefined,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getAttributeRoutingSpaceHandle(): attributeHandle=%d, whichClass=%d",
	               theHandle, whichClass );
	
	// call the method
	jint handle = privateRefs->env->CallIntMethod( privateRefs->rti->jproxy,
	                                               privateRefs->rti->GET_ATTRIBUTE_ROUTING_SPACE_HANDLE,
	                                               theHandle,
	                                               whichClass );

	// clean up and run the exception check
	privateRefs->rti->exceptionCheck();

	logger->trace( "[Finished] getAttributeRoutingSpaceHandle(): attributeHandle=%d, whichClass=%d (return: %d)",
	               theHandle, whichClass, handle );
	
	// return the handle
	return handle;
}

// 10.17
RTI::ObjectClassHandle RTI::RTIambassador::getObjectClass( RTI::ObjectHandle theObject )
	throw( RTI::ObjectNotKnown,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getObjectClass(): objectHandle=%d", theObject );
	
	// call the method
	jint handle = privateRefs->env->CallIntMethod( privateRefs->rti->jproxy,
	                                               privateRefs->rti->GET_OBJECT_CLASS,
	                                               theObject );

	// clean up and run the exception check
	privateRefs->rti->exceptionCheck();

	logger->trace( "[Finished] getObjectClass(): objectHandle=%d (return: %d)", theObject, handle );

	// return the handle
	return handle;
}

// 10.18
RTI::SpaceHandle
RTI::RTIambassador::getInteractionRoutingSpaceHandle( RTI::InteractionClassHandle theHandle )
	throw( RTI::InteractionClassNotDefined,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getInteractionRoutingSpaceHandle(): classHandle=%d", theHandle );
	
	// call the method
	jint handle = privateRefs->env->CallIntMethod( privateRefs->rti->jproxy,
	                                               privateRefs->rti->GET_INTERACTION_ROUTING_SPACE_HANDLE,
	                                               theHandle );

	// clean up and run the exception check
	privateRefs->rti->exceptionCheck();

	logger->trace( "[Finished] getInteractionRoutingSpaceHandle(): classHandle=%d (return: %d)",
	               theHandle, handle );

	// return the handle
	return handle;
}

// 10.19
RTI::TransportationHandle RTI::RTIambassador::getTransportationHandle( const char *theName )
	throw( RTI::NameNotFound,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getTransportationHandle(): transportName=%s", theName );
	
	// get java versions of the parameters
	jstring jname = privateRefs->env->NewStringUTF( theName );

	// call the method
	jint handle = privateRefs->env->CallIntMethod( privateRefs->rti->jproxy,
	                                               privateRefs->rti->GET_TRANSPORTATION_HANDLE,
	                                               jname );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jname );
	privateRefs->rti->exceptionCheck();

	logger->trace( "[Finished] getTransportationHandle(): transportName=%s (return: %d)",
	               theName, handle );

	// return the handle
	return handle;
}

// 10.20
char* RTI::RTIambassador::getTransportationName( RTI::TransportationHandle theHandle )
	throw( RTI::InvalidTransportationHandle,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getTransportationName(): transportHandle=%d", theHandle );
	
	// call the method
	jstring name =
		(jstring)privateRefs->env->CallObjectMethod( privateRefs->rti->jproxy,
	                                                 privateRefs->rti->GET_TRANSPORTATION_NAME,
	                                                 theHandle );
	
	// check up and run the exception check
	privateRefs->rti->exceptionCheck();
	
	char* tName = privateRefs->rti->convertAndReleaseJString( name );
	logger->trace( "[Finished] getTransportationName(): transportHandle=%d (return: %s)",
	               theHandle, tName );
	return tName;
}

// 10.21
RTI::OrderingHandle RTI::RTIambassador::getOrderingHandle( const char *theName )
	throw( RTI::NameNotFound,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getOrderingHandle(): name=%s", theName );
	
	// get java versions of the parameters
	jstring jname = privateRefs->env->NewStringUTF( theName );

	// call the method
	jint handle = privateRefs->env->CallIntMethod( privateRefs->rti->jproxy,
	                                               privateRefs->rti->GET_ORDERING_HANDLE,
	                                               jname );

	// clean up and run the exception check
	privateRefs->env->DeleteLocalRef( jname );
	privateRefs->rti->exceptionCheck();

	logger->trace( "[Finished] getOrderingHandle(): name=%s (return: %d)", theName, handle );

	// return the handle
	return handle;
}

// 10.22
char* RTI::RTIambassador::getOrderingName( RTI::OrderingHandle theHandle )
	throw( RTI::InvalidOrderingHandle,
	       RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] getOrderingName(): orderHandle=%d", theHandle );
	
	// call the method
	jstring name =
		(jstring)privateRefs->env->CallObjectMethod( privateRefs->rti->jproxy,
	                                                 privateRefs->rti->GET_ORDERING_NAME,
	                                                 theHandle );
	
	// check up and run the exception check
	privateRefs->rti->exceptionCheck();
	
	char* oName = privateRefs->rti->convertAndReleaseJString( name );
	logger->trace( "[Finished] getOrderingName(): orderHandle=%d (return: %s)", theHandle, oName );
	return oName;
}

// 10.23
void RTI::RTIambassador::enableClassRelevanceAdvisorySwitch()
	throw( RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] enableClassRelevanceAdvisorySwitch()" );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->ENABLE_CLASS_RELEVANCE_ADVISORY_SWITCH );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] enableClassRelevanceAdvisorySwitch()" );
}

// 10.24
void RTI::RTIambassador::disableClassRelevanceAdvisorySwitch()
	throw( RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] disableClassRelevanceAdvisorySwitch()" );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->DISABLE_CLASS_RELEVANCE_ADVISORY_SWITCH );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();

	logger->trace( "[Finished] disableClassRelevanceAdvisorySwitch()" );
}

// 10.25
void RTI::RTIambassador::enableAttributeRelevanceAdvisorySwitch()
	throw( RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] enableAttributeRelevanceAdvisorySwitch()" );

	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->ENABLE_ATTRIBUTE_RELEVANCE_ADVISORY_SWITCH );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] enableAttributeRelevanceAdvisorySwitch()" );
}

// 10.26
void RTI::RTIambassador::disableAttributeRelevanceAdvisorySwitch()
	throw( RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] disableAttributeRelevanceAdvisorySwitch()" );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->DISABLE_ATTRIBUTE_RELEVANCE_ADVISORY_SWITCH );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] disableAttributeRelevanceAdvisorySwitch()" );
}

// 10.27
void RTI::RTIambassador::enableAttributeScopeAdvisorySwitch()
	throw( RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] enableAttributeScopeAdvisorySwitch()" );
	
	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->ENABLE_ATTRIBUTE_SCOPE_ADVISORY_SWITCH );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] enableAttributeScopeAdvisorySwitch()" );
}

// 10.28
void RTI::RTIambassador::disableAttributeScopeAdvisorySwitch()
	throw( RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] disableAttributeScopeAdvisorySwitch()" );

	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->DISABLE_ATTRIBUTE_SCOPE_ADVISORY_SWITCH );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] disableAttributeScopeAdvisorySwitch()" );
}

// 10.29
void RTI::RTIambassador::enableInteractionRelevanceAdvisorySwitch()
	throw( RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] enableInteractionRelevanceAdvisorySwitch()" );

	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->ENABLE_INTERACTION_RELEVANCE_ADVISORY_SWITCH );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] enableInteractionRelevanceAdvisorySwitch()" );
}

// 10.30
void RTI::RTIambassador::disableInteractionRelevanceAdvisorySwitch()
	throw( RTI::FederateNotExecutionMember,
	       RTI::ConcurrentAccessAttempted,
	       RTI::SaveInProgress,
	       RTI::RestoreInProgress,
	       RTI::RTIinternalError )
{
	logger->trace( "[Starting] disableInteractionRelevanceAdvisorySwitch()" );

	// call the method
	privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy,
	                                  privateRefs->rti->DISABLE_INTERACTION_RELEVANCE_ADVISORY_SWITCH );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();
	
	logger->trace( "[Finished] disableInteractionRelevanceAdvisorySwitch()" );
}

//
RTI::Boolean RTI::RTIambassador::tick()
	throw( RTI::SpecifiedSaveLabelDoesNotExist,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->noisy( "[Starting] tick()" );
	
	// fix: PORT-621: attach the current thread
	JNIEnv *env = 0;
	Runtime::getRuntime()->jvm->AttachCurrentThread( (void**)&env, NULL );
	if( env == 0 )
		throw new RTI::RTIinternalError( "couldn't attach to jvm in tick()" );
	
	// call the method
	//privateRefs->env->CallVoidMethod( privateRefs->rti->jproxy, privateRefs->rti->TICK );
	env->CallVoidMethod( privateRefs->rti->jproxy, privateRefs->rti->TICK );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();

	// java version returns void, just return true and hope for the best
	logger->noisy( "[Finished] tick()" );
	return RTI::RTI_TRUE;
}

RTI::Boolean RTI::RTIambassador::tick( RTI::TickTime min, RTI::TickTime max )
	throw( RTI::SpecifiedSaveLabelDoesNotExist,
	       RTI::ConcurrentAccessAttempted,
	       RTI::RTIinternalError )
{
	logger->noisy( "[Starting] tick(min,max)" );

	// fix: PORT-621: attach the current thread
	JNIEnv *env = 0;
	Runtime::getRuntime()->jvm->AttachCurrentThread( (void**)&env, NULL );
	if( env == 0 )
		throw new RTI::RTIinternalError( "couldn't attach to jvm in tick()" );

	// call the method
	jboolean result = env->CallBooleanMethod( privateRefs->rti->jproxy,
	                                          privateRefs->rti->TICK_WITH_TIME,
	                                          min,
	                                          max );
	
	// run the exception check
	privateRefs->rti->exceptionCheck();

	// like tick(), the java version returns void, just return true and hope for the best
	logger->noisy( "[Finished] tick(min,max)" );
	if( result == JNI_TRUE )
		return RTI::RTI_TRUE;
	else
		return RTI::RTI_FALSE;
}

