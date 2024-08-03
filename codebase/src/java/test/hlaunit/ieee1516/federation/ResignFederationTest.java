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

import hla.rti1516.FederateNotExecutionMember;
import hla.rti1516.FederateOwnsAttributes;
import hla.rti1516.RTIinternalError;
import hla.rti1516.ResignAction;
import hlaunit.ieee1516.common.Abstract1516Test;
import hlaunit.ieee1516.common.TestFederate;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"ResignFederationTest", "basic", "resign", "federationManagement"})
public class ResignFederationTest extends Abstract1516Test
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
		defaultFederate.quickResign();
		// destroy the federation that we are working in //
		defaultFederate.quickDestroy();
	}

	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////
	// TEST: (valid) testResignWithNoAction() //
	////////////////////////////////////////////
	@Test
	public void testResignWithNoAction()
	{
		// Test with vaild resign action: NO_ACTION //
		try
		{
			defaultFederate.quickJoin();
			defaultFederate.rtiamb.resignFederationExecution( ResignAction.NO_ACTION );
		}
		catch( Exception e )
		{
			Assert.fail( "Failed while testing a valid resign (NO_ACTION)", e );
		}
	}
	
	/////////////////////////////////////////////////
	// TEST: (valid) testResignWithDeleteObjects() //
	/////////////////////////////////////////////////
	@Test
	public void testResignWithDeleteObjects()
	{
		// join, and register an instance
		defaultFederate.quickJoin();
		defaultFederate.quickPublish( "ObjectRoot.A", "aa" );
		int oHandle = defaultFederate.quickRegister( "ObjectRoot.A" );
		
		// join a second federate and make sure it discovers the instance
		TestFederate otherFederate = new TestFederate( "otherFederate", this );
		otherFederate.quickJoin();
		otherFederate.quickSubscribe( "ObjectRoot.A", "aa" );
		otherFederate.fedamb.waitForDiscovery( oHandle );
		
		// Test with vaild resign action: DELETE_OBJECTS //
		try
		{
			defaultFederate.rtiamb.resignFederationExecution( ResignAction.DELETE_OBJECTS );
		}
		catch( Exception e )
		{
			Assert.fail( "Failed while testing a valid resign (DELETE_OBJECTS)", e );
		}
		
		// make sure the object has been deleted
		otherFederate.fedamb.waitForRORemoval( oHandle );
		otherFederate.quickResign();
	}
	
	///////////////////////////////////////////////////////////
	// TEST: (valid) testResignWithDeleteObjectsThenDivest() //
	///////////////////////////////////////////////////////////
	@Test
	public void testResignWithDeleteObjectsThenDivest()
	{
		// Test with vaild resign action: DELETE_OBJECTS_THEN_DIVEST //
		try
		{
			defaultFederate.quickJoin();
			defaultFederate.rtiamb.resignFederationExecution(
			    ResignAction.DELETE_OBJECTS_THEN_DIVEST );
		}
		catch( Exception e )
		{
			Assert.fail( "Failed while testing a valid resign "+
			             "(DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES)", e );
		}
		
		log( "testResignWithDeleteObjectsThenDivest() not yet validated" );
	}
	
	///////////////////////////////////////////////////////////
	// TEST: testResignWithNoActionWhileAttributesAreOwned() //
	///////////////////////////////////////////////////////////
	@Test
	public void testResignWithNoActionWhileAttributesAreOwned()
	{
		// join and register an object instance
		defaultFederate.quickJoin();
		defaultFederate.quickPublish( "ObjectRoot.A", "aa" );
		int oHandle = defaultFederate.quickRegister( "ObjectRoot.A" );
		
		// try and resign without deleting theh attributes
		try
		{
			defaultFederate.rtiamb.resignFederationExecution( ResignAction.NO_ACTION );
		}
		catch( FederateOwnsAttributes foa )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			Assert.fail( "Invalid exception while resigning, expected FederateOwnsAttributes", e );
		}
		finally
		{
			defaultFederate.quickDelete( oHandle, "".getBytes() );
			defaultFederate.quickResign( ResignAction.DELETE_OBJECTS );
		}
	}
	
	//////////////////////////////////////
	// TEST: testResignWithNullAction() //
	//////////////////////////////////////
	@Test
	public void testResignWithNullAction()
	{
		// test resign with an invalid resign action //
		try
		{
			defaultFederate.quickJoin();
			defaultFederate.rtiamb.resignFederationExecution( null );
			Assert.fail( "No exception while resigning when not joined" );
		}
		catch( RTIinternalError rtie )
		{
			// SUCCESS
			
		}
		catch( Exception e )
		{
			Assert.fail( "Invalid exception caught while resigning with invalid resign action", e );
		}
		finally
		{
			defaultFederate.quickResign();
		}
	}
	
	/////////////////////////////////////////////////
	// TEST: testResignFromFederationNotJoinedTo() //
	/////////////////////////////////////////////////
	@Test
	public void testResignFromFederationNotJoinedTo()
	{
		// test resign from federation we have no joined //
		try
		{
			defaultFederate.rtiamb.resignFederationExecution( ResignAction.NO_ACTION );
			Assert.fail( "No exception while resigning when not joined" );
		}
		catch( FederateNotExecutionMember fnem )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			Assert.fail( "Invalid exception caught while resigning when not joined", e );
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
