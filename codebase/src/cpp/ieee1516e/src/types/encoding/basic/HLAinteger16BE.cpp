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

struct HLAinteger16BEImplementation
{
	Integer16 value;
};

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Constructor: Default
// Uses internal memory.
HLAinteger16BE::HLAinteger16BE()
{
	this->_impl = new HLAinteger16BEImplementation();
	this->_impl->value = 0;
}

// Constructor: Initial Value
// Uses internal memory.
HLAinteger16BE::HLAinteger16BE( const Integer16& inData )
{
	this->_impl = new HLAinteger16BEImplementation();
	this->_impl->value = inData;
}

// Constructor: External memory
// This instance changes or is changed by contents of external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through setDataPointer.
// A null value will construct instance to use internal memory.
HLAinteger16BE::HLAinteger16BE( Integer16* inData )
{
	this->_impl = new HLAinteger16BEImplementation();
	this->_impl->value = *inData;
}

// Constructor: Copy
// Uses internal memory.
HLAinteger16BE::HLAinteger16BE( const HLAinteger16BE& rhs )
{
	this->_impl = new HLAinteger16BEImplementation();
	this->_impl->value = rhs._impl->value;
}

HLAinteger16BE::~HLAinteger16BE()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the DataElement
// Copy uses internal memory.
std::auto_ptr<DataElement> HLAinteger16BE::clone() const
{
	return std::auto_ptr<DataElement>( new HLAinteger16BE(*this) );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAinteger16BE::encode() const
	throw( EncoderException )
{
	return VariableLengthData();
}

// Encode this element into an existing VariableLengthData
void HLAinteger16BE::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	
}

// Encode this element and append it to a buffer
void HLAinteger16BE::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	
}

// Decode this element from the RTI's VariableLengthData.
void HLAinteger16BE::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	
}

// Decode this element starting at the index in the provided buffer
// Return the index immediately after the decoded data.
size_t HLAinteger16BE::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	return 0;
}

// Return the size in bytes of this element's encoding.
size_t HLAinteger16BE::getEncodedLength() const
	throw( EncoderException )
{
	return 0;
}

// Return the octet boundary of this element.
unsigned int HLAinteger16BE::getOctetBoundary() const
{
	return 0;
}

// Return a hash of the encoded data
// Provides mechanism to map DataElement discriminants to variants
// in VariantRecord.
Integer64 HLAinteger16BE::hash() const
{
	return this->_impl->value;
}

// Change this instance to use supplied external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through this call.
// Null pointer results in an exception.
void HLAinteger16BE::setDataPointer( Integer16* inData )
	throw( EncoderException )
{
	
}

// Set the value to be encoded.
// If this element uses external memory, the memory will be modified.
void HLAinteger16BE::set( Integer16 inData )
{
	this->_impl->value = inData;
}

// Get the value from encoded data.
Integer16 HLAinteger16BE::get() const
{
	return this->_impl->value;
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Assignment Operator
// Uses existing memory of this instance.
HLAinteger16BE& HLAinteger16BE::operator= ( const HLAinteger16BE& rhs )
{
	this->_impl->value = rhs._impl->value;
	return *this;
}

// Assignment of the value to be encoded data.
// If this element uses external memory, the memory will be modified.
HLAinteger16BE& HLAinteger16BE::operator= ( Integer16 rhs )
{
	this->_impl->value = rhs;
	return *this;
}

// Conversion operator to Integer16
// Return value from encoded data.
HLAinteger16BE::operator Integer16() const
{
	return this->_impl->value;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
