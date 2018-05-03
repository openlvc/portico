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

#include "portico/types/Field.h"
#include "portico/IDatatype.h"

/**
 * Stores the name and datatype of a field used in the FixedRecordType
 * @see FixedRecordType
 */
class Field
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:

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
		Field(const std::wstring& name, IDatatype* datatype);

		virtual ~Field();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		/**
		 * Returns the name of this datatype.
		 * @return The name of this datatype as a string.
		 */
		virtual std::wstring getName() const;

		/**
		 * Returns the datatype class of this field.
		 *
		 * @note It is the caller's responsibility to clean up and manage the
		 *       returned datatype pointer.
		 *
		 * @return the Datatype of this record.
		 * @see DatatypeClass.
		 */
		virtual IDatatype* getDatatype();

		/**
		 * Check to see if two Field objects are equal.
		 *
		 * @return True if they are equal, otherwise false.
		 */
		virtual bool operator==(const Field& other) const;

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:
 
};
