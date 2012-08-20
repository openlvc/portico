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
import hla.rti1516e.encoding.HLAvariantRecord;

public class HLA1516eVariantRecord
       extends HLA1516eDataElement
       implements HLAvariantRecord<HLA1516eDataElement>
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
	 * Associates the data element for a specified discriminant.
	 * 
	 * @param discriminant discriminant to associate data element with
	 * @param dataElement data element to associate the discriminant with
	 */
	public void setVariant( HLA1516eDataElement discriminant, DataElement dataElement )
	{
		
	}

	/**
	 * Sets the active discriminant.
	 * 
	 * @param discriminant active discriminant
	 */
	public void setDiscriminant( HLA1516eDataElement discriminant )
	{
		
	}

	/**
	 * Returns the active discriminant.
	 * 
	 * @return the active discriminant
	 */
	public HLA1516eDataElement getDiscriminant()
	{
		return null;
	}

	/**
	 * Returns element associated with the active discriminant.
	 * 
	 * @return value
	 */
	public DataElement getValue()
	{
		return null;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
