/*
 *   Copyright 2007 The Portico Project
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
package hlaunit.hla1516.common;

import org.apache.log4j.Logger;

import org.testng.Assert;

/**
 * This class provides access to common state and helper methods for tests
 */
public abstract class Abstract1516Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected Logger logger;
	protected TestFederate defaultFederate;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	protected Abstract1516Test()
	{
		this.logger = Logger.getLogger( "portico." + this.getClass().getSimpleName() );
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
	}
	
	protected void afterClass()
	{
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
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
