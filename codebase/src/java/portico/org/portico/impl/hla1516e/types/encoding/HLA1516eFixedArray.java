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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HLA1516eFixedArray<T extends DataElement> extends HLA1516eDataElement
       implements HLAfixedArray<T>
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected List<T> elements;
	private int boundary;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLA1516eFixedArray( @SuppressWarnings("unchecked") T... provided )
	{
		this.elements = new ArrayList<T>( provided.length );
		for( T element : provided )
			this.elements.add( element );
		
		this.boundary = -1;
	}

	/**
	 * Create a new fixed array of the provided size and prepopulate it with
	 * the identified number of T instances (using the factory) 
	 */
	public HLA1516eFixedArray( DataElementFactory<T> factory, int size )
	{
		this.elements = new ArrayList<T>( size );
		for( int i = 0; i < size; i++ )
			elements.add( factory.createElement(i) );
		
		this.boundary = -1;
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
		this.boundary = -1;
		return this.elements.iterator();
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// DataElement Methods //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int getOctetBoundary()
	{
		if( this.boundary == -1 )
		{
    		// Return the size of the largest element
    		int maxSize = 1;
    		
    		for( T element : this.elements )
    			maxSize = Math.max( maxSize, element.getEncodedLength() );
    		
    		this.boundary = maxSize;
		}
		
		return this.boundary;
	}

	@Override
	public int getEncodedLength()
	{
		int length = 0;
		for( DataElement element : this.elements )
		{
			// put padding to the octet buondary before the element as required by encoding rules
			int boundary = element.getOctetBoundary();
			while( length % boundary != 0 )
				++length;
			
			length += element.getEncodedLength();
		}
		
		return length;
	}

	@Override
	public void encode( ByteWrapper byteWrapper ) throws EncoderException
	{
		byteWrapper.align( getOctetBoundary() );
		for( DataElement element : elements )
			element.encode( byteWrapper );
	}

	@Override
	public void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		byteWrapper.align( getOctetBoundary() );
		for( DataElement element : this.elements )
			element.decode( byteWrapper );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
