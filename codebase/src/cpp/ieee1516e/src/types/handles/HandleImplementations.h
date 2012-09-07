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
#ifndef HANDLEIMPLEMENTATIONS_H_
#define HANDLEIMPLEMENTATIONS_H_

#include "common.h"
#include "types/handles/PorticoHandle.h"

//
// The macro in RTI/Handle.h declares each handle to have a member (_impl)
// of type <HandleName>Implementation. They do this through the use of a
// forward declaration of the class. The full declarations for each of these
// types can be found below. They lean pretty much entirely on PorticoHandle
// and the bodies are provided in HandleImplementations.cpp
//
#define HANDLE_IMPL_HEADER(Name)                                     \
	class Name : public PORTICO1516E_NS::PorticoHandle               \
	{                                                                \
		public:                                                      \
			Name();                                                  \
			Name( int32_t handle );                                  \
			Name( const VariableLengthData& encodedValue );          \
	};

// expand the macro
IEEE1516E_NS_START

HANDLE_IMPL_HEADER(FederateHandleImplementation)
HANDLE_IMPL_HEADER(ObjectClassHandleImplementation)
HANDLE_IMPL_HEADER(InteractionClassHandleImplementation)
HANDLE_IMPL_HEADER(ObjectInstanceHandleImplementation)
HANDLE_IMPL_HEADER(AttributeHandleImplementation)
HANDLE_IMPL_HEADER(ParameterHandleImplementation)
HANDLE_IMPL_HEADER(DimensionHandleImplementation)
HANDLE_IMPL_HEADER(MessageRetractionHandleImplementation)
HANDLE_IMPL_HEADER(RegionHandleImplementation)

IEEE1516E_NS_END

#endif /* HANDLEIMPLEMENTATIONS_H_ */
