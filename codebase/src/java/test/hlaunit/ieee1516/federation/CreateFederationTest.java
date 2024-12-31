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
package hlaunit.ieee1516.federation;

import java.net.URI;
import java.net.URL;

import hla.rti1516.CouldNotOpenFDD;
import hla.rti1516.ErrorReadingFDD;
import hla.rti1516.FederationExecutionAlreadyExists;
import hla.rti1516.RTIinternalError;
import hlaunit.ieee1516.common.Abstract1516Test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"CreateFederationTest", "basic", "create", "federationManagement"})
public class CreateFederationTest extends Abstract1516Test
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

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	@BeforeClass(alwaysRun=true)
	public void beforeClass()
	{
		super.beforeClass();
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		defaultFederate.quickDestroyTolerant( defaultFederate.simpleName );
	}
	
	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}
	
	//////////////////////////////////////////
	// TEST: (valid) testCreateFederation() //
	//////////////////////////////////////////
	@Test
	public void testCreateFederation()
	{
		// create a link to the FOM //
		URL fom = ClassLoader.getSystemResource( "fom/testfom.xml" );
		
		// try and create a valid federation //
		try
		{
			defaultFederate.rtiamb.createFederationExecution( defaultFederate.simpleName, fom );
		}
		catch( Exception e )
		{
			Assert.fail( "Could not create valid federation", e );
		}
		
		// ensure that federation can't be created again (now that it has already been created //
		try
		{
			defaultFederate.rtiamb.createFederationExecution( defaultFederate.simpleName, fom );
			Assert.fail( "Could not ensure that valid federation was created" );
		}
		catch( FederationExecutionAlreadyExists ae )
		{
			// SUCCESS!
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception while testing creation of existing federation", e );
		}
	}
	
	////////////////////////////////////////////////////////
	// TEST: testCreateFederationWithInvalidFomLocation() //
	////////////////////////////////////////////////////////
	@Test
	public void testCreateFederationWithInvalidFomLocation()
	{
		// attempt to create with invalid fom URL //
		try
		{
			URI uri = new URI( "http://localhost/dummyURL" );
			defaultFederate.rtiamb.createFederationExecution( defaultFederate.simpleName, uri.toURL() );
			Assert.fail( "No exception while creating federation with invalid FOM (invalid URL)" );
		}
		catch( CouldNotOpenFDD cnof )
		{
			// SUCCESS!
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception while testing create with null FOM", e );
		}
	}
	
	////////////////////////////////////////////////
	// TEST: testCreateFederationWithInvalidFom() //
	////////////////////////////////////////////////
	@Test
	public void testCreateFederationWithInvalidFom()
	{
		// attempt to create with invalid fom //
		try
		{
			URL invalid = ClassLoader.getSystemResource( "fom/testfom-invalid.xml" );
			defaultFederate.rtiamb.createFederationExecution( defaultFederate.simpleName, invalid );
			Assert.fail( "No exception while creating federation with invalid FOM" );
		}
		catch( ErrorReadingFDD erf )
		{
			// SUCCESS!
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception while testing create with invalid FOM", e );
		}
	}
	
	/////////////////////////////////////////////
	// TEST: testCreateFederationWithNullFom() //
	/////////////////////////////////////////////
	@Test
	public void testCreateFederationWithNullFom()
	{
		// attempt to create with null fom //
		try
		{
			defaultFederate.rtiamb.createFederationExecution( defaultFederate.simpleName, null );
			Assert.fail( "No exception while creating federation with null FOM" );
		}
		catch( CouldNotOpenFDD cnof )
		{
			// SUCCESS!
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception while testing create with null FOM", e );
		}
	}
	
	//////////////////////////////////////////////
	// TEST: testCreateFederationWithNullName() //
	//////////////////////////////////////////////
	@Test
	public void testCreateFederationWithNullName()
	{
		// attempt create with dodgy name //
		try
		{
			URL validFom = ClassLoader.getSystemResource( "fom/testfom.xml" );
			defaultFederate.rtiamb.createFederationExecution( null, validFom );
			Assert.fail( "No exception while creating federation with null name" );
		}
		catch( RTIinternalError rtie )
		{
			// SUCCESS!
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception while testing create with create with invalid name", e );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
