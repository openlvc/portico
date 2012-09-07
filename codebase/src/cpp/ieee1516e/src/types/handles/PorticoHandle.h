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
#ifndef PORTICOHANDLE_H_
#define PORTICOHANDLE_H_

#include "common.h"

PORTICO1516E_NS_START

/**
 * Generic handle implementation type for all Portico 1516e handles.
 */
class PorticoHandle
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		int32_t handle;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		PorticoHandle();
		PorticoHandle( int32_t handle );
		PorticoHandle( const VariableLengthData& encodedValue );
		virtual ~PorticoHandle();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		bool isValid() const;
		long hash() const;

		VariableLengthData encode() const;
		void encode( VariableLengthData& buffer ) const;
		size_t encode( void* buffer, size_t bufferSize ) const throw( CouldNotEncode );
		size_t encodedLength() const;
		
		std::wstring toString() const;
		
		// non-standard methods in the implementation
		std::string toStdString() const;
		int32_t getHandle();
		void setHandle( int32_t handle );

	private:

	//----------------------------------------------------------
	//                   OPERATOR OVERLOADS
	//----------------------------------------------------------
	public:
		PorticoHandle& operator= ( PorticoHandle const & rhs );
		bool operator== ( PorticoHandle const & rhs ) const;
		bool operator!= ( PorticoHandle const & rhs ) const;
		bool operator< ( PorticoHandle const & rhs ) const;

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:

};

PORTICO1516E_NS_END

#endif /* PORTICOHANDLE_H_ */
