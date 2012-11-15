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
#include "AggregateTypeImpl.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS
//------------------------------------------------------------------------------------------
AggregateTypeImpl::AggregateTypeImpl()
{

}

AggregateTypeImpl::AggregateTypeImpl( const DataElement& prototype, size_t size )
{
	this->prototype = prototype.clone();
	this->appendPrototypeClones( size );
}

AggregateTypeImpl::AggregateTypeImpl( const AggregateTypeImpl& other )
{
	if( other.prototype.get() != NULL )
		this->prototype = (*other.prototype).clone();

	for( size_t i = 0 ; i < other.elements.size() ; ++i )
		this->append( *other.elements[i] );
}

AggregateTypeImpl::~AggregateTypeImpl()
{
	this->clear();
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
/**
 * Releases the memory of array element at index if it is internally managed by the array.
 * If the element is not internally managed by the array, the corresponding memory is
 * not affected.
 *
 * If the provided index is out of bounds, this call will have no affect.
 */
void AggregateTypeImpl::releaseExistingElement( size_t index )
{
	if( index < this->size() )
	{
		DataElement* existing = this->elements.at( index );
		size_t removed = this->ownedElements.erase( existing );
		if( removed )
			delete existing;
	}
}

void AggregateTypeImpl::clear()
{
	// Clean up all elements that we are responsible for
	std::set<DataElement*>::iterator it;
	for( it = ownedElements.begin() ; it != ownedElements.end() ; ++it )
		delete *it;

	// Clear internal collections
	this->elements.clear();
	this->ownedElements.clear();
}

void AggregateTypeImpl::appendPrototypeClones( size_t count )
{
	if( this->prototype.get() )
	{
		for( size_t i = 0 ; i < count ; ++i )
			this->append( *this->prototype );
	}
}

void AggregateTypeImpl::resize( size_t newSize )
{
	if( this->prototype.get() )
	{
		size_t currentSize = this->size();
		if( newSize > currentSize )
		{
			this->appendPrototypeClones( newSize - currentSize );
		}
		else
		{
			while( currentSize > newSize )
			{
				// Release the memory used by the last element (if it is internal) and remove
				// the element from the list
				this->releaseExistingElement( currentSize - 1 );
				this->elements.pop_back();

				// Update current size and iterate
				currentSize = this->size();
			}
		}
	}
}

/**
 * Sets the content of the array element at the specified index to match that of the
 * provided element. If the element at index has been set to point to an external location
 * through setExternal() that DataElement will be modified.
 *
 * If the provided index is out of bounds, this call will have no affect.
 */
void AggregateTypeImpl::set( size_t index, const DataElement& element )
{
	if( index < this->size() )
	{
		// Get the pointer to the element that we are modifying
		DataElement* elementPointer = this->elements[index];

		// MRF: I can't find a better way to do this within the spec :(
		// Encode the source element into a VariableLengthData and decode into the destination
		// element
		VariableLengthData asBytes = element.encode();
		elementPointer->decode( asBytes );
	}
}

/**
 * Sets the array element at index to point to an external DataElement. Subsequent calls to
 * get/set at this index will directly modify the element.
 *
 * The caller is responsible for ensuring that the memory specified by this method is
 * available for the lifecycle of the array or until a new memory location is specified
 * with a subsequent call to setExternal()
 *
 * If the provided index is out of bounds, this call will have no affect.
 */
void AggregateTypeImpl::setExternal( size_t index, DataElement* element )
{
	if( index < this->size() && element )
	{
		// If the existing element is owned by us, then release it
		releaseExistingElement( index );

		// Store the element pointer
		this->elements[index] = element;
	}
}

void AggregateTypeImpl::append( const DataElement& element )
{
	auto_ptr<DataElement> otherPointer = element.clone();
	DataElement* other = otherPointer.release();

	// Now the element auto_ptr has been released, we are responsible for managing
	// it's memory
	this->ownedElements.insert( other );
	this->elements.push_back( other );
}

void AggregateTypeImpl::append( DataElement* element )
{
	this->elements.push_back( element );
}

/**
 * Returns a pointer to the DataElement at the specified index
 *
 * If the provided index is out of bounds NULL is returned
 */
DataElement* AggregateTypeImpl::get( size_t index ) const
{
	DataElement* result = NULL;

	if( index < this->size() )
		result = this->elements[index];

	return result;
}

/**
 * Returns the number of elements in the array
 */
size_t AggregateTypeImpl::size() const
{
	return this->elements.size();
}

/**
 * Returns true if the specified type is the same as this array's prototype
 */
const DataElement* AggregateTypeImpl::getPrototype() const
{
	return this->prototype.get();
}

bool AggregateTypeImpl::isPrototypeSameTypeAs( const DataElement& element ) const
{
	bool isSame = false;

	if( this->prototype.get() )
		isSame = this->prototype->isSameTypeAs( element );

	return isSame;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
