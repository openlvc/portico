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

import org.portico.impl.hla13.types.HLA13SuppliedAttributes;

public class SuppliedAttributes
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private HLA13SuppliedAttributes attributes;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	protected SuppliedAttributes()
	{
		this.attributes = new HLA13SuppliedAttributes();
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void add( int h, byte[] buff ) throws ValueLengthExceeded, ValueCountExceeded
	{
		this.attributes.add( h, buff );
	}

	public void empty()
	{
		this.attributes.empty();
	}

	public int getHandle( int i ) throws ArrayIndexOutOfBounds
	{
		try
		{
			return this.attributes.getHandle( i );
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
			return this.attributes.getValue( i );
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
			return this.attributes.getValueLength( i );
		}
		catch( hla.rti.ArrayIndexOutOfBounds e )
		{
			throw new ArrayIndexOutOfBounds( e.getMessage() );
		}
	}

	public void remove( int h ) throws ArrayIndexOutOfBounds
	{
		try
		{
			this.attributes.remove( h );
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
	
	public HLA13SuppliedAttributes toPorticoMap()
	{
		return this.attributes;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
