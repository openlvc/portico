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

/**
 * Represents one particular form that a VariantRecordType may assume.
 * @see VariantRecordType
 */
class Alternative
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		std::wstring           name;          /// The name of this datatype
		IDatatype*             datatype;      /// The size of this datatype
		std::list<Enumerator*> enumerators;   /// The enumerators that this type is valid for

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		/**
		 * Constructor for an Alternative with specified name, datatype and enumerator collection.
		 *
		 * @param name The name of the alternative
		 * @param datatype The datatype that the alternative will store
		 * @param enumerators The collection of discriminant enumerators that this type is valid for
		 */
		Alternative(const std::wstring& name, IDatatype* datatype, const std::list<Enumerator*>& enumerators);

		virtual ~Alternative();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		/**
		 * Get the IDatatype associated with this array.
		 *
		 * @note It is the caller's responsibility to clean up and manage the
		 *       returned datatype pointer.
		 *
		 * @return The IDatatype associated with this ArrayType.
		 * @see IDatatype
		 */
		virtual IDatatype* getDatatype() const;

		/**
		 * Get the list of Enumerator objects associated with this Alternative object.
		 * @return A set of Enumerators associated with this Alternative.
		 * @see Enumerator
		 */
		virtual std::list<Enumerator*> getEnumerators() const;

		/**
		 * Returns the name of this datatype.
		 * @return The name of this datatype as a string.
		 */
		virtual std::wstring getName() const;

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:

};
