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
MessageRetractionHandle::MessageRetractionHandle()
{
	this->_impl = new MessageRetractionHandleImplementation();
	this->_impl->value = -1;
}

MessageRetractionHandle::MessageRetractionHandle( const MessageRetractionHandle& rhs )
{
	this->_impl = new MessageRetractionHandleImplementation();
	this->_impl->value = rhs._impl->value;
}

MessageRetractionHandle::MessageRetractionHandle( const VariableLengthData& value )
{
	this->_impl = new MessageRetractionHandleImplementation();
	this->_impl->value = -1;
}

MessageRetractionHandle::MessageRetractionHandle( MessageRetractionHandleImplementation *impl )
{
	this->_impl = new MessageRetractionHandleImplementation();
	this->_impl->value = impl->value;
}

MessageRetractionHandle::~MessageRetractionHandle() throw()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
bool MessageRetractionHandle::isValid() const
{
	return this->_impl->value != -1;
}

long MessageRetractionHandle::hash() const
{
	return (long)this->_impl->value;
}

VariableLengthData MessageRetractionHandle::encode() const
{
	return VariableLengthData( (void*)this->_impl->value, 4 );
}

void MessageRetractionHandle::encode( VariableLengthData& buffer ) const
{
	// TODO Fill me out!!
}

size_t MessageRetractionHandle::encode( void* buffer, size_t bufferSize ) const throw( CouldNotEncode )
{
	// TODO Fill me out!!
	return 1;
}

size_t MessageRetractionHandle::encodedLength() const
{
	return sizeof(int32_t); // yeah, I know, should be 32
}

std::wstring MessageRetractionHandle::toString() const
{
	wstringstream wss;
	wss << this->_impl->value;
	return wss.str();
}

///////////////////////////////////////////////////////
////// Private Methods ////////////////////////////////
///////////////////////////////////////////////////////
const MessageRetractionHandleImplementation* MessageRetractionHandle::getImplementation() const
{
	return this->_impl;
}

MessageRetractionHandleImplementation* MessageRetractionHandle::getImplementation()
{
	return this->_impl;
}

//------------------------------------------------------------------------------------------
//                                    OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
MessageRetractionHandle& MessageRetractionHandle::operator= ( MessageRetractionHandle const & rhs )
{
	this->_impl->value = rhs._impl->value;
	return *this;
}

/* All invalid handles are equivalent */
bool MessageRetractionHandle::operator== ( MessageRetractionHandle const & rhs ) const
{
	return this->_impl->value == rhs._impl->value;
}

bool MessageRetractionHandle::operator!= ( MessageRetractionHandle const & rhs ) const
{
	return this->_impl->value != rhs._impl->value;
}

bool MessageRetractionHandle::operator< ( MessageRetractionHandle const & rhs ) const
{
	return this->_impl->value < rhs._impl->value;
}

std::wostream& operator<< ( std::wostream& stream, const MessageRetractionHandle& handle )
{
	return stream << handle.toString();
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
