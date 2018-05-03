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

/**
* Represents the discrete datatypes that can be specified in the FOM.
*/
enum DatatypeClass
{
	/**
	 * Underpinning of all OMT datatypes
	 */
	BASIC,
	/**
	 * Simple, scalar data items
	 */
	SIMPLE,
	/**
	 * Data elements that can take on a finite discrete set of possible values
	 */
	ENUMERATED,
	/**
	 * Indexed homogenous collections of datatypes
	 */
	ARRAY,
	/**
	 * Heterogeneous collections of types
	 */
	FIXEDRECORD,
	/**
	 * Discriminated unions of types
	 */
	VARIANTRECORD,
	/**
	 * NA type (supports HLAprivelegeToDelete in 1516)
	 */
	NA
};
