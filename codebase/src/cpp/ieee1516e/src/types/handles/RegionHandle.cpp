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
RegionHandle::RegionHandle()
{
	this->_impl = new RegionHandleImplementation();
	this->_impl->value = -1;
}

RegionHandle::RegionHandle( const RegionHandle& rhs )
{
	this->_impl = new RegionHandleImplementation();
	this->_impl->value = rhs._impl->value;
}

RegionHandle::RegionHandle( const VariableLengthData& value )
{
	this->_impl = new RegionHandleImplementation();
	this->_impl->value = -1;
}

RegionHandle::RegionHandle( RegionHandleImplementation *impl )
{
	this->_impl = new RegionHandleImplementation();
	this->_impl->value = impl->value;
}

RegionHandle::~RegionHandle() throw()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
bool RegionHandle::isValid() const
{
	return this->_impl->value != -1;
}

long RegionHandle::hash() const
{
	return (long)this->_impl->value;
}

VariableLengthData RegionHandle::encode() const
{
	return VariableLengthData( (void*)this->_impl->value, 4 );
}

void RegionHandle::encode( VariableLengthData& buffer ) const
{
	// TODO Fill me out!!
}

size_t RegionHandle::encode( void* buffer, size_t bufferSize ) const throw( CouldNotEncode )
{
	// TODO Fill me out!!
	return 1;
}

size_t RegionHandle::encodedLength() const
{
	return sizeof(int32_t); // yeah, I know, should be 32
}

std::wstring RegionHandle::toString() const
{
	wstringstream wss;
	wss << this->_impl->value;
	return wss.str();
}

///////////////////////////////////////////////////////
////// Private Methods ////////////////////////////////
///////////////////////////////////////////////////////
const RegionHandleImplementation* RegionHandle::getImplementation() const
{
	return this->_impl;
}

RegionHandleImplementation* RegionHandle::getImplementation()
{
	return this->_impl;
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
RegionHandle& RegionHandle::operator= ( RegionHandle const & rhs )
{
	this->_impl->value = rhs._impl->value;
	return *this;
}

/* All invalid handles are equivalent */
bool RegionHandle::operator== ( RegionHandle const & rhs ) const
{
	return this->_impl->value == rhs._impl->value;
}

bool RegionHandle::operator!= ( RegionHandle const & rhs ) const
{
	return this->_impl->value != rhs._impl->value;
}

bool RegionHandle::operator< ( RegionHandle const & rhs ) const
{
	return this->_impl->value < rhs._impl->value;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
