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
		return new LongTime( decode(buffer,offset) );
	}

	public HLAinteger64Interval decodeInterval( byte[] buffer, int offset ) throws CouldNotDecode
	{
		return new LongTimeInterval( decode(buffer,offset) );
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
	public static void encode( long value, byte[] buffer, int offset )
	{
		int length = buffer.length-offset;
		if( length <= 8 )
		{
			throw new RuntimeException( "Buffer overflow. Tried to write 8 bytes, only "+
			                            length+" bytes available" );
		}

		buffer[offset]   = (byte)(value >>> 56);
		buffer[offset+1] = (byte)(value >>> 48);
		buffer[offset+2] = (byte)(value >>> 40);
		buffer[offset+3] = (byte)(value >>> 32);
		buffer[offset+4] = (byte)(value >>> 24);
		buffer[offset+5] = (byte)(value >>> 16);
		buffer[offset+6] = (byte)(value >>>  8);
		buffer[offset+7] = (byte)(value >>>  0);
	}

	public static long decode( byte[] buffer, int offset )
	{
		int length = buffer.length-offset;
		if( buffer.length-offset <= 8 )
			throw new RuntimeException( "Buffer underflow. Tried to read 8 bytes, found "+length );

        return (((long)buffer[0] << 56) +
                ((long)(buffer[1] & 255) << 48) +
                ((long)(buffer[2] & 255) << 40) +
                ((long)(buffer[3] & 255) << 32) +
                ((long)(buffer[4] & 255) << 24) +
                ((buffer[5] & 255) << 16) +
                ((buffer[6] & 255) <<  8) +
                ((buffer[7] & 255) <<  0));
	}
}
