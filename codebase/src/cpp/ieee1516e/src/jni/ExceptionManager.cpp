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
#include "jni/org_portico_impl_cpp1516e_ExceptionManager.h"
#include "jni/JniUtils.h"
#include "jni/Runtime.h"

           ////////////////////////////////////////////////////////////////////////////
           // IMPLEMENTATION NOTES:                                                  //
           //                                                                        //
           //  This class is the C implementation that the Java ExceptionManager.    //
           //  When exceptions occur inside the core RTI (in Java) they need a way   //
           //  to find their way back into the C++ code that triggered them. This    //
           //  class provides that mechanism. The Java side of the binding calls     //
           //  methods on the Java ExceptionManager, which in turn passed them via   //
           //  JNI to this function.
           //                                                                        //
           //  Each incoming call from the Java side provided with it an ID that is  //
           //  used to locate the local federate container instance responsible for  //
           //  causing the exception. The getRTI(int) method will return a pointer   //
           //  to the appropriate JavaRTI instance into which we push the exception  //
           //  details. Periodically, the JavaRTI services check this store to see   //
           //  is an exception was raised, and if to, exacts and throws the details. //
           ////////////////////////////////////////////////////////////////////////////

/*
 * Class:     org_portico_impl_cpp1516e_ExceptionManager
 * Method:    pushException
 * Signature: (ILjava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void
JNICALL Java_org_portico_impl_cpp1516e_ExceptionManager_pushException( JNIEnv *jnienv,
                                                                       jclass callingClass,
                                                                       jint rtiid,
                                                                       jstring exceptionName,
                                                                       jstring exceptionReason )
{
	// get access to the link that the exception belongs to
	portico1516e::JavaRTI *theRTI = portico1516e::Runtime::getRuntime()->getRtiAmbassador( rtiid );
	
	// check to make sure we have a reference. if an exception occurs while we
	// are creating the RTI, it won't be in the map yet
	if( theRTI == NULL )
	{
		// futile attempt to print some sort of log message
		std::cerr << "Exception received from Java binding for RTI with ID [" << rtiid
		          << "]: cannot locate that JavaRTI reference. Something is horribly wrong"
		          << std::endl;
		return;
	}

	// convert the string information into local space that won't get cleaned up
	string eName = portico1516e::JniUtils::toStringAndRelease( jnienv, exceptionName );
	string eReason = portico1516e::JniUtils::toStringAndRelease( jnienv, exceptionReason );
	theRTI->pushException( eName, eReason );
}

