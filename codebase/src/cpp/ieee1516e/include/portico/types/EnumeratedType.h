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
#include "portico/types/Enumerator.h"
#include "portico/types/BasicType.h"

namespace portico1516e
{
	/**
	 * This class contains metadata about a FOM Enumerated data type.
	 * <p/>
	 * An enumerated type represents a data element that can take on a finite discrete set of 
	 * possible values
	 *
	 * @see Enumerator
	 */
	class RTI_EXPORT EnumeratedType : public virtual IDatatype
	{
		//----------------------------------------------------------
		//                    STATIC VARIABLES
		//----------------------------------------------------------
		private:

		//----------------------------------------------------------
		//                   INSTANCE VARIABLES
		//----------------------------------------------------------
		private:
			std::wstring            name;            /// The name of this datatype
			IDatatype*              representation;  /// The datatype of this EnumeratedType
			std::list<Enumerator>  enumerators;      /// @see Enumerator

		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
		public:
			/**
			 * Create a EnumeratedType from a list of enumerators.
			 *
			 * @param name the name of the EnumerationType
			 * @param representation the datatype that this enumerated type will represent
			 * @param enumerators this type's enumerators
			 */
			EnumeratedType( const std::wstring& name, 
							IDatatype* representation, 
							const std::list<Enumerator>& enumerators );

			virtual ~EnumeratedType();

		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
		public:
			/**
			 * Returns the datatype that this enumerated type represents
			 *
			 * @note It is the caller's responsibility to clean up and manage the
			 *       returned datatype pointer.
			 *
			 * @return The IDatatype that this EnumeratedType represents.
			 *
			 * @see IDatatype
			 */
			IDatatype* getRepresentation() const;

			/**
			 * Returns the list of discrete enumerator values associated with this 
			 * EnumeratedType.
			 *
			 * @return The enumerators associated with this EnumeratedType.
			 *
			 * @see IDatatype
			 * @see Enumerator
			 */
			std::list<Enumerator> getEnumerators() const;

			/**
			 * @return <code>true</code> if the provided {@link EnumeratorType} is identical 
			 * to this type, otherwise <code>false</code>
			 */
			bool operator==( const EnumeratedType& other ) const;

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