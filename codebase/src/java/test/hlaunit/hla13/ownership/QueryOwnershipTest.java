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
package hlaunit.hla13.ownership;

import hla.rti.AttributeNotDefined;
import hla.rti.FederateNotExecutionMember;
import hla.rti.ObjectNotKnown;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.portico.impl.HLAVersion;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.model.Mom;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"QueryOwnershipTest", "queryOwnership", "ownershipManagement"})
public class QueryOwnershipTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate secondFederate;
	private int aaHandle, abHandle, acHandle, baHandle;
	private int theObject;
	private int momFederationHandle;

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
		this.momFederationHandle = Mom.getMomObjectClassHandle( HLAVersion.HLA13, "Manager.Federation" );
		this.secondFederate = new Test13Federate( "secondFederate", this );
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
		
		// cache the handles
		this.aaHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "aa" );
		this.abHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ab" );
		this.acHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ac" );
		this.baHandle = defaultFederate.quickACHandle( "ObjectRoot.B", "ba" );
		
		// do publish and subscribe
		// only half the atts are published so some are unowned from the start
		defaultFederate.quickPublish( "ObjectRoot.A", "aa", "ab" );
		secondFederate.quickSubscribe( "ObjectRoot.A", "aa", "ab", "ac" );
		secondFederate.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		
		// register an object to play with
		theObject = defaultFederate.quickRegister( "ObjectRoot.A" );
		secondFederate.fedamb.waitForDiscovery( this.theObject );
		
		defaultFederate.quickSubscribe( "ObjectRoot.Manager.Federation", "FederationName" );
		defaultFederate.fedamb.waitForDiscovery( this.momFederationHandle );
		secondFederate.quickSubscribe( "ObjectRoot.Manager.Federation", "FederationName" );
		secondFederate.fedamb.waitForDiscovery( this.momFederationHandle );
		secondFederate.fedamb.waitForDiscovery( theObject );
	}

	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		// clear the handles
		this.aaHandle = PorticoConstants.NULL_HANDLE;
		this.abHandle = PorticoConstants.NULL_HANDLE;
		this.acHandle = PorticoConstants.NULL_HANDLE;
		this.baHandle = PorticoConstants.NULL_HANDLE;
		this.theObject = PorticoConstants.NULL_HANDLE;
		
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
	////////////////////////////// Query Ownership Test Methods //////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void queryAttributeOwnership( int theObject, int theAttribute )
	//     throws ObjectNotKnown,
	//            AttributeNotDefined,
	//            FederateNotExecutionMember,
	//            SaveInProgress,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;
	
	/////////////////////////////////////////////////
	// TEST: (valid) testQueryAttributeOwnership() //
	/////////////////////////////////////////////////
	@Test
	public void testQueryAttributeOwnership()
	{
		// do initial queries
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject, aaHandle),
		                     defaultFederate.federateHandle );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject, abHandle),
		                     defaultFederate.federateHandle );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject, acHandle),
		                     OWNER_UNOWNED );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject, aaHandle),
		                     defaultFederate.federateHandle );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject, abHandle),
		                     defaultFederate.federateHandle );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject, acHandle),
		                     OWNER_UNOWNED );

		// change ownership up and make sure we have the right values
		secondFederate.quickAcquireIfAvailableRequest( theObject, acHandle );
		secondFederate.fedamb.waitForOwnershipAcquisition( theObject, acHandle );
		defaultFederate.quickTick( 1.0, 1.0 );
		quickSleep();

		// do follow-up checks
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject, aaHandle),
		                     defaultFederate.federateHandle );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject, abHandle),
		                     defaultFederate.federateHandle );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject, acHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject, aaHandle),
		                     defaultFederate.federateHandle );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject, abHandle),
		                     defaultFederate.federateHandle );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject, acHandle),
		                     secondFederate.federateHandle );

		// check the MOM object
		int federationName = defaultFederate.quickACHandle("Manager.Federation", "FederationName");
		Assert.assertEquals( defaultFederate.quickQueryOwnership(this.momFederationHandle,federationName), OWNER_RTI );
		Assert.assertEquals( secondFederate.quickQueryOwnership(this.momFederationHandle,federationName), OWNER_RTI );
	}

	//////////////////////////////////////////////////////////
	// TEST: testQueryAttributeOwnershipWithUnknownObject() //
	//////////////////////////////////////////////////////////
	@Test
	public void testQueryAttributeOwnershipWithUnknownObject()
	{
		// register a new object that the second federate doesn't yet know about it
		// the handle will be valid in the federation, but unknown to secondFederate
		int secondObject = defaultFederate.quickRegister( "ObjectRoot.A" );
		
		try
		{
			secondFederate.rtiamb.queryAttributeOwnership( secondObject, aaHandle );
			expectedException( ObjectNotKnown.class );
		}
		catch( ObjectNotKnown onk )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectNotKnown.class );
		}
	}
	
	//////////////////////////////////////////////////////////
	// TEST: testQueryAttributeOwnershipWithInvalidObject() //
	//////////////////////////////////////////////////////////
	@Test
	public void testQueryAttributeOwnershipWithInvalidObject()
	{
		try
		{
			secondFederate.rtiamb.queryAttributeOwnership( -1, aaHandle );
			expectedException( ObjectNotKnown.class );
		}
		catch( ObjectNotKnown onk )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectNotKnown.class );
		}
	}
	
	/////////////////////////////////////////////////////////////
	// TEST: testQueryAttributeOwnershipWithInvalidAttribute() //
	/////////////////////////////////////////////////////////////
	@Test
	public void testQueryAttributeOwnershipWithInvalidAttribute()
	{
		try
		{
			secondFederate.rtiamb.queryAttributeOwnership( theObject, -1 );
			expectedException( AttributeNotDefined.class );
		}
		catch( AttributeNotDefined and )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeNotDefined.class );
		}
	}
	
	///////////////////////////////////////////////////////////////////
	// TEST: testQueryAttributeOwnershipWithWrongAttributeForClass() //
	///////////////////////////////////////////////////////////////////
	@Test
	public void testQueryAttributeOwnershipWithWrongAttributeForClass()
	{
		try
		{
			secondFederate.rtiamb.queryAttributeOwnership( theObject, baHandle );
			expectedException( AttributeNotDefined.class );
		}
		catch( AttributeNotDefined and )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeNotDefined.class );
		}
	}
	
	///////////////////////////////////////////////////////
	// TEST: testQueryAttributeOwnershipWhileNotJoined() //
	///////////////////////////////////////////////////////
	@Test
	public void testQueryAttributeOwnershipWhileNotJoined()
	{
		secondFederate.quickResign();

		try
		{
			secondFederate.rtiamb.queryAttributeOwnership( theObject, aaHandle );
			expectedException( FederateNotExecutionMember.class );
		}
		catch( FederateNotExecutionMember fnem )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, FederateNotExecutionMember.class );
		}
	}
	
	///////////////////////////////////////////////////////////
	// TEST: testQueryAttributeOwnershipWhenSaveInProgress() //
	///////////////////////////////////////////////////////////
	@Test
	public void testQueryAttributeOwnershipWhenSaveInProgress()
	{
		secondFederate.quickSaveInProgress( "save" );
		try
		{
			secondFederate.rtiamb.queryAttributeOwnership( theObject, aaHandle );
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

	//////////////////////////////////////////////////////////////
	// TEST: testQueryAttributeOwnershipWhenRestoreInProgress() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testQueryAttributeOwnershipWhenRestoreInProgress()
	{
		secondFederate.quickRestoreInProgress( "save" );
		try
		{
			secondFederate.rtiamb.queryAttributeOwnership( theObject, aaHandle );
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
