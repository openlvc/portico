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
#include "RTI/encoding/HLAfixedRecord.h"
#include "AggregateTypeImpl.h"

IEEE1516E_NS_START

DEFINE_AGGREGATE_TYPE_IMPL( HLAfixedRecordImplementation )

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Default Constructor
HLAfixedRecord::HLAfixedRecord()
{
	this->_impl = new HLAfixedRecordImplementation();
}

// Copy Constructor
HLAfixedRecord::HLAfixedRecord( const HLAfixedRecord& rhs )
{
	this->_impl = new HLAfixedRecordImplementation( *rhs._impl );
}

// Destructor
HLAfixedRecord::~HLAfixedRecord()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the array
std::auto_ptr<DataElement> HLAfixedRecord::clone() const
{
	return auto_ptr<DataElement>( new HLAfixedRecord(*this) );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAfixedRecord::encode() const
	throw( EncoderException )
{
	VariableLengthData data;
	this->encode( data );

	return data;
}

// Encode this element into an existing VariableLengthData
void HLAfixedRecord::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	std::vector<Octet> buffer;
	this->encodeInto( buffer );
	
	inData.setData( &buffer, buffer.size() );
}

// Encode this element and append it to a buffer
void HLAfixedRecord::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	// Encode the individual elements
	for( size_t i = 0 ; i < this->size() ; ++i )
	{
		const DataElement& element = this->get( i );
		element.encodeInto( buffer );
	}
}

// Decode this element from the RTI's VariableLengthData.
void HLAfixedRecord::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	// Wrap the VariableLengthData's internal byte array in a std::vector<Octet>
	const char* bytes = (const char*)&inData;
	std::vector<Octet> buffer( bytes, bytes + inData.size() );

	// Decode!
	this->decodeFrom( buffer, 0 );
}

// Decode this element starting at the index in the provided buffer
size_t HLAfixedRecord::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	// Decode all the elements!
	for( size_t i = 0 ; i < this->size() ; ++i )
	{
		DataElement* element = this->_impl->get( i );
		index = element->decodeFrom( buffer, index );
	}

	return index;
}

// Return the size in bytes of this element's encoding.
size_t HLAfixedRecord::getEncodedLength() const
	throw( EncoderException )
{
	size_t length = 0;
	for( size_t i = 0 ; i < this->size() ; ++i )
	{
		const DataElement& element = this->get( i );
		length += element.getEncodedLength();
	}

	return length;
}

// Return the octet boundary of this element.
unsigned int HLAfixedRecord::getOctetBoundary() const
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
bool HLAfixedRecord::isSameTypeAs( const DataElement& inData ) const
{
	return typeid(inData) == typeid(*this);
}

// Return the number of elements in this fixed array.
size_t HLAfixedRecord::size() const
{
	return this->_impl->size();
}

// Sets the element at the given index to a copy of the given element instance
// Element must match prototype.
// If indexed element uses external memory, the memory will be modified.
void HLAfixedRecord::set( size_t index, const DataElement& dataElement )
	throw( EncoderException )
{
	if( index >= this->size() )
			throw EncoderException( L"Index out of bounds." );

	if( !this->hasElementSameTypeAs(index, dataElement) )
		throw EncoderException( L"Provided element type does not match existing prototype." );

	this->_impl->set( index, dataElement );
}

// Sets the element at the given index to the given element instance
// Element must match prototype.
// Null pointer results in an exception.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until the indexed element
// acquires new memory through this call.
void HLAfixedRecord::setElementPointer( size_t index, DataElement* dataElement )
	throw( EncoderException )
{
	if( !dataElement )
		throw EncoderException( L"Cannot set HLAfixedArray element pointer to NULL" );

	if( index >= this->size() )
		throw EncoderException( L"Index out of bounds." );
	
	if( !this->hasElementSameTypeAs(index, *dataElement) )
		throw EncoderException( L"Provided element type does not match existing prototype." );

	this->_impl->setExternal( index, dataElement );
}

// Return a reference to the element instance at the specified index.
// Access of indexed element that has not been set will set that index
// with a clone of prototype and return it.
// Must use set to change element.
const DataElement& HLAfixedRecord::get( size_t index ) const
	throw( EncoderException )
{
	if( index >= this->size() )
		throw EncoderException( L"Index out of bounds." );

	return *(this->_impl->get(index));
}

/////////////////////////////////////
// HLAfixedRecord Specific Methods //
/////////////////////////////////////
// Return true if given element is same type as the indexed element;
// otherwise, false.
bool HLAfixedRecord::hasElementSameTypeAs( size_t index, const DataElement& inData ) const
{
	bool isSame = false;

	if( index <= this->size() )
	{
		const DataElement& element = this->get( index );
		isSame = element.isSameTypeAs( inData );
	}

	return isSame;
}

// Append a copy of the dataElement instance to this fixed record.
void HLAfixedRecord::appendElement( const DataElement& dataElement )
{
	this->_impl->append( dataElement );
}

// Append the dataElement instance to this fixed record.
// Null pointer results in an exception.
void HLAfixedRecord::appendElementPointer( DataElement* dataElement )
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
const DataElement& HLAfixedRecord::operator[] ( size_t index ) const
	throw( EncoderException )
{
	return this->get( index );
}

// private
// Assignment Operator not allowed
HLAfixedRecord& HLAfixedRecord::operator= ( const HLAfixedRecord& rhs )
{
	return *this;
}


//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
