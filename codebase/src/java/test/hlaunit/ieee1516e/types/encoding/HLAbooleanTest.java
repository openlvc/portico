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
import hla.rti1516e.encoding.HLAboolean;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;

@Test(singleThreaded=true, groups={"HLAbooleanTest","datatype","encoding"})
public class HLAbooleanTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final byte[] FALSE_BIN = new byte[]{ (byte)0x00, (byte)0x00, (byte)0x00, 
	                                                    (byte)0x00 };
	
	private static final byte[] TRUE_BIN = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, 
	                                                    (byte)0x01 };
		
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
		ByteArrayOutputStream bytes = new ByteArrayOutputStream( TRUE_BIN.length + 
		                                                         FALSE_BIN.length +
		                                                         TRUE_BIN.length );
		try
		{
			bytes.write( TRUE_BIN );
			bytes.write( FALSE_BIN );
			bytes.write( TRUE_BIN );
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

	//////////////////////////////////
	// TEST: testHLAbooleanCreate() //
	//////////////////////////////////
	@Test
	public void testHLAbooleanCreate()
	{
		// Default constructor
		HLAboolean defaultConstructor = this.encoderFactory.createHLAboolean();
		Assert.assertNotNull( defaultConstructor );

		// Value constructor
		HLAboolean valueConstructor = this.encoderFactory.createHLAboolean( true );
		Assert.assertNotNull( valueConstructor );
		Assert.assertEquals( valueConstructor.getValue(), true );
	}

	///////////////////////////////////////
	// TEST: testHLAbooleanGetSetValue() //
	///////////////////////////////////////
	@Test
	public void testHLAbooleanGetSetValue()
	{
		HLAboolean data = this.encoderFactory.createHLAboolean( true );

		// Get/Set valid values
		data.setValue( false );

		Assert.assertFalse( data.getValue() == true );
		Assert.assertEquals( data.getValue(), false );
	}

	////////////////////////////////////////////
	// TEST: testHLAbooleanGetOctetBoundary() //
	////////////////////////////////////////////
	@Test
	public void testHLAbooleanGetOctetBoundary()
	{
		HLAboolean data = this.encoderFactory.createHLAboolean();
		Assert.assertEquals( data.getOctetBoundary(), 4 );
	}

	////////////////////////////////////////
	// TEST: testHLAbooleanEncodeSingle() //
	////////////////////////////////////////
	@Test
	public void testHLAbooleanEncodeSingle()
	{
		HLAboolean data = this.encoderFactory.createHLAboolean( true );

		// Encode with a wrapper that is exactly the right size
		int length = data.getEncodedLength();
		ByteWrapper wrapper = new ByteWrapper( length );
		Assert.assertEquals( wrapper.getPos(), 0 );

		// Does the position increment by the expected amount?
		data.encode( wrapper );
		Assert.assertEquals( wrapper.getPos(), length );

		// Are the contents of the ByteWrapper what is expected?
		Assert.assertEquals( wrapper.array(), TRUE_BIN );

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

	//////////////////////////////////////////
	// TEST: testHLAbooleanEncodeMultiple() //
	//////////////////////////////////////////
	@Test
	public void testHLAbooleanEncodeMultiple()
	{
		byte[] dataRaw = getCombinedTestArray();
		
		HLAboolean[] data = { this.encoderFactory.createHLAboolean( true ),
		                      this.encoderFactory.createHLAboolean( false ),
		                      this.encoderFactory.createHLAboolean( true ) };

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
		HLAboolean extra = this.encoderFactory.createHLAboolean();
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

	//////////////////////////////////////////////
	// TEST: testHLAbooleanEncodeEmptyWrapper() //
	//////////////////////////////////////////////
	@Test
	public void testHLAbooleanEncodeEmptyWrapper()
	{
		HLAboolean data = this.encoderFactory.createHLAboolean();
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

	////////////////////////////////////////////
	// TEST: testHLAbooleanGetEncodedLength() //
	////////////////////////////////////////////
	@Test
	public void testHLAbooleanGetEncodedLength()
	{
		HLAboolean data = this.encoderFactory.createHLAboolean( true );
		Assert.assertEquals( data.getEncodedLength(), TRUE_BIN.length );
	}

	///////////////////////////////////////
	// TEST: testHLAbooleanToByteArray() //
	///////////////////////////////////////
	@Test
	public void testHLAbooleanToByteArray()
	{
		HLAboolean data = this.encoderFactory.createHLAboolean( true );
		byte[] asByteArray = data.toByteArray();

		Assert.assertEquals( asByteArray, TRUE_BIN );
	}

	///////////////////////////////////////////////////
	// TEST: testHLAbooleanDecodeByteWrapperSingle() //
	///////////////////////////////////////////////////
	@Test
	public void testHLAbooleanDecodeByteWrapperSingle()
	{
		// Create a ByteWrapper with data for a single type contained within
		ByteWrapper wrapper = new ByteWrapper( TRUE_BIN );

		// Create the object to decode into
		HLAboolean data = this.encoderFactory.createHLAboolean();

		try
		{
			data.decode( wrapper );
			Assert.assertEquals( data.getValue(), true );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAboolean", e );
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

	/////////////////////////////////////////////////////
	// TEST: testHLAbooleanDecodeByteWrapperMultiple() //
	/////////////////////////////////////////////////////
	@Test
	public void testHLAbooleanDecodeByteWrapperMultiple()
	{
		// Create a ByteWrapper with data for a three individual types contained within
		byte[] buffer = getCombinedTestArray();
		boolean[] values = { true, false, true };
		ByteWrapper wrapper = new ByteWrapper( buffer );

		// Attempt to decode all three types contained within the wrapper
		try
		{
			for( int i = 0; i < 3; ++i )
			{
				// Create an object to decode into
				HLAboolean data = this.encoderFactory.createHLAboolean( false );
				data.decode( wrapper );

				Assert.assertEquals( data.getValue(), values[i] );
			}
		}
		catch( Exception e )
		{
			unexpectedException( "Decoding a HLAboolean", e );
		}

		// Attempting to decode another type should result in a DecoderException
		try
		{
			HLAboolean data = this.encoderFactory.createHLAboolean();
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

	//////////////////////////////////////////////////
	// TEST: testHLAbooleanDecodeByteWrapperEmpty() //
	//////////////////////////////////////////////////
	@Test
	public void testHLAbooleanDecodeByteWrapperEmpty()
	{
		HLAboolean data = this.encoderFactory.createHLAboolean();
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

	/////////////////////////////////////
	// testHLAbooleanDecodeByteArray() //
	/////////////////////////////////////
	@Test
	public void testHLAbooleanDecodeByteArray()
	{
		// Create the object to decode into
		HLAboolean data = this.encoderFactory.createHLAboolean( false );

		try
		{
			data.decode( TRUE_BIN );
			
			Assert.assertFalse( data.getValue() == false );
			Assert.assertEquals( data.getValue(), true );
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAboolean", e );
		}
	}

	//////////////////////////////////////////
	// testHLAbooleanDecodeByteArrayEmpty() //
	//////////////////////////////////////////
	@Test
	public void testHLAbooleanDecodeByteArrayEmpty()
	{
		HLAboolean data = this.encoderFactory.createHLAboolean();
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
