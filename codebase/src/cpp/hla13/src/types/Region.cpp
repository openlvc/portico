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
#include "Region.h"

#ifdef BUILDING_DLC
HLA::ULong rti13::Region::getMaxExtent() throw ()
{
	return 0;
}

HLA::ULong rti13::Region::getMinExtent() throw ()
{
	return 0;
}
#endif

PORTICO13_NS_START

//----------------------------------------------------------
//                      CONSTRUCTORS
//----------------------------------------------------------
Region::Region( portico13::JavaRTI *rti, jobject regionProxy )
{
	this->rti = rti;
	// create a global reference for the region proxy so that we can keep a hold of it
	this->regionProxy = rti->getJniEnvironment()->NewGlobalRef( regionProxy );
}

Region::~Region()
{
	// release the global reference to the proxy
	rti->getJniEnvironment()->DeleteGlobalRef( this->regionProxy );
}

//----------------------------------------------------------
//                 PRIVATE INSTANCE METHODS
//----------------------------------------------------------

//----------------------------------------------------------
//                 PUBLIC INSTANCE METHODS
//----------------------------------------------------------

jobject Region::getRegionProxy()
{
	return this->regionProxy;
}


HLA::ULong Region::getRangeLowerBound( HLA::ExtentIndex theExtent,
                                       HLA::DimensionHandle theDimension ) const
	throw( HLA::ArrayIndexOutOfBounds )
{
	// call the method
	jlong retval = rti->getJniEnvironment()->CallLongMethod( regionProxy,
	                                                         rti->REGION_GET_RANGE_LOWER_BOUND,
	                                                         theExtent,
	                                                         theDimension );

	// clean up and run the exception check
	rti->exceptionCheck();
	return retval;
}

HLA::ULong Region::getRangeUpperBound( HLA::ExtentIndex theExtent,
                                       HLA::DimensionHandle theDimension )
	const throw( HLA::ArrayIndexOutOfBounds )
{
	// call the method
	jlong retval = rti->getJniEnvironment()->CallLongMethod( regionProxy,
	                                                         rti->REGION_GET_RANGE_UPPER_BOUND,
	                                                         theExtent,
	                                                         theDimension );

	// clean up and run the exception check
	rti->exceptionCheck();
	return retval;
}

void Region::setRangeLowerBound( HLA::ExtentIndex extent,
                                 HLA::DimensionHandle dimension,
                                 HLA::ULong bound )
	throw( HLA::ArrayIndexOutOfBounds )
{
	// call the method
	rti->getJniEnvironment()->CallVoidMethod( regionProxy,
	                                          rti->REGION_SET_RANGE_LOWER_BOUND,
	                                          extent,
	                                          dimension,
	                                          (jlong)bound );

	// clean up and run the exception check
	rti->exceptionCheck();
}

void Region::setRangeUpperBound( HLA::ExtentIndex extent,
                                 HLA::DimensionHandle dimension,
                                 HLA::ULong bound )
	throw( HLA::ArrayIndexOutOfBounds )
{
	// call the method
	rti->getJniEnvironment()->CallVoidMethod( regionProxy,
	                                          rti->REGION_SET_RANGE_UPPER_BOUND,
	                                          extent,
	                                          dimension,
	                                          (jlong)bound );

	// clean up and run the exception check
	rti->exceptionCheck();
}

HLA::SpaceHandle Region::getSpaceHandle() const throw ()
{
	// call the method
	jint retval = rti->getJniEnvironment()->CallIntMethod( regionProxy, rti->REGION_GET_SPACE_HANDLE );

	// clean up and run the exception check
	rti->exceptionCheck();
	return retval;
}

HLA::ULong Region::getNumberOfExtents() const throw ()
{
	// call the method
	jlong retval = rti->getJniEnvironment()->CallLongMethod( regionProxy, rti->REGION_GET_NUMBER_OF_EXTENTS );

	// clean up and run the exception check
	rti->exceptionCheck();
	return retval;
}

HLA::ULong Region::getRangeLowerBoundNotificationLimit( HLA::ExtentIndex theExtent,
                                                        HLA::DimensionHandle theDimension )
	const throw( HLA::ArrayIndexOutOfBounds )
{
	// call the method
	jlong retval = rti->getJniEnvironment()->CallLongMethod( regionProxy,
	                                                         rti->REGION_GET_RANGE_LOWER_BOUND_NOTIFICATION_LIMIT,
	                                                         theExtent,
	                                                         theDimension );

	// clean up and run the exception check
	rti->exceptionCheck();
	return retval;
}

HLA::ULong Region::getRangeUpperBoundNotificationLimit( HLA::ExtentIndex theExtent,
                                                        HLA::DimensionHandle theDimension )
	const throw( HLA::ArrayIndexOutOfBounds )
{
	// call the method
	jlong retval = rti->getJniEnvironment()->CallLongMethod( regionProxy,
	                                                         rti->REGION_GET_RANGE_UPPER_BOUND_NOTIFICATION_LIMIT,
	                                                         theExtent,
	                                                         theDimension );

	// clean up and run the exception check
	rti->exceptionCheck();
	return retval;
}

PORTICO13_NS_END

