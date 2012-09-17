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
#include "jni/org_portico_impl_cpp1516e_FederateAmbassadorLink.h"
#include "jni/JavaRTI.h"
#include "jni/JniUtils.h"
#include "jni/Runtime.h"

using namespace PORTICO1516E_NS;

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

//------------------------------------------------------------------------------------------
//                                      HELPER METHODS                                       
//------------------------------------------------------------------------------------------
/*
 * Helper method to fetch the JavaRTI associated with the given id. If it doesn't exist,
 * an error message will be printed to screen and NULL will be returned. Otherwise, a
 * pointer to the JavaRTI instance will be returned.
 */
JavaRTI* getRTI( int id )
{
	// get access to the link that the exception belongs to
	JavaRTI *theRTI = Runtime::getRuntime()->getRtiAmbassador( id );
	if( theRTI == NULL )
	{
		printf( "ERROR [fedamb] Received callback for unknown federate (id=%d)\n", id );
	}
	
	return theRTI;
}

//------------------------------------------------------------------------------------------
//                                 IMPLEMENTATION METHODS
//------------------------------------------------------------------------------------------

//////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// Federation Management Services /////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    connectionLost
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_connectionLost( JNIEnv *jnienv,
                                                                      jobject jfedamb,
                                                                      jint fedid,
                                                                      jstring description )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	wstring cDescription = JniUtils::toWideString( jnienv, description );
	javarti->fedamb->connectionLost( cDescription );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    synchronizationPointRegistrationSucceeded
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_synchronizationPointRegistrationSucceeded
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jstring label )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	wstring cLabel = JniUtils::toWideString( jnienv, label );
	javarti->fedamb->synchronizationPointRegistrationSucceeded( cLabel );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    synchronizationPointRegistrationFailed
 * Signature: (ILjava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_synchronizationPointRegistrationFailed
	 ( JNIEnv *jnienv,
	   jobject jfedamb,
	   jint fedid,
	   jstring label,
	   jstring reason )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	wstring cLabel = JniUtils::toWideString( jnienv, label );
	SynchronizationPointFailureReason cReason = JniUtils::toSyncPointFailureReason( jnienv, reason );
	// FIXME cReason could be NULL
	javarti->fedamb->synchronizationPointRegistrationFailed( cLabel, cReason );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    announceSynchronizationPoint
 * Signature: (ILjava/lang/String;[B)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_announceSynchronizationPoint
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jstring label,
	  jbyteArray tag )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	wstring cLabel = JniUtils::toWideString( jnienv, label );
	VariableLengthData cTag = JniUtils::toTag( jnienv, tag );
	javarti->fedamb->announceSynchronizationPoint( cLabel, cTag );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    federationSynchronized
 * Signature: (ILjava/lang/String;[I)V
 */
JNIEXPORT void
JNICALL Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_federationSynchronized
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jstring label,
	  jintArray failedSet )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	wstring cLabel = JniUtils::toWideString( jnienv, label );
	FederateHandleSet fhs = JniUtils::toFederateSet( jnienv, failedSet );
	javarti->fedamb->federationSynchronized( cLabel, fhs );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    initiateFederateSave
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_initiateFederateSave__ILjava_lang_String_2
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jstring label )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	wstring cLabel = JniUtils::toWideString( jnienv, label );
	javarti->fedamb->initiateFederateSave( cLabel );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    initiateFederateSave
 * Signature: (ILjava/lang/String;D)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_initiateFederateSave__ILjava_lang_String_2D
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jstring label,
	  jdouble doubleTime )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	wstring cLabel = JniUtils::toWideString( jnienv, label );
	javarti->fedamb->initiateFederateSave( cLabel, HLAfloat64Time(doubleTime) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    initiateFederateSave
 * Signature: (ILjava/lang/String;J)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_initiateFederateSave__ILjava_lang_String_2J
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jstring label,
	  jlong longTime )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	wstring cLabel = JniUtils::toWideString( jnienv, label );
	javarti->fedamb->initiateFederateSave( cLabel, HLAinteger64Time(longTime) );
}


/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    federationSaved
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_federationSaved
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->federationSaved();
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    federationNotSaved
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_federationNotSaved
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jstring reason )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	SaveFailureReason cReason = JniUtils::toSaveFailureReason( jnienv, reason );
	javarti->fedamb->federationNotSaved( cReason );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    federationSaveStatusResponse
 * Signature: (I[I[Ljava/lang/String;)V
 */
// The SaveStatusPair argument is replaced by two arrays (one for each part)
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_federationSaveStatusResponse
	 ( JNIEnv *jnienv,
	   jobject jfedamb,
	   jint fedid,
	   jintArray federates,
	   jobjectArray statuses )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	FederateHandleSaveStatusPairVector statusVector =
		JniUtils::toSaveStatusPairVector( jnienv, federates, statuses );
	
	javarti->fedamb->federationSaveStatusResponse( statusVector );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    requestFederationRestoreSucceeded
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_requestFederationRestoreSucceeded
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jstring label )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	wstring cLabel = JniUtils::toWideString( jnienv, label );
	javarti->fedamb->requestFederationRestoreSucceeded( cLabel );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    requestFederationRestoreFailed
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_requestFederationRestoreFailed
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jstring label )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	wstring cLabel = JniUtils::toWideString( jnienv, label );
	javarti->fedamb->requestFederationRestoreFailed( cLabel );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    federationRestoreBegun
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_federationRestoreBegun
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->federationRestoreBegun();
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    initiateFederateRestore
 * Signature: (ILjava/lang/String;Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_initiateFederateRestore
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jstring label,
	  jstring federateName,
	  jint federateHandle )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->initiateFederateRestore( JniUtils::toWideString(jnienv,label),
	                                          JniUtils::toWideString(jnienv,federateName),
	                                          JniUtils::toFederateHandle(federateHandle) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    federationRestored
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_federationRestored
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->federationRestored();
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    federationNotRestored
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_federationNotRestored
	 ( JNIEnv *jnienv,
	   jobject jfedamb,
	   jint fedid,
	   jstring reason )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	RestoreFailureReason cReason = JniUtils::toRestoreFailureReason( jnienv, reason );
	javarti->fedamb->federationNotRestored( cReason );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    federationRestoreStatusResponse
 * Signature: (I[I[I[Ljava/lang/String;)V
 */
// FederateRestoreStatus expanded into:
//    -> int[] preHandles
//    -> int[] postHandles
//    -> String[] statuses
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_federationRestoreStatusResponse
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jintArray preHandles,
	  jintArray postHandles,
	  jobjectArray statuses )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	FederateRestoreStatusVector statusVector = JniUtils::toRestoreStatusVector( jnienv,
	                                                                            preHandles,
	                                                                            postHandles,
	                                                                            statuses );

	javarti->fedamb->federationRestoreStatusResponse( statusVector );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    reportFederationExecutions
 * Signature: (I[Ljava/lang/String;[Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_reportFederationExecutions
	( JNIEnv *jnienv,
	  jobject jfedamb, 
	  jint fedid,
	  jobjectArray federations,
	  jobjectArray timeImplementations )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	FederationExecutionInformationVector fedVector =
		JniUtils::toFedInformationVector( jnienv, federations, timeImplementations );

	javarti->fedamb->reportFederationExecutions( fedVector );
}

//////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////// Registration Services //////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    startRegistrationForObjectClass
 * Signature: (II)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_startRegistrationForObjectClass
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint classHandle )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->startRegistrationForObjectClass( JniUtils::toObjectClassHandle(classHandle) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    stopRegistrationForObjectClass
 * Signature: (II)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_stopRegistrationForObjectClass
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint classHandle )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->stopRegistrationForObjectClass( JniUtils::toObjectClassHandle(classHandle) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    turnInteractionsOn
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_turnInteractionsOn
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint classHandle )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->turnInteractionsOn( JniUtils::toInteractionClassHandle(classHandle) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    turnInteractionsOff
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_turnInteractionsOff
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint classHandle )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->turnInteractionsOff( JniUtils::toInteractionClassHandle(classHandle) );
}

//////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Object Management Services ///////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    objectInstanceNameReservationSucceeded
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_objectInstanceNameReservationSucceeded
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jstring objectName )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->objectInstanceNameReservationSucceeded( JniUtils::toWideString(jnienv,objectName) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    multipleObjectInstanceNameReservationSucceeded
 * Signature: (I[Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_multipleObjectInstanceNameReservationSucceeded
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jobjectArray objectNames ) // String[]
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	set<wstring> names = JniUtils::toWideStringSet( jnienv, objectNames );
	javarti->fedamb->multipleObjectInstanceNameReservationSucceeded( names );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    objectInstanceNameReservationFailed
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_objectInstanceNameReservationFailed
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jstring objectName )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->objectInstanceNameReservationFailed( JniUtils::toWideString(jnienv,objectName) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    multipleObjectInstanceNameReservationFailed
 * Signature: (I[Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_multipleObjectInstanceNameReservationFailed
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jobjectArray objectNames )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	set<wstring> names = JniUtils::toWideStringSet( jnienv, objectNames );
	javarti->fedamb->multipleObjectInstanceNameReservationFailed( names );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    discoverObjectInstance
 * Signature: (IIILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_discoverObjectInstance__IIILjava_lang_String_2
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jint objectClass,
	  jstring objectName )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->discoverObjectInstance( JniUtils::toObjectHandle(objectHandle),
	                                         JniUtils::toObjectClassHandle(objectClass),
	                                         JniUtils::toWideString(jnienv,objectName) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    discoverObjectInstance
 * Signature: (IIILjava/lang/String;I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_discoverObjectInstance__IIILjava_lang_String_2I
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jint objectClass,
	  jstring objectName,
	  jint producingFederate )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->discoverObjectInstance( JniUtils::toObjectHandle(objectHandle),
	                                         JniUtils::toObjectClassHandle(objectClass),
	                                         JniUtils::toWideString(jnienv,objectName),
	                                         JniUtils::toFederateHandle(producingFederate) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    reflectAttributeValues
 * Signature: (II[I[[B[BIII[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_reflectAttributeValues__II_3I_3_3B_3BIII_3I
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray attributeHandles,
	  jobjectArray attributeValues,  // byte[][]
	  jbyteArray tag,
	  jint sentOrdering,
	  jint transport,
	  jint producingFederate,
	  jintArray regionHandles )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	// attributes
	AttributeHandleValueMap attributes = JniUtils::toAttributeValueMap( jnienv,
	                                                                    attributeHandles,
	                                                                    attributeValues );

	// additional info
	SupplementalReflectInfo supplemental = JniUtils::toReflectSupplement( jnienv,
	                                                                      producingFederate,
	                                                                      regionHandles );

	// call that thang
	javarti->fedamb->reflectAttributeValues( JniUtils::toObjectHandle(objectHandle),
	                                         attributes,
	                                         JniUtils::toTag(jnienv,tag),
	                                         JniUtils::toOrder(sentOrdering),
	                                         JniUtils::toTransport(transport),
	                                         supplemental );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    reflectAttributeValues
 * Signature: (II[I[[B[BIIDII[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_reflectAttributeValues__II_3I_3_3B_3BIIDJII_3I
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray attributeHandles,
	  jobjectArray attributeValues,   // byte[][]
	  jbyteArray tag,
	  jint sentOrdering,
	  jint transport,
	  jdouble doubleTime,
	  jint receivedOrdering,
	  jint producingFederate,
	  jintArray regionHandles )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	// attributes
	AttributeHandleValueMap attributes = JniUtils::toAttributeValueMap( jnienv,
	                                                                    attributeHandles,
	                                                                    attributeValues );

	// additional info
	SupplementalReflectInfo supplemental = JniUtils::toReflectSupplement( jnienv,
	                                                                      producingFederate,
	                                                                      regionHandles );

	// make the call
	javarti->fedamb->reflectAttributeValues( JniUtils::toObjectHandle(objectHandle),
	                                         attributes,
	                                         JniUtils::toTag(jnienv,tag),
	                                         JniUtils::toOrder(sentOrdering),
	                                         JniUtils::toTransport(transport),
	                                         HLAfloat64Time(doubleTime),
	                                         JniUtils::toOrder(receivedOrdering),
	                                         supplemental );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    reflectAttributeValues
 * Signature: (II[I[[B[BIIJII[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_reflectAttributeValues__II_3I_3_3B_3BIIJII_3I
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray attributeHandles,
	  jobjectArray attributeValues,   // byte[][]
	  jbyteArray tag,
	  jint sentOrdering,
	  jint transport,
	  jlong longTime,
	  jint receivedOrdering,
	  jint producingFederate,
	  jintArray regionHandles )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	// attributes
	AttributeHandleValueMap attributes = JniUtils::toAttributeValueMap( jnienv,
	                                                                    attributeHandles,
	                                                                    attributeValues );

	// additional info
	SupplementalReflectInfo supplemental = JniUtils::toReflectSupplement( jnienv,
	                                                                      producingFederate,
	                                                                      regionHandles );

	// make the call
	javarti->fedamb->reflectAttributeValues( JniUtils::toObjectHandle(objectHandle),
	                                         attributes,
	                                         JniUtils::toTag(jnienv,tag),
	                                         JniUtils::toOrder(sentOrdering),
	                                         JniUtils::toTransport(transport),
	                                         HLAinteger64Time(longTime),
	                                         JniUtils::toOrder(receivedOrdering),
	                                         supplemental );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    reflectAttributeValues
 * Signature: (II[I[[B[BIIDIII[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_reflectAttributeValues__II_3I_3_3B_3BIIDIII_3I
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray attributeHandles,
	  jobjectArray attributeValues,   // byte[][]
	  jbyteArray tag,
	  jint sentOrdering,
	  jint transport,
	  jdouble doubleTime,
	  jint receivedOrdering,
	  jint retractionHandle,
	  jint producingFederate,
	  jintArray regionHandles )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	// attributes
	AttributeHandleValueMap attributes = JniUtils::toAttributeValueMap( jnienv,
	                                                                    attributeHandles,
	                                                                    attributeValues );

	// additional info
	SupplementalReflectInfo supplemental = JniUtils::toReflectSupplement( jnienv,
	                                                                      producingFederate,
	                                                                      regionHandles );

	// make the call
	javarti->fedamb->reflectAttributeValues( JniUtils::toObjectHandle(objectHandle),
	                                         attributes,
	                                         JniUtils::toTag(jnienv,tag),
	                                         JniUtils::toOrder(sentOrdering),
	                                         JniUtils::toTransport(transport),
	                                         HLAfloat64Time(doubleTime),
	                                         JniUtils::toOrder(receivedOrdering),
	                                         JniUtils::toRetractionHandle(retractionHandle),
	                                         supplemental );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    reflectAttributeValues
 * Signature: (II[I[[B[BIIJIII[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_reflectAttributeValues__II_3I_3_3B_3BIIJIII_3I
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray attributeHandles,
	  jobjectArray attributeValues,   // byte[][]
	  jbyteArray tag,
	  jint sentOrdering,
	  jint transport,
	  jlong longTime,
	  jint receivedOrdering,
	  jint retractionHandle,
	  jint producingFederate,
	  jintArray regionHandles )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	// attributes
	AttributeHandleValueMap attributes = JniUtils::toAttributeValueMap( jnienv,
	                                                                    attributeHandles,
	                                                                    attributeValues );

	// additional info
	SupplementalReflectInfo supplemental = JniUtils::toReflectSupplement( jnienv,
	                                                                      producingFederate,
	                                                                      regionHandles );

	// make the call
	javarti->fedamb->reflectAttributeValues( JniUtils::toObjectHandle(objectHandle),
	                                         attributes,
	                                         JniUtils::toTag(jnienv,tag),
	                                         JniUtils::toOrder(sentOrdering),
	                                         JniUtils::toTransport(transport),
	                                         HLAinteger64Time(longTime),
	                                         JniUtils::toOrder(receivedOrdering),
	                                         JniUtils::toRetractionHandle(retractionHandle),
	                                         supplemental );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    receiveInteraction
 * Signature: (II[I[[B[BIII[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_receiveInteraction__II_3I_3_3B_3BIII_3I
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint interactionHandle,
	  jintArray parameterHandles,
	  jobjectArray parameterValues,   // byte[][]
	  jbyteArray tag,
	  jint sentOrdering,
	  jint transport,
	  jint producingFederate,
	  jintArray regionHandles )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	// attributes
	ParameterHandleValueMap parameters = JniUtils::toParameterValueMap( jnienv,
	                                                                    parameterHandles,
	                                                                    parameterValues );

	// additional info
	SupplementalReceiveInfo supplemental = JniUtils::toReceiveSupplement( jnienv,
	                                                                      producingFederate,
	                                                                      regionHandles );

	// call that thang
	javarti->fedamb->receiveInteraction( JniUtils::toInteractionClassHandle(interactionHandle),
	                                     parameters,
	                                     JniUtils::toTag(jnienv,tag),
	                                     JniUtils::toOrder(sentOrdering),
	                                     JniUtils::toTransport(transport),
	                                     supplemental );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    receiveInteraction
 * Signature: (II[I[[B[BIIDII[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_receiveInteraction__II_3I_3_3B_3BIIDII_3I
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint interactionHandle,
	  jintArray parameterHandles,
	  jobjectArray parameterValues,   // byte[][]
	  jbyteArray tag,
	  jint sentOrdering,
	  jint transport,
	  jdouble doubleTime,
	  jint receiveOrdering,
	  jint producingFederate,
	  jintArray regionHandles )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	// attributes
	ParameterHandleValueMap parameters = JniUtils::toParameterValueMap( jnienv,
	                                                                    parameterHandles,
	                                                                    parameterValues );

	// additional info
	SupplementalReceiveInfo supplemental = JniUtils::toReceiveSupplement( jnienv,
	                                                                      producingFederate,
	                                                                      regionHandles );

	// call that thang
	javarti->fedamb->receiveInteraction( JniUtils::toInteractionClassHandle(interactionHandle),
	                                     parameters,
	                                     JniUtils::toTag(jnienv,tag),
	                                     JniUtils::toOrder(sentOrdering),
	                                     JniUtils::toTransport(transport),
	                                     HLAfloat64Time(doubleTime),
	                                     JniUtils::toOrder(receiveOrdering),
	                                     supplemental );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    receiveInteraction
 * Signature: (II[I[[B[BIIJII[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_receiveInteraction__II_3I_3_3B_3BIIJII_3I
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint interactionHandle,
	  jintArray parameterHandles,
	  jobjectArray parameterValues,   // byte[][]
	  jbyteArray tag,
	  jint sentOrdering,
	  jint transport,
	  jlong longTime,
	  jint receiveOrdering,
	  jint producingFederate,
	  jintArray regionHandles )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	// attributes
	ParameterHandleValueMap parameters = JniUtils::toParameterValueMap( jnienv,
	                                                                    parameterHandles,
	                                                                    parameterValues );

	// additional info
	SupplementalReceiveInfo supplemental = JniUtils::toReceiveSupplement( jnienv,
	                                                                      producingFederate,
	                                                                      regionHandles );

	// call that thang
	javarti->fedamb->receiveInteraction( JniUtils::toInteractionClassHandle(interactionHandle),
	                                     parameters,
	                                     JniUtils::toTag(jnienv,tag),
	                                     JniUtils::toOrder(sentOrdering),
	                                     JniUtils::toTransport(transport),
	                                     HLAinteger64Time(longTime),
	                                     JniUtils::toOrder(receiveOrdering),
	                                     supplemental );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    receiveInteraction
 * Signature: (II[I[[B[BIIDIII[I)V
 */
JNIEXPORT void JNICALL Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_receiveInteraction__II_3I_3_3B_3BIIDIII_3I
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint interactionHandle,
	  jintArray parameterHandles,
	  jobjectArray parameterValues,   // byte[][]
	  jbyteArray tag,
	  jint sentOrdering,
	  jint transport,
	  jdouble doubleTime,
	  jint receiveOrdering,
	  jint retractionHandle,
	  jint producingFederate,
	  jintArray regionHandles )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	// attributes
	ParameterHandleValueMap parameters = JniUtils::toParameterValueMap( jnienv,
	                                                                    parameterHandles,
	                                                                    parameterValues );

	// additional info
	SupplementalReceiveInfo supplemental = JniUtils::toReceiveSupplement( jnienv,
	                                                                      producingFederate,
	                                                                      regionHandles );

	// call that thang
	javarti->fedamb->receiveInteraction( JniUtils::toInteractionClassHandle(interactionHandle),
	                                     parameters,
	                                     JniUtils::toTag(jnienv,tag),
	                                     JniUtils::toOrder(sentOrdering),
	                                     JniUtils::toTransport(transport),
	                                     HLAfloat64Time(doubleTime),
	                                     JniUtils::toOrder(receiveOrdering),
	                                     JniUtils::toRetractionHandle(retractionHandle),
	                                     supplemental );
}
/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    receiveInteraction
 * Signature: (II[I[[B[BIIJIII[I)V
 */
JNIEXPORT void JNICALL Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_receiveInteraction__II_3I_3_3B_3BIIJIII_3I
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint interactionHandle,
	  jintArray parameterHandles,
	  jobjectArray parameterValues,   // byte[][]
	  jbyteArray tag,
	  jint sentOrdering,
	  jint transport,
	  jlong longTime,
	  jint receiveOrdering,
	  jint retractionHandle,
	  jint producingFederate,
	  jintArray regionHandles )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	// attributes
	ParameterHandleValueMap parameters = JniUtils::toParameterValueMap( jnienv,
	                                                                    parameterHandles,
	                                                                    parameterValues );

	// additional info
	SupplementalReceiveInfo supplemental = JniUtils::toReceiveSupplement( jnienv,
	                                                                      producingFederate,
	                                                                      regionHandles );

	// call that thang
	javarti->fedamb->receiveInteraction( JniUtils::toInteractionClassHandle(interactionHandle),
	                                     parameters,
	                                     JniUtils::toTag(jnienv,tag),
	                                     JniUtils::toOrder(sentOrdering),
	                                     JniUtils::toTransport(transport),
	                                     HLAinteger64Time(longTime),
	                                     JniUtils::toOrder(receiveOrdering),
	                                     JniUtils::toRetractionHandle(retractionHandle),
	                                     supplemental );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    removeObjectInstance
 * Signature: (II[BII)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_removeObjectInstance__II_3BII
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jbyteArray tag,
	  jint sentOrdering,
	  jint producingFederate )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->removeObjectInstance( JniUtils::toObjectHandle(objectHandle),
	                                       JniUtils::toTag(jnienv,tag),
	                                       JniUtils::toOrder(sentOrdering),
	                                       JniUtils::toRemoveSupplement(producingFederate) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    removeObjectInstance
 * Signature: (II[BIDII)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_removeObjectInstance__II_3BIDII
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jbyteArray tag,
	  jint sentOrdering,
	  jdouble doubleTime,
	  jint receiveOrdering,
	  jint producingFederate )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->removeObjectInstance( JniUtils::toObjectHandle(objectHandle),
	                                       JniUtils::toTag(jnienv,tag),
	                                       JniUtils::toOrder(sentOrdering),
	                                       HLAfloat64Time(doubleTime),
	                                       JniUtils::toOrder(receiveOrdering),
	                                       JniUtils::toRemoveSupplement(producingFederate) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    removeObjectInstance
 * Signature: (II[BIJII)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_removeObjectInstance__II_3BIJII
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jbyteArray tag,
	  jint sentOrdering,
	  jlong longTime,
	  jint receiveOrdering,
	  jint producingFederate )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->removeObjectInstance( JniUtils::toObjectHandle(objectHandle),
	                                       JniUtils::toTag(jnienv,tag),
	                                       JniUtils::toOrder(sentOrdering),
	                                       HLAinteger64Time(longTime),
	                                       JniUtils::toOrder(receiveOrdering),
	                                       JniUtils::toRemoveSupplement(producingFederate) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    removeObjectInstance
 * Signature: (II[BIDIII)V
 */
JNIEXPORT void JNICALL 
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_removeObjectInstance__II_3BIDIII
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jbyteArray tag,
	  jint sentOrdering,
	  jdouble doubleTime,
	  jint receiveOrdering,
	  jint retractionHandle,
	  jint producingFederate )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->removeObjectInstance( JniUtils::toObjectHandle(objectHandle),
	                                       JniUtils::toTag(jnienv,tag),
	                                       JniUtils::toOrder(sentOrdering),
	                                       HLAfloat64Time(doubleTime),
	                                       JniUtils::toOrder(receiveOrdering),
	                                       JniUtils::toRetractionHandle(retractionHandle),
	                                       JniUtils::toRemoveSupplement(producingFederate) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    removeObjectInstance
 * Signature: (II[BIJIII)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_removeObjectInstance__II_3BIJIII
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jbyteArray tag,
	  jint sentOrdering,
	  jlong longTime,
	  jint receiveOrdering,
	  jint retractionHandle,
	  jint producingFederate )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;

	javarti->fedamb->removeObjectInstance( JniUtils::toObjectHandle(objectHandle),
	                                       JniUtils::toTag(jnienv,tag),
	                                       JniUtils::toOrder(sentOrdering),
	                                       HLAinteger64Time(longTime),
	                                       JniUtils::toOrder(receiveOrdering),
	                                       JniUtils::toRetractionHandle(retractionHandle),
	                                       JniUtils::toRemoveSupplement(producingFederate) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    provideAttributeValueUpdate
 * Signature: (II[I[B)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_provideAttributeValueUpdate
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray attributeHandles,
	  jbyteArray tag )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->provideAttributeValueUpdate( JniUtils::toObjectHandle(objectHandle),
	                                              JniUtils::toAttributeSet(jnienv,attributeHandles),
	                                              JniUtils::toTag(jnienv,tag) );
}

//////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////// Scope and Misc Services ////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    attributesInScope
 * Signature: (II[I)V
 */
JNIEXPORT void JNICALL Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_attributesInScope
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray theAttributes )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->attributesInScope( JniUtils::toObjectHandle(objectHandle),
	                                    JniUtils::toAttributeSet(jnienv,theAttributes) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    attributesOutOfScope
 * Signature: (II[I)V
 */
JNIEXPORT void JNICALL Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_attributesOutOfScope
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray theAttributes )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->attributesOutOfScope( JniUtils::toObjectHandle(objectHandle),
	                                       JniUtils::toAttributeSet(jnienv,theAttributes) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    turnUpdatesOnForObjectInstance
 * Signature: (II[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_turnUpdatesOnForObjectInstance__II_3I
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray theAttributes )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->turnUpdatesOnForObjectInstance( JniUtils::toObjectHandle(objectHandle),
	                                                 JniUtils::toAttributeSet(jnienv,theAttributes) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    turnUpdatesOnForObjectInstance
 * Signature: (II[ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_turnUpdatesOnForObjectInstance__II_3ILjava_lang_String_2
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray theAttributes,
	  jstring rate )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->turnUpdatesOnForObjectInstance( JniUtils::toObjectHandle(objectHandle),
	                                                 JniUtils::toAttributeSet(jnienv,theAttributes),
	                                                 JniUtils::toWideString(jnienv,rate) );

}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    turnUpdatesOffForObjectInstance
 * Signature: (II[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_turnUpdatesOffForObjectInstance
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray theAttributes )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->turnUpdatesOffForObjectInstance( JniUtils::toObjectHandle(objectHandle),
	                                                  JniUtils::toAttributeSet(jnienv,theAttributes) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    confirmAttributeTransportationTypeChange
 * Signature: (II[II)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_confirmAttributeTransportationTypeChange
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray theAttributes,
	  jint transport )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->confirmAttributeTransportationTypeChange(
		JniUtils::toObjectHandle(objectHandle),
		JniUtils::toAttributeSet(jnienv,theAttributes),
		JniUtils::toTransport(transport) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    confirmInteractionTransportationTypeChange
 * Signature: (III)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_confirmInteractionTransportationTypeChange
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint interactionHandle,
	  jint transport )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->confirmInteractionTransportationTypeChange(
		JniUtils::toInteractionClassHandle(interactionHandle),
		JniUtils::toTransport(transport) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    reportAttributeTransportationType
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_reportAttributeTransportationType
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jint attributeHandle,
	  jint transport )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->reportAttributeTransportationType( JniUtils::toObjectHandle(objectHandle),
	                                                    JniUtils::toAttributeHandle(attributeHandle),
	                                                    JniUtils::toTransport(transport) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    reportInteractionTransportationType
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_reportInteractionTransportationType
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint federateHandle,
	  jint interactionHandle,
	  jint transport )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->reportInteractionTransportationType(
		JniUtils::toFederateHandle(federateHandle),
		JniUtils::toInteractionClassHandle(interactionHandle),
		JniUtils::toTransport(transport) );
}

//////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////// Ownership Management Services /////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    requestAttributeOwnershipAssumption
 * Signature: (II[I[B)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_requestAttributeOwnershipAssumption
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray offeredAttributes,
	  jbyteArray tag )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->requestAttributeOwnershipAssumption(
		JniUtils::toObjectHandle(objectHandle),
		JniUtils::toAttributeSet(jnienv,offeredAttributes),
		JniUtils::toTag(jnienv,tag) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    requestDivestitureConfirmation
 * Signature: (II[I)V
 */
JNIEXPORT void JNICALL 
ava_org_portico_impl_cpp1516e_FederateAmbassadorLink_requestDivestitureConfirmation
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray attributeHandles )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->requestDivestitureConfirmation(
		JniUtils::toObjectHandle(objectHandle),
		JniUtils::toAttributeSet(jnienv,attributeHandles) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    attributeOwnershipAcquisitionNotification
 * Signature: (II[I[B)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_attributeOwnershipAcquisitionNotification
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray attributeHandles,
	  jbyteArray tag )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->attributeOwnershipAcquisitionNotification(
		JniUtils::toObjectHandle(objectHandle),
		JniUtils::toAttributeSet(jnienv,attributeHandles),
		JniUtils::toTag(jnienv,tag) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    attributeOwnershipUnavailable
 * Signature: (II[I)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_attributeOwnershipUnavailable
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray attributeHandles )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->attributeOwnershipUnavailable(
		JniUtils::toObjectHandle(objectHandle),
		JniUtils::toAttributeSet(jnienv,attributeHandles) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    requestAttributeOwnershipRelease
 * Signature: (II[I[B)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_requestAttributeOwnershipRelease
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray attributeHandles,
	  jbyteArray tag )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->requestAttributeOwnershipRelease(
		JniUtils::toObjectHandle(objectHandle),
		JniUtils::toAttributeSet(jnienv,attributeHandles),
		JniUtils::toTag(jnienv,tag) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    confirmAttributeOwnershipAcquisitionCancellation
 * Signature: (II[I)V
 */
JNIEXPORT void JNICALL 
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_confirmAttributeOwnershipAcquisitionCancellation
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jintArray attributeHandles )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->confirmAttributeOwnershipAcquisitionCancellation(
		JniUtils::toObjectHandle(objectHandle),
		JniUtils::toAttributeSet(jnienv,attributeHandles) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    informAttributeOwnership
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_informAttributeOwnership
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle, 
	  jint attributeHandle,
	  jint federateHandle )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->informAttributeOwnership( JniUtils::toObjectHandle(objectHandle),
	                                           JniUtils::toAttributeHandle(attributeHandle),
	                                           JniUtils::toFederateHandle(federateHandle) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    attributeIsNotOwned
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_attributeIsNotOwned
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jint attributeHandle )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->attributeIsNotOwned( JniUtils::toObjectHandle(objectHandle),
	                                      JniUtils::toAttributeHandle(attributeHandle) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    attributeIsOwnedByRTI
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_attributeIsOwnedByRTI
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint objectHandle,
	  jint attributeHandle )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->attributeIsOwnedByRTI( JniUtils::toObjectHandle(objectHandle),
	                                        JniUtils::toAttributeHandle(attributeHandle) );
}

//////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////// Time Management Services ///////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////
/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    timeRegulationEnabled
 * Signature: (ID)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_timeRegulationEnabled__ID
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jdouble doubleTime )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->timeRegulationEnabled( HLAfloat64Time(doubleTime) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    timeRegulationEnabled
 * Signature: (IJ)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_timeRegulationEnabled__IJ
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jlong longTime )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->timeRegulationEnabled( HLAinteger64Time(longTime) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    timeConstrainedEnabled
 * Signature: (ID)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_timeConstrainedEnabled__ID
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jdouble doubleTime )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->timeConstrainedEnabled( HLAfloat64Time(doubleTime) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    timeConstrainedEnabled
 * Signature: (IJ)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_timeConstrainedEnabled__IJ
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jlong longTime )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->timeConstrainedEnabled( HLAinteger64Time(longTime) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    timeAdvanceGrant
 * Signature: (ID)V
 */
JNIEXPORT void JNICALL
Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_timeAdvanceGrant__ID
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jdouble doubleTime )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->timeAdvanceGrant( HLAfloat64Time(doubleTime) );
}
/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    timeAdvanceGrant
 * Signature: (IJ)V
 */
JNIEXPORT void JNICALL Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_timeAdvanceGrant__IJ
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jlong longTime )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->timeAdvanceGrant( HLAinteger64Time(longTime) );
}

/*
 * Class:     org_portico_impl_cpp1516e_FederateAmbassadorLink
 * Method:    requestRetraction
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_org_portico_impl_cpp1516e_FederateAmbassadorLink_requestRetraction
	( JNIEnv *jnienv,
	  jobject jfedamb,
	  jint fedid,
	  jint retractionHandle )
{
	JavaRTI *javarti = getRTI( fedid );
	if( javarti == NULL )
		return;
	
	javarti->fedamb->requestRetraction( JniUtils::toRetractionHandle(retractionHandle) );
}
