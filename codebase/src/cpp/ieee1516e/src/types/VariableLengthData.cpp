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

IEEE1516E_NS_START

struct VariableLengthDataImplementation
{
	// pointer to the raw data, which will be dynamically allocated
	void *data;
	
	// how much we're storing
	size_t size;
	
	// Use this function to delete the memory. Set to null if we are not responsible for it
	void (*deleteFunction)(void* data);
};

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
VariableLengthData::VariableLengthData()
{
	this->_impl = new VariableLengthDataImplementation();
	this->_impl->data = NULL;
	this->_impl->size = 0;
	this->_impl->deleteFunction = NULL;
}

// Caller is free to delete inData after the call
VariableLengthData::VariableLengthData( const void* inData, size_t inSize )
{
	this->_impl = new VariableLengthDataImplementation();
	this->_impl->data = ::operator new( inSize );
	this->_impl->size = inSize;
	this->_impl->deleteFunction = ::operator delete;
	memcpy( _impl->data, inData, inSize );
}

// Caller is free to delete rhs after the call
VariableLengthData::VariableLengthData( const VariableLengthData& rhs )
{
	this->_impl = new VariableLengthDataImplementation();
	this->_impl->data = ::operator new( rhs._impl->size );
	this->_impl->size = rhs._impl->size;
	this->_impl->deleteFunction = ::operator delete;
	memcpy( _impl->data, rhs._impl->data, rhs._impl->size );
}

VariableLengthData::~VariableLengthData()
{
	// delete the contained data - but only if we are responsible for it
	if( this->_impl->deleteFunction != NULL )
		this->_impl->deleteFunction( this->_impl->data );

	// delete the actual struct
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------

// Caller is free to delete rhs after the call
// This instance will revert to internal storage as a result of assignment.
VariableLengthData& VariableLengthData::operator= ( const VariableLengthData& rhs )
{
	if( this == &rhs )
		return *this;
	else
		setData( rhs._impl->data, rhs._impl->size );
	
	return *this;
}

// This pointer should not be expected to be valid past the
// lifetime of this object, or past the next time this object
// is given new data
const void* VariableLengthData::data() const
{
	return this->_impl->data;
}

size_t VariableLengthData::size() const
{
	return this->_impl->size;
}

// Caller is free to delete inData after the call
void VariableLengthData::setData( const void* inData, size_t inSize )
{
	// delete what we already have there if we're responsible for it
	if( this->_impl->deleteFunction != NULL )
		this->_impl->deleteFunction( this->_impl->data );
	
	// make some new space and copy stuff over
	this->_impl->data = ::operator new( inSize );
	this->_impl->size = inSize;
	this->_impl->deleteFunction = ::operator delete;
	memcpy( this->_impl->data, inData, inSize );
}

// Caller is responsible for ensuring that the data that is
// pointed to is valid for the lifetime of this object, or past
// the next time this object is given new data.
void VariableLengthData::setDataPointer( void* inData, size_t inSize )
{
	// delete what we already have if we're responsible for it
	if( this->_impl->deleteFunction != NULL )
		this->_impl->deleteFunction( this->_impl->data );

	// make some new space and copy stuff over
	this->_impl->data = ::operator new( inSize );
	this->_impl->size = inSize;
	this->_impl->deleteFunction = NULL; // we're not responsible for it!
	memcpy( this->_impl->data, inData, inSize );
}

// Caller gives up ownership of inData to this object.
// This object assumes the responsibility of deleting inData
// when it is no longer needed.
// The allocation of inData is assumed to have been through an array
// alloctor (e.g., char* data = new char[20]. If the data was allocated
// in some other fashion, a deletion function must be supplied.
void VariableLengthData::takeDataPointer( void* inData,
                                          size_t inSize,
                                          VariableLengthDataDeleteFunction func )
{
	// delete what we already have if we're responsible for it
	if( this->_impl->deleteFunction != NULL )
		this->_impl->deleteFunction( this->_impl->data );

	// make some new space and copy stuff over
	this->_impl->data = ::operator new( inSize );
	this->_impl->size = inSize;
	this->_impl->deleteFunction = operator delete; // for now
	memcpy( this->_impl->data, inData, inSize );

	// we might have a special delete function that should be called
	if( func != 0 )
		this->_impl->deleteFunction = func;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
