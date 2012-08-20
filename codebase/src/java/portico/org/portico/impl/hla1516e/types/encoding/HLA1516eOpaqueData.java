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

import hla.rti1516e.encoding.HLAopaqueData;

import java.util.Iterator;

public class HLA1516eOpaqueData extends HLA1516eDataElement implements HLAopaqueData
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns the number of bytes in this array.
	 * 
	 * @return the number of bytes in this array.
	 */
	public int size()
	{
		return -1;
	}

	/**
	 * Returns the <code>byte</code> at the specified position in this array.
	 * 
	 * @param index index of <code>byte</code> to return
	 * 
	 * @return <code>byte</code> at the specified index
	 */
	public byte get( int index )
	{
		return -1;
	}

	/**
	 * Returns an iterator over the bytes in this array in a proper sequence.
	 * 
	 * @return an iterator over the bytes in this array in a proper sequence
	 */
	public Iterator<Byte> iterator()
	{
		return null;
	}

	/**
	 * Returns the byte[] value of this element.
	 * 
	 * @return byte[] value
	 */
	public byte[] getValue()
	{
		return null;
	}

	/**
	 * Sets the byte[] value of this element.
	 * 
	 * @param value new value
	 */
	public void setValue( byte[] value )
	{
		
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
