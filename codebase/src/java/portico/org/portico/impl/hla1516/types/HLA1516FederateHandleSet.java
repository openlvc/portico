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

import hla.rti1516.FederateHandle;
import hla.rti1516.FederateHandleSet;
import hla.rti1516.RTIinternalError;

public class HLA1516FederateHandleSet extends HashSet<FederateHandle> implements FederateHandleSet
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

	public HLA1516FederateHandleSet()
	{
		super();
	}

	public HLA1516FederateHandleSet( Set<Integer> values )
	{
		super();
		for( Integer i : values )
		{
			super.add( new HLA1516FederateHandle(i) );
		}
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static HashSet<Integer> toJavaSet( FederateHandleSet set ) throws RTIinternalError
	{
		try
		{
			HashSet<Integer> realSet = new HashSet<Integer>();
			for( FederateHandle handle : set )
			{
				realSet.add( ((HLA1516FederateHandle)handle).handle );
			}
			
			return realSet;
		}
		catch( Exception e )
		{
			throw new RTIinternalError( "Can't convert FederateHandleSet to Portico native type: " +
			                            e.getMessage() , e );
		}
	}
}
