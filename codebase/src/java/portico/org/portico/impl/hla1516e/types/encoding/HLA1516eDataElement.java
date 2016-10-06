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
import hla.rti1516e.encoding.DataElement;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderException;

public abstract class HLA1516eDataElement implements DataElement
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns the octet boundary of this element.
	 * 
	 * @return the octet boundary of this element
	 */
	public abstract int getOctetBoundary();

	/**
	 * Encodes this element into the specified ByteWrapper.
	 * 
	 * @param byteWrapper destination for the encoded element
	 * 
	 * @throws EncoderException if the element can not be encoded
	 */
	public abstract void encode( ByteWrapper byteWrapper ) throws EncoderException;

	/**
	 * Returns the size in bytes of this element's encoding.
	 * 
	 * @return the size in bytes of this element's encoding
	 */
	public abstract int getEncodedLength();

	/**
	 * Returns a byte array with this element encoded.
	 * 
	 * @return byte array with encoded element
	 * 
	 * @throws EncoderException if the element can not be encoded
	 */
	public abstract byte[] toByteArray() throws EncoderException;

	/**
	 * Decodes this element from the ByteWrapper.
	 * 
	 * @param byteWrapper source for the decoding of this element
	 * 
	 * @throws DecoderException if the element can not be decoded
	 */
	public abstract void decode( ByteWrapper byteWrapper ) throws DecoderException;

	/**
	 * Decodes this element from the byte array.
	 * 
	 * @param bytes source for the decoding of this element
	 * 
	 * @throws DecoderException if the element can not be decoded
	 */
	public abstract void decode( byte[] bytes ) throws DecoderException;

	/////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////// Helper Methods /////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	protected final void verifyNotNull( Object buffer ) throws DecoderException
	{
		if( buffer == null )
			throw new DecoderException( "buffer was null" );
	}

	protected final void checkForUnderflow( byte[] buffer, int offset, int expected )
		throws DecoderException
	{
		if( buffer.length-offset < expected )
		{
			int remaining = buffer.length-offset;
			throw new DecoderException( "Buffer Underflow. Remaining="+remaining+"b, Expected="+
			                            expected+"b, Total Buffer="+buffer.length+"b" );
		}
	}
	
	protected final void checkForUnderflow( ByteWrapper wrapper, int expected )
		throws DecoderException
	{
		if( wrapper.remaining() < expected )
		{
			int total = wrapper.getPos()+1+wrapper.remaining();
			throw new DecoderException( "Buffer Underflow. Remaining="+wrapper.remaining()+
			                            "b, Expected="+expected+"b, Total Buffer="+total+"b" );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
