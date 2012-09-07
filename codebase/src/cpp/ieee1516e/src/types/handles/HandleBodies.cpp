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

//
// Handle types are defined by a macro in RTI/Handle.h (part of the standard
// headers). That macro declares a consistent header for each standard handle
// type. The macro below defines the bodies for those headers and is expanded
// at the bottom of this file.
//
#define HANDLE_BODY(Type)                                                                      \
/************************************************/                                             \
/**** Constructors ******************************/                                             \
/************************************************/                                             \
Type::Type()                                                                                   \
{                                                                                              \
	this->_impl = new Type##Implementation();                                                  \
}                                                                                              \
                                                                                               \
Type::Type( Type const & rhs )                                                                 \
{                                                                                              \
	this->_impl = new Type##Implementation( rhs._impl->getHandle() );                          \
}                                                                                              \
                                                                                               \
/* protected */                                                                                \
Type::Type( Type##Implementation* impl )                                                       \
{                                                                                              \
	this->_impl = impl;                                                                        \
}                                                                                              \
                                                                                               \
/* protected */                                                                                \
Type::Type( VariableLengthData const & encodedValue )                                          \
{                                                                                              \
	this->_impl = new Type##Implementation( encodedValue );                                    \
}                                                                                              \
                                                                                               \
Type::~Type() throw()                                                                          \
{                                                                                              \
	delete this->_impl;                                                                        \
}                                                                                              \
                                                                                               \
/************************************************/                                             \
/**** Instance Methods **************************/                                             \
/************************************************/                                             \
bool Type::isValid() const                                                                     \
{                                                                                              \
	return this->_impl->isValid();                                                             \
}                                                                                              \
                                                                                               \
long Type::hash() const                                                                        \
{                                                                                              \
	return this->_impl->hash();                                                                \
}                                                                                              \
                                                                                               \
VariableLengthData Type::encode() const                                                        \
{                                                                                              \
	return this->_impl->encode();                                                              \
}                                                                                              \
                                                                                               \
void Type::encode( VariableLengthData& buffer ) const                                          \
{                                                                                              \
	this->_impl->encode( buffer );                                                             \
}                                                                                              \
                                                                                               \
size_t Type::encode( void* buffer, size_t bufferSize ) const throw( CouldNotEncode )           \
{                                                                                              \
	return this->_impl->encode( buffer, bufferSize );                                          \
}                                                                                              \
                                                                                               \
size_t Type::encodedLength() const                                                             \
{                                                                                              \
	return this->_impl->encodedLength();                                                       \
}                                                                                              \
                                                                                               \
std::wstring Type::toString() const                                                            \
{                                                                                              \
	return this->_impl->toString();                                                            \
}                                                                                              \
                                                                                               \
/* protected */                                                                                \
const Type##Implementation* Type::getImplementation() const                                    \
{                                                                                              \
	return this->_impl;                                                                        \
}                                                                                              \
                                                                                               \
/* protected */                                                                                \
Type##Implementation* Type::getImplementation()                                                \
{                                                                                              \
	return this->_impl;                                                                        \
}                                                                                              \
                                                                                               \
/************************************************/                                             \
/**** Operator Overloads ************************/                                             \
/************************************************/                                             \
Type& Type::operator= ( Type const & rhs )                                                     \
{                                                                                              \
	this->_impl = rhs._impl;                                                                   \
	return *this;                                                                              \
}                                                                                              \
                                                                                               \
/* All invalid handles are equivalent */                                                       \
bool Type::operator== ( Type const & rhs ) const                                               \
{                                                                                              \
	return this->_impl == rhs._impl;                                                           \
}                                                                                              \
                                                                                               \
bool Type::operator!= ( Type const & rhs ) const                                               \
{                                                                                              \
	return this->_impl != rhs._impl;                                                           \
}                                                                                              \
                                                                                               \
bool Type::operator< ( Type const & rhs ) const                                                \
{                                                                                              \
	return this->_impl < rhs._impl;                                                            \
}                                                                                              \
                                                                                               \
std::wostream& operator<< ( std::wostream& stream, const Type& handle )                        \
{                                                                                              \
	return stream << handle.toString();                                                        \
}

// expand the macro
IEEE1516E_NS_START

HANDLE_BODY(FederateHandle)
HANDLE_BODY(ObjectClassHandle)
HANDLE_BODY(InteractionClassHandle)
HANDLE_BODY(ObjectInstanceHandle)
HANDLE_BODY(AttributeHandle)
HANDLE_BODY(ParameterHandle)
HANDLE_BODY(DimensionHandle)
HANDLE_BODY(MessageRetractionHandle)
HANDLE_BODY(RegionHandle)

IEEE1516E_NS_END
