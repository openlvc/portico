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

DEFINE_TYPE_IMPL( HLAbooleanImplementation, bool )

const int HLAfalse = 0;
const int HLAtrue = 1;

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Constructor: Default
// Uses internal memory.
HLAboolean::HLAboolean()
{
	this->_impl = new HLAbooleanImplementation( false );
}

// Constructor: Initial Value
// Uses internal memory.
HLAboolean::HLAboolean( const bool& inData )
{
	this->_impl = new HLAbooleanImplementation( inData );
}

// Constructor: External memory
// This instance changes or is changed by contents of external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through setDataPointer.
// A null value will construct instance to use internal memory.
HLAboolean::HLAboolean( bool* inData )
{
	this->_impl = new HLAbooleanImplementation( inData );
}

// Constructor: Copy
// Uses internal memory.
HLAboolean::HLAboolean( const HLAboolean& rhs )
{
	this->_impl = new HLAbooleanImplementation( rhs.get() );
}

HLAboolean::~HLAboolean()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the DataElement
// Copy uses internal memory.
std::auto_ptr<DataElement> HLAboolean::clone() const
{
	return std::auto_ptr<DataElement>( new HLAboolean(*this) );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAboolean::encode() const
	throw( EncoderException )
{
	VariableLengthData data;
	this->encode( data );

	return data;
}

// Encode this element into an existing VariableLengthData
void HLAboolean::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	char asBytes[BitHelpers::LENGTH_INT];
	BitHelpers::encodeIntBE( this->get() ? HLAtrue : HLAfalse, asBytes, 0 );
	inData.setData( asBytes, BitHelpers::LENGTH_INT );
}

// Encode this element and append it to a buffer
void HLAboolean::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	BitHelpers::encodeIntBE( this->get() ? HLAtrue : HLAfalse, buffer );
}

// Decode this element from the RTI's VariableLengthData.
void HLAboolean::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	if( inData.size() < BitHelpers::LENGTH_INT )
		throw EncoderException( L"Insufficient data in buffer to decode value" );
	int value = BitHelpers::decodeIntBE( (const char*)inData.data(), 0 );
	this->set( value != HLAfalse );
}

// Decode this element starting at the index in the provided buffer
// Return the index immediately after the decoded data.
size_t HLAboolean::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	int value = BitHelpers::decodeIntBE( buffer, index );
	this->set( value != HLAfalse );

	return index + BitHelpers::LENGTH_INT;
}

// Return the size in bytes of this element's encoding.
size_t HLAboolean::getEncodedLength() const
	throw( EncoderException )
{
	return BitHelpers::LENGTH_INT;
}

// Return the octet boundary of this element.
unsigned int HLAboolean::getOctetBoundary() const
{
	return BitHelpers::LENGTH_INT;
}

// Return a hash of the encoded data
// Provides mechanism to map DataElement discriminants to variants
// in VariantRecord.
Integer64 HLAboolean::hash() const
{
	return 31 * 7 + this->get();
}

// Change this instance to use supplied external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through this call.
// Null pointer results in an exception.
void HLAboolean::setDataPointer( bool* inData )
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
void HLAboolean::set( bool inData )
{
	this->_impl->setValue( inData );
}

// Get the value from encoded data.
bool HLAboolean::get() const
{
	return this->_impl->getValue();
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Assignment Operator
// Uses existing memory of this instance.
HLAboolean& HLAboolean::operator= ( const HLAboolean& rhs )
{
	this->_impl->setUseInternalMemory( rhs.get() );
	return *this;
}

// Assignment of the value to be encoded data.
// If this element uses external memory, the memory will be modified.
HLAboolean& HLAboolean::operator= ( bool rhs )
{
	this->set( rhs );
	return *this;
}

// Conversion operator to bool
// Return value from encoded data.
HLAboolean::operator bool() const
{
	return this->get();
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
