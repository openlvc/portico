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
#include "portico/types/Enumerator.h"
#include <list>

namespace portico1516e
{
	/**
	 * Represents one particular form that a VariantRecordType may assume.
	 *
	 * @see VariantRecordType
	 */
	class RTI_EXPORT Alternative
	{
		//----------------------------------------------------------
		//                    STATIC VARIABLES
		//----------------------------------------------------------

		//----------------------------------------------------------
		//                   INSTANCE VARIABLES
		//----------------------------------------------------------
		private:
			std::wstring           name;          /// The name of this datatype
			IDatatype*             datatype;      /// The datatype that the alternative will store
			std::list<Enumerator>  enumerators;   /// The enumerators that this type is valid for

		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
		public:
			/**
			 * Constructor for an Alternative with specified name, datatype and enumerator 
			 * collection.
			 *
			 * @param name The name of the alternative
			 * @param datatype The datatype that the alternative will store
			 * @param enumerators The collection of discriminant enumerators that this type 
			 *                    is valid for
			 */
			Alternative( const std::wstring& name, 
						 IDatatype* datatype, 
						 const std::list<Enumerator>& enumerators);

			virtual ~Alternative();

		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
		public:
			/**
			 * Returns the IDatatype associated with this alternative.
			 * <p/>
			 * <b>Memory Management</b> the pointer returned by this function points to the 
			 * internal LRC datatype cache and should not be deleted by the user.
			 *
			 * @return The IDatatype associated with this Alternative.
			 * @see IDatatype
			 */
			IDatatype* getDatatype() const;

			/**
			 * Returns the list of Enumerators associated with this Alternative object.
			 *
			 * @return the Enumerators associated with this Alternative.
			 *
			 * @see Enumerator
			 */
			std::list<Enumerator> getEnumerators() const;

			/**
			 * @return the name of this datatype.
			 */
			std::wstring getName() const;

		//----------------------------------------------------------
		//                     STATIC METHODS
		//----------------------------------------------------------
	};
}