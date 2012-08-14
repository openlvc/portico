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
   File: RTI/Typedefs.h
***********************************************************************/

// Purpose: This file contains the standard RTI types that are defined in
// namespace rti1516e.  These definitions/declarations are standard for all RTI
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

namespace rti1516e
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

   // RTI::FederateRestoreStatusVector implements a collection of
   // FederateRestoreStatus objects, each of which contains a pre-restore handle,
   // a post-restore handle, and a restore status.
   class RTI_EXPORT FederateRestoreStatus
   {
   public:
      FederateRestoreStatus (
         FederateHandle const & thePreHandle,
         FederateHandle const & thePostHandle,
         RestoreStatus theStatus);

      FederateHandle preRestoreHandle;
      FederateHandle postRestoreHandle;
      RestoreStatus status;
   };

   typedef std::vector< FederateRestoreStatus > FederateRestoreStatusVector;

   // RTI::FederationExectionInformationSet implements a collection of
   // FederationExecutionInformation, each of which contains
   // a federation execution name and a logical time implementation name.
   class RTI_EXPORT FederationExecutionInformation
   {
   public:
      FederationExecutionInformation (
         std::wstring const & theFederationExecutionName,
         std::wstring const & theLogicalTimeImplementationName);

      std::wstring federationExecutionName;
      std::wstring logicalTimeImplementationName;
   };

   typedef std::vector<FederationExecutionInformation> FederationExecutionInformationVector;

   class RTI_EXPORT SupplementalReflectInfo
   {
   public:
      SupplementalReflectInfo ();
      SupplementalReflectInfo (
         FederateHandle const & theFederateHandle);
      SupplementalReflectInfo (
         RegionHandleSet const & theRegionHandleSet);
      SupplementalReflectInfo (
         FederateHandle const & theFederateHandle,
         RegionHandleSet const & theRegionHandleSet);

      bool hasProducingFederate;
      bool hasSentRegions;
      FederateHandle producingFederate;
      RegionHandleSet sentRegions;
    };

   class RTI_EXPORT SupplementalReceiveInfo
   {
   public:
      SupplementalReceiveInfo ();
      SupplementalReceiveInfo (
         FederateHandle const & theFederateHandle);
      SupplementalReceiveInfo (
         RegionHandleSet const & theRegionHandleSet);
      SupplementalReceiveInfo (
         FederateHandle const & theFederateHandle,
         RegionHandleSet const & theRegionHandleSet);

      bool hasProducingFederate;
      bool hasSentRegions;
      FederateHandle producingFederate;
      RegionHandleSet sentRegions;
    };


   class RTI_EXPORT SupplementalRemoveInfo
   {
   public:
      SupplementalRemoveInfo ();
      SupplementalRemoveInfo (
         FederateHandle const & theFederateHandle);

      bool hasProducingFederate;
      FederateHandle producingFederate;
    };
}

#endif // RTI_Typedefs_h
