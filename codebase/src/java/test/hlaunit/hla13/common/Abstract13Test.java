/*
 *   Copyright 2006 The Portico Project
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
package hlaunit.hla13.common;

import hlaunit.CommonSetup;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.testng.Assert;

/**
 * This class provides access to common state and helper methods for tests
 */
public abstract class Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	protected static final int OWNER_UNOWNED = -1;
	protected static final int OWNER_RTI = 0;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected Logger logger;
	protected Test13Federate defaultFederate;
	protected Set<Test13Federate> federates;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	protected Abstract13Test()
	{
		this.logger = LogManager.getFormatterLogger( "portico." + this.getClass().getSimpleName() );
		this.federates = new HashSet<Test13Federate>();
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
		this.defaultFederate = new Test13Federate( "defaultFederate", this );
	}
	
	protected void afterClass()
	{
		Test13Federate.killActiveFederates();
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
	
	/**
	 * Returns a set of all the federates associated with this test that are currently joined
	 * to a federation (using {@link Test13Federate#isJoined()} to test). If none are linked
	 * to the tests or none are joined, an empty set is returned.
	 */
	protected Set<Test13Federate> joinedFederates()
	{
		HashSet<Test13Federate> returnSet = new HashSet<Test13Federate>();
		for( Test13Federate federate : federates )
		{
			if( federate.isJoined() )
				returnSet.add( federate );
		}
		
		return returnSet;
	}

	/**
	 * This method will cause the current federate to sleep for a time deemed valid by the
	 * communications binding in use
	 */
	protected void quickSleep()
	{
		try
		{
			Thread.sleep( CommonSetup.TIMEOUT );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
