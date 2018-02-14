/*
 *   Copyright 2006 The Portico Project
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

import hla.rti.ArrayIndexOutOfBounds;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.portico.lrc.model.RegionInstance;
import org.portico2.common.services.object.msg.UpdateAttributes.FilteredAttribute;

/**
 * This class provides the common Map "functionality" required by the HLA 1.3 spec. Implementations
 * for SuppliedAttributes and SuppliedParameters can just extend it to get the required
 * functionality. 
 */
public class HLA13ByteArrayMap implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected List<Pair> pairs;
	protected List<RegionInstance> regions;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public HLA13ByteArrayMap()
	{
		this.pairs = new ArrayList<Pair>();
		this.regions = new ArrayList<RegionInstance>();
	}
	
	public HLA13ByteArrayMap( int capacity )
	{
		this.pairs = new ArrayList<Pair>( capacity );
		this.regions = new ArrayList<RegionInstance>( capacity );
	}

	public HLA13ByteArrayMap( Map<Integer,byte[]> map )
	{
		this();
		
		for( Integer key : map.keySet() )
		{
			this.pairs.add( new Pair(key,map.get(key) ) );
		}
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * When an attribute reflection is received on the client side, its contents are filtered
	 * down to contain only the information subscribed to. Also contained with that information
	 * is the subscribed region that overlapped with the senders region. This method will take
	 * the filtered information and will populate the local array-map from it. This only applies
	 * to attribute reflections and not interactions. 
	 */
	public void populateForCallback( Map<Integer,FilteredAttribute> filteredAttributes )
	{
		for( Integer key : filteredAttributes.keySet() )
		{
			FilteredAttribute filtered = filteredAttributes.get( key );
			this.pairs.add( new Pair(key,filtered.value) );
			this.regions.add( filtered.region );
		}
	}
	
	public RegionInstance getRawRegion( int index ) throws ArrayIndexOutOfBounds
	{
		if( index > regions.size() )
			throw new ArrayIndexOutOfBounds("index: "+index );
		else
			return regions.get( index );
	}
	
	/**
	 * Non-standard method to help ease some insanity.
	 * <p/>
	 * Convert this into a proper java map. <b>NOTE:</b> The returned map is not tied in any way
	 * to this map. If you add an element to one, it will not appear in the other.
	 */
	public HashMap<Integer,byte[]> toJavaMap()
	{
		HashMap<Integer,byte[]> map = new HashMap<Integer,byte[]>();
		for( Pair pair : pairs )
		{
			map.put( pair.handle, pair.value );
		}
		
		return map;
	}
	
	/**
	 * Add pair beyond last index.
	 * 
	 * @param handle int
	 * @param value byte[]
	 */
	public void add( int handle, byte[] value )
	{
		this.pairs.add( new Pair(handle,value) );
	}

	/**
	 * Removes all handles & values.
	 */
	public void empty()
	{
		this.pairs.clear();
	}

	/**
	 * Return handle at index position.
	 * 
	 * @return int attribute handle
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public int getHandle( int index ) throws ArrayIndexOutOfBounds
	{
		return this.getPair(index).handle;
	}

	/**
	 * Return copy of value at index position.
	 * 
	 * @return byte[] copy (clone) of value
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public byte[] getValue( int index ) throws ArrayIndexOutOfBounds
	{
		return this.getPair(index).value;
	}

	/**
	 * Return length of value at index position.
	 * 
	 * @return int value length
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public int getValueLength( int index ) throws ArrayIndexOutOfBounds
	{
		return this.getPair(index).value.length;
	}

	/**
	 * Get the reference of the value at position index (not a clone)
	 * 
	 * @return byte[] the reference
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public byte[] getValueReference( int index ) throws ArrayIndexOutOfBounds
	{
		return this.getPair(index).value;
	}

	/**
	 * Remove handle & value corresponding to handle. All other elements shifted down. Not safe
	 * during iteration.
	 * 
	 * @param handle int
	 * @exception hla.rti.ArrayIndexOutOfBounds if handle not in set
	 */
	public void remove( int handle ) throws ArrayIndexOutOfBounds
	{
		for( int i = 0; i < this.pairs.size(); ++i )
		{
			if( this.getPair(i).handle == handle )
			{
				this.pairs.remove( i );
				return;
			}
		}
		
		throw new ArrayIndexOutOfBounds( "Handle: " + handle + " not found" );
	}

	/**
	 * Remove handle & value at index position. All other elements shifted down. Not safe during
	 * iteration.
	 * 
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public void removeAt( int index ) throws ArrayIndexOutOfBounds
	{
		this.getPair( index ); // if the index is too much, an exception will be thrown
		this.pairs.remove( index );
	}

	/** 
	 * @return int Number of elements 
	 */
	public int size()
	{
		return this.pairs.size();
	}
	
	/**
	 * Gets an array of all the handles contained within this map
	 */
	public int[] getHandles()
	{
		int[] handles = new int[pairs.size()];
		for( int i = 0; i < handles.length; i++ )
		{
			handles[i] = this.pairs.get(i).handle;
		}
		
		return handles;
	}
	
	private Pair getPair( int index ) throws ArrayIndexOutOfBounds
	{
		try
		{
			return this.pairs.get(index);
		}
		catch( IndexOutOfBoundsException e )
		{
			throw new ArrayIndexOutOfBounds( "max: " + (pairs.size()-1) + ", requested: " + index );
		}
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

	private class Pair implements Serializable
	{
		private static final long serialVersionUID = 98121116105109L;
		
		private int handle;
		private byte[] value;
		
		private Pair( int handle, byte[] value )
		{
			this.handle = handle;
			this.value = value;
		}
	}
}
