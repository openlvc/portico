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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * This class contains metadata about a FOM Fixed Record data type.
 * <p/>
 * An fixed record type is a heterogeneous collections of types. Fixed record types contain named
 * fields that are of other types, allowing users to build "structures of data structures". 
 */
public class FixedRecordType implements IDatatype, Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 3112252018924L;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String      name;
	private List<Field> fields;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Constructor for a fixed record with an arbitrary number of fields
	 * 
	 * @param name the name of the fixed record type
	 * @param fields the ordered list of fields that this fixed record type will contain
	 */
	public FixedRecordType( String name, Field... fields )
	{
		this( name, Arrays.asList(fields) );
	}
	
	/**
	 * Constructor for a fixed record with an arbitrary number of fields
	 * 
	 * @param name the name of the fixed record type
	 * @param fields the ordered list of fields that this fixed record type will contain
	 */
	public FixedRecordType( String name, Collection<? extends Field> fields )
	{
		this.name     = name;
		this.fields   = new ArrayList<Field>( fields );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public List<Field> getFields()
	{
		return new ArrayList<Field>( this.fields );
	}
	
	@Override
	public boolean equals( Object other )
	{
		boolean equal = false;
		if( other instanceof FixedRecordType )
		{
			FixedRecordType asFixed = (FixedRecordType)other;
			equal = Objects.equals( this.name, asFixed.name ) &&
			        this.fields.equals( asFixed.fields );
		}
		
		return equal;
	}
	
	@Override
	public String toString()
	{
		return this.name;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// IDatatype Interface ////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public DatatypeClass getDatatypeClass()
	{
		return DatatypeClass.FIXEDRECORD;
	}
	
	@Override
	public FixedRecordType createUnlinkedClone()
	{
		List<Field> newFields = new ArrayList<Field>();
		for( Field field : this.fields )
		{
			Field newField = field.createUnlinkedClone();
			newFields.add( newField );
		}
		
		return new FixedRecordType( this.name, newFields );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
