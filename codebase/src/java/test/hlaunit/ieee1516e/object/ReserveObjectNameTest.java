/*
 *   Copyright 2013 The Portico Project
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
package hlaunit.ieee1516e.object;

import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.exceptions.IllegalName;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;
import hlaunit.ieee1516e.common.TypeFactory;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"ReserveObjectNameTest", "reserveObjectName", "objectManagement"})
public class ReserveObjectNameTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate secondFederate;
	private int objectClass;
	private ObjectClassHandle classHandle;

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

		secondFederate = new TestFederate( "secondFederate", this );
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
		
		// cache the handles
		this.objectClass = defaultFederate.quickOCHandle( "ObjectRoot.A" );
		this.classHandle = TypeFactory.getObjectClassHandle( objectClass );

		// do publication and subscription
		defaultFederate.quickPublish( "ObjectRoot.A", "aa" );
		secondFederate.quickSubscribe( "ObjectRoot.A", "aa" );
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		secondFederate.quickResign();
		defaultFederate.quickResign();
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
	// public void reserveObjectInstanceName( String theObjectName )
	//    throws IllegalName,
	//           SaveInProgress,
	//           RestoreInProgress,
	//           FederateNotExecutionMember,
	//           NotConnected,
	//           RTIinternalError
	
	///////////////////////////////////////////
	// TEST: (valid) testReserveObjectName() //
	///////////////////////////////////////////
	@Test
	public void testReserveObjectName() throws Exception
	{
		defaultFederate.rtiamb.reserveObjectInstanceName( "myObject" );
		defaultFederate.fedamb.waitForObjectNameReservationSuccess( "myObject" );
	}

	///////////////////////////////////////////////////////////
	// TEST: (valid) testReserveObjectNameWithReservedName() //
	///////////////////////////////////////////////////////////
	@Test(enabled=false) // disabled as negoriation is currently turned off in the RTI by default
	public void testReserveObjectNameWithReservedName() throws Exception
	{
		// register the name in the first federate
		defaultFederate.rtiamb.reserveObjectInstanceName( "myObject" );
		defaultFederate.fedamb.waitForObjectNameReservationSuccess( "myObject" );

		// validate that the second federate can't register it
		secondFederate.rtiamb.reserveObjectInstanceName( "myObject" );
		secondFederate.fedamb.waitForObjectNameReservationFailure( "myObject" );
	}

	///////////////////////////////////////////////
	// TEST: testReserveObjectNameWithNullName() //
	///////////////////////////////////////////////
	@Test
	public void testReserveObjectNameWithNullName()
	{
		try
		{
			defaultFederate.rtiamb.reserveObjectInstanceName( null );
			expectedException( IllegalName.class );
		}
		catch( IllegalName illegalName )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, IllegalName.class );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
