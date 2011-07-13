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
import java.util.HashMap;
import java.util.Map;

import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JArrayIndexOutOfBounds;

/**
 * <b>All the below relates to HLA 1.3</b>
 * <p/>
 * {@link Extent} is a construct used for DDM. Extents are created and populated at runtime
 * (much like ACInstances) and are not metadata/fom data (like ACMetadatas).
 * <p/>
 * The DDM constructs for Portico are defined as follows:
 * <p/>A <b>{@link Space}</b> is a metadata type that is defined within the FOM
 * <p/>Each {@link Space} defines a number of <b>{@link Dimension}s</b> that it consists of.
 * (the Dimension class is also a metadata class).
 * <p/>A <b>{@link RegionInstance}</b> is defined at runtime. Each Region is associated with a
 * SINGLE {@link Space}
 * <p/>A {@link RegionInstance} contains a number of <b>{@link Extent}s</b>. Each {@link Extent}
 * defines a bound (a minimum and maximum value) for each {@link Dimension} in the {@link Space}
 * associated with the {@link RegionInstance} it is contained in.
 * <p/>
 * Confused yet? Yeah, I was as well. Wait until you deal with 1516.
 */
public class Extent implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<Integer,Range> ranges;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public Extent( Space space )
	{
		this.ranges = new HashMap<Integer,Range>();
		
		// populate the extent with range information from the space
		for( Dimension dimension : space.getDimensions() )
		{
			this.ranges.put( dimension.getHandle(), new Range() );
		}
	}
	
	/**
	 * This constructor is only for use during the cloning process in {@link #clone()}.
	 * It doesn't require the {@link Space} as it doesn't need to create its ranges for
	 * the set of dimensions. Rather, the cloning process will copy the existing set.
	 */
	private Extent()
	{
		this.ranges = new HashMap<Integer,Range>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * This method will create a full copy of the current {@link Extent} and return it. It is
	 * used by the {@link RegionInstance#clone()} method.
	 */
	protected Extent clone()
	{
		// create the new extent
		Extent newExtent = new Extent();

		for( int key : this.ranges.keySet() )
		{
			// get the current range
			Range currentRange = this.ranges.get( key );
			
			// create a new range and store it in the extent
			Range newRange = new Range();
			newRange.lowerBound = currentRange.lowerBound;
			newRange.upperBound = currentRange.upperBound;
			newExtent.ranges.put( key, newRange );
		}
		
		// return the cloned instance
		return newExtent;
	}

	/**
	 * Returns <code>true</code> if the ranges for all dimensions in this {@link Extent} overlap
	 * with the ranges for all the dimensions in the other {@link Extent}.
	 */
	public boolean overlapsWith( Extent otherExtent )
	{
		// as each region that these extents reside in should be associated with the same
		// space, we can assume that the number of dimensions is the same (thus, there is
		// no need to check for a lack of overlap due to a value for a particular dimension
		// being in one extent and not in the other)
		
		// check the ranges for each dimension
		for( Integer dimensionHandle : ranges.keySet() )
		{
			Range ourRange = ranges.get( dimensionHandle );
			Range theirRange = otherExtent.ranges.get( dimensionHandle );

			// To see if there is an overlap between the ranges, we
			// need to consider three calculations:
			//
			// -lb(ours) == lb(theirs) == OVERLAP!!!
			// -lb(ours) >= ub(theirs) == NO OVERLAP
			// -ub(ours) <= lb(theirs) == NO OVERLAP
			//
			// If both of the last two calculations above are true, there is no overlap.
			// We use <= and >= because the spec says the upperBound is non-inclusive
			if( ourRange.lowerBound == theirRange.lowerBound )
				continue;
			
			if( ourRange.lowerBound >= theirRange.upperBound ||
				ourRange.upperBound <= theirRange.lowerBound )
			{
				return false;
			}
		}
		
		// if we get here it means that the ranges for each dimension overlap, return true
		return true;
	}

	////////////////////////////////////////////////////////////
	////////////////// Metadata Based Methods //////////////////
	////////////////////////////////////////////////////////////
	/**
	 * Returns the {@link Range} contained within this {@link Extent} for the given
	 * {@link Dimension}. If none exists, null is returned.
	 */
	public Range getRange( Dimension dimension )
	{
		return this.ranges.get( dimension.getHandle() );
	}

	/**
	 * Sets the {@link Range} for the given {@link Dimension} in this {@link Extent}. If the
	 * given {@link Dimension} or {@link Range} is null, no action is taken and nothing will
	 * be added.
	 */
	public void setRange( Dimension dimension, Range range )
	{
		// check for dodgy values
		if( dimension == null || range == null )
			return;
		
		this.ranges.put( dimension.getHandle(), range );
	}

	////////////////////////////////////////////////////////////
	/////////////////// Handle Based Methods ///////////////////
	////////////////////////////////////////////////////////////
	/**
	 * Return the map containing all the range values for this extent. The key is the
	 * dimension handle the range is associated with.
	 */
	public Map<Integer,Range> getAllRanges()
	{
		return this.ranges;
	}

	/**
	 * The 1.3 spec says that if there is no dimension in the extent for a requested action, an
	 * ArrayIndexOutOfBounds exception should be thrown. This method will do the appropriate check
	 * and throw an exception if that is the case.
	 */
	private void checkDimension( int dimension ) throws JArrayIndexOutOfBounds
	{
		// spec says that if the dimension is not valid, we have to throw the exception
		if( ranges.containsKey(dimension) == false )
		{
			throw new JArrayIndexOutOfBounds( "dimension [" + dimension + "] not found in extent" );
		}
	}
	
	/**
	 * Returns the {@link Range} contained within this {@link Extent} for the given
	 * dimension handle. If none exists, a {@link JArrayIndexOutOfBounds} is thrown.
	 */
	public Range getRange( int dimensionHandle ) throws JArrayIndexOutOfBounds
	{
		checkDimension( dimensionHandle );
		return this.ranges.get( dimensionHandle );
	}

	/**
	 * Sets the {@link Range} for the given dimension handle in this {@link Extent}. If the
	 * given {@link Range} is null, no action is taken and nothing will be added. If the dimension
	 * handle does not represent a dimension in the extent, a {@link JArrayIndexOutOfBounds} is
	 * thrown.
	 */
	public void setRange( int dimensionHandle, Range range ) throws JArrayIndexOutOfBounds
	{
		// check for a dodgy range
		if( range == null )
			return;

		checkDimension( dimensionHandle );
		this.ranges.put( dimensionHandle, range );
	}

	/**
	 * Fetches the upper bound for the range associated with the given dimension in this extent
	 * and returns it. If the given dimension handle does not exist in this extent,
	 * a {@link JArrayIndexOutOfBounds} will be thrown.
	 */
	public long getRangeUpperBound( int dimensionHandle ) throws JArrayIndexOutOfBounds
	{
		checkDimension( dimensionHandle );
		return ranges.get(dimensionHandle).upperBound;
	}

	/**
	 * Fetches the lower bound for the range associated with the given dimension in this extent
	 * and returns it. If the given dimension handle does not exist in this extent,
	 * a {@link JArrayIndexOutOfBounds} will be thrown.
	 */
	public long getRangeLowerBound( int dimensionHandle ) throws JArrayIndexOutOfBounds
	{
		checkDimension( dimensionHandle );
		return ranges.get(dimensionHandle).lowerBound;
	}

	/**
	 * Sets the upper bound for the range associated with the given dimension in this extent.
	 * If the dimension does not exist, a {@link JArrayIndexOutOfBounds} will be thrown.
	 * There is no checking of the bound value, you can set an invalid value if you wish.
	 */
	public void setRangeUpperBound( int dimensionHandle, long upperBound )
		throws JArrayIndexOutOfBounds
	{
		checkDimension( dimensionHandle );
		ranges.get(dimensionHandle).upperBound = upperBound;
	}

	/**
	 * Sets the lower bound for the range associated with the given dimension in this extent.
	 * If the dimension does not exist, a {@link JArrayIndexOutOfBounds} will be thrown.
	 * There is no checking of the bound value, you can set an invalid value if you wish.
	 */
	public void setRangeLowerBound( int dimensionHandle, long lowerBound )
		throws JArrayIndexOutOfBounds
	{
		checkDimension( dimensionHandle );
		ranges.get(dimensionHandle).lowerBound = lowerBound;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

	//////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Inner Class: Range ///////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * A Range is a structure that contains an upper and lower bound for a specific
	 * {@link Dimension}. Multiple instances of these can be contained within a single
	 * {@link Extent}.
	 */
	public class Range implements Serializable
	{
		private static final long serialVersionUID = 98121116105109L;

		public long lowerBound = PorticoConstants.MIN_EXTENT;
		public long upperBound = PorticoConstants.MAX_EXTENT;
	}
}
