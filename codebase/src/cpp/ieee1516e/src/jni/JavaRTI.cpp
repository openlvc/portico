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
JavaRTI::JavaRTI( Runtime* jniRuntime, int id )
{
	this->id = id;

	// do some basic setup
	this->jniRuntime  = jniRuntime;
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
		JNIEnv* jnienv = getJniEnvironment();
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
	this->eName.assign( exceptionName );
	this->eReason.assign( reason );
}

JNIEnv* JavaRTI::getJniEnvironment()
{
	return this->jniRuntime->attachToJVM();
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

	// Get active environment
	JNIEnv* jnienv = getJniEnvironment();

	this->jproxyClass = jnienv->FindClass( "org/portico/impl/cpp1516e/ProxyRtiAmbassador" );
	if( jproxyClass != NULL )
	{
		// mark the object as a global reference so that the GC doesn't suck it up on us
		this->jproxyClass = (jclass)jnienv->NewGlobalRef( this->jproxyClass );
	}
	if( jproxyClass == NULL )
	{
		logger->fatal( "Can't locate: org.portico.impl.cpp1516e.ProxyRtiAmbassador" );
		exceptionCheck();
		throw RTIinternalError( L"Can't locate: org.portico.impl.cpp1516e.ProxyRtiAmbassador" );
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
void JavaRTI::cacheMethod( JNIEnv *jnienv, jmethodID *handle, string method, string signature )
	throw( RTIinternalError )
{
	cacheMethod( jnienv, handle, jproxyClass, method, signature );
}

/*
 * The same as the cacheMethod(jmethodId,char*,char*) function, except that it also takes the
 * class on which the method should be located (where the other method will look for the id on
 * the ProxyRtiAmbassador class)
 */
void JavaRTI::cacheMethod( JNIEnv *jnienv, jmethodID *handle, jclass clazz, string method, string signature )
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
	
	JNIEnv* jnienv = getJniEnvironment();

	// federation management
	cacheMethod( jnienv, &CONNECT, "connect", "(Ljava/lang/String;)V" );
	cacheMethod( jnienv, &DISCONNECT, "disconnect", "()V" );
	cacheMethod( jnienv, &CREATE_FEDERATION, "createFederationExecution", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V" );
	cacheMethod( jnienv, &CREATE_FEDERATION_WITH_MODULES, "createFederationExecution", "(Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)V" );
	cacheMethod( jnienv, &CREATE_FEDERATION_WITH_MIM, "createFederationExecutionWithMIM", "(Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V" );
	cacheMethod( jnienv, &DESTROY_FEDERATION, "destroyFederationExecution", "(Ljava/lang/String;)V" );
	cacheMethod( jnienv, &LIST_FEDERATIONS, "listFederationExecutions", "()V" );
	cacheMethod( jnienv, &JOIN_FEDERATION, "joinFederationExecution", "(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)I" );
	cacheMethod( jnienv, &JOIN_FEDERATION_WITH_NAME, "joinFederationExecution", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)I" );
	cacheMethod( jnienv, &RESIGN_FEDERATION, "resignFederationExecution", "(Ljava/lang/String;)V" );
	
	cacheMethod( jnienv, &REGISTER_FEDERATION_SYNC, "registerFederationSynchronizationPoint", "(Ljava/lang/String;[B)V" );
	cacheMethod( jnienv, &REGISTER_FEDERATION_SYNC_FEDSET, "registerFederationSynchronizationPoint", "(Ljava/lang/String;[B[I)V" );
	cacheMethod( jnienv, &SYNC_POINT_ACHIEVED, "synchronizationPointAchieved", "(Ljava/lang/String;)V" );
	cacheMethod( jnienv, &SYNC_POINT_ACHIEVED_WITH_INDICATOR, "synchronizationPointAchieved", "(Ljava/lang/String;Z)V" );
	cacheMethod( jnienv, &REQUEST_SAVE, "requestFederationSave", "(Ljava/lang/String;)V" );
	cacheMethod( jnienv, &REQUEST_SAVE_TIME, "requestFederationSave", "(Ljava/lang/String;D)V" );
	cacheMethod( jnienv, &SAVE_BEGUN, "federateSaveBegun", "()V" );
	cacheMethod( jnienv, &SAVE_COMPLETE, "federateSaveComplete", "()V" );
	cacheMethod( jnienv, &SAVE_NOT_COMPLETE, "federateSaveNotComplete", "()V" );
	cacheMethod( jnienv, &SAVE_ABORT, "abortFederationSave", "()V" );
	cacheMethod( jnienv, &SAVE_QUERY, "queryFederationSaveStatus", "()V" );
	cacheMethod( jnienv, &REQUEST_RESTORE, "requestFederationRestore", "(Ljava/lang/String;)V" );
	cacheMethod( jnienv, &RESTORE_COMPLETE, "federateRestoreComplete", "()V" );
	cacheMethod( jnienv, &RESTORE_NOT_COMPLETE, "federateRestoreNotComplete", "()V" );
	cacheMethod( jnienv, &RESTORE_ABORT, "abortFederationRestore", "()V" );
	cacheMethod( jnienv, &RESTORE_QUERY, "queryFederationRestoreStatus", "()V" );

	// publication and subscription
	cacheMethod( jnienv, &PUBLISH_OBJECT_CLASS, "publishObjectClassAttributes", "(I[I)V" );
	cacheMethod( jnienv, &UNPUBLISH_OBJECT_CLASS, "unpublishObjectClass", "(I)V" );
	cacheMethod( jnienv, &UNPUBLISH_OBJECT_CLASS_WITH_ATTRIBUTES, "publishObjectClassAttributes", "(I[I)V" );
	cacheMethod( jnienv, &PUBLISH_INTERACTION_CLASS, "publishInteractionClass", "(I)V" );
	cacheMethod( jnienv, &UNPUBLISH_INTERACTION_CLASS, "unpublishInteractionClass", "(I)V" );
	cacheMethod( jnienv, &SUBSCRIBE_ATTRIBUTES, "subscribeObjectClassAttributes", "(I[ILjava/lang/String;)V" );
	cacheMethod( jnienv, &SUBSCRIBE_ATTRIBUTES_PASSIVE, "subscribeObjectClassAttributesPassively", "(I[ILjava/lang/String;)V" );
	cacheMethod( jnienv, &UNSUBSCRIBE_OBJECT_CLASS, "unsubscribeObjectClass", "(I)V" );
	cacheMethod( jnienv, &UNSUBSCRIBE_OBJECT_CLASS_WITH_ATTRIBUTES, "unsubscribeObjectClassAttributes", "(I[I)V" );
	cacheMethod( jnienv, &SUBSCRIBE_INTERACTION_CLASS, "subscribeInteractionClass", "(I)V" );
	cacheMethod( jnienv, &SUBSCRIBE_INTERACTION_CLASS_PASSIVE, "subscribeInteractionClassPassively", "(I)V" );
	cacheMethod( jnienv, &UNSUBSCRIBE_INTERACTION_CLASS, "unsubscribeInteractionClass", "(I)V" );

	// object management
	cacheMethod( jnienv, &RESERVE_NAME, "reserveObjectInstanceName", "(Ljava/lang/String;)V" );
	cacheMethod( jnienv, &RELEASE_NAME, "releaseObjectInstanceName", "(Ljava/lang/String;)V" );
	cacheMethod( jnienv, &RESERVE_MULTIPLE_NAMES, "reserveMultipleObjectInstanceName", "([Ljava/lang/String;)V" );
	cacheMethod( jnienv, &RELEASE_MULTIPLE_NAMES, "releaseMultipleObjectInstanceName", "([Ljava/lang/String;)V" );
	cacheMethod( jnienv, &REGISTER_OBJECT, "registerObjectInstance", "(I)I" );
	cacheMethod( jnienv, &REGISTER_OBJECT_WITH_NAME, "registerObjectInstance", "(ILjava/lang/String;)I" );
	cacheMethod( jnienv, &UPDATE_ATTRIBUTE_VALUES, "updateAttributeValues", "(I[I[[B[B)V" );
	cacheMethod( jnienv, &UPDATE_ATTRIBUTE_VALUES_WITH_TIME, "updateAttributeValues", "(I[I[[B[BD)I" );
	cacheMethod( jnienv, &SEND_INTERACTION, "sendInteraction", "(I[I[[B[B)V" );
	cacheMethod( jnienv, &SEND_INTERACTION_WITH_TIME, "sendInteraction", "(I[I[[B[BD)I" );
	cacheMethod( jnienv, &DELETE_OBJECT_INSTANCE, "deleteObjectInstance", "(I[B)V" );
	cacheMethod( jnienv, &DELETE_OBJECT_INSTANCE_WITH_TIME, "deleteObjectInstance", "(I[BD)I" );
	cacheMethod( jnienv, &LOCAL_DELETE_OBJECT_INSTANCE, "localDeleteObjectInstance", "(I)V" );
	cacheMethod( jnienv, &REQUEST_OBJECT_ATTRIBUTE_VALUE_UPDATE, "requestAttributeValueUpdate", "(I[I[B)V" );
	cacheMethod( jnienv, &REQUEST_CLASS_ATTRIBUTE_VALUE_UPDATE, "requestAttributeValueUpdate", "(I[I[B)V" );
	cacheMethod( jnienv, &REQUEST_ATTRIBUTE_TRANSPORT_CHANGE, "requestAttributeTransportationTypeChange", "(I[ILjava/lang/String;)V" );
	cacheMethod( jnienv, &QUERY_ATTRIBUTE_TRANSPORT_TYPE, "queryAttributeTransportationType", "(II)V" );
	cacheMethod( jnienv, &REQUEST_INTERACTION_TRANSPORT_CHANGE, "requestInteractionTransportationTypeChange", "(ILjava/lang/String;)V" );
	cacheMethod( jnienv, &QUERY_INTERACTION_TRANSPORT_TYPE, "queryInteractionTransportationType", "(II)V" );

	// ownership management
	cacheMethod( jnienv, &UNCONDITIONAL_DIVEST, "unconditionalAttributeOwnershipDivestiture", "(I[I)V" );
	cacheMethod( jnienv, &NEGOTIATED_DIVEST, "negotiatedAttributeOwnershipDivestiture", "(I[I[B)V" );
	cacheMethod( jnienv, &CONFIRM_DIVEST, "confirmDivestiture", "(I[I[B)V" );
	cacheMethod( jnienv, &ATTRIBUTE_ACQUISITION, "attributeOwnershipAcquisition", "(I[I[B)V" );
	cacheMethod( jnienv, &ATTRIBUTE_ACQUISITION_AVAILABLE, "attributeOwnershipAcquisitionIfAvailable", "(I[I)V" );
	cacheMethod( jnienv, &ATTRIBUTE_OWNERSHIP_RELEASE_DENIED, "attributeOwnershipReleaseDenied", "(I[I)V" );
	cacheMethod( jnienv, &ATTRIBUTE_DIVEST_IF_WANTED, "attributeOwnershipDivestitureIfWanted", "(I[I)[I" );
	cacheMethod( jnienv, &CANCEL_NEGOTIATED_DIVEST, "cancelNegotiatedAttributeOwnershipDivestiture", "(I[I)V" );
	cacheMethod( jnienv, &CANCEL_OWNERSHIP_ACQUISITION, "cancelAttributeOwnershipAcquisition", "(I[I)V" );
	cacheMethod( jnienv, &QUERY_ATTRIBUTE_OWNERSHIP, "queryAttributeOwnership", "(II)V" );
	cacheMethod( jnienv, &IS_ATTRIBUTE_OWNED_BY_FEDERATE, "isAttributeOwnedByFederate", "(II)Z" );

	// time management
	cacheMethod( jnienv, &ENABLE_TIME_REGULATION, "enableTimeRegulation", "(D)V" );
	cacheMethod( jnienv, &DISABLE_TIME_REGULATION, "disableTimeRegulation", "()V" );
	cacheMethod( jnienv, &ENABLE_TIME_CONSTRAINED, "enableTimeConstrained", "()V" );
	cacheMethod( jnienv, &DISABLE_TIME_CONSTRAINED, "disableTimeConstrained", "()V" );
	cacheMethod( jnienv, &TIME_ADVANCE_REQUEST, "timeAdvanceRequest", "(D)V" );
	cacheMethod( jnienv, &TIME_ADVANCE_REQUEST_AVAILABLE, "timeAdvanceRequestAvailable", "(D)V" );
	cacheMethod( jnienv, &NEXT_EVENT_REQUEST, "nextMessageRequest", "(D)V" );
	cacheMethod( jnienv, &NEXT_EVENT_REQUEST_AVAILABLE, "nextMessageRequestAvailable", "(D)V" );
	cacheMethod( jnienv, &FLUSH_QUEUE_REQUEST, "flushQueueRequest", "(D)V" );
	cacheMethod( jnienv, &ENABLE_ASYNCHRONOUS_DELIVERY, "enableAsynchronousDelivery", "()V" );
	cacheMethod( jnienv, &DISABLE_ASYNCHRONOUS_DELIVERY, "disableAsynchronousDelivery", "()V" );
	cacheMethod( jnienv, &QUERY_GALT, "queryGALT", "()D" );
	cacheMethod( jnienv, &QUERY_TIME, "queryLogicalTime", "()D" );
	cacheMethod( jnienv, &QUERY_LITS, "queryLITS", "()D" );
	cacheMethod( jnienv, &MODIFY_LOOKAHEAD, "modifyLookahead", "(D)V" );
	cacheMethod( jnienv, &QUERY_LOOKAHEAD, "queryLookahead", "()D" );
	cacheMethod( jnienv, &RETRACT, "retract", "(I)V" );
	cacheMethod( jnienv, &CHANGE_ATTRIBUTE_ORDER_TYPE, "changeAttributeOrderType", "(I[ILjava/lang/String;)V" );
	cacheMethod( jnienv, &CHANGE_INTERACTION_ORDER_TYPE, "changeInteractionOrderType", "(ILjava/lang/String;)V" );

	// data distribution management
	// Note supported as yet
	cacheMethod( jnienv, &CREATE_REGION, "createRegion", "([I)I" );
	cacheMethod( jnienv, &COMMIT_REGION_MODIFICATION, "commitRegionModifications", "([I)V" );
	cacheMethod( jnienv, &DELETE_REGION, "deleteRegion", "(I)V" );
	cacheMethod( jnienv, &REGISTER_OBJECT_WITH_REGION, "registerObjectInstanceWithRegions", "(I[I[I)I" );
	cacheMethod( jnienv, &REGISTER_OBJECT_WITH_NAME_AND_REGION, "registerObjectInstanceWithRegions", "(I[I[ILjava/lang/String;)I" );
	cacheMethod( jnienv, &ASSOCIATE_REGION_FOR_UPDATES, "associateRegionsForUpdates", "(I[I[I)V" );
	cacheMethod( jnienv, &UNASSOCIATE_REGION_FOR_UPDATES, "unassociateRegionsForUpdates", "(I[I[I)V" );
	cacheMethod( jnienv, &SUBSCRIBE_ATTRIBUTES_WITH_REGION, "subscribeObjectClassAttributesWithRegions", "(I[I[I)V" );
	cacheMethod( jnienv, &SUBSCRIBE_ATTRIBUTES_WITH_REGION_AND_RATE, "subscribeObjectClassAttributesWithRegions", "(I[I[ILjava/lang/String;)V" );
	cacheMethod( jnienv, &SUBSCRIBE_ATTRIBUTES_PASSIVELY_WITH_REGION, "subscribeObjectClassAttributesPassivelyWithRegions", "(I[I[I)V" );
	cacheMethod( jnienv, &SUBSCRIBE_ATTRIBUTES_PASSIVELY_WITH_REGION_AND_RATE, "subscribeObjectClassAttributesPassivelyWithRegions", "(I[I[ILjava/lang/String;)V" );
	cacheMethod( jnienv, &UNSUBSCRIBE_ATTRIBUTES_WITH_REGION, "unsubscribeObjectClassAttributesWithRegions", "(I[I[I)V" );
	cacheMethod( jnienv, &SUBSCRIBE_INTERACTION_CLASS_WITH_REGION, "subscribeInteractionClassWithRegions", "(I[I)V" );
	cacheMethod( jnienv, &SUBSCRIBE_INTERACTION_CLASS_PASSIVELY_WITH_REGION, "subscribeInteractionClassPassivelyWithRegions", "(I[I)V" );
	cacheMethod( jnienv, &UNSUBSCRIBE_INTERACTION_CLASS_WITH_REGION, "unsubscribeInteractionClassWithRegions", "(I[I)V" );
	cacheMethod( jnienv, &SEND_INTERACTION_WITH_REGION, "sendInteractionWithRegions", "(I[I[[B[B)V" );
	cacheMethod( jnienv, &SEND_INTERACTION_WITH_TIME_AND_REGION, "sendInteractionWithRegions", "(I[I[[B[I[BD)I" );
	cacheMethod( jnienv, &REQUEST_CLASS_ATTRIBUTE_VALUE_UPDATE_WITH_REGION, "requestAttributeValueUpdateWithRegions", "(I[I[I[B)V" );


	// support services
	cacheMethod( jnienv, &GET_AUTO_RESIGN_DIRECTIVE, "getAutomaticResignDirective", "()Ljava/lang/String;" );
	cacheMethod( jnienv, &SET_AUTO_RESIGN_DIRECTIVE, "setAutomaticResignDirective", "(Ljava/lang/String;)V" );
	cacheMethod( jnienv, &GET_FEDERATE_HANDLE, "getFederateHandle", "(Ljava/lang/String;)I" );
	cacheMethod( jnienv, &GET_FEDERATE_NAME, "getFederateName", "(I)Ljava/lang/String;" );
	cacheMethod( jnienv, &GET_OBJECT_CLASS_HANDLE, "getObjectClassHandle", "(Ljava/lang/String;)I" );
	cacheMethod( jnienv, &GET_OBJECT_CLASS_NAME, "getObjectClassName", "(I)Ljava/lang/String;" );
	cacheMethod( jnienv, &GET_KNOWN_OBJECT_CLASS_HANDLE, "getKnownObjectClassHandle", "(I)I" );
	cacheMethod( jnienv, &GET_OBJECT_INSTANCE_HANDLE, "getObjectInstanceHandle", "(Ljava/lang/String;)I" );
	cacheMethod( jnienv, &GET_OBJECT_INSTANCE_NAME, "getObjectInstanceName", "(I)Ljava/lang/String;" );
	cacheMethod( jnienv, &GET_ATTRIBUTE_HANDLE, "getAttributeHandle", "(ILjava/lang/String;)I" );
	cacheMethod( jnienv, &GET_ATTRIBUTE_NAME, "getAttributeName", "(II)Ljava/lang/String;" );
	cacheMethod( jnienv, &GET_UPDATE_RATE, "getUpdateRateValue", "(Ljava/lang/String;)D" );
	cacheMethod( jnienv, &GET_UPDATE_RATE_FOR_ATTRIBUTE, "getUpdateRateValueForAttribute", "(II)D" );
	
	cacheMethod( jnienv, &GET_INTERACTION_CLASS_HANDLE, "getInteractionClassHandle", "(Ljava/lang/String;)I" );
	cacheMethod( jnienv, &GET_INTERACTION_CLASS_NAME, "getInteractionClassName", "(I)Ljava/lang/String;" );
	cacheMethod( jnienv, &GET_PARAMETER_HANDLE, "getParameterHandle", "(ILjava/lang/String;)I" );
	cacheMethod( jnienv, &GET_PARAMETER_NAME, "getParameterName", "(II)Ljava/lang/String;" );

	cacheMethod( jnienv, &GET_DIMENSIONS_FOR_CLASS_ATTRIBUTE, "getAvailableDimensionsForClassAttribute", "(II)[I" );
	cacheMethod( jnienv, &GET_DIMENSIONS_FOR_INTERACTION_CLASS, "getAvailableDimensionsForInteractionClass", "(I)[I" );
	cacheMethod( jnienv, &GET_DIMENSION_HANDLE, "getDimensionHandle", "(Ljava/lang/String;)I" );
	cacheMethod( jnienv, &GET_DIMENSION_NAME, "getDimensionName", "(I)Ljava/lang/String;" );
	cacheMethod( jnienv, &GET_DIMENSION_UPPER, "getDimensionUpperBound", "(I)J" );
	cacheMethod( jnienv, &GET_DIMENSION_HANDLE_SET, "getDimensionHandleSet", "(I)[I" );
	cacheMethod( jnienv, &GET_RANGE_BOUNDS, "getRangeBounds", "(II)[J" );
	cacheMethod( jnienv, &SET_RANGE_BOUNDS, "setRangeBounds", "(IIJJ)V" );
	cacheMethod( jnienv, &ENABLE_CLASS_RELEVANCE_ADVISORY_SWITCH, "enableObjectClassRelevanceAdvisorySwitch", "()V" );
	cacheMethod( jnienv, &DISABLE_CLASS_RELEVANCE_ADVISORY_SWITCH, "disableObjectClassRelevanceAdvisorySwitch", "()V" );
	cacheMethod( jnienv, &ENABLE_ATTRIBUTE_RELEVANCE_ADVISORY_SWITCH, "enableAttributeRelevanceAdvisorySwitch", "()V" );
	cacheMethod( jnienv, &DISABLE_ATTRIBUTE_RELEVANCE_ADVISORY_SWITCH, "disableAttributeRelevanceAdvisorySwitch", "()V" );
	cacheMethod( jnienv, &ENABLE_ATTRIBUTE_SCOPE_ADVISORY_SWITCH, "enableAttributeScopeAdvisorySwitch", "()V" );
	cacheMethod( jnienv, &DISABLE_ATTRIBUTE_SCOPE_ADVISORY_SWITCH, "disableAttributeScopeAdvisorySwitch", "()V" );
	cacheMethod( jnienv, &ENABLE_INTERACTION_RELEVANCE_ADVISORY_SWITCH, "enableInteractionRelevanceAdvisorySwitch", "()V" );
	cacheMethod( jnienv, &DISABLE_INTERACTION_RELEVANCE_ADVISORY_SWITCH, "disableInteractionRelevanceAdvisorySwitch", "()V" );
	
	cacheMethod( jnienv, &EVOKE_CALLBACK, "evokeCallback", "(D)Z" );
	cacheMethod( jnienv, &EVOKE_MULTIPLE_CALLBACKS, "evokeMultipleCallbacks", "(DD)Z" );
	cacheMethod( jnienv, &ENABLE_CALLBACKS, "enableCallbacks", "()V" );
	cacheMethod( jnienv, &DISABLE_CALLBACKS, "disableCallbacks", "()V" );
	cacheMethod( jnienv, &GET_HLA_VERSION, "getHLAversion", "()Ljava/lang/String;" );
	//cacheMethod( jnienv, &KILL, "", "" );

	//custom portico
	cacheMethod(jnienv, &GET_ATTRIBUTE_DATATYPE, "getAttributeDatatype", "(II)Ljava/lang/String;");
	cacheMethod(jnienv, &GET_PARAMETER_DATATYPE, "getParameterDatatype", "(II)Ljava/lang/String;");
	cacheMethod(jnienv, &GET_FOM, "getFom", "()Ljava/lang/String;");


	logger->trace( "Cached RTIambassador method ids" );


	
//	logger->trace( "Caching Region method ids" );
//	cacheMethod( jnienv, &REGION_GET_NUMBER_OF_EXTENTS, jregionClass, "getNumberOfExtents", "()J" );
//	cacheMethod( jnienv, &REGION_GET_RANGE_LOWER_BOUND, jregionClass, "getRangeLowerBound", "(II)J" );
//	cacheMethod( jnienv, &REGION_GET_RANGE_UPPER_BOUND, jregionClass, "getRangeUpperBound", "(II)J" );
//	cacheMethod( jnienv, &REGION_GET_SPACE_HANDLE, jregionClass, "getSpaceHandle", "()I" );
//	cacheMethod( jnienv, &REGION_SET_RANGE_LOWER_BOUND, jregionClass, "setRangeLowerBound", "(IIJ)V" );
//	cacheMethod( jnienv, &REGION_SET_RANGE_UPPER_BOUND, jregionClass, "setRangeUpperBound", "(IIJ)V" );
//	cacheMethod( jnienv, &REGION_GET_RANGE_UPPER_BOUND_NOTIFICATION_LIMIT, jregionClass,
//	             "getRangeUpperBoundNotificationLimit", "(II)J" );
//	cacheMethod( jnienv, &REGION_GET_RANGE_LOWER_BOUND_NOTIFICATION_LIMIT, jregionClass,
//	             "getRangeLowerBoundNotificationLimit", "(II)J" );
//	logger->trace( "Cached Region method ids" );
}


//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

PORTICO1516E_NS_END
