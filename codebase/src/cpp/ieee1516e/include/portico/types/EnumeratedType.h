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
#include "portico/types/BasicType.h"
#include <list>

/**
 * This class contains metadata about a FOM Enumerated data type.
 * <p/>
 * An enumerated type represents a data element that can take on a finite discrete set of possible
 * values
 * @see Enumerator
 */
class EnumeratedType : public virtual IDatatype
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
		IDatatype*              representation;  /// The size of this datatype
		std::list<Enumerator*>  enumerators;     /// @see Enumerator

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		/**
		* Create a EnumerationType from a list of enumeration strings.
		*
		* @param name the name of the EnumerationType
		* @param representation the type of data that will be stored in instances of this array
		* @param enumerators A list of enumerator names (that will be given default int values ??)
		*/
		EnumeratedType(const std::wstring& name, IDatatype* representation, const std::list<std::wstring>& enumerators);

		/**
		* Create a EnumerationType from a list of enumerations.
		*
		* @param name the name of the EnumerationType
		* @param representation the type of data that will be stored in instances of this array
		* @param enumerators A list of enumerator names (that will be given default int values ??)
		*/
		EnumeratedType(const std::wstring& name, IDatatype* representation, const std::list<Enumerator*>& enumerators);

		virtual ~EnumeratedType();
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		/**
		 * Get the IDatatype Representation of this EnumerationType.
		 *
		 * @note It is the caller's responsibility to clean up and manage the
		 *       returned datatype pointer.
		 *
		 * @return The IDatatype representation of this EnumerationType.
		 * @see IDatatype
		 */
		virtual IDatatype* getRepresentation() const;

		/**
		 * Get the Enumerators associated with this EnumerationType.
		 *
		 * @return The Enumerators associated with this EnumerationType as a list.
		 * @see IDatatype
		 * @see Enumeration
		 */
		virtual std::list<Enumerator*> getEnumerators() const;

		/**
		 * Check to see if two EnumeratedTypes are equal.
		 *
		 * @return True if they are equal, otherwise false.
		 */
		virtual bool operator==(const EnumeratedType& other);

		/**
		 * Take a list of strings and create enumerations from them.
		 *
		 * @param constants A list of string name values.
		 * @return A list of Enumerator objects.
		 */
		virtual std::list<Enumerator*> createEnumeratorsFromNames(const std::list<std::wstring>& constants);

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
