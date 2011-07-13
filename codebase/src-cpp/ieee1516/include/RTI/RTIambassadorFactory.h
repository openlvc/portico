/***********************************************************************
  IEEE 1516.1 High Level Architecture Interface Specification C++ API
  File: RTI/RTIambassadorFactory.h
***********************************************************************/

#ifndef RTI_RTIambassadorFactory_h
#define RTI_RTIambassadorFactory_h

namespace rti1516
{
  class RTIambassador;
}

namespace std
{
  template <class T> class auto_ptr;
}

#include <RTI/SpecificConfig.h>
#include <RTI/Exception.h>
#include <vector>
#include <string>

namespace rti1516
{
  class RTI_EXPORT RTIambassadorFactory
  {
  public:
    RTIambassadorFactory();
    
    virtual
    ~RTIambassadorFactory()
      throw ();
    
    // 10.35
    std::auto_ptr< RTIambassador >
    createRTIambassador(std::vector< std::wstring > & args)
      throw (BadInitializationParameter,
             RTIinternalError);
  };
}

#endif // RTI_RTIambassadorFactory_h
