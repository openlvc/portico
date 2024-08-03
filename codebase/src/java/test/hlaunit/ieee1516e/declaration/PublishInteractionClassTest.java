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
package hlaunit.ieee1516e.declaration;

import static hlaunit.ieee1516e.common.TypeFactory.*;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.InteractionClassNotDefined;
import hlaunit.ieee1516e.common.Abstract1516eTest;
import hlaunit.ieee1516e.common.TestFederate;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"PublishInteractionClassTest",
                                   "publishInteraction",
                                   "publish",
                                   "pubsub",
                                   "declarationManagement"})
public class PublishInteractionClassTest extends Abstract1516eTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int xHandle;
	private int yHandle;

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
	}

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		
		// cache the handles
		xHandle = defaultFederate.quickICHandle( "InteractionRoot.X" );
		yHandle = defaultFederate.quickICHandle( "InteractionRoot.X.Y" );
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		defaultFederate.quickResign();
		defaultFederate.quickDestroy();
	}

	@AfterClass(alwaysRun=true)
	public void afterClass()
	{
		super.afterClass();
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// Helper  Methods /////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will validate that the given federate is publishing the given interaction
	 * class. It will do this by trying to send an instance of the interaction class. If the
	 * send fails, Assert.fail() will be used to kill the test.
	 */
	private void validatePublished( TestFederate federate, int expectedClass )
	{
		// try and send an interaction of the given type
		federate.quickSend( expectedClass, null, "tag".getBytes() );
	}
	
	/**
	 * This method will validate that the given federate is NOT publishing the given interaction
	 * class. It will do this by trying to send an instance of the interaction class, expecting
	 * that it will fail. If the request doesn't fail, the interaction class must be listed as
	 * published, and Assert.fail() will be used to kill the test.
	 */
	private void validateNotPublished( TestFederate federate, int expectedClass )
	{
		federate.quickSendFail( expectedClass, null, "tag".getBytes() );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Publication Methods ///////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// public void publishInteractionClass( InteractionClassHandle theInteraction )
	//        throws InteractionClassNotDefined,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

	///////////////////////////////////
	// TEST: (valid) testICPublish() //
	///////////////////////////////////
	@Test
	public void testICPublish()
	{
		// validate that we are in the right state to start
		validateNotPublished( defaultFederate, xHandle );
		
		// do the publication
		try
		{
			defaultFederate.rtiamb.publishInteractionClass( getInteractionHandle(xHandle) );
		}
		catch( Exception e )
		{
			unexpectedException( "publishing interaction class", e );
		}
		
		// validate that we are now publishing the interaction
		validatePublished( defaultFederate, xHandle );
	}

	////////////////////////////////////////////////////////////
	// TEST: (valid) testICPublishBothParentAndChildClasses() //
	////////////////////////////////////////////////////////////
	@Test
	public void testICPublishBothParentAndChildClasses()
	{
		// validate that we are in the right state to start with
		validateNotPublished( defaultFederate, xHandle );
		validateNotPublished( defaultFederate, yHandle );
		
		// publish the first class
		try
		{
			defaultFederate.rtiamb.publishInteractionClass( getInteractionHandle(yHandle) );
		}
		catch( Exception e )
		{
			unexpectedException( "publishing interaction class", e );
		}
		
		// validate that the right publication properties exist
		validateNotPublished( defaultFederate, xHandle );
		validatePublished( defaultFederate, yHandle );
		
		// publish the parent class
		try
		{
			defaultFederate.rtiamb.publishInteractionClass( getInteractionHandle(xHandle) );
		}
		catch( Exception e )
		{
			unexpectedException( "publishing interaction class", e );
		}
		
		// validate the publications
		validatePublished( defaultFederate, xHandle );
		validatePublished( defaultFederate, yHandle );
	}

	///////////////////////////////////////////////////
	// TEST: testICPublishWithUndefinedClassHandle() //
	///////////////////////////////////////////////////
	@Test
	public void testICPublishWithUndefinedClassHandle()
	{
		try
		{
			defaultFederate.rtiamb.publishInteractionClass( getInteractionHandle(11111111) );
			expectedException( InteractionClassNotDefined.class );
		}
		catch( InteractionClassNotDefined icnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InteractionClassNotDefined.class );
		}
	}

	////////////////////////////////////////
	// TEST: testICPublishWhenNotJoined() //
	////////////////////////////////////////
	@Test
	public void testICPublishWhenNotJoined()
	{
		// resign
		defaultFederate.quickResign();

		try
		{
			defaultFederate.rtiamb.publishInteractionClass( getInteractionHandle(xHandle) );
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
	
	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Unpublication Methods //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// public void unpublishInteractionClass( InteractionClassHandle theInteraction )
	//        throws InteractionClassNotDefined,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError;

	/////////////////////////////////////
	// TEST: (valid) testICUnpublish() //
	/////////////////////////////////////
	@Test
	public void testICUnpublish()
	{
		// setup //
		defaultFederate.quickPublish( xHandle );
		validatePublished( defaultFederate, xHandle );
		
		// unpublish the class
		try
		{
			defaultFederate.rtiamb.unpublishInteractionClass( getInteractionHandle(xHandle) );
		}
		catch( Exception e )
		{
			unexpectedException( "unpublishing interaction class", e );
		}
		
		// validate that we no longer publish the class
		validateNotPublished( defaultFederate, xHandle );
	}

	////////////////////////////////////////////////////////////////
	// TEST: (valid) testICUnpublishWithNonPublishedInteraction() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testICUnpublishWithNonPublishedInteraction()
	{
		try
		{
			defaultFederate.rtiamb.unpublishInteractionClass( getInteractionHandle(xHandle) );
		}
		catch( Exception e )
		{
			unexpectedException( "unpublishing non-published interaction", e );
		}
	}

	/////////////////////////////////////////////////////
	// TEST: testICUnpublishWithUndefinedClassHandle() //
	/////////////////////////////////////////////////////
	@Test
	public void testICUnpublishWithUndefinedClassHandle()
	{
		try
		{
			defaultFederate.rtiamb.unpublishInteractionClass( getInteractionHandle(11111111) );
			expectedException( InteractionClassNotDefined.class );
		}
		catch( InteractionClassNotDefined icnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InteractionClassNotDefined.class );
		}
	}

	//////////////////////////////////////////
	// TEST: testICUnpublishWhenNotJoined() //
	//////////////////////////////////////////
	@Test
	public void testICUnpublishWhenNotJoined()
	{
		// resign so we can run the test
		defaultFederate.quickResign();
		
		try
		{
			defaultFederate.rtiamb.unpublishInteractionClass( getInteractionHandle(xHandle) );
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

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
