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
#include "types/VariableLengthDataImplementation.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
VariableLengthDataImplementation::VariableLengthDataImplementation()
{
	
}

// Caller is free to delete inData after the call
VariableLengthDataImplementation::VariableLengthDataImplementation( const void* inData,
                                                                    size_t inSize )
{
	
}

// Caller is free to delete rhs after the call
VariableLengthDataImplementation::
	VariableLengthDataImplementation( const VariableLengthDataImplementation& rhs )
{
	
}

VariableLengthDataImplementation::~VariableLengthDataImplementation()
{
	
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Caller is free to delete rhs after the call
// This instance will revert to internal storage as a result of assignment.
VariableLengthDataImplementation& VariableLengthDataImplementation::
	operator= ( const VariableLengthDataImplementation& rhs )
{
	if( this == &rhs )
		return *this;
	
	// ; do some copying
	
	// return a reference to us
	return *this;
}

// This pointer should not be expected to be valid past the
// lifetime of this object, or past the next time this object
// is given new data
const void* VariableLengthDataImplementation::data() const
{
	return NULL;
}

size_t VariableLengthDataImplementation::size() const
{
	return 0;
}

// Caller is free to delete inData after the call
void VariableLengthDataImplementation::setData( const void* inData, size_t inSize )
{
	
}

// Caller is responsible for ensuring that the data that is
// pointed to is valid for the lifetime of this object, or past
// the next time this object is given new data.
void VariableLengthDataImplementation::setDataPointer( void* inData, size_t inSize )
{
	
}

// Caller gives up ownership of inData to this object.
// This object assumes the responsibility of deleting inData
// when it is no longer needed.
// The allocation of inData is assumed to have been through an array
// alloctor (e.g., char* data = new char[20]. If the data was allocated
// in some other fashion, a deletion function must be supplied.
void VariableLengthDataImplementation::takeDataPointer( void* inData,
                                                        size_t inSize,
                                                        VariableLengthDataDeleteFunction func )
{
	
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
