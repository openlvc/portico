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
#include "jni/JniUtils.h"

PORTICO1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------
string JniUtils::convert( JNIEnv *jnienv, jstring javaString )
{
	const char *temp = jnienv->GetStringUTFChars( javaString, NULL );
	string converted( temp );
	jnienv->ReleaseStringUTFChars( javaString, temp );
	return converted;
}

string JniUtils::convertAndRelease( JNIEnv *jnienv, jstring javaString )
{
	string converted = JniUtils::convert( jnienv, javaString );
	jnienv->DeleteLocalRef( javaString );
	return converted;
}

PORTICO1516E_NS_END
