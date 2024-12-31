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

import hla.rti.FederateNotExecutionMember;
import hla.rti.ObjectAlreadyRegistered;
import hla.rti.ObjectClassNotDefined;
import hla.rti.ObjectClassNotPublished;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;
import hlaunit.hla13.common.Test13Instance;

import org.portico.lrc.PorticoConstants;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"RegisterObjectTest", "registerObject", "objectManagement"})
public class RegisterObjectTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate secondFederate;
	private int aHandle, aaHandle, abHandle, acHandle;

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
	// int registerObjectInstance( int classHandle )
	//     throws ObjectClassNotDefined,
	//            ObjectClassNotPublished,
	//            FederateNotExecutionMember,
	//            SaveInProgress,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;
	
	////////////////////////////////////////
	// TEST: (valid) testRegisterObject() //
	////////////////////////////////////////
	@Test
	public void testRegisterObject()
	{
		// register an object instance
		int oHandle = -1;
		try
		{
			oHandle = defaultFederate.rtiamb.registerObjectInstance( aHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while registering object instance", e );
		}
		
		// make sure the other federates discover it
		secondFederate.fedamb.waitForDiscovery( oHandle );
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
			oHandle = defaultFederate.rtiamb.registerObjectInstance( aHandle );
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
			boolean aa = defaultFederate.rtiamb.isAttributeOwnedByFederate( oHandle, aaHandle );
			boolean ab = defaultFederate.rtiamb.isAttributeOwnedByFederate( oHandle, abHandle );
			boolean ac = defaultFederate.rtiamb.isAttributeOwnedByFederate( oHandle, acHandle );
			
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
			defaultFederate.rtiamb.registerObjectInstance( 111111 );
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
			secondFederate.rtiamb.registerObjectInstance( aHandle );
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

	/////////////////////////////////////////////
	// TEST: testRegisterObjectWhenNotJoined() //
	/////////////////////////////////////////////
	@Test
	public void testRegisterObjectWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			defaultFederate.rtiamb.registerObjectInstance( aHandle );
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
	// TEST: testRegisterObjectWhenSaveInProgress() //
	//////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.registerObjectInstance( aHandle );
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
	// TEST: testRegisterObjectWhenRestoreInProgress() //
	/////////////////////////////////////////////////////
	@Test
	public void testRegisterObjectWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.registerObjectInstance( aHandle );
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
	//////////////////////////////// Object Name Test Methods ////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// int registerObjectInstance( int classHandle, String name )
	//     throws ObjectClassNotDefined,
	//            ObjectClassNotPublished,
	//            ObjectAlreadyRegistered,
	//            FederateNotExecutionMember,
	//            SaveInProgress,
	//            RestoreInProgress,
	//            RTIinternalError,
	//            ConcurrentAccessAttempted;

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
			oHandle = defaultFederate.rtiamb.registerObjectInstance( aHandle, oName );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while registering named object instance", e );
		}
		
		// make sure the other federates discover it
		secondFederate.fedamb.waitForDiscovery( oHandle );
		Test13Instance instance = secondFederate.fedamb.getInstances().get( oHandle );
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
			oHandle = defaultFederate.rtiamb.registerObjectInstance( aHandle, null );
		}
		catch( Exception e )
		{
			unexpectedException( "registering object instance with null name", e );
		}
		
		// verify that the object was registered
		secondFederate.fedamb.waitForDiscovery( oHandle );
		Test13Instance instance = secondFederate.fedamb.getInstances().get( oHandle );
		Assert.assertNotNull( instance.getName(), "Discovered object has null name" );
	}

	///////////////////////////////////////////////////////////////
	// TEST: (valid) testRegisterNamedObjectWithWhitespaceName() //
	///////////////////////////////////////////////////////////////
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
			oHandle = defaultFederate.rtiamb.registerObjectInstance( aHandle, name );
		}
		catch( Exception e )
		{
			unexpectedException( "registering object instance with whitespace name", e );
		}
		
		// verify that the object was registered
		secondFederate.fedamb.waitForDiscovery( oHandle );
		Test13Instance instance = secondFederate.fedamb.getInstances().get( oHandle );
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
			defaultFederate.rtiamb.registerObjectInstance( 111111, "myobject" );
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
		int handleOne = -1;
		String nameOne = "nameOne";
		
		try
		{
			handleOne = defaultFederate.rtiamb.registerObjectInstance( aHandle, nameOne );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while registering named object instance", e );
		}
		
		// wait for the object to be discovered
		secondFederate.fedamb.waitForDiscovery( handleOne );
		
		// try and register another object with the same name
		try
		{
			defaultFederate.rtiamb.registerObjectInstance( aHandle, nameOne );
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
			secondFederate.rtiamb.registerObjectInstance( aHandle, "myObject" );
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
	
	///////////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWithExistingNameAtSameTime() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWithExistingNameAtSameTime()
	{
		// make sure that object name checking is ENABLED!
		System.setProperty( PorticoConstants.PROPERTY_NEGOTIATE_OBJECT_NAMES, "true" );
		
		try
		{
        	secondFederate.quickPublish( "ObjectRoot.A", "aa" );
        	
        	// register the object in the default federate, but don't tick the second federate,
        	// so it shouldn't get the discovery notificiation
        	defaultFederate.quickRegister( "ObjectRoot.A", "myObject" );
        	
        	try
        	{
        		secondFederate.rtiamb.registerObjectInstance( aHandle, "myObject" );
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
		finally
		{
			// back to the default value
			System.setProperty( PorticoConstants.PROPERTY_NEGOTIATE_OBJECT_NAMES, "false" );
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
			secondFederate.rtiamb.registerObjectInstance( aHandle, "myobject" );
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
			oHandle = defaultFederate.rtiamb.registerObjectInstance( aHandle, "myobject" );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while registering named object instance", e );
		}
		
		// make sure the other federates discover it
		secondFederate.fedamb.waitForDiscovery( oHandle );
		Test13Instance instance = secondFederate.fedamb.getInstances().get( oHandle );
		Assert.assertEquals( instance.getName(), "myobject",
		                     "Discovered Object didn't have the correct name" );

		// validate that the correct ownership is in place
		try
		{
			boolean aa = defaultFederate.rtiamb.isAttributeOwnedByFederate( oHandle, aaHandle );
			boolean ab = defaultFederate.rtiamb.isAttributeOwnedByFederate( oHandle, abHandle );
			boolean ac = defaultFederate.rtiamb.isAttributeOwnedByFederate( oHandle, acHandle );
			
			Assert.assertTrue( aa, "Federate doesn't own attribute aa, it should" );
			Assert.assertTrue( ab, "Federate doesn't own attribute ab, it should" );
			Assert.assertFalse( ac, "Federate owns attribute ac, it should not" );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception determining ownership of attributes" , e );
		}
	}

	//////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWhenNotJoined() //
	//////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			defaultFederate.rtiamb.registerObjectInstance( aHandle, "myObject" );
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

	///////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWhenSaveInProgress() //
	///////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWhenSaveInProgress()
	{
		defaultFederate.quickSaveInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.registerObjectInstance( aHandle, "myObject" );
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

	//////////////////////////////////////////////////////////
	// TEST: testRegisterNamedObjectWhenRestoreInProgress() //
	//////////////////////////////////////////////////////////
	@Test
	public void testRegisterNamedObjectWhenRestoreInProgress()
	{
		defaultFederate.quickRestoreInProgress( "save" );
		try
		{
			defaultFederate.rtiamb.registerObjectInstance( aHandle, "myObject" );
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
