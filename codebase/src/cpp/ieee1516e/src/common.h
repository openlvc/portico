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
#ifndef PORTICO_CPP_HLA1516E_COMMON_H_
#define PORTICO_CPP_HLA1516E_COMMON_H_

#pragma once

// determine the platform
#if _WIN32 || _WIN64
	// determine the platform 
	#if _WIN32
		#define ARCH_X86
	#elif _WIN64
		#define ARCH_AMD64
	#endif

	// windows platform, determine the compiler version
	#if _MSC_VER >= 1700
		#define VC_VERSION vc11
		#define VC11
	#elif _MSC_VER >= 1600
		#define VC_VERSION vc10
		#define VC10
	#elif _MSC_VER >= 1500
		#define VC_VERSION vc9
		#define VC9
	#elif _MSC_VER >= 1400
		#define VC_VERSION vc8
		#define VC8
	#endif
#elif __GNUC__
	#if __x86_64__
		#define ARCH_AMD64
	#else
		#define ARCH_X86
	#endif
#endif

// include some platform-dependant headers
#ifdef _WIN32 || _WIN64
    #include <windows.h>
	#include <stdint.h>
#elif defined(__APPLE__)
    #include <stdarg.h>
    #include <ctype.h>
	#include <float.h>
#else
    #include <stdarg.h>
    #include <ctype.h>
#endif

// standard library types
using namespace std;
#include <iostream>
#include <map>
#include <set>
#include <sstream>
#include <string>

//////////////////////////////////////////
////////// HLA Standard Headers //////////
//////////////////////////////////////////
// HLA includes
#include "RTI/RTI1516.h"
using namespace rti1516e;

// HLA time stuff
#include "RTI/time/HLAinteger64Time.h"
#include "RTI/time/HLAinteger64Interval.h"
#include "RTI/time/HLAinteger64TimeFactory.h"
#include "RTI/time/HLAfloat64Time.h"
#include "RTI/time/HLAfloat64Interval.h"
#include "RTI/time/HLAfloat64TimeFactory.h"

// the HLA interface has defines for Integer8, 16, 32 and 64, but then
// just goes and uses boring old "double" for floats. At least be consistent
// with the naming!
typedef float Float32;
typedef double Float64;

// defines for the standard 1516e namespace
#define IEEE1516E_NS rti1516e
#define IEEE1516E_NS_START namespace IEEE1516E_NS {
#define IEEE1516E_NS_END };

//////////////////////////////////////////
///////// Portico Releated Items /////////
//////////////////////////////////////////

// java native interface
#include <jni.h>

// defines for the portico namespace - I like this because it stands out a bit
#define PORTICO1516E_NS portico1516e
#define PORTICO1516E_NS_START namespace PORTICO1516E_NS {
#define PORTICO1516E_NS_END };



#endif /*PORTICO_CPP_HLA1516E_COMMON_H_*/
