/*
 *   Copyright 2012 The Portico Project
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
package org.portico.impl.hla1516e.types;

import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.exceptions.RTIinternalError;

import java.util.HashMap;
import java.util.Map;

public class HLA1516eParameterHandleValueMap
       extends HashMap<ParameterHandle,byte[]>
       implements ParameterHandleValueMap
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLA1516eParameterHandleValueMap()
	{
		super();
	}

	public HLA1516eParameterHandleValueMap( int capacity )
	{
		super( capacity );
	}
	
	public HLA1516eParameterHandleValueMap( Map<Integer,byte[]> parameters )
	{
		this();
		for( Integer parameter : parameters.keySet() )
			this.put( new HLA1516eHandle(parameter), parameters.get(parameter) );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns a {@link ByteWrapper} around the byte[] mapped to the provided
	 * {@link ParameterHandle}, or null if there is no mapping for the key.
	 */
	public ByteWrapper getValueReference( ParameterHandle key )
	{
		byte[] value = super.get( key );
		if( value == null )
			return null;
		else
			return new ByteWrapper( value );
	}

	/**
	 * Finds the mapping for the provided {@link ParameterHandle} and update the
	 * provided {@link ByteWrapper} with it. The same wrapper is then returned.
	 * If there is no value for the attribute, null is returned.
	 */
	public ByteWrapper getValueReference( ParameterHandle key, ByteWrapper byteWrapper )
	{
		byte[] value = super.get( key );
		if( value == null )
			return null;
		
		// update the wrapper and return
		byteWrapper.reassign( value, 0, value.length );
		return byteWrapper;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static HashMap<Integer,byte[]> toJavaMap( ParameterHandleValueMap map )
		throws RTIinternalError
	{
		try
		{
			HashMap<Integer,byte[]> realMap = new HashMap<Integer,byte[]>();
			for( ParameterHandle handle : map.keySet() )
			{
				realMap.put( ((HLA1516eHandle)handle).handle, map.get(handle) );
			}
			
			return realMap;
		}
		catch( Exception e )
		{
			throw new RTIinternalError( "Can't convert ParameterHandleValueMap to Portico native type: " +
			                            e.getMessage() , e );
		}
	}
}
