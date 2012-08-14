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
   File: EncodingExceptions.h
***********************************************************************/
#ifndef RTI_EncodingExcpetions_H_
#define RTI_EncodingExcpetions_H_

#include <RTI/SpecificConfig.h>
#include <RTI/Exception.h>
#include <RTI/encoding/EncodingConfig.h>

namespace rti1516e
{

   class RTI_EXPORT EncoderException: public Exception
   {
   public:
      EncoderException (std::wstring const & message)
         throw();

      std::wstring what () const
         throw();

   private:
      std::wstring _msg;
   };
}


#endif // RTI_EncodingExcpetions_H_

