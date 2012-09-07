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
#ifndef HLAFLOAT64INTERVALIMPL_H_
#define HLAFLOAT64INTERVALIMPL_H_

#include "common.h"

IEEE1516E_NS_START

/*
 * As required by the HLA standard headers, we have an interval impl type to sit
 * inside an HLAfloat64Interval. Why we have to have this and the stanard headers
 * couldn't just declare a member of type double I don't know, but here we are anyway.
 */
class HLAfloat64IntervalImpl
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		Float64 value;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		HLAfloat64IntervalImpl( Float64 value );
		HLAfloat64IntervalImpl( const HLAfloat64IntervalImpl& rhs );
		~HLAfloat64IntervalImpl();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		Float64 getValue();
		void setValue( Float64 value );

		// operators
		operator Float64();
		HLAfloat64IntervalImpl& operator= ( const HLAfloat64IntervalImpl& rhs );

	private:

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:

};

IEEE1516E_NS_END

#endif /* HLAFLOAT64INTERVALIMPL_H_ */
