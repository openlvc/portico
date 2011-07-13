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
package org.portico.impl.hla1516.types;

import hla.rti1516.ParameterHandle;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.RTIinternalError;

import java.util.HashMap;
import java.util.Map;

public class HLA1516ParameterHandleValueMap  extends HashMap<ParameterHandle,byte[]>
	implements ParameterHandleValueMap 
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public HLA1516ParameterHandleValueMap()
	{
		super();
	}
	
	public HLA1516ParameterHandleValueMap( Map<Integer,byte[]> values )
	{
		super();
		for( Integer key : values.keySet() )
		{
			super.put( new HLA1516ParameterHandle(key), values.get(key) );
		}
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

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
				realMap.put( ((HLA1516ParameterHandle)handle).handle, map.get(handle) );
			}
			
			return realMap;
		}
		catch( Exception e )
		{
			throw new RTIinternalError( "Can't convert ParameterHandleValueMap to " +
			                            "Portico native type: " + e.getMessage() , e );
		}
	}
}
