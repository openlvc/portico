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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.encoding.DataElement;
import hla.rti1516e.encoding.DataElementFactory;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderException;
import hla.rti1516e.encoding.HLAvariableArray;

public class HLA1516eVariableArray<T extends DataElement>
       extends HLA1516eDataElement
       implements HLAvariableArray<T>
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private DataElementFactory<T> factory;
	private List<T> elements;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLA1516eVariableArray( DataElementFactory<T> factory, @SuppressWarnings("unchecked") T... provided )
	{
		this.factory = factory;
		this.elements = new ArrayList<T>( provided.length );
		
		for( T element : provided )
			this.elements.add( element );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Adds an element to this variable array.
	 * 
	 * @param dataElement element to add
	 */
	public void addElement( T dataElement )
	{
		this.elements.add( dataElement );
	}

	/**
	 * Resize the variable array to the <code>newSize</code>. Uses the
	 * <code>DataElementFactory</code> if new elements needs to be added.
	 * 
	 * @param newSize the new size
	 */
	public void resize( int newSize )
	{
		int existingSize = this.elements.size();
		if( newSize > existingSize )
		{
			// Up-sizing to a larger capacity, so make up the difference using elements created
			// from the provided factory
			int deltaSize = newSize - existingSize;
			for( int i = 0 ; i < deltaSize ; ++i )
				this.elements.add( this.factory.createElement(existingSize + i) );
		}
		else if ( newSize < existingSize )
		{
			// Down-sizing to a smaller capacity, so cull items from the end of the list 
			while( this.elements.size() > newSize )
				this.elements.remove( this.elements.size() - 1 );
		}
	}

	public int size()
    {
	    return this.elements.size();
    }

	public T get( int index )
    {
	    return this.elements.get( index );
    }

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
		// Return the size of the largest element
		int maxSize = 1;
		
		for( T element : this.elements )
			maxSize = Math.max( maxSize, element.getEncodedLength() );
		
		return maxSize;
    }

	@Override
    public void encode( ByteWrapper byteWrapper )
        throws EncoderException
    {
		if( byteWrapper.remaining() < this.getEncodedLength() )
			throw new EncoderException( "Insufficient space remaining in buffer to encode this value" );
		
		// Write the number of elements encoded
		byteWrapper.putInt( this.elements.size() );
		
		// Write the elements
		for( T element : this.elements )
			element.encode( byteWrapper );
    }

	@Override
    public int getEncodedLength()
    {
		int length = 4;
		
		for( T element : this.elements )
			length += element.getEncodedLength();
		
	    return length;
    }

	@Override
    public byte[] toByteArray()
        throws EncoderException
    {
		// Create a ByteWrapper to encode into
		int length = this.getEncodedLength();
		ByteWrapper byteWrapper = new ByteWrapper( length );
		this.encode( byteWrapper );
		
		// Return the underlying array
	    return byteWrapper.array();
    }

	@Override
    public void decode( ByteWrapper byteWrapper )
        throws DecoderException
    {
		// Make sure we have at least the minimum we need to read
		super.checkForUnderflow( byteWrapper, 4 );
		int size = byteWrapper.getInt();
		
		// Clear the underlying collection so that it's ready to receive the new values
		this.elements.clear();
		
		for( int i = 0 ; i < size ; ++i )
		{
			// Create a new element to house the new value and read it in from the byte wrapper
			T element = this.factory.createElement( i );
			element.decode( byteWrapper );
			
			// Add the new element to the collection
			this.elements.add( element );
		}
		
    }

	@Override
    public void decode( byte[] bytes )
        throws DecoderException
    {
		// Wrap the byte array in a ByteWrapper to decode from
		ByteWrapper byteWrapper = new ByteWrapper( bytes );
		this.decode( byteWrapper );
    }

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
