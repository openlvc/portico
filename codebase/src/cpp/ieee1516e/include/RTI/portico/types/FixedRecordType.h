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
File: RTI/portico/types/FixedRecordType.h
***********************************************************************/
#pragma once

#include "RTI/portico/IDatatype.h" 
#include "RTI/portico/types/Field.h"
#include <list> 

/**
* This class contains metadata about a FOM Fixed Record data type.
* <p/>
* An fixed record type is a heterogeneous collections of types. Fixed record types contain named
* fields that are of other types, allowing users to build "structures of data structures".
* @see Field
*/
class FixedRecordType : public virtual IDatatype
{
protected:	 
	std::string	        name;   /// The name of this datatype 
	std::list<Field>   fields; /// The fileds that make up the record.
	
public:

	/**
	* Constructor for a fixed record with an arbitrary number of fields
	*
	* @param name the name of the fixed record type
	* @param fields the ordered list of fields that this fixed record type will contain
	*/
	FixedRecordType(const std::string& name, std::list<Field> fields);
 
	virtual ~FixedRecordType();

	/////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// FixedRecordType Interface /////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////

	/**
	* Get the list of fields this FixedRecordType contains. 
	*
	* @return The list of fields of this FixedRecordType contains. 
	* @see IDatatype
	* @see Field
	*/
	virtual std::list<Field>& getFields();
  
	/**
	* Check to see if two FixedRecordTypes are equal.
	*
	* @return True if they are equal, otherwise false.
	*/
	virtual bool operator==(const FixedRecordType& other);

	/////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Datatype Interface ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////

public:
	/**
	* Returns the name of this datatype.
	*
	* @return The name of this datatype as a string.
	*/
	virtual std::string getName() const ;

	/**
	* Returns the FOM datatype class of this datatype (e.g. Basic, Simple, Enumerated, Array,
	* FixedRecord or Variant).
	*
	* @return the DatatypeClass of this record.
	* @see DatatypeClass.
	*/
	virtual DatatypeClass getDatatypeClass() ; 
};
