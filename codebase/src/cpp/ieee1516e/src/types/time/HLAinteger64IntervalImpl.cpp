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
#include "types/time/HLAinteger64IntervalImpl.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
HLAinteger64IntervalImpl::HLAinteger64IntervalImpl( Integer64 value )
{
	this->value = value;
}

HLAinteger64IntervalImpl::HLAinteger64IntervalImpl( const HLAinteger64IntervalImpl& rhs )
{
	this->value = rhs.value;
}

HLAinteger64IntervalImpl::~HLAinteger64IntervalImpl()
{
	
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
Integer64 HLAinteger64IntervalImpl::getValue()
{
	return this->value;
}

void HLAinteger64IntervalImpl::setValue( Integer64 value )
{
	this->value = value;
}

//------------------------------------------------------------------------------------------
//                                     OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
HLAinteger64IntervalImpl::operator Integer64()
{
	return value;
}

HLAinteger64IntervalImpl& HLAinteger64IntervalImpl::operator= ( const HLAinteger64IntervalImpl& rhs )
{
	this->value = rhs.value;
	return *this;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
