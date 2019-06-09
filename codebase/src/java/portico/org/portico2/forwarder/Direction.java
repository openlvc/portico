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
package org.portico2.forwarder;

/** The "direction" that a connection will pass messages. */
public enum Direction
{
	//----------------------------------------------------------
	//                        VALUES
	//----------------------------------------------------------
	Upstream, Downstream;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public String toString() { return name().toLowerCase(); }
	public String flowDirection() { return this == Upstream ? "upstream<<<downstream" : "upstream>>>downstream"; }
	public Direction reverse() { return this == Upstream ? Downstream : Upstream; }

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
