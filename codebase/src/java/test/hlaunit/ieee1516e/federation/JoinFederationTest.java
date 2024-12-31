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
package hlaunit.ieee1516e.federation;

import java.net.URL;

import hla.rti1516e.exceptions.FederateAlreadyExecutionMember;
import hla.rti1516e.exceptions.FederationExecutionDoesNotExist;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"JoinFederationTest", "basic", "join", "federationManagement"})
public class JoinFederationTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate secondFederate;

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
		this.secondFederate = new TestFederate( "secondFederate", this );
	}

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		this.secondFederate.quickConnect();

		// create a federation that we can test with //
		defaultFederate.quickCreate();
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		defaultFederate.quickResignTolerant();
		secondFederate.quickResignTolerant();
		defaultFederate.quickDestroy();
		
		secondFederate.quickDisconnect();
	}

	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// IEEE-1516e specifies a number of joinFederation overloads. The tests     //
	// are split up based on each overload in order to keep them in some sense  //
	// and order. Some overloads just call into others, so some of the testing  //
	// is redundant, but still important in detecting regressions incase we     //
	// have to split some of those implementations out on their own.            //
	//                                                                          //
	// The list below contains all the 1516e createFederation overloads         //
	//    * joinFederationExecution( Type, Federation )                         //
	//    * joinFederationExecution( Name, Type, Federation )                   //
	//    * joinFederationExecution( Type, Federation, URL[] )                  //
	//    * joinFederationExecution( Name, Type, Federation, URL[] )            //
	//                                                                          //
	//////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////////////////////////////////////////////////////////
	// joinFederation( Type, Federation ) //////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
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
			                                                defaultFederate.federateType,
			                                                defaultFederate.simpleName );
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
			otherFederate.quickConnect();
			otherFederate.rtiamb.joinFederationExecution( defaultFederate.federateName,
			                                              defaultFederate.federateType,
			                                              defaultFederate.simpleName );
			Assert.fail( "No exception while joining federation where name is taken" );
		}
		catch( FederateAlreadyExecutionMember aem )
		{
			// SUCCESS
		}
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
			defaultFederate.rtiamb.joinFederationExecution( defaultFederate.federateName,
			                                                defaultFederate.federateType,
			                                                "noSuchFederation" );
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
			defaultFederate.rtiamb.joinFederationExecution( defaultFederate.federateName,
			                                                defaultFederate.federateType,
			                                                (String)null ); /* null federation name */
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
			                                                defaultFederate.federateType,
			                                                defaultFederate.simpleName );
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
			                                                defaultFederate.federateType,
			                                                defaultFederate.simpleName );
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
	
	/////////////////////////////////////////////////
	// TEST: testJoinFederationWhenAlreadyJoined() //
	/////////////////////////////////////////////////
	@Test()
	public void testJoinFederationWhenAlreadyJoined()
	{
		// set up by running a valid join //
		defaultFederate.quickJoin();
		
		// try and join the rtiamb to a second federation //
		try
		{
			defaultFederate.rtiamb.joinFederationExecution( "aValidName",
			                                                defaultFederate.federateType,
			                                                defaultFederate.simpleName+"2" );
			Assert.fail( "No exception while joining two federations through same ambassador" );
		}
		catch( FederateAlreadyExecutionMember aem )
		{
			// SUCCESS
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

	////////////////////////////////////////////////////////////////////////////////////////////
	// joinFederation( Type, Name, Federation ) ////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	////////////////////////////////////////
	// TEST: (valid) testJoinFederation() //
	////////////////////////////////////////
	@Test
	public void testJoinFederationWithNameAndType()
	{
		try
		{
			// try and join a federation //
			defaultFederate.rtiamb.joinFederationExecution( defaultFederate.federateName,
			                                                defaultFederate.federateType,
			                                                defaultFederate.simpleName );
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
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// joinFederation( Type, Federation, URL[] ) ///////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////
	// TEST: (valid) testJoinFederationWithTypeAndModules() //
	//////////////////////////////////////////////////////////
	@Test
	public void testJoinFederationWithTypeAndModules()
	{
		// join the base federate to the federation
		defaultFederate.quickJoin();

		// validate that we don't have any information about the model the second federate
		// will add when it joins
		defaultFederate.quickOCHandleMissing( "HLAobjectRoot.Employee.Waiter" );
		defaultFederate.quickICHandleMissing( "HLAinteractionRoot.CustomerTransactions.FoodServed" );
		defaultFederate.quickOCHandleMissing( "HLAobjectRoot.Food.SideDish.Corn" );

		try
		{
			// try and join a federation with mode modules
			URL[] modules = new URL[]{
			    ClassLoader.getSystemResource( "fom/ieee1516e/restaurant/RestaurantProcesses.xml" ),
			    ClassLoader.getSystemResource( "fom/ieee1516e/restaurant/RestaurantFood.xml" ),
			};

			secondFederate.rtiamb.joinFederationExecution( secondFederate.federateName,
			                                               secondFederate.simpleName,
			                                               modules );
		}
		catch( Exception e )
		{
			// clean up for the next test //
			defaultFederate.quickResign();
			secondFederate.quickResignTolerant();
			Assert.fail( "Failed while testing a valid join request with modules", e );
		}
		
		defaultFederate.quickTick();
		secondFederate.quickTick();
		
		// validate that the default federate now fetch information about the extended FOM
		// classes from RestaurantProcess.xml
		int handleA = defaultFederate.quickOCHandle( "HLAobjectRoot.Employee.Waiter" );
		int handleB = secondFederate.quickOCHandle( "HLAobjectRoot.Employee.Waiter" );
		Assert.assertEquals( handleB, handleA );
		
		handleA = defaultFederate.quickICHandle( "HLAinteractionRoot.CustomerTransactions.FoodServed" );
		handleB = secondFederate.quickICHandle( "HLAinteractionRoot.CustomerTransactions.FoodServed" );
		Assert.assertEquals( handleB, handleA );
		
		// classes from RestaurantFood.xml
		handleA = defaultFederate.quickOCHandle( "HLAobjectRoot.Food.SideDish.Corn" );
		handleB = secondFederate.quickOCHandle( "HLAobjectRoot.Food.SideDish.Corn" );
		Assert.assertEquals( handleB, handleA );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// joinFederation( Name, Type, Federation, URL[] ) /////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////////
	// TEST: (valid) testJoinFederationWithNameTypeAndModules() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testJoinFederationWithNameTypeAndModules()
	{
		// join the base federate to the federation
		defaultFederate.quickJoin();

		// validate that we don't have any information about the model the second federate
		// will add when it joins
		defaultFederate.quickOCHandleMissing( "HLAobjectRoot.Employee.Waiter" );
		defaultFederate.quickICHandleMissing( "HLAinteractionRoot.CustomerTransactions.FoodServed" );
		defaultFederate.quickOCHandleMissing( "HLAobjectRoot.Food.SideDish.Corn" );

		try
		{
			// try and join a federation with mode modules
			URL[] modules = new URL[]{
			    ClassLoader.getSystemResource( "fom/ieee1516e/restaurant/RestaurantProcesses.xml" ),
			    ClassLoader.getSystemResource( "fom/ieee1516e/restaurant/RestaurantFood.xml" ),
			};

			secondFederate.rtiamb.joinFederationExecution( secondFederate.federateName,
			                                               secondFederate.federateName,
			                                               secondFederate.simpleName,
			                                               modules );
		}
		catch( Exception e )
		{
			// clean up for the next test //
			defaultFederate.quickResign();
			secondFederate.quickResignTolerant();
			Assert.fail( "Failed while testing a valid join request with modules", e );
		}
		
		defaultFederate.quickTick();
		secondFederate.quickTick();
		
		// validate that the default federate now fetch information about the extended FOM
		// classes from RestaurantProcess.xml
		defaultFederate.quickOCHandle( "HLAobjectRoot.Employee.Waiter" );
		secondFederate.quickOCHandle( "HLAobjectRoot.Employee.Waiter" );
		defaultFederate.quickICHandle( "HLAinteractionRoot.CustomerTransactions.FoodServed" );
		secondFederate.quickICHandle( "HLAinteractionRoot.CustomerTransactions.FoodServed" );
		
		// classes from RestaurantFood.xml
		defaultFederate.quickOCHandle( "HLAobjectRoot.Food.SideDish.Corn" );
		secondFederate.quickOCHandle( "HLAobjectRoot.Food.SideDish.Corn" );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
