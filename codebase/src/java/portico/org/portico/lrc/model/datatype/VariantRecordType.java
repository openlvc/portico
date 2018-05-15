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
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.portico.lrc.model.datatype.linker.DatatypePlaceholder;

/**
 * This class contains metadata about a FOM Variant data type.
 * <p/>
 * A variant record datatype represents a discriminated union of types.
 */
public class VariantRecordType implements IDatatype, Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String name;
	private String discriminantName;
	private IDatatype discriminantDatatype; // EnumeratedType or DatatypePlaceholder only
	private Set<Alternative> alternatives;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public VariantRecordType( String name, 
	                          String discriminantName, 
	                          IDatatype discriminantDatatype, 
	                          Collection<? extends Alternative> alternatives )
	{
		this.name = name;
		this.discriminantName = discriminantName;
		this.discriminantDatatype = discriminantDatatype;
		this.alternatives = new HashSet<Alternative>( alternatives );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public String getDiscriminantName()
	{
		return this.discriminantName;
	}
	
	public IDatatype getDiscriminantDatatype()
	{
		return this.discriminantDatatype;
	}
	
	public void setDiscriminantDatatype( IDatatype datatype )
	{
		// Discriminants types can only be based on Enumerated types. We also have to allow 
		// placeholder references for deferred linking
		if( datatype instanceof EnumeratedType || datatype instanceof DatatypePlaceholder )
			this.discriminantDatatype = datatype;
		else
			throw new IllegalArgumentException( datatype.getName() + " is not an Enumerated type" );
	}
	
	public Set<Alternative> getAlternatives()
	{
		return new HashSet<Alternative>( this.alternatives );
	}
	
	@Override
	public boolean equals( Object other )
	{
		boolean equal = false;
		
		if( other instanceof VariantRecordType )
		{
			VariantRecordType asVariant = (VariantRecordType)other;
			equal = Objects.equals( this.name, asVariant.name ) &&
			        Objects.equals( this.discriminantName, asVariant.discriminantName ) &&
			        Objects.equals( this.discriminantDatatype, asVariant.discriminantDatatype ) && 
			        Objects.equals( this.alternatives, asVariant.alternatives );
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
		return DatatypeClass.VARIANTRECORD;
	}
	
	@Override
	public VariantRecordType createUnlinkedClone()
	{
		Set<Alternative> unlinkedAlternatives = new HashSet<Alternative>();
		for( Alternative alternative : this.alternatives )
			unlinkedAlternatives.add( alternative.createUnlinkedClone() );
		
		return new VariantRecordType( name, 
		                              discriminantName, 
		                              new DatatypePlaceholder(discriminantDatatype), 
		                              unlinkedAlternatives );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
