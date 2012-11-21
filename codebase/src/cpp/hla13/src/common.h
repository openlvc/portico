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
#ifndef __PORTICO_CPP_HLA13_COMMON_H_
#define __PORTICO_CPP_HLA13_COMMON_H_

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
#ifdef _WIN32
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

#define PORTICO13_NS_START namespace portico13 {
#define PORTICO13_NS_END };

//////////////////////////////////
/// Java/PorticoSpecific Stuff ///
//////////////////////////////////
#include <jni.h>

//////////////////////////////////
/////// HLA Specific Stuff ///////
//////////////////////////////////
#ifdef BUILDING_DLC
	#include "RTI.hh"
	#include "fedtime13.h" // we don't have fedtime.hh
	#define RTI_INTERNAL_ERROR rti13::RTIinternalError
	#define HLA rti13 // e.g. HLA::ULong (replacing RTI::ULong or rti13::ULong)
#else
	#include "RTI.hh"
	#include "fedtime.hh"
	#define RTI_INTERNAL_ERROR RTI::RTIinternalError
	#define HLA RTI // e.g. HLA::ULong (replacing RTI::ULong or rti13::ULong)
#endif

//////////////////////////////////
///////////// macros /////////////
//////////////////////////////////
/*
 * Iterates over a map and calls "delete" on each of the values. This code is contained in its
 * own little block so that it can be used multiple times in one method (if it wasn't in its own
 * {} block, the iterator variable would end up being redeclared).
 *
 * keytype:   the type of the keys in the map
 * valuetype: the type of the values in the map
 * mapname:   the name of the map variable (must be a POINTER)
 */
#define MAP_CLEANUP(keytype,valuetype,mapname)          \
{                                                       \
	std::map<keytype,valuetype>::iterator iterator;     \
	for( iterator = mapname->begin();                   \
	     iterator != mapname->end();                    \
	     iterator++ )                                   \
	{                                                   \
		valuetype current = (*iterator).second;         \
		delete current;                                 \
	}                                                   \
}

/*
 * Same as MAP_CLEANUP except that it is for vectors
 */
#define VECTOR_CLEANUP(valuetype,vectorname)            \
{                                                       \
	std::vector<valuetype>::iterator iterator;          \
	for( iterator = vectorname->begin();                \
	     iterator != vectorname->end();                 \
	     iterator++ )                                   \
	{                                                   \
		valuetype current = *iterator;                  \
		delete current;                                 \
	}                                                   \
}

/*
 * Same as MAP_CLEANUP except that it is for sets
 */
#define SET_CLEANUP(valuetype,setname)                  \
{                                                       \
	std::set<valuetype>::iterator iterator;             \
	for( iterator = setname->begin();                   \
	     iterator != setname->end();                    \
	     iterator++ )                                   \
	{                                                   \
		valuetype current = *iterator;                  \
		delete current;                                 \
	}                                                   \
}

#endif

