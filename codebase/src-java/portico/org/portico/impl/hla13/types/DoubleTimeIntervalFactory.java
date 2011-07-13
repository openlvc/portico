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

import hla.rti.CouldNotDecode;
import hla.rti.LogicalTimeInterval;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class DoubleTimeIntervalFactory
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

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	public LogicalTimeInterval decode( byte[] buffer, int offset ) throws CouldNotDecode
	{
		try
		{
			ByteArrayInputStream byteStream =
				new ByteArrayInputStream( buffer, offset, (buffer.length - offset) );
			return new DoubleTimeInterval( new DataInputStream(byteStream).readDouble() );
		}
		catch( IOException ioex )
		{
			throw new CouldNotDecode( ioex.getMessage() );
		}
	}

	public LogicalTimeInterval makeZero()
	{
		return new DoubleTimeInterval();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
