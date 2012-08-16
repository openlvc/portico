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
package hlaunit.hla13;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import hla.rti.jlc.RTIambassadorEx;
import hla.rti.jlc.RtiFactory;
import hla.rti.jlc.RtiFactoryFactory;

import hlaunit.CommonSetup;

/**
 * This class provides a number of simple utility/shortcut methods for helping with the testing
 * of the HLA 1.3 implementation. It also provides test suite setup and shutdown methods.
 */
public class TestSetup
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public TestSetup()
	{
		
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------	
	////////////////////////////////////////////////////////////
	/////////////////// Suite Setup/Shutdown ///////////////////
	////////////////////////////////////////////////////////////
	@BeforeSuite(alwaysRun=true)
	public void beforeSuite()
	{
		CommonSetup.commonBeforeSuiteSetup();
	}
	
	@AfterSuite(alwaysRun=true)
	public void afterSuite()
	{
		CommonSetup.commonAfterSuiteCleanup();
	}
	
	////////////////////////////////////////////////////////////
	////////////////////// Helper Methods //////////////////////
	////////////////////////////////////////////////////////////
	/**
	 * Create a new RTIambassador and return it. If there is an error Assert.fail() is used
	 */
	public static RTIambassadorEx createRTIambassador()
	{
		// create the actual connection and return it
		try
		{
			return RtiFactoryFactory.getRtiFactory().createRtiAmbassador();
		}
		catch( Exception e )
		{
			Assert.fail( "Couldn't created RTIambassador: " + e.getMessage(), e );
			return null;
		}
	}
	
	/**
	 * Get a direct reference to the JLC RtiFactory
	 */
	public static RtiFactory getRTIFactory()
	{
		try
		{
			return RtiFactoryFactory.getRtiFactory();
		}
		catch( Exception e )
		{
			return null;
		}
	}
}
