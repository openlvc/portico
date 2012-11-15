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

DEFINE_BASIC_TYPE_IMPL( HLAunicodeCharImplementation, wchar_t )

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Constructor: Default
// Uses internal memory.
HLAunicodeChar::HLAunicodeChar()
{
	this->_impl = new HLAunicodeCharImplementation( (wchar_t)0 );
}

// Constructor: Initial Value
// Uses internal memory.
HLAunicodeChar::HLAunicodeChar( const wchar_t& inData )
{
	this->_impl = new HLAunicodeCharImplementation( inData );
}

// Constructor: External memory
// This instance changes or is changed by contents of external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through setDataPointer.
// A null value will construct instance to use internal memory.
HLAunicodeChar::HLAunicodeChar( wchar_t* inData )
{
	this->_impl = new HLAunicodeCharImplementation( inData );
}

// Constructor: Copy
// Uses internal memory.
HLAunicodeChar::HLAunicodeChar( const HLAunicodeChar& rhs )
{
	this->_impl = new HLAunicodeCharImplementation( rhs.get() );
}

HLAunicodeChar::~HLAunicodeChar()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the DataElement
// Copy uses internal memory.
std::auto_ptr<DataElement> HLAunicodeChar::clone() const
{
	return std::auto_ptr<DataElement>( new HLAunicodeChar(*this) );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAunicodeChar::encode() const
	throw( EncoderException )
{
	VariableLengthData data;
	this->encode( data );

	return data;
}

// Encode this element into an existing VariableLengthData
void HLAunicodeChar::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	// Assign a buffer to take the wchar_t
	char buffer[BitHelpers::LENGTH_WCHAR];
	BitHelpers::encodeShortBE( (short)this->get(), buffer, 0 );

	inData.setData( buffer, BitHelpers::LENGTH_WCHAR );
}

// Encode this element and append it to a buffer
void HLAunicodeChar::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	char data[BitHelpers::LENGTH_WCHAR];
	BitHelpers::encodeShortBE( (short)this->get(), data, 0 );

	buffer.insert( buffer.end(), data, data + BitHelpers::LENGTH_WCHAR );
}

// Decode this element from the RTI's VariableLengthData.
void HLAunicodeChar::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	if( inData.size() < BitHelpers::LENGTH_WCHAR )
		throw EncoderException( L"Insufficient data in buffer to decode value" );

	wchar_t value = (wchar_t)BitHelpers::decodeShortBE( (const char*)inData.data(), 0 );
	this->set( value );
}

// Decode this element starting at the index in the provided buffer
// Return the index immediately after the decoded data.
size_t HLAunicodeChar::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	wchar_t value = (wchar_t)BitHelpers::decodeShortBE( buffer, index );
	this->set( value );
	return index + BitHelpers::LENGTH_WCHAR;
}

// Return the size in bytes of this element's encoding.
size_t HLAunicodeChar::getEncodedLength() const
	throw( EncoderException )
{
	return BitHelpers::LENGTH_WCHAR;
}

// Return the octet boundary of this element.
unsigned int HLAunicodeChar::getOctetBoundary() const
{
	return BitHelpers::LENGTH_WCHAR;
}

// Return a hash of the encoded data
// Provides mechanism to map DataElement discriminants to variants
// in VariantRecord.
Integer64 HLAunicodeChar::hash() const
{
	return 31 * 7 + this->get();
}

// Change this instance to use supplied external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through this call.
// Null pointer results in an exception.
void HLAunicodeChar::setDataPointer( wchar_t* inData )
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
void HLAunicodeChar::set( wchar_t inData )
{
	this->_impl->setValue( inData );
}

// Get the value from encoded data.
wchar_t HLAunicodeChar::get() const
{
	return this->_impl->getValue();
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Assignment Operator
// Uses existing memory of this instance.
HLAunicodeChar& HLAunicodeChar::operator= ( const HLAunicodeChar& rhs )
{
	this->_impl->setUseInternalMemory( rhs.get() );
	return *this;
}

// Assignment of the value to be encoded data.
// If this element uses external memory, the memory will be modified.
HLAunicodeChar& HLAunicodeChar::operator= ( wchar_t rhs )
{
	this->set( rhs );
	return *this;
}

// Conversion operator to wchar_t
// Return value from encoded data.
HLAunicodeChar::operator wchar_t() const
{
	return this->get();
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
