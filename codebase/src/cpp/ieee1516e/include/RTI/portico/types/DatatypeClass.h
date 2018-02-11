/***********************************************************************
The IEEE hereby grants a general, royalty-free license to copy, distribute,
display and make derivative works from this material, for all purposes,
provided that any use of the material contains the following
attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
Should you require additional information, contact the Manager, Standards
Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).

Copyright 2017 The Portico Project

This file is part of portico.
portico is free software; you can redistribute it and/or modify
it under the terms of the Common Developer and Distribution License (CDDL)
as published by Sun Microsystems. For more information see the LICENSE file.
Use of this software is strictly AT YOUR OWN RISK!!!
If something bad happens you do not have permission to come crying to me.
(that goes for your lawyer as well)
***********************************************************************/
/***********************************************************************
IEEE 1516.1 High Level Architecture Interface Specification C++ API
File: RTI/portico/types/DatatypeClass.h
***********************************************************************/

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