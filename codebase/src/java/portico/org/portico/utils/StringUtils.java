/*
 *   Copyright 2015 The Portico Project
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
package org.portico.utils;

public class StringUtils
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

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Convert the given size (in bytes) to a more human readable string. Returned values
	 * will be in the form: "16B", "16KB", "16MB", "16GB".
	 */
	public static String getSizeString( long size )
	{
		return getSizeString( size, 2 );
	}
	
	/**
	 * Convert the given size (in bytes) to a more human readable string. Returned values
	 * will be in the form: "16B", "16KB", "16MB", "16GB".
	 */
	public static String getSizeString( long bytes, int decimalPlaces )
	{
		// let's see how much we have so we can figure out the right qualifier
		double totalkb = bytes / 1000;
		double totalmb = totalkb / 1000;
		double totalgb = totalmb / 1000;
		if( totalgb >= 1 )
			return String.format( "%4."+decimalPlaces+"fGB", totalgb );
		else if( totalmb >= 1 )
			return String.format( "%4."+decimalPlaces+"fMB", totalmb );
		else if( totalkb >= 1 )
			return String.format( "%4."+decimalPlaces+"fKB", totalkb );
		else
			return bytes+"b";
	}
}
