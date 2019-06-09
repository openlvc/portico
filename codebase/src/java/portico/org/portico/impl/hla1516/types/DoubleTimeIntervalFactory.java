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

import hla.rti1516.CouldNotDecode;
import hla.rti1516.LogicalTimeInterval;
import hla.rti1516.LogicalTimeIntervalFactory;

public class DoubleTimeIntervalFactory implements LogicalTimeIntervalFactory
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	public static final DoubleTimeIntervalFactory INSTANCE = new DoubleTimeIntervalFactory();
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	public LogicalTimeInterval decode( byte[] buffer, int offset ) throws CouldNotDecode
	{
		try
		{
			return DoubleTimeInterval.decode( buffer, offset );
		}
		catch( Exception e )
		{
			throw new CouldNotDecode( "Error decoding LogicalTimeInterval: " + e.getMessage() );
		}
	}

	public LogicalTimeInterval makeZero()
	{
		return new DoubleTimeInterval( 0.0 );
	}

	public LogicalTimeInterval makeEpsilon()
	{
		return null;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
