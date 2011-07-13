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
package org.portico.lrc.model;

import java.io.Serializable;
import java.util.Map;

import org.portico.lrc.compat.JArrayIndexOutOfBounds;

/**
 * This class represents a <b>HLA 1.3</b> Region.
 * <p/>
 * A {@link RegionInstance} contains a number of {@link Extent}s, where each {@link Extent} contains
 * a set of upper and lower bounds for a number of {@link Dimension}s. {@link Extent}s are contained
 * within the {@link RegionInstance} in an array, so you must specify the size on construction.
 * A {@link RegionInstance} is also linked to a single routing {@link Space}.
 */
public class RegionInstance implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int spaceHandle;
	private int regionToken;
	private Extent[] extents;
	private int federateHandle;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create a new region instance from the given space.
	 * 
	 * @param federateHandle the handle of the federate that created the region
	 * @param regionToken    the token (id) given to this region instance
	 * @param space          the routing space this region refers to
	 * @param extentCount    the number of extents this region is to have
	 */
	public RegionInstance( int federateHandle, int regionToken, Space space, int extentCount )
	{
		// store the handle of the creating federate
		this.federateHandle = federateHandle;
		this.regionToken = regionToken;

		// store the given information
		this.spaceHandle = space.getHandle();
		this.extents = new Extent[extentCount];
		
		// populate the region with empty extents
		for( int i = 0; i < extentCount; i++ )
			this.extents[i] = new Extent( space );
	}

	/**
	 * This constructor is used in the {@link #clone()} method only. It doesn't require a
	 * {@link Space} as it doesn't need to build the entire instance from scratch.
	 */
	private RegionInstance( int regionToken, int federateHandle, int spaceHandle, int extentCount )
	{
		this.regionToken    = regionToken;
		this.federateHandle = federateHandle;
		this.spaceHandle    = spaceHandle;
		this.extents        = new Extent[extentCount];
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * This method will do a deep-copy of the current {@link RegionInstance}, returning the
	 * new instance such that it has all the values that the original (current) instance has.
	 */
	public RegionInstance clone()
	{
		// create the new instance
		RegionInstance newInstance = new RegionInstance( this.regionToken,
		                                                 this.federateHandle,
		                                                 this.spaceHandle,
		                                                 this.extents.length );
		
		// create copies of the extents
		for( int i = 0; i < extents.length; i++ )
		{
			newInstance.extents[i] = this.extents[i].clone();
		}

		// return the cloned instance
		return newInstance;
	}

	/**
	 * Remove any current state and copy all the values from the given {@link RegionInstance}
	 * to this one. Note that this is a <b>semi-clone</b> in that it will create new instances
	 * of all the contained {@link Extent}s, rather than just using the same references that
	 * exist in the other region. This way, changes to the other region will not affect this
	 * region.
	 */
	public void copy( RegionInstance other )
	{
		this.regionToken = other.regionToken;
		this.federateHandle = other.federateHandle;
		this.spaceHandle = other.spaceHandle;
		this.extents = new Extent[other.extents.length];
		
		// we can clone the extents to create new versions
		// no internal component should hold a reference to the Extents directly,
		// so there is no danger in creating new instances only to have something
		// holding on to the old ones
		for( int i = 0; i < extents.length; i++ )
			this.extents[i] = other.extents[i].clone();
	}

	/**
	 * Returns <code>true</code> if the parameter is an instance of {@link RegionInstance} and
	 * it has the same region token as the current instance, <code>false</code> otherwise. Note
	 * that only the token is checked. The values contained in the two instances could be totally
	 * different (if one had been changed locally for example).
	 */
	public boolean equals( Object other )
	{
		try
		{
			return ((RegionInstance)other).regionToken == this.regionToken;
		}
		catch( Exception e )
		{
			return false;
		}
	}

	/**
	 * Checks the provided index to make sure it is valid for this Region (greater than 0 and
	 * less than the number of contained extents-1). If the index is not OK, an exception will
	 * be thrown.
	 */
	private void checkIndex( int index ) throws JArrayIndexOutOfBounds
	{
		if( index >= extents.length || index < 0 )
		{
			throw new JArrayIndexOutOfBounds( "Index " + index + " beyond extent count of " +
			                                  extents.length );
		}
	}
	
	/**
	 * Get the extent at the given index. If the index is below 0 or greater than the
	 * number of extents in this region, an exception will be thrown.
	 */
	public Extent getExtent( int index ) throws JArrayIndexOutOfBounds
	{
		checkIndex( index );
		return extents[index];
	}

	/**
	 * Get the number of extents stored in this region
	 */
	public int getSize()
	{
		return this.extents.length;
	}

	/**
	 * Set the extent at the given index. If the index is below 0 or greater than the
	 * number of extents in this region, an exception will be thrown. If the extent is
	 * null, the call will be ignored and no addition will be made.
	 * <p/>
	 * If an {@link Extent} already exists at the given index, it will be overwritten.
	 */
	public void setExtent( Extent extent, int index ) throws JArrayIndexOutOfBounds
	{
		// check for a dodgy extent
		if( extent == null )
			return;
		
		checkIndex( index );
		extents[index] = extent;
	}

	/**
	 * Get the unique handle for this region instance
	 */
	public int getToken()
	{
		return this.regionToken;
	}

	/**
	 * Set the unique handle for this region instance
	 */
	public void setToken( int regionHandle )
	{
		this.regionToken = regionHandle;
	}

	/**
	 * Returns the handle of the federate that created this region
	 */
	public int getFederateHandle()
	{
		return this.federateHandle;
	}
	
	/**
	 * This method will return <code>true</code> if the given region overlaps with the other
	 * region, <code>false</code> otherwise. This method will compare each of the {@link Extent}s
	 * in this region, with each of the {@link Extent}s in the other region. If *any* extent from
	 * this region overlaps with one from the other region, then <code>true</code> will be
	 * returned.
	 */
	public boolean overlapsWith( RegionInstance other )
	{
		// make sure the regions refer to the same space
		if( this.spaceHandle != other.spaceHandle )
			return false;
		
		// compare each extent in this region to the extent in the other region
		for( Extent ourCurrentExtent : extents )
		{
			for( Extent theirCurrentExtent : other.extents )
			{
				if( ourCurrentExtent.overlapsWith(theirCurrentExtent) )
					return true;
			}
		}
		
		// if there was an overlap, we would never get here
		return false;
	}

	public String toString()
	{
		//StringBuilder builder = new StringBuilder( "Region(token=" );
		//builder.append( regionToken );
		//builder.append( ", space=" );
		//builder.append( spaceHandle );
		//builder.append( ", extents=" );
		//builder.append( extents.length );
		//builder.append( ")" );

		return String.format( "Region{token=%d,space=%d}", regionToken, spaceHandle ); 
	}
	
	/**
	 * Generates a verbose string outlining the current state of the region. This string include
	 * full extent data, so it could be quite large.
	 */
	public String toVerboseString()
	{
		StringBuilder builder = new StringBuilder( "Region{token=" );
		builder.append( regionToken );
		builder.append( ",space=" );
		builder.append( spaceHandle );
		builder.append( "}" );

		for( int extentIndex = 0; extentIndex < extents.length; extentIndex++ )
		{
			Map<Integer,Extent.Range> ranges = extents[extentIndex].getAllRanges();
			for( Integer dimensionHandle : ranges.keySet() )
			{
				builder.append( "\n\t[e:" );
				builder.append( extentIndex );
				builder.append( "][dimension:" );
				builder.append( dimensionHandle );
				builder.append( "] lower=" );
				Extent.Range currentRange = ranges.get( dimensionHandle );
				builder.append( currentRange.lowerBound );
				builder.append( ", upper=" );
				builder.append( currentRange.upperBound );
			}
		}

		return builder.toString();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Spec Related Methods //////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// These methods generally relate to the HLA spec methods //
	// so it should be possible to just call them by the same //
	// name used in the spec. People will have to catch the   //
	// exception and rethrow it in the appropriate form.      //
	////////////////////////////////////////////////////////////

	/**
	 * @return long Number of extents in this Region
	 */
	public long getNumberOfExtents()
	{
		return this.extents.length;
	}

	/**
	 * @return long Lower bound of extent along indicated dimension
	 */
	public long getRangeLowerBound( int index, int dimension ) throws JArrayIndexOutOfBounds
	{
		checkIndex( index );
		return extents[index].getRangeLowerBound( dimension );
	}

	/**
	 * @return long Upper bound of extent along indicated dimension
	 */
	public long getRangeUpperBound( int index, int dimension ) throws JArrayIndexOutOfBounds
	{
		checkIndex( index );
		return extents[index].getRangeUpperBound( dimension );
	}

	/**
	 * @return int Handle of routing space of which this Region is a subset
	 */
	public int getSpaceHandle()
	{
		return this.spaceHandle;
	}

	/**
	 * Modify lower bound of extent along indicated dimension.
	 */
	public void setRangeLowerBound( int index, int dimension, long bound )
		throws JArrayIndexOutOfBounds
	{
		checkIndex( index );
		extents[index].setRangeLowerBound( dimension, bound );
	}

	/**
	 * Modify upper bound of extent along indicated dimension.
	 */
	public void setRangeUpperBound( int index, int dimension, long bound )
		throws JArrayIndexOutOfBounds
	{
		checkIndex( index );
		extents[index].setRangeUpperBound( dimension, bound );
	}
	
	/**
	 * For now, this just returns the exact same value as {@link #getRangeLowerBound(int, int)}
	 */
	public long getRangeLowerBoundNotificationLimit( int extentHandle, int dimensionHandle )
		throws JArrayIndexOutOfBounds
	{
		return getRangeLowerBound( extentHandle, dimensionHandle );
	}
	
	/**
	 * For now, this just returns the exact same value as {@link #getRangeUpperBound(int, int)}
	 */
	public long getRangeUpperBoundNotificationLimit( int extentHandle, int dimensionHandle )
		throws JArrayIndexOutOfBounds
	{
		return getRangeUpperBound( extentHandle, dimensionHandle );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
