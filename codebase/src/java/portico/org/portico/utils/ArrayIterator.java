/*
 *   Copyright 2012 The Portico Project
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
package org.portico.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The HLA API has a bit of a love affair with iterators. For many situations we need
 * an iterator to move over the values in an array. This is an attempt to provide a
 * generic implementation.
 * <p/>
 * Unfortunately, because you can't safely use generics and raw-arrays together, creating
 * an instance of this class will show up with a warning about type safety. To kill this
 * you'll need to include the suppress warnings annotation. For example:
 * <code>
 * @SuppressWarnings("unchecked")
 * ArrayIterator<Byte> iterator = new ArrayItertor<Byte>( myarray );
 * </code>
 */
public class ArrayIterator<T> implements Iterator<T>
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private T[] values;
	private int index;
	private int maxIndex;
	private boolean removeCalled;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create an new instance of the iterator, backing it on the provided array
	 */
	public ArrayIterator( T[] values ) // will cause an unchecked warning - generics and arrays :(
	{
		this.values = values;
		this.index = -1;
		this.maxIndex = values.length-1;
		this.removeCalled = false;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Return true if there are more elements left to iterate over, false otherwise.
	 */
	public boolean hasNext()
	{
		return (this.index+1) <= this.maxIndex;
	}

	/**
	 * Return the next element, throwing an exception if there aren't any more.
	 */
	public T next() throws NoSuchElementException
	{
		if( this.hasNext() == false )
			throw new NoSuchElementException( "Past end of the iterator: index=" + index );
		
		// the user has now called next(), so they can validly call remove()
		// flick the flag back to false
		this.removeCalled = false;

		// move the index forward and return
		index++;
		return this.values[index];
	}

	/**
	 * Removes the value from the array at the position we are currently located at.
	 * For example:
	 * <code>
	 * Iterator iterator = ...
	 * iterator.next();   // returns object at index 1
	 * iterator.next();   // returns object at index 2
	 * iterator.remove(); // removes from index 2
	 * </code>
	 * 
	 * If the iterator has not had next() called on it yet, the index will be at
	 * a position <i>before</i> the first element. In this case, an exception will
	 * be thrown.
	 * <p/>
	 * An exception will also be thrown if the user calls remove twice (as the element
	 * at the index will already have been removed by the first call).
	 * 
	 * @throws IllegalStateException if next() hasn't been called yet and the index of the
	 *                               iterator has not been advanced into the array, or if
	 *                               the user has already called remove once
	 * 
	 * @since ostermillerutils 1.03.00
	 */
	public void remove()
	{
		if( this.index < 0 )
			throw new IllegalStateException( "Can't remove from iterator before calling next()" );
		else if( this.removeCalled )
			throw new IllegalStateException( "Can't call remove() twice before calling next()" );
		
		// we're all cool, remove and flick the removed flag on
		this.values[index] = null;
		this.removeCalled = true;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
