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
package org.portico.lrc.compat;

public enum JResignAction
{
	RELEASE_ATTRIBUTES(1),
	DELETE_OBJECTS(2),
	DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES(3),
	NO_ACTION(4);
	
	private int value;
	
	private JResignAction( int value )
	{
		this.value = value;
	}
	
	public int getValue()
	{
		return value;
	}
	
	public static JResignAction for13Value( int value ) throws JInvalidResignAction
	{
		switch( value )
		{
			case 1:
				return RELEASE_ATTRIBUTES;
			case 2:
				return DELETE_OBJECTS;
			case 3:
				return DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES;
			case 4:
				return NO_ACTION;
			default:
				throw new JInvalidResignAction( value + " is not a valid resign action value" );
		}
	}
	
	public static JResignAction for1516Value( int value ) throws JInvalidResignAction
	{
		switch( value )
		{
			case 1:
				return RELEASE_ATTRIBUTES;
			case 2:
				return DELETE_OBJECTS;
			case 3:
				return NO_ACTION; // CANCEL_PENDING_OWNERSHIP_ACQUISITIONS
			case 4:
				return DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES;
			case 5:
				return DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES; // CANCEL_THEN_DELETE_THEN_DIVEST
			case 6:
				return NO_ACTION;
			default:
				throw new JInvalidResignAction( value + " is not a valid resign action value" );
		}
	}
}
