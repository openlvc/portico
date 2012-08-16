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

import java.util.HashSet;
import java.util.Set;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.RTIinternalError;

public class HLA1516AttributeHandleSet extends HashSet<AttributeHandle>
	implements AttributeHandleSet
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

	public HLA1516AttributeHandleSet()
	{
		super();
	}

	public HLA1516AttributeHandleSet( Set<Integer> values )
	{
		super();
		for( Integer i : values )
		{
			super.add( new HLA1516AttributeHandle(i) );
		}
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static HashSet<Integer> toJavaSet( AttributeHandleSet set ) throws RTIinternalError
	{
		try
		{
			HashSet<Integer> realSet = new HashSet<Integer>();
			for( AttributeHandle handle : set )
			{
				realSet.add( ((HLA1516AttributeHandle)handle).handle );
			}
			
			return realSet;
		}
		catch( Exception e )
		{
			throw new RTIinternalError( "Can't convert AttributeHandleSet to Portico native type: "+
			                            e.getMessage() , e );
		}
	}
}
