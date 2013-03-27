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
package org.portico.impl.hla13.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.Serializable;

import hla.rti.HandleIterator;

/**
 * This class provides the common Set "functionality" required by the HLA 1.3 spec. Implementations
 * for AttributeHandleSet and FederateHandleSet can just extend it to get the required
 * functionality. 
 */
public class HLA13Set implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected List<Integer> handles;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public HLA13Set()
	{
		this.handles = new ArrayList<Integer>();
	}
	
	public HLA13Set( List<Integer> contents )
	{
		this.handles = contents;
	}
	
	public HLA13Set( Set<Integer> contents )
	{
		this();
		
		for( Integer i : contents )
		{
			this.handles.add( i );
		}
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Add the handle to the set. Won't squawk if handle already member.
	 * 
	 * @param handle int
	 */
	public void add( int handle )
	{
		// removing this: if people want to use an invalid handle, that is their choice
		// check for a valid handle
		//if( handle < 0 )
		//{
			// handle is invalid, ignore it
			//return;
		//}
		
		if( this.handles.contains(handle) )
		{
			// if we already have it, don't use it. Have to emulate Set functionality for a list
			// because the CRAPPY SPEC NEEDS CRAPPY SUPPORT FOR ITS CRAPPY HANDLE ITERATOR
			return;
		}
		this.handles.add( handle );
	}

	/**
	 * Classic clone
	 * 
	 * @return java.lang.Object
	 */
	public Object clone()
	{
		ArrayList<Integer> alternate = new ArrayList<Integer>();
		for( Integer i : handles )
		{
			alternate.add( i );
		}
		
		return new HLA13Set( alternate );
	}

	/**
	 * Empties set of its members.
	 * 
	 */
	public void empty()
	{
		this.handles.clear();
	}

	/**
	 * Classic equals.
	 * 
	 * @return boolean: true if set of same type and same members.
	 * @param obj java.lang.Object
	 */
	public boolean equals( Object other )
	{
		if( other == this )
			return true;
		
		// check to see if the type is a collection
		if( other instanceof HLA13Set )
		{
			HLA13Set otherSet = (HLA13Set)other;
			if( this.handles.containsAll(otherSet.handles) )
				return true;
		}
		
		return false;
	}

	public HandleIterator handles()
	{
		return new HSIterator();
	}

	/**
	 * Fetch the value at a given index. I added this method to keep the rti13.javapackage  happy
	 * even though it is not part of the standard DLC interface
	 */
	public int get( int index )
	{
		return this.handles.get( index );
	}
	
	/**
	 * Classic hashCode
	 * 
	 * @return int: hash code
	 */
	public int hashCode()
	{
		return this.handles.hashCode();
	}

	/**
	 * 
	 * @return boolean: true if set empty.
	 */
	public boolean isEmpty()
	{
		return this.handles.isEmpty();
	}

	/**
	 * 
	 * @return boolean: true if handle is a meber
	 * @param handle int: an attribute handle
	 */
	public boolean isMember( int handle )
	{
		return this.handles.contains( handle );
	}

	/**
	 * Remove the handle from the set. Won't squawk if handle not a member.
	 * 
	 * @param handle int
	 */
	public void remove( int handle )
	{
		this.handles.remove( handle );
	}

	/**
	 * 
	 * @return int: number of members
	 */
	public int size()
	{
		return this.handles.size();
	}

	/** 
	 * 
	 * @return java.lang.String 
	 */
	public String toString()
	{
		return handles.toString();
	}
	
	/**
	 * Non-standard method to help address the insanity 
	 */
	public HashSet<Integer> toJavaSet()
	{
		return new HashSet<Integer>( this.handles );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	//////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Private Class: JHSIterator ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	private class HSIterator implements HandleIterator
	{
		private int pos;
		
		private HSIterator()
		{
			pos = 0;
		}
		
		/**
		 * Call this to get the first valid handle. Resets the iterator.
		 * 
		 * @return int: first valid handle in set, or -1
		 */
		public int first()
		{
			if( handles.size() >= 1 )
			{
				pos = 0;
				return handles.get( pos );
			}
			else
			{
				return -1;
			}
		}

		/**
		 * Should be checked before using return from first() or next()
		 * 
		 * @return boolean: true if currently reported handle is valid.
		 */
		public boolean isValid()
		{
			return pos < handles.size();
		}

		/**
		 * @return int: next valid handle in set, or -1
		 */
		public int next()
		{
			pos++;
			if( isValid() )
			{
				return handles.get( pos );
			}
			else
			{
				return -1;
			}
		}
	}
}
