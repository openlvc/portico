/*
 *   Copyright 2017 The Portico Project
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
package org.portico.lrc.model.datatype;

import org.portico.lrc.compat.JConfigurationException;

/**
 * Describes the byte ordering of {@link BasicType} datatypes.
 */
public enum Endianness
{
	/**
	 * Least significant byte first
	 */
	LITTLE, 
	/**
	 * Most significant byte first
	 */
	BIG;
	
	@Override
	public String toString()
	{
		switch( this )
		{
			case LITTLE:
				return "Little";
			default:
			case BIG:
				return "Big";
		}
	}
	
	/**
	 * If the provided string matches (ignoring case) the name of either
	 * endianness type, that type is returned. Otherwise an exception is thrown
	 */
	public static Endianness fromFomString( String fomString ) throws JConfigurationException
	{
		if( fomString.equalsIgnoreCase("little") )
			return LITTLE;
		else if( fomString.equalsIgnoreCase("big") )
			return BIG;
		else
			throw new JConfigurationException( "Unsupported Endianness found: "+fomString );
	}
}
