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
#include "portico/types/Field.h"

namespace portico1516e
{
	/**
	 * Stores metadata of a {@link FixedRecordType} field
	 *
	 * @see FixedRecordType
	 */
	class RTI_EXPORT Field
	{
		//----------------------------------------------------------
		//                    STATIC VARIABLES
		//----------------------------------------------------------

		//----------------------------------------------------------
		//                   INSTANCE VARIABLES
		//----------------------------------------------------------
		private:
			std::wstring  name;
			IDatatype*    datatype;

		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
		public:
			/**
			 * Constructor for Field with specified name, and Datatype.
			 *
			 * @param name the name of this enumerator
			 * @param value the value of this enumerator
			 */
			Field( const std::wstring& name, IDatatype* datatype );
			virtual ~Field();

		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
		public:
			/**
			 * @return the name of this datatype.
			 */
			std::wstring getName() const;

			/**
			 * Returns the datatype class of this field.
			 * <p/>
			 * <b>Memory Management</b> the pointer returned by this function points to the 
			 * internal LRC datatype cache and should not be deleted by the user.
			 *
			 * @return the Datatype of this record.
			 * @see DatatypeClass.
			 */
			IDatatype* getDatatype() const;

			/**
			 * Check to see if two Field objects are equal.
			 *
			 * @return <code>true</code> if they are equal, otherwise <code>false</code>
			 */
			bool operator==( const Field& other ) const;

		//----------------------------------------------------------
		//                     STATIC METHODS
		//----------------------------------------------------------
	};
}
