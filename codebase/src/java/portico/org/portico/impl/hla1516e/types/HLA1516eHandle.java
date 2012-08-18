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

import org.portico.utils.bithelpers.BitHelpers;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.DimensionHandle;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.RegionHandle;

/**
 * Generic handle class. All the IEEE 1516e handle types provide the same interface.
 * Rather than signal that there should be just a single handle type, the specification
 * defines a bunch of independent interfaces (all the same, mercifully). As such, we
 * just provide a single implementation.
 * <p/>
 * Technically believe this <i>may</i> not be fully specification compliant, in that
 * you could call {@link #equals(Object)} on a handle of two separate types, and get
 * true if the underyling values are the same, such as in the following example:
 * <pre>
 * AttributeHandle ahandle = ...(1);
 * FederateHandle fhandle = ....(1);
 * assert ahandle.equals(fhandle); // would work, but should it...?   
 * </pre>
 * Ultimately, I don't care. So there.
 */
public class HLA1516eHandle implements AttributeHandle,
                                       DimensionHandle,
                                       FederateHandle,
                                       InteractionClassHandle,
                                       MessageRetractionHandle,
                                       ObjectClassHandle,
                                       ObjectInstanceHandle,
                                       ParameterHandle,
                                       RegionHandle
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int handle;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLA1516eHandle( int handle )
	{
		this.handle = handle;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public boolean equals( Object otherHandle )
	{
		if( otherHandle instanceof HLA1516eHandle )
			return ((HLA1516eHandle)otherHandle).handle == this.handle;
		else
			return false;
	}

	public int hashCode()
	{
		return this.handle;
	}

	public int encodedLength()
	{
		return 4;
	}
	
	public void encode( byte[] buffer, int offset )
	{
		BitHelpers.putInt( this.handle, buffer, offset );
	}

	public int decode( byte[] buffer, int offset )
	{
		return BitHelpers.readInt( buffer, offset );
	}

	
	public String toString()
	{
		return ""+handle;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Decode a generic handle. Should only pass one of the standard handle interfaces
	 * to this method, otherwise you'll get a class cast exception.
	 */
	public static <T> T decode( Class<T> standardType, byte[] buffer, int offset )
	{
		return standardType.cast( new HLA1516eHandle(BitHelpers.readInt(buffer,offset)) );
	}

}
