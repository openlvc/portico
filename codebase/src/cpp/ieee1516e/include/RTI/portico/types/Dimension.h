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
File: RTI/portico/types/Dimension.h
***********************************************************************/
#pragma once

/**
* Describes a dimension of an ArrayType.
* @see ArrayType
*/
class Dimension {
public:
	/// The cardinality of the dynamic dimension type
    const static int CARDINALITY_DYNAMIC = -1; 

private:
    int lowerCardinality; /// The lower bound of a ranged simension
    int upperCardinality; /// The upper bounds of a ranged dimension

public:

	/**
	* Create a dimension with the specified cardinaliy. 
	* @param cardinality the size of this dimension.
	* @note If the size is -1 this represents a dynamic dimension
	*/
    Dimension(int cardinality);

	/**
	* Create a dimension with the specified cardinaliy range.
	* @param cardinality the size of this dimension.
	* @note If the size is -1 this represents a dynamic dimension
	*/
    Dimension(int lower, int upper);

    virtual ~Dimension();

	/**
	* Get the lower bounds of a range cardinaliy.
	* @return the lower bound or the size of this dimension.
	*/
    virtual int getCardinalityLowerBound();

	/**
	* Get the upper bounds of a range cardinaliy.
	* @return the lower bound or the size of this dimension.
	*/
    virtual int getCardinalityUpperBound();

	/**
	* Check to see if this dimension is dynamic.
	* @return True if the dimension is dynamic, otherwise false.
	*/
    virtual bool isCardinalityDynamic();

};