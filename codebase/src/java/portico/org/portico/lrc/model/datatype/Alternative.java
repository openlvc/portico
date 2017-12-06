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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.portico.lrc.model.datatype.linker.DatatypePlaceholder;
import org.portico.lrc.model.datatype.linker.EnumeratorPlaceholder;

/**
 * Represents one particular form that a {@link VariantRecordType} may assume.
 */
public class Alternative implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Set<IEnumerator> enumerators;
	private String name;
	private IDatatype datatype;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Constructor for an Alternative with specified name, datatype and enumerator collection.
	 * 
	 * @param name The name of the alternative
	 * @param datatype The datatype that the alternative will store
	 * @param enumerators The collection of discriminant enumerators that this type is valid for
	 */
	public Alternative( String name, 
	                    IDatatype datatype, 
	                    IEnumerator... enumerators )
	{
		this( name, datatype, Arrays.asList(enumerators) );
	}
	
	/**
	 * Constructor for an Alternative with specified name, datatype and enumerator collection.
	 * 
	 * @param name The name of the alternative
	 * @param datatype The datatype that the alternative will store
	 * @param enumerators The collection of discriminant enumerators that this type is valid for
	 */
	public Alternative( String name, 
	                    IDatatype datatype, 
	                    Collection<? extends IEnumerator> enumerators )
	{
		this.name = name;
		this.datatype = datatype;
		this.enumerators = new HashSet<IEnumerator>( enumerators );
	}

	public String getName()
	{
		return this.name;
	}
	
	public IDatatype getDatatype()
	{
		return this.datatype;
	}
	
	public void setDatatype( IDatatype datatype )
	{
		this.datatype = datatype;
	}
	
	public Set<IEnumerator> getEnumerators()
	{
		return new HashSet<IEnumerator>( this.enumerators );
	}
	
	public void setEnumerators( Set<IEnumerator> enumerators )
	{
		this.enumerators = new HashSet<IEnumerator>( enumerators );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Creates a copy of this alternative with its datatype replaced by a {@link DatatypePlaceholder}
	 * and its enumerators replaced by {@link EnumeratorPlaceholder} references.
	 * <p/>
	 * This method is used by the model merger while it imports extension datatypes into a base 
	 * model. As dependent datatypes may not have been imported at the time this datatype is 
	 * imported into the base model, the placeholders are used as a temporary reference. After all
	 * extension datatypes have been imported into the base model, all placeholder types will
	 * be resolved to their actual representations.
	 * 
	 * @return a copy of this alternative with its datatype replaced by a {@link DatatypePlaceholder}
	 *         and its enumerators replaced by {@link EnumeratorPlaceholder} references.
	 */
	public Alternative createUnlinkedClone()
	{
		Set<EnumeratorPlaceholder> placeholderEnumerators = new HashSet<EnumeratorPlaceholder>();
		for( IEnumerator enumerator : this.enumerators )
			placeholderEnumerators.add( new EnumeratorPlaceholder(enumerator) );
		
		return new Alternative( this.name, 
		                        new DatatypePlaceholder(this.datatype), 
		                        placeholderEnumerators );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
