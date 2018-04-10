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
File: RTI/portico/types/Enumerator.h
***********************************************************************/

#pragma once

#include "RTI/portico/types/Endianness.h"  
#include <string>

/**
*  Stores the name and value of an enumerator used by the Enumerated type.
* @see EnumeratedType
* @see VariantRecordType
*/
class Enumerator  
{
protected:	 

    std::string name;
    std::string value;

public:

	/**
	* Constructor for BasicType with specified name, size and endianness
	* 
	* @param name the name of this enumerator
	* @param value the value of this enumerator
	*/
	Enumerator(const std::string& name, const std::string& value);
 
	virtual ~Enumerator();

	/////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Enumerator Interface ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////	
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
	virtual std::string getName() const;

	/**
	* Returns the FOM datatype class of this datatype (e.g. Basic, Simple, Enumerated, Array,
	* FixedRecord or Variant).
	*
	* @return the DatatypeClass of this record.
	* @see DatatypeClass.
	*/
	virtual std::string getValue();
 
};
