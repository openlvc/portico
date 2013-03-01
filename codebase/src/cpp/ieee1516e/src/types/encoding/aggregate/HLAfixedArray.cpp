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
#include "RTI/encoding/HLAfixedArray.h"
#include "AggregateTypeImpl.h"

IEEE1516E_NS_START

DEFINE_AGGREGATE_TYPE_IMPL( HLAfixedArrayImplementation )

////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////  HLAfixedArray  /////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Constructor which accepts a prototype element and size
// that specifies the type and number of elements to be stored in the array.
// A clone of the given element works as a prototype.
HLAfixedArray::HLAfixedArray( const DataElement& protoType, size_t length )
{
	this->_impl = new HLAfixedArrayImplementation( protoType, length );
}

// Copy Constructor
// Copied elements use internal memory
HLAfixedArray::HLAfixedArray( const HLAfixedArray& rhs )
{
	this->_impl = new HLAfixedArrayImplementation( *rhs._impl );
}

// Destructor
HLAfixedArray::~HLAfixedArray()
{
	delete this->_impl;
}

// Private: Default Constructor not allowed
HLAfixedArray::HLAfixedArray()
{
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the array
std::auto_ptr<DataElement> HLAfixedArray::clone() const
{
	return auto_ptr<DataElement>( new HLAfixedArray(*this) );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAfixedArray::encode() const
	throw( EncoderException )
{
	VariableLengthData data;
	this->encode( data );

	return data;
}

// Encode this element into an existing VariableLengthData
void HLAfixedArray::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	std::vector<Octet> buffer;
	this->encodeInto( buffer );
	
	inData.setData( &buffer, buffer.size() );
}

// Encode this element and append it to a buffer
void HLAfixedArray::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	// Encode the length
	size_t elements = this->size();
	HLAinteger32BE length( this->size() );
	length.encodeInto( buffer );

	// Encode the individual elements
	for( size_t i = 0 ; i < elements ; ++i )
	{
		const DataElement& element = this->get( i );
		element.encodeInto( buffer );
	}
}

// Decode this element from the RTI's VariableLengthData.
void HLAfixedArray::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	// Wrap the VariableLengthData's internal byte array in a std::vector<Octet>
	const char* bytes = (const char*)&inData;
	std::vector<Octet> buffer( bytes, bytes + inData.size() );

	// Decode!
	this->decodeFrom( buffer, 0 );
}

// Decode this element starting at the index in the provided buffer
size_t HLAfixedArray::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	size_t available = buffer.size();
	if( index + BitHelpers::LENGTH_INT > available )
		throw EncoderException( L"Insufficient data in buffer to decode value" );

	// Decode received array size
	HLAinteger32BE size;
	index = size.decodeFrom( buffer, index );

	size_t receivedSize = size.get();
	if( receivedSize != this->size() )
	{
		// Received array size was different to what we were expecting, so throw an exception
		std::wstringstream stream;
		stream << L"Element count in decoded array differs. Expected[" <<
					this->size() <<
					"] Received [" <<
					receivedSize << "]";

		throw EncoderException( stream.str() );
	}

	// Decode all the elements!
	for( size_t i = 0 ; i < receivedSize ; ++i )
	{
		DataElement* element = this->_impl->get( i );
		index = element->decodeFrom( buffer, index );
	}

	return index;
}

// Return the size in bytes of this element's encoding.
size_t HLAfixedArray::getEncodedLength() const
	throw( EncoderException )
{
	size_t length = BitHelpers::LENGTH_INT;

	for( size_t i = 0 ; i < this->size() ; ++i )
	{
		const DataElement& element = this->get( i );
		length += element.getEncodedLength();
	}

	return length;
}

// Return the octet boundary of this element.
unsigned int HLAfixedArray::getOctetBoundary() const
{
	// Return the size of the largest element
	size_t maxSize = 1;
	for( size_t i = 0 ; i < this->size() ; ++i )
	{
		const DataElement& element = this->get( i );
		maxSize = max( maxSize, element.getEncodedLength() );
	}

	return maxSize;
}

// Return true if given element is same type as this; otherwise, false.
bool HLAfixedArray::isSameTypeAs( const DataElement& inData ) const
{
	return typeid(inData) == typeid(*this);
}

// Return true if given element matches prototype of this array.
bool HLAfixedArray::hasPrototypeSameTypeAs( const DataElement& dataElement ) const
{
	return this->_impl->isPrototypeSameTypeAs( dataElement );
}

// Return the number of elements in this fixed array.
size_t HLAfixedArray::size() const
{
	return this->_impl->size();
}

// Sets the element at the given index to a copy of the given element instance
// Element must match prototype.
// If indexed element uses external memory, the memory will be modified.
void HLAfixedArray::set( size_t index, const DataElement& dataElement )
	throw( EncoderException )
{
	if( !this->hasPrototypeSameTypeAs(dataElement) )
		throw EncoderException( L"Provided element type does not match array prototype." );

	if( index >= this->size() )
		throw EncoderException( L"Index out of bounds." );

	this->_impl->set( index, dataElement );
}

// Sets the element at the given index to the given element instance
// Element must match prototype.
// Null pointer results in an exception.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until the indexed element
// acquires new memory through this call.
void HLAfixedArray::setElementPointer( size_t index, DataElement* dataElement )
	throw( EncoderException )
{
	if( !dataElement )
		throw EncoderException( L"Cannot set HLAfixedArray element pointer to NULL" );

	if( index >= this->size() )
		throw EncoderException( L"Index out of bounds." );

	if( !this->hasPrototypeSameTypeAs(*dataElement) )
		throw EncoderException( L"Provided element type does not match array prototype." );

	this->_impl->setExternal( index, dataElement );
}

// Return a reference to the element instance at the specified index.
// Access of indexed element that has not been set will set that index
// with a clone of prototype and return it.
// Must use set to change element.
const DataElement& HLAfixedArray::get( size_t index ) const
	throw( EncoderException )
{
	if( index >= this->size() )
		throw EncoderException( L"Index out of bounds." );

	return *(this->_impl->get(index));
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Return a const reference to the element instance at the specified index.
// Access of indexed element that has not been set will set that index
// with a clone of prototype and return it.
// Must use set to change element.
const DataElement& HLAfixedArray::operator[] ( size_t index ) const
	throw( EncoderException )
{
	return this->get( index );
}

// private
// Assignment Operator not allowed
HLAfixedArray& HLAfixedArray::operator= ( const HLAfixedArray& rhs )
{
	return *this;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
