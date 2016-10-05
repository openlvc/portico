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
import hla.rti1516e.encoding.HLAoctet;

public class HLA1516eOctet extends HLA1516eDataElement implements HLAoctet
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
	public HLA1516eOctet()
	{
		this.value = Byte.MIN_VALUE;
	}

	public HLA1516eOctet( byte value )
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
		if( byteWrapper.remaining() < this.getEncodedLength() )
			throw new EncoderException( "Insufficient space remaining in buffer to encode this value" );
		
		byteWrapper.put( this.value );
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
		super.checkForUnderflow( byteWrapper, 1 );
		this.value = (byte)byteWrapper.get();
	}

	@Override
	public void decode( byte[] bytes ) throws DecoderException
	{
		super.checkForUnderflow( bytes, 0, 1 );
		this.value = bytes[0];
	}

	/**
	 * hashCode is required so that HLA1516eOctet can be used as a key in java.util.HashMap, which
	 * is used by HLA1516eVariantRecord to store discriminant/variant pairs.
	 */
	@Override
	public int hashCode()
	{
		return this.getValue();
	}

	/**
	 * equals is required so that HLA1516eOctet can be used as a key in java.util.HashMap, which
	 * is used by HLA1516eVariantRecord to store discriminant/variant pairs.
	 */
	@Override
	public boolean equals( Object other )
	{
		return (this == other) || ((other != null) &&
		                          (other instanceof HLA1516eOctet) &&
		                          (this.getValue() == ((HLA1516eOctet)other).getValue()));
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
