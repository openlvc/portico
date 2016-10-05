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

import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderException;
import hla.rti1516e.encoding.HLAASCIIchar;

public class HLA1516eASCIIchar extends HLA1516eDataElement implements HLAASCIIchar
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private byte value;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLA1516eASCIIchar()
	{
		this.value = Byte.MIN_VALUE;
	}

	public HLA1516eASCIIchar( byte value )
	{
		this.value = value;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns the byte value of this element.
	 * 
	 * @return value current value
	 */
	public byte getValue()
	{
		return this.value;
	}

	/**
	 * Sets the byte value of this element.
	 * 
	 * @param value new value
	 */
	public void setValue( byte value )
	{
		this.value = value;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// DataElement Methods //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int getOctetBoundary()
	{
		return 1;
	}

	@Override
	public void encode( ByteWrapper byteWrapper ) throws EncoderException
	{
		try
		{
			byteWrapper.put( this.value );
		}
		catch( ArrayIndexOutOfBoundsException aioobe )
		{
			// The ByteWrapper class can throw an ArrayIndexOutOfBoundsException, so repackage
			// it as an EncoderException
			throw new EncoderException( aioobe.getMessage(), aioobe );
		}
	}

	@Override
	public int getEncodedLength()
	{
		return 1;
	}

	@Override
	public byte[] toByteArray() throws EncoderException
	{
		return new byte[]{ this.value };
	}

	@Override
	public void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		checkForUnderflow( byteWrapper, 1 );
		this.value = (byte)byteWrapper.get();
	}

	@Override
	public void decode( byte[] bytes ) throws DecoderException
	{
		checkForUnderflow( bytes, 0, 1 );
		this.value = bytes[0];
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
