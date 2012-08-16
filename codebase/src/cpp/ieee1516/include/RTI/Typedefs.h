/***********************************************************************
  IEEE 1516.1 High Level Architecture Interface Specification C++ API
  File: RTI/Typedefs.h
***********************************************************************/

// Purpose: This file contains the standard RTI types that are defined in 
// namespace rti1516.  These definitions/declarations are standard for all RTI
// implementations.
//
// The types declared here require the use of some RTI Specific types.

#ifndef RTI_Typedefs_h
#define RTI_Typedefs_h

// The following type definitions use standard C++ classes for containers
// that are used in the RTI API.

#include <RTI/SpecificConfig.h>
#include <set>
#include <map>
#include <vector>
#include <RTI/Enums.h>
#include <RTI/Handle.h>

namespace rti1516
{
  typedef std::set< AttributeHandle > AttributeHandleSet;
  typedef std::set< ParameterHandle > ParameterHandleSet;
  typedef std::set< FederateHandle  > FederateHandleSet;
  typedef std::set< DimensionHandle > DimensionHandleSet;
  typedef std::set< RegionHandle    > RegionHandleSet;

  // RTI::AttributeHandleValueMap implements a constrained set of 
  // (attribute handle and value) pairs
  typedef std::map< AttributeHandle, VariableLengthData >
  AttributeHandleValueMap;

  // RTI::ParameterHandleValueMap implements a constrained set of 
  // (parameter handle and value) pairs
  typedef std::map< ParameterHandle, VariableLengthData >
  ParameterHandleValueMap;

  // RTI::AttributeHandleSetRegionHandleSetPairVector implements a collection of
  // (attribute handle set and region set) pairs
  typedef std::pair< AttributeHandleSet, RegionHandleSet >
  AttributeHandleSetRegionHandleSetPair;
  
  typedef std::vector< AttributeHandleSetRegionHandleSetPair >
  AttributeHandleSetRegionHandleSetPairVector;
  
  // RTI::FederateHandleSaveStatusPairVector implements a collection of
  // (federate handle and save status) pairs
  typedef std::pair< FederateHandle, SaveStatus >
  FederateHandleSaveStatusPair;
  
  typedef std::vector< FederateHandleSaveStatusPair >
  FederateHandleSaveStatusPairVector;
  
  // RTI::FederateHandleRestoreStatusPairVector implements a collection of
  // (federate handle and restore status) pairs
  typedef std::pair< FederateHandle, RestoreStatus >
  FederateHandleRestoreStatusPair;
  
  typedef std::vector< FederateHandleRestoreStatusPair >
  FederateHandleRestoreStatusPairVector;
}

#endif // RTI_Typedefs_h
