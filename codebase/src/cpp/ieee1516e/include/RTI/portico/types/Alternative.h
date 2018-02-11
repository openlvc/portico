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
File: RTI/portico/types/Alternative.h
***********************************************************************/

#pragma once

#include "RTI/portico/IDatatype.h"  
#include "RTI/portico/types/Enumerator.h"
#include <list>

/**
* Represents one particular form that a VariantRecordType may assume.
* @see VariantRecordType
*/
class Alternative 
{
protected:
	 
	std::string	            name;		    /// The name of this datatype
	IDatatype*	            datatype;	    /// The size of this datatype
	std::list<Enumerator*> enumerators;    /// The collection of discriminant enumerators that this type is valid for
	
public:

	/**
	* Constructor for an Alternative with specified name, datatype and enumerator collection.
	*
	* @param name The name of the alternative
	* @param datatype The datatype that the alternative will store
	* @param enumerators The collection of discriminant enumerators that this type is valid for
	*/
	Alternative(const std::string& name, IDatatype* datatype, std::list<Enumerator*> enumerators);
 
	virtual ~Alternative();


	/////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Alternative Interface //////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////

	/**
	* Get the IDatatype associated with this array. 
	* @return The IDatatype associated with this ArrayType.
	* @see IDatatype
	*/
	virtual IDatatype* getDatatype();
 
	/**
	* Get the list of Enumerator objects associated with this Alternative object.
	* @return A set of Enumerators associated with this Alternative.
	* @see Enumerator
	*/
	virtual std::list<Enumerator*>& getEnumerators();
 
	/**
	* Returns the name of this datatype.
	* @return The name of this datatype as a string.
	*/
	virtual std::string getName() const ;

};
