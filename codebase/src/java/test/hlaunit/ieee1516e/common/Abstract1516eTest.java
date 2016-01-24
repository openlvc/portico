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
package hlaunit.ieee1516e.common;

import org.apache.log4j.Logger;
import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.impl.hla1516e.types.time.DoubleTime;
import org.testng.Assert;

import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAunicodeString;

/**
 * This class provides access to common state and helper methods for tests
 */
public abstract class Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected Logger logger;
	protected EncoderFactory encoder;
	protected TestFederate defaultFederate;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	protected Abstract1516eTest()
	{
		this.logger = Logger.getLogger( "portico." + this.getClass().getSimpleName() );

		// get a reference to the encoder factory to serializing and deserializing things
		try
		{
			this.encoder = RtiFactoryFactory.getRtiFactory().getEncoderFactory();
		}
		catch( Exception e )
		{
			Assert.fail( "Could not get Encoder Factory: "+e.getMessage(), e );
		}
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	/**
	 * This method will log a message with the appropriate formatting
	 */
	public void log( String message )
	{
		System.out.println( "LOG  " + message );
	}

	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Setup/Cleanup Methods ///////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	protected void beforeClass()
	{
		this.defaultFederate = new TestFederate( "defaultFederate", this );
		this.defaultFederate.quickConnect();
	}
	
	protected void afterClass()
	{
		this.defaultFederate.quickDisconnect();
		TestFederate.killActiveFederates();
	}
	
	protected void expectedException( Class<?>... expected )
	{
		// build a string with the name of the expected exception types
		StringBuilder builder = new StringBuilder(expected[0].getName());
		for( int i = 1; i < expected.length; i++ )
		{
			builder.append( " or " );
			builder.append( expected[i].getName() );
		}
		
		Assert.fail( "Didn't receive an exception: Expected " + builder.toString() );
	}
	
	protected void unexpectedException( String activity, Exception e )
	{
		Assert.fail( "Unexpected exception while " + activity + ":" + e.getMessage(), e );
	}
	
	protected void wrongException( Exception received, Class<?>... expected )
	{
		// build a string with the name of the expected exception types
		StringBuilder builder = new StringBuilder(expected[0].getName());
		for( int i = 1; i < expected.length; i++ )
		{
			builder.append( " or " );
			builder.append( expected[i].getName() );
		}
		
		Assert.fail( "Unexpected exception. Expected [" + builder.toString() + "] received [" +
		             received.getClass().getName() + "]: " + received.getMessage(), received );
	}

	/////////////////////////////////////////////////////////////////////////////////////
	/// Encoding / Decoding Helpers    //////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	protected String decodeString( byte[] bytes )
	{
		try
		{
    		HLAunicodeString temp = encoder.createHLAunicodeString();
    		temp.decode( bytes );
    		return temp.getValue();
		}
		catch( Exception e )
		{
			Assert.fail( "Failed to decode HLAunicodeString: "+e.getMessage(), e );
			return null;
		}
	}
	
	protected int decodeHandle( byte[] handle )
	{
		return HLA1516eHandle.decode( handle );
	}
	
	protected double decodeTime( byte[] time )
	{
		return DoubleTime.decode( time );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
