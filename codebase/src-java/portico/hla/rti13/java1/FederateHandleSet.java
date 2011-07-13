/*
 *   Copyright 2008 The Portico Project
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
package hla.rti13.java1;

import org.portico.impl.hla13.types.HLA13FederateHandleSet;

public class FederateHandleSet
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private HLA13FederateHandleSet set;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	protected FederateHandleSet()
	{
		this.set = new HLA13FederateHandleSet();
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void add( int handle ) throws ArrayIndexOutOfBounds, AttributeNotDefined
	{
		set.add( handle );
	}

	public void empty()
	{
		set.empty();
	}

	public int getHandle( int i ) throws ArrayIndexOutOfBounds
	{
		return set.get( i );
	}

	public boolean isMember( int handle )
	{
		return set.isMember( handle );
	}

	public void remove( int handle ) throws AttributeNotDefined
	{
		set.remove( handle );
	}

	public int size()
	{
		return set.size();
	}

	public Object clone()
	{
		return set.clone();
	}

	public boolean equals( Object obj )
	{
		return set.equals( obj );
	}

	public int hashCode()
	{
		return set.hashCode();
	}

	public String toString()
	{
		return set.toString();
	}

	public HLA13FederateHandleSet toPorticoSet()
	{
		return this.set;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
