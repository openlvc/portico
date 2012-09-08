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
#include "RTI/encoding/HLAvariableArray.h"
#include "types/encoding/aggregate/HLAvariableArrayImplementation.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Constructor which accepts a prototype element
// that specifies the type of elements to be stored in the array.
// A clone of the given element works as a seed.
HLAvariableArray::HLAvariableArray( const DataElement& prototype )
{
	this->_impl = new HLAvariableArrayImplementation();
}

// Copy Constructor
HLAvariableArray::HLAvariableArray( const HLAvariableArray& rhs )
{
	this->_impl = new HLAvariableArrayImplementation();
}

// Destructor
HLAvariableArray::~HLAvariableArray()
{
	delete this->_impl;
}

// Private
HLAvariableArray::HLAvariableArray()
{
	this->_impl = new HLAvariableArrayImplementation();
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the array
std::auto_ptr<DataElement> HLAvariableArray::clone() const
{
	return auto_ptr<DataElement>( new HLAvariableArray() );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAvariableArray::encode() const
	throw( EncoderException )
{
	return VariableLengthData();
}

// Encode this element into an existing VariableLengthData
void HLAvariableArray::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	
}

// Encode this element and append it to a buffer
void HLAvariableArray::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	
}

// Decode this element from the RTI's VariableLengthData.
void HLAvariableArray::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	
}

// Decode this element starting at the index in the provided buffer
size_t HLAvariableArray::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	return 0;
}

// Return the size in bytes of this element's encoding.
size_t HLAvariableArray::getEncodedLength() const
	throw( EncoderException )
{
	return 0;
}

// Return the octet boundary of this element.
unsigned int HLAvariableArray::getOctetBoundary() const
{
	return 0;
}

// Return true if given element is same type as this; otherwise, false.
bool HLAvariableArray::isSameTypeAs( const DataElement& inData ) const
{
	return false;
}

// Return true if given element matches prototype of this array.
bool HLAvariableArray::hasPrototypeSameTypeAs( const DataElement& dataElement ) const
{
	return false;
}

// Return the number of elements in this fixed array.
size_t HLAvariableArray::size() const
{
	return 0;
}

// Sets the element at the given index to a copy of the given element instance
// Element must match prototype.
// If indexed element uses external memory, the memory will be modified.
void HLAvariableArray::set( size_t index, const DataElement& dataElement )
	throw( EncoderException )
{
	
}

// Sets the element at the given index to the given element instance
// Element must match prototype.
// Null pointer results in an exception.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until the indexed element
// acquires new memory through this call.
void HLAvariableArray::setElementPointer( size_t index, DataElement* dataElement )
	throw( EncoderException )
{
	
}

// Return a reference to the element instance at the specified index.
// Access of indexed element that has not been set will set that index
// with a clone of prototype and return it.
// Must use set to change element.
const DataElement& HLAvariableArray::get( size_t index ) const
	throw( EncoderException )
{
	return *this;
}

///////////////////////////////////////
// HLAvariableArray Specific Methods //
///////////////////////////////////////
// Adds a copy of the given element instance to this array
// Element must match prototype.
void HLAvariableArray::addElement( const DataElement& dataElement )
	throw( EncoderException )
{
	
}

// Adds the given element instance to this variable array
// Element must match prototype.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until the indexed element
// acquires new memory through the set method.
// Null pointer results in an exception.
void HLAvariableArray::addElementPointer( DataElement* dataElement )
	throw( EncoderException )
{
	
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Return a const reference to the element instance at the specified index.
// Access of indexed element that has not been set will set that index
// with a clone of prototype and return it.
// Must use set to change element.
const DataElement& HLAvariableArray::operator[] ( size_t index ) const
	throw( EncoderException )
{
	return *this;
}

// private
// Assignment Operator not allowed
HLAvariableArray& HLAvariableArray::operator= ( const HLAvariableArray& rhs )
{
	return *this;
}


//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
