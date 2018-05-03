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

#include "portico//IDatatype.h"
#include "portico//types/Alternative.h"
#include <list>

/**
 * This class contains metadata about a FOM Simple data type.
 * <p/>
 * A variant record datatype represents a discriminated union of types.
 * @see Alternative
 */
class VariantRecordType : public virtual IDatatype
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		std::wstring            name;		            /// The name of this datatype
		IDatatype*              discriminantDatatype;	/// The descriminant Datatype
		std::wstring            discriminantName;       /// The descriminant datatype name
		std::list<Alternative>  alternatives;			/// @see Alternative
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		/**
		 * Constructor for VariantRecordType
		 *
		 * @param name the name of this data type
		 * @param discriminantName the name of the descriminant datatype
		 * @param discriminantDatatype a descriminant Datatype from the DataClassType enumerations.
		 * @param alternatives the alternative datatypes
		 */	
		VariantRecordType(const std::wstring& name,
		                  const std::wstring& discriminantName,
		                  IDatatype* discriminantDatatype,
		                  const std::list<Alternative>&  alternatives);

		virtual ~VariantRecordType();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		/**
		 * Get the descriminate datatype name.
		 *
		 * @return the discriminant name as a string
		 */
		virtual std::wstring getDiscriminateName() const;

		/**
		 * Get the descriminate datatype.
		 *
		 * @note It is the caller's responsibility to clean up and manage the
		 *       returned datatype pointer.
		 *
		 * @return the discriminant datatype
		 */
		virtual IDatatype* getDiscriminateDatatype() const;

		/**
		 * Get the Alternate.
		 *
		 * @return the the Alternate for this variant record
		 */
		virtual std::list<Alternative> getAlternatives() const;


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
