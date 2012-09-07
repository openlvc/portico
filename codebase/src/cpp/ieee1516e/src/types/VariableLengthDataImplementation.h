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
#ifndef VARIABLELENGTHDATA_H_
#define VARIABLELENGTHDATA_H_

#include "common.h"

IEEE1516E_NS_START

/*
 * Underlying implementation for VariableLengthData as prescribed by the HLA
 * headers. This just mirrors (as much as it can) the VariableLengthData definition
 * so that it can be delegated to directly.
 */
class VariableLengthDataImplementation
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
		VariableLengthDataImplementation();
		~VariableLengthDataImplementation();

		// Caller is free to delete inData after the call
		VariableLengthDataImplementation( const void* inData, size_t inSize );

		// Caller is free to delete rhs after the call
		VariableLengthDataImplementation( const VariableLengthDataImplementation& rhs );

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		// Caller is free to delete rhs after the call
		// This instance will revert to internal storage as a result of assignment.
		VariableLengthDataImplementation& operator= ( const VariableLengthDataImplementation& rhs );
		
		// This pointer should not be expected to be valid past the
		// lifetime of this object, or past the next time this object
		// is given new data
		const void* data() const;

		size_t size() const;
		
		// Caller is free to delete inData after the call
		void setData( const void* inData, size_t inSize );

		// Caller is responsible for ensuring that the data that is
		// pointed to is valid for the lifetime of this object, or past
		// the next time this object is given new data.
		void setDataPointer( void* inData, size_t inSize );

		// Caller gives up ownership of inData to this object.
		// This object assumes the responsibility of deleting inData
		// when it is no longer needed.
		// The allocation of inData is assumed to have been through an array
		// alloctor (e.g., char* data = new char[20]. If the data was allocated
		// in some other fashion, a deletion function must be supplied.
		void takeDataPointer( void* inData,
		                      size_t inSize,
		                      VariableLengthDataDeleteFunction func = 0 );

	private:

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:

};

IEEE1516E_NS_END

#endif /* VARIABLELENGTHDATA_H_ */
