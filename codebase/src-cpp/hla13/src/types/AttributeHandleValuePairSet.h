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
#ifndef ATTRIBUTEHANDLEVALUEPAIRSET_H_
#define ATTRIBUTEHANDLEVALUEPAIRSET_H_

#include "common.h"
#include "GenericHandleValuePair.h"

PORTICO13_NS_START

class AttributeHandleValuePairSet : public HLA::AttributeHandleValuePairSet
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		map<HLA::Handle,GenericHandleValuePair*> pairs;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		AttributeHandleValuePairSet();
		AttributeHandleValuePairSet( HLA::ULong size );
		virtual ~AttributeHandleValuePairSet();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private:
		GenericHandleValuePair* getPairAt( HLA::ULong i ) const;
		void checkIndex( HLA::ULong index ) const throw( HLA::ArrayIndexOutOfBounds );

	public:
		virtual HLA::ULong size() const;

		virtual HLA::Handle getHandle( HLA::ULong index ) const
			throw( HLA::ArrayIndexOutOfBounds );

		virtual HLA::ULong getValueLength( HLA::ULong index ) const
			throw( HLA::ArrayIndexOutOfBounds );

		virtual void getValue( HLA::ULong index, char* buffer, HLA::ULong& valueLength ) const 
			throw( HLA::ArrayIndexOutOfBounds );

		virtual char* getValuePointer( HLA::ULong index, HLA::ULong& valueLength ) const
			throw( HLA::ArrayIndexOutOfBounds );

		virtual HLA::TransportType getTransportType( HLA::ULong index ) const
			throw( HLA::ArrayIndexOutOfBounds, HLA::InvalidHandleValuePairSetContext );

		virtual HLA::OrderType getOrderType( HLA::ULong index ) const
			throw( HLA::ArrayIndexOutOfBounds, HLA::InvalidHandleValuePairSetContext );

		virtual HLA::Region* getRegion( HLA::ULong index ) const
			throw( HLA::ArrayIndexOutOfBounds, HLA::InvalidHandleValuePairSetContext );

		virtual void add( HLA::Handle handle, const char* buffer, HLA::ULong valueLength )
			throw( HLA::ValueLengthExceeded, HLA::ValueCountExceeded );

		virtual void remove( HLA::Handle handle ) throw( HLA::ArrayIndexOutOfBounds );

		virtual void moveFrom( const HLA::AttributeHandleValuePairSet& ahvps, HLA::ULong& index ) 
			throw( HLA::ValueCountExceeded, HLA::ArrayIndexOutOfBounds );

		virtual void empty(); // Empty the Set without deallocating space.

		virtual HLA::ULong start() const;

		virtual HLA::ULong valid( HLA::ULong index ) const;

		virtual HLA::ULong next( HLA::ULong index ) const;

	public: // non-standard methods			
		// this method should NOT copy the given data, but rather, should just
		// store the pointer and take ownership for the maangement of the data.
		// this is designed to allow data coming in from the JNI to only be copied
		// once, rather than being copied from the JNI, and then copied again when
		// it is put in the set.
		virtual void addButDontCopy( HLA::Handle handle, char *buffer, HLA::ULong size );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

};

PORTICO13_NS_END

#endif /* ATTRIBUTEHANDLEVALUEPAIRSET_H_ */
