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
#include "types/encoding/TypeImplementation.h"
#include "types/encoding/BitHelpers.h"
#include "RTI/encoding/BasicDataElements.h"

IEEE1516E_NS_START

DEFINE_TYPE_IMPL( HLAASCIIstringImplementation, std::string )

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Constructor: Default
// Uses internal memory.
HLAASCIIstring::HLAASCIIstring()
{
	this->_impl = new HLAASCIIstringImplementation( "" );
}

// Constructor: Initial Value
// Uses internal memory.
HLAASCIIstring::HLAASCIIstring( const std::string& inData )
{
	this->_impl = new HLAASCIIstringImplementation( inData );
}

// Constructor: External memory
// This instance changes or is changed by contents of external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through setDataPointer.
// A null value will construct instance to use internal memory.
HLAASCIIstring::HLAASCIIstring( std::string* inData )
{
	this->_impl = new HLAASCIIstringImplementation( inData );
}

// Constructor: Copy
// Uses internal memory.
HLAASCIIstring::HLAASCIIstring( const HLAASCIIstring& rhs )
{
	this->_impl = new HLAASCIIstringImplementation( rhs.get() );
}

HLAASCIIstring::~HLAASCIIstring()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the DataElement
// Copy uses internal memory.
std::auto_ptr<DataElement> HLAASCIIstring::clone() const
{
	return std::auto_ptr<DataElement>( new HLAASCIIstring(*this) );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAASCIIstring::encode() const
	throw( EncoderException )
{
	VariableLengthData data;
	this->encode( data );

	return data;
}

// Encode this element into an existing VariableLengthData
void HLAASCIIstring::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	std::string value = this->get();
	size_t len = value.size();
	size_t size = len * BitHelpers::LENGTH_CHAR;
	size_t totalLength = BitHelpers::LENGTH_INT + size;

	// Assign a buffer to take an int (for the strlen) + the data
	char* buffer = new char[totalLength];

	// Encode the size
	BitHelpers::encodeIntBE( len, buffer, 0 );

	// Encode the data
	const char* asBytes = this->get().c_str();
	::memcpy( buffer + BitHelpers::LENGTH_INT, asBytes, size );
	
	// Call to setData will take a copy
	inData.setData( buffer, totalLength );

	// Clean up!
	delete [] buffer;
}

// Encode this element and append it to a buffer
void HLAASCIIstring::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	std::string value = this->get();

	// Encode strlen
	size_t len = value.size();
	BitHelpers::encodeIntBE( value.size(), buffer );

	const char* asBytes = value.c_str();
	buffer.insert( buffer.end(), asBytes, asBytes + len );
}

// Decode this element from the RTI's VariableLengthData.
void HLAASCIIstring::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	size_t availableLength = inData.size();
	
	// Have to initially be able to get an int out of the buffer
	if( availableLength < BitHelpers::LENGTH_INT )
		throw EncoderException( L"Insufficient data in buffer to decode value" );

	// Read the string length from the buffer
	char* asBytes = (char*)inData.data();
	size_t len = BitHelpers::decodeIntBE( asBytes, 0 );

	if( (availableLength - BitHelpers::LENGTH_INT) < len )
		throw EncoderException( L"Insufficient data in buffer to decode value" );

	std::string value = std::string( asBytes + BitHelpers::LENGTH_INT, len );
	this->set( value );
}

// Decode this element starting at the index in the provided buffer
// Return the index immediately after the decoded data.
size_t HLAASCIIstring::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	size_t len = BitHelpers::decodeIntBE( buffer, index );
	size_t size = len * BitHelpers::LENGTH_CHAR;
	size_t stringStartIndex = index + BitHelpers::LENGTH_INT;

	if( stringStartIndex + size > buffer.size() )
		throw EncoderException( L"Insufficient data in buffer to decode value" );

	const char* stringStartPtr = buffer.data() + stringStartIndex;

	std::string value( stringStartPtr, len );
	this->set( value );

	return stringStartIndex + (len * BitHelpers::LENGTH_CHAR);
}

// Return the size in bytes of this element's encoding.
size_t HLAASCIIstring::getEncodedLength() const
	throw( EncoderException )
{
	return BitHelpers::LENGTH_INT + this->get().size();
}

// Return the octet boundary of this element.
unsigned int HLAASCIIstring::getOctetBoundary() const
{
	return getEncodedLength();
}

// Return a hash of the encoded data
// Provides mechanism to map DataElement discriminants to variants
// in VariantRecord.
Integer64 HLAASCIIstring::hash() const
{
	Integer64 hash = 7;

	const std::string value = this->get();
	for( size_t i = 0 ; i < value.length() ; ++i )
		hash = 31 * hash + value.at(i);

	return hash;
}

// Change this instance to use supplied external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through this call.
// Null pointer results in an exception.
void HLAASCIIstring::setDataPointer( std::string* inData )
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
void HLAASCIIstring::set( std::string inData )
{
	this->_impl->setValue( inData );
}

// Get the value from encoded data.
std::string HLAASCIIstring::get() const
{
    return this->_impl->getValue();
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Assignment Operator
// Uses existing memory of this instance.
HLAASCIIstring& HLAASCIIstring::operator= ( const HLAASCIIstring& rhs )
{
	this->_impl->setUseInternalMemory( rhs.get() );
	return *this;
}

// Assignment of the value to be encoded data.
// If this element uses external memory, the memory will be modified.
HLAASCIIstring& HLAASCIIstring::operator= ( std::string rhs )
{
	this->set( rhs );
	return *this;
}

// Conversion operator to std::string
// Return value from encoded data.
HLAASCIIstring::operator std::string() const
{
	return this->get();
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
