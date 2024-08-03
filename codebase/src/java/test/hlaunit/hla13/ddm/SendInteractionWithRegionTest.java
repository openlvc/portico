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
package hlaunit.hla13.ddm;

import java.util.HashMap;

import hla.rti.FederateNotExecutionMember;
import hla.rti.InteractionClassNotDefined;
import hla.rti.InteractionClassNotPublished;
import hla.rti.InteractionParameterNotDefined;
import hla.rti.InvalidFederationTime;
import hla.rti.InvalidRegionContext;
import hla.rti.LogicalTime;
import hla.rti.RTIinternalError;
import hla.rti.Region;
import hla.rti.RegionNotKnown;
import hla.rti.RestoreInProgress;
import hla.rti.SaveInProgress;
import hla.rti.SuppliedParameters;
import hlaunit.hla13.TestSetup;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;
import hlaunit.hla13.common.Test13Interaction;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"SendInteractionWithRegionTest", "sendInteractionWithRegion","ddm"})
public class SendInteractionWithRegionTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate sender;    // publishes and sends InteractionRoot.X,Y,Z
	private Test13Federate xlistener; // subscribed to InteractionRoot.X
	private Test13Federate ylistener; // subscribed to InteractionRoot.X.Y (doesn't use ddm)
	
	private byte[] tag;
	private SuppliedParameters params;

	private int xHandle, xaHandle, xbHandle;
	private int yHandle, yaHandle, ybHandle;
	private int zHandle, zaHandle, zbHandle;
	private int spaceHandle;
	private int dimensionHandle;
	
	private Region senderRegion;
	private Region xRegion;

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
		this.sender = new Test13Federate( "sender", this );
		this.xlistener = new Test13Federate( "xlistener", this );
		this.ylistener = new Test13Federate( "ylistener", this );
		this.tag = "letag".getBytes();
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception
	{
		sender.quickCreate();
		sender.quickJoin();
		xlistener.quickJoin();
		ylistener.quickJoin();
		
		///////////////////////
		// cache all handles //
		///////////////////////
		xHandle         = sender.quickICHandle( "InteractionRoot.X" );
		xaHandle        = sender.quickPCHandle( "InteractionRoot.X", "xa" );
		xbHandle        = sender.quickPCHandle( "InteractionRoot.X", "xb" );
		yHandle         = sender.quickICHandle( "InteractionRoot.X.Y" );
		yaHandle        = sender.quickPCHandle( "InteractionRoot.X.Y", "ya" );
		ybHandle        = sender.quickPCHandle( "InteractionRoot.X.Y", "yb" );
		zHandle         = sender.quickICHandle( "InteractionRoot.X.Y.Z" );
		zaHandle        = sender.quickPCHandle( "InteractionRoot.X.Y.Z", "za" );
		zbHandle        = sender.quickPCHandle( "InteractionRoot.X.Y.Z", "zb" );
		spaceHandle     = sender.quickSpaceHandle( "TestSpace" );
		dimensionHandle = sender.quickDimensionHandle( "TestSpace", "TestDimension" );

		////////////////////////////////
		// set up for sender federate //
		////////////////////////////////
		sender.quickEnableRegulating( 5.0 );
		sender.quickPublish( zHandle );
		// generate a couple of regions that the federate can use
		senderRegion = generateRegion( sender, 100, 200 );
		// set up the default parameter set
		this.params = TestSetup.getRTIFactory().createSuppliedParameters();
		this.params.add( xaHandle, "xa".getBytes() );
		this.params.add( xbHandle, "xb".getBytes() );
		this.params.add( yaHandle, "ya".getBytes() );
		this.params.add( ybHandle, "yb".getBytes() );
		this.params.add( zaHandle, "za".getBytes() );
		this.params.add( zbHandle, "zb".getBytes() );
		
		///////////////////////////////////////
		// set up for the xlistener federate //
		///////////////////////////////////////
		xlistener.quickEnableAsyncDelivery();
		xlistener.quickEnableConstrained();
		xRegion = generateRegion( xlistener, 150, 250 );
		xlistener.quickSubscribeWithRegion( xHandle, xRegion );

		///////////////////////////////////////
		// set up for the ylistener federate //
		///////////////////////////////////////
		ylistener.quickSubscribe( yHandle );
	}
	
	@AfterMethod(alwaysRun=true)
	public void afterMethod()
	{
		xlistener.quickResign();
		ylistener.quickResign();
		sender.quickResign();
		sender.quickDestroy();
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
	 * Generates a new Region instance using the given federate and associated with the
	 * space "TestSpace". The region will have a single extent, using the given upper and
	 * lower bound, for the single dimension in the space. Finally, this method will notify
	 * the RTI of the new stats for the region, so that upon return, the region is known to
	 * the federate and the RTI in its exact state.
	 */
	private Region generateRegion( Test13Federate federate, long lowerBound, long upperBound )
	{
		try
		{
			Region region = federate.quickCreateRegion( spaceHandle, 1 );
			region.setRangeLowerBound( 0, dimensionHandle, lowerBound );
			region.setRangeUpperBound( 0, dimensionHandle, upperBound );
			federate.quickModifyRegion( region );
			return region;
		}
		catch( Exception e )
		{
			Assert.fail( "Can't set up region", e );
			return null;
		}
	}

	/**
	 * Validates that each of the supplied parameters was received with the given interaction and
	 * that each of the parameters has the appropriate value.
	 */
	private void validateReceived( Test13Interaction interaction, int expectedClass )
	{
		if( expectedClass == xHandle )
		{
			// check the values for InteractionRoot.X
			Assert.assertEquals( xHandle, interaction.getClassHandle(),
			                     "federate received wrong interaction class type (X expected)" );
			Assert.assertEquals( "xa".getBytes(), interaction.getParameterValue( xaHandle ),
			                     "federate has wrong value for parameter xa" );
			Assert.assertEquals( "xb".getBytes(), interaction.getParameterValue( xbHandle ),
			                     "federate has wrong value for parameter xb" );
			Assert.assertNull( interaction.getParameterValue(yaHandle), 
			                   "federate has wrong value for attribute ya, not null" );
			Assert.assertNull( interaction.getParameterValue(ybHandle),
			                   "federate has wrong value for attribute yb, not null" );
			Assert.assertNull( interaction.getParameterValue(zaHandle), 
			                   "federate has wrong value for attribute za, not null" );
			Assert.assertNull( interaction.getParameterValue(zbHandle),
			                   "federate has wrong value for attribute zb, not null" );
			Assert.assertEquals( interaction.getRegion(), this.xRegion );
		}
		else if( expectedClass == yHandle )
		{
			// check the values for InteractionRoot.X.Y
			Assert.assertEquals( yHandle, interaction.getClassHandle(),
			                     "federate received wrong interaction class type (Y expected)" );
			Assert.assertEquals( "xa".getBytes(), interaction.getParameterValue( xaHandle ),
			                     "federate has wrong value for parameter xa" );
			Assert.assertEquals( "xb".getBytes(), interaction.getParameterValue( xbHandle ),
			                     "federate has wrong value for parameter xb" );
			Assert.assertEquals( "ya".getBytes(), interaction.getParameterValue( yaHandle ),
			                     "federate has wrong value for parameter ya" );
			Assert.assertEquals( "yb".getBytes(), interaction.getParameterValue( ybHandle ),
			                     "federate has wrong value for parameter yb" );
			Assert.assertNull( interaction.getParameterValue(zaHandle), 
			                   "federate has wrong value for attribute za, not null" );
			Assert.assertNull( interaction.getParameterValue(zbHandle),
			                   "federate has wrong value for attribute zb, not null" );
			Assert.assertNull( interaction.getRegion() );
		}
		else if( expectedClass == zHandle )
		{
			// check the values for InteractionRoot.X.Y.Z
			Assert.assertEquals( zHandle, interaction.getClassHandle(),
			                     "federate received wrong interaction class type (Z expected)" );
			Assert.assertEquals( "xa".getBytes(), interaction.getParameterValue( xaHandle ),
			                     "federate has wrong value for parameter xa" );
			Assert.assertEquals( "xb".getBytes(), interaction.getParameterValue( xbHandle ),
			                     "federate has wrong value for parameter xb" );
			Assert.assertEquals( "ya".getBytes(), interaction.getParameterValue( yaHandle ),
			                     "federate has wrong value for parameter ya" );
			Assert.assertEquals( "yb".getBytes(), interaction.getParameterValue( ybHandle ),
			                     "federate has wrong value for parameter yb" );
			Assert.assertEquals( "za".getBytes(), interaction.getParameterValue( zaHandle ),
			                     "federate has wrong value for parameter za" );
			Assert.assertEquals( "zb".getBytes(), interaction.getParameterValue( zbHandle ),
			                     "federate has wrong value for parameter zb" );
		}
		else
		{
			Assert.fail( "Unknown interaction class received" );
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Receive Order Test Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	// public void sendInteractionWithRegion( int theInteraction,
	//                                        SuppliedParameters theParameters,
	//                                        byte[] userSuppliedTag,
	//                                        Region theRegion )
	//        throws InteractionClassNotDefined,
	//               InteractionClassNotPublished,
	//               InteractionParameterNotDefined,
	//               RegionNotKnown,
	//               InvalidRegionContext,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	/////////////////////////////////////////////////
	// TEST: (valid) testROInteractionWithRegion() //
	/////////////////////////////////////////////////
	@Test
	public void testROInteractionWithRegion()
	{
		// send an interaction and make sure that the xlistener receives it
		try
		{
			sender.rtiamb.sendInteractionWithRegion( zHandle, params, tag, senderRegion );
		}
		catch( Exception e )
		{
			unexpectedException( "sending RO interaction with region", e );
		}
		
		// validate that the xlistener received it
		Test13Interaction interaction = xlistener.fedamb.waitForROInteraction( xHandle );
		validateReceived( interaction, xHandle );
		
		// do the same for the ylistener
		interaction = ylistener.fedamb.waitForROInteraction( yHandle );
		validateReceived( interaction, yHandle );
	}

	////////////////////////////////////////////////////////////////
	// TEST: (valid) testROInteractionWithRegionOutOfRangeBelow() //
	////////////////////////////////////////////////////////////////
	/**
	 * Send an RO interaction with a range that is beyond the subscrition range of that
	 * of the receiving federate to validate that it is filtered out.
	 */
	@Test
	public void testROInteractionWithRegionOutOfRangeBelow()
	{
		// send the interaction with a region that won't overlap with the
		// receivers subscription region
		Region oobBelow = generateRegion( sender, 0, 100 );
		sender.quickSendWithRegion( zHandle, new HashMap<Integer,byte[]>(), tag, oobBelow );
		
		// validate that the xlistener DOESN'T receive the interaction
		xlistener.fedamb.waitForROInteractionTimeout( xHandle );
		
		// validate that the ylistener does (as it doesn't use a subscription region)
		Test13Interaction interaction = ylistener.fedamb.waitForROInteraction( yHandle );
		Assert.assertTrue( interaction.getParameters().size() == 0 );
	}

	////////////////////////////////////////////////////////////////
	// TEST: (valid) testROInteractionWithRegionOutOfRangeAbove() //
	////////////////////////////////////////////////////////////////
	/**
	 * Send an RO interaction with a range that is beyond the subscrition range of that
	 * of the receiving federate to validate that it is filtered out.
	 */
	@Test
	public void testROInteractionWithRegionOutOfRangeAbove()
	{
		// send the interaction with a region that won't overlap with the
		// receivers subscription region
		Region oobAbove = generateRegion( sender, 300, 400 );
		sender.quickSendWithRegion( zHandle, new HashMap<Integer,byte[]>(), tag, oobAbove );
		
		// validate that the xlistener DOESN'T receive the interaction
		xlistener.fedamb.waitForROInteractionTimeout( xHandle );
		
		// validate that the ylistener does (as it doesn't use a subscription region)
		Test13Interaction interaction = ylistener.fedamb.waitForROInteraction( yHandle );
		Assert.assertTrue( interaction.getParameters().size() == 0 );
	}

	///////////////////////////////////////////////////////////////
	// TEST: (valid) testROInteractionWithRegionUsingTimestamp() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithRegionUsingTimestamp()
	{
		// before continuing, make the sender NON-REGULATING
		sender.quickDisableRegulating();
		
		// prepare and send the interaction //
		try
		{
			sender.rtiamb.sendInteractionWithRegion( zHandle,
			                                         params,
			                                         tag,
			                                         senderRegion,
			                                         sender.createTime(10.0) );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid RO interacion with timestamp", e );
		}
		
		// wait for the interaction in the other federates and validate them when they come in //
		Test13Interaction interaction = xlistener.fedamb.waitForROInteraction( xHandle );
		validateReceived( interaction, xHandle );
		interaction = ylistener.fedamb.waitForROInteraction( yHandle );
		validateReceived( interaction, yHandle );
	}
	
	///////////////////////////////////////////////////////////////////
	// TEST: (valid) testROInteractionWithRegionUsingNullTimestamp() //
	///////////////////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithRegionUsingNullTimestamp()
	{
		// prepare and send the interaction //
		try
		{
			sender.rtiamb.sendInteractionWithRegion( zHandle, params, tag, senderRegion, null );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid RO interacion with timestamp", e );
		}
		
		// wait for the interaction in the other federates and validate them when they come in //
		Test13Interaction interaction = xlistener.fedamb.waitForROInteraction( xHandle );
		validateReceived( interaction, xHandle );
		interaction = ylistener.fedamb.waitForROInteraction( yHandle );
		validateReceived( interaction, yHandle );
	}

	///////////////////////////////////////////////////////////////////////
	// TEST: (valid) testROInteractionWithRegionUsingEmptyParameterSet() //
	///////////////////////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithRegionUsingEmptyParameterSet()
	{
		// prepare and send the interaction //
		try
		{
			SuppliedParameters empty = TestSetup.getRTIFactory().createSuppliedParameters();
			sender.rtiamb.sendInteractionWithRegion( zHandle, empty, tag, senderRegion );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid RO interacion with timestamp", e );
		}

		// validate that both listeners receive the interaction, but don't get any parameters
		Test13Interaction interaction = xlistener.fedamb.waitForROInteraction( xHandle );
		Assert.assertEquals( interaction.getParameters().size(), 0,
		                     "Received parameter values when null set was sent for interaction" );

		interaction = null;
		interaction = ylistener.fedamb.waitForROInteraction( yHandle );
		Assert.assertEquals( interaction.getParameters().size(), 0,
		                     "Received parameter values when null set was sent for interaction" );
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testROInteractionWithRegionReceivedWithNoParamsDueToSubscription() //
	//////////////////////////////////////////////////////////////////////////////////////
	/**
	 * this will send an interaction of type InteractionRoot.X.Y.Z with only parameters
	 * from the Y class. This should result in xlistener and ylistener receiving the
	 * interaction, but only the ylistener should get the parameters.
	 */
	@Test
	public void testROInteractionWithRegionReceivedWithNoParamsDueToSubscription()
	{
		// prepare and send the interaction //
		try
		{
			SuppliedParameters set = TestSetup.getRTIFactory().createSuppliedParameters();
			set.add( yaHandle, "ya".getBytes() );
			set.add( ybHandle, "yb".getBytes() );
			sender.rtiamb.sendInteractionWithRegion( zHandle, set, tag, senderRegion );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid RO interacion with timestamp", e );
		}

		// validate that both federates receive the interaction, but that only the ylistener
		// gets any parameters
		Test13Interaction interaction = xlistener.fedamb.waitForROInteraction( xHandle );
		Assert.assertEquals( interaction.getParameters().size(), 0,
		                     "Received parameters when subscription status says we shouldn't" );

		interaction = null;
		interaction = ylistener.fedamb.waitForROInteraction( yHandle );
		Assert.assertEquals( interaction.getParameters().size(), 2,
		                     "Received 0 parameters when subscription says we should get 2" );
	}

	//////////////////////////////////////////////////////////////
	// TEST: testROInteractionWithRegionUsingNullParameterSet() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithRegionUsingNullParameterSet()
	{
		// try and send an interaction with null as the parameters
		try
		{
			sender.rtiamb.sendInteractionWithRegion( zHandle, null, tag, senderRegion );
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

	////////////////////////////////////////////////////////////////////
	// TEST: testROInteractionWithRegionUsingNonExistentClassHandle() //
	////////////////////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithRegionUsingNonExistentClassHandle()
	{
		// try and send an interaction that doesn't exist
		try
		{
			sender.rtiamb.sendInteractionWithRegion( 11111111, params, tag, senderRegion );
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
	
	/////////////////////////////////////////////////////////////////
	// TEST: testROInteractionWithRegionUsingNegativeClassHandle() //
	/////////////////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithRegionUsingNegativeClassHandle()
	{
		// try and send with a negative interaction handle
		try
		{
			sender.rtiamb.sendInteractionWithRegion( -1111111, params, tag, senderRegion );
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
	
	/////////////////////////////////////////////////////////
	// TEST: testROInteractionWithRegionWhenNotPublished() //
	/////////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithRegionWhenNotPublished()
	{
		// try and send an unpublished interaction
		try
		{
			sender.rtiamb.sendInteractionWithRegion( xHandle, params, tag, senderRegion );
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
	
	//////////////////////////////////////////////////////////////////////
	// TEST: testROInteractionWithRegionUsingUndefinedParameterHandle() //
	//////////////////////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithRegionUsingUndefinedParameterHandle()
	{
		// try and send an interaction with an unknown parameter handle
		try
		{
			params.add( 123456, "bad".getBytes() );
			sender.rtiamb.sendInteractionWithRegion( zHandle, params, tag, senderRegion );
			expectedException( InteractionParameterNotDefined.class );
		}
		catch( InteractionParameterNotDefined ipnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InteractionParameterNotDefined.class );
		}
	}

	///////////////////////////////////////////////////////////
	// TEST: testROInteractionWithRegionUsingUnknownRegion() //
	///////////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithRegionUsingUnknownRegion()
	{
		try
		{
			sender.rtiamb.sendInteractionWithRegion( zHandle, params, tag, xRegion );
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

	////////////////////////////////////////////////////////
	// TEST: testROInteractionWithRegionUsingNullRegion() //
	////////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithRegionUsingNullRegion()
	{
		try
		{
			sender.rtiamb.sendInteractionWithRegion( zHandle, params, tag, null );
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
	// TEST: testROInteractionWithRegionUsingInvalidRegionForClass() //
	///////////////////////////////////////////////////////////////////
	/**
	 * I'm not entirely sure what the InvalidRegionContext exception is for, but I am assuming
	 * in the case of interactions that it is used when you try to send an interaction with
	 * a region that is associated with a space which the FOM does not also associate with the
	 * interaction.
	 */
	@Test
	public void testROInteractionWithRegionUsingInvalidRegionForClass()
	{
		// make sure the sender is now publishing InteractionRoot.X.Y, then attempt
		// to send an interaction of that class with a region. There is no region defined
		// for that class in the FOM, so it should error out
		sender.quickPublish( yHandle );
		try
		{
			// create an empty paramter set to use, the other parameter set has values
			// for parameters that aren't valid, and we don't want that tripping things out
			SuppliedParameters set = TestSetup.getRTIFactory().createSuppliedParameters();
			sender.rtiamb.sendInteractionWithRegion( yHandle, set, tag, senderRegion );
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

	//////////////////////////////////////////////////////
	// TEST: testROInteractionWithRegionWhenNotJoined() //
	//////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithRegionWhenNotJoined()
	{
		sender.quickResign();
		try
		{
			sender.rtiamb.sendInteractionWithRegion( zHandle, params, tag, senderRegion );
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

	///////////////////////////////////////////////////////////
	// TEST: testROInteractionWithRegionWhenSaveInProgress() //
	///////////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithRegionWhenSaveInProgress()
	{
		sender.quickSaveInProgress( "save" );
		try
		{
			sender.rtiamb.sendInteractionWithRegion( zHandle, params, tag, senderRegion );
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

	///////////////////////////////////////////////////////////
	// TEST: testROInteractionWithRegionWhenRestoreInProgress() //
	///////////////////////////////////////////////////////////
	@Test
	public void testROInteractionWithRegionWhenRestoreInProgress()
	{
		sender.quickRestoreInProgress( "save" );
		try
		{
			sender.rtiamb.sendInteractionWithRegion( zHandle, params, tag, senderRegion );
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
	// public EventRetractionHandle sendInteractionWithRegion( int theInteraction,
	//                                                         SuppliedParameters theParameters,
	//                                                         byte[] userSuppliedTag,
	//                                                         Region theRegion,
	//                                                         LogicalTime theTime )
	//        throws InteractionClassNotDefined,
	//               InteractionClassNotPublished,
	//               InteractionParameterNotDefined,
	//               InvalidFederationTime,
	//               RegionNotKnown,
	//               InvalidRegionContext,
	//               FederateNotExecutionMember,
	//               SaveInProgress,
	//               RestoreInProgress,
	//               RTIinternalError,
	//               ConcurrentAccessAttempted;

	//////////////////////////////////////////////////
	// TEST: (valid) testTSOInteractionWithRegion() //
	//////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithRegion()
	{
		// send an interaction and make sure that the xlistener receives it
		try
		{
			sender.rtiamb.sendInteractionWithRegion( zHandle,
			                                         params,
			                                         tag,
			                                         senderRegion,
			                                         sender.createTime(10.0) );
		}
		catch( Exception e )
		{
			unexpectedException( "sending RO interaction with region", e );
		}
		
		// make sure the non-constrained federate gets the interaction
		Test13Interaction interaction = ylistener.fedamb.waitForROInteraction( yHandle );
		validateReceived( interaction, yHandle );
		
		// make sure the constrained federate doesn't get the interaction yet
		xlistener.fedamb.waitForTSOInteractionTimeout( xHandle );
		// advance time far enough to allow for the interaction delivery
		sender.quickAdvanceRequest( 10.0 );
		xlistener.quickAdvanceRequest( 10.0 );
		interaction = xlistener.fedamb.waitForTSOInteraction( xHandle );
		validateReceived( interaction, xHandle );
	}

	/////////////////////////////////////////////////////////////////
	// TEST: (valid) testTSOInteractionWithRegionOutOfRangeBelow() //
	/////////////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithRegionOutOfRangeBelow()
	{
		// send the interaction with a region that won't overlap with the
		// receivers subscription region
		Region oobBelow = generateRegion( sender, 0, 100 );
		sender.quickSendWithRegion( zHandle, new HashMap<Integer,byte[]>(), tag, 10.0, oobBelow );

		// validate that the ylistener does (as it doesn't use a subscription region or time)
		Test13Interaction interaction = ylistener.fedamb.waitForROInteraction( yHandle );
		Assert.assertTrue( interaction.getParameters().size() == 0 );

		// make sure the constrained federate doesn't get the interaction
		xlistener.fedamb.waitForTSOInteractionTimeout( xHandle );
		// advance the xlistener far enough so that it would receive the interaction
		// if it wasn't sent with a region that was out of the rage
		sender.quickAdvanceRequest( 100.0 );
		xlistener.quickAdvanceRequest( 100.0 );
		// validate that the xlistener DOESN'T receive the interaction
		xlistener.fedamb.waitForTSOInteractionTimeout( xHandle );
	}

	/////////////////////////////////////////////////////////////////
	// TEST: (valid) testTSOInteractionWithRegionOutOfRangeAbove() //
	/////////////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithRegionOutOfRangeAbove()
	{
		// send the interaction with a region that won't overlap with the
		// receivers subscription region
		Region oobAbove = generateRegion( sender, 300, 400 );
		sender.quickSendWithRegion( zHandle, new HashMap<Integer,byte[]>(), tag, 10.0, oobAbove );

		// validate that the ylistener does (as it doesn't use a subscription region or time)
		Test13Interaction interaction = ylistener.fedamb.waitForROInteraction( yHandle );
		Assert.assertTrue( interaction.getParameters().size() == 0 );

		// make sure the constrained federate doesn't get the interaction
		xlistener.fedamb.waitForTSOInteractionTimeout( xHandle );
		// advance the xlistener far enough so that it would receive the interaction
		// if it wasn't sent with a region that was out of the rage
		sender.quickAdvanceRequest( 100.0 );
		xlistener.quickAdvanceRequest( 100.0 );
		// validate that the xlistener DOESN'T receive the interaction
		xlistener.fedamb.waitForTSOInteractionTimeout( xHandle );
	}

	////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testTSOInteractionWithRegionUsingEmptyParameterSet() //
	////////////////////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithRegionUsingEmptyParameterSet()
	{
		// prepare and send the interaction //
		try
		{
			SuppliedParameters empty = TestSetup.getRTIFactory().createSuppliedParameters();
			sender.rtiamb.sendInteractionWithRegion( zHandle,
			                                         empty,
			                                         tag,
			                                         senderRegion,
			                                         sender.createTime(10.0) );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid RO interacion with timestamp", e );
		}

		// make sure the non-constrained federate gets the interaction
		Test13Interaction interaction = ylistener.fedamb.waitForROInteraction( yHandle );
		Assert.assertEquals( interaction.getParameters().size(), 0,
		                     "Received parameter values when null set was sent for interaction" );

		// make sure the constrained federate doesn't get the interaction yet
		xlistener.fedamb.waitForTSOInteractionTimeout( xHandle );
		// advance time far enough to allow for the interaction delivery
		sender.quickAdvanceRequest( 10.0 );
		xlistener.quickAdvanceRequest( 10.0 );
		interaction = xlistener.fedamb.waitForTSOInteraction( xHandle );
		Assert.assertEquals( interaction.getParameters().size(), 0,
		                     "Received parameter values when null set was sent for interaction" );
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	// TEST: (valid) testTSOInteractionWithRegionReceivedWithNoParamsDueToSubscription() //
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * this will send an interaction of type InteractionRoot.X.Y.Z with only parameters
	 * from the Y class. This should result in xlistener and ylistener receiving the
	 * interaction, but only the ylistener should get the parameters.
	 */
	@Test
	public void testTSOInteractionWithRegionReceivedWithNoParamsDueToSubscription()
	{
		// prepare and send the interaction //
		try
		{
			SuppliedParameters set = TestSetup.getRTIFactory().createSuppliedParameters();
			set.add( yaHandle, "ya".getBytes() );
			set.add( ybHandle, "yb".getBytes() );
			sender.rtiamb.sendInteractionWithRegion( zHandle,
			                                         set,
			                                         tag,
			                                         senderRegion,
			                                         sender.createTime(10.0) );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during valid RO interacion with timestamp", e );
		}

		// make sure the non-constrained federate gets the interaction
		Test13Interaction interaction = ylistener.fedamb.waitForROInteraction( yHandle );
		Assert.assertEquals( interaction.getParameters().size(), 2,
		                     "Received 0 parameters when subscription says we should get 2" );

		// make sure the constrained federate doesn't get the interaction yet
		xlistener.fedamb.waitForTSOInteractionTimeout( xHandle );
		// advance time far enough to allow for the interaction delivery
		sender.quickAdvanceRequest( 10.0 );
		xlistener.quickAdvanceRequest( 10.0 );
		interaction = xlistener.fedamb.waitForTSOInteraction( xHandle );
		Assert.assertEquals( interaction.getParameters().size(), 0,
		                     "Received parameters when subscription status says we shouldn't" );
	}

	///////////////////////////////////////////////////////////////////////
	// TEST: (valid) testTSOInteractionNotReceivedAfterModifyingRegion() //
	///////////////////////////////////////////////////////////////////////
	/**
	 * This test ensures that the proper client-side filtering of region data is working. It works
	 * as follows:
	 * <ol>
	 *   <li>Sending federate sends TSO interaction with region that OVERLAPS with receiving
	 *       federates subscription region</li>
	 *   <li>RTI Queues and sends a callback to receiving federate, but LRC doesn't deliver it
	 *       yet because the time isn't right for the timestamp</li>
	 *   <li>Before advancing, the receiving federate alters its subscription region so that there
	 *       is NO OVERLAP (and thus, the RTI wouldn't deliver the interaction if it were sent
	 *       again at this point). However, the original message is still in the LRC queue,
	 *       although the subscription now has no overlap, and thus, it shouldn't be delivered</li>
	 *   <li>Receiving federate advances time such that the original message would no longer
	 *       be held back. Receiving federate should NOT receive the callback as the regions
	 *       no longer overlap (according to the spec)</li>
	 * </ol>
	 */
	@Test(enabled=false)
	public void testTSOInteractionNotReceivedAfterModifyingRegion() throws Exception
	{
		// send the original interaction using an overlapping region
		HashMap<Integer,byte[]> parameters = new HashMap<Integer,byte[]>();
		sender.quickSendWithRegion( zHandle, parameters, tag, 10.0, senderRegion );
		
		// make sure the ylistener gets it, as that federate is not constrained
		ylistener.fedamb.waitForROInteraction( yHandle );
		// make sure the xlistener DOESN'T get it, as that federate is constrained
		xlistener.fedamb.waitForTSOInteractionTimeout( xHandle );
		
		// change the subscription region for the xlistener
		xRegion.setRangeLowerBound( 0, dimensionHandle, 1000 );
		xRegion.setRangeUpperBound( 0, dimensionHandle, 2000 );
		xlistener.quickModifyRegion( xRegion );
		
		// advance time so that the federate would receive the interaction if it were valid
		sender.quickAdvanceRequest( 100.0 );
		xlistener.quickAdvanceRequest( 10.0 );
		
		// wait for the interaction to come through, which is SHOULD NOT
		xlistener.fedamb.waitForTSOInteractionTimeout( xHandle );
	}

	///////////////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithRegionUsingNullParameterSet() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithRegionUsingNullParameterSet()
	{
		// try and send an interaction with null as the parameters
		try
		{
			sender.rtiamb.sendInteractionWithRegion( zHandle,
			                                         null,
			                                         tag,
			                                         senderRegion,
			                                         sender.createTime(10.0) );
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

	/////////////////////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithRegionUsingNonExistentClassHandle() //
	/////////////////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithRegionUsingNonExistentClassHandle()
	{
		// try and send an interaction that doesn't exist
		try
		{
			sender.rtiamb.sendInteractionWithRegion( 11111111,
			                                         params,
			                                         tag,
			                                         senderRegion,
			                                         sender.createTime(10.0) );
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
	
	//////////////////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithRegionUsingNegativeClassHandle() //
	//////////////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithRegionUsingNegativeClassHandle()
	{
		// try and send with a negative interaction handle
		try
		{
			sender.rtiamb.sendInteractionWithRegion( -1111111,
			                                         params,
			                                         tag,
			                                         senderRegion,
			                                         sender.createTime(10.0) );
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
	
	//////////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithRegionWhenNotPublished() //
	//////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithRegionWhenNotPublished()
	{
		// try and send an unpublished interaction
		try
		{
			sender.rtiamb.sendInteractionWithRegion( xHandle,
			                                         params,
			                                         tag,
			                                         senderRegion,
			                                         sender.createTime(10.0) );
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
	
	///////////////////////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithRegionUsingUndefinedParameterHandle() //
	///////////////////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithRegionUsingUndefinedParameterHandle()
	{
		// try and send an interaction with an unknown parameter handle
		try
		{
			params.add( 123456, "bad".getBytes() );
			sender.rtiamb.sendInteractionWithRegion( zHandle,
			                                         params,
			                                         tag,
			                                         senderRegion,
			                                         sender.createTime(10.0) );
			expectedException( InteractionParameterNotDefined.class );
		}
		catch( InteractionParameterNotDefined ipnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, InteractionParameterNotDefined.class );
		}
	}

	////////////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithRegionUsingUnknownRegion() //
	////////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithRegionUsingUnknownRegion()
	{
		try
		{
			sender.rtiamb.sendInteractionWithRegion( zHandle,
			                                         params,
			                                         tag,
			                                         xRegion,
			                                         sender.createTime(10.0) );
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

	/////////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithRegionUsingNullRegion() //
	/////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithRegionUsingNullRegion()
	{
		try
		{
			sender.rtiamb.sendInteractionWithRegion( zHandle,
			                                         params,
			                                         tag,
			                                         null,
			                                         sender.createTime(10.0) );
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

	////////////////////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithRegionUsingInvalidRegionForClass() //
	////////////////////////////////////////////////////////////////////
	/**
	 * I'm not entirely sure what the InvalidRegionContext exception is for, but I am assuming
	 * in the case of interactions that it is used when you try to send an interaction with
	 * a region that is associated with a space which the FOM does not also associate with the
	 * interaction.
	 */
	@Test
	public void testTSOInteractionWithRegionUsingInvalidRegionForClass()
	{
		// make sure the sender is now publishing InteractionRoot.X.Y, then attempt
		// to send an interaction of that class with a region. There is no region defined
		// for that class in the FOM, so it should error out
		sender.quickPublish( yHandle );
		try
		{
			// create an empty paramter set to use, the other parameter set has values
			// for parameters that aren't valid, and we don't want that tripping things out
			SuppliedParameters set = TestSetup.getRTIFactory().createSuppliedParameters();
			sender.rtiamb.sendInteractionWithRegion( yHandle,
			                                         set,
			                                         tag,
			                                         senderRegion,
			                                         sender.createTime(10.0) );
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

	///////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithRegionWhenNotJoined() //
	///////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithRegionWhenNotJoined()
	{
		sender.quickResign();
		try
		{
			sender.rtiamb.sendInteractionWithRegion( zHandle,
			                                         params,
			                                         tag,
			                                         senderRegion,
			                                         sender.createTime(10.0) );
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
	// TEST: testTSOInteractionWithRegionWhenSaveInProgress() //
	////////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithRegionWhenSaveInProgress()
	{
		sender.quickSaveInProgress( "save" );
		try
		{
			sender.rtiamb.sendInteractionWithRegion( zHandle,
			                                         params,
			                                         tag,
			                                         senderRegion,
			                                         sender.createTime(10.0) );
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
	// TEST: testTSOInteractionWithRegionWhenRestoreInProgress() //
	///////////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithRegionWhenRestoreInProgress()
	{
		sender.quickRestoreInProgress( "save" );
		try
		{
			sender.rtiamb.sendInteractionWithRegion( zHandle,
			                                         params,
			                                         tag,
			                                         senderRegion,
			                                         sender.createTime(10.0) );
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

	////////////////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithRegionUsingNegativeTimestamp() //
	////////////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithRegionUsingNegativeTimestamp()
	{
		// try and send an interaction with a negative time
		try
		{
			LogicalTime time = sender.createTime( -11.0 );
			sender.rtiamb.sendInteractionWithRegion( zHandle, params, tag, senderRegion, time );
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
	
	////////////////////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithRegionUsingTimestampLessThanLBTS() //
	////////////////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithRegionUsingTimestampLessThanLBTS()
	{
		// send an interaction  with a time that is less than our LBTS
		try
		{
			LogicalTime time = sender.createTime( 1.0 );
			sender.rtiamb.sendInteractionWithRegion( zHandle, params, tag, senderRegion, time );
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

	//////////////////////////////////////////////////////////////
	// TEST: testTSOInteractionWithRegionUsingTimestampInPast() //
	//////////////////////////////////////////////////////////////
	@Test
	public void testTSOInteractionWithRegionUsingTimestampInPast()
	{
		// setup
		sender.quickAdvanceAndWait( 50.0 );
		
		// send an interaction  with a time that is less than our LBTS
		try
		{
			LogicalTime time = sender.createTime( 10.0 );
			sender.rtiamb.sendInteractionWithRegion( zHandle, params, tag, senderRegion, time );
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

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
