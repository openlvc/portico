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

#ifndef RTI_1516_h
#define RTI_1516_h

// Identification of the API version number.
#define HLA_SPECIFICATION_NAME "1516"
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

// The file containing the RTI Ambassador Factory.
#include <RTI/RTIambassadorFactory.h>

#endif // RTI_1516_h
