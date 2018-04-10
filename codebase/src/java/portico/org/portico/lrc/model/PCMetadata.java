/*
 *   Copyright 2006 The Portico Project
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
package org.portico.lrc.model;

import java.io.Serializable;

import org.portico.lrc.model.datatype.IDatatype;

/**
 * This class contains metadata for a FOM parameter class 
 */
public class PCMetadata implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String     name;
	private IDatatype  datatype;
	private int        handle;
	private ICMetadata container;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * <b>NOTE:</b> This constructor should generally not be used. If you want an instance of this
	 * class you should use the creation methods of {@link ObjectModel ObjectModel}.
	 */
	public PCMetadata( String name, IDatatype datatype, int handle )
	{
		this.name      = name;
		this.datatype  = datatype;
		this.handle    = handle;
		this.container = null;
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
	
	public int getHandle()
	{
		return this.handle;
	}

	/**
	 * Changes the handle of this class. To prevent external tampering, this
	 * is marked as protected and should not be called by anything except the
	 * model merger.
	 */
	protected void setHandle( int handle )
	{
		this.handle = handle;
	}

	public ICMetadata getContainer()
	{
		return this.container;
	}
	
	public void setContainer( ICMetadata container )
	{
		this.container = container;
	}

	@Override
	public String toString()
	{
		return this.name;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
