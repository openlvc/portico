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
package hlaunit.hla13.object;

import hla.rti.DeletePrivilegeNotHeld;
import hla.rti.FederateNotExecutionMember;
import hla.rti.InvalidFederationTime;
import hla.rti.LogicalTime;
import hla.rti.ObjectNotKnown;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"DeleteObjectTest", "deleteObject", "objectManagement"})
public class DeleteObjectTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate secondFederate;
	private Test13Federate thirdFederate;

	private int bHandle; // handle for class ObjectRoot.A.B

	private int oHandle;
	private byte[] tag;
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

		secondFederate = new Test13Federate( "secondFederate", this );
		thirdFederate = new Test13Federate( "thirdFederate", this );
		this.tag = "letag".getBytes();
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
		thirdFederate.quickJoin();
		
		// cache the handles
		bHandle  = defaultFederate.quickOCHandle( "ObjectRoot.A.B" );

		// do publication and subscription
		defaultFederate.quickPublish( "ObjectRoot.A.B", "aa", "ab", "ac", "ba", "bb", "bc" );
		secondFederate.quickSubscribe( "ObjectRoot.A.B", "aa", "ab", "ac", "ba", "bb", "bc" );
		thirdFederate.quickSubscribe( "ObjectRoot.A", "aa", "ab", "ac" );
		
		// do time setup
		defaultFederate.quickEnableRegulating( 5.0 );
		secondFederate.quickEnableAsyncDelivery();
		secondFederate.quickEnableConstrained();
		// third federate is neither regulating nor constrained
		
		// pre-register and discover an instance
		oHandle = defaultFederate.quickRegister( bHandle );
		secondFederate.fedamb.waitForDiscovery( oHandle );
		thirdFederate.fedamb.waitForDiscovery( oHandle );
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
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
	//////////////////////////////////// RO Test Methods /////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void deleteObjectInstance( int ObjectHandle, byte[] userSuppliedTag )
	//        throws ObjectNotKnown,
	//               DeletePrivilegeNotHeld,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	////////////////////////////////////////
	// TEST: (valid) testRODeleteObject() //
	////////////////////////////////////////
	@Test
	public void testRODeleteObject()
	{
		// try and delete an existing object
		try
		{
			defaultFederate.rtiamb.deleteObjectInstance( oHandle, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while deleting object RO" );
		}
		
		// wait for the object to be removed
		secondFederate.fedamb.waitForRORemoval( oHandle );
		thirdFederate.fedamb.waitForRORemoval( oHandle );
	}
	
	///////////////////////////////////////////////
	// TEST: (valid) testRODeleteWithTimestamp() //
	///////////////////////////////////////////////
	@Test
	public void testRODeleteWithTimestamp()
	{
		// disable regulating so that the method SHOULD be sent RO even through
		// we supply it with a timestamp
		defaultFederate.quickDisableRegulating();
		
		// try and delete an existing object
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.deleteObjectInstance( oHandle, tag, time );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while deleting object RO" );
		}
		
		// wait for the object to be removed
		secondFederate.fedamb.waitForRORemoval( oHandle );
		thirdFederate.fedamb.waitForRORemoval( oHandle );
	}

	/////////////////////////////////////////////////
	// TEST: testRODeleteObjectWithInvalidHandle() //
	/////////////////////////////////////////////////
	@Test
	public void testRODeleteObjectWithInvalidHandle()
	{
		// try and delete and object that doesn't exist
		try
		{
			defaultFederate.rtiamb.deleteObjectInstance( 111111, tag );
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
	
	/////////////////////////////////////////////
	// TEST: testRODeleteObjectThatIsUnowned() //
	/////////////////////////////////////////////
	@Test
	public void testRODeleteObjectThatIsUnowned()
	{
		// try and delete an object that doesn't belong to the federate
		try
		{
			// delete it with the second federate, where the defaultFederate is the owner
			secondFederate.rtiamb.deleteObjectInstance( oHandle, tag );
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
	}
	
	////////////////////////////////////////////
	// TEST: testRODeleteObjectUndiscovered() //
	////////////////////////////////////////////
	@Test
	public void testRODeleteObjectUndiscovered()
	{
		// try and delete and object that hasn't been discovered
		// this will require a new federate that doesn't subscribe to ObjectRoot.A or below
		Test13Federate fourthFederate = new Test13Federate( "fourthFederate", this );
		fourthFederate.quickJoin();
		
		try
		{
			fourthFederate.rtiamb.deleteObjectInstance( oHandle, tag );
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
		finally
		{
			// clean up after ourselves
			fourthFederate.quickResign();
		}
	}

	/////////////////////////////////////////////
	// TEST: testRODeleteObjectWhenNotJoined() //
	/////////////////////////////////////////////
	@Test
	public void testRODeleteObjectWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			defaultFederate.rtiamb.deleteObjectInstance( oHandle, tag );
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

	//////////////////////////////////////////////////
	// TEST: testRODeleteObjectWhenSaveInProgress() //
	//////////////////////////////////////////////////
	@Test
	public void testRODeleteObjectWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.deleteObjectInstance( oHandle, tag );
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
	// TEST: testRODeleteObjectWhenRestoreInProgress() //
	/////////////////////////////////////////////////////
	@Test
	public void testRODeleteObjectWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.deleteObjectInstance( oHandle, tag );
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
	///////////////////////////////// TSO Delete Test Methods ////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public EventRetractionHandle deleteObjectInstance( int ObjectHandle,
	//                                                    byte[] userSuppliedTag,
	//                                                    LogicalTime theTime )
	//        throws ObjectNotKnown,
	//               DeletePrivilegeNotHeld,
	//               InvalidFederationTime,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	/////////////////////////////////////////
	// TEST: (valid) testTSODeleteObject() //
	/////////////////////////////////////////
	@Test
	public void testTSODeleteObject()
	{
		// try and delete an existing object
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.deleteObjectInstance( oHandle, tag, time );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while deleting object RO" );
		}
		
		// wait for the object to be removed in the non-constrained federate
		thirdFederate.fedamb.waitForRORemoval( oHandle );
		
		// wait for the removal in the constrained federate, we shouldn't get it yet
		secondFederate.fedamb.waitForTSORemovalTimeout( oHandle );
		
		// advance time in the relevant federates far enough to enable
		// delivery of the removal notification
		defaultFederate.quickAdvanceAndWait( 20.0 );
		secondFederate.quickAdvanceRequest( 20.0 );
		// make sure we receive the event
		secondFederate.fedamb.waitForTSORemoval( oHandle );
	}

	////////////////////////////////////////////////////
	// TEST: (valid) testTSODeleteWithNullTimestamp() //
	////////////////////////////////////////////////////
	/**
	 * Deleting an object with a null timestamp is valid, it just means that the delete will be
	 * sent RO, rather than TSO
	 */
	@Test
	public void testTSODeleteWithNullTimestamp()
	{
		try
		{
			defaultFederate.rtiamb.deleteObjectInstance( oHandle, tag, null );
		}
		catch( Exception e )
		{
			unexpectedException( "Deleting object with null timestamp", e );
		}
		
		// make sure the delete is received RO
		secondFederate.fedamb.waitForRORemoval( oHandle );
	}

	//////////////////////////////////////////////////
	// TEST: testTSODeleteObjectWithInvalidHandle() //
	//////////////////////////////////////////////////
	@Test
	public void testTSODeleteObjectWithInvalidHandle()
	{
		// try and delete and object that doesn't exist
		try
		{
			LogicalTime time = defaultFederate.createTime( 5.0 );
			defaultFederate.rtiamb.deleteObjectInstance( 111111, tag, time );
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

	//////////////////////////////////////////////
	// TEST: testTSODeleteObjectThatIsUnowned() //
	//////////////////////////////////////////////
	@Test
	public void testTSODeleteObjectThatIsUnowned()
	{
		// delete it with the second federate, where the defaultFederate is the owner
		// enable regulating so that the time isn't just disregarded as a matter of course
		secondFederate.quickEnableRegulating( 1.0 );

		try
		{
			LogicalTime time = defaultFederate.createTime( 5.0 );
			secondFederate.rtiamb.deleteObjectInstance( oHandle, tag, time );
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
	}

	/////////////////////////////////////////////
	// TEST: testTSODeleteObjectUndiscovered() //
	/////////////////////////////////////////////
	@Test
	public void testTSODeleteObjectUndiscovered()
	{
		// try and delete and object that hasn't been discovered
		// this will require a new federate that doesn't subscribe to ObjectRoot.A or below
		// it also needs to be regulating so that the time isn't disregarded as a matter of course
		Test13Federate fourthFederate = new Test13Federate( "fourthFederate", this );
		fourthFederate.quickJoin();
		fourthFederate.quickEnableRegulating( 5.0 );
		
		try
		{
			LogicalTime time = fourthFederate.createTime( 5.0 );
			fourthFederate.rtiamb.deleteObjectInstance( oHandle, tag, time );
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
		finally
		{
			// clean up after ourselves
			fourthFederate.quickResign();
		}
	}

	/////////////////////////////////////////////////////
	// TEST: testTSODeleteObjectWithInvalidTimestamp() //
	/////////////////////////////////////////////////////
	@Test
	public void testTSODeleteObjectWithInvalidTimestamp()
	{
		try
		{
			LogicalTime time = defaultFederate.createTime( -5.0 );
			defaultFederate.rtiamb.deleteObjectInstance( oHandle, tag, time );
			expectedException( InvalidFederationTime.class );
		}
		catch( InvalidFederationTime ift )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidFederationTime.class );
		}
	}
	
	///////////////////////////////////////
	// TEST: testTSODeleteObjectInPast() //
	///////////////////////////////////////
	@Test
	public void testTSODeleteObjectInPast()
	{
		// advance the federate a bit, so that we can delete them in the past
		defaultFederate.quickAdvanceAndWait( 20.0 );
		
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.deleteObjectInstance( oHandle, tag, time );
			expectedException( InvalidFederationTime.class );
		}
		catch( InvalidFederationTime ift )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidFederationTime.class );
		}
	}

	//////////////////////////////////////////
	// TEST: testTSODeleteObjectBelowLBTS() //
	//////////////////////////////////////////
	@Test
	public void testTSODeleteObjectBelowLBTS()
	{
		try
		{
			LogicalTime time = defaultFederate.createTime( 1.0 );
			defaultFederate.rtiamb.deleteObjectInstance( oHandle, tag, time );
			expectedException( InvalidFederationTime.class );
		}
		catch( InvalidFederationTime ift )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidFederationTime.class );
		}
	}

	/////////////////////////////////////////////
	// TEST: testRODeleteObjectWhenNotJoined() //
	/////////////////////////////////////////////
	@Test
	public void testTSODeleteObjectWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.deleteObjectInstance( oHandle, tag, time );
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

	//////////////////////////////////////////////////
	// TEST: testRODeleteObjectWhenSaveInProgress() //
	//////////////////////////////////////////////////
	@Test
	public void testTSODeleteObjectWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.deleteObjectInstance( oHandle, tag, time );
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
	// TEST: testRODeleteObjectWhenRestoreInProgress() //
	/////////////////////////////////////////////////////
	@Test
	public void testTSODeleteObjectWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.deleteObjectInstance( oHandle, tag, time );
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
	/////////////////////////// General Remove/Delete Test Methods ///////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	//
	// These tests are those that relate to the delete/remove process in general, not a specific
	// TSO/RO flavor of them. This is more to test that callbacks work properly, not just the
	// RTIambassador side of the equation
	//

	////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testDeletedCallbacksOnlyReachFederatesThatHaveDiscoveredObject() //
	////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This test makes sure that federates that have not discovered an object don't get
	 * remove callbacks when they're deleted
	 */
	@Test
	public void testDeletedCallbacksOnlyReachFederatesThatHaveDiscoveredObject()
	{
		// unsubscribe to any relevant class in the third federate so we no longer get discovers
		thirdFederate.quickUnsubscribe( "ObjectRoot.A" );
		
		// register a new instance in the default federate, it should get discovered in the
		// second federate but not the third
		int newObject = defaultFederate.quickRegister( "ObjectRoot.A.B" );
		secondFederate.fedamb.waitForDiscovery( newObject );
		thirdFederate.fedamb.waitForDiscoveryTimeout( newObject );
		
		// delete the object, make sure only the second federate gets the callback
		defaultFederate.quickDelete( newObject, null );
		secondFederate.fedamb.waitForRORemoval( newObject );
		thirdFederate.fedamb.waitForRORemovalTimeout( newObject );
	}
	

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
