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
package org.portico.impl.hla1516e.types.time;

import hla.rti1516e.exceptions.CouldNotDecode;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;

public class DoubleTimeFactory implements HLAfloat64TimeFactory
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
	public DoubleTimeFactory()
	{
		
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public HLAfloat64Time decodeTime( byte[] buffer, int offset ) throws CouldNotDecode
	{
		return DoubleTime.decode( buffer, offset );
	}

	public HLAfloat64Interval decodeInterval( byte[] buffer, int offset ) throws CouldNotDecode
	{
		return DoubleTimeInterval.decode( buffer, offset );
	}

	public HLAfloat64Time makeInitial()
	{
		return new DoubleTime( 0.0 );
	}

	public HLAfloat64Time makeFinal()
	{
		return new DoubleTime( Double.MAX_VALUE );
	}

	public HLAfloat64Time makeTime( double value )
	{
		return new DoubleTime( value );
	}

	public HLAfloat64Interval makeZero()
	{
		return new DoubleTimeInterval( 0.0 );
	}

	public HLAfloat64Interval makeEpsilon()
	{
		return new DoubleTimeInterval( 0x0.0000000000001P-1022 );
	}

	public HLAfloat64Interval makeInterval( double value )
	{
		return new DoubleTimeInterval( value );
	}

	public String getName()
	{
		return "HLAfloat64Time";
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
