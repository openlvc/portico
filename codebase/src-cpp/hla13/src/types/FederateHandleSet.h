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
#ifndef FEDERATEHANDLESET_H_
#define FEDERATEHANDLESET_H_

#include "common.h"

PORTICO13_NS_START

class FederateHandleSet : public HLA::FederateHandleSet
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		set<HLA::FederateHandle> handles;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		FederateHandleSet( HLA::ULong size );
		virtual ~FederateHandleSet();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private:
		void checkIndex( HLA::ULong index ) const throw( HLA::ArrayIndexOutOfBounds );

	public:
		virtual HLA::ULong size() const;

		virtual HLA::FederateHandle getHandle( HLA::ULong index ) const
			throw( HLA::ArrayIndexOutOfBounds );

		virtual void add( HLA::FederateHandle handle ) throw( HLA::ValueCountExceeded );

		virtual void remove( HLA::FederateHandle handle ) throw( HLA::ArrayIndexOutOfBounds );

		virtual void empty();

		virtual HLA::Boolean isMember( HLA::FederateHandle handle ) const;

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

};

PORTICO13_NS_END

#endif /* FEDERATEHANDLESET_H_ */
