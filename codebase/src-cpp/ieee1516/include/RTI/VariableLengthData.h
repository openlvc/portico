/***********************************************************************
  IEEE 1516.1 High Level Architecture Interface Specification C++ API
  File: RTI/VariableLengthData.h
***********************************************************************/

#ifndef RTI_VariableLengthData_h
#define RTI_VariableLengthData_h

#include <RTI/SpecificConfig.h>

// A class to hold an arbitrary array of bytes for encoded values,
// attribute values, parameter values, etc.  The class provides
// several ways of setting data, allowing tradeoffs between
// efficiency and memory management reponsibility.

namespace rti1516
{  
  // Forward declaration for the RTI-internal class
  // used to implement VariableLengthData
  class VariableLengthDataImplementation;

  class RTI_EXPORT VariableLengthData
  {
  public:
    VariableLengthData();

    // Caller is free to delete inData after the call
    VariableLengthData(void const * inData, unsigned long inSize);

    // Caller is free to delete rhs after the call
    VariableLengthData(VariableLengthData const & rhs);

    ~VariableLengthData();

    // Caller is free to delete rhs after the call
    VariableLengthData &
    operator=(VariableLengthData const & rhs);

    // This pointer should not be expected to be valid past the 
    // lifetime of this object, or past the next time this object
    // is given new data
    void const * data() const;
    unsigned long size() const;

    // Caller is free to delete inData after the call
    void setData(void const * inData, unsigned long inSize);

    // Caller is responsible for ensuring that the data that is 
    // pointed to is valid for the lifetime of this object, or past
    // the next time this object is given new data.
    void setDataPointer(void* inData, unsigned long inSize);

    // Caller gives up ownership of inData to this object.
    // This object assumes the responsibility of deleting inData
    // when it is no longer needed.
    void takeDataPointer(void* inData, unsigned long inSize);

  private:

    // Friend declaration for an RTI-internal class that
    // can access the implementation of a VariableLengthValue
    friend class VariableLengthDataFriend;

    VariableLengthDataImplementation* _impl;
  };
}

#endif // RTI_VariableLengthData_h
