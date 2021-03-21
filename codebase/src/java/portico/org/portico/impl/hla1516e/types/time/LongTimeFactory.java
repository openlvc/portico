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
import hla.rti1516e.time.HLAinteger64TimeFactory;
import hla.rti1516e.time.HLAinteger64Interval;
import hla.rti1516e.time.HLAinteger64Time;

public class LongTimeFactory implements HLAinteger64TimeFactory
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
	public LongTimeFactory()
	{
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public HLAinteger64Time decodeTime( byte[] buffer, int offset ) throws CouldNotDecode
	{
		return LongTime.decode( buffer, offset );
	}

	public HLAinteger64Interval decodeInterval( byte[] buffer, int offset ) throws CouldNotDecode
	{
		return LongTimeInterval.decode( buffer, offset );
	}

	public HLAinteger64Time makeInitial()
	{
		return new LongTime( 0 );
	}

	public HLAinteger64Time makeFinal()
	{
		return new LongTime( Long.MAX_VALUE );
	}

	public HLAinteger64Time makeTime( long value )
	{
		return new LongTime( value );
	}

	public HLAinteger64Interval makeZero()
	{
		return new LongTimeInterval( 0 );
	}

	public HLAinteger64Interval makeEpsilon()
	{
		return new LongTimeInterval( 1 );
	}

	public HLAinteger64Interval makeInterval( long value )
	{
		return new LongTimeInterval( value );
	}

	public String getName()
	{
		return "HLAinteger64Time";
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
