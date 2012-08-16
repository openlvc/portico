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
   File: RTI/time/HLAfloat64TimeFactory.h
***********************************************************************/

#ifndef RTI_HLAfloat64TimeFactory_H_
#define RTI_HLAfloat64TimeFactory_H_

#include <RTI/LogicalTimeFactory.h>

namespace rti1516e
{
   class HLAfloat64Time;
   class HLAfloat64Interval;

   // Defines interface for HLAfloat64TimeFactory which presents a
   // floating point-based time/interval representation in the range 0 - 2^63-1.

   const std::wstring HLAfloat64TimeName(L"HLAfloat64Time");

   class RTI_EXPORT HLAfloat64TimeFactory : public rti1516e::LogicalTimeFactory
   {
   public:
      HLAfloat64TimeFactory ();

      virtual ~HLAfloat64TimeFactory ()
         throw ();

      // Return a LogicalTime with the given value
      virtual std::auto_ptr< HLAfloat64Time > makeLogicalTime (
         double value)
         throw (rti1516e::InternalError);

      // Return a LogicalTime with a value of "initial"
      virtual std::auto_ptr< LogicalTime > makeInitial()
         throw (InternalError);

      // Return a LogicalTime with a value of "final"
      virtual std::auto_ptr< LogicalTime > makeFinal()
         throw (InternalError);

      // Return a LogicalTimeInterval with the given value
      virtual std::auto_ptr< HLAfloat64Interval > makeLogicalTimeInterval (
         double value)
         throw (rti1516e::InternalError);

      // Return a LogicalTimeInterval with a value of "zero"
      virtual std::auto_ptr< LogicalTimeInterval > makeZero()
         throw (InternalError);

      // Return a LogicalTimeInterval with a value of "epsilon"
      virtual std::auto_ptr< LogicalTimeInterval > makeEpsilon()
         throw (InternalError);

      // LogicalTime decode from an encoded LogicalTime
      virtual std::auto_ptr< LogicalTime > decodeLogicalTime (
         VariableLengthData const & encodedLogicalTime)
         throw (InternalError,
         CouldNotDecode);

      // Alternate LogicalTime decode that reads directly from a buffer
      virtual std::auto_ptr< LogicalTime > decodeLogicalTime (
         void* buffer,
         size_t bufferSize)
         throw (InternalError,
         CouldNotDecode);

      // LogicalTimeInterval decode from an encoded LogicalTimeInterval
      virtual std::auto_ptr< LogicalTimeInterval > decodeLogicalTimeInterval (
         VariableLengthData const & encodedValue)
         throw (InternalError,
         CouldNotDecode);

      // Alternate LogicalTimeInterval decode that reads directly from a buffer
      virtual std::auto_ptr< LogicalTimeInterval > decodeLogicalTimeInterval (
         void* buffer,
         size_t bufferSize)
         throw (InternalError,
         CouldNotDecode);

      virtual std::wstring getName () const;
   };
}

#endif // RTI_HLAfloat64TimeFactory_H_

