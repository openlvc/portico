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
   File: RTI/time/HLAinteger64Time.h
***********************************************************************/

#ifndef RTI_HLAinteger64Time_H_
#define RTI_HLAinteger64Time_H_

#include <RTI/LogicalTime.h>
#include <RTI/time/HLAinteger64Interval.h>

// Defines interface for HLAinteger64Time which presents an integer-based
// time representation in the range 0 - 2^63-1. The encoded representation
// is HLAinteger64BE (signed, 64-bit, big-endian).

namespace rti1516e
{
   class HLAinteger64TimeImpl;

   class RTI_EXPORT HLAinteger64Time : public rti1516e::LogicalTime
   {
   public:
      // Constructors
      HLAinteger64Time ();
      HLAinteger64Time (
         Integer64 value);
      HLAinteger64Time (
         rti1516e::LogicalTime const & value);
      HLAinteger64Time (
         HLAinteger64Time const & value);

      // Destructor
      virtual ~HLAinteger64Time ()
         throw ();

      // Basic accessors/mutators
      virtual void setInitial ();

      virtual bool isInitial () const;

      virtual void setFinal ();

      virtual bool isFinal () const;

      // Assignment
      virtual rti1516e::LogicalTime& operator= (
         rti1516e::LogicalTime const & value)
         throw (rti1516e::InvalidLogicalTime);

      // Operators

      virtual rti1516e::LogicalTime& operator+= (
         rti1516e::LogicalTimeInterval const & addend)
         throw (rti1516e::IllegalTimeArithmetic,
                rti1516e::InvalidLogicalTimeInterval);

      virtual rti1516e::LogicalTime& operator-= (
         rti1516e::LogicalTimeInterval const & subtrahend)
         throw (rti1516e::IllegalTimeArithmetic,
                rti1516e::InvalidLogicalTimeInterval);

      virtual bool operator> (
         rti1516e::LogicalTime const & value) const
         throw (rti1516e::InvalidLogicalTime);

      virtual bool operator< (
         rti1516e::LogicalTime const & value) const
         throw (rti1516e::InvalidLogicalTime);

      virtual bool operator== (
         rti1516e::LogicalTime const & value) const
         throw (rti1516e::InvalidLogicalTime);

      virtual bool operator>= (
         rti1516e::LogicalTime const & value) const
         throw (rti1516e::InvalidLogicalTime);

      virtual bool operator<= (
         rti1516e::LogicalTime const & value) const
         throw (rti1516e::InvalidLogicalTime);

      // Generates an encoded value that can be used to send
      // LogicalTimes to other federates in updates or interactions
      virtual rti1516e::VariableLengthData encode () const;

      // Alternate encode for directly filling a buffer
      // Return the length of the encoded data
      virtual size_t encode (
         void* buffer,
         size_t bufferSize) const
         throw (rti1516e::CouldNotEncode);

      // The length of the encoded data
      virtual size_t encodedLength () const;

      // Decode VariableLengthData into self
      virtual void decode (
         rti1516e::VariableLengthData const & VariableLengthData)
         throw (rti1516e::InternalError,
                rti1516e::CouldNotDecode);

      // Alternate decode that reads directly from a buffer
      virtual void decode (
         void* buffer,
         size_t bufferSize)
         throw (rti1516e::InternalError,
                rti1516e::CouldNotDecode);

      // Diagnostic string representation of time
      virtual std::wstring toString () const;

      // Return the name of the implementation, as needed by
      // createFederationExecution.
      virtual std::wstring implementationName () const;


   public:
      //-----------------------------------------------------------------
      // Implementation functions
      //-----------------------------------------------------------------
      virtual Integer64 getTime () const;

      virtual void setTime (
         Integer64 value);

      //-----------------------------------------------------------------
      // Implementation operators
      //-----------------------------------------------------------------
      virtual HLAinteger64Time& operator= (
         const HLAinteger64Time&)
         throw (rti1516e::InvalidLogicalTime);

      operator Integer64 () const;

   private:

      //-----------------------------------------------------------------
      // Interval implementation must have access to time implementation
      // in order to calculate difference
      //-----------------------------------------------------------------
      friend class HLAinteger64Interval;;

      //-----------------------------------------------------------------
      // Pointer to internal implementation
      //-----------------------------------------------------------------
      HLAinteger64TimeImpl* _impl;
   };
}


#endif // RTI_HLAinteger64Time_H_

