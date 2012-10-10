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
import hla.rti1516e.encoding.HLAunicodeString;

public class HLA1516eUnicodeString extends HLA1516eDataElement implements HLAunicodeString
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final String CHARSET = "UTF-16";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String value;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLA1516eUnicodeString()
	{
		this.value = "";
	}

	public HLA1516eUnicodeString( String value )
	{
		setValue( value );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns the string value of this element.
	 * 
	 * @return string value
	 */
	public String getValue()
	{
		return this.value;
	}

	/**
	 * Sets the string value of this element.
	 * 
	 * @param value new value
	 */
	public void setValue( String value )
	{
		if( value == null )
			this.value = "null";
		else
			this.value = value;
	}

	public byte[] getBytes() throws EncoderException
	{
		try
		{
			// NOTE: String.getBytes("UTF-16") returns a byte array with the Unicode BOM at the
			// start (0xfe, 0xff). We are currently including this in our String data.
			return this.value.getBytes( CHARSET );
		}
		catch( Exception e )
		{
			throw new EncoderException( e.getMessage(), e );
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// DataElement Methods //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int getOctetBoundary()
	{
		return 4 + getBytes().length;
	}

	@Override
	public int getEncodedLength()
	{
		return 4 + getBytes().length;
	}

	@Override
	public void encode( ByteWrapper byteWrapper ) throws EncoderException
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
	public byte[] toByteArray() throws EncoderException
	{
		byte[] bytes = getBytes();
		byte[] buffer = new byte[4+bytes.length];
		BitHelpers.putIntBE( bytes.length, buffer, 0 );
		BitHelpers.putByteArray( bytes, buffer, 4 );
		return buffer;
	}

	@Override
	public void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		try
		{
    		int length = byteWrapper.getInt();
    		byte[] buffer = new byte[length];
    		byteWrapper.get( buffer );
    		
    		this.value = new String( buffer, CHARSET );
		}
		catch( Exception e )
		{
			throw new DecoderException( e.getMessage(), e );
		}
	}

	@Override
	public void decode( byte[] bytes ) throws DecoderException
	{
		try
		{
    		int length = BitHelpers.readIntBE( bytes, 0 );
    		byte[] stringBytes = BitHelpers.readByteArray( bytes, 4, length );
		
			this.value = new String( stringBytes, CHARSET );
		}
		catch( Exception e )
		{
			throw new DecoderException( e.getMessage(), e );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
