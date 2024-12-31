/*
 *   Copyright 2016 The Portico Project
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

import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.*;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import org.portico.impl.hla1516e.types.encoding.HLA1516eEncoderFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"HLAvariantRecordTest","datatype","encoding"})
public class HLAvariantRecordTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final float VALUE_ONE = 3.14159f;
	public static final String VALUE_TWO = "Hello World";
	public static final boolean VALUE_THREE = true;

	public static final byte DISCRIMINANT_ONE = (byte)1;
	public static final byte DISCRIMINANT_TWO = (byte)2;
	public static final byte DISCRIMINANT_THREE = (byte)3;

	private static final byte[] TEST_BIN_ONE = {
	        // discriminant
	        (byte)0x01,
	        // VALUE_ONE float
			(byte)0x40, (byte)0x49, (byte)0x0f, (byte)0xd0
	};

	private static final byte[] TEST_BIN_TWO = {
	        // discriminant
	        (byte)0x02,
	        // VALUE_TWO string (first four bytes are string length)
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0B,
			(byte)0x48, (byte)0x65, (byte)0x6C, (byte)0x6C,
			(byte)0x6F, (byte)0x20, (byte)0x57, (byte)0x6F,
			(byte)0x72, (byte)0x6C, (byte)0x64
	};

	private static final byte[] TEST_BIN_THREE = {
	        // discriminant
	        (byte)0x03,
	        // VALUE_THREE boolean
			(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01
	};

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
		
		this.encoderFactory = new HLA1516eEncoderFactory();
	}

	@Override
	@AfterClass(alwaysRun = true)
	public void afterClass()
	{
		this.encoderFactory = null;

		super.afterClass();
	}

	@SuppressWarnings("unchecked")
	private void addTestFieldsToRecord( HLAvariantRecord record, boolean setValues )
	{
		if( setValues )
		{
			record.setVariant( this.encoderFactory.createHLAoctet( DISCRIMINANT_ONE ),
			                   this.encoderFactory.createHLAfloat32BE( VALUE_ONE ) );

			record.setVariant( this.encoderFactory.createHLAoctet( DISCRIMINANT_TWO ),
			                   this.encoderFactory.createHLAASCIIstring( VALUE_TWO ) );
			
			record.setVariant( this.encoderFactory.createHLAoctet( DISCRIMINANT_THREE ),
			                   this.encoderFactory.createHLAboolean( VALUE_THREE ) );
		}
		else
		{
			record.setVariant( this.encoderFactory.createHLAoctet( DISCRIMINANT_ONE ),
			                   this.encoderFactory.createHLAfloat32BE() );
			
			record.setVariant( this.encoderFactory.createHLAoctet( DISCRIMINANT_TWO ),
			                   this.encoderFactory.createHLAASCIIstring() );
			
			record.setVariant( this.encoderFactory.createHLAoctet( DISCRIMINANT_THREE ),
			                   this.encoderFactory.createHLAboolean() );
		}
	}
		
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////// Test Methods //////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////
	// TEST: testHLAvariantRecordCreate() //
	////////////////////////////////////////
	@Test
	public void testHLAvariantRecordCreate()
	{
		// Only one constructor to test!
		HLAvariantRecord defaultConstructor = encoderFactory.createHLAvariantRecord(
		                                      encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );
		Assert.assertNotNull( defaultConstructor );
	}

	/////////////////////////////////////
	// TEST: testHLAvariantRecordAdd() //
	/////////////////////////////////////
	@Test
	@SuppressWarnings("unchecked")
	public void testHLAvariantRecordAdd()
	{
		HLAvariantRecord data =
		    encoderFactory.createHLAvariantRecord( encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );

		// Add some fields to the record
		this.addTestFieldsToRecord( data, false );

		// Each discriminant should have an assigned, non-null value
		Assert.assertNotNull( data.getValue() );
		data.setDiscriminant( this.encoderFactory.createHLAoctet(DISCRIMINANT_TWO) );
		Assert.assertNotNull( data.getValue() );
		data.setDiscriminant( this.encoderFactory.createHLAoctet(DISCRIMINANT_THREE) );
		Assert.assertNotNull( data.getValue() );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testHLAvariantRecordGet()
	{
		HLAvariantRecord data =
		    encoderFactory.createHLAvariantRecord( encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );

		// Add some fields to the record
		this.addTestFieldsToRecord( data, false );

		// Order of fields and type information is maintained
		Assert.assertTrue( data.getValue() instanceof HLAfloat32BE );
		data.setDiscriminant( this.encoderFactory.createHLAoctet(DISCRIMINANT_TWO) );
		Assert.assertTrue( data.getValue() instanceof HLAASCIIstring );
		data.setDiscriminant( this.encoderFactory.createHLAoctet(DISCRIMINANT_THREE) );
		Assert.assertTrue( data.getValue() instanceof HLAboolean );
	}

	////////////////////////////////////////////////////////
	// TEST: testHLAvariantRecordGetUnknownDiscriminant() //
	////////////////////////////////////////////////////////
	@Test
	public void testHLAvariantRecordGetUnknownDiscriminant()
	{
		HLAvariantRecord data =
		    this.encoderFactory.createHLAvariantRecord( encoderFactory.createHLAoctet((byte)4) );

		// Add some fields to the record
		this.addTestFieldsToRecord( data, false );

		Assert.assertNull( data.getValue() );
	}

	//////////////////////////////////////////////////
	// TEST: testHLAvariantRecordGetOctetBoundary() //
	//////////////////////////////////////////////////
	@Test
	@SuppressWarnings("unchecked")
	public void testHLAvariantRecordGetOctetBoundary()
	{
		HLAvariantRecord data = encoderFactory.createHLAvariantRecord( 
		                        encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );

		// Add some fields to the record
		this.addTestFieldsToRecord( data, true );

		// Octet boundary should be the largest element (The string)
		data.setDiscriminant( this.encoderFactory.createHLAoctet(DISCRIMINANT_TWO) );
		Assert.assertEquals( data.getOctetBoundary(), data.getValue().getEncodedLength() );
	}

	//////////////////////////////////////////////
	// TEST: testHLAvariantRecordEncodeSingle() //
	//////////////////////////////////////////////
	@Test
	@SuppressWarnings("unchecked")
	public void testHLAvariantRecordEncodeSingle()
	{
		HLAvariantRecord data = encoderFactory.createHLAvariantRecord(
		                        encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );

		// Add some fields to the record
		this.addTestFieldsToRecord( data, true );


		data.setDiscriminant( this.encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );
		ByteWrapper byteWrapper = new ByteWrapper( data.getEncodedLength() );
		data.encode( byteWrapper );
		Assert.assertEquals( byteWrapper.array(), TEST_BIN_ONE );

		data.setDiscriminant( this.encoderFactory.createHLAoctet(DISCRIMINANT_TWO) );
		byteWrapper = new ByteWrapper( data.getEncodedLength() );
		data.encode( byteWrapper );
		Assert.assertEquals( byteWrapper.array(), TEST_BIN_TWO );

		data.setDiscriminant( this.encoderFactory.createHLAoctet(DISCRIMINANT_THREE) );
		byteWrapper = new ByteWrapper( data.getEncodedLength() );
		data.encode( byteWrapper );
		Assert.assertEquals( byteWrapper.array(), TEST_BIN_THREE );

		// Attempting to encode beyond the ByteWrapper's bounds should result
		// in an EncoderException
		try
		{
			data.encode( byteWrapper );

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

	/////////////////////////////////////////////////
	// TEST: testHLAvariantRecordEncodeNoVariant() //
	/////////////////////////////////////////////////
	@Test
	@SuppressWarnings("unchecked")
	public void testHLAvariantRecordEncodeNoVariant()
	{
		HLAvariantRecord data = encoderFactory.createHLAvariantRecord(
		                        encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );
		addTestFieldsToRecord( data, true );

		final byte DISCRIMINANT_FOUR = (byte)4;
		data.setVariant( this.encoderFactory.createHLAoctet(DISCRIMINANT_FOUR), null );
		data.setDiscriminant( this.encoderFactory.createHLAoctet(DISCRIMINANT_FOUR) );

		ByteWrapper byteWrapper = new ByteWrapper( data.getEncodedLength() );
		data.encode( byteWrapper );

		final byte[] TEST_BIN_FOUR = { (byte)0x04 };

		Assert.assertEquals( byteWrapper.array(), TEST_BIN_FOUR );
	}

	///////////////////////////////////////////////////////
	// TEST: testHLAvariantRecordEncodeSingleUnderflow() //
	///////////////////////////////////////////////////////
	@Test
	public void testHLAvariantRecordEncodeSingleUnderflow()
	{
		HLAvariantRecord data = encoderFactory.createHLAvariantRecord(
		                        encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );
		addTestFieldsToRecord( data, true );

		// Create a byte wrapper that less capacity than what is required
		ByteWrapper byteWrapper = new ByteWrapper( data.getEncodedLength() - 1 );

		try
		{
			data.encode( byteWrapper );

			// FAIL: An exception should have been thrown
			expectedException( EncoderException.class );
		}
		catch( EncoderException ee )
		{
			// PASS: Expected this exception
		}
		catch( Exception e )
		{
			// FAIL: Wrong exception type
			wrongException( e, EncoderException.class );
		}
	}

	////////////////////////////////////////////////////
	// TEST: testHLAvariantRecordEncodeEmptyWrapper() //
	////////////////////////////////////////////////////
	@Test
	public void testHLAvariantRecordEncodeEmptyWrapper()
	{
		HLAvariantRecord data = encoderFactory.createHLAvariantRecord(
		                        encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );
		addTestFieldsToRecord( data, true );

		// Create an empty byte wrapper
		ByteWrapper byteWrapper = new ByteWrapper();

		try
		{
			data.encode( byteWrapper );

			// FAIL: An exception should have been thrown
			expectedException( EncoderException.class );
		}
		catch( EncoderException ee )
		{
			// PASS: Expected this exception
		}
		catch( Exception e )
		{
			// FAIL: Wrong exception type
			wrongException( e, EncoderException.class );
		}
	}

	////////////////////////////////////////////////
	// TEST: testHLAvariantRecordGetEncodedLength() //
	////////////////////////////////////////////////
	@Test
	@SuppressWarnings("unchecked")
	public void testHLAvariantRecordGetEncodedLength()
	{
		HLAvariantRecord data = encoderFactory.createHLAvariantRecord( 
		                        encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );
		addTestFieldsToRecord( data, true );

		data.setDiscriminant( this.encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );
		Assert.assertEquals( data.getEncodedLength(), TEST_BIN_ONE.length );
		data.setDiscriminant( this.encoderFactory.createHLAoctet(DISCRIMINANT_TWO) );
		Assert.assertEquals( data.getEncodedLength(), TEST_BIN_TWO.length );
		data.setDiscriminant( this.encoderFactory.createHLAoctet(DISCRIMINANT_THREE) );
		Assert.assertEquals( data.getEncodedLength(), TEST_BIN_THREE.length );
	}

	///////////////////////////////////////////
	// TEST: testHLAvariantRecordToByteArray() //
	///////////////////////////////////////////
	@Test
	@SuppressWarnings("unchecked")
	public void testHLAvariantRecordToByteArray()
	{
		HLAvariantRecord data = encoderFactory.createHLAvariantRecord(
		                        encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );
		addTestFieldsToRecord( data, true );

		data.setDiscriminant( this.encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );
		byte[] bytes = data.toByteArray();
		Assert.assertEquals( bytes, TEST_BIN_ONE );
		data.setDiscriminant( this.encoderFactory.createHLAoctet(DISCRIMINANT_TWO) );
		bytes = data.toByteArray();
		Assert.assertEquals( bytes, TEST_BIN_TWO );
		data.setDiscriminant( this.encoderFactory.createHLAoctet(DISCRIMINANT_THREE) );
		bytes = data.toByteArray();
		Assert.assertEquals( bytes, TEST_BIN_THREE );
	}

	/////////////////////////////////////////////////////////
	// TEST: testHLAvariantRecordDecodeByteWrapperSingle() //
	/////////////////////////////////////////////////////////
	@Test
	public void testHLAvariantRecordDecodeByteWrapperSingle()
	{
		HLAvariantRecord data = encoderFactory.createHLAvariantRecord(
		                        encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );
		addTestFieldsToRecord( data, false );

		try
		{
			// Decode binary data into HLAvariantRecord instance
			ByteWrapper byteWrapper = new ByteWrapper( TEST_BIN_ONE );

			data.decode( byteWrapper );

			// Test values imported from the byte array
			HLAfloat32BE elementOne = (HLAfloat32BE)data.getValue();
			Assert.assertEquals( elementOne.getValue(), VALUE_ONE );

			byteWrapper = new ByteWrapper( TEST_BIN_TWO );
			data.decode( byteWrapper );
			HLAASCIIstring elementTwo = (HLAASCIIstring)data.getValue();
			Assert.assertEquals( elementTwo.getValue(), VALUE_TWO );

			byteWrapper = new ByteWrapper( TEST_BIN_THREE );
			data.decode( byteWrapper );
			HLAboolean elementThree = (HLAboolean)data.getValue();
			Assert.assertEquals( elementThree.getValue(), VALUE_THREE );

		}
		catch( Exception e )
		{
			unexpectedException( "Decoding a HLAvariantRecord", e );
		}
	}

	///////////////////////////////////////////////////////////
	// TEST: testHLAvariantRecordDecodeUnknownDiscriminant() //
	///////////////////////////////////////////////////////////
	@Test
	public void testHLAvariantRecordDecodeOversize()
	{
		HLAvariantRecord data = encoderFactory.createHLAvariantRecord(
		                        encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );
		addTestFieldsToRecord( data, false );

		final byte[] TEST_BIN_FOUR = { (byte)0x04 };

		// Decode binary data into HLAvariantRecord instance
		ByteWrapper byteWrapper = new ByteWrapper( TEST_BIN_FOUR );

		try
		{
			data.decode( byteWrapper );

			// FAIL: Expected an exception
			expectedException( DecoderException.class );
		}
		catch( DecoderException de )
		{
			// PASS: This is what we expected
		}
		catch( Exception e )
		{
			// FAIL: Wrong exception type
			wrongException( e, DecoderException.class );
		}
	}

	////////////////////////////////////////////////////////
	// TEST: testHLAvariantRecordDecodeByteWrapperEmpty() //
	////////////////////////////////////////////////////////
	@Test
	public void testHLAvariantRecordDecodeByteWrapperEmpty()
	{
		HLAvariantRecord data = encoderFactory.createHLAvariantRecord( 
		                        encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );
		addTestFieldsToRecord( data, false );

		// Create an empty byte wrapper to decode into
		ByteWrapper byteWrapper = new ByteWrapper();

		try
		{
			data.decode( byteWrapper );

			// FAIL: Expected an exception
			expectedException( DecoderException.class );
		}
		catch( DecoderException de )
		{
			// PASS: This is what we expected
		}
		catch( Exception e )
		{
			// FAIL: Wrong exception type
			wrongException( e, DecoderException.class );
		}
	}

	///////////////////////////////////////////
	// testHLAvariableArrayDecodeByteArray() //
	///////////////////////////////////////////
	@Test
	public void testHLAvariableArrayDecodeByteArray()
	{
		HLAvariantRecord data = encoderFactory.createHLAvariantRecord(
		                        encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );
		addTestFieldsToRecord( data, false );

		// Decode binary data into HLAvariantRecord instance
		try
		{
			data.decode( TEST_BIN_ONE );
			HLAfloat32BE elementOne = (HLAfloat32BE)data.getValue();
			Assert.assertEquals( elementOne.getValue(), VALUE_ONE );

			data.decode( TEST_BIN_TWO );
			HLAASCIIstring elementTwo = (HLAASCIIstring)data.getValue();
			Assert.assertEquals( elementTwo.getValue(), VALUE_TWO );

			data.decode( TEST_BIN_THREE );
			HLAboolean elementThree = (HLAboolean)data.getValue();
			Assert.assertEquals( elementThree.getValue(), VALUE_THREE );

		}
		catch( Exception e )
		{
			unexpectedException( "Decoding a HLAvariantRecord", e );
		}
	}

	////////////////////////////////////////////////
	// testHLAvariantRecordDecodeByteArrayEmpty() //
	////////////////////////////////////////////////
	@Test
	public void testHLAvariantRecordDecodeByteArrayEmpty()
	{
		HLAvariantRecord data = encoderFactory.createHLAvariantRecord(
		                        encoderFactory.createHLAoctet(DISCRIMINANT_ONE) );
		addTestFieldsToRecord( data, false );

		// Create an empty byte array to decode from
		byte[] bytes = new byte[0];

		try
		{
			data.decode( bytes );

			// FAIL: Expected an exception
			expectedException( DecoderException.class );
		}
		catch( DecoderException de )
		{
			// PASS: This is what we expected
		}
		catch( Exception e )
		{
			// FAIL: Wrong exception type
			wrongException( e, DecoderException.class );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
