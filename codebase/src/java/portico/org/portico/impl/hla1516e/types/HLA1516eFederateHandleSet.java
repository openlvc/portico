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

import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.exceptions.RTIinternalError;

import java.util.HashSet;

public class HLA1516eFederateHandleSet
       extends HashSet<FederateHandle>
       implements FederateHandleSet
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
	public static HashSet<Integer> toJavaSet( FederateHandleSet set ) throws RTIinternalError
	{
		try
		{
			HashSet<Integer> realSet = new HashSet<Integer>();
			for( FederateHandle handle : set )
			{
				realSet.add( ((HLA1516eHandle)handle).handle );
			}
			
			return realSet;
		}
		catch( Exception e )
		{
			throw new RTIinternalError( "Can't convert FederateHandleSet to Portico native type: " +
			                            e.getMessage() , e );
		}
	}
}
