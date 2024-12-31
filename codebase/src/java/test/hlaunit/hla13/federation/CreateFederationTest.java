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
package hlaunit.hla13.federation;

import java.net.URI;
import java.net.URL;

import hla.rti.CouldNotOpenFED;
import hla.rti.ErrorReadingFED;
import hla.rti.FederationExecutionAlreadyExists;
import hla.rti.RTIinternalError;
import hla.rti.jlc.RTIambassadorEx;
import hlaunit.hla13.TestSetup;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"CreateFederationTest", "basic", "create", "federationManagement"})
public class CreateFederationTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTIambassadorEx rtiamb;
	private String fedname = "CreateFederationTest";

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@BeforeClass(alwaysRun=true)
	public void initialize() throws Exception
	{
		// get an RTIambassador
		this.rtiamb = TestSetup.createRTIambassador();
	}
	
	@AfterClass(alwaysRun=true)
	public void cleanup() throws Exception
	{
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Create Federation Test Methods //////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// public void createFederationExecution( String executionName, java.net.URL fed )
	//     throws FederationExecutionAlreadyExists,
	//            CouldNotOpenFED,
	//            ErrorReadingFED,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

	//////////////////////////////////////////
	// TEST: (valid) testCreateFederation() //
	//////////////////////////////////////////
	@Test
	public void testCreateFederation() throws Exception
	{
		// create a link to the FOM //
		URL fom = ClassLoader.getSystemResource( "fom/testfom.fed" );
		
		// try and create a valid federation //
		try
		{
			this.rtiamb.createFederationExecution( fedname, fom );
		}
		catch( Exception e )
		{
			Assert.fail( "Could not create valid federation", e );
		}
		
		// ensure that federation can't be created again (now that it has already been created //
		try
		{
			this.rtiamb.createFederationExecution( fedname, fom );
			Assert.fail( "Could not ensure that valid federation was created" );
		}
		catch( FederationExecutionAlreadyExists ae )
		{
			// SUCCESS!
			this.rtiamb.destroyFederationExecution( fedname );
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
			this.rtiamb.createFederationExecution( fedname, uri.toURL() );
			Assert.fail( "No exception while creating federation with invalid FOM (invalid URL)" );
		}
		catch( CouldNotOpenFED cnof )
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
			URL invalidFom = ClassLoader.getSystemResource( "fom/testfom-unbalanced.fed" );
			this.rtiamb.createFederationExecution( fedname, invalidFom );
			Assert.fail( "No exception while creating federation with invalid FOM" );
		}
		catch( ErrorReadingFED erf )
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
			this.rtiamb.createFederationExecution( fedname, null );
			Assert.fail( "No exception while creating federation with null FOM" );
		}
		catch( CouldNotOpenFED cnof )
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
			URL validFom = ClassLoader.getSystemResource( "fom/testfom.fed" );
			this.rtiamb.createFederationExecution( null, validFom );
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
