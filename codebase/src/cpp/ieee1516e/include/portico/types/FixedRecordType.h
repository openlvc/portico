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
#include <list>

/**
 * This class contains metadata about a FOM Fixed Record data type.
 * <p/>
 * An fixed record type is a heterogeneous collections of types. Fixed record types contain named
 * fields that are of other types, allowing users to build "structures of data structures".
 * @see Field
 */
class FixedRecordType : public virtual IDatatype
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		std::wstring      name;   /// The name of this datatype
		std::list<Field>  fields; /// The fileds that make up the record.

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		/**
		 * Constructor for a fixed record with an arbitrary number of fields
		 *
		 * @param name the name of the fixed record type
		 * @param fields the ordered list of fields that this fixed record type will contain
		 */
		FixedRecordType(const std::wstring& name, const std::list<Field>& fields);

		virtual ~FixedRecordType();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		/**
		 * Get the list of fields this FixedRecordType contains.
		 *
		 * @return The list of fields of this FixedRecordType contains.
		 * @see IDatatype
		 * @see Field
		 */
		virtual std::list<Field> getFields() const;

		/**
		 * Check to see if two FixedRecordTypes are equal.
		 *
		 * @return True if they are equal, otherwise false.
		 */
		virtual bool operator==(const FixedRecordType& other) const;

		/////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////// Datatype Interface ////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////

		virtual std::wstring getName() const;

		virtual DatatypeClass getDatatypeClass() const;

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:

};
