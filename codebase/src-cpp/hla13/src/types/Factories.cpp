/*
 *   Copyright 2009 The Portico Project
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

#include "AttributeHandleSet.h"
#include "AttributeHandleValuePairSet.h"
#include "FederateHandleSet.h"
#include "ParameterHandleValuePairSet.h"

HLA::AttributeHandleSet* HLA::AttributeHandleSetFactory::create( HLA::ULong size )
	throw( HLA::MemoryExhausted, HLA::ValueCountExceeded )
{
	HLA::AttributeHandleSet *theSet = new portico13::AttributeHandleSet( size );
	return theSet;
}

HLA::AttributeHandleValuePairSet* HLA::AttributeSetFactory::create( HLA::ULong size )
	throw( HLA::MemoryExhausted, HLA::ValueCountExceeded, HLA::HandleValuePairMaximumExceeded )
{
	HLA::AttributeHandleValuePairSet *theSet = new portico13::AttributeHandleValuePairSet( size );
	return theSet;
}

HLA::FederateHandleSet* HLA::FederateHandleSetFactory::create( HLA::ULong size )
	throw( HLA::MemoryExhausted, HLA::ValueCountExceeded )
{
	HLA::FederateHandleSet *theSet = new portico13::FederateHandleSet( size );
	return theSet;
}

HLA::ParameterHandleValuePairSet* HLA::ParameterSetFactory::create( HLA::ULong size )
	throw( HLA::MemoryExhausted, HLA::ValueCountExceeded, HLA::HandleValuePairMaximumExceeded )
{
	HLA::ParameterHandleValuePairSet *theSet = new portico13::ParameterHandleValuePairSet( size );
	return theSet;
}

