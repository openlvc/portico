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
File: RTI/portico/types/BasicType.h
***********************************************************************/

#pragma once

#include "RTI/portico/IDatatype.h"
#include "RTI/portico/types/Endianness.h"  

 /**
 * This class contains metadata about a FOM Basic data type.
 * <p/>
 * Basic data types represent primitive data types in the FOM and are often the building blocks
 * of more complex data types.
 */
class BasicType : public virtual IDatatype
{
protected:	 

	std::string	name;		/// The name of this datatype
	int			size;		/// The size of this datatype
	Endianness	endianness; /// The endianness of this datatype
	
public:

	/**
	* Constructor for BasicType with specified name, size and endianness
	* 
	* @param name the name of this data type
	* @param size the size of this data type in bits
	* @param endianness the byte ordering of this data type
	*/
	BasicType(const std::string& name, int size, Endianness endianness);
	virtual ~BasicType();
	/////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Basictype Interface ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////	
	/**
	* Get the endianness of thei datatype.
	* 
	* @return otherBasicDataType A basic data type to compare to
	*/
	virtual Endianness getEndianness();

	/////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Datatype Interface ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
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
	virtual DatatypeClass getDatatypeClass(); 
 
};
