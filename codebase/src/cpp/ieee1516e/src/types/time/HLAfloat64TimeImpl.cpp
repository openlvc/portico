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
#include "types/time/HLAfloat64TimeImpl.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
HLAfloat64TimeImpl::HLAfloat64TimeImpl( Float64 value )
{
	this->value = value;
}

HLAfloat64TimeImpl::HLAfloat64TimeImpl( const HLAfloat64TimeImpl& rhs )
{
	this->value = rhs.value;
}

HLAfloat64TimeImpl::~HLAfloat64TimeImpl()
{
	
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
Float64 HLAfloat64TimeImpl::getValue()
{
	return this->value;
}

void HLAfloat64TimeImpl::setValue( Float64 value )
{
	this->value = value;
}

//------------------------------------------------------------------------------------------
//                                     OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
HLAfloat64TimeImpl::operator Float64()
{
	return value;
}

HLAfloat64TimeImpl& HLAfloat64TimeImpl::operator= ( const HLAfloat64TimeImpl& rhs )
{
	this->value = rhs.value;
	return *this;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
