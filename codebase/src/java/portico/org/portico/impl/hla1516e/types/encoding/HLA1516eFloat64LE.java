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
package org.portico.impl.hla1516e.types.encoding;

import org.portico.utils.bithelpers.BitHelpers;

import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderException;
import hla.rti1516e.encoding.HLAfloat64LE;

public class HLA1516eFloat64LE extends HLA1516eDataElement implements HLAfloat64LE
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private double value;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLA1516eFloat64LE()
	{
		this.value = Double.MIN_VALUE;
	}

	public HLA1516eFloat64LE( double value )
	{
		this.value = value;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns the double value of this element.
	 * 
	 * @return double value
	 */
	public double getValue()
	{
		return this.value;
	}

	/**
	 * Sets the double value of this element.
	 * 
	 * @param value new value
	 */
	public void setValue( double value )
	{
		this.value = value;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// DataElement Methods //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public final int getOctetBoundary()
	{
		return 8;
	}

	@Override
	public final int getEncodedLength()
	{
		return 8;
	}

	@Override
	public final void encode( ByteWrapper byteWrapper ) throws EncoderException
	{
		try
		{
			byteWrapper.put( toByteArray() );
		}
		catch( Exception e )
		{
			throw new EncoderException( e.getMessage(), e );
		}
	}

	@Override
	public final byte[] toByteArray() throws EncoderException
	{
		byte[] buffer = new byte[8];
		BitHelpers.putDoubleLE( value, buffer, 0 );
		return buffer;
	}

	@Override
	public final void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		super.checkForUnderflow( byteWrapper, 8 );
		
		byte[] buffer = new byte[8];
		byteWrapper.get( buffer );
		decode( buffer );
	}

	@Override
	public final void decode( byte[] bytes ) throws DecoderException
	{
		super.checkForUnderflow( bytes, 0, 8 );
		this.value = BitHelpers.readDoubleLE( bytes, 0 );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
