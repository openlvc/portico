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
#include "RTI/encoding/HLAfixedArray.h"
#include "types/encoding/aggregate/HLAfixedArrayImplementation.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Constructor which accepts a prototype element and size
// that specifies the type and number of elements to be stored in the array.
// A clone of the given element works as a prototype.
HLAfixedArray::HLAfixedArray( const DataElement& protoType, size_t length )
{
	this->_impl = new HLAfixedArrayImplementation();
}

// Copy Constructor
// Copied elements use internal memory
HLAfixedArray::HLAfixedArray( const HLAfixedArray& rhs )
{
	this->_impl = new HLAfixedArrayImplementation();
}

// Destructor
HLAfixedArray::~HLAfixedArray()
{
	delete this->_impl;
}

// Private: Default Constructor not allowed
HLAfixedArray::HLAfixedArray()
{
	this->_impl = new HLAfixedArrayImplementation();
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the array
std::auto_ptr<DataElement> HLAfixedArray::clone() const
{
	return auto_ptr<DataElement>( new HLAfixedArray() );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAfixedArray::encode() const
	throw( EncoderException )
{
	return VariableLengthData();
}

// Encode this element into an existing VariableLengthData
void HLAfixedArray::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	
}

// Encode this element and append it to a buffer
void HLAfixedArray::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	
}

// Decode this element from the RTI's VariableLengthData.
void HLAfixedArray::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	
}

// Decode this element starting at the index in the provided buffer
size_t HLAfixedArray::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	return 0;
}

// Return the size in bytes of this element's encoding.
size_t HLAfixedArray::getEncodedLength() const
	throw( EncoderException )
{
	return 0;
}

// Return the octet boundary of this element.
unsigned int HLAfixedArray::getOctetBoundary() const
{
	return 0;
}

// Return true if given element is same type as this; otherwise, false.
bool HLAfixedArray::isSameTypeAs( const DataElement& inData ) const
{
	return false;
}

// Return true if given element matches prototype of this array.
bool HLAfixedArray::hasPrototypeSameTypeAs( const DataElement& dataElement ) const
{
	return false;
}

// Return the number of elements in this fixed array.
size_t HLAfixedArray::size() const
{
	return 0;
}

// Sets the element at the given index to a copy of the given element instance
// Element must match prototype.
// If indexed element uses external memory, the memory will be modified.
void HLAfixedArray::set( size_t index, const DataElement& dataElement )
	throw( EncoderException )
{
	
}

// Sets the element at the given index to the given element instance
// Element must match prototype.
// Null pointer results in an exception.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until the indexed element
// acquires new memory through this call.
void HLAfixedArray::setElementPointer( size_t index, DataElement* dataElement )
	throw( EncoderException )
{
	
}

// Return a reference to the element instance at the specified index.
// Access of indexed element that has not been set will set that index
// with a clone of prototype and return it.
// Must use set to change element.
const DataElement& HLAfixedArray::get( size_t index ) const
	throw( EncoderException )
{
	return *this;
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Return a const reference to the element instance at the specified index.
// Access of indexed element that has not been set will set that index
// with a clone of prototype and return it.
// Must use set to change element.
const DataElement& HLAfixedArray::operator[] ( size_t index ) const
	throw( EncoderException )
{
	return *this;
}

// private
// Assignment Operator not allowed
HLAfixedArray& HLAfixedArray::operator= ( const HLAfixedArray& rhs )
{
	return *this;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
