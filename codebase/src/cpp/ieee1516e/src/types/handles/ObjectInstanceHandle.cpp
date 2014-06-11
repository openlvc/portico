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
ObjectInstanceHandle::ObjectInstanceHandle()
{
	this->_impl = new ObjectInstanceHandleImplementation();
	this->_impl->value = -1;
}

ObjectInstanceHandle::ObjectInstanceHandle( const ObjectInstanceHandle& rhs )
{
	this->_impl = new ObjectInstanceHandleImplementation();
	this->_impl->value = rhs._impl->value;
}

ObjectInstanceHandle::ObjectInstanceHandle( const VariableLengthData& value )
{
	this->_impl = new ObjectInstanceHandleImplementation();
	this->_impl->value = -1;
}

ObjectInstanceHandle::ObjectInstanceHandle( ObjectInstanceHandleImplementation *impl )
{
	this->_impl = new ObjectInstanceHandleImplementation();
	this->_impl->value = impl->value;
}

ObjectInstanceHandle::~ObjectInstanceHandle() throw()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
bool ObjectInstanceHandle::isValid() const
{
	return this->_impl->value != -1;
}

long ObjectInstanceHandle::hash() const
{
	return (long)this->_impl->value;
}

VariableLengthData ObjectInstanceHandle::encode() const
{
	return VariableLengthData( (void*)&this->_impl->value, 4 );
}

void ObjectInstanceHandle::encode( VariableLengthData& buffer ) const
{
	// TODO Fill me out!!
}

size_t ObjectInstanceHandle::encode( void* buffer, size_t bufferSize ) const throw( CouldNotEncode )
{
	// TODO Fill me out!!
	return 1;
}

size_t ObjectInstanceHandle::encodedLength() const
{
	return sizeof(int32_t); // yeah, I know, should be 32
}

std::wstring ObjectInstanceHandle::toString() const
{
	wstringstream wss;
	wss << this->_impl->value;
	return wss.str();
}

///////////////////////////////////////////////////////
////// Private Methods ////////////////////////////////
///////////////////////////////////////////////////////
const ObjectInstanceHandleImplementation* ObjectInstanceHandle::getImplementation() const
{
	return this->_impl;
}

ObjectInstanceHandleImplementation* ObjectInstanceHandle::getImplementation()
{
	return this->_impl;
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
ObjectInstanceHandle& ObjectInstanceHandle::operator= ( ObjectInstanceHandle const & rhs )
{
	this->_impl->value = rhs._impl->value;
	return *this;
}

/* All invalid handles are equivalent */
bool ObjectInstanceHandle::operator== ( ObjectInstanceHandle const & rhs ) const
{
	return this->_impl->value == rhs._impl->value;
}

bool ObjectInstanceHandle::operator!= ( ObjectInstanceHandle const & rhs ) const
{
	return this->_impl->value != rhs._impl->value;
}

bool ObjectInstanceHandle::operator< ( ObjectInstanceHandle const & rhs ) const
{
	return this->_impl->value < rhs._impl->value;
}

std::wostream& operator<< ( std::wostream& stream, const ObjectInstanceHandle& handle )
{
	return stream << handle.toString();
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
