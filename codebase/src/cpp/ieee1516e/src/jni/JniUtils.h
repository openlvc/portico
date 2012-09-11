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

PORTICO1516E_NS_START

/**
 * A set of utility methods to make the handling of common JNI tasks a little less
 * painful than it typically is.
 */
class JniUtils
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:
		/**
		 * Converts between jstring and std::string, optionally releasing the jstring once done
		 */
		static string convert( JNIEnv *jnienv, jstring javaString );
		static string convertAndRelease( JNIEnv *jnienv, jstring javaString );
};

PORTICO1516E_NS_END

#endif /* JNIUTILS_H_ */
