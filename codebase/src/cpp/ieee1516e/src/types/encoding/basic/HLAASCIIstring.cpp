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

struct HLAASCIIstringImplementation
{
	std::string value;
};

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Constructor: Default
// Uses internal memory.
HLAASCIIstring::HLAASCIIstring()
{
	this->_impl = new HLAASCIIstringImplementation();
	this->_impl->value = std::string("");
}

// Constructor: Initial Value
// Uses internal memory.
HLAASCIIstring::HLAASCIIstring( const std::string& inData )
{
	this->_impl = new HLAASCIIstringImplementation();
	this->_impl->value = std::string( inData );
}

// Constructor: External memory
// This instance changes or is changed by contents of external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through setDataPointer.
// A null value will construct instance to use internal memory.
HLAASCIIstring::HLAASCIIstring( std::string* inData )
{
	this->_impl = new HLAASCIIstringImplementation();
	this->_impl->value = std::string( *inData );
}

// Constructor: Copy
// Uses internal memory.
HLAASCIIstring::HLAASCIIstring( const HLAASCIIstring& rhs )
{
	this->_impl = new HLAASCIIstringImplementation();
	this->_impl->value = std::string(rhs._impl->value);
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
	return VariableLengthData();
}

// Encode this element into an existing VariableLengthData
void HLAASCIIstring::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	
}

// Encode this element and append it to a buffer
void HLAASCIIstring::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	
}

// Decode this element from the RTI's VariableLengthData.
void HLAASCIIstring::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	
}

// Decode this element starting at the index in the provided buffer
// Return the index immediately after the decoded data.
size_t HLAASCIIstring::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	return 0;
}

// Return the size in bytes of this element's encoding.
size_t HLAASCIIstring::getEncodedLength() const
	throw( EncoderException )
{
	return 0;
}

// Return the octet boundary of this element.
unsigned int HLAASCIIstring::getOctetBoundary() const
{
	return 0;
}

// Return a hash of the encoded data
// Provides mechanism to map DataElement discriminants to variants
// in VariantRecord.
Integer64 HLAASCIIstring::hash() const
{
	return 0;
}

// Change this instance to use supplied external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through this call.
// Null pointer results in an exception.
void HLAASCIIstring::setDataPointer( std::string* inData )
	throw( EncoderException )
{
	
}

// Set the value to be encoded.
// If this element uses external memory, the memory will be modified.
void HLAASCIIstring::set( std::string inData )
{
	this->_impl->value = std::string(inData);
}

// Get the value from encoded data.
std::string HLAASCIIstring::get() const
{
	return this->_impl->value;
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Assignment Operator
// Uses existing memory of this instance.
HLAASCIIstring& HLAASCIIstring::operator= ( const HLAASCIIstring& rhs )
{
	this->_impl->value = std::string( rhs._impl->value );
	return *this;
}

// Assignment of the value to be encoded data.
// If this element uses external memory, the memory will be modified.
HLAASCIIstring& HLAASCIIstring::operator= ( std::string rhs )
{
	this->_impl->value = std::string( rhs );
	return *this;
}

// Conversion operator to std::string
// Return value from encoded data.
HLAASCIIstring::operator std::string() const
{
	return this->_impl->value;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
