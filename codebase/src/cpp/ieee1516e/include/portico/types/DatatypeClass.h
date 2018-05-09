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

#include "RTI/SpecificConfig.h"

namespace portico1516e
{
	/**
	* Represents the discrete datatypes that can be specified in the FOM.
	*/
	enum RTI_EXPORT DatatypeClass
	{
		/**
		 * Underpinning of all OMT datatypes
		 */
		DATATYPE_BASIC,

		/**
		 * Simple, scalar data items
		 */
		DATATYPE_SIMPLE,

		/**
		 * Data elements that can take on a finite discrete set of possible values
		 */
		DATATYPE_ENUMERATED,

		/**
		 * Indexed homogenous collections of datatypes
		 */
		DATATYPE_ARRAY,

		/**
		 * Heterogeneous collections of types
		 */
		DATATYPE_FIXEDRECORD,

		/**
		 * Discriminated unions of types
		 */
		DATATYPE_VARIANTRECORD,

		/**
		 * NA type (supports privilegeToDelete in 1516)
		 */
		DATATYPE_NA
	};
}
