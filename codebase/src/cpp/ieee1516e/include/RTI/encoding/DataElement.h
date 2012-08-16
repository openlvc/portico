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
   File: DataElement.h
***********************************************************************/
#ifndef RTI_DataElement_h_
#define RTI_DataElement_h_

#include <RTI/SpecificConfig.h>
#include <RTI/encoding/EncodingConfig.h>
#include <RTI/encoding/EncodingExceptions.h>
#include <memory>


namespace rti1516e
{
   // Forward Declarations
   class VariableLengthData;

   // Interface provided by all HLA data elements.
   class RTI_EXPORT DataElement
   {
   public:

      // Destructor
      virtual ~DataElement () = 0;

      // Return a new copy of the DataElement
      virtual std::auto_ptr<DataElement> clone () const = 0;

      // Encode this element into a new VariableLengthData
      virtual VariableLengthData encode () const
         throw (EncoderException) = 0;

      // Encode this element into an existing VariableLengthData
      virtual void encode (
         VariableLengthData& inData) const
         throw (EncoderException) = 0;

      // Encode this element and append it to a buffer
      virtual void encodeInto (
         std::vector<Octet>& buffer) const
         throw (EncoderException) = 0;

      // Decode this element from the RTI's VariableLengthData.
      virtual void decode (
         VariableLengthData const & inData)
         throw (EncoderException) = 0;

      // Decode this element starting at the index in the provided buffer
      virtual size_t decodeFrom (
         std::vector<Octet> const & buffer,
         size_t index)
         throw (EncoderException) = 0;

      // Return the size in bytes of this element's encoding.
      virtual size_t getEncodedLength () const
         throw (EncoderException) = 0;

      // Return the octet boundary of this element.
      virtual unsigned int getOctetBoundary () const = 0;

      // Return true if given element is same type as this; otherwise, false.
      virtual bool isSameTypeAs(
         DataElement const& inData ) const;

      // Return a hash of the encoded data
      // Provides mechanism to map DataElement discriminants to variants
      // in VariantRecord.
      virtual Integer64 hash() const;

   };
}

#endif // RTI_DataElement_h_

