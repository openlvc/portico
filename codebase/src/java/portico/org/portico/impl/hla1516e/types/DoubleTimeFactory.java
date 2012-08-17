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
		return new DoubleTime( decode(buffer,offset) );
	}

	public HLAfloat64Interval decodeInterval( byte[] buffer, int offset ) throws CouldNotDecode
	{
		return new DoubleTimeInterval( decode(buffer,offset) );
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
	public static void encode( double value, byte[] buffer, int offset )
	{
		int length = buffer.length-offset;
		if( length <= 8 )
		{
			throw new RuntimeException( "Buffer overflow. Tried to write 8 bytes, only "+
			                            length+" bytes available" );
		}

		long rawbits = Double.doubleToLongBits( value );
		buffer[offset]   = (byte)(rawbits >>> 56);
		buffer[offset+1] = (byte)(rawbits >>> 48);
		buffer[offset+2] = (byte)(rawbits >>> 40);
		buffer[offset+3] = (byte)(rawbits >>> 32);
		buffer[offset+4] = (byte)(rawbits >>> 24);
		buffer[offset+5] = (byte)(rawbits >>> 16);
		buffer[offset+6] = (byte)(rawbits >>>  8);
		buffer[offset+7] = (byte)(rawbits >>>  0);
	}

	public static double decode( byte[] buffer, int offset )
	{
		int length = buffer.length - offset;
		if( buffer.length-offset <= 8 )
			throw new RuntimeException( "Buffer underflow. Tried to read 8 bytes, found "+length );

		long temp = (((long)buffer[0] << 56) +
                     ((long)(buffer[1] & 255) << 48) +
                     ((long)(buffer[2] & 255) << 40) +
                     ((long)(buffer[3] & 255) << 32) +
                     ((long)(buffer[4] & 255) << 24) +
                     ((buffer[5] & 255) << 16) +
                     ((buffer[6] & 255) <<  8) +
                     ((buffer[7] & 255) <<  0));

		return Double.longBitsToDouble( temp );
	}
}
