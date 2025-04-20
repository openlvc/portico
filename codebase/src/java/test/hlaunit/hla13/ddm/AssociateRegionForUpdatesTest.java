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
package hlaunit.hla13.ddm;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotDefined;
import hla.rti.FederateNotExecutionMember;
import hla.rti.InvalidRegionContext;
import hla.rti.ObjectNotKnown;
import hla.rti.RTIinternalError;
import hla.rti.Region;
import hla.rti.RegionNotKnown;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;
import hlaunit.hla13.common.Test13Instance;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"associateRegion", "AssociateRegionForUpdatesTest", "ddm"})
public class AssociateRegionForUpdatesTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate listener;
	private Test13Federate nonddmlistener;
	private int aHandle, aaHandle, abHandle, acHandle;
	private AttributeHandleSet allAttributes;
	private int theObject;
	private Region defaultRegion;
	private Region defaultRegionOob;
	private Region listenerRegion;
	
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
		
		this.listener = new Test13Federate( "listener", this );
		this.nonddmlistener = new Test13Federate( "nonddmlistener", this );
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		listener.quickJoin();
		nonddmlistener.quickJoin();
		
		// fetch the handles
		aHandle = defaultFederate.quickOCHandle( "ObjectRoot.A" );
		aaHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "aa" );
		abHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ab" );
		acHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ac" );
		allAttributes = defaultFederate.createAHS( aaHandle, abHandle, acHandle );
		
		// do pub/sub and object setup
		defaultFederate.quickPublish( aHandle, aaHandle, abHandle, acHandle );
		theObject = defaultFederate.quickRegister( aHandle );
		defaultRegion = defaultFederate.quickCreateTestRegion( 100, 200 );
		defaultRegionOob = defaultFederate.quickCreateTestRegion( 500, 600 );
		
		listenerRegion = listener.quickCreateTestRegion( 150, 250 );
		listener.quickSubscribeWithRegion( aHandle, listenerRegion, aaHandle, abHandle, acHandle );
		listener.fedamb.waitForDiscovery( theObject );
		
		nonddmlistener.quickSubscribe( "ObjectRoot.A", "aa", "ab", "ac" );
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		nonddmlistener.quickResign();
		listener.quickResign();
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
	 * Send an update for <b>ALL</b> the attributes of ObjectRoot.A by the defaultFederate.
	 * Once the update has been received in the listener, validate that ONLY the attributes
	 * with the provided handle were updated (and thus, confirm that the subscription region
	 * overlaps with the associated region in the defaultFederate).
	 */
	private void validateInRegion( int... attributes )
	{
		// do the send
		String valueString = doSend();
		
		// validate the values that were received
		Test13Instance received = listener.fedamb.getInstances().get( theObject );
		for( int attributeHandle : attributes )
		{
			String current = new String( received.getAttributeValue(attributeHandle) );
			Assert.assertEquals( current, valueString );
			Assert.assertEquals( received.getRegion(attributeHandle), listenerRegion );
		}
		
		// validate that the attributes were received in non-ddm subscribers
		received = nonddmlistener.fedamb.getInstances().get( theObject );
		for( int attributeHandle : attributes )
		{
			String current = new String( received.getAttributeValue(attributeHandle) );
			Assert.assertEquals( current, valueString );
		}
	}

	/**
	 * Send an update for <b>ALL</b> the attributes of ObjectRoot.A by the defaultFederate.
	 * Once the update has been received in the listener, validate that all attributes
	 * with the provided handle were updated and associated with *NO* region at all.
	 */
	private void validateInDefaultRegion( int... attributes )
	{
		// do the send
		String valueString = doSend();
		
		// validate the values that were received
		Test13Instance received = listener.fedamb.getInstances().get( theObject );
		for( int attributeHandle : attributes )
		{
			String current = new String( received.getAttributeValue(attributeHandle) );
			Assert.assertEquals( current, valueString );
			Assert.assertEquals( received.getRegion(attributeHandle), null );
		}
		
		// validate that the attributes were received in non-ddm subscribers
		received = nonddmlistener.fedamb.getInstances().get( theObject );
		for( int attributeHandle : attributes )
		{
			String current = new String( received.getAttributeValue(attributeHandle) );
			Assert.assertEquals( current, valueString );
		}
	}

	
	/**
	 * Send an update for <b>ALL</b> the attributes of ObjectRoot.A by the defaultFederate.
	 * Once the update has been received in the listener, validate that the attributes with
	 * the provided handle were <b>NOT</b> updated (and thus, confirm that the subscription
	 * region for those attributes does not overlap with the associated region in the
	 * defaultFederate).
	 */
	private void validateNotInRegion( int... attributes )
	{
		// do the send
		String valueString = doSend();
		
		// validate the values that were received
		Test13Instance received = listener.fedamb.getInstances().get( theObject );
		for( int attributeHandle : attributes )
		{
			// if the value is null, doSend() didn't update it, thus, we got what we wanted
			if( received.getAttributeValue(attributeHandle) != null )
			{
				String current = new String( received.getAttributeValue(attributeHandle) );
				Assert.assertNotSame( current, valueString );
			}
		}

		// validate that the attributes WERE received in non-ddm subscribers
		received = nonddmlistener.fedamb.getInstances().get( theObject );
		for( int attributeHandle : attributes )
		{
			String current = new String( received.getAttributeValue(attributeHandle) );
			Assert.assertEquals( current, valueString );
		}
	}
	
	/**
	 * This method is used by the other valiadation method as a means to avoid some duplication.
	 * It will generate a new random integer and put it in String form, using the bytes of the
	 * string for the update. It will then update each of attributes for the class ObjectRoot.A
	 * with that value. Finally, it will return the string it generated so that it can be used
	 * for comparison in the validation methods. Note that this method will send the update from
	 * the defaultFederate and then wait until the listener federate receives it (as an RO update).
	 * Thus, after a successful call to this method, the new values for the provided attributes
	 * should reside in the listener.
	 * 
	 * @return The String for which the byte value was used as the new value for the attributes
	 */
	private String doSend()
	{
		// generate a unique string to use as the value for the update
		String valueString = "" + (new Random()).nextInt();
		
		// generate the new attribute values
		Map<Integer,byte[]> attributeMap = new HashMap<Integer,byte[]>();
		attributeMap.put( aaHandle, valueString.getBytes() );
		attributeMap.put( abHandle, valueString.getBytes() );
		attributeMap.put( acHandle, valueString.getBytes() );
		
		// do the send
		defaultFederate.quickReflectWithHandles( theObject, attributeMap, "letag".getBytes() );
		
		// validate that the listener receives the reflection
		listener.fedamb.waitForROUpdate( theObject );
		nonddmlistener.fedamb.waitForROUpdate( theObject );

		return valueString;
	}
	
	/**
	 * Because attributes sent without a region are reflected to all subscribed federates
	 * (regardless of subscribed region), we have to first associate the attributes with a region
	 * that is NOT in range, and then validate that the attributes ARE in range once an
	 * unassociation has been processed if we are to properly test unassociation.
	 */
	private void associateAllAttributesWithOob()
	{
		defaultFederate.quickAssociateWithRegion( theObject, defaultRegionOob,
		                                          aaHandle, abHandle, acHandle );
		
		// update the object and make sure the listener doesn't receive it
		defaultFederate.quickReflect( theObject, aaHandle, abHandle, acHandle );
		listener.fedamb.waitForROUpdateTimeout( theObject );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Associate Region Tests /////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void associateRegionForUpdates( Region theRegion,
	//                                        int theObject,
	//                                        AttributeHandleSet theAttributes )
	//        throws ObjectNotKnown,
	//               AttributeNotDefined,
	//               InvalidRegionContext,
	//               RegionNotKnown,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	/////////////////////////////////////////////////////////////////////
	// TEST: (valid) testAssociateRegionForUpdatesUsingAllAttributes() //
	/////////////////////////////////////////////////////////////////////
	@Test
	public void testAssociateRegionForUpdates()
	{
		// put the attributes in the subscription region and validate that it worked
		defaultFederate.quickAssociateWithRegion( theObject, defaultRegion,
		                                          aaHandle, abHandle, acHandle );
		validateInRegion( aaHandle, abHandle, acHandle );
	}
	
	/////////////////////////////////////////////////////////////////////
	// TEST: (valid) testAssociateRegionForUpdatesUsingAllAttributes() //
	/////////////////////////////////////////////////////////////////////
	@Test
	public void testAssociateRegionForUpdatesWithEmptyHandleSet()
	{
		// quick associate and validate so we can test that the empty handle set does cause
		// an unassociation to occur later on
		defaultFederate.quickAssociateWithRegion( theObject, defaultRegion,
		                                          aaHandle, abHandle, acHandle );
		validateInRegion( aaHandle, abHandle, acHandle );

		// call associate with an empty handle set: an implicit unassociate
		try
		{
			AttributeHandleSet emptySet = defaultFederate.createAHS();
			defaultFederate.rtiamb.associateRegionForUpdates( defaultRegion, theObject, emptySet );
		}
		catch( Exception e )
		{
			unexpectedException( "Calling assocateRegionForUpdates() with empty handle set", e );
		}
		
		// validate that the association no longer stands
		validateNotInRegion( aaHandle, abHandle, acHandle );
	}
	
	////////////////////////////////////////////////////////
	// TEST: (valid) testAssociateRegionForUpdatesTwice() //
	////////////////////////////////////////////////////////
	/**
	 * You can associate a set of attributes with a region to which it is already associated. This
	 * test validates that this works and doesn't cause an error.
	 */
	@Test
	public void testAssociateRegionForUpdatesTwice()
	{
		// associate and validate
		defaultFederate.quickAssociateWithRegion( theObject, defaultRegion, aaHandle );
		validateInRegion( aaHandle );

		// mmm, double up, mmm, mmm
		defaultFederate.quickAssociateWithRegion( theObject, defaultRegion, aaHandle );
		validateInRegion( aaHandle );
	}

	//////////////////////////////////////////////////////////////////////
	// TEST: (valid) testAssociateRegionForUpdatesUsingSomeAttributes() //
	//////////////////////////////////////////////////////////////////////
	@Test
	public void testAssociateRegionForUpdatesUsingSomeAttributes()
	{
		// put the attributes in the subscription region and validate that it worked
		defaultFederate.quickAssociateWithRegion( theObject, defaultRegion, aaHandle );
		defaultFederate.quickAssociateWithRegion( theObject, defaultRegionOob, abHandle, acHandle );
		validateInRegion( aaHandle );
		validateNotInRegion( abHandle, acHandle );
	}

	///////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testAssociateRegionForUpdatesAllAttributesOutOfRegion() //
	///////////////////////////////////////////////////////////////////////////
	@Test
	public void testAssociateRegionForUpdatesAllAttributesOutOfRegion()
	{
		// put all the attributes in some other region and make sure they're not updates
		defaultFederate.quickAssociateWithRegion( theObject, defaultRegionOob,
		                                          aaHandle, abHandle, acHandle );
		
		//validateNotInRegion( aaHandle, abHandle, acHandle ); -- won't work, all atts oob
		defaultFederate.quickReflect( theObject, aaHandle, abHandle, acHandle );
		listener.fedamb.waitForROUpdateTimeout( theObject );
	}

	//////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testAssociateRegionForUpdatesReassociatingSomeAttributes() //
	//////////////////////////////////////////////////////////////////////////////
	@Test
	public void testAssociateRegionForUpdatesReassociatingSomeAttributes()
	{
		// put all the attributes in the overlapping region
		defaultFederate.quickAssociateWithRegion( theObject, defaultRegion,
		                                          aaHandle, abHandle, acHandle );
		validateInRegion( aaHandle, abHandle, acHandle );
		
		// reassociate some of the attributes with the oob region
		defaultFederate.quickAssociateWithRegion( theObject, defaultRegionOob, aaHandle );
		validateNotInRegion( aaHandle );
		validateInRegion( abHandle, acHandle );
	}
	
	/////////////////////////////////////////////////////////////////////
	// TEST: (valid) testAssociateRegionForUpdatesWhenRangesAreEqual() //
	/////////////////////////////////////////////////////////////////////
	@Test
	public void testAssociateRegionForUpdatesWhenRangesAreEqual()
	{
		// create two regions to test with
		Region senderRegionEqual = defaultFederate.quickCreateTestRegion( 100000, 100000 );
		Region listenerRegionEqual = listener.quickCreateTestRegion( 100000, 100000 );
		listener.quickSubscribeWithRegion( "ObjectRoot.A", listenerRegionEqual, "aa", "ab", "ac" );
		
		// create an entirely separate object to test with and make the association
		int localObject = defaultFederate.quickRegister( "ObjectRoot.A" );
		listener.fedamb.waitForDiscovery( localObject );
		defaultFederate.quickAssociateWithRegion( localObject, senderRegionEqual,
		                                          aaHandle, abHandle, acHandle );
		
		// update the attributes
		defaultFederate.quickReflect( localObject, aaHandle, abHandle, acHandle );
		
		// wait for the callback to validate that there is overlap
		listener.fedamb.waitForROUpdate( localObject );
	}

	/////////////////////////////////////////////////////////////
	// TEST: testAssociateRegionForUpdatesUsingUnknownObject() //
	/////////////////////////////////////////////////////////////
	@Test
	public void testAssociateRegionForUpdatesUsingUnknownObject()
	{
		try
		{
			defaultFederate.rtiamb.associateRegionForUpdates( defaultRegion, 11111, allAttributes );
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
	// TEST: testAssociateRegionForUpdatesUsingNegativeObject() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testAssociateRegionForUpdatesUsingNegativeObject()
	{
		try
		{
			defaultFederate.rtiamb.associateRegionForUpdates( defaultRegion, -1, allAttributes );
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
	// TEST: testAssociateRegionForUpdatesUsingUnknownRegion() //
	/////////////////////////////////////////////////////////////
	@Test
	public void testAssociateRegionForUpdatesUsingUnknownRegion()
	{
		// create a region in the listener and then try to use it for the association
		// with the defaultFederate
		Region newRegion = listener.quickCreateTestRegion( 100, 200 );
		
		try
		{
			defaultFederate.rtiamb.associateRegionForUpdates( newRegion, theObject, allAttributes );
			expectedException( RegionNotKnown.class );
		}
		catch( RegionNotKnown rnk )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RegionNotKnown.class );
		}
	}

	/////////////////////////////////////////////////////////////
	// TEST: testAssociateRegionForUpdatesUsingInvalidRegion() //
	/////////////////////////////////////////////////////////////
	@Test
	public void testAssociateRegionForUpdatesUsingInvalidRegion()
	{
		// create a region for a space that none of the attributes are associated
		// with in the object model
		Region newRegion = defaultFederate.quickCreateRegion( "OtherSpace", 1 );
		
		try
		{
			defaultFederate.rtiamb.associateRegionForUpdates( newRegion, theObject, allAttributes );
			expectedException( InvalidRegionContext.class );
		}
		catch( InvalidRegionContext irc )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InvalidRegionContext.class );
		}
	}

	//////////////////////////////////////////////////////////
	// TEST: testAssociateRegionForUpdatesUsingNullRegion() //
	//////////////////////////////////////////////////////////
	@Test
	public void testAssociateRegionForUpdatesUsingNullRegion()
	{
		try
		{
			defaultFederate.rtiamb.associateRegionForUpdates( null, theObject, allAttributes );
			expectedException( RegionNotKnown.class );
		}
		catch( RegionNotKnown rnk )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RegionNotKnown.class );
		}
	}

	//////////////////////////////////////////////////////////////////
	// TEST: testAssociateRegionForUpdatesUsingUndefinedAttribute() //
	//////////////////////////////////////////////////////////////////
	@Test
	public void testAssociateRegionForUpdatesUsingUndefinedAttribute()
	{
		try
		{
			// a valid attribute, but not for ObjectRoot.A
			int wrong = defaultFederate.quickACHandle( "ObjectRoot.BestEffortTest", "blah" );

			AttributeHandleSet handleSet = defaultFederate.createAHS( aaHandle, abHandle, wrong );
			defaultFederate.rtiamb.associateRegionForUpdates( defaultRegion, theObject, handleSet );
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

	/////////////////////////////////////////////////////////////////
	// TEST: testAssociateRegionForUpdatesUsingNegativeAttribute() //
	/////////////////////////////////////////////////////////////////
	@Test
	public void testAssociateRegionForUpdatesUsingNegativeAttribute()
	{
		try
		{
			AttributeHandleSet handleSet = defaultFederate.createAHS( aaHandle, abHandle, -1 );
			defaultFederate.rtiamb.associateRegionForUpdates( defaultRegion, theObject, handleSet );
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

	////////////////////////////////////////////////////////////////
	// TEST: testAssociateRegionForUpdatesUsingNullAttributeSet() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testAssociateRegionForUpdatesUsingNullAttributeSet()
	{
		try
		{
			defaultFederate.rtiamb.associateRegionForUpdates( defaultRegion, theObject, null );
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

	////////////////////////////////////////////////////////
	// TEST: testAssociateRegionForUpdatesWhenNotJoined() //
	////////////////////////////////////////////////////////
	@Test
	public void testAssociateRegionForUpdatesWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			AttributeHandleSet handleSet = defaultFederate.createAHS( aaHandle, abHandle );
			defaultFederate.rtiamb.associateRegionForUpdates( defaultRegion, theObject, handleSet );
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
	// TEST: testAssociateRegionForUpdatesWhenSaveInProgress() //
	/////////////////////////////////////////////////////////////
	@Test
	public void testAssociateRegionForUpdatesWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			AttributeHandleSet handleSet = defaultFederate.createAHS( aaHandle, abHandle );
			defaultFederate.rtiamb.associateRegionForUpdates( defaultRegion, theObject, handleSet );
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
	// TEST: testAssociateRegionForUpdatesWhenRestoreInProgress() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testAssociateRegionForUpdatesWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			AttributeHandleSet handleSet = defaultFederate.createAHS( aaHandle, abHandle );
			defaultFederate.rtiamb.associateRegionForUpdates( defaultRegion, theObject, handleSet );
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
	//////////////////////////////// Unassociate Region Tests ////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void unassociateRegionForUpdates( Region theRegion, int theObject )
	//        throws ObjectNotKnown,
	//               InvalidRegionContext,
	//               RegionNotKnown,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	/////////////////////////////////////////////////////
	// TEST: (valid) testUnassociateRegionForUpdates() //
	/////////////////////////////////////////////////////
	@Test
	public void testUnassociateRegionForUpdates()
	{
		associateAllAttributesWithOob();
		
		// unassociate and bring attributes back into the update zone
		defaultFederate.quickUnassociateWithRegion( theObject, defaultRegionOob );
		validateInDefaultRegion( aaHandle, abHandle, acHandle );
	}

	////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testUnassociateRegionForUpdatesWithNonAssociatedRegion() //
	////////////////////////////////////////////////////////////////////////////
	/**
	 * It isn't invalid to unassociate an object instance with a region when that region isn't
	 * associated with any of the attributes. Validate that this doesn't cause an error.
	 */
	@Test
	public void testUnassociateRegionForUpdatesWithNonAssociatedRegion()
	{
		associateAllAttributesWithOob();
		
		// make sure that there isn't an error unassociating with a region we're not associated with
		defaultFederate.quickUnassociateWithRegion( theObject, defaultRegion );
	}

	///////////////////////////////////////////////////////////////
	// TEST: testUnassociateRegionForUpdatesUsingUnknownObject() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testUnassociateRegionForUpdatesUsingUnknownObject()
	{
		try
		{
			defaultFederate.rtiamb.unassociateRegionForUpdates( defaultRegion, 11111 );
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
	// TEST: testUnassociateRegionForUpdatesUsingNegativeObject() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testUnassociateRegionForUpdatesUsingNegativeObject()
	{
		try
		{
			defaultFederate.rtiamb.unassociateRegionForUpdates( defaultRegion, -1 );
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
	// TEST: testUnassociateRegionForUpdatesUsingUnknownRegion() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testUnassociateRegionForUpdatesUsingUnknownRegion()
	{
		// create a region in the listener and then try to use it for the association
		// with the defaultFederate
		Region newRegion = listener.quickCreateTestRegion( 100, 200 );
		
		try
		{
			defaultFederate.rtiamb.unassociateRegionForUpdates( newRegion, theObject );
			expectedException( RegionNotKnown.class );
		}
		catch( RegionNotKnown rnk )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RegionNotKnown.class );
		}
	}

	////////////////////////////////////////////////////////////
	// TEST: testUnassociateRegionForUpdatesUsingNullRegion() //
	////////////////////////////////////////////////////////////
	@Test
	public void testUnassociateRegionForUpdatesUsingNullRegion()
	{
		try
		{
			defaultFederate.rtiamb.unassociateRegionForUpdates( null, theObject );
			expectedException( RegionNotKnown.class );
		}
		catch( RegionNotKnown rnk )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RegionNotKnown.class );
		}
	}

	//////////////////////////////////////////////////////////
	// TEST: testUnassociateRegionForUpdatesWhenNotJoined() //
	//////////////////////////////////////////////////////////
	@Test
	public void testUnassociateRegionForUpdatesWhenNotJoined()
	{
		defaultFederate.quickResign();

		try
		{
			defaultFederate.rtiamb.unassociateRegionForUpdates( defaultRegion, theObject );
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

	///////////////////////////////////////////////////////////////
	// TEST: testUnassociateRegionForUpdatesWhenSaveInProgress() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testUnassociateRegionForUpdatesWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );

		try
		{
			defaultFederate.rtiamb.unassociateRegionForUpdates( defaultRegion, theObject );
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

	//////////////////////////////////////////////////////////////////
	// TEST: testUnassociateRegionForUpdatesWhenRestoreInProgress() //
	//////////////////////////////////////////////////////////////////
	@Test
	public void testUnassociateRegionForUpdatesWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );

		try
		{
			defaultFederate.rtiamb.unassociateRegionForUpdates( defaultRegion, theObject );
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
