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
#include "types/handles/PorticoHandle.h"

PORTICO1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
PorticoHandle::PorticoHandle()
	: handle(-1)
{
}

PorticoHandle::PorticoHandle( int32_t handle )
	: handle( handle )
{
}

PorticoHandle::PorticoHandle( const VariableLengthData& encodedValue )
	: handle(-1)
{
}

PorticoHandle::~PorticoHandle()
{
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
bool PorticoHandle::isValid() const
{
	return this->handle != -1;
}

long PorticoHandle::hash() const
{
	return (long)this->handle;
}

////////////////////////////////////
VariableLengthData PorticoHandle::encode() const
{
	return VariableLengthData( (void*)this->handle, 4 );
}

void PorticoHandle::encode( VariableLengthData& buffer ) const
{
	// TODO Fill me out!!
}

size_t PorticoHandle::encode( void* buffer, size_t bufferSize ) const throw( CouldNotEncode )
{
	// TODO Fill me out!!
	return 1;
}

size_t PorticoHandle::encodedLength() const
{
	return sizeof(int32_t); // yeah, I know, should be 32
}

std::wstring PorticoHandle::toString() const
{
	wstringstream wss;
	wss << this->handle;
	return wss.str();
}

// non-standard methods in the implementation
int32_t PorticoHandle::getHandle()
{
	return this->handle;
}

void PorticoHandle::setHandle( int32_t handle )
{
	this->handle = handle;
}

std::string PorticoHandle::toStdString() const
{
	stringstream ss;
	ss << this->handle;
	return ss.str();
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
PorticoHandle& PorticoHandle::operator= ( PorticoHandle const & rhs )
{
	this->handle = rhs.handle;
	return *this;
}

/* All invalid handles are equivalent */
bool PorticoHandle::operator== ( PorticoHandle const & rhs ) const
{
	return this->handle == rhs.handle;
}

bool PorticoHandle::operator!= ( PorticoHandle const & rhs ) const
{
	return this->handle != rhs.handle;
}

bool PorticoHandle::operator< ( PorticoHandle const & rhs ) const
{
	return this->handle < rhs.handle;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

PORTICO1516E_NS_END
