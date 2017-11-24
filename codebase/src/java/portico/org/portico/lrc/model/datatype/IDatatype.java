/*
 *   Copyright 2017 The Portico Project
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
package org.portico.lrc.model.datatype;

import org.portico.lrc.model.datatype.linker.DatatypePlaceholder;

/**
 * Common interface for all FOM datatypes.
 */
public interface IDatatype
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns the name of this datatype.
	 */
	public String getName();
	
	/**
	 * Returns the FOM datatype class of this datatype (e.g. Basic, Simple, Enumerated, Array, 
	 * Fixed Record or Variant).
	 */
	public DatatypeClass getDatatypeClass();
	
	/**
	 * Creates a copy of this datatype with any dependent datatypes it references replaced by a 
	 * {@link DatatypePlaceholder}.
	 * <p/>
	 * This method is used by the model merger while it imports extension datatypes into a base 
	 * model. As dependent datatypes may not have been imported at the time this datatype is 
	 * imported into the base model, the placeholder is used as a temporary reference. After all
	 * extension datatypes have been imported into the base model, all placeholder types will
	 * be resolved to their actual representations.
	 * 
	 * @return a copy of this datatype replaced with a {@link DatatypePlaceholder}
	 */
	public IDatatype createUnlinkedClone();
}
