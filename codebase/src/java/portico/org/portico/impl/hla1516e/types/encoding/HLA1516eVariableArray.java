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

public class HLA1516eVariableArray<T extends DataElement> extends HLA1516eDataElement
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
	private int boundary;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLA1516eVariableArray( DataElementFactory<T> factory, @SuppressWarnings("unchecked") T... provided )
	{
		this.factory = factory;
		this.elements = new ArrayList<T>( provided.length );
		this.boundary = -1;
		
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
		this.boundary = -1;
	}

	/**
	 * Resize the variable array to the <code>newSize</code>. Uses the
	 * <code>DataElementFactory</code> if new elements needs to be added.
	 * 
	 * @param newSize the new size
	 */
	public void resize( int newSize )
	{
		if( newSize < elements.size() )
		{
			while( newSize < elements.size() )
				elements.remove( elements.size()-1 );
		}
		else if( newSize > elements.size() )
		{
			while( newSize > elements.size() )
				elements.add( (T)factory.createElement(elements.size()) );
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
			int calculated = 4;
			for( DataElement dataElement : this.elements )
				calculated = Math.max( calculated, dataElement.getOctetBoundary() );

			// if the list if empty we need to create the default type of element
			// and pull the boundary from there - but minimum will be 4
			if( this.elements.isEmpty() )
				calculated = Math.max( calculated, factory.createElement(0).getOctetBoundary() );

			this.boundary = calculated;
		}

		return this.boundary;
	}
	
	@Override
    public int getEncodedLength()
	{
		int length = 4;
		for( DataElement dataElement : this.elements )
		{
			// pad out to the octet boundary
			while( length % dataElement.getOctetBoundary() != 0 )
				++length;
			
			length += dataElement.getEncodedLength();
		}

		return length;
	}


	@Override
    public void encode( ByteWrapper byteWrapper ) throws EncoderException
	{
		byteWrapper.align( getOctetBoundary() );
		byteWrapper.putInt( this.elements.size() );
		for( DataElement dataElement : this.elements )
			dataElement.encode( byteWrapper );
	}

	@Override
	public void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		byteWrapper.align( getOctetBoundary() );
		
		// get the size and make sure there is enough data to feed us
		int size = byteWrapper.getInt();
		byteWrapper.verify( size ); // this ain't right - size is length; not size. Refactor.
		
		// if for some reason we don't have a factory then everything will have gone to poo, UNLESS
		// the arrays happen to be exactly the same size
		if( this.factory == null && this.elements.size() < size )
			throw new DecoderException( "We have wrong number of elements and no factory we can use to fix this" );

		resize( size );
		for( DataElement dataElement : this.elements )
			dataElement.decode( byteWrapper );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
