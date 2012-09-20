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

import java.util.HashSet;
import java.util.Set;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.exceptions.RTIinternalError;

public class HLA1516eAttributeHandleSet
       extends HashSet<AttributeHandle>
       implements AttributeHandleSet
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
	public HLA1516eAttributeHandleSet()
	{
		super();
	}
	
	public HLA1516eAttributeHandleSet( Set<Integer> attributes )
	{
		super( attributes.size() );
		for( Integer attribute : attributes )
			this.add( new HLA1516eHandle(attribute) );
	}
	
	public HLA1516eAttributeHandleSet( int[] attributes )
	{
		super( attributes.length );
		for( int attribute : attributes )
			add( new HLA1516eHandle(attribute) );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public AttributeHandleSet clone()
	{
		return (AttributeHandleSet)super.clone();
	}

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
				realSet.add( ((HLA1516eHandle)handle).handle );
			}
			
			return realSet;
		}
		catch( Exception e )
		{
			throw new RTIinternalError( "Can't convert AttributeHandleSet to Portico native type: " +
			                            e.getMessage() , e );
		}
	}
}
