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
#include "types/time/HLAinteger64TimeImpl.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
HLAinteger64TimeImpl::HLAinteger64TimeImpl( Integer64 value )
{
	this->value = value;
}

HLAinteger64TimeImpl::HLAinteger64TimeImpl( const HLAinteger64TimeImpl& rhs )
{
	this->value = rhs.value;
}

HLAinteger64TimeImpl::~HLAinteger64TimeImpl()
{
	
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
Integer64 HLAinteger64TimeImpl::getValue()
{
	return this->value;
}

void HLAinteger64TimeImpl::setValue( Integer64 value )
{
	this->value = value;
}

//------------------------------------------------------------------------------------------
//                                     OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
HLAinteger64TimeImpl::operator Integer64()
{
	return value;
}

HLAinteger64TimeImpl& HLAinteger64TimeImpl::operator= ( const HLAinteger64TimeImpl& rhs )
{
	this->value = rhs.value;
	return *this;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
