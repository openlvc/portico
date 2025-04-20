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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import hla.rti.AttributeNotDefined;
import hla.rti.AttributeNotOwned;
import hla.rti.FederateNotExecutionMember;
import hla.rti.InvalidFederationTime;
import hla.rti.LogicalTime;
import hla.rti.ObjectNotKnown;
import hla.rti.RTIinternalError;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hla.rti.SuppliedAttributes;
import hlaunit.hla13.TestSetup;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;
import hlaunit.hla13.common.Test13Instance;

import static org.testng.Assert.*;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"ReflectAttributesTest", "reflectAttributes", "objectManagement"})
public class ReflectAttributesTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate secondFederate;
	private Test13Federate thirdFederate;

	private int aHandle, aaHandle, abHandle, acHandle;
	private int bHandle, baHandle, bbHandle, bcHandle;
	
	private int objectHandle;
	private SuppliedAttributes updateSet;
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
		secondFederate = new Test13Federate( "secondFederate", this );
		thirdFederate = new Test13Federate( "thirdFederate", this );
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
		updateSet = TestSetup.getRTIFactory().createSuppliedAttributes();
		updateSet.add( aaHandle, "aa".getBytes() );
		updateSet.add( abHandle, "ab".getBytes() );
		updateSet.add( baHandle, "ba".getBytes() );
		updateSet.add( bbHandle, "bb".getBytes() );

		// do publish and subscribe //
		defaultFederate.quickPublish( "ObjectRoot.A.B", "aa", "ab", "ba", "bb" );
		secondFederate.quickSubscribe( "ObjectRoot.A.B", "aa", "ab", "ac", "ba", "bb", "bc" );
		thirdFederate.quickSubscribe( "ObjectRoot.A", "aa", "ab", "ac" );
		
		objectHandle = defaultFederate.quickRegister( "ObjectRoot.A.B" );
		secondFederate.fedamb.waitForDiscoveryAs( objectHandle, bHandle );
		thirdFederate.fedamb.waitForDiscoveryAs( objectHandle, aHandle );
		
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
	 * This method will check the values in the given {@link Test13Instance} and ensure that they
	 * are consistent with those that are expected given the subscription interests of the second
	 * federate.
	 */
	private void checkSecondFederateAttributes( Test13Instance instance )
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
	 * This method will check the values in the given {@link Test13Instance} and ensure that they
	 * are consistent with those that are expected given the subscription interests of the third
	 * federate.
	 */
	private void checkThirdFederateAttributes( Test13Instance instance )
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
	///////////////////////////////// General Update Methods /////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * According to a bug report filed against Portico (using JGroups), there are problems with
	 * bytes being zero'd out after a certain number when sending large updates (GH #65). This
	 * method generates an update of size 1MB to test for this behaviour. Random values are used
	 * to fill the byte[]'s sent.
	 */
	@Test(groups="jgroups")
	public void testROUpdateWithLargeAttributeValue()
	{
		// let's just specify how large a message we want to use for testing in one
		// place so that we can change it quickly.
		int payloadSize = 1048576; // 1MiB
		
		byte[] sentArray = new byte[payloadSize];
		Random random = new Random();
		random.nextBytes( sentArray );
		
		// package this into an update and sent it from the sender to the receiver
		Map<String,byte[]> updatedAttributes = new HashMap<String,byte[]>();
		updatedAttributes.put( "aa", sentArray );
		defaultFederate.quickReflect( objectHandle, updatedAttributes, null );
		
		// validate that the values reach the other side ok
		secondFederate.fedamb.waitForROUpdate( objectHandle );
		Test13Instance temp = secondFederate.fedamb.getInstances().get( objectHandle );
		
		// ensure that it has all the appropriate values
		byte[] receivedArray = temp.getAttributeValue( aaHandle );
		assertNotNull( receivedArray, "did not receive update for correct attribute" );
		assertEquals( receivedArray.length, payloadSize, "received wrong number of bytes in update" );
		for( int i = 0; i < payloadSize; i++ )
		{
			assertEquals( receivedArray[i], sentArray[i], "byte at ["+i+"] was incorrect" );
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Receive Order Test Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void updateAttributeValues( int theObject,
	//                                    SuppliedAttributes theAttributes,
	//                                    byte[] userSuppliedTag )
	//        throws ObjectNotKnown,
	//               AttributeNotDefined,
	//               AttributeNotOwned,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	//////////////////////////////////
	// TEST: (valid) testROUpdate() //
	//////////////////////////////////
	// NOTE: I've put the test below into the "simple" group so that it can be used to test very
	//       simple cases of errors, primarily configuration errors in things like JGroups.
	@Test(groups="simple")
	public void testROUpdate()
	{
		//////////////////////////////////////////////
		// attempt a valid update of all attributes //
		//////////////////////////////////////////////
		// we will not be providing updates for either
		// of the 'ac' or 'bc' attributes
		try
		{
			// do the actual update
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during valid RO attribute update: " + e.getMessage(), e );
		}
		
		//////////////////////////////////////////////////
		// validate that the information is distributed //
		//////////////////////////////////////////////////
		// wait for the update in the other federates //
		secondFederate.fedamb.waitForROUpdate( objectHandle );
		Test13Instance temp = secondFederate.fedamb.getInstances().get( objectHandle );
		// ensure that it has all the appropriate values //
		checkSecondFederateAttributes( temp );
		
		thirdFederate.fedamb.waitForROUpdate( objectHandle );
		temp = thirdFederate.fedamb.getInstances().get( objectHandle );
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
		//////////////////////////////////////////////
		// attempt a valid update of all attributes //
		//////////////////////////////////////////////
		// we will not be providing updates for either
		// of the 'ac' or 'bc' attributes
		try
		{
			// do the actual update
			LogicalTime time = defaultFederate.createTime( 100.0 );
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag, time );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during RO attribute update with timestamp: "+e.getMessage(),e );
		}
		
		//////////////////////////////////////////////////
		// validate that the information is distributed //
		//////////////////////////////////////////////////
		// wait for the update in the other federates //
		secondFederate.fedamb.waitForROUpdate( objectHandle );
		Test13Instance temp = secondFederate.fedamb.getInstances().get( objectHandle );
		// ensure that it has all the appropriate values //
		checkSecondFederateAttributes( temp );
		
		thirdFederate.fedamb.waitForROUpdate( objectHandle );
		temp = thirdFederate.fedamb.getInstances().get( objectHandle );
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
		//////////////////////////////////////////////
		// attempt a valid update of all attributes //
		//////////////////////////////////////////////
		// we will not be providing updates for either
		// of the 'ac' or 'bc' attributes
		try
		{
			// do the actual update with a NULL TIMESTAMP
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag, null );
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
		secondFederate.fedamb.waitForROUpdate( objectHandle );
		Test13Instance temp = secondFederate.fedamb.getInstances().get( objectHandle );
		// ensure that it has all the appropriate values //
		checkSecondFederateAttributes( temp );
		
		thirdFederate.fedamb.waitForROUpdate( objectHandle );
		temp = thirdFederate.fedamb.getInstances().get( objectHandle );
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
			defaultFederate.rtiamb.updateAttributeValues( 142, updateSet, tag );
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
	
	//////////////////////////////////////////////////
	// TEST: testROUpdateWithNegativeObjectHandle() //
	//////////////////////////////////////////////////
	@Test
	public void testROUpdateWithNegativeObjectHandle()
	{
		// try and update a negative instance handle //
		try
		{
			defaultFederate.rtiamb.updateAttributeValues( -142, updateSet, tag );
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
	
	//////////////////////////////////////////////////////
	// TEST: testROUpdateWithUndefinedAttributeHandle() //
	//////////////////////////////////////////////////////
	@Test
	public void testROUpdateWithUndefinedAttributeHandle()
	{
		// try and update with an unknown attribute handle //
		try
		{
			updateSet.add( 123456, "bad".getBytes() );
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag );
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
			updateSet.add( bcHandle, "bc".getBytes() ); // not published!
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag );
			expectedException( AttributeNotDefined.class );
		}
		catch( AttributeNotOwned ano )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, AttributeNotDefined.class );
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
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, null, tag );
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
		// transfer ownership of one of the attributes
		defaultFederate.quickUnconditionalDivest( objectHandle, aaHandle );
		try
		{
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag );
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

	///////////////////////////////////////
	// TEST: testROUpdateWhenNotJoined() //
	///////////////////////////////////////
	@Test
	public void testROUpdateWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag );
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

	////////////////////////////////////////////
	// TEST: testROUpdateWhenSaveInProgress() //
	////////////////////////////////////////////
	@Test
	public void testROUpdateWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag );
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

	///////////////////////////////////////////////
	// TEST: testROUpdateWhenRestoreInProgress() //
	///////////////////////////////////////////////
	@Test
	public void testROUpdateWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag );
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
	////////////////////////////// Timestamp Order Test Methods //////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public EventRetractionHandle updateAttributeValues( int theObject,
	//                                                     SuppliedAttributes theAttributes,
	//                                                     byte[] userSuppliedTag,
	//                                                     LogicalTime theTime )
	//        throws ObjectNotKnown,
	//               AttributeNotDefined,
	//               AttributeNotOwned,
	//               InvalidFederationTime,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

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
		
		//////////////////////////////////////////////
		// update all attribute values at time 10.0 //
		//////////////////////////////////////////////
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag, time );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid TSO attribute update", e );
		}
		
		///////////////////////////////////////////////////
		// wait for the RO update in the second federate //
		///////////////////////////////////////////////////
		secondFederate.fedamb.waitForROUpdate( objectHandle );
		Test13Instance temp = secondFederate.fedamb.getInstances().get( objectHandle );
		// ensure that it has all the appropriate values //
		checkSecondFederateAttributes( temp );

		///////////////////////////////////////////
		// wait for the update in third federate //
		///////////////////////////////////////////
		// ensure the constrained federate hasn't got any premature update
		thirdFederate.fedamb.waitForTSOUpdateTimeout( objectHandle );
		// no update, set the advance process in motion
		defaultFederate.quickAdvanceAndWait( 100.0 ); // that should be plenty :P
		// request the time advance - this should release the update
		thirdFederate.quickAdvanceRequest( 10.0 );
		// wait for the update
		thirdFederate.fedamb.waitForTSOUpdate( objectHandle );
		
		// check the received values
		temp = thirdFederate.fedamb.getInstances().get( objectHandle );
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
		
		//////////////////////////////////////////////
		// update all attribute values at time 10.0 //
		//////////////////////////////////////////////
		try
		{
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag, null );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid TSO attribute update", e );
		}
		
		///////////////////////////////////////////////////
		// wait for the RO update in the second federate //
		///////////////////////////////////////////////////
		secondFederate.fedamb.waitForROUpdate( objectHandle );
		Test13Instance temp = secondFederate.fedamb.getInstances().get( objectHandle );
		// ensure that it has all the appropriate values //
		checkSecondFederateAttributes( temp );

		///////////////////////////////////////////
		// wait for the update in third federate //
		///////////////////////////////////////////
		// we should get the update as RO
		thirdFederate.fedamb.waitForROUpdate( objectHandle );
		// check the received values
		temp = thirdFederate.fedamb.getInstances().get( objectHandle );
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
		
		// try and update an instance with a negative time //
		try
		{
			LogicalTime time = defaultFederate.createTime( -11.0 );
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag, time );
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

	///////////////////////////////////////////////////
	// TEST: testTSOUpdateWithNegativeObjectHandle() //
	///////////////////////////////////////////////////
	@Test
	public void testTSOUpdateWithNegativeObjectHandle()
	{
		// setup
		defaultFederate.quickEnableRegulating( 5.0 );
		LogicalTime time = defaultFederate.createTime( 5.0 );
		
		// try and update a negative instance handle //
		try
		{
			defaultFederate.rtiamb.updateAttributeValues( -142, updateSet, tag, time );
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
	
	//////////////////////////////////////////////////////
	// TEST: testTSOUpdateWithNonExistentObjectHandle() //
	//////////////////////////////////////////////////////
	@Test
	public void testTSOUpdateWithNonExistentObjectHandle()
	{
		// setup
		defaultFederate.quickEnableRegulating( 5.0 );
		LogicalTime time = defaultFederate.createTime( 5.0 );

		// try and update an instance that doesn't exist //
		try
		{
			defaultFederate.rtiamb.updateAttributeValues( 142, updateSet, tag, time );
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
	
	///////////////////////////////////////////////
	// TEST: testTSOUpdateWithNullAttributeSet() //
	///////////////////////////////////////////////
	@Test
	public void testTSOUpdateWithNullAttributeSet()
	{
		// setup
		defaultFederate.quickEnableRegulating( 5.0 );
		LogicalTime time = defaultFederate.createTime( 5.0 );

		// try and update an instance with null attributes //
		try
		{
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, null, tag, time );
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
		LogicalTime time = defaultFederate.createTime( 5.0 );

		// try and update with an unknown attribute handle //
		try
		{
			updateSet.add( 123456, "bad".getBytes() );
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag, time );
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
		// transfer ownership of one of the attributes
		defaultFederate.quickUnconditionalDivest( objectHandle, aaHandle );
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag, time );
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
	// TEST: testTSOUpdateWithUnpublishedAttributeHandle() //
	/////////////////////////////////////////////////////////
	@Test
	public void testTSOUpdateWithUnpublishedAttributeHandle()
	{
		// setup
		defaultFederate.quickEnableRegulating( 5.0 );
		LogicalTime time = defaultFederate.createTime( 5.0 );
		
		// try and update with an unpublished attribute handle //
		try
		{
			updateSet.add( bcHandle, "bc".getBytes() ); // not published!
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag, time );
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
		
		// try and update an instance with a time that is less than our lookahead //
		try
		{
			LogicalTime time = defaultFederate.createTime( 1.0 );
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag, time );
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
	
	//////////////////////////////////////////////
	// TEST: testTSOUpdateWithTimestampInPast() //
	//////////////////////////////////////////////
	@Test
	public void testTSOUpdateWithTimestampInPast()
	{
		// setup
		defaultFederate.quickEnableRegulating( 5.0 );

		// advance in time some
		defaultFederate.quickAdvanceAndWait( 100.0 );
		
		// try and update an instance with a time that is less than our lookahead //
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag, time );
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

	////////////////////////////////////////
	// TEST: testTSOUpdateWhenNotJoined() //
	////////////////////////////////////////
	@Test
	public void testTSOUpdateWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag, time );
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

	/////////////////////////////////////////////
	// TEST: testTSOUpdateWhenSaveInProgress() //
	/////////////////////////////////////////////
	@Test
	public void testTSOUpdateWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag, time );
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

	////////////////////////////////////////////////
	// TEST: testTSOUpdateWhenRestoreInProgress() //
	////////////////////////////////////////////////
	@Test
	public void testTSOUpdateWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			LogicalTime time = defaultFederate.createTime( 10.0 );
			defaultFederate.rtiamb.updateAttributeValues( objectHandle, updateSet, tag, time );
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
