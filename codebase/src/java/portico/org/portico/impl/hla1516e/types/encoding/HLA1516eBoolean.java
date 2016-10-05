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
import hla.rti1516e.encoding.HLAboolean;

public class HLA1516eBoolean extends HLA1516eDataElement implements HLAboolean
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final int HLAtrue = 0x01;
	private static final int HLAfalse = 0x00;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private HLA1516eInteger32BE value;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLA1516eBoolean()
	{
		this.value = new HLA1516eInteger32BE( HLAfalse );
	}

	public HLA1516eBoolean( boolean value )
	{
		this();
		this.setValue( value );
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
		return this.value.getValue() == HLAtrue;
	}

	/**
	 * Sets the boolean value of this element.
	 * 
	 * @param value new value
	 */
	public void setValue( boolean value )
	{
		int valueAsInt = value ? HLAtrue : HLAfalse;
		this.value.setValue( valueAsInt );
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// DataElement Methods //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int getOctetBoundary()
	{
		return this.value.getOctetBoundary();
	}

	@Override
	public void encode( ByteWrapper byteWrapper ) throws EncoderException
	{
		this.value.encode( byteWrapper );
	}

	@Override
	public int getEncodedLength()
	{
		return this.value.getEncodedLength();
	}

	@Override
	public byte[] toByteArray() throws EncoderException
	{
		return this.value.toByteArray();
	}

	@Override
	public void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		this.value.decode( byteWrapper );
	}

	@Override
	public void decode( byte[] bytes ) throws DecoderException
	{
		try
		{
			int candidateValue = BitHelpers.readIntBE( bytes, 0 );
			if( candidateValue == HLAtrue || candidateValue == HLAfalse )
				this.value.setValue( candidateValue );
			else
				throw new DecoderException("Only valid values for boolean are 0 and 1, found: "+candidateValue);
		}
		catch( DecoderException de )
		{
			throw de;
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
