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
#include "GenericHandleValuePair.h"

PORTICO13_NS_START

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////// Constructors ////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
GenericHandleValuePair::GenericHandleValuePair()
{
	this->theHandle = 0;
	this->buffer = NULL;
	this->valueLength = 0;
}

GenericHandleValuePair::~GenericHandleValuePair()
{
	if( this->buffer != NULL )
		delete [] this->buffer;
}

//////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// Instance Methods //////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////

HLA::Handle GenericHandleValuePair::getHandle()
{
	return this->theHandle;
}

void GenericHandleValuePair::setHandle( HLA::Handle newHandle )
{
	this->theHandle = newHandle;
}

void GenericHandleValuePair::setValue( const char *newData, HLA::ULong newDataLength )
{
	if( this->buffer != NULL )
		delete [] this->buffer;

	this->buffer = new char[newDataLength]();
	memcpy( this->buffer, newData, newDataLength );
	this->valueLength = newDataLength;
}

// sets the value to the given data, but DOESN'T COPY IT. this means that
// when call this method, it is passing responsibility for deleting it over
// to us and making the guarantee that the caller won't delete the data.
void GenericHandleValuePair::setValueButDontCopy( char *newData, HLA::ULong newDataLength )
{
	if( this->buffer != NULL )
	{
		delete [] this->buffer;
		this->buffer = NULL;
	}

	// don't copy! we're just going to use what's given to us
	this->buffer = newData;
	this->valueLength = newDataLength;
}

char* GenericHandleValuePair::getValue()
{
	return this->buffer;
}

HLA::ULong GenericHandleValuePair::getValueLength()
{
	return this->valueLength;
}
//////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Static Methods ///////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////

PORTICO13_NS_END
