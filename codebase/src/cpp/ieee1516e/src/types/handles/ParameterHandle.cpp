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
ParameterHandle::ParameterHandle()
{
	this->_impl = new ParameterHandleImplementation();
	this->_impl->value = -1;
}

ParameterHandle::ParameterHandle( const ParameterHandle& rhs )
{
	this->_impl = new ParameterHandleImplementation();
	this->_impl->value = rhs._impl->value;
}

ParameterHandle::ParameterHandle( const VariableLengthData& value )
{
	this->_impl = new ParameterHandleImplementation();
	this->_impl->value = -1;
}

ParameterHandle::ParameterHandle( ParameterHandleImplementation *impl )
{
	this->_impl = new ParameterHandleImplementation();
	this->_impl->value = impl->value;
}

ParameterHandle::~ParameterHandle() throw()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
bool ParameterHandle::isValid() const
{
	return this->_impl->value != -1;
}

long ParameterHandle::hash() const
{
	return (long)this->_impl->value;
}

VariableLengthData ParameterHandle::encode() const
{
	return VariableLengthData( (void*)&this->_impl->value, 4 );
}

void ParameterHandle::encode( VariableLengthData& buffer ) const
{
	// TODO Fill me out!!
}

size_t ParameterHandle::encode( void* buffer, size_t bufferSize ) const throw( CouldNotEncode )
{
	// TODO Fill me out!!
	return 1;
}

size_t ParameterHandle::encodedLength() const
{
	return sizeof(int32_t); // yeah, I know, should be 32
}

std::wstring ParameterHandle::toString() const
{
	wstringstream wss;
	wss << this->_impl->value;
	return wss.str();
}

///////////////////////////////////////////////////////
////// Private Methods ////////////////////////////////
///////////////////////////////////////////////////////
const ParameterHandleImplementation* ParameterHandle::getImplementation() const
{
	return this->_impl;
}

ParameterHandleImplementation* ParameterHandle::getImplementation()
{
	return this->_impl;
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
ParameterHandle& ParameterHandle::operator= ( ParameterHandle const & rhs )
{
	this->_impl->value = rhs._impl->value;
	return *this;
}

/* All invalid handles are equivalent */
bool ParameterHandle::operator== ( ParameterHandle const & rhs ) const
{
	return this->_impl->value == rhs._impl->value;
}

bool ParameterHandle::operator!= ( ParameterHandle const & rhs ) const
{
	return this->_impl->value != rhs._impl->value;
}

bool ParameterHandle::operator< ( ParameterHandle const & rhs ) const
{
	return this->_impl->value < rhs._impl->value;
}

std::wostream& operator<< ( std::wostream& stream, const ParameterHandle& handle )
{
	return stream << handle.toString();
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
