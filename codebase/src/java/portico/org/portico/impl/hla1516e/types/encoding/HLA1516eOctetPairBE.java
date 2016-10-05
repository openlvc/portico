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
import hla.rti1516e.encoding.HLAoctetPairBE;

public class HLA1516eOctetPairBE extends HLA1516eDataElement implements HLAoctetPairBE
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private short value;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLA1516eOctetPairBE()
	{
		this.value = Short.MIN_VALUE;
	}

	public HLA1516eOctetPairBE( short value )
	{
		this.value = value;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns the short value of this element.
	 * 
	 * @return short value
	 */
	public short getValue()
	{
		return this.value;
	}

	/**
	 * Sets the short value of this element.
	 * 
	 * @param value New value.
	 */
	public void setValue( short value )
	{
		this.value = value;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// DataElement Methods //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public final int getOctetBoundary()
	{
		return 2;
	}

	@Override
	public final int getEncodedLength()
	{
		return 2;
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
		byte[] buffer = new byte[2];
		BitHelpers.putShortBE( value, buffer, 0 );
		return buffer;
	}

	@Override
	public final void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		super.checkForUnderflow( byteWrapper, 2 );
		byte[] buffer = new byte[2];
		byteWrapper.get( buffer );
		decode( buffer );
	}

	@Override
	public final void decode( byte[] bytes ) throws DecoderException
	{
		super.checkForUnderflow( bytes, 0, 2 );
		this.value = BitHelpers.readShortBE( bytes, 0 );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
