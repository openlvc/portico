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

import hla.rti.AttributeNotDefined;
import hla.rti.AttributeNotPublished;
import hla.rti.FederateNotExecutionMember;
import hla.rti.InvalidRegionContext;
import hla.rti.ObjectAlreadyRegistered;
import hla.rti.ObjectClassNotDefined;
import hla.rti.ObjectClassNotPublished;
import hla.rti.RTIinternalError;
import hla.rti.Region;
import hla.rti.RegionNotKnown;
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

@Test(singleThreaded=true, groups={"RegisterObjectWithRegionTest", "registerObjectWithRegion","ddm"})
public class RegisterObjectWithRegionTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate aListener; // subscribed to all the ObjectRoot.A attributes
	private Test13Federate bListener; // subscribed to all the ObjectRoot.A.B attributes
	private Test13Federate cListener; // subscribed to ObjectRoot.A.B attributes without ANY region
	
	private Region bigRegion; // covers the area of ALL the other regions in the test
	private Region aRegion;
	private Region bRegion;
	
	private int aHandle, aaHandle, abHandle, acHandle;
	private int bHandle, baHandle, bbHandle, bcHandle;
	private int spaceHandle;
	private int dimensionHandle;

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
		
		// create the listeners
		this.aListener = new Test13Federate( "aListener", this );
		this.bListener = new Test13Federate( "bListener", this );
		this.cListener = new Test13Federate( "cListener", this );
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception
	{
		// create and join the federation
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		aListener.quickJoin();
		bListener.quickJoin();
		cListener.quickJoin();
		
		// cache the handles
		aHandle  = defaultFederate.quickOCHandle( "ObjectRoot.A" );
		aaHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "aa" );
		abHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ab" );
		acHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ac" );
		bHandle  = defaultFederate.quickOCHandle( "ObjectRoot.A.B" );
		baHandle = defaultFederate.quickACHandle( "ObjectRoot.A.B", "ba" );
		bbHandle = defaultFederate.quickACHandle( "ObjectRoot.A.B", "bb" );
		bcHandle = defaultFederate.quickACHandle( "ObjectRoot.A.B", "bc" );
		spaceHandle = defaultFederate.quickSpaceHandle( "TestSpace" );
		dimensionHandle = defaultFederate.quickDimensionHandle( "TestSpace", "TestDimension" );
		
		// publish and subscribe
		defaultFederate.quickPublish( "ObjectRoot.A.B", "aa", "ab", "ac", "ba", "bb", "bc" );
		bigRegion = defaultFederate.quickCreateRegion( spaceHandle, 1 );
		bigRegion.setRangeLowerBound( 0, dimensionHandle, 0 );
		bigRegion.setRangeUpperBound( 0, dimensionHandle, 1000 );
		defaultFederate.quickModifyRegion( bigRegion );

		// federate: aListener
		aRegion = aListener.quickCreateTestRegion( 100, 200 );
		aListener.quickSubscribeWithRegion( aHandle, aRegion, aaHandle, abHandle, acHandle );
		
		// federate: bListener
		bRegion = bListener.quickCreateTestRegion( 300, 400 );
		bListener.quickSubscribeWithRegion( bHandle, bRegion, baHandle, bbHandle, bcHandle );
		
		// federate: cListener
		cListener.quickSubscribe( "ObjectRoot.A.B", "aa", "ab", "ac", "ba", "bb", "bc" );
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		cListener.quickResign();
		bListener.quickResign();
		aListener.quickResign();
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

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Register Object Tests //////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public int registerObjectInstanceWithRegion( int theClass,
	//                                              int[] theAttributes,
	//                                              Region[] theRegions )
	//        throws ObjectClassNotDefined,
	//               ObjectClassNotPublished,
	//               AttributeNotDefined,
	//               AttributeNotPublished,
	//               RegionNotKnown,
	//               InvalidRegionContext,
	//               FederateNotExecutionMember, 
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	// NOTES:
	//  When an object instance is registered, it should be discovered by all federates with
	//  a subscription interest in the associated object class. This occurs regardless of ddm
	//  considerations (based on my understanding of the specification). This means that a
	//  federate would discover an instance of an object class it was interested in, but if the
	//  attributes of that class as associated with a region that doesn't overlap with the
	//  subscribing region (for a given attribute), then the subscriber won't actually receive
	//  any updated until the regions do overlap. The important point is that the instance is
	//  discovered regardless of this fact.
	//

	//////////////////////////////////////////////////
	// TEST: (valid) testRegisterObjectWithRegion() //
	//////////////////////////////////////////////////
	/**
	 * This test will also validate that the registerObjectWithRegion call makes the proper region
	 * associations and that subsequent updates are subject to those region restrictions. This
	 * involves sending an update and making sure that the various attributes are only received by
	 * federates that have a subscription interest with region data that intersects with the region
	 * that the object was registered with (assuming that the region had not since been updated). 
	 */
	@Test
	public void testRegisterObjectWithRegion()
	{
		int objectHandle = -1;
		
		try
		{
			int[] attributes = new int[]{ aaHandle, abHandle, acHandle,
			                              baHandle, bbHandle, bcHandle };
			Region[] regions = new Region[]{ bigRegion, bigRegion, bigRegion,
			                                 bigRegion, bigRegion, bigRegion };

			objectHandle = defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                                        attributes,
			                                                                        regions );
		}
		catch( Exception e )
		{
			unexpectedException( "registering object instance with region", e );
		}
		
		// validate that both the subscribers discover the object
		aListener.fedamb.waitForDiscoveryAs( objectHandle, aHandle );
		bListener.fedamb.waitForDiscoveryAs( objectHandle, bHandle );
		cListener.fedamb.waitForDiscoveryAs( objectHandle, bHandle );
	}

	////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testRegisterObjectWithRegionDiscoveredIrrespectiveOfRegion() //
	////////////////////////////////////////////////////////////////////////////////
	/**
	 * Ensure that an object instance registered with a region is discovered by all subscribers,
	 * even if their region DOESN'T overlap with any of the regions supplied to the register.
	 */
	@Test
	public void testRegisterObjectWithRegionDiscoveredIrrespectiveOfRegion()
	{
		int objectHandle = -1;
		
		try
		{
			Region oob = defaultFederate.quickCreateTestRegion( 3000, 4000 );
			int[] theAtts = new int[]{ aaHandle, abHandle, acHandle, baHandle, bbHandle, bcHandle };
			Region[] regions = new Region[]{ oob, oob, oob, oob, oob, oob };
			
			objectHandle = defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                                        theAtts,
			                                                                        regions );
		}
		catch( Exception e )
		{
			unexpectedException( "registering object instance with region", e );
		}
		
		// make sure both listeners get the discovery regardless of region status
		aListener.fedamb.waitForDiscoveryAs( objectHandle, aHandle );
		bListener.fedamb.waitForDiscoveryAs( objectHandle, bHandle );
		cListener.fedamb.waitForDiscoveryAs( objectHandle, bHandle );
	}

	/////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testRegisterObjectWithRegionWhenArrayContainsAttributeTwice() //
	/////////////////////////////////////////////////////////////////////////////////
	/**
	 * According to the spec, if the array of attributes contains a valid handle twice,
	 * the first region is the one that should be used.
	 */
	@Test
	public void testRegisterObjectWithRegionWhenArrayContainsAttributeTwice()
	{
		int objectHandle = -1;
		try
		{
			// Register the object with region data in which an attribute appears twice.
			// Only include a single "a*" attribute, but include that attribute twice in the
			// registration data. Make sure that the first instance is associated with a region
			// that does not overlap with the aListener subscribed region, but that the second
			// time it does. The aListener federate should DISCOVER the instance, but it should
			// not receive a reflection of that attribute after the discover.
			Region oobRegion = defaultFederate.quickCreateTestRegion( 3000, 4000 );
			int[] attributes = new int[]{ aaHandle, baHandle, aaHandle };
			Region[] regions = new Region[]{ oobRegion, bigRegion, bigRegion };
			
			objectHandle = defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                                        attributes,
			                                                                        regions );
		}
		catch( Exception e )
		{
			unexpectedException( "registering object instance with region", e );
		}
		
		// validate that BOTH the subscribers discover the object
		aListener.fedamb.waitForDiscoveryAs( objectHandle, aHandle );
		bListener.fedamb.waitForDiscoveryAs( objectHandle, bHandle );
		cListener.fedamb.waitForDiscoveryAs( objectHandle, bHandle );
		
		// validate that ONLY the bListener gets a reflection (as it is the only one that knows
		// of an attribute that is associated with a region that overlaps the updating region).
		defaultFederate.quickReflect( objectHandle, aaHandle, baHandle );
		aListener.fedamb.waitForROUpdateTimeout( objectHandle );
		bListener.fedamb.waitForROUpdate( objectHandle );
		cListener.fedamb.waitForROUpdate( objectHandle );
	}

	///////////////////////////////////////////////////////////////////
	// TEST: testRegisterObjectWithRegionUsingUndefinedObjectClass() //
	///////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWithRegionUsingUndefinedObjectClass()
	{
		try
		{
			int[] handles = new int[]{ aaHandle };
			Region[] regions = new Region[]{ bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( 1111111,
			                                                         handles,
			                                                         regions );
			expectedException( ObjectClassNotDefined.class );
		}
		catch( ObjectClassNotDefined ocnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectClassNotDefined.class );
		}
	}

	//////////////////////////////////////////////////////////////////
	// TEST: testRegisterObjectWithRegionUsingNegativeObjectClass() //
	//////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWithRegionUsingNegativeObjectClass()
	{
		try
		{
			int[] handles = new int[]{ aaHandle };
			Region[] regions = new Region[]{ bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( -1,
			                                                         handles,
			                                                         regions );
			expectedException( ObjectClassNotDefined.class );
		}
		catch( ObjectClassNotDefined ocnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectClassNotDefined.class );
		}
	}

	/////////////////////////////////////////////////////////////////////
	// TEST: testRegisterObjectWithRegionUsingUnpublishedObjectClass() //
	/////////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWithRegionUsingUnpublishedObjectClass()
	{
		try
		{
			int[] handles = new int[]{ aaHandle };
			Region[] regions = new Region[]{ bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( aHandle,
			                                                         handles,
			                                                         regions );
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

	/////////////////////////////////////////////////////////////////
	// TEST: testRegisterObjectWithRegionUsingUndefinedAttribute() //
	/////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWithRegionUsingUndefinedAttribute()
	{
		try
		{
			int[] handles = new int[]{ aaHandle, 11111 };
			Region[] regions = new Region[]{ bigRegion, bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         handles,
			                                                         regions );
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
	// TEST: testRegisterObjectWithRegionUsingUnpublishedAttribute() //
	///////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWithRegionUsingUnpublishedAttribute()
	{
		// publish ObjectRoot.A with just aa and ab so that we have an attribute that
		// we are not publishing of an object class that we are publishing (by default,
		// we are publishing all the attributes with ObjectRoot.A.B)
		defaultFederate.quickPublish( aHandle, aaHandle, abHandle );
		
		try
		{
			int[] handles = new int[]{ aaHandle, acHandle };
			Region[] regions = new Region[]{ bigRegion, bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( aHandle,
			                                                         handles,
			                                                         regions );
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

	////////////////////////////////////////////////////////////////
	// TEST: testRegisterObjectWithRegionUsingNegativeAttribute() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWithRegionUsingNegativeAttribute()
	{
		try
		{
			int[] handles = new int[]{ aaHandle, -11111 };
			Region[] regions = new Region[]{ bigRegion, bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         handles,
			                                                         regions );
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
	// TEST: testRegisterObjectWithRegionUsingNullAttributeArray() //
	/////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWithRegionUsingNullAttributeArray()
	{
		try
		{
			Region[] regions = new Region[]{ bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         null,
			                                                         regions );
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

	////////////////////////////////////////////////////////////
	// TEST: testRegisterObjectWithRegionUsingUnknownRegion() //
	////////////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWithRegionUsingUnknownRegion()
	{
		try
		{
			int[] attributes = new int[]{ aaHandle };
			Region[] regions = new Region[]{ aRegion /*created by aListener*/ };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         attributes,
			                                                         regions );
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
	// TEST: testRegisterObjectWithRegionUsingInvalidRegion() //
	////////////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWithRegionUsingInvalidRegion()
	{
		// create a region for a space that isn't valid for any attributes we are using in the rego
		Region invalidRegion = defaultFederate.quickCreateRegion( "OtherSpace", 1 );

		try
		{
			int[] attributes = new int[]{ aaHandle };
			Region[] regions = new Region[]{ invalidRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         attributes,
			                                                         regions );
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

	//////////////////////////////////////////////////////////////////
	// TEST: testRegisterObjectWithRegionWhenNoRegionDefinedInFOM() //
	//////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWithRegionWhenNoRegionDefinedInFOM()
	{
		// publish the ObjectRoot.BestEffortTest object class as it's attribute "blah" has
		// no space associated with it (so we can use it for this test)
		int classHandle = defaultFederate.quickOCHandle( "ObjectRoot.BestEffortTest" );
		int attributeHandle = defaultFederate.quickACHandle( "ObjectRoot.BestEffortTest", "blah" );
		defaultFederate.quickPublish( classHandle, attributeHandle );
		
		try
		{
			int[] attributes = new int[]{ attributeHandle };
			Region[] regions = new Region[]{ bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( classHandle,
			                                                         attributes,
			                                                         regions );
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

	/////////////////////////////////////////////////////////
	// TEST: testRegisterObjectWithRegionUsingNullRegion() //
	/////////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWithRegionUsingNullRegion()
	{
		try
		{
			int[] attributes = new int[]{ aaHandle };
			Region[] regions = new Region[]{ null };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         attributes,
			                                                         regions );
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

	//////////////////////////////////////////////////////////////
	// TEST: testRegisterObjectWithRegionUsingNullRegionArray() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWithRegionUsingNullRegionArray()
	{
		try
		{
			int[] handles = new int[]{ aaHandle };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         handles,
			                                                         null );
			expectedException( RegionNotKnown.class, RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// success!
		}
		catch( RegionNotKnown rnk )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RegionNotKnown.class, RTIinternalError.class );
		}
	}

	///////////////////////////////////////////////////////
	// TEST: testRegisterObjectWithRegionWhenNotJoined() //
	///////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWithRegionWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			int[] handles = new int[]{ aaHandle };
			Region[] regions = new Region[]{ bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         handles,
			                                                         regions );
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
	// TEST: testRegisterObjectWithRegionWhenSaveInProgress() //
	////////////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWithRegionWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			int[] handles = new int[]{ aaHandle };
			Region[] regions = new Region[]{ bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         handles,
			                                                         regions );
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
	// TEST: testRegisterObjectWithRegionWhenRestoreInProgress() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWithRegionWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			int[] handles = new int[]{ aaHandle };
			Region[] regions = new Region[]{ bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         handles,
			                                                         regions );
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
	//////////////////////////// Register Object With Name Tests /////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public int registerObjectInstanceWithRegion( int theClass,
	//                                              String theObject,
	//                                              int[] theAttributes,
	//                                              Region[] theRegions )
	//        throws ObjectClassNotDefined,
	//               ObjectClassNotPublished,
	//               AttributeNotDefined,
	//               AttributeNotPublished,
	//               RegionNotKnown,
	//               InvalidRegionContext,
	//               ObjectAlreadyRegistered,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	///////////////////////////////////////////////////////
	// TEST: (valid) testRegisterNamedObjectWithRegion() //
	///////////////////////////////////////////////////////
	/**
	 * Same as {@link #testRegisterObjectWithRegion()} except that it also validates that the
	 * name provided to the registration request is the same as that which is discovered by the
	 * listener federates. 
	 */
	@Test
	public void testRegisterNamedObjectWithRegion()
	{
		int objectHandle = -1;
		String name = "ohhai";
		
		try
		{
			int[] attributes = new int[]{ aaHandle, abHandle, acHandle,
			                              baHandle, bbHandle, bcHandle };
			Region[] regions = new Region[]{ bigRegion, bigRegion, bigRegion,
			                                 bigRegion, bigRegion, bigRegion };

			objectHandle = defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                                        name,
			                                                                        attributes,
			                                                                        regions );
			// make sure we got the name
			Assert.assertEquals( defaultFederate.rtiamb.getObjectInstanceName(objectHandle), name );
		}
		catch( Exception e )
		{
			unexpectedException( "registering object instance with region", e );
		}

		// validate that both the subscribers discover the object
		aListener.fedamb.waitForDiscoveryAs( objectHandle, aHandle );
		Assert.assertEquals( aListener.fedamb.getInstances().get(objectHandle).getName(), name );		
		bListener.fedamb.waitForDiscoveryAs( objectHandle, bHandle );
		Assert.assertEquals( bListener.fedamb.getInstances().get(objectHandle).getName(), name );
		cListener.fedamb.waitForDiscoveryAs( objectHandle, bHandle );
		Assert.assertEquals( cListener.fedamb.getInstances().get(objectHandle).getName(), name );
	}

	////////////////////////////////////////////////////////////////////
	// TEST: (valid) testRegisterNamedObjectWithRegionUsingNullName() //
	////////////////////////////////////////////////////////////////////
	/**
	 * This should be valid and should be the same as registering the instance without providing
	 * the object name (so the instance name should be RTI assigned).
	 */
	@Test
	public void testRegisterNamedObjectWithRegionUsingNullName()
	{
		int objectHandle = -1;
		String name = null;

		try
		{
			int[] attributes = new int[]{ aaHandle, abHandle, acHandle,
			                              baHandle, bbHandle, bcHandle };
			Region[] regions = new Region[]{ bigRegion, bigRegion, bigRegion,
			                                 bigRegion, bigRegion, bigRegion };

			objectHandle = defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                                        null,
			                                                                        attributes,
			                                                                        regions );
			// fetch the expected name
			name = defaultFederate.rtiamb.getObjectInstanceName( objectHandle );
		}
		catch( Exception e )
		{
			unexpectedException( "registering object instance with region", e );
		}
		
		// validate that both the subscribers discover the object
		aListener.fedamb.waitForDiscoveryAs( objectHandle, aHandle );
		Assert.assertEquals( aListener.fedamb.getInstances().get(objectHandle).getName(), name );		
		bListener.fedamb.waitForDiscoveryAs( objectHandle, bHandle );
		Assert.assertEquals( bListener.fedamb.getInstances().get(objectHandle).getName(), name );
		cListener.fedamb.waitForDiscoveryAs( objectHandle, bHandle );
		Assert.assertEquals( cListener.fedamb.getInstances().get(objectHandle).getName(), name );
	}

	//////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testRegisterNamedObjectWithRegionUsingWhitespaceName() //
	//////////////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionUsingWhitespaceName()
	{
		int objectHandle = -1;
		String name = "    ";
		try
		{
			int[] attributes = new int[]{ aaHandle, abHandle, acHandle,
			                              baHandle, bbHandle, bcHandle };
			Region[] regions = new Region[]{ bigRegion, bigRegion, bigRegion,
			                                 bigRegion, bigRegion, bigRegion };

			objectHandle = defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                                        name,
			                                                                        attributes,
			                                                                        regions );
		}
		catch( Exception e )
		{
			unexpectedException( "registering object with region and empty name", e );
		}
		
		// validate that both the subscribers discover the object with the right name
		aListener.fedamb.waitForDiscoveryAs( objectHandle, aHandle );
		Assert.assertEquals( aListener.fedamb.getInstances().get(objectHandle).getName(), name );		
		bListener.fedamb.waitForDiscoveryAs( objectHandle, bHandle );
		Assert.assertEquals( bListener.fedamb.getInstances().get(objectHandle).getName(), name );
		cListener.fedamb.waitForDiscoveryAs( objectHandle, bHandle );
		Assert.assertEquals( cListener.fedamb.getInstances().get(objectHandle).getName(), name );
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testRegisterNamedObjectWithRegionDiscoveredIrrespectiveOfRegion() //
	/////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The same as {@link #testRegisterObjectWithRegionDiscoveredIrrespectiveOfRegion()} except
	 * that it provides a name to the registration request and validates that this name is used
	 * and passed to the discovering federates.
	 */
	@Test
	public void testRegisterNamedObjectWithRegionDiscoveredIrrespectiveOfRegion()
	{
		int objectHandle = -1;
		String name = "ohhai";
		
		try
		{
			Region oob = defaultFederate.quickCreateTestRegion( 3000, 4000 );
			int[] theAtts = new int[]{ aaHandle, abHandle, acHandle, baHandle, bbHandle, bcHandle };
			Region[] regions = new Region[]{ oob, oob, oob, oob, oob, oob };
			
			objectHandle = defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                                        name,
			                                                                        theAtts,
			                                                                        regions );
			// make sure we got the name
			Assert.assertEquals( defaultFederate.rtiamb.getObjectInstanceName(objectHandle), name );
		}
		catch( Exception e )
		{
			unexpectedException( "registering object instance with region", e );
		}
		
		// validate that both the subscribers discover the object
		aListener.fedamb.waitForDiscoveryAs( objectHandle, aHandle );
		Assert.assertEquals( aListener.fedamb.getInstances().get(objectHandle).getName(), name );		
		bListener.fedamb.waitForDiscoveryAs( objectHandle, bHandle );
		Assert.assertEquals( bListener.fedamb.getInstances().get(objectHandle).getName(), name );
		cListener.fedamb.waitForDiscoveryAs( objectHandle, bHandle );
		Assert.assertEquals( cListener.fedamb.getInstances().get(objectHandle).getName(), name );
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testRegisterNamedObjectWithRegionWhenArrayContainsAttributeTwice() //
	//////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The same as {@link #testRegisterObjectWithRegionWhenArrayContainsAttributeTwice()} except
	 * that it provides a name to the registration request and validates that this name is used
	 * and passed to the discovering federates.
	 */
	@Test
	public void testRegisterNamedObjectWithRegionWhenArrayContainsAttributeTwice()
	{
		int objectHandle = -1;
		String name = "ohhai";

		try
		{
			//
			// See note in testRegisterObjectWithRegionWhenArrayContainsAttributeTwice
			//
			Region oobRegion = defaultFederate.quickCreateTestRegion( 3000, 4000 );
			int[] attributes = new int[]{ aaHandle, baHandle, aaHandle };
			Region[] regions = new Region[]{ oobRegion, bigRegion, bigRegion };
			
			objectHandle = defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                                        name,
			                                                                        attributes,
			                                                                        regions );
		}
		catch( Exception e )
		{
			unexpectedException( "registering object instance with region", e );
		}
		
		// validate that both the subscribers discover the object
		aListener.fedamb.waitForDiscoveryAs( objectHandle, aHandle );
		Assert.assertEquals( aListener.fedamb.getInstances().get(objectHandle).getName(), name );		
		bListener.fedamb.waitForDiscoveryAs( objectHandle, bHandle );
		Assert.assertEquals( bListener.fedamb.getInstances().get(objectHandle).getName(), name );
		cListener.fedamb.waitForDiscoveryAs( objectHandle, bHandle );
		Assert.assertEquals( cListener.fedamb.getInstances().get(objectHandle).getName(), name );
		
		// validate that ONLY the bListener gets a reflection (as it is the only one that knows
		// of an attribute that is associated with a region that overlaps the updating region).
		defaultFederate.quickReflect( objectHandle, aaHandle, baHandle );
		aListener.fedamb.waitForROUpdateTimeout( objectHandle );
		bListener.fedamb.waitForROUpdate( objectHandle );
		cListener.fedamb.waitForROUpdate( objectHandle );
	}

	////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithRegionUsingExistingName() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionUsingExistingName()
	{
		// register an instance so we can try to register a new one with the same name
		int firstHandle = defaultFederate.quickRegister( bHandle );
		String usedName = defaultFederate.quickObjectName( firstHandle );
		
		try
		{
			int[] handles = new int[]{ aaHandle };
			Region[] regions = new Region[]{ bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         usedName,
			                                                         handles,
			                                                         regions );
			expectedException( ObjectAlreadyRegistered.class );
		}
		catch( ObjectAlreadyRegistered oar )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectAlreadyRegistered.class );
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithRegionUsingExistingNameFromDifferentFederate() //
	/////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionUsingExistingNameFromDifferentFederate()
	{
		// register an instance so we can try to register a new one with the same name
		int firstHandle = defaultFederate.quickRegister( bHandle );
		String usedName = defaultFederate.quickObjectName( firstHandle );
		
		// make sure the other federate has the right publication interest
		aListener.quickPublish( "ObjectRoot.A", "aa", "ab", "ac" );
		quickSleep();
		aListener.quickTick( 0.1, 1.0 );
		
		try
		{
			int[] handles = new int[]{ aaHandle };
			Region[] regions = new Region[]{ aRegion };
			aListener.rtiamb.registerObjectInstanceWithRegion( aHandle, usedName, handles, regions);
			expectedException( ObjectAlreadyRegistered.class );
		}
		catch( ObjectAlreadyRegistered oar )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectAlreadyRegistered.class );
		}
	}

	////////////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithRegionUsingUndefinedObjectClass() //
	////////////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionUsingUndefinedObjectClass()
	{
		try
		{
			int[] handles = new int[]{ aaHandle };
			Region[] regions = new Region[]{ bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( 1111111,
			                                                         "someObject",
			                                                         handles,
			                                                         regions );
			expectedException( ObjectClassNotDefined.class );
		}
		catch( ObjectClassNotDefined ocnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectClassNotDefined.class );
		}
	}

	///////////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithRegionUsingNegativeObjectClass() //
	///////////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionUsingNegativeObjectClass()
	{
		try
		{
			int[] handles = new int[]{ aaHandle };
			Region[] regions = new Region[]{ bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( -1,
			                                                         "someObject",
			                                                         handles,
			                                                         regions );
			expectedException( ObjectClassNotDefined.class );
		}
		catch( ObjectClassNotDefined ocnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectClassNotDefined.class );
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithRegionUsingUnpublishedObjectClass() //
	//////////////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionUsingUnpublishedObjectClass()
	{
		try
		{
			int[] handles = new int[]{ aaHandle };
			Region[] regions = new Region[]{ bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( aHandle,
			                                                         "someObject",
			                                                         handles,
			                                                         regions );
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

	//////////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithRegionUsingUndefinedAttribute() //
	//////////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionUsingUndefinedAttribute()
	{
		try
		{
			int[] handles = new int[]{ aaHandle, 11111 };
			Region[] regions = new Region[]{ bigRegion, bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         "someObject",
			                                                         handles,
			                                                         regions );
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

	////////////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithRegionUsingUnpublishedAttribute() //
	////////////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionUsingUnpublishedAttribute()
	{
		// publish ObjectRoot.A with just aa and ab so that we have an attribute that
		// we are not publishing of an object class that we are publishing (by default,
		// we are publishing all the attributes with ObjectRoot.A.B)
		defaultFederate.quickPublish( aHandle, aaHandle, abHandle );
		
		try
		{
			int[] handles = new int[]{ aaHandle, acHandle };
			Region[] regions = new Region[]{ bigRegion, bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( aHandle,
			                                                         "someObject",
			                                                         handles,
			                                                         regions );
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

	/////////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithRegionUsingNegativeAttribute() //
	/////////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionUsingNegativeAttribute()
	{
		try
		{
			int[] handles = new int[]{ aaHandle, -11111 };
			Region[] regions = new Region[]{ bigRegion, bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         "someObject",
			                                                         handles,
			                                                         regions );
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

	//////////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithRegionUsingNullAttributeArray() //
	//////////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionUsingNullAttributeArray()
	{
		try
		{
			Region[] regions = new Region[]{ bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         "someObject",
			                                                         null,
			                                                         regions );
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

	/////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithRegionUsingUnknownRegion() //
	/////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionUsingUnknownRegion()
	{
		try
		{
			int[] attributes = new int[]{ aaHandle };
			Region[] regions = new Region[]{ aRegion /*created by aListener*/ };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         "someObject",
			                                                         attributes,
			                                                         regions );
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

	/////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithRegionUsingInvalidRegion() //
	/////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionUsingInvalidRegion()
	{
		// create a region for a space that isn't valid for any attributes we are using in the rego
		Region invalidRegion = defaultFederate.quickCreateRegion( "OtherSpace", 1 );

		try
		{
			int[] attributes = new int[]{ aaHandle };
			Region[] regions = new Region[]{ invalidRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         "someObject",
			                                                         attributes,
			                                                         regions );
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

	/////////////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithRegionWhenNoRegionDefinedInFOM() //
	/////////////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionWhenNoRegionDefinedInFOM()	
	{
		// publish the ObjectRoot.BestEffortTest object class as it's attribute "blah" has
		// no space associated with it (so we can use it for this test)
		int classHandle = defaultFederate.quickOCHandle( "ObjectRoot.BestEffortTest" );
		int attributeHandle = defaultFederate.quickACHandle( "ObjectRoot.BestEffortTest", "blah" );
		defaultFederate.quickPublish( classHandle, attributeHandle );
		
		try
		{
			int[] attributes = new int[]{ attributeHandle };
			Region[] regions = new Region[]{ bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( classHandle,
			                                                         "someObject",
			                                                         attributes,
			                                                         regions );
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

	//////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithRegionUsingNullRegion() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionUsingNullRegion()
	{
		try
		{
			int[] attributes = new int[]{ aaHandle };
			Region[] regions = new Region[]{ null };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         "someObject",
			                                                         attributes,
			                                                         regions );
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

	///////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithRegionUsingNullRegionArray() //
	///////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionUsingNullRegionArray()
	{
		try
		{
			int[] handles = new int[]{ aaHandle };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         "someObject",
			                                                         handles,
			                                                         null );
			expectedException( RegionNotKnown.class, RTIinternalError.class );
		}
		catch( RTIinternalError rtie )
		{
			// success!
		}
		catch( RegionNotKnown rnk )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RegionNotKnown.class, RTIinternalError.class );
		}
	}

	////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithRegionWhenNotJoined() //
	////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			int[] handles = new int[]{ aaHandle };
			Region[] regions = new Region[]{ bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         "someObject",
			                                                         handles,
			                                                         regions );
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

	/////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithRegionWhenSaveInProgress() //
	/////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			int[] handles = new int[]{ aaHandle };
			Region[] regions = new Region[]{ bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         "someObject",
			                                                         handles,
			                                                         regions );
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

	////////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithRegionWhenRestoreInProgress() //
	////////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithRegionWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			int[] handles = new int[]{ aaHandle };
			Region[] regions = new Region[]{ bigRegion };
			defaultFederate.rtiamb.registerObjectInstanceWithRegion( bHandle,
			                                                         "someObject",
			                                                         handles,
			                                                         regions );
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
