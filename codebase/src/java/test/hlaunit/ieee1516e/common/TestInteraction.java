/*
 *   Copyright 2007 The Portico Project
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
package hlaunit.ieee1516e.common;

import java.util.HashMap;
import java.util.Map.Entry;

import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.InteractionParameterNotDefined;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
import hla.rti1516e.exceptions.InvalidParameterHandle;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIinternalError;

import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.impl.hla1516e.types.HLA1516eParameterHandleValueMap;

/**
 * This class represents a specific instance of an interaction. It contains a link to the parameters
 * that were sent with the interaction, in addition to other information (such as the timestamp,
 * if there was one).
 */
public class TestInteraction
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int classHandle;
	private HashMap<Integer,byte[]> parameters;
	private byte[] tag;
	private double timestamp;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public TestInteraction( int classHandle, ParameterHandleValueMap given, byte[] tag )
	{
		this.classHandle = classHandle;
		this.parameters = new HashMap<Integer,byte[]>();
		this.tag = tag;
		this.timestamp = -1.0;
		
		// get the parameters //
		try
		{
			this.parameters = HLA1516eParameterHandleValueMap.toJavaMap( given );
		}
		catch( Exception e )
		{
			// should never occur
		}
	}
	
	public TestInteraction( int classHandle,
	                        ParameterHandleValueMap parameters,
	                        byte[] tag,
	                        double timestamp )
	{
		this( classHandle, parameters, tag );
		this.timestamp = timestamp;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public int getClassHandle()
	{
		return this.classHandle;
	}
	
	public HashMap<Integer,byte[]> getParameters()
    {
    	return parameters;
    }
	
	public HashMap<String,byte[]> getParametersNamed( RTIambassador rtiamb )
		throws RTIinternalError, 
		       NotConnected, 
		       FederateNotExecutionMember, 
		       InvalidInteractionClassHandle,
		       InvalidParameterHandle,
		       InteractionParameterNotDefined
	{
		HashMap<String,byte[]> namedMap = new HashMap<>();
		HLA1516eHandle icHandle = new HLA1516eHandle( classHandle );
		for( Entry<Integer,byte[]> entry : this.parameters.entrySet() )
		{
			HLA1516eHandle pHandle = new HLA1516eHandle( entry.getKey() );
			String pName = rtiamb.getParameterName( icHandle, pHandle );
			namedMap.put( pName, entry.getValue() );
		}
		
		return namedMap;
	}
	
	public byte[] getParameterValue( int handle )
	{
		return parameters.get( handle );
	}

	public byte[] getTag()
    {
    	return tag;
    }

	public double getTimestamp()
    {
    	return timestamp;
    }

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
