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

import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.AttributeHandleValueMapFactory;

public class HLA1516AttributeHandleValueMapFactory implements AttributeHandleValueMapFactory
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	public static final HLA1516AttributeHandleValueMapFactory INSTANCE =
		new HLA1516AttributeHandleValueMapFactory();
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public AttributeHandleValueMap create( int capacity )
	{
		// it's dynamic in size, no need to create it with a given capacity
		return new HLA1516AttributeHandleValueMap();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
