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

#include <string>
#include "RTI/SpecificConfig.h"

namespace portico1516e
{
	/**
	 * Describes a possible value of an {@link EnumeratedType}.
	 *
	 * @see EnumeratedType
	 * @see VariantRecordType
	 */
	class RTI_EXPORT Enumerator
	{
		//----------------------------------------------------------
		//                    STATIC VARIABLES
		//----------------------------------------------------------

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
			 * @param name the name of the enumerator constant
			 * @param value the plain text value of the enumerator value, as it appears in the FOM
			 */
			Enumerator( const std::wstring& name, const std::wstring& value );

			virtual ~Enumerator();

		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
		public:
			/**
			 * Check to see if two Enumerators are equal.
			 *
			 * @return <code>true</code> if they are equal, otherwise <code>false</code>.
			 */
			virtual bool operator==(const Enumerator& other) const;

			/**
			 * @return the name of this enumerator constant.
			 */
			virtual std::wstring getName() const;

			/**
			 * @return the value of this enumerator constant
			 */
			virtual std::wstring getValue() const;

		//----------------------------------------------------------
		//                     STATIC METHODS
		//----------------------------------------------------------
	};
}
