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
import hla.rti1516e.encoding.DataElementFactory;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAASCIIstring;
import hla.rti1516e.encoding.HLAfixedArray;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;

@Test(singleThreaded=true, groups={"HLAfixedArrayTest","datatype","encoding"})
public class HLAfixedArrayTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final String NOTHING = "Nothing";
	
	private static final float[] EMPTY = new float[0];
	private static final byte[] EMPTY_BIN = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00,
	                                                     (byte)0x00 };
	
	private static final float[] THREE_FLOATS = { 3.14159f, 2.71828f, 9.80665f };
	private static final byte[] THREE_FLOATS_BIN = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00,
	                                                            (byte)0x03, (byte)0x40, (byte)0x49, 
	                                                            (byte)0x0f, (byte)0xd0, (byte)0x40, 
	                                                            (byte)0x2d, (byte)0xf8, (byte)0x4d,
	                                                            (byte)0x41, (byte)0x1c, (byte)0xe8, 
	   	                                                 		(byte)0x0a };
	
	private static final String[] THREE_STRINGS = { "Hello World", 
	                                                "Bonjour World", 
	                                                "OH HAI World" };
	private static final byte[] THREE_STRINGS_BIN = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00,
	                                                             (byte)0x03, (byte)0x00, (byte)0x00, 
	                                                             (byte)0x00, (byte)0x0B, (byte)0x48, 
	                                                             (byte)0x65, (byte)0x6C, (byte)0x6C, 
	                                                             (byte)0x6F, (byte)0x20, (byte)0x57, 
	                                                             (byte)0x6F, (byte)0x72, (byte)0x6C, 
	                                                             (byte)0x64, (byte)0x00, (byte)0x00, 
	                                                             (byte)0x00, (byte)0x0D, (byte)0x42, 
	                                                             (byte)0x6F, (byte)0x6E, (byte)0x6A, 
	                                                             (byte)0x6F, (byte)0x75, (byte)0x72, 
	                                                             (byte)0x20, (byte)0x57, (byte)0x6F, 
	                                                             (byte)0x72, (byte)0x6C, (byte)0x64,
	                                                             (byte)0x00, (byte)0x00, (byte)0x00, 
	                                                             (byte)0x0C, (byte)0x4F, (byte)0x48, 
	                                                             (byte)0x20, (byte)0x48, (byte)0x41, 
	                                                             (byte)0x49, (byte)0x20, (byte)0x57, 
	                                                             (byte)0x6F, (byte)0x72, (byte)0x6C, 
	                                                             (byte)0x64 };
	
	private static final int[] TWO_INTS = { 1431655765, -1431655766 };
	private static final byte[] TWO_INTS_BIN = { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x02,
	                                             (byte)0x55, (byte)0x55, (byte)0x55, (byte)0x55,
	                                             (byte)0xAA, (byte)0xAA, (byte)0xAA, (byte)0xAA };
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private EncoderFactory encoderFactory;
	private DataElementFactory<HLAfloat32BE> floatFactory;
	private DataElementFactory<HLAASCIIstring> stringFactory;

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
				
		this.floatFactory = new DataElementFactory<HLAfloat32BE>()
		{
			public HLAfloat32BE createElement( int index )
            {
	            return encoderFactory.createHLAfloat32BE();
            }
		};
		
		this.stringFactory = new DataElementFactory<HLAASCIIstring>()
		{
			public HLAASCIIstring createElement( int index )
			{
				return encoderFactory.createHLAASCIIstring();
			}
		};
	}

	@Override
	@AfterClass(alwaysRun = true)
	public void afterClass()
	{
		this.encoderFactory = null;

		super.afterClass();
	}
	
	private HLAfloat32BE[] wrapFloatData( float... data )
	{
		int length = data.length;
		HLAfloat32BE[] array = new HLAfloat32BE[length];
		for( int i = 0 ; i < length ; ++i )
			array[i] = this.encoderFactory.createHLAfloat32BE( data[i] );
		
		return array;
	}
	
	private HLAASCIIstring[] wrapStringData( String... data )
	{
		int length = data.length;
		HLAASCIIstring[] array = new HLAASCIIstring[length];
		for( int i = 0 ; i < length ; ++i )
			array[i] = this.encoderFactory.createHLAASCIIstring( data[i] );
		
		return array;
	}
	
	private HLAinteger32BE[] wrapIntData( int... data )
	{
		int length = data.length;
		HLAinteger32BE[] array = new HLAinteger32BE[length];
		for( int i = 0 ; i < length ; ++i )
			array[i] = this.encoderFactory.createHLAinteger32BE( data[i] );
		
		return array;
	}
	
	private byte[] getCombinedTestArray()
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream( 4 +
		                                                         THREE_FLOATS_BIN.length + 
		                                                         THREE_STRINGS_BIN.length +
		                                                         TWO_INTS.length );
		try
		{
			bytes.write( THREE_FLOATS_BIN );
			bytes.write( THREE_STRINGS_BIN );
			bytes.write( TWO_INTS_BIN );
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

	/////////////////////////////////////
	// TEST: testHLAfixedArrayCreate() //
	/////////////////////////////////////
	@Test
	public void testHLAfixedArrayCreate()
	{
		// Size constructor
		HLAfixedArray<HLAfloat32BE> sizeConstructor = 
			this.encoderFactory.createHLAfixedArray( this.floatFactory, 5 );
		
		Assert.assertNotNull( sizeConstructor );
		Assert.assertEquals( sizeConstructor.size(), 5 );
		
		// Zero size constructor
		HLAfixedArray<HLAfloat32BE> zeroSizeConstructor = 
			this.encoderFactory.createHLAfixedArray( this.floatFactory, 0 );
		
		Assert.assertNotNull( zeroSizeConstructor );
		Assert.assertEquals( zeroSizeConstructor.size(), 0 );

		// Value constructor
		HLAfloat32BE[] testOne = wrapFloatData( THREE_FLOATS );
		HLAfixedArray<HLAfloat32BE> valueConstructor = 
			this.encoderFactory.createHLAfixedArray( testOne );
		
		Assert.assertNotNull( valueConstructor );
		Assert.assertEquals( valueConstructor.size(), THREE_FLOATS.length );
		
		// Value constructor with empty list
		HLAfixedArray<HLAfloat32BE> valueConstructorEmpty = 
			this.encoderFactory.createHLAfixedArray();
		Assert.assertNotNull( valueConstructorEmpty );
		Assert.assertEquals( valueConstructorEmpty.size(), 0 );
	}

    /////////////////////////////////////////////
    // TEST: testHLAfixedArrayGetOutOfBounds() //
    /////////////////////////////////////////////
	@Test
	public void testHLAfixedArrayGetOutOfBounds()
	{
		// Create an array that initially holds three strings
		HLAASCIIstring[] strings = wrapStringData( THREE_STRINGS );
		HLAfixedArray<HLAASCIIstring> array = 
			this.encoderFactory.createHLAfixedArray( this.stringFactory, 3 );
		
		try
		{
			// Attempting to access an OOB element should throw an IndexOutOfBoundsException
			array.get( strings.length );
			
			// FAIL: Expected an Exception
			expectedException( IndexOutOfBoundsException.class );
		}
		catch( IndexOutOfBoundsException aioobe )
		{
			// PASS: This was expected
		}
		catch( Exception e )
		{
			// FAIL: Wrong exception
			wrongException( e, IndexOutOfBoundsException.class );
		}
	}
	
	//////////////////////////////////////////////
	// TEST: testHLAfixedArrayGetOctetBoundary() //
	//////////////////////////////////////////////
	@Test
	public void testHLAfixedArrayGetOctetBoundary()
	{
		HLAASCIIstring[] strings = wrapStringData( THREE_STRINGS );
		HLAfixedArray data = this.encoderFactory.createHLAfixedArray( strings );
		
		// Bonjour World (element #1) is the largest string
		Assert.assertEquals( data.getOctetBoundary(), strings[1].getEncodedLength() );
	}

	///////////////////////////////////////////
	// TEST: testHLAfixedArrayEncodeSingle() //
	///////////////////////////////////////////
	@Test
	public void testHLAfixedArrayEncodeSingle()
	{
		HLAfixedArray<HLAfloat32BE> data = 
			this.encoderFactory.createHLAfixedArray( wrapFloatData(THREE_FLOATS) );

		// Encode with a wrapper that is exactly the right size
		int length = data.getEncodedLength();
		ByteWrapper wrapper = new ByteWrapper( length );
		Assert.assertEquals( wrapper.getPos(), 0 );

		// Does the position increment by the expected amount?
		data.encode( wrapper );
		Assert.assertEquals( wrapper.getPos(), length );

		// Are the contents of the ByteWrapper what is expected?
		Assert.assertEquals( wrapper.array(), THREE_FLOATS_BIN );

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

    ////////////////////////////////////////////////////
    // TEST: testHLAfixedArrayEncodeSingleUnderflow() //
    ////////////////////////////////////////////////////
    @Test
    public void testHLAfixedArrayEncodeSingleUnderflow()
    {
    	try
    	{
        	HLAfixedArray<HLAfloat32BE> data = 
    			this.encoderFactory.createHLAfixedArray( wrapFloatData(THREE_FLOATS) );
    
    		// Encode with a wrapper that has less than the required size
    		int length = data.getEncodedLength();
    		ByteWrapper wrapper = new ByteWrapper( length - 1  );
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
	
	/////////////////////////////////////////////
	// TEST: testHLAfixedArrayEncodeMultiple() //
	/////////////////////////////////////////////
	@Test
	public void testHLAfixedArrayEncodeMultiple()
	{
		byte[] dataRaw = getCombinedTestArray();
		
		HLAfloat32BE[] threeFloats = wrapFloatData( THREE_FLOATS );
		HLAASCIIstring[] threeStrings = wrapStringData( THREE_STRINGS );
		HLAinteger32BE[] twoInts = wrapIntData( TWO_INTS );
		
		HLAfixedArray[] data = { this.encoderFactory.createHLAfixedArray(threeFloats),
		                         this.encoderFactory.createHLAfixedArray(threeStrings),
		                         this.encoderFactory.createHLAfixedArray(twoInts) };

		// Encode with a wrapper that can contain up to 3 of this type
		ByteWrapper wrapper = new ByteWrapper( dataRaw.length );
		int expectedPosition = 0;
		for( int i = 0; i < data.length; ++i )
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
		HLAfixedArray extra = this.encoderFactory.createHLAfixedArray();
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

	/////////////////////////////////////////////////
	// TEST: testHLAfixedArrayEncodeEmptyWrapper() //
	/////////////////////////////////////////////////
	@Test
	public void testHLAfixedArrayEncodeEmptyWrapper()
	{
		HLAfixedArray data = this.encoderFactory.createHLAfixedArray();
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
	// TEST: testHLAfixedArrayGetEncodedLength() //
	//////////////////////////////////////////////
	@Test
	public void testHLAfixedArrayGetEncodedLength()
	{
		HLAfixedArray<HLAfloat32BE> data = 
			this.encoderFactory.createHLAfixedArray( wrapFloatData(THREE_FLOATS) );
		Assert.assertEquals( data.getEncodedLength(), THREE_FLOATS_BIN.length );
	}

	/////////////////////////////////////////
	// TEST: testHLAfixedArrayToByteArray() //
	/////////////////////////////////////////
	@Test
	public void testHLAfixedArrayToByteArray()
	{
		HLAfixedArray<HLAfloat32BE> data = 
			this.encoderFactory.createHLAfixedArray( wrapFloatData(THREE_FLOATS) );
		byte[] asByteArray = data.toByteArray();

		Assert.assertEquals( asByteArray, THREE_FLOATS_BIN );
	}

	//////////////////////////////////////////////////////
	// TEST: testHLAfixedArrayDecodeByteWrapperSingle() //
	//////////////////////////////////////////////////////
	@Test
	public void testHLAfixedArrayDecodeByteWrapperSingle()
	{
		// Create a ByteWrapper with data for a single type contained within
		ByteWrapper wrapper = new ByteWrapper( THREE_FLOATS_BIN );

		// Create the object to decode into
		HLAfixedArray<HLAfloat32BE> data = 
			this.encoderFactory.createHLAfixedArray( this.floatFactory, THREE_FLOATS.length );

		try
		{
			data.decode( wrapper );
			
			// Is the result the correct size?
			Assert.assertEquals( data.size(), THREE_FLOATS.length );
			
			// Are the elements in the array what we expected?
			for( int i = 0 ; i < THREE_FLOATS.length ; ++i )
			{
				HLAfloat32BE element = data.get( i );
				Assert.assertEquals( element.getValue(), THREE_FLOATS[i] );
			}
				
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAfixedArray", e );
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
	// TEST: testHLAfixedArrayDecodeByteWrapperEmpty() //
	/////////////////////////////////////////////////////
	@Test
	public void testHLAfixedArrayDecodeByteWrapperEmpty()
	{
		HLAfixedArray<HLAfloat32BE> data = this.encoderFactory.createHLAfixedArray();
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

	////////////////////////////////////////
	// testHLAfixedArrayDecodeByteArray() //
	////////////////////////////////////////
	@Test
	public void testHLAfixedArrayDecodeByteArray()
	{
		// Create the object to decode into
		HLAfixedArray<HLAASCIIstring> data = 
			this.encoderFactory.createHLAfixedArray( this.stringFactory, THREE_STRINGS.length );
		
		for( HLAASCIIstring element : data )
			element.setValue( NOTHING );

		try
		{
			data.decode( THREE_STRINGS_BIN );
			
			// Is the result the correct size?
			Assert.assertEquals( data.size(), THREE_STRINGS.length );
			
			// Are the elements in the array what we expected?
			for( int i = 0 ; i < THREE_STRINGS.length ; ++i )
			{
				HLAASCIIstring element = data.get( i );
				Assert.assertFalse( element.getValue().equals(NOTHING) );
				Assert.assertEquals( element.getValue(), THREE_STRINGS[i] );
			}
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAfixedArray", e );
		}
	}

	/////////////////////////////////////////////
	// testHLAfixedArrayDecodeByteArrayEmpty() //
	/////////////////////////////////////////////
	@Test
	public void testHLAfixedArrayDecodeByteArrayEmpty()
	{
		HLAfixedArray<HLAfloat32BE> data = this.encoderFactory.createHLAfixedArray();
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
	
    ////////////////////////////////////////////
    // testHLAfixedArrayDecodeIntoUndersize() //
    ////////////////////////////////////////////
	@Test
	public void testHLAfixedArrayDecodeIntoUndersize()
	{
		// Create an array that holds less than what is coming in off the wire
		HLAfixedArray<HLAfloat32BE> data = 
			this.encoderFactory.createHLAfixedArray( this.floatFactory, THREE_FLOATS.length - 1 );
		
		try
		{
			data.decode( THREE_FLOATS_BIN );

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
	
    ////////////////////////////////////////////
    // testHLAfixedArrayDecodeIntoOversize() //
    ////////////////////////////////////////////
	@Test
	public void testHLAfixedArrayDecodeIntoOversize()
	{
		// Create an array that holds more than what is coming in off the wire
		HLAfixedArray<HLAfloat32BE> data = 
			this.encoderFactory.createHLAfixedArray( this.floatFactory, THREE_FLOATS.length + 1 );
		
		try
		{
			data.decode( THREE_FLOATS_BIN );

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
