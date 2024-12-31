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
import hla.rti1516e.encoding.HLAASCIIstring;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;

@Test(singleThreaded=true, groups={"HLAASCIIstringTest","datatype","encoding"})
public class HLAASCIIstringTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final String NOTHING = "Nothing";
	
	private static final String HELLO_WORLD = "Hello World";
	private static final byte[] HELLO_WORLD_BIN = new byte[]{ (byte)0x00, (byte)0x00, (byte)0x00, 
	                                                          (byte)0x0B, (byte)0x48, (byte)0x65,
	                                                          (byte)0x6C, (byte)0x6C, (byte)0x6F,
	                                                          (byte)0x20, (byte)0x57, (byte)0x6F,
	                                                          (byte)0x72, (byte)0x6C, (byte)0x64 };
	
	private static final String BONJOUR_WORLD = "Bonjour World";
	private static final byte[] BONJOUR_WORLD_BIN = new byte[]{ (byte)0x00, (byte)0x00, (byte)0x00,
	                                                            (byte)0x0D, (byte)0x42, (byte)0x6F, 
	                                                            (byte)0x6E, (byte)0x6A, (byte)0x6F, 
	                                                            (byte)0x75, (byte)0x72, (byte)0x20, 
	                                                            (byte)0x57, (byte)0x6F, (byte)0x72, 
	                                                            (byte)0x6C, (byte)0x64 };
	
	private static final String OH_HAI_WORLD = "OH HAI World";
	private static final byte[] OH_HAI_WORLD_BIN = new byte[]{ (byte)0x00, (byte)0x00, (byte)0x00, 
	                                                           (byte)0x0C, (byte)0x4F, (byte)0x48, 
	                                                           (byte)0x20, (byte)0x48, (byte)0x41, 
	                                                           (byte)0x49, (byte)0x20, (byte)0x57, 
	                                                           (byte)0x6F, (byte)0x72, (byte)0x6C, 
	                                                           (byte)0x64 };

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
	// TEST: testHLAASCIIstringCreate() //
	//////////////////////////////////////
	@Test
	public void testHLAASCIIstringCreate()
	{
		// Default constructor
		HLAASCIIstring defaultConstructor = this.encoderFactory.createHLAASCIIstring();
		Assert.assertNotNull( defaultConstructor );

		// Value constructor
		HLAASCIIstring valueConstructor = this.encoderFactory.createHLAASCIIstring( HELLO_WORLD );
		Assert.assertNotNull( valueConstructor );
		Assert.assertEquals( valueConstructor.getValue(), HELLO_WORLD );
	}

	///////////////////////////////////////////
	// TEST: testHLAASCIIstringGetSetValue() //
	///////////////////////////////////////////
	@Test
	public void testHLAASCIIstringGetSetValue()
	{
		HLAASCIIstring data = this.encoderFactory.createHLAASCIIstring( HELLO_WORLD );

		// Get/Set valid values
		data.setValue( BONJOUR_WORLD );

		Assert.assertFalse( data.getValue().equals(HELLO_WORLD) );
		Assert.assertEquals( data.getValue(), BONJOUR_WORLD );
	}

	////////////////////////////////////////////////
	// TEST: testHLAASCIIstringGetOctetBoundary() //
	////////////////////////////////////////////////
	@Test
	public void testHLAASCIIstringGetOctetBoundary()
	{
		HLAASCIIstring data = this.encoderFactory.createHLAASCIIstring( HELLO_WORLD );
		Assert.assertEquals( data.getOctetBoundary(), 4 + HELLO_WORLD.length() );
		
		data.setValue( BONJOUR_WORLD );
		Assert.assertEquals( data.getOctetBoundary(), 4 + BONJOUR_WORLD.length() );
		
		data.setValue( OH_HAI_WORLD );
		Assert.assertEquals( data.getOctetBoundary(), 4 + OH_HAI_WORLD.length() );
	}

	////////////////////////////////////////////
	// TEST: testHLAASCIIstringEncodeSingle() //
	////////////////////////////////////////////
	@Test
	public void testHLAASCIIstringEncodeSingle()
	{
		HLAASCIIstring data = this.encoderFactory.createHLAASCIIstring( HELLO_WORLD );

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
	// TEST: testHLAASCIIstringEncodeMultiple() //
	//////////////////////////////////////////////
	@Test
	public void testHLAASCIIstringEncodeMultiple()
	{
		byte[] dataRaw = getCombinedTestArray();
		
		HLAASCIIstring[] data = { this.encoderFactory.createHLAASCIIstring( HELLO_WORLD ),
		                        this.encoderFactory.createHLAASCIIstring( BONJOUR_WORLD ),
		                        this.encoderFactory.createHLAASCIIstring( OH_HAI_WORLD ) };

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
		HLAASCIIstring extra = this.encoderFactory.createHLAASCIIstring();
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
	// TEST: testHLAASCIIstringEncodeEmptyWrapper() //
	//////////////////////////////////////////////////
	@Test
	public void testHLAASCIIstringEncodeEmptyWrapper()
	{
		HLAASCIIstring data = this.encoderFactory.createHLAASCIIstring();
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
	// TEST: testHLAASCIIstringGetEncodedLength() //
	////////////////////////////////////////////////
	@Test
	public void testHLAASCIIstringGetEncodedLength()
	{
		HLAASCIIstring data = this.encoderFactory.createHLAASCIIstring( HELLO_WORLD );
		Assert.assertEquals( data.getEncodedLength(), HELLO_WORLD_BIN.length );
	}

	///////////////////////////////////////////
	// TEST: testHLAASCIIstringToByteArray() //
	///////////////////////////////////////////
	@Test
	public void testHLAASCIIstringToByteArray()
	{
		HLAASCIIstring data = this.encoderFactory.createHLAASCIIstring( HELLO_WORLD );
		byte[] asByteArray = data.toByteArray();

		Assert.assertEquals( asByteArray, HELLO_WORLD_BIN );
	}

	///////////////////////////////////////////////////////
	// TEST: testHLAASCIIstringDecodeByteWrapperSingle() //
	///////////////////////////////////////////////////////
	@Test
	public void testHLAASCIIstringDecodeByteWrapperSingle()
	{
		// Create a ByteWrapper with data for a single type contained within
		ByteWrapper wrapper = new ByteWrapper( HELLO_WORLD_BIN );

		// Create the object to decode into
		HLAASCIIstring data = this.encoderFactory.createHLAASCIIstring();

		try
		{
			data.decode( wrapper );
			Assert.assertEquals( data.getValue(), HELLO_WORLD );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAASCIIstring", e );
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
	// TEST: testHLAASCIIstringDecodeByteWrapperMultiple() //
	/////////////////////////////////////////////////////////
	@Test
	public void testHLAASCIIstringDecodeByteWrapperMultiple()
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
				HLAASCIIstring data = this.encoderFactory.createHLAASCIIstring( NOTHING );
				data.decode( wrapper );

				Assert.assertFalse( data.getValue().equals(NOTHING) );
				Assert.assertEquals( data.getValue(), values[i] );
			}
		}
		catch( Exception e )
		{
			unexpectedException( "Decoding a HLAASCIIstring", e );
		}

		// Attempting to decode another type should result in a DecoderException
		try
		{
			HLAASCIIstring data = this.encoderFactory.createHLAASCIIstring();
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
	// TEST: testHLAASCIIstringDecodeByteWrapperEmpty() //
	//////////////////////////////////////////////////////
	@Test
	public void testHLAASCIIstringDecodeByteWrapperEmpty()
	{
		HLAASCIIstring data = this.encoderFactory.createHLAASCIIstring();
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
	// testHLAASCIIstringDecodeByteArray() //
	/////////////////////////////////////////
	@Test
	public void testHLAASCIIstringDecodeByteArray()
	{
		// Create the object to decode into
		HLAASCIIstring data = this.encoderFactory.createHLAASCIIstring( NOTHING );

		try
		{
			data.decode( HELLO_WORLD_BIN );
			
			Assert.assertFalse( data.getValue().equals(NOTHING) );
			Assert.assertEquals( data.getValue(), HELLO_WORLD );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAASCIIstring", e );
		}
	}

	//////////////////////////////////////////////
	// testHLAASCIIstringDecodeByteArrayEmpty() //
	//////////////////////////////////////////////
	@Test
	public void testHLAASCIIstringDecodeByteArrayEmpty()
	{
		HLAASCIIstring data = this.encoderFactory.createHLAASCIIstring();
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
