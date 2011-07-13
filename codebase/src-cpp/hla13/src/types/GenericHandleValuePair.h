/*
 *   Copyright 2007 The Portico Project
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
#ifndef GENERIC_HANDLE_VALUE_PAIR_H
#define GENERIC_HANDLE_VALUE_PAIR_H

#include "common.h"

PORTICO13_NS_START

class GenericHandleValuePair
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		HLA::Handle theHandle;
		char *buffer;
		HLA::ULong valueLength;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		GenericHandleValuePair();
		~GenericHandleValuePair();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		HLA::Handle getHandle();
		void setHandle( HLA::Handle newHandle );
		void setValue( const char *newData, HLA::ULong newDataLength );
		char* getValue();
		HLA::ULong getValueLength();
		
		// sets the value to the given data, but DOESN'T COPY IT. this means that
		// when call this method, it is passing responsibility for deleting it over
		// to us and making the guarantee that the caller won't delete the data.
		void setValueButDontCopy( char *newData, HLA::ULong newDataLength );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

};

PORTICO13_NS_END

#endif

