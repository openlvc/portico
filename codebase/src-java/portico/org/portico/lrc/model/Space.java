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

import org.portico.lrc.PorticoConstants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Space implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String                         name;
	private int                            handle;
	private Map<Integer,Dimension> dimensions;
	private ObjectModel                    model;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public Space( String name, int handle )
	{
		this.name = name;
		this.handle = handle;
		this.dimensions = new HashMap<Integer,Dimension>();
		this.model = null;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////
	///////////////////// Attribute Methods ////////////////////
	////////////////////////////////////////////////////////////
	/**
	 * Adds the given dimension to this space. If the name of the dimension is the same
	 * as one already in this class, the request will be ignored and false will be returned.
	 * Otherwise, the dimension will be added and true will be returned.
	 */
	public boolean addDimension( Dimension dimension )
	{
		String name = dimension.getName();
		
		// check to see if we already have a dimension with the same name
		for( Dimension temp : dimensions.values() )
		{
			if( temp.getName().equals(name) )
			{
				return false;
			}
		}
		
		// no existing dimension, throw this one in
		this.dimensions.put( dimension.getHandle(), dimension );
		// assign the container property of the dimension to us
		dimension.setContainer( this );
		return true;
	}

	/**
	 * Remove and return the contained dimension with the given handle. If there is no
	 * dimension by that handle, null will be returned. 
	 */
	public Dimension removeDimension( int handle )
	{
		Dimension dimension = this.dimensions.remove( handle );
		if( dimension == null )
		{
			return null;
		}
		else
		{
			dimension.setContainer( null );
			return dimension;
		}
	}

	/**
	 * Get a set of all the dimensions contained in this space. If there are none, an empty set
	 * is returned.
	 */
	public Set<Dimension> getDimensions()
	{
		return new HashSet<Dimension>( this.dimensions.values() );
	}

	/**
	 * Find the contained {@link Dimension} of the given name and return it. If there is no
	 * {@link Dimension}, null will be returned.
	 */
	public Dimension getDimension( String dimensionName )
	{
		for( Dimension temp : dimensions.values() )
		{
			if( temp.getName().equals(dimensionName) )
				return temp;
		}
		
		// couldn't find it if we get here
		return null;
	}

	/**
	 * Find the contained {@link Dimension} of the given handle and return it. If there is no
	 * {@link Dimension}, null will be returned.
	 */
	public Dimension getDimension( int dimensionHandle )
	{
		return this.dimensions.get( dimensionHandle );
	}

	/**
	 * Returns <code>true</code> if there is a dimension of the given name inside this space.
	 * Returns <code>false</code> otherwise.
	 */
	public boolean hasDimension( String dimensionName )
	{
		for( Dimension temp : dimensions.values() )
		{
			if( temp.getName().equals(dimensionName) )
				return true;
		}
		
		return false;
	}

	/**
	 * Returns <code>true</code> if there is a dimension with the given handle inside this space.
	 * Returns <code>false</code> otherwise.
	 */
	public boolean hasDimension( int dimensionHandle )
	{
		return this.dimensions.containsKey( dimensionHandle );
	}
	
	/**
	 * Return the handle for the contained dimension of the given handle. If there is no dimension
	 * with that handle, {@link ObjectModel#INVALID_HANDLE} is returned.
	 */
	public int getDimensionHandle( String dimensionName )
	{
		for( Dimension temp: this.dimensions.values() )
		{
			if( temp.getName().equalsIgnoreCase(dimensionName) )
				return temp.getHandle();
		}
		
		return ObjectModel.INVALID_HANDLE;
	}

	/**
	 * Return the name of the contained dimension with the given handle. If the dimension doesn't
	 * exist, null is returned.
	 */
	public String getDimensionName( int dimensionHandle )
	{
		if( this.dimensions.containsKey(dimensionHandle) )
		{
			return dimensions.get(dimensionHandle).getName();
		}
		else
		{
			return null;
		}
	}

	////////////////////////////////////////////////////////////
	//////////////////// Get and Set Methods ///////////////////
	////////////////////////////////////////////////////////////
	public int getHandle()
	{
		return this.handle;
	}
	
	public String getName()
	{
		return this.name;
	}

	public ObjectModel getModel()
	{
		return this.model;
	}
	
	public void setModel( ObjectModel model )
	{
		this.model = model;
	}

	public String toString()
	{
		if( PorticoConstants.USE_Q_NAMES )
			return this.name;
		else
			return "" + this.handle;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
