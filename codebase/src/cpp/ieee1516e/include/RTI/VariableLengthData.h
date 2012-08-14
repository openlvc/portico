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
   File: RTI/VariableLengthData.h
***********************************************************************/

#ifndef RTI_VariableLengthData_h
#define RTI_VariableLengthData_h

#include <RTI/SpecificConfig.h>
#include <stddef.h>

// A class to hold an arbitrary array of bytes for encoded values,
// attribute values, parameter values, etc.  The class provides
// several ways of setting data, allowing trade offs between
// efficiency and memory management responsibility.

namespace rti1516e
{
   // Forward declaration for the RTI-internal class
   // used to implement VariableLengthData
   class VariableLengthDataImplementation;

   // Function to delete memory taken by instance of VariableLengthData class
   typedef void (*VariableLengthDataDeleteFunction)(void* data);

   class RTI_EXPORT VariableLengthData
   {
   public:
      VariableLengthData ();

      // Caller is free to delete inData after the call
      VariableLengthData (
         void const * inData,
         size_t inSize);

      // Caller is free to delete rhs after the call
      VariableLengthData (
         VariableLengthData const & rhs);

      ~VariableLengthData ();

      // Caller is free to delete rhs after the call
      // This instance will revert to internal storage as a result of assignment.
      VariableLengthData &
      operator= (
         VariableLengthData const & rhs);

      // This pointer should not be expected to be valid past the
      // lifetime of this object, or past the next time this object
      // is given new data
      void const * data () const;

      size_t size () const;

      // Caller is free to delete inData after the call
      void setData (
         void const * inData,
         size_t inSize);

      // Caller is responsible for ensuring that the data that is
      // pointed to is valid for the lifetime of this object, or past
      // the next time this object is given new data.
      void setDataPointer (
         void* inData,
         size_t inSize);

      // Caller gives up ownership of inData to this object.
      // This object assumes the responsibility of deleting inData
      // when it is no longer needed.
      // The allocation of inData is assumed to have been through an array
      // alloctor (e.g., char* data = new char[20]. If the data was allocated
      // in some other fashion, a deletion function must be supplied.
      void takeDataPointer (
         void* inData,
         size_t inSize,
         VariableLengthDataDeleteFunction func = 0);

   private:

      // Friend declaration for an RTI-internal class that
      // can access the implementation of a VariableLengthValue
      friend class VariableLengthDataFriend;

      VariableLengthDataImplementation* _impl;
   };
}

#endif // RTI_VariableLengthData_h
