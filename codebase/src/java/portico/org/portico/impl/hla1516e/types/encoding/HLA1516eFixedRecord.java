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

import hla.rti1516e.encoding.DataElement;
import hla.rti1516e.encoding.HLAfixedRecord;

import java.util.Iterator;

public class HLA1516eFixedRecord extends HLA1516eDataElement implements HLAfixedRecord
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
	 * Adds an element to this fixed record.
	 * 
	 * @param dataElement element to add
	 */
	public void add( DataElement dataElement )
	{
		
	}

	/**
	 * Returns the number of elements in this fixed record.
	 * 
	 * @return the number of elements in this fixed record
	 */
	public int size()
	{
		return -1;
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
		return null;
	}

	/**
	 * Returns an iterator for the elements in this fixed record.
	 * 
	 * @return an iterator for the elements in this fixed record.
	 */
	public Iterator<DataElement> iterator()
	{
		return null;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
