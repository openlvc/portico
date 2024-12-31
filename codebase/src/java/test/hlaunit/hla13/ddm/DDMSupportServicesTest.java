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

import hla.rti.DimensionNotDefined;
import hla.rti.FederateNotExecutionMember;
import hla.rti.NameNotFound;
import hla.rti.Region;
import hla.rti.RegionNotKnown;
import hla.rti.SpaceNotDefined;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * These tests are used to validate the DDM-related support service methods in the RTIambassador
 * API.
 */
@Test(singleThreaded=true,groups={"DDMSupportServicesTest", "ddm","supportServices"})
public class DDMSupportServicesTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate secondFederate;
	private int spaceHandle;
	private int dimensionHandle;
	private Region region;
	private int regionHandle;

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
		
		// initialize the second federate
		this.secondFederate = new Test13Federate( "secondFederate", this );
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
		
		// create the region
		spaceHandle = defaultFederate.quickSpaceHandle( "TestSpace" );
		dimensionHandle = defaultFederate.quickDimensionHandle( "TestSpace", "TestDimension" );
		region = defaultFederate.quickCreateRegion( spaceHandle, 1 );
		regionHandle = defaultFederate.quickGetRegionToken( region );
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
	
	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// DDM Suport Test Methods /////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// public int getRoutingSpaceHandle( String theName )
	//        throws NameNotFound,
	//               FederateNotExecutionMember,
	//               RTIinternalError;
	@Test
	public void testGetRoutingSpaceHandle() throws Exception
	{
		Assert.assertEquals( defaultFederate.rtiamb.getRoutingSpaceHandle("TestSpace"), spaceHandle,
		                     "Received incorrect routing space handle in default federate" );
		Assert.assertEquals( secondFederate.rtiamb.getRoutingSpaceHandle("TestSpace"), spaceHandle,
		                     "Received incorrect routing space handle in second federate" );
	}

	@Test
	public void testGetRoutingSpaceHandleWhenNotJoined()
	{
		secondFederate.quickResign();
		try
		{
			secondFederate.rtiamb.getRoutingSpaceHandle( "TestSpace" );
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

	@Test
	public void testGetRoutingSpaceHandleWithNonExistentName()
	{
		try
		{
			defaultFederate.rtiamb.getRoutingSpaceHandle( "NoSuchSpace" );
			expectedException( NameNotFound.class );
		}
		catch( NameNotFound nnf )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, NameNotFound.class );
		}
	}

	@Test
	public void testGetRoutingSpaceHandleWithNullName()
	{
		try
		{
			defaultFederate.rtiamb.getRoutingSpaceHandle( null );
			expectedException( NameNotFound.class );
		}
		catch( NameNotFound nnf )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, NameNotFound.class );
		}
	}

	// public String getRoutingSpaceName( int theHandle )
	//        throws SpaceNotDefined,
	//               FederateNotExecutionMember,
	//               RTIinternalError;
	@Test
	public void testGetRoutingSpaceName() throws Exception
	{
		Assert.assertEquals( defaultFederate.rtiamb.getRoutingSpaceName(spaceHandle), "TestSpace",
		                     "Incorrect routing space name received from RTI by defaultFederate" );
		Assert.assertEquals( secondFederate.rtiamb.getRoutingSpaceName(spaceHandle), "TestSpace",
		                     "Incorrect routing space name received from RTI by secondFederate" );
	}

	@Test
	public void testGetRoutingSpaceNameWhenNotJoined()
	{
		secondFederate.quickResign();
		try
		{
			secondFederate.rtiamb.getRoutingSpaceName( spaceHandle );
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

	@Test
	public void testGetRoutingSpaceNameWithNonExistentHandle()
	{
		try
		{
			defaultFederate.rtiamb.getRoutingSpaceName( 1000000 );
			expectedException( SpaceNotDefined.class );
		}
		catch( SpaceNotDefined snd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, SpaceNotDefined.class );
		}
	}
	
	@Test
	public void testGetRoutingSpaceNameWithNegativeHandle()
	{
		try
		{
			defaultFederate.rtiamb.getRoutingSpaceName( -1 );
			expectedException( SpaceNotDefined.class );
		}
		catch( SpaceNotDefined snd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, SpaceNotDefined.class );
		}
	}

	// public int getDimensionHandle( String theName, int whichSpace )
	//        throws SpaceNotDefined,
	//               NameNotFound,
	//               FederateNotExecutionMember,
	//               RTIinternalError;
	@Test
	public void testGetDimensionHandle() throws Exception
	{
		Assert.assertEquals( defaultFederate.rtiamb.getDimensionHandle("TestDimension",spaceHandle),
		                     dimensionHandle,
		                     "Wrong dimension handle received from RTI in defaultFederate" );

		Assert.assertEquals( secondFederate.rtiamb.getDimensionHandle("TestDimension",spaceHandle),
		                     dimensionHandle,
		                     "Wrong dimension handle received from RTI in secondFederate" );

	}

	@Test
	public void testGetDimensionHandleWhenNotJoined()
	{
		secondFederate.quickResign();
		try
		{
			secondFederate.rtiamb.getDimensionHandle( "TestDimension", spaceHandle );
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

	@Test
	public void testGetDimensionHandleWithNonExistentName()
	{
		try
		{
			defaultFederate.rtiamb.getDimensionHandle( "NoSuchDimension", spaceHandle );
			expectedException( NameNotFound.class );
		}
		catch( NameNotFound nnf )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, NameNotFound.class );
		}
	}
	
	@Test
	public void testGetDimensionHandleWithNullName()
	{
		try
		{
			defaultFederate.rtiamb.getDimensionHandle( null, spaceHandle );
			expectedException( NameNotFound.class );
		}
		catch( NameNotFound nnf )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, NameNotFound.class );
		}
	}
	
	@Test
	public void testGetDimensionHandleWithNonExistentSpace()
	{
		try
		{
			defaultFederate.rtiamb.getDimensionHandle( "TestDimension", 1000000 );
			expectedException( SpaceNotDefined.class );
		}
		catch( SpaceNotDefined snd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, SpaceNotDefined.class );
		}
	}
	
	@Test
	public void testGetDimensionHandleWithNegativeSpace()
	{
		try
		{
			defaultFederate.rtiamb.getDimensionHandle( "TestDimension", -1 );
			expectedException( SpaceNotDefined.class );
		}
		catch( SpaceNotDefined snd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, SpaceNotDefined.class );
		}
	}

	// public String getDimensionName( int theHandle, int whichClass )
	//        throws SpaceNotDefined,
	//               DimensionNotDefined,
	//               FederateNotExecutionMember,
	//               RTIinternalError;
	@Test
	public void testGetDimensionName() throws Exception
	{
		Assert.assertEquals( defaultFederate.rtiamb.getDimensionName(dimensionHandle,spaceHandle),
		                     "TestDimension",
		                     "Wrong dimension name received from RTI in defaultFederate" );

		Assert.assertEquals( secondFederate.rtiamb.getDimensionName(dimensionHandle,spaceHandle),
		                     "TestDimension",
		                     "Wrong dimension name received from RTI in secondFederate" );
	}

	@Test
	public void testGetDimensionNameWhenNotJoined()
	{
		secondFederate.quickResign();
		try
		{
			secondFederate.rtiamb.getDimensionName( dimensionHandle, spaceHandle );
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

	@Test
	public void testGetDimensionNameWithNonExistentDimension()
	{
		try
		{
			defaultFederate.rtiamb.getDimensionName( 1000000, spaceHandle );
			expectedException( DimensionNotDefined.class );
		}
		catch( DimensionNotDefined dnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, DimensionNotDefined.class );
		}
	}
	
	@Test
	public void testGetDimensionNameWithNegativeDimension()
	{
		try
		{
			defaultFederate.rtiamb.getDimensionName( -1, spaceHandle );
			expectedException( DimensionNotDefined.class );
		}
		catch( DimensionNotDefined dnd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, DimensionNotDefined.class );
		}
	}
	
	@Test
	public void testGetDimensionNameWithNonExistentSpace()
	{
		try
		{
			defaultFederate.rtiamb.getDimensionName( dimensionHandle, 100000 );
			expectedException( SpaceNotDefined.class );
		}
		catch( SpaceNotDefined snd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, SpaceNotDefined.class );
		}
	}
	
	@Test
	public void testGetDimensionNameWithNegativeSpace()
	{
		try
		{
			defaultFederate.rtiamb.getDimensionName( dimensionHandle, -1 );
			expectedException( SpaceNotDefined.class );
		}
		catch( SpaceNotDefined snd )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, SpaceNotDefined.class );
		}
	}

	// public int getAttributeRoutingSpaceHandle( int theHandle, int whichClass )
	//        throws ObjectClassNotDefined,
	//               AttributeNotDefined,
	//               FederateNotExecutionMember,
	//               RTIinternalError;

	// not yet implemented

	// public int getInteractionRoutingSpaceHandle( int theHandle )
	//        throws InteractionClassNotDefined,
	//               FederateNotExecutionMember,
	//               RTIinternalError;

	// not yet implemented

	// public Region getRegion( int regionToken )
	//        throws FederateNotExecutionMember,
	//               ConcurrentAccessAttempted,
	//               RegionNotKnown,
	//               RTIinternalError;
	@Test
	public void testGetRegion() throws Exception
	{
		Assert.assertEquals( defaultFederate.rtiamb.getRegion(regionHandle), region,
		                     "Wrong Region reference received from the RTI in defaultFederate" );
	}

	@Test
	public void testGetRegionWithUnknownHandleInFederate()
	{
		// now try with the second federate. should result in an exception
		try
		{
			secondFederate.rtiamb.getRegion( regionHandle );
			expectedException( RegionNotKnown.class );
		}
		catch( RegionNotKnown rnn )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RegionNotKnown.class );
		}
	}
	
	@Test
	public void testGetRegionWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			defaultFederate.rtiamb.getRegion( regionHandle );
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

	@Test
	public void testGetRegionWithNonExistentRegionToken()
	{
		try
		{
			secondFederate.rtiamb.getRegion( 100000 );
			expectedException( RegionNotKnown.class );
		}
		catch( RegionNotKnown rnn )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RegionNotKnown.class );
		}
	}
	
	@Test
	public void testGetRegionWithNegativeRegionToken()
	{
		try
		{
			secondFederate.rtiamb.getRegion( -1 );
			expectedException( RegionNotKnown.class );
		}
		catch( RegionNotKnown rnn )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RegionNotKnown.class );
		}
	}

	// public int getRegionToken( Region region )
	//        throws FederateNotExecutionMember,
	//               ConcurrentAccessAttempted,
	//               RegionNotKnown,
	//               RTIinternalError;
	@Test
	public void testGetRegionToken() throws Exception
	{
		Assert.assertEquals( defaultFederate.rtiamb.getRegionToken(region), regionHandle,
		                     "Wrong region token received from RTI in defaultFederate" );
	}

	@Test
	public void testGetRegionTokenInvalidInFederate()
	{
		try
		{
			secondFederate.rtiamb.getRegionToken( region );
			expectedException( RegionNotKnown.class );
		}
		catch( RegionNotKnown rnn )
		{
			// success!
		}
		catch( Exception e )
		{
			wrongException( e, RegionNotKnown.class );
		}
	}
	
	@Test
	public void testGetRegionTokenWhenNotJoined()
	{
		defaultFederate.quickResign();
		try
		{
			defaultFederate.rtiamb.getRegionToken( region );
			expectedException( FederateNotExecutionMember.class );
		}
		catch( FederateNotExecutionMember fnem )
		{
			// succcess!
		}
		catch( Exception e )
		{
			wrongException( e, FederateNotExecutionMember.class );
		}
	}

	@Test
	public void testGetRegionTokenWithNullRegion()
	{
		try
		{
			defaultFederate.rtiamb.getRegionToken( null );
			expectedException( RegionNotKnown.class );
		}
		catch( RegionNotKnown rnn )
		{
			// succcess!
		}
		catch( Exception e )
		{
			wrongException( e, RegionNotKnown.class );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
