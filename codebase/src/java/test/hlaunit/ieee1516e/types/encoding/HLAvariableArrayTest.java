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

import org.portico.impl.hla1516e.types.encoding.HLA1516eEncoderFactory;
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
import hla.rti1516e.encoding.HLAvariableArray;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAinteger32BE;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;

@Test(singleThreaded=true, groups={"HLAvariableArrayTest","datatype","encoding"})
public class HLAvariableArrayTest extends Abstract1516eTest
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
	private DataElementFactory<HLAinteger32BE> intFactory;

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
		
		this.intFactory = new DataElementFactory<HLAinteger32BE>()
		{
			public HLAinteger32BE createElement( int index )
			{
				return encoderFactory.createHLAinteger32BE();
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

	////////////////////////////////////////
	// TEST: testHLAvariableArrayCreate() //
	////////////////////////////////////////
	@Test
	public void testHLAvariableArrayCreate()
	{
		// Value constructor
		HLAfloat32BE[] testOne = wrapFloatData( THREE_FLOATS );
		HLAvariableArray<HLAfloat32BE> valueConstructor = 
			this.encoderFactory.createHLAvariableArray( this.floatFactory, testOne );
		
		Assert.assertNotNull( valueConstructor );
		Assert.assertEquals( valueConstructor.size(), THREE_FLOATS.length );
		
		// Value constructor with empty list
		HLAvariableArray<HLAfloat32BE> valueConstructorEmpty = 
			this.encoderFactory.createHLAvariableArray( this.floatFactory );
		Assert.assertNotNull( valueConstructorEmpty );
		Assert.assertEquals( valueConstructorEmpty.size(), 0 );
	}
	
    /////////////////////////////////////
    // TEST: testHLAvariableArrayGet() //
    /////////////////////////////////////
	@Test
	public void testHLAvariableArrayGet()
	{
		HLAASCIIstring[] strings = wrapStringData( THREE_STRINGS );
		HLAvariableArray<HLAASCIIstring> array = 
			this.encoderFactory.createHLAvariableArray( this.stringFactory, strings );
				
		for( int i = 0 ; i < strings.length ; ++i )
		{
			HLAASCIIstring element = array.get( i );
			Assert.assertEquals( element, strings[i] );
		}
	}
	
    ////////////////////////////////////////////////
    // TEST: testHLAvariableArrayGetOutOfBounds() //
    ////////////////////////////////////////////////
	@Test
	public void testHLAvariableArrayGetOutOfBounds()
	{
		// Create an array that initially holds three strings
		HLAASCIIstring[] strings = wrapStringData( THREE_STRINGS );
		HLAvariableArray<HLAASCIIstring> array = 
			this.encoderFactory.createHLAvariableArray( this.stringFactory, strings );
		
		try
		{
			// Attempting to access an OOB element should throw an Exception
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
			// PASS: This was expected
		}
	}
	
    /////////////////////////////////////
    // TEST: testHLAvariableArrayAdd() //
    /////////////////////////////////////
	@Test
	public void testHLAvariableArrayAdd()
	{
		HLAvariableArray<HLAASCIIstring> array = 
			this.encoderFactory.createHLAvariableArray( this.stringFactory );
		HLAASCIIstring[] strings = wrapStringData( THREE_STRINGS );
		
		Assert.assertEquals( array.size(), 0 );
		
		for( int i = 0 ; i < strings.length ; ++i )
		{
			array.addElement( strings[i] );
			
			Assert.assertEquals( array.size(), i + 1 );
			Assert.assertEquals( array.get(i), strings[i] );
		}
	}

    ////////////////////////////////////////////
    // TEST: testHLAvariableArrayResizeOver() //
    ////////////////////////////////////////////
	@Test
	public void testHLAvariableArrayResizeOver()
	{
		// Create an array that initially holds three strings
		HLAASCIIstring[] strings = wrapStringData( THREE_STRINGS );
		HLAvariableArray<HLAASCIIstring> array = 
			this.encoderFactory.createHLAvariableArray( this.stringFactory, strings );
		
		Assert.assertEquals( array.size(), strings.length );
		
		// Resize the array to be larger than the existing capacity
		int newLength = array.size() + 1;
		array.resize( newLength );
		Assert.assertEquals( array.size(), newLength );
		
		// Are the original strings still present?
		for( int i = 0 ; i < strings.length ; ++i )
		{
			HLAASCIIstring element = array.get( i );
			Assert.assertEquals( element, strings[i] );
		}
		
		// The element added by resize should not be null
		Assert.assertNotNull( array.get(newLength - 1) );
	}
	
    /////////////////////////////////////////////
    // TEST: testHLAvariableArrayResizeUnder() //
    /////////////////////////////////////////////
	@Test
	public void testHLAvariableArrayResizeUnder()
	{
		// Create an array that initially holds three strings
		HLAASCIIstring[] strings = wrapStringData( THREE_STRINGS );
		HLAvariableArray<HLAASCIIstring> array = 
			this.encoderFactory.createHLAvariableArray( this.stringFactory, strings );
		
		Assert.assertEquals( array.size(), strings.length );
		
		// Resize the array to be smaller than the existing capacity
		int newLength = array.size() - 1;
		array.resize( newLength );
		Assert.assertEquals( array.size(), newLength );
		
		// Are the original strings still present?
		for( int i = 0 ; i < newLength ; ++i )
		{
			HLAASCIIstring element = array.get( i );
			Assert.assertEquals( element, strings[i] );
		}
	}
	
	//////////////////////////////////////////////////
	// TEST: testHLAvariableArrayGetOctetBoundary() //
	//////////////////////////////////////////////////
	@Test
	public void testHLAvariableArrayGetOctetBoundary()
	{
		HLAASCIIstring[] strings = wrapStringData( THREE_STRINGS );
		HLAvariableArray data = this.encoderFactory.createHLAvariableArray( this.stringFactory,
		                                                                    strings );
		
		// "Bonjour World" (Element at index 1) is the largest string
		Assert.assertEquals( data.getOctetBoundary(), strings[1].getEncodedLength() );
		
	}

	//////////////////////////////////////////////
	// TEST: testHLAvariableArrayEncodeSingle() //
	//////////////////////////////////////////////
	@Test
	public void testHLAvariableArrayEncodeSingle()
	{
		HLAvariableArray<HLAfloat32BE> data = 
			this.encoderFactory.createHLAvariableArray( this.floatFactory, 
			                                            wrapFloatData(THREE_FLOATS) );

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
	
	////////////////////////////////////////////////
	// TEST: testHLAvariableArrayEncodeMultiple() //
	////////////////////////////////////////////////
	@Test
	public void testHLAvariableArrayEncodeMultiple()
	{
		byte[] dataRaw = getCombinedTestArray();
		
		HLAfloat32BE[] threeFloats = wrapFloatData( THREE_FLOATS );
		HLAASCIIstring[] threeStrings = wrapStringData( THREE_STRINGS );
		HLAinteger32BE[] twoInts = wrapIntData( TWO_INTS );
		
		HLAvariableArray<HLAfloat32BE> floatArray = 
			this.encoderFactory.createHLAvariableArray( this.floatFactory, threeFloats ); 
		
		HLAvariableArray<HLAASCIIstring> stringArray =
			this.encoderFactory.createHLAvariableArray( this.stringFactory, threeStrings );
		
		HLAvariableArray<HLAinteger32BE> intArray =
			this.encoderFactory.createHLAvariableArray( this.intFactory, twoInts );
		
		HLAvariableArray[] data = { floatArray, stringArray, intArray };

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
		HLAvariableArray<HLAfloat32BE> extra = 
			this.encoderFactory.createHLAvariableArray( this.floatFactory );
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

	////////////////////////////////////////////////////
	// TEST: testHLAvariableArrayEncodeEmptyWrapper() //
	////////////////////////////////////////////////////
	@Test
	public void testHLAvariableArrayEncodeEmptyWrapper()
	{
		HLAvariableArray<HLAfloat32BE> data = 
			this.encoderFactory.createHLAvariableArray( this.floatFactory );
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

	//////////////////////////////////////////////////
	// TEST: testHLAvariableArrayGetEncodedLength() //
	//////////////////////////////////////////////////
	@Test
	public void testHLAvariableArrayGetEncodedLength()
	{
		HLAfloat32BE[] floats = wrapFloatData( THREE_FLOATS );
		HLAvariableArray<HLAfloat32BE> data = 
			this.encoderFactory.createHLAvariableArray( this.floatFactory, floats );
		Assert.assertEquals( data.getEncodedLength(), THREE_FLOATS_BIN.length );
	}

	/////////////////////////////////////////
	// TEST: testHLAvariableArrayToByteArray() //
	/////////////////////////////////////////
	@Test
	public void testHLAvariableArrayToByteArray()
	{
		HLAfloat32BE[] floats = wrapFloatData( THREE_FLOATS );
		HLAvariableArray<HLAfloat32BE> data = 
			this.encoderFactory.createHLAvariableArray( this.floatFactory, floats );
		byte[] asByteArray = data.toByteArray();

		Assert.assertEquals( asByteArray, THREE_FLOATS_BIN );
	}

	/////////////////////////////////////////////////////////
	// TEST: testHLAvariableArrayDecodeByteWrapperSingle() //
	/////////////////////////////////////////////////////////
	@Test
	public void testHLAvariableArrayDecodeByteWrapperSingle()
	{
		// Create a ByteWrapper with data for a single type contained within
		ByteWrapper wrapper = new ByteWrapper( THREE_FLOATS_BIN );

		// Create the object to decode into
		HLAvariableArray<HLAfloat32BE> data = 
			this.encoderFactory.createHLAvariableArray( this.floatFactory );

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
			unexpectedException( "Decoding an HLAvariableArray", e );
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
	
	////////////////////////////////////////////////////
	// TEST: testHLAvariableArrayDecodeByteWrapperEmpty() //
	////////////////////////////////////////////////////
	@Test
	public void testHLAvariableArrayDecodeByteWrapperEmpty()
	{
		HLAvariableArray<HLAfloat32BE> data = 
			this.encoderFactory.createHLAvariableArray( this.floatFactory );
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

	///////////////////////////////////////////
	// testHLAvariableArrayDecodeByteArray() //
	///////////////////////////////////////////
	@Test
	public void testHLAvariableArrayDecodeByteArray()
	{
		// Create the object to decode into
		HLAvariableArray<HLAASCIIstring> data = 
			this.encoderFactory.createHLAvariableArray( this.stringFactory );
		
		try
		{
			data.decode( THREE_STRINGS_BIN );
			
			// Is the result the correct size?
			Assert.assertEquals( data.size(), THREE_STRINGS.length );
			
			// Are the elements in the array what we expected?
			for( int i = 0 ; i < THREE_STRINGS.length ; ++i )
			{
				HLAASCIIstring element = data.get( i );
				Assert.assertEquals( element.getValue(), THREE_STRINGS[i] );
			}
		}
		catch( Exception e )
		{
			// FAIL: not expecting an exception here
			unexpectedException( "Decoding an HLAvariableArray", e );
		}
	}

	/////////////////////////////////////////////
	// testHLAvariableArrayDecodeByteArrayEmpty() //
	/////////////////////////////////////////////
	@Test
	public void testHLAvariableArrayDecodeByteArrayEmpty()
	{
		HLAvariableArray<HLAfloat32BE> data = 
			this.encoderFactory.createHLAvariableArray( this.floatFactory );
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
	
    ///////////////////////////////////////////////
    // testHLAvariableArrayDecodeIntoUndersize() //
    ///////////////////////////////////////////////
	@Test
	public void testHLAvariableArrayDecodeIntoUndersize()
	{		
		// Create an array that holds less than what is coming in off the wire
		HLAfloat32BE[] twoFloats = { this.floatFactory.createElement(0), 
		                             this.floatFactory.createElement(1) };
		HLAvariableArray<HLAfloat32BE> data = 
			this.encoderFactory.createHLAvariableArray( this.floatFactory, twoFloats );
		
		try
		{
			data.decode( THREE_FLOATS_BIN );

			// Should now contain 3 elements
			Assert.assertEquals( data.size(), THREE_FLOATS.length );
			
			// Element should all be the same as the floats contained within the binary data
			for( int i = 0 ; i < THREE_FLOATS.length ; ++i )
			{
				HLAfloat32BE element = data.get( i );
				Assert.assertEquals( element.getValue(), THREE_FLOATS[i] );
			}
		}
		catch( Exception e )
		{
			// FAIL: Did not expect an exception
			unexpectedException( "Decoding a HLAvariableArray", e );
		}
	}
	
    ////////////////////////////////////////////
    // testHLAvariableArrayDecodeIntoOversize() //
    ////////////////////////////////////////////
	@Test
	public void testHLAvariableArrayDecodeIntoOversize()
	{		
		// Create an array that holds more than what is coming in off the wire
		HLAfloat32BE[] fourFloats = { this.floatFactory.createElement(0), 
		                              this.floatFactory.createElement(1),
		                              this.floatFactory.createElement(2),
		                              this.floatFactory.createElement(3)};
		HLAvariableArray<HLAfloat32BE> data = 
			this.encoderFactory.createHLAvariableArray( this.floatFactory, fourFloats );
		
		try
		{
			data.decode( THREE_FLOATS_BIN );

			// Should now contain 3 elements
			Assert.assertEquals( data.size(), THREE_FLOATS.length );
			
			// Element should all be the same as the floats contained within the binary data
			for( int i = 0 ; i < THREE_FLOATS.length ; ++i )
			{
				HLAfloat32BE element = data.get( i );
				Assert.assertEquals( element.getValue(), THREE_FLOATS[i] );
			}
		}
		catch( Exception e )
		{
			// FAIL: Did not expect an exception
			unexpectedException( "Decoding a HLAvariableArray", e );
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
