/*
 *   Copyright 2007 The Portico Project
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

public class Dimension implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String        name;
	private int           handle;
	private Space container;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public Dimension( String name, int handle )
	{
		this.name = name;
		this.handle = handle;
		this.container = null;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public String getName()
	{
		return this.name;
	}
	
	public void setName( String name )
	{
		this.name = name;
	}
	
	public int getHandle()
	{
		return this.handle;
	}
	
	public void setHandle( int handle )
	{
		this.handle = handle;
	}
	
	public Space getContainer()
	{
		return this.container;
	}
	
	public void setContainer( Space container )
	{
		this.container = container;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
