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

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HLA1516eFixedRecord()
	{
		this.elements = new ArrayList<DataElement>();
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
		if( dataElement != null )
			this.elements.add( dataElement );
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
		return this.elements.iterator();
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// DataElement Methods //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Not really sure what to do here. Will just figure out what the octet boundary of
	 * the biggest element is and return that.
	 */
	@Override
	public int getOctetBoundary()
	{
		int biggest = 1;
		for( DataElement element : elements )
		{
			if( element.getOctetBoundary() > biggest )
				biggest = element.getOctetBoundary();
		}
		
		return biggest;
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
