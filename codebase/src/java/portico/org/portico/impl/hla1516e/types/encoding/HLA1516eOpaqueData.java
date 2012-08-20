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

import org.portico.utils.bithelpers.BitHelpers;

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
		return 4;
	}

	@Override
	public final int getEncodedLength()
	{
		return 4+value.length;
	}

	@Override
	public final void encode( ByteWrapper byteWrapper ) throws EncoderException
	{
		byteWrapper.put( toByteArray() );
	}

	@Override
	public final byte[] toByteArray() throws EncoderException
	{
		byte[] buffer = new byte[4+value.length];
		BitHelpers.putIntBE( value.length, buffer, 0 );
		BitHelpers.putByteArray( value, buffer, 4 );
		return buffer;
	}

	@Override
	public final void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		int length = byteWrapper.getInt();
		byte[] buffer = new byte[length];
		byteWrapper.get( buffer );
	}

	@Override
	public final void decode( byte[] bytes ) throws DecoderException
	{
		int length = BitHelpers.readIntBE( bytes, 0 );
		this.value = BitHelpers.readByteArray( bytes, 4, length );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
