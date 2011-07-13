/*
 *   Copyright 2008 The Portico Project
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
package hla.rti13.java1;

import java.util.HashMap;
import java.util.Map;

import org.portico.impl.hla13.types.Java1ByteArrayMap;
import org.portico.impl.hla13.types.Java1Region;
import org.portico.lrc.model.RegionInstance;

public class ReceivedInteraction
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Java1ByteArrayMap values;
	private Java1Region region;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public ReceivedInteraction( HashMap<Integer,byte[]> values, RegionInstance region )
	{
		this.values = new Java1ByteArrayMap( values );
		if( region == null )
			this.region = null;
		else
			this.region = new Java1Region( region );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Return order type - Not currently supported
	 * 
	 * @return int order type
	 */
	public int getOrderType()
	{
		return -1;
	}

	/**
	 * Return parameter handle at index position.
	 * 
	 * @return int parameter handle
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public int getParameterHandle( int index ) throws ArrayIndexOutOfBounds
	{
		return values.getHandle( index );
	}

	/**
	 * This will return null if there is no region, or the region that the interaction was sent with
	 */
	public Region getRegion()
	{
		return region;
	}

	/**
	 * Return transport type - Not currently supported
	 * 
	 * @return int transport type
	 */
	public int getTransportType()
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
		return values.getValue( index );
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
		return values.getValue(index).length;
	}

	/**
	 * Not really supported, this will just return the same as {@link #getValue(int) getValue(int)}
	 * 
	 * @return byte[] the reference
	 * @param index int
	 * @exception hla.rti.ArrayIndexOutOfBounds
	 */
	public byte[] getValueReference( int index ) throws ArrayIndexOutOfBounds
	{
		return values.getValue( index );
	}

	/**
	 * @return int Number of parameter handle-value pairs
	 */
	public int size()
	{
		return values.size();
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
