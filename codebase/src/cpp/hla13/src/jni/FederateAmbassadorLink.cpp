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
#include "common.h"
#include "org_portico_impl_cpp13_FederateAmbassadorLink.h"
#include "Runtime.h"
#include "JavaRTI.h"
#include "types/AttributeHandleValuePairSet.h"
#include "types/ParameterHandleValuePairSet.h"

           ////////////////////////////////////////////////////////////////////////////
           // IMPLEMENTATION NOTES:                                                  //
           //                                                                        //
           //  This class is the C implementation that the Java FederateAmbassador   //
           //  calls into. This is C code, so each of the methods here are just a    //
           //  collection of separate functions.                                     //
           //                                                                        //
           //  Each incoming call from the Java side provided with it an ID that is  //
           //  used to locate the FederateAmbassador instance it is meant to deal    //
           //  with. The getRTI(int) method will return a pointer to the appropriate //
           //  JavaRTI instance through which all calls can be passed back.          //
           //                                                                        //
           ////////////////////////////////////////////////////////////////////////////

/*
 * Helper method to fetch the JavaRTI associated with the given id. If it doesn't exist,
 * an error message will be printed to screen and NULL will be returned. Otherwise, a
 * pointer to the JavaRTI instance will be returned.
 */
portico13::JavaRTI* getRTI( int id )
{
	// get access to the link that the exception belongs to
	portico13::JavaRTI *theRTI = portico13::Runtime::getRuntime()->getRtiAmbassador( id );
	if( theRTI == NULL )
	{
		printf( "ERROR [fedamb] Received callback for unknown federate (id=%d)\n", id );
	}
	
	return theRTI;
}

/**
 * Converts an incoming java int[] into an AttributeHandleSet that can be returned to the
 * user space FederateAmbassador
 */
HLA::AttributeHandleSet* toAHS( JNIEnv *env, jintArray handles )
{
	// create the store
	jsize size = env->GetArrayLength( handles );
	HLA::AttributeHandleSet *theSet = HLA::AttributeHandleSetFactory::create( size );
	
	// populate the set
	jint *content = env->GetIntArrayElements( handles, NULL );
	for( int i = 0; i < size; ++i )
	{
		theSet->add( content[i] );
	}
	
	env->ReleaseIntArrayElements( handles, content, 0 );
	return theSet;
}

/**
 * Converts an incoming int[] (representing attribute handles) and byte[][] (representing attribute
 * values) into an AttributeHandleValuePairSet that can be returned to the FederateAmbassador.
 */
HLA::AttributeHandleValuePairSet* toAHVPS( JNIEnv *env, jintArray handles, jobjectArray values )
{
	// create the set
	jsize size = env->GetArrayLength( handles );
	portico13::AttributeHandleValuePairSet *theSet = new portico13::AttributeHandleValuePairSet( size );
	
	// populate the set
	jint *content = env->GetIntArrayElements( handles, NULL );
	for( int i = 0; i < size; ++i )
	{
		// get the value
		jbyteArray valueArray = (jbyteArray)env->GetObjectArrayElement( values, i );
		jsize valueSize       = env->GetArrayLength( valueArray );
		jbyte *valueBuffer    = new jbyte[valueSize];
		env->GetByteArrayRegion( valueArray, 0, valueSize, valueBuffer );
		
		// store the handle and value, but tell the set NOT to copy the data, it can have
		// it and take responsibility for it, we won't delete it
		theSet->addButDontCopy( content[i], (char*)valueBuffer, valueSize );
		//delete [] valueBuffer; --naughty!
	}
	
	env->ReleaseIntArrayElements( handles, content, 0 );
	return theSet;
}

/**
 * Converts an incoming int[] (representing attribute handles) and byte[][] (representing attribute
 * values) into an ParameterHandleValuePairSet that can be returned to the FederateAmbassador.
 */
HLA::ParameterHandleValuePairSet* toPHVPS( JNIEnv *env, jintArray handles, jobjectArray values )
{
	// create the set
	jsize size = env->GetArrayLength( handles );
	portico13::ParameterHandleValuePairSet *theSet = new portico13::ParameterHandleValuePairSet( size );
	
	// populate the set
	jint *content = env->GetIntArrayElements( handles, NULL );
	for( int i = 0; i < size; ++i )
	{
		// get the value
		jbyteArray valueArray = (jbyteArray)env->GetObjectArrayElement( values, i );
		jsize valueSize       = env->GetArrayLength( valueArray );
		jbyte *valueBuffer    = new jbyte[valueSize];
		env->GetByteArrayRegion( valueArray, 0, valueSize, valueBuffer );
		
		// store the handle and value
		theSet->addButDontCopy( content[i], (char*)valueBuffer, valueSize );
		//delete [] valueBuffer; --naughty as we're calling addButDontCopy!
	}
	
	env->ReleaseIntArrayElements( handles, content, 0 );
	return theSet;
}

//////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// Federation Management Services /////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    synchronizationPointRegistrationFailed
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_synchronizationPointRegistrationFailed(
		JNIEnv *env, 
		jobject jfedamb,
		jint fedid,
		jstring label )
{
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	char *theLabel = javarti->convertAndReleaseJString( label );
	javarti->fedamb->synchronizationPointRegistrationFailed( theLabel );
	
	// clean up
	delete [] theLabel;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    synchronizationPointRegistrationSucceeded
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_synchronizationPointRegistrationSucceeded(
		JNIEnv *env, 
		jobject jfedamb,
		jint fedid,
		jstring label )
{
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	char *theLabel = javarti->convertAndReleaseJString( label );
	javarti->fedamb->synchronizationPointRegistrationSucceeded( theLabel );
	
	// clean up
	delete [] theLabel;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    announceSynchronizationPoint
 * Signature: (ILjava/lang/String;[B)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_announceSynchronizationPoint(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jstring label, 
		jbyteArray tag )
{
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	char *theLabel = javarti->convertAndReleaseJString( label );
	char *theTag   = javarti->convertJTag( tag );
	
	javarti->fedamb->announceSynchronizationPoint( theLabel, theTag );
	
	// clean up
	delete [] theLabel;
	if( theTag != NULL )
		delete [] theTag;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    federationSynchronized
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_federationSynchronized(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jstring label )
{
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	char *theLabel = javarti->convertAndReleaseJString( label );
	javarti->fedamb->federationSynchronized( theLabel );
	
	// clean up
	delete [] theLabel;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    initiateFederateSave
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_initiateFederateSave(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jstring label )
{
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	char *theLabel = javarti->convertAndReleaseJString( label );
	javarti->fedamb->initiateFederateSave( theLabel );
	
	// clean up
	delete [] theLabel;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    federationSaved
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_federationSaved(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid )
{
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->federationSaved();
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    federationNotSaved
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_federationNotSaved(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid )
{
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->federationNotSaved();
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    requestFederationRestoreSucceeded
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_requestFederationRestoreSucceeded(
		JNIEnv *env,
		jobject jfedamb, 
		jint fedid, 
		jstring label )
{
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	char *theLabel = javarti->convertAndReleaseJString( label );
	javarti->fedamb->requestFederationRestoreSucceeded( theLabel );
	
	// clean up
	delete [] theLabel;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    requestFederationRestoreFailed
 * Signature: (ILjava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_requestFederationRestoreFailed(
		JNIEnv *env, 
		jobject jfedamb,
		jint fedid,
		jstring label, 
		jstring reason )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	char *theLabel = javarti->convertAndReleaseJString( label );
	char *theReason = javarti->convertAndReleaseJString( reason );
	javarti->fedamb->requestFederationRestoreFailed( theLabel, theReason );
	
	// clean up
	delete [] theLabel;
	delete [] theReason;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    federationRestoreBegun
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_federationRestoreBegun(
		JNIEnv *env,
		jobject jfedamb,
		jint fedid )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->federationRestoreBegun();
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    initiateFederateRestore
 * Signature: (ILjava/lang/String;I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_initiateFederateRestore(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jstring label, 
		jint federateHandle )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	char *theLabel = javarti->convertAndReleaseJString( label );
	javarti->fedamb->initiateFederateRestore( theLabel, federateHandle );
	
	// clean up
	delete [] theLabel;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    federationRestored
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_federationRestored(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->federationRestored();
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    federationNotRestored
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_federationNotRestored(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->federationNotRestored();
}

//////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// Declaration Management Services ////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    startRegistrationForObjectClass
 * Signature: (II)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_startRegistrationForObjectClass(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jint theClass )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->startRegistrationForObjectClass( theClass );
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    stopRegistrationForObjectClass
 * Signature: (II)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_stopRegistrationForObjectClass(
		JNIEnv *env,
		jobject jfedamb,
		jint fedid,
		jint theClass )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->stopRegistrationForObjectClass( theClass );
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    turnInteractionsOn
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_org_portico_impl_cpp13_FederateAmbassadorLink_turnInteractionsOn(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jint theClass )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->turnInteractionsOn( theClass );
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    turnInteractionsOff
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_org_portico_impl_cpp13_FederateAmbassadorLink_turnInteractionsOff(
		JNIEnv *env,
		jobject jfedamb, 
		jint fedid, 
		jint theClass )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->turnInteractionsOff( theClass );
}

//////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Object Management Services ///////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    discoverObjectInstance
 * Signature: (IIILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_discoverObjectInstance(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jint theObject,
		jint theClass, 
		jstring objectName )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	char *theName = javarti->convertAndReleaseJString( objectName );
	javarti->fedamb->discoverObjectInstance( theObject, theClass, theName );
	
	// clean up
	delete [] theName;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    reflectAttributeValues
 * Signature: (II[I[[B[B)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_reflectAttributeValues__II_3I_3_3B_3B(
		JNIEnv *env,
		jobject jfedamb,
		jint fedid,
		jint theObject,
		jintArray handles,
		jobjectArray values,
		jbyteArray tag )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	char *theTag = javarti->convertJTag( tag );
	HLA::AttributeHandleValuePairSet *attributes = toAHVPS( env, handles, values );

	javarti->fedamb->reflectAttributeValues( theObject, *attributes, theTag );
	
	// clean up
	delete attributes;
	if( theTag != NULL )
		delete [] theTag;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    reflectAttributeValues
 * Signature: (II[I[[B[BDI)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_reflectAttributeValues__II_3I_3_3B_3BDI(
		JNIEnv *env,
		jobject jfedamb,
		jint fedid, 
		jint theObject, 
		jintArray handles,
		jobjectArray values, 
		jbyteArray tag, 
		jdouble time, 
		jint erh )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	char *theTag = javarti->convertJTag( tag );
	HLA::AttributeHandleValuePairSet *attributes = toAHVPS( env, handles, values );
	HLA::FedTime *fedtime = new RTIfedTime( time );

	javarti->fedamb->reflectAttributeValues( theObject,
	                                         *attributes,
	                                         *fedtime,
	                                         theTag,
	                                         HLA::EventRetractionHandle() );
	
	// clean up
	delete attributes;
	delete fedtime;
	if( theTag != NULL )
		delete [] theTag;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    receiveInteraction
 * Signature: (II[I[[B[B)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_receiveInteraction__II_3I_3_3B_3B(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jint theClass, 
		jintArray handles, 
		jobjectArray values, 
		jbyteArray tag )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	char *theTag = javarti->convertJTag( tag );
	HLA::ParameterHandleValuePairSet *parameters = toPHVPS( env, handles, values );

	javarti->fedamb->receiveInteraction( theClass, *parameters, theTag );

	// clean up
	delete parameters;
	if( theTag != NULL )
		delete [] theTag;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    receiveInteraction
 * Signature: (II[I[[B[BDI)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_receiveInteraction__II_3I_3_3B_3BDI(
		JNIEnv *env,
		jobject jfedamb,
		jint fedid,
		jint theClass,
		jintArray handles,
		jobjectArray values,
		jbyteArray tag,
		jdouble time,
		jint erh )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	char *theTag = javarti->convertJTag( tag );
	HLA::ParameterHandleValuePairSet *parameters = toPHVPS( env, handles, values );
	HLA::FedTime *fedtime = new RTIfedTime( time );

	javarti->fedamb->receiveInteraction( theClass,
	                                     *parameters,
	                                     *fedtime,
	                                     theTag,
	                                     HLA::EventRetractionHandle() );
	
	// clean up
	delete parameters;
	delete fedtime;
	if( theTag != NULL )
		delete [] theTag;

}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    removeObjectInstance
 * Signature: (II[B)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_removeObjectInstance__II_3B(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jint theObject, 
		jbyteArray tag )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	char *theTag = javarti->convertJTag( tag );
	
	javarti->fedamb->removeObjectInstance( theObject, theTag );
	
	// clean up
	if( theTag != NULL )
		delete [] theTag;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    removeObjectInstance
 * Signature: (II[BDI)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_removeObjectInstance__II_3BDI(
		JNIEnv *env,
		jobject jfedamb, 
		jint fedid, 
		jint theObject, 
		jbyteArray tag,
		jdouble time, 
		jint erh )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	char *theTag = javarti->convertJTag( tag );
	const HLA::FedTime* fedtime = new RTIfedTime( time );

	javarti->fedamb->removeObjectInstance( theObject, *fedtime, theTag, HLA::EventRetractionHandle() );
	
	// clean up
	delete fedtime;
	if( theTag != NULL )
		delete [] theTag;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    attributesInScope
 * Signature: (II[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_theAttributesInScope(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jint theObject, 
		jintArray theAttributes )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	HLA::AttributeHandleSet *attributes = toAHS( env, theAttributes );
	javarti->fedamb->attributesInScope( theObject, *attributes );
	// clean up
	delete attributes;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    attributesOutOfScope
 * Signature: (II[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_theAttributesOutOfScope(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jint theObject, 
		jintArray theAttributes )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	HLA::AttributeHandleSet *attributes = toAHS( env, theAttributes );
	javarti->fedamb->attributesOutOfScope( theObject, *attributes );
	// clean up
	delete attributes;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    provideAttributeValueUpdate
 * Signature: (II[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_provideAttributeValueUpdate(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jint theObject,
		jintArray theAttributes )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	HLA::AttributeHandleSet *attributes = toAHS( env, theAttributes );
	javarti->fedamb->provideAttributeValueUpdate( theObject, *attributes );
	// clean up
	delete attributes;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    turnUpdatesOnForObjectInstance
 * Signature: (II[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_turnUpdatesOnForObjectInstance(
		JNIEnv *env,
		jobject jfedamb, 
		jint fedid,
		jint theObject, 
		jintArray theAttributes )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	HLA::AttributeHandleSet *attributes = toAHS( env, theAttributes );
	javarti->fedamb->turnUpdatesOnForObjectInstance( theObject, *attributes );
	// clean up
	delete attributes;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    turnUpdatesOffForObjectInstance
 * Signature: (II[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_turnUpdatesOffForObjectInstance(
		JNIEnv *env,
		jobject jfedamb, 
		jint fedid, 
		jint theObject, 
		jintArray theAttributes )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	HLA::AttributeHandleSet *attributes = toAHS( env, theAttributes );
	javarti->fedamb->turnUpdatesOffForObjectInstance( theObject, *attributes );
	// clean up
	delete attributes;
}

//////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////// Ownership Management Services /////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    requestAttributeOwnershipAssumption
 * Signature: (II[I[B)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_requestAttributeOwnershipAssumption(
		JNIEnv *env,
		jobject jfedamb, 
		jint fedid, 
		jint theObject, 
		jintArray theAttributes,
		jbyteArray tag )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	HLA::AttributeHandleSet *attributes = toAHS( env, theAttributes );
	char *theTag = javarti->convertJTag( tag );
	
	javarti->fedamb->requestAttributeOwnershipAssumption( theObject, *attributes, theTag );
	
	// clean up
	delete attributes;
	if( theTag != NULL )
		delete [] theTag;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    attributeOwnershipDivestitureNotification
 * Signature: (II[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_attributeOwnershipDivestitureNotification(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jint theObject, 
		jintArray theAttributes )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	HLA::AttributeHandleSet *attributes = toAHS( env, theAttributes );
	javarti->fedamb->attributeOwnershipDivestitureNotification( theObject, *attributes );
	
	// clean up
	delete attributes;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    attributeOwnershipAcquisitionNotification
 * Signature: (II[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_attributeOwnershipAcquisitionNotification(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jint theObject, 
		jintArray theAttributes )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	HLA::AttributeHandleSet *attributes = toAHS( env, theAttributes );
	javarti->fedamb->attributeOwnershipAcquisitionNotification( theObject, *attributes );
	
	// clean up
	delete attributes;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    attributeOwnershipUnavailable
 * Signature: (II[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_attributeOwnershipUnavailable(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jint theObject, 
		jintArray theAttributes )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	HLA::AttributeHandleSet *attributes = toAHS( env, theAttributes );
	javarti->fedamb->attributeOwnershipUnavailable( theObject, *attributes );
	
	// clean up
	delete attributes;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    requestAttributeOwnershipRelease
 * Signature: (II[I[B)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_requestAttributeOwnershipRelease(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jint theObject, 
		jintArray theAttributes, 
		jbyteArray tag )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	HLA::AttributeHandleSet *attributes = toAHS( env, theAttributes );
	char *theTag = javarti->convertJTag( tag );
	
	javarti->fedamb->requestAttributeOwnershipRelease( theObject, *attributes, theTag );
	
	// clean up
	delete attributes;
	if( theTag != NULL )
		delete [] theTag;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    confirmAttributeOwnershipAcquisitionCancellation
 * Signature: (II[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_confirmAttributeOwnershipAcquisitionCancellation(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jint theObject, 
		jintArray theAttributes )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	HLA::AttributeHandleSet *attributes = toAHS( env, theAttributes );
	javarti->fedamb->confirmAttributeOwnershipAcquisitionCancellation( theObject, *attributes );
	
	// clean up
	delete attributes;
}


/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    informAttributeOwnership
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_informAttributeOwnership(
		JNIEnv *env,
		jobject jfedamb,
		jint fedid, 
		jint theObject,
		jint attribute, 
		jint owner )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->informAttributeOwnership( theObject, attribute, owner );
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    attributeIsNotOwned
 * Signature: (III)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_attributeIsNotOwned(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jint theObject, 
		jint attribute )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->attributeIsNotOwned( theObject, attribute );
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    attributeOwnedByRTI
 * Signature: (III)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_attributeOwnedByRTI(
		JNIEnv *env,
		jobject jfedamb, 
		jint fedid, 
		jint theObject, 
		jint attribute )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->attributeOwnedByRTI( theObject, attribute );
}

//////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////// Time Management Services ////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    timeRegulationEnabled
 * Signature: (ID)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_timeRegulationEnabled(
		JNIEnv *env, 
		jobject jfedamb, 
		jint fedid, 
		jdouble theTime )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	const HLA::FedTime* fedtime = new RTIfedTime( theTime );
	javarti->fedamb->timeRegulationEnabled( *fedtime );
	delete fedtime;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    timeConstrainedEnabled
 * Signature: (ID)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_timeConstrainedEnabled(
		JNIEnv *env,
		jobject jfedamb, 
		jint fedid, 
		jdouble theTime )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	const HLA::FedTime* fedtime = new RTIfedTime( theTime );
	javarti->fedamb->timeConstrainedEnabled( *fedtime );
	delete fedtime;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    timeAdvanceGrant
 * Signature: (ID)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_timeAdvanceGrant(
		JNIEnv *env,
		jobject jfedamb, 
		jint fedid, 
		jdouble theTime )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	const HLA::FedTime* fedtime = new RTIfedTime( theTime );
	javarti->fedamb->timeAdvanceGrant( *fedtime );
	delete fedtime;
}

/*
 * Class:     org_portico_impl_cpp13_FederateAmbassadorLink
 * Method:    requestRetraction
 * Signature: (II)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp13_FederateAmbassadorLink_requestRetraction(
		JNIEnv *env,
		jobject jfedamb,
		jint fedid,
		jint erh )
{
	// get access to the associated JavaRTI instance
	portico13::JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->requestRetraction( HLA::EventRetractionHandle() );
}
