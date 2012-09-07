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
#ifndef HLAINTEGER64INTERVALIMPL_H_
#define HLAINTEGER64INTERVALIMPL_H_

#include "common.h"

IEEE1516E_NS_START

/*
 * As required by the HLA standard headers, we have an interval impl type to sit
 * inside an HLAinteger64Interval. Why we have to have this and the stanard headers
 * couldn't just declare a member of type double I don't know, but here we are anyway.
 */
class HLAinteger64IntervalImpl
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		Integer64 value;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		HLAinteger64IntervalImpl( Integer64 value );
		HLAinteger64IntervalImpl( const HLAinteger64IntervalImpl& rhs );
		~HLAinteger64IntervalImpl();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		Integer64 getValue();
		void setValue( Integer64 value );

		// operators
		operator Integer64();
		HLAinteger64IntervalImpl& operator= ( const HLAinteger64IntervalImpl& rhs );

	private:

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:

};

IEEE1516E_NS_END

#endif /* HLAINTEGER64INTERVALIMPL_H_ */
