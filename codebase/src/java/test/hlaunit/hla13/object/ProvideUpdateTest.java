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

import java.util.Set;

import hla.rti.AttributeHandleSet;
import hla.rti.AttributeNotDefined;
import hla.rti.FederateNotExecutionMember;
import hla.rti.ObjectClassNotDefined;
import hla.rti.ObjectNotKnown;
import hla.rti.RTIinternalError;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hlaunit.hla13.TestSetup;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"ProvideUpdateTest", "provideUpdate", "objectManagement"})
public class ProvideUpdateTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int aHandle, aaHandle, abHandle, acHandle;
	private int bHandle, baHandle, bbHandle;

	private Test13Federate secondFederate;

	private int oHandle;
	private AttributeHandleSet set;
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
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
		this.secondFederate.quickJoin();
		
		// get the FOM handles //
		aHandle  = defaultFederate.quickOCHandle( "ObjectRoot.A" );
		aaHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "aa" );
		abHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ab" );
		acHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ac" );
		bHandle  = defaultFederate.quickOCHandle( "ObjectRoot.A.B" );
		baHandle = defaultFederate.quickACHandle( "ObjectRoot.A.B", "ba" );
		bbHandle = defaultFederate.quickACHandle( "ObjectRoot.A.B", "bb" );

		// do the publish and subscribe //
		defaultFederate.quickPublish( bHandle, aaHandle, abHandle, baHandle, bbHandle );
		secondFederate.quickSubscribe( aHandle, aaHandle, abHandle, acHandle );
		
		// set up the default handle set
		set = TestSetup.getRTIFactory().createAttributeHandleSet();
		try
		{
			set.add( aaHandle );
			set.add( abHandle );
			set.add( acHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during AttributeHandleSet initialization", e );
		}
		
		// register an instance and discover it in the other federate //
		oHandle = defaultFederate.quickRegister( bHandle );
		secondFederate.fedamb.waitForDiscovery( oHandle );
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		defaultFederate.quickResign();
		secondFederate.quickResign();
		defaultFederate.quickDestroy();
	}
	
	@Override
	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}
	

	//////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Object Test Methods //////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void requestObjectAttributeValueUpdate( int theObject,
	//                                                AttributeHandleSet theAttributes )
	//        throws ObjectNotKnown,
	//               AttributeNotDefined,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	////////////////////////////////////////////////
	// TEST: (valid) testRequestAttributeUpdate() //
	////////////////////////////////////////////////
	@Test
	public void testRequestAttributeUpdate()
	{
		try
		{
			secondFederate.rtiamb.requestObjectAttributeValueUpdate( oHandle, set );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while issuing valid instance att update request: " +
			             e.getMessage(), e );
		}
		
		// ensure that the request reaches the other side //
		Set<Integer> attributes = defaultFederate.fedamb.waitForProvideRequest( oHandle );
		Assert.assertEquals( attributes.size(), 2, "Was expecting request to update 2 handles" );
		Assert.assertTrue( attributes.contains(aaHandle),
		                   "Update request set didn't have aaHandle" );		
		Assert.assertTrue( attributes.contains(abHandle),
		                   "Update request set didn't have aaHandle" );		
		Assert.assertFalse( attributes.contains(acHandle),
		                    "Update request had abHandle (wasn't meant to)" );
	}

	//////////////////////////////////////////////////////////////////
	// TEST: (valid) testRequestAttributeUpdateForOwnedAttributes() //
	//////////////////////////////////////////////////////////////////
	/**
	 * A federate is allowed to ask for an update of attributes that it owns, but it is taken as
	 * implicit that the federate knows about its own attributes, and thus, not provide request
	 * should be issued for those attributes owned by the requesting federate
	 */
	@Test
	public void testRequestAttributeUpdateForOwnedAttributes()
	{
		// request an update through the default federate. as it owns the attributes
		// it will be requesting the update for, it shouldn't receive the provide request
		try
		{
			defaultFederate.rtiamb.requestObjectAttributeValueUpdate( oHandle, set );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while issuing valid instance att update request: " +
			             e.getMessage(), e );
		}
		
		// make sure the request doesn't come through
		defaultFederate.fedamb.waitForProvideRequestTimeout( oHandle );
	}
	
	///////////////////////////////////////////////////////////////////
	// TEST: testRequestAttributeUpdateWithNonExistentObjectHandle() //
	///////////////////////////////////////////////////////////////////
	@Test
	public void testRequestAttributeUpdateWithNonExistentObjectHandle()
	{
		try
		{
			secondFederate.rtiamb.requestObjectAttributeValueUpdate( 111111, set );
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
	// TEST: testRequestAttributeUpdateWithUndiscoveredObject() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testRequestAttributeUpdateWithUndiscoveredObject()
	{
		// put a third federate into the federation that doesn't subscribe and
		// won't get the discovery of the object, then request an update of the object
		Test13Federate thirdFederate = new Test13Federate( "thirdFederate", this );
		thirdFederate.quickJoin();
		
		try
		{
			thirdFederate.rtiamb.requestObjectAttributeValueUpdate( oHandle, set );
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
			thirdFederate.quickResign();
		}
	}
	
	//////////////////////////////////////////////////////////////////////
	// TEST: testRequestAttributeUpdateWithNonExistentAttributeHandle() //
	//////////////////////////////////////////////////////////////////////
	@Test//(expectedExceptions={AttributeNotDefined.class})
	public void testRequestAttributeUpdateWithNonExistentAttributeHandle() throws Exception
	{
		// add a non-existent handle to the set (this won't exception in Portico)
		try
		{
			set.add(11111);
		}
		catch( AttributeNotDefined and )
		{
			log( "testRequestAttributeUpdateWithNonExistentAttributeHandle not run: " +
			     "AHS gave error (acceptable)" );
		}

		try
		{
			// request with set containing wrong value
			secondFederate.rtiamb.requestObjectAttributeValueUpdate( oHandle, set );
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
	// TEST: testRequestAttributeUpdateWithHandleFromDifferentClass() //
	////////////////////////////////////////////////////////////////////
	@Test
	public void testRequestAttributeUpdateWithHandleFromDifferentClass()
	{
		// register an instance of OR.A
		defaultFederate.quickPublish( "ObjectRoot.A", "aa" );
		int theHandle = defaultFederate.quickRegister( "ObjectRoot.A" );
		secondFederate.fedamb.waitForDiscovery( theHandle );

		// add a non-existent handle to the set (this won't exception in Portico)
		try
		{
			set.add( baHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception setting up AHS", e );
		}

		try
		{
			// request for instance of OR.A with set containing attribute from OR.A.B
			secondFederate.rtiamb.requestObjectAttributeValueUpdate( theHandle, set );
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
	// TEST: testRequestAttributeUpdateWithEmptyAttributeHandleSet() //
	///////////////////////////////////////////////////////////////////
	@Test
	public void testRequestAttributeUpdateWithEmptyAttributeHandleSet()
	{
		// empty the set
		set.empty();
		
		try
		{
			// request with set containing wrong value
			secondFederate.rtiamb.requestObjectAttributeValueUpdate( oHandle, set );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected Exception while requesting class update with empty set", e );
		}
		
		// make sure no provide is issued (as there are no relevant attributes to update)
		defaultFederate.fedamb.waitForProvideRequestTimeout( oHandle );
	}
	
	//////////////////////////////////////////////////////////////////
	// TEST: testRequestAttributeUpdateWithNullAttributeHandleSet() //
	//////////////////////////////////////////////////////////////////
	@Test
	public void testRequestAttributeUpdateWithNullAttributeHandleSet()
	{
		try
		{
			// request with set containing wrong value
			secondFederate.rtiamb.requestObjectAttributeValueUpdate( oHandle, null );
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

	/////////////////////////////////////////////////////
	// TEST: testRequestAttributeUpdateWhenNotJoined() //
	/////////////////////////////////////////////////////
	@Test
	public void testRequestAttributeUpdateWhenNotJoined()
	{
		secondFederate.quickResign();

		try
		{
			secondFederate.rtiamb.requestObjectAttributeValueUpdate( oHandle, set );
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

	//////////////////////////////////////////////////////////
	// TEST: testRequestAttributeUpdateWhenSaveInProgress() //
	//////////////////////////////////////////////////////////
	@Test
	public void testRequestAttributeUpdateWhenSaveInProgress()
	{
		secondFederate.quickSaveInProgress( "save" );

		try
		{
			secondFederate.rtiamb.requestObjectAttributeValueUpdate( oHandle, set );
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

	/////////////////////////////////////////////////////////////
	// TEST: testRequestAttributeUpdateWhenRestoreInProgress() //
	/////////////////////////////////////////////////////////////
	@Test
	public void testRequestAttributeUpdateWhenRestoreInProgress()
	{
		secondFederate.quickRestoreInProgress( "save" );

		try
		{
			secondFederate.rtiamb.requestObjectAttributeValueUpdate( oHandle, set );
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
	/////////////////////////////////// Class Test Methods ///////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void requestClassAttributeValueUpdate( int theClass,
	//                                               AttributeHandleSet theAttributes )
	//        throws ObjectClassNotDefined,
	//               AttributeNotDefined,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	////////////////////////////////////////////
	// TEST: (valid) testRequestClassUpdate() //
	////////////////////////////////////////////
	@Test
	public void testRequestClassUpdate()
	{
		// request an update for the "a" attributes, but from the "b" class handle
		try
		{
			// request the update
			secondFederate.rtiamb.requestClassAttributeValueUpdate( bHandle, set );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while issuing class update request", e );
		}
		
		// wait for the update request to be received by the owning federate //
		Set<Integer> attributes = defaultFederate.fedamb.waitForProvideRequest( oHandle );
		Assert.assertEquals( attributes.size(), 2, "Was expecting request to update 2 handles" );
		Assert.assertTrue( attributes.contains(aaHandle),
		                   "Update request set didn't have aaHandle" );		
		Assert.assertTrue( attributes.contains(abHandle),
		                   "Update request set didn't have aaHandle" );		
		Assert.assertFalse( attributes.contains(acHandle),
		                    "Update request had abHandle (wasn't meant to)" );
	}

	//////////////////////////////////////////////////////////////
	// TEST: (valid) testRequestClassUpdateForOwnedAttributes() //
	//////////////////////////////////////////////////////////////
	/**
	 * A federate is allowed to ask for an update of attributes of a class for which there exist
	 * attribute instances that it owns. However, it is taken as implicit that the federate knows
	 * about these attributes, and thus, no callback should be issued for those attributes which
	 * are owned by the issuing federate.
	 */
	@Test
	public void testRequestClassUpdateForOwnedAttributes()
	{
		// request an update through the default federate. as it owns the attributes
		// it will be requesting the update for, it shouldn't receive the provide request
		try
		{
			defaultFederate.rtiamb.requestClassAttributeValueUpdate( bHandle, set );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while issuing valid instance att update request: " +
			             e.getMessage(), e );
		}
		
		// make sure the request doesn't come through
		defaultFederate.fedamb.waitForProvideRequestTimeout( oHandle );
	}

	////////////////////////////////////////////////////////////////
	// TEST: (valid) testRequestClassUpdateForParentObjectClass() //
	////////////////////////////////////////////////////////////////
	/**
	 * By requesting an update for various attributes of all instances of the class ObjectRoot.A,
	 * any federates owning corresponding attributes in instances that are of a child type should
	 * receive a "provide" request (in this case, instances of ObjectRoot.A.B).
	 */
	@Test
	public void testRequestClassUpdateForParentObjectClass()
	{
		try
		{
			secondFederate.rtiamb.requestClassAttributeValueUpdate( aHandle, set );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception issuing class update request for parent object class", e );
		}
		
		// make sure the request doesn't come through
		defaultFederate.fedamb.waitForProvideRequest( oHandle );
	}

	//////////////////////////////////////////////////////////////
	// TEST: testRequestClassUpdateWithNonExistentClassHandle() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWithNonExistentClassHandle()
	{
		try
		{
			secondFederate.rtiamb.requestClassAttributeValueUpdate( 111111, set );
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
	// TEST: testRequestClassUpdateWithNonExistentAttributeHandle() //
	//////////////////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWithNonExistentAttributeHandle()
	{
		// add a non-existent handle to the set (this won't exception in Portico)
		try
		{
			set.add(11111);
		}
		catch( AttributeNotDefined and )
		{
			log( "testRequestAttributeUpdateWithNonExistentAttributeHandle not run: " +
			     "AHS gave error (acceptable)" );
		}

		try
		{
			// request with set containing wrong value
			secondFederate.rtiamb.requestClassAttributeValueUpdate( bHandle, set );
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

	/////////////////////////////////////////////////////////////////////////
	// TEST: testRequestClassUpdateWithAttributeHandleFromDifferentClass() //
	/////////////////////////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWithAttributeHandleFromDifferentClass()
	{
		// add a non-existent handle to the set (this won't exception in Portico)
		try
		{
			set.add( baHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception setting up AHS", e );
		}

		try
		{
			// request for instance of OR.A with set containing attribute from OR.A.B
			secondFederate.rtiamb.requestClassAttributeValueUpdate( aHandle, set );
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
	// TEST: testRequestClassUpdateWithEmptyAttributeHandleSet() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWithEmptyAttributeHandleSet()
	{
		// empty the set
		set.empty();
		
		try
		{
			// request with set containing wrong value
			secondFederate.rtiamb.requestClassAttributeValueUpdate( bHandle, set );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected Exception while requesting class update with empty set", e );
		}
		
		// make sure no provide is issued (as there are no relevant attributes to update)
		defaultFederate.fedamb.waitForProvideRequestTimeout( oHandle );
	}
	
	//////////////////////////////////////////////////////////////
	// TEST: testRequestClassUpdateWithNullAttributeHandleSet() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWithNullAttributeHandleSet()
	{
		try
		{
			// request with set containing wrong value
			secondFederate.rtiamb.requestClassAttributeValueUpdate( aHandle, null );
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

	/////////////////////////////////////////////////
	// TEST: testRequestClassUpdateWhenNotJoined() //
	/////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWhenNotJoined()
	{
		secondFederate.quickResign();

		try
		{
			// request the update
			secondFederate.rtiamb.requestClassAttributeValueUpdate( bHandle, set );
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

	//////////////////////////////////////////////////////
	// TEST: testRequestClassUpdateWhenSaveInProgress() //
	//////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWhenSaveInProgress()
	{
		secondFederate.quickSaveInProgress( "save" );

		try
		{
			// request the update
			secondFederate.rtiamb.requestClassAttributeValueUpdate( bHandle, set );
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

	/////////////////////////////////////////////////////////
	// TEST: testRequestClassUpdateWhenRestoreInProgress() //
	/////////////////////////////////////////////////////////
	@Test
	public void testRequestClassUpdateWhenRestoreInProgress()
	{
		secondFederate.quickRestoreInProgress( "save" );

		try
		{
			// request the update
			secondFederate.rtiamb.requestClassAttributeValueUpdate( bHandle, set );
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
