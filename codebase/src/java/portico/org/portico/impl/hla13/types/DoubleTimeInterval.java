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

import hla.rti.LogicalTimeInterval;

/**
 * Implementation of the DLC (for 1.3) LogicalTimeInterval interface. See {@link DoubleTime
 * DoubleTime} for a rant on this. 
 */
public class DoubleTimeInterval implements LogicalTimeInterval
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final double ZERO    = Double.valueOf(0.0);
	public static final double EPSILON = Double.valueOf(0.0001);
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private double interval;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public DoubleTimeInterval()
	{
		this.interval = ZERO;
	}
	
	public DoubleTimeInterval( double interval )
	{
		this.interval = interval;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////
	//////////////////////// Non-Standard Compliant Methods ////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	public double getInterval()
	{
		return this.interval;
	}
	
	public void setInterval( double interval )
	{
		this.interval = interval;
	}
	
	////////////////////////////////////////////////////////////////////////////////
	//////////////////////// Logical Time Interval Methods /////////////////////////
	////////////////////////////////////////////////////////////////////////////////
	public void encode( byte[] buffer, int offset )
	{
		byte[] leBytes = DoubleTime.encode( interval );
		// copy the bytes across to the given buffer
		System.arraycopy( leBytes, 0, buffer, offset, leBytes.length );
	}

	public int encodedLength()
	{
		return DoubleTime.encode(interval).length;
	}

	public boolean isEpsilon()
	{
		return interval == EPSILON;
	}

	public boolean isEqualTo( LogicalTimeInterval value )
	{
		// check for null
		if( value == null )
		{
			return false;
		}
		
		// try and find out, possible class cast
		try
		{
			return interval == ((DoubleTimeInterval)value).interval;
		}
		catch( Exception ex )
		{
			// doh! error, return false
			return false;
		}
	}

	public boolean isGreaterThan( LogicalTimeInterval value )
	{
		// check for null
		if( value == null )
		{
			return false;
		}
		
		// try and find out, possible class cast
		try
		{
			return interval > ((DoubleTimeInterval)value).interval;
		}
		catch( Exception ex )
		{
			// doh! error, return false
			return false;
		}
	}

	public boolean isGreaterThanOrEqualTo( LogicalTimeInterval value )
	{
		// check for null
		if( value == null )
		{
			return false;
		}
		
		// try and find out, possible class cast
		try
		{
			return interval >= ((DoubleTimeInterval)value).interval;
		}
		catch( Exception ex )
		{
			// doh! error, return false
			return false;
		}
	}

	public boolean isLessThan( LogicalTimeInterval value )
	{
		// check for null
		if( value == null )
		{
			return false;
		}
		
		// try and find out, possible class cast
		try
		{
			return interval < ((DoubleTimeInterval)value).interval;
		}
		catch( Exception ex )
		{
			// doh! error, return false
			return false;
		}
	}

	public boolean isLessThanOrEqualTo( LogicalTimeInterval value )
	{
		// check for null
		if( value == null )
		{
			return false;
		}
		
		// try and find out, possible class cast
		try
		{
			return interval <= ((DoubleTimeInterval)value).interval;
		}
		catch( Exception ex )
		{
			// doh! error, return false
			return false;
		}
	}

	public boolean isZero()
	{
		return interval == ZERO;
	}

	public void setEpsilon()
	{
		this.interval = EPSILON;
	}

	public void setTo( LogicalTimeInterval value )
	{
		// check for null
		if( value == null )
		{
			return;
		}
		
		// try and find out, possible class cast
		try
		{
			this.interval = ((DoubleTimeInterval)value).interval;
		}
		catch( Exception ex )
		{
			// doh! error, ignore request
			return;
		}
	}

	public void setZero()
	{
		this.interval = ZERO;
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
