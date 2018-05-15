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

#include "RTI/SpecificConfig.h"

namespace portico1516e
{
	/**
	 * Describes a dimension of an {@link ArrayType}.
	 *
	 * @see ArrayType
	 */
	class RTI_EXPORT Dimension
	{
		//----------------------------------------------------------
		//                    STATIC VARIABLES
		//----------------------------------------------------------
		public:
			/// Represents a dynamic dimension cardinality
			const static int CARDINALITY_DYNAMIC = -1;

		//----------------------------------------------------------
		//                   INSTANCE VARIABLES
		//----------------------------------------------------------
		private:
			int lowerCardinality; /// The lower bound of a ranged dimension
			int upperCardinality; /// The upper bounds of a ranged dimension

		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
		public:	
			/**
			 * Create a dimension with the specified cardinality.
			 *
			 * @param cardinality the size of this dimension, or 
			 *        {@link CARDINALITY_DYNAMIC} if the dimension should have dynamic 
			 *        cardinality
			 */
			Dimension( int cardinality );

			/**
			 * Create a dimension with the specified cardinality range.
			 *
			 * @param lower the bound of this dimension's cardinality
			 * @param upper the upper bound of this dimension's cardinality
			 */
			Dimension( int lower, int upper );

			virtual ~Dimension();

		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
		public:
			/**
			 * @return the lower bound of this dimension's cardinality.
			 */
			int getCardinalityLowerBound() const;

			/**
			 * @return the upper bound of this dimension's cardinality.
			 */
			int getCardinalityUpperBound() const;

			/**
			 * @return <code>true</code> if the dimension has dynamic cardinality, otherwise 
			 *         <code>false</code>
			 */
			bool isCardinalityDynamic() const;	
		 
		//----------------------------------------------------------
		//                     STATIC METHODS
		//----------------------------------------------------------
	};
}
