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
InteractionClassHandle::InteractionClassHandle()
{
	this->_impl = new InteractionClassHandleImplementation();
	this->_impl->value = -1;
}

InteractionClassHandle::InteractionClassHandle( const InteractionClassHandle& rhs )
{
	this->_impl = new InteractionClassHandleImplementation();
	this->_impl->value = rhs._impl->value;
}

InteractionClassHandle::InteractionClassHandle( const VariableLengthData& value )
{
	this->_impl = new InteractionClassHandleImplementation();
	this->_impl->value = -1;
}

InteractionClassHandle::InteractionClassHandle( InteractionClassHandleImplementation *impl )
{
	this->_impl = new InteractionClassHandleImplementation();
	this->_impl->value = impl->value;
}

InteractionClassHandle::~InteractionClassHandle() throw()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
bool InteractionClassHandle::isValid() const
{
	return this->_impl->value != -1;
}

long InteractionClassHandle::hash() const
{
	return (long)this->_impl->value;
}

VariableLengthData InteractionClassHandle::encode() const
{
	return VariableLengthData( (void*)&(this->_impl->value), 4 );
}

void InteractionClassHandle::encode( VariableLengthData& buffer ) const
{
	// TODO Fill me out!!
}

size_t InteractionClassHandle::encode( void* buffer, size_t bufferSize ) const throw( CouldNotEncode )
{
	// TODO Fill me out!!
	return 1;
}

size_t InteractionClassHandle::encodedLength() const
{
	return sizeof(int32_t); // yeah, I know, should be 32
}

std::wstring InteractionClassHandle::toString() const
{
	wstringstream wss;
	wss << this->_impl->value;
	return wss.str();
}

///////////////////////////////////////////////////////
////// Private Methods ////////////////////////////////
///////////////////////////////////////////////////////
const InteractionClassHandleImplementation* InteractionClassHandle::getImplementation() const
{
	return this->_impl;
}

InteractionClassHandleImplementation* InteractionClassHandle::getImplementation()
{
	return this->_impl;
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
InteractionClassHandle& InteractionClassHandle::operator= ( InteractionClassHandle const & rhs )
{
	this->_impl->value = rhs._impl->value;
	return *this;
}

/* All invalid handles are equivalent */
bool InteractionClassHandle::operator== ( InteractionClassHandle const & rhs ) const
{
	return this->_impl->value == rhs._impl->value;
}

bool InteractionClassHandle::operator!= ( InteractionClassHandle const & rhs ) const
{
	return this->_impl->value != rhs._impl->value;
}

bool InteractionClassHandle::operator< ( InteractionClassHandle const & rhs ) const
{
	return this->_impl->value < rhs._impl->value;
}

std::wostream& operator<< ( std::wostream& stream, const InteractionClassHandle& handle )
{
	return stream << handle.toString();
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
