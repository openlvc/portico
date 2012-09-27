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
FederateHandle::FederateHandle()
{
	this->_impl = new FederateHandleImplementation();
	this->_impl->value = -1;
}

FederateHandle::FederateHandle( const FederateHandle& rhs )
{
	this->_impl = new FederateHandleImplementation();
	this->_impl->value = rhs._impl->value;
}

FederateHandle::FederateHandle( const VariableLengthData& value )
{
	this->_impl = new FederateHandleImplementation();
	this->_impl->value = -1;
}

FederateHandle::FederateHandle( FederateHandleImplementation *impl )
{
	this->_impl = new FederateHandleImplementation();
	this->_impl->value = impl->value;
}

FederateHandle::~FederateHandle() throw()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
bool FederateHandle::isValid() const
{
	return this->_impl->value != -1;
}

long FederateHandle::hash() const
{
	return (long)this->_impl->value;
}

VariableLengthData FederateHandle::encode() const
{
	return VariableLengthData( (void*)this->_impl->value, 4 );
}

void FederateHandle::encode( VariableLengthData& buffer ) const
{
	// TODO Fill me out!!
}

size_t FederateHandle::encode( void* buffer, size_t bufferSize ) const throw( CouldNotEncode )
{
	// TODO Fill me out!!
	return 1;
}

size_t FederateHandle::encodedLength() const
{
	return sizeof(int32_t); // yeah, I know, should be 32
}

std::wstring FederateHandle::toString() const
{
	wstringstream wss;
	wss << this->_impl->value;
	return wss.str();
}

///////////////////////////////////////////////////////
////// Private Methods ////////////////////////////////
///////////////////////////////////////////////////////
const FederateHandleImplementation* FederateHandle::getImplementation() const
{
	return this->_impl;
}

FederateHandleImplementation* FederateHandle::getImplementation()
{
	return this->_impl;
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
FederateHandle& FederateHandle::operator= ( FederateHandle const & rhs )
{
	this->_impl->value = rhs._impl->value;
	return *this;
}

/* All invalid handles are equivalent */
bool FederateHandle::operator== ( FederateHandle const & rhs ) const
{
	return this->_impl->value == rhs._impl->value;
}

bool FederateHandle::operator!= ( FederateHandle const & rhs ) const
{
	return this->_impl->value != rhs._impl->value;
}

bool FederateHandle::operator< ( FederateHandle const & rhs ) const
{
	return this->_impl->value < rhs._impl->value;
}

std::wostream& operator<< ( std::wostream& stream, const FederateHandle& handle )
{
	return stream << handle.toString();
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
