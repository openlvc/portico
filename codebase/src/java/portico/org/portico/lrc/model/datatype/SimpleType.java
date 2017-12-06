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
 * This class contains metadata about a FOM Simple data type.
 * 
 * A simple type describes a simple, scalar data item
 */
public class SimpleType implements IDatatype, Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 3112252018924L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String    name;
	private IDatatype representation;	// BasicType or DatatypePlaceholder only

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public SimpleType( String name, IDatatype representation )
	{
		this.name = name;
		this.setRepresentation( representation );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public IDatatype getRepresentation()
	{
		return this.representation;
	}
	
	public void setRepresentation( IDatatype representation )
	{
		// Simple types can only be representations of Basic types. We also have to allow 
		// placeholder references for deferred linking
		if( representation instanceof BasicType || representation instanceof DatatypePlaceholder )
			this.representation = representation;
		else
			throw new IllegalArgumentException( representation.getName() + " is not a Basic type" );
	}

	@Override
	public boolean equals( Object other )
	{
		boolean equal = false;
		
		if( other instanceof SimpleType )
		{
			SimpleType asSimple = (SimpleType)other;
			equal = Objects.equals( this.name, asSimple.name ) &&
			        Objects.equals( this.representation, asSimple.representation );
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
		return DatatypeClass.SIMPLE;
	}
	
	@Override
	public SimpleType createUnlinkedClone()
	{
		return new SimpleType( this.name, new DatatypePlaceholder(this.representation) );
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
