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
File: RTI/portico/IDataType.h
***********************************************************************/

#pragma once

#include "RTI/portico/types/DatatypeClass.h"
#include "RTI/SpecificConfig.h"
#include <string>

/**
* Common interface for all FOM datatypes.
*/
class RTI_EXPORT IDatatype
{
protected:
	IDatatype(){}

public:
	virtual ~IDatatype(){}

	/**
	* Returns the name of this datatype.
	*/
	virtual std::string getName() const = 0;

	/**
	* Returns the FOM datatype class of this datatype (e.g. Basic, Simple, Enumerated, Array,
	* Fixed Record or Variant).
	*/
	virtual DatatypeClass getDatatypeClass() = 0;
};
