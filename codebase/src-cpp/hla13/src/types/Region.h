/*
 *   Copyright 2009 The Portico Project
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
#ifndef REGION_IMPL_H
#define REGION_IMPL_H

#include "common.h"
#include "jni/JavaRTI.h"

PORTICO13_NS_START

class Region : public HLA::Region
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		JavaRTI *rti;
		jobject regionProxy;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		Region( JavaRTI *rti, jobject regionProxy );
		virtual ~Region();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		/////////////////////////////////////////////////////////////////////////////
		////////////////////// Implementation Specific Methods ////////////////////// 
		/////////////////////////////////////////////////////////////////////////////
		jobject getRegionProxy();

		/////////////////////////////////////////////////////////////////////////////
		///////////////////////////// HLA Spec Methods //////////////////////////////
		/////////////////////////////////////////////////////////////////////////////
		virtual HLA::ULong getRangeLowerBound( HLA::ExtentIndex theExtent,
											   HLA::DimensionHandle theDimension ) const
			throw( HLA::ArrayIndexOutOfBounds );

		virtual HLA::ULong getRangeUpperBound( HLA::ExtentIndex theExtent,
											   HLA::DimensionHandle theDimension ) const
			throw( HLA::ArrayIndexOutOfBounds );
		
		virtual void setRangeLowerBound( HLA::ExtentIndex theExtent,
										 HLA::DimensionHandle theDimension,
										 HLA::ULong theLowerBound )
			throw( HLA::ArrayIndexOutOfBounds );
		
		virtual void setRangeUpperBound( HLA::ExtentIndex theExtent,
										 HLA::DimensionHandle theDimension,
										 HLA::ULong theUpperBound )
			throw( HLA::ArrayIndexOutOfBounds );
		
		virtual HLA::SpaceHandle getSpaceHandle() const throw ();
		
		virtual HLA::ULong getNumberOfExtents() const throw ();
		
		virtual HLA::ULong
				getRangeLowerBoundNotificationLimit( HLA::ExtentIndex theExtent,
													 HLA::DimensionHandle theDimension ) const
			throw( HLA::ArrayIndexOutOfBounds );
		
		virtual HLA::ULong
				getRangeUpperBoundNotificationLimit( HLA::ExtentIndex theExtent,
													 HLA::DimensionHandle theDimension ) const
			throw( HLA::ArrayIndexOutOfBounds );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:
		static HLA::ULong getMaxExtent() throw ();
		static HLA::ULong getMinExtent() throw ();

};

PORTICO13_NS_END

#endif

