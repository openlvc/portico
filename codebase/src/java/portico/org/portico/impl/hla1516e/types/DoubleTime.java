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

import hla.rti1516e.exceptions.IllegalTimeArithmetic;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

public class DoubleTime implements HLAfloat64Time
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
	public DoubleTime( double value )
	{
		this.time = value;
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
		return "" + time;
	}

	////////////////////////////////////////////////////////////////////////////////
	//////////////////////// Logical Time Interface Methods ////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	public boolean isInitial()
	{
		return this.time == 0.0;
	}

	public boolean isFinal()
	{
		return this.time == Double.MAX_VALUE;
	}

	/**
     * Returns a new HLAfloat64Time whose value is (this + interval).
     */
	public HLAfloat64Time add( HLAfloat64Interval interval ) throws IllegalTimeArithmetic
	{
		return new DoubleTime( this.time + interval.getValue() );
	}

	/**
     * Returns a new HLAfloat64Time whose value is (this - interval).
     */
	public HLAfloat64Time subtract( HLAfloat64Interval interval ) throws IllegalTimeArithmetic
	{
		return new DoubleTime( this.time - interval.getValue() );
	}

	/**
     * Returns a new HLAfloat64Interval whose value is the time interval between this
     * and the provided time.
     */
	public HLAfloat64Interval distance( HLAfloat64Time other )
	{
		return new DoubleTimeInterval( Math.abs(this.time-other.getValue()) );
	}

	public int compareTo( HLAfloat64Time other )
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

	public void encode( byte[] buffer, int offset )
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
