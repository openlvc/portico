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

DEFINE_BASIC_TYPE_IMPL( HLAoctetPairLEImplementation, OctetPair )

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Constructor: Default
// Uses internal memory.
HLAoctetPairLE::HLAoctetPairLE()
{
	this->_impl = new HLAoctetPairLEImplementation( OctetPair(0, 0) );
}

// Constructor: Initial Value
// Uses internal memory.
HLAoctetPairLE::HLAoctetPairLE( const OctetPair& inData )
{
	this->_impl = new HLAoctetPairLEImplementation( inData );
}

// Constructor: External memory
// This instance changes or is changed by contents of external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through setDataPointer.
// A null value will construct instance to use internal memory.
HLAoctetPairLE::HLAoctetPairLE( OctetPair* inData )
{
	this->_impl = new HLAoctetPairLEImplementation( inData );
}

// Constructor: Copy
// Uses internal memory.
HLAoctetPairLE::HLAoctetPairLE( const HLAoctetPairLE& rhs )
{
	this->_impl = new HLAoctetPairLEImplementation( rhs.get() );
}

HLAoctetPairLE::~HLAoctetPairLE()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the DataElement
// Copy uses internal memory.
std::auto_ptr<DataElement> HLAoctetPairLE::clone() const
{
	return std::auto_ptr<DataElement>( new HLAoctetPairLE(*this) );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAoctetPairLE::encode() const
	throw( EncoderException )
{
	VariableLengthData data;
	this->encode( data );

	return data;
}

// Encode this element into an existing VariableLengthData
void HLAoctetPairLE::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	// Assign a buffer to take the OctetPair
	char buffer[2];
	buffer[0] = this->get().first;
	buffer[1] = this->get().second;

	inData.setData( buffer, 2 );
}

// Encode this element and append it to a buffer
void HLAoctetPairLE::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	char data[2];
	data[0] = this->get().first;
	data[1] = this->get().second;

	buffer.insert( buffer.end(), data, data + 2 );
}

// Decode this element from the RTI's VariableLengthData.
void HLAoctetPairLE::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	if( inData.size() < 2 )
		throw EncoderException( L"Insufficient data in buffer to decode value" );

	const char* asChars = (const char*)inData.data();
	this->set( OctetPair(asChars[0], asChars[1]) );
}

// Decode this element starting at the index in the provided buffer
// Return the index immediately after the decoded data.
size_t HLAoctetPairLE::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	if( index + 2 > buffer.size() )
		throw EncoderException( L"Insufficient data in buffer to decode value" );

	const char* asChars = (const char*)&buffer[0];
	this->set( OctetPair(asChars[0], asChars[1]) );

	return index + 2;
}

// Return the size in bytes of this element's encoding.
size_t HLAoctetPairLE::getEncodedLength() const
	throw( EncoderException )
{
	return 2;
}

// Return the octet boundary of this element.
unsigned int HLAoctetPairLE::getOctetBoundary() const
{
	return 2;
}

// Return a hash of the encoded data
// Provides mechanism to map DataElement discriminants to variants
// in VariantRecord.
Integer64 HLAoctetPairLE::hash() const
{
	Integer64 hash = 7;
	hash = 31 * hash + this->get().first;
	hash = 31 * hash + this->get().second;

	return hash;
}

// Change this instance to use supplied external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through this call.
// Null pointer results in an exception.
void HLAoctetPairLE::setDataPointer( OctetPair* inData )
	throw( EncoderException )
{
	this->_impl->setUseExternalMemory( inData );
}

// Set the value to be encoded.
// If this element uses external memory, the memory will be modified.
void HLAoctetPairLE::set( OctetPair inData )
{
	this->_impl->setValue( inData );
}

// Get the value from encoded data.
OctetPair HLAoctetPairLE::get() const
{
	return this->_impl->getValue();
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Assignment Operator
// Uses existing memory of this instance.
HLAoctetPairLE& HLAoctetPairLE::operator= ( const HLAoctetPairLE& rhs )
{
	this->_impl->setUseInternalMemory( rhs.get() );
	return *this;
}

// Assignment of the value to be encoded data.
// If this element uses external memory, the memory will be modified.
HLAoctetPairLE& HLAoctetPairLE::operator= ( OctetPair rhs )
{
	this->set( rhs );
	return *this;
}

// Conversion operator to OctetPair
// Return value from encoded data.
HLAoctetPairLE::operator OctetPair() const
{
	return this->get();
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
