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

import hla.rti1516.FederatesCurrentlyJoined;
import hla.rti1516.FederationExecutionDoesNotExist;
import hla.rti1516.RTIinternalError;
import hlaunit.ieee1516.common.Abstract1516Test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"DestroyFederationTest", "basic", "destroy", "federationManagement"})
public class DestroyFederationTest extends Abstract1516Test
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
		defaultFederate.quickCreate();
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		defaultFederate.quickDestroyTolerant();
	}
	
	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}

	///////////////////////////////////////////
	// TEST: (valid) testDestroyFederation() //
	///////////////////////////////////////////
	@Test
	public void testDestroyFederation()
	{
		try
		{
			// destroy the federation //
			defaultFederate.rtiamb.destroyFederationExecution( defaultFederate.simpleName );
		}
		catch( Exception e )
		{
			Assert.fail( "Failed to destroy existing federation", e );
		}
		
		try
		{
			// ensure that federation is gone and can't be destroyed again //
			defaultFederate.rtiamb.destroyFederationExecution( defaultFederate.simpleName );
			Assert.fail( "Could not ensure that valid federation was destroyed" );
		}
		catch( FederationExecutionDoesNotExist dne )
		{
			// SUCCESS!
		}
		catch( Exception e )
		{
			Assert.fail( "Failed to destroy existing federation", e );
		}
	}
	
	///////////////////////////////////////////////////
	// TEST: testDestroyFederationThatDoesNotExist() //
	///////////////////////////////////////////////////
	@Test
	public void testDestroyFederationThatDoesNotExist()
	{
		// try and destroy a federation that does not exist //
		try
		{
			defaultFederate.rtiamb.destroyFederationExecution( "noSuchFederation" );
			Assert.fail( "No exception while destroying non-existent federation" );
		}
		catch( FederationExecutionDoesNotExist dne )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception while destorying non-existent federation", e );
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
			defaultFederate.rtiamb.destroyFederationExecution( null );
			Assert.fail( "No exception while destroying federation with null name" );
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
			Assert.fail( "Wrong exception while destorying federation with null name", e );
		}
	}
	
	//////////////////////////////////////////////////////
	// TEST: testDestroyFederationWithJoinedFederates() //
	//////////////////////////////////////////////////////
	@Test
	public void testDestroyFederationWithJoinedFederates()
	{
		// join the federation
		defaultFederate.quickJoin();
		
		try
		{
			// destroy the federation //
			defaultFederate.rtiamb.destroyFederationExecution( defaultFederate.simpleName );
			Assert.fail( "Was able to destroy a federation with active federates" );
		}
		catch( FederatesCurrentlyJoined fcj )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception while destroying federation with active federates", e );
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
