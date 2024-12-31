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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

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
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;

@Test(singleThreaded=true, groups={"HLAunicodeStringTest","datatype","encoding"})
public class HLAunicodeSringTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final String NOTHING = "Nothing";
	
	private static final String HELLO_WORLD = "Hello World";
	private static final byte[] HELLO_WORLD_BIN = new byte[]{ (byte)0x00, (byte)0x00, (byte)0x00, 
	                                                          (byte)0x0c, (byte)0xfe, (byte)0xff,
	                                                          (byte)0x00, (byte)0x48, (byte)0x00, 
	                                                          (byte)0x65, (byte)0x00, (byte)0x6c, 
	                                                          (byte)0x00, (byte)0x6c, (byte)0x00, 
	                                                          (byte)0x6f, (byte)0x00, (byte)0x20, 
	                                                          (byte)0x00, (byte)0x57, (byte)0x00, 
	                                                          (byte)0x6f, (byte)0x00, (byte)0x72, 
	                                                          (byte)0x00, (byte)0x6c, (byte)0x00, 
	                                                          (byte)0x64 };
	
	private static final String BONJOUR_WORLD = "Bonjour World";
	private static final byte[] BONJOUR_WORLD_BIN = new byte[]{ (byte)0x00, (byte)0x00, (byte)0x00, 
		                                                        (byte)0x0e, (byte)0xfe, (byte)0xff, 
		                                                        (byte)0x00, (byte)0x42, (byte)0x00, 
		                                                        (byte)0x6f, (byte)0x00, (byte)0x6e, 
		                                                        (byte)0x00, (byte)0x6a, (byte)0x00, 
		                                                        (byte)0x6f, (byte)0x00, (byte)0x75, 
		                                                        (byte)0x00, (byte)0x72, (byte)0x00, 
		                                                        (byte)0x20, (byte)0x00, (byte)0x57, 
		                                                        (byte)0x00, (byte)0x6f, (byte)0x00, 
		                                                        (byte)0x72, (byte)0x00, (byte)0x6c, 
		                                                        (byte)0x00, (byte)0x64};
	
	private static final String OH_HAI_WORLD = "OH HAI World";
	private static final byte[] OH_HAI_WORLD_BIN = new byte[]{ (byte)0x00, (byte)0x00, (byte)0x00, 
		                                                       (byte)0x0d, (byte)0xfe, (byte)0xff, 
		                                                       (byte)0x00, (byte)0x4f, (byte)0x00, 
		                                                       (byte)0x48, (byte)0x00, (byte)0x20, 
		                                                       (byte)0x00, (byte)0x48, (byte)0x00, 
		                                                       (byte)0x41, (byte)0x00, (byte)0x49, 
		                                                       (byte)0x00, (byte)0x20, (byte)0x00, 
		                                                       (byte)0x57, (byte)0x00, (byte)0x6f, 
		                                                       (byte)0x00, (byte)0x72, (byte)0x00, 
		                                                       (byte)0x6c, (byte)0x00, (byte)0x64 };

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
	
	private byte[] getCombinedTestArray()
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream( HELLO_WORLD_BIN.length + 
		                                                         BONJOUR_WORLD_BIN.length +
		                                                         OH_HAI_WORLD_BIN.length );
		try
		{
			bytes.write( HELLO_WORLD_BIN );
			bytes.write( BONJOUR_WORLD_BIN );
			bytes.write( OH_HAI_WORLD_BIN );
		}
		catch( IOException ioe )
		{
			unexpectedException( "Writing raw data types", ioe );
		}
		
		return bytes.toByteArray();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////
	// TEST: testHLAunicodeStringCreate() //
	//////////////////////////////////////
	@Test
	public void testHLAunicodeStringCreate()
	{
		// Default constructor
		HLAunicodeString defaultConstructor = this.encoderFactory.createHLAunicodeString();
		Assert.assertNotNull( defaultConstructor );

		// Value constructor
		HLAunicodeString valueConstructor = this.encoderFactory.createHLAunicodeString( "Hello World" );
		Assert.assertNotNull( valueConstructor );
		Assert.assertEquals( valueConstructor.getValue(), "Hello World" );
	}

	///////////////////////////////////////////
	// TEST: testHLAunicodeStringGetSetValue() //
	///////////////////////////////////////////
	@Test
	public void testHLAunicodeStringGetSetValue()
	{
		HLAunicodeString data = this.encoderFactory.createHLAunicodeString( "Hello World" );

		// Get/Set valid values
		data.setValue( "Bonjour World" );

		Assert.assertFalse( data.getValue().equals("Hello World") );
		Assert.assertEquals( data.getValue(), "Bonjour World" );
	}

	////////////////////////////////////////////////
	// TEST: testHLAunicodeStringGetOctetBoundary() //
	////////////////////////////////////////////////
	@Test
	public void testHLAunicodeStringGetOctetBoundary()
	{
		HLAunicodeString data = this.encoderFactory.createHLAunicodeString();
		Assert.assertEquals( data.getOctetBoundary(), 4 );
	}

	////////////////////////////////////////////
	// TEST: testHLAunicodeStringEncodeSingle() //
	////////////////////////////////////////////
	@Test
	public void testHLAunicodeStringEncodeSingle()
	{
		HLAunicodeString data = this.encoderFactory.createHLAunicodeString( "Hello World" );

		// Encode with a wrapper that is exactly the right size
		int length = data.getEncodedLength();
		ByteWrapper wrapper = new ByteWrapper( length );
		Assert.assertEquals( wrapper.getPos(), 0 );

		// Does the position increment by the expected amount?
		data.encode( wrapper );
		Assert.assertEquals( wrapper.getPos(), length );

		// Are the contents of the ByteWrapper what is expected?
		Assert.assertEquals( wrapper.array(), HELLO_WORLD_BIN );

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

	//////////////////////////////////////////////
	// TEST: testHLAunicodeStringEncodeMultiple() //
	//////////////////////////////////////////////
	@Test
	public void testHLAunicodeStringEncodeMultiple()
	{
		byte[] dataRaw = getCombinedTestArray();
		
		HLAunicodeString[] data = { this.encoderFactory.createHLAunicodeString( HELLO_WORLD ),
		                        this.encoderFactory.createHLAunicodeString( BONJOUR_WORLD ),
		                        this.encoderFactory.createHLAunicodeString( OH_HAI_WORLD ) };

		// Encode with a wrapper that can contain up to 3 of this type
		ByteWrapper wrapper = new ByteWrapper( dataRaw.length );
		int expectedPosition = 0;
		
		for( int i = 0; i < 3; ++i )
		{
			Assert.assertEquals( expectedPosition, wrapper.getPos() );

			// Does the position increment by the expected amount?
			data[i].encode( wrapper );
			expectedPosition += data[i].getEncodedLength();
			
			Assert.assertEquals( wrapper.getPos(), expectedPosition );

			// Are the contents of the ByteWrapper what was expected?
			byte[] wrapperContents = Arrays.copyOf( wrapper.array(), expectedPosition );
			byte[] dataToHere = Arrays.copyOf( dataRaw, expectedPosition );
			Assert.assertEquals( wrapperContents, dataToHere );
		}

		// Attempting to encode beyond the ByteWrapper's bounds should result in an EncoderException
		HLAunicodeString extra = this.encoderFactory.createHLAunicodeString();
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

	//////////////////////////////////////////////////
	// TEST: testHLAunicodeStringEncodeEmptyWrapper() //
	//////////////////////////////////////////////////
	@Test
	public void testHLAunicodeStringEncodeEmptyWrapper()
	{
		HLAunicodeString data = this.encoderFactory.createHLAunicodeString();
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

	////////////////////////////////////////////////
	// TEST: testHLAunicodeStringGetEncodedLength() //
	////////////////////////////////////////////////
	@Test
	public void testHLAunicodeStringGetEncodedLength()
	{
		HLAunicodeString data = this.encoderFactory.createHLAunicodeString( HELLO_WORLD );
		Assert.assertEquals( data.getEncodedLength(), HELLO_WORLD_BIN.length );
	}

	///////////////////////////////////////////
	// TEST: testHLAunicodeStringToByteArray() //
	///////////////////////////////////////////
	@Test
	public void testHLAunicodeStringToByteArray()
	{
		HLAunicodeString data = this.encoderFactory.createHLAunicodeString( HELLO_WORLD );
		byte[] asByteArray = data.toByteArray();

		Assert.assertEquals( asByteArray, HELLO_WORLD_BIN );
	}

	///////////////////////////////////////////////////////
	// TEST: testHLAunicodeStringDecodeByteWrapperSingle() //
	///////////////////////////////////////////////////////
	@Test
	public void testHLAunicodeStringDecodeByteWrapperSingle()
	{
		// Create a ByteWrapper with data for a single type contained within
		ByteWrapper wrapper = new ByteWrapper( HELLO_WORLD_BIN );

		// Create the object to decode into
		HLAunicodeString data = this.encoderFactory.createHLAunicodeString();

		try
		{
			data.decode( wrapper );
			Assert.assertEquals( data.getValue(), HELLO_WORLD );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAunicodeString", e );
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

	/////////////////////////////////////////////////////////
	// TEST: testHLAunicodeStringDecodeByteWrapperMultiple() //
	/////////////////////////////////////////////////////////
	@Test
	public void testHLAunicodeStringDecodeByteWrapperMultiple()
	{
		// Create a ByteWrapper with data for a three individual types contained within
		byte[] buffer = getCombinedTestArray();
		String[] values = { HELLO_WORLD, BONJOUR_WORLD, OH_HAI_WORLD };
		ByteWrapper wrapper = new ByteWrapper( buffer );

		// Attempt to decode all three types contained within the wrapper
		try
		{
			for( int i = 0; i < 3; ++i )
			{
				// Create an object to decode into
				HLAunicodeString data = this.encoderFactory.createHLAunicodeString( NOTHING );
				data.decode( wrapper );

				Assert.assertFalse( data.getValue().equals(NOTHING) );
				Assert.assertEquals( data.getValue(), values[i] );
			}
		}
		catch( Exception e )
		{
			unexpectedException( "Decoding a HLAunicodeString", e );
		}

		// Attempting to decode another type should result in a DecoderException
		try
		{
			HLAunicodeString data = this.encoderFactory.createHLAunicodeString();
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

	//////////////////////////////////////////////////////
	// TEST: testHLAunicodeStringDecodeByteWrapperEmpty() //
	//////////////////////////////////////////////////////
	@Test
	public void testHLAunicodeStringDecodeByteWrapperEmpty()
	{
		HLAunicodeString data = this.encoderFactory.createHLAunicodeString();
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

	/////////////////////////////////////////
	// testHLAunicodeStringDecodeByteArray() //
	/////////////////////////////////////////
	@Test
	public void testHLAunicodeStringDecodeByteArray()
	{
		// Create the object to decode into
		HLAunicodeString data = this.encoderFactory.createHLAunicodeString( NOTHING );

		try
		{
			data.decode( HELLO_WORLD_BIN );
			
			Assert.assertFalse( data.getValue().equals(NOTHING) );
			Assert.assertEquals( data.getValue(), HELLO_WORLD );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAunicodeString", e );
		}
	}

	//////////////////////////////////////////////
	// testHLAunicodeStringDecodeByteArrayEmpty() //
	//////////////////////////////////////////////
	@Test
	public void testHLAunicodeStringDecodeByteArrayEmpty()
	{
		HLAunicodeString data = this.encoderFactory.createHLAunicodeString();
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
