/*
 *   Copyright 2012 The Portico Project
 *
 *   This file is part of portico.
 *
 *   portico is free software; you can redistribute it and/or modify
 *   it under the terms of the Common Developer and Distribution License (CDDL)
 *   as published by Sun Microsystems. For more information see the LICENSE file.
 *
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *   (that goes for your lawyer as well)
 *
 */
#include "common.h"
#include "RTI/encoding/HLAvariantRecord.h"
#include "types/encoding/aggregate/HLAvariantRecordImplementation.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Constructor which accepts a prototype element for discriminants.
// A clone of the given element acts to validate the discriminant type.
HLAvariantRecord::HLAvariantRecord( const DataElement& discriminantPrototype )
{
	this->_impl = new HLAvariantRecordImplementation();
}

// Copy Constructor
HLAvariantRecord::HLAvariantRecord( const HLAvariantRecord& rhs )
{
	this->_impl = new HLAvariantRecordImplementation();
}

// Destructor
HLAvariantRecord::~HLAvariantRecord()
{
	delete this->_impl;
}

// Private
// Default constructor not allowed
HLAvariantRecord::HLAvariantRecord()
{
	this->_impl = new HLAvariantRecordImplementation();
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the array
std::auto_ptr<DataElement> HLAvariantRecord::clone() const
{
	return auto_ptr<DataElement>( new HLAvariantRecord() );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAvariantRecord::encode() const
	throw( EncoderException )
{
	return VariableLengthData();
}

// Encode this element into an existing VariableLengthData
void HLAvariantRecord::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	
}

// Encode this element and append it to a buffer
void HLAvariantRecord::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	
}

// Decode this element from the RTI's VariableLengthData.
void HLAvariantRecord::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	
}

// Decode this element starting at the index in the provided buffer
size_t HLAvariantRecord::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	return 0;
}

// Return the size in bytes of this element's encoding.
size_t HLAvariantRecord::getEncodedLength() const
	throw( EncoderException )
{
	return 0;
}

// Return the octet boundary of this element.
unsigned int HLAvariantRecord::getOctetBoundary() const
{
	return 0;
}

// Return true if given element is same type as this; otherwise, false.
bool HLAvariantRecord::isSameTypeAs( const DataElement& inData ) const
{
	return false;
}

///////////////////////////////////////
// HLAvariantRecord Specific Methods //
///////////////////////////////////////
// Return true if given element is same type as specified variant; otherwise, false.
bool HLAvariantRecord::isSameTypeAs( const DataElement& discriminant, 
                                     const DataElement& inData ) const
	throw( EncoderException )
{
	return false;
}

// Return true if given element matches prototype of this array.
bool HLAvariantRecord::hasMatchingDiscriminantTypeAs( const DataElement& dataElement ) const
{
	return false;
}

// Add a new discriminant/variant pair: adds a mapping between the given
// unique discriminant and a copy of the value element.
// When encoding, the last discriminant specified (either by adding or setDescriminant)
// determines the value to be encoded.
// When decoding, the encoded discriminant will determine which variant is
// used. The getDescriminant call indicates the variant data element returned
// by getValue.
// Discriminants must match prototype
void HLAvariantRecord::addVariant( const DataElement& discriminant,
                                   const DataElement& valuePrototype )
	throw( EncoderException )
{
}

// Add a new discriminant/variant pair: adds a mapping between the given
// unique discriminant and the given value element.
// When encoding, the last discriminant specified (either by adding or
// setVariant, or setDescriminant) determines the value to be encoded.
// When decoding, the encoded discriminant will determine which variant is
// used. The getDescriminant call indicates the variant data element
// returned by getValue.
//
// Discriminants must match prototype
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until the variant for the
// given discriminant acquires new memory through setVariantPointer.
// Null pointer results in an exception.
void HLAvariantRecord::addVariantPointer( const DataElement& discriminant,
                                          DataElement* valuePtr )
	throw( EncoderException )
{

}

// Set the current value of the discriminant (specifies the type of the value)
// Discriminants must match prototype
void HLAvariantRecord::setDiscriminant( const DataElement& discriminant )
	throw( EncoderException )
{

}

// Sets the variant with the given discriminant to a copy of the given value
// Discriminant must match prototype and value must match its variant
void HLAvariantRecord::setVariant( const DataElement& discriminant,
                                   const DataElement& value )
	throw( EncoderException )
{

}

// Sets the variant with the given discriminant to the given value
// Discriminant must match prototype and value must match its variant
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until the variant for the
// given discriminant acquires new memory through this call.
// Null pointer results in an exception.
void HLAvariantRecord::setVariantPointer( const DataElement& discriminant,
                                          DataElement* valuePtr )
	throw( EncoderException )
{

}

// Return a reference to the discriminant element
const DataElement& HLAvariantRecord::getDiscriminant() const
{
	return *this;
}

// Return a reference to the variant element.
// Exception thrown if encoded discriminant is not mapped to a value.
const DataElement& HLAvariantRecord::getVariant() const
	throw( EncoderException )
{
	return *this;
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Private
// Assignment Operator not allowed
HLAvariantRecord& HLAvariantRecord::operator= ( const HLAvariantRecord& rhs )
{
	return *this;
}


//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
