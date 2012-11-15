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
#include "RTI/encoding/HLAopaqueData.h"
#include "types/encoding/BitHelpers.h"
#include "AggregateTypeImpl.h"

IEEE1516E_NS_START

class HLAopaqueDataImplementation
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		Octet* buffer;
		size_t bufferSize;
		size_t dataSize;
		bool bufferInternallyAllocated;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		HLAopaqueDataImplementation()
		{
			this->initialiseToEmpty();
		}

		/**
		 * Creates a HLAopaqueDataImplementation that uses an internal buffer to store the specified
		 * data
		 */
		HLAopaqueDataImplementation( const Octet* inData, size_t dataSize )
		{
			this->initialiseToEmpty();
			this->setInternal( inData, dataSize );
		}

		virtual ~HLAopaqueDataImplementation()
		{
			if( this->bufferInternallyAllocated )
				this->releaseInternal();
		}
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private:
		void initialiseToEmpty()
		{
			this->buffer = NULL;
			this->bufferSize = 0;
			this->dataSize = 0;
			this->bufferInternallyAllocated = false;
		}

		/**
		 * Releases the buffer memory if it is being internally managed by this object. If the
		 * memory is not internally managed by the object, the corresponding memory is not
		 * affected.
		 */
		void releaseInternal()
		{
			if( bufferInternallyAllocated )
			{
				delete[] buffer;

				this->buffer = NULL;
				this->dataSize = 0;
				this->bufferSize = 0;
				this->bufferInternallyAllocated = false;
			}
		}

	public:
		/**
		 * Instructs the instance to use an internal buffer to store the opaque data, intiialising
		 * it with the provided value.
		 * <p/>
		 * Subsequent calls to get() and set() will return/modify memory internal to this instance.
		 */
		void setInternal( const Octet* inData, size_t dataSize )
		{
			// TODO Could possibly optimize this by overwriting the existing internal buffer
			// if we are already using an internal buffer, and its large enough to hold the new
			// data
			if( this->bufferInternallyAllocated )
				this->releaseInternal();

			this->buffer = new Octet[dataSize];
			::memcpy( this->buffer, inData, dataSize );

			this->dataSize = dataSize;
			this->bufferSize = dataSize;
			this->bufferInternallyAllocated = true;
		}

		/**
		 * Instructs the instance to use an external buffer to store the opaque data.
		 * <p/>
		 * Subsequent calls to get() and set() will return/modify the data stored at the memory
		 * location specified by this function
		 */
		void setExternal( Octet** inData, size_t bufferSize, size_t dataSize )
		{
			if( this->bufferInternallyAllocated )
				this->releaseInternal();

			this->buffer = *inData;
			this->bufferSize = bufferSize;
			this->dataSize = dataSize;
			this->bufferInternallyAllocated = false;
		}

		/**
		 * Sets this instance's value.
		 * <p/>
		 * The value may be written to memory internal or external to this instance depending
		 * on whether setInternal()/setExternal() has been called previously.
		 */
		void set( const Octet* inData, size_t dataSize )
		{
			if( this->bufferInternallyAllocated || this->buffer == NULL )
			{
				this->setInternal( inData, dataSize );
			}
			else
			{
				size_t copySize = ::min( dataSize, bufferSize );
				::memcpy( this->buffer, inData, copySize );
				this->dataSize = copySize;
			}
		}

		/**
		 * Returns this instance's value.
		 * <p/>
		 * The value may be obtained from memory internal or external to this instance depending
		 * on whether setInternal()/setExternal() has been called previously.
		 */
		const Octet* get() const
		{
			return this->buffer;
		}

		size_t getBufferSize() const
		{
			return this->bufferSize;
		}

		size_t getDataSize() const
		{
			return this->dataSize;
		}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
};

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
// Constructor: Default
// Uses internal memory
HLAopaqueData::HLAopaqueData()
{
	this->_impl = new HLAopaqueDataImplementation();
}

// Constructor: Initial Value
// Uses internal memory
HLAopaqueData::HLAopaqueData( const Octet* inData, size_t dataSize )
{
	this->_impl = new HLAopaqueDataImplementation( inData, dataSize );
}

// Constructor: Use external memory with buffer and data of given lengths.
// This instance changes or reflects changes to contents of external memory.
// Changes to external memory are reflected in subsequent encodings.
// Changes to encoder (i.e., set or decode) are reflected in external memory.
// Caller is responsible for ensuring that the external memory is
// valid for the lifetime of this object or until this object acquires
// new memory through setDataPointer.
// Buffer length indicates size of memory; data length indicates size of
// data stored in memory.
// Exception is thrown for null memory or zero buffer size.
HLAopaqueData::HLAopaqueData( Octet** inData, size_t bufferSize, size_t dataSize )
	throw( EncoderException )
{
	this->_impl = new HLAopaqueDataImplementation();
	this->_impl->setExternal( inData, bufferSize, dataSize );
}

// Constructor: Copy
// Uses internal memory
HLAopaqueData::HLAopaqueData( const HLAopaqueData& rhs )
{
	this->_impl = new HLAopaqueDataImplementation( rhs.get(), rhs.dataLength() );
}

// Caller is free to delete rhs.
HLAopaqueData::~HLAopaqueData()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a new copy of the array
std::auto_ptr<DataElement> HLAopaqueData::clone() const
{
	return auto_ptr<DataElement>( new HLAopaqueData(*this) );
}

// Encode this element into a new VariableLengthData
VariableLengthData HLAopaqueData::encode() const
	throw( EncoderException )
{
	VariableLengthData data;
	this->encode( data );

	return data;
}

// Encode this element into an existing VariableLengthData
void HLAopaqueData::encode( VariableLengthData& inData ) const
	throw( EncoderException )
{
	std::vector<Octet> buffer;
	this->encodeInto( buffer );
	
	inData.setData( buffer.data(), buffer.size() );
}

// Encode this element and append it to a buffer
void HLAopaqueData::encodeInto( std::vector<Octet>& buffer ) const
	throw( EncoderException )
{
	// Encode the length
	size_t rawLength = this->dataLength();
	const char* data = this->get();
	HLAinteger32BE length( rawLength );
	length.encodeInto( buffer );

	buffer.insert( buffer.end(), data, data + rawLength );
}

// Decode this element from the RTI's VariableLengthData.
void HLAopaqueData::decode( const VariableLengthData& inData )
	throw( EncoderException )
{
	// Wrap the VariableLengthData's internal byte array in a std::vector<Octet>
	const char* bytes = (const char*)inData.data();
	std::vector<Octet> buffer( bytes, bytes + inData.size() );

	// Decode!
	this->decodeFrom( buffer, 0 );
}

// Decode this element starting at the index in the provided buffer
size_t HLAopaqueData::decodeFrom( const std::vector<Octet>& buffer, size_t index )
	throw( EncoderException )
{
	size_t available = buffer.size();
	if( index + BitHelpers::LENGTH_INT > available )
		throw EncoderException( L"Insufficient data in buffer to decode value" );

	// Decode received array size
	HLAinteger32BE size;
	index = size.decodeFrom( buffer, index );
	size_t receivedSize = size.get();

	if( index + receivedSize > available )
		throw EncoderException( L"Insufficient data in buffer to decode value" );

	this->_impl->set( buffer.data() + index, receivedSize );

	return index + receivedSize;
}

// Return the size in bytes of this element's encoding.
size_t HLAopaqueData::getEncodedLength() const
	throw( EncoderException )
{
	return BitHelpers::LENGTH_INT + this->_impl->getDataSize();
}

// Return the octet boundary of this element.
unsigned int HLAopaqueData::getOctetBoundary() const
{
	return this->_impl->getDataSize();
}

////////////////////////////////////
// HLAopaqueData Specific Methods //
////////////////////////////////////
// Return the length of the contained buffer
size_t HLAopaqueData::bufferLength() const
{
	return this->_impl->getBufferSize();
}

// Return the length of the data stored in the buffer
size_t HLAopaqueData::dataLength() const
{
	return this->_impl->getDataSize();
}

// Change memory to use given external memory
// Changes to this instance will be reflected in external memory
// Caller is responsible for ensuring that the data that is
// pointed to is valid for the lifetime of this object, or past
// the next time this object is given new data.
// Buffer length indicates size of memory; data length indicates size of
// data stored in memory.
// Exception is thrown for null memory or zero buffer size.
void HLAopaqueData::setDataPointer( Octet** inData, size_t bufferSize, size_t dataSize )
	throw( EncoderException )
{
	if( inData == NULL )
		throw EncoderException( L"NULL data pointer provided to HLAopaqueData::setDataPointer" );

	if( bufferSize == 0 )
		throw EncoderException( L"Buffer with zero size provided to HLAopaqueData::setDataPointer" );

	this->_impl->setExternal( inData, bufferSize, dataSize );
}

// Set the data to be encoded.
void HLAopaqueData::set( const Octet* inData, size_t dataSize )
{
	this->_impl->set( inData, dataSize );
}

// Return a reference to the contained array
const Octet* HLAopaqueData::get() const
{
	return this->_impl->get();
}

// Conversion operator to std::vector<Octet>
// Value returned is from encoded data.
HLAopaqueData::operator const Octet*() const
{
	return this->get();
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Assignment Operator not allowed
HLAopaqueData& HLAopaqueData::operator= ( const HLAopaqueData& rhs )
{
	return *this;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
