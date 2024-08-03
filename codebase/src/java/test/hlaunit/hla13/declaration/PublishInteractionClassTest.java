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
package hlaunit.hla13.declaration;

import hla.rti.FederateNotExecutionMember;
import hla.rti.InteractionClassNotDefined;
import hla.rti.InteractionClassNotPublished;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

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
public class PublishInteractionClassTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	int xHandle;
	int yHandle;

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
	private void validatePublished( Test13Federate federate, int expectedClass )
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
	private void validateNotPublished( Test13Federate federate, int expectedClass )
	{
		federate.quickSendFail( expectedClass, null, "tag".getBytes() );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Publication Methods ///////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// public void publishInteractionClass( int theInteraction )
	//        throws InteractionClassNotDefined,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

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
			defaultFederate.rtiamb.publishInteractionClass( xHandle );
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
			defaultFederate.rtiamb.publishInteractionClass( yHandle );
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
			defaultFederate.rtiamb.publishInteractionClass( xHandle );
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
			defaultFederate.rtiamb.publishInteractionClass( 11111111 );
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
			defaultFederate.rtiamb.publishInteractionClass( xHandle );
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
	// TEST: testICPublishWhenSaveInProgress() //
	/////////////////////////////////////////////
	@Test
	public void testICPublishWhenSaveInProgress()
	{
		// resign
		defaultFederate.quickSaveInProgress( "save" );

		try
		{
			defaultFederate.rtiamb.publishInteractionClass( xHandle );
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
	// TEST: testICPublishWhenRestoreInProgress() //
	////////////////////////////////////////////////
	@Test
	public void testICPublishWhenRestoreInProgress()
	{
		// resign
		defaultFederate.quickRestoreInProgress( "save" );

		try
		{
			defaultFederate.rtiamb.publishInteractionClass( xHandle );
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

	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Unpublication Methods //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// public void unpublishInteractionClass( int theInteraction )
	//        throws InteractionClassNotDefined,
	//               InteractionClassNotPublished,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

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
			defaultFederate.rtiamb.unpublishInteractionClass( xHandle );
		}
		catch( Exception e )
		{
			unexpectedException( "unpublishing interaction class", e );
		}
		
		// validate that we no longer publish the class
		validateNotPublished( defaultFederate, xHandle );
	}

	////////////////////////////////////////////////////////
	// TEST: testICUnpublishWithNonPublishedInteraction() //
	////////////////////////////////////////////////////////
	@Test
	public void testICUnpublishWithNonPublishedInteraction()
	{
		try
		{
			defaultFederate.rtiamb.unpublishInteractionClass( xHandle );
			expectedException( InteractionClassNotPublished.class );
		}
		catch( InteractionClassNotPublished icnp )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InteractionClassNotPublished.class );
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
			defaultFederate.rtiamb.unpublishInteractionClass( 11111111 );
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
			defaultFederate.rtiamb.unpublishInteractionClass( xHandle );
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

	///////////////////////////////////////////////
	// TEST: testICUnpublishWhenSaveInProgress() //
	///////////////////////////////////////////////
	@Test
	public void testICUnpublishWhenSaveInProgress()
	{
		// resign so we can run the test
		defaultFederate.quickSaveInProgress( "save" );
		
		try
		{
			defaultFederate.rtiamb.unpublishInteractionClass( xHandle );
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

	//////////////////////////////////////////////////
	// TEST: testICUnpublishWhenRestoreInProgress() //
	//////////////////////////////////////////////////
	@Test
	public void testICUnpublishWhenRestoreInProgress()
	{
		// resign so we can run the test
		defaultFederate.quickRestoreInProgress( "save" );
		
		try
		{
			defaultFederate.rtiamb.unpublishInteractionClass( xHandle );
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
