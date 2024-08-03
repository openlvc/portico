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
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.ObjectClassNotDefined;
import hla.rti1516e.exceptions.ObjectClassNotPublished;
import hla.rti1516e.exceptions.ObjectInstanceNameInUse;
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

@Test(singleThreaded=true, groups={"RegisterObjectTest", "registerObject", "objectManagement"})
public class RegisterObjectTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate secondFederate;
	private int aHandle, aaHandle, abHandle, acHandle;
	private ObjectClassHandle aClass;

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
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
		
		// cache the handles
		aHandle  = defaultFederate.quickOCHandle( "ObjectRoot.A" );
		aaHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "aa" );
		abHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ab" );
		acHandle = defaultFederate.quickACHandle( "ObjectRoot.A", "ac" );
		
		aClass = TypeFactory.getObjectClassHandle( aHandle );
		
		// do publication and subscription
		defaultFederate.quickPublish( "ObjectRoot.A", "aa", "ab" );
		secondFederate.quickSubscribe( "ObjectRoot.A", "aa", "ab", "ac" );
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
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
	////////////////////////////////////// Test Methods //////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public ObjectInstanceHandle registerObjectInstance( ObjectClassHandle theClass )
	//        throws ObjectClassNotDefined,
	//               ObjectClassNotPublished,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

	
	////////////////////////////////////////
	// TEST: (valid) testRegisterObject() //
	////////////////////////////////////////
	@Test
	public void testRegisterObject()
	{
		// register an object instance
		ObjectInstanceHandle oHandle = null;
		try
		{
			oHandle = defaultFederate.rtiamb.registerObjectInstance( aClass );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while registering object instance", e );
		}
		
		// make sure the other federates discover it
		secondFederate.fedamb.waitForDiscovery( TypeFactory.getObjectHandle(oHandle) );
	}

	///////////////////////////////////////////////////////////////////
	// TEST: testRegisterObjectFederateOnlyOwnsPublishedAttributes() //
	///////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectFederateOnlyOwnsPublishedAttributes()
	{
		// register an object instance
		int oHandle = -1;
		try
		{
			oHandle = getObjectHandle( defaultFederate.rtiamb.registerObjectInstance(aClass) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while registering object instance", e );
		}
		
		// make sure the other federates discover it
		secondFederate.fedamb.waitForDiscovery( oHandle );
		
		// validate that the correct ownership is in place
		try
		{
			boolean aa = defaultFederate.quickIsOwned( oHandle, aaHandle );
			boolean ab = defaultFederate.quickIsOwned( oHandle, abHandle );
			boolean ac = defaultFederate.quickIsOwned( oHandle, acHandle );
			
			Assert.assertTrue( aa, "Federate doesn't own attribute aa, it should" );
			Assert.assertTrue( ab, "Federate doesn't own attribute ab, it should" );
			Assert.assertFalse( ac, "Federate owns attribute ac, it should not" );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception determining ownership of attributes" , e );
		}
	}

	//////////////////////////////////////////////////////
	// TEST: testRegisterObjectWithInvalidClassHandle() //
	//////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWithInvalidClassHandle()
	{
		try
		{
			defaultFederate.rtiamb.registerObjectInstance( getObjectClassHandle(111111) );
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
	
	///////////////////////////////////////////////////////////
	// TEST: testRegisterObjectWithNonPublishedClassHandle() //
	///////////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWithNonPublishedClassHandle()
	{
		try
		{
			// the second federate does not publish anything
			secondFederate.rtiamb.registerObjectInstance( aClass );
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
	
	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Object Name Test Methods ////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public ObjectInstanceHandle registerObjectInstance( ObjectClassHandle theClass,
	//                                                     String theObjectName )
	//        throws ObjectClassNotDefined,
	//               ObjectClassNotPublished,
	//               ObjectInstanceNameNotReserved,
	//               ObjectInstanceNameInUse,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;


	/////////////////////////////////////////////
	// TEST: (valid) testRegisterNamedObject() //
	/////////////////////////////////////////////
	@Test
	public void testRegisterNamedObject()
	{
		// register an object instance
		int oHandle = -1;
		String oName = "myobject";
		try
		{
			oHandle =
				getObjectHandle( defaultFederate.rtiamb.registerObjectInstance(aClass, oName) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while registering named object instance", e );
		}
		
		// make sure the other federates discover it
		secondFederate.fedamb.waitForDiscovery( oHandle );
		TestObject instance = secondFederate.fedamb.getInstances().get( oHandle );
		Assert.assertEquals( instance.getName(), oName,
		                     "Discovered Object didn't have the correct name" );
	}

	/////////////////////////////////////////////////////////
	// TEST: (valid) testRegisterNamedObjectWithNullName() //
	/////////////////////////////////////////////////////////
	/**
	 * This should be allowed, but the registered object should have an RTI generated name.
	 * Null should not be used for it.
	 */
	@Test
	public void testRegisterNamedObjectWithNullName()
	{
		int oHandle = -1;
		try
		{
			oHandle =
				getObjectHandle( defaultFederate.rtiamb.registerObjectInstance(aClass, null) );
		}
		catch( Exception e )
		{
			unexpectedException( "registering object instance with null name", e );
		}
		
		// verify that the object was registered
		secondFederate.fedamb.waitForDiscovery( oHandle );
		TestObject instance = secondFederate.fedamb.getInstances().get( oHandle );
		Assert.assertNotNull( instance.getName(), "Discovered object has null name" );
	}

	///////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithWhitespaceName() //
	///////////////////////////////////////////////////////
	/**
	 * As with registering an object with a null name, this should also work. However, the
	 * whitespace name used should be given to the instance. Although it isn't very elegant
	 * to use such a value for a name, it shouldn't be illegal
	 */
	@Test
	public void testRegisterNamedObjectWithWhitespaceName()
	{
		int oHandle = -1;
		String name = "   ";
		try
		{
			oHandle =
				getObjectHandle( defaultFederate.rtiamb.registerObjectInstance(aClass, name) );
		}
		catch( Exception e )
		{
			unexpectedException( "registering object instance with whitespace name", e );
		}
		
		// verify that the object was registered
		secondFederate.fedamb.waitForDiscovery( oHandle );
		TestObject instance = secondFederate.fedamb.getInstances().get( oHandle );
		Assert.assertEquals( instance.getName(), name,
		                     "Discovered object should have whitespace name" );
	}

	///////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithInvalidClassHandle() //
	///////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithInvalidClassHandle()
	{
		try
		{
			defaultFederate.rtiamb.registerObjectInstance( getObjectClassHandle(111111),
			                                               "myobject" );
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

	/////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithExistingName() //
	/////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithExistingName()
	{
		// register the first object
		int objectHandle = defaultFederate.quickRegister( "ObjectRoot.A", "myObject" );
		// wait for the object to be discovered
		secondFederate.fedamb.waitForDiscovery( objectHandle );
		
		// try and register another object with the same name
		try
		{
			defaultFederate.rtiamb.registerObjectInstance( aClass, "myObject" );
			expectedException( ObjectInstanceNameInUse.class );
		}
		catch( ObjectInstanceNameInUse oiniu )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectInstanceNameInUse.class );
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithExistingNameFromDifferentFederate() //
	//////////////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithExistingNameFromDifferentFederate()
	{
		int objectHandle = defaultFederate.quickRegister( "ObjectRoot.A", "myObject" );
		secondFederate.fedamb.waitForDiscovery( objectHandle );
		
		// make sure the second federate is publishing as required
		secondFederate.quickPublish( "ObjectRoot.A", "aa" );
		
		try
		{
			secondFederate.rtiamb.registerObjectInstance( aClass, "myObject" );
			expectedException( ObjectInstanceNameInUse.class );
		}
		catch( ObjectInstanceNameInUse oiniu )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, ObjectInstanceNameInUse.class );
		}
	}

	////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithNonPublishedClassHandle() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithNonPublishedClassHandle()
	{
		try
		{
			// the second federate does not publish anything
			secondFederate.rtiamb.registerObjectInstance( aClass, "myobject" );
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
	
	////////////////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectFederateOnlyOwnsPublishedAttributes() //
	////////////////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectFederateOnlyOwnsPublishedAttributes()
	{
		// register an object instance
		int oHandle = -1;
		try
		{
			oHandle = getObjectHandle( defaultFederate.rtiamb.registerObjectInstance(aClass,
			                                                                         "myobject") );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while registering named object instance", e );
		}
		
		// make sure the other federates discover it
		secondFederate.fedamb.waitForDiscovery( oHandle );
		TestObject instance = secondFederate.fedamb.getInstances().get( oHandle );
		Assert.assertEquals( instance.getName(), "myobject",
		                     "Discovered Object didn't have the correct name" );

		// validate that the correct ownership is in place
		try
		{
			boolean aa = defaultFederate.quickIsOwned( oHandle, aaHandle );
			boolean ab = defaultFederate.quickIsOwned( oHandle, abHandle );
			boolean ac = defaultFederate.quickIsOwned( oHandle, acHandle );
			
			Assert.assertTrue( aa, "Federate doesn't own attribute aa, it should" );
			Assert.assertTrue( ab, "Federate doesn't own attribute ab, it should" );
			Assert.assertFalse( ac, "Federate owns attribute ac, it should not" );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception determining ownership of attributes" , e );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
