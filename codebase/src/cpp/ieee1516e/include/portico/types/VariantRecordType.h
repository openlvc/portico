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

#include <list>
#include "portico/IDatatype.h"
#include "portico/types/Alternative.h"

namespace portico1516e
{
	/**
	 * This class contains metadata about a FOM Variant data type.
	 * <p/>
	 * A variant record datatype represents a discriminated union of types.
	 *
	 * @see Alternative
	 */
	class RTI_EXPORT VariantRecordType : public virtual IDatatype
	{
		//----------------------------------------------------------
		//                    STATIC VARIABLES
		//----------------------------------------------------------
	
		//----------------------------------------------------------
		//                   INSTANCE VARIABLES
		//----------------------------------------------------------
		private:
			std::wstring            name;                   /// The name of this datatype
			IDatatype*              discriminantDatatype;   /// The discriminant Datatype
			std::wstring            discriminantName;       /// The discriminant datatype name
			std::list<Alternative>  alternatives;           /// @see Alternative

		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
		public:
			/**
			 * Constructor for VariantRecordType
			 *
			 * @param name the name of this data type
			 * @param discriminantName the name of the discriminantor
			 * @param discriminantDatatype the EnumeratedType that acts as the discriminator
			 * @param alternatives the alternative records based on the discriminant value
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
			 * Returns the name of the discriminant.
			 * <p/>
			 * <b>Note:</b> The name does not represent the name of the disciminant datatype, 
			 * rather a semantic name for the discriminant itself.
			 * 
			 * @return the name of the discriminant. This value 
			 */
			std::wstring getDiscriminantName() const;

			/**
			 * Returns the {@link EnumeratedDatatype} that acts as the discriminant
			 * <p/>
			 * <b>Memory Management</b> the pointer returned by this function points to the 
			 * internal LRC datatype cache and should not be deleted by the user.
			 *
			 * @return the discriminant datatype
			 */
			IDatatype* getDiscriminantDatatype() const;

			/**
			 * @returns all Alternative records for this variant type
			 */
			std::list<Alternative> getAlternatives() const;

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
