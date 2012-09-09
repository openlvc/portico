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

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Constructor: Default
// Uses internal memory.
HLAfloat32LE::HLAfloat32LE()
{
	
}

// Constructor: Initial Value
// Uses internal memory.
HLAfloat32LE::HLAfloat32LE( const float& inData )
{
	
}

// Constructor: External memory
// This instance changes or is changed by contents of external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through setDataPointer.
// A null value will construct instance to use internal memory.
HLAfloat32LE::HLAfloat32LE( float* inData )
{
	
}

// Constructor: Copy
// Uses internal memory.
HLAfloat32LE::HLAfloat32LE( const HLAfloat32LE& rhs )
{
	
}

HLAfloat32LE::~HLAfloat32LE()
{
	
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the DataElement
// Copy uses internal memory.
std::auto_ptr<DataElement> HLAfloat32LE::clone() const
{
	return std::auto_ptr<DataElement>( new HLAfloat32LE() );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAfloat32LE::encode() const
	throw( EncoderException )
{
	return VariableLengthData();
}

// Encode this element into an existing VariableLengthData
void HLAfloat32LE::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	
}

// Encode this element and append it to a buffer
void HLAfloat32LE::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	
}

// Decode this element from the RTI's VariableLengthData.
void HLAfloat32LE::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	
}

// Decode this element starting at the index in the provided buffer
// Return the index immediately after the decoded data.
size_t HLAfloat32LE::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	return 0;
}

// Return the size in bytes of this element's encoding.
size_t HLAfloat32LE::getEncodedLength() const
	throw( EncoderException )
{
	return 0;
}

// Return the octet boundary of this element.
unsigned int HLAfloat32LE::getOctetBoundary() const
{
	return 0;
}

// Return a hash of the encoded data
// Provides mechanism to map DataElement discriminants to variants
// in VariantRecord.
Integer64 HLAfloat32LE::hash() const
{
	return 0;
}

// Change this instance to use supplied external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through this call.
// Null pointer results in an exception.
void HLAfloat32LE::setDataPointer( float* inData )
	throw( EncoderException )
{
	
}

// Set the value to be encoded.
// If this element uses external memory, the memory will be modified.
void HLAfloat32LE::set( float inData )
{
	
}

// Get the value from encoded data.
float HLAfloat32LE::get() const
{
	return (float)0;
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Assignment Operator
// Uses existing memory of this instance.
HLAfloat32LE& HLAfloat32LE::operator= ( const HLAfloat32LE& rhs )
{
	return *this;
}

// Assignment of the value to be encoded data.
// If this element uses external memory, the memory will be modified.
HLAfloat32LE& HLAfloat32LE::operator= ( float rhs )
{
	return *this;
}

// Conversion operator to float
// Return value from encoded data.
HLAfloat32LE::operator float() const
{
	return *this;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
