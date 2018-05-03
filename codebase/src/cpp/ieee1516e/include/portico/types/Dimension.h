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
* Describes a dimension of an ArrayType.
* @see ArrayType
*/
class Dimension {
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public:
		/// The cardinality of the dynamic dimension type
		const static int CARDINALITY_DYNAMIC = -1;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		int lowerCardinality; /// The lower bound of a ranged simension
		int upperCardinality; /// The upper bounds of a ranged dimension

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
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

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		/**
		 * Get the lower bounds of a range cardinaliy.
		 * @return the lower bound or the size of this dimension.
		 */
		virtual int getCardinalityLowerBound() const;

		/**
		 * Get the upper bounds of a range cardinaliy.
		 * @return the lower bound or the size of this dimension.
		 */
		virtual int getCardinalityUpperBound() const;

		/**
		 * Check to see if this dimension is dynamic.
		 * @return True if the dimension is dynamic, otherwise false.
		 */
		virtual bool isCardinalityDynamic() const;	
		 
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:

};
