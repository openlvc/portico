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
import hla.rti1516e.encoding.HLAinteger64LE;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;

@Test(singleThreaded=true, groups={"HLAinteger64LETest","datatype","encoding"})
public class HLAinteger64LETest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long NOTHING = 0;
	
	private static final long TEST1 = 6148914691236517205l;
	private static final byte[] TEST1_BIN = new byte[] { (byte)0x55, (byte)0x55, 
	                                                     (byte)0x55, (byte)0x55,
	                                                     (byte)0x55, (byte)0x55, 
	                                                     (byte)0x55, (byte)0x55 };
	
	private static final long TEST2 = -6148914691236517206l;
	private static final byte[] TEST2_BIN = new byte[] { (byte)0xAA, (byte)0xAA,
	                                                     (byte)0xAA, (byte)0xAA,
	                                                     (byte)0xAA, (byte)0xAA,
	                                                     (byte)0xAA, (byte)0xAA };
	
	private static final long TEST3 = 71777214294589695l;
	private static final byte[] TEST3_BIN = new byte[] { (byte)0xFF, (byte)0x00,
	                                                     (byte)0xFF, (byte)0x00,
	                                                     (byte)0xFF, (byte)0x00,
	                                                     (byte)0xFF, (byte)0x00};
			
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
		ByteArrayOutputStream bytes = new ByteArrayOutputStream( TEST1_BIN.length * 3 );
		try
		{
			bytes.write( TEST1_BIN );
			bytes.write( TEST2_BIN );
			bytes.write( TEST3_BIN );
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
	// TEST: testHLAinteger64LECreate() //
	//////////////////////////////////////
	@Test
	public void testHLAinteger64LECreate()
	{
		// Default constructor
		HLAinteger64LE defaultConstructor = this.encoderFactory.createHLAinteger64LE();
		Assert.assertNotNull( defaultConstructor );

		// Value constructor
		HLAinteger64LE valueConstructor = this.encoderFactory.createHLAinteger64LE( TEST1 );
		Assert.assertNotNull( valueConstructor );
		Assert.assertEquals( valueConstructor.getValue(), TEST1 );
	}

	///////////////////////////////////////////
	// TEST: testHLAinteger64LEGetSetValue() //
	///////////////////////////////////////////
	@Test
	public void testHLAinteger64LEGetSetValue()
	{
		HLAinteger64LE data = this.encoderFactory.createHLAinteger64LE( NOTHING );

		// Get/Set valid values
		data.setValue( TEST1 );

		Assert.assertFalse( data.getValue() == NOTHING );
		Assert.assertEquals( data.getValue(), TEST1 );
	}

	////////////////////////////////////////////////
	// TEST: testHLAinteger64LEGetOctetBoundary() //
	////////////////////////////////////////////////
	@Test
	public void testHLAinteger64LEGetOctetBoundary()
	{
		HLAinteger64LE data = this.encoderFactory.createHLAinteger64LE();
		Assert.assertEquals( data.getOctetBoundary(), 8 );
	}

	////////////////////////////////////////////
	// TEST: testHLAinteger64LEEncodeSingle() //
	////////////////////////////////////////////
	@Test
	public void testHLAinteger64LEEncodeSingle()
	{
		HLAinteger64LE data = this.encoderFactory.createHLAinteger64LE( TEST1 );

		// Encode with a wrapper that is exactly the right size
		int length = data.getEncodedLength();
		ByteWrapper wrapper = new ByteWrapper( length );
		Assert.assertEquals( wrapper.getPos(), 0 );

		// Does the position increment by the expected amount?
		data.encode( wrapper );
		Assert.assertEquals( wrapper.getPos(), length );

		// Are the contents of the ByteWrapper what is expected?
		Assert.assertEquals( wrapper.array(), TEST1_BIN );

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
	// TEST: testHLAinteger64LEEncodeMultiple() //
	//////////////////////////////////////////////
	@Test
	public void testHLAinteger64LEEncodeMultiple()
	{
		byte[] dataRaw = getCombinedTestArray();
		
		HLAinteger64LE[] data = { this.encoderFactory.createHLAinteger64LE( TEST1 ),
		                          this.encoderFactory.createHLAinteger64LE( TEST2 ),
		                          this.encoderFactory.createHLAinteger64LE( TEST3 ) };

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
		HLAinteger64LE extra = this.encoderFactory.createHLAinteger64LE();
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
	// TEST: testHLAinteger64LEEncodeEmptyWrapper() //
	//////////////////////////////////////////////////
	@Test
	public void testHLAinteger64LEEncodeEmptyWrapper()
	{
		HLAinteger64LE data = this.encoderFactory.createHLAinteger64LE();
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
	// TEST: testHLAinteger64LEGetEncodedLength() //
	////////////////////////////////////////////////
	@Test
	public void testHLAinteger64LEGetEncodedLength()
	{
		HLAinteger64LE data = this.encoderFactory.createHLAinteger64LE( TEST1 );
		Assert.assertEquals( data.getEncodedLength(), TEST1_BIN.length );
	}

	///////////////////////////////////////////
	// TEST: testHLAinteger64LEToByteArray() //
	///////////////////////////////////////////
	@Test
	public void testHLAinteger64LEToByteArray()
	{
		HLAinteger64LE data = this.encoderFactory.createHLAinteger64LE( TEST1 );
		byte[] asByteArray = data.toByteArray();

		Assert.assertEquals( asByteArray, TEST1_BIN );
	}

	///////////////////////////////////////////////////////
	// TEST: testHLAinteger64LEDecodeByteWrapperSingle() //
	///////////////////////////////////////////////////////
	@Test
	public void testHLAinteger64LEDecodeByteWrapperSingle()
	{
		// Create a ByteWrapper with data for a single type contained within
		ByteWrapper wrapper = new ByteWrapper( TEST1_BIN );

		// Create the object to decode into
		HLAinteger64LE data = this.encoderFactory.createHLAinteger64LE();

		try
		{
			data.decode( wrapper );
			Assert.assertEquals( data.getValue(), TEST1 );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAinteger64LE", e );
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
	// TEST: testHLAinteger64LEDecodeByteWrapperMultiple() //
	/////////////////////////////////////////////////////////
	@Test
	public void testHLAinteger64LEDecodeByteWrapperMultiple()
	{
		// Create a ByteWrapper with data for a three individual types contained within
		byte[] buffer = getCombinedTestArray();
		long[] values = { TEST1, TEST2, TEST3 };
		ByteWrapper wrapper = new ByteWrapper( buffer );

		// Attempt to decode all three types contained within the wrapper
		try
		{
			for( int i = 0; i < 3; ++i )
			{
				// Create an object to decode into
				HLAinteger64LE data = this.encoderFactory.createHLAinteger64LE( NOTHING );
				data.decode( wrapper );

				Assert.assertFalse( data.getValue() == NOTHING );
				Assert.assertEquals( data.getValue(), values[i] );
			}
		}
		catch( Exception e )
		{
			unexpectedException( "Decoding a HLAinteger64LE", e );
		}

		// Attempting to decode another type should result in a DecoderException
		try
		{
			HLAinteger64LE data = this.encoderFactory.createHLAinteger64LE();
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
	// TEST: testHLAinteger64LEDecodeByteWrapperEmpty() //
	//////////////////////////////////////////////////////
	@Test
	public void testHLAinteger64LEDecodeByteWrapperEmpty()
	{
		HLAinteger64LE data = this.encoderFactory.createHLAinteger64LE();
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
	// testHLAinteger64LEDecodeByteArray() //
	/////////////////////////////////////////
	@Test
	public void testHLAinteger64LEDecodeByteArray()
	{
		// Create the object to decode into
		HLAinteger64LE data = this.encoderFactory.createHLAinteger64LE( NOTHING );

		try
		{
			data.decode( TEST1_BIN );
			
			Assert.assertFalse( data.getValue() == NOTHING );
			Assert.assertEquals( data.getValue(), TEST1 );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAinteger64LE", e );
		}
	}

	//////////////////////////////////////////////
	// testHLAinteger64LEDecodeByteArrayEmpty() //
	//////////////////////////////////////////////
	@Test
	public void testHLAinteger64LEDecodeByteArrayEmpty()
	{
		HLAinteger64LE data = this.encoderFactory.createHLAinteger64LE();
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
