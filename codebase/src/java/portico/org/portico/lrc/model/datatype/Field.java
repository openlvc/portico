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
import java.util.Objects;

import org.portico.lrc.model.datatype.linker.DatatypePlaceholder;

/**
 * Describes a field of a {@link FixedRecordType} datatype
 */
public class Field implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 3112252018924L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String    name;
	private IDatatype datatype;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Field( String name, IDatatype datatype )
	{
		this.name     = name;
		this.datatype = datatype;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
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
	
	/**
	 * Creates a copy of this field with its datatype replaced by a {@link DatatypePlaceholder}.
	 * <p/>
	 * This method is used by the model merger while it imports extension datatypes into a base 
	 * model. As dependent datatypes may not have been imported at the time this datatype is 
	 * imported into the base model, the placeholder is used as a temporary reference. After all
	 * extension datatypes have been imported into the base model, all placeholder types will
	 * be resolved to their actual representations.
	 * 
	 * @return a copy of this field with its datatype replaced with a {@link DatatypePlaceholder}
	 */
	public Field createUnlinkedClone()
	{
		return new Field( this.name, new DatatypePlaceholder(datatype) );
	}
	
	@Override
	public boolean equals( Object other )
	{
		boolean equal = false;
		
		if( other instanceof Field )
		{
			Field asField = (Field)other;
			equal = Objects.equals( this.name, asField.name ) &&
			        Objects.equals( this.datatype, asField.datatype );
		}
		
		return equal;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
