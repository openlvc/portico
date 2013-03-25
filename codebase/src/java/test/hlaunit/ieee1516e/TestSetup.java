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
package hlaunit.ieee1516e;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.RtiFactoryFactory;
import hlaunit.CommonSetup;

/**
 * This class provides a number of simple utility/shortcut methods for helping with the testing
 * of the HLA 1516e implementation. It also provides test suite setup and shutdown methods.
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
	public static RTIambassador createRTIambassador()
	{
		try
		{
			return RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
		}
		catch( Exception e )
		{
			Assert.fail( "Couldn't create RTIambassador: " + e.getMessage(), e );
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
