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
package org.portico.impl.hla1516.types;

import hla.rti1516.AttributeHandle;
import hla.rti1516.FederateHandle;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.InvalidAttributeHandle;
import hla.rti1516.InvalidFederateHandle;
import hla.rti1516.InvalidInteractionClassHandle;
import hla.rti1516.InvalidObjectClassHandle;
import hla.rti1516.InvalidParameterHandle;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.ParameterHandle;
import hla.rti1516.RTIinternalError;

import org.portico.utils.bithelpers.BitHelpers;

public class HLA1516Handle
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected int handle;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public HLA1516Handle( int handle )
	{
		this.handle = handle;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
     * @return true if this refers to the same federate as other handle
     */
	public boolean equals( Object other )
	{
		if( this.getClass().isInstance(other) )
		{
			return (this.getClass().cast(other)).handle == this.handle;
		}
		else
		{
			return false;
		}
	}

	/**
     * @return int. All instances that refer to the same federate should return the same hashcode.
     */
	public int hashCode()
	{
		return handle;
	}

	public int encodedLength()
	{
		return 8;
	}

	public void encode( byte[] buffer, int offset )
	{
		// Length is required to be compatible with HLAvariableArray<HLAbyte>
		BitHelpers.putIntBE( 4, buffer, offset );	
		BitHelpers.putIntBE( this.handle, buffer, offset + 4 );
	}

	public String toString()
	{
		return "" + handle;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static int fromHandle( AttributeHandle handle ) throws RTIinternalError
	{
		if( handle instanceof HLA1516AttributeHandle )
		{
			return ((HLA1516Handle)handle).handle;
		}
		else
		{
			throw new RTIinternalError( "Expecting HLA1516AttributeHandle, found: " +
			                            handle.getClass() );
		}
	}
	
	public static int fromHandle( FederateHandle handle ) throws RTIinternalError
	{
		if( handle instanceof HLA1516FederateHandle )
		{
			return ((HLA1516Handle)handle).handle;
		}
		else
		{
			throw new RTIinternalError( "Expecting HLA1516FederateHandle, found: " +
			                            handle.getClass() );
		}
	}
	
	public static int fromHandle( ObjectClassHandle handle ) throws RTIinternalError
	{
		if( handle instanceof HLA1516ObjectClassHandle )
		{
			return ((HLA1516Handle)handle).handle;
		}
		else
		{
			throw new RTIinternalError( "Expecting HLA1516ObjectClassHandle, found: " +
			                            handle.getClass() );
		}
	}
	
	public static int fromHandle( InteractionClassHandle handle ) throws RTIinternalError
	{
		if( handle instanceof HLA1516InteractionClassHandle )
		{
			return ((HLA1516Handle)handle).handle;
		}
		else
		{
			throw new RTIinternalError(
			    "Expecting HLA1516InteractionClassHandle, found: " + handle.getClass() );
		}
	}
	
	public static int fromHandle( ParameterHandle handle ) throws RTIinternalError
	{
		if( handle instanceof HLA1516ParameterHandle )
		{
			return ((HLA1516Handle)handle).handle;
		}
		else
		{
			throw new RTIinternalError( "Expecting HLA1516ParameterHandle, found: " +
			                            handle.getClass() );
		}
	}
	
	public static int fromHandle( ObjectInstanceHandle handle ) throws RTIinternalError
	{
		if( handle instanceof HLA1516ObjectInstanceHandle )
		{
			return ((HLA1516Handle)handle).handle;
		}
		else
		{
			throw new RTIinternalError( 
			    "Expecting HLA1516ObjectInstanceHandle, found: " + handle.getClass() );
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	public static int validatedHandle( AttributeHandle handle ) throws InvalidAttributeHandle
	{
		if( handle instanceof HLA1516AttributeHandle )
		{
			return ((HLA1516Handle)handle).handle;
		}
		else
		{
			throw new InvalidAttributeHandle( "Expecting HLA1516AttributeHandle, found: " +
			                                  handle.getClass() );
		}
	}
	
	public static int validatedHandle( FederateHandle handle ) throws InvalidFederateHandle
	{
		if( handle instanceof HLA1516FederateHandle )
		{
			return ((HLA1516Handle)handle).handle;
		}
		else
		{
			throw new InvalidFederateHandle( "Expecting HLA1516FederateHandle, found: " +
			                                 handle.getClass() );
		}
	}
	
	public static int validatedHandle( ObjectClassHandle handle ) throws InvalidObjectClassHandle
	{
		if( handle instanceof HLA1516ObjectClassHandle )
		{
			return ((HLA1516Handle)handle).handle;
		}
		else
		{
			throw new InvalidObjectClassHandle( "Expecting HLA1516ObjectClassHandle, found: " +
			                                    handle.getClass() );
		}
	}
	
	public static int validatedHandle( InteractionClassHandle handle )
		throws InvalidInteractionClassHandle
	{
		if( handle instanceof HLA1516InteractionClassHandle )
		{
			return ((HLA1516Handle)handle).handle;
		}
		else
		{
			throw new InvalidInteractionClassHandle(
			    "Expecting HLA1516InteractionClassHandle, found: " + handle.getClass() );
		}
	}
	
	public static int validatedHandle( ParameterHandle handle ) throws InvalidParameterHandle
	{
		if( handle instanceof HLA1516ParameterHandle )
		{
			return ((HLA1516Handle)handle).handle;
		}
		else
		{
			throw new InvalidParameterHandle( "Expecting HLA1516ParameterHandle, found: " +
			                                  handle.getClass() );
		}
	}
	
	public static int validatedHandle( ObjectInstanceHandle handle ) throws RTIinternalError
	{
		if( handle instanceof HLA1516ObjectInstanceHandle )
		{
			return ((HLA1516Handle)handle).handle;
		}
		else
		{
			throw new RTIinternalError( 
			    "Expecting HLA1516ObjectInstanceHandle, found: " + handle.getClass() );
		}
	}
}
