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
package hlaunit.hla13.common;

import java.io.Serializable;
import java.util.HashMap;

import hla.rti.ReceivedInteraction;
import hla.rti.Region;

/**
 * This class represents a specific instance of an interaction. It contains a link to the parameters
 * that were sent with the interaction, in addition to other information (such as the timestamp,
 * if there was one).
 */
public class Test13Interaction implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int classHandle;
	private HashMap<Integer,byte[]> parameters;
	private byte[] tag;
	private double timestamp;
	private Region region;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public Test13Interaction( int classHandle, ReceivedInteraction given, byte[] tag )
	{
		this.classHandle = classHandle;
		this.parameters = new HashMap<Integer,byte[]>();
		this.tag = tag;
		this.timestamp = -1.0;
		this.region = given.getRegion();
		
		// get the parameters //
		for( int i = 0; i < given.size(); i++ )
		{
			try
			{
				this.parameters.put( given.getParameterHandle(i), given.getValue(i) );
			}
			catch( Exception e )
			{
				// should never occur
			}
		}
	}
	
	public Test13Interaction( int classHandle,
	                          ReceivedInteraction parameters,
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
	
	public Region getRegion()
	{
		return this.region;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
