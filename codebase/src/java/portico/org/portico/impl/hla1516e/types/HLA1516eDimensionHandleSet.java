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

import hla.rti1516e.DimensionHandle;
import hla.rti1516e.DimensionHandleSet;

import java.util.HashSet;
import java.util.Set;

public class HLA1516eDimensionHandleSet
       extends HashSet<DimensionHandle>
       implements DimensionHandleSet
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
	public HLA1516eDimensionHandleSet()
	{
		super();
	}
	
	public HLA1516eDimensionHandleSet( Set<Integer> dimensions )
	{
		super( dimensions.size() );
		for( Integer dimension : dimensions )
			this.add( new HLA1516eHandle(dimension) );
	}
	
	public HLA1516eDimensionHandleSet( int[] dimensions )
	{
		super( dimensions.length );
		for( int dimension : dimensions )
			add( new HLA1516eHandle(dimension) );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
