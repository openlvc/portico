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

import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.encoding.DataElement;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderException;

/**
 * An immutable record of the number of instances of a particular object class.
 * <p/>
 * This class mirrors the HLAobjectClassBasedCount Fixed Record structure defined in the MOM 
 */
public class ObjectClassBasedCount implements DataElement
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int classHandle;
	private int count;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ObjectClassBasedCount( int classHandle, int count )
	{
		this.classHandle = classHandle;
		this.count = count;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public int getOctetBoundary()
	{
		return 4;
	}
	
	@Override
	public void encode( ByteWrapper byteWrapper ) throws EncoderException
	{
		HLA1516eHandle handle = new HLA1516eHandle( classHandle );
		handle.encode( byteWrapper );
		byteWrapper.putInt( count );
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
	public int getClassHandle()
	{
		return this.classHandle;
	}
	
	public int getCount()
	{
		return this.count;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
