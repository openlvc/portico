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
#ifndef JAVARTI_H_
#define JAVARTI_H_

#include "common.h"
#include "utils/Logger.h"
#include "Runtime.h"

PORTICO1516E_NS_START

// forward declaration of Runtime to resolve circular-dependency
class Runtime;

/*
 * Where the Runtime class is responsible for managing the lifecycle of the JVM
 * and ensuring that it is instantiated and cleaned up properly for all of the
 * RTIambassadors that are created within an execution, this class is responsible
 * for managing an individual instantiation and connection for a particular
 * RTIambassador.
 */
class JavaRTI
{
	// give the Runtime access to our bits - this is so we can
	// ensure that it is the only class to ever create one of us
	friend class Runtime;

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	public:
		Logger* logger;
		
		int      id; // the unique ID for the RTI within the process
		Runtime* jniRuntime;
		jclass   jproxyClass;
		jobject  jproxy;
		
		// federate ambassador to contact back for callbacks
		FederateAmbassador *fedamb;

	private:
		// exception information
		string eName;
		string eReason;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private:
		// constructor is private but Runtime is a friend
		JavaRTI( Runtime* jniRuntime, int id );
		~JavaRTI();

	public:

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		int getId();
		void exceptionCheck();
		void pushException( string exceptionName, string reason );
		
		JNIEnv* getJniEnvironment();

	private:
		void initialize() throw( RTIinternalError );

		// JNI method caching
		void cacheMethodIds() throw( RTIinternalError );
		void cacheMethod( JNIEnv* env, jmethodID *handle, string method, string signature )
			throw( RTIinternalError );
		void cacheMethod( JNIEnv* env, jmethodID *handle, jclass clazz, string method, string signature )
			throw( RTIinternalError );

		
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	private:

	//----------------------------------------------------------
	// Method ID Cache Variables
	//----------------------------------------------------------
	public: // public for easy accessibility
		// federation management
		jmethodID CONNECT;
		jmethodID DISCONNECT;
		jmethodID CREATE_FEDERATION;
		jmethodID CREATE_FEDERATION_WITH_MODULES;
		jmethodID CREATE_FEDERATION_WITH_MIM;
		jmethodID DESTROY_FEDERATION;
		jmethodID LIST_FEDERATIONS;
		jmethodID JOIN_FEDERATION;
		jmethodID JOIN_FEDERATION_WITH_NAME;
		jmethodID RESIGN_FEDERATION;
		
		jmethodID REGISTER_FEDERATION_SYNC;
		jmethodID REGISTER_FEDERATION_SYNC_FEDSET;
		jmethodID SYNC_POINT_ACHIEVED;
		jmethodID SYNC_POINT_ACHIEVED_WITH_INDICATOR;
		jmethodID REQUEST_SAVE;
		jmethodID REQUEST_SAVE_TIME;
		jmethodID SAVE_BEGUN;
		jmethodID SAVE_COMPLETE;
		jmethodID SAVE_NOT_COMPLETE;
		jmethodID SAVE_ABORT;
		jmethodID SAVE_QUERY;
		jmethodID REQUEST_RESTORE;
		jmethodID RESTORE_COMPLETE;
		jmethodID RESTORE_NOT_COMPLETE;
		jmethodID RESTORE_ABORT;
		jmethodID RESTORE_QUERY;

		// publication and subscription
		jmethodID PUBLISH_OBJECT_CLASS;
		jmethodID UNPUBLISH_OBJECT_CLASS;
		jmethodID UNPUBLISH_OBJECT_CLASS_WITH_ATTRIBUTES;
		jmethodID PUBLISH_INTERACTION_CLASS;
		jmethodID UNPUBLISH_INTERACTION_CLASS;
		jmethodID SUBSCRIBE_ATTRIBUTES;
		jmethodID SUBSCRIBE_ATTRIBUTES_PASSIVE;
		jmethodID UNSUBSCRIBE_OBJECT_CLASS;
		jmethodID UNSUBSCRIBE_OBJECT_CLASS_WITH_ATTRIBUTES;
		jmethodID SUBSCRIBE_INTERACTION_CLASS;
		jmethodID SUBSCRIBE_INTERACTION_CLASS_PASSIVE;
		jmethodID UNSUBSCRIBE_INTERACTION_CLASS;

		// object management
		jmethodID RESERVE_NAME;
		jmethodID RELEASE_NAME;
		jmethodID RESERVE_MULTIPLE_NAMES;
		jmethodID RELEASE_MULTIPLE_NAMES;
		jmethodID REGISTER_OBJECT;
		jmethodID REGISTER_OBJECT_WITH_NAME;
		jmethodID UPDATE_ATTRIBUTE_VALUES;
		jmethodID UPDATE_ATTRIBUTE_VALUES_WITH_TIME;
		jmethodID SEND_INTERACTION;
		jmethodID SEND_INTERACTION_WITH_TIME;
		jmethodID DELETE_OBJECT_INSTANCE;
		jmethodID DELETE_OBJECT_INSTANCE_WITH_TIME;
		jmethodID LOCAL_DELETE_OBJECT_INSTANCE;
		jmethodID REQUEST_OBJECT_ATTRIBUTE_VALUE_UPDATE;
		jmethodID REQUEST_CLASS_ATTRIBUTE_VALUE_UPDATE;
		jmethodID REQUEST_ATTRIBUTE_TRANSPORT_CHANGE;
		jmethodID QUERY_ATTRIBUTE_TRANSPORT_TYPE;
		jmethodID REQUEST_INTERACTION_TRANSPORT_CHANGE;
		jmethodID QUERY_INTERACTION_TRANSPORT_TYPE;

		// ownership management
		jmethodID UNCONDITIONAL_DIVEST;
		jmethodID NEGOTIATED_DIVEST;
		jmethodID CONFIRM_DIVEST;
		jmethodID ATTRIBUTE_ACQUISITION;
		jmethodID ATTRIBUTE_ACQUISITION_AVAILABLE;
		jmethodID ATTRIBUTE_OWNERSHIP_RELEASE_DENIED;
		jmethodID ATTRIBUTE_DIVEST_IF_WANTED;
		jmethodID CANCEL_NEGOTIATED_DIVEST;
		jmethodID CANCEL_OWNERSHIP_ACQUISITION;
		jmethodID QUERY_ATTRIBUTE_OWNERSHIP;
		jmethodID IS_ATTRIBUTE_OWNED_BY_FEDERATE;

		// time management
		jmethodID ENABLE_TIME_REGULATION;
		jmethodID DISABLE_TIME_REGULATION;
		jmethodID ENABLE_TIME_CONSTRAINED;
		jmethodID DISABLE_TIME_CONSTRAINED;
		jmethodID TIME_ADVANCE_REQUEST;
		jmethodID TIME_ADVANCE_REQUEST_AVAILABLE;
		jmethodID NEXT_EVENT_REQUEST;
		jmethodID NEXT_EVENT_REQUEST_AVAILABLE;
		jmethodID FLUSH_QUEUE_REQUEST;
		jmethodID ENABLE_ASYNCHRONOUS_DELIVERY;
		jmethodID DISABLE_ASYNCHRONOUS_DELIVERY;
		jmethodID QUERY_GALT;
		jmethodID QUERY_TIME;
		jmethodID QUERY_LITS;
		jmethodID MODIFY_LOOKAHEAD;
		jmethodID QUERY_LOOKAHEAD;
		jmethodID RETRACT;
		jmethodID CHANGE_ATTRIBUTE_ORDER_TYPE;
		jmethodID CHANGE_INTERACTION_ORDER_TYPE;

		// data distribution management
		// Note supported as yet
		jmethodID CREATE_REGION;
		jmethodID COMMIT_REGION_MODIFICATION;
		jmethodID DELETE_REGION;
		jmethodID REGISTER_OBJECT_WITH_REGION;
		jmethodID REGISTER_OBJECT_WITH_NAME_AND_REGION;
		jmethodID ASSOCIATE_REGION_FOR_UPDATES;
		jmethodID UNASSOCIATE_REGION_FOR_UPDATES;
		jmethodID SUBSCRIBE_ATTRIBUTES_WITH_REGION;
		jmethodID SUBSCRIBE_ATTRIBUTES_WITH_REGION_AND_RATE;
		jmethodID SUBSCRIBE_ATTRIBUTES_PASSIVELY_WITH_REGION;
		jmethodID SUBSCRIBE_ATTRIBUTES_PASSIVELY_WITH_REGION_AND_RATE;
		jmethodID UNSUBSCRIBE_ATTRIBUTES_WITH_REGION;
		jmethodID SUBSCRIBE_INTERACTION_CLASS_WITH_REGION;
		jmethodID SUBSCRIBE_INTERACTION_CLASS_PASSIVELY_WITH_REGION;
		jmethodID UNSUBSCRIBE_INTERACTION_CLASS_WITH_REGION;
		jmethodID SEND_INTERACTION_WITH_REGION;
		jmethodID SEND_INTERACTION_WITH_TIME_AND_REGION;
		jmethodID REQUEST_CLASS_ATTRIBUTE_VALUE_UPDATE_WITH_REGION;


		// support services
		jmethodID GET_AUTO_RESIGN_DIRECTIVE;
		jmethodID SET_AUTO_RESIGN_DIRECTIVE;
		jmethodID GET_FEDERATE_HANDLE;
		jmethodID GET_FEDERATE_NAME;
		jmethodID GET_OBJECT_CLASS_HANDLE;
		jmethodID GET_OBJECT_CLASS_NAME;
		jmethodID GET_KNOWN_OBJECT_CLASS_HANDLE;
		jmethodID GET_OBJECT_INSTANCE_HANDLE;
		jmethodID GET_OBJECT_INSTANCE_NAME;
		jmethodID GET_ATTRIBUTE_HANDLE;
		jmethodID GET_ATTRIBUTE_NAME;
		jmethodID GET_UPDATE_RATE;
		jmethodID GET_UPDATE_RATE_FOR_ATTRIBUTE;
		
		jmethodID GET_INTERACTION_CLASS_HANDLE;
		jmethodID GET_INTERACTION_CLASS_NAME;
		jmethodID GET_PARAMETER_HANDLE;
		jmethodID GET_PARAMETER_NAME;

		jmethodID GET_DIMENSIONS_FOR_CLASS_ATTRIBUTE;
		jmethodID GET_DIMENSIONS_FOR_INTERACTION_CLASS;
		jmethodID GET_DIMENSION_HANDLE;
		jmethodID GET_DIMENSION_NAME;
		jmethodID GET_DIMENSION_UPPER;
		jmethodID GET_DIMENSION_HANDLE_SET;
		jmethodID GET_RANGE_BOUNDS;
		jmethodID SET_RANGE_BOUNDS;
		jmethodID ENABLE_CLASS_RELEVANCE_ADVISORY_SWITCH;
		jmethodID DISABLE_CLASS_RELEVANCE_ADVISORY_SWITCH;
		jmethodID ENABLE_ATTRIBUTE_RELEVANCE_ADVISORY_SWITCH;
		jmethodID DISABLE_ATTRIBUTE_RELEVANCE_ADVISORY_SWITCH;
		jmethodID ENABLE_ATTRIBUTE_SCOPE_ADVISORY_SWITCH;
		jmethodID DISABLE_ATTRIBUTE_SCOPE_ADVISORY_SWITCH;
		jmethodID ENABLE_INTERACTION_RELEVANCE_ADVISORY_SWITCH;
		jmethodID DISABLE_INTERACTION_RELEVANCE_ADVISORY_SWITCH;
		
		jmethodID EVOKE_CALLBACK;
		jmethodID EVOKE_MULTIPLE_CALLBACKS;
		jmethodID ENABLE_CALLBACKS;
		jmethodID DISABLE_CALLBACKS;
		jmethodID GET_HLA_VERSION;
		jmethodID KILL;

		//custom portico
		jmethodID GET_ATTRIBUTE_DATATYPE;
		jmethodID GET_PARAMETER_DATATYPE; 
		jmethodID GET_FOM;
};

PORTICO1516E_NS_END

#endif /* JAVARTI_H_ */
