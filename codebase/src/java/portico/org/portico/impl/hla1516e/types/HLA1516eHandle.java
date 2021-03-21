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

import org.portico.lrc.model.ObjectModel;
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
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.exceptions.InvalidAttributeHandle;
import hla.rti1516e.exceptions.InvalidFederateHandle;
import hla.rti1516e.exceptions.InvalidInteractionClassHandle;
import hla.rti1516e.exceptions.InvalidObjectClassHandle;
import hla.rti1516e.exceptions.InvalidParameterHandle;
import hla.rti1516e.exceptions.RTIinternalError;

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
                                       RegionHandle,
                                       TransportationTypeHandle
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final int EncodedLength = 8;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected int handle;

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
		return EncodedLength;
	}
	
	public void encode( byte[] buffer, int offset )
	{
		// Length is required to be compatible with HLAvariableArray<HLAbyte>
		BitHelpers.putIntBE( 4, buffer, offset );	
		BitHelpers.putIntBE( this.handle, buffer, offset + 4 );
	}

	public void encode( ByteWrapper wrapper )
	{
		// Length is required to be compatible with HLAvariableArray<HLAbyte>
		wrapper.putInt( 4 );
		wrapper.putInt( this.handle );
	}
	
	public void decode( byte[] buffer, int offset )
	{
		BitHelpers.readIntBE( buffer, offset );
		this.handle = BitHelpers.readIntBE( buffer, offset + 4 );
	}

	public byte[] getBytes()
	{
		byte[] buffer = new byte[EncodedLength];
		encode( buffer, 0 );
		return buffer;
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
		HLA1516eHandle handle = new HLA1516eHandle( ObjectModel.INVALID_HANDLE );
		handle.decode( buffer, offset );
		return standardType.cast( handle );
	}

	public static int decode( byte[] buffer )
	{
		HLA1516eHandle handle = new HLA1516eHandle( ObjectModel.INVALID_HANDLE );
		handle.decode( buffer, 0 );
		return handle.handle;
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Attribute Handle Conversion ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	public static final int fromHandle( AttributeHandle handle )
	{
		return ((HLA1516eHandle)handle).handle;
	}
	
	public static final int fromHandle( DimensionHandle handle )
	{
		return ((HLA1516eHandle)handle).handle;
	}
	
	public static final int fromHandle( FederateHandle handle )
	{
		return ((HLA1516eHandle)handle).handle;
	}
	
	public static final int fromHandle( InteractionClassHandle handle )
	{
		return ((HLA1516eHandle)handle).handle;
	}
	
	public static final int fromHandle( MessageRetractionHandle handle )
	{
		return ((HLA1516eHandle)handle).handle;
	}
	
	public static final int fromHandle( ObjectClassHandle handle )
	{
		return ((HLA1516eHandle)handle).handle;
	}
	
	public static final int fromHandle( ObjectInstanceHandle handle )
	{
		return ((HLA1516eHandle)handle).handle;
	}
	
	public static final int fromHandle( ParameterHandle handle )
	{
		return ((HLA1516eHandle)handle).handle;
	}
	
	public static final int fromHandle( RegionHandle handle )
	{
		return ((HLA1516eHandle)handle).handle;
	}
	
	public static final int fromHandle( TransportationTypeHandle handle )
	{
		return ((HLA1516eHandle)handle).handle;
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// Handle Conversion with Validation ////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	public static int validatedHandle( AttributeHandle handle ) throws InvalidAttributeHandle
	{
		if( handle instanceof HLA1516eHandle )
		{
			return ((HLA1516eHandle)handle).handle;
		}
		else
		{
			throw new InvalidAttributeHandle( "Expecting HLA1516eHandle, found: " +
			                                  handle.getClass() );
		}
	}
	
	public static int validatedHandle( FederateHandle handle ) throws InvalidFederateHandle
	{
		if( handle instanceof HLA1516eHandle )
		{
			return ((HLA1516eHandle)handle).handle;
		}
		else
		{
			throw new InvalidFederateHandle( "Expecting HLA1516eHandle, found: " +
			                                 handle.getClass() );
		}
	}
	
	public static int validatedHandle( ObjectClassHandle handle ) throws InvalidObjectClassHandle
	{
		if( handle instanceof HLA1516eHandle )
		{
			return ((HLA1516eHandle)handle).handle;
		}
		else
		{
			throw new InvalidObjectClassHandle( "Expecting HLA1516eHandle, found: " +
			                                    handle.getClass() );
		}
	}
	
	public static int validatedHandle( InteractionClassHandle handle )
		throws InvalidInteractionClassHandle
	{
		if( handle instanceof HLA1516eHandle )
		{
			return ((HLA1516eHandle)handle).handle;
		}
		else
		{
			throw new InvalidInteractionClassHandle( "Expecting HLA1516eHandle, found: " +
			                                         handle.getClass() );
		}
	}
	
	public static int validatedHandle( ParameterHandle handle ) throws InvalidParameterHandle
	{
		if( handle instanceof HLA1516eHandle )
		{
			return ((HLA1516eHandle)handle).handle;
		}
		else
		{
			throw new InvalidParameterHandle( "Expecting HLA1516eHandle, found: " +
			                                  handle.getClass() );
		}
	}
	
	public static int validatedHandle( ObjectInstanceHandle handle ) throws RTIinternalError
	{
		if( handle instanceof HLA1516eHandle )
		{
			return ((HLA1516eHandle)handle).handle;
		}
		else
		{
			throw new RTIinternalError( "Expecting HLA1516eHandle, found: " + 
			                            handle.getClass() );
		}
	}
}
