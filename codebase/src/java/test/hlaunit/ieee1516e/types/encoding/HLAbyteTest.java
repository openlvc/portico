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
import hla.rti1516e.encoding.HLAbyte;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;

@Test(singleThreaded=true, groups={"HLAbyteTest","datatype","encoding"})
public class HLAbyteTest extends Abstract1516eTest
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

	////////////////////////////////////
	// TEST: testHLAbyteCreate() //
	////////////////////////////////////
	@Test
	public void testHLAbyteCreate()
	{
		// Default constructor
		HLAbyte defaultConstructor = this.encoderFactory.createHLAbyte();
		Assert.assertNotNull( defaultConstructor );

		// Value constructor
		HLAbyte valueConstructor = this.encoderFactory.createHLAbyte( (byte)0x41 );
		Assert.assertNotNull( valueConstructor );
		Assert.assertEquals( valueConstructor.getValue(), (byte)0x41 );
	}

	/////////////////////////////////////////
	// TEST: testHLAbyteGetSetValue() //
	/////////////////////////////////////////
	@Test
	public void testHLAbyteGetSetValue()
	{
		HLAbyte data = this.encoderFactory.createHLAbyte( (byte)0x41 );

		// Get/Set valid values
		data.setValue( (byte)0x42 );

		Assert.assertFalse( (byte)0x41 == data.getValue() );
		Assert.assertEquals( data.getValue(), (byte)0x42 );
	}

	//////////////////////////////////////////////
	// TEST: testHLAbyteGetOctetBoundary() //
	//////////////////////////////////////////////
	@Test
	public void testHLAbyteGetOctetBoundary()
	{
		HLAbyte data = this.encoderFactory.createHLAbyte();
		Assert.assertEquals( data.getOctetBoundary(), 1 );
	}

	//////////////////////////////////////////
	// TEST: testHLAbyteEncodeSingle() //
	//////////////////////////////////////////
	@Test
	public void testHLAbyteEncodeSingle()
	{
		HLAbyte data = this.encoderFactory.createHLAbyte( (byte)0x41 );

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

	////////////////////////////////////////////
	// TEST: testHLAbyteEncodeMultiple() //
	////////////////////////////////////////////
	@Test
	public void testHLAbyteEncodeMultiple()
	{
		HLAbyte[] data = { this.encoderFactory.createHLAbyte( (byte)0x41 ),
		                        this.encoderFactory.createHLAbyte( (byte)0x42 ),
		                        this.encoderFactory.createHLAbyte( (byte)0x43 ) };

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
		HLAbyte extra = this.encoderFactory.createHLAbyte();
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

	////////////////////////////////////////////////
	// TEST: testHLAbyteEncodeEmptyWrapper() //
	////////////////////////////////////////////////
	@Test
	public void testHLAbyteEncodeEmptyWrapper()
	{
		HLAbyte data = this.encoderFactory.createHLAbyte();
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

	//////////////////////////////////////////////
	// TEST: testHLAbyteGetEncodedLength() //
	//////////////////////////////////////////////
	@Test
	public void testHLAbyteGetEncodedLength()
	{
		HLAbyte data = this.encoderFactory.createHLAbyte();
		Assert.assertEquals( data.getEncodedLength(), 1 );
	}

	/////////////////////////////////////////
	// TEST: testHLAbyteToByteArray() //
	/////////////////////////////////////////
	@Test
	public void testHLAbyteToByteArray()
	{
		HLAbyte data = this.encoderFactory.createHLAbyte( (byte)0x41 );
		byte[] asByteArray = data.toByteArray();

		Assert.assertEquals( asByteArray.length, 1 );
		Assert.assertEquals( asByteArray, new byte[]{ (byte)0x41 } );
	}

	/////////////////////////////////////////////////////
	// TEST: testHLAbyteDecodeByteWrapperSingle() //
	/////////////////////////////////////////////////////
	@Test
	public void testHLAbyteDecodeByteWrapperSingle()
	{
		// Create a ByteWrapper with data for a single type contained within
		ByteWrapper wrapper = new ByteWrapper( new byte[]{ (byte)0x41 } );

		// Create the object to decode into
		HLAbyte data = this.encoderFactory.createHLAbyte( (byte)0x00 );

		try
		{
			data.decode( wrapper );
			
			Assert.assertFalse( data.getValue() == (byte)0x00 );
			Assert.assertEquals( data.getValue(), (byte)0x41 );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAbyte", e );
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

	///////////////////////////////////////////////////////
	// TEST: testHLAbyteDecodeByteWrapperMultiple() //
	///////////////////////////////////////////////////////
	@Test
	public void testHLAbyteDecodeByteWrapperMultiple()
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
				HLAbyte data = this.encoderFactory.createHLAbyte( (byte)0x00 );
				data.decode( wrapper );

				Assert.assertFalse( data.getValue() == (byte)0x00 );
				Assert.assertEquals( data.getValue(), buffer[i] );
			}
		}
		catch( Exception e )
		{
			unexpectedException( "Decoding a HLAbyte", e );
		}

		// Attempting to decode another type should result in a DecoderException
		try
		{
			HLAbyte data = this.encoderFactory.createHLAbyte();
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

	////////////////////////////////////////////////////
	// TEST: testHLAbyteDecodeByteWrapperEmpty() //
	////////////////////////////////////////////////////
	@Test
	public void testHLAbyteDecodeByteWrapperEmpty()
	{
		HLAbyte data = this.encoderFactory.createHLAbyte();
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

	///////////////////////////////////////
	// testHLAbyteDecodeByteArray() //
	///////////////////////////////////////
	@Test
	public void testHLAbyteDecodeByteArray()
	{
		// Create a ByteWrapper with data for a single type contained within
		byte[] buffer = { (byte)0x41 };

		// Create the object to decode into
		HLAbyte data = this.encoderFactory.createHLAbyte( (byte)0x00 );

		try
		{
			data.decode( buffer );
			
			Assert.assertFalse( data.getValue() == (byte)0x00 );
			Assert.assertEquals( data.getValue(), (byte)0x41 );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAbyte", e );
		}
	}

	////////////////////////////////////////////
	// testHLAbyteDecodeByteArrayEmpty() //
	////////////////////////////////////////////
	@Test
	public void testHLAbyteDecodeByteArrayEmpty()
	{
		HLAbyte data = this.encoderFactory.createHLAbyte();
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
