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

#include "portico/types/Endianness.h"
#include <string>

/**
 * Stores the name and value of an enumerator used by the Enumerated type.
 * @see EnumeratedType
 * @see VariantRecordType
 */
class Enumerator
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		std::wstring name;
		std::wstring value;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		/**
		 * Constructor for BasicType with specified name, size and endianness
		 *
		 * @param name the name of this enumerator
		 * @param value the value of this enumerator
		 */
		Enumerator(const std::wstring& name, const std::wstring& value);

		virtual ~Enumerator();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		/**
		 * Check to see if two Enumerators are equal..
		 *
		 * @return True if they are equal, otherwise false.
		 */
		virtual bool operator==(const Enumerator& other);

		/**
		 * Returns the name of this datatype.
		 * @return The name of this datatype as a string.
		 */
		virtual std::wstring getName() const;

		/**
		 * Returns the FOM datatype class of this datatype (e.g. Basic, Simple, Enumerated, Array,
		 * FixedRecord or Variant).
		 *
		 * @return the DatatypeClass of this record.
		 * @see DatatypeClass.
		 */
		virtual std::wstring getValue() const;

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:
 
};
