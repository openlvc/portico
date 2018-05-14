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

import hla.rti1516e.exceptions.IllegalTimeArithmetic;
import hla.rti1516e.time.HLAinteger64Interval;
import hla.rti1516e.time.HLAinteger64Time;

import org.portico.utils.bithelpers.BitHelpers;

public class LongTime implements HLAinteger64Time
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private long time;
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public LongTime( long value )
	{
		this.time = value;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////
	//////////////////////// Non-Standard Compliant Methods ////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	public long getTime()
	{
		return this.time;
	}
	
	public void setTime( long time )
	{
		this.time = time;
	}

	public String toString()
	{
		return "" + time;
	}

	////////////////////////////////////////////////////////////////////////////////
	//////////////////////// Logical Time Interface Methods ////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	public boolean isInitial()
	{
		return this.time == 0;
	}

	public boolean isFinal()
	{
		return this.time == Double.MAX_VALUE;
	}

	/**
     * Returns a new HLAfloat64Time whose value is (this + interval).
     */
	public HLAinteger64Time add( HLAinteger64Interval interval ) throws IllegalTimeArithmetic
	{
		return new LongTime( this.time + interval.getValue() );
	}

	/**
     * Returns a new HLAfloat64Time whose value is (this - interval).
     */
	public HLAinteger64Time subtract( HLAinteger64Interval interval ) throws IllegalTimeArithmetic
	{
		return new LongTime( this.time - interval.getValue() );
	}

	/**
     * Returns a new HLAfloat64Interval whose value is the time interval between this
     * and the provided time.
     */
	public HLAinteger64Interval distance( HLAinteger64Time other )
	{
		return new LongTimeInterval( Math.abs(this.time-other.getValue()) );
	}

	public int compareTo( HLAinteger64Time other )
	{
		double otherTime = other.getValue();
		if( this.time == otherTime )
			return 0;
		else if( this.time > otherTime )
			return 1;
		else
			return -1;
	}

	public int encodedLength()
	{
		return 12;  // 4 (size) + 8 (value)
	}

	public void encode( byte[] buffer, int offset )
	{
		BitHelpers.putIntBE( 8, buffer, offset );
		BitHelpers.putLongBE( this.time, buffer, offset + 4 );
	}

	public long getValue()
	{
		return this.time;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static LongTime decode( byte[] buffer, int offset )
	{
		// int length = BitHelpers.readIntBE( buffer, offset );     // size
		long value = BitHelpers.readLongBE( buffer, offset + 4 );   // value
		return new LongTime( value );
	}
}
