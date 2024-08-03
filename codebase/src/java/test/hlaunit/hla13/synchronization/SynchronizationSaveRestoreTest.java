/*
 *   Copyright 2009 The Portico Project
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
package hlaunit.hla13.synchronization;

import hla.rti.RTIinternalError;
import hlaunit.hla13.common.Abstract13Test;
import hlaunit.hla13.common.Test13Federate;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(singleThreaded=true, groups={"SynchronizationSaveRestoreTest",
                                   "synchronization",
                                   "federationManagement",
                                   "SaveRestore"})
public class SynchronizationSaveRestoreTest extends Abstract13Test
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate secondFederate;
	private String saveLabel;
	private String syncPoint;
	
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
		
		this.secondFederate = new Test13Federate( "secondFederate", this );
		this.saveLabel = "SynchronizationSaveRestoreTest";
		this.syncPoint = "SyncPointOne";
	}
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod()
	{
		defaultFederate.quickCreate();
		defaultFederate.quickJoin();
		secondFederate.quickJoin();
	
		// register a sync point and make sure it is announced to everyone
		defaultFederate.quickAnnounce( syncPoint, new byte[0] );
		secondFederate.fedamb.waitForSyncAnnounce( syncPoint );
		defaultFederate.fedamb.waitForSyncAnnounce( syncPoint );

		// get that data saved out
		defaultFederate.quickSaveToCompletion( saveLabel );
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

	/////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Sync Point Save/Restore Tests /////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////
	// TEST: (valid) testSyncPointAchievedAfterSaveRestore() //
	///////////////////////////////////////////////////////////
	@Test
	public void testSyncPointAchievedAfterSaveRestore()
	{
		// achieve the point in default federate
		defaultFederate.quickAchieved( syncPoint );
		
		// try and a
		try
		{
			defaultFederate.rtiamb.synchronizationPointAchieved( syncPoint );
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

		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// achieve the point again, should not fail
		defaultFederate.quickAchieved( syncPoint );
	}

	//////////////////////////////////////////////////////
	// TEST: (valid) testSynchronizedAfterSaveRestore() //
	//////////////////////////////////////////////////////
	@Test
	public void testSynchronizedAfterSaveRestore()
	{
		// synchornize on the point
		defaultFederate.quickAchieved( syncPoint );
		secondFederate.quickAchieved( syncPoint );
		defaultFederate.fedamb.waitForSynchronized( syncPoint );
		secondFederate.fedamb.waitForSynchronized( syncPoint );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// go through the whole process again
		defaultFederate.quickAchieved( syncPoint );
		secondFederate.quickAchieved( syncPoint );
		defaultFederate.fedamb.waitForSynchronized( syncPoint );
		secondFederate.fedamb.waitForSynchronized( syncPoint );
	}

	//////////////////////////////////////////////////////////////////////
	// TEST: (valid) testSynchronizedFromPartialStateAfterSaveRestore() //
	//////////////////////////////////////////////////////////////////////
	@Test
	public void testSynchronizedFromPartialStateAfterSaveRestore()
	{
		// achieve the point in the default federate
		defaultFederate.quickAchieved( syncPoint );
		
		// save the state again
		defaultFederate.quickSaveToCompletion( saveLabel );
		
		// synchronize on the point
		secondFederate.quickAchieved( syncPoint );
		defaultFederate.fedamb.waitForSynchronized( syncPoint );
		secondFederate.fedamb.waitForSynchronized( syncPoint );
		
		// restore
		defaultFederate.quickRestoreToCompletion( saveLabel );
		
		// go through the whole process again
		secondFederate.quickAchieved( syncPoint );
		defaultFederate.fedamb.waitForSynchronized( syncPoint );
		secondFederate.fedamb.waitForSynchronized( syncPoint );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
