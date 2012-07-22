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
#include "JavaRTI.h"

#include "Runtime.h"
#include "types/Region.h"

PORTICO13_NS_START

int JavaRTI::rtiCounter = 0;

//----------------------------------------------------------
//                      CONSTRUCTORS
//----------------------------------------------------------
JavaRTI::JavaRTI()
{
	// do some basic setup
	this->jnienv      = NULL;
	this->jproxyClass = NULL;
	this->jproxy      = NULL;
	this->id          = ++rtiCounter;

	// get a name for the logger
	char name[16];
	sprintf( name, "rti-%d", this->id );
	this->logger = new Logger( name );

	// initialize the exception stuff
	this->eName = NULL;
	this->eReason = NULL;

	// initialize the connection
	this->attachToJVM();
	this->initialize();
	this->cacheMethodIds();
}

JavaRTI::~JavaRTI()
{
	// delete the exception information
	if( this->eName != NULL )
		delete [] this->eName;
	if( this->eReason != NULL )
		delete [] this->eReason;

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
	Runtime::getRuntime()->removeRtiAmbassador( this->id );
}

//----------------------------------------------------------
//                    INSTANCE METHODS
//----------------------------------------------------------
////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// Public Methods //////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
/*
 * Return the current system time in milliseconds, up to the resolution provided by the OS
 */
long JavaRTI::currentTimeMillis()
{
#ifdef WIN32
	LARGE_INTEGER now;
	LARGE_INTEGER freq;
	QueryPerformanceFrequency( &freq );
	QueryPerformanceCounter( &now );
	return (unsigned long)(now.QuadPart / (freq.QuadPart/1000));
#else //#elif defined(__APPLE__)
	timeval time;
	gettimeofday( &time, NULL );
	return (long)((time.tv_sec*1000) + (time.tv_usec/1000));
#endif
}

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
	if( this->eName == NULL )
		return;

	// log the exception information at the INFO level
	this->logger->info( "Exception received: %s", this->eName );
	this->logger->info( "%s", this->eReason );

	// clear the exception information now that it has been handled

	////////////////////////////////////////////////////////////////////
	// The convertAndThrow method will throw an exception if there is //
	// one to be thrown. However, we have to clear the existing info  //
	// so that it isn't confused for an existing exception during the //
	// next check. To cleanAndThrow method will delete the existing   //
	// data, so that takes care of cleanup. We get temporary pointers //
	// to the data here (so that we can pass them to convertAndThrow  //
	// and then set the instance-var pointer to NULL, marking the     //
	// exception as handled.                                          //
	////////////////////////////////////////////////////////////////////
	char *namePtr = this->eName;
	char *reasonPtr = this->eReason;

	// done in convertAndThrow
	//delete [] this->eName;
	//delete [] this->eReason;
	this->eName = NULL;
	this->eReason = NULL;

	// get the exception information and throw it, rely on it to delete the existing data
	ExceptionHacks::cleanAndThrow( namePtr, reasonPtr );
}

/*
 * This method will convert the given tag into a jbytearray. If the given tag is NULL,
 * an empty byte array will be created and returned.
 */
jbyteArray JavaRTI::convertTag( const char *tag )
{
	// if we don't have a tag, just return an empty byte[]
	if( tag == NULL )
		return NULL;

	// create the byte[] and populate it with the tag data
	//   strip the null terminator because it won't be there for Java-federates
	//   we have to manually add and remove it to keep consistent with Java
	jbyteArray jtag = jnienv->NewByteArray( strlen(tag) );
	jnienv->SetByteArrayRegion( jtag, 0, strlen(tag), (jbyte*)tag );
	return jtag;
}

/*
 * This method will convert the values in a AttributeHandleSet into an int array that
 * can then be passed across to the JVM
 */
jintArray JavaRTI::convertAHS( const HLA::AttributeHandleSet& ahs )
{
	// create the array
	jintArray array = jnienv->NewIntArray( ahs.size() );
	jint* content = jnienv->GetIntArrayElements( array, NULL );
	for( HLA::ULong i = 0; i < ahs.size(); i++ )
		content[i] = ahs.getHandle(i);

	jnienv->ReleaseIntArrayElements( array, content, 0 );
	return array;
}

/*
 * This method is much like convertAHS except that it converts an array of attribute handles to
 * a jintArray
 */
jintArray JavaRTI::convertAHA( HLA::AttributeHandle incoming[], HLA::ULong size )
{
	// create the array
	jintArray array = jnienv->NewIntArray( size );
	jint* content = jnienv->GetIntArrayElements( array, NULL );
	for( HLA::ULong i = 0; i < size; i++ )
		content[i] = incoming[i];

	jnienv->ReleaseIntArrayElements( array, content, 0 );
	return array;
}

/*
 * This method will convert the values in a FederateHandleSet into an int array that
 * can then be passed across to the JVM
 */
jintArray JavaRTI::convertFHS( const HLA::FederateHandleSet& fhs )
{
	// create the array
	jintArray array = jnienv->NewIntArray( fhs.size() );
	jint* content = jnienv->GetIntArrayElements( array, NULL );
	for( HLA::ULong i = 0; i < fhs.size(); i++ )
		content[i] = fhs.getHandle(i);

	jnienv->ReleaseIntArrayElements( array, content, 0 );
	return array;
}

/*
 * This method will serialize an AttributeHandleValuePair set in preparation for sending
 * the information across to the java side of the C++ interface. It will create a HVPS
 * struct which contains the handle and value information.
 */
HVPS JavaRTI::convertAHVPS( const HLA::AttributeHandleValuePairSet& attributes )
	throw( HLA::RTIinternalError )
{
	// FIX: Null values for AttributeHandleValuePairSet trip out the serialisation
	//      process. So we'll check for null values first and throw exceptions before
	//      any work is done on them.
	if( &attributes == NULL )
	{
		throw HLA::RTIinternalError( "AttributeHandleValuePairSet was NULL" );
	}

	// create the struct to hold the values
	HVPS hvps = HVPS();
	hvps.handles = jnienv->NewIntArray( attributes.size() );
	hvps.values  = jnienv->NewObjectArray( attributes.size(), BYTE_ARRAY, 0 );

	// get references to the array contents as JNI requires
	jint *handlesContent = jnienv->GetIntArrayElements( hvps.handles, NULL );

	// do the conversion
	for( HLA::ULong i = 0; i < attributes.size(); ++i )
	{
		// get the handle
		handlesContent[i] = attributes.getHandle(i);

		// turn the value into a byte[] and store it
		HLA::ULong valueSize = attributes.getValueLength(i);
		jbyteArray value = jnienv->NewByteArray( valueSize );
		jnienv->SetByteArrayRegion( value,
		                            0,
		                            valueSize,
		                            (jbyte*)attributes.getValuePointer(i,valueSize) );
		jnienv->SetObjectArrayElement( hvps.values, i, value );
		
		// don't forget to release our local reference to this inner byte[]
		jnienv->DeleteLocalRef( value );
	}

	// release the content pointers
	jnienv->ReleaseIntArrayElements( hvps.handles, handlesContent, 0 );

	// return the result
	return hvps;
}

/*
 * This method will serialize an ParameterHandleValuePairSet set in preparation for sending
 * the information across to the java side of the C++ interface. It will create a HVPS
 * struct which contains the handle and value information.
 */
HVPS JavaRTI::convertPHVPS( const HLA::ParameterHandleValuePairSet& parameters )
	throw( HLA::RTIinternalError )
{
	// FIX: Null values for ParameterHandleValuePairSet trip out the serialisation
	//      process. So we'll check for null values first and throw exceptions before
	//      any work is done on them.
	if( &parameters == NULL )
	{
		throw HLA::RTIinternalError( "ParameterHandleValuePairSet was NULL" );
	}

	// create the struct to hold the values
	HVPS hvps = HVPS();
	hvps.handles = jnienv->NewIntArray( parameters.size() );
	hvps.values  = jnienv->NewObjectArray( parameters.size(), BYTE_ARRAY, 0 );

	// get references to the array contents as JNI requires
	jint *handlesContent = jnienv->GetIntArrayElements( hvps.handles, NULL );

	// do the conversion
	for( HLA::ULong i = 0; i < parameters.size(); ++i )
	{
		// get the handle
		handlesContent[i] = parameters.getHandle(i);

		// turn the value into a byte[] and store it
		HLA::ULong valueSize = parameters.getValueLength(i);
		jbyteArray value = jnienv->NewByteArray( valueSize );
		jnienv->SetByteArrayRegion( value,
		                            0,
		                            valueSize,
		                            (jbyte*)parameters.getValuePointer(i,valueSize) );
		jnienv->SetObjectArrayElement( hvps.values, i, value );

		// don't forget to release our local reference to this inner byte[]
		jnienv->DeleteLocalRef( value );
	}

	// release the content pointers
	jnienv->ReleaseIntArrayElements( hvps.handles, handlesContent, 0 );

	// return the result
	return hvps;
}

/*
 * This method will convert the given time into a jdouble that can be sent to the
 * Java side of the interface
 */
jdouble JavaRTI::convertTime( const HLA::FedTime& time )
{
	RTIfedTime *ngtime = (RTIfedTime*)&time;
	return ngtime->getTime();
}

/*
 * This method will take the given double and push it into the memory provided as
 * the second argument. It will turn the double into an RTIfedTime to do this.
 */
void JavaRTI::pushTime( jdouble time, HLA::FedTime& fedtime )
{
	RTIfedTime *ngtime = (RTIfedTime*)&fedtime;
	*ngtime = time;
}

/*
 * Takes a java string and converts it to a userspace string, returning a pointer
 * to it. Also note that this method will release the reference to the jstring that
 * is being passed in.
 *
 * MEMORY MANAGEMENT: The caller of this method is responsible for cleaning up the memory
 */
char* JavaRTI::convertAndReleaseJString( jstring string )
{
	const char *javaString = jnienv->GetStringUTFChars( string, NULL );
	char *userString = new char[strlen(javaString)+1];
	strcpy( userString, javaString );

	// release the java resources
	jnienv->ReleaseStringUTFChars( string, javaString );
	jnienv->DeleteLocalRef( string );

	// return the userspace string
	return userString;
}

/*
 * This methods converts the given jbyteArray into a char*. The general use for this
 * method is to convert data that was passed to the c++ side of the bindings as a tag.
 * If the tag is NULL, no conversion attempt will be made and NULL will be returned.
 *
 * MEMORY MANAGEMENT NOTE: The caller is responsible for cleaning up the returned memory.
 */
char* JavaRTI::convertJTag( jbyteArray tag )
{
	// check for null
	if( tag == NULL )
		return NULL;

	// convert the tag
	//   we assume that there is no null terminator
	jsize length = jnienv->GetArrayLength( tag );
	jbyte *buffer = new jbyte[length+1];
	jnienv->GetByteArrayRegion( tag, 0, length, buffer );
	buffer[length] = '\0';
	return (char*)buffer;
}

/*
 * This method converts the given RTI::Region instance into an RegionImpl. From that
 * implementation it fetches the underlying jobject pointing to the region on the java
 * side of the binding. This jobject can be passed in JNI calls to identify a region.
 */
jobject JavaRTI::convertRegion( const HLA::Region &region )
{
	portico13::Region &regionImpl = (portico13::Region&)region;
	return regionImpl.getRegionProxy();
}

/*
 * This method will convert an array of region pointers to a jobjectArray that can be passed
 * over the JNI to the Java RTIambassador. Ut will use JavaRTI::convertRegion() to convert each
 * individual region instance.
 */
jobjectArray JavaRTI::convertRegions( HLA::Region *theRegions[], HLA::ULong size )
{
	// create the array
	jobjectArray array = jnienv->NewObjectArray( size, jregionClass, NULL );
	for( HLA::ULong i = 0; i < size; i++ )
	{
		jnienv->SetObjectArrayElement( array, i, convertRegion(*theRegions[i]) );
	}

	return array;
}

/*
 * This method is called by the ExceptionManager (which is in turn called from java).
 * It is called when an exception relating to a particular instance of the JavaRTI
 * class has triggered an exception. The name of the exception class, along with the
 * reason is provided with the exception. The reason will generally include the java
 * stack-trace to provide additional debugging help.
 *
 * NOTE: The exceptionCheck() method will clear the exception. If there is still a
 *       pending exception when a new one is being pushed, the information will be
 *       logged at the ERROR level.
 */
void JavaRTI::pushException( char *exceptionName, char *reason )
{
	// check to see if there is already a pending exception
	if( this->eName != NULL )
	{
		logger->warn( "WARNING: Exception pending (%s)!!", this->eName );
	}

	// clear the current exception information
	if( this->eName != NULL )
		delete [] this->eName;

	if( this->eReason != NULL )
		delete [] this->eReason;

	// store the new information
	this->eName = exceptionName;
	this->eReason = reason;
}


/*
 * Converts the provided jintArray into an AttributeHandle set. Freeing the handle set memory
 * is the responsibility of the caller.
 */
HLA::AttributeHandleSet* JavaRTI::convertToAHS( jintArray array )
{
	// create the AHS
	int size = jnienv->GetArrayLength( array );
	HLA::AttributeHandleSet *handleSet = HLA::AttributeHandleSetFactory::create(size);
	
	// put the values into the AHS
	jint *content = jnienv->GetIntArrayElements( array, NULL );
	for( int i = 0; i < size; i++ )
		handleSet->add( content[i] );
	
	jnienv->ReleaseIntArrayElements( array, content, 0 );
	return handleSet;
}

////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////// Private Methods //////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
/*
 * This method will attach the current JavaRTI to the JVM, storing a reference
 * to the JNIEnv in the instance variable. If there is an error during this
 * process, an RTIinternalError will be thrown.
 */
void JavaRTI::attachToJVM() throw( HLA::RTIinternalError )
{
	logger->debug( "Attaching to JVM" );
	JavaVMInitArgs vmArgs;
	JNI_GetDefaultJavaVMInitArgs( &vmArgs );
	jint result = Runtime::getRuntime()->jvm->AttachCurrentThread( (void**)&jnienv, &vmArgs );
	if( result == 0 )
	{
		logger->info( "Attached to JVM" );
	}
	else
	{
		logger->fatal( "Couldn't attach to JVM" );
		throw HLA::RTIinternalError( "couldn't attach to JVM" );
	}
}

/*
 * This method will call DetachCurrentThread in an effort to remove any thread
 * associated with this JavaRTI from the JVM. This should only ever be called
 * as the JavaRTI is being destructed.
 */
void JavaRTI::detachFromJVM()
{
	logger->debug( "Detaching from JVM" );
	jint result = Runtime::getRuntime()->jvm->DetachCurrentThread();
	if( result == 0 )
		logger->info( "Detached from JVM" );
	else
		logger->fatal( "Couldn't detach from JVM" );
}

/*
 * This method will pre-fetch all the necessary JNI ID's. It expects there to be
 * a valid JNIEnv in this->jnienv (so attachToJVM() should have been called first.
 *
 * This method will also create an instance of the java proxy class through which
 * RTIambassador calls will be routed.
 */
void JavaRTI::initialize() throw( HLA::RTIinternalError )
{
	//////////////////////////////////////////////////////////////
	// get the proxy class id data and create an instance of it //
	//////////////////////////////////////////////////////////////
	// find the Region class //
	this->jregionClass = jnienv->FindClass( "org/portico/impl/hla13/types/HLA13Region" );
	if( jregionClass == NULL )
	{
		logger->fatal( "Can't locate: org.portico.impl.hla13.types.HLA13Region" );
		exceptionCheck();
		throw HLA::RTIinternalError( "Can't locate: org.portico.impl.hla13.types.HLA13Region" );
	}

	// find the ProxyRtiAmbassador class //
	this->jproxyClass = jnienv->FindClass( "org/portico/impl/cpp13/ProxyRtiAmbassador" );
	if( jproxyClass == NULL )
	{
		logger->fatal( "Can't locate: org.portico.impl.cpp13.ProxyRtiAmbassador" );
		exceptionCheck();
		throw HLA::RTIinternalError( "Can't locate: org.portico.impl.cpp13.ProxyRtiAmbassador" );
	}

	// find the method id of the constructor //
	jmethodID constructor;
	constructor = jnienv->GetMethodID( jproxyClass, "<init>", "(I)V" );
	if( constructor == NULL )
	{
		logger->fatal( "Can't locate ProxyRtiAmbassador() constructor ID" );
		exceptionCheck();
		throw HLA::RTIinternalError( "Can't locate ProxyRtiAmbassador() constructor ID" );
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
		throw HLA::RTIinternalError( "Exception during ProxyRtiAmbassador() constructor" );
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

/*
 * This will cache an individual method id. It will find the method of the given name
 * and signature and place it into the provided pointer. If there is a problem finding
 * the method, an exception is thrown.
 */
void JavaRTI::cacheMethod( jmethodID *handle, 
                           const char *method, 
                           const char *signature )
	throw( HLA::RTIinternalError )
{
	logger->noisy( "Caching %s [%s]", method, signature );

	// get the method and store it
	*handle = jnienv->GetMethodID( jproxyClass, method, signature );
	if( *handle == NULL )
	{
		char *message = new char[1024];
		sprintf( message, "Could not find %s [%s]", method, signature );
		logger->error( message );
		exceptionCheck();
		throw HLA::RTIinternalError( message );
		delete [] message; // our Exception implementation takes a copy
	}
}

/*
 * The same as the cacheMethod(jmethodId,char*,char*) function, except that it also takes the
 * class on which the method should be located (where the other method will look for the id on
 * the ProxyRtiAmbassador class)
 */
void JavaRTI::cacheMethod( jmethodID *handle,
                           jclass clazz, 
                           const char *method, 
                           const char *signature )
	throw( HLA::RTIinternalError )
{
	logger->noisy( "Caching %s [%s]", method, signature );

	// get the method and store it
	*handle = jnienv->GetMethodID( clazz, method, signature );
	if( *handle == NULL )
	{
		char *message = new char[1024];
		sprintf( message, "Could not find %s [%s]", method, signature );
		logger->error( message );
		exceptionCheck();
		throw HLA::RTIinternalError( message );
		delete [] message; // our Exception implementation takes a copy
	}
}

/*
 * This method will go through all of the methods that we will use and cache up their IDs.
 * These are needed when we actually call the methods, so rather than get them on the fly,
 * we cache up all the values before hand.
 */
void JavaRTI::cacheMethodIds() throw( HLA::RTIinternalError )
{
	logger->trace( "Caching RTIambassador method ids..." );

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

	cacheMethod( &REGION_GET_NUMBER_OF_EXTENTS, jregionClass, "getNumberOfExtents", "()J" );
	cacheMethod( &REGION_GET_RANGE_LOWER_BOUND, jregionClass, "getRangeLowerBound", "(II)J" );
	cacheMethod( &REGION_GET_RANGE_UPPER_BOUND, jregionClass, "getRangeUpperBound", "(II)J" );
	cacheMethod( &REGION_GET_SPACE_HANDLE, jregionClass, "getSpaceHandle", "()I" );
	cacheMethod( &REGION_SET_RANGE_LOWER_BOUND, jregionClass, "setRangeLowerBound", "(IIJ)V" );
	cacheMethod( &REGION_SET_RANGE_UPPER_BOUND, jregionClass, "setRangeUpperBound", "(IIJ)V" );
	cacheMethod( &REGION_GET_RANGE_UPPER_BOUND_NOTIFICATION_LIMIT, jregionClass,
	             "getRangeUpperBoundNotificationLimit", "(II)J" );
	cacheMethod( &REGION_GET_RANGE_LOWER_BOUND_NOTIFICATION_LIMIT, jregionClass,
	             "getRangeLowerBoundNotificationLimit", "(II)J" );

	logger->trace( "Cached Region method ids" );
}
//----------------------------------------------------------
//                     STATIC METHODS
//----------------------------------------------------------

PORTICO13_NS_END
