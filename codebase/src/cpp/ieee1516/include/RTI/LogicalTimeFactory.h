/***********************************************************************
  IEEE 1516.1 High Level Architecture Interface Specification C++ API
  File: RTI/LogicalTimeFactory.h
***********************************************************************/

#ifndef RTI_LogicalTimeFactory_h
#define RTI_LogicalTimeFactory_h

namespace rti1516
{
  class LogicalTime;
  class LogicalTimeInterval;
}

namespace std
{
  template <class T> class auto_ptr;
}

#include <RTI/SpecificConfig.h>
#include <RTI/Exception.h>
#include <string>

// LogicalTimeFactory is used by the RTI to construct instances of classes
// derived from LogicalTime and LogicalTimeInterval.  A federation is responsible
// for providing a fedtime library that includes one or more subclasses
// of LogicalTime and LogicalTimeInterval, one or more subclasses of LogicalTimeFactory
// (which is used to create instances of those LogicalTime and LogicalTimeInterval
// subclasses), and a single implementation of 
// LogicalTimeFactoryFactory::createLogicalTimeFactory.  This static function should
// choose a LogicalTimeFactory based on the string identifier passed as an argument,
// and return an instance of that kind of factory.  The RTI will call this function to
// obtain a LogicalTimeFactory for a federation, and then will use that factory to create
// any instances of LogicalTime or LogicalTimeInterval that it needs.

namespace rti1516
{
  class RTI_EXPORT LogicalTimeFactory
  {
  public:
    virtual
    ~LogicalTimeFactory()
      throw () = 0;
    
    // Returns a LogicalTime with a value of "initial"
    virtual
    std::auto_ptr< LogicalTime >
    makeLogicalTime()
      throw (InternalError) = 0;
    
    // Returns a LogicalTimeInterval with a value of "zero"
    virtual 
    std::auto_ptr< LogicalTimeInterval >
    makeLogicalTimeInterval() 
      throw (InternalError) = 0;
  };
}

namespace rti1516
{  
  class RTI_EXPORT_FEDTIME LogicalTimeFactoryFactory
  {
  public:

    // The name is used to choose among several LogicalTimeFactories that might
    // be present in the fedtime library.  Each federation chooses its
    // implementation by passing the appropriate name to createFederationExecution.
    // If the supplied name is the empty string, a default LogicalTimeFactory is
    // returned.  If the supplied implementation name does not match any name 
    // supported by the library, then a NULL pointer is returned. 
    static std::auto_ptr< LogicalTimeFactory > 
       makeLogicalTimeFactory(std::wstring const & implementationName);
  };
}

#endif // RTI_LogicalTimeFactory_h
