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

namespace portico1516e
{
	/**
	 * This class contains metadata about a FOM Simple data type.
	 *
	 * A simple type describes a simple, scalar data item
	 */
	class RTI_EXPORT SimpleType : public virtual IDatatype
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
			IDatatype*    representation;	/// BasicType only

		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
		public:
			/**
			 * Constructor for SimpleType with specified name and representation
			 *
			 * @param name the name of this data type
			 * @param representation the BasicDatatype that this simple type represents
			 */
			SimpleType( const std::wstring& name, IDatatype *representation );
			virtual ~SimpleType();

		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
		public:
			/**
			 * Returns the {@link BasicDatatype} that this datatype represents
			 *
			 * <b>Memory Management</b> the pointer returned by this function points to the 
			 * internal LRC datatype cache and should not be deleted by the caller.
			 *
			 * @return The {@link BasicType} representation of this datatype
			 *
			 * @see BasicType.
			 */
			IDatatype* getRepresentation();

			//////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////// Datatype Interface /////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////
			virtual std::wstring getName() const;
			virtual DatatypeClass getDatatypeClass() const;

		//----------------------------------------------------------
		//                     STATIC METHODS
		//----------------------------------------------------------
	};
}
