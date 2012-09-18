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
#ifndef JNIUTILS_H_
#define JNIUTILS_H_

#include "common.h"
#include "utils/Logger.h"

PORTICO1516E_NS_START

/**
 * A set of utility methods to make the handling of common JNI tasks a little less
 * painful than it typically is.
 */
class JniUtils
{
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	private:
		// a shared place to log conversion errors
		static Logger *logger;
		
	public:
		/**
		 * Converts between jstring and std::string, optionally releasing the jstring once done
		 */
		static string toString( JNIEnv *jnienv, jstring javaString );
		static wstring toWideString( JNIEnv *jnienv, jstring javaString );
		static string toStringAndRelease( JNIEnv *jnienv, jstring javaString );
		static set<wstring> toWideStringSet( JNIEnv *jnienv, jobjectArray stringArray );
		
		static jstring fromString( JNIEnv *jnienv, string cstring );
		static jstring fromWideString( JNIEnv *jnienv, wstring cstring );

		///// handle conversions ////////////////////////////////////////////////////////////
		static FederateHandle toFederateHandle( jint handle );
		static ObjectClassHandle toObjectClassHandle( jint handle );
		static ObjectInstanceHandle toObjectHandle( jint handle );
		static AttributeHandle toAttributeHandle( jint handle );
		static InteractionClassHandle toInteractionClassHandle( jint handle );
		static ParameterHandle toParameterHandle( jint handle );
		static RegionHandle toRegionHandle( jint handle );
		
		static OrderType toOrder( jint type );
		static TransportationType toTransport( jint type );

		///// set and map conversion methods ////////////////////////////////////////////////
		static AttributeHandleSet toAttributeSet( JNIEnv *jnienv, jintArray handles );
		static FederateHandleSet toFederateSet( JNIEnv *jnienv, jintArray handles );
		static AttributeHandleValueMap toAttributeValueMap( JNIEnv *jnienv,
		                                                    jintArray handles,
		                                                    jobjectArray values );
		static ParameterHandleValueMap toParameterValueMap( JNIEnv *jnienv,
		                                                    jintArray handles,
		                                                    jobjectArray values );
		
		static RegionHandleSet toRegionSet( JNIEnv *jnienv, jintArray handles );
		
		///// support and misc types ////////////////////////////////////////////////////////
		static VariableLengthData toTag( JNIEnv *jnienv, jbyteArray jtag );
		
		static jstring fromCallbackModel( JNIEnv *jnienv, CallbackModel model );
		
		///// time conversion methods ///////////////////////////////////////////////////////
		static MessageRetractionHandle toRetractionHandle( jint handle );
		
		///// synchronization helpers ///////////////////////////////////////////////////////
		static SynchronizationPointFailureReason toSyncPointFailureReason( JNIEnv *jnienv,
		                                                                   jstring reason );
		
		
		///// save restore helpers //////////////////////////////////////////////////////////
		static SaveFailureReason toSaveFailureReason( JNIEnv *jnienv, jstring reason );
		static SaveStatus toSaveStatus( JNIEnv *jnienv, jstring status );
		static FederateHandleSaveStatusPairVector toSaveStatusPairVector( JNIEnv *jnienv,
		                                                                  jintArray handles,
		                                                                  jobjectArray statuses );
		
		static RestoreFailureReason toRestoreFailureReason( JNIEnv *jnienv, jstring reason );
		static RestoreStatus toRestoreStatus( JNIEnv *jnienv, jstring status );
		static FederateRestoreStatusVector toRestoreStatusVector( JNIEnv *jnienv,
		                                                          jintArray preHandles,
		                                                          jintArray postHandles,
		                                                          jobjectArray statuses );

		///// supplemental types ////////////////////////////////////////////////////////////
		/**
		 * For each of the supplemental conversion methods, if the source federate ID is
		 * -1 it will be ignored, and if the sent regions array is empty (length 0) it will
		 * also be ignored.
		 */
		static SupplementalReflectInfo toReflectSupplement( JNIEnv *jnienv,
		                                                    jint source,
		                                                    jintArray regions );
		static SupplementalReceiveInfo toReceiveSupplement( JNIEnv *jnienv,
		                                                    jint source,
		                                                    jintArray regions );
		static SupplementalRemoveInfo toRemoveSupplement( jint source );

		///// misc //////////////////////////////////////////////////////////////////////////
		static FederationExecutionInformationVector
			toFedInformationVector( JNIEnv *jnienv, jobjectArray fedNames, jobjectArray timeNames );
};

PORTICO1516E_NS_END

#endif /* JNIUTILS_H_ */
