/***********************************************************************
   The IEEE hereby grants a general, royalty-free license to copy, distribute,
   display and make derivative works from this material, for all purposes,
   provided that any use of the material contains the following
   attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
   Should you require additional information, contact the Manager, Standards
   Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
***********************************************************************/
/***********************************************************************
      IEEE 1516.1 High Level Architecture Interface Specification C++ API
      File: RTI/1516.h
***********************************************************************/

//
// This file is simply a convenience provided for those developers that would
// like to include everything all at once
//

#ifndef COMMON_H
#define COMMON_H

#define va_start _crt_va_start
#define va_arg _crt_va_arg
#define va_end _crt_va_end

// Identification of the API version number.
#define HLA_SPECIFICATION_NAME "1516e"
#define HLA_API_MAJOR_VERSION 2
#define HLA_API_MINOR_VERSION 0

// The file containing the platform specific configuration info.
#include <RTI/SpecificConfig.h>

// The files containing the declarations/definitions for ISO 14882 standard C++.
#include <string>
#include <set>
#include <map>
#include <vector>
#include <memory>

// CppUnit includes
#include <cppunit/extensions/HelperMacros.h>
#include <cppunit/TestFixture.h>

// The files containing the standard RTI type declarations/definitions.
#include <RTI/Enums.h>
#include <RTI/Exception.h>
#include <RTI/Handle.h>
#include <RTI/RangeBounds.h>
#include <RTI/Typedefs.h>
#include <RTI/VariableLengthData.h>

// The files containing the standard RTI type declarations/definitions for logical time.
#include <RTI/LogicalTime.h>
#include <RTI/LogicalTimeFactory.h>
#include <RTI/LogicalTimeInterval.h>

namespace rti1516e
{
   // Vendor-specific name and version of the RTI implementation
   std::wstring RTI_EXPORT rtiName();
   std::wstring RTI_EXPORT rtiVersion();
}

// The files containing the RTI Ambassdor and Federate Ambassador service calls
#include <RTI/FederateAmbassador.h>
#include <RTI/RTIambassador.h>
#include <RTI/portico/RTIambassadorEx.h>

// The file containing the RTI Ambassador Factory.
#include <RTI/RTIambassadorFactory.h>


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
	map<keytype, valuetype>::iterator iterator;          \
for (iterator = mapname->begin();                   \
	iterator != mapname->end();                    \
	iterator++)                                   \
{                                                   \
	valuetype current = (*iterator).second;         \
if (current != NULL)                           \
	delete current;                             \
}                                                   \
}

/*
* Specialist version of MAP_CLEANUP that works with the
* maps we define using ltstr comparision. Stupid MSVC doesn't
* like using the other version. Humbug.
*/
#define MAP_LTSTR_CLEANUP(mapname)                          \
{                                                           \
	map<const char*, const char*, ltstr>::iterator iterator;  \
for (iterator = mapname->begin();                       \
	iterator != mapname->end();                        \
	iterator++)                                       \
{                                                       \
	const char *current = (*iterator).second;           \
	delete current;                                     \
}                                                       \
}

/*
* Same as MAP_CLEANUP except that it is for vectors
*/
#define VECTOR_CLEANUP(valuetype,vectorname)            \
{                                                       \
	vector<valuetype>::iterator iterator;               \
for (iterator = vectorname->begin();                \
	iterator != vectorname->end();                 \
	iterator++)                                   \
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
for (iterator = setname->begin();                   \
	iterator != setname->end();                    \
	iterator++)                                   \
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
	set<const char*, ltstr>::iterator iterator;          \
for (iterator = setname->begin();                   \
	iterator != setname->end();                    \
	iterator++)                                   \
{                                                   \
	const char *current = *iterator;                \
	delete current;                                 \
}                                                   \
}


////////////////////////////////////
////////// useful methods //////////
////////////////////////////////////
/*
* Fail the current test with the given message
*/
void failTest(const char *format, ...);

/*
* Test should fail because an exception was expected, but none occurred. The failure message
* will also include the action that was underway (and should have caused an exception).
*/
void failTestMissingException(const char *expectedException, const char* action);

/*
* An exception was received, but it wasn't the one we expected. The failure message will
* include the expected and actual exception types and a message regarding the action that
* was in progress.
*/
void failTestWrongException(const char *expected, rti1516e::Exception &actual, const char *action);


#endif // RTI_1516_h
