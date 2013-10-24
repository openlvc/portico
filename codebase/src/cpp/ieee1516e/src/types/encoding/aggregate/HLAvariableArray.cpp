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
#include "RTI/encoding/HLAvariableArray.h"
#include "RTI/encoding/BasicDataElements.h"
#include "types/encoding/BitHelpers.h"
#include "AggregateTypeImpl.h"

IEEE1516E_NS_START

DEFINE_AGGREGATE_TYPE_IMPL( HLAvariableArrayImplementation )

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Constructor which accepts a prototype element
// that specifies the type of elements to be stored in the array.
// A clone of the given element works as a seed.
HLAvariableArray::HLAvariableArray( const DataElement& prototype )
{
	this->_impl = new HLAvariableArrayImplementation( prototype, 0 );
}

// Copy Constructor
HLAvariableArray::HLAvariableArray( const HLAvariableArray& rhs )
{
	this->_impl = new HLAvariableArrayImplementation( *this->_impl );
}

// Destructor
HLAvariableArray::~HLAvariableArray()
{
	delete this->_impl;
}

// Private
HLAvariableArray::HLAvariableArray()
{
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the array
std::auto_ptr<DataElement> HLAvariableArray::clone() const
{
	return auto_ptr<DataElement>( new HLAvariableArray(*this) );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAvariableArray::encode() const
	throw( EncoderException )
{
	VariableLengthData data;
	this->encode( data );

	return data;
}

// Encode this element into an existing VariableLengthData
void HLAvariableArray::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	std::vector<Octet> buffer;
	this->encodeInto( buffer );
	
	inData.setData( &buffer[0], buffer.size() );
}

// Encode this element and append it to a buffer
void HLAvariableArray::encodeInto( std::vector<Octet>& buffer ) const
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
void HLAvariableArray::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	// Wrap the VariableLengthData's internal byte array in a std::vector<Octet>
	const char* bytes = (const char*)inData.data();
	std::vector<Octet> buffer( bytes, bytes + inData.size() );

	// Decode!
	this->decodeFrom( buffer, 0 );
}

// Decode this element starting at the index in the provided buffer
size_t HLAvariableArray::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	size_t available = buffer.size();
	if( index + BitHelpers::LENGTH_INT > available )
		throw EncoderException( L"Insufficient data in buffer to decode value" );

	// Decode received array size
	HLAinteger32BE size;
	index = size.decodeFrom( buffer, index );
	size_t receivedSize = size.get();

	// Resize to the new element count. This will either add element clones, or cull the end of
	// the existing list until it is the right size
	this->_impl->resize( receivedSize );

	// Decode all the elements!
	for( size_t i = 0 ; i < receivedSize ; ++i )
	{
		DataElement* element = this->_impl->get( i );
		index = element->decodeFrom( buffer, index );
	}

	return index;
}

// Return the size in bytes of this element's encoding.
size_t HLAvariableArray::getEncodedLength() const
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
unsigned int HLAvariableArray::getOctetBoundary() const
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
bool HLAvariableArray::isSameTypeAs( const DataElement& inData ) const
{
	return typeid(inData) == typeid(*this);
}

// Return true if given element matches prototype of this array.
bool HLAvariableArray::hasPrototypeSameTypeAs( const DataElement& dataElement ) const
{
	return this->_impl->isPrototypeSameTypeAs( dataElement );
}

// Return the number of elements in this fixed array.
size_t HLAvariableArray::size() const
{
	return this->_impl->size();
}

// Sets the element at the given index to a copy of the given element instance
// Element must match prototype.
// If indexed element uses external memory, the memory will be modified.
void HLAvariableArray::set( size_t index, const DataElement& dataElement )
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
void HLAvariableArray::setElementPointer( size_t index, DataElement* dataElement )
	throw( EncoderException )
{
	if( !dataElement )
			throw EncoderException( L"Cannot set HLAvariableArray element pointer to NULL" );

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
const DataElement& HLAvariableArray::get( size_t index ) const
	throw( EncoderException )
{
	if( index >= this->size() )
		throw EncoderException( L"Index out of bounds." );

	return *(this->_impl->get(index));
}

///////////////////////////////////////
// HLAvariableArray Specific Methods //
///////////////////////////////////////
// Adds a copy of the given element instance to this array
// Element must match prototype.
void HLAvariableArray::addElement( const DataElement& dataElement )
	throw( EncoderException )
{
	this->_impl->append( dataElement );
}

// Adds the given element instance to this variable array
// Element must match prototype.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until the indexed element
// acquires new memory through the set method.
// Null pointer results in an exception.
void HLAvariableArray::addElementPointer( DataElement* dataElement )
	throw( EncoderException )
{
	if( !dataElement )
		throw EncoderException( L"Cannot append a NULL element pointer" );

	this->_impl->append( dataElement );
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Return a const reference to the element instance at the specified index.
// Access of indexed element that has not been set will set that index
// with a clone of prototype and return it.
// Must use set to change element.
const DataElement& HLAvariableArray::operator[] ( size_t index ) const
	throw( EncoderException )
{
	return this->get( index );
}

// private
// Assignment Operator not allowed
HLAvariableArray& HLAvariableArray::operator= ( const HLAvariableArray& rhs )
{
	return *this;
}


//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
