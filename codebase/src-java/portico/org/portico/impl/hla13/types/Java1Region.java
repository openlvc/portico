/*
 *   Copyright 2007 The Portico Project
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
package org.portico.impl.hla13.types;

import org.portico.lrc.compat.JArrayIndexOutOfBounds;
import org.portico.lrc.model.RegionInstance;

import hla.rti13.java1.Region;
import hla.rti13.java1.ArrayIndexOutOfBounds;
import hla.rti13.java1.RTIinternalError;

/**
 * This class represents a <code>hla.rti13.java1.Region</code>. Unlike with the HLA 1.3 spec where
 * the Region type is an interface, in the java1 package, it is a concrete class. To avoid putting
 * Portico specific code into that class, this class just extends it. This class wraps around an
 * instance of {@link RegionInstance} (from the Portico object model types) and passes all requests
 * on through to it. 
 */
public class Java1Region extends hla.rti13.java1.Region
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RegionInstance region;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public Java1Region( RegionInstance region )
	{
		// check the region
		if( region == null )
			throw new IllegalArgumentException( "Can't pass null region to constructor" );

		this.region = region;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * Return the wrapped Portico region insance
	 */
	public RegionInstance getWrappedRegion()
	{
		return region;
	}

	/**
	 * Gets the region handle for the wrapped region
	 */
	public int getRegionHandle()
	{
		return region.getToken();
	}
	
	public boolean equals( Object otherRegion )
	{
		try
		{
			Java1Region other = (Java1Region)otherRegion;
			return this.region.equals( other.region );
		}
		catch( ClassCastException cce )
		{
			return false;
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// HLA Interface Methods /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return long Number of extents in this Region
	 */
	@Override
	public long getNumberOfExtents()
	{
		return region.getNumberOfExtents();
	}

	/**
	 * @return long Lower bound of extent along indicated dimension
	 */
	@Override
	public long getRangeLowerBound( int extentIndex, int dimensionHandle )
		throws ArrayIndexOutOfBounds
	{
		try
		{
			return region.getRangeLowerBound( extentIndex, dimensionHandle );
		}
		catch( JArrayIndexOutOfBounds oob )
		{
			throw new ArrayIndexOutOfBounds( oob );
		}
	}

	/**
	 * @return long Upper bound of extent along indicated dimension
	 */
	@Override
	public long getRangeUpperBound( int extentIndex, int dimensionHandle )
		throws ArrayIndexOutOfBounds
	{
		try
		{
			return region.getRangeUpperBound( extentIndex, dimensionHandle );
		}
		catch( JArrayIndexOutOfBounds oob )
		{
			throw new ArrayIndexOutOfBounds( oob );
		}
	}

	/**
	 * @return int Handle of routing space of which this Region is a subset
	 */
	@Override
	public int getSpaceHandle()
	{
		return region.getSpaceHandle();
	}

	/**
	 * Modify lower bound of extent along indicated dimension.
	 */
	@Override
	public void setRangeLowerBound( int extentIndex, int dimensionHandle, long newLowerBound )
		throws ArrayIndexOutOfBounds
	{
		try
		{
			region.setRangeLowerBound( extentIndex, dimensionHandle, newLowerBound );
		}
		catch( JArrayIndexOutOfBounds oob )
		{
			throw new ArrayIndexOutOfBounds( oob );
		}
	}

	/**
	 * Modify upper bound of extent along indicated dimension.
	 */
	@Override
	public void setRangeUpperBound( int extentIndex, int dimensionHandle, long newUpperBound )
		throws ArrayIndexOutOfBounds
	{
		try
		{
			region.setRangeUpperBound( extentIndex, dimensionHandle, newUpperBound );
		}
		catch( JArrayIndexOutOfBounds oob )
		{
			throw new ArrayIndexOutOfBounds( oob );
		}
	}

	@Override
	public long getRangeUpperBoundNotificationLimit( int theExtent, int theDimension )
		throws ArrayIndexOutOfBounds
	{
		try
		{
			return region.getRangeUpperBoundNotificationLimit( theExtent, theDimension );
		}
		catch( JArrayIndexOutOfBounds oob )
		{
			throw new ArrayIndexOutOfBounds( oob );
		}
	}

	@Override
	public long getRangeLowerBoundNotificationLimit( int theExtent, int theDimension )
		throws ArrayIndexOutOfBounds
	{
		try
		{
			return region.getRangeLowerBoundNotificationLimit( theExtent, theDimension );
		}
		catch( JArrayIndexOutOfBounds oob )
		{
			throw new ArrayIndexOutOfBounds( oob );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * This method will take a region as the HLA1.3(java1) type and attempt to convert and extract
	 * from it the wrapped {@link RegionInstance}. If there is a problem during this conversion
	 * (such as the method not being passed an instance of {@link Java1Region}), an exception
	 * will be thrown.
	 */
	public static RegionInstance toPorticoRegion( Region region ) throws RTIinternalError
	{
		// convert the region and extract the underlying wrapped instance
		try
		{
			return ((Java1Region)region).region;
		}
		catch( ClassCastException cce )
		{
			throw new RTIinternalError( "Region not of appropriate type (expecting Java1Region)" );
		}
		catch( Exception e )
		{
			throw new RTIinternalError( "Problem converting Region: " + e.getMessage(), e );
		}
	}
}
