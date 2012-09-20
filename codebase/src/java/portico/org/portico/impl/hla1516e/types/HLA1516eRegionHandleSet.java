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

import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;

import java.util.HashSet;
import java.util.Set;

public class HLA1516eRegionHandleSet
       extends HashSet<RegionHandle>
       implements RegionHandleSet
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
	public HLA1516eRegionHandleSet()
	{
		super();
	}
	
	public HLA1516eRegionHandleSet( Set<Integer> regions )
	{
		super( regions.size() );
		for( Integer region : regions )
			this.add( new HLA1516eHandle(region) );
	}
	
	public HLA1516eRegionHandleSet( int[] regions )
	{
		super( regions.length );
		for( int region : regions )
			add( new HLA1516eHandle(region) );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
