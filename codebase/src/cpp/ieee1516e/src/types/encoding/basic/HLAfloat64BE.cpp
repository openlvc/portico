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

DEFINE_TYPE_IMPL( HLAfloat64BEImplementation, double )

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Constructor: Default
// Uses internal memory.
HLAfloat64BE::HLAfloat64BE()
{
	this->_impl = new HLAfloat64BEImplementation( 0.0 );
}

// Constructor: Initial Value
// Uses internal memory.
HLAfloat64BE::HLAfloat64BE( const double& inData )
{
	this->_impl = new HLAfloat64BEImplementation( inData );
}

// Constructor: External memory
// This instance changes or is changed by contents of external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through setDataPointer.
// A null value will construct instance to use internal memory.
HLAfloat64BE::HLAfloat64BE( double* inData )
{
	this->_impl = new HLAfloat64BEImplementation( inData );
}

// Constructor: Copy
// Uses internal memory.
HLAfloat64BE::HLAfloat64BE( const HLAfloat64BE& rhs )
{
	this->_impl = new HLAfloat64BEImplementation( rhs.get() );
}

HLAfloat64BE::~HLAfloat64BE()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the DataElement
// Copy uses internal memory.
std::auto_ptr<DataElement> HLAfloat64BE::clone() const
{
	return std::auto_ptr<DataElement>( new HLAfloat64BE(*this) );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAfloat64BE::encode() const
	throw( EncoderException )
{
	VariableLengthData data;
	this->encode( data );

	return data;
}

// Encode this element into an existing VariableLengthData
void HLAfloat64BE::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	// Assign a buffer to take the double
	char buffer[BitHelpers::LENGTH_DOUBLE];
	BitHelpers::encodeDoubleBE( this->get(), buffer, 0 );
	
	inData.setData( buffer, BitHelpers::LENGTH_DOUBLE );
}

// Encode this element and append it to a buffer
void HLAfloat64BE::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	char data[BitHelpers::LENGTH_DOUBLE];
	BitHelpers::encodeDoubleBE( this->get(), data, 0 );

	buffer.insert( buffer.end(), data, data + BitHelpers::LENGTH_DOUBLE );
}

// Decode this element from the RTI's VariableLengthData.
void HLAfloat64BE::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	if( inData.size() < BitHelpers::LENGTH_DOUBLE )
		throw EncoderException( L"Insufficient data in buffer to decode value" );

	double value = BitHelpers::decodeDoubleBE( (const char*)inData.data(), 0 );
	this->set( value );
}

// Decode this element starting at the index in the provided buffer
// Return the index immediately after the decoded data.
size_t HLAfloat64BE::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	double value = BitHelpers::decodeDoubleBE( buffer, index );
	this->set( value );
	return index + BitHelpers::LENGTH_DOUBLE;
}

// Return the size in bytes of this element's encoding.
size_t HLAfloat64BE::getEncodedLength() const
	throw( EncoderException )
{
	return BitHelpers::LENGTH_DOUBLE;
}

// Return the octet boundary of this element.
unsigned int HLAfloat64BE::getOctetBoundary() const
{
	return BitHelpers::LENGTH_DOUBLE;
}

// Return a hash of the encoded data
// Provides mechanism to map DataElement discriminants to variants
// in VariantRecord.
Integer64 HLAfloat64BE::hash() const
{
	// recast value as a long
	double value = this->get();
	long asLong = *((long*)&value);

	return 31 * 7 + asLong;
}

// Change this instance to use supplied external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through this call.
// Null pointer results in an exception.
void HLAfloat64BE::setDataPointer( double* inData )
	throw( EncoderException )
{
	this->_impl->setUseExternalMemory( inData );
}

// Set the value to be encoded.
// If this element uses external memory, the memory will be modified.
void HLAfloat64BE::set( double inData )
{
	this->_impl->setValue( inData );
}

// Get the value from encoded data.
double HLAfloat64BE::get() const
{
	return this->_impl->getValue();
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Assignment Operator
// Uses existing memory of this instance.
HLAfloat64BE& HLAfloat64BE::operator= ( const HLAfloat64BE& rhs )
{
	this->_impl->setUseInternalMemory( rhs.get() );
	return *this;
}

// Assignment of the value to be encoded data.
// If this element uses external memory, the memory will be modified.
HLAfloat64BE& HLAfloat64BE::operator= ( double rhs )
{
	this->set( rhs );
	return *this;
}

// Conversion operator to double
// Return value from encoded data.
HLAfloat64BE::operator double() const
{
	return this->get();
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
