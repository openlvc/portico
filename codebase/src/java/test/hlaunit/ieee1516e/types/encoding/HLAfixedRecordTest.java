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
import hla.rti1516e.encoding.HLAboolean;
import hla.rti1516e.encoding.HLAfixedRecord;
import hla.rti1516e.encoding.HLAfloat32BE;
import hla.rti1516e.encoding.HLAfloat64BE;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;

@Test(singleThreaded=true, groups={"HLAfixedRecordTest","datatype","encoding"})
public class HLAfixedRecordTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final float VALUE_ONE = 3.14159f;
	public static final String VALUE_TWO = "Hello World";
	public static final boolean VALUE_THREE = true;
	
	private static final byte[] TEST_BIN = { (byte)0x40, (byte)0x49, (byte)0x0f, (byte)0xd0, 
	                                         (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0B, 
	                                         (byte)0x48, (byte)0x65, (byte)0x6C, (byte)0x6C, 
	                                         (byte)0x6F, (byte)0x20, (byte)0x57, (byte)0x6F,
                                             (byte)0x72, (byte)0x6C, (byte)0x64, (byte)0x00, 
                                             (byte)0x00, (byte)0x00, (byte)0x01 };
	
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
		
//		this.encoderFactory = new HLA1516eEncoderFactory();
	}

	@Override
	@AfterClass(alwaysRun = true)
	public void afterClass()
	{
		this.encoderFactory = null;

		super.afterClass();
	}
	
	private void addTestFieldsToRecord( HLAfixedRecord record, boolean setValues )
	{
		if( setValues )
		{
			record.add( this.encoderFactory.createHLAfloat32BE(VALUE_ONE) );
    		record.add( this.encoderFactory.createHLAASCIIstring(VALUE_TWO) );
    		record.add( this.encoderFactory.createHLAboolean(VALUE_THREE) );
		}
		else
		{
    		record.add( this.encoderFactory.createHLAfloat32BE() );
    		record.add( this.encoderFactory.createHLAASCIIstring() );
    		record.add( this.encoderFactory.createHLAboolean() );
		}
	}
		
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////// Test Methods //////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testHlaFixedRecord() throws DecoderException
	{
		// Create a new fixed record representing a position
		HLAfixedRecord before = encoderFactory.createHLAfixedRecord();
		before.add( encoderFactory.createHLAfloat64BE() );
		before.add( encoderFactory.createHLAfloat64BE() );
		before.add( encoderFactory.createHLAfloat64BE() );
		
		// Populate it
		double[] doubles = new double[]{ 12.3, 23.4, 34.5 };
		for( int i = 0; i < 3; i++ )
		{
			((HLAfloat64BE)before.get(i)).setValue( doubles[i] );
		}
		
		// Encode it
		byte[] bytes = before.toByteArray();

		// Decode it
		HLAfixedRecord after = encoderFactory.createHLAfixedRecord();
		after.add( encoderFactory.createHLAfloat64BE() );
		after.add( encoderFactory.createHLAfloat64BE() );
		after.add( encoderFactory.createHLAfloat64BE() );
		after.decode( bytes );

		// Compare the results
		for( int i = 0; i < 3; i++ )
		{
			double afterValue = ((HLAfloat64BE)after.get(i)).getValue();
			Assert.assertEquals( afterValue, doubles[i] );
		}
	}
	
    //////////////////////////////////////////////
    // TEST: testHLAfixedRecordGetOutOfBounds() //
    //////////////////////////////////////////////
    @Test
    public void testHLAfixedRecordGetOutOfBounds()
    {
    	HLAfixedRecord data = this.encoderFactory.createHLAfixedRecord();
    	
    	// Add some fields to the record
    	this.addTestFieldsToRecord( data, false );
    	
    	try
    	{
    		// Try and access an element that doesn't exist
    		data.get( 3 );
    		
    		// FAIL: An exception should have been thrown
    		expectedException( IndexOutOfBoundsException.class );
    	}
    	catch( IndexOutOfBoundsException ioobe )
    	{
    		// PASS: This is the exception that was expected
    	}
    	catch( Exception e )
    	{
    		// FAIL: Wrong exception type
    		wrongException( e, IndexOutOfBoundsException.class );
    	}
    }
    
    ////////////////////////////////////////////////
    // TEST: testHLAfixedRecordGetOctetBoundary() //
    ////////////////////////////////////////////////
    @Test
    public void testHLAfixedRecordGetOctetBoundary()
    {
    	HLAfixedRecord data = this.encoderFactory.createHLAfixedRecord();
    	
    	// Add some fields to the record
    	this.addTestFieldsToRecord( data, true );
    	
    	// Octet boundary should be the largest element (The string)
    	Assert.assertEquals( data.getOctetBoundary(), data.get(1).getEncodedLength() );
    }
    
    ////////////////////////////////////////////
    // TEST: testHLAfixedRecordEncodeSingle() //
    ////////////////////////////////////////////
    @Test
    public void testHLAfixedRecordEncodeSingle()
    {
    	HLAfixedRecord data = this.encoderFactory.createHLAfixedRecord();
    	
    	// Add some fields to the record
    	this.addTestFieldsToRecord( data, true );
    	
    	
    	ByteWrapper byteWrapper = new ByteWrapper( data.getEncodedLength() );
    	data.encode( byteWrapper );
    	
    	Assert.assertEquals( byteWrapper.array(), TEST_BIN );
    	
    	// Attempting to encode beyond the ByteWrapper's bounds should result in an EncoderException
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
    
    //////////////////////////////////////////////
    // TEST: testHLAfixedRecordEncodeNoFields() //
    //////////////////////////////////////////////
    @Test
    public void testHLAfixedRecordEncodeNoFields()
    {
    	HLAfixedRecord data = this.encoderFactory.createHLAfixedRecord();
    	
    	ByteWrapper byteWrapper = new ByteWrapper( data.getEncodedLength() );
    	
    	try
    	{
    		// Encoding a record with no fields should throw an exception
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
    
    /////////////////////////////////////////////////////
    // TEST: testHLAfixedRecordEncodeSingleUnderflow() //
    /////////////////////////////////////////////////////
    @Test
    public void testHLAfixedRecordEncodeSingleUnderflow()
    {
    	HLAfixedRecord data = this.encoderFactory.createHLAfixedRecord();
    	addTestFieldsToRecord( data, true );
    	
    	// Create a byte wrapper that less capacity than what is required
    	ByteWrapper byteWrapper = new ByteWrapper( data.getEncodedLength() -1 );
    	
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
        
    //////////////////////////////////////////////////
    // TEST: testHLAfixedRecordEncodeEmptyWrapper() //
    //////////////////////////////////////////////////
    @Test
    public void testHLAfixedRecordEncodeEmptyWrapper()
    {
    	HLAfixedRecord data = this.encoderFactory.createHLAfixedRecord();
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
    // TEST: testHLAfixedRecordGetEncodedLength() //
    ////////////////////////////////////////////////
    @Test
    public void testHLAfixedRecordGetEncodedLength()
    {
    	HLAfixedRecord data = this.encoderFactory.createHLAfixedRecord();
    	addTestFieldsToRecord( data, true );
    	
    	Assert.assertEquals( data.getEncodedLength(), TEST_BIN.length );
    }
    
    ///////////////////////////////////////////
    // TEST: testHLAfixedRecordToByteArray() //
    ///////////////////////////////////////////
    @Test
    public void testHLAfixedRecordToByteArray()
    {
    	HLAfixedRecord data = this.encoderFactory.createHLAfixedRecord();
    	addTestFieldsToRecord( data, true );
    	
    	byte[] bytes = data.toByteArray();
    	Assert.assertEquals( bytes, TEST_BIN );
    }
    
    ///////////////////////////////////////////////////////
    // TEST: testHLAfixedRecordDecodeByteWrapperSingle() //
    ///////////////////////////////////////////////////////
    @Test
    public void testHLAfixedRecordDecodeByteWrapperSingle()
    {
    	HLAfixedRecord data = this.encoderFactory.createHLAfixedRecord();
    	addTestFieldsToRecord( data, true );
    	
    	// Decode binary data into HLAfixedRecord instance
    	ByteWrapper byteWrapper = new ByteWrapper( TEST_BIN );
    	
    	try
    	{
    		data.decode( byteWrapper );
    		
        	// Should still be 3 fields
        	Assert.assertEquals( data.size(), 3 );
        	
        	// Test values imported from the byte array
        	HLAfloat32BE elementOne = (HLAfloat32BE)data.get( 0 );
        	Assert.assertEquals( elementOne.getValue(), VALUE_ONE );
        	
        	HLAASCIIstring elementTwo = (HLAASCIIstring)data.get( 1 );
        	Assert.assertEquals( elementTwo.getValue(), VALUE_TWO );
        	
        	HLAboolean elementThree = (HLAboolean)data.get( 2 );
        	Assert.assertEquals( elementThree.getValue(), VALUE_THREE );
    		
    	}
    	catch( Exception e )
    	{
    		unexpectedException( "Decoding a HLAfixedRecord", e );
    	}
    }
    
    //////////////////////////////////////////////
    // TEST: testHLAfixedRecordDecodeOversize() //
    //////////////////////////////////////////////
    @Test
    public void testHLAfixedRecordDecodeOversize()
    {
    	HLAfixedRecord data = this.encoderFactory.createHLAfixedRecord();
    	addTestFieldsToRecord( data, true );
    	
    	// Add an extra field that we don't have binary data for
    	data.add( this.encoderFactory.createHLAinteger64BE() );
    	
    	// Decode binary data into HLAfixedRecord instance
    	ByteWrapper byteWrapper = new ByteWrapper( TEST_BIN );
    	
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
    
    ////////////////////////////////////////////////////
    // TEST: testHLAfixedRecordDecodeByteWrapperEmpty() //
    ////////////////////////////////////////////////////
    @Test
    public void testHLAfixedRecordDecodeByteWrapperEmpty()
    {
    	HLAfixedRecord data = this.encoderFactory.createHLAfixedRecord();
    	addTestFieldsToRecord( data, true );
    	    	
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
    	HLAfixedRecord data = this.encoderFactory.createHLAfixedRecord();
    	addTestFieldsToRecord( data, true );
    	
    	// Decode binary data into HLAfixedRecord instance
    	try
    	{
    		data.decode( TEST_BIN );
    		
        	// Should still be 3 fields
        	Assert.assertEquals( data.size(), 3 );
        	
        	// Test values imported from the byte array
        	HLAfloat32BE elementOne = (HLAfloat32BE)data.get( 0 );
        	Assert.assertEquals( elementOne.getValue(), VALUE_ONE );
        	
        	HLAASCIIstring elementTwo = (HLAASCIIstring)data.get( 1 );
        	Assert.assertEquals( elementTwo.getValue(), VALUE_TWO );
        	
        	HLAboolean elementThree = (HLAboolean)data.get( 2 );
        	Assert.assertEquals( elementThree.getValue(), VALUE_THREE );
    		
    	}
    	catch( Exception e )
    	{
    		unexpectedException( "Decoding a HLAfixedRecord", e );
    	}
    }
    
    //////////////////////////////////////////////
    // testHLAfixedRecordDecodeByteArrayEmpty() //
    //////////////////////////////////////////////
	@Test
	public void testHLAfixedRecordDecodeByteArrayEmpty()
	{
		HLAfixedRecord data = this.encoderFactory.createHLAfixedRecord();
    	addTestFieldsToRecord( data, true );
    	    	
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
