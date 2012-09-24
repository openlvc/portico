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
#include "RTI/encoding/HLAopaqueData.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Constructor: Default
// Uses internal memory
HLAopaqueData::HLAopaqueData()
{
}

// Constructor: Initial Value
// Uses internal memory
HLAopaqueData::HLAopaqueData( const Octet* inData, size_t dataSize )
{
}

// Constructor: Use external memory with buffer and data of given lengths.
// This instance changes or reflects changes to contents of external memory.
// Changes to external memory are reflected in subsequent encodings.
// Changes to encoder (i.e., set or decode) are reflected in external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through setDataPointer.
// Buffer length indicates size of memory; data length indicates size of
// data stored in memory.
// Exception is thrown for null memory or zero buffer size.
HLAopaqueData::HLAopaqueData( Octet** inData, size_t bufferSize, size_t dataSize )
	throw( EncoderException )
{
}

// Constructor: Copy
// Uses internal memory
HLAopaqueData::HLAopaqueData( const HLAopaqueData& rhs )
{
}

// Caller is free to delete rhs.
HLAopaqueData::~HLAopaqueData()
{
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the array
std::auto_ptr<DataElement> HLAopaqueData::clone() const
{
	return auto_ptr<DataElement>( new HLAopaqueData() );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAopaqueData::encode() const
	throw( EncoderException )
{
	return VariableLengthData();
}

// Encode this element into an existing VariableLengthData
void HLAopaqueData::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	
}

// Encode this element and append it to a buffer
void HLAopaqueData::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	
}

// Decode this element from the RTI's VariableLengthData.
void HLAopaqueData::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	
}

// Decode this element starting at the index in the provided buffer
size_t HLAopaqueData::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	return 0;
}

// Return the size in bytes of this element's encoding.
size_t HLAopaqueData::getEncodedLength() const
	throw( EncoderException )
{
	return 0;
}

// Return the octet boundary of this element.
unsigned int HLAopaqueData::getOctetBoundary() const
{
	return 0;
}

////////////////////////////////////
// HLAopaqueData Specific Methods //
////////////////////////////////////
// Return the length of the contained buffer
size_t HLAopaqueData::bufferLength() const
{
	return 0;
}

// Return the length of the data stored in the buffer
size_t HLAopaqueData::dataLength() const
{
	return 0;
}

// Change memory to use given external memory
// Changes to this instance will be reflected in external memory
// Caller is responsible for ensuring that the data that is
// pointed to is valid for the lifetime of this object, or past
// the next time this object is given new data.
// Buffer length indicates size of memory; data length indicates size of
// data stored in memory.
// Exception is thrown for null memory or zero buffer size.
void HLAopaqueData::setDataPointer( Octet** inData, size_t bufferSize, size_t dataSize )
	throw( EncoderException )
{
	
}

// Set the data to be encoded.
void HLAopaqueData::set( const Octet* inData, size_t dataSize )
{
	
}

// Return a reference to the contained array
const Octet* HLAopaqueData::get() const
{
	return (Octet*)0;
}

// Conversion operator to std::vector<Octet>
// Value returned is from encoded data.
HLAopaqueData::operator const Octet*() const
{
	return NULL;
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Assignment Operator not allowed
HLAopaqueData& HLAopaqueData::operator= ( const HLAopaqueData& rhs )
{
	return *this;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
