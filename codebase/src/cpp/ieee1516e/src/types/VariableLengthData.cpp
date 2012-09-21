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
#include "VariableLengthDataImplementation.h"

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
VariableLengthData::VariableLengthData()
{
	this->_impl = new VariableLengthDataImplementation();
}

// Caller is free to delete inData after the call
VariableLengthData::VariableLengthData( const void* inData, size_t inSize )
{
	this->_impl = new VariableLengthDataImplementation( inData, inSize );
}

// Caller is free to delete rhs after the call
VariableLengthData::VariableLengthData( const VariableLengthData& rhs )
{
	this->_impl = new VariableLengthDataImplementation( *rhs._impl );
}

VariableLengthData::~VariableLengthData()
{
	if( this->_impl != NULL )
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

	this->_impl = rhs._impl;
	return *this;
}

// This pointer should not be expected to be valid past the
// lifetime of this object, or past the next time this object
// is given new data
const void* VariableLengthData::data() const
{
	return this->_impl->data();
}

size_t VariableLengthData::size() const
{
	return this->_impl->size();
}

// Caller is free to delete inData after the call
void VariableLengthData::setData( const void* inData, size_t inSize )
{
	this->_impl->setData( inData, inSize );
}

// Caller is responsible for ensuring that the data that is
// pointed to is valid for the lifetime of this object, or past
// the next time this object is given new data.
void VariableLengthData::setDataPointer( void* inData, size_t inSize )
{
	this->_impl->setDataPointer( inData, inSize );
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
	this->_impl->takeDataPointer( inData, inSize, func );
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

