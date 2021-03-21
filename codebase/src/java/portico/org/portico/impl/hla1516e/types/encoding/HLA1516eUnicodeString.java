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
		return 4;
	}

	@Override
	public int getEncodedLength()
	{
		return 4 + (getBytes().length*2);
	}

	@Override
	public void encode( ByteWrapper byteWrapper ) throws EncoderException
	{
		byteWrapper.align( getOctetBoundary() );
		byteWrapper.putInt( this.value.length() );
		
		for( int i = 0; i < value.length(); i++ )
		{
			// faster just to do this by hand rather than use BitHelpers which will create an array
			char temp = value.charAt( i );
			byteWrapper.put( temp >>> 8 & 0xFF );
			byteWrapper.put( temp >>> 0 & 0xFF );
		}
	}
	

	@Override
	public void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		byteWrapper.align( getOctetBoundary() );

		// get the length of the string and make sure there is enough space
		int length = byteWrapper.get();
		byteWrapper.verify( length*2 );
		
		// loop through and get each 16-bit char
		char[] chars = new char[length];
		for( int i = 0; i < length; i++ )
		{
			int firstOctet = byteWrapper.get();
			int secondOctet = byteWrapper.get();
			chars[i] = (char)((firstOctet << 8) + (secondOctet << 0));
		}

		this.value = new String( chars );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
