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
package hlaunit.hla13.common;

import hla.rti.Region;

import java.io.Serializable;
import java.util.HashMap;

/**
 * This class represents a particular object instance contained within a federation. It contains
 * a number of attributes to which a specific byte[] value can be bound.
 */
public class Test13Instance implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int handle;
	private int classHandle;
	private String name;
	protected HashMap<Integer,byte[]> attributes;
	protected HashMap<Integer,Region> regions;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public Test13Instance()
	{
		this.attributes = new HashMap<Integer,byte[]>();
		this.regions = new HashMap<Integer,Region>();
	}
	
	public Test13Instance( int handle, int classHandle, String name )
	{
		this();
		this.handle = handle;
		this.classHandle = classHandle;
		this.name = name;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public int getHandle()
	{
		return handle;
	}
	
	public void setHandle( int handle )
	{
		this.handle = handle;
	}
	
	/**
	 * This method will return the set of attributes contained within this instance (and their
	 * values). *NOTE*: This is a reference to the actual set, so any changes in here will be
	 * reflected in the instance.
	 */
	public HashMap<Integer,byte[]> getAttributes()
	{
		return this.attributes;
	}
	
	public byte[] getAttributeValue( int handle )
	{
		return this.attributes.get( handle );
	}

	public Region getRegion( int handle )
	{
		return this.regions.get( handle );
	}
	
	public int getClassHandle()
    {
    	return classHandle;
    }

	public void setClassHandle( int classHandle )
    {
    	this.classHandle = classHandle;
    }

	public String getName()
    {
    	return name;
    }

	public void setName( String name )
    {
    	this.name = name;
    }

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
