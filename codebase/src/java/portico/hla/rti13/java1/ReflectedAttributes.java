/*
 *   Copyright 2008 The Portico Project
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
package hla.rti13.java1;

import java.util.HashMap;

import org.portico.impl.hla13.types.HLA13ReflectedAttributes;
import org.portico.impl.hla13.types.Java1Region;
import org.portico.lrc.model.RegionInstance;
import org.portico2.common.services.object.msg.UpdateAttributes.FilteredAttribute;

public class ReflectedAttributes
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private HLA13ReflectedAttributes attributes;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	protected ReflectedAttributes()
	{
		
	}
	
	public ReflectedAttributes( HashMap<Integer,FilteredAttribute> params )
	{
		this.attributes = new HLA13ReflectedAttributes( params );
	}

	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void empty()
	{
		attributes.empty();
	}

	public int getHandle( int i ) throws ArrayIndexOutOfBounds
	{
		try
		{
			return attributes.getHandle( i );
		}
		catch( Exception e )
		{
			throw new ArrayIndexOutOfBounds( e.getMessage() );
		}
	}

	public int getOrderType( int i ) throws ArrayIndexOutOfBounds, InvalidHandleValuePairSetContext
	{
		try
		{
			return attributes.getOrderType( i );
		}
		catch( hla.rti.ArrayIndexOutOfBounds e )
		{
			throw new ArrayIndexOutOfBounds( e.getMessage() );
		}
	}

	public Region getRegion( int i ) throws ArrayIndexOutOfBounds, InvalidHandleValuePairSetContext
	{
		try
		{
			RegionInstance rawRegion = attributes.getRawRegion(i);
			if( rawRegion == null )
				return null;
			else
				return new Java1Region( rawRegion );
		}
		catch( hla.rti.ArrayIndexOutOfBounds e )
		{
			throw new ArrayIndexOutOfBounds( e.getMessage() );
		}
	}

	public int getTransportType( int i ) throws ArrayIndexOutOfBounds,
	                                            InvalidHandleValuePairSetContext
	{
		try
		{
			return attributes.getTransportType( i );
		}
		catch( hla.rti.ArrayIndexOutOfBounds e )
		{
			throw new ArrayIndexOutOfBounds( e.getMessage() );
		}
	}

	public byte[] getValue( int i ) throws ArrayIndexOutOfBounds
	{
		try
		{
			return attributes.getValue( i );
		}
		catch( hla.rti.ArrayIndexOutOfBounds e )
		{
			throw new ArrayIndexOutOfBounds( e.getMessage() );
		}
	}

	public int getValueLength( int i ) throws ArrayIndexOutOfBounds
	{
		try
		{
			return attributes.getValueLength( i );
		}
		catch( hla.rti.ArrayIndexOutOfBounds e )
		{
			throw new ArrayIndexOutOfBounds( e.getMessage() );
		}
	}

	public int size()
	{
		return attributes.size();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
