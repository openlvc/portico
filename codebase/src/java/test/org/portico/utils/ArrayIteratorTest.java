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

import java.util.NoSuchElementException;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Tests the {@link ArrayIterator} we can use to wrap around arrays to provide a
 * valid iterator implementation (which the HLA API loves so much).
 */
@Test(groups={"ArrayIteratorTest","utils"})
public class ArrayIteratorTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String[] strings;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@BeforeTest
	public void beforeTest()
	{
		this.strings = new String[]{ "one", "two", "three" };
	}
	
	@Test(enabled=false)
	public void testArrayIterator()
	{
		@SuppressWarnings("unchecked")
		ArrayIterator<String> iterator = new ArrayIterator( strings );
		
		Assert.assertTrue( iterator.hasNext() );
		Assert.assertEquals( iterator.next(), "one" );
		Assert.assertTrue( iterator.hasNext() );
		Assert.assertEquals( iterator.next(), "two" );
		Assert.assertTrue( iterator.hasNext() );
		Assert.assertEquals( iterator.next(), "three" );

		// should be done now
		Assert.assertFalse( iterator.hasNext() );
		try
		{
			iterator.next();
			Assert.fail( "Called next() past end of iterator. Expected NoSuchElementException" );
		}
		catch( NoSuchElementException nse )
		{
			// yay!
		}
	}
	
	@Test
	public void testArrayIteratorRemoval()
	{
		@SuppressWarnings("unchecked")
		ArrayIterator<String> iterator = new ArrayIterator( strings );
		
		iterator.next();
		iterator.remove();
		Assert.assertNull( strings[0] );
		
		try
		{
			iterator.remove();
			Assert.fail( "Called remove() twice. Excepted an IllegalStateException" );
		}
		catch( IllegalStateException ise )
		{
			// yay!
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
