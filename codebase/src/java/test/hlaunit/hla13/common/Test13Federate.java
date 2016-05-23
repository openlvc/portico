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
package hlaunit.hla13.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;

import org.portico.impl.hla13.Rti13Ambassador;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;
import org.portico.lrc.LRCMessageQueue;
import org.portico.lrc.PorticoConstants;

import hla.rti.AttributeHandleSet;
import hla.rti.FederateHandleSet;
import hla.rti.FederateNotExecutionMember;
import hla.rti.LogicalTime;
import hla.rti.LogicalTimeInterval;
import hla.rti.Region;
import hla.rti.ResignAction;
import hla.rti.SuppliedAttributes;
import hla.rti.SuppliedParameters;
import hla.rti.jlc.RTIambassadorEx;
import hlaunit.hla13.TestSetup;

/**
 * This class represents a federate that can be used for testing purposes. It provides a number
 * of helper methods that speed the process of writing tests
 */
public class Test13Federate
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static Set<Test13Federate> TICKSET = new HashSet<Test13Federate>();

	/** Set of all active federates - added here on construction so we can destroy with
	    the {@link #killActiveFederates()} method at the end of a test class. */
	private static Set<Test13Federate> ACTIVE_FEDERATES = new HashSet<Test13Federate>();
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	public String federateName;
	public int federateHandle;
	public RTIambassadorEx rtiamb;
	public Test13FederateAmbassador fedamb;

	public String simpleName;
	public Abstract13Test test;
	
	private Map<String,byte[]> saveData;
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public Test13Federate( String name, Abstract13Test test )
	{
		if( name == null || test == null )
		{
			Assert.fail( "Null value given when creating Test13Federate, can't continue" );
		}
		
		this.federateName = name;
		this.federateHandle = -1;
		this.test = test;
		this.test.federates.add( this );
		this.simpleName = this.test.getClass().getSimpleName();
		this.rtiamb = TestSetup.createRTIambassador();
		this.fedamb = new Test13FederateAmbassador( this );
		this.saveData = new HashMap<String,byte[]>();
		
		Test13Federate.ACTIVE_FEDERATES.add( this );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////
	/////////////////// Basic Access Methods ///////////////////
	////////////////////////////////////////////////////////////
	public RTIambassadorEx getRtiAmb()
	{
		return this.rtiamb;
	}
	
	public AttributeHandleSet createAHS( int...handles )
	{
		try
		{
			AttributeHandleSet set = TestSetup.getRTIFactory().createAttributeHandleSet();
			for( int handle : handles )
			{
				set.add( handle );
			}
			return set;
		}
		catch( Exception e )
		{
			// This should hopefully never happen, unless we're stupid and pass negative handles
			Assert.fail( "Couldn't create AttributeHandleSet: " + e.getMessage(), e );
			return null;
		}
	}
	
	public LogicalTime createTime( double time )
	{
		return new DoubleTime( time );
	}
	
	public double decodeTime( LogicalTime time )
	{
		return ((DoubleTime)time).getTime();
	}
	
	public LogicalTimeInterval createInterval( double time )
	{
		return new DoubleTimeInterval( time );
	}
	
	public boolean isJoined()
	{
		return this.federateHandle != PorticoConstants.NULL_HANDLE;
	}

	public void printQueue()
	{
		System.out.println( ((Rti13Ambassador)rtiamb).getHelper().getState().getQueue() );
	}

	public LRCMessageQueue getLrcQueue()
	{
		return ((Rti13Ambassador)rtiamb).getHelper().getState().getQueue();
	}

	/////////////////////////////////////////////////////////////
	/////////////////// Serialization Methods ///////////////////
	/////////////////////////////////////////////////////////////
	public void save( String label )
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream ostream = new ObjectOutputStream( baos );
			this.fedamb.saveToStream( ostream );
			saveData.put( label, baos.toByteArray() );
			ostream.close();
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while saving local data", e );
		}
	}
	
	public void restore( String label )
	{
		if( saveData.containsKey(label) == false )
		{
			Assert.fail( "Can't restore local data with label ["+label+"], don't have save data" );
		}
		
		try
		{
			byte[] data = saveData.remove( label );
			ObjectInputStream istream = new ObjectInputStream( new ByteArrayInputStream(data) );
			this.fedamb.restoreFromStream( istream );
			istream.close();
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while restoring local data", e );
		}
	}
	
	////////////////////////////////////////////////////////////
	//////////////// Create and Destroy Methods ////////////////
	////////////////////////////////////////////////////////////
	/**
	 * Calls {@link #quickCreate(String)}, using the name of the test this federate is associated
	 * with as the name of the federation.
	 */
	public void quickCreate()
	{
		quickCreate( this.simpleName );
	}
	
	/**
	 * Same as {@link #quickCreate()} except that you can specify the name of the federation
	 */
	public void quickCreate( String name )
	{
		try
		{
			URL fom = ClassLoader.getSystemResource( "fom/testfom.fed" );
			this.rtiamb.createFederationExecution( name, fom );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception in quickCreate in " + simpleName + " ("+e.getClass()+")", e );
		}
	}
	
	/**
	 * Destroy the federation of the name equal to the fedname protected variable
	 * (which is the simple name of the test class). If the destroy fails, Assert.fail() will
	 * be used rather than throwing an exception.
	 */
	public void quickDestroy()
	{
		quickDestroy( this.simpleName );
	}
	
	/**
	 * Same as {@link #quickDestroy()} except that you can specify the federation name
	 * @param name
	 */
	public void quickDestroy( String name )
	{
		try
		{
			this.rtiamb.destroyFederationExecution( name );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception in quickDestroy in " + simpleName + " ("+e.getClass()+")", e );
		}		
	}
	
	////////////////////////////////////////////////////////////
	/////////////////////// Join Methods ///////////////////////
	////////////////////////////////////////////////////////////
	/**
	 * Joins this federate to the default federation. If there is an error Assert.fail()
	 * will be used
	 */
	public int quickJoin()
	{
		return quickJoin( simpleName );
	}
	
	/**
	 * Joins this federate to the given federation. If there is an error Assert.fail() will be used
	 */
	public int quickJoin( String theFederation )
	{
		try
		{
			this.fedamb = new Test13FederateAmbassador( this );
			this.federateHandle = this.rtiamb.joinFederationExecution( federateName,
			                                                           theFederation,
			                                                           this.fedamb );
			// store the federate in the quick-tick list
			Test13Federate.TICKSET.add( this );
			return this.federateHandle;
		}
		catch( Exception e )
		{
			Assert.fail( "Exception in quickJoin in " + simpleName + " ("+e.getClass()+")", e );
			return -1;
		}
	}
	
	////////////////////////////////////////////////////////////
	////////////////////// Resign Methods //////////////////////
	////////////////////////////////////////////////////////////
	/**
	 * Centralize the resign processing just to make things easier. If there is a problem while
	 * attempting the resign, this method will use Assert.fail() to fail the test rather than
	 * throw an exception. If the federate is not currently joined (and a
	 * FederateNotExecutionMember exception is thrown), it will be ignored.
	 */
	public void quickResign( int resignAction )
	{
		// remove the federate from the quick-tick set
		Test13Federate.TICKSET.remove( this );
		
		try
		{
			this.federateHandle = PorticoConstants.NULL_HANDLE;
			this.rtiamb.resignFederationExecution( resignAction );
		}
		catch( FederateNotExecutionMember nem )
		{
			// Ignore this
		}
		catch( Exception e )
		{
			Assert.fail( "Exception in quickResign in " + simpleName + " ("+e.getClass()+")", e );
		}
	}
	
	/**
	 * Same as {@link #quickResign(int)} with a resign action of
	 * DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES.
	 */
	public void quickResign()
	{
		this.quickResign( ResignAction.DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES );
	}
	
	
	//////////////////////////////////////////////////////////////
	/////////////// Synchronization Helper Methods ///////////////
	//////////////////////////////////////////////////////////////
	/**
	 * Announce a federation-wide synchronization point with the given label and tag. This method
	 * will block until the synchornization success message has been recevied. If there is any
	 * problem, Assert.fail() will be used rather than throwing an exception.
	 */
	public void quickAnnounce( String label, byte[] tag )
	{
		try
		{
			// announce the sync point //
			rtiamb.registerFederationSynchronizationPoint( label, tag );

			// wait for the success/failure result //
			if( fedamb.waitForSyncResult(label) == false )
			{
				// we didn't get the result we wanted, fail
				Assert.fail( "RTI notified that syncpoint reg [" + label + "] failed" );
			}
			
			// wait for the announcement //
			fedamb.waitForSyncAnnounce( label );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickAnnounce(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * The same as {@link #quickAnnounce(String, byte[])} except that it will automatically
	 * pass an empty array for the tag.
	 */
	public void quickAnnounce( String label )
	{
		quickAnnounce( label, new byte[0] );
	}
	
	/**
	 * This is the same as {@link #quickAnnounce(String, byte[])} except that the given array of
	 * federate handles is used to declare a restricted sync point.
	 */
	public void quickAnnounce( String label, byte[] tag, Integer... handles )
	{
		// turn the set into the appropriate FederateHandleSet
		FederateHandleSet set = TestSetup.getRTIFactory().createFederateHandleSet();
		for( Integer handle : handles )
		{
			set.add( handle );
		}
		
		// announce the sync point //
		try
		{
			rtiamb.registerFederationSynchronizationPoint( label, tag, set );

			// wait for the success/failure result //
			if( fedamb.waitForSyncResult(label) == false )
			{
				// we didn't get the result we wanted, fail
				Assert.fail( "RTI notified that syncpoint reg [" + label + "] failed" );
			}
			
			// wait for the announcement //
			fedamb.waitForSyncAnnounce( label );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickAnnounce(): " + e.getMessage(), e );
		}
	}

	/**
	 * This method will signal to the RTI that the federate has reached the given sync point. If
	 * this causes an exception, Assert.fail() will be used to kill the test.
	 */
	public void quickAchieved( String label )
	{
		try
		{
			rtiamb.synchronizationPointAchieved( label );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickAchieved(): " + e.getMessage(), e );
		}
	}

	////////////////////////////////////////////////////////////////////////
	/////////////////// Save and Restore Helper Methods ////////////////////
	////////////////////////////////////////////////////////////////////////
	/**
	 * Request a federation save with the given label, fail the test if there is an exception
	 */
	public void quickSaveRequest( String label )
	{
		try
		{
			rtiamb.requestFederationSave( label );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception requesting Federaiton Save: " + e.getMessage(), e );
		}
	}
	
	/**
	 * Request a federation save with the given label and time, fail the test if there
	 * is an exception
	 */
	public void quickSaveRequest( String label, double time )
	{
		try
		{
			rtiamb.requestFederationSave( label, createTime(time) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception requesting Federaiton Save (with time): " + e.getMessage(), e );
		}
	}

	/**
	 * Signal to the RTI that the federation save has begun. If there is an exception, kill the test
	 */
	public void quickSaveBegun()
	{
		try
		{
			rtiamb.federateSaveBegun();
		}
		catch( Exception e )
		{
			Assert.fail( "Exception informing RTI that save has begun: " + e.getMessage(), e );
		}
	}

	/**
	 * Inform the RTI that the federate has completed its save. Kill the current test if there
	 * is an exception
	 */
	public void quickSaveComplete()
	{
		try
		{
			rtiamb.federateSaveComplete();
		}
		catch( Exception e )
		{
			Assert.fail( "Exception informing RTI that save was successfully completed: "+
			             e.getMessage(), e );
		}
	}
	
	/**
	 * Inform the RTI that the federate has *NOT* completed its save. Kill the current test
	 * if there is an exception
	 */
	public void quickSaveNotComplete()
	{
		try
		{
			rtiamb.federateSaveNotComplete();
		}
		catch( Exception e )
		{
			Assert.fail( "Exception informing RTI that save was not completed successfully: "+
			             e.getMessage(), e );
		}
	}

	/**
	 * This method will trigger a save request with the given label and then wait until a
	 * callback initiating the save have been received. If there is a problem sending out
	 * the request or the initiation isn't received in a timely fashion, the current test
	 * will be failed.
	 * <p/>
	 * The main purpose of this method is for getting a federation to a point where the
	 * "SaveInProgress" exceptions can be tested for.
	 */
	public void quickSaveInProgress( String label )
	{
		// initiate a save from this federate using the given label
		quickSaveRequest( label );
		
		// wait for the callback telling us the save has been initiated
		fedamb.waitForSaveInitiated( label );
	}
	
	/**
	 * This method will handle the entire save process, triggering and completing the process
	 * using the given label. A reference to each of the federates is taken from the test to which
	 * this federate is attached. Note that this method will call methods on the other federates
	 * as required to complete the save.
	 * <p/>
	 * At the end of this process, the federation will have been successfully saved using the
	 * given label. If there is a problem in any of the steps (initiating the save, starting it
	 * or successfully completing in any of the federates), the current test will be failed.
	 */
	public void quickSaveToCompletion( String label )
	{
		// initiate a save from this federate using the given label
		quickSaveRequest( label );
		
		Set<Test13Federate> federates = test.joinedFederates();

		// wait for the callback telling EACH FEDERATE that the save has been initiated
		for( Test13Federate federate : federates )
			federate.fedamb.waitForSaveInitiated( label );

		// have each federate signal that it has begun its save
		for( Test13Federate federate : federates )
		{
			// we always signal that the save completed successfully. if it didn't, an exception
			// will have been thrown, causing processing to stop and the test to fail
			federate.quickSaveBegun();
			federate.save( label );
			federate.quickSaveComplete();
		}
		
		// wait for the save to be completed
		for( Test13Federate federate : federates )
		{
			federate.fedamb.waitForFederationSaved();
		}
	}
	
	/**
	 * Requests a federation restore from the RTIambassador and then returns. If there is an
	 * exception performing this call, the current test is failed.
	 */
	public void quickRestoreRequest( String label )
	{
		try
		{
			rtiamb.requestFederationRestore( label );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception requesting federation restore with label ["+label+"]: " +
			             e.getMessage(), e );
		}
	}

	/**
	 * This method initiates a federation restore request (calling
	 * {@link #quickRestoreRequest(String)}) and then ticks until either a success of failure
	 * notification from the RTI is received. If the initiation is not a success, or there is an
	 * exception initiating it, the current test is failed.
	 * <p/>
	 * <b>**NOTE**</b> This will block until a notification from the RTI is received about whether
	 * the request was a failure or a success. If you just want to initiate a restore, use the
	 * {@link #quickRestoreRequest(String)} method.
	 * 
	 * @param label
	 */
	public void quickRestoreRequestSuccess( String label )
	{
		quickRestoreRequest( label );
		try
		{
			fedamb.waitForRestoreRequestSuccess( label );
		}
		catch( Exception e )
		{
			Assert.fail( "Expected a successful response when initiating federation restore: "+
			             e.getMessage(), e );
		}
	}

	/**
	 * Tell the RTIambassador that the federate has successfully completed it's restore.
	 * If there is an exception, the current test is failed.
	 */
	public void quickRestoreComplete()
	{
		try
		{
			rtiamb.federateRestoreComplete();
		}
		catch( Exception e )
		{
			Assert.fail( "Exception informing RTI that restore was successfully completed: "+
			             e.getMessage(), e );
		}
	}
	
	/**
	 * Tell the RTIambassador that the federate has *NOT* successfully completed it's restore.
	 * If there is an exception, the current test is failed.
	 */
	public void quickRestoreNotComplete()
	{
		try
		{
			rtiamb.federateRestoreNotComplete();
		}
		catch( Exception e )
		{
			Assert.fail( "Exception informing RTI that restore was not completed successfully: "+
			             e.getMessage(), e );
		}
	}
	
	/**
	 * This method will trigger and complete a Save and then initiate a restore, waiting for a
	 * callback indicating that the federation restore has begun. The given label is used both
	 * for the save and the restore. The set of all the federates is received from the test
	 * class that this federate is associated with. 
	 * <p/>
	 * The main purpose of this method is for getting a federation to a point where the
	 * "RestoreInProgress" exceptions can be tested for.
	 */
	public void quickRestoreInProgress( String label )
	{
		// save the state out
		quickSaveToCompletion( label );
		
		// kick off a restore
		quickRestoreRequest( label );
		fedamb.waitForRestoreRequestSuccess( label );
		
		// wait for the "restore begun" notice
		Set<Test13Federate> federates = test.joinedFederates();
		for( Test13Federate federate : federates )
		{
			federate.fedamb.waitForFederationRestoreBegun();
		}

		for( Test13Federate federate : federates )
		{
			federate.fedamb.waitForFederateRestoreInitiated( label );
		}
	}

	/**
	 * This method is much like {@link #quickSaveToCompletion(String)}, except for restoration.
	 * It runs through the complete restore cycle, making sure all the federates get the right
	 * messages and respond appropriately. This is primarily intended for use in methods which
	 * test that some state that was present before a save is properly set after a restore.
	 * <p/>
	 * <b>NOTE:</b> For this method to work properly you will need to have previously completed
	 *              a federation save with the same label given as a parameter
	 */
	public void quickRestoreToCompletion( String label )
	{
		// kick off a restore
		quickRestoreRequest( label );
		fedamb.waitForRestoreRequestSuccess( label );
		
		// wait for the "restore begun" notice
		Set<Test13Federate> federates = test.joinedFederates();
		for( Test13Federate federate : federates )
		{
			federate.fedamb.waitForFederationRestoreBegun();
		}
		
		// wait for the restore to be initiated locally
		for( Test13Federate federate : federates )
		{
			// we always signal that the restore completed successfully. if it didn't, an exception
			// will have been thrown, causing processing to stop and the test to fail
			federate.fedamb.waitForFederateRestoreInitiated( label );
			federate.restore( label );
			federate.quickRestoreComplete();
		}
		
		// wait for the message telling us that the federation has restores
		for( Test13Federate federate : federates )
		{
			federate.fedamb.waitForFederationRestored();
		}
	}

	////////////////////////////////////////////////////////////////////
	/////////// Object Publish and Subscribe helper methods ////////////
	////////////////////////////////////////////////////////////////////
	/**
	 * Attempts to publish the given class handle with the given attribute handles. If there is an
	 * error, Assert.fail() is used to kill the test.
	 * <p/>
	 * <b>This method is for Object Classes (obviously - what with this talk of attributes)</b>
	 */
	public void quickPublish( int classHandle, int... attributeHandles )
	{
		try
		{
			// create the attribute handle set //
			AttributeHandleSet set = TestSetup.getRTIFactory().createAttributeHandleSet();
			for( int handle : attributeHandles )
			{
				set.add( handle );
			}
			
			// attempt to publish the object class
			rtiamb.publishObjectClass( classHandle, set );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickPublish(): " + e.getMessage(), e );
		}
	}

	/**
	 * This method is the same as {@link #quickPublish(int, int[])}, except that it will fetch the
	 * handles on your behalf.
	 * <p/>
	 * <b>This method is for Object Classes (obviously - what with this talk of attributes)</b>
	 */
	public void quickPublish( String className, String... attributeNames )
	{
		try
		{
			// get the class handle //
			int classHandle = rtiamb.getObjectClassHandle( className );
			
			// get the attribute handles //
			int[] attributeHandles = new int[attributeNames.length];
			for( int i = 0; i < attributeNames.length; i++ )
			{
				attributeHandles[i] = rtiamb.getAttributeHandle( attributeNames[i], classHandle );
			}
			
			// pass the call onto the other method //
			this.quickPublish( classHandle, attributeHandles );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickPublish(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * Unpublishes the object class of the provided name. If the object class isn't valid, or the
	 * unpublish request fails, the current test will be failed.
	 */
	public void quickUnpublishOC( String className )
	{
		try
		{
			int classHandle = rtiamb.getObjectClassHandle( className );
			rtiamb.unpublishObjectClass( classHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickUnpublishOC(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * Unpublished the object class with the provided handle. If the object class isn't valid, or
	 * the unpublish request fails, the current test will be failed.
	 */
	public void quickUnpublishOC( int classHandle )
	{
		try
		{
			rtiamb.unpublishObjectClass( classHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickUnpublishOC(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * Attempts to subscribe to the given class handle with the given attribute handles. If there
	 * is an error, Assert.fail() is used to kill the test.
	 */
	public void quickSubscribe( int classHandle, int... attributeHandles )
	{
		try
		{
			// create the attribute handle set //
			AttributeHandleSet set = TestSetup.getRTIFactory().createAttributeHandleSet();
			for( int handle : attributeHandles )
			{
				set.add( handle );
			}
			
			// attempt to publish the object class
			rtiamb.subscribeObjectClassAttributes( classHandle, set );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickSubscribe(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method is the same as {@link #quickSubscribe(int, int[])}, except that it will fetch
	 * the handles on your behalf.
	 */
	public void quickSubscribe( String className, String... attributeNames )
	{
		try
		{
			// get the class handle //
			int classHandle = rtiamb.getObjectClassHandle( className );
			
			// get the attribute handles //
			int[] attributeHandles = new int[attributeNames.length];
			for( int i = 0; i < attributeNames.length; i++ )
			{
				attributeHandles[i] = rtiamb.getAttributeHandle( attributeNames[i], classHandle );
			}
			
			// pass the call onto the other method //
			this.quickSubscribe( classHandle, attributeHandles );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickSubscribe(): " + e.getMessage(), e );
		}
	}

	/**
	 * This method will attempt to subscribe to the identified object class using the given
	 * attributes. The subscription to each attribute is intended to be associated with the
	 * provided region. Handles for the object class and attribute names will be resolved on
	 * behalf of the user. If there is an exception during any of these activities, Assert.fail()
	 * will be used to kill the test.
	 */
	public void quickSubscribeWithRegion( String className, Region region, String... attributes )
	{
		// resolve the class name
		int classHandle = quickOCHandle( className );
		int[] attributeHandles = new int[attributes.length];
		for( int i = 0; i < attributes.length; i++ )
			attributeHandles[i] = quickACHandle( className, attributes[i] );
		
		// fire off the subscription (just use the other method)
		quickSubscribeWithRegion( classHandle, region, attributeHandles );
	}

	/**
	 * This method will attempt to subscribe to the identified object class using the given
	 * attributes. The subscription to each attribute is intended to be associated with the
	 * provided region. If there is an exception during any of these activities, Assert.fail()
	 * will be used to kill the test.
	 */
	public void quickSubscribeWithRegion( int classHandle, Region region, int... attributes )
	{
		try
		{
			// create an AHS from the attribute handles
			AttributeHandleSet handles = TestSetup.getRTIFactory().createAttributeHandleSet();
			for( int handle : attributes )
				handles.add( handle );

			// do the subscribe
			rtiamb.subscribeObjectClassAttributesWithRegion( classHandle, region, handles );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickSubscribeWithRegion(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * Unsubscribe from the given object class, Assert.fail() if there is an error.
	 */
	public void quickUnsubscribe( int objectClassHandle )
	{
		try
		{
			rtiamb.unsubscribeObjectClass( objectClassHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickUnsubscribe(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * Unsubscribe from the given object class, Assert.fail() if there is an error.
	 */
	public void quickUnsubscribe( String objectClassName )
	{
		quickUnsubscribe( quickOCHandle(objectClassName) );
	}

	/////////////////////////////////////////////////////////////////////////
	/////////// Interaction Publish and Subscribe helper methods ////////////
	/////////////////////////////////////////////////////////////////////////
	/**
	 * Attempts to publish the given interaction class handle. If there is an error, Assert.fail()
	 * is used to kill the test.
	 * <p/>
	 * <b>This method is for Interaction Classes only.</b>
	 */
	public void quickPublish( int classHandle )
	{
		try
		{
			rtiamb.publishInteractionClass( classHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickPublish(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method is the same as {@link #quickPublish(int)} except that it will fetch the
	 * interaction handle on your behalf.
	 */
	public void quickPublish( String className )
	{
		try
		{
			this.quickPublish( rtiamb.getInteractionClassHandle(className) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickPublish(): " + e.getMessage(), e );
		}
	}

	/**
	 * Unpublishes the interaction class of the provided name. If the object class isn't valid,
	 * or the unpublish request fails, the current test will be failed.
	 */
	public void quickUnpublishIC( String className )
	{
		try
		{
			int classHandle = rtiamb.getInteractionClassHandle( className );
			rtiamb.unpublishInteractionClass( classHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickUnpublishIC(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * Unpublished the interaction class with the provided handle. If the object class isn't valid,
	 * or the unpublish request fails, the current test will be failed.
	 */
	public void quickUnpublishIC( int classHandle )
	{
		try
		{
			rtiamb.unpublishInteractionClass( classHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickUnpublishIC(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method will attempt to subscribe to the interaction class with the given handle. If
	 * there is an exception during this process, Assert.fail() will be used to kill the test.
	 * <p/>
	 * <b>This method is for interaction classes only</b>
	 */
	public void quickSubscribe( int classHandle )
	{
		try
		{
			rtiamb.subscribeInteractionClass( classHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickSubscribe(): " + e.getMessage(), e );
		}
	}

	/**
	 * This method is the same as {@link #quickSubscribe(int)} except that it will fetch the
	 * interaction class handle on your behalf. Again, if there is an exception during the process
	 * of getting the handle or subscribing to it, an exception will be thrown.
	 * <p/>
	 * <b>This method is for interaction classes only</b>
	 */
	public void quickSubscribe( String className )
	{
		try
		{
			this.quickSubscribe( rtiamb.getInteractionClassHandle(className) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickSubscribe(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method unsubscribes the local federate from the named interaction class. If there is
	 * a problem converting the name into a handle or unsubscribing from the interaction, the
	 * current test will be failed via Assert.fail().
	 */
	public void quickUnsubscribeIC( String className )
	{
		try
		{
			int classHandle = rtiamb.getInteractionClassHandle( className );
			rtiamb.unsubscribeInteractionClass( classHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickUnsubscribeIC(): " + e.getMessage(), e );
		}
	}
	
	/**
	 * Same as {@link #quickSubscribe(int)} except that it includes region data.
	 * <p/>
	 * <b>This method is for interaction classes only</b>
	 */
	public void quickSubscribeWithRegion( int classHandle, Region region )
	{
		try
		{
			rtiamb.subscribeInteractionClassWithRegion( classHandle, region );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickSubscribeWithRegion(): " + e.getMessage(), e );
		}
	}

	/**
	 * Same as {@link #quickSubscribe(String)} except that it includes region data.
	 * <p/>
	 * <b>This method is for interaction classes only</b>
	 */
	public void quickSubscribeWithRegion( String className, Region region )
	{
		try
		{
			this.quickSubscribeWithRegion( rtiamb.getInteractionClassHandle(className), region );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickSubscribeWithRegion(): " + e.getMessage(), e );
		}
	}

	/**
	 * Unsubscribe from the given interaction class using the given class handle and region.
	 * If there is an exception during this process, Assert.fail() will be used to kill the test.
	 */
	public void quickUnsubscribeICWithRegion( int classHandle, Region region )
	{
		try
		{
			rtiamb.unsubscribeInteractionClassWithRegion( classHandle, region );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickICUnsubscribeWithRegion(): " + e.getMessage(), e );
		}
	}

	/////////////////////////////////////////////////////////////
	///////////// Object Management helper methods //////////////
	/////////////////////////////////////////////////////////////
	/**
	 * This method will attempt to register an object instance of the given class handle. If
	 * successful, the instance handle will be returned, if not, Assert.fail() will be used to
	 * kill the test
	 */
	public int quickRegister( int classHandle )
	{
		try
		{
			return rtiamb.registerObjectInstance( classHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickRegister(int): " + e.getMessage(), e );
			return -1;
		}
	}

	/**
	 * This method will attempt to register an object instance of the given class handle with
	 * the given name. If successful, the instance handle will be returned, if not, Assert.fail()
	 * will be used to kill the test
	 */
	public int quickRegister( int classHandle, String objectName )
	{
		try
		{
			return rtiamb.registerObjectInstance( classHandle, objectName );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickRegister(int,String): " + e.getMessage(), e );
			return -1;
		}
	}
	
	/**
	 * This method is the same as {@link #quickRegister(int)}, except that you can pass it the
	 * name of the class you wish to register an instance of (rather than the class handle).
	 */
	public int quickRegister( String className )
	{
		try
		{
			// find the handle for the class
			int cHandle = rtiamb.getObjectClassHandle( className );
			return rtiamb.registerObjectInstance( cHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickRegister(String): " + e.getMessage(), e );
			return -1;
		}
	}
	
	/**
	 * Thism ethod is the same as {@link #quickRegister(int, String)} except that it lets you
	 * secify the class name rather than the class handle. If there is a problem registering
	 * the object, Assert.fail() will be used to kill the test.
	 */
	public int quickRegister( String className, String objectName )
	{
		try
		{
			int classHandle = rtiamb.getObjectClassHandle( className );
			return rtiamb.registerObjectInstance( classHandle, objectName );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickRegister(String,String): " + e.getMessage(), e );
			return -1;
		}
	}
	
	/**
	 * This is the opposite of {@link #quickRegister(String, String)} in that it expects the
	 * registration of the object to fail with an exception. If this doesn't happen, and the
	 * object is registered happily, Assert.fail() will be used to kill the test.
	 */
	public void quickRegisterFail( String className, String objectName )
	{
		try
		{
			int classHandle = rtiamb.getObjectClassHandle( className );
			rtiamb.registerObjectInstance( classHandle, objectName );
			Assert.fail( "Was expecting registration of object with name ["+objectName+
			             "] of type ["+className+"] would fail (but it didn't)" );
		}
		catch( Exception e )
		{
			// success!
		}
	}

	/**
	 * This method is much like {@link #quickRegister(int)}, except that it expects *failure*. If
	 * the request to register the instance does not fail, Assert.fail() will be used to kill the
	 * test. If the attempt to register the instance does fail, this method will return as normal.
	 */
	public void quickRegisterFail( int classHandle )
	{
		try
		{
			rtiamb.registerObjectInstance( classHandle );
			Assert.fail( "Was expecting registration of class [" + classHandle + "] would fail" );
		}
		catch( Exception e )
		{
			// success!
		}
	}
	
	/**
	 * This method is much like {@link #quickRegister(int)}, except that it expects *failure*. If
	 * the request to register the instance does not fail, Assert.fail() will be used to kill the
	 * test. If the attempt to register the instance does fail, this method will return as normal.
	 */
	public void quickRegisterFail( String className )
	{
		quickRegisterFail( quickOCHandle(className) );
	}

	/**
	 * This method will create an instance of the object class identified by the provided
	 * <code>className</code>. The method will associate the given region with each of
	 * the provided attributes in the varargs array. If there is a problem performing any
	 * of these steps, Assert.fail() will be used to kill the test.
	 */
	public int quickRegisterWithRegion( String className, Region theRegion, String... attributes )
	{
		// create the arrays to use in the registration
		Region[] regionArray = new Region[attributes.length];
		for( int i = 0; i < regionArray.length; i++ )
		{
			regionArray[i] = theRegion;
		}

		// convert the class and attribute names
		int classHandle = this.quickOCHandle( className );
		int[] attributeHandles = new int[attributes.length];
		for( int i = 0; i < attributes.length; i++ )
			attributeHandles[i] = this.quickACHandle( className, attributes[i] );
		
		// register the instance and return its handle
		try
		{
			return rtiamb.registerObjectInstanceWithRegion( classHandle, attributeHandles, regionArray );
		}
		catch( Exception e )
		{
			Assert.fail( "Failed to register instance of object class [" + classHandle +
			             "] with region [" + theRegion + "]", e );
			return -1;
		}
	}
	
	/**
	 * This method will create an instance of the object class identified by the provided
	 * <code>classHandle</code>. The method will associate the given region with each of
	 * the provided attributes in the varargs array. If there is a problem performing any
	 * of these steps, Assert.fail() will be used to kill the test.
	 * 
	 * @param classHandle The handle of the object class that the newly registered instance
	 *                    should be a type of
	 * @param theRegion The region that should be associated with all the provided attributes
	 */
	public int quickRegisterWithRegion( int classHandle, Region theRegion, int... attributes )
	{
		// create the arrays to use in the registration
		Region[] regionArray = new Region[attributes.length];
		for( int i = 0; i < regionArray.length; i++ )
		{
			regionArray[i] = theRegion;
		}

		// register the instance and return its handle
		try
		{
			return rtiamb.registerObjectInstanceWithRegion( classHandle, attributes, regionArray );
		}
		catch( Exception e )
		{
			Assert.fail( "Failed to register instance of object class [" + classHandle +
			             "] with region [" + theRegion + "]", e );
			return -1;
		}
	}
	
	/**
	 * This method will attempt to delete the object instance with the given handle. If the
	 * delete attempt fails, Assert.fai() will be used to kill the test.
	 */
	public void quickDelete( int handle, byte[] tag )
	{
		try
		{
			rtiamb.deleteObjectInstance( handle, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickDelete(int,byte[]): " + e.getMessage(), e );
		}
	}
	
	/**
	 * The same as {@link #quickDelete(int, byte[])} except it passes an empty byte[] for the tag.
	 */
	public void quickDelete( int handle )
	{
		try
		{
			rtiamb.deleteObjectInstance( handle, new byte[0] );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickDelete(int): " + e.getMessage(), e );
		}
	}
	
	/**
	 * Like {@link Test13Federate#quickDelete(int)} except that it expects the delete call to
	 * fail with an exception. If no exception is thrown, the current test will be killed using
	 * Assert.fail().
	 */
	public void quickDeleteFail( int handle )
	{
		try
		{
			rtiamb.deleteObjectInstance( handle, new byte[0] );
			Assert.fail( "Expected an exception to be thrown while trying to delete object" );
		}
		catch( Exception e )
		{
			// success!
		}
	}
	
	/**
	 * Attempt to locally delete the object identified by the given handle. If there is an error
	 * doing this, use Assert.fail() to kill the test. Also note that this will remove the object
	 * instance with the given handle from the store in the federate ambassador so that we don't
	 * think we still know about an object that we no longer do.
	 */
	public void quickLocalDelete( int handle )
	{
		try
		{
			rtiamb.localDeleteObjectInstance( handle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickLocalDelete(int): " + e.getMessage(), e );
		}
		
		fedamb.removeObjectInstance( handle, "".getBytes() );
	}
	
	/**
	 * This method will send out a request for the update of each of the attributes identified for
	 * the object identified with the given handle.
	 */
	public void quickProvide( int objectHandle, int... attributes )
	{
		try
		{
			rtiamb.requestObjectAttributeValueUpdate( objectHandle, this.createAHS(attributes) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickProvide(int,int...): " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method will send out a request for updates for each of the provided attributes of each
	 * of the objects of the identified class. 
	 */
	public void quickProvideClass( int classHandle, int... attributes )
	{
		try
		{
			rtiamb.requestClassAttributeValueUpdate( classHandle, this.createAHS(attributes) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickProvideClass(int,int...): " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method fetches the class handle for the object class of the given name. If there is
	 * an exception while this is happening, Assert.fail is used to kill the test.
	 */
	public int quickOCHandle( String className )
	{
		try
		{
			return rtiamb.getObjectClassHandle( className );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching class handle for [" + className + "]", e );
			return -1;
		}
	}

	/**
	 * Returns the name of the class represented by the given class handle (as fetched from the
	 * RTI). If there is a problem getting the name, Assert.fail() will be used to kill the test.
	 */
	public String quickOCName( int classHandle )
	{
		try
		{
			return rtiamb.getObjectClassName( classHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching name for class handle [" + classHandle + "]" );
			return "error";
		}
	}
	
	/**
	 * This method is roughly the same as {@link #quickOCHandle(String)}, except that it is for 
	 * an attribute handle (rather than a class handle)
	 */
	public int quickACHandle( String className, String attName )
	{
		try
		{
			return rtiamb.getAttributeHandle( attName, rtiamb.getObjectClassHandle(className) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching attribute handle for [" + attName +
			             "] of class [" + className + "]", e );
			return -1;
		}
	}

	/**
	 * Returns the name of the attribute represented by the given attribute handle (coming from the
	 * class of the given handle). If there is a problem getting the name, Assert.fail() will be
	 * used to kill the test.
	 */
	public String quickACName( int attributeHandle, int whichClass )
	{
		try
		{
			return rtiamb.getAttributeName( attributeHandle, whichClass );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching name for attribute handle [" +
			             attributeHandle + "] of class [" + whichClass + "]" );
			return "error";
		}
	}
	
	/**
	 * This method fetches the class handle for the interaction class of the given name. If there
	 * is an exception while this is happening, Assert.fail() is used to kill the test.
	 */
	public int quickICHandle( String className )
	{
		try
		{
			return rtiamb.getInteractionClassHandle( className  );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fecthing interaction handle for [" + className + "]", e );
			return -1;
		}
	}

	/**
	 * This method is roughly the same as {@link #quickICHandle(String)}, except that it is for
	 * fetching the handle of a parameter (rather than the interaction class). If there is an
	 * exception while carrying out this action, Assert.fail() is used to kill the test.
	 */
	public int quickPCHandle( String className, String paramName )
	{
		try
		{
			return rtiamb.getParameterHandle( paramName,
			                                  rtiamb.getInteractionClassHandle(className) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception while fetching parameter handle for [" + paramName +
			             "] of class [" + className + "]", e );
			return -1;
		}
	}

	/////////////////////////////////////////////////////////////
	///////////// Object/Interaction Helper Methods /////////////
	/////////////////////////////////////////////////////////////
	/**
	 * This method sends a reflection for the identified object instance, passing the given map of
	 * values as the attributes with the update. The keys for that map should contain the attribute
	 * handles. If there is a problem sending the reflection, Assert.fail() will be used to kill
	 * the test.
	 * 
	 * @param oHandle The handle of the object to send the update for
	 * @param attributes The set of attributes and their values to send with the reflection
	 * @param tag The tag to send with the reflection
	 */
	public void quickReflectWithHandles( int oHandle, Map<Integer,byte[]> attributes, byte[] tag )
	{
		try
		{
			// resolve the attribute names to handles //
			SuppliedAttributes toSend = TestSetup.getRTIFactory().createSuppliedAttributes();
			for( Integer aHandle : attributes.keySet() )
			{
				toSend.add( aHandle, attributes.get(aHandle) );
			}
			
			// send the update //
			rtiamb.updateAttributeValues( oHandle, toSend, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while sending reflection: " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method sends a reflection for the identified object instance, passing the given map of
	 * values as the attributes with the update. The keys for that map should contain the attribute
	 * names (they will be resolved to handles on your behalf). If there is a problem sending the
	 * reflection, Assert.fail() will be used to kill the test.
	 * 
	 * @param oHandle The handle of the object to send the update for
	 * @param attributes The set of attributes and their values to send with the reflection
	 * @param tag The tag to send with the reflection
	 */
	public void quickReflect( int oHandle, Map<String,byte[]> attributes, byte[] tag )
	{
		try
		{
			// get the class of the object instance //
			// we'll need this to resolve the attribute names //
			int oClass = rtiamb.getObjectClass( oHandle );
			
			// resolve the attribute names to handles //
			SuppliedAttributes toSend = TestSetup.getRTIFactory().createSuppliedAttributes();
			for( String aName : attributes.keySet() )
			{
				toSend.add( rtiamb.getAttributeHandle(aName,oClass), attributes.get(aName) );
			}
			
			// send the update //
			rtiamb.updateAttributeValues( oHandle, toSend, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while sending reflection: " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method is much like {@link #quickReflect(int, Map, byte[])} except that it will
	 * automatically generate the content of the attributes to send. The content will be the
	 * raw bytes for the name of the attribute. Thus, if the name of the attribute was "aa",
	 * the bytes would be equal to "aa".getBytes(). The tag will be the same, using the string
	 * "letag".
	 * <p/>
	 * This method will just call {@link #quickReflect(int, Map, byte[])} after having generated
	 * the necessary values.
	 */
	public void quickReflect( int oHandle, String... attributes )
	{
		HashMap<String,byte[]> map = new HashMap<String,byte[]>();
		for( String attribute : attributes )
			map.put( attribute, attribute.getBytes() );
		
		quickReflect( oHandle, map, "letag".getBytes() );
	}
	
	/**
	 * The same as {@link #quickReflect(int, String...)} except that it uses attribute handles,
	 * rather than names. Thus, the value for an attribute handle 123 is equal to "123".getBytes()
	 */
	public void quickReflect( int oHandle, int... attributes )
	{
		HashMap<Integer,byte[]> map = new HashMap<Integer,byte[]>();
		for( int attribute : attributes )
			map.put( attribute, ("" + attribute).getBytes() );
		
		quickReflectWithHandles( oHandle, map, "letag".getBytes() );
	}
	
	/**
	 * This method is the same as {@link #quickReflect(int, String...)} except that it expects the
	 * update request to fail with an exception. If no exception is thrown, Assert.fail() will be
	 * used to fail the test. To do the testing, this method will pass the bytes of the names of
	 * each of the attributes as the values for those attributes and then hand execution off to the
	 * {@link #quickReflectFail(int, Map, byte[])} method to ensure the call actually fails.
	 */
	public void quickReflectFail( int objectHandle, String... attributes )
	{
		HashMap<String,byte[]> map = new HashMap<String,byte[]>();
		for( String attribute : attributes )
			map.put( attribute, attribute.getBytes() );
		
		quickReflectFail( objectHandle, map, "letag".getBytes() );
	}
	
	/**
	 * This method is the same as {@link #quickReflect(int, Map, byte[])}, except that it expects
	 * *failure*. This is useful in the publication tests when wanting to ensure that we can't
	 * reflect non-published attributes. If the reflection doesn't fail, Assert.fail() will be used
	 * to kill the set. If the reflection does fail, this method will return as normal.
	 */
	public void quickReflectFail( int oHandle, Map<String,byte[]> attributes, byte[] tag )
	{
		try
		{
			// get the class of the object instance //
			// we'll need this to resolve the attribute names //
			int oClass = rtiamb.getObjectClass( oHandle );
			
			// resolve the attribute names to handles //
			SuppliedAttributes toSend = TestSetup.getRTIFactory().createSuppliedAttributes();
			for( String aName : attributes.keySet() )
			{
				toSend.add( rtiamb.getAttributeHandle(aName,oClass), attributes.get(aName) );
			}
			
			// send the update //
			rtiamb.updateAttributeValues( oHandle, toSend, tag );
			Assert.fail( "Was expecting an exception during reflection" );
		}
		catch( Exception e )
		{
			// success!
		}
	}
	
	/**
	 * This method sends a reflection for the given object at the given time. The attributes to
	 * send are provided here by name, with the value for each attribute being the bytes that
	 * make up the name of the attributes (using String.getBytes()). If there is a problem
	 * resolving any of the attribute names to handles or doing the reflection, Assert.fail()
	 * will be used to kill the current test.
	 */
	public void quickReflect( int oHandle, double time, String... attributes )
	{
		HashMap<String,byte[]> map = new HashMap<String,byte[]>();
		for( String attribute : attributes )
			map.put( attribute, attribute.getBytes() );
		
		quickReflect( oHandle, map, "letag".getBytes(), time );
	}
	
	/**
	 * This method is basically the same as {@link #quickReflect(int, Map, byte[])}, except that
	 * you can specify the time to send with the reflection.
	 */
	public void quickReflect( int oHandle, Map<String,byte[]> attributes, byte[] tag, double time )
	{
		try
		{
			// get the class of the object instance //
			// we'll need this to resolve the attribute names //
			int oClass = rtiamb.getObjectClass( oHandle );
			
			// resolve the attribute names to handles //
			SuppliedAttributes toSend = TestSetup.getRTIFactory().createSuppliedAttributes();
			for( String aName : attributes.keySet() )
			{
				toSend.add( rtiamb.getAttributeHandle(aName,oClass), attributes.get(aName) );
			}
			
			// send the update //
			rtiamb.updateAttributeValues( oHandle, toSend, tag, createTime(time) );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while sending reflection: " + e.getMessage(), e );
		}
	}

	/**
	 * This method is the same as {@link #quickReflect(int, String...)} except that it expects the
	 * update request to fail with an exception. If no exception is thrown, Assert.fail() will be
	 * used to fail the test. To do the testing, this method will pass the bytes of the names of
	 * each of the attributes as the values for those attributes and then hand execution off to the
	 * {@link #quickReflectFail(int, Map, byte[], double)} method to ensure the call actually fails.
	 */
	public void quickReflectFail( int oHandle, double time, String... attributes )
	{
		HashMap<String,byte[]> map = new HashMap<String,byte[]>();
		for( String attribute : attributes )
			map.put( attribute, attribute.getBytes() );
		
		quickReflectFail( oHandle, map, "letag".getBytes(), time );
	}
	
	/**
	 * The opposite of {@link #quickReflect(int, Map, byte[], double)}. This method expects the
	 * call to fail with an exception. If it doesn't, Assert.fail() is used to kill the test.
	 */
	public void quickReflectFail( int oHandle,
	                              Map<String,byte[]> attributes,
	                              byte[] tag,
	                              double time )
	{
		try
		{
			// get the class of the object instance //
			// we'll need this to resolve the attribute names //
			int oClass = rtiamb.getObjectClass( oHandle );
			
			// resolve the attribute names to handles //
			SuppliedAttributes toSend = TestSetup.getRTIFactory().createSuppliedAttributes();
			for( String aName : attributes.keySet() )
			{
				toSend.add( rtiamb.getAttributeHandle(aName,oClass), attributes.get(aName) );
			}
			
			// send the update //
			rtiamb.updateAttributeValues( oHandle, toSend, tag, createTime(time) );
			
			// fail if we get here without an exception!
			Assert.fail( "Expected updateAttributeValues(tso) to throw an exception" );
		}
		catch( Exception e )
		{
			// success!
		}
	}
	
	/**
	 * This method handles the process of sending an interaction. The type of interaction send is
	 * identified by the given interaction class name. The parameters sent are supplied in the 
	 * given map, with each key being the name of the parameter. This method will attempt to resolve
	 * the handles for the class and parameters on your behalf. If there is a problem creating or
	 * sending the interaction, Assert.fail() will be used to kill the test.
	 * <p/>
	 * <b>NOTE:</b> If you don't want to send any parameters with the interaction, you can just
	 * pass <code>null</code> for the <code>parameters</code> argument.
	 * 
	 * @param clazz The name of the interaction type you wish to send
	 * @param parameters The set of parameters, with the keys being the parameter names and the
	 * value being the desired values to send out to the federation
	 * @param tag The tag to send with the interaction
	 */
	public void quickSend( String clazz, Map<String,byte[]> parameters, byte[] tag )
	{
		this.quickSend( clazz, parameters, tag, PorticoConstants.NULL_TIME );
	}

	/**
	 * This method tries to send an interaction of the given class with the given parameters. The
	 * value for the parameters is equal to the byte[] value of the parameter names (as gotten
	 * from String.getBytes()). If there is a problem resolving the name to handles or an exception
	 * is thrown while trying to send, the current test will be failed with Assert.fail().
	 */
	public void quickSend( String clazz, String... parameters )
	{
		HashMap<String,byte[]> map = new HashMap<String,byte[]>();
		for( String parameter : parameters )
			map.put( parameter, parameter.getBytes() );
		
		this.quickSend( clazz, map, "letag".getBytes(), PorticoConstants.NULL_TIME );
	}

	/**
	 * This method tries to send an interaction of the given class with the given parameters at
	 * the given time. The value for the parameters is equal to the byte[] value of the parameter
	 * names (as gotten from String.getBytes()). If there is a problem resolving the name to handles
	 * or an exception is thrown while trying to send, the current test will be failed with
	 * Assert.fail().
	 */
	public void quickSend( String clazz, double time, String... parameters )
	{
		HashMap<String,byte[]> map = new HashMap<String,byte[]>();
		for( String parameter : parameters )
			map.put( parameter, parameter.getBytes() );
		
		this.quickSend( clazz, map, "letag".getBytes(), time );
	}
	
	/**
	 * This method is the same as {@link #quickSend(String, Map, byte[])} except that it
	 * will send the message with the given time value.
	 */
	public void quickSend( String clazz, Map<String,byte[]> params, byte[] tag, double time )
	{
		try
		{
			// resolve the class name //
			int cHandle = rtiamb.getInteractionClassHandle( clazz );
			
			// resolve the names to handles //
			SuppliedParameters toSend = TestSetup.getRTIFactory().createSuppliedParameters();
			if( params != null )
			{
				for( String pName : params.keySet() )
				{
					toSend.add( rtiamb.getParameterHandle(pName,cHandle), params.get(pName) );
				}
			}
			
			// send the interaction (only send with timestamp if time isn't NULL_TIME)
			if( time == PorticoConstants.NULL_TIME )
				rtiamb.sendInteraction( cHandle, toSend, tag );
			else
				rtiamb.sendInteraction( cHandle, toSend, tag, createTime(time) );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while sending interaction: " + e.getMessage(), e );
		}		
	}

	/**
	 * The same as {@link #quickSend(String, Map, byte[])} except that it works with handles rather
	 * than class/parameter names.
	 */
	public void quickSend( int classHandle, Map<Integer,byte[]> parameters, byte[] tag )
	{
		try
		{
			// create the parameter set to send //
			SuppliedParameters toSend = TestSetup.getRTIFactory().createSuppliedParameters();
			if( parameters != null )
			{
				for( Integer key : parameters.keySet() )
				{
					toSend.add( key, parameters.get(key) );
				}
			}
			
			// send the interaction //
			rtiamb.sendInteraction( classHandle, toSend, tag );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while sending interaction: " + e.getMessage(), e );
		}
	}

	/**
	 * This method is much the same as {@link #quickSend(String, Map, byte[])}, except that it
	 * *expects failure*. If the request to send the interaction does not fail, Assert.fail() is
	 * used to kill the test. If the request does fail, the method returns as normal.
	 */
	public void quickSendFail( String clazz, Map<String,byte[]> parameters, byte[] tag )
	{
		quickSendFail( clazz, parameters, tag, PorticoConstants.NULL_TIME );
	}
	
	/**
	 * This method is much the same as {@link #quickSend(String, Map, byte[],double)}, except that
	 * it *expects failure*. If the request to send the interaction does not fail, Assert.fail() is
	 * used to kill the test. If the request does fail, the method returns as normal. If you pass
	 * {@link PorticoConstants#NULL_TIME} as the time, the non-timestampped version of send will
	 * be used.
	 */
	public void quickSendFail( String clazz, Map<String,byte[]> parameters, byte[] tag, double time )
	{
		try
		{
			// resolve the class name //
			int cHandle = rtiamb.getInteractionClassHandle( clazz );
			
			// resolve the names to handles //
			SuppliedParameters toSend = TestSetup.getRTIFactory().createSuppliedParameters();
			if( parameters != null )
			{
				for( String pName : parameters.keySet() )
				{
					toSend.add( rtiamb.getParameterHandle(pName,cHandle), parameters.get(pName) );
				}
			}
			
			// send the interaction //
			if( time == PorticoConstants.NULL_TIME )
				rtiamb.sendInteraction( cHandle, toSend, tag );
			else
				rtiamb.sendInteraction( cHandle, toSend, tag, createTime(time) );
			Assert.fail( "Was expecting the sending of interaction [" + clazz + "] to fail" );
		}
		catch( Exception e )
		{
			// success!
		}
	}

	/**
	 * The same as {@link #quickSendFail(String, Map, byte[])}, except that it works with handles,
	 * rather than class/parameter names.
	 */
	public void quickSendFail( int classHandle, Map<Integer,byte[]> parameters, byte[] tag )
	{
		try
		{
			// create the parameter set
			SuppliedParameters toSend = TestSetup.getRTIFactory().createSuppliedParameters();
			if( parameters != null )
			{
				for( Integer key : parameters.keySet() )
					toSend.add( key, parameters.get(key) );
			}
			
			// send the interaction
			rtiamb.sendInteraction( classHandle, toSend, tag );
			
			// fail if there wasn't an exception
			Assert.fail( "Was expecting the sending of interaction [" + classHandle + "] to fail" );
		}
		catch( Exception e )
		{
			// success!
		}
	}

	/**
	 * This method is much like {@link #quickSend(String, String...)} except that it
	 * *expects failure*. If the call to send the interaction doesn't fail with an exception,
	 * Assert.fail() will be used to kill the current test.
	 */
	public void quickSendFail( String clazz, String... parameters )
	{
		HashMap<String,byte[]> map = new HashMap<String,byte[]>();
		for( String parameter : parameters )
			map.put( parameter, parameter.getBytes() );
		
		this.quickSendFail( clazz, map, "letag".getBytes(), PorticoConstants.NULL_TIME );
	}

	/**
	 * This method is much like {@link #quickSend(String, double, String...)} except that it
	 * *expects failure*. If the call to send the interaction doesn't fail with an exception,
	 * Assert.fail() will be used to kill the current test.
	 */
	public void quickSendFail( String clazz, double time, String... parameters )
	{
		HashMap<String,byte[]> map = new HashMap<String,byte[]>();
		for( String parameter : parameters )
			map.put( parameter, parameter.getBytes() );
		
		this.quickSendFail( clazz, map, "letag".getBytes(), time );
	}

	//////////////////////////////////////////////////////////////
	//////////// Send Interaction With Region Helpers ////////////
	//////////////////////////////////////////////////////////////
	/**
	 * This method will send an interaction of the given class with the given region. The
	 * parameters mentioned will be sent and the values for those parameters shall be the
	 * byte[] values of the parameter names (Strings). If there is a problem resolving the
	 * names to handles or sending the interaction, the current test will be failed.
	 */
	public void quickSendWithRegion( String clazz, Region region, String... parameters )
	{
		HashMap<String,byte[]> parameterMap = new HashMap<String,byte[]>();
		for( String name : parameters )
			parameterMap.put( name, name.getBytes() );
		
		quickSendWithRegion( clazz, parameterMap, new byte[0], region );
	}
	
	/**
	 * Same as {@link #quickSend(String, Map, byte[])} except that it includes region data.
	 */
	public void quickSendWithRegion( String clazz,
	                                 Map<String,byte[]> parameters,
	                                 byte[] tag,
	                                 Region region )
	{
		this.quickSendWithRegion( clazz, parameters, tag, PorticoConstants.NULL_TIME, region );
	}

	/**
	 * Same as {@link #quickSend(String, Map, byte[], double)} except that it includes region data.
	 */
	public void quickSendWithRegion( String clazz,
	                                 Map<String,byte[]> params,
	                                 byte[] tag,
	                                 double time,
	                                 Region region )
	{
		try
		{
			// resolve the class name //
			int cHandle = rtiamb.getInteractionClassHandle( clazz );
			
			// resolve the names to handles //
			SuppliedParameters toSend = TestSetup.getRTIFactory().createSuppliedParameters();
			if( params != null )
			{
				for( String pName : params.keySet() )
				{
					toSend.add( rtiamb.getParameterHandle(pName,cHandle), params.get(pName) );
				}
			}
			
			// send the interaction (only use a timestamp if time isn't NULL_TIME)
			if( time == PorticoConstants.NULL_TIME )
				rtiamb.sendInteractionWithRegion( cHandle, toSend, tag, region );
			else
				rtiamb.sendInteractionWithRegion( cHandle, toSend, tag, region, createTime(time) );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while sending interaction: " + e.getMessage(), e );
		}
	}

	/**
	 * Same as {@link #quickSend(int, Map, byte[])} except that it includes region data.
	 */
	public void quickSendWithRegion( int classHandle,
	                                 Map<Integer,byte[]> parameters,
	                                 byte[] tag,
	                                 Region region )
	{
		this.quickSendWithRegion(classHandle, parameters, tag, PorticoConstants.NULL_TIME, region);
	}

	/**
	 * Same as {@link #quickSend(String, Map, byte[], double)} except that it includes region data.
	 */
	public void quickSendWithRegion( int classHandle,
	                                 Map<Integer,byte[]> parameters,
	                                 byte[] tag,
	                                 double time,
	                                 Region region )
	{
		try
		{
			// create the parameter set to send //
			SuppliedParameters toSend = TestSetup.getRTIFactory().createSuppliedParameters();
			if( parameters != null )
			{
				for( Integer key : parameters.keySet() )
				{
					toSend.add( key, parameters.get(key) );
				}
			}
			
			// send the interaction (only use a timestamp if time isn't NULL_TIME)
			if( time == PorticoConstants.NULL_TIME )
				rtiamb.sendInteractionWithRegion( classHandle, toSend, tag, region );
			else
				rtiamb.sendInteractionWithRegion( classHandle, toSend, tag, region, createTime(time) );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while sending interaction: " + e.getMessage(), e );
		}
	}
	
	/////////////////////////////////////////////////////////////
	///////////////// Ownership helper methods //////////////////
	/////////////////////////////////////////////////////////////
	public boolean quickIsOwned( int objectHandle, int attributeHandle )
	{
		try
		{
			return rtiamb.isAttributeOwnedByFederate( objectHandle, attributeHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during attribute owner lookup: "+e.getMessage(), e );
			return false; // will never get here, but the method needs it anyway
		}
	}
	
	/**
	 * Issue a query for the ownership of the given attribute of the given object. This method will
	 * wait for the response and return the owner handle. If the handle is -1, it means the
	 * attribute is unowned. If the attribute is 0 it means the attribute is owned by the RTI.
	 */
	public int quickQueryOwnership( int objectHandle, int attributeHandle )
	{
		try
		{
			rtiamb.queryAttributeOwnership( objectHandle, attributeHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception in query attribute ownership: "+e.getMessage(), e );
			return -2; // just to keep the compiler happy
		}
		
		// wait for the response
		Test13FederateAmbassador.OwnershipInfo info =
			fedamb.waitForOwnershipQueryResponse( objectHandle, attributeHandle );

		return info.owner;
	}
	
	/**
	 * This method will request ownership of the provided attributes in the given object. If there
	 * is a problem with this request, the current test will be failed using Assert.fail().
	 */
	public void quickAcquireRequest( int objectHandle, int... attributes )
	{
		try
		{
			// get the attribute handles //
			AttributeHandleSet set = TestSetup.getRTIFactory().createAttributeHandleSet();
			for( int i = 0; i < attributes.length; i++ )
				set.add( attributes[i] );
			
			// make the request
			rtiamb.attributeOwnershipAcquisition( objectHandle, set, new byte[0] );
		}
		catch( Exception e )
		{
			Assert.fail("Unexpected exception requesting attribute aquisition: "+e.getMessage(),e);
		}
	}

	/**
	 * This method will request ownership of the provided attributes in the given object using the
	 * attributeOwnershipAcquisitionIfAvailable() service (attributes will only be acquired if they
	 * are currently unowned). If there is a problem with this request, the current test will be
	 * failed using Assert.fail().
	 */
	public void quickAcquireIfAvailableRequest( int objectHandle, int... attributes )
	{
		try
		{
			// get the attribute handles //
			AttributeHandleSet set = TestSetup.getRTIFactory().createAttributeHandleSet();
			for( int i = 0; i < attributes.length; i++ )
				set.add( attributes[i] );
			
			// make the request
			rtiamb.attributeOwnershipAcquisitionIfAvailable( objectHandle, set );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception requesting attribute aquisition (if available): " +
			             e.getMessage(), e );
		}
	}

	/**
	 * This method is much like {@link #quickAcquireIfAvailableRequest(int, int...)} except that
	 * it expects that acquisition will happen and will wait for the callback notifying the
	 * federate that it has. If it doesn't happen, the current test is failed.
	 */
	public void quickAcquireIfAvailable( int objectHandle, int... attributes )
	{
		quickAcquireIfAvailableRequest( objectHandle, attributes );
		fedamb.waitForOwnershipAcquisition( objectHandle, attributes );
	}

	/**
	 * This method issues a response to an attribute release request, passing the given object
	 * handle and attributes. If there is a problem issuing this response, the current test is
	 * failed.
	 */
	public void quickReleaseResponse( int objectHandle, int... attributes )
	{
		try
		{
			// get the attribute handles //
			AttributeHandleSet set = TestSetup.getRTIFactory().createAttributeHandleSet();
			for( int i = 0; i < attributes.length; i++ )
				set.add( attributes[i] );
			
			// make the request
			rtiamb.attributeOwnershipReleaseResponse( objectHandle, set );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception responding to release request: "+e.getMessage(), e );
		}
	}

	/**
	 * This method sends an ownership acquisition cancellation notification, passing the given
	 * object handle and attributes. If there is a problem making this call (and an exception is
	 * caused), the current test will be failed.
	 */
	public void quickCancelAcquire( int objectHandle, int... attributes )
	{
		try
		{
			AttributeHandleSet set = TestSetup.getRTIFactory().createAttributeHandleSet();
			for( int i = 0; i < attributes.length; i++ )
				set.add( attributes[i] );
			
			rtiamb.cancelAttributeOwnershipAcquisition( objectHandle, set );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception cancelling attribute acquisition request: " +
			             e.getMessage(), e );
		}
	}

	/**
	 * Requests a negotiated ownership divestiture for the specified attributes of the specified
	 * object. If there is an exception during this process, the current test is failed.
	 */
	public void quickDivestRequest( int objectHandle, int... attributes )
	{
		try
		{
			rtiamb.negotiatedAttributeOwnershipDivestiture( objectHandle,
			                                                createAHS(attributes),
			                                                new byte[0] );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception requesting negotiated attribute divest: "+
			             e.getMessage(), e );
		}
	}

	/**
	 * Tells the RTI that the federate is unconditionally divesting the identified attributs of
	 * the specified object.
	 */
	public void quickUnconditionalDivest( int objectHandle, int... attributes )
	{
		try
		{
			AttributeHandleSet ahs = createAHS( attributes );
			rtiamb.unconditionalAttributeOwnershipDivestiture( objectHandle, ahs );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while unconditionally releasing attributes: "+
			             e.getMessage(), e );
		}
	}
	
	/**
	 * Calls negotiatedAttributeOwnershipDivestiture() on the RTIambassador. If this causes an
	 * exception, the current test will be failed.
	 */
	public void quickNegotiatedDivetRequest( int objectHandle, int... attributes )
	{
		try
		{
			rtiamb.negotiatedAttributeOwnershipDivestiture( objectHandle,
			                                                createAHS(attributes),
			                                                new byte[0] );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while requesting a negoriated divest: "+
			             e.getMessage(), e );
		}
	}
	
	/**
	 * This method will go through each of the attributes for the specified object and find out
	 * who owns them through the queryOwnership service. If the owner isn't the expected federate
	 * for any of them, the current test will be failed.
	 */
	public void quickAssertOwnership( int expectedFederate, int objectHandle, int... attributes )
	{
		for( int i = 0; i < attributes.length; i++ )
		{
			int owner = quickQueryOwnership( objectHandle, attributes[i] );
			if( owner != expectedFederate )
			{
				// turn the names into string for better error messages
				String expectedFederateName = ""+expectedFederate;
				if( expectedFederate == Abstract13Test.OWNER_RTI )
					expectedFederateName = "RTI";
				else if( expectedFederate == Abstract13Test.OWNER_UNOWNED )
					expectedFederateName = "UNOWNED";
				
				String actualFederateName = ""+owner;
				if( owner == Abstract13Test.OWNER_RTI )
					actualFederateName = "RTI";
				else if( owner == Abstract13Test.OWNER_UNOWNED )
					actualFederateName = "UNOWNED";

				// fail
				Assert.fail( "Expected owner of attribute ["+attributes[i]+"] in object ["+
				             objectHandle+"] to be federate ["+expectedFederateName+"], but was ["+
				             actualFederateName+"]" );
			}
		}
	}

	/**
	 * This method will go through each attribute handle and query who is the owner, asserting
	 * that it IS THE CURRENT FEDERATE. If the current federate does not own one of the attributes,
	 * the test will be failed.
	 */	
	public void quickAssertIOwn( int objectHandle, int...attributes )
	{
		for( int i = 0; i < attributes.length; i++ )
		{
			int owner = quickQueryOwnership( objectHandle, attributes[i] );
			Assert.assertSame( owner, this.federateHandle, "Expected owner of attribute ["+
			                   attributes[i]+"] in object ["+objectHandle+
			                   "] to be us, but it was not." );
		}		
	}
	
	/**
	 * This method will go through each attribute handle and query who is the owner, asserting
	 * that it IS NOT the current federate. If the current federate does own one of the attributes,
	 * the test will be failed.
	 */
	public void quickAssertIDontOwn( int objectHandle, int... attributes )
	{
		for( int i = 0; i < attributes.length; i++ )
		{
			int owner = quickQueryOwnership( objectHandle, attributes[i] );
			Assert.assertNotSame( owner, this.federateHandle, "Expected owner of attribute ["+
			                      attributes[i]+"] in object ["+objectHandle+
			                      "] to not be us, but it was" );
		}
	}

	/**
	 * Exchange ownership between the current federate (requesting) and the given owner federate.
	 * Exchange requires action by both federates, so this method will trigger each of the current
	 * and owning federates to behave appropriately at the right times.
	 * <p/>
	 * The exchange process:
	 * <ol>
	 *   <li>(This federate) Requests ownership acquisition</li>
	 *   <li>(Owning federate) Wait for ownership request</li>
	 *   <li>(Owning federate) Releases ownership through release response</li>
	 *   <li>(This federate) Waits until it has received ownership</li>
	 * </ol>
	 */
	public void quickExchangeOwnership( Test13Federate owningFederate,
	                                    int theObject,
	                                    int... attributes )
	{
		this.quickAcquireRequest( theObject, attributes );
		owningFederate.fedamb.waitForOwnershipReleaseRequest( theObject, attributes );
		owningFederate.quickReleaseResponse( theObject, attributes );
		this.fedamb.waitForOwnershipAcquisition( theObject, attributes );
	}
	
	/////////////////////////////////////////////////////////////
	//////// Data Distribution Managemetn helper methods ////////
	/////////////////////////////////////////////////////////////
	/**
	 * Fetch the handle for the given space name. If there is an error, Assert.fail() is used 
	 * to kill the test.
	 */
	public int quickSpaceHandle( String spaceName )
	{
		try
		{
			return rtiamb.getRoutingSpaceHandle( spaceName );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during space handle lookup: " + e.getMessage(), e );
			return -1;
		}
	}

	/**
	 * Fetch the handle for the identified dimension in the identified space. The
	 * {@link #quickSpaceHandle(String)} method is used to get the space handle for the given
	 * space name. If there is an error during the request, Assert.fail() will be used to kill
	 * the test.
	 */
	public int quickDimensionHandle( String spaceName, String dimensionName )
	{
		try
		{
			return rtiamb.getDimensionHandle( dimensionName, quickSpaceHandle(spaceName) );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception during dimension handle lookup: " +
			             e.getMessage(), e );
			return -1;
		}
	}

	/**
	 * Create a new region with the given space and numbe of extents. The
	 * {@link #quickSpaceHandle(String)} method is used to get the space handle for the given
	 * space name. If there is an error during the request, Assert.fail() is used to kill the test.
	 */
	public Region quickCreateRegion( String spaceName, int extentCount )
	{
		try
		{
			return rtiamb.createRegion( quickSpaceHandle(spaceName), extentCount );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while creating region (space=" + spaceName +
			             ",extents=" + extentCount + "): " + e.getMessage(), e );
			return null;
		}
	}

	/**
	 * Create a new region using the given space handle and extent count. If there is an error
	 * during the request, Assert.fail() is used to kill the test.
	 */
	public Region quickCreateRegion( int spaceHandle, int extentCount )
	{
		try
		{
			return rtiamb.createRegion( spaceHandle, extentCount );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while creating region (space=" + spaceHandle +
			             ",extents=" + extentCount + "): " + e.getMessage(), e );
			return null;
		}
	}

	/**
	 * Create a new Region using the space handle for "TestSpace" (as defined in the testing fom),
	 * give it a single extent and set the upper and lower bounds for the only dimension in that
	 * extent to the given values. Notify the RTI of the changes and then return the region. This
	 * method is useful for quickly getting simple instances of a region for use in unit testing.
	 * If there is a problem, Assert.fail() will be used to kill the test.
	 */
	public Region quickCreateTestRegion( long lowerBound, long upperBound )
	{
		// create the region using the handle
		Region region = quickCreateRegion( quickSpaceHandle("TestSpace"), 1 );
		try
		{
			int dimensionHandle = quickDimensionHandle( "TestSpace", "TestDimension" );
			region.setRangeLowerBound( 0, dimensionHandle, lowerBound );
			region.setRangeUpperBound( 0, dimensionHandle, upperBound );
			quickModifyRegion( region );
			return region;
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected execption while creating test region: " + e.getMessage(), e );
			return null;
		}
	}
	
	/**
	 * Create a new Region using the space handle for "OtherSpace" (as defined in the testing fom).
	 * Give it a single extent and set the upper and lower bounds for the only dimension to the
	 * values provided in the arguments. Notify the RTI of the changes and then return the region.
	 * If there are problems creating or updating the region, Assert.fail() will be used to kill
	 * the test.
	 */
	public Region quickCreateOtherRegion( long lowerBound, long upperBound )
	{
		// create the region using the handle
		Region region = quickCreateRegion( quickSpaceHandle("OtherSpace"), 1 );
		try
		{
			int dimensionHandle = quickDimensionHandle( "OtherSpace", "OtherDimension" );
			region.setRangeLowerBound( 0, dimensionHandle, lowerBound );
			region.setRangeUpperBound( 0, dimensionHandle, upperBound );
			quickModifyRegion( region );
			return region;
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected execption while creating other region: " + e.getMessage(), e );
			return null;
		}
	}
	
	/**
	 * Notify the RTI of modification to the provided region. If there is an error during the
	 * request, Assert.fail() is used to kill the test.
	 */
	public void quickModifyRegion( Region region )
	{
		try
		{
			rtiamb.notifyOfRegionModification( region );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while notifying RTI of region modification (region:"+
			             region + ")" );
		}
	}

	/**
	 * Return the region token for the provided Region instance. If there is an error during
	 * the request, Assert.fail() is used to kill the test.
	 */
	public int quickGetRegionToken( Region region )
	{
		try
		{
			return rtiamb.getRegionToken( region );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while getting region handle", e );
			return -1;
		}
	}

	/**
	 * Return the Region instance associated with the region token. If there is an error during
	 * the request, Assert.fail() is used to kill the test.
	 */
	public Region quickGetRegion( int regionToken )
	{
		try
		{
			return rtiamb.getRegion( regionToken );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while getting region with token", e );
			return null;
		}
	}

	/**
	 * Associate the provided attributes of the provided object instance with the provided region.
	 * If anything goes wrong, use Assert.fail() to kill the test.
	 */
	public void quickAssociateWithRegion( int objectHandle, Region region, int... attributes )
	{
		try
		{
			rtiamb.associateRegionForUpdates( region, objectHandle, createAHS(attributes) );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while associating region with attributes", e );
		}
	}
	
	/**
	 * Unassociate the provided attributes of the provided object instance with the provided region.
	 * If anything goes wrong, use Assert.fail() to kill the test.
	 */
	public void quickUnassociateWithRegion( int objectHandle, Region region )
	{
		try
		{
			rtiamb.unassociateRegionForUpdates( region, objectHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception while unassociating region with attributes", e );
		}
	}

	/////////////////////////////////////////////////////////////
	//////////////// Time related helper methods ////////////////
	/////////////////////////////////////////////////////////////
	/**
	 * Turns on time constrained and blocks until either the callback is received or a timeout
	 * occurs. If there is a timeout, Assert.fail is used to kill the test.
	 */
	public void quickEnableConstrained()
	{
		try
		{
			rtiamb.enableTimeConstrained();
			fedamb.waitForConstrainedEnabled();
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickEnableConstrained()", e );
		}
	}
	
	/**
	 * Requests that time constrained become enabled (this only issues the request, it will not
	 * wait for the callback). If there is an exception in this call, Assert.fail is used to kill
	 * the test.
	 */
	public void quickEnableConstrainedRequest()
	{
		try
		{
			rtiamb.enableTimeConstrained();
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickEnableConstrainedRequest()" );
		}
	}
	
	/**
	 * This method will disable time constrained and set the appropriate value in the federate
	 * ambassador. If there is an exception, Assert.fail() is used to fail the test.
	 */
	public void quickDisableConstrained()
	{
		try
		{
			rtiamb.disableTimeConstrained();
			fedamb.constrained = false; // set manually, there is no callback
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickDisableConstrained()", e );
		}
	}
	
	/**
	 * Turns on time regulation and blocks until either the callback is received, or a timeout
	 * occurs while waiting nfor it. If there is an exception, Assert.fail is used to kill the test
	 */
	public void quickEnableRegulating( double lookahead )
	{
		try
		{
			LogicalTime time = this.createTime( 0 );
			LogicalTimeInterval interval = this.createInterval( lookahead );
			rtiamb.enableTimeRegulation( time, interval );
			
			// wait for the callback
			fedamb.waitForRegulatingEnabled();
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickEnableRegulating()", e );
		}
	}

	/**
	 * This method is much like {@link #quickEnableRegulating(double)}, except that it only issues
	 * the request, it won't wait for regulating to become enabled before returning. If there is
	 * a problem issuing the regulating request, Assert.fail will be used to kill the test.
	 */
	public void quickEnableRegulatingRequest( double lookahead )
	{
		try
		{
			LogicalTime time = this.createTime( 0 );
			LogicalTimeInterval interval = this.createInterval( lookahead );
			rtiamb.enableTimeRegulation( time, interval );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickEnableRegulatingRequest()", e );
		}
	}
	
	public void quickDisableRegulating()
	{
		try
		{
			rtiamb.disableTimeRegulation();
			fedamb.regulating = false; // set manually, there is no callback
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickDisableRegulating()", e );
		}
	}
	
	public double quickQueryLookahead()
	{
		try
		{
			LogicalTimeInterval lookahead = rtiamb.queryLookahead();
			return ((DoubleTimeInterval)lookahead).getInterval();
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickQueryLookahead()", e );
			return -1.0; // won't actually execute, but need it to keep the compiler happy
		}
	}
	
	public void quickModifyLookahead( double newLookahead )
	{
		try
		{
			rtiamb.modifyLookahead( this.createInterval(newLookahead) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickModifyLookahead()", e );
		}
	}
	
	public double quickQueryCurrentTime()
	{
		try
		{
			return decodeTime( rtiamb.queryFederateTime() );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickQueryCurrentTime()", e );
			return -1.0;
		}
	}
	
	/**
	 * Request a time advance to the given time. If there is a problem, Assert.fail() will be used
	 * to kill the test.
	 */
	public void quickAdvanceRequest( double newTime )
	{
		try
		{
			rtiamb.timeAdvanceRequest( createTime(newTime) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickAdvanceRequest(" + newTime + ")", e );
		}
	}
	
	/**
	 * Same as {@link #quickAdvanceRequest(double)}, except that it calls
	 * <code>timeAdvanceRequestAvailable()</code>, rather than <code>timeAdvanceRequest()</code>.
	 */
	public void quickAdvanceRequestAvailable( double newTime )
	{
		try
		{
			rtiamb.timeAdvanceRequestAvailable( createTime(newTime) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickAdvanceRequestAvailable(" + newTime + ")", e );
		}
	}
	
	/**
	 * Same as {@link #quickAdvanceRequest(double)}, except that it calls
	 * <code>nextEventRequest()</code>, rather than <code>timeAdvanceRequest()</code>.
	 */
	public void quickNextEventRequest( double maxTime )
	{
		try
		{
			rtiamb.nextEventRequest( createTime(maxTime) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during quickNextEventRequest(" + maxTime + ")", e );
		}
	}
	
	/**
	 * This method builds on {@link #quickAdvanceRequest(double)} by making a time advancement
	 * request and then waiting blocking until either the advance comes through, or a timeout
	 * occurs.
	 */
	public void quickAdvanceAndWait( double newTime )
	{
		// make the advancement request
		this.quickAdvanceRequest( newTime );
		
		// wait for the advance to be granted
		fedamb.waitForTimeAdvance( newTime );
	}

	/**
	 * This method will advance all federates associated with the same test as this federate to
	 * the given time. This is just a helper method to handle the advancement process when multiple
	 * federates are involved. Doing this with pure requests/waitFor's requires a couple of extra
	 * steps (for example, you can only advanceAndWait() in the last regulating federate because if
	 * you do this on a constrained federate it might not give a regulating federate a chance to
	 * advance, thus causing the wait to fail - and other such circumstances).
	 * <p/>
	 * The method will get access to all the joined federates from the {@link Abstract13Test} and
	 * will invoke requests on them, before invoking advances on them later when it should be safe.
	 * @param newTime
	 */
	public void quickAdvanceFederation( double newTime )
	{
		Set<Test13Federate> federates = test.joinedFederates();
		
		// put the requests out there for each federate
		for( Test13Federate federate : federates )
		{
			federate.quickAdvanceRequest( newTime );
		}
		
		// now wait for all the advance granted callbacks
		for( Test13Federate federate : federates )
		{
			federate.fedamb.waitForTimeAdvance( newTime );
		}
	}
	
	/**
	 * Issues a flushQueueRequest() on the RTIambassador and kills the current test if this call
	 * results in an exception.
	 */
	public void quickFlushQueueRequest( double upToThisTime )
	{
		try
		{
			rtiamb.flushQueueRequest( createTime(upToThisTime) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception during flushQueueRequest("+upToThisTime+")", e );
		}
	}
	
	////////////////////////////////////////////////////////////
	/////////////// Asynchronous Delivery Methods //////////////
	////////////////////////////////////////////////////////////
	/**
	 * This method enabled asynchronous delivery (because it's annoying). If there is an exception
	 * during this process, Assert.fail() is used to kill the test.
	 */
	public void quickEnableAsyncDelivery()
	{
		try
		{
			rtiamb.enableAsynchronousDelivery();
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception enabling async delivery: " + e.getMessage(), e );
		}
	}

	/**
	 * This method disabled asynchronous delivery. If there is an exception during this process,
	 * Assert.fail() is used to kill the test.
	 */
	public void quickDisableAsyncDelivery()
	{
		try
		{
			rtiamb.disableAsynchronousDelivery();
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception disabling async delivery: " + e.getMessage(), e );
		}
	}

	////////////////////////////////////////////////////////////
	////////////// Support Service Helper Methods //////////////
	////////////////////////////////////////////////////////////
	/**
	 * Returns the name of the object instance with the given handle (name fetched from the RTI,
	 * not stored locally). If there is a problem, Assert.fail() is used to kill the test.
	 */
	public String quickObjectName( int instanceHandle )
	{
		try
		{
			return rtiamb.getObjectInstanceName( instanceHandle );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception fetching object instance name", e );
			return null;
		}
	}

	////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////
	/**
	 * This method will call the RTIambassador tick() call. If there is an exception, Assert.fail()
	 * will be used to kill the test.
	 */
	public void quickTick()
	{
		try
		{
			// tick ourselves first, incase there was a problem and we're not in the tick set
			rtiamb.tick();
			
			// tick all the other federates in the tick set
			//for( Test13Federate temp : Test13Federate.TICKSET )
			//{
				// don't tick ourselves, we've already done that
			//	if( temp != this )
			//		temp.rtiamb.tick();
			//}
		}
		catch( Exception e )
		{
			Assert.fail( "There was an exception while tick()'ing: " + e.getMessage(), e );
		}
	}

	/**
	 * This method will call the RTIambassador tick(double,double) call. If there is an exception,
	 * Assert.fail() will be used to kill the test.
	 */
	public void quickTick( double min, double max )
	{
		try
		{
			rtiamb.tick( min, max );
		}
		catch( Exception e )
		{
			Assert.fail( "There was an exception while tick(min,max)'ing: " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method will tell the RTI to deliver a *single* callback, waiting for at most "wait"
	 * seconds for one to show up if there are currently no messages for processing. This method
	 * uses a dirty hack to get at this functionality, which is not part of the HLA 1.3 spec.
	 */
	public void quickTickSingle( double wait )
	{
		try
		{
			((org.portico.impl.hla13.Rti13Ambassador)rtiamb).getHelper().getLrc().tickSingle(wait);
		}
		catch( Exception e )
		{
			Assert.fail( "There was an exception while tickSingle()'ing: " + e.getMessage(), e );
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void killActiveFederates()
	{
		for( Test13Federate federate : ACTIVE_FEDERATES )
		{
			((Rti13Ambassador)federate.rtiamb).getHelper().getLrc().stopLrc();
		}
		
		ACTIVE_FEDERATES.clear();
	}
}
