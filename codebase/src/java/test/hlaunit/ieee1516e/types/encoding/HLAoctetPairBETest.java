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
import hla.rti1516e.encoding.HLAoctetPairBE;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;

@Test(singleThreaded=true, groups={"HLAoctetPairBETest","datatype","encoding"})
public class HLAoctetPairBETest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final short NOTHING = (short)0;
	
	private static final short TEST1 = (short)21845;
	private static final byte[] TEST1_BIN = new byte[] { (byte)0x55, (byte)0x55 };
	
	private static final short TEST2 = (short)-21846;
	private static final byte[] TEST2_BIN = new byte[] { (byte)0xAA, (byte)0xAA };
	
	private static final short TEST3 = (short)255;
	private static final byte[] TEST3_BIN = new byte[] { (byte)0x00, (byte)0xFF };
			
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
	// TEST: testHLAoctetPairBECreate() //
	//////////////////////////////////////
	@Test
	public void testHLAoctetPairBECreate()
	{
		// Default constructor
		HLAoctetPairBE defaultConstructor = this.encoderFactory.createHLAoctetPairBE();
		Assert.assertNotNull( defaultConstructor );

		// Value constructor
		HLAoctetPairBE valueConstructor = this.encoderFactory.createHLAoctetPairBE( TEST1 );
		Assert.assertNotNull( valueConstructor );
		Assert.assertEquals( valueConstructor.getValue(), TEST1 );
	}

	///////////////////////////////////////////
	// TEST: testHLAoctetPairBEGetSetValue() //
	///////////////////////////////////////////
	@Test
	public void testHLAoctetPairBEGetSetValue()
	{
		HLAoctetPairBE data = this.encoderFactory.createHLAoctetPairBE( NOTHING );

		// Get/Set valid values
		data.setValue( TEST1 );

		Assert.assertFalse( data.getValue() == NOTHING );
		Assert.assertEquals( data.getValue(), TEST1 );
	}

	////////////////////////////////////////////////
	// TEST: testHLAoctetPairBEGetOctetBoundary() //
	////////////////////////////////////////////////
	@Test
	public void testHLAoctetPairBEGetOctetBoundary()
	{
		HLAoctetPairBE data = this.encoderFactory.createHLAoctetPairBE();
		Assert.assertEquals( data.getOctetBoundary(), 2 );
	}

	////////////////////////////////////////////
	// TEST: testHLAoctetPairBEEncodeSingle() //
	////////////////////////////////////////////
	@Test
	public void testHLAoctetPairBEEncodeSingle()
	{
		HLAoctetPairBE data = this.encoderFactory.createHLAoctetPairBE( TEST1 );

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
	// TEST: testHLAoctetPairBEEncodeMultiple() //
	//////////////////////////////////////////////
	@Test
	public void testHLAoctetPairBEEncodeMultiple()
	{
		byte[] dataRaw = getCombinedTestArray();
		
		HLAoctetPairBE[] data = { this.encoderFactory.createHLAoctetPairBE( TEST1 ),
		                          this.encoderFactory.createHLAoctetPairBE( TEST2 ),
		                          this.encoderFactory.createHLAoctetPairBE( TEST3 ) };

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
		HLAoctetPairBE extra = this.encoderFactory.createHLAoctetPairBE();
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
	// TEST: testHLAoctetPairBEEncodeEmptyWrapper() //
	//////////////////////////////////////////////////
	@Test
	public void testHLAoctetPairBEEncodeEmptyWrapper()
	{
		HLAoctetPairBE data = this.encoderFactory.createHLAoctetPairBE();
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
	// TEST: testHLAoctetPairBEGetEncodedLength() //
	////////////////////////////////////////////////
	@Test
	public void testHLAoctetPairBEGetEncodedLength()
	{
		HLAoctetPairBE data = this.encoderFactory.createHLAoctetPairBE( TEST1 );
		Assert.assertEquals( data.getEncodedLength(), TEST1_BIN.length );
	}

	///////////////////////////////////////////
	// TEST: testHLAoctetPairBEToByteArray() //
	///////////////////////////////////////////
	@Test
	public void testHLAoctetPairBEToByteArray()
	{
		HLAoctetPairBE data = this.encoderFactory.createHLAoctetPairBE( TEST1 );
		byte[] asByteArray = data.toByteArray();

		Assert.assertEquals( asByteArray, TEST1_BIN );
	}

	///////////////////////////////////////////////////////
	// TEST: testHLAoctetPairBEDecodeByteWrapperSingle() //
	///////////////////////////////////////////////////////
	@Test
	public void testHLAoctetPairBEDecodeByteWrapperSingle()
	{
		// Create a ByteWrapper with data for a single type contained within
		ByteWrapper wrapper = new ByteWrapper( TEST1_BIN );

		// Create the object to decode into
		HLAoctetPairBE data = this.encoderFactory.createHLAoctetPairBE();

		try
		{
			data.decode( wrapper );
			Assert.assertEquals( data.getValue(), TEST1 );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAoctetPairBE", e );
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
	// TEST: testHLAoctetPairBEDecodeByteWrapperMultiple() //
	/////////////////////////////////////////////////////////
	@Test
	public void testHLAoctetPairBEDecodeByteWrapperMultiple()
	{
		// Create a ByteWrapper with data for a three individual types contained within
		byte[] buffer = getCombinedTestArray();
		short[] values = { TEST1, TEST2, TEST3 };
		ByteWrapper wrapper = new ByteWrapper( buffer );

		// Attempt to decode all three types contained within the wrapper
		try
		{
			for( int i = 0; i < 3; ++i )
			{
				// Create an object to decode into
				HLAoctetPairBE data = this.encoderFactory.createHLAoctetPairBE( NOTHING );
				data.decode( wrapper );

				Assert.assertFalse( data.getValue() == NOTHING );
				Assert.assertEquals( data.getValue(), values[i] );
			}
		}
		catch( Exception e )
		{
			unexpectedException( "Decoding a HLAoctetPairBE", e );
		}

		// Attempting to decode another type should result in a DecoderException
		try
		{
			HLAoctetPairBE data = this.encoderFactory.createHLAoctetPairBE();
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
	// TEST: testHLAoctetPairBEDecodeByteWrapperEmpty() //
	//////////////////////////////////////////////////////
	@Test
	public void testHLAoctetPairBEDecodeByteWrapperEmpty()
	{
		HLAoctetPairBE data = this.encoderFactory.createHLAoctetPairBE();
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
	// testHLAoctetPairBEDecodeByteArray() //
	/////////////////////////////////////////
	@Test
	public void testHLAoctetPairBEDecodeByteArray()
	{
		// Create the object to decode into
		HLAoctetPairBE data = this.encoderFactory.createHLAoctetPairBE( NOTHING );

		try
		{
			data.decode( TEST1_BIN );
			
			Assert.assertFalse( data.getValue() == NOTHING );
			Assert.assertEquals( data.getValue(), TEST1 );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAoctetPairBE", e );
		}
	}

	//////////////////////////////////////////////
	// testHLAoctetPairBEDecodeByteArrayEmpty() //
	//////////////////////////////////////////////
	@Test
	public void testHLAoctetPairBEDecodeByteArrayEmpty()
	{
		HLAoctetPairBE data = this.encoderFactory.createHLAoctetPairBE();
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
