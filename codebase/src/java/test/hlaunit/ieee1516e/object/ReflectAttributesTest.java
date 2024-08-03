/*
 *   Copyright 2012 The Portico Project
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

import static hlaunit.ieee1516e.common.TypeFactory.*;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.AttributeNotDefined;
import hla.rti1516e.exceptions.AttributeNotOwned;
import hla.rti1516e.exceptions.InvalidLogicalTime;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hla.rti1516e.exceptions.RTIinternalError;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;
import hlaunit.ieee1516e.common.TestObject;
import hlaunit.ieee1516e.common.TypeFactory;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"ReflectAttributesTest", "reflectAttributes", "objectManagement"})
public class ReflectAttributesTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate secondFederate;
	private TestFederate thirdFederate;

	private int aHandle, aaHandle, abHandle, acHandle;
	private int bHandle, baHandle, bbHandle, bcHandle;
	
	private AttributeHandleValueMap updateSet;
	private byte[] tag = "testing".getBytes();
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
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
		thirdFederate.quickJoin();
		
		// update the handle information //
		aHandle  = defaultFederate.quickOCHandle( "ObjectRoot.A" );
		aaHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "aa" );
		abHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ab" );
		acHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ac" );
		bHandle  = defaultFederate.quickOCHandle( "ObjectRoot.A.B" );
		baHandle = defaultFederate.quickACHandle( "ObjectRoot.A.B", "ba" );
		bbHandle = defaultFederate.quickACHandle( "ObjectRoot.A.B", "bb" );
		bcHandle = defaultFederate.quickACHandle( "ObjectRoot.A.B", "bc" );
		
		// create and populate the update set that is used in all reflections
		updateSet = TypeFactory.newAttributeMap();
		updateSet.put( getAttributeHandle(aaHandle), "aa".getBytes() );
		updateSet.put( getAttributeHandle(abHandle), "ab".getBytes() );
		updateSet.put( getAttributeHandle(baHandle), "ba".getBytes() );
		updateSet.put( getAttributeHandle(bbHandle), "bb".getBytes() );

		// do publish and subscribe //
		defaultFederate.quickPublish( "ObjectRoot.A.B", "aa", "ab", "ba", "bb" );
		secondFederate.quickSubscribe( "ObjectRoot.A.B", "aa", "ab", "ac", "ba", "bb", "bc" );
		thirdFederate.quickSubscribe( "ObjectRoot.A", "aa", "ab", "ac" );
		
		// enable constrained for the third federate
		thirdFederate.quickEnableAsyncDelivery();
		thirdFederate.quickEnableConstrained();
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
	///////////////////////////////////// Helper Methods /////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will check the values in the given {@link TestObject} and ensure that they
	 * are consistent with those that are expected given the subscription interests of the second
	 * federate.
	 */
	private void checkSecondFederateAttributes( TestObject instance )
	{
		Assert.assertEquals( bHandle, instance.getClassHandle(),
		                     "Second federate discovered wrong class type" );
		Assert.assertEquals( "aa".getBytes(), instance.getAttributeValue( aaHandle ),
		                     "Second federate has wrong value for attribute aa" );
		Assert.assertEquals( "ab".getBytes(), instance.getAttributeValue( abHandle ),
		                     "Second federate has wrong value for attribute ab" );
		Assert.assertEquals( "ba".getBytes(), instance.getAttributeValue( baHandle ),
		                     "Second federate has wrong value for attribute ba" );
		Assert.assertEquals( "bb".getBytes(), instance.getAttributeValue( bbHandle ),
		                     "Second federate has wrong value for attribute bb" );
		Assert.assertNull( instance.getAttributeValue(acHandle),
		                   "Second federate has wrong value for attribute ac" );
		Assert.assertNull( instance.getAttributeValue(bcHandle),
		                   "Second federate has wrong value for attribute bc" );
	}
	
	/**
	 * This method will check the values in the given {@link TestObject} and ensure that they
	 * are consistent with those that are expected given the subscription interests of the third
	 * federate.
	 */
	private void checkThirdFederateAttributes( TestObject instance )
	{
		Assert.assertEquals( aHandle, instance.getClassHandle(),
		                     "Third federate discovered wrong class type" );
		Assert.assertEquals( "aa".getBytes(), instance.getAttributeValue( aaHandle ),
		                     "Third federate has wrong value for attribute aa" );
		Assert.assertEquals( "ab".getBytes(), instance.getAttributeValue( abHandle ),
		                     "Third federate has wrong value for attribute ab" );
		Assert.assertNull( instance.getAttributeValue(acHandle),
		                   "Third federate has wrong value for attribute ac" );
		Assert.assertNull( instance.getAttributeValue(baHandle), 
		                   "Third federate has wrong value for attribute ba" );
		Assert.assertNull( instance.getAttributeValue(bbHandle),
		                   "Third federate has wrong value for attribute bb" );
		Assert.assertNull( instance.getAttributeValue(bcHandle),
		                   "Third federate has wrong value for attribute bc" );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Receive Order Test Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void updateAttributeValues( ObjectInstanceHandle theObject,
	//                                    AttributeHandleValueMap theAttributes,
	//                                    byte[] userSuppliedTag )
	//        throws ObjectInstanceNotKnown,
	//               AttributeNotDefined,
	//               AttributeNotOwned,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

	//////////////////////////////////
	// TEST: (valid) testROUpdate() //
	//////////////////////////////////
	@Test
	public void testROUpdate()
	{
		// do any necessary setup
		int oHandle = defaultFederate.quickRegister( "ObjectRoot.A.B" );
		secondFederate.fedamb.waitForDiscovery( oHandle );
		thirdFederate.fedamb.waitForDiscovery( oHandle );
		
		//////////////////////////////////////////////
		// attempt a valid update of all attributes //
		//////////////////////////////////////////////
		// we will not be providing updates for either
		// of the 'ac' or 'bc' attributes
		try
		{
			// do the actual update
			ObjectInstanceHandle handle = getObjectHandle( oHandle );
			defaultFederate.rtiamb.updateAttributeValues( handle, updateSet, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during valid RO attribute update: " + e.getMessage(), e );
		}
		
		//////////////////////////////////////////////////
		// validate that the information is distributed //
		//////////////////////////////////////////////////
		// wait for the update in the other federates //
		secondFederate.fedamb.waitForROUpdate( oHandle );
		TestObject temp = secondFederate.fedamb.getInstances().get( oHandle );
		// ensure that it has all the appropriate values //
		checkSecondFederateAttributes( temp );
		
		thirdFederate.fedamb.waitForROUpdate( oHandle );
		temp = thirdFederate.fedamb.getInstances().get( oHandle );
		// ensure that it has all the appropriate values //
		checkThirdFederateAttributes( temp );
	}

	///////////////////////////////////////////////
	// TEST: (valid) testROUpdateWithTimestamp() //
	///////////////////////////////////////////////
	/**
	 * The timestamp should be dropped and the update should be received RO.
	 */
	@Test
	public void testROUpdateWithTimestamp()
	{
		// do any necessary setup
		int oHandle = defaultFederate.quickRegister( "ObjectRoot.A.B" );
		secondFederate.fedamb.waitForDiscovery( oHandle );
		thirdFederate.fedamb.waitForDiscovery( oHandle );
		
		//////////////////////////////////////////////
		// attempt a valid update of all attributes //
		//////////////////////////////////////////////
		// we will not be providing updates for either
		// of the 'ac' or 'bc' attributes
		try
		{
			// do the actual update
			LogicalTime time = TypeFactory.createTime( 100.0 );
			ObjectInstanceHandle handle = getObjectHandle( oHandle );
			defaultFederate.rtiamb.updateAttributeValues( handle, updateSet, tag, time );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during RO attribute update with timestamp: "+e.getMessage(),e );
		}
		
		//////////////////////////////////////////////////
		// validate that the information is distributed //
		//////////////////////////////////////////////////
		// wait for the update in the other federates //
		secondFederate.fedamb.waitForROUpdate( oHandle );
		TestObject temp = secondFederate.fedamb.getInstances().get( oHandle );
		// ensure that it has all the appropriate values //
		checkSecondFederateAttributes( temp );
		
		thirdFederate.fedamb.waitForROUpdate( oHandle );
		temp = thirdFederate.fedamb.getInstances().get( oHandle );
		// ensure that it has all the appropriate values //
		checkThirdFederateAttributes( temp );
	}

	///////////////////////////////////////////////////
	// TEST: (valid) testROUpdateWithNullTimestamp() //
	///////////////////////////////////////////////////
	/**
	 * This method will ensure that when a null timestamp is passed by a non-regulating federate,
	 * that it is ignored (as all timestamps should be for non-regulating federates)
	 */
	@Test(dependsOnMethods="testROUpdateWithTimestamp")
	public void testROUpdateWithNullTimestamp()
	{
		// do any necessary setup
		int oHandle = defaultFederate.quickRegister( "ObjectRoot.A.B" );
		secondFederate.fedamb.waitForDiscovery( oHandle );
		thirdFederate.fedamb.waitForDiscovery( oHandle );
		
		//////////////////////////////////////////////
		// attempt a valid update of all attributes //
		//////////////////////////////////////////////
		// we will not be providing updates for either
		// of the 'ac' or 'bc' attributes
		try
		{
			// do the actual update with a NULL TIMESTAMP
			ObjectInstanceHandle handle = getObjectHandle( oHandle );
			defaultFederate.rtiamb.updateAttributeValues( handle, updateSet, tag, null );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during RO attribute update with null timestamp: " +
			             e.getMessage(), e );
		}
		
		//////////////////////////////////////////////////
		// validate that the information is distributed //
		//////////////////////////////////////////////////
		// wait for the update in the other federates //
		secondFederate.fedamb.waitForROUpdate( oHandle );
		TestObject temp = secondFederate.fedamb.getInstances().get( oHandle );
		// ensure that it has all the appropriate values //
		checkSecondFederateAttributes( temp );
		
		thirdFederate.fedamb.waitForROUpdate( oHandle );
		temp = thirdFederate.fedamb.getInstances().get( oHandle );
		// ensure that it has all the appropriate values //
		checkThirdFederateAttributes( temp );
	}
	
	/////////////////////////////////////////////////////
	// TEST: testROUpdateWithNonExistentObjectHandle() //
	/////////////////////////////////////////////////////
	@Test
	public void testROUpdateWithNonExistentObjectHandle()
	{
		// try and update an instance that doesn't exist //
		try
		{
			ObjectInstanceHandle handle = getObjectHandle( 142 );
			defaultFederate.rtiamb.updateAttributeValues( handle, updateSet, tag );
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
	
	//////////////////////////////////////////////////
	// TEST: testROUpdateWithNegativeObjectHandle() //
	//////////////////////////////////////////////////
	@Test
	public void testROUpdateWithNegativeObjectHandle()
	{
		// try and update a negative instance handle //
		try
		{
			ObjectInstanceHandle handle = getObjectHandle( -142 );
			defaultFederate.rtiamb.updateAttributeValues( handle, updateSet, tag );
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
	
	//////////////////////////////////////////////////////
	// TEST: testROUpdateWithUndefinedAttributeHandle() //
	//////////////////////////////////////////////////////
	@Test
	public void testROUpdateWithUndefinedAttributeHandle()
	{
		// try and update with an unknown attribute handle //
		try
		{
			ObjectInstanceHandle oHandle =
				getObjectHandle( defaultFederate.quickRegister("ObjectRoot.A.B") );
			updateSet.put( getAttributeHandle(123456), "bad".getBytes() );
			defaultFederate.rtiamb.updateAttributeValues( oHandle, updateSet, tag );
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
	
	////////////////////////////////////////////////////////
	// TEST: testROUpdateWithUnpublishedAttributeHandle() //
	////////////////////////////////////////////////////////
	@Test
	public void testROUpdateWithUnpublishedAttributeHandle()
	{
		// try and update with an unpublished attribute handle //
		try
		{
			ObjectInstanceHandle oHandle =
				getObjectHandle( defaultFederate.quickRegister("ObjectRoot.A.B") );
			updateSet.put( getAttributeHandle(bcHandle), "bc".getBytes() ); // not published!
			defaultFederate.rtiamb.updateAttributeValues( oHandle, updateSet, tag );
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
	
	//////////////////////////////////////////////
	// TEST: testROUpdateWithNullAttributeSet() //
	//////////////////////////////////////////////
	@Test
	public void testROUpdateWithNullAttributeSet()
	{
		// try and update an instance with null attributes //
		try
		{
			ObjectInstanceHandle oHandle =
				getObjectHandle( defaultFederate.quickRegister("ObjectRoot.A.B") );
			defaultFederate.rtiamb.updateAttributeValues( oHandle, null, tag );
			expectedException( RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RTIinternalError.class );
		}
	}

	////////////////////////////////////////////////////
	// TEST: testROUpdateWithUnownedAttributeHandle() //
	////////////////////////////////////////////////////
	@Test
	public void testROUpdateWithUnownedAttributeHandle()
	{
		log( "Requires Onwership. Not Yet Implemented." );
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Timestamp Order Test Methods //////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public MessageRetractionReturn updateAttributeValues( ObjectInstanceHandle theObject,
	//                                                       AttributeHandleValueMap theAttributes,
	//                                                       byte[] userSuppliedTag,
	//                                                       LogicalTime theTime )
	//        throws ObjectInstanceNotKnown,
	//               AttributeNotDefined,
	//               AttributeNotOwned,
	//               InvalidLogicalTime,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

	///////////////////////////////////
	// TEST: (valid) testTSOUpdate() //
	///////////////////////////////////
	/**
	 * This method depends on {@link #testROUpdate()}. It will extend that test by running
	 * through the same process, only involving a TSO reflection. The default federate is
	 * regulating, with a lookahead of 5. The second federate will be neither constrained, nor
	 * regulating, and so should receive all reflections as RO. The third federate will be
	 * constrained, and as such, should not receive the event until it has advanced to the proper
	 * time, and when it does it should recieve it as TSO.
	 */
	@Test(dependsOnMethods="testROUpdate")
	public void testTSOUpdate()
	{
		/////////////////////////
		// initialize the test //
		/////////////////////////
		defaultFederate.quickEnableRegulating( 5.0 );
		
		// register the instance and wait for it to be discovered //
		int oHandle = defaultFederate.quickRegister( "ObjectRoot.A.B" );
		secondFederate.fedamb.waitForDiscovery( oHandle );
		thirdFederate.fedamb.waitForDiscovery( oHandle );
		
		//////////////////////////////////////////////
		// update all attribute values at time 10.0 //
		//////////////////////////////////////////////
		try
		{
			LogicalTime time = createTime( 10.0 );
			ObjectInstanceHandle handle = getObjectHandle( oHandle );
			defaultFederate.rtiamb.updateAttributeValues( handle, updateSet, tag, time );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid TSO attribute update", e );
		}
		
		///////////////////////////////////////////////////
		// wait for the RO update in the second federate //
		///////////////////////////////////////////////////
		secondFederate.fedamb.waitForROUpdate( oHandle );
		TestObject temp = secondFederate.fedamb.getInstances().get( oHandle );
		// ensure that it has all the appropriate values //
		checkSecondFederateAttributes( temp );

		///////////////////////////////////////////
		// wait for the update in third federate //
		///////////////////////////////////////////
		// ensure the constrained federate hasn't got any premature update
		thirdFederate.fedamb.waitForTSOUpdateTimeout( oHandle );
		// no update, set the advance process in motion
		defaultFederate.quickAdvanceAndWait( 100.0 ); // that should be plenty :P
		// request the time advance - this should release the update
		thirdFederate.quickAdvanceRequest( 10.0 );
		// wait for the update
		thirdFederate.fedamb.waitForTSOUpdate( oHandle );
		
		// check the received values
		temp = thirdFederate.fedamb.getInstances().get( oHandle );
		// ensure that it has all the appropriate values //
		checkThirdFederateAttributes( temp );
		
		// wait for the time advance to be granted //
		thirdFederate.fedamb.waitForTimeAdvance( 10.0 );
	}
	
	////////////////////////////////////////////////////
	// TEST: (valid) testTSOUpdateWithNullTimestamp() //
	////////////////////////////////////////////////////
	/**
	 * If an update is passed null for the LogicalTime, the message should still send, but it
	 * should send RO.
	 */
	@Test(dependsOnMethods="testTSOUpdate")
	public void testTSOUpdateWithNullTimestamp()
	{
		/////////////////////////
		// initialize the test //
		/////////////////////////
		defaultFederate.quickEnableRegulating( 5.0 );
		
		// register the instance and wait for it to be discovered //
		int oHandle = defaultFederate.quickRegister( "ObjectRoot.A.B" );
		secondFederate.fedamb.waitForDiscovery( oHandle );
		thirdFederate.fedamb.waitForDiscovery( oHandle );
		
		//////////////////////////////////////////////
		// update all attribute values at time 10.0 //
		//////////////////////////////////////////////
		try
		{
			ObjectInstanceHandle handle = getObjectHandle( oHandle );
			defaultFederate.rtiamb.updateAttributeValues( handle, updateSet, tag, null );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid TSO attribute update", e );
		}
		
		///////////////////////////////////////////////////
		// wait for the RO update in the second federate //
		///////////////////////////////////////////////////
		secondFederate.fedamb.waitForROUpdate( oHandle );
		TestObject temp = secondFederate.fedamb.getInstances().get( oHandle );
		// ensure that it has all the appropriate values //
		checkSecondFederateAttributes( temp );

		///////////////////////////////////////////
		// wait for the update in third federate //
		///////////////////////////////////////////
		// we should get the update as RO
		thirdFederate.fedamb.waitForROUpdate( oHandle );
		// check the received values
		temp = thirdFederate.fedamb.getInstances().get( oHandle );
		// ensure that it has all the appropriate values //
		checkThirdFederateAttributes( temp );
	}
	
	////////////////////////////////////////////////
	// TEST: testTSOUpdateWithNegativeTimestamp() //
	////////////////////////////////////////////////
	@Test
	public void testTSOUpdateWithNegativeTimestamp()
	{
		// setup
		defaultFederate.quickEnableRegulating( 5.0 );
		int oHandle = defaultFederate.quickRegister( "ObjectRoot.A.B" );
		
		// try and update an instance with a negative time //
		try
		{
			LogicalTime time = createTime( -11.0 );
			ObjectInstanceHandle handle = getObjectHandle( oHandle );
			defaultFederate.rtiamb.updateAttributeValues( handle, updateSet, tag, time );
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

	///////////////////////////////////////////////////
	// TEST: testTSOUpdateWithNegativeObjectHandle() //
	///////////////////////////////////////////////////
	@Test
	public void testTSOUpdateWithNegativeObjectHandle()
	{
		// setup
		defaultFederate.quickEnableRegulating( 5.0 );
		LogicalTime time = createTime( 5.0 );
		
		// try and update a negative instance handle //
		try
		{
			ObjectInstanceHandle handle = getObjectHandle( -142 );
			defaultFederate.rtiamb.updateAttributeValues( handle, updateSet, tag, time );
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
	
	//////////////////////////////////////////////////////
	// TEST: testTSOUpdateWithNonExistentObjectHandle() //
	//////////////////////////////////////////////////////
	@Test
	public void testTSOUpdateWithNonExistentObjectHandle()
	{
		// setup
		defaultFederate.quickEnableRegulating( 5.0 );
		LogicalTime time = createTime( 5.0 );

		// try and update an instance that doesn't exist //
		try
		{
			ObjectInstanceHandle handle = getObjectHandle( 142 );
			defaultFederate.rtiamb.updateAttributeValues( handle, updateSet, tag, time );
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
	
	///////////////////////////////////////////////
	// TEST: testTSOUpdateWithNullAttributeSet() //
	///////////////////////////////////////////////
	@Test
	public void testTSOUpdateWithNullAttributeSet()
	{
		// setup
		defaultFederate.quickEnableRegulating( 5.0 );
		LogicalTime time = createTime( 5.0 );
		int oHandle = defaultFederate.quickRegister( "ObjectRoot.A.B" );
		ObjectInstanceHandle handle = getObjectHandle( oHandle );

		// try and update an instance with null attributes //
		try
		{
			defaultFederate.rtiamb.updateAttributeValues( handle, null, tag, time );
			expectedException( RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RTIinternalError.class );
		}
	}
	
	///////////////////////////////////////////////////////
	// TEST: testTSOUpdateWithUndefinedAttributeHandle() //
	///////////////////////////////////////////////////////
	@Test
	public void testTSOUpdateWithUndefinedAttributeHandle()
	{
		// setup
		defaultFederate.quickEnableRegulating( 5.0 );
		LogicalTime time = createTime( 5.0 );
		int oHandle = defaultFederate.quickRegister( "ObjectRoot.A.B" );

		// try and update with an unknown attribute handle //
		try
		{
			updateSet.put( getAttributeHandle(123456), "bad".getBytes() );
			ObjectInstanceHandle handle = getObjectHandle( oHandle );
			defaultFederate.rtiamb.updateAttributeValues( handle, updateSet, tag, time );
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
	
	/////////////////////////////////////////////////////
	// TEST: testTSOUpdateWithUnownedAttributeHandle() //
	/////////////////////////////////////////////////////
	@Test
	public void testTSOUpdateWithUnownedAttributeHandle()
	{
		log( "Requires Onwership. Not Yet Implemented." );
	}
	
	/////////////////////////////////////////////////////////
	// TEST: testTSOUpdateWithUnpublishedAttributeHandle() //
	/////////////////////////////////////////////////////////
	@Test
	public void testTSOUpdateWithUnpublishedAttributeHandle()
	{
		// setup
		defaultFederate.quickEnableRegulating( 5.0 );
		LogicalTime time = createTime( 5.0 );
		int oHandle = defaultFederate.quickRegister( "ObjectRoot.A.B" );
		
		// try and update with an unpublished attribute handle //
		try
		{
			updateSet.put( getAttributeHandle(bcHandle), "bc".getBytes() ); // not published!
			ObjectInstanceHandle handle = getObjectHandle( oHandle );
			defaultFederate.rtiamb.updateAttributeValues( handle, updateSet, tag, time );
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

	/////////////////////////////////////////////////////////
	// TEST: testTSOUpdateWithTimestampLessThanLookahead() //
	/////////////////////////////////////////////////////////
	@Test
	public void testTSOUpdateWithTimestampLessThanLookahead()
	{
		// setup
		defaultFederate.quickEnableRegulating( 5.0 );
		int oHandle = defaultFederate.quickRegister( "ObjectRoot.A.B" );
		
		// try and update an instance with a time that is less than our lookahead //
		try
		{
			LogicalTime time = createTime( 1.0 );
			ObjectInstanceHandle handle = getObjectHandle( oHandle );
			defaultFederate.rtiamb.updateAttributeValues( handle, updateSet, tag, time );
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
	
	//////////////////////////////////////////////
	// TEST: testTSOUpdateWithTimestampInPast() //
	//////////////////////////////////////////////
	@Test
	public void testTSOUpdateWithTimestampInPast()
	{
		// setup
		defaultFederate.quickEnableRegulating( 5.0 );
		int oHandle = defaultFederate.quickRegister( "ObjectRoot.A.B" );

		// advance in time some
		defaultFederate.quickAdvanceAndWait( 100.0 );
		
		// try and update an instance with a time that is less than our lookahead //
		try
		{
			LogicalTime time = createTime( 10.0 );
			ObjectInstanceHandle handle = getObjectHandle( oHandle );
			defaultFederate.rtiamb.updateAttributeValues( handle, updateSet, tag, time );
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
