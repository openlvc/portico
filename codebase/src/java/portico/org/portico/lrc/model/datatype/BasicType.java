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

/**
 * This class contains metadata about a FOM Basic data type.
 * <p/>
 * Basic data types represent primitive data types in the FOM and are often the building blocks
 * of more complex data types.
 */
public class BasicType implements IDatatype, Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 3112252018924L;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String     name;
	private int        size;
	private Endianness endianness;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Constructor for BasicType with specified name, size and endianness
	 * 
	 * @param name the name of this data type
	 * @param size the size of this data type in bits
	 * @param endianness the byte ordering of this data type
	 */
	public BasicType( String name, int size, Endianness endianness )
	{
		this.name       = name;
		this.size       = size;
		this.endianness = endianness;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public int getSize()
	{
		return this.size;
	}
	
	public Endianness getEndianness()
	{
		return this.endianness;
	}
	
	@Override
	public boolean equals( Object other )
	{
		boolean equal = false;
		
		if( other instanceof BasicType )
		{
			BasicType asBasic = (BasicType)other;
			equal = Objects.equals( this.name, asBasic.name ) &&
			        this.size == asBasic.size &&
			        this.endianness == asBasic.endianness;
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
		return DatatypeClass.BASIC;
	}

	@Override
	public BasicType createUnlinkedClone()
	{
		return new BasicType( this.name, this.size, this.endianness );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
