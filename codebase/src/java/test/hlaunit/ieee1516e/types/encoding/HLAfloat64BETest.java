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
import hla.rti1516e.encoding.HLAfloat64BE;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;

@Test(singleThreaded=true, groups={"HLAfloat64BETest","datatype","encoding"})
public class HLAfloat64BETest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final double NOTHING = 0d;
	
	private static final double PI = 3.14159d;
	private static final byte[] PI_BIN = new byte[] { (byte)0x40, (byte)0x09, (byte)0x21, 
	                                                  (byte)0xf9, (byte)0xf0, (byte)0x1b, 
	                                                  (byte)0x86, (byte)0x6e };
	
	private static final double E = 2.71828d;
	private static final byte[] E_BIN = new byte[]{ (byte)0x40, (byte)0x05, (byte)0xbf, 
	                                                (byte)0x09, (byte)0x95, (byte)0xaa, 
	                                                (byte)0xf7, (byte)0x90 };
	
	private static final double G = 9.80665d;
	private static final byte[] G_BIN = new byte[] { (byte)0x40, (byte)0x23, (byte)0x9d, 
	                                                 (byte)0x01, (byte)0x3a, (byte)0x92, 
	                                                 (byte)0xa3, (byte)0x05 };
	
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
	// TEST: testHLAfloat64BECreate() //
	////////////////////////////////////
	@Test
	public void testHLAfloat64BECreate()
	{
		// Default constructor
		HLAfloat64BE defaultConstructor = this.encoderFactory.createHLAfloat64BE();
		Assert.assertNotNull( defaultConstructor );

		// Value constructor
		HLAfloat64BE valueConstructor = this.encoderFactory.createHLAfloat64BE( PI );
		Assert.assertNotNull( valueConstructor );
		Assert.assertEquals( valueConstructor.getValue(), PI );
	}

	/////////////////////////////////////////
	// TEST: testHLAfloat64BEGetSetValue() //
	/////////////////////////////////////////
	@Test
	public void testHLAfloat64BEGetSetValue()
	{
		HLAfloat64BE data = this.encoderFactory.createHLAfloat64BE( PI );

		// Get/Set valid values
		data.setValue( E );

		Assert.assertFalse( data.getValue() == PI );
		Assert.assertEquals( data.getValue(), E );
	}

	//////////////////////////////////////////////
	// TEST: testHLAfloat64BEGetOctetBoundary() //
	//////////////////////////////////////////////
	@Test
	public void testHLAfloat64BEGetOctetBoundary()
	{
		HLAfloat64BE data = this.encoderFactory.createHLAfloat64BE();
		Assert.assertEquals( data.getOctetBoundary(), 8 );
	}

	//////////////////////////////////////////
	// TEST: testHLAfloat64BEEncodeSingle() //
	//////////////////////////////////////////
	@Test
	public void testHLAfloat64BEEncodeSingle()
	{
		HLAfloat64BE data = this.encoderFactory.createHLAfloat64BE( PI );

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
	// TEST: testHLAfloat64BEEncodeMultiple() //
	////////////////////////////////////////////
	@Test
	public void testHLAfloat64BEEncodeMultiple()
	{
		byte[] dataRaw = getCombinedTestArray();
		
		HLAfloat64BE[] data = { this.encoderFactory.createHLAfloat64BE( PI ),
		                        this.encoderFactory.createHLAfloat64BE( E ),
		                        this.encoderFactory.createHLAfloat64BE( G ) };

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
		HLAfloat64BE extra = this.encoderFactory.createHLAfloat64BE();
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
	// TEST: testHLAfloat64BEEncodeEmptyWrapper() //
	////////////////////////////////////////////////
	@Test
	public void testHLAfloat64BEEncodeEmptyWrapper()
	{
		HLAfloat64BE data = this.encoderFactory.createHLAfloat64BE();
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
	// TEST: testHLAfloat64BEGetEncodedLength() //
	//////////////////////////////////////////////
	@Test
	public void testHLAfloat64BEGetEncodedLength()
	{
		HLAfloat64BE data = this.encoderFactory.createHLAfloat64BE( PI );
		Assert.assertEquals( data.getEncodedLength(), PI_BIN.length );
	}

	/////////////////////////////////////////
	// TEST: testHLAfloat64BEToByteArray() //
	/////////////////////////////////////////
	@Test
	public void testHLAfloat64BEToByteArray()
	{
		HLAfloat64BE data = this.encoderFactory.createHLAfloat64BE( PI );
		byte[] asByteArray = data.toByteArray();

		Assert.assertEquals( asByteArray, PI_BIN );
	}

	/////////////////////////////////////////////////////
	// TEST: testHLAfloat64BEDecodeByteWrapperSingle() //
	/////////////////////////////////////////////////////
	@Test
	public void testHLAfloat64BEDecodeByteWrapperSingle()
	{
		// Create a ByteWrapper with data for a single type contained within
		ByteWrapper wrapper = new ByteWrapper( PI_BIN );

		// Create the object to decode into
		HLAfloat64BE data = this.encoderFactory.createHLAfloat64BE();

		try
		{
			data.decode( wrapper );
			Assert.assertEquals( data.getValue(), PI );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAfloat64BE", e );
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
	// TEST: testHLAfloat64BEDecodeByteWrapperMultiple() //
	///////////////////////////////////////////////////////
	@Test
	public void testHLAfloat64BEDecodeByteWrapperMultiple()
	{
		// Create a ByteWrapper with data for a three individual types contained within
		byte[] buffer = getCombinedTestArray();
		double[] values = { PI, E, G };
		ByteWrapper wrapper = new ByteWrapper( buffer );

		// Attempt to decode all three types contained within the wrapper
		try
		{
			for( int i = 0; i < 3; ++i )
			{
				// Create an object to decode into
				HLAfloat64BE data = this.encoderFactory.createHLAfloat64BE( NOTHING );
				data.decode( wrapper );

				Assert.assertFalse( data.getValue() == NOTHING );
				Assert.assertEquals( data.getValue(), values[i] );
			}
		}
		catch( Exception e )
		{
			unexpectedException( "Decoding a HLAfloat64BE", e );
		}

		// Attempting to decode another type should result in a DecoderException
		try
		{
			HLAfloat64BE data = this.encoderFactory.createHLAfloat64BE();
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
	// TEST: testHLAfloat64BEDecodeByteWrapperEmpty() //
	////////////////////////////////////////////////////
	@Test
	public void testHLAfloat64BEDecodeByteWrapperEmpty()
	{
		HLAfloat64BE data = this.encoderFactory.createHLAfloat64BE();
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
	// testHLAfloat64BEDecodeByteArray() //
	///////////////////////////////////////
	@Test
	public void testHLAfloat64BEDecodeByteArray()
	{
		// Create the object to decode into
		HLAfloat64BE data = this.encoderFactory.createHLAfloat64BE( NOTHING );

		try
		{
			data.decode( PI_BIN );
			
			Assert.assertFalse( data.getValue() == NOTHING );
			Assert.assertEquals( data.getValue(), PI );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAfloat64BE", e );
		}
	}

	////////////////////////////////////////////
	// testHLAfloat64BEDecodeByteArrayEmpty() //
	////////////////////////////////////////////
	@Test
	public void testHLAfloat64BEDecodeByteArrayEmpty()
	{
		HLAfloat64BE data = this.encoderFactory.createHLAfloat64BE();
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
