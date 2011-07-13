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
#ifndef EXCEPTIONHACKS_H_
#define EXCEPTIONHACKS_H_

#include "common.h"

// macro to make the throwing and cleanup of information easier
#define CLEAN_AND_THROW(A) \
	A exception( reason ); \
	delete[] name;         \
	delete[] reason;       \
	throw exception;       \

PORTICO13_NS_START

class ExceptionHacks
{
		//----------------------------------------------------------
		//                    STATIC VARIABLES
		//----------------------------------------------------------

		//----------------------------------------------------------
		//                   INSTANCE VARIABLES
		//----------------------------------------------------------
	private:

		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
	public:
		ExceptionHacks();
		virtual ~ExceptionHacks();

		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
	public:

		//----------------------------------------------------------
		//                     STATIC METHODS
		//----------------------------------------------------------
		static void cleanAndThrow( char *name, char *reason ); // throws any HLA exception

};

PORTICO13_NS_END

#endif /* EXCEPTIONHACKS_H_ */
