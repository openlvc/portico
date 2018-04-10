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
File: RTI/portico/types/ArrayType.h
***********************************************************************/

#pragma once

#include "RTI/portico/IDatatype.h"  
#include "RTI/portico/types/Dimension.h"  
#include <list> 

/**
* This class contains metadata about a FOM Array data type.
* <p/>
* An array data type is a homogenous collection of a specified data type. Array data types may
* be single or multi-dimensional, and each dimension may have a fixed or dynamic cardinality.
*/
class ArrayType : public virtual IDatatype
{
protected:
	 
	std::string	            name;			/// The name of this datatype
	IDatatype*	            datatype;		/// The size of this datatype
    std::list<Dimension> dimensions;
	
public:

    /**
    * Constructor for a single dimension array type with Dynamic cardinality
    *
    * @param name the name of the array type
    * @param datatype the type of data that will be stored in instances of this array
    */
    ArrayType(const std::string& name, IDatatype* datatype);
     
 
    /**
    * Constructor for a single dimension array type with a specified cardinality value
    *
    * @param name the name of the array type
    * @param datatype the type of data that will be stored in instances of this array
    * @param cardinality the cardinality of this array type
    */
    ArrayType(const std::string& name, IDatatype *datatype, int cardinality);
     
    /**
    * Constructor for an array type with an arbitrary number of dimensions
    * <p/>
    * <b>Note:</b> at least one dimension must be supplied.
    *
    * @param name the name of the array type
    * @param datatype the type of data that will be stored in instances of this array
    * @param dimensions the dimensions this array type will contain
    */
    ArrayType(const std::string& name, IDatatype *datatype, const std::list<Dimension>& dimensions);

	virtual ~ArrayType();

    /////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// ArrayType Interface //////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    /**
    * Get the IDatatype associated with this array. 
    * @return The IDatatype associated with this ArrayType.
    * @see IDatatype
    */
    virtual IDatatype* getDatatype();
 
    /**
    * Get the Demensions associated with this array object.
    * @param datatype The IDatatype associated with this ArrayType.
    * @see IDatatype
    */
    virtual std::list<Dimension>& getDimensions();

    /**
    * Get the cardinality lowest bounds of the first element.
    * Each ArrayType must have atleast one Dimension.
    * 
    * @return the lower bounds of the first dimension for this ArrayType
    */
    virtual int getCardinalityLowerBound();
   
    /**
    * Get the cardinality upper bounds of the first element.
    * Each ArrayType must have atleast one Dimension.
    *
    * @return the upper bounds of the first dimension for this ArrayType
    */
    virtual int getCardinalityUpperBound();
    
    /**
    * Check to see if the first cardinality is dynamic.
    * Each ArrayType must have atleast one Dimension.
    *
    * @return True if the first cardinality is dynamic, otherwise false.
    */
    virtual bool isCardinalityDynamic();

    /**
    * Get the dimension for cardinality of a particular size. 
    *
    * @return A dynamic Dimension or a Dimension of the specified cardinality.
    */
    //virtual Dimension getDimensionFor(int cardinality);

	/////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Datatype Interface ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	/**
	* Returns the name of this datatype.
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
