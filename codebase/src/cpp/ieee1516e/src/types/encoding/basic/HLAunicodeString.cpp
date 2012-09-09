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
#include "RTI/encoding/BasicDataElements.h"

IEEE1516E_NS_START

struct HLAunicodeStringImplementation
{
	std::wstring value;
};

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Constructor: Default
// Uses internal memory.
HLAunicodeString::HLAunicodeString()
{
	this->_impl = new HLAunicodeStringImplementation();
	this->_impl->value = L"";
}

// Constructor: Initial Value
// Uses internal memory.
HLAunicodeString::HLAunicodeString( const std::wstring& inData )
{
	this->_impl = new HLAunicodeStringImplementation();
	this->_impl->value = std::wstring( inData );
}

// Constructor: External memory
// This instance changes or is changed by contents of external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through setDataPointer.
// A null value will construct instance to use internal memory.
HLAunicodeString::HLAunicodeString( std::wstring* inData )
{
	this->_impl = new HLAunicodeStringImplementation();
	this->_impl->value = std::wstring( *inData );
}

// Constructor: Copy
// Uses internal memory.
HLAunicodeString::HLAunicodeString( const HLAunicodeString& rhs )
{
	this->_impl = new HLAunicodeStringImplementation();
	this->_impl->value = std::wstring( rhs._impl->value );
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
	return VariableLengthData();
}

// Encode this element into an existing VariableLengthData
void HLAunicodeString::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	
}

// Encode this element and append it to a buffer
void HLAunicodeString::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	
}

// Decode this element from the RTI's VariableLengthData.
void HLAunicodeString::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	
}

// Decode this element starting at the index in the provided buffer
// Return the index immediately after the decoded data.
size_t HLAunicodeString::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	return 0;
}

// Return the size in bytes of this element's encoding.
size_t HLAunicodeString::getEncodedLength() const
	throw( EncoderException )
{
	return 0;
}

// Return the octet boundary of this element.
unsigned int HLAunicodeString::getOctetBoundary() const
{
	return 0;
}

// Return a hash of the encoded data
// Provides mechanism to map DataElement discriminants to variants
// in VariantRecord.
Integer64 HLAunicodeString::hash() const
{
	return 0;
}

// Change this instance to use supplied external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through this call.
// Null pointer results in an exception.
void HLAunicodeString::setDataPointer( std::wstring* inData )
	throw( EncoderException )
{
	
}

// Set the value to be encoded.
// If this element uses external memory, the memory will be modified.
void HLAunicodeString::set( std::wstring inData )
{
	this->_impl->value = std::wstring( inData );
}

// Get the value from encoded data.
std::wstring HLAunicodeString::get() const
{
	return this->_impl->value;
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Assignment Operator
// Uses existing memory of this instance.
HLAunicodeString& HLAunicodeString::operator= ( const HLAunicodeString& rhs )
{
	this->_impl->value = std::wstring( rhs._impl->value );
	return *this;
}

// Assignment of the value to be encoded data.
// If this element uses external memory, the memory will be modified.
HLAunicodeString& HLAunicodeString::operator= ( std::wstring rhs )
{
	this->_impl->value = std::wstring( rhs );
	return *this;
}

// Conversion operator to std::wstring
// Return value from encoded data.
HLAunicodeString::operator std::wstring() const
{
	return this->_impl->value;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
