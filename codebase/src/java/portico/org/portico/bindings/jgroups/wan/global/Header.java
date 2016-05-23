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
package org.portico.bindings.jgroups.wan.global;

public class Header
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final byte WELCOME            = 1;
	public static final byte READY              = 2;
	public static final byte FIND_COORD         = 3;
	public static final byte SET_MANIFEST       = 4;
	public static final byte RELAY              = 5;
	public static final byte CREATE_FEDERATION  = 6;
	public static final byte JOIN_FEDERATION    = 7;
	public static final byte RESIGN_FEDERATION  = 8;
	public static final byte DESTROY_FEDERATION = 9;
	
	public static final byte BUNDLE             = 127;

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
	 * For the given `value` representing a header, return a descriptive name.
	 */
	public static final String toString( byte value )
	{
		switch( value )
		{
			case 1: return "WELCOME";
			case 2: return "READY";
			case 3: return "FIND_COORD";
			case 4: return "SET_MANIFEST";
			case 5: return "RELAY";
			case 6: return "CREATE_FEDERATION";
			case 7: return "JOIN_FEDERATION";
			case 8: return "RESIGN_FEDERATION";
			case 9: return "DESTROY_FEDERATION";
			case 127: return "BUNDLE";
			default: return "UNKNOWN";
		}
	}
}
