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
#ifndef HANDLESET_H_
#define HANDLESET_H_

#include "common.h"

PORTICO13_NS_START

class AttributeHandleSet : public HLA::AttributeHandleSet
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		set<HLA::AttributeHandle> handles;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		AttributeHandleSet( HLA::ULong size );
		virtual ~AttributeHandleSet();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private:
		void checkIndex( HLA::ULong index ) const throw( HLA::ArrayIndexOutOfBounds );

	public:
		virtual HLA::ULong size() const;

		virtual HLA::AttributeHandle getHandle( HLA::ULong index ) const
			throw( HLA::ArrayIndexOutOfBounds );

		virtual void add( HLA::AttributeHandle handle )
			throw( HLA::ArrayIndexOutOfBounds, HLA::AttributeNotDefined );

		virtual void remove( HLA::AttributeHandle handle ) throw( HLA::AttributeNotDefined );

		virtual void empty();
		
		virtual HLA::Boolean isEmpty() const;
		
		virtual HLA::Boolean isMember( HLA::AttributeHandle handle ) const;

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

};

PORTICO13_NS_END

#endif /* HANDLESET_H_ */
