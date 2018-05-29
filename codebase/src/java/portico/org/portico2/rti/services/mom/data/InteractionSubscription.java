/*
 *   Copyright 2018 The Portico Project
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
package org.portico2.rti.services.mom.data;

import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.impl.hla1516e.types.encoding.HLA1516eBoolean;

import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.encoding.DataElement;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderException;

/**
 * Represents a federate's subscription to an interaction.
 * <p/>
 * This class mirrors the HLAinteractionSubscription Fixed Record structure defined in the MOM 
 */
public class InteractionSubscription implements DataElement
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int interactionClass;
	private boolean active;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public InteractionSubscription( int interactionClass )
	{
		this( interactionClass, true );
	}
	
	public InteractionSubscription( int interactionClass, boolean active )
	{
		this.interactionClass = interactionClass;
		this.active = active;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public int getOctetBoundary()
	{
		return HLA1516eHandle.EncodedLength;
	}
	
	@Override
	public void encode( ByteWrapper byteWrapper ) throws EncoderException
	{
		new HLA1516eHandle( this.interactionClass ).encode( byteWrapper );
		new HLA1516eBoolean( this.active ).encode( byteWrapper );
	}
	
	@Override
	public int getEncodedLength()
	{
		return HLA1516eHandle.EncodedLength + 4;
	}
	
	@Override
	public byte[] toByteArray() throws EncoderException
	{
		ByteWrapper wrapper = new ByteWrapper( getEncodedLength() );
		this.encode( wrapper );
		return wrapper.array();
	}
	
	@Override
	public void decode( ByteWrapper byteWrapper ) throws DecoderException
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void decode( byte[] bytes ) throws DecoderException
	{
		throw new UnsupportedOperationException();
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
