/*
 *   Copyright 2012 The Portico Project
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
package org.portico.impl.hla1516e.types;

import hla.rti1516e.ResignAction;
import hla.rti1516e.exceptions.InvalidResignAction;

import org.portico.lrc.compat.JResignAction;

/**
 * This class does not implement any standard interface in relation to resign actions,
 * rather it just provides a utility conversion method to take a 1516e resign action and
 * turn it into the resign action from the compatiblity layer: {@link JResignAction}
 */
public class HLA1516eResignAction
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
	 * Convert from a 1516e resign action to the compatibility layer resign action. Throws
	 * an exception if the action is not known.
	 */
	public static JResignAction fromResignAction( ResignAction action ) throws InvalidResignAction
	{
		switch( action )
		{
			case UNCONDITIONALLY_DIVEST_ATTRIBUTES:
				return JResignAction.RELEASE_ATTRIBUTES;
			case DELETE_OBJECTS:
				return JResignAction.DELETE_OBJECTS;
			case CANCEL_PENDING_OWNERSHIP_ACQUISITIONS:
				return JResignAction.NO_ACTION;
			case DELETE_OBJECTS_THEN_DIVEST:
				return JResignAction.DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES;
			case CANCEL_THEN_DELETE_THEN_DIVEST:
				return JResignAction.DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES;
			case NO_ACTION:
				return JResignAction.NO_ACTION;
			default:
				throw new InvalidResignAction( "Unknown resign action: "+action );
		}
	}
}
