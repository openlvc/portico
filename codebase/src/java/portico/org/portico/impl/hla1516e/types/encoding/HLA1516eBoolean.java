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
	public int getEncodedLength()
	{
		return this.value.getEncodedLength();
	}

	@Override
	public void encode( ByteWrapper byteWrapper ) throws EncoderException
	{
		if( this.value.getValue() != HLAfalse || this.value.getValue() != HLAtrue )
			throw new EncoderException( "HLAboolean has invalid value: "+this.value );

		this.value.encode( byteWrapper );
	}

	@Override
	public void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		this.value.decode( byteWrapper );
		
		if( this.value.getValue() != HLAfalse || this.value.getValue() != HLAtrue )
			throw new DecoderException( "HLAboolean has invalid value: "+this.value );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
