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
ObjectClassHandle::ObjectClassHandle()
{
	this->_impl = new ObjectClassHandleImplementation();
	this->_impl->value = -1;
}

ObjectClassHandle::ObjectClassHandle( const ObjectClassHandle& rhs )
{
	this->_impl = new ObjectClassHandleImplementation();
	this->_impl->value = rhs._impl->value;
}

ObjectClassHandle::ObjectClassHandle( const VariableLengthData& value )
{
	this->_impl = new ObjectClassHandleImplementation();
	this->_impl->value = -1;
}

ObjectClassHandle::ObjectClassHandle( ObjectClassHandleImplementation *impl )
{
	this->_impl = new ObjectClassHandleImplementation();
	this->_impl->value = impl->value;
}

ObjectClassHandle::~ObjectClassHandle() throw()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
bool ObjectClassHandle::isValid() const
{
	return this->_impl->value != -1;
}

long ObjectClassHandle::hash() const
{
	return (long)this->_impl->value;
}

VariableLengthData ObjectClassHandle::encode() const
{
	return VariableLengthData( (void*)this->_impl->value, 4 );
}

void ObjectClassHandle::encode( VariableLengthData& buffer ) const
{
	// TODO Fill me out!!
}

size_t ObjectClassHandle::encode( void* buffer, size_t bufferSize ) const throw( CouldNotEncode )
{
	// TODO Fill me out!!
	return 1;
}

size_t ObjectClassHandle::encodedLength() const
{
	return sizeof(int32_t); // yeah, I know, should be 32
}

std::wstring ObjectClassHandle::toString() const
{
	wstringstream wss;
	wss << this->_impl->value;
	return wss.str();
}

///////////////////////////////////////////////////////
////// Private Methods ////////////////////////////////
///////////////////////////////////////////////////////
const ObjectClassHandleImplementation* ObjectClassHandle::getImplementation() const
{
	return this->_impl;
}

ObjectClassHandleImplementation* ObjectClassHandle::getImplementation()
{
	return this->_impl;
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
ObjectClassHandle& ObjectClassHandle::operator= ( ObjectClassHandle const & rhs )
{
	this->_impl->value = rhs._impl->value;
	return *this;
}

/* All invalid handles are equivalent */
bool ObjectClassHandle::operator== ( ObjectClassHandle const & rhs ) const
{
	return this->_impl->value == rhs._impl->value;
}

bool ObjectClassHandle::operator!= ( ObjectClassHandle const & rhs ) const
{
	return this->_impl->value != rhs._impl->value;
}

bool ObjectClassHandle::operator< ( ObjectClassHandle const & rhs ) const
{
	return this->_impl->value < rhs._impl->value;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
