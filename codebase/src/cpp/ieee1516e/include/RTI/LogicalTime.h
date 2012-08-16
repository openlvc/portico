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
   File: RTI/LogicalTime.h
***********************************************************************/

#ifndef RTI_LogicalTime_h
#define RTI_LogicalTime_h

// The classes associated with logical time allow a federation to provide their
// own representation for logical time and logical time interval. The federation
// is responsible to inherit from the abstract classes declared below. The
// encoded time classes are used to hold the arbitrary bit representation of the
// logical time and logical time intervals.

namespace rti1516e
{
   class LogicalTimeInterval;
}

#include <RTI/SpecificConfig.h>
#include <RTI/Exception.h>
#include <string>
#include <RTI/VariableLengthData.h>

namespace rti1516e
{
   class RTI_EXPORT LogicalTime
   {
   public:
      // Destructor
      virtual ~LogicalTime ()
         throw () = 0;

      // Basic accessors/mutators

      virtual void setInitial () = 0;

      virtual bool isInitial () const = 0;

      virtual void setFinal () = 0;

      virtual bool isFinal () const = 0;

      // Operators

      virtual LogicalTime & operator= (
         LogicalTime const & value)
         throw (
            InvalidLogicalTime) = 0;

      virtual LogicalTime & operator+= (
         LogicalTimeInterval const & addend)
         throw (
            IllegalTimeArithmetic,
            InvalidLogicalTimeInterval) = 0;

      virtual LogicalTime & operator-= (
         LogicalTimeInterval const & subtrahend)
         throw (
            IllegalTimeArithmetic,
            InvalidLogicalTimeInterval) = 0;

      virtual bool operator> (
         LogicalTime const & value) const
         throw (
            InvalidLogicalTime) = 0;

      virtual bool operator< (
         LogicalTime const & value) const
         throw (
            InvalidLogicalTime) = 0;

      virtual bool operator== (
         LogicalTime const & value) const
         throw (
            InvalidLogicalTime) = 0;

      virtual bool operator>= (
         LogicalTime const & value) const
         throw (
            InvalidLogicalTime) = 0;

      virtual bool operator<= (
         LogicalTime const & value) const
         throw (
            InvalidLogicalTime) = 0;

      // Generates an encoded value that can be used to send
      // LogicalTimes to other federates in updates or interactions
      virtual VariableLengthData encode () const = 0;

      // Alternate encode for directly filling a buffer
      virtual size_t encode (
         void* buffer,
         size_t bufferSize) const
         throw (
            CouldNotEncode) = 0;

      // The length of the encoded data
      virtual size_t encodedLength () const = 0;

      // Decode encodedLogicalTime into self
      virtual void decode (
         VariableLengthData const & encodedLogicalTime)
         throw (
            InternalError,
            CouldNotDecode) = 0;

      // Alternate decode that reads directly from a buffer
      virtual void decode (
         void* buffer,
         size_t bufferSize)
         throw (
            InternalError,
            CouldNotDecode) = 0;

      // Diagnostic string representation of time
      virtual std::wstring toString () const = 0;

      // Return the name of the implementation, as needed by
      // createFederationExecution.
      virtual std::wstring implementationName () const = 0;
   };

   // Output operator for LogicalTime
   std::wostream RTI_EXPORT & operator << (
      std::wostream &,
      LogicalTime const &);
}

#endif // RTI_LogicalTime_h
