/*
 *   Copyright 2018 The Portico Project
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
package org.portico2.common.utils;

import org.portico.utils.StringUtils;

public enum ByteUnit
{
	//----------------------------------------------------------
	//                        VALUES
	//----------------------------------------------------------
	BYTES
	{
		public long   toBytes(long value)      { return value; }
		public double toKilobytes(long value)  { return value / ONE; } 
		public double toMegabytes(long value)  { return value / TWO; } 
		public double toGigabytes(long value)  { return value / THREE; } 
		public double toTerabytes(long value)  { return value / FOUR; }

		public long   toBytes(double value)    { return (long)(value); }
		public long   toKilobytes(double value){ return (long)(value/ONE); }
		public long   toMegabytes(double value){ return (long)(value/TWO); } 
		public long   toGigabytes(double value){ return (long)(value/THREE); } 
		public long   toTerabytes(double value){ return (long)(value/FOUR); } 
	},
	
	KILOBYTES
	{
		public long   toBytes(long value)      { return value * (long)ONE; }
		public double toKilobytes(long value)  { return value; }
		public double toMegabytes(long value)  { return value / ONE; } 
		public double toGigabytes(long value)  { return value / TWO; } 
		public double toTerabytes(long value)  { return value / THREE; } 

		public long   toBytes(double value)    { return (long)(value * ONE); }
		public long   toKilobytes(double value){ return (long)value; }
		public long   toMegabytes(double value){ return (long)(value/ONE); } 
		public long   toGigabytes(double value){ return (long)(value/TWO); } 
		public long   toTerabytes(double value){ return (long)(value/THREE); } 
	},
	
	MEGABYTES
	{
		public long   toBytes(long value)      { return value * (long)TWO; }
		public double toKilobytes(long value)  { return value * (long)ONE; } 
		public double toMegabytes(long value)  { return value; } 
		public double toGigabytes(long value)  { return value / ONE; } 
		public double toTerabytes(long value)  { return value / TWO; } 

		public long   toBytes(double value)    { return (long)(value * TWO); }
		public long   toKilobytes(double value){ return (long)(value * ONE); }
		public long   toMegabytes(double value){ return (long)(value); } 
		public long   toGigabytes(double value){ return (long)(value/ONE); } 
		public long   toTerabytes(double value){ return (long)(value/TWO); } 
	},
	
	GIGABYTES
	{
		public long   toBytes(long value)      { return value * (long)THREE; }
		public double toKilobytes(long value)  { return value * (long)TWO; } 
		public double toMegabytes(long value)  { return value * (long)ONE; } 
		public double toGigabytes(long value)  { return value; } 
		public double toTerabytes(long value)  { return value / ONE; } 

		public long   toBytes(double value)    { return (long)(value * THREE); }
		public long   toKilobytes(double value){ return (long)(value * TWO); }
		public long   toMegabytes(double value){ return (long)(value * ONE); } 
		public long   toGigabytes(double value){ return (long)(value); } 
		public long   toTerabytes(double value){ return (long)(value/ONE); } 
	},
	
	TERABYTES
	{
		public long   toBytes(long value)      { return value * (long)FOUR; }
		public double toKilobytes(long value)  { return value * (long)THREE; } 
		public double toMegabytes(long value)  { return value * (long)TWO; } 
		public double toGigabytes(long value)  { return value * (long)ONE; } 
		public double toTerabytes(long value)  { return value; } 

		public long   toBytes(double value)    { return (long)(value * FOUR); }
		public long   toKilobytes(double value){ return (long)(value * THREE); }
		public long   toMegabytes(double value){ return (long)(value * TWO); } 
		public long   toGigabytes(double value){ return (long)(value / ONE); } 
		public long   toTerabytes(double value){ return (long)(value); } 
	};

	// Constants
	private static final double ONE    = 1000.0d;
	private static final double TWO    = 1000000.0d;
	private static final double THREE  = 1000000000000.0d;
	private static final double FOUR   = 1000000000000000000000000.0d;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public long   toBytes(long value)      { throw new AbstractMethodError(); }
	public double toKilobytes(long value)  { throw new AbstractMethodError(); } 
	public double toMegabytes(long value)  { throw new AbstractMethodError(); }
	public double toGigabytes(long value)  { throw new AbstractMethodError(); }
	public double toTerabytes(long value)  { throw new AbstractMethodError(); }
	public String toString(long value)     { return StringUtils.humanReadableSize(toBytes(value)); }

	public long   toBytes(double value)    { throw new AbstractMethodError(); }
	public long   toKilobytes(double value){ throw new AbstractMethodError(); } 
	public long   toMegabytes(double value){ throw new AbstractMethodError(); }
	public long   toGigabytes(double value){ throw new AbstractMethodError(); }
	public long   toTerabytes(double value){ throw new AbstractMethodError(); }
	public String toString(double value)   { return StringUtils.humanReadableSize(toBytes(value)); }

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}

