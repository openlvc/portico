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
package hlaunit.hla13.support;

import hlaunit.hla13.common.Abstract13Test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"HandleFetchingTest", "model", "fetchHandles", "supportServices"})
public class HandleFetchingTest extends Abstract13Test
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
	@Override
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
		defaultFederate.quickJoin();
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		defaultFederate.quickResign();
		defaultFederate.quickDestroy();
	}

	@Override
	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////
	// TEST: testFetchWithQualifiedName //
	//////////////////////////////////////
	@Test
	public void testFetchWithQualifiedName()
	{
		try
		{
			// test for object classes
			defaultFederate.rtiamb.getObjectClassHandle( "ObjectRoot" );
			defaultFederate.rtiamb.getObjectClassHandle( "ObjectRoot.A" );
			defaultFederate.rtiamb.getObjectClassHandle( "ObjectRoot.A.B" );
			
			// test for case insensitivity
			defaultFederate.rtiamb.getObjectClassHandle( "oBjEcTrOoT" );
			defaultFederate.rtiamb.getObjectClassHandle( "ObJeCtRoOt.A" );
			defaultFederate.rtiamb.getObjectClassHandle( "objECTrooT.a.B" );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching object class handles with qualified names", e );
		}

		try
		{
			// test for interaction classes
			defaultFederate.rtiamb.getInteractionClassHandle( "InteractionRoot" );
			defaultFederate.rtiamb.getInteractionClassHandle( "InteractionRoot.X" );
			defaultFederate.rtiamb.getInteractionClassHandle( "InteractionRoot.X.Y" );
			
			// test for case insensivity
			defaultFederate.rtiamb.getInteractionClassHandle( "iNtErAcTiOnRoOt" );
			defaultFederate.rtiamb.getInteractionClassHandle( "InTeRaCtIoNrOoT.x" );
			defaultFederate.rtiamb.getInteractionClassHandle( "INTeraCTIonrOOT.x.Y" );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching interaction handles with qualified names", e );
		}
	}
	
	/////////////////////////////////////////
	// TEST: testFetchWithoutQualifiedName //
	/////////////////////////////////////////
	@Test
	public void testFetchWithoutQualifiedName()
	{
		try
		{
			// test for object classes
			defaultFederate.rtiamb.getObjectClassHandle( "A" );
			defaultFederate.rtiamb.getObjectClassHandle( "B" );
			defaultFederate.rtiamb.getObjectClassHandle( "A.B" );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching object class handles with qualified names", e );
		}

		try
		{
			// test for interaction classes
			defaultFederate.rtiamb.getInteractionClassHandle( "X" );
			defaultFederate.rtiamb.getInteractionClassHandle( "Y" );
			defaultFederate.rtiamb.getInteractionClassHandle( "X.Y" );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching interaction handles with qualified names", e );
		}
	}
	
	////////////////////////////////////////
	// TEST: testCrossVersionFetchHandles //
	////////////////////////////////////////
	@Test 
	public void testCrossVersionFetchHandles()
	{
		try
		{
			// test for object classes
			int or13  = defaultFederate.rtiamb.getObjectClassHandle( "ObjectRoot" );
			int orA13 = defaultFederate.rtiamb.getObjectClassHandle( "ObjectRoot.A" );
			int orB13 = defaultFederate.rtiamb.getObjectClassHandle( "ObjectRoot.A.B" );
			// check for priv to delete
			int p13   = defaultFederate.rtiamb.getAttributeHandle( "privilegeToDelete", orB13 );
			int p1516 = defaultFederate.rtiamb.getAttributeHandle( "HLAprivilegeToDeleteObject", orB13 );
			Assert.assertEquals( p13, p1516, "privToDelete handles differ from 1.3 to 1516" );
			
			// check the 1516 handles and ensure they are the same as 1.3
			int or1516  = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot" );
			int orA1516 = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A" );
			int orB1516 = defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.A.B" );
			
			Assert.assertEquals( or13, or1516, "ObjectRoot and HLAobjectRoot differ" );
			Assert.assertEquals( orA13, orA1516, "ObjectRoot.A and HLAobjectRoot.A differ" );
			Assert.assertEquals( orB13, orB1516, "ObjectRoot.A.B and HLAobjectRoot.A.B differ" );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching object class handles with qualified names", e );
		}

		try
		{
			// test for interaction classes
			int ir13  = defaultFederate.rtiamb.getInteractionClassHandle( "InteractionRoot" );
			int irX13 = defaultFederate.rtiamb.getInteractionClassHandle( "InteractionRoot.X" );
			int irY13 = defaultFederate.rtiamb.getInteractionClassHandle( "InteractionRoot.X.Y" );
			
			// get the 1516 handles and ensure they are the same as 1.3
			int ir1516  = defaultFederate.rtiamb.getInteractionClassHandle( "HLAinteractionRoot" );
			int irX1516 = defaultFederate.rtiamb.getInteractionClassHandle( "HLAinteractionRoot.X" );
			int irY1516 = defaultFederate.rtiamb.getInteractionClassHandle( "HLAinteractionRoot.X.Y" );
			
			Assert.assertEquals( ir13, ir1516, "InteractionRoot and HLAinteractionRoot differ" );
			Assert.assertEquals( irX13, irX1516, "InteractionRoot.X and HLAinteractionRoot.X differ" );
			Assert.assertEquals( irY13, irY1516, "InteractionRoot.X.Y and HLAinteractionRoot.X.Y differ" );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching interaction handles with qualified names", e );
		}
	}
	
	///////////////////////////////////////////
	// TEST: testCrossVersionFetchMomHandles //
	///////////////////////////////////////////
	@Test
	public void testCrossVersionFetchMomHandles()
	{
		/////////////////////
		// QUALIFIED NAMES //
		/////////////////////
		try
		{
			int manager13 =
				defaultFederate.rtiamb.getObjectClassHandle( "ObjectRoot.Manager" );
			int federate13 =
				defaultFederate.rtiamb.getObjectClassHandle( "ObjectRoot.Manager.Federate" );
			int federation13 =
				defaultFederate.rtiamb.getObjectClassHandle( "ObjectRoot.Manager.Federation" );

			int manager1516 =
				defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.HLAmanager" );
			int federate1516 =
				defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.HLAmanager.HLAfederate" );
			int federation1516 =
				defaultFederate.rtiamb.getObjectClassHandle( "HLAobjectRoot.HLAmanager.HLAfederation" );
			
			// check that the handles are the same
			Assert.assertEquals( manager13, manager1516, "Manager handles differ (qualified)" );
			Assert.assertEquals( federate13, federate1516, "Federate handles differ (qualified)" );
			Assert.assertEquals( federation13, federation1516, "Federation handles differ (qualified)" );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching MOM handles with qualified names", e );
		}
		
		/////////////////////////////
		// PARTIAL QUALIFIED NAMES //
		/////////////////////////////
		try
		{
			int manager13 =
				defaultFederate.rtiamb.getObjectClassHandle( "Manager" );
			int federate13 =
				defaultFederate.rtiamb.getObjectClassHandle( "Manager.Federate" );
			int federation13 =
				defaultFederate.rtiamb.getObjectClassHandle( "Manager.Federation" );

			int manager1516 =
				defaultFederate.rtiamb.getObjectClassHandle( "HLAmanager" );
			int federate1516 =
				defaultFederate.rtiamb.getObjectClassHandle( "HLAmanager.HLAfederate" );
			int federation1516 =
				defaultFederate.rtiamb.getObjectClassHandle( "HLAmanager.HLAfederation" );
			
			// check that the handles are the same
			Assert.assertEquals( manager13, manager1516, "Manager handles differ (partial qualified)" );
			Assert.assertEquals( federate13, federate1516, "Federate handles differ (partial qualified)" );
			Assert.assertEquals( federation13, federation1516, "Federation handles differ (partial qualified)" );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching MOM handles with partially qualified names", e );
		}

		/////////////////////////
		// NON QUALIFIED NAMES //
		/////////////////////////
		try
		{
			int manager13 = defaultFederate.rtiamb.getObjectClassHandle( "Manager" );
			int federate13 = defaultFederate.rtiamb.getObjectClassHandle( "Federate" );
			int federation13 = defaultFederate.rtiamb.getObjectClassHandle( "Federation" );

			int manager1516 = defaultFederate.rtiamb.getObjectClassHandle( "HLAmanager" );
			int federate1516 = defaultFederate.rtiamb.getObjectClassHandle( "HLAfederate" );
			int federation1516 = defaultFederate.rtiamb.getObjectClassHandle( "HLAfederation" );
			
			// check that the handles are the same
			Assert.assertEquals( manager13, manager1516, "Manager handles differ (local)" );
			Assert.assertEquals( federate13, federate1516, "Federate handles differ (local)" );
			Assert.assertEquals( federation13, federation1516, "Federation handles differ (local)" );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching MOM handles with qualified names", e );
		}

	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
