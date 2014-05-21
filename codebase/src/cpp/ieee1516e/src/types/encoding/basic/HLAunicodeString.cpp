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
#include "types/encoding/basic/BasicTypeImpl.h"
#include "RTI/encoding/BasicDataElements.h"

IEEE1516E_NS_START

DEFINE_BASIC_TYPE_IMPL( HLAunicodeStringImplementation, std::wstring )

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Constructor: Default
// Uses internal memory.
HLAunicodeString::HLAunicodeString()
{
	this->_impl = new HLAunicodeStringImplementation( L"" );
}

// Constructor: Initial Value
// Uses internal memory.
HLAunicodeString::HLAunicodeString( const std::wstring& inData )
{
	this->_impl = new HLAunicodeStringImplementation( inData );
}

// Constructor: External memory
// This instance changes or is changed by contents of external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through setDataPointer.
// A null value will construct instance to use internal memory.
HLAunicodeString::HLAunicodeString( std::wstring* inData )
{
	this->_impl = new HLAunicodeStringImplementation( inData );
}

// Constructor: Copy
// Uses internal memory.
HLAunicodeString::HLAunicodeString( const HLAunicodeString& rhs )
{
	this->_impl = new HLAunicodeStringImplementation( rhs.get() );
}

HLAunicodeString::~HLAunicodeString()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the DataElement
// Copy uses internal memory.
std::auto_ptr<DataElement> HLAunicodeString::clone() const
{
	return std::auto_ptr<DataElement>( new HLAunicodeString(*this) );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAunicodeString::encode() const
	throw( EncoderException )
{
	VariableLengthData data;
	this->encode( data );

	return data;
}

// Encode this element into an existing VariableLengthData
void HLAunicodeString::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	// Assign a buffer to take the std::wstring
	const std::wstring value = this->get();
	size_t encodedLength = BitHelpers::getEncodedLength( value );
	char* data = new char[encodedLength];

	// Encode to buffer
	BitHelpers::encodeUnicodeString( value, data, 0 );

	// Set data into VariableLengthData
	inData.setData( data, encodedLength );

	// Clean up!
	delete [] data;
}

// Encode this element and append it to a buffer
void HLAunicodeString::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	// Encode into VariableLengthData
	VariableLengthData data;
	this->encode( data );

	// Append data to the end of the provided buffer
	char* bytes = (char*)data.data();
	buffer.insert( buffer.end(), bytes, bytes + data.size() );
}

// Decode this element from the RTI's VariableLengthData.
void HLAunicodeString::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	// Wrap the VariableLengthData's internal byte array in a std::vector<Octet>
	const char* bytes = (const char*)inData.data();
	std::vector<Octet> buffer( bytes, bytes + inData.size() );

	// Decode!
	this->decodeFrom( buffer, 0 );
}

// Decode this element starting at the index in the provided buffer
// Return the index immediately after the decoded data.
size_t HLAunicodeString::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	// Are there enough bytes to read in the character length?
	if( index + BitHelpers::LENGTH_INT > buffer.size() )
		throw EncoderException( L"Insufficient data in buffer to decode value" );

	// Read in the character length
	const char* data = (const char*)&buffer[index];
	size_t length = BitHelpers::decodeIntBE( data, 0 );

	// Are there enough bytes to read in the string?
	if( index + BitHelpers::LENGTH_INT + (length * BitHelpers::LENGTH_SHORT) > buffer.size() )
		throw EncoderException( L"Insufficient data in buffer to decode value" );

	if( length > 0 )
	{
		wchar_t* chars = new wchar_t[length - 1];

		// Read in the characters, excluding the Unicode BOM at the start
		for( size_t i = 1 ; i < length ; ++i )
		{
			size_t iIndex = BitHelpers::LENGTH_INT + (i * BitHelpers::LENGTH_SHORT);
			chars[i - 1] = (wchar_t)BitHelpers::decodeShortBE( data, iIndex );
		}

		// Construct a wstring, and assign it as the current value
		std::wstring value( chars, length - 1 );
		this->set( value );

		// Clean up temp array
		delete [] chars;
	}

	return index + BitHelpers::LENGTH_INT + (length * BitHelpers::LENGTH_SHORT);
}

// Return the size in bytes of this element's encoding.
size_t HLAunicodeString::getEncodedLength() const
	throw( EncoderException )
{
	return BitHelpers::getEncodedLength( this->get() );
}

// Return the octet boundary of this element.
unsigned int HLAunicodeString::getOctetBoundary() const
{
	return BitHelpers::getEncodedLength( this->get() );
}

// Return a hash of the encoded data
// Provides mechanism to map DataElement discriminants to variants
// in VariantRecord.
Integer64 HLAunicodeString::hash() const
{
	Integer64 hash = 7;

	const std::wstring value = this->get();
	for( size_t i = 0 ; i < value.length() ; ++i )
		hash = 31 * hash + value.at(i);

	return hash;
}

// Change this instance to use supplied external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through this call.
// Null pointer results in an exception.
void HLAunicodeString::setDataPointer( std::wstring* inData )
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
void HLAunicodeString::set( std::wstring inData )
{
	this->_impl->setValue( inData );
}

// Get the value from encoded data.
std::wstring HLAunicodeString::get() const
{
	return this->_impl->getValue();
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Assignment Operator
// Uses existing memory of this instance.
HLAunicodeString& HLAunicodeString::operator= ( const HLAunicodeString& rhs )
{
	this->_impl->setUseInternalMemory( rhs.get() );
	return *this;
}

// Assignment of the value to be encoded data.
// If this element uses external memory, the memory will be modified.
HLAunicodeString& HLAunicodeString::operator= ( std::wstring rhs )
{
	this->set( rhs );
	return *this;
}

// Conversion operator to std::wstring
// Return value from encoded data.
HLAunicodeString::operator std::wstring() const
{
	return this->get();
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
