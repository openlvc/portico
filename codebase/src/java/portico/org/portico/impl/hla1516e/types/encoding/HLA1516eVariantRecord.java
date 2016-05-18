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
import hla.rti1516e.encoding.HLAvariantRecord;

import java.beans.Encoder;

/**
 * Urgh. Can't even touch this for now. Will have to go back and read. Le sigh.
 */
public class HLA1516eVariantRecord<T extends DataElement>
       extends HLA1516eDataElement
       implements HLAvariantRecord<T>
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	T discriminant = null;
	java.util.Map<T, DataElement> variants = new java.util.HashMap<>();

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
    HLA1516eVariantRecord()
    {
    }

    HLA1516eVariantRecord(T discriminant)
    {
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
		this.variants.put(discriminant, dataElement);
	}

	/**
	 * Sets the active discriminant.
	 * 
	 * @param discriminant active discriminant
	 */
	public void setDiscriminant( T discriminant )
	{
	    this.discriminant = discriminant;
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
	 * @return value
	 */
	public DataElement getValue()
	{
        if (!this.variants.containsKey(this.discriminant)) {
            return null;
        }

        return this.variants.get(this.discriminant);
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// DataElement Methods //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int getOctetBoundary()
	{
		if (this.discriminant == null) {
            return 1;
        }

        int maxOctetBoundary = this.discriminant.getOctetBoundary();
        for (DataElement variant: this.variants.values()) {
            maxOctetBoundary = Math.max(maxOctetBoundary, variant.getOctetBoundary());
        }

        return maxOctetBoundary;
	}

	@Override
	public void encode( ByteWrapper byteWrapper ) throws EncoderException
	{
        if (this.discriminant == null) {
            throw new EncoderException("No discriminant set in HLAvariantRecord");
        }

        if (this.getEncodedLength() > byteWrapper.remaining()) {
            throw new EncoderException("Not enought space in ByteWrapper to encode HLAvariantRecord");
        }

        // ignoring padding for now
        this.discriminant.encode(byteWrapper);
        if (this.variants.containsKey(this.discriminant)) {
            DataElement variant = this.variants.get(this.discriminant);
            if (variant != null) {
                this.variants.get(this.discriminant).encode(byteWrapper);
            }
        } else {
            throw new EncoderException("Discriminant is unknown to this HLAvariantRecord");
        }
	}

	@Override
	public int getEncodedLength()
	{
		if (this.discriminant == null) {
            return 0;
        }

        int encodedLength = this.discriminant.getEncodedLength();
        if (this.variants.containsKey(this.discriminant)) {
            DataElement variant = this.variants.get(this.discriminant);
            if (variant != null) {
                encodedLength += this.variants.get(this.discriminant).getEncodedLength();
            }
        }

        return encodedLength;
	}

	@Override
	public byte[] toByteArray() throws EncoderException
	{
        ByteWrapper byteWrapper = new ByteWrapper(this.getEncodedLength());
        this.encode(byteWrapper);

		return byteWrapper.array();
	}

	@Override
	public void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
	    if (this.discriminant == null) {
            throw new DecoderException("No space to decode the discriminant");
        }

        if (this.discriminant.getEncodedLength() > byteWrapper.remaining()) {
            throw new DecoderException("Not enough data in ByteWrapper to decode a discriminant");
        }

        this.discriminant.decode(byteWrapper);

        if (this.variants.containsKey(this.discriminant)) {
            if (this.variants.get(this.discriminant).getEncodedLength() > byteWrapper.remaining()) {
                throw new DecoderException("Not enough data in ByteWrapper to decode variant associated with discriminant.");
            }
            this.variants.get(this.discriminant).decode(byteWrapper);
        } else {
            throw new DecoderException("Decoded discriminant is unknown to this HLAvariantRecord");
        }
	}

	@Override
	public void decode( byte[] bytes ) throws DecoderException
	{
        ByteWrapper byteWrapper = new ByteWrapper(bytes);

        this.decode(byteWrapper);
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
