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
import hla.rti.ReflectedAttributes;
import hla.rti.Region;

import java.util.HashMap;
import java.util.Map;

import org.portico.lrc.model.RegionInstance;
import org.portico2.common.services.object.msg.UpdateAttributes.FilteredAttribute;

/**
 * This class just wraps a {@link HLA13ByteArrayMap HLA13ByteArrayMap} and passes all the relevant
 * calls on to it.
 */
public class HLA13ReflectedAttributes implements ReflectedAttributes
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private HLA13ByteArrayMap values;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public HLA13ReflectedAttributes( HashMap<Integer,FilteredAttribute> filteredAttributes )
	{
		this.values = new HLA13ByteArrayMap();
		this.values.populateForCallback( filteredAttributes );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Return attribute handle at index position.
	 * 
	 * @return int attribute handle
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public int getAttributeHandle( int index ) throws ArrayIndexOutOfBounds
	{
		return this.values.getHandle( index );
	}

	/**
	 * Return order handle at index position. - not currently supported
	 * 
	 * @return int order type
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public int getOrderType( int index ) throws ArrayIndexOutOfBounds
	{
		return -1;
	}

	/**
	 * Return Region handle at index position. - not currently supported
	 * 
	 * @return int region handle
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public Region getRegion( int index ) throws ArrayIndexOutOfBounds
	{
		RegionInstance rawRegion = values.getRawRegion( index );
		if( rawRegion == null )
			return null;
		else
			return new HLA13Region( rawRegion );
	}
	
	public RegionInstance getRawRegion( int index ) throws ArrayIndexOutOfBounds
	{
		return values.getRawRegion( index );
	}

	/**
	 * Return transport handle at index position. - not currently supported
	 * 
	 * @return int transport type
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public int getTransportType( int index ) throws ArrayIndexOutOfBounds
	{
		return -1;
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
		return this.values.getValue( index );
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
		return this.values.getValue(index).length;
	}

	/**
	 * Get the reference of the value at position index (not a clone). -Not supported, just returns
	 * the same as {@link #getValue(int) getValue(int)}.
	 * 
	 * @return byte[] the reference
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public byte[] getValueReference( int index ) throws ArrayIndexOutOfBounds
	{
		return this.values.getValue( index );
	}

	/**
	 * @return int Number of attribute handle-value pairs
	 */
	public int size()
	{
		return this.values.size();
	}
	
	/**
	 * Empty the internal store. Added to support rti13.javapackage 
	 */
	public void empty()
	{
		values.empty();
	}
	
	/**
	 * Get the handle for the given index. Added to support rti13.javapackage 
	 */
	public int getHandle( int index ) throws ArrayIndexOutOfBounds
	{
		return values.getHandle( index );
	}

	public Map<Integer,byte[]> toJavaMap()
	{
		return this.values.toJavaMap();
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
