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
import hla.rti1516e.encoding.HLAopaqueData;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;

@Test(singleThreaded=true, groups={"HLAopaqueDataTest","datatype","encoding"})
public class HLAopaqueDataTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final byte[] NOTHING = new byte[0];
	
	private static final byte[] TEST_ONE = new byte[]{ (byte)0x48, (byte)0x65, (byte)0x6C, 
	                                                   (byte)0x6C, (byte)0x6F, (byte)0x20, 
	                                                   (byte)0x57, (byte)0x6F, (byte)0x72, 
	                                                   (byte)0x6C, (byte)0x64 };

	private static final byte[] TEST_TWO = new byte[]{ (byte)0x42, (byte)0x6F, (byte)0x6E, 
	                                                   (byte)0x6A, (byte)0x6F, (byte)0x75, 
	                                                   (byte)0x72, (byte)0x20, (byte)0x57, 
	                                                   (byte)0x6F, (byte)0x72, (byte)0x6C, 
	                                                   (byte)0x64 };
	
	private static final byte[] TEST_THREE = new byte[]{ (byte)0x4F, (byte)0x48, (byte)0x20, 
	                                                     (byte)0x48, (byte)0x41, (byte)0x49, 
	                                                     (byte)0x20, (byte)0x57, (byte)0x6F, 
	                                                     (byte)0x72, (byte)0x6C, (byte)0x64 };

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
	
	private int getExpectedEncodedLength( byte[] opaqueData )
	{
		return 4 + opaqueData.length;
	}
	
	private byte[] getExpectedEncodedResult( byte[] opaqueData )
	{
		int length = getExpectedEncodedLength( opaqueData );
		ByteWrapper wrapper = new ByteWrapper( length );
		
		wrapper.putInt( opaqueData.length );
		wrapper.put( opaqueData );
		
		return wrapper.array();
	}
	
	private byte[] getCombinedTestArray()
	{
		byte[] testOneEncoded = getExpectedEncodedResult( TEST_ONE );
		byte[] testTwoEncoded = getExpectedEncodedResult( TEST_TWO );
		byte[] testThreeEncoded = getExpectedEncodedResult( TEST_THREE );
		
		ByteWrapper bytes = new ByteWrapper( testOneEncoded.length + 
		                                     testTwoEncoded.length + 
		                                     testThreeEncoded.length );
		
		bytes.put( testOneEncoded );
		bytes.put( testTwoEncoded );
		bytes.put( testThreeEncoded );
		
		return bytes.array();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////
	// TEST: testHLAopaqueDataCreate() //
	//////////////////////////////////////
	@Test
	public void testHLAopaqueDataCreate()
	{
		// Default constructor
		HLAopaqueData defaultConstructor = this.encoderFactory.createHLAopaqueData();
		Assert.assertNotNull( defaultConstructor );

		// Value constructor
		HLAopaqueData valueConstructor = this.encoderFactory.createHLAopaqueData( TEST_ONE );
		Assert.assertNotNull( valueConstructor );
		Assert.assertEquals( valueConstructor.getValue(), TEST_ONE );
	}

	///////////////////////////////////////////
	// TEST: testHLAopaqueDataGetSetValue() //
	///////////////////////////////////////////
	@Test
	public void testHLAopaqueDataGetSetValue()
	{
		HLAopaqueData data = this.encoderFactory.createHLAopaqueData( TEST_ONE );

		// Get/Set valid values
		data.setValue( TEST_TWO );

		Assert.assertFalse( data.getValue().equals(TEST_ONE) );
		Assert.assertEquals( data.getValue(), TEST_TWO );
	}

	////////////////////////////////////////////////
	// TEST: testHLAopaqueDataGetOctetBoundary() //
	////////////////////////////////////////////////
	@Test
	public void testHLAopaqueDataGetOctetBoundary()
	{
		HLAopaqueData data = this.encoderFactory.createHLAopaqueData( TEST_ONE );
		Assert.assertEquals( data.getOctetBoundary(), getExpectedEncodedLength(TEST_ONE) );
		
		data.setValue( TEST_TWO );
		Assert.assertEquals( data.getOctetBoundary(), getExpectedEncodedLength(TEST_TWO) );
		
		data.setValue( TEST_THREE );
		Assert.assertEquals( data.getOctetBoundary(), getExpectedEncodedLength(TEST_THREE) );
	}

	////////////////////////////////////////////
	// TEST: testHLAopaqueDataEncodeSingle() //
	////////////////////////////////////////////
	@Test
	public void testHLAopaqueDataEncodeSingle()
	{
		HLAopaqueData data = this.encoderFactory.createHLAopaqueData( TEST_ONE );

		// Encode with a wrapper that is exactly the right size
		int length = data.getEncodedLength();
		ByteWrapper wrapper = new ByteWrapper( length );
		Assert.assertEquals( wrapper.getPos(), 0 );

		// Does the position increment by the expected amount?
		data.encode( wrapper );
		Assert.assertEquals( wrapper.getPos(), length );

		// Are the contents of the ByteWrapper what is expected?
		Assert.assertEquals( wrapper.array(), getExpectedEncodedResult(TEST_ONE) );

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
	// TEST: testHLAopaqueDataEncodeMultiple() //
	//////////////////////////////////////////////
	@Test
	public void testHLAopaqueDataEncodeMultiple()
	{
		byte[] dataRaw = getCombinedTestArray();
		
		HLAopaqueData[] data = { this.encoderFactory.createHLAopaqueData(TEST_ONE),
		                         this.encoderFactory.createHLAopaqueData(TEST_TWO),
		                         this.encoderFactory.createHLAopaqueData(TEST_THREE) };

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
		HLAopaqueData extra = this.encoderFactory.createHLAopaqueData();
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
	// TEST: testHLAopaqueDataEncodeEmptyWrapper() //
	//////////////////////////////////////////////////
	@Test
	public void testHLAopaqueDataEncodeEmptyWrapper()
	{
		HLAopaqueData data = this.encoderFactory.createHLAopaqueData();
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
	// TEST: testHLAopaqueDataGetEncodedLength() //
	////////////////////////////////////////////////
	@Test
	public void testHLAopaqueDataGetEncodedLength()
	{
		HLAopaqueData data = this.encoderFactory.createHLAopaqueData( TEST_ONE );
		Assert.assertEquals( data.getEncodedLength(), getExpectedEncodedLength(TEST_ONE) );
	}

	///////////////////////////////////////////
	// TEST: testHLAopaqueDataToByteArray() //
	///////////////////////////////////////////
	@Test
	public void testHLAopaqueDataToByteArray()
	{
		HLAopaqueData data = this.encoderFactory.createHLAopaqueData( TEST_ONE );
		byte[] asByteArray = data.toByteArray();

		Assert.assertEquals( asByteArray, getExpectedEncodedResult(TEST_ONE) );
	}

	///////////////////////////////////////////////////////
	// TEST: testHLAopaqueDataDecodeByteWrapperSingle() //
	///////////////////////////////////////////////////////
	@Test
	public void testHLAopaqueDataDecodeByteWrapperSingle()
	{
		// Create a ByteWrapper with data for a single type contained within
		byte[] binary = getExpectedEncodedResult( TEST_ONE );
		ByteWrapper wrapper = new ByteWrapper( binary );

		// Create the object to decode into
		HLAopaqueData data = this.encoderFactory.createHLAopaqueData();

		try
		{
			data.decode( wrapper );
			Assert.assertEquals( data.getValue(), TEST_ONE );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAopaqueData", e );
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
	// TEST: testHLAopaqueDataDecodeByteWrapperMultiple() //
	/////////////////////////////////////////////////////////
	@Test
	public void testHLAopaqueDataDecodeByteWrapperMultiple()
	{
		// Create a ByteWrapper with data for a three individual types contained within
		byte[] buffer = getCombinedTestArray();
		byte[][] values = { TEST_ONE, TEST_TWO, TEST_THREE };
		ByteWrapper wrapper = new ByteWrapper( buffer );

		// Attempt to decode all three types contained within the wrapper
		try
		{
			for( int i = 0; i < 3; ++i )
			{
				// Create an object to decode into
				HLAopaqueData data = this.encoderFactory.createHLAopaqueData( );
				data.decode( wrapper );

				Assert.assertFalse( data.getValue().equals(NOTHING) );
				Assert.assertEquals( data.getValue(), values[i] );
			}
		}
		catch( Exception e )
		{
			unexpectedException( "Decoding a HLAopaqueData", e );
		}

		// Attempting to decode another type should result in a DecoderException
		try
		{
			HLAopaqueData data = this.encoderFactory.createHLAopaqueData();
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
	// TEST: testHLAopaqueDataDecodeByteWrapperEmpty() //
	//////////////////////////////////////////////////////
	@Test
	public void testHLAopaqueDataDecodeByteWrapperEmpty()
	{
		HLAopaqueData data = this.encoderFactory.createHLAopaqueData();
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
	// testHLAopaqueDataDecodeByteArray() //
	/////////////////////////////////////////
	@Test
	public void testHLAopaqueDataDecodeByteArray()
	{
		// Create the object to decode into
		HLAopaqueData data = this.encoderFactory.createHLAopaqueData( NOTHING );

		try
		{
			byte[] binary = getExpectedEncodedResult( TEST_ONE );
			data.decode( binary );
			
			Assert.assertFalse( data.getValue().equals(NOTHING) );
			Assert.assertEquals( data.getValue(), TEST_ONE );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAopaqueData", e );
		}
	}

	//////////////////////////////////////////////
	// testHLAopaqueDataDecodeByteArrayEmpty() //
	//////////////////////////////////////////////
	@Test
	public void testHLAopaqueDataDecodeByteArrayEmpty()
	{
		HLAopaqueData data = this.encoderFactory.createHLAopaqueData();
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
