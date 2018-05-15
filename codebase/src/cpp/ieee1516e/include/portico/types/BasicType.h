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
#include "portico/types/Endianness.h"

namespace portico1516e
{
	/**
	 * This class contains metadata about a FOM Basic data type.
	 * <p/>
	 * Basic data types represent primitive data types in the FOM and are often the building 
	 * blocks of more complex data types.
	 */
	class RTI_EXPORT BasicType : public virtual IDatatype
	{
		//----------------------------------------------------------
		//                    STATIC VARIABLES
		//----------------------------------------------------------

		//----------------------------------------------------------
		//                   INSTANCE VARIABLES
		//----------------------------------------------------------
		private:
			std::wstring   name;        /// The name of this datatype
			int            size;        /// The size of this datatype
			Endianness     endianness;  /// The endianness of this datatype

		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
		public:
			/**
			 * Constructor for BasicType with specified name, size and endianness
			 *
			 * @param name the name of this data type
			 * @param size the size of this data type in bits
			 * @param endianness the byte ordering of this data type
			 */
			BasicType( const std::wstring& name, int size, Endianness endianness );
			virtual ~BasicType();

		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
		public:
			/**
			 * @return the size of this datatype, in bits
			 */
			int getSize() const;

			/**
			 * @return the endianness of this datatype
			 */
			Endianness getEndianness() const;

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