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

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
RangeBounds::RangeBounds()
{
	this->_lowerBound = 0;
	this->_upperBound = 0;
}

RangeBounds::RangeBounds( unsigned long lowerBound, unsigned long upperBound )
{
	this->_lowerBound = lowerBound;
	this->_upperBound = upperBound;
}

RangeBounds::~RangeBounds() throw()
{
	
}

RangeBounds::RangeBounds( const RangeBounds& rhs )
{
	this->_lowerBound = rhs._lowerBound;
	this->_upperBound = rhs._upperBound;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
unsigned long RangeBounds::getLowerBound() const
{
	return this->_lowerBound;
}

unsigned long RangeBounds::getUpperBound() const
{
	return this->_upperBound;
}

void RangeBounds::setLowerBound( unsigned long lowerBound )
{
	this->_lowerBound = lowerBound;
}

void RangeBounds::setUpperBound( unsigned long upperBound )
{
	this->_upperBound = upperBound;
}

//------------------------------------------------------------------------------------------
//                                     OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
RangeBounds& RangeBounds::operator= ( const RangeBounds& rhs )
{
	this->_lowerBound = rhs._lowerBound;
	this->_upperBound = rhs._upperBound;
	return *this;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
