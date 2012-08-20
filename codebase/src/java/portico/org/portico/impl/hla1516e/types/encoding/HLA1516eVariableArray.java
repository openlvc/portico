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

import hla.rti1516e.encoding.HLAvariableArray;

import java.util.Iterator;

public class HLA1516eVariableArray
       extends HLA1516eDataElement
       implements HLAvariableArray<HLA1516eDataElement>
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
	 * Adds an element to this variable array.
	 * 
	 * @param dataElement element to add
	 */
	public void addElement( HLA1516eDataElement dataElement )
	{
		
	}

	/**
	 * Returns the number of elements in this variable array.
	 * 
	 * @return the number of elements in this variable array
	 */
	public int size()
	{
		return -1;
	}

	/**
	 * Returns the element at the specified <code>index</code>.
	 * 
	 * @param index index of element to get
	 * 
	 * @return the element at the specified <code>index</code>
	 */
	public HLA1516eDataElement get( int index )
	{
		return null;
	}

	/**
	 * Returns an iterator for the elements in this variable array.
	 * 
	 * @return an iterator for the elements in this variable array
	 */
	public Iterator<HLA1516eDataElement> iterator()
	{
		return null;
	}

	/**
	 * Resize the variable array to the <code>newSize</code>. Uses the
	 * <code>DataElementFactory</code> if new elements needs to be added.
	 * 
	 * @param newSize the new size
	 */
	public void resize( int newSize )
	{
		
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
