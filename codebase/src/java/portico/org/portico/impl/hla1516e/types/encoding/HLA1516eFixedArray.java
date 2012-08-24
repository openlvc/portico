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
import hla.rti1516e.encoding.DataElementFactory;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderException;
import hla.rti1516e.encoding.HLAfixedArray;

import java.util.Iterator;
import java.util.List;

public class HLA1516eFixedArray<T extends DataElement>
       extends HLA1516eDataElement
       implements HLAfixedArray<T>
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected List<T> elements;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLA1516eFixedArray( T... provided )
	{
		for( T element : provided )
			this.elements.add( element );
	}

	/**
	 * Create a new fixed array of the provided size and prepopulate it with
	 * the identified number of T instances (using the factory) 
	 */
	public HLA1516eFixedArray( DataElementFactory<T> factory, int size )
	{
		for( int i = 0; i < size; i++ )
			elements.add( factory.createElement(i) );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns the number of elements in this fixed array.
	 * 
	 * @return the number of elements in this fixed array
	 */
	public int size()
	{
		return this.elements.size();
	}

	/**
	 * Returns the element at the specified <code>index</code>.
	 * 
	 * @param index index of element to get
	 * 
	 * @return the element at the specified <code>index</code>
	 */
	public T get( int index )
	{
		return this.elements.get( index );
	}

	/**
	 * Returns an iterator for the elements in this fixed array.
	 * 
	 * @return an iterator for the elements in this fixed array
	 */
	public Iterator<T> iterator()
	{
		return this.elements.iterator();
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// DataElement Methods //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int getOctetBoundary()
	{
		if( elements.isEmpty() )
			return 1; // can't return 0 or we'll have problems later... what to do!?
		else
			return elements.get(0).getOctetBoundary();
	}

	@Override
	public void encode( ByteWrapper byteWrapper ) throws EncoderException
	{
		
	}

	@Override
	public int getEncodedLength()
	{
		return -1;
	}

	@Override
	public byte[] toByteArray() throws EncoderException
	{
		return null;
	}

	@Override
	public void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		
	}

	@Override
	public void decode( byte[] bytes ) throws DecoderException
	{
		
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
