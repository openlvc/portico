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

import hla.rti1516e.exceptions.CouldNotEncode;
import hla.rti1516e.exceptions.IllegalTimeArithmetic;
import hla.rti1516e.exceptions.InvalidLogicalTimeInterval;
import hla.rti1516e.time.HLAinteger64Interval;

import org.portico.utils.bithelpers.BitHelpers;

public class LongTimeInterval implements HLAinteger64Interval
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
	public LongTimeInterval( long time )
	{
		this.time = time;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////
	//////////////////////// Non-Standard Compliant Methods ////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	public double getTime()
	{
		return this.time;
	}
	
	public void setTime( long time )
	{
		this.time = time;
	}

	public String toString()
	{
		return "" + this.time;
	}

	/////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Interface Methods ///////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////
	public boolean isZero()
	{
		return this.time == 0;
	}

	public boolean isEpsilon()
	{
		return false;
	}

	/**
     * Returns a LogicalTimeInterval whose value is (this + addend).
     */
	public HLAinteger64Interval add( HLAinteger64Interval addend )
		throws IllegalTimeArithmetic, InvalidLogicalTimeInterval
	{
		return new LongTimeInterval( this.time + addend.getValue() );
	}

	/**
     * Returns a LogicalTimeInterval whose value is (this - subtrahend).
     */
	public HLAinteger64Interval subtract( HLAinteger64Interval subtrahend )
		throws IllegalTimeArithmetic, InvalidLogicalTimeInterval
	{
		return new LongTimeInterval( this.time - subtrahend.getValue() );
	}

	public int compareTo( HLAinteger64Interval other )
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
		return 12;  // 4 (size) + 8 (length)
	}

	public void encode( byte[] buffer, int offset ) throws CouldNotEncode
	{
		BitHelpers.putIntBE( 8, buffer, offset );           // size
		BitHelpers.putLongBE( this.time, buffer, offset );  // value
	}

	public long getValue()
	{
		return this.time;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static LongTimeInterval decode( byte[] buffer, int offset )
	{
		// int length = BitHelpers.readIntBE( buffer, offset );    // size
		long value = BitHelpers.readLongBE( buffer, offset + 4 );  // value
		return new LongTimeInterval( value );
	}
}
