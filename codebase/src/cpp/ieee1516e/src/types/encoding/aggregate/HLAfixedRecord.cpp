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
#include "RTI/encoding/HLAfixedRecord.h"
#include "types/encoding/aggregate/HLAfixedRecordImplementation.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Default Constructor
HLAfixedRecord::HLAfixedRecord()
{
	this->_impl = new HLAfixedRecordImplementation();
}

// Copy Constructor
HLAfixedRecord::HLAfixedRecord( const HLAfixedRecord& rhs )
{
	this->_impl = new HLAfixedRecordImplementation();
}

// Destructor
HLAfixedRecord::~HLAfixedRecord()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the array
std::auto_ptr<DataElement> HLAfixedRecord::clone() const
{
	return auto_ptr<DataElement>( new HLAfixedRecord() );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAfixedRecord::encode() const
	throw( EncoderException )
{
	return VariableLengthData();
}

// Encode this element into an existing VariableLengthData
void HLAfixedRecord::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	
}

// Encode this element and append it to a buffer
void HLAfixedRecord::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	
}

// Decode this element from the RTI's VariableLengthData.
void HLAfixedRecord::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	
}

// Decode this element starting at the index in the provided buffer
size_t HLAfixedRecord::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	return 0;
}

// Return the size in bytes of this element's encoding.
size_t HLAfixedRecord::getEncodedLength() const
	throw( EncoderException )
{
	return 0;
}

// Return the octet boundary of this element.
unsigned int HLAfixedRecord::getOctetBoundary() const
{
	return 0;
}

// Return true if given element is same type as this; otherwise, false.
bool HLAfixedRecord::isSameTypeAs( const DataElement& inData ) const
{
	return false;
}

// Return the number of elements in this fixed array.
size_t HLAfixedRecord::size() const
{
	return 0;
}

// Sets the element at the given index to a copy of the given element instance
// Element must match prototype.
// If indexed element uses external memory, the memory will be modified.
void HLAfixedRecord::set( size_t index, const DataElement& dataElement )
	throw( EncoderException )
{
	
}

// Sets the element at the given index to the given element instance
// Element must match prototype.
// Null pointer results in an exception.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until the indexed element
// acquires new memory through this call.
void HLAfixedRecord::setElementPointer( size_t index, DataElement* dataElement )
	throw( EncoderException )
{
	
}

// Return a reference to the element instance at the specified index.
// Access of indexed element that has not been set will set that index
// with a clone of prototype and return it.
// Must use set to change element.
const DataElement& HLAfixedRecord::get( size_t index ) const
	throw( EncoderException )
{
	return *this;
}

/////////////////////////////////////
// HLAfixedRecord Specific Methods //
/////////////////////////////////////
// Return true if given element is same type as the indexed element;
// otherwise, false.
bool HLAfixedRecord::hasElementSameTypeAs( size_t index, const DataElement& inData ) const
{
	return false;
}

// Append a copy of the dataElement instance to this fixed record.
void HLAfixedRecord::appendElement( const DataElement& dataElement )
{
	
}

// Append the dataElement instance to this fixed record.
// Null pointer results in an exception.
void HLAfixedRecord::appendElementPointer( DataElement* dataElement )
{
	
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Return a const reference to the element instance at the specified index.
// Access of indexed element that has not been set will set that index
// with a clone of prototype and return it.
// Must use set to change element.
const DataElement& HLAfixedRecord::operator[] ( size_t index ) const
	throw( EncoderException )
{
	return *this;
}

// private
// Assignment Operator not allowed
HLAfixedRecord& HLAfixedRecord::operator= ( const HLAfixedRecord& rhs )
{
	return *this;
}


//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
