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
#include "common.h"
#include "jni/JniUtils.h"
#include "types/handles/HandleImplementations.h"
#include "types/handles/HandleFriends.h"
#include "utils/StringUtils.h"

PORTICO1516E_NS_START

// set up our special logger
Logger* JniUtils::logger = new Logger( "jni" );

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------
string JniUtils::toString( JNIEnv *jnienv, jstring javaString )
{
	const char *temp = jnienv->GetStringUTFChars( javaString, NULL );
	string converted( temp );
	jnienv->ReleaseStringUTFChars( javaString, temp );
	return converted;
}

wstring JniUtils::toWideString( JNIEnv *jnienv, jstring javaString )
{
	const jchar *characters = jnienv->GetStringChars( javaString, NULL );
	const jchar *pointer = characters;
	wstring wideload;
	while( *pointer )
	{
		wideload += *(pointer++);
	}
	
	jnienv->ReleaseStringChars( javaString, characters );
	return wideload;
}


string JniUtils::toStringAndRelease( JNIEnv *jnienv, jstring javaString )
{
	string converted = JniUtils::toString( jnienv, javaString );
	jnienv->DeleteLocalRef( javaString );
	return converted;
}

set<wstring> JniUtils::toWideStringSet( JNIEnv *jnienv, jobjectArray stringArray )
{
	jsize size = jnienv->GetArrayLength( stringArray );
	set<wstring> stringSet;
	for( int i = 0; i < size; i++ )
	{
		jstring temp = (jstring)jnienv->GetObjectArrayElement( stringArray, i );
		stringSet.insert( JniUtils::toWideString(jnienv,temp) );
	}
	
	return stringSet;
}

jstring JniUtils::fromString( JNIEnv *jnienv, string cstring )
{
	return jnienv->NewStringUTF( cstring.c_str() );
}

jstring JniUtils::fromWideString( JNIEnv *jnienv, wstring cstring )
{
	return jnienv->NewStringUTF( StringUtils::toShortString(cstring).c_str() );
}

////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// Handle Conversion Methods //////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
FederateHandle JniUtils::toFederateHandle( jint handle )
{
	return FederateHandleFriend::create( handle );
}

ObjectClassHandle JniUtils::toObjectClassHandle( jint handle )
{
	return ObjectClassHandleFriend::create( handle );
}

ObjectInstanceHandle JniUtils::toObjectHandle( jint handle )
{
	return ObjectInstanceHandleFriend::create( handle );
}

AttributeHandle JniUtils::toAttributeHandle( jint handle )
{
	return AttributeHandleFriend::create( handle );
}

InteractionClassHandle JniUtils::toInteractionClassHandle( jint handle )
{
	return InteractionClassHandleFriend::create( handle );
}

ParameterHandle JniUtils::toParameterHandle( jint handle )
{
	return ParameterHandleFriend::create( handle );
}

RegionHandle JniUtils::toRegionHandle( jint handle )
{
	return RegionHandleFriend::create( handle );
}

////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////// Set Conversion Methods ////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////

/////////////
// *Need basic test framework so I can write some simple tests
// *Need to ensure this is how we handle jni arrays
// *Need to ensure memory conventions of VariableLengthData are obeyed
// *How do I get the implementation into the handle!!?
/////////////

// set and map conversion methods
AttributeHandleSet JniUtils::toAttributeSet( JNIEnv *jnienv, jintArray handles )
{
	// figure out how many elements we have to copy over
	jsize size = jnienv->GetArrayLength( handles );
	
	// create the store and populate it
	AttributeHandleSet handleSet;
	jint *content = jnienv->GetIntArrayElements( handles, NULL );
	for( int i = 0; i < size; ++i )
		handleSet.insert( AttributeHandleFriend::create(content[i]) );

	jnienv->ReleaseIntArrayElements( handles, content, JNI_ABORT );
	return handleSet;
}

FederateHandleSet JniUtils::toFederateSet( JNIEnv *jnienv, jintArray handles )
{
	// figure out how many elements we have to copy over
	jsize size = jnienv->GetArrayLength( handles );
	
	// create the store and populate it
	FederateHandleSet handleSet;
	jint *content = jnienv->GetIntArrayElements( handles, NULL );
	for( int i = 0; i < size; ++i )
		handleSet.insert( FederateHandleFriend::create(content[i]) );

	jnienv->ReleaseIntArrayElements( handles, content, JNI_ABORT );
	return handleSet;
}

AttributeHandleValueMap JniUtils::toAttributeValueMap( JNIEnv *jnienv,
                                                       jintArray handles,
                                                       jobjectArray values )
{
	// figure out how many elements we have to copy over
	jsize size = jnienv->GetArrayLength( handles );

	// populate the map
	AttributeHandleValueMap valueMap;
	jint *content = jnienv->GetIntArrayElements( handles, NULL );
	for( int i = 0; i < size; ++i )
	{
		// get the byte[] value
		jbyteArray value = (jbyteArray)jnienv->GetObjectArrayElement( values, i );
		jsize valueSize = jnienv->GetArrayLength( value );
		jbyte valueBuffer[valueSize];
		jnienv->GetByteArrayRegion( value, 0, valueSize, valueBuffer );

		// store the byte[] in a VariableLengthData
		VariableLengthData data( (void*)valueBuffer, valueSize );
		AttributeHandle handle = AttributeHandleFriend::create( content[i] );
		valueMap[handle] = data;
	}
	
	jnienv->ReleaseIntArrayElements( handles, content, JNI_ABORT );
	return valueMap;
}

ParameterHandleValueMap JniUtils::toParameterValueMap( JNIEnv *jnienv,
                                                       jintArray handles,
                                                       jobjectArray values )
{
	// figure out how many elements we have to copy over
	jsize size = jnienv->GetArrayLength( handles );

	// populate the map
	ParameterHandleValueMap valueMap;
	jint *content = jnienv->GetIntArrayElements( handles, NULL );
	for( int i = 0; i < size; ++i )
	{
		// get the byte[] value
		jbyteArray value = (jbyteArray)jnienv->GetObjectArrayElement( values, i );
		jsize valueSize = jnienv->GetArrayLength( value );
		jbyte valueBuffer[valueSize];
		jnienv->GetByteArrayRegion( value, 0, valueSize, valueBuffer );

		// store the byte[] in a VariableLengthData
		VariableLengthData data( (void*)valueBuffer, valueSize );
		ParameterHandle handle = ParameterHandleFriend::create( content[i] );
		valueMap[handle] = data;
	}
	
	jnienv->ReleaseIntArrayElements( handles, content, JNI_ABORT );
	return valueMap;
}

RegionHandleSet JniUtils::toRegionSet( JNIEnv *jnienv, jintArray handles )
{
	jsize size = jnienv->GetArrayLength( handles );
	// populate the map
	RegionHandleSet regionSet;
	jint *content = jnienv->GetIntArrayElements( handles, NULL );
	for( int i = 0; i < size; i++ )
	{
		regionSet.insert( RegionHandleFriend::create(content[i]) );
	}
	
	jnienv->ReleaseIntArrayElements( handles, content, JNI_ABORT );
	
	return regionSet;
}

////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////// Support and Misc Type Conversion Methods ///////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
VariableLengthData JniUtils::toTag( JNIEnv *jnienv, jbyteArray jtag )
{
	// if we don't have a tag, just return an empty byte[]
	if( jtag == NULL )
		return VariableLengthData();

	// convert the tag
	//   we assume that there is no null terminator
	jsize size = jnienv->GetArrayLength( jtag );
	jbyte *buffer = new jbyte[size];
	jnienv->GetByteArrayElements( jtag, NULL );
	VariableLengthData data( (void*)buffer, size );
	jnienv->ReleaseByteArrayElements( jtag, buffer, JNI_ABORT );
	return data;
}

jstring JniUtils::fromCallbackModel( JNIEnv *jnienv, CallbackModel model )
{
	if( model == HLA_EVOKED )
	{
		return jnienv->NewStringUTF( "HLA_EVOKED" );
	}
	else
	{
		return jnienv->NewStringUTF( "HLA_IMMEDIATE" );
	}
}

////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// Time Conversion Helpers ////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
MessageRetractionHandle JniUtils::toRetractionHandle( jint handle )
{
	return MessageRetractionHandleFriend::create( handle );
}

////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// Sync Point Helpers //////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
//enum SynchronizationPointFailureReason
//{
//   SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE,
//   SYNCHRONIZATION_SET_MEMBER_NOT_JOINED
//};
SynchronizationPointFailureReason JniUtils::toSyncPointFailureReason( JNIEnv *jnienv,
                                                                      jstring reason )
{
	string value = JniUtils::toString( jnienv, reason );
	if( value.compare("SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE") == 0 )
	{
		return SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE;
	}
	else if( value.compare("SYNCHRONIZATION_SET_MEMBER_NOT_JOINED") == 0 )
	{
		return SYNCHRONIZATION_SET_MEMBER_NOT_JOINED;
	}
	else
	{
		JniUtils::logger->error( "Conversion failure [%s]: received unknown value from Java: %s",
		                         "SynchronizationPointFailureReason",
		                         value.c_str() );
		JniUtils::logger->error( "Defaulting to SYNCHRONIZATION_SET_MEMBER_NOT_JOINED" );
		return SYNCHRONIZATION_SET_MEMBER_NOT_JOINED;
	}
}

////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////// Save/Restore Conversion Helpers ///////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
//enum SaveFailureReason
//{
//   RTI_UNABLE_TO_SAVE,
//   FEDERATE_REPORTED_FAILURE_DURING_SAVE,
//   FEDERATE_RESIGNED_DURING_SAVE,
//   RTI_DETECTED_FAILURE_DURING_SAVE,
//   SAVE_TIME_CANNOT_BE_HONORED,
//   SAVE_ABORTED
//};
SaveFailureReason JniUtils::toSaveFailureReason( JNIEnv *jnienv, jstring reason )
{
	string value = JniUtils::toString( jnienv, reason );
	if( value.compare("RTI_UNABLE_TO_SAVE") == 0 )
	{
		return RTI_UNABLE_TO_SAVE;
	}
	else if( value.compare("FEDERATE_REPORTED_FAILURE_DURING_SAVE") == 0 )
	{
		return FEDERATE_REPORTED_FAILURE_DURING_SAVE;
	}
	else if( value.compare("FEDERATE_RESIGNED_DURING_SAVE") == 0 )
	{
		return FEDERATE_RESIGNED_DURING_SAVE;
	}
	else if( value.compare("RTI_DETECTED_FAILURE_DURING_SAVE") == 0 )
	{
		return RTI_DETECTED_FAILURE_DURING_SAVE;
	}
	else if( value.compare("SAVE_TIME_CANNOT_BE_HONORED") == 0 )
	{
		return SAVE_TIME_CANNOT_BE_HONORED;
	}
	else if( value.compare("SAVE_ABORTED") == 0 )
	{
		return SAVE_ABORTED;
	}
	else
	{
		JniUtils::logger->error( "Conversion failure [%s]: received unknown value from Java: %s",
		                         "SaveFailureReason",
		                         value.c_str() );
		JniUtils::logger->error( "Defaulting to RTI_UNABLE_TO_SAVE" );
		return RTI_UNABLE_TO_SAVE;
	}
}

//enum SaveStatus
//{
//   NO_SAVE_IN_PROGRESS,
//   FEDERATE_INSTRUCTED_TO_SAVE,
//   FEDERATE_SAVING,
//   FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE
//};
SaveStatus JniUtils::toSaveStatus( JNIEnv *jnienv, jstring status )
{
	string value = JniUtils::toString( jnienv, status );
	if( value.compare("NO_SAVE_IN_PROGRESS") == 0 )
	{
		return NO_SAVE_IN_PROGRESS;
	}
	else if( value.compare("FEDERATE_INSTRUCTED_TO_SAVE") == 0 )
	{
		return FEDERATE_INSTRUCTED_TO_SAVE;
	}
	else if( value.compare("FEDERATE_SAVING") == 0 )
	{
		return FEDERATE_SAVING;
	}
	else if( value.compare("FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE") == 0 )
	{
		return FEDERATE_WAITING_FOR_FEDERATION_TO_SAVE;
	}
	else
	{
		JniUtils::logger->error( "Conversion failure [%s]: received unknown value from Java: %s",
		                         "SaveStatus",
		                         value.c_str() );
		JniUtils::logger->error( "Defaulting to NO_SAVE_IN_PROGRESS" );
		return NO_SAVE_IN_PROGRESS;
	}
}

/**
 * This method takes an int array (federate handles) and a string array (save statuses)
 * and reconstitutes it into a FederateHandleSaveStatusPairVector. That type is just a
 * vector of std::pair<FederateHandle,SaveStatus> instances. In the Java side, this was
 * split into two arrays to make it easy to pass, so in this side we link it back into
 * one type.
 */
FederateHandleSaveStatusPairVector JniUtils::toSaveStatusPairVector( JNIEnv *jnienv,
                                                                     jintArray handles,
                                                                     jobjectArray statuses )
{
	// figure out how many of these things we have to deal with
	// these SHOULD be the same
	jsize size = jnienv->GetArrayLength( handles );

	// Create the return vector and loop through each of the handles to
	// genreate the pairs. For each handle, we pair it with the status at
	// the same position
	FederateHandleSaveStatusPairVector values;

	jint *handleContent = jnienv->GetIntArrayElements( handles, NULL );
	for( int i = 0; i < size; ++i )
	{
		FederateHandleSaveStatusPair thePair;
		thePair.first = FederateHandleFriend::create( handleContent[i] );
		
		jstring statusJString = (jstring)jnienv->GetObjectArrayElement( statuses, i );
		thePair.second = JniUtils::toSaveStatus( jnienv, statusJString );
	}

	// release our reference to the int array handle content
	jnienv->ReleaseIntArrayElements( handles, handleContent, JNI_ABORT );
	return values;
}

//enum RestoreFailureReason
//{
//   RTI_UNABLE_TO_RESTORE,
//   FEDERATE_REPORTED_FAILURE_DURING_RESTORE,
//   FEDERATE_RESIGNED_DURING_RESTORE,
//   RTI_DETECTED_FAILURE_DURING_RESTORE,
//   RESTORE_ABORTED
//};
RestoreFailureReason JniUtils::toRestoreFailureReason( JNIEnv *jnienv, jstring reason )
{
	string value = JniUtils::toString( jnienv, reason );
	if( value.compare("RTI_UNABLE_TO_RESTORE") == 0 )
	{
		return RTI_UNABLE_TO_RESTORE;
	}
	else if( value.compare("FEDERATE_REPORTED_FAILURE_DURING_RESTORE") == 0 )
	{
		return FEDERATE_REPORTED_FAILURE_DURING_RESTORE;
	}
	else if( value.compare("FEDERATE_RESIGNED_DURING_RESTORE") == 0 )
	{
		return FEDERATE_RESIGNED_DURING_RESTORE;
	}
	else if( value.compare("RTI_DETECTED_FAILURE_DURING_RESTORE") == 0 )
	{
		return RTI_DETECTED_FAILURE_DURING_RESTORE;
	}
	else if( value.compare("RESTORE_ABORTED") == 0 )
	{
		return RESTORE_ABORTED;
	}
	else
	{
		JniUtils::logger->error( "Conversion failure [%s]: received unknown value from Java: %s",
		                         "RestoreFailureReason",
		                         value.c_str() );
		JniUtils::logger->error( "Defaulting to RTI_UNABLE_TO_RESTORE" );
		return RTI_UNABLE_TO_RESTORE;
	}
}

//enum RestoreStatus
//{
//   NO_RESTORE_IN_PROGRESS,
//   FEDERATE_RESTORE_REQUEST_PENDING,
//   FEDERATE_WAITING_FOR_RESTORE_TO_BEGIN,
//   FEDERATE_PREPARED_TO_RESTORE,
//   FEDERATE_RESTORING,
//   FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE
//};
RestoreStatus JniUtils::toRestoreStatus( JNIEnv *jnienv, jstring status )
{
	string value = JniUtils::toString( jnienv, status );
	if( value.compare("NO_RESTORE_IN_PROGRESS") == 0 )
	{
		return NO_RESTORE_IN_PROGRESS;
	}
	else if( value.compare("FEDERATE_RESTORE_REQUEST_PENDING") == 0 )
	{
		return FEDERATE_RESTORE_REQUEST_PENDING;
	}
	else if( value.compare("FEDERATE_WAITING_FOR_RESTORE_TO_BEGIN") == 0 )
	{
		return FEDERATE_WAITING_FOR_RESTORE_TO_BEGIN;
	}
	else if( value.compare("FEDERATE_PREPARED_TO_RESTORE") == 0 )
	{
		return FEDERATE_PREPARED_TO_RESTORE;
	}
	else if( value.compare("FEDERATE_RESTORING") == 0 )
	{
		return FEDERATE_RESTORING;
	}
	else if( value.compare("FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE") == 0 )
	{
		return FEDERATE_WAITING_FOR_FEDERATION_TO_RESTORE;
	}
	else
	{
		JniUtils::logger->error( "Conversion failure [%s]: received unknown value from Java: %s",
		                         "RestoreStatus",
		                         value.c_str() );
		JniUtils::logger->error( "Defaulting to NO_RESTORE_IN_PROGRESS" );
		return NO_RESTORE_IN_PROGRESS;
	}
}

/**
 * Convert the provided values (pre-restore handle, post-restore handle and restore status)
 * into a vector of FederateRestoreStatus objects and return.
 */
FederateRestoreStatusVector JniUtils::toRestoreStatusVector( JNIEnv *jnienv,
                                                             jintArray preHandles,
                                                             jintArray postHandles,
                                                             jobjectArray statuses )
{
	// how many of these things do we have to deal with?
	jsize size = jnienv->GetArrayLength( preHandles );
	
	// get references to each of the int arrays so we can loop through them
	jint *preHandleContent = jnienv->GetIntArrayElements( preHandles, NULL );
	jint *postHandleContent = jnienv->GetIntArrayElements( postHandles, NULL );
	FederateRestoreStatusVector statusVector;

	for( int i = 0; i < size; ++i )
	{
		FederateHandle preHandle = FederateHandleFriend::create( preHandleContent[i] );
		FederateHandle postHandle = FederateHandleFriend::create( postHandleContent[i] );

		jstring statusJString = (jstring)jnienv->GetObjectArrayElement( statuses, i );
		RestoreStatus status = JniUtils::toRestoreStatus( jnienv, statusJString );
		jnienv->DeleteLocalRef( statusJString );

		FederateRestoreStatus restoreStatus( preHandle, postHandle, status );
		statusVector.push_back( restoreStatus );
	}
	
	jnienv->ReleaseIntArrayElements( preHandles, preHandleContent, JNI_ABORT );
	jnienv->ReleaseIntArrayElements( postHandles, postHandleContent, JNI_ABORT );
	return statusVector;
}

////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////// Supplemental Types ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
SupplementalReflectInfo JniUtils::toReflectSupplement( JNIEnv *jnienv,
                                                       jint source,
                                                       jintArray regions )
{
	SupplementalReflectInfo reflectInfo;

	// do we have a producing federate handle?
	if( source != -1 )
	{
		reflectInfo.hasProducingFederate = true;
		reflectInfo.producingFederate = FederateHandleFriend::create( source );
	}
	
	// how many regions do we have?
	jsize size = jnienv->GetArrayLength( regions );
	if( size > 0 )
	{
		reflectInfo.hasSentRegions = true;
		reflectInfo.sentRegions = JniUtils::toRegionSet( jnienv, regions );
	}
	
	return reflectInfo;
}

SupplementalReceiveInfo JniUtils::toReceiveSupplement( JNIEnv *jnienv,
                                                       jint source,
                                                       jintArray regions )
{
	SupplementalReceiveInfo receiveInfo;

	// do we have a producing federate handle?
	if( source != -1 )
	{
		receiveInfo.hasProducingFederate = true;
		receiveInfo.producingFederate = FederateHandleFriend::create( source );
	}
	
	// how many regions do we have?
	jsize size = jnienv->GetArrayLength( regions );
	if( size > 0 )
	{
		receiveInfo.hasSentRegions = true;
		receiveInfo.sentRegions = JniUtils::toRegionSet( jnienv, regions );
	}
	
	return receiveInfo;
}

SupplementalRemoveInfo JniUtils::toRemoveSupplement( jint source )
{
	SupplementalRemoveInfo removeInfo;
	
	// do we have a producing federate handle?
	if( source != -1 )
	{
		removeInfo.hasProducingFederate = true;
		removeInfo.producingFederate = FederateHandleFriend::create( source );
	}
	
	return removeInfo;
}

////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// Misc Conversion Methods ////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
FederationExecutionInformationVector JniUtils::toFedInformationVector( JNIEnv *jnienv,
                                                                       jobjectArray fedNames,
                                                                       jobjectArray timeNames )
{
	jsize size = jnienv->GetArrayLength( fedNames );
	
	FederationExecutionInformationVector fedVector;
	for( int i = 0; i < size; i++ )
	{
		jstring jFedName = (jstring)jnienv->GetObjectArrayElement( fedNames, i );
		wstring cFedName = JniUtils::toWideString( jnienv, jFedName );
		jnienv->DeleteLocalRef( jFedName );

		jstring jTimeName = (jstring)jnienv->GetObjectArrayElement( timeNames, i );
		wstring cTimeName = JniUtils::toWideString( jnienv, jTimeName );
		jnienv->DeleteLocalRef( jTimeName );
		
		FederationExecutionInformation info( cFedName, cTimeName );
		fedVector.push_back( info );
	}
	
	return fedVector;
}

OrderType JniUtils::toOrder( jint type )
{
	if( type == 1 )
	{
		return RECEIVE;
	}
	else if( type == 2 )
	{
		return TIMESTAMP;
	}
	else
	{
		JniUtils::logger->error( "Conversion failure [%s]: received unknown value from Java: %d",
		                         "OrderType",
		                         type );
		JniUtils::logger->error( "Defaulting to RECEIVE" );
		return RECEIVE;
	}
}

TransportationType JniUtils::toTransport( jint type )
{
	if( type == 1 )
	{
		return RELIABLE;
	}
	else if( type == 2 )
	{
		return BEST_EFFORT;
	}
	else
	{
		JniUtils::logger->error( "Conversion failure [%s]: received unknown value from Java: %d",
		                         "TransportationType",
		                         type );
		JniUtils::logger->error( "Defaulting to BEST_EFFORT" );
		return BEST_EFFORT;
	}
}

PORTICO1516E_NS_END
