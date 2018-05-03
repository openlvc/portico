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
#include "portico/types/BasicType.h"

/**
 * This class contains metadata about a FOM Simple data type.
 *
 * A simple type describes a simple, scalar data item
 */
class SimpleType : public virtual IDatatype
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		std::wstring  name;				/// The name of this datatype
		IDatatype*    representation;	/// BasicType or DatatypePlaceholder only

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		/**
		 * Constructor for SimpleType with specified name and representation
		 *
		 * @param name the name of this data type
		 * @param representation // BasicType or DatatypePlaceholder only
		 */
		SimpleType(const std::wstring& name, IDatatype *representation);
		virtual ~SimpleType();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		/**
		 * Returns the FOM datatype class of this datatype (e.g. Basic, Simple, Enumerated, Array,
		 * FixedRecord or Variant).
		 *
		 * @note It is the caller's responsibility to clean up and manage the
		 *       returned datatype pointer.
		 *
		 * @return The BasicType of this datatype representation.
		 * @see BasicType.
		 */
		virtual IDatatype* getRepresentation();


		//////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////// Datatype Interface /////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////////////

		virtual std::wstring getName() const;

		virtual DatatypeClass getDatatypeClass() const;

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:

};
