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
import hla.rti1516e.encoding.HLAopaqueData;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class HLA1516eOpaqueData extends HLA1516eDataElement implements HLAopaqueData
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	public byte[] value;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLA1516eOpaqueData()
	{
		this.setValue( new byte[0] );
	}

	public HLA1516eOpaqueData( byte[] value )
	{
		this.setValue( value );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns the number of bytes in this array.
	 * 
	 * @return the number of bytes in this array.
	 */
	public int size()
	{
		return this.value.length;
	}

	/**
	 * Returns the <code>byte</code> at the specified position in this array.
	 * 
	 * @param index index of <code>byte</code> to return
	 * 
	 * @return <code>byte</code> at the specified index
	 */
	public byte get( int index )
	{
		return this.value[index];
	}

	/**
	 * Returns an iterator over the bytes in this array in a proper sequence.
	 * 
	 * @return an iterator over the bytes in this array in a proper sequence
	 */
	public Iterator<Byte> iterator()
	{
		return new Iterator<Byte>()
		{
			private int index = 0;
			public boolean hasNext()
			{
				return index+1 < value.length;
			}
			
			public Byte next()
			{
				if( hasNext() == false )
					throw new NoSuchElementException( "Past end of the iterator: index=" + index );
				
				index++;
				return value[index];
			}
			
			public void remove() throws UnsupportedOperationException
			{
				throw new UnsupportedOperationException( "Removal not supported" );
			}
		};
	}

	/**
	 * Returns the byte[] value of this element.
	 * 
	 * @return byte[] value
	 */
	public byte[] getValue()
	{
		return this.value;
	}

	/**
	 * Sets the byte[] value of this element.
	 * 
	 * @param value new value
	 */
	public void setValue( byte[] value )
	{
		if( value == null )
			this.value = new byte[0];
		else
			this.value = value;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// DataElement Methods //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public final int getOctetBoundary()
	{
		return 4 + value.length;
	}

	@Override
	public final int getEncodedLength()
	{
		return 4 + value.length;
	}

	@Override
	public final void encode( ByteWrapper byteWrapper ) throws EncoderException
	{
		if( byteWrapper.remaining() < getEncodedLength() )
			throw new EncoderException( "Insufficient space remaining in buffer to encode this value" );
		
		byteWrapper.putInt( this.value.length );
		byteWrapper.put( this.value );
	}

	@Override
	public final byte[] toByteArray() throws EncoderException
	{
		// Encode into a byte wrapper
		ByteWrapper byteWrapper = new ByteWrapper( getEncodedLength() );
		this.encode( byteWrapper );
		
		// Return underlying array
		return byteWrapper.array();
	}

	@Override
	public final void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		super.checkForUnderflow( byteWrapper, 4 );
		int length = byteWrapper.getInt();
		super.checkForUnderflow( byteWrapper, length );
		this.value = new byte[length];
		byteWrapper.get( this.value );
	}

	@Override
	public final void decode( byte[] bytes ) throws DecoderException
	{
		ByteWrapper byteWrapper = new ByteWrapper( bytes );
		this.decode( byteWrapper );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
