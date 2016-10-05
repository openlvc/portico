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
import hla.rti1516e.encoding.HLAfloat32LE;

public class HLA1516eFloat32LE extends HLA1516eDataElement implements HLAfloat32LE
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private float value;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLA1516eFloat32LE()
	{
		this.value = Float.MIN_VALUE;
	}

	public HLA1516eFloat32LE( float value )
	{
		this.value = value;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns the float value of this element.
	 * 
	 * @return float value
	 */
	public float getValue()
	{
		return this.value;
	}

	/**
	 * Sets the float value of this element.
	 * 
	 * @param value new value
	 */
	public void setValue( float value )
	{
		this.value = value;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// DataElement Methods //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public final int getOctetBoundary()
	{
		return 4;
	}

	@Override
	public final int getEncodedLength()
	{
		return 4;
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
		byte[] buffer = new byte[4];
		BitHelpers.putFloatLE( value, buffer, 0 );
		return buffer;
	}

	@Override
	public final void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		super.checkForUnderflow( byteWrapper, 4 );
		
		byte[] buffer = new byte[4];
		byteWrapper.get( buffer );
		decode( buffer );
	}

	@Override
	public final void decode( byte[] bytes ) throws DecoderException
	{
		super.checkForUnderflow( bytes, 0, 4 );
		this.value = BitHelpers.readFloatLE( bytes, 0 );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
