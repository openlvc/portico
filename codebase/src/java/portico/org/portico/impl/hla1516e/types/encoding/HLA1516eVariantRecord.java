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

import java.util.HashMap;
import java.util.Map;

import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.encoding.DataElement;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderException;
import hla.rti1516e.encoding.HLAvariantRecord;

public class HLA1516eVariantRecord<T extends DataElement> extends HLA1516eDataElement
       implements HLAvariantRecord<T>
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private T discriminant;
	private Map<T,DataElement> variants;
	
	// cache these values to speed up common calls
	private int maxBoundary;
	private int valueBoundary;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	HLA1516eVariantRecord()
	{
		this.discriminant = null;
		this.variants = new HashMap<>();

		this.maxBoundary = -1;
		this.valueBoundary = -1;
	}

	HLA1516eVariantRecord( T discriminant )
	{
		this();
		this.discriminant = discriminant;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Associates the data element for a specified discriminant.
	 * 
	 * @param discriminant discriminant to associate data element with
	 * @param dataElement data element to associate the discriminant with
	 */
	public void setVariant( T discriminant, DataElement dataElement )
	{
		this.variants.put( discriminant, dataElement );
		
		// variant changed - reset the counters
		this.maxBoundary = -1;
		this.valueBoundary = -1;
	}

	/**
	 * Sets the active discriminant.
	 * 
	 * @param discriminant active discriminant
	 */
	public void setDiscriminant( T discriminant )
	{
		this.discriminant = discriminant;
		if( this.discriminant != null )
			this.maxBoundary = -1; // reset counter so we recalculate on next fetch
	}

	/**
	 * Returns the active discriminant.
	 * 
	 * @return the active discriminant
	 */
	public T getDiscriminant()
	{
		return this.discriminant;
	}

	/**
	 * Returns element associated with the active discriminant.
	 * 
	 * @return value or null if there is no entry for the active discriminant
	 */
	public DataElement getValue()
	{
		return this.variants.get( this.discriminant );
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// DataElement Methods //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int getOctetBoundary()
	{
		if( this.discriminant == null )
			return -1;

		// the octet boundary of a HLAvariantRecord is the maximum of the octet boundary
		// of the discriminant and each of the possible alternatives. See section 4.13.9.2
		// of IEEE Std 1516-2010.2.
		//
		// Recalculate the boundaries if we need to.
		// This will trigger a potential recalculation of the value boundary as well.
		if( this.maxBoundary == -1 )
			this.maxBoundary = Math.max( discriminant.getOctetBoundary(), getValueBoundary() );

		return maxBoundary;
	}

	/**
	 * @return The max boundary for any variant we contain.
	 */
	private int getValueBoundary()
	{
		if( this.valueBoundary != -1 )
			return this.valueBoundary;
		
		int max = 0;
		for( DataElement element : this.variants.values() )
			max = Math.max( max, element.getOctetBoundary() );
		
		this.valueBoundary = max;
		return max;
	}
	
	@Override
	public void encode( ByteWrapper byteWrapper ) throws EncoderException
	{
		// validity checks
		if( this.discriminant == null )
			throw new EncoderException( "No discriminant set in HLAvariantRecord" );
		
		DataElement variant = this.variants.get( discriminant );
		if( variant == null )
			throw new EncoderException( "No variant set for active discrinimnant in HLAvariantRecord" );

		// write the discriminant
		byteWrapper.align( getOctetBoundary() );
		this.discriminant.encode( byteWrapper );
		
		// write the value
		byteWrapper.align( getValueBoundary() );
		variant.encode( byteWrapper );
	}

	@Override
	public int getEncodedLength()
	{
		// start with the value of the discriminant
		int length = this.discriminant.getEncodedLength();
		// padding
		int boundary = getValueBoundary();
		while( length % boundary != 0 )
			++length;
		
		DataElement value = getValue();
		if( value != null )
			length += value.getEncodedLength();
		
		return length;
	}
		
	@Override
	public byte[] toByteArray() throws EncoderException
	{
		ByteWrapper byteWrapper = new ByteWrapper( this.getEncodedLength() );
		this.encode( byteWrapper );

		return byteWrapper.array();
	}

	@Override
	public void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		if( this.discriminant == null )
			throw new DecoderException( "No space to decode the discriminant" );

		if( this.discriminant.getEncodedLength() > byteWrapper.remaining() )
			throw new DecoderException( "Not enough data in ByteWrapper to decode a discriminant" );

		// decode the discrinimnant
		byteWrapper.align( getOctetBoundary() );
		this.discriminant.decode( byteWrapper );
		
		// decode the variant/value
		byteWrapper.align( getValueBoundary() );
		DataElement value = getValue();
		if( value == null )
			throw new DecoderException( "Nothing to decode the variant value in to" );
		else
			value.decode( byteWrapper );
	}

	@Override
	public void decode( byte[] bytes ) throws DecoderException
	{
		ByteWrapper byteWrapper = new ByteWrapper( bytes );

		this.decode( byteWrapper );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
