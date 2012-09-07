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
#include "types/time/HLAfloat64IntervalImpl.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
HLAfloat64IntervalImpl::HLAfloat64IntervalImpl( Float64 value )
{
	this->value = value;
}

HLAfloat64IntervalImpl::HLAfloat64IntervalImpl( const HLAfloat64IntervalImpl& rhs )
{
	this->value = rhs.value;
}

HLAfloat64IntervalImpl::~HLAfloat64IntervalImpl()
{
	
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
Float64 HLAfloat64IntervalImpl::getValue()
{
	return this->value;
}

void HLAfloat64IntervalImpl::setValue( Float64 value )
{
	this->value = value;
}

//------------------------------------------------------------------------------------------
//                                     OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
HLAfloat64IntervalImpl::operator Float64()
{
	return value;
}

HLAfloat64IntervalImpl& HLAfloat64IntervalImpl::operator= ( const HLAfloat64IntervalImpl& rhs )
{
	this->value = rhs.value;
	return *this;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
