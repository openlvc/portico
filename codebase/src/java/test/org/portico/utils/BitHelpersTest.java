/*
 *   Copyright 2018 The Portico Project
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

import org.portico.utils.bithelpers.BitHelpers;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups={"BitHelpersTest","utils"})
public class BitHelpersTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	@Test()
	public void testUint8()
	{
		short limit = (short)(Math.pow(2,8));
		short printAt = (short)(limit/8);
		
		for( short i = 0; i < limit; i++ )
		{
			byte[] buffer = new byte[1];
			BitHelpers.putUint8( i, buffer, 0 );
			short result = BitHelpers.readUint8( buffer, 0 );
			Assert.assertEquals(i,result);
		}
	}

	@Test()
	public void testUint16()
	{
		int limit = (int)(Math.pow(2,16));
		int printAt = (int)(limit/16);
		
		for( int i = 0; i < limit; i++ )
		{
			byte[] buffer = new byte[2];
			BitHelpers.putUint16( i, buffer, 0 );
			int result = BitHelpers.readUint16( buffer, 0 );
			Assert.assertEquals(i,result);
		}
	}

	@Test()
	public void testUint24()
	{
		long limit = (long)(Math.pow(2,24));
		long printAt = (long)(limit/24);
		
		for( long i = 0; i < limit; i++ )
		{
			byte[] buffer = new byte[3];
			BitHelpers.putUint24( i, buffer, 0 );
			long result = BitHelpers.readUint24( buffer, 0 );
			Assert.assertEquals(i,result);
		}
	}

	@Test()
	public void testUint32()
	{
		long limit = (long)(Math.pow(2,32));
		long printAt = (long)(limit/32);
		
		for( long i = 0; i < limit; i+=printAt )
		{
			byte[] buffer = new byte[4];
			BitHelpers.putUint32( i, buffer, 0 );
			long result = BitHelpers.readUint32( buffer, 0 );
			Assert.assertEquals(i,result);
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
