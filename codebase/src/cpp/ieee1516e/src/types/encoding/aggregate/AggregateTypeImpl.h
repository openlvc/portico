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
#pragma once
#include "common.h"
#include <RTI/encoding/DataElement.h>

IEEE1516E_NS_START

class AggregateTypeImpl
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		std::auto_ptr<DataElement> prototype;
		std::vector<DataElement*> elements;
		std::set<DataElement*> ownedElements;


	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		AggregateTypeImpl();
		AggregateTypeImpl( const DataElement& prototype, size_t size );
		AggregateTypeImpl( const AggregateTypeImpl& other );

		virtual ~AggregateTypeImpl();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private:
		/**
		 * Releases the memory of array element at index if it is internally managed by the array.
		 * If the element is not internally managed by the array, the corresponding memory is
		 * not affected.
		 *
		 * If the provided index is out of bounds, this call will have no affect.
		 */
		void releaseExistingElement( size_t index );

	public:
		void clear();

		void appendPrototypeClones( size_t count );

		void resize( size_t newSize );

		/**
		 * Sets the content of the array element at the specified index to match that of the
		 * provided element. If the element at index has been set to point to an external location
		 * through setExternal() that DataElement will be modified.
		 *
		 * If the provided index is out of bounds, this call will have no affect.
		 */
		void set( size_t index, const DataElement& element );

		/**
		 * Sets the array element at index to point to an external DataElement. Subsequent calls to
		 * get/set at this index will directly modify the element.
		 *
		 * The caller is responsible for ensuring that the memory specified by this method is
		 * available for the lifecycle of the array or until a new memory location is specified
		 * with a subsequent call to setExternal()
		 *
		 * If the provided index is out of bounds, this call will have no affect.
		 */
		void setExternal( size_t index, DataElement* element );

		void append( const DataElement& element );

		void append( DataElement* element );

		/**
		 * Returns a pointer to the DataElement at the specified index
		 *
		 * If the provided index is out of bounds NULL is returned
		 */
		DataElement* get( size_t index ) const;

		/**
		 * Returns the number of elements in the array
		 */
		size_t size() const;

		/**
		 * Returns true if the specified type is the same as this array's prototype
		 */
		const DataElement* getPrototype() const;

		bool isPrototypeSameTypeAs( const DataElement& element ) const;
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
};

// As the spec itself forward declares the implementation types, we cannot simply typedef them to
// the AggregateTypeImpl class above. Instead we must define the implementation type as a
//generalization of AggregateTypeImpl and provide the appropriate constructors.
//
// The macro below has been provided to keep this process to a single line statement
#define DEFINE_AGGREGATE_TYPE_IMPL( TypeName )                                      			\
class TypeName : public AggregateTypeImpl		                                          		\
{                                                                                               \
    public:  																					\
		TypeName() : AggregateTypeImpl() {}														\
        TypeName( const DataElement& prototype, size_t size ) :									\
			AggregateTypeImpl( prototype, size ) {}												\
        TypeName( const AggregateTypeImpl& other ) :											\
			AggregateTypeImpl( other ) {}	        											\
        virtual ~TypeName() {}																	\
};

IEEE1516E_NS_END

