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
#include "types/encoding/BitHelpers.h"
#include "types/encoding/TypeImplementation.h"
#include "RTI/encoding/BasicDataElements.h"

IEEE1516E_NS_START

DEFINE_TYPE_IMPL( HLAASCIIcharImplementation, char )

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS
//------------------------------------------------------------------------------------------
// Constructor: Default
// Uses internal memory.
HLAASCIIchar::HLAASCIIchar()
{
	this->_impl = new HLAASCIIcharImplementation( (char)0 );
}

// Constructor: Initial Value
// Uses internal memory.
HLAASCIIchar::HLAASCIIchar( const char& inData )
{
	this->_impl = new HLAASCIIcharImplementation( inData );
}

// Constructor: External memory
// This instance changes or is changed by contents of external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through setDataPointer.
// A null value will construct instance to use internal memory.
HLAASCIIchar::HLAASCIIchar( char* inData )
{
	this->_impl = new HLAASCIIcharImplementation( inData );
}

// Constructor: Copy
// Uses internal memory.
HLAASCIIchar::HLAASCIIchar( const HLAASCIIchar& rhs )
{
	this->_impl = new HLAASCIIcharImplementation( rhs.get() );
}

HLAASCIIchar::~HLAASCIIchar()
{
    if( this->_impl )
        delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the DataElement
// Copy uses internal memory.
std::auto_ptr<DataElement> HLAASCIIchar::clone() const
{
	return std::auto_ptr<DataElement>( new HLAASCIIchar(*this) );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAASCIIchar::encode() const
	throw( EncoderException )
{
    char value = this->get();
	return VariableLengthData( &value, BitHelpers::LENGTH_CHAR );
}

// Encode this element into an existing VariableLengthData
void HLAASCIIchar::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
    char value = this->get();
	inData.setData( &value, BitHelpers::LENGTH_CHAR );
}

// Encode this element and append it to a buffer
void HLAASCIIchar::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
    buffer.push_back( this->get() );
}

// Decode this element from the RTI's VariableLengthData.
void HLAASCIIchar::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	if( inData.size() < BitHelpers::LENGTH_CHAR )
        throw EncoderException( L"Insufficient data in buffer to decode value" );

    char* rawData = (char*)inData.data();
    this->_impl->setValue( *rawData );

}

// Decode this element starting at the index in the provided buffer
// Return the index immediately after the decoded data.
size_t HLAASCIIchar::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
    size_t endIndex = index + BitHelpers::LENGTH_CHAR;
    if( buffer.size() < endIndex )
        throw EncoderException( L"Insufficient data in buffer to decode value" );
    
    this->_impl->setValue( buffer.at(index) );
    
    return endIndex;
}

// Return the size in bytes of this element's encoding.
size_t HLAASCIIchar::getEncodedLength() const
	throw( EncoderException )
{
	return BitHelpers::LENGTH_CHAR;
}

// Return the octet boundary of this element.
unsigned int HLAASCIIchar::getOctetBoundary() const
{
	return BitHelpers::LENGTH_CHAR;
}

// Return a hash of the encoded data
// Provides mechanism to map DataElement discriminants to variants
// in VariantRecord.
Integer64 HLAASCIIchar::hash() const
{
	return 31 * 7 + this->get();
}

// Change this instance to use supplied external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through this call.
// Null pointer results in an exception.
void HLAASCIIchar::setDataPointer( char* inData )
	throw( EncoderException )
{
	if( inData )
    {
        this->_impl->setUseExternalMemory( inData );
    }
    else
    {
        throw EncoderException( L"NULL inData pointer provided to setDataPointer" );
    }
}

// Set the value to be encoded.
// If this element uses external memory, the memory will be modified.
void HLAASCIIchar::set( char inData )
{
    this->_impl->setValue( inData );
}

// Get the value from encoded data.
char HLAASCIIchar::get() const
{
	return this->_impl->getValue();
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Assignment Operator
// Uses existing memory of this instance.
HLAASCIIchar& HLAASCIIchar::operator= ( const HLAASCIIchar& rhs )
{
    this->_impl->setUseInternalMemory( rhs.get() );
	return *this;
}

// Assignment of the value to be encoded data.
// If this element uses external memory, the memory will be modified.
HLAASCIIchar& HLAASCIIchar::operator= ( char rhs )
{
    this->set( rhs );
	return *this;
}

// Conversion operator to char
// Return value from encoded data.
HLAASCIIchar::operator char() const
{
	return this->get();
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
