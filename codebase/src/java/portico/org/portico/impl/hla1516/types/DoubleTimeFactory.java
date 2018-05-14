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
import hla.rti1516.LogicalTime;
import hla.rti1516.LogicalTimeFactory;

public class DoubleTimeFactory implements LogicalTimeFactory
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	public static final DoubleTimeFactory INSTANCE = new DoubleTimeFactory();
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	public LogicalTime decode( byte[] buffer, int offset ) throws CouldNotDecode
	{
		try
		{
			return DoubleTime.decode( buffer, offset );
		}
		catch( Exception e )
		{
			throw new CouldNotDecode( "Error decoding LogicalTime: " + e.getMessage() );
		}
	}

	public LogicalTime makeInitial()
	{
		return new DoubleTime( 0.0 );
	}

	public LogicalTime makeFinal()
	{
		return new DoubleTime( Double.MAX_VALUE );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
