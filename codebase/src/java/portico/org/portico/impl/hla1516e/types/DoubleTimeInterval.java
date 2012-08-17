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

import hla.rti1516e.exceptions.CouldNotEncode;
import hla.rti1516e.exceptions.IllegalTimeArithmetic;
import hla.rti1516e.exceptions.InvalidLogicalTimeInterval;
import hla.rti1516e.time.HLAfloat64Interval;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class DoubleTimeInterval implements HLAfloat64Interval
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private double time;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public DoubleTimeInterval( double time )
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
	
	public void setTime( double time )
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
		return this.time == 0.0;
	}

	public boolean isEpsilon()
	{
		return false;
	}

	/**
     * Returns a LogicalTimeInterval whose value is (this + addend).
     */
	public HLAfloat64Interval add( HLAfloat64Interval addend )
		throws IllegalTimeArithmetic, InvalidLogicalTimeInterval
	{
		return new DoubleTimeInterval( this.time + addend.getValue() );
	}

	/**
     * Returns a LogicalTimeInterval whose value is (this - subtrahend).
     */
	public HLAfloat64Interval subtract( HLAfloat64Interval subtrahend )
		throws IllegalTimeArithmetic, InvalidLogicalTimeInterval
	{
		return new DoubleTimeInterval( this.time - subtrahend.getValue() );
	}

	public int compareTo( HLAfloat64Interval other )
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
		try
		{
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream( byteStream );
			stream.writeDouble( this.time );
			stream.close();
			return byteStream.toByteArray().length;
		}
		catch( Exception e )
		{
			// shouldn't happen
			return -1;
		}
	}

	public void encode( byte[] buffer, int offset ) throws CouldNotEncode
	{
		try
		{
			// convert the into an array
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			DataOutputStream stream = new DataOutputStream( byteStream );
			stream.writeDouble( this.time );
			stream.close();
			byte[] bytes = byteStream.toByteArray();
			
			// copy it into the given array
			System.arraycopy( bytes, 0, buffer, offset, bytes.length );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	public double getValue()
	{
		return this.time;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
