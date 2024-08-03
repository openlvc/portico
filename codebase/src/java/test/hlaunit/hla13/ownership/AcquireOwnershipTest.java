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

import hla.rti.AttributeAcquisitionWasNotRequested;
import hla.rti.AttributeAlreadyOwned;
import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotDefined;
import hla.rti.AttributeNotPublished;
import hla.rti.DeletePrivilegeNotHeld;
import hla.rti.FederateNotExecutionMember;
import hla.rti.FederateOwnsAttributes;
import hla.rti.FederateWasNotAskedToReleaseAttribute;
import hla.rti.ObjectClassNotPublished;
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

@Test(singleThreaded=true, groups={"AcquireOwnershipTest", "acquireOwnership", "ownershipManagement"})
public class AcquireOwnershipTest extends Abstract13Test
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
	
	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Acquire Ownership Test Methods /////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	//public void attributeOwnershipAcquisition( int object, AttributeHandleSet atts, byte[] tag )
	//    throws ObjectNotKnown,
	//           ObjectClassNotPublished,
	//           AttributeNotDefined,
	//           AttributeNotPublished,
	//           FederateOwnsAttributes,
	//           FederateNotExecutionMember,
	//           SaveInProgress,
	//           RestoreInProgress,
	//           RTIinternalError,
	//           ConcurrentAccessAttempted
	
	//////////////////////////////////////////////
	// TEST: (valid) testAttributeAcquisition() //
	//////////////////////////////////////////////
	@Test
	public void testAttributeAcquisition()
	{
		// request currently owned attributes from the default federate
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( aaHandle, abHandle );
			secondFederate.rtiamb.attributeOwnershipAcquisition(theObject, attributes, new byte[0]);
		}
		catch( Exception e )
		{
			unexpectedException( "Issuing request to aquire owned attributes", e );
		}
		
		// wait for the timeout on the acquisition - we can't have them yet
		secondFederate.fedamb.waitForOwnershipAcquisitionTimeout( theObject, aaHandle, abHandle );
		
		// wait for the request in the default federate
		defaultFederate.fedamb.waitForOwnershipReleaseRequest( theObject, aaHandle, abHandle );
		// now issue the release
		defaultFederate.quickReleaseResponse( theObject, aaHandle, abHandle );
		
		// wait for the notficiation in the second federate
		secondFederate.fedamb.waitForOwnershipAcquisition( theObject, aaHandle, abHandle );
		
		// validate that the right ownership exists (for both federates)
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,aaHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,abHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,acHandle),
		                     defaultFederate.federateHandle );
		
		// tick the default federate incase the ownership exchange information is still queued
		defaultFederate.quickTick( 0.1, 1.0 );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,aaHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,abHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,acHandle),
		                     defaultFederate.federateHandle );
	}

	///////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testAttributeAcquisitionUsingUnconditionalDivestToRelease() //
	///////////////////////////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionUsingUnconditionalDivestToRelease()
	{
		// do the release request
		secondFederate.quickAcquireRequest( theObject, aaHandle, abHandle );
		defaultFederate.fedamb.waitForOwnershipReleaseRequest( theObject, aaHandle, abHandle );
		
		// release the attributes
		defaultFederate.quickUnconditionalDivest( theObject, aaHandle, abHandle );
		secondFederate.fedamb.waitForOwnershipAcquisition( theObject, aaHandle, abHandle );
		
		// make sure the ownership is represented properly
		secondFederate.quickAssertOwnership( secondFederate.federateHandle,
		                                     theObject, aaHandle, abHandle );
		secondFederate.quickAssertOwnership( defaultFederate.federateHandle, theObject, acHandle );
		
		defaultFederate.quickTick( 0.1, 1.0 );
		defaultFederate.quickAssertOwnership( secondFederate.federateHandle,
		                                      theObject, aaHandle, abHandle );
		defaultFederate.quickAssertOwnership( defaultFederate.federateHandle, theObject, acHandle );
	}
	
	///////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testAttributeAcquisitionUsingNegotiatedDivestToRelease() //
	///////////////////////////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionUsingNegotiatedDivestToRelease()
	{
		// do the release request
		secondFederate.quickAcquireRequest( theObject, aaHandle, abHandle );
		defaultFederate.fedamb.waitForOwnershipReleaseRequest( theObject, aaHandle, abHandle );
		
		// release the attributes
		defaultFederate.quickNegotiatedDivetRequest( theObject, aaHandle, abHandle );
		secondFederate.fedamb.waitForOwnershipAcquisition( theObject, aaHandle, abHandle );
		
		// make sure the ownership is represented properly
		secondFederate.quickAssertOwnership( secondFederate.federateHandle,
		                                     theObject, aaHandle, abHandle );
		secondFederate.quickAssertOwnership( defaultFederate.federateHandle, theObject, acHandle );
		
		defaultFederate.quickTick( 0.1, 1.0 );
		defaultFederate.quickAssertOwnership( secondFederate.federateHandle,
		                                      theObject, aaHandle, abHandle );
		defaultFederate.quickAssertOwnership( defaultFederate.federateHandle, theObject, acHandle );
	}
	
	//////////////////////////////////////////////////////////////////
	// TEST: (valid) testAttributeAcquisitionForUnownedAttributes() //
	//////////////////////////////////////////////////////////////////
	/**
	 * This method tests that unowned attirbutes are immediately handed over to a federate when
	 * they request them. This should work just like the if-available counterpart, so we just need
	 * a brief little test here.
	 */
	@Test
	public void testAttributeAcquisitionForUnownedAttributes()
	{
		// issue the request
		secondFederate.quickAcquireRequest( theObject, baHandle, bbHandle, bcHandle );
		// wait for the callback notifying us that we have it
		secondFederate.fedamb.waitForOwnershipAcquisition(theObject, baHandle, bbHandle, bcHandle);
	}

	///////////////////////////////////////////////////////////
	// TEST: (valid) testAttributeAcquisitionForPartialSet() //
	///////////////////////////////////////////////////////////
	/**
	 * This test makes sure everything behaves properly when only some of the attributes that
	 * were requested to be released are released.
	 */
	@Test
	public void testAttributeAcquisitionForPartialSet()
	{
		// request currently owned attributes from the default federate
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( aaHandle, abHandle );
			secondFederate.rtiamb.attributeOwnershipAcquisition(theObject, attributes, new byte[0]);
		}
		catch( Exception e )
		{
			unexpectedException( "Issuing request to aquire owned attributes", e );
		}
		
		// wait for the timeout on the acquisition - we can't have them yet
		secondFederate.fedamb.waitForOwnershipAcquisitionTimeout( theObject, aaHandle, abHandle );
		
		// wait for the request in the default federate
		defaultFederate.fedamb.waitForOwnershipReleaseRequest( theObject, aaHandle, abHandle );
		// now issue the release
		defaultFederate.quickReleaseResponse( theObject, aaHandle );
		
		// wait for the notficiation in the second federate
		secondFederate.fedamb.waitForOwnershipAcquisition( theObject, aaHandle );
		
		// validate that the right ownership exists (for both federates)
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,aaHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,abHandle),
		                     defaultFederate.federateHandle );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,acHandle),
		                     defaultFederate.federateHandle );
		
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,aaHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,abHandle),
		                     defaultFederate.federateHandle );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,acHandle),
		                     defaultFederate.federateHandle );
	}

	/////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testAttributeAcquisitionWithSomeOwnedAndSomeUnowned() //
	/////////////////////////////////////////////////////////////////////////
	/**
	 * Request the acquisition of attributes that are both unowned and owned.
	 */
	@Test
	public void testAttributeAcquisitionWithSomeOwnedAndSomeUnowned()
	{
		secondFederate.quickAcquireRequest( theObject, aaHandle, abHandle, baHandle, bbHandle );
		secondFederate.fedamb.waitForOwnershipAcquisition( theObject, baHandle, bbHandle );
		secondFederate.fedamb.waitForOwnershipAcquisitionTimeout( theObject, aaHandle, abHandle );
		defaultFederate.fedamb.waitForOwnershipReleaseRequest( theObject, aaHandle, abHandle );
		defaultFederate.quickReleaseResponse( theObject, aaHandle, abHandle );
		secondFederate.fedamb.waitForOwnershipAcquisition( theObject, aaHandle, abHandle );
	}

	/////////////////////////////////////////////////////////////////
	// TEST: (valid) testAttributeAcquisitionOfPrivilegeToDelete() //
	/////////////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionOfPrivilegeToDelete()
	{
		// exchange ownership of privilege to delete between the two federates
		int privilegeToDelete = secondFederate.quickACHandle( "ObjectRoot", "privilegeToDelete" );
		secondFederate.quickExchangeOwnership( defaultFederate, theObject, privilegeToDelete );
		
		// make sure default federate can no longer delete the object, but the second federate can
		try
		{
			defaultFederate.rtiamb.deleteObjectInstance( theObject, new byte[0] );
			expectedException( DeletePrivilegeNotHeld.class );
		}
		catch( DeletePrivilegeNotHeld dpnh )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, DeletePrivilegeNotHeld.class );
		}
		
		// delete the object in the second federate now that we've transferred ownership
		secondFederate.quickDelete( theObject );
	}

	/////////////////////////////////////////////////
	// TEST: (valid) testRapidDivestAndReacquire() //
	/////////////////////////////////////////////////
	//
	// This test is currently failing for JGroups only. Everything works fine in JVM binding.
	// Report is that if the wait time between return exchange of ownership is long enough,
	// it will work (hence the two quickTick() blocks for testing various cadences).
	//
	@Test
	public void testRapidDivestAndReacquire()
	{
		// exchange ownership of privilege to delete between the two federates
		int aa = secondFederate.quickACHandle( "ObjectRoot.A", "aa" );
		int ab = secondFederate.quickACHandle( "ObjectRoot.A", "ab" );
		int ac = secondFederate.quickACHandle( "ObjectRoot.A", "ac" );
		
		for( int i = 0; i < 10; i++ )
		{
			//System.out.println( "Starting exchange "+i );
			//System.out.print( "   Hand off..." );
			secondFederate.quickExchangeOwnership( defaultFederate, theObject, aa, ab, ac );
			secondFederate.quickAssertIOwn( theObject, aa, ab, ac );
			defaultFederate.quickAssertIDontOwn( theObject, aa, ab, ac );
			//System.out.println( "done" );
			
			defaultFederate.quickTick();
			secondFederate.quickTick();
			
			//System.out.print( "   Take back..." );
			defaultFederate.quickExchangeOwnership( secondFederate, theObject, aa, ab, ac );
			defaultFederate.quickAssertIOwn( theObject, aa, ab, ac );
			secondFederate.quickAssertIDontOwn( theObject, aa, ab, ac );

			defaultFederate.quickTick();
			secondFederate.quickTick();
		}
	}
	
	///////////////////////////////////////////////////////
	// TEST: testAttributeAcquisitionWithUnknownObject() //
	///////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionWithUnknownObject()
	{
		// register a second, undiscovered object
		int other = defaultFederate.quickRegister( "ObjectRoot.A.B" );
		
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle );
			secondFederate.rtiamb.attributeOwnershipAcquisition( other, attributes, new byte[0] );
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
	// TEST: testAttributeAcquisitionWithInvalidObjectHandle() //
	/////////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionWithInvalidObjectHandle()
	{
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle );
			secondFederate.rtiamb.attributeOwnershipAcquisition( -1, attributes, new byte[0] );
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
	// TEST: testAttributeAcquisitionWithUnpublishedClass() //
	//////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionWithUnpublishedClass()
	{
		// unpublish ObjectRoot.A.B in the second federate
		secondFederate.quickUnpublishOC( "ObjectRoot.A.B" );
		
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle );
			secondFederate.rtiamb.attributeOwnershipAcquisition(theObject, attributes, new byte[0]);
			expectedException( ObjectClassNotPublished.class );
		}
		catch( ObjectClassNotPublished ocnp )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectClassNotPublished.class );
		}
	}
	
	//////////////////////////////////////////////////////////////
	// TEST: testAttributeAcquisitionWithUnpublishedAttribute() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionWithUnpublishedAttribute()
	{
		// publish over the top of the previous publication, giving us a publication of only ba, bb
		secondFederate.quickPublish( "ObjectRoot.A.B", "ba", "bb" );
		
		try
		{
			// try and get hold of bc
			AttributeHandleSet attributes = secondFederate.createAHS(baHandle, bbHandle, bcHandle);
			secondFederate.rtiamb.attributeOwnershipAcquisition(theObject, attributes, new byte[0]);
			expectedException( AttributeNotPublished.class );
		}
		catch( AttributeNotPublished anp )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeNotPublished.class );
		}
	}
	
	//////////////////////////////////////////////////////////
	// TEST: testAttributeAcquisitionWithInvalidAttribute() //
	//////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionWithInvalidAttribute()
	{
		try
		{
			// try and get hold of bc
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle, -1 );
			secondFederate.rtiamb.attributeOwnershipAcquisition(theObject, attributes, new byte[0]);
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
	
	///////////////////////////////////////////////////////////////
	// TEST: testAttributeAcquisitionWithAlreadyOwnedAttribute() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionWithAlreadyOwnedAttribute()
	{
		try
		{
			// try and get hold of bc
			AttributeHandleSet attributes = defaultFederate.createAHS( aaHandle );
			defaultFederate.rtiamb.attributeOwnershipAcquisition( theObject,
			                                                      attributes,
			                                                      new byte[0] );
			expectedException( FederateOwnsAttributes.class );
		}
		catch( FederateOwnsAttributes foa )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, FederateOwnsAttributes.class );
		}
	}
	
	//////////////////////////////////////////////////////////
	// TEST: testAttributeAcquisitionFromUnjoinedFederate() //
	//////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionFromUnjoinedFederate()
	{
		secondFederate.quickResign();
		
		try
		{
			// try and get hold of bc
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle );
			secondFederate.rtiamb.attributeOwnershipAcquisition(theObject, attributes, new byte[0]);
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

	////////////////////////////////////////////////////////
	// TEST: testAttributeAcquisitionWhenSaveInProgress() //
	////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionWhenSaveInProgress()
	{
		secondFederate.quickSaveInProgress( "save" );
		try
		{
			// try and get hold of bc
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle );
			secondFederate.rtiamb.attributeOwnershipAcquisition(theObject, attributes, new byte[0]);
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

	///////////////////////////////////////////////////////////
	// TEST: testAttributeAcquisitionWhenRestoreInProgress() //
	///////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionWhenRestoreInProgress()
	{
		secondFederate.quickRestoreInProgress( "save" );
		try
		{
			// try and get hold of bc
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle );
			secondFederate.rtiamb.attributeOwnershipAcquisition(theObject, attributes, new byte[0]);
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

	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// Acquire If Available Test Methods ///////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	//public void attributeOwnershipAcquisitionIfAvailable( int theObject,
	//                                                      AttributeHandleSet attributes )
	//    throws ObjectNotKnown,
	//           ObjectClassNotPublished,
	//           AttributeNotDefined,
	//           AttributeNotPublished,
	//           FederateOwnsAttributes,
	//           AttributeAlreadyBeingAcquired,
	//           FederateNotExecutionMember,
	//           SaveInProgress,
	//           RestoreInProgress,
	//           RTIinternalError,
	//           ConcurrentAccessAttempted

	//////////////////////////////////////////////
	// TEST: (valid) testAttributeAcquisition() //
	//////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionIfAvailable()
	{
		// second federate should be able to pick up ownership of ba, bb and bc right away
		// because the defaultFederate isn't publishing them, so they're unowned
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle, bbHandle, bcHandle );
			secondFederate.rtiamb.attributeOwnershipAcquisitionIfAvailable( theObject, attributes );
		}
		catch( Exception e )
		{
			unexpectedException( "Acquiring unowned attributes (if available)", e );
		}
		
		// wait for the notification callback
		defaultFederate.quickTick ( 0.1, 1.0 );
		secondFederate.fedamb.waitForOwnershipAcquisition( theObject, baHandle, bbHandle, bcHandle );

		// validate the ownership listings
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,baHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,bbHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,bcHandle),
		                     secondFederate.federateHandle );
		
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,baHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,bbHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,bcHandle),
		                     secondFederate.federateHandle );
	}

	//////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testAttributeAcquisitionIfAvailableForSomeAttributes() //
	//////////////////////////////////////////////////////////////////////////
	/**
	 * Tests acquire-if-available where only some of the attibutes are available.
	 */
	@Test
	public void testAttributeAcquisitionIfAvailableForSomeAttributes()
	{
		// second federate should be able to pick up ba and bb, but not aa
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS(aaHandle, baHandle, bbHandle);
			secondFederate.rtiamb.attributeOwnershipAcquisitionIfAvailable( theObject, attributes );
		}
		catch( Exception e )
		{
			unexpectedException( "Acquiring some unowned attributes (if available)", e );
		}
		
		// wait for the notification callbacks
		secondFederate.fedamb.waitForOwnershipAcquisition( theObject, baHandle, bbHandle  );
		secondFederate.fedamb.waitForOwnershipUnavailable( theObject, aaHandle );
		
		quickSleep();
		
		// validate the ownership listings
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,aaHandle),
		                     defaultFederate.federateHandle );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,baHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,bbHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,bcHandle),
		                     OWNER_UNOWNED );
		
		// let the default federate tick a little bit so it gets all the information
		defaultFederate.quickTick( 0.1, 1.0 );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,aaHandle),
		                     defaultFederate.federateHandle );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,baHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,bbHandle),
		                     secondFederate.federateHandle );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,bcHandle),
		                     OWNER_UNOWNED );
	}

	/////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testAttributeAcquisitionIfAvailableForUnavailableAttributes() //
	/////////////////////////////////////////////////////////////////////////////////
	/**
	 * This test will use the attributeOwnershipAcquisitionIfAvailable() call for attributes that
	 * are not directly available to be aquired. This should result in an
	 * "attributeOwnershipUnavailable()" callback and no release request should be sent through to
	 * the federate that does own the attributes.
	 */
	@Test
	public void testAttributeAcquisitionIfAvailableForUnavailableAttributes()
	{
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( aaHandle, abHandle );
			secondFederate.rtiamb.attributeOwnershipAcquisitionIfAvailable( theObject, attributes );
		}
		catch( Exception e )
		{
			unexpectedException( "Requesting aquire of attributes (if availble)", e );
		}
		
		// wait for the notification that we can't have them
		secondFederate.fedamb.waitForOwnershipUnavailable( theObject, aaHandle, abHandle );
		// make sure no request hits the other federate
		defaultFederate.fedamb.waitForOwnershipReleaseRequestTimeout( theObject,aaHandle,abHandle );
	}	
	
	///////////////////////////////////////////////////////
	// TEST: testAttributeAcquisitionWithUnknownObject() //
	///////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionIfAvailableWithUnknownObject()
	{
		// register a second, undiscovered object
		int other = defaultFederate.quickRegister( "ObjectRoot.A.B" );
		
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle );
			secondFederate.rtiamb.attributeOwnershipAcquisitionIfAvailable( other, attributes );
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
	// TEST: testAttributeAcquisitionWithInvalidObjectHandle() //
	/////////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionIfAvailableWithInvalidObjectHandle()
	{
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle );
			secondFederate.rtiamb.attributeOwnershipAcquisitionIfAvailable( -1, attributes );
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
	// TEST: testAttributeAcquisitionWithUnpublishedClass() //
	//////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionIfAvailableWithUnpublishedClass()
	{
		// unpublish ObjectRoot.A.B in the second federate
		secondFederate.quickUnpublishOC( "ObjectRoot.A.B" );
		
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle );
			secondFederate.rtiamb.attributeOwnershipAcquisitionIfAvailable( theObject, attributes );
			expectedException( ObjectClassNotPublished.class );
		}
		catch( ObjectClassNotPublished ocnp )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectClassNotPublished.class );
		}
	}
	
	//////////////////////////////////////////////////////////////
	// TEST: testAttributeAcquisitionWithUnpublishedAttribute() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionIfAvailableWithUnpublishedAttribute()
	{
		// publish over the top of the previous publication, giving us a publication of only ba, bb
		secondFederate.quickPublish( "ObjectRoot.A.B", "ba", "bb" );
		
		try
		{
			// try and get hold of bc
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle, bbHandle, bcHandle );
			secondFederate.rtiamb.attributeOwnershipAcquisitionIfAvailable( theObject, attributes );
			expectedException( AttributeNotPublished.class );
		}
		catch( AttributeNotPublished anp )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeNotPublished.class );
		}
	}
	
	//////////////////////////////////////////////////////////
	// TEST: testAttributeAcquisitionWithInvalidAttribute() //
	//////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionIfAvailableWithInvalidAttribute()
	{
		try
		{
			// try and get hold of bc
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle, -1 );
			secondFederate.rtiamb.attributeOwnershipAcquisitionIfAvailable( theObject, attributes );
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
	
	///////////////////////////////////////////////////////////////
	// TEST: testAttributeAcquisitionWithAlreadyOwnedAttribute() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionIfAvailableWithAlreadyOwnedAttribute()
	{
		try
		{
			// try and get hold of bc
			AttributeHandleSet attributes = defaultFederate.createAHS( aaHandle );
			defaultFederate.rtiamb.attributeOwnershipAcquisitionIfAvailable( theObject, attributes );
			expectedException( FederateOwnsAttributes.class );
		}
		catch( FederateOwnsAttributes foa )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, FederateOwnsAttributes.class );
		}
	}
	
	//////////////////////////////////////////////////////////
	// TEST: testAttributeAcquisitionFromUnjoinedFederate() //
	//////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionIfAvailableFromUnjoinedFederate()
	{
		secondFederate.quickResign();
		
		try
		{
			// try and get hold of bc
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle );
			secondFederate.rtiamb.attributeOwnershipAcquisitionIfAvailable( theObject, attributes );
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

	///////////////////////////////////////////////////////////////////
	// TEST: testAttributeAcquisitionIfAvailableWhenSaveInProgress() //
	///////////////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionIfAvailableWhenSaveInProgress()
	{
		secondFederate.quickSaveInProgress( "save" );
		try
		{
			// try and get hold of bc
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle );
			secondFederate.rtiamb.attributeOwnershipAcquisitionIfAvailable( theObject, attributes );
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

	//////////////////////////////////////////////////////////////////////
	// TEST: testAttributeAcquisitionIfAvailableWhenRestoreInProgress() //
	//////////////////////////////////////////////////////////////////////
	@Test
	public void testAttributeAcquisitionIfAvailableWhenRestoreInProgress()
	{
		secondFederate.quickRestoreInProgress( "save" );
		try
		{
			// try and get hold of bc
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle );
			secondFederate.rtiamb.attributeOwnershipAcquisitionIfAvailable( theObject, attributes );
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

	///////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////// Cancel Ownership Acquisition Test Methods ////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// public void cancelAttributeAcquisition( int object, AttributeHandleSet attributes )
	//     throws ObjectNotKnown,
	//            AttributeNotDefined,
	//            AttributeAlreadyOwned,
	//            AttributeAcquisitionWasNotRequested,
	//            FederateNotExecutionMember,
	//            SaveInProgress,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

	////////////////////////////////////////////////////
	// TEST: (valid) testCancelAttributeAcquisition() //
	////////////////////////////////////////////////////
	@Test(groups="cancelAcquisition")
	public void testCancelAttributeOwnershipAcquisition()
	{
		secondFederate.quickAcquireRequest( theObject, aaHandle );
		defaultFederate.fedamb.waitForOwnershipReleaseRequest( theObject, aaHandle );
		
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( aaHandle );
			secondFederate.rtiamb.cancelAttributeOwnershipAcquisition( theObject, attributes );
		}
		catch( Exception e )
		{
			unexpectedException( "Cancelling attribute ownership acquisition", e );
		}
		
		secondFederate.fedamb.waitForOwnershipAcquireCancel( theObject, aaHandle );
	}

	///////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testCancelAttributeAcquisitionWhenAllAreBeingReleased() //
	///////////////////////////////////////////////////////////////////////////
	/**
	 * Test the cancel acquisition request when the owning federate has already received the
	 * release request and released ALL of the attributes, but the second federate has NOT
	 * received notification of ownership assumption yet (hasn't ticked for it). An exception
	 * should occur because ownership transfer has already taken place and NO cancel confirmations
	 * should be received.
	 */
	@Test(groups="cancelAcquisition")
	public void testCancelAttributeAcquisitionAfterReleased()
	{
		secondFederate.quickAcquireRequest( theObject, aaHandle, abHandle );
		defaultFederate.fedamb.waitForOwnershipReleaseRequest( theObject, aaHandle, abHandle );
		defaultFederate.quickReleaseResponse( theObject, aaHandle, abHandle );
		
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( aaHandle, abHandle );
			secondFederate.rtiamb.cancelAttributeOwnershipAcquisition( theObject, attributes );
			expectedException( AttributeAlreadyOwned.class );
		}
		catch( AttributeAlreadyOwned aao )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeAlreadyOwned.class );
		}
		
		// now make sure we DO NOT get confirmation that acquisition request for the
		// other attribute has been cancelled
		secondFederate.fedamb.waitForOwnershipAcquireCancelTimeout( theObject, aaHandle, abHandle );
	}

	//////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testCancelAttributeAcquisitionForOnlySomeRequestedAttributes() //
	//////////////////////////////////////////////////////////////////////////////////
	/**
	 * Request the release of some attributes, then cancel that request for only some of those
	 * that we wanted released in the first place (so that some of the initial request still
	 * stands). Make sure that when the owning federate tries to release all of them, that it
	 * fails, but when it tries to release only those that were not part of the cancellation, that
	 * it succeeds.
	 */
	@Test(groups="cancelAcquisition")
	public void testCancelAttributeAcquisitionForOnlySomeRequestedAttributes()
	{
		// issue the initial request
		secondFederate.quickAcquireRequest( theObject, aaHandle, abHandle, acHandle );
		defaultFederate.fedamb.waitForOwnershipReleaseRequest( theObject,
		                                                       aaHandle,
		                                                       abHandle,
		                                                       acHandle );
		
		// cancel the request for some of the attributes
		secondFederate.quickCancelAcquire( theObject, aaHandle, abHandle );
		secondFederate.fedamb.waitForOwnershipAcquireCancel( theObject, aaHandle, abHandle );
		
		// make sure there is an exception generated when the default federate tries to
		// release all of the attributes, now that some of the request has been cancelled
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
		
		// now just release the third attribute
		defaultFederate.quickReleaseResponse( theObject, acHandle );
		secondFederate.fedamb.waitForOwnershipAcquisition( theObject, acHandle );
	}
	
	/////////////////////////////////////////////////////////////
	// TEST: testCancelAttributeAcquisitionWithUnknownObject() //
	/////////////////////////////////////////////////////////////
	@Test(groups="cancelAcquisition")
	public void testCancelAttributeAcquisitionWithUnknownObject()
	{
		// register another object, but don't have it discovered in the second federate
		// this will be a valid object, but undiscovered for the purposes of the test
		int other = defaultFederate.quickRegister( "ObjectRoot.A.B" );
		
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle );
			secondFederate.rtiamb.cancelAttributeOwnershipAcquisition( other, attributes );
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

	///////////////////////////////////////////////////////////////////
	// TEST: testCancelAttributeAcquisitionWithInvalidObjectHandle() //
	///////////////////////////////////////////////////////////////////
	@Test(groups="cancelAcquisition")
	public void testCancelAttributeAcquisitionWithInvalidObjectHandle()
	{
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( baHandle );
			secondFederate.rtiamb.cancelAttributeOwnershipAcquisition( -1, attributes );
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

	////////////////////////////////////////////////////////////////
	// TEST: testCancelAttributeAcquisitionWithInvalidAttribute() //
	////////////////////////////////////////////////////////////////
	@Test(groups="cancelAcquisition")
	public void testCancelAttributeAcquisitionWithInvalidAttribute()
	{
		// request ownership of at least one attribute so we've got some valid in the cancel request
		secondFederate.quickAcquireRequest( theObject, aaHandle );
		
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( aaHandle, -1 );
			secondFederate.rtiamb.cancelAttributeOwnershipAcquisition( theObject, attributes );
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

	/////////////////////////////////////////////////////////////////////
	// TEST: testCancelAttributeAcquisitionWithAlreadyOwnedAttribute() //
	/////////////////////////////////////////////////////////////////////
	@Test(groups="cancelAcquisition")
	public void testCancelAttributeAcquisitionWithAlreadyOwnedAttribute()
	{
		// transfer aa to the second federate
		secondFederate.quickAcquireRequest( theObject, aaHandle );
		defaultFederate.fedamb.waitForOwnershipReleaseRequest( theObject, aaHandle );
		defaultFederate.quickReleaseResponse( theObject, aaHandle );
		secondFederate.fedamb.waitForOwnershipAcquisition( theObject, aaHandle );
		
		// now try and cancel the transfer
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( aaHandle );
			secondFederate.rtiamb.cancelAttributeOwnershipAcquisition( theObject, attributes );
			expectedException( AttributeAlreadyOwned.class );
		}
		catch( AttributeAlreadyOwned aao )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeAlreadyOwned.class );
		}
	}

	/////////////////////////////////////////////////////////////////////
	// TEST: testCancelAttributeAcquisitionWithoutOutstandingRequest() //
	/////////////////////////////////////////////////////////////////////
	@Test(groups="cancelAcquisition")
	public void testCancelAttributeAcquisitionWithoutOutstandingRequest()
	{
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( aaHandle );
			secondFederate.rtiamb.cancelAttributeOwnershipAcquisition( theObject, attributes );
			expectedException( AttributeAcquisitionWasNotRequested.class );
		}
		catch( AttributeAcquisitionWasNotRequested aawnr )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeAcquisitionWasNotRequested.class );
		}
	}

	////////////////////////////////////////////////////////////////
	// TEST: testCancelAttributeAcquisitionWithUnjoinedFederate() //
	////////////////////////////////////////////////////////////////
	@Test(groups="cancelAcquisition")
	public void testCancelAttributeAcquisitionWithUnjoinedFederate()
	{
		secondFederate.quickResign();
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( aaHandle );
			secondFederate.rtiamb.cancelAttributeOwnershipAcquisition( theObject, attributes );
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

	//////////////////////////////////////////////////////////////
	// TEST: testCancelAttributeAcquisitionWhenSaveInProgress() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testCancelAttributeAcquisitionWhenSaveInProgress()
	{
		secondFederate.quickSaveInProgress( "save" );
		try
		{
			// try and get hold of bc
			AttributeHandleSet attributes = secondFederate.createAHS( aaHandle );
			secondFederate.rtiamb.cancelAttributeOwnershipAcquisition( theObject, attributes );
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

	/////////////////////////////////////////////////////////////////
	// TEST: testCancelAttributeAcquisitionWhenRestoreInProgress() //
	/////////////////////////////////////////////////////////////////
	@Test
	public void testCancelAttributeAcquisitionWhenRestoreInProgress()
	{
		secondFederate.quickRestoreInProgress( "save" );
		try
		{
			// try and get hold of bc
			AttributeHandleSet attributes = secondFederate.createAHS( aaHandle );
			secondFederate.rtiamb.cancelAttributeOwnershipAcquisition( theObject, attributes );
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
