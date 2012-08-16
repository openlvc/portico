/*
 *   Copyright 2009 The Portico Project
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
#include "AttributeHandleValuePairSet.h"

PORTICO13_NS_START

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////// Constructors ////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
AttributeHandleValuePairSet::AttributeHandleValuePairSet()
{
}

AttributeHandleValuePairSet::AttributeHandleValuePairSet( HLA::ULong size )
{
	// we ignore the size for now because we're using a map which will resize itself
}

AttributeHandleValuePairSet::~AttributeHandleValuePairSet()
{
	this->empty();
}

//////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// Instance Methods //////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
GenericHandleValuePair* AttributeHandleValuePairSet::getPairAt( HLA::ULong index ) const
{
	map<HLA::Handle,GenericHandleValuePair*>::const_iterator mapIterator = pairs.begin();
	HLA::ULong counter = 0;

	while( mapIterator != pairs.end() )
	{
		if( counter == index )
		{
			return mapIterator->second;
		}
		counter++;
		mapIterator++;
	}

	return 0;
}

void AttributeHandleValuePairSet::checkIndex( HLA::ULong index ) const
	throw( HLA::ArrayIndexOutOfBounds )
{
	if( pairs.empty() || index > pairs.size()-1 )
	{
		char message[32];
		sprintf( message, "Index [%lo] out of bounds", index );
		throw HLA::ArrayIndexOutOfBounds( message ); // leaky leaky!
	}
}

HLA::ULong AttributeHandleValuePairSet::size() const
{
	return (HLA::ULong)pairs.size();
}

HLA::Handle AttributeHandleValuePairSet::getHandle( HLA::ULong index ) const
	throw( HLA::ArrayIndexOutOfBounds )
{
	checkIndex( index );
	GenericHandleValuePair *thePair = this->getPairAt(index);
	return thePair->getHandle();
}

HLA::ULong AttributeHandleValuePairSet::getValueLength( HLA::ULong index ) const
	throw( HLA::ArrayIndexOutOfBounds )
{
	checkIndex( index );
	GenericHandleValuePair *thePair = this->getPairAt(index);
	return thePair->getValueLength();
}

void AttributeHandleValuePairSet::getValue( HLA::ULong index,
                                            char* buffer,
                                            HLA::ULong& valueLength ) const 
	throw( HLA::ArrayIndexOutOfBounds )
{
	// note: the 1.3 spec says that we should assume that the given buffer is big
	//       enough to hold all the handle information, so we should just try and
	//       write it all, putting the written amount into the given valueLength
	//       reference on the way out

	checkIndex( index );

	// fetch the pair that contains the information
	GenericHandleValuePair *thePair = this->getPairAt(index);
	// attempt to copy the entire value into the given buffer
	memcpy( buffer, thePair->getValue(), thePair->getValueLength() );
	// store the amount of bytes that were written
	valueLength = thePair->getValueLength();
}

char* AttributeHandleValuePairSet::getValuePointer( HLA::ULong index,
                                                    HLA::ULong& valueLength ) const
	throw( HLA::ArrayIndexOutOfBounds )
{
	checkIndex( index );

	GenericHandleValuePair *thePair = this->getPairAt(index);
	valueLength = thePair->getValueLength();
	return thePair->getValue();
}

HLA::TransportType AttributeHandleValuePairSet::getTransportType( HLA::ULong index ) const
	throw( HLA::ArrayIndexOutOfBounds, HLA::InvalidHandleValuePairSetContext )
{
	return 0;
}

HLA::OrderType AttributeHandleValuePairSet::getOrderType( HLA::ULong index ) const
	throw( HLA::ArrayIndexOutOfBounds, HLA::InvalidHandleValuePairSetContext )
{
	return 0;
}

HLA::Region* AttributeHandleValuePairSet::getRegion( HLA::ULong index ) const
	throw( HLA::ArrayIndexOutOfBounds, HLA::InvalidHandleValuePairSetContext )
{
	return 0;
}

void AttributeHandleValuePairSet::add( HLA::Handle handle,
                                       const char* buffer,
                                       HLA::ULong valueLength )
	throw( HLA::ValueLengthExceeded, HLA::ValueCountExceeded )
{
	GenericHandleValuePair *thePair = new GenericHandleValuePair();

	thePair->setHandle( handle );
	thePair->setValue( buffer, valueLength ); // will make a copy

	pairs[handle] = thePair;
}

void AttributeHandleValuePairSet::remove( HLA::Handle handle ) throw( HLA::ArrayIndexOutOfBounds )
{
	delete pairs[handle];  // delete the GenericHandleValuePair, releasing the memory
	pairs.erase( handle ); // remove the key from the map
}

void AttributeHandleValuePairSet::moveFrom( const HLA::AttributeHandleValuePairSet& ahvps,
                                                  HLA::ULong& index ) 
	throw( HLA::ValueCountExceeded, HLA::ArrayIndexOutOfBounds )
{
	
}

void AttributeHandleValuePairSet::empty() // Empty the Set without deallocating space.
{
	// recoup any space for the contents of the map
	std::map<HLA::Handle,GenericHandleValuePair*>::iterator iterator;
	for( iterator = pairs.begin(); iterator != pairs.end(); iterator++ )
	{
		GenericHandleValuePair *current = (*iterator).second;
		delete current;
	}

	// empty the set
	pairs.clear();
}

HLA::ULong AttributeHandleValuePairSet::start() const
{
	return 0;
}

HLA::ULong AttributeHandleValuePairSet::valid( HLA::ULong index ) const
{
	return 0;	
}

HLA::ULong AttributeHandleValuePairSet::next( HLA::ULong index ) const
{
	return 0;	
}

////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////// Non-Standard Methods /////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////
// this method should NOT copy the given data, but rather, should just
// store the pointer and take ownership for the maangement of the data.
// this is designed to allow data coming in from the JNI to only be copied
// once, rather than being copied from the JNI, and then copied again when
// it is put in the set.
void AttributeHandleValuePairSet::addButDontCopy( HLA::Handle handle,
                                                  char *buffer,
                                                  HLA::ULong size )
{
	GenericHandleValuePair *thePair = new GenericHandleValuePair();

	thePair->setHandle( handle );
	thePair->setValueButDontCopy( buffer, size );
	// store the pair
	pairs[handle] = thePair;
}

//////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Static Methods ///////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////

PORTICO13_NS_END
