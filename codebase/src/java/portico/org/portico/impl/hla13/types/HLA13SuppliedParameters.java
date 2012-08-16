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

import java.util.Map;

import hla.rti.SuppliedParameters;

/**
 * This class just falls back on {@link HLA13ByteArrayMap HLA13ByteArrayMap} for its functionality
 */
public class HLA13SuppliedParameters extends HLA13ByteArrayMap implements SuppliedParameters
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

	public HLA13SuppliedParameters()
	{
		super();
	}
	
	public HLA13SuppliedParameters( int capacity )
	{
		super( capacity );
	}
	
	public HLA13SuppliedParameters( HLA13ByteArrayMap map )
	{
		super.pairs = map.pairs;
	}
	
	public HLA13SuppliedParameters( Map<Integer,byte[]> map )
	{
		super( map );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
