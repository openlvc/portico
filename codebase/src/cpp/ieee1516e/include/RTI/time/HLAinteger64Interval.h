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
   File: RTI/time/HLAinteger64Interval.h
***********************************************************************/

#ifndef RTI_HLAinteger64Interval_H_
#define RTI_HLAinteger64Interval_H_

#include <RTI/LogicalTimeInterval.h>
#include <RTI/encoding/EncodingConfig.h>

// Defines interface for HLAinteger64Time which presents an integer-based
// interval representation in the range 0 - 2^63-1. The encoded representation
// is HLAinteger64BE (signed, 64-bit, big-endian).

namespace rti1516e
{
   class HLAinteger64IntervalImpl;

   class RTI_EXPORT HLAinteger64Interval : public rti1516e::LogicalTimeInterval
   {
   public:

      // Constructors
      HLAinteger64Interval ();

      HLAinteger64Interval (
         HLAinteger64Interval const & rhs);

      HLAinteger64Interval (
         rti1516e::LogicalTimeInterval const & rhs);

      HLAinteger64Interval (
         Integer64);

      // Destructor
      virtual ~HLAinteger64Interval()
         throw ();

      // Basic accessors/mutators

      virtual void setZero ();

      virtual bool isZero () const;

      virtual void setEpsilon ();

      virtual bool isEpsilon () const;

      // Operators

      virtual rti1516e::LogicalTimeInterval& operator= (
         rti1516e::LogicalTimeInterval const & value)
         throw (rti1516e::InvalidLogicalTimeInterval);

      virtual rti1516e::LogicalTimeInterval& operator+= (
         rti1516e::LogicalTimeInterval const & addend)
         throw (rti1516e::IllegalTimeArithmetic,
                rti1516e::InvalidLogicalTimeInterval);

      virtual rti1516e::LogicalTimeInterval& operator-= (
         rti1516e::LogicalTimeInterval const & subtrahend)
         throw (rti1516e::IllegalTimeArithmetic,
                rti1516e::InvalidLogicalTimeInterval);

      virtual bool operator> (
         rti1516e::LogicalTimeInterval const & value) const
         throw (rti1516e::InvalidLogicalTimeInterval);

      virtual bool operator< (
         rti1516e::LogicalTimeInterval const & value) const
         throw (rti1516e::InvalidLogicalTimeInterval);

      virtual bool operator== (
         rti1516e::LogicalTimeInterval const & value) const
         throw (rti1516e::InvalidLogicalTimeInterval);

      virtual bool operator>= (
         rti1516e::LogicalTimeInterval const & value) const
         throw (rti1516e::InvalidLogicalTimeInterval);

      virtual bool operator<= (
         rti1516e::LogicalTimeInterval const & value) const
         throw (rti1516e::InvalidLogicalTimeInterval);

      // Set self to the difference between two LogicalTimes
      virtual void setToDifference (
         rti1516e::LogicalTime const & minuend,
         rti1516e::LogicalTime const& subtrahend)
         throw (rti1516e::IllegalTimeArithmetic,
                rti1516e::InvalidLogicalTime);

      // Generates an encoded value that can be used to send
      // LogicalTimeIntervals to other federates in updates or interactions
      virtual rti1516e::VariableLengthData encode () const;

      // Alternate encode for directly filling a buffer
      // Return the length of encoded data
      virtual size_t encode (
         void* buffer,
         size_t bufferSize) const
         throw (rti1516e::CouldNotEncode);

      // The length of the encoded data
      virtual size_t encodedLength () const;

      // Decode encodedValue into self
      virtual void decode (
         rti1516e::VariableLengthData const & encodedValue)
         throw (rti1516e::InternalError,
                rti1516e::CouldNotDecode);

      // Decode encodedValue into self
      // Alternate decode that reads directly from a buffer
      virtual void decode (
         void* buffer,
         size_t bufferSize)
         throw (rti1516e::InternalError,
                rti1516e::CouldNotDecode);

      // Diagnostic string representation of time
      virtual std::wstring toString () const;

      // Return the name of the Implementation, as needed by
      // createFederationExecution.
      virtual std::wstring implementationName () const;

   public:

      //-----------------------------------------------------------------
      // Implementation functions
      //-----------------------------------------------------------------

      virtual Integer64 getInterval () const;

      virtual void setInterval (
         Integer64 value);

      //-----------------------------------------------------------------
      // Implementation operators
      //-----------------------------------------------------------------
      virtual HLAinteger64Interval& operator= (
         const HLAinteger64Interval& value)
         throw (rti1516e::InvalidLogicalTimeInterval);

      operator Integer64 () const;

   private:

      //-----------------------------------------------------------------
      // Pointer to internal Implementation
      //-----------------------------------------------------------------
      HLAinteger64IntervalImpl* _impl;
   };
}

#endif // RTI_HLAinteger64Interval_H_

