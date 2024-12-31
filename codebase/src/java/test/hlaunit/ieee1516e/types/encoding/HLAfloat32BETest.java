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
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;

@Test(singleThreaded=true, groups={"HLAfloat32BETest","datatype","encoding"})
public class HLAfloat32BETest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final float NOTHING = 0f;
	
	private static final float PI = 3.14159f;
	private static final byte[] PI_BIN = new byte[] { (byte)0x40, (byte)0x49, (byte)0x0f, 
	                                                  (byte)0xd0 };
	
	private static final float E = 2.71828f;
	private static final byte[] E_BIN = new byte[]{ (byte)0x40, (byte)0x2d, (byte)0xf8, 
	                                                (byte)0x4d };
	
	private static final float G = 9.80665f;
	private static final byte[] G_BIN = new byte[] { (byte)0x41, (byte)0x1c, (byte)0xe8, 
	                                                 (byte)0x0a };
	
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
		ByteArrayOutputStream bytes = new ByteArrayOutputStream( PI_BIN.length + 
		                                                         E_BIN.length +
		                                                         G_BIN.length );
		try
		{
			bytes.write( PI_BIN );
			bytes.write( E_BIN );
			bytes.write( G_BIN );
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

	////////////////////////////////////
	// TEST: testHLAfloat32BECreate() //
	////////////////////////////////////
	@Test
	public void testHLAfloat32BECreate()
	{
		// Default constructor
		HLAfloat32BE defaultConstructor = this.encoderFactory.createHLAfloat32BE();
		Assert.assertNotNull( defaultConstructor );

		// Value constructor
		HLAfloat32BE valueConstructor = this.encoderFactory.createHLAfloat32BE( PI );
		Assert.assertNotNull( valueConstructor );
		Assert.assertEquals( valueConstructor.getValue(), PI );
	}

	/////////////////////////////////////////
	// TEST: testHLAfloat32BEGetSetValue() //
	/////////////////////////////////////////
	@Test
	public void testHLAfloat32BEGetSetValue()
	{
		HLAfloat32BE data = this.encoderFactory.createHLAfloat32BE( PI );

		// Get/Set valid values
		data.setValue( E );

		Assert.assertFalse( data.getValue() == PI );
		Assert.assertEquals( data.getValue(), E );
	}

	//////////////////////////////////////////////
	// TEST: testHLAfloat32BEGetOctetBoundary() //
	//////////////////////////////////////////////
	@Test
	public void testHLAfloat32BEGetOctetBoundary()
	{
		HLAfloat32BE data = this.encoderFactory.createHLAfloat32BE();
		Assert.assertEquals( data.getOctetBoundary(), 4 );
	}

	//////////////////////////////////////////
	// TEST: testHLAfloat32BEEncodeSingle() //
	//////////////////////////////////////////
	@Test
	public void testHLAfloat32BEEncodeSingle()
	{
		HLAfloat32BE data = this.encoderFactory.createHLAfloat32BE( PI );

		// Encode with a wrapper that is exactly the right size
		int length = data.getEncodedLength();
		ByteWrapper wrapper = new ByteWrapper( length );
		Assert.assertEquals( wrapper.getPos(), 0 );

		// Does the position increment by the expected amount?
		data.encode( wrapper );
		Assert.assertEquals( wrapper.getPos(), length );

		// Are the contents of the ByteWrapper what is expected?
		Assert.assertEquals( wrapper.array(), PI_BIN );

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
	// TEST: testHLAfloat32BEEncodeMultiple() //
	////////////////////////////////////////////
	@Test
	public void testHLAfloat32BEEncodeMultiple()
	{
		byte[] dataRaw = getCombinedTestArray();
		
		HLAfloat32BE[] data = { this.encoderFactory.createHLAfloat32BE( PI ),
		                        this.encoderFactory.createHLAfloat32BE( E ),
		                        this.encoderFactory.createHLAfloat32BE( G ) };

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
		HLAfloat32BE extra = this.encoderFactory.createHLAfloat32BE();
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
	// TEST: testHLAfloat32BEEncodeEmptyWrapper() //
	////////////////////////////////////////////////
	@Test
	public void testHLAfloat32BEEncodeEmptyWrapper()
	{
		HLAfloat32BE data = this.encoderFactory.createHLAfloat32BE();
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
	// TEST: testHLAfloat32BEGetEncodedLength() //
	//////////////////////////////////////////////
	@Test
	public void testHLAfloat32BEGetEncodedLength()
	{
		HLAfloat32BE data = this.encoderFactory.createHLAfloat32BE( PI );
		Assert.assertEquals( data.getEncodedLength(), PI_BIN.length );
	}

	/////////////////////////////////////////
	// TEST: testHLAfloat32BEToByteArray() //
	/////////////////////////////////////////
	@Test
	public void testHLAfloat32BEToByteArray()
	{
		HLAfloat32BE data = this.encoderFactory.createHLAfloat32BE( PI );
		byte[] asByteArray = data.toByteArray();

		Assert.assertEquals( asByteArray, PI_BIN );
	}

	/////////////////////////////////////////////////////
	// TEST: testHLAfloat32BEDecodeByteWrapperSingle() //
	/////////////////////////////////////////////////////
	@Test
	public void testHLAfloat32BEDecodeByteWrapperSingle()
	{
		// Create a ByteWrapper with data for a single type contained within
		ByteWrapper wrapper = new ByteWrapper( PI_BIN );

		// Create the object to decode into
		HLAfloat32BE data = this.encoderFactory.createHLAfloat32BE();

		try
		{
			data.decode( wrapper );
			Assert.assertEquals( data.getValue(), PI );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAfloat32BE", e );
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
	// TEST: testHLAfloat32BEDecodeByteWrapperMultiple() //
	///////////////////////////////////////////////////////
	@Test
	public void testHLAfloat32BEDecodeByteWrapperMultiple()
	{
		// Create a ByteWrapper with data for a three individual types contained within
		byte[] buffer = getCombinedTestArray();
		float[] values = { PI, E, G };
		ByteWrapper wrapper = new ByteWrapper( buffer );

		// Attempt to decode all three types contained within the wrapper
		try
		{
			for( int i = 0; i < 3; ++i )
			{
				// Create an object to decode into
				HLAfloat32BE data = this.encoderFactory.createHLAfloat32BE( NOTHING );
				data.decode( wrapper );

				Assert.assertFalse( data.getValue() == NOTHING );
				Assert.assertEquals( data.getValue(), values[i] );
			}
		}
		catch( Exception e )
		{
			unexpectedException( "Decoding a HLAfloat32BE", e );
		}

		// Attempting to decode another type should result in a DecoderException
		try
		{
			HLAfloat32BE data = this.encoderFactory.createHLAfloat32BE();
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
	// TEST: testHLAfloat32BEDecodeByteWrapperEmpty() //
	////////////////////////////////////////////////////
	@Test
	public void testHLAfloat32BEDecodeByteWrapperEmpty()
	{
		HLAfloat32BE data = this.encoderFactory.createHLAfloat32BE();
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
	// testHLAfloat32BEDecodeByteArray() //
	///////////////////////////////////////
	@Test
	public void testHLAfloat32BEDecodeByteArray()
	{
		// Create the object to decode into
		HLAfloat32BE data = this.encoderFactory.createHLAfloat32BE( NOTHING );

		try
		{
			data.decode( PI_BIN );
			
			Assert.assertFalse( data.getValue() == NOTHING );
			Assert.assertEquals( data.getValue(), PI );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAfloat32BE", e );
		}
	}

	////////////////////////////////////////////
	// testHLAfloat32BEDecodeByteArrayEmpty() //
	////////////////////////////////////////////
	@Test
	public void testHLAfloat32BEDecodeByteArrayEmpty()
	{
		HLAfloat32BE data = this.encoderFactory.createHLAfloat32BE();
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
