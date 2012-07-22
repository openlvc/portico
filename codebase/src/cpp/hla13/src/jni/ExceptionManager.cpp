/*
 *   Copyright 2007 The Portico Project
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
#include "org_portico_impl_cpp13_ExceptionManager.h"
#include "Runtime.h"
#include "JavaRTI.h"

/*
 * Class:     org_portico_impl_cpp13_ExceptionManager
 * Method:    pushException
 * Signature: (ILjava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void
JNICALL Java_org_portico_impl_cpp13_ExceptionManager_pushException( JNIEnv * env,
                                                                    jclass callingClass,
                                                                    jint rtiid,
                                                                    jstring exceptionName,
                                                                    jstring exceptionReason )
{
	// get access to the link that the exception belongs to
	portico13::JavaRTI *theRTI = portico13::Runtime::getRuntime()->getRtiAmbassador( rtiid );

	// check to make sure we have a reference. if an exception occurs while we
	// are creating the RTI, it won't be in the map yet
	if( theRTI == NULL )
	{
		// do nothing for now
		return;
	}

	// convert the string information into local space that won't get cleaned up
	char *usExceptionName   = theRTI->convertAndReleaseJString( exceptionName );
	char *usExceptionReason = theRTI->convertAndReleaseJString( exceptionReason );

	// push the exception into the instance - it will be responsible for releasing the memory
	theRTI->pushException( usExceptionName, usExceptionReason );
}

