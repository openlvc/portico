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
#ifndef NG6PRIVATE_H_
#define NG6PRIVATE_H_

#include "jni/JavaRTI.h"

using namespace portico13;

#ifdef BUILDING_DLC
namespace rti13 {
#endif

struct RTIambPrivateRefs
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	public:
		JavaRTI *rti;
		JNIEnv *env; // shortcut to rti->jnienv

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		RTIambPrivateRefs();
		virtual ~RTIambPrivateRefs();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		JavaRTI* getRti();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

};

#ifdef BUILDING_DLC
}; // namespace
#endif

#endif /* NG6PRIVATE_H_ */
