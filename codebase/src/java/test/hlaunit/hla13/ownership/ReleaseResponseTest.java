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

import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotDefined;
import hla.rti.AttributeNotOwned;
import hla.rti.FederateNotExecutionMember;
import hla.rti.FederateWasNotAskedToReleaseAttribute;
import hla.rti.ObjectNotKnown;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.portico.lrc.PorticoConstants;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"ReleaseResponseTest", "releaseOwnership", "ownershipManagement"})
public class ReleaseResponseTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate secondFederate;
	private int aaHandle, abHandle, acHandle;
	private int baHandle, bbHandle, bcHandle;
	private int theObject;

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
		this.baHandle = defaultFederate.quickACHandle( "ObjectRoot.A.B", "ba" );
		this.bbHandle = defaultFederate.quickACHandle( "ObjectRoot.A.B", "bb" );
		this.bcHandle = defaultFederate.quickACHandle( "ObjectRoot.A.B", "bc" );
		
		// do publish and subscribe
		// only half the atts are published so some are unowned from the start
		defaultFederate.quickPublish( "ObjectRoot.A.B", "aa", "ab", "ac" );
		secondFederate.quickSubscribe( "ObjectRoot.A.B", "aa", "ab", "ac", "ba", "bb", "bc" );
		secondFederate.quickPublish( "ObjectRoot.A.B", "aa", "ab", "ac", "ba", "bb", "bc" );
		
		
		// register an object to play with
		theObject = defaultFederate.quickRegister( "ObjectRoot.A.B" );
		secondFederate.fedamb.waitForDiscovery( theObject );
		
		// put in a release request
		secondFederate.quickAcquireRequest( theObject, aaHandle, abHandle, acHandle );
		defaultFederate.fedamb.waitForOwnershipReleaseRequest( theObject,
		                                                       aaHandle,
		                                                       abHandle,
		                                                       acHandle );
		
		// tick a bit to get all the pub/sub inital messages out of the queue
		defaultFederate.quickTick();
		secondFederate.quickTick();
	}

	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		// clear the handles
		this.aaHandle = PorticoConstants.NULL_HANDLE;
		this.abHandle = PorticoConstants.NULL_HANDLE;
		this.acHandle = PorticoConstants.NULL_HANDLE;
		this.baHandle = PorticoConstants.NULL_HANDLE;
		this.bbHandle = PorticoConstants.NULL_HANDLE;
		this.bcHandle = PorticoConstants.NULL_HANDLE;
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
	
	/////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Release Response Test Methods /////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	// public AttributeHandleSet attributeOwnershipReleaseResponse( int theObject,
	//                                                              AttributeHandleSet attributes )
	//    throws ObjectNotKnown,
	//           AttributeNotDefined,
	//           AttributeNotOwned,
	//           FederateWasNotAskedToReleaseAttribute,
	//           FederateNotExecutionMember,
	//           SaveInProgress,
	//           RestoreInProgress,
	//           RTIinternalError,
	//           ConcurrentAccessAttempted;

	//////////////////////////////////////////////////
	// TEST: (valid) testOwnershipReleaseResponse() //
	//////////////////////////////////////////////////
	@Test
	public void testOwnershipReleaseResponse()
	{
		try
		{
			AttributeHandleSet ahs = defaultFederate.createAHS( aaHandle, abHandle, acHandle );
			defaultFederate.rtiamb.attributeOwnershipReleaseResponse( theObject, ahs );
		}
		catch( Exception e )
		{
			unexpectedException( "releasing ownership of attributes through release response", e );
		}
		
		secondFederate.fedamb.waitForOwnershipAcquisition( theObject, aaHandle, abHandle, acHandle );
		
		// validate that the right ownership exists (for both federates)
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,aaHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,abHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,acHandle),
		                     secondFederate.federateHandle );
		
		// give the other federate some time to process callbacks
		defaultFederate.quickTick( 0.1, 1.0 );
		
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,aaHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,abHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,acHandle),
		                     secondFederate.federateHandle );
	}

	///////////////////////////////////////////////////////////
	// TEST: testOwnershipReleaseResponseWithInvalidObject() //
	///////////////////////////////////////////////////////////
	@Test
	public void testOwnershipReleaseResponseWithInvalidObject()
	{
		try
		{
			AttributeHandleSet ahs = defaultFederate.createAHS( aaHandle, abHandle, acHandle );
			defaultFederate.rtiamb.attributeOwnershipReleaseResponse( -1, ahs );
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

	///////////////////////////////////////////////////////////
	// TEST: testOwnershipReleaseResponseWithUnknownObject() //
	///////////////////////////////////////////////////////////
	@Test
	public void testOwnershipReleaseResponseWithUnknownObject()
	{
		// register an object that won't be discovered by the default federate, giving us a handle
		// that is valid in the federation, but unknown by the default federate
		int secondObject = secondFederate.quickRegister( "ObjectRoot.A.B" );
		
		try
		{
			AttributeHandleSet ahs = defaultFederate.createAHS( aaHandle, abHandle, acHandle );
			defaultFederate.rtiamb.attributeOwnershipReleaseResponse( secondObject, ahs );
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

	//////////////////////////////////////////////////////////////
	// TEST: testOwnershipReleaseResponseWithInvalidAttribute() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testOwnershipReleaseResponseWithInvalidAttribute()
	{
		try
		{
			AttributeHandleSet ahs = defaultFederate.createAHS( aaHandle, abHandle, -1 );
			defaultFederate.rtiamb.attributeOwnershipReleaseResponse( theObject, ahs );
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

	////////////////////////////////////////////////////////////////////
	// TEST: testOwnershipReleaseResponseWithWrongAttributeForClass() //
	////////////////////////////////////////////////////////////////////
	@Test
	public void testOwnershipReleaseResponseWithWrongAttributeForClass()
	{
		// register an object of type ObjectRoot.A and try and release "ba"
		defaultFederate.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		int otherObject = defaultFederate.quickRegister( "ObjectRoot.A" );
		// put in place a valid release request so that doesn't trip us up
		secondFederate.quickSubscribe( "ObjectRoot.A", "aa", "ab", "ac" );
		secondFederate.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		secondFederate.fedamb.waitForDiscovery( otherObject );
		secondFederate.quickAcquireRequest( otherObject, aaHandle, abHandle, acHandle );
		defaultFederate.fedamb.waitForOwnershipReleaseRequest( otherObject,
		                                                       aaHandle,
		                                                       abHandle,
		                                                       acHandle );
		
		// now try and response, but with a wrong handle for the class
		try
		{
			AttributeHandleSet ahs = defaultFederate.createAHS( baHandle, bbHandle, bcHandle );
			defaultFederate.rtiamb.attributeOwnershipReleaseResponse( otherObject, ahs );
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

	//////////////////////////////////////////////////////////////
	// TEST: testOwnershipReleaseResponseWithUnownedAttribute() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testOwnershipReleaseResponseWithUnownedAttribute()
	{
		try
		{
			AttributeHandleSet ahs = defaultFederate.createAHS( baHandle );
			defaultFederate.rtiamb.attributeOwnershipReleaseResponse( theObject, ahs );
			expectedException( AttributeNotOwned.class );
		}
		catch( AttributeNotOwned ano )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeNotOwned.class );
		}
	}

	//////////////////////////////////////////////////////////////////
	// TEST: testOwnershipReleaseResponseWithNoOutstandingRequest() //
	//////////////////////////////////////////////////////////////////
	@Test
	public void testOwnershipReleaseResponseWithNoOutstandingRequest()
	{
		// cancel the outstanding request put in place in setUp
		secondFederate.quickCancelAcquire( theObject, aaHandle, abHandle, acHandle );
		secondFederate.fedamb.waitForOwnershipAcquireCancel( theObject,
		                                                     aaHandle,
		                                                     abHandle,
		                                                     acHandle );
		
		try
		{
			AttributeHandleSet ahs = defaultFederate.createAHS( aaHandle, abHandle, acHandle );
			defaultFederate.rtiamb.attributeOwnershipReleaseResponse( theObject, ahs );
			expectedException( FederateWasNotAskedToReleaseAttribute.class );
		}
		catch( FederateWasNotAskedToReleaseAttribute fwnatra )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, FederateWasNotAskedToReleaseAttribute.class );
		}
	}

	///////////////////////////////////////////////////////
	// TEST: testOwnershipReleaseResponseWhenNotJoined() //
	///////////////////////////////////////////////////////
	@Test
	public void testOwnershipReleaseResponseWhenNotJoined()
	{
		defaultFederate.quickResign();

		try
		{
			AttributeHandleSet ahs = defaultFederate.createAHS( aaHandle, abHandle, acHandle );
			defaultFederate.rtiamb.attributeOwnershipReleaseResponse( theObject, ahs );
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

	////////////////////////////////////////////////////////////
	// TEST: testOwnershipReleaseResponseWhenSaveInProgress() //
	////////////////////////////////////////////////////////////
	@Test
	public void testOwnershipReleaseResponseWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			AttributeHandleSet ahs = defaultFederate.createAHS( aaHandle, abHandle, acHandle );
			defaultFederate.rtiamb.attributeOwnershipReleaseResponse( theObject, ahs );
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
	
	///////////////////////////////////////////////////////////////
	// TEST: testOwnershipReleaseResponseWhenRestoreInProgress() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testOwnershipReleaseResponseWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			AttributeHandleSet ahs = defaultFederate.createAHS( aaHandle, abHandle, acHandle );
			defaultFederate.rtiamb.attributeOwnershipReleaseResponse( theObject, ahs );
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
