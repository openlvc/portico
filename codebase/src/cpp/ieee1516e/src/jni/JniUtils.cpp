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
#include "jni/Runtime.h"
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

jint JniUtils::fromHandle( FederateHandle handle )
{
	return FederateHandleFriend::getInt( handle );
}

jint JniUtils::fromHandle( ObjectClassHandle handle )
{
	return ObjectClassHandleFriend::getInt( handle );
}

jint JniUtils::fromHandle( ObjectInstanceHandle handle )
{
	return ObjectInstanceHandleFriend::getInt( handle );
}

jint JniUtils::fromHandle( AttributeHandle handle )
{
	return AttributeHandleFriend::getInt( handle );
}

jint JniUtils::fromHandle( InteractionClassHandle handle )
{
	return InteractionClassHandleFriend::getInt( handle );
}

jint JniUtils::fromHandle( ParameterHandle handle )
{
	return ParameterHandleFriend::getInt( handle );
}

jint JniUtils::fromHandle( RegionHandle handle )
{
	return RegionHandleFriend::getInt( handle );
}

jint JniUtils::fromHandle( MessageRetractionHandle handle )
{
	return MessageRetractionHandleFriend::getInt( handle );
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

/*
 * Converts the provided jintArray into an AttributeHandleSet.
 */
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

/*
 * Converts the provided jintArray into an FederateHandleSet.
 */
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

/*
 * Converts the provided jintArray and jobjectArray into a single AttributeHandleValueMap.
 * The int[] is expected to contain the handles, with the values for each handle held at
 * the same index in the jobjectArray (a byte[][] from the Java side). This method will
 * convert the handles into AttributeHandles and will wrap the values in VariableLengthData
 * objects, storing them in the map before returning.
 */
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

/*
 * Converts the provided jintArray and jobjectArray into a single ParameterHandleValueMap.
 * The int[] is expected to contain the handles, with the values for each handle held at
 * the same index in the jobjectArray (a byte[][] from the Java side). This method will
 * convert the handles into ParameterHandles and will wrap the values in VariableLengthData
 * objects, storing them in the map before returning.
 */
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

/*
 * Converts the provided jintArray into an RegionHandleSet.
 */
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

/**
 * Takes the provided vector of wstring's and converts it into a String[] to be passed
 * over the JNI boundary to Java.
 * 
 * NOTE: The caller is responsible for releasing the references held by the returned array
 */
jobjectArray JniUtils::fromVector( JNIEnv *jnienv, vector<wstring> stringVector )
{
	jclass stringClass = jnienv->FindClass( "[Ljava/lang/String;" );
	jobjectArray array = jnienv->NewObjectArray( stringVector.size(), stringClass, 0 );
	
	int count = 0;
	vector<wstring>::iterator iterator;
	for( iterator = stringVector.begin(); iterator != stringVector.end(); iterator++, count++ )
	{
		jstring temp = jnienv->NewString( (jchar*)(*iterator).c_str(), (*iterator).length() );
		jnienv->SetObjectArrayElement( array, count, temp );
	}
	
	return array;
}

/**
 * Takes the provided set of wstring's and converts it into a String[] to be passed
 * over the JNI boundary to Java.
 * 
 * NOTE: The caller is responsible for releasing the references held by the returned array
 */
jobjectArray JniUtils::fromSet( JNIEnv *jnienv, set<wstring> stringSet )
{
	jclass stringClass = jnienv->FindClass( "[Ljava/lang/String;" );
	jobjectArray array = jnienv->NewObjectArray( stringSet.size(), stringClass, 0 );
	
	int count = 0;
	set<wstring>::iterator iterator;
	for( iterator = stringSet.begin(); iterator != stringSet.end(); iterator++, count++ )
	{
		jstring temp = jnienv->NewString( (jchar*)(*iterator).c_str(), (*iterator).length() );
		jnienv->SetObjectArrayElement( array, count, temp );
	}
	
	return array;
}

jintArray JniUtils::fromSet( JNIEnv *jnienv, AttributeHandleSet attributes )
{
	jintArray array = jnienv->NewIntArray( attributes.size() );
	jint* content = jnienv->GetIntArrayElements( array, NULL );
	int count = 0;
	AttributeHandleSet::iterator iterator;
	for( iterator = attributes.begin(); iterator != attributes.end(); iterator++, count++ )
	{
		content[count] = AttributeHandleFriend::getInt( (AttributeHandle&)*iterator );
	}

	jnienv->ReleaseIntArrayElements( array, content, 0 );
	return array;
}

jintArray JniUtils::fromSet( JNIEnv *jnienv, FederateHandleSet federates )
{
	jintArray array = jnienv->NewIntArray( federates.size() );
	jint* content = jnienv->GetIntArrayElements( array, NULL );
	int count = 0;
	FederateHandleSet::iterator iterator;
	for( iterator = federates.begin(); iterator != federates.end(); iterator++, count++ )
	{
		content[count] = FederateHandleFriend::getInt( (FederateHandle&)*iterator );
	}

	jnienv->ReleaseIntArrayElements( array, content, 0 );
	return array;
}

/**
 * Convert the given AttributeHandleValueMap into a HVPS, which itself has two elements:
 * a jintArray representing the handle in the map, and a jobjectArray representing the
 * various values in the map. This will put the values in a form we can pass over the
 * JNI boundary.
 */
HVPS JniUtils::fromMap( JNIEnv *jnienv, AttributeHandleValueMap values )
{
	// create the struct to hold the values
	HVPS hvps = HVPS();
	hvps.handles = jnienv->NewIntArray( values.size() );
	hvps.values  = jnienv->NewObjectArray( values.size(), Runtime::JCLASS_BYTE_ARRAY, 0 );

	// get references to the array contents as JNI requires
	jint *handlesContent = jnienv->GetIntArrayElements( hvps.handles, NULL );

	// loop through all values in the map and put them into the appropriate location
	AttributeHandleValueMap::iterator iterator;
	int i = 0;
	for( iterator = values.begin(); iterator != values.end(); iterator++, i++ )
	{
		// get the AttributeHandle
		handlesContent[i] = JniUtils::fromHandle( (*iterator).first );
	
		// get the value and convert it into a byte[]
		VariableLengthData data = (*iterator).second;
		jbyteArray tempArray = jnienv->NewByteArray( data.size() );
		jnienv->SetByteArrayRegion( tempArray, 0, data.size(), (jbyte*)data.data() );
		jnienv->SetObjectArrayElement( hvps.values, i, tempArray );
		jnienv->DeleteLocalRef( tempArray );
	}

	// release the handles array contents pointer
	jnienv->ReleaseIntArrayElements( hvps.handles, handlesContent, 0 );

	// return the result
	return hvps;
}

/**
 * Convert the given ParameterHandleValueMap into a HVPS, which itself has two elements:
 * a jintArray representing the handle in the map, and a jobjectArray representing the
 * various values in the map. This will put the values in a form we can pass over the
 * JNI boundary.
 */
HVPS JniUtils::fromMap( JNIEnv *jnienv, ParameterHandleValueMap values )
{
	// create the struct to hold the values
	HVPS hvps = HVPS();
	hvps.handles = jnienv->NewIntArray( values.size() );
	hvps.values  = jnienv->NewObjectArray( values.size(), Runtime::JCLASS_BYTE_ARRAY, 0 );

	// get references to the array contents as JNI requires
	jint *handlesContent = jnienv->GetIntArrayElements( hvps.handles, NULL );

	// loop through all values in the map and put them into the appropriate location
	ParameterHandleValueMap::iterator iterator;
	int i = 0;
	for( iterator = values.begin(); iterator != values.end(); iterator++, i++ )
	{
		// get the AttributeHandle
		handlesContent[i] = JniUtils::fromHandle( (*iterator).first );
	
		// get the value and convert it into a byte[]
		VariableLengthData data = (*iterator).second;
		jbyteArray tempArray = jnienv->NewByteArray( data.size() );
		jnienv->SetByteArrayRegion( tempArray, 0, data.size(), (jbyte*)data.data() );
		jnienv->SetObjectArrayElement( hvps.values, i, tempArray );
		jnienv->DeleteLocalRef( tempArray );
	}

	// release the handles array contents pointer
	jnienv->ReleaseIntArrayElements( hvps.handles, handlesContent, 0 );

	// return the result
	return hvps;
}

////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// Time Conversion Helpers ////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
MessageRetractionHandle JniUtils::toRetractionHandle( jint handle )
{
	return MessageRetractionHandleFriend::create( handle );
}

/**
 * Converts the provided logical time into a jdouble for transmission over the JNI boundary.
 * This method will try and cast the type to a HLAfloat64Time, followed by a HLAinteger64Time
 * in order to extract the underlying value. If the provided logical time isn't one of these
 * two types, an InvalidLogicalTime is thrown
 */
jdouble JniUtils::fromTime( const LogicalTime& time ) throw( InvalidLogicalTime )
{
	if( time.implementationName().compare(L"HLAfloat64Time") == 0 )
	{
		return ((HLAfloat64Time&)time).getTime();
	}
	else if( time.implementationName().compare(L"HLAinteger64Time") == 0 )
	{
		return (jdouble)((HLAinteger64Time&)time).getTime();
	}
	else
	{
		JniUtils::logger->error( "Conversion failure [time]: Received unknown time type: %ls",
		                         time.implementationName().c_str() );
		throw InvalidLogicalTime( L"Portico only supports HLAfloat64Time and HLAinteger64Time" );
	}
}

/**
 * Converts the provided logical time interval into a jdouble for transmission over the JNI
 * boundary. This method will try and cast the type to a HLAfloat64Interval, followed by a
 * HLAinteger64Interval in order to extract the underlying value. If the provided logical time
 * isn't one of these two types, an InvalidLookahead is thrown.
 */
jdouble JniUtils::fromInterval( const LogicalTimeInterval& interval )
	throw( InvalidLookahead )
{
	if( interval.implementationName().compare(L"HLAfloat64Interval") == 0 )
	{
		return ((HLAfloat64Interval&)interval).getInterval();
	}
	else if( interval.implementationName().compare(L"HLAinteger64Interval") == 0 )
	{
		return (jdouble)((HLAinteger64Interval&)interval).getInterval();
	}
	else
	{
		JniUtils::logger->error( "Conversion failure [interval]: Received unknown type: %ls",
		                         interval.implementationName().c_str() );
		throw InvalidLookahead( L"Portico only supports HLAfloat64Interval and HLAinteger64Interval" );
	}
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

/**
 * Map the provided int to a OrderType enumerated value, logging an error if we don't
 * know what value to provide for the given int (defaults to OrderType::RECEIVE).
 */
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

/**
 * Map the provided int to a TransportationType enumerated value, logging an error if we don't
 * know what value to provide for the given int (defaults to TransportationType::BEST_EFFORT).
 */
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

ResignAction JniUtils::toResignAction( JNIEnv *jnienv, jstring action )
{
	string actionString = JniUtils::toString( jnienv, action );
	if( actionString.compare("NO_ACTION") == 0 )
	{
		return NO_ACTION;
	}
	else if( actionString.compare("DELETE_OBJECTS") == 0 )
	{
		return DELETE_OBJECTS;
	}
	else if( actionString.compare("DELETE_OBJECTS_THEN_DIVEST") == 0 )
	{
		return DELETE_OBJECTS_THEN_DIVEST;
	}
	else if( actionString.compare("UNCONDITIONALLY_DIVEST_ATTRIBUTES") == 0 )
	{
		return UNCONDITIONALLY_DIVEST_ATTRIBUTES;
	}
	else if( actionString.compare("CANCEL_PENDING_OWNERSHIP_ACQUISITIONS") == 0 )
	{
		return CANCEL_PENDING_OWNERSHIP_ACQUISITIONS;
	}
	else if( actionString.compare("CANCEL_THEN_DELETE_THEN_DIVEST") == 0 )
	{
		return CANCEL_THEN_DELETE_THEN_DIVEST;
	}
	else
	{
		JniUtils::logger->error( "Conversion failure [%s]: received unknown value from Java: %s",
		                         "ResignAction",
		                         actionString.c_str() );
		JniUtils::logger->error( "Defaulting to NO_ACTION" );
		return NO_ACTION;
	}
}

jbyteArray JniUtils::fromTag( JNIEnv *jnienv, VariableLengthData tag )
{
	// create the byte[] and populate it with the tag data
	//   strip the null terminator because it won't be there for Java-federates
	//   we have to manually add and remove it to keep consistent with Java
	jbyteArray jtag = jnienv->NewByteArray( tag.size() );
	jnienv->SetByteArrayRegion( jtag, 0, tag.size(), (jbyte*)tag.data() );
	return jtag;
}

jstring JniUtils::fromOrder( JNIEnv *jnienv, OrderType order )
{
	if( order == TIMESTAMP )
	{
		return jnienv->NewStringUTF( "TIMESTAMP" );
	}
	else
	{
		return jnienv->NewStringUTF( "RECEIVE" );
	}
}

jstring JniUtils::fromTransport( JNIEnv *jnienv, TransportationType transport )
{
	if( transport == BEST_EFFORT )
	{
		return jnienv->NewStringUTF( "BEST_EFFORT" );
	}
	else
	{
		return jnienv->NewStringUTF( "RELIABLE" );
	}
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

jstring JniUtils::fromResignAction( JNIEnv *jnienv, ResignAction action )
{
	switch( action )
	{
		case NO_ACTION:
			return jnienv->NewStringUTF( "NO_ACTION" );
			break;
		case DELETE_OBJECTS:
			return jnienv->NewStringUTF( "DELETE_OBJECTS" );
			break;
		case DELETE_OBJECTS_THEN_DIVEST:
			return jnienv->NewStringUTF( "DELETE_OBJECTS_THEN_DIVEST" );
			break;
		case UNCONDITIONALLY_DIVEST_ATTRIBUTES:
			return jnienv->NewStringUTF( "UNCONDITIONALLY_DIVEST_ATTRIBUTES" );
			break;
		case CANCEL_PENDING_OWNERSHIP_ACQUISITIONS:
			return jnienv->NewStringUTF( "CANCEL_PENDING_OWNERSHIP_ACQUISITIONS" );
			break;
		case CANCEL_THEN_DELETE_THEN_DIVEST:
			return jnienv->NewStringUTF( "CANCEL_THEN_DELETE_THEN_DIVEST" );
			break;
		default:
			return jnienv->NewStringUTF( "NO_ACTION" );
	}
}

/**
 * Loops through the provided array and calls DeleteLocalRef for each of the contained
 * elements before deleting the local reference for the array itself
 */
void JniUtils::deleteJniArray( JNIEnv *jnienv, jobjectArray array )
{
	jsize size = jnienv->GetArrayLength( array );
	for( int i = 0; i < size; i++ )
		jnienv->DeleteLocalRef( jnienv->GetObjectArrayElement(array,i) );
	
	jnienv->DeleteLocalRef( array );
}

/**
 * Loops through the HVPS and releases the contains array's contents.
 */
void JniUtils::deleteHVPS( JNIEnv *jnienv, HVPS hvps )
{
	jnienv->DeleteLocalRef( hvps.handles );
	JniUtils::deleteJniArray( jnienv, hvps.values );
}

PORTICO1516E_NS_END
