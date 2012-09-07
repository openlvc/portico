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
#ifndef HLAINTEGER64TIMEIMPL_H_
#define HLAINTEGER64TIMEIMPL_H_

#include "common.h"

IEEE1516E_NS_START

/*
 * As required by the HLA standard headers, we have a time impl type to sit
 * inside an HLAinteger64Time. Why we have to have this and the stanard headers
 * couldn't just declare a member of type double I don't know, but here we
 * are anyway.
 */
class HLAinteger64TimeImpl
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
		HLAinteger64TimeImpl( Integer64 value );
		HLAinteger64TimeImpl( const HLAinteger64TimeImpl& rhs );
		~HLAinteger64TimeImpl();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		Integer64 getValue();
		void setValue( Integer64 value );

		// operators
		operator Integer64();
		HLAinteger64TimeImpl& operator= ( const HLAinteger64TimeImpl& rhs );

	private:

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:

};

IEEE1516E_NS_END

#endif /* HLAINTEGER64TIMEIMPL_H_ */
