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
   File: RTI/RTIambassadorFactory.h
***********************************************************************/

#ifndef RTI_RTIambassadorFactory_h
#define RTI_RTIambassadorFactory_h

namespace rti1516e
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

namespace rti1516e
{
   class RTI_EXPORT RTIambassadorFactory
   {
   public:
      RTIambassadorFactory();

      virtual ~RTIambassadorFactory()
         throw ();

      // 10.35
      std::auto_ptr< RTIambassador > createRTIambassador ()
         throw (
            RTIinternalError);
   };
}

#endif // RTI_RTIambassadorFactory_h
