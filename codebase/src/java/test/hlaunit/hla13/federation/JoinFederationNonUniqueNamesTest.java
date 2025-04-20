/*
 *   Copyright 2009 The Portico Project
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

import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.portico.lrc.PorticoConstants;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"JoinFederationNonUniqueNamesTest", "basic", "join", "federationManagement"})
public class JoinFederationNonUniqueNamesTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate secondFederate;

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
		this.secondFederate = new Test13Federate( "second", this );
	}

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		// disable unique federate name checking
		System.setProperty( PorticoConstants.PROPERTY_UNIQUE_FEDERATE_NAMES, "false" );
		// create a federation that we can test with //
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		System.setProperty( PorticoConstants.PROPERTY_UNIQUE_FEDERATE_NAMES, "true" );
		secondFederate.quickResign();
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

	////////////////////////////////////////////////////
	// TEST: (valid) testJoinFederationWithUsedName() //
	////////////////////////////////////////////////////
	@Test
	public void testJoinFederationWithUsedName()
	{
		try
		{
			// try and join a federation using the same name as the default federate
			secondFederate.rtiamb.joinFederationExecution( defaultFederate.federateName,
			                                               defaultFederate.simpleName,
			                                               defaultFederate.fedamb );
			
		}
		catch( Exception e )
		{
			unexpectedException( "joining federation with used name when checking diabled", e  );
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
