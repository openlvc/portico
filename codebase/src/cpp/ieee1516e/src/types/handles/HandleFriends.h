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
#ifndef HANDLEFRIENDS_H_
#define HANDLEFRIENDS_H_

#include "common.h"
#include "types/handles/HandleImplementations.h"


//
// Declare a friend class for each of the standard handle types. This will
// contain some helper methods for getting useful information about the 
// underlying handle implementations. The bodies are defined in HandleFriends.cpp
//
#define HANDLE_FRIEND_HEADER(Type)                             \
class Type##Friend                                             \
{                                                              \
	public:                                                    \
		static Type create( int32_t value );                   \
		static std::string toString( Type* handle );           \
		static std::string toString( Type& handle );           \
		static std::string toString( const Type& handle );     \
};


// expand the macro
IEEE1516E_NS_START

HANDLE_FRIEND_HEADER(FederateHandle)
HANDLE_FRIEND_HEADER(ObjectClassHandle)
HANDLE_FRIEND_HEADER(InteractionClassHandle)
HANDLE_FRIEND_HEADER(ObjectInstanceHandle)
HANDLE_FRIEND_HEADER(AttributeHandle)
HANDLE_FRIEND_HEADER(ParameterHandle)
HANDLE_FRIEND_HEADER(DimensionHandle)
HANDLE_FRIEND_HEADER(MessageRetractionHandle)
HANDLE_FRIEND_HEADER(RegionHandle)

IEEE1516E_NS_END

#endif /* HANDLEFRIENDS_H_ */
