/*
 *   Copyright 2008 The Portico Project
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
package org.portico;

import hlaunit.CommonSetup;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * This class provides a number of simple utility/shortcut methods for helping with the testing
 * of the IEEE1516 implementation. It also provides test suite setup and shutdown methods.
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
}
