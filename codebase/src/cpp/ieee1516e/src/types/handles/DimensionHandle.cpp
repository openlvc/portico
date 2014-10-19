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
#include "types/handles/HandleImplementations.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
DimensionHandle::DimensionHandle()
{
	this->_impl = new DimensionHandleImplementation();
	this->_impl->value = -1;
}

DimensionHandle::DimensionHandle( const DimensionHandle& rhs )
{
	this->_impl = new DimensionHandleImplementation();
	this->_impl->value = rhs._impl->value;
}

DimensionHandle::DimensionHandle( const VariableLengthData& value )
{
	this->_impl = new DimensionHandleImplementation();
	this->_impl->value = -1;
}

DimensionHandle::DimensionHandle( DimensionHandleImplementation *impl )
{
	this->_impl = new DimensionHandleImplementation();
	this->_impl->value = impl->value;
}

DimensionHandle::~DimensionHandle() throw()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
bool DimensionHandle::isValid() const
{
	return this->_impl->value != -1;
}

long DimensionHandle::hash() const
{
	return (long)this->_impl->value;
}

VariableLengthData DimensionHandle::encode() const
{
	return VariableLengthData( (void*)&this->_impl->value, 4 );
}

void DimensionHandle::encode( VariableLengthData& buffer ) const
{
	// TODO Fill me out!!
}

size_t DimensionHandle::encode( void* buffer, size_t bufferSize ) const throw( CouldNotEncode )
{
	// TODO Fill me out!!
	return 1;
}

size_t DimensionHandle::encodedLength() const
{
	return sizeof(int32_t); // yeah, I know, should be 32
}

std::wstring DimensionHandle::toString() const
{
	wstringstream wss;
	wss << this->_impl->value;
	return wss.str();
}

///////////////////////////////////////////////////////
////// Private Methods ////////////////////////////////
///////////////////////////////////////////////////////
const DimensionHandleImplementation* DimensionHandle::getImplementation() const
{
	return this->_impl;
}

DimensionHandleImplementation* DimensionHandle::getImplementation()
{
	return this->_impl;
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
DimensionHandle& DimensionHandle::operator= ( DimensionHandle const & rhs )
{
	this->_impl->value = rhs._impl->value;
	return *this;
}

/* All invalid handles are equivalent */
bool DimensionHandle::operator== ( DimensionHandle const & rhs ) const
{
	return this->_impl->value == rhs._impl->value;
}

bool DimensionHandle::operator!= ( DimensionHandle const & rhs ) const
{
	return this->_impl->value != rhs._impl->value;
}

bool DimensionHandle::operator< ( DimensionHandle const & rhs ) const
{
	return this->_impl->value < rhs._impl->value;
}

std::wostream& operator<< ( std::wostream& stream, const DimensionHandle& handle )
{
	return stream << handle.toString();
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
