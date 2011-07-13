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

import java.util.Set;

import org.portico.impl.hla13.types.HLA13AttributeHandleSet;

public class AttributeHandleSet
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private HLA13AttributeHandleSet set;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	protected AttributeHandleSet()
	{
		this.set = new HLA13AttributeHandleSet();
	}
	
	public AttributeHandleSet( Set<Integer> handles )
	{
		this.set = new HLA13AttributeHandleSet( handles );
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

	public boolean isEmpty()
	{
		return set.isEmpty();
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

	public HLA13AttributeHandleSet toPorticoSet()
	{
		return this.set;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
