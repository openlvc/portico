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
#include "types/handles/HandleImplementations.h"

IEEE1516E_NS_START

//
// The macro in RTI/Handle.h declares each handle to have a member (_impl)
// of type <HandleName>Implementation. They do this through the use of a
// forward declaration of the class. The full declarations for each of these
// types can be found in the HandleImplemenations.h header file. This file
// contains the actual bodies. They lean pretty much entirely on PorticoHandle
// so we just need to construct things here and let the base class take care
// of the rest.
//
#define HANDLE_IMPL_BODY(Name)                                                              \
	Name::Name() : PORTICO1516E_NS::PorticoHandle(){};                                      \
	Name::Name(int32_t handle) : PORTICO1516E_NS::PorticoHandle(handle){};                  \
	Name::Name(const VariableLengthData& value) : PORTICO1516E_NS::PorticoHandle(value){};

HANDLE_IMPL_BODY(FederateHandleImplementation)
HANDLE_IMPL_BODY(ObjectClassHandleImplementation)
HANDLE_IMPL_BODY(InteractionClassHandleImplementation)
HANDLE_IMPL_BODY(ObjectInstanceHandleImplementation)
HANDLE_IMPL_BODY(AttributeHandleImplementation)
HANDLE_IMPL_BODY(ParameterHandleImplementation)
HANDLE_IMPL_BODY(DimensionHandleImplementation)
HANDLE_IMPL_BODY(MessageRetractionHandleImplementation)
HANDLE_IMPL_BODY(RegionHandleImplementation)

IEEE1516E_NS_END
	
