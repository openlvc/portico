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

import hla.rti.LogicalTime;
import hla.rti.LogicalTimeInterval;
import hla.rti.IllegalTimeArithmetic;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Let me start off by saying just how stupid the use of 4 freaking interfaces to define time is.
 * Why didn't either the standards board or the LRC board have the ***** to just mandate that it
 * be represented as a 64-bit signed floating point number (or unsigned, or anything else) I don't
 * know. It would make everything SO MUCH SIMPLER, but I guess that's not the spirit of the HLA
 * now is it. Sigh. This is the Portico implementation of the LogicalTime interface that is backed
 * by a double. I have included "non-standards-compliant" methods to do such amazing things as
 * GET THE FREAKING TIME. I see no problem in using these methods even though it will break
 * dynamic link compat because to actually use time in any way will mean having to break it anyway.
 * You might as well get some convenience out of it.
 * <p/>
 * <b>NOTE:</b> This implementation is for the 1.3 version of the DLC
 * <p/>
 * PS. While I'm sounding off on the DLC, what is the point in providing a subtract method but
 * no add method? I'm tipping this was just a mistake, but really, don't people read things before
 * they vote for them?
 */
public class DoubleTime implements LogicalTime
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final double INITIAL = 0.0;
	public static final double FINAL   = Double.MAX_VALUE;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private double time;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public DoubleTime()
	{
		this.time = INITIAL;
	}
	
	public DoubleTime( double time )
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
	
	////////////////////////////////////////////////////////////////////////////////
	//////////////////////// Logical Time Interface Methods ////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	public void decreaseBy( LogicalTimeInterval subtrahend ) throws IllegalTimeArithmetic
	{
		// check for null
		if( subtrahend == null )
		{
			// no good!
			throw new IllegalTimeArithmetic( "Given argument was null" );
		}
		
		// cast and decrease
		try
		{
			this.time -= ((DoubleTimeInterval)subtrahend).getInterval();
		}
		catch( Exception ex )
		{
			throw new IllegalTimeArithmetic( ex.getMessage() );
		}
	}

	public void encode( byte[] buffer, int offset )
	{
		byte[] leBytes = DoubleTime.encode( time );
		// copy the bytes across to the given buffer
		System.arraycopy( leBytes, 0, buffer, offset, leBytes.length );
	}

	public int encodedLength()
	{
		return DoubleTime.encode(this.time).length;
	}

	public void increaseBy( LogicalTimeInterval addend ) throws IllegalTimeArithmetic
	{
		// check for null
		if( addend == null )
		{
			// no good!
			throw new IllegalTimeArithmetic( "Given argument was null" );
		}
		
		// cast and decrease
		try
		{
			this.time += ((DoubleTimeInterval)addend).getInterval();
		}
		catch( Exception ex )
		{
			throw new IllegalTimeArithmetic( ex.getMessage() );
		}
	}

	public boolean isEqualTo( LogicalTime value )
	{
		// check for null
		if( value == null )
		{
			// no good!
			return false;
		}
		
		// cast and decrease
		try
		{
			return this.time == ((DoubleTime)value).time;
		}
		catch( Exception ex )
		{
			// no love, falseys it is
			return false;
		}
	}

	public boolean isFinal()
	{
		return this.time == FINAL;
	}

	public boolean isGreaterThan( LogicalTime value )
	{
		// check for null
		if( value == null )
		{
			// no good!
			return false;
		}
		
		// cast and decrease
		try
		{
			return this.time > ((DoubleTime)value).time;
		}
		catch( Exception ex )
		{
			// no love, falseys it is
			return false;
		}
	}

	public boolean isGreaterThanOrEqualTo( LogicalTime value )
	{
		// check for null
		if( value == null )
		{
			// no good!
			return false;
		}
		
		// cast and decrease
		try
		{
			return this.time >= ((DoubleTime)value).time;
		}
		catch( Exception ex )
		{
			// no love, falseys it is
			return false;
		}
	}

	public boolean isInitial()
	{
		return this.time == INITIAL;
	}

	public boolean isLessThan( LogicalTime value )
	{
		// check for null
		if( value == null )
		{
			// no good!
			return false;
		}
		
		// cast and decrease
		try
		{
			return this.time < ((DoubleTime)value).time;
		}
		catch( Exception ex )
		{
			// no love, falseys it is
			return false;
		}
	}

	public boolean isLessThanOrEqualTo( LogicalTime value )
	{
		// check for null
		if( value == null )
		{
			// no good!
			return false;
		}
		
		// cast and decrease
		try
		{
			return this.time <= ((DoubleTime)value).time;
		}
		catch( Exception ex )
		{
			// no love, falseys it is
			return false;
		}
	}

	public void setFinal()
	{
		this.time = FINAL;
	}

	public void setInitial()
	{
		this.time = INITIAL;
	}

	public void setTo( LogicalTime value )
	{
		// make sure we have a time first
		if( value == null )
		{
			return;
		}
		
		try
		{
			this.time = ((DoubleTime)value).time;
			return;
		}
		catch( Exception ex )
		{
			// skip the update
			return;
		}
	}

	/**
	 * Will return an interval whose value is the subtraction of this logical time and the given
	 * logical time. If there is a problem, null will be returned (so check for it!).
	 */
	public LogicalTimeInterval subtract( LogicalTime subtrahend )
	{
		// check for null
		if( subtrahend == null )
		{
			// no good!
			return null;
		}
		
		// cast and decrease
		try
		{
			double temp = this.time - ((DoubleTimeInterval)subtrahend).getInterval();
			return new DoubleTimeInterval( temp );
		}
		catch( Exception ex )
		{
			return null;
		}
	}
	
	/**
	 * Will return an interval whose value is the sum of this logical time and the given logical
	 * time. If there is a problem, null will be returned (so check for it!).
	 * <p/>
	 * Sadly I don't think this is actually in the DLC standard (as of Dec,2004 which is the most
	 * up to date version I have). Still, as I think the whole DLC and HLA spec's are pure jive
	 * with regard to time I really don't give a hoot. I've added this here for any sick puppies
	 * out there who want it. 
	 */
	public LogicalTimeInterval add( LogicalTime addend )
	{
		// check for null
		if( addend == null )
		{
			// no good!
			return null;
		}
		
		// cast and decrease
		try
		{
			double temp = this.time + ((DoubleTimeInterval)addend).getInterval();
			return new DoubleTimeInterval( temp );
		}
		catch( Exception ex )
		{
			return null;
		}
	}
	
	public String toString()
	{
		return "" + this.time;
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static byte[] encode( double value )
	{
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		DataOutputStream dataStream      = new DataOutputStream( byteStream );
		try
		{
			dataStream.writeDouble( value );
			dataStream.close();
			return byteStream.toByteArray();
		}
		catch (IOException ioe)
		{
			// bollocks! errrr, return an empty array and go hide in some dark corner.
			return new byte[0];
		}
	}
}
