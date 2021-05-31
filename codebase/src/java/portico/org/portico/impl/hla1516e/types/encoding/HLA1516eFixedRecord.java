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
import hla.rti1516e.encoding.HLAfixedRecord;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HLA1516eFixedRecord extends HLA1516eDataElement implements HLAfixedRecord
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private List<DataElement> elements;
	private int boundary;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLA1516eFixedRecord()
	{
		this.elements = new ArrayList<DataElement>();
		this.boundary = -1;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Adds an element to this fixed record.
	 * 
	 * @param dataElement element to add
	 */
	public void add( DataElement dataElement )
	{
		this.elements.add( dataElement );
		this.boundary = -1;
	}

	/**
	 * Returns the number of elements in this fixed record.
	 * 
	 * @return the number of elements in this fixed record
	 */
	public int size()
	{
		return this.elements.size();
	}

	/**
	 * Returns element at the specified index.
	 * 
	 * @param index index of element to get
	 * 
	 * @return the element at the specified <code>index</code>
	 */
	public DataElement get( int index )
	{
		return this.elements.get( index );
	}

	/**
	 * Returns an iterator for the elements in this fixed record.
	 * 
	 * @return an iterator for the elements in this fixed record.
	 */
	public Iterator<DataElement> iterator()
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
			int temp = 1; // minimum boundary is 1
			for( DataElement dataElement : this.elements )
				temp = Math.max( temp, dataElement.getOctetBoundary() ); 

			this.boundary = temp;
		}
		
		return this.boundary;
	}

	@Override
	public int getEncodedLength()
	{
		int length = 0;
		for( DataElement element : this.elements )
		{
			// get the boundary of the element and make sure we pad out to it
			int boundary = element.getOctetBoundary();
			while( length % boundary != 0 )
				length++;
			
			length += element.getEncodedLength();
		}

		return length;
	}

	@Override
	public void encode( ByteWrapper byteWrapper ) throws EncoderException
	{
		byteWrapper.align( getOctetBoundary() );
		for( DataElement element : this.elements )
			element.encode( byteWrapper );
	}

	@Override
	public void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		byteWrapper.align( getOctetBoundary() );
		for( DataElement dataElement : this.elements )
			dataElement.decode( byteWrapper );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
