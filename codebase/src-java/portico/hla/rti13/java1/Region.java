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
package hla.rti13.java1;

public class Region
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

	protected Region()
	{
		// nothing stored in here, all instances of this class should really be instances of
		// its subclass org.portico.impl.hla13.types.Java1Region
	}

//	protected Region( hla.rti.Region region )
//	{
//		throw new IllegalArgumentException( "You can't construct Regions this way with Portico" );
//	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public long getNumberOfExtents()
	{
		return 0;
	}

	public long getRangeLowerBound( int theExtent, int theDimension ) throws ArrayIndexOutOfBounds
	{
		return 0;
	}

	public long getRangeLowerBoundNotificationLimit( int theExtent, int theDimension )
	    throws ArrayIndexOutOfBounds
	{
		return 0;
	}

	public long getRangeUpperBound( int theExtent, int theDimension )
	    throws ArrayIndexOutOfBounds
	{
		return 0;
	}

	public long getRangeUpperBoundNotificationLimit( int theExtent, int theDimension )
	    throws ArrayIndexOutOfBounds
	{
		return 0;
	}

	public int getSpaceHandle()
	{
		return 0;
	}

	public void setRangeLowerBound( int theExtent, int theDimension, long theLowerBound )
	    throws ArrayIndexOutOfBounds
	{
	}

	public void setRangeUpperBound( int theExtent, int theDimension, long theUpperBound )
	    throws ArrayIndexOutOfBounds
	{
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
