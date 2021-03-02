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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.InvalidLookahead;
import hla.rti1516.LogicalTimeInterval;

public class DoubleTimeInterval implements LogicalTimeInterval
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

	public DoubleTimeInterval()
	{
		
	}
	
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
     * Returns a LogicalTimeInterval whose value is (this - subtrahend).
     */
	public LogicalTimeInterval subtract( LogicalTimeInterval subtrahend )
	{
		if( subtrahend instanceof DoubleTimeInterval )
		{
			return new DoubleTimeInterval( time - ((DoubleTimeInterval)subtrahend).time );
		}
		else
		{
			throw new IllegalArgumentException( "Invalid argument: " + subtrahend.getClass() );
		}
	}

	public int compareTo( Object other )
	{
		if( other instanceof DoubleTimeInterval )
		{
			double otherValue = ((DoubleTimeInterval)other).time;
			if( this.time > otherValue )
			{
				return 1;
			}
			else if( this.time == otherValue )
			{
				return 0;
			}
			else
			{
				return -1;
			}
		}
		else
		{
			throw new IllegalArgumentException( "Invalid argument: " + other.getClass() );
		}
	}

	/**
     * Returns true iff this and other represent the same time interval.
     */
	public boolean equals( Object other )
	{
		if( other instanceof DoubleTimeInterval )
		{
			if( ((DoubleTimeInterval)other).time == this.time )
			{
				return true;
			}
		}
		
		return false;
	}

	/**
     * Two LogicalTimeIntervals for which equals() is true should yield same hash code
     */
	public int hashCode()
	{
		return ( Double.valueOf(this.time) ).hashCode();
	}

	public String toString()
	{
		return "" + this.time;
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
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static double fromInterval( LogicalTimeInterval lti ) throws InvalidLogicalTime
	{
		if( lti == null )
			throw new InvalidLogicalTime( "Expecting DoubleTimeInterval, found: null" );
		
		if( lti instanceof DoubleTimeInterval )
		{
			return ((DoubleTimeInterval)lti).time;
		}
		else
		{
			throw new InvalidLogicalTime( "Expecting DoubleTimeInterval, found: "+lti.getClass() );
		}
	}
	
	public static double fromLookahead( LogicalTimeInterval lti ) throws InvalidLookahead
	{
		if( lti == null )
			throw new InvalidLookahead( "Expecting DoubleTimeInterval, found: null" );
		
		if( lti instanceof DoubleTimeInterval )
		{
			return ((DoubleTimeInterval)lti).time;
		}
		else
		{
			throw new InvalidLookahead( "Expecting DoubleTimeInterval, found: "+lti.getClass() );
		}
	}
}
