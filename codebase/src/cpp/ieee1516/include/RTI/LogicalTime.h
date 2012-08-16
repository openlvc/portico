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

namespace rti1516
{
  class LogicalTimeInterval;
}

#include <RTI/SpecificConfig.h>
#include <RTI/Exception.h>
#include <string>
#include <RTI/VariableLengthData.h>

namespace rti1516
{
  class RTI_EXPORT LogicalTime
  {
  public:
    virtual
    ~LogicalTime()
      throw () = 0;

    virtual
    void
    setInitial() = 0;

    virtual
    bool
    isInitial() const = 0;
  
    virtual
    void
    setFinal() = 0;

    virtual
    bool
    isFinal() const = 0;

    virtual
    LogicalTime &
    operator=(LogicalTime const & value)
      throw (InvalidLogicalTime) = 0;

    virtual
    LogicalTime &
    operator+=(LogicalTimeInterval const & addend)
      throw (IllegalTimeArithmetic, InvalidLogicalTimeInterval) = 0;

    virtual
    LogicalTime &
    operator-=(LogicalTimeInterval const & subtrahend)
      throw (IllegalTimeArithmetic, InvalidLogicalTimeInterval) = 0;

    virtual
    bool
    operator>(LogicalTime const & value) const
      throw (InvalidLogicalTime) = 0;

    virtual
    bool
    operator<(LogicalTime const & value) const
      throw (InvalidLogicalTime) = 0;

    virtual
    bool
    operator==(LogicalTime const & value) const
      throw (InvalidLogicalTime) = 0;

    virtual
    bool
    operator>=(LogicalTime const & value) const
      throw (InvalidLogicalTime) = 0;

    virtual
    bool
    operator<=(LogicalTime const & value) const
      throw (InvalidLogicalTime) = 0;
    
    // Generates an encoded value that can be used to send
    // LogicalTimes to other federates in updates or interactions
    virtual VariableLengthData encode() const = 0;

    // Alternate encode for directly filling a buffer
    virtual unsigned long encodedLength() const = 0;
    virtual unsigned long encode(void* buffer, unsigned long bufferSize) const 
       throw (CouldNotEncode) = 0;
   
    // Decode encodedLogicalTime into self
    virtual void decode(VariableLengthData const & encodedLogicalTime)
      throw (InternalError,
             CouldNotDecode) = 0;

    // Alternate decode that reads directly from a buffer
    virtual void decode(void* buffer, unsigned long bufferSize)
      throw (InternalError,
             CouldNotDecode) = 0;

    virtual std::wstring toString() const = 0;

    // Returns the name of the implementation, as needed by
    // createFederationExecution.
    virtual std::wstring implementationName() const = 0;
  };

  // Output operator for LogicalTime
  std::wostream RTI_EXPORT &
    operator << (std::wostream &, LogicalTime const &);
}

#endif // RTI_LogicalTime_h
