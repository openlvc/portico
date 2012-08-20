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
import hla.rti1516e.encoding.HLAboolean;

public class HLA1516eBoolean extends HLA1516eDataElement implements HLAboolean
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean value;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLA1516eBoolean()
	{
		this.value = false;
	}

	public HLA1516eBoolean( boolean value )
	{
		this.value = value;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns the boolean value of this element.
	 * 
	 * @return value
	 */
	public boolean getValue()
	{
		return this.value;
	}

	/**
	 * Sets the boolean value of this element.
	 * 
	 * @param value new value
	 */
	public void setValue( boolean value )
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
		if( this.value )
			byteWrapper.put( 1 );
		else
			byteWrapper.put( 0 );
	}

	@Override
	public int getEncodedLength()
	{
		return 1;
	}

	@Override
	public byte[] toByteArray() throws EncoderException
	{
		return this.value ? new byte[]{1} : new byte[]{0};
	}

	@Override
	public void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		byte[] found = new byte[1];
		byteWrapper.get( found );
		decode( found );
	}

	@Override
	public void decode( byte[] bytes ) throws DecoderException
	{
		if( bytes[0] == 1 )
			this.value = true;
		else if( bytes[0] == 0 )
			this.value = false;
		else
			throw new DecoderException("Only valid values for boolean are 0 and 1, found: "+bytes[0]);
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
