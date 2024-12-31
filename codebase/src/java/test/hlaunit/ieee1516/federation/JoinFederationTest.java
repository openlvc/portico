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

import hla.rti1516.FederateAlreadyExecutionMember;
import hla.rti1516.FederationExecutionDoesNotExist;
import hla.rti1516.RTIinternalError;
import hlaunit.ieee1516.common.Abstract1516Test;
import hlaunit.ieee1516.common.TestFederate;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"JoinFederationTest", "basic", "join", "federationManagement"})
public class JoinFederationTest extends Abstract1516Test
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
		// create a federation that we can test with //
		defaultFederate.quickCreate();
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		defaultFederate.quickDestroy();
	}

	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}
	
	////////////////////////////////////////
	// TEST: (valid) testJoinFederation() //
	////////////////////////////////////////
	@Test
	public void testJoinFederation()
	{
		try
		{
			// try and join a federation //
			defaultFederate.rtiamb.joinFederationExecution( defaultFederate.federateName,
			                                                defaultFederate.simpleName,
			                                                defaultFederate.fedamb,
			                                                null );
		}
		catch( Exception e )
		{
			Assert.fail( "Failed while testing a valid join request", e );
		}
		finally
		{
			// clean up for the next test //
			defaultFederate.quickResign();
		}
	}
	
	
	///////////////////////////////////////////////////////
	// TEST: testJoinFederationWhereFederateNameExists() //
	///////////////////////////////////////////////////////
	@Test
	public void testJoinFederationWhereFederateNameExists()
	{
		defaultFederate.quickJoin();
		
		// try and join a second federate with the same name //
		try
		{
			TestFederate otherFederate = new TestFederate( "otherFederate", this );
			otherFederate.rtiamb.joinFederationExecution( defaultFederate.federateName,
			                                              defaultFederate.simpleName,
			                                              otherFederate.fedamb,
			                                              null );
			Assert.fail( "No exception while joining federation where name is taken" );
		}
		catch( FederateAlreadyExecutionMember aem )
		{
			// SUCCESS
		}
//		catch( RTIinternalError rtie )
//		{
//			// ALSO VALID
//		}
		catch( Exception e )
		{
			Assert.fail( "Invalid exception while joining federation where name is taken", e );
		}
		finally
		{
			defaultFederate.quickResign();
		}
	}
	
	////////////////////////////////////////////////
	// TEST: testJoinFederationThatDoesNotExist() //
	////////////////////////////////////////////////
	@Test
	public void testJoinFederationThatDoesNotExist()
	{
		// try and join a federation that does no exist //
		try
		{
			defaultFederate.rtiamb.joinFederationExecution( "testFederate",
			                                                "noSuchFederation",
			                                                defaultFederate.fedamb,
			                                                null );
			Assert.fail( "No exception while joining a non-existent federation" );
		}
		catch( FederationExecutionDoesNotExist dne )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			Assert.fail( "Invalid exception while trying to join a non-existent federation", e );
		}
	}
	
	//////////////////////////////////////////////////////
	// TEST: testJoinFederationWithNullFederationName() //
	//////////////////////////////////////////////////////
	@Test
	public void testJoinFederationWithNullFederationName()
	{
		// try and join a federation that does no exist (null name) //
		try
		{
			defaultFederate.rtiamb.joinFederationExecution( "testFederate",
			                                                null, /* null federation name */
			                                                defaultFederate.fedamb,
			                                                null );
			Assert.fail( "No exception while joining a non-existent federation (null name)" );
		}
		catch( FederationExecutionDoesNotExist dne )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			Assert.fail( "Invalid exception while joining wrong federation (null name)", e );
		}
	}
	
	////////////////////////////////////////////////////
	// TEST: testJoinFederationWithNullFederateName() //
	////////////////////////////////////////////////////
	@Test
	public void testJoinFederationWithNullFederateName()
	{
		// try and join a federation using null name //
		try
		{
			defaultFederate.rtiamb.joinFederationExecution( null, /* null federate name */
			                                                defaultFederate.simpleName,
			                                                defaultFederate.fedamb,
			                                                null );
			Assert.fail( "No exception while joining a with null federate name" );
		}
		catch( RTIinternalError rtie )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			Assert.fail( "Invalid exception while trying to join federation using empty name", e );
		}
	}
	
	/////////////////////////////////////////////////////
	// TEST: testJoinFederationWithEmptyFederateName() //
	/////////////////////////////////////////////////////
	@Test
	public void testJoinFederationWithEmptyFederateName()
	{
		// try and join a federation using empty name //
		try
		{
			defaultFederate.rtiamb.joinFederationExecution( "", /* empty federate name */
			                                                defaultFederate.simpleName,
			                                                defaultFederate.fedamb,
			                                                null );
			Assert.fail( "No exception while joining a with empty federate name" );
		}
		catch( RTIinternalError rtie )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			Assert.fail( "Invalid exception while trying to join federation using null name", e );
		}
	}
	
	//////////////////////////////////////////////////////////
	// TEST: testJoinFederationWithNullFederateAmbassador() //
	//////////////////////////////////////////////////////////
	@Test
	public void testJoinFederationWithNullFederateAmbassador()
	{
		try
		{
			defaultFederate.rtiamb.joinFederationExecution( "testFederate",
			                                                defaultFederate.simpleName,
			                                                null /* null fedamb */,
			                                                null );
			Assert.fail( "No exception while joining with null federate ambassador" );
		}
		catch( RTIinternalError rtie )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			Assert.fail( "Invalid exception while joining with null federate ambassador", e );
		}
	}
	
	/////////////////////////////////////////////////
	// TEST: testJoinFederationWhenAlreadyJoined() //
	/////////////////////////////////////////////////
	@Test
	public void testJoinFederationWhenAlreadyJoined()
	{
		// set up by running a valid join //
		defaultFederate.quickJoin();
		
		// try and join the rtiamb to a second federation //
		try
		{
			defaultFederate.rtiamb.joinFederationExecution( "aValidName",
			                                                defaultFederate.simpleName+"2",
			                                                defaultFederate.fedamb,
			                                                null );
			Assert.fail( "No exception while joining two federations through same ambassador" );
		}
		catch( FederateAlreadyExecutionMember aem )
		{
			// SUCCESS
		}
		catch( RTIinternalError rtie )
		{
			// ALSO VALID
		}
		catch( Exception e )
		{
			Assert.fail( "Invalid exception while joining two federations with same ambassador",e );
		}
		finally
		{
			defaultFederate.quickResign();
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
