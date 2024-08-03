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

import hla.rti.FederateAlreadyExecutionMember;
import hla.rti.FederationExecutionDoesNotExist;
import hla.rti.RTIinternalError;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"JoinFederationTest", "basic", "join", "federationManagement"})
public class JoinFederationTest extends Abstract13Test
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
		defaultFederate.quickDestroy();
	}

	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Join Federation Test Methods ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// public int joinFederationExecution( String federateType,
	//                                     String federationExecutionName,
	//                                     FederateAmbassador federateReference )
	//     throws FederateAlreadyExecutionMember,
	//            FederationExecutionDoesNotExist,
	//            SaveInProgress,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

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
			                                                defaultFederate.fedamb );
		}
		catch( Exception e )
		{
			unexpectedException( "joining federation", e  );
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
			Test13Federate otherFederate = new Test13Federate( "otherFederate", this );
			otherFederate.rtiamb.joinFederationExecution( defaultFederate.federateName,
			                                              defaultFederate.simpleName,
			                                              otherFederate.fedamb );
			expectedException( FederateAlreadyExecutionMember.class, RTIinternalError.class );
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
			wrongException( e, FederateAlreadyExecutionMember.class, RTIinternalError.class );
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
			                                                defaultFederate.fedamb );
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
			                                                defaultFederate.fedamb );
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
			                                                defaultFederate.fedamb );
			expectedException( RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, RTIinternalError.class );
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
			                                                defaultFederate.fedamb );
			expectedException( RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, RTIinternalError.class );
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
			                                                null /* null fedamb */ );
			expectedException( RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// SUCCESS
		}
		catch( Exception e )
		{
			wrongException( e, RTIinternalError.class );
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
			                                                defaultFederate.fedamb );
			expectedException( FederateAlreadyExecutionMember.class, RTIinternalError.class );
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
			wrongException( e, FederateAlreadyExecutionMember.class, RTIinternalError.class );
		}
		finally
		{
			defaultFederate.quickResign();
		}
	}
	
	//////////////////////////////////////////////////
	// TEST: testJoinFederationWhenSaveInProgress() //
	//////////////////////////////////////////////////
	@Test(enabled=false)
	public void testJoinFederationWhenSaveInProgress()
	{
		defaultFederate.quickJoin();
		defaultFederate.quickSaveInProgress( "save" );
		Test13Federate secondFederate = new Test13Federate( "secondFederate", this );
		try
		{
			secondFederate.rtiamb.joinFederationExecution( "secondFederate",
			                                               "JoinFederationTest",
			                                               secondFederate.fedamb );
			expectedException( SaveInProgress.class );
		}
		catch( SaveInProgress sip )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, SaveInProgress.class );
		}
	}

	/////////////////////////////////////////////////////
	// TEST: testJoinFederationWhenRestoreInProgress() //
	/////////////////////////////////////////////////////
	@Test(enabled=false)
	public void testJoinFederationWhenRestoreInProgress()
	{
		defaultFederate.quickJoin();
		defaultFederate.quickRestoreInProgress( "save" );
		Test13Federate secondFederate = new Test13Federate( "secondFederate", this );
		try
		{
			secondFederate.rtiamb.joinFederationExecution( "secondFederate",
			                                               "JoinFederationTest",
			                                               secondFederate.fedamb );
			expectedException( RestoreInProgress.class );
		}
		catch( RestoreInProgress rip )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RestoreInProgress.class );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
