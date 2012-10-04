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

/**
 * Enumeration representing the transport of the various FOM types 
 */
public enum Transport
{
	RELIABLE,
	BEST_EFFORT;

	/**
	 * If the given FOM string is "HLAreliable", RELIABLE is returned. If it is "HLAbestEffort"
	 * then BEST_ERRORT is returned. If the type is not known, we just use best effort as Portico
	 * basically ignores the transport type anyway.
	 */
	public static Transport fromFomString( String fomString )
	{
		if( fomString.equalsIgnoreCase("HLAreliable") )
			return RELIABLE;
		else
			return BEST_EFFORT;
	}
};
