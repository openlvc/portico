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
AttributeHandle::AttributeHandle()
{
	this->_impl = new AttributeHandleImplementation();
	this->_impl->value = -1;
}

AttributeHandle::AttributeHandle( const AttributeHandle& rhs )
{
	this->_impl = new AttributeHandleImplementation();
	this->_impl->value = rhs._impl->value;
}

AttributeHandle::AttributeHandle( const VariableLengthData& value )
{
	this->_impl = new AttributeHandleImplementation();
	this->_impl->value = -1;
}

AttributeHandle::AttributeHandle( AttributeHandleImplementation *impl )
{
	this->_impl = new AttributeHandleImplementation();
	this->_impl->value = impl->value;
}

AttributeHandle::~AttributeHandle() throw()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
bool AttributeHandle::isValid() const
{
	return this->_impl->value != -1;
}

long AttributeHandle::hash() const
{
	return (long)this->_impl->value;
}

VariableLengthData AttributeHandle::encode() const
{
	return VariableLengthData( (void*)this->_impl->value, 4 );
}

void AttributeHandle::encode( VariableLengthData& buffer ) const
{
	// TODO Fill me out!!
}

size_t AttributeHandle::encode( void* buffer, size_t bufferSize ) const throw( CouldNotEncode )
{
	// TODO Fill me out!!
	return 1;
}

size_t AttributeHandle::encodedLength() const
{
	return sizeof(int32_t); // yeah, I know, should be 32
}

std::wstring AttributeHandle::toString() const
{
	wstringstream wss;
	wss << this->_impl->value;
	return wss.str();
}

///////////////////////////////////////////////////////
////// Private Methods ////////////////////////////////
///////////////////////////////////////////////////////
const AttributeHandleImplementation* AttributeHandle::getImplementation() const
{
	return this->_impl;
}

AttributeHandleImplementation* AttributeHandle::getImplementation()
{
	return this->_impl;
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
AttributeHandle& AttributeHandle::operator= ( AttributeHandle const & rhs )
{
	this->_impl->value = rhs._impl->value;
	return *this;
}

/* All invalid handles are equivalent */
bool AttributeHandle::operator== ( AttributeHandle const & rhs ) const
{
	return this->_impl->value == rhs._impl->value;
}

bool AttributeHandle::operator!= ( AttributeHandle const & rhs ) const
{
	return this->_impl->value != rhs._impl->value;
}

bool AttributeHandle::operator< ( AttributeHandle const & rhs ) const
{
	return this->_impl->value < rhs._impl->value;
}

std::wostream& operator<< ( std::wostream& stream, const AttributeHandle& handle )
{
	return stream << handle.toString();
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
