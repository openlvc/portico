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
package hlaunit.ieee1516e.types.encoding;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAoctet;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;

@Test(singleThreaded=true, groups={"HLAoctetTest","datatype","encoding"})
public class HLAoctetTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private EncoderFactory encoderFactory;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	@BeforeClass(alwaysRun = true)
	public void beforeClass()
	{
		super.beforeClass();

		// Create an EncoderFactory for the duration of the test session
		try
		{
			RtiFactory rtiFactory = RtiFactoryFactory.getRtiFactory();
			this.encoderFactory = rtiFactory.getEncoderFactory();
		}
		catch( RTIinternalError rtiie )
		{
			super.unexpectedException( "Creating an EncoderFactory", rtiie );
		}
	}

	@Override
	@AfterClass(alwaysRun = true)
	public void afterClass()
	{
		this.encoderFactory = null;

		super.afterClass();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////
	// TEST: testHLAoctetCreate() //
	////////////////////////////////
	@Test
	public void testHLAoctetCreate()
	{
		// Default constructor
		HLAoctet defaultConstructor = this.encoderFactory.createHLAoctet();
		Assert.assertNotNull( defaultConstructor );

		// Value constructor
		HLAoctet valueConstructor = this.encoderFactory.createHLAoctet( (byte)0x41 );
		Assert.assertNotNull( valueConstructor );
		Assert.assertEquals( valueConstructor.getValue(), (byte)0x41 );
	}

	/////////////////////////////////////
	// TEST: testHLAoctetGetSetValue() //
	/////////////////////////////////////
	@Test
	public void testHLAoctetGetSetValue()
	{
		HLAoctet data = this.encoderFactory.createHLAoctet( (byte)0x41 );

		// Get/Set valid values
		data.setValue( (byte)0x42 );

		Assert.assertFalse( (byte)0x41 == data.getValue() );
		Assert.assertEquals( data.getValue(), (byte)0x42 );
	}

	//////////////////////////////////////////
	// TEST: testHLAoctetGetOctetBoundary() //
	//////////////////////////////////////////
	@Test
	public void testHLAoctetGetOctetBoundary()
	{
		HLAoctet data = this.encoderFactory.createHLAoctet();
		Assert.assertEquals( data.getOctetBoundary(), 1 );
	}

	//////////////////////////////////////
	// TEST: testHLAoctetEncodeSingle() //
	//////////////////////////////////////
	@Test
	public void testHLAoctetEncodeSingle()
	{
		HLAoctet data = this.encoderFactory.createHLAoctet( (byte)0x41 );

		// Encode with a wrapper that is exactly the right size
		int length = data.getEncodedLength();
		ByteWrapper wrapper = new ByteWrapper( length );
		Assert.assertEquals( wrapper.getPos(), 0 );

		// Does the position increment by the expected amount?
		data.encode( wrapper );
		Assert.assertEquals( wrapper.getPos(), length );

		// Are the contents of the ByteWrapper what is expected?
		Assert.assertEquals( wrapper.array(), new byte[]{ (byte)0x41 } );

		// Attempting to encode beyond the ByteWrapper's bounds should result in an EncoderException
		try
		{
			data.encode( wrapper );

			// FAIL: Expected an exception
			expectedException( EncoderException.class );
		}
		catch( EncoderException ee )
		{
			// PASS: Expected this exception
		}
		catch( Exception e )
		{
			wrongException( e, EncoderException.class );
		}
	}

	////////////////////////////////////////
	// TEST: testHLAoctetEncodeMultiple() //
	////////////////////////////////////////
	@Test
	public void testHLAoctetEncodeMultiple()
	{
		HLAoctet[] data = { this.encoderFactory.createHLAoctet( (byte)0x41 ),
		                    this.encoderFactory.createHLAoctet( (byte)0x42 ),
		                    this.encoderFactory.createHLAoctet( (byte)0x43 ) };

		// Encode with a wrapper that can contain up to 3 of this type
		int length = data[0].getEncodedLength();
		ByteWrapper wrapper = new ByteWrapper( length * 3 );
		for( int i = 0; i < 3; ++i )
		{
			Assert.assertEquals( i * length, wrapper.getPos() );

			// Does the position increment by the expected amount?
			data[i].encode( wrapper );
			Assert.assertEquals( (i + 1) * length, wrapper.getPos() );

			// Are the contents of the ByteWrapper what was expected?
			byte[] wrapperContents = wrapper.array();
			for( int j = 0; j < i; ++j )
				Assert.assertEquals( wrapperContents[j], data[j].getValue() );
		}

		// Attempting to encode beyond the ByteWrapper's bounds should result in an EncoderException
		HLAoctet extra = this.encoderFactory.createHLAoctet();
		try
		{
			extra.encode( wrapper );

			// FAIL: Expected an exception
			expectedException( EncoderException.class );
		}
		catch( EncoderException ee )
		{
			// PASS: Expected this exception
		}
		catch( Exception e )
		{
			// FAIL: Wrong exception thrown
			wrongException( e, EncoderException.class );
		}
	}

	////////////////////////////////////////////
	// TEST: testHLAoctetEncodeEmptyWrapper() //
	////////////////////////////////////////////
	@Test
	public void testHLAoctetEncodeEmptyWrapper()
	{
		HLAoctet data = this.encoderFactory.createHLAoctet();
		ByteWrapper emptyWrapper = new ByteWrapper();

		try
		{
			data.encode( emptyWrapper );

			// FAIL: Expected EncoderException
			expectedException( EncoderException.class );
		}
		catch( EncoderException ee )
		{
			// PASS: Expected this exception
		}
		catch( Exception e )
		{
			// FAIL: Wrong exception
			wrongException( e, EncoderException.class );
		}
	}

	//////////////////////////////////////////
	// TEST: testHLAoctetGetEncodedLength() //
	//////////////////////////////////////////
	@Test
	public void testHLAoctetGetEncodedLength()
	{
		HLAoctet data = this.encoderFactory.createHLAoctet();
		Assert.assertEquals( data.getEncodedLength(), 1 );
	}

	/////////////////////////////////////
	// TEST: testHLAoctetToByteArray() //
	/////////////////////////////////////
	@Test
	public void testHLAoctetToByteArray()
	{
		HLAoctet data = this.encoderFactory.createHLAoctet( (byte)0x41 );
		byte[] asByteArray = data.toByteArray();

		Assert.assertEquals( asByteArray.length, 1 );
		Assert.assertEquals( asByteArray, new byte[]{ (byte)0x41 } );
	}

	/////////////////////////////////////////////////
	// TEST: testHLAoctetDecodeByteWrapperSingle() //
	/////////////////////////////////////////////////
	@Test
	public void testHLAoctetDecodeByteWrapperSingle()
	{
		// Create a ByteWrapper with data for a single type contained within
		ByteWrapper wrapper = new ByteWrapper( new byte[]{ (byte)0x41 } );

		// Create the object to decode into
		HLAoctet data = this.encoderFactory.createHLAoctet( (byte)0x00 );

		try
		{
			data.decode( wrapper );
			
			Assert.assertFalse( data.getValue() == (byte)0x00 );
			Assert.assertEquals( data.getValue(), (byte)0x41 );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAoctet", e );
		}

		// Attempting to decode another type should result in a DecoderException
		try
		{
			data.decode( wrapper );

			// FAIL: expected a DecoderException
			expectedException( DecoderException.class );
		}
		catch( DecoderException de )
		{
			// PASS: Expected this exception
		}
		catch( Exception e )
		{
			// FAIL: Expected a DecoderException
			wrongException( e, DecoderException.class );
		}
	}

	///////////////////////////////////////////////////
	// TEST: testHLAoctetDecodeByteWrapperMultiple() //
	///////////////////////////////////////////////////
	@Test
	public void testHLAoctetDecodeByteWrapperMultiple()
	{
		// Create a ByteWrapper with data for a three individual types contained within
		byte[] buffer = new byte[]{ (byte)0x41, (byte)0x42, (byte)0x43 };
		ByteWrapper wrapper = new ByteWrapper( buffer );

		// Attempt to decode all three types contained within the wrapper
		try
		{
			for( int i = 0; i < 3; ++i )
			{
				// Create an object to decode into
				HLAoctet data = this.encoderFactory.createHLAoctet( (byte)0x00 );
				data.decode( wrapper );

				Assert.assertFalse( data.getValue() == (byte)0x00 );
				Assert.assertEquals( data.getValue(), buffer[i] );
			}
		}
		catch( Exception e )
		{
			unexpectedException( "Decoding a HLAoctet", e );
		}

		// Attempting to decode another type should result in a DecoderException
		try
		{
			HLAoctet data = this.encoderFactory.createHLAoctet();
			data.decode( wrapper );

			// FAIL: expected a DecoderException
			expectedException( DecoderException.class );
		}
		catch( DecoderException de )
		{
			// PASS: Expected this exception
		}
		catch( Exception e )
		{
			// FAIL: Expected a DecoderException
			wrongException( e, DecoderException.class );
		}
	}

	////////////////////////////////////////////////
	// TEST: testHLAoctetDecodeByteWrapperEmpty() //
	////////////////////////////////////////////////
	@Test
	public void testHLAoctetDecodeByteWrapperEmpty()
	{
		HLAoctet data = this.encoderFactory.createHLAoctet();
		ByteWrapper emptyWrapper = new ByteWrapper();

		try
		{
			data.decode( emptyWrapper );

			// FAIL: expected a DecoderException
			expectedException( DecoderException.class );
		}
		catch( DecoderException de )
		{
			// PASS: Expected this exception
		}
		catch( Exception e )
		{
			// FAIL: Expected a DecoderException
			wrongException( e, DecoderException.class );
		}
	}

	///////////////////////////////////
	// testHLAoctetDecodeByteArray() //
	///////////////////////////////////
	@Test
	public void testHLAoctetDecodeByteArray()
	{
		// Create a ByteWrapper with data for a single type contained within
		byte[] buffer = { (byte)0x41 };

		// Create the object to decode into
		HLAoctet data = this.encoderFactory.createHLAoctet( (byte)0x00 );

		try
		{
			data.decode( buffer );
			
			Assert.assertFalse( data.getValue() == (byte)0x00 );
			Assert.assertEquals( data.getValue(), (byte)0x41 );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAoctet", e );
		}
	}

	////////////////////////////////////////
	// testHLAoctetDecodeByteArrayEmpty() //
	////////////////////////////////////////
	@Test
	public void testHLAoctetDecodeByteArrayEmpty()
	{
		HLAoctet data = this.encoderFactory.createHLAoctet();
		byte[] emptyBuffer = new byte[0];

		try
		{
			data.decode( emptyBuffer );

			// FAIL: expected a DecoderException
			expectedException( DecoderException.class );
		}
		catch( DecoderException de )
		{
			// PASS: Expected this exception
		}
		catch( Exception e )
		{
			// FAIL: Expected a DecoderException
			wrongException( e, DecoderException.class );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
