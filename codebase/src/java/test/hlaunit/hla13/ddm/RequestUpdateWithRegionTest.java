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

import java.util.Set;

import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotDefined;
import hla.rti.FederateNotExecutionMember;
import hla.rti.ObjectClassNotDefined;
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

@Test(singleThreaded=true, groups={"RequestUpdateWithRegionTest", "requestUpdate", "ddm"})
public class RequestUpdateWithRegionTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate provokerFederate;
	private int aHandle;
	private int bHandle;
	private int aaHandle;
	private int abHandle;
	private int acHandle;
	private AttributeHandleSet set;
	private int firstObject;
	private int secondObject;
	private Region senderRegion;
	private Region updateRegion;
	private Region updateRegionOOB;

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
		this.provokerFederate = new Test13Federate( "provokerFederate", this );
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		provokerFederate.quickJoin();
		
		// cache the fom handles
		aHandle  = defaultFederate.quickOCHandle( "ObjectRoot.A" );
		aaHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "aa" );
		abHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ab" );
		acHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ac" );
		bHandle  = defaultFederate.quickOCHandle( "ObjectRoot.A.B" );

		// register regions and do publish and subscribe
		defaultFederate.quickPublish( aHandle, aaHandle, abHandle, acHandle );
		defaultFederate.quickPublish( bHandle, aaHandle, abHandle, acHandle );
		senderRegion = defaultFederate.quickCreateTestRegion( 100, 200 );
		updateRegion = provokerFederate.quickCreateTestRegion( 150, 250 );
		updateRegionOOB = provokerFederate.quickCreateTestRegion( 1000, 2000 );
		
		// register an instance and discover it in the other federate //
		
		firstObject = defaultFederate.quickRegisterWithRegion( aHandle,
		                                                       senderRegion,
		                                                       aaHandle,
		                                                       abHandle );
		secondObject = defaultFederate.quickRegisterWithRegion( bHandle,
		                                                        senderRegion,
		                                                        aaHandle,
		                                                        abHandle,
		                                                        acHandle );

		// set up the default handle used during udpate
		set = provokerFederate.createAHS( aaHandle, abHandle, acHandle );
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		provokerFederate.quickResign();
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
	////////////////////////////// Request Class Update Methods //////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void requestClassAttributeValueUpdateWithRegion( int theClass,
	//                                                         AttributeHandleSet theAttributes,
	//                                                         Region theRegion )
	//        throws ObjectClassNotDefined,
	//               AttributeNotDefined,
	//               RegionNotKnown,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	//////////////////////////////////////////////////////
	// TEST: (valid) testRequestClassUpdateWithRegion() //
	//////////////////////////////////////////////////////
	/**
	 * Two objects have been registered, one of type ObjectRoot.A and the other of type
	 * ObjectRoot.B. The provoker issues the request passing the class handle of ObjectRoot.B and
	 * all the handles for attributes from ObjectRoot.A.
	 * <p/>
	 * The defaultFederate should get a provide callback for the first object with only the first
	 * two attributes (as the third isn't associated with a region).
	 * <p/>
	 * The defaultFederate should get a provide callback for the second object with all three
	 * attributes.
	 */
	@Test
	public void testRequestClassUpdateWithRegion()
	{
		try
		{
			provokerFederate.rtiamb.requestClassAttributeValueUpdateWithRegion( aHandle,
			                                                                    set,
			                                                                    updateRegion );
		}
		catch( Exception e )
		{
			unexpectedException( "Requesting class attribute update with region", e );
		}
		
		///////////////////////////////////////////////////////////////
		// validate that the requests reach the registering federate //
		///////////////////////////////////////////////////////////////
		Set<Integer> attributes = defaultFederate.fedamb.waitForProvideRequest( firstObject );
		Assert.assertEquals( attributes.size(), 2, "Was expecting request to update 2 handles" );
		Assert.assertTrue( attributes.contains(aaHandle),
		                   "Update request set didn't have aaHandle" );		
		Assert.assertTrue( attributes.contains(abHandle),
		                   "Update request set didn't have aaHandle" );		
		Assert.assertFalse( attributes.contains(acHandle),
		                    "Update request had abHandle (wasn't meant to)" );

		attributes = defaultFederate.fedamb.waitForProvideRequest( secondObject );
		Assert.assertEquals( attributes.size(), 3, "Was expecting request to update 3 handles" );
		Assert.assertTrue( attributes.contains(aaHandle),
		                   "Update request set didn't have aaHandle" );		
		Assert.assertTrue( attributes.contains(abHandle),
		                   "Update request set didn't have aaHandle" );		
		Assert.assertTrue( attributes.contains(acHandle),
		                   "Update request set didn't have aaHandle" );		
	}

	///////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testRequestClassUpdateWithRegionUsingNonOverlappingRegion() //
	///////////////////////////////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWithRegionUsingNonOverlappingRegion()
	{
		try
		{
			provokerFederate.rtiamb.requestClassAttributeValueUpdateWithRegion( aHandle,
			                                                                    set,
			                                                                    updateRegionOOB );
		}
		catch( Exception e )
		{
			unexpectedException( "Requesting class attribute update with region", e );
		}
		
		// ensure the request reaches the other side
		defaultFederate.fedamb.waitForProvideRequestTimeout( firstObject );
		defaultFederate.fedamb.waitForProvideRequestTimeout( secondObject );
	}

	/////////////////////////////////////////////////////////////////
	// TEST: testRequestClassUpdateWithRegionUsingUndefinedClass() //
	/////////////////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWithRegionUsingUndefinedClass()
	{
		try
		{
			provokerFederate.rtiamb.requestClassAttributeValueUpdateWithRegion( 1000000,
			                                                                    set,
			                                                                    updateRegion );
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

	////////////////////////////////////////////////////////////////
	// TEST: testRequestClassUpdateWithRegionUsingNegativeClass() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWithRegionUsingNegativeClass()
	{
		try
		{
			provokerFederate.rtiamb.requestClassAttributeValueUpdateWithRegion( -1,
			                                                                    set,
			                                                                    updateRegion );
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
	// TEST: testRequestClassUpdateWithRegionUsingUndefinedAttribute() //
	/////////////////////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWithRegionUsingUndefinedAttribute()
	{
		AttributeHandleSet badSet = provokerFederate.createAHS( aaHandle, abHandle, 1000000 );
		
		try
		{
			provokerFederate.rtiamb.requestClassAttributeValueUpdateWithRegion( aHandle,
			                                                                    badSet,
			                                                                    updateRegion );
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
	// TEST: testRequestClassUpdateWithRegionUsingNegativeAttribute() //
	////////////////////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWithRegionUsingNegativeAttribute()
	{
		AttributeHandleSet badSet = provokerFederate.createAHS( aaHandle, abHandle, -1 );
		
		try
		{
			provokerFederate.rtiamb.requestClassAttributeValueUpdateWithRegion( aHandle,
			                                                                    badSet,
			                                                                    updateRegion );
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
	// TEST: testRequestClassUpdateWithReginUsingNullAttibuteList() //
	//////////////////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWithRegionUsingNullAttributeList()
	{
		try
		{
			provokerFederate.rtiamb.requestClassAttributeValueUpdateWithRegion( aHandle,
			                                                                    null,
			                                                                    updateRegion );
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

	////////////////////////////////////////////////////////////////
	// TEST: testRequestClassUpdateWithRegionUsingUnknownRegion() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWithRegionUsingUnknownRegion()
	{
		try
		{
			provokerFederate.rtiamb.requestClassAttributeValueUpdateWithRegion( aHandle,
			                                                                    set,
			                                                                    senderRegion );
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
	// TEST: testRequestClassUpdateWithRegionUsingNullRegion() //
	/////////////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWithRegionUsingNullRegion()
	{
		try
		{
			provokerFederate.rtiamb.requestClassAttributeValueUpdateWithRegion( aHandle, set, null );
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
	// TEST: testRequestClassUpdateWithRegionWhileNotJoined() //
	////////////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWithRegionWhileNotJoined()
	{
		provokerFederate.quickResign();

		try
		{
			provokerFederate.rtiamb.requestClassAttributeValueUpdateWithRegion( aHandle,
			                                                                    set,
			                                                                    updateRegion );
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
	// TEST: testRequestClassUpdateWithRegionWhenSaveInProgress() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWithRegionWhenSaveInProgress()
	{
		provokerFederate.quickSaveInProgress( "save" );

		try
		{
			provokerFederate.rtiamb.requestClassAttributeValueUpdateWithRegion( aHandle,
			                                                                    set,
			                                                                    updateRegion );
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
	// TEST: testRequestClassUpdateWithRegionWhenRestoreInProgress() //
	///////////////////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWithRegionWhenRestoreInProgress()
	{
		provokerFederate.quickRestoreInProgress( "save" );

		try
		{
			provokerFederate.rtiamb.requestClassAttributeValueUpdateWithRegion( aHandle,
			                                                                    set,
			                                                                    updateRegion );
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
