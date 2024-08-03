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
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.exceptions.FederateOwnsAttributes;
import hla.rti1516e.exceptions.ObjectInstanceNotKnown;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;
import hlaunit.ieee1516e.common.TypeFactory;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"LocalDeleteTest", "localDelete", "objectManagement"})
public class LocalDeleteTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate secondFederate;

	private int oHandle;
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
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		// create a federation and join it
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		
		// do the publish and subscribe
		defaultFederate.quickSubscribe( "ObjectRoot.A", "aa", "ab" );
		
		// create the second federate
		secondFederate = new TestFederate( "secondFederate", this );
		secondFederate.quickJoin();
		secondFederate.quickPublish( "ObjectRoot.A.B", "aa", "ab", "ba", "bb" );
		
		// rgister an object to locally delete
		oHandle = secondFederate.quickRegister( "ObjectRoot.A.B" );
		defaultFederate.fedamb.waitForDiscovery( oHandle );
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		// resign from and destroy the federation
		secondFederate.quickResign();
		defaultFederate.quickResign();
		defaultFederate.quickDestroy();
	}
	
	@Override
	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////
	// TEST: testLocalDeleteObjectInstance() //
	///////////////////////////////////////////
	/**
	 * This method will create a second test federate and have it register an instance. It will
	 * then wait for this instance to be discovered by the defaultFederate. Following this, the
	 * defaultFederate will attempt to delete the instance, and then request an update for it.
	 * This should cause an exception as the federate will no longer know about the instance.
	 */
	@Test
	public void testLocalDeleteObjectInstance()
	{
		// remove the subscription first so that we don't rediscover the object
		defaultFederate.quickUnsubscribe( "ObjectRoot.A" );
		
		// try the local delete //
		try
		{
			defaultFederate.rtiamb.localDeleteObjectInstance( getObjectHandle(oHandle) );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while attempting a local delete", e );
		}
		
		// no need to wait for the delete, there isn't one for local deletes
		
		// try and request an update for the instance //
		try
		{
			AttributeHandleSet set = TypeFactory.newAttributeSet();
			set.add( getAttributeHandle(defaultFederate.quickACHandle("ObjectRoot.A", "aa")) );
			defaultFederate.rtiamb.requestAttributeValueUpdate( getObjectHandle(oHandle),
			                                                    set,
			                                                    "tag".getBytes() );
			expectedException( ObjectInstanceNotKnown.class );
		}
		catch( ObjectInstanceNotKnown oink )
		{
			// Success!
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while testing local delete (requesting " +
			             "update of locally deleted instance)", e );
		}
	}


	//////////////////////////////////////////////////////
	// TEST: testLocalDeleteWithRediscoveryAtSameType() //
	//////////////////////////////////////////////////////
	/**
	 * This method will create a second test federate and have it register an instance. It will
	 * then wait for this instance to be discovered by the defaultFederate. Following this, the
	 * defaultFederate will attempt to locally delete the instance. The defaultFederate will still
	 * have the same subscription interests, and so it should receive a (re)discovery for the
	 * object.
	 */
	@Test
	public void testLocalDeleteWithRediscoveryAtSameType()
	{
		// try the local delete //
		try
		{
			defaultFederate.rtiamb.localDeleteObjectInstance( getObjectHandle(oHandle) );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while attempting a local delete", e );
		}
		
		// no need to wait for the delete, there isn't one for local deletes
		// wait for the object to be rediscovered (should be at the same type)
		int expectedType = defaultFederate.quickOCHandle( "ObjectRoot.A" );
		defaultFederate.fedamb.waitForDiscoveryAs( oHandle, expectedType );
	}
	
	//////////////////////////////////////////////////////////////
	// TEST: testLocalDeleteWithRediscoveryAtMoreSpecificType() //
	//////////////////////////////////////////////////////////////
	/**
	 * This method will create a second test federate and have it register an instance. It will
	 * then wait for this instance to be discovered by the defaultFederate. Following this, the
	 * defaultFederate will attempt to locally delete the instance. Before the local delete, the
	 * default federate will subscribe to a more specific type (that is still valid for the object).
	 * Given this, the federate should (re)discover the object, but this time it should be at the
	 * more specific type.
	 */
	@Test
	public void testLocalDeleteWithRediscoveryAtMoreSpecificType()
	{
		// subscribe to the more specific type
		defaultFederate.quickSubscribe( "ObjectRoot.A.B", "aa", "ab", "ba", "bb" );
		
		// try the local delete //
		try
		{
			defaultFederate.rtiamb.localDeleteObjectInstance( getObjectHandle(oHandle) );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while attempting a local delete", e );
		}
		
		// no need to wait for the delete, there isn't one for local deletes
		// wait for the object to be rediscovered (should be at the more specific type)
		int expectedType = defaultFederate.quickOCHandle( "ObjectRoot.A.B" );
		defaultFederate.fedamb.waitForDiscoveryAs( oHandle, expectedType );
	}

	///////////////////////////////////////////////////////////////////
	// TEST: testLocalDeleteWithRediscoveryAfterSubscriptionChange() //
	///////////////////////////////////////////////////////////////////
	/**
	 * This test makes sure that a previously locally deleted object will be rediscovered when the
	 * subscription interests of the federate change such that the (re)discovery is once again
	 * possible.
	 * <p/>
	 * The second federate will register an instance and the default federate will discover it.
	 * The default fedeate will then change its subscription interests so that it would not longer
	 * discover the object and it will then locally delete it. It should not automatically
	 * rediscover it. After this, the default federate will change its subscription interests so
	 * that it once again can discover the instance, at this point it should do so.
	 */
	@Test
	public void testLocalDeleteWithRediscoveryAfterSubscriptionChange()
	{
		// change subscription interests so that we don't automatically rediscover the object
		defaultFederate.quickUnsubscribe( "ObjectRoot.A" );
		
		// locally delete the object
		defaultFederate.quickLocalDelete( oHandle );
		
		// make sure we don't rediscover it right away
		defaultFederate.fedamb.waitForDiscoveryTimeout( oHandle );
		
		// change the subscription so we can get it again
		defaultFederate.quickSubscribe( "ObjectRoot.A.B", "aa" );
		int expectedType = defaultFederate.quickOCHandle( "ObjectRoot.A.B" );
		defaultFederate.fedamb.waitForDiscoveryAs( oHandle, expectedType );
	}

	////////////////////////////////////////////
	// TEST: testLocalDeleteOfOwnedInstance() //
	////////////////////////////////////////////
	/**
	 * This method will ensure that we can't locally delete an instance we own attributes for.
	 * The secondFederate will register an instance and then attempt to locally delete it.
	 */
	@Test
	public void testLocalDeleteOfOwnedInstance()
	{
		// register the instance //
		int iHandle = secondFederate.quickRegister( "ObjectRoot.A.B" );

		// try and locally delete it //
		try
		{
			secondFederate.rtiamb.localDeleteObjectInstance( getObjectHandle(iHandle) );
			expectedException( FederateOwnsAttributes.class );
		}
		catch( FederateOwnsAttributes foa )
		{
			// Success!
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while trying to locally delete owned instance", e );
		}
	}

	////////////////////////////////////////////////
	// TEST: testLocalDeleteOfNonexistentObject() //
	////////////////////////////////////////////////
	@Test
	public void testLocalDeleteOfNonexistentObject()
	{
		try
		{
			defaultFederate.rtiamb.localDeleteObjectInstance( getObjectHandle(11111) );
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

	/////////////////////////////////////////////////
	// TEST: testLocalDeleteOfUndiscoveredObject() //
	/////////////////////////////////////////////////
	@Test
	public void testLocalDeleteOfUndiscoveredObject()
	{
		// create a third federate that is not subscribed to any classes, thus, it
		// won't get a discovery callback for the registered instance
		TestFederate thirdFederate = new TestFederate( "thirdFederate", this );
		thirdFederate.quickJoin();
		// make sure we don't discover the object
		thirdFederate.fedamb.waitForDiscoveryTimeout( oHandle );
		
		try
		{
			// attempt to locally delete an existing object, but one that we haven't discovered
			thirdFederate.rtiamb.localDeleteObjectInstance( getObjectHandle(oHandle) );
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
			thirdFederate.quickResign();
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
