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

import hla.rti.SuppliedAttributes;

/**
 * This class just falls back on {@link HLA13ByteArrayMap HLA13ByteArrayMap} for its functionality
 */
public class HLA13SuppliedAttributes extends HLA13ByteArrayMap implements SuppliedAttributes
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

	public HLA13SuppliedAttributes()
	{
		super();
	}
	
	public HLA13SuppliedAttributes( int capacity )
	{
		super( capacity );
	}
	
	public HLA13SuppliedAttributes( HLA13ByteArrayMap map )
	{
		super.pairs = map.pairs;
	}
	
	public HLA13SuppliedAttributes( Map<Integer,byte[]> map )
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
