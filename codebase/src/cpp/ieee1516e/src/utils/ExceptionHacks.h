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
#ifndef EXCEPTIONHACKS_H_
#define EXCEPTIONHACKS_H_

#include "common.h"

PORTICO1516E_NS_START

/**
 * This class provides utility methods that take information about an exception
 * that occurred in the Java side of the binding and cause it to be packaged up
 * and thrown as an exception in the C++ side.
 */
class ExceptionHacks
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:
		static void checkAndThrow( string name, string reason ); // throws any HLA exception

};

PORTICO1516E_NS_END

#endif /* EXCEPTIONHACKS_H_ */
