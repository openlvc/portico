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

import hla.rti.FederatesCurrentlyJoined;
import hla.rti.FederationExecutionDoesNotExist;
import hla.rti.RTIinternalError;
import hla.rti.jlc.RTIambassadorEx;
import hlaunit.CommonSetup;
import hlaunit.hla13.TestSetup;
import hlaunit.hla13.common.Abstract13Test;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"DestroyFederationTest", "basic", "destroy", "federationManagement"})
public class DestroyFederationTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTIambassadorEx rtiamb;
	private String fedname = "DestroyFederationTest";
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
		// get an RTIambassador
		this.rtiamb = TestSetup.createRTIambassador();
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		
	}
	
	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Destroy Federation Test Methods /////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// public void destroyFederationExecution( String executionName )
	//     throws FederatesCurrentlyJoined,
	//            FederationExecutionDoesNotExist,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

	///////////////////////////////////////////
	// TEST: (valid) testDestroyFederation() //
	///////////////////////////////////////////
	@Test
	public void testDestroyFederation()
	{
		defaultFederate.quickCreate( fedname );
		try
		{
			// destroy the federation //
			this.rtiamb.destroyFederationExecution( fedname );
		}
		catch( Exception e )
		{
			unexpectedException( "destroying federation", e );
		}
		
		// skip this bit if we're using JGroups - it'll give us a false positive
		if( CommonSetup.JGROUPS_ACTIVE )
			return;

		try
		{
			// ensure that federation is gone and can't be destroyed again //
			this.rtiamb.destroyFederationExecution( fedname );
			expectedException( FederationExecutionDoesNotExist.class );
		}
		catch( FederationExecutionDoesNotExist dne )
		{
			// SUCCESS!
		}
		catch( Exception e )
		{
			wrongException( e, FederationExecutionDoesNotExist.class );
		}
	}
	
	///////////////////////////////////////////////////
	// TEST: testDestroyFederationThatDoesNotExist() //
	///////////////////////////////////////////////////
	@Test
	public void testDestroyFederationThatDoesNotExist()
	{
		// skip this bit if we're using JGroups - it'll give us a false positive
		if( CommonSetup.JGROUPS_ACTIVE )
			return;

		// try and destroy a federation that does not exist //
		try
		{
			this.rtiamb.destroyFederationExecution( "noSuchFederation" );
			expectedException( FederationExecutionDoesNotExist.class );
		}
		catch( FederationExecutionDoesNotExist dne )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, FederationExecutionDoesNotExist.class );
		}
	}
	
	///////////////////////////////////////////////
	// TEST: testDestroyFederationWithNullName() //
	///////////////////////////////////////////////
	@Test
	public void testDestroyFederationWithNullName()
	{
		// try and destroy with null federation name //
		try
		{
			this.rtiamb.destroyFederationExecution( null );
			expectedException( FederationExecutionDoesNotExist.class, RTIinternalError.class );
		}
		catch( FederationExecutionDoesNotExist ne )
		{
			// SUCCESS
		}
		catch( RTIinternalError rtie )
		{
			// ALSO FINE
		}
		catch( Exception e )
		{
			wrongException( e, FederationExecutionDoesNotExist.class, RTIinternalError.class );
		}
	}
	
	//////////////////////////////////////////////////////
	// TEST: testDestroyFederationWithJoinedFederates() //
	//////////////////////////////////////////////////////
	@Test
	public void testDestroyFederationWithJoinedFederates()
	{
		// create and join the federation
		defaultFederate.quickCreate( fedname );
		defaultFederate.quickJoin( fedname );
		
		try
		{
			// destroy the federation //
			this.rtiamb.destroyFederationExecution( fedname );
			expectedException( FederatesCurrentlyJoined.class );
		}
		catch( FederatesCurrentlyJoined fcj )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, FederatesCurrentlyJoined.class );
		}
		finally
		{
			defaultFederate.quickResign();
			defaultFederate.quickDestroy();
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
