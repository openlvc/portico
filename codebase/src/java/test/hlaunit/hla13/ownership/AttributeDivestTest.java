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

import hla.rti.AttributeAlreadyBeingDivested;
import hla.rti.AttributeDivestitureWasNotRequested;
import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotDefined;
import hla.rti.AttributeNotOwned;
import hla.rti.FederateNotExecutionMember;
import hla.rti.ObjectNotKnown;
import hla.rti.DeletePrivilegeNotHeld;
import hla.rti.ResignAction;
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

@Test(singleThreaded=true, groups={"AttributeDivestTest", "divestOwnership", "ownershipManagement"})
public class AttributeDivestTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate secondFederate;
	private Test13Federate thirdFederate;
	private int aaHandle, abHandle, acHandle;
	private int baHandle;
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
		this.thirdFederate = new Test13Federate( "thirdFederate", this );
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
		thirdFederate.quickJoin();

		// cache the handles
		this.aaHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "aa" );
		this.abHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ab" );
		this.acHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ac" );
		this.baHandle = defaultFederate.quickACHandle( "ObjectRoot.A.B", "ba" );
		
		// do publish and subscribe
		// only half the atts are published so some are unowned from the start
		defaultFederate.quickPublish( "ObjectRoot.A.B", "aa", "ab", "ac" );
		secondFederate.quickSubscribe( "ObjectRoot.A.B", "aa", "ab", "ac", "ba", "bb", "bc" );
		secondFederate.quickPublish( "ObjectRoot.A.B", "aa", "ab", "ac", "ba", "bb", "bc" );
		thirdFederate.quickSubscribe( "ObjectRoot.A.B", "aa", "ab", "ac", "ba", "bb", "bc" );
		thirdFederate.quickPublish( "ObjectRoot.A.B", "aa", "ab", "ac", "ba", "bb", "bc" );
				
		// register an object to play with
		theObject = defaultFederate.quickRegister( "ObjectRoot.A.B" );
		secondFederate.fedamb.waitForDiscovery( this.theObject );
		thirdFederate.fedamb.waitForDiscovery( this.theObject );
		
		// tick a bit to get all the pub/sub inital messages out of the queue
		defaultFederate.quickTick();
		secondFederate.quickTick();
		thirdFederate.quickTick();
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
		
		thirdFederate.quickResign();
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
	/////////////////////////// Unconditional Divest Test Methods ////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void unconditionalAttributeOwnershipDivestiture( int theObject,
	//                                                         AttributeHandleSet theAttributes )
	//     throws ObjectNotKnown,
	//            AttributeNotDefined,
	//            AttributeNotOwned,
	//            FederateNotExecutionMember,
	//            SaveInProgress,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

	//////////////////////////////////////////////////////
	// TEST: (valid) testUnconditionalAttributeDivest() //
	//////////////////////////////////////////////////////
	@Test
	public void testUnconditionalAttributeDivest()
	{
		// release the attributes
		try
		{
			AttributeHandleSet atts = defaultFederate.createAHS( aaHandle, abHandle );
			defaultFederate.rtiamb.unconditionalAttributeOwnershipDivestiture( theObject, atts );
		}
		catch( Exception e )
		{
			unexpectedException( "Unconditionally divesting attributes", e );
		}
		
		// wait for the ownership assumption request in the second federate
		secondFederate.fedamb.waitForOwnershipOffering( theObject, aaHandle, abHandle );
		
		// check the ownership
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,aaHandle),OWNER_UNOWNED );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,abHandle),OWNER_UNOWNED );
		Assert.assertEquals( defaultFederate.quickQueryOwnership(theObject,acHandle),
		                     defaultFederate.federateHandle );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,aaHandle),OWNER_UNOWNED );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,abHandle),OWNER_UNOWNED );
		Assert.assertEquals( secondFederate.quickQueryOwnership(theObject,acHandle),
		                     defaultFederate.federateHandle );
		
		// take up ownership of the attributes in the second federate using the "if available"
		secondFederate.quickAcquireIfAvailableRequest( theObject, aaHandle, abHandle );
		secondFederate.fedamb.waitForOwnershipAcquisition(theObject, aaHandle, abHandle );
	}
	
	/////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testUnconditionalAttributeDivestOfPrivilegeToDelete() //
	/////////////////////////////////////////////////////////////////////////
	@Test
	public void testUnconditionalAttributeDivestOfPrivilegeToDelete()
	{
		// unconditionally divest privilegeToDelete
		int privilegeToDelete = secondFederate.quickACHandle( "ObjectRoot", "privilegeToDelete" );
		defaultFederate.quickUnconditionalDivest( theObject, privilegeToDelete );
		
		// check ownership in both federates
		defaultFederate.quickAssertOwnership( OWNER_UNOWNED, theObject, privilegeToDelete );
		quickSleep();
		secondFederate.quickTick();
		secondFederate.quickAssertOwnership( OWNER_UNOWNED, theObject, privilegeToDelete );
		
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

		// try and pick up ownership in the second federate
		secondFederate.quickAcquireIfAvailable( theObject, privilegeToDelete );
		
		secondFederate.quickDelete( theObject );
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testUnconditionalAttributeDivestCausesProperAssumptionCallbacks() //
	/////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testUnconditionalAttributeDivestCausesProperAssumptionCallbacks()
	{
		defaultFederate.quickUnconditionalDivest( theObject, aaHandle, abHandle );
		secondFederate.fedamb.waitForOwnershipOffering( theObject, aaHandle, abHandle );
		defaultFederate.fedamb.waitForOwnershipOfferingTimeout( theObject, aaHandle, abHandle );
	}

	///////////////////////////////////////////////////////////////
	// TEST: testUnconditionalAttributeDivestWithUnknownObject() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testUnconditionalAttributeDivestWithUnknownObject()
	{
		int other = secondFederate.quickRegister( "ObjectRoot.A.B" );

		try
		{
			AttributeHandleSet attributes = defaultFederate.createAHS(aaHandle, abHandle, acHandle);
			defaultFederate.rtiamb.unconditionalAttributeOwnershipDivestiture( other, attributes );
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
	
	///////////////////////////////////////////////////////////////
	// TEST: testUnconditionalAttributeDivestWithInvalidObject() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testUnconditionalAttributeDivestWithInvalidObject()
	{
		try
		{
			AttributeHandleSet attributes = defaultFederate.createAHS( aaHandle );
			defaultFederate.rtiamb.unconditionalAttributeOwnershipDivestiture( -1, attributes );
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
	// TEST: testUnconditionalAttributeDivestWithWrongAttribute() //
	////////////////////////////////////////////////////////////////	
	@Test
	public void testUnconditionalAttributeDivestWithWrongAttribute()
	{
		// create an instance of ObjectRoot.A and then try and release attribute "ba"
		defaultFederate.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		int other = defaultFederate.quickRegister( "ObjectRoot.A" );
		
		try
		{
			AttributeHandleSet attributes = defaultFederate.createAHS( baHandle );
			defaultFederate.rtiamb.unconditionalAttributeOwnershipDivestiture( other, attributes );
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

	//////////////////////////////////////////////////////////////////
	// TEST: testUnconditionalAttributeDivestWithInvalidAttribute() //
	//////////////////////////////////////////////////////////////////	
	@Test
	public void testUnconditionalAttributeDivestWithInvalidAttribute()
	{
		try
		{
			//get hold of an invalid handle value in the attributeSet of the object
			AttributeHandleSet atts = defaultFederate.createAHS( -1 );
			defaultFederate.rtiamb.unconditionalAttributeOwnershipDivestiture( theObject, atts );
			expectedException( AttributeNotDefined.class );			
		}
		catch( AttributeNotDefined and )
		{
			// success!!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeNotDefined.class );
		}
	}

	///////////////////////////////////////////////////////////////////
	// TEST: testUnconditionalAttributeDivestWithUnownedAttribute() //
	///////////////////////////////////////////////////////////////////
	@Test
	public void testUnconditionalAttributeDivestWithUnownedAttribute()
	{
		try
		{
			AttributeHandleSet atts = defaultFederate.createAHS( baHandle );
			defaultFederate.rtiamb.unconditionalAttributeOwnershipDivestiture( theObject, atts );
			expectedException( AttributeNotOwned.class );
		}
		catch( AttributeNotOwned ano )
		{
			// success!!		
		}
		catch( Exception e )
		{		
			wrongException( e, AttributeNotOwned.class );			
		}	
	}

	///////////////////////////////////////////////////////////
	// TEST: testUnconditionalAttributeDivestWhenNotJoined() //
	///////////////////////////////////////////////////////////
	@Test
	public void testUnconditionalAttributeDivestWhenNotJoined()
	{
		defaultFederate.quickResign();
		
		try
		{
			AttributeHandleSet atts = defaultFederate.createAHS( aaHandle );
			defaultFederate.rtiamb.unconditionalAttributeOwnershipDivestiture( theObject, atts );
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

	////////////////////////////////////////////////////////////////
	// TEST: testUnconditionalAttributeDivestWhenSaveInProgress() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testUnconditionalAttributeDivestWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		
		try
		{
			AttributeHandleSet atts = defaultFederate.createAHS( aaHandle );
			defaultFederate.rtiamb.unconditionalAttributeOwnershipDivestiture( theObject, atts );
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

	///////////////////////////////////////////////////////////////////
	// TEST: testUnconditionalAttributeDivestWhenRestoreInProgress() //
	///////////////////////////////////////////////////////////////////
	@Test
	public void testUnconditionalAttributeDivestWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		
		try
		{
			AttributeHandleSet atts = defaultFederate.createAHS( aaHandle );
			defaultFederate.rtiamb.unconditionalAttributeOwnershipDivestiture( theObject, atts );
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
	
	//////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Negotiated Divest Test Methods ///////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	// public void negotiatedAttributeOwnershipDivestiture( int theObject,
	// 														AttributeHandleSet theAttributes, 
	//														byte[] userSuppliedTag )
	//		 throws ObjectNotKnown,					
	//				AttributeNotDefined, 
	//				AttributeNotOwned,
	//				AttributeAlreadyBeingDivested, 
	//				FederateNotExecutionMember, 
	//				SaveInProgress,
	//				RestoreInProgress, 
	//				RTIinternalError, 
	//				ConcurrentAccessAttempted;
	
	///////////////////////////////////////////////////
	// TEST: (valid) testNegotiatedAttributeDivest() //
	///////////////////////////////////////////////////
	@Test
	public void testNegotiatedAttributeDivest()
	{
		// throw the request out there
		try
		{
			AttributeHandleSet attributes = defaultFederate.createAHS(aaHandle, abHandle, acHandle);
			defaultFederate.rtiamb.negotiatedAttributeOwnershipDivestiture( theObject,
			                                                                attributes,
			                                                                new byte[0] );
		}
		catch( Exception e )
		{
			unexpectedException( "Requesting negotiated attribute divest", e );
		}
		
		// wait for the request information to flow through to the second federate
		secondFederate.fedamb.waitForOwnershipOffering( theObject, aaHandle, abHandle, acHandle );
		// take up the offer in the second federate
		secondFederate.quickAcquireRequest( theObject, aaHandle, abHandle, acHandle );
		
		// let the default federate get the request and process it internally, note that
		// this SHOULD NOT result in a request on the default federate's fedamb to release
		// the attributes. The LRC should take care of it because of the outstanding divest request
		defaultFederate.quickTick( 0.1, 1.0 );
		defaultFederate.fedamb.waitForOwnershipReleaseRequestTimeout( theObject,
		                                                              aaHandle,
		                                                              abHandle,
		                                                              acHandle );
		
		// now that the default federate's LRC has had time to process things, it should
		// have automatically granted the release to the second federate
		secondFederate.fedamb.waitForOwnershipAcquisition( theObject, aaHandle, abHandle, acHandle );
		
		// and the default federate should get the divest notification
		defaultFederate.fedamb.waitForOwnershipDivest( theObject, aaHandle, abHandle, acHandle );
	}
	
	////////////////////////////////////////////////////////////
	// TEST: testNegotiatedAttributeDivestWithUnknownObject() //
	////////////////////////////////////////////////////////////
	@Test
	public void testNegotiatedAttributeDivestWithUnknownObject()
	{
		// create a valid object, but one that isn't known to the second federate
		int other = defaultFederate.quickRegister( "ObjectRoot.A.B" );

		// try and divest attributes with this handle that is valid in the federation, but
		// not known by the second federate
		try
		{
			AttributeHandleSet handleSet = secondFederate.createAHS( aaHandle );
			secondFederate.rtiamb.negotiatedAttributeOwnershipDivestiture( other,
			                                                               handleSet,
			                                                               new byte[0] );
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
	
	////////////////////////////////////////////////////////////
	// TEST: testNegotiatedAttributeDivestWithInvalidObject() //
	////////////////////////////////////////////////////////////	
	@Test
	public void testNegotiatedAttributeDivestWithInvalidObject()
	{
		try
		{
			AttributeHandleSet attributes = secondFederate.createAHS( aaHandle );
			secondFederate.rtiamb.negotiatedAttributeOwnershipDivestiture( -1,
			                                                               attributes,
			                                                               new byte[0] );
			expectedException( ObjectNotKnown.class );
		}
		catch( ObjectNotKnown onk )
		{
			//success
		}
		catch( Exception e )
		{
			wrongException( e, ObjectNotKnown.class );
		}	
	}
	
	/////////////////////////////////////////////////////////////
	// TEST: testNegotiatedAttributeDivestWithWrongAttribute() //
	/////////////////////////////////////////////////////////////
	@Test
	public void testNegotiatedAttributeDivestWithWrongAttribute()
	{
		// register an instance of ObjectRoot.A and then try and divest attribute "ba"
		defaultFederate.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		int other = defaultFederate.quickRegister( "ObjectRoot.A" );
		
		try
		{
			AttributeHandleSet attributes = defaultFederate.createAHS( baHandle );
			defaultFederate.rtiamb.negotiatedAttributeOwnershipDivestiture( other,
			                                                                attributes,
			                                                                new byte[0] );
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
	// TEST: testNegotiatedAttributeDivestWithInvalidAttribute() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testNegotiatedAttributeDivestWithInvalidAttribute()
	{
		try
		{
        	// include invalid attribute handle in the handle set
        	AttributeHandleSet attributes = defaultFederate.createAHS( aaHandle, -1 );
        	defaultFederate.rtiamb.negotiatedAttributeOwnershipDivestiture( theObject,
        	                                                                attributes,
        	                                                                new byte[0] );
        	
        	expectedException( AttributeNotDefined.class );
		}
		catch( AttributeNotDefined and )
		{
			// success!!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeNotDefined.class );
		}		
	}
	
	///////////////////////////////////////////////////////////////
	// TEST: testNegotiatedAttributeDivestWithUnownedAttribute() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testNegotiatedAttributeDivestWithUnownedAttribute()
	{
		try
		{
			AttributeHandleSet attributes = defaultFederate.createAHS( baHandle );
			defaultFederate.rtiamb.negotiatedAttributeOwnershipDivestiture( theObject,
			                                                                attributes,
			                                                                new byte[0] );			
			expectedException( AttributeNotOwned.class );			
		}
		catch( AttributeNotOwned and )
		{
			// success!!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeNotOwned.class );			
		}		
	}
	
	/////////////////////////////////////////////////////////////////////////////
	// TEST: testNegotiatedAttributeDivestWhileAttributeAlreadyBeingDivested() //
	/////////////////////////////////////////////////////////////////////////////
	@Test
	public void testNegotiatedAttributeDivestWhileAttributeAlreadyBeingDivested()
	{
		// kick the divest off
		defaultFederate.quickDivestRequest( theObject, aaHandle );
		secondFederate.fedamb.waitForOwnershipOffering( theObject, aaHandle );
		
		try
		{
			AttributeHandleSet attributes = defaultFederate.createAHS( aaHandle );
			defaultFederate.rtiamb.negotiatedAttributeOwnershipDivestiture( theObject,
			                                                                attributes,
			                                                                new byte[0] );
			expectedException( AttributeAlreadyBeingDivested.class );
		}
		catch( AttributeAlreadyBeingDivested aabd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeAlreadyBeingDivested.class );
		}
	}
	
	////////////////////////////////////////////////////////
	// TEST: testNegotiatedAttributeDivestWhenNotJoined() //
	////////////////////////////////////////////////////////
	@Test
	public void testNegotiatedAttributeDivestWhenNotJoined()
	{
		defaultFederate.quickResign();
		
		try
		{
			AttributeHandleSet attributes = defaultFederate.createAHS( aaHandle );
			defaultFederate.rtiamb.negotiatedAttributeOwnershipDivestiture( theObject,
			                                                                attributes,
			                                                                new byte[0] );
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

	/////////////////////////////////////////////////////////////
	// TEST: testNegotiatedAttributeDivestWhenSaveInProgress() //
	/////////////////////////////////////////////////////////////
	@Test
	public void testNegotiatedAttributeDivestWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			AttributeHandleSet attributes = defaultFederate.createAHS( aaHandle );
			defaultFederate.rtiamb.negotiatedAttributeOwnershipDivestiture( theObject,
			                                                                attributes,
			                                                                new byte[0] );
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

	////////////////////////////////////////////////////////////////
	// TEST: testNegotiatedAttributeDivestWhenRestoreInProgress() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testNegotiatedAttributeDivestWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "restore" );
		try
		{
			AttributeHandleSet attributes = defaultFederate.createAHS( aaHandle );
			defaultFederate.rtiamb.negotiatedAttributeOwnershipDivestiture( theObject,
			                                                                attributes,
			                                                                new byte[0] );
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
	/////////////////////////////// Cancel Divest Test Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void cancelNegotiatedAttributeOwnershipDivestiture( int theObject,
	//                                                            AttributeHandleSet theAttributes )
	//     throws ObjectNotKnown,
	//            AttributeNotDefined,
	//            AttributeNotOwned,
	//            AttributeDivestitureWasNotRequested,
	//            FederateNotExecutionMember,
	//            SaveInProgress,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

	/////////////////////////////////////////////////////////
	// TEST: (valid) testCancelNegotiatedAttributeDivest() //
	/////////////////////////////////////////////////////////
	@Test(groups="cancelDivest")
	public void testCancelNegotiatedAttributeDivest()
	{
		// put a valid divest request into the mix
		defaultFederate.quickDivestRequest( theObject, aaHandle, abHandle, acHandle );
		
		// wait for it to be received in the second federate
		secondFederate.fedamb.waitForOwnershipOffering( theObject, aaHandle, abHandle, acHandle );		
		secondFederate.quickAcquireRequest( theObject, aaHandle, abHandle, acHandle );

		// cancel the divest request
		try
		{
			AttributeHandleSet atts = defaultFederate.createAHS( aaHandle, abHandle, acHandle );
			defaultFederate.rtiamb.cancelNegotiatedAttributeOwnershipDivestiture( theObject, atts );
		}
		catch( Exception e )
		{
			unexpectedException( "Cancelling negotiated attribute divest", e );
		}
		
		// now when we try to take up the offering in the second federate, it should not
		// fail, but we should not be granted the attributes, rather, this should divert
		// to a normal pull-style ownership request
		secondFederate.quickTick(); // get the cancel notice through the LRC first
		secondFederate.quickAcquireRequest( theObject, aaHandle, abHandle, acHandle );
		defaultFederate.fedamb.waitForOwnershipReleaseRequest( theObject,
		                                                       aaHandle,
		                                                       abHandle,
		                                                       acHandle );
	}

	//////////////////////////////////////////////////////////////////
	// TEST: testCancelNegotiatedAttributeDivestWithUnknownObject() //
	//////////////////////////////////////////////////////////////////
	@Test(groups="cancelDivest")
	public void testCancelNegotiatedAttributeDivestWithUnknownObject()
	{
		// register an object in the second federate, but don't discover it in the default
		// use this handle (that is valid in the federation, but not for the default federate)
		int other = secondFederate.quickRegister( "ObjectRoot.A.B" );
		
		try
		{
			AttributeHandleSet handles = defaultFederate.createAHS( aaHandle );
			defaultFederate.rtiamb.cancelNegotiatedAttributeOwnershipDivestiture( other, handles );
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

	//////////////////////////////////////////////////////////////////
	// TEST: testCancelNegotiatedAttributeDivestWithInvalidObject() //
	//////////////////////////////////////////////////////////////////
	@Test(groups="cancelDivest")
	public void testCancelNegotiatedAttributeDivestWithInvalidObject()
	{
		try
		{
			AttributeHandleSet handles = defaultFederate.createAHS( aaHandle );
			defaultFederate.rtiamb.cancelNegotiatedAttributeOwnershipDivestiture( -1, handles );
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
	// TEST: testCancelNegotiatedAttributeDivestWithWrongAttribute() //
	///////////////////////////////////////////////////////////////////
	@Test(groups="cancelDivest")
	public void testCancelNegotiatedAttributeDivestWithWrongAttribute()
	{
		// regiter an object of type ObjectRoot.A and then try and give up attribute "ba"
		defaultFederate.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		int object = defaultFederate.quickRegister( "ObjectRoot.A" );
		// register a valid divest request, so there is something in there for the object
		defaultFederate.quickDivestRequest( object, aaHandle, abHandle, acHandle );
		
		try
		{
			AttributeHandleSet handles = defaultFederate.createAHS( baHandle );
			defaultFederate.rtiamb.cancelNegotiatedAttributeOwnershipDivestiture( object, handles );
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
	// TEST: testCancelNegotiatedAttributeDivestWithInvalidAttribute() //
	/////////////////////////////////////////////////////////////////////
	@Test(groups="cancelDivest")
	public void testCancelNegotiatedAttributeDivestWithInvalidAttribute()
	{
		// create a valid divest request so that there is one in there for the object
		defaultFederate.quickDivestRequest( theObject, aaHandle );
		
		try
		{
			AttributeHandleSet atts = defaultFederate.createAHS( aaHandle, -1 );
			defaultFederate.rtiamb.cancelNegotiatedAttributeOwnershipDivestiture( theObject, atts );
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
	// TEST: testCancelNegotiatedAttributeDivestWithUnownedAttribute() //
	/////////////////////////////////////////////////////////////////////
	@Test(groups="cancelDivest")
	public void testCancelNegotiatedAttributeDivestWithUnownedAttribute()
	{
		// create a valid divest request so that one is registered for the object
		defaultFederate.quickDivestRequest( theObject, aaHandle, abHandle, acHandle );
		
		try
		{
			AttributeHandleSet atts = defaultFederate.createAHS( aaHandle, baHandle );
			defaultFederate.rtiamb.cancelNegotiatedAttributeOwnershipDivestiture( theObject, atts );
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
	
	////////////////////////////////////////////////////////////////////
	// TEST: testCancelNegotiatedAttributeDivestWithoutActiveDivest() //
	////////////////////////////////////////////////////////////////////
	@Test(groups="cancelDivest")
	public void testCancelNegotiatedAttributeDivestWithoutActiveDivest()
	{
		try
		{
			AttributeHandleSet atts = defaultFederate.createAHS( aaHandle );
			defaultFederate.rtiamb.cancelNegotiatedAttributeOwnershipDivestiture( theObject, atts );
			expectedException( AttributeDivestitureWasNotRequested.class );
		}
		catch( AttributeDivestitureWasNotRequested adwnr )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeDivestitureWasNotRequested.class );
		}
	}
	
	//////////////////////////////////////////////////////////////
	// TEST: testCancelNegotiatedAttributeDivestWhenNotJoined() //
	//////////////////////////////////////////////////////////////
	@Test(groups="cancelDivest")
	public void testCancelNegotiatedAttributeDivestWhenNotJoined()
	{
		defaultFederate.quickResign();
		
		try
		{
			AttributeHandleSet atts = defaultFederate.createAHS( aaHandle );
			defaultFederate.rtiamb.cancelNegotiatedAttributeOwnershipDivestiture( theObject, atts );
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
	// TEST: testCancelNegotiatedAttributeDivestWhenSaveInProgress() //
	///////////////////////////////////////////////////////////////////
	@Test(groups="cancelDivest")
	public void testCancelNegotiatedAttributeDivestWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			AttributeHandleSet atts = defaultFederate.createAHS( aaHandle );
			defaultFederate.rtiamb.cancelNegotiatedAttributeOwnershipDivestiture( theObject, atts );
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
	// TEST: testCancelNegotiatedAttributeDivestWhenRestoreInProgress() //
	//////////////////////////////////////////////////////////////////////
	@Test(groups="cancelDivest")
	public void testCancelNegotiatedAttributeDivestWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			AttributeHandleSet atts = defaultFederate.createAHS( aaHandle );
			defaultFederate.rtiamb.cancelNegotiatedAttributeOwnershipDivestiture( theObject, atts );
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
	////////////////////// Misc Divest/Assumption Callback Test Methods //////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////
	// TEST: (valid) testOwnershipReleasedOnUnpublish() //
	//////////////////////////////////////////////////////
	/**
	 * This method will take a federate that has registered and object and have it unpublish the
	 * class that it registered the object under. This should cause all attributes it owns to be
	 * unconditionally released and offered to those who can take them up.
	 */
	@Test
	public void testOwnershipReleasedOnUnpublish()
	{
		// unpublish A.B and wait for notification in the second federate
		defaultFederate.quickUnpublishOC( "ObjectRoot.A.B" );
		
		// wait for an ownership offering
		int privilegeToDelete = secondFederate.quickACHandle( "ObjectRoot", "privilegeToDelete" );
		secondFederate.fedamb.waitForOwnershipOffering( theObject,
		                                                aaHandle,
		                                                abHandle,
		                                                acHandle,
		                                                privilegeToDelete );

		// make sure the ownership is right
		defaultFederate.quickAssertOwnership( OWNER_UNOWNED, theObject, aaHandle, abHandle, acHandle );
		secondFederate.quickAssertOwnership( OWNER_UNOWNED, theObject, aaHandle, abHandle, acHandle );
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOwnershipReleasedOnResignWithActionReleaseAttributes() //
	//////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will take a federate that has registered and object and have it resign using the
	 * action RELEASE_ATTRIUBTES. This should cause all attributes to be unconditionally released
	 * and offered to those who can take them up.
	 */
	@Test
	public void testOwnershipReleasedOnResignWithActionReleaseAttributes()
	{
		defaultFederate.quickResign( ResignAction.RELEASE_ATTRIBUTES );
		
		// wait for an ownership offering
		int privilegeToDelete = secondFederate.quickACHandle( "ObjectRoot", "privilegeToDelete" );
		secondFederate.fedamb.waitForOwnershipOffering( theObject,
		                                                aaHandle,
		                                                abHandle,
		                                                acHandle,
		                                                privilegeToDelete );

		// make sure the ownership is right
		secondFederate.quickTick( 0.1, 1.0 );
		secondFederate.quickAssertOwnership( OWNER_UNOWNED, theObject, aaHandle, abHandle, acHandle );
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testOwnershipReleasedOnResignWithActionDeleteObjectsAndReleaseAttributes() //
	//////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The default federate will register and object and obtain some of the attributes of another
	 * object (but not its privilege to delete). The federate will then resign, which should cause
	 * a delete for the object it registered (and holds the privilegeToDelete in) *AND* cause it
	 * to release the attributes it owns in objects it doesn't have the privilege to delete.
	 */
	@Test
	public void testOwnershipReleasedOnResignWithActionDeleteObjectsAndReleaseAttributes()
	{
		int privilegeToDelete = secondFederate.quickACHandle( "ObjectRoot", "privilegeToDelete" );
		defaultFederate.quickSubscribe( "ObjectRoot.A.B", "aa", "ab", "ac", "ba", "bb", "bc" );

		// register a second object and have the default federate get ownership of some of the atts
		int secondObject = secondFederate.quickRegister( "ObjectRoot.A.B" );
		defaultFederate.fedamb.waitForDiscovery( secondObject );
		defaultFederate.quickExchangeOwnership( secondFederate, secondObject, aaHandle, abHandle );
		
		// make sure we have the correct ownership at this point
		defaultFederate.quickAssertOwnership( defaultFederate.federateHandle, theObject,
		                                      aaHandle, abHandle, acHandle, privilegeToDelete );
		defaultFederate.quickAssertOwnership( defaultFederate.federateHandle, secondObject,
		                                      aaHandle, abHandle );
		defaultFederate.quickAssertOwnership( secondFederate.federateHandle, secondObject,
		                                      acHandle, privilegeToDelete );
		
		secondFederate.quickTick( 0.1, 1.0 );
		secondFederate.quickAssertOwnership( defaultFederate.federateHandle, theObject,
		                                     aaHandle, abHandle, acHandle, privilegeToDelete );
		secondFederate.quickAssertOwnership( defaultFederate.federateHandle, secondObject,
		                                     aaHandle, abHandle );
		secondFederate.quickAssertOwnership( secondFederate.federateHandle, secondObject,
		                                     acHandle, privilegeToDelete );
		
		// resign the default federate
		defaultFederate.quickResign( ResignAction.DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES );
		
		// wait for ownership offering
		secondFederate.fedamb.waitForOwnershipOffering( secondObject, aaHandle, abHandle );
		secondFederate.fedamb.waitForRORemoval( theObject );
		
		// make sure the ownership is right
		secondFederate.quickTick( 0.1, 1.0 );
		secondFederate.quickAssertOwnership( OWNER_UNOWNED, secondObject, aaHandle, abHandle );
		secondFederate.quickAssertOwnership( secondFederate.federateHandle, secondObject,
		                                     acHandle, privilegeToDelete );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
