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
#include "jni/JavaRTI.h"
#include "utils/ExceptionHacks.h"
#include "utils/StringUtils.h"

PORTICO1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
JavaRTI::JavaRTI( JNIEnv* env, int id )
{
	this->id = id;

	// do some basic setup
	this->jnienv      = env;
	this->jproxyClass = NULL;
	this->jproxy      = NULL;

	// get a name for the logger
	stringstream ss;
	ss << "rti-" << id;
	this->logger = new Logger( ss.str() );

	// initialize the exception handling statics
	//this->eName;
	//this->eReason;

	// initialize the connection
	this->initialize();
	this->cacheMethodIds();
}

JavaRTI::~JavaRTI()
{
	// delete the global reference to the proxy
	if( this->jproxy != NULL )
	{
		jnienv->DeleteGlobalRef( jproxy );
		exceptionCheck();
	}

	// detach from the JVM
	// TODO bring this back in when I can figure out a way to do it that
	//      won't stuff up in situations where there are multiple instances
	//      attaching in a single thread
	//this->detachFromJVM();

	delete this->logger;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
/*
 * Each JavaRTI object is represented by a unique id value. This is so that we
 * have a unique reference that can be used in both the JVM and the C++ sides
 * of the bindings.
 */
int JavaRTI::getId()
{
	return this->id;
}

/*
 * This method will execute the JNI exception handling logic. It is responsible for
 * gathering exception information and then logging it and throwing the right exception.
 *
 * NOTE: Although it doesn't declare that it throws any sort of exception, be aware
 *       that it will!
 */
void JavaRTI::exceptionCheck()
{
	// check locally to see if there was an exception received
	if( this->eName.empty() )
		return;

	// log the exception information at the INFO level
	this->logger->info( "Exception received: %s", this->eName.c_str() );
	this->logger->info( "%s", this->eReason.c_str() );

	// clear the exception information now that it has been handled

	///////////////////////////////////////////////////////////////////
	// The checkAndThrow method will throw an exception if there is  //
	// one to be thrown. However, we have to clear the existing data //
	// because otherwise the next time we call exceptionCheck() the  //
	// old information will still be stored and it will look like we //
	// have an exception to deal with. Once we throw the exception,  //
	// we won't have a chance to clean up. So, we copy the name and  //
	// reason, clear out the existing values and then proceed.       //
	////////////////////////////////////////////////////////////////////
	string nameCopy( eName );
	string reasonCopy( eReason );
	this->eName.clear();
	this->eReason.clear();
	ExceptionHacks::checkAndThrow( nameCopy, reasonCopy );
}

/**
 * This method is invoked by the ExceptionManager (where the Exception Manager is
 * called through the JNI from active Java code). It is here to allow the Java code
 * to pass exception information across to the C++ side, where it is stored for later
 * processing. The name will be the Java name of the exception, with the reason
 * typically containing a strack trace pointing to the problem.
 *
 * NOTE: The exceptionCheck() method will clear the exception. If there is still a
 *       pending exception when a new one is being pushed, the information will be
 *       logged at the ERROR level.
 */
void JavaRTI::pushException( string exceptionName, string reason )
{
	// check to see if there is already a pending exception
	if( this->eName.empty() == false )
	{
		logger->warn( "WARNING: Exception pending (%s)!!", this->eName.c_str() );
	}

	// clear the current exception information
	this->eName.clear();
	this->eReason.clear();

	// store the new information
	this->eName = exceptionName;
	this->eReason = reason;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// VM Management Methods ///////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
/*
 * This method will pre-fetch all the necessary JNI ID's. It expects there to be
 * a valid JNIEnv in this->jnienv (so attachToJVM() should have been called first.
 *
 * This method will also create an instance of the java proxy class through which
 * RTIambassador calls will be routed.
 */
void JavaRTI::initialize() throw( RTIinternalError )
{
	//////////////////////////////////////////////////////////////
	// get the proxy class id data and create an instance of it //
	//////////////////////////////////////////////////////////////
	// find the ProxyRtiAmbassador class //
	this->jproxyClass = jnienv->FindClass( "org/portico/impl/cpp13/ProxyRtiAmbassador" );
	if( jproxyClass == NULL )
	{
		logger->fatal( "Can't locate: org.portico.impl.cpp13.ProxyRtiAmbassador" );
		exceptionCheck();
		throw RTIinternalError( L"Can't locate: org.portico.impl.cpp13.ProxyRtiAmbassador" );
	}

	// find the method id of the constructor //
	jmethodID constructor;
	constructor = jnienv->GetMethodID( jproxyClass, "<init>", "(I)V" );
	if( constructor == NULL )
	{
		logger->fatal( "Can't locate ProxyRtiAmbassador() constructor ID" );
		exceptionCheck();
		throw RTIinternalError( L"Can't locate ProxyRtiAmbassador() constructor ID" );
	}

	// create the instance of the ambassador //
	logger->debug( "Creating new instance of ProxyRtiAmbassador" );
	jobject localReference = jnienv->NewObject( jproxyClass, constructor, this->id );

	// check for an exception
	// we have to use the plain old JNI method of exception detection as to work, the
	// exception manager requires that the JavaRTI instance be in the Runtime's map of
	// instances, and that won't happen until after the constructor, which this method
	// is a part of (in terms of method flow)
	if( jnienv->ExceptionOccurred() )
	{
		jnienv->ExceptionDescribe();
		jnienv->ExceptionClear();
		throw RTIinternalError( L"Exception during ProxyRtiAmbassador() constructor" );
	}

	// turn the reference into something more persistent (stop it from being garbage collected)
	jproxy = jnienv->NewGlobalRef( localReference );
	if( jproxy == NULL )
	{
		logger->fatal( "Could not instantiate ProxyRtiAmbassador" );
		exceptionCheck();
	}

	// cache the jclass for byte[] //
	BYTE_ARRAY = jnienv->FindClass( "[B" );
	BYTE_ARRAY = (jclass)jnienv->NewGlobalRef( BYTE_ARRAY );

	logger->info( "Initialized new JavaRTI (rti-%d)", this->id );
}

/////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////// JNI Caching Methods ////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
/*
 * This will cache an individual method id. It will find the method of the given name
 * and signature and place it into the provided pointer. If there is a problem finding
 * the method, an exception is thrown.
 * 
 * This will look for ProxyRtiAmbassador methods. The overload takes a specific class
 */
void JavaRTI::cacheMethod( jmethodID *handle, string method, string signature )
	throw( RTIinternalError )
{
	cacheMethod( handle, jproxyClass, method, signature );
}

/*
 * The same as the cacheMethod(jmethodId,char*,char*) function, except that it also takes the
 * class on which the method should be located (where the other method will look for the id on
 * the ProxyRtiAmbassador class)
 */
void JavaRTI::cacheMethod( jmethodID *handle, jclass clazz, string method, string signature )
	throw( RTIinternalError )
{
	logger->noisy( "Caching %s [%s]", method.c_str(), signature.c_str() );

	// get the method and store it
	*handle = jnienv->GetMethodID( clazz, method.c_str(), signature.c_str() );
	if( *handle == NULL )
	{
		stringstream ss;
		ss << "(jni) Could not locate method " << method << "[" << signature << "]";
		string message = ss.str();
		logger->error( message );
		throw RTIinternalError( StringUtils::toWideString(message) );
	}
}

/*
 * This method will go through all of the methods that we will use and cache up their IDs.
 * These are needed when we actually call the methods, so rather than get them on the fly,
 * we cache up all the values before hand.
 */
void JavaRTI::cacheMethodIds() throw( RTIinternalError )
{
	logger->trace( "Caching RTIambassador method ids..." );
	
	// just going to dive out for now as I haven't put the java side together yet
	if( true )
		return;

	// federation management
	cacheMethod( &CREATE_FEDERATION, "createFederationExecution", "(Ljava/lang/String;Ljava/lang/String;)V" );
	cacheMethod( &DESTROY_FEDERATION, "destroyFederationExecution", "(Ljava/lang/String;)V" );
	cacheMethod( &JOIN_FEDERATION, "joinFederationExecution", "(Ljava/lang/String;Ljava/lang/String;)I" );
	cacheMethod( &RESIGN_FEDERATION, "resignFederationExecution", "(I)V" );
	cacheMethod( &REGISTER_FEDERATION_SYNCH, "registerFederationSynchronizationPoint", "(Ljava/lang/String;[B)V" );
	cacheMethod( &REGISTER_FEDERATION_SYNCH_FEDHANDLESET, "registerFederationSynchronizationPoint", "(Ljava/lang/String;[B[I)V" );
	cacheMethod( &SYNCH_POINT_ACHIEVED, "synchronizationPointAchieved", "(Ljava/lang/String;)V" );
	cacheMethod( &REQUEST_FEDERATION_SAVE_TIME, "requestFederationSave", "(Ljava/lang/String;D)V" );
	cacheMethod( &REQUEST_FEDERATION_SAVE, "requestFederationSave", "(Ljava/lang/String;)V" );
	cacheMethod( &FEDERATE_SAVE_BEGUN, "federateSaveBegun", "()V" );
	cacheMethod( &FEDERATE_SAVE_COMPLETE, "federateSaveComplete", "()V" );
	cacheMethod( &FEDERATE_SAVE_NOT_COMPLETE, "federateSaveNotComplete", "()V" );
	cacheMethod( &REQUEST_FEDERATION_RESTORE, "requestFederationRestore", "(Ljava/lang/String;)V" );
	cacheMethod( &FEDERATE_RESTORE_COMPLETE, "federateRestoreComplete", "()V" );
	cacheMethod( &FEDERATE_RESTORE_NOT_COMPLETE, "federateRestoreNotComplete", "()V" );

	// publication and subscription
	cacheMethod( &PUBLISH_OBJECT_CLASS, "publishObjectClass", "(I[I)V" );
	cacheMethod( &UNPUBLISH_OBJECT_CLASS, "unpublishObjectClass", "(I)V" );
	cacheMethod( &PUBLISH_INTERACTION_CLASS, "publishInteractionClass", "(I)V" );
	cacheMethod( &UNPUBLISH_INTERACTION_CLASS, "unpublishInteractionClass", "(I)V" );
	cacheMethod( &SUBSCRIBE_OBJECT_CLASS_ATTRIBUTES_ACTIVELY, "subscribeObjectClassAttributes", "(I[I)V" );
	cacheMethod( &SUBSCRIBE_OBJECT_CLASS_ATTRIBUTES_PASSIVELY, "subscribeObjectClassAttributesPassively", "(I[I)V" );
	cacheMethod( &UNSUBSCRIBE_OBJECT_CLASS, "unsubscribeObjectClass", "(I)V" );
	cacheMethod( &SUBSCRIBE_INTERACTION_CLASS, "subscribeInteractionClass", "(I)V" );
	cacheMethod( &SUBSCRIBE_INTERACTION_CLASS_PASSIVELY, "subscribeInteractionClassPassively", "(I)V" );
	cacheMethod( &UNSUBSCRIBE_INTERACTION_CLASS, "unsubscribeInteractionClass", "(I)V" );

	// object management
	cacheMethod( &REGISTER_OBJECT_INSTANCE, "registerObjectInstance", "(I)I" );
	cacheMethod( &REGISTER_OBJECT_INSTANCE_WITH_NAME, "registerObjectInstance", "(ILjava/lang/String;)I" );
	cacheMethod( &UPDATE_ATTRIBUTE_VALUES, "updateAttributeValues", "(I[I[[B[B)V" );
	cacheMethod( &UPDATE_ATTRIBUTE_VALUES_WITH_TIME, "updateAttributeValues", "(I[I[[B[BD)I" );
	cacheMethod( &SEND_INTERACTION, "sendInteraction", "(I[I[[B[B)V" );
	cacheMethod( &SEND_INTERACTION_WITH_TIME, "sendInteraction", "(I[I[[B[BD)I" );
	cacheMethod( &DELETE_OBJECT_INSTANCE, "deleteObjectInstance", "(I[B)V" );
	cacheMethod( &DELETE_OBJECT_INSTANCE_WITH_TIME, "deleteObjectInstance", "(I[BD)I" );
	cacheMethod( &LOCAL_DELETE_OBJECT_INSTANCE, "localDeleteObjectInstance", "(I)V" );
	cacheMethod( &CHANGE_ATTRIBUTE_TRANSPORTATION_TYPE, "changeAttributeTransportationType", "(I[II)V" );
	cacheMethod( &CHANGE_INTERACTION_TRANSPORTATION_TYPE, "changeInteractionTransportationType", "(II)V" );
	cacheMethod( &REQUEST_OBJECT_ATTRIBUTE_VALUE_UPDATE, "requestObjectAttributeValueUpdate", "(I[I)V" );
	cacheMethod( &REQUEST_CLASS_ATTRIBUTE_VALUE_UPDATE, "requestClassAttributeValueUpdate", "(I[I)V" );

	// ownership management
	cacheMethod( &UNCONDITIONAL_DIVEST, "unconditionalAttributeOwnershipDivestiture", "(I[I)V" );
	cacheMethod( &NEGOTIATED_DIVEST, "negotiatedAttributeOwnershipDivestiture", "(I[I[B)V" );
	cacheMethod( &ATTRIBUTE_ACQUISITION, "attributeOwnershipAcquisition", "(I[I[B)V" );
	cacheMethod( &ATTRIBUTE_ACQUISITION_AVAILABLE, "attributeOwnershipAcquisitionIfAvailable", "(I[I)V" );
	cacheMethod( &ATTRIBUTE_OWNERSHIP_RELEASE_RESPOSE, "attributeOwnershipReleaseResponse", "(I[I)[I" );
	cacheMethod( &CANCEL_NEGOTIATED_DIVEST, "cancelNegotiatedAttributeOwnershipDivestiture", "(I[I)V" );
	cacheMethod( &CANCEL_OWNERSHIP_ACQUISITION, "cancelAttributeOwnershipAcquisition", "(I[I)V" );
	cacheMethod( &QUERY_ATTRIBUTE_OWNERSHIP, "queryAttributeOwnership", "(II)V" );
	cacheMethod( &IS_ATTRIBUTE_OWNED_BY_FEDERATE, "isAttributeOwnedByFederate", "(II)Z" );

	// time management
	cacheMethod( &ENABLE_TIME_REGULATION, "enableTimeRegulation", "(DD)V" );
	cacheMethod( &DISABLE_TIME_REGULATION, "disableTimeRegulation", "()V" );
	cacheMethod( &ENABLE_TIME_CONSTRAINED, "enableTimeConstrained", "()V" );
	cacheMethod( &DISABLE_TIME_CONSTRAINED, "disableTimeConstrained", "()V" );
	cacheMethod( &TIME_ADVANCE_REQUEST, "timeAdvanceRequest", "(D)V" );
	cacheMethod( &TIME_ADVANCE_REQUEST_AVAILABLE, "timeAdvanceRequestAvailable", "(D)V" );
	cacheMethod( &NEXT_EVENT_REQUEST, "nextEventRequest", "(D)V" );
	cacheMethod( &NEXT_EVENT_REQUEST_AVAILABLE, "nextEventRequestAvailable", "(D)V" );
	cacheMethod( &FLUSH_QUEUE_REQUEST, "flushQueueRequest", "(D)V" );
	cacheMethod( &ENABLE_ASYNCHRONOUS_DELIVERY, "enableAsynchronousDelivery", "()V" );
	cacheMethod( &DISABLE_ASYNCHRONOUS_DELIVERY, "disableAsynchronousDelivery", "()V" );
	cacheMethod( &QUERY_LBTS, "queryLBTS", "()D" );
	cacheMethod( &QUERY_FEDERATE_TIME, "queryFederateTime", "()D" );
	cacheMethod( &QUERY_MIN_NEXT_EVENT_TIME, "queryMinNextEventTime", "()D" );
	cacheMethod( &MODIFY_LOOKAHEAD, "modifyLookahead", "(D)V" );
	cacheMethod( &QUERY_LOOKAHEAD, "queryLookahead", "()D" );
	cacheMethod( &RETRACT, "retract", "(I)V" );
	cacheMethod( &CHANGE_ATTRIBUTE_ORDER_TYPE, "changeAttributeOrderType", "(I[II)V" );
	cacheMethod( &CHANGE_INTERACTION_ORDER_TYPE, "changeInteractionOrderType", "(II)V" );

	// data distribution management
	cacheMethod( &GET_REGION, "getRegion", "(I)Lhla/rti/Region;" );
	cacheMethod( &GET_REGION_TOKEN, "getRegionToken", "(Lhla/rti/Region;)I" );
	cacheMethod( &CREATE_REGION, "createRegion", "(II)Lhla/rti/Region;" );
	cacheMethod( &NOTIFY_OF_REGION_MODIFICATION, "notifyOfRegionModification", "(Lhla/rti/Region;)V" );
	cacheMethod( &DELETE_REGION, "deleteRegion", "(Lhla/rti/Region;)V" );
	cacheMethod( &REGISTER_OBJECT_WITH_REGION, "registerObjectInstanceWithRegion", "(I[I[Lhla/rti/Region;)I" );
	cacheMethod( &REGISTER_OBJECT_WITH_NAME_AND_REGION, "registerObjectInstanceWithRegion", "(ILjava/lang/String;[I[Lhla/rti/Region;)I" );
	cacheMethod( &ASSOCIATE_REGION_FOR_UPDATES, "associateRegionForUpdates", "(Lhla/rti/Region;I[I)V" );
	cacheMethod( &UNASSOCIATE_REGION_FOR_UPDATES, "unassociateRegionForUpdates", "(Lhla/rti/Region;I)V" );
	cacheMethod( &SUBSCRIBE_ATTRIBUTES_WITH_REGION, "subscribeObjectClassAttributesWithRegion", "(ILhla/rti/Region;[I)V" );
	cacheMethod( &SUBSCRIBE_ATTRIBUTES_PASSIVELY_WITH_REGION, "subscribeObjectClassAttributesPassivelyWithRegion", "(ILhla/rti/Region;[I)V" );
	cacheMethod( &UNSUBSCRIBE_ATTRIBUTES_WITH_REGION, "unsubscribeObjectClassWithRegion", "(ILhla/rti/Region;)V" );
	cacheMethod( &SUBSCRIBE_INTERACTION_CLASS_WITH_REGION, "subscribeInteractionClassWithRegion", "(ILhla/rti/Region;)V" );
	cacheMethod( &SUBSCRIBE_INTERACTION_CLASS_PASSIVELY_WITH_REGION, "subscribeInteractionClassPassivelyWithRegion", "(ILhla/rti/Region;)V" );
	cacheMethod( &UNSUBSCRIBE_INTERACTION_CLASS_WITH_REGION, "unsubscribeInteractionClassWithRegion", "(ILhla/rti/Region;)V" );
	cacheMethod( &SEND_INTERACTION_WITH_REGION, "sendInteractionWithRegion", "(I[I[[B[BLhla/rti/Region;)V" );
	cacheMethod( &SEND_INTERACTION_WITH_TIME_AND_REGION, "sendInteractionWithRegion", "(I[I[[B[BLhla/rti/Region;D)I" );
	cacheMethod( &REQUEST_CLASS_ATTRIBUTE_VALUE_UPDATE_WITH_REGION, "requestClassAttributeValueUpdateWithRegion", "(I[ILhla/rti/Region;)V" );

	// support services
	cacheMethod( &GET_OBJECT_CLASS_HANDLE, "getObjectClassHandle", "(Ljava/lang/String;)I" );
	cacheMethod( &GET_OBJECT_CLASS_NAME, "getObjectClassName", "(I)Ljava/lang/String;" );
	cacheMethod( &GET_ATTRIBUTE_HANDLE, "getAttributeHandle", "(Ljava/lang/String;I)I" );
	cacheMethod( &GET_ATTRIBUTE_NAME, "getAttributeName", "(II)Ljava/lang/String;" );
	cacheMethod( &GET_INTERACTION_CLASS_HANDLE, "getInteractionClassHandle", "(Ljava/lang/String;)I" );
	cacheMethod( &GET_INTERACTION_CLASS_NAME, "getInteractionClassName", "(I)Ljava/lang/String;" );
	cacheMethod( &GET_PARAMETER_HANDLE, "getParameterHandle", "(Ljava/lang/String;I)I" );
	cacheMethod( &GET_PARAMETER_NAME, "getParameterName", "(II)Ljava/lang/String;" );
	cacheMethod( &GET_OBJECT_INSTANCE_HANDLE, "getObjectInstanceHandle", "(Ljava/lang/String;)I" );
	cacheMethod( &GET_OBJECT_INSTANCE_NAME, "getObjectInstanceName", "(I)Ljava/lang/String;" );
	cacheMethod( &GET_ROUTING_SPACE_HANDLE, "getRoutingSpaceHandle", "(Ljava/lang/String;)I" );
	cacheMethod( &GET_ROUTING_SPACE_NAME, "getRoutingSpaceName", "(I)Ljava/lang/String;" );
	cacheMethod( &GET_DIMENSION_HANDLE, "getDimensionHandle", "(Ljava/lang/String;I)I" );
	cacheMethod( &GET_DIMENSION_NAME, "getDimensionName", "(II)Ljava/lang/String;" );
	cacheMethod( &GET_ATTRIBUTE_ROUTING_SPACE_HANDLE, "getAttributeRoutingSpaceHandle", "(II)I" );
	cacheMethod( &GET_OBJECT_CLASS, "getObjectClass", "(I)I" );
	cacheMethod( &GET_INTERACTION_ROUTING_SPACE_HANDLE, "getInteractionRoutingSpaceHandle", "(I)I" );
	cacheMethod( &GET_TRANSPORTATION_HANDLE, "getTransportationHandle", "(Ljava/lang/String;)I" );
	cacheMethod( &GET_TRANSPORTATION_NAME, "getTransportationName", "(I)Ljava/lang/String;" );
	cacheMethod( &GET_ORDERING_HANDLE, "getOrderingHandle", "(Ljava/lang/String;)I" );
	cacheMethod( &GET_ORDERING_NAME, "getOrderingName", "(I)Ljava/lang/String;" );
	cacheMethod( &ENABLE_CLASS_RELEVANCE_ADVISORY_SWITCH, "enableClassRelevanceAdvisorySwitch", "()V" );
	cacheMethod( &DISABLE_CLASS_RELEVANCE_ADVISORY_SWITCH, "disableClassRelevanceAdvisorySwitch", "()V" );
	cacheMethod( &ENABLE_ATTRIBUTE_RELEVANCE_ADVISORY_SWITCH, "enableAttributeRelevanceAdvisorySwitch", "()V" );
	cacheMethod( &DISABLE_ATTRIBUTE_RELEVANCE_ADVISORY_SWITCH, "disableAttributeRelevanceAdvisorySwitch", "()V" );
	cacheMethod( &ENABLE_ATTRIBUTE_SCOPE_ADVISORY_SWITCH, "enableAttributeScopeAdvisorySwitch", "()V" );
	cacheMethod( &DISABLE_ATTRIBUTE_SCOPE_ADVISORY_SWITCH, "disableAttributeScopeAdvisorySwitch", "()V" );
	cacheMethod( &ENABLE_INTERACTION_RELEVANCE_ADVISORY_SWITCH, "enableInteractionRelevanceAdvisorySwitch", "()V" );
	cacheMethod( &DISABLE_INTERACTION_RELEVANCE_ADVISORY_SWITCH, "disableInteractionRelevanceAdvisorySwitch", "()V" );

	cacheMethod( &TICK, "tick", "()V" );
	cacheMethod( &TICK_WITH_TIME, "tick", "(DD)Z" );
	cacheMethod( &KILL, "kill", "()V" );

	logger->trace( "Cached RTIambassador method ids" );
	logger->trace( "Caching Region method ids" );

//	cacheMethod( &REGION_GET_NUMBER_OF_EXTENTS, jregionClass, "getNumberOfExtents", "()J" );
//	cacheMethod( &REGION_GET_RANGE_LOWER_BOUND, jregionClass, "getRangeLowerBound", "(II)J" );
//	cacheMethod( &REGION_GET_RANGE_UPPER_BOUND, jregionClass, "getRangeUpperBound", "(II)J" );
//	cacheMethod( &REGION_GET_SPACE_HANDLE, jregionClass, "getSpaceHandle", "()I" );
//	cacheMethod( &REGION_SET_RANGE_LOWER_BOUND, jregionClass, "setRangeLowerBound", "(IIJ)V" );
//	cacheMethod( &REGION_SET_RANGE_UPPER_BOUND, jregionClass, "setRangeUpperBound", "(IIJ)V" );
//	cacheMethod( &REGION_GET_RANGE_UPPER_BOUND_NOTIFICATION_LIMIT, jregionClass,
//	             "getRangeUpperBoundNotificationLimit", "(II)J" );
//	cacheMethod( &REGION_GET_RANGE_LOWER_BOUND_NOTIFICATION_LIMIT, jregionClass,
//	             "getRangeLowerBoundNotificationLimit", "(II)J" );

	logger->trace( "Cached Region method ids" );
}


//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

PORTICO1516E_NS_END
