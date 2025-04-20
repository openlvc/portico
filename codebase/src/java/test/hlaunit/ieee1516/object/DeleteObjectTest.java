/*
 *   Copyright 2007 The Portico Project
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
package hlaunit.ieee1516.object;

import static hlaunit.ieee1516.common.TypeFactory.*;

import hla.rti1516.DeletePrivilegeNotHeld;
import hla.rti1516.InvalidLogicalTime;
import hla.rti1516.LogicalTime;
import hla.rti1516.ObjectInstanceNotKnown;
import hlaunit.ieee1516.common.Abstract1516Test;
import hlaunit.ieee1516.common.TestFederate;
import hlaunit.ieee1516.common.TypeFactory;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"DeleteObjectTest", "deleteObject", "objectManagement"})
public class DeleteObjectTest extends Abstract1516Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate secondFederate;
	private TestFederate thirdFederate;

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

		secondFederate = new TestFederate( "secondFederate", this );
		thirdFederate = new TestFederate( "thirdFederate", this );
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
	// public void deleteObjectInstance( ObjectInstanceHandle objectHandle, byte[] userSuppliedTag )
	//        throws DeletePrivilegeNotHeld,
	//               ObjectInstanceNotKnown,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;


	////////////////////////////////////////
	// TEST: (valid) testRODeleteObject() //
	////////////////////////////////////////
	@Test
	public void testRODeleteObject()
	{
		// try and delete an existing object
		try
		{
			defaultFederate.rtiamb.deleteObjectInstance( getObjectHandle(oHandle), tag );
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
			LogicalTime time = TypeFactory.createTime( 10.0 );
			defaultFederate.rtiamb.deleteObjectInstance( getObjectHandle(oHandle), tag, time );
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
			defaultFederate.rtiamb.deleteObjectInstance( getObjectHandle(111111), tag );
			expectedException( ObjectInstanceNotKnown.class );
		}
		catch( ObjectInstanceNotKnown onk )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectInstanceNotKnown.class );
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
			secondFederate.rtiamb.deleteObjectInstance( getObjectHandle(oHandle), tag );
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
		TestFederate fourthFederate = new TestFederate( "fourthFederate", this );
		fourthFederate.quickJoin();
		
		try
		{
			fourthFederate.rtiamb.deleteObjectInstance( getObjectHandle(oHandle), tag );
			expectedException( ObjectInstanceNotKnown.class );
		}
		catch( ObjectInstanceNotKnown oink )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectInstanceNotKnown.class );
		}
		finally
		{
			// clean up after ourselves
			fourthFederate.quickResign();
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// TSO Delete Test Methods ////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public MessageRetractionReturn deleteObjectInstance( ObjectInstanceHandle objectHandle,
	//                                                      byte[] userSuppliedTag,
	//                                                      LogicalTime theTime )
	//        throws DeletePrivilegeNotHeld,
	//               ObjectInstanceNotKnown,
	//               InvalidLogicalTime,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

	/////////////////////////////////////////
	// TEST: (valid) testTSODeleteObject() //
	/////////////////////////////////////////
	@Test
	public void testTSODeleteObject()
	{
		// try and delete an existing object
		try
		{
			LogicalTime time = TypeFactory.createTime( 10.0 );
			defaultFederate.rtiamb.deleteObjectInstance( getObjectHandle(oHandle), tag, time );
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
			defaultFederate.rtiamb.deleteObjectInstance( getObjectHandle(oHandle), tag, null );
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
			LogicalTime time = TypeFactory.createTime( 5.0 );
			defaultFederate.rtiamb.deleteObjectInstance( getObjectHandle(111111), tag, time );
			expectedException( ObjectInstanceNotKnown.class );
		}
		catch( ObjectInstanceNotKnown oink )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectInstanceNotKnown.class );
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
			LogicalTime time = TypeFactory.createTime( 5.0 );
			secondFederate.rtiamb.deleteObjectInstance( getObjectHandle(oHandle), tag, time );
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
		TestFederate fourthFederate = new TestFederate( "fourthFederate", this );
		fourthFederate.quickJoin();
		fourthFederate.quickEnableRegulating( 5.0 );
		
		try
		{
			LogicalTime time = TypeFactory.createTime( 5.0 );
			fourthFederate.rtiamb.deleteObjectInstance( getObjectHandle(oHandle), tag, time );
			expectedException( ObjectInstanceNotKnown.class );
		}
		catch( ObjectInstanceNotKnown onk )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectInstanceNotKnown.class );
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
			LogicalTime time = TypeFactory.createTime( -5.0 );
			defaultFederate.rtiamb.deleteObjectInstance( getObjectHandle(oHandle), tag, time );
			expectedException( InvalidLogicalTime.class );
		}
		catch( InvalidLogicalTime ilt )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidLogicalTime.class );
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
			LogicalTime time = TypeFactory.createTime( 10.0 );
			defaultFederate.rtiamb.deleteObjectInstance( getObjectHandle(oHandle), tag, time );
			expectedException( InvalidLogicalTime.class );
		}
		catch( InvalidLogicalTime ilt )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidLogicalTime.class );
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
			LogicalTime time = TypeFactory.createTime( 1.0 );
			defaultFederate.rtiamb.deleteObjectInstance( getObjectHandle(oHandle), tag, time );
			expectedException( InvalidLogicalTime.class );
		}
		catch( InvalidLogicalTime ilt )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidLogicalTime.class );
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
