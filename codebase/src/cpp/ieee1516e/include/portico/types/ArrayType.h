/*
 *   Copyright 2018 The Portico Project
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

#include "portico/IDatatype.h"
#include "portico/types/Dimension.h"
#include <list>

namespace portico1516e
{
	/**
	 * This class contains metadata about a FOM Array data type.
	 * <p/>
	 * An array data type is a homogenous collection of a specified data type. Array data types may
	 * be single or multi-dimensional, and each dimension may have a fixed or dynamic cardinality.
	 */
	class RTI_EXPORT ArrayType : public virtual IDatatype
	{
		//----------------------------------------------------------
		//                    STATIC VARIABLES
		//----------------------------------------------------------

		//----------------------------------------------------------
		//                   INSTANCE VARIABLES
		//----------------------------------------------------------
		private:
			std::wstring          name;      /// The name of this datatype
			IDatatype*            datatype;  /// The datatype that this array will store
			std::list<Dimension>  dimensions;

		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
		public:
			/**
			 * Constructor for a single dimension array type with Dynamic cardinality
			 *
			 * @param name the name of the array type
			 * @param datatype the type of data that will be stored in instances of this array
			 */
			ArrayType( const std::wstring& name, IDatatype* datatype );


			/**
			 * Constructor for a single dimension array type with a specified cardinality value
			 *
			 * @param name the name of the array type
			 * @param datatype the type of data that will be stored in instances of this array
			 * @param cardinality the cardinality of this array type
			 */
			ArrayType( const std::wstring& name, IDatatype *datatype, int cardinality );

			/**
			 * Constructor for an array type with an arbitrary number of dimensions
			 * <p/>
			 * <b>Note:</b> at least one dimension must be supplied.
			 *
			 * @param name the name of the array type
			 * @param datatype the type of data that will be stored in instances of this array
			 * @param dimensions the dimensions this array type will contain
			 */
			ArrayType( const std::wstring& name, 
					   IDatatype *datatype, 
					   const std::list<Dimension>& dimensions );

			virtual ~ArrayType();
		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
		public:
			/**
			 * Get the IDatatype associated with this array.
			 * <p/>
			 * <b>Memory Management</b> the pointers returned by this function points to 
			 * the internal LRC datatype cache and should not be deleted by the user.
			 *
			 * @return The IDatatype associated with this ArrayType.
			 *
			 * @see IDatatype
			 */
			IDatatype* getDatatype() const;

			/**
			 * Get the Demensions associated with this array object.
			 *
			 * @param datatype The IDatatype associated with this ArrayType.
			 *
			 * @see IDatatype
			 */
			std::list<Dimension> getDimensions() const;

			/**
			 * Get the cardinality lowest bounds of the first dimension.
			 *
			 * @return the lower bounds of the first dimension for this ArrayType
			 */
			int getCardinalityLowerBound() const;

			/**
			 * Get the cardinality upper bounds of the first dimension.
			 *
			 * @return the upper bounds of the first dimension for this ArrayType
			 */
			int getCardinalityUpperBound() const;

			/**
			 * Returns whether the cardinality of the first dimensino is dynamic.
			 *
			 * @return True if the first dimension's cardinality is dynamic, otherwise false.
			 */
			bool isCardinalityDynamic() const;

			/////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////// Datatype Interface ////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////
			virtual std::wstring getName() const;
			virtual DatatypeClass getDatatypeClass() const;

		//----------------------------------------------------------
		//                     STATIC METHODS
		//----------------------------------------------------------
	};
}
