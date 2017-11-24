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

/**
 * Describes a possible value of an {@link EnumeratedType}.
 * <p/>
 * According to the specification, the value of an {@link EnumeratedType} can be any 
 * {@link BasicType}. As a result, we'll use the {@link Number} class to represent it, as that is 
 * large enough to represent all basic types.
 * <p/>
 * <b>Note</b> An interface is required for enumerators due to the working assumption that datatypes
 * may be imported in an arbitrary order. {@link Alternative} entries reference enumerators
 * and at parse/merge time we must work on the assumption that the {@link EnumeratedType} of the 
 * discriminant not been imported yet. The interface allows the parser/merger to insert a 
 * placeholder until all datatypes have been imported and the {@link Linker} is able to resolve 
 * them to their complete representation
 */
public interface IEnumerator
{
	/**
	 * Returns the name of the enumerator constant
	 */
	public String getName();
	
	/**
	 * Returns the value of the enumerator constant
	 */
	public Number getValue();
}
