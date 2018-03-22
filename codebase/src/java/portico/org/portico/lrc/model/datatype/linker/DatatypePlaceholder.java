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
package org.portico.lrc.model.datatype.linker;

import java.io.Serializable;

import org.portico.lrc.model.datatype.DatatypeClass;
import org.portico.lrc.model.datatype.IDatatype;

/**
 * This type is used as a placeholder while parsing the FOM or merging FOM modules. At parse time, 
 * the list of available types is incomplete and type declarations may reference other types that 
 * have not been parsed in yet.
 * <p/>
 * Thus any types that reference another type are provided with a {@link DatatypePlaceholder} named 
 * after the type that they reference. Once all datatypes have been parsed, a linking pass takes 
 * place and will resolve all {@link DatatypePlaceholder} instances to the actual type that they
 * represent.
 * 
 * @see Linker
 */
public class DatatypePlaceholder implements IDatatype, Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 3112252018924L;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String name;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public DatatypePlaceholder( String name )
	{
		this.name = name;
	}
	
	public DatatypePlaceholder( IDatatype type )
	{
		this.name = type.getName();
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public DatatypeClass getDatatypeClass()
	{
		return DatatypeClass.NA;
	}
	
	@Override
	public DatatypePlaceholder createUnlinkedClone()
	{
		// Clones of placeholder types should never be requested
		throw new IllegalStateException( "creating a clone of Placeholder type" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
