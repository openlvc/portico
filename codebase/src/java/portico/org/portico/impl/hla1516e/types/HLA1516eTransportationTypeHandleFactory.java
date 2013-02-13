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

import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.TransportationTypeHandleFactory;
import hla.rti1516e.exceptions.CouldNotDecode;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIinternalError;

public class HLA1516eTransportationTypeHandleFactory implements TransportationTypeHandleFactory
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final TransportationTypeHandle RELIABLE = new HLA1516eHandle(1);
	public static final TransportationTypeHandle BEST_EFFORT = new HLA1516eHandle(2);

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public TransportationTypeHandle decode( byte[] buffer, int offset )
		throws CouldNotDecode, FederateNotExecutionMember, NotConnected, RTIinternalError
	{
		return HLA1516eHandle.decode( TransportationTypeHandle.class, buffer, offset );
	}

	public TransportationTypeHandle getHLAdefaultReliable() throws RTIinternalError
	{
		return HLA1516eTransportationTypeHandleFactory.RELIABLE;
	}

	public TransportationTypeHandle getHLAdefaultBestEffort() throws RTIinternalError
	{
		return HLA1516eTransportationTypeHandleFactory.BEST_EFFORT;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static TransportationTypeHandle fromString( String string )
	{
		if( string.equals("BEST_EFFORT") )
			return BEST_EFFORT;
		else
			return RELIABLE;
	}

}
