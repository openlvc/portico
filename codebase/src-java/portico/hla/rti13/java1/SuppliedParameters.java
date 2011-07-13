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

import org.portico.impl.hla13.types.HLA13SuppliedParameters;

public class SuppliedParameters
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private HLA13SuppliedParameters parameters;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	protected SuppliedParameters()
	{
		this.parameters = new HLA13SuppliedParameters();
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void add( int h, byte[] buff ) throws ValueLengthExceeded, ValueCountExceeded
	{
		parameters.add( h, buff );
	}

	public void empty()
	{
		parameters.empty();
	}

	public int getHandle( int i ) throws ArrayIndexOutOfBounds
	{
		try
		{
			return this.parameters.getHandle( i );
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
			return this.parameters.getValue( i );
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
			return this.parameters.getValueLength( i );
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
			this.parameters.remove( h );
		}
		catch( hla.rti.ArrayIndexOutOfBounds e )
		{
			throw new ArrayIndexOutOfBounds( e.getMessage() );
		}
	}
	
	public int size()
	{
		return parameters.size();
	}
	
	public HLA13SuppliedParameters toPorticoMap()
	{
		return this.parameters;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
