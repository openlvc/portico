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
import hla.rti1516e.encoding.HLAvariableArray;

public class HLA1516eVariableArray<T extends DataElement>
       extends HLA1516eFixedArray<T>
       implements HLAvariableArray<T>
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
	public HLA1516eVariableArray( T... provided )
	{
		super( provided );
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
		elements.add( dataElement );
	}

	/**
	 * Resize the variable array to the <code>newSize</code>. Uses the
	 * <code>DataElementFactory</code> if new elements needs to be added.
	 * 
	 * @param newSize the new size
	 */
	public void resize( int newSize )
	{
		// we already back this with a list anyway, so just ignore
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// DataElement Methods //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
