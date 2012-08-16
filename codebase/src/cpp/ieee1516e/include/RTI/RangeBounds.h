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
   File: RTI/RangeBounds.h
***********************************************************************/

#ifndef RTI_RangeBounds_h
#define RTI_RangeBounds_h

#include <RTI/SpecificConfig.h>

namespace rti1516e
{
   class RTI_EXPORT RangeBounds
   {
   public:
      RangeBounds ();

      RangeBounds (
         unsigned long lowerBound,
         unsigned long upperBound);

      ~RangeBounds ()
         throw ();

      RangeBounds (
         RangeBounds const & rhs);

      RangeBounds & operator= (
         RangeBounds const & rhs);

      unsigned long getLowerBound () const;

      unsigned long getUpperBound () const;

      void setLowerBound (
         unsigned long lowerBound);

      void setUpperBound (
         unsigned long upperBound);

   private:
      unsigned long _lowerBound;
      unsigned long _upperBound;
   };
}

#endif // RTI_RangeBounds_h
