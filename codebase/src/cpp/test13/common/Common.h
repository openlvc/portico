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
#ifndef COMMON_H_
#define COMMON_H_

/////////////////////////////////
////////// #includes's //////////
/////////////////////////////////

// Default stuff
#include <stdio.h>
#include <stdarg.h>

// CppUnit includes
#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestFixture.h>

// Portico includes
#include "RTI.hh"
#include "fedtime.hh"

// Testing framework includes
#include "Test13Federate.h"
#include "Test13FederateAmbassador.h"
#include "Test13Object.h"
#include "Test13Interaction.h"

////////////////////////////
////////// macros //////////
////////////////////////////
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
	map<keytype,valuetype>::iterator iterator;          \
	for( iterator = mapname->begin();                   \
	     iterator != mapname->end();                    \
	     iterator++ )                                   \
	{                                                   \
		valuetype current = (*iterator).second;         \
		if( current != NULL )                           \
			delete current;                             \
	}                                                   \
}

/*
 * Specialist version of MAP_CLEANUP that works with the
 * maps we define using ltstr comparision. Stupid MSVC doesn't
 * like using the other version. Humbug.
 */
#define MAP_LTSTR_CLEANUP(mapname)                      \
{                                                       \
	map<char*,char*,ltstr>::iterator iterator;          \
	for( iterator = mapname->begin();                   \
	     iterator != mapname->end();                    \
	     iterator++ )                                   \
	{                                                   \
		char *current = (*iterator).second;             \
		delete current;                                 \
	}                                                   \
}

/*
 * Same as MAP_CLEANUP except that it is for vectors
 */
#define VECTOR_CLEANUP(valuetype,vectorname)            \
{                                                       \
	vector<valuetype>::iterator iterator;               \
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
	set<valuetype>::iterator iterator;                  \
	for( iterator = setname->begin();                   \
	     iterator != setname->end();                    \
	     iterator++ )                                   \
	{                                                   \
		valuetype current = *iterator;                  \
		delete current;                                 \
	}                                                   \
}

/*
 * Specialist version of SET_CLEANUP that works with the
 * sets we define using ltstr comparision. Stupid MSVC doesn't
 * like using the other version. Humbug.
 */
#define SET_LTSTR_CLEANUP(setname)                      \
{                                                       \
	set<char*,ltstr>::iterator iterator;                \
	for( iterator = setname->begin();                   \
	     iterator != setname->end();                    \
	     iterator++ )                                   \
	{                                                   \
		char *current = *iterator;                      \
		delete current;                                 \
	}                                                   \
}


////////////////////////////////////
////////// useful methods //////////
////////////////////////////////////
/*
 * Fail the current test with the given message
 */
void failTest( const char *format, ... );

/*
 * Test should fail because an exception was expected, but none occurred. The failure message
 * will also include the action that was underway (and should have caused an exception).
 */
void failTestMissingException( char *expectedException, char* action );

/*
 * An exception was received, but it wasn't the one we expected. The failure message will
 * include the expected and actual exception types and a message regarding the action that
 * was in progress.
 */
void failTestWrongException( char *expected, RTI::Exception &actual, char *action );

#endif /*COMMON_H_*/
