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
package org.portico.lrc.model;

import org.portico.lrc.compat.JErrorReadingFED;

/**
 * Enumeration representing the order property of various FOM elements 
 */
public enum Order
{
	TIMESTAMP,
	RECEIVE;

	/**
	 * If the provided string matches (ignoring case) the name of either
	 * order type, that type is returned. Otherwise an exception is thrown
	 */
	public static Order fromFomString( String fomString ) throws JErrorReadingFED
	{
		if( fomString.equalsIgnoreCase("timestamp") )
			return TIMESTAMP;
		else if( fomString.equalsIgnoreCase("receive") )
			return RECEIVE;
		else
			throw new JErrorReadingFED( "Unsupported Order found: "+fomString );
	}
};
