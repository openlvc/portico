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
package hlaunit.hla13.common;


import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.HLA13Set;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.testng.Assert;

import hla.rti.ArrayIndexOutOfBounds;
import hla.rti.AttributeHandleSet;
import hla.rti.EventRetractionHandle;
import hla.rti.FederateInternalError;
import hla.rti.InvalidFederationTime;
import hla.rti.LogicalTime;
import hla.rti.ObjectNotKnown;
import hla.rti.ReceivedInteraction;
import hla.rti.ReflectedAttributes;
import hla.rti.jlc.NullFederateAmbassador;
import hlaunit.CommonSetup;

/**
 * <b>NOTE:</b> I have replaced all the timeout settings with a single, Test13FederateAmbassador
 * specific value (set via the get/set defaultTimeout methods). So if you see a mention of a
 * timeout the doco for any methods, you'll know I now mean that default value.
 */
public class Test13FederateAmbassador extends NullFederateAmbassador
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Test13Federate federate;
	public Map<String,byte[]> announced;
	public Set<String> synched;
	public String syncSucceeded; // last sync point to get a "success" message for
	public String syncFailed; // last sync point to get a "failure" message for
	
	public boolean constrained;
	public boolean regulating;
	public double  logicalTime;

	private ActiveSR currentSave;
	private ActiveSR currentRestore;
	private String successfulRestoreRequest;
	private String failedRestoreRequest;
	
	protected HashMap<Integer,Test13Instance> instances;
	protected List<Test13Instance> discovered;
	protected HashSet<Integer> roUpdated;
	protected HashSet<Integer> tsoUpdated;
	protected HashSet<Integer> roRemoved;
	protected HashSet<Integer> tsoRemoved;
	protected HashMap<Integer,Set<Integer>> updatesRequested;
	
	protected List<Test13Interaction> roInteractions;
	protected List<Test13Interaction> tsoInteractions;
	
	// counts of activities, they're useful sometimes
	private HashMap<Integer,Integer> roUpdateCount;
	private HashMap<Integer,Integer> tsoUpdateCount;
	private HashMap<Integer,Integer> roInteractionCount;
	private HashMap<Integer,Integer> tsoInteractionCount;
	
	// ownership acquisition notification cache
	private HashMap<Integer,Set<Integer>> attributesOffered;
	private HashMap<Integer,Set<Integer>> attributesAcquired;
	private HashMap<Integer,Set<Integer>> attributesDivested;
	private HashMap<Integer,Set<Integer>> attributesUnavailable;
	private HashMap<Integer,Set<Integer>> attributesRequestedForRelease;
	private HashMap<Integer,Set<Integer>> attributesCancelled;
	private List<OwnershipInfo> attributeOwnershipQueryResponses;

	/** should a check to see if an event was received in the past be run in callbacks? defaults
	    to true, but can be necessary to turn off for things like flushQueueRequest checking */
	public boolean checkForPastEvent = true;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public Test13FederateAmbassador( Test13Federate federate )
	{
		this.federate = federate;
		
		this.announced = new HashMap<String,byte[]>();
		this.synched = new HashSet<String>();
		this.syncSucceeded = "";
		this.syncFailed = "";
		
		this.constrained = false;
		this.regulating = false;
		this.logicalTime = -1;
		
		this.currentSave = new ActiveSR();
		this.currentRestore = new ActiveSR();
		this.successfulRestoreRequest = null;
		this.failedRestoreRequest = null;
		
		this.instances = new HashMap<Integer,Test13Instance>();
		this.discovered = new Vector<Test13Instance>();
		this.roUpdated = new HashSet<Integer>();
		this.tsoUpdated = new HashSet<Integer>();
		this.roRemoved = new HashSet<Integer>();
		this.tsoRemoved = new HashSet<Integer>();
		this.updatesRequested = new HashMap<Integer,Set<Integer>>();
		
		this.roInteractions = new Vector<Test13Interaction>();
		this.tsoInteractions = new Vector<Test13Interaction>();
		
		this.roUpdateCount = new HashMap<Integer,Integer>();
		this.tsoUpdateCount = new HashMap<Integer,Integer>();
		this.roInteractionCount = new HashMap<Integer,Integer>();
		this.tsoInteractionCount = new HashMap<Integer,Integer>();
		
		this.attributesOffered = new HashMap<Integer,Set<Integer>>();
		this.attributesAcquired = new HashMap<Integer,Set<Integer>>();
		this.attributesDivested = new HashMap<Integer,Set<Integer>>();
		this.attributesUnavailable = new HashMap<Integer,Set<Integer>>();
		this.attributesRequestedForRelease = new HashMap<Integer,Set<Integer>>();
		this.attributesCancelled = new HashMap<Integer,Set<Integer>>();
		this.attributeOwnershipQueryResponses = new ArrayList<OwnershipInfo>();
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	protected void saveToStream( ObjectOutput output ) throws Exception
	{
		output.writeObject( this.announced );
		output.writeObject( this.synched );
		output.writeUTF( this.syncSucceeded );
		output.writeUTF( this.syncFailed );
		
		output.writeBoolean( constrained );
		output.writeBoolean( regulating );
		output.writeDouble( logicalTime );
		
		output.writeObject( this.instances );
		output.writeObject( this.discovered );
		output.writeObject( this.roUpdated );
		output.writeObject( this.tsoUpdated );
		output.writeObject( this.roRemoved );
		output.writeObject( this.tsoRemoved );
		output.writeObject( this.updatesRequested );
		output.writeObject( this.roInteractions );
		output.writeObject( this.tsoInteractions );
		
		output.writeObject( this.roUpdateCount );
		output.writeObject( this.tsoUpdateCount );
		output.writeObject( this.roInteractionCount );
		output.writeObject( this.tsoInteractionCount );
		
		output.writeObject( this.attributesOffered );
		output.writeObject( this.attributesAcquired );
		output.writeObject( this.attributesDivested );
		output.writeObject( this.attributesUnavailable );
		output.writeObject( this.attributesRequestedForRelease );
		output.writeObject( this.attributesCancelled );
		output.writeObject( this.attributeOwnershipQueryResponses );
	}
	
	@SuppressWarnings("unchecked")
	protected void restoreFromStream( ObjectInput input ) throws Exception
	{
		this.announced = (HashMap<String,byte[]>)input.readObject();
		this.synched = (HashSet<String>)input.readObject();
		this.syncSucceeded = input.readUTF();
		this.syncFailed = input.readUTF();
		
		this.constrained = input.readBoolean();
		this.regulating = input.readBoolean();
		this.logicalTime = input.readDouble();
		
		this.instances = (HashMap<Integer,Test13Instance>)input.readObject();
		this.discovered = (Vector<Test13Instance>)input.readObject();
		this.roUpdated = (HashSet<Integer>)input.readObject();
		this.tsoUpdated = (HashSet<Integer>)input.readObject();
		this.roRemoved = (HashSet<Integer>)input.readObject();
		this.tsoRemoved = (HashSet<Integer>)input.readObject();
		this.updatesRequested = (HashMap<Integer,Set<Integer>>)input.readObject();
		this.roInteractions = (Vector<Test13Interaction>)input.readObject();
		this.tsoInteractions = (Vector<Test13Interaction>)input.readObject();
		
		this.roUpdateCount = (HashMap<Integer,Integer>)input.readObject();
		this.tsoUpdateCount = (HashMap<Integer,Integer>)input.readObject();
		this.roInteractionCount = (HashMap<Integer,Integer>)input.readObject();
		this.tsoInteractionCount = (HashMap<Integer,Integer>)input.readObject();
		
		this.attributesOffered = (HashMap<Integer,Set<Integer>>)input.readObject();
		this.attributesAcquired = (HashMap<Integer,Set<Integer>>)input.readObject();
		this.attributesDivested = (HashMap<Integer,Set<Integer>>)input.readObject();
		this.attributesUnavailable = (HashMap<Integer,Set<Integer>>)input.readObject();
		this.attributesRequestedForRelease = (HashMap<Integer,Set<Integer>>)input.readObject();
		this.attributesCancelled = (HashMap<Integer,Set<Integer>>)input.readObject();
		this.attributeOwnershipQueryResponses = (ArrayList<OwnershipInfo>)input.readObject();
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Helper Methods //////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public long getTimeout()
	{
		return System.currentTimeMillis() + CommonSetup.TIMEOUT+10;
	}
	
	private void tick()
	{
		try
		{
			federate.quickTick();
		}
		catch( Exception e )
		{
			throw new TimeoutException( "Problem while ticking: " + e.getMessage(), e );
		}
	}
	
	public HashMap<Integer,Test13Instance> getInstances()
	{
		return this.instances;
	}
	
	public List<Test13Interaction> getTsoInteractions()
	{
		return this.tsoInteractions;
	}
	
	public int getRoUpdateCount( int objectHandle )
	{
		if( this.roUpdateCount.containsKey(objectHandle) )
			return this.roUpdateCount.get( objectHandle );
		else
			return 0;
	}
	
	public int getTsoUpdateCount( int objectHandle )
	{
		if( this.tsoUpdateCount.containsKey(objectHandle) )
			return this.tsoUpdateCount.get( objectHandle );
		else
			return 0;
	}
	
	public int getUpdateCount( int objectHandle )
	{
		return getRoUpdateCount(objectHandle) + getTsoUpdateCount(objectHandle);
	}
	
	public int getRoInteractionCount( int classHandle )
	{
		if( roInteractionCount.containsKey(classHandle) )
			return this.roInteractionCount.get( classHandle );
		else
			return 0;
	}
	
	public int getTsoInteractionCount( int classHandle )
	{
		if( tsoInteractionCount.containsKey(classHandle) )
			return this.tsoInteractionCount.get( classHandle );
		else
			return 0;
	}
	
	public int getInteractionCount( int classHandle )
	{
		return getRoInteractionCount(classHandle) + getTsoInteractionCount(classHandle);
	}
	
	private void incrementCounter( Map<Integer,Integer> map, int handle )
	{
		if( map.containsKey(handle) )
			map.put( handle, map.get(handle)+1 );
		else
			map.put( handle, 1 );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Synchronization Helper Methods /////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Waits <code>timeout</code> seconds for the sync point <code>label</code> to be announced.
	 * It it is already announced or is announced before the time out, the tag is returned. If
	 * it is not announced before the timeout, Assert.fail() will be used.
	 */
	public byte[] waitForSyncAnnounce( String label )
	{
		long finishTime = getTimeout();
		while( announced.containsKey(label) == false )
		{
			// check the time to see if it is up
			if( finishTime < System.currentTimeMillis() )
			{
				//return null;
				Assert.fail( "Timeout while waiting for announcement of syncpoint ["+label+"]" );
			}
			
			// tick away
			tick();
		}

		// we have the announcement
		return announced.get( label );
	}
	
	/**
	 * This method is the inverse of {@link #waitForSyncAnnounce(String)} except that it is to
	 * be used when you are *NOT* expecting an announcement to be received. It will wait for a
	 * timeout to occur, and if the label is not announced before that point, the method will
	 * return happily. If the point IS announced (or has been previously announced), Assert.fail()
	 * will be used to kill the test.
	 */
	public void waitForSyncAnnounceTimeout( String label )
	{
		long finishTime = getTimeout();
		while( true )
		{
			// tick for callbacks
			tick();
			
			// see if the point has been announced
			if( isAnnounced(label) )
			{
				// if we get here, it means we didn't timeout. fail the test
				Assert.fail( "Received announcement of Sync Point [" + label +
				             "] but was EXPECTING A TIMEOUT!" );
			}
			else if( finishTime < System.currentTimeMillis() )
			{
				// timeout has gone, return
				return;
			}
		}
	}
	
	/**
	 * Returns true if the given sync point has been announced, false otherwise
	 */
	public boolean isAnnounced( String label )
	{
		return this.announced.containsKey( label );
	}
	
	/**
	 * This method will wait for the given time (in seconds) until a registration result for the
	 * given sync point is received. If the timeout occurs or there is a problem ticking, a
	 * TimeoutException will be thrown. If a success message is received, true is returned, if a
	 * failure message is received, false is returned.
	 */
	public boolean waitForSyncResult( String label )
	{
		// clear the current values //
		synchronized( this )
		{
			this.syncFailed = "";
			this.syncSucceeded = "";
		}
		
		// wait for the result //
		long finishTime = getTimeout();
		while( this.syncSucceeded.equals(label) == false &&
			   this.syncFailed.equals(label) == false )
		{
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for sync point announce result" );
			}

			tick();
		}

		// we have the label //
		if( this.syncSucceeded.equals(label) )
		{
			return true;
		}
		else if( this.syncFailed.equals(label) )
		{
			return false;
		}
		else
		{
			throw new TimeoutException( "Timeout waitingn for sync point announce result" );
		}
	}
	
	/**
	 * This method is much like {@link #waitForSyncResult(String)}, except that you can specify
	 * what you are expecting the result to be. If the actual result differs from that,
	 * Assert.fail() will be used to kill the test.
	 */
	public void waitForSyncResult( String label, boolean expectedResult )
	{
		if( waitForSyncResult(label) != expectedResult )
		{
			Assert.fail( "Didn't get expected result for registration of sync point [" +
			             label + "]. Was " + (!expectedResult) + ", expected " + expectedResult );
		}
	}

	/**
	 * Wait the given number of seconds for the federation to synchronize on the syncpoint with
	 * the given label. 
	 */
	public void waitForSynchronized( String label )
	{
		long finishTime = getTimeout();
		while( isSynchronized(label) == false )
		{
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for synchronize on [" + label + "]" );
			}
			
			tick();
		}
	}
	
	/**
	 * The same as {@link #waitForSynchronized(String)} except that it expects to fail. It will
	 * wait for a timeout period, and if that timeout is reached, the method will return happily.
	 * If the synchronized notification is reached before the timeout, Assert.fail() is used to
	 * kill the test.
	 */
	public void waitForSynchronizedTimeout( String label )
	{
		long finishTime = getTimeout();
		while( isSynchronized(label) == false )
		{
			if( finishTime < System.currentTimeMillis() )
			{
				return;
			}
			
			tick();
		}
		
		throw new TimeoutException( "Synchronized on point before expected timeout [" +label+ "]" );
	}
	
	/**
	 * Has the federation synchronized on the given label?
	 */
	public boolean isSynchronized( String label )
	{
		return this.synched.contains( label );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Save & Restore Helper Methods /////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Tick until a federation save using the given label is initiated. If that doesn't happen
	 * in a timely fashion, throw a TimeoutException.
	 */
	public void waitForSaveInitiated( String label )
	{
		long finishTime = getTimeout();
		while( currentSave.isInitiated(label) == false )
		{
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for initiate save [" +label+ "]" );
			}
			
			tick();
		}
	}
	
	/**
	 * The opposite of {@link #waitForSaveInitiated(String)}, this method expects a
	 * timeout to occur and will fail the current test if one doesn't happen.
	 */
	public void waitForSaveInitiatedTimeout( String label )
	{
		try
		{
			waitForSaveInitiated( label );
			Assert.fail( "Expected timeout waiting for save initiated with label ["+label+
			             "], but received initiation notice" );
		}
		catch( TimeoutException te )
		{
			// success!
		}
	}

	/**
	 * Wait until the federation has successfully saved. If this notification isn't received in
	 * a timely manner, throw a TimeoutException.
	 */
	public void waitForFederationSaved()
	{
		long finishTime = getTimeout();
		while( currentSave.isComplete() == false )
		{
			if( finishTime < System.currentTimeMillis() )
				break;
			
			tick();
		}

		if( currentSave.isFailure() )
			throw new TimeoutException( "Timeout waiting for federation to save, got failure notice" );
		else if( currentSave.isSuccess() == false )
			throw new TimeoutException( "Timeout waiting for federation to save, got no response" );
		else
			return;
	}

	/**
	 * The opposite of {@link #waitForFederationSaved()}, this method expects a timeout to
	 * occur and will fail the current test if one doesn't happen.
	 */
	public void waitForFederationSavedTimeout()
	{
		try
		{
			waitForFederationSaved();
			Assert.fail( "Expected timeout waiting for federation saved notification"+
			             "but received notice" );
		}
		catch( TimeoutException te )
		{
			// success!
		}
	}

	/**
	 * Wait until the federate has received a callback indicating that the save was not successful.
	 * If a success notice is received, or no notice at all is received, an exception is thrown.
	 * a timely manner, throw a TimeoutException.
	 */
	public void waitForFederationNotSaved()
	{
		long finishTime = getTimeout();
		while( currentSave.isComplete() == false )
		{
			if( finishTime < System.currentTimeMillis() )
				break;
			
			tick();
		}

		if( currentSave.isSuccess() )
		{
			throw new TimeoutException( "Timeout waiting for \"federation not saved\" notice: "+
			                            " got federation saved notice" );
		}
		else if( currentSave.isFailure() == false )
		{
			throw new TimeoutException( "Timeout waiting for \"federation to saved\" notice: "+
			                            "no response" );
		}
		else
		{
			return;
		}
	}

	/**
	 * Wait for a notification that the federation restore has begun. If this isn't received in a
	 * timely fashion, throw an exception.
	 */
	public void waitForFederationRestoreBegun()
	{
		long finishTime = getTimeout();
		while( currentRestore.isBegun() == false )
		{
			if( finishTime < System.currentTimeMillis() )
				break;
			
			tick();
		}
		
		if( currentRestore.isBegun() == false )
			throw new TimeoutException("Timeout waiting for restore to begin, received no notice");
	}

	/**
	 * The opposite of {@link #waitForFederationRestoreBegun()}, this method expects a timeout to
	 * occur and will fail the current test if one doesn't happen.
	 */
	public void waitForFederationRestoreBegunTimeout()
	{
		try
		{
			waitForFederationRestoreBegun();
			Assert.fail( "Expected timeout waiting for federation restore begun notification"+
			             "but received notice" );
		}
		catch( TimeoutException te )
		{
			// success!
		}
	}

	/**
	 * Wait for a notification that a federation restore has been initiated with the given label.
	 * If this happens, return the federation handle we were given. If not, throw an exception.
	 */
	public int waitForFederateRestoreInitiated( String label )
	{
		long finishTime = getTimeout();
		while( currentRestore.isInitiated(label) == false )
		{
			if( finishTime < System.currentTimeMillis() )
				break;
			
			tick();
		}
		
		if( currentRestore.isInitiated(label) )
		{
			return currentRestore.federateHandle;
		}
		else
		{
			throw new TimeoutException( "Timeout waiting for restore to be initiated with label ["+
			                            label+"]" );
		}
	}

	/**
	 * The opposite of {@link #waitForFederateRestoreInitiated(String)}, this method expects a
	 * timeout to occur and will fail the current test if one doesn't happen.
	 */
	public void waitForFederationRestoreInitiatedTimeout( String label )
	{
		try
		{
			waitForFederateRestoreInitiated( label );
			Assert.fail( "Expected timeout waiting for federate restore initiated notification"+
			             "but received notice" );
		}
		catch( TimeoutException te )
		{
			// success!
		}
	}

	/**
	 * Wait for a notification from the RTI that the federation restore has succeeded. If we
	 * get a failure notice or no notice at all, a TimeoutException is thrown.
	 */
	public void waitForFederationRestored()
	{
		long finishTime = getTimeout();
		while( currentRestore.isComplete() == false )
		{
			if( finishTime < System.currentTimeMillis() )
				break;
			
			tick();
		}
		
		if( currentRestore.isFailure() )
		{
			throw new TimeoutException( "Timeout waiting for \"federation restored\" notice, got "+
			                            "failure notice instead" );
		}
		else if( currentRestore.isSuccess() == false )
		{
			throw new TimeoutException( "Timeout waiting for \"federation restored\" notice, got "+
			                            "no notice at all" );
		}
	}

	/**
	 * The opposite of {@link #waitForFederationRestored()}, this method expects a timeout to
	 * occur and will fail the current test if one doesn't happen.
	 */
	public void waitForFederationRestoredTimeout()
	{
		try
		{
			waitForFederationRestored();
			Assert.fail( "Expected timeout waiting for federation restored notification"+
			             "but received notice" );
		}
		catch( TimeoutException te )
		{
			// success!
		}
	}

	/**
	 * Wait for a notification from the RTI that the federation restore has not succeeded. If we
	 * get a success notice or no notice at all, a TimeoutException is thrown.
	 */
	public void waitForFederationNotRestored()
	{
		long finishTime = getTimeout();
		while( currentRestore.isComplete() == false )
		{
			if( finishTime < System.currentTimeMillis() )
				break;
			
			tick();
		}
		
		if( currentRestore.isSuccess() )
		{
			throw new TimeoutException( "Timeout waiting for \"not restored\" notice, got "+
			                            "success notice instead" );
		}
		else if( currentRestore.isFailure() == false )
		{
			throw new TimeoutException( "Timeout waiting for \"not restored\" notice, got "+
			                            "no notice at all" );
		}
	}

	/**
	 * Wait until the ambassador receives notification that the federation restore with the given
	 * label that they attempted to initiate was successful. If it doesn't happen in a timely
	 * fashion, a TimeoutException is thrown.
	 */
	public void waitForRestoreRequestSuccess( String label )
	{
		long finishTime = getTimeout();
		while( label.equals(this.successfulRestoreRequest) == false )
		{
			if( finishTime < System.currentTimeMillis() )
				throw new TimeoutException( "Timeout waiting for initiate restore success notice" );
			
			tick();
		}
	}

	/**
	 * Wait until the ambassador receives notification that the federation restore with the given
	 * label that they attempted to initiate has FAILED. If it doesn't happen in a timely fashion,
	 * a TimeoutException is thrown.
	 */
	public void waitForRestoreRequestFailure( String label )
	{
		long finishTime = getTimeout();
		while( label.equals(this.failedRestoreRequest) == false )
		{
			if( finishTime < System.currentTimeMillis() )
				throw new TimeoutException( "Timeout waiting for initiate restore failed notice" );
			
			tick();
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// Object Management Helper Methods /////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Block execution until we get a discovery of an instance with the given handle. Only wait
	 * for the given number of seconds (in timeout). If we don't discover the instance by then,
	 * throw a TimeoutException. If the instance has already been discovered, this method will
	 * return right away.
	 */
	public void waitForDiscovery( int instanceHandle )
	{
		// check to see if we already have this instance //
		if( this.instances.containsKey(instanceHandle) )
		{
			return;
		}
		
		// we haven't discovered this instance yet, wait for it //
		long finishTime = getTimeout();
		while( instances.containsKey(instanceHandle) == false )
		{
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for discovery of instance [" +
				                            instanceHandle + "]" );
			}
			
			tick();
		}
	}

	/**
	 * This method will block until we get a discovery callback for an object instance that has
	 * the given instanceHandle. If the discovery isn't received before a timeout, an exception
	 * will be thrown. Once we have discovered the instance, this method will validate that the
	 * object class the instance was discovered as is equal to the <code>expectedClass</code>.
	 * If it isn't, Assert.fail() will be used to kill the test.
	 * 
	 * @param instanceHandle The handle of the object instance to wait for discovery of
	 * @param expectedClass The object class we expect to be associated with the instance when
	 *                      we get the discovery callback for it
	 * @throws TimeoutException If the discovery takes too long to turn up
	 */
	public void waitForDiscoveryAs( int instanceHandle, int expectedClass )
	{
		// wait for the discovery
		waitForDiscovery( instanceHandle );
		
		// validate that the instance is of the expected class
		Test13Instance instance = this.instances.get( instanceHandle );
		if( instance.getClassHandle() != expectedClass )
		{
			Assert.fail( "Expected to discover object [" + instanceHandle + "] as instance of [" +
			             expectedClass + "]: was discovered as instance of [" + 
			             instance.getClassHandle() + "]" );
		}
	}

	/**
	 * This method is the opposite of {@link #waitForDiscovery(int)}. It will wait to be notified
	 * of the creation of an instance by the given handle, but if it gets one (or the instance
	 * already exists) before the timeout, it will assert an error. If the timeout occurs, it will
	 * return happily.
	 */
	public void waitForDiscoveryTimeout( int instanceHandle )
	{
		try
		{
			waitForDiscovery( instanceHandle );
			Assert.fail( "Received discover notification for object [" + instanceHandle +
			             "] while waiting for timeout" );
		}
		catch( TimeoutException te )
		{
			// success!
		}
	}
	
	/**
	 * This method will wait for the given object to be discovered, however, it is expecting it to
	 * either a) not be discovered at all, or, b) to be discovered, but not as an instance of the
	 * given class. If it is discovered before a timeout is up, AND it is discovered as an instance
	 * of the given class, the current test will be failed.
	 */
	public void waitForDiscoveryAsTimeout( int instanceHandle, int classItShouldntBe )
	{
		try
		{
			waitForDiscovery( instanceHandle );
		}
		catch( TimeoutException te )
		{
			// success!
			return;
		}
		
		// if we get here it means we discovered the instance, this might be ok, as long
		// as we didn't discover it as an instance of the given class
		if( instances.get(instanceHandle).getClassHandle() == classItShouldntBe )
		{
			Assert.fail( "Received discover notification for object [" + instanceHandle +
			             "] as class [" + classItShouldntBe +
			             "]: expecting timeout or for it to be discovered at a different class" );
		}
	}
	
	/**
	 * THis method will block execution until either an instance of the given class handle is
	 * discovered, or the given timeout (in seconds) has elapsed. If an instance of the given
	 * class was previously discovered, it will be returned right away. As instances are
	 * discovered, they are stored in order. Thus, this method will return the oldest discovered
	 * instance of the given class first. If the timeout occurs, an exception will be thrown.
	 * 
	 * @param classHandle The handle of the class to find the latest discovered instance of
	 * @return The latest discovered instance of the given class
	 * @throws TimeoutException If the timeout is up before the a suitable instance is discovered
	 */
	public Test13Instance waitForLatestDiscovery( int classHandle )
	{
		long finishTime = getTimeout();
		do
		{
			// look for the first instance of the given class handle //
			for( Test13Instance temp : this.discovered )
			{
				if( temp.getClassHandle() == classHandle )
				{
					// this is the first instance of the given class, remove and return it
					this.discovered.remove( temp );
					return temp;
				}
			}
			
			// haven't got what we want, tick for callbacks
			tick();
		}
		while( System.currentTimeMillis() < finishTime );

		// we didn't find the instance before the timeout :(
		throw new TimeoutException( "Timeout waiting for next discovery of instance from class [" +
		                            classHandle + "]" );
	}
	
	/**
	 * This method is the same as {@link #waitForLatestDiscovery(int)} except that it takes an
	 * object class name rather than an object class handle.
	 */
	public Test13Instance waitForLatestDiscovery( String className )
	{
		return waitForLatestDiscovery( federate.quickOCHandle(className) );
	}
	
	/**
	 * This method will block until the federate ambassador receives a callback informing it that
	 * the specified instance has been updated. If there is no update before the given timeout
	 * (in seconds), a TimeoutException will be thrown. It will always check for RO updates before
	 * it checks for TSO ones. Also, if both an RO and TSO update exists, only the first one found
	 * will be removed.
	 */
	public void waitForUpdate( int instanceHandle )
	{
		// see if the instance already has a pending update
		if( this.roUpdated.contains(instanceHandle) )
		{
			this.roUpdated.remove( instanceHandle );
			return;
		}
		
		if( this.tsoUpdated.contains(instanceHandle) )
		{
			// we've taken notice of this update, so remove it now
			this.tsoUpdated.remove( instanceHandle );
			return;
		}
		
		// no pending update, wait for the next one //
		long finishTime = getTimeout();
		while( finishTime > System.currentTimeMillis() )
		{
			// get some callback love
			tick();
			
			if( this.roUpdated.contains(instanceHandle) )
			{
				this.roUpdated.remove( instanceHandle );
				return;
			}
			
			if( this.tsoUpdated.contains(instanceHandle) )
			{
				// we've taken notice of this update, so remove it now
				this.tsoUpdated.remove( instanceHandle );
				return;
			}
		}
		
		// we have timed out, throw an exception
		throw new TimeoutException( "Timeout waiting for update of instance ["+instanceHandle+"]" );
	}

	/**
	 * This method will block until the federate ambassador receives a RO callback informing it that
	 * the specified instance has been updated. If there is no update before the given timeout
	 * (in seconds), a TimeoutException will be thrown. Any TSO updates received will not be
	 * considered here.
	 */
	public void waitForROUpdate( int handle )
	{
		// check to see if an update is already queued
		if( this.roUpdated.contains(handle) )
		{
			this.roUpdated.remove( handle );
			return;
		}
		
		// no outstanding update, we will have to wait
		long finishTime = getTimeout();
		while( finishTime > System.currentTimeMillis() )
		{
			tick();
			if( this.roUpdated.contains(handle) )
			{
				this.roUpdated.remove( handle );
				return;
			}
		}
		
		// no update received in time
		throw new TimeoutException( "Timeout waiting for RO update of instance [" + handle + "]" );
	}
	
	/**
	 * This method will block until the federate ambassador receives a TSO callback informing it
	 * that the specified instance has been updated. If there is no update before the given timeout
	 * (in seconds), a TimeoutException will be thrown. Any RO updates received will not be
	 * considered here.
	 */
	public void waitForTSOUpdate( int handle )
	{
		// check to see if an update is already queued
		if( this.tsoUpdated.contains(handle) )
		{
			this.tsoUpdated.remove( handle );
			return;
		}
		
		// no outstanding update, we will have to wait
		long finishTime = getTimeout();
		while( finishTime > System.currentTimeMillis() )
		{
			tick();
			if( this.tsoUpdated.contains(handle) )
			{
				this.tsoUpdated.remove( handle );
				return;
			}
		}
		
		// no update received in time
		throw new TimeoutException( "Timeout waiting for TSO update of instance [" + handle + "]" );
	}
	
	/**
	 * This is the inverse of the {@link #waitForUpdate(int)} method (sort of). It will wait for
	 * an update to come through for the given instance, but it <b>IS EXPECTING A TIMEOUT</b>. If
	 * the timeout does occur, this method will happily return. If not, Assert.fail() will be used
	 * to kill the test. It will tick() away happily while it waits.
	 * 
	 * @param instanceHandle The handle of the instance to wait for an update of
	 */
	public void waitForUpdateTimeout( int instanceHandle )
	{
		try
		{
			this.waitForUpdate( instanceHandle );
			// if we get here, we got the update :(
			Assert.fail( "Received update for instance [" + instanceHandle +
			             "] when we were expecting a timeout" );
		}
		catch( TimeoutException te )
		{
			return;
		}
	}

	/**
	 * This is the same as {@link #waitForUpdateTimeout(int)}, except that it will only check for
	 * RO updates, rather than all updates.
	 */
	public void waitForROUpdateTimeout( int instanceHandle )
	{
		try
		{
			this.waitForROUpdate( instanceHandle );
			// if we get here, we got the update :(
			Assert.fail( "Received RO update for instance [" + instanceHandle +
			             "] when we were expecting a timeout" );
		}
		catch( TimeoutException te )
		{
			return;
		}
	}
	
	/**
	 * This is the same as {@link #waitForUpdateTimeout(int)}, except that it will only check for
	 * TSO updates, rather than all updates.
	 */
	public void waitForTSOUpdateTimeout( int instanceHandle )
	{
		try
		{
			this.waitForTSOUpdate( instanceHandle );
			// if we get here, we got the update :(
			Assert.fail( "Received TSO update for instance [" + instanceHandle +
			             "] when we were expecting a timeout" );
		}
		catch( TimeoutException te )
		{
			return;
		}
	}
	
	/**
	 * This method will block until a request to update the attributes of the given object
	 * instance has been received. It will only block for the given timeout (in seconds), after
	 * which it will throw a TimeoutException if the update request has not been received. If
	 * the request is received in time, the handles requested for update will be returned.
	 */
	public Set<Integer> waitForProvideRequest( int instanceHandle )
	{
		// check for an exiting update request, if there is one, return it, otherwise we'll wait
		if( this.updatesRequested.containsKey(instanceHandle) )
		{
			return this.updatesRequested.remove( instanceHandle );
		}
		
		// wait for the update to come through //
		long finishTime = getTimeout();
		while( updatesRequested.containsKey(instanceHandle) == false )
		{
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for update request of instance [" +
				                            instanceHandle + "]" );
			}
			
			tick();
		}
		
		// remove and return the set of updates requested
		return this.updatesRequested.remove( instanceHandle );
	}
	
	/**
	 * This is the inverse of {@link #waitForProvideRequest(int)}. It expects a timeout. If a
	 * "provide update" notification is delivered before that timeout can occur, Assert.fail()
	 * will be used to kill the test. If the timeout does occur, this method will return happily.
	 */
	public void waitForProvideRequestTimeout( int instanceHandle )
	{
		try
		{
			waitForProvideRequest( instanceHandle );
			Assert.fail( "Received provide update notification for object [" + instanceHandle +
			             "] while waiting for timeout" );
		}
		catch( TimeoutException te )
		{
			// success!
		}
	}
	
	/**
	 * This method will block until either the timeout it up, or we have received an RO remove
	 * callback for the given instance handle (it will tick while blocking). If the timeout runs
	 * up before we get a remove, a TimeoutException will be thrown.
	 */
	public void waitForRORemoval( int instanceHandle )
	{
		// check to make sure it hasn't already been removed //
		if( this.roRemoved.contains(instanceHandle) )
		{
			// it has!
			this.roRemoved.remove( instanceHandle );
			return;
		}
		
		// wait until we get the callback //
		long finishTime = getTimeout();
		while( this.roRemoved.contains(instanceHandle) == false )
		{
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for RO removal of instance [" +
				                            instanceHandle + "]" );
			}
			
			tick();
		}
		
		// we've received the callback, clean up and get out of here
		this.roRemoved.remove( instanceHandle );
	}

	/**
	 * This method is the inverse of {@link #waitForRORemoval(int)}. Rather than waiting for the
	 * removal, and then failing the test if it does not occur, it waits for the timeout to occur,
	 * erroring if a RO removal of the given instance occurs in that time.
	 */
	public void waitForRORemovalTimeout( int instanceHandle )
	{
		try
		{
			waitForRORemoval( instanceHandle );
			Assert.fail( "Received a RO removal notification for object [" + instanceHandle +
			             "] while waiting for timeout" );
		}
		catch( TimeoutException te )
		{
			// success!
		}
	}

	/**
	 * This method will block until either the timeout it up, or we have received an TSO remove
	 * callback for the given instance handle (it will tick while blocking). If the timeout runs
	 * up before we get a remove, a TimeoutException will be thrown.
	 */
	public void waitForTSORemoval( int instanceHandle )
	{
		// check to make sure it hasn't already been removed //
		if( this.tsoRemoved.contains(instanceHandle) )
		{
			// it has!
			this.roRemoved.remove( instanceHandle );
			return;
		}
		
		// wait until we get the callback //
		long finishTime = getTimeout();
		while( this.tsoRemoved.contains(instanceHandle) == false )
		{
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for TSO removal of instance [" +
				                            instanceHandle + "]" );
			}
			
			tick();
		}
		
		// we've received the callback, clean up and get out of here
		this.tsoRemoved.remove( instanceHandle );
	}
	
	/**
	 * This method is the inverse of {@link #waitForTSORemoval(int)}. Rather than waiting for the
	 * removal, and then failing the test if it does not occur, it waits for the timeout to occur,
	 * erroring if a TSO removal of the given instance occurs in that time.
	 */
	public void waitForTSORemovalTimeout( int instanceHandle )
	{
		try
		{
			waitForTSORemoval( instanceHandle );
			Assert.fail( "Received a TSO removal notification for object [" + instanceHandle +
			             "] while waiting for timeout" );
		}
		catch( TimeoutException te )
		{
			// success!
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Interaction Helper Methods ////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will block until an RO interaction of the given class handle is received. If an
	 * appropriate interaction is not received in a timely fashion, a TimeoutException will be
	 * thrown. <b>NOTE:</b> interactions are stored up as they are received, so you may actually
	 * get an older interaction than you expect.
	 */
	public Test13Interaction waitForROInteraction( int classHandle )
	{
		// wait until we get the interaction //
		Test13Interaction interaction = null;
		long finishTime = getTimeout();
		while( interaction == null )
		{
			interaction = fetchROInteraction( classHandle );
			if( interaction != null )
				return interaction;
			
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for RO interaction of type [" +
				                            classHandle + "]" );
			}
			
			tick();
		}
		
		// we should never get here, we'll timeout first
		return null;
	}
	
	/**
	 * This method is the same as {@link #waitForROInteraction(int)}, except that it will resolve
	 * the name of the class to a handle for you. If there is a problem doing this, Assert.fail()
	 * will be used to kill the test.
	 */
	public Test13Interaction waitForROInteraction( String className )
	{
		// resolve the handle for the class name //
		int iHandle = -1;
		try
		{
			iHandle = federate.rtiamb.getInteractionClassHandle( className );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception resolving interaction handle from name: " +
			             className + ". Can't wait for RO interaction", e );
		}
		
		// wait for the interaction //
		return waitForROInteraction( iHandle );
	}
	
	private Test13Interaction fetchROInteraction( int classHandle )
	{
		for( int i = 0; i < roInteractions.size(); i++ )
		{
			if( roInteractions.get(i).getClassHandle() == classHandle )
			{
				return roInteractions.remove( i ); 
			}
		}
		
		return null;
	}
	
	/**
	 * This method is the rough inverse of the {@link #waitForROInteraction(int)} method. It will
	 * wait for the timeout period for an RO interaction of the given class handle to be received,
	 * however, it is expecting a timeout rather than to actually receive an interaction. If the
	 * wait times out as desired, the method will return happily. If however an interaction of
	 * the correct type is received, Assert.fail() will be used to kill the test.
	 */
	public void waitForROInteractionTimeout( int classHandle )
	{
		try
		{
			waitForROInteraction( classHandle );
			Assert.fail( "Received an RO interaction of type [" + classHandle +
			             "] while waiting for timeout" );
		}
		catch( TimeoutException te )
		{
			// success!
		}
	}
	
	/**
	 * This method is the as {@link #waitForROInteractionTimeout(int)}, except that it will resolve
	 * the handle of the interaction for you. If there is a problem doing this, Assert.fail() will
	 * be used to kill the test.
	 */
	public void waitForROInteractionTimeout( String className )
	{
		// resolve the handle for the class name //
		int iHandle = -1;
		try
		{
			iHandle = federate.rtiamb.getInteractionClassHandle( className );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception resolving interaction handle from name: " +
			             className + ". Can't wait for RO interaction", e );
		}
		
		// wait for the interaction //
		waitForROInteractionTimeout( iHandle );
	}

	/**
	 * This method will block until an TSO interaction of the given class handle is received. If an
	 * appropriate interaction is not received in a timely fashion, a TimeoutException will be
	 * thrown. <b>NOTE:</b> interactions are stored up as they are received, so you may actually
	 * get an older interaction than you expect.
	 */
	public Test13Interaction waitForTSOInteraction( int classHandle )
	{
		// wait until we get the interaction //
		Test13Interaction interaction = null;
		long finishTime = getTimeout();
		while( interaction == null )
		{
			interaction = fetchTSOInteraction( classHandle );
			if( interaction != null )
				return interaction;
			
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for TSO interaction of type [" +
				                            classHandle + "]" );
			}
			
			tick();
		}
		
		// we should never get here, we'll timeout first
		return null;
	}
	
	/**
	 * This method is the same as {@link #waitForTSOInteraction(int)}, except that it will resolve
	 * the name of the class to a handle for you. If there is a problem doing this, Assert.fail()
	 * will be used to kill the test.
	 */
	public Test13Interaction waitForTSOInteraction( String className )
	{
		// resolve the handle for the class name //
		int iHandle = -1;
		try
		{
			iHandle = federate.rtiamb.getInteractionClassHandle( className );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception resolving interaction handle from name: " +
			             className + ". Can't wait for TSO interaction", e );
		}
		
		// wait for the interaction //
		return waitForTSOInteraction( iHandle );
	}
	
	private Test13Interaction fetchTSOInteraction( int classHandle )
	{
		for( int i = 0; i < tsoInteractions.size(); i++ )
		{
			if( tsoInteractions.get(i).getClassHandle() == classHandle )
			{
				return tsoInteractions.remove( i ); 
			}
		}
		
		return null;
	}
	
	/**
	 * This method is the rough inverse of the {@link #waitForTSOInteraction(int)} method. It will
	 * wait for the timeout period for an TSO interaction of the given class handle to be received,
	 * however, it is expecting a timeout rather than to actually receive an interaction. If the
	 * wait times out as desired, the method will return happily. If however an interaction of
	 * the correct type is received, Assert.fail() will be used to kill the test.
	 */
	public void waitForTSOInteractionTimeout( int classHandle )
	{
		try
		{
			waitForTSOInteraction( classHandle );
			Assert.fail( "Received an TSO interaction of type [" + classHandle +
			             "] while waiting for timeout" );
		}
		catch( TimeoutException te )
		{
			// success!
		}
	}
	
	/**
	 * This method is the as {@link #waitForTSOInteractionTimeout(int)}, except that it will resolve
	 * the handle of the interaction for you. If there is a problem doing this, Assert.fail() will
	 * be used to kill the test.
	 */
	public void waitForTSOInteractionTimeout( String className )
	{
		// resolve the handle for the class name //
		int iHandle = -1;
		try
		{
			iHandle = federate.rtiamb.getInteractionClassHandle( className );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception resolving interaction handle from name: " +
			             className + ". Can't wait for TSO interaction", e );
		}
		
		// wait for the interaction //
		waitForTSOInteractionTimeout( iHandle );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Ownership Management Helper Methods ///////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method waits for an ownership acquisition notification for the provided object with
	 * the provided attributes to occur. If a notification comes in, but all the attributes are
	 * not present, an exception will be thrown. If no notification is received in a timely
	 * manner, an exception will be thrown.
	 */
	public void waitForOwnershipAcquisition( int objectHandle, int... attributes )
	{
		long finishTime = getTimeout();
		while( attributesAcquired.containsKey(objectHandle) == false )
		{
			tick();
			
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for acquisition notice: attributes="+
				                            Arrays.toString(attributes)+", object="+objectHandle );
			}
		}
		
		// we have the notification, make sure all the required attributes, and ONLY those
		Set<Integer> acquired = attributesAcquired.remove( objectHandle );
		if( attributes.length != acquired.size() )
		{
			Assert.fail( "Waiting for acquisition of attributes "+Arrays.toString(attributes)+
			             ", but got notice for "+acquired );
		}
		
		for( int expected : attributes )
		{
			if( acquired.contains(expected) == false )
			{
				Assert.fail( "Waiting for acquisition of attributes "+Arrays.toString(attributes)+
				             ", got some, but not ["+expected+"]" );
			}
		}
	}
	
	/**
	 * This is the inverse of {@link #waitForOwnershipAcquisition(int, int...)}. It expects a
	 * timeout. If the timeout doesn't occur, the current test is failed with Assert.fail().
	 */
	public void waitForOwnershipAcquisitionTimeout( int objectHandle, int... attributes )
	{
		try
		{
			waitForOwnershipAcquisition( objectHandle, attributes );
			Assert.fail( "Expected a timeout waiting for attribute acquisition notice: attributes="+
			             Arrays.toString(attributes)+", object="+objectHandle );
		}
		catch( TimeoutException te )
		{
			// success!
		}
	}

	/**
	 * This method waits for a notification that the given attributes of the given object are
	 * available for ownership. If a notificiation comes in, but all the attributes are not
	 * present, an exception will be thrown. If no notification is received in a timely manner,
	 * an exception will be thrown.
	 */
	public void waitForOwnershipOffering( int objectHandle, int... attributes )
	{
		long finishTime = getTimeout();
		while( attributesOffered.containsKey(objectHandle) == false )
		{
			tick();
			
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for attribute offering: attributes="+
				                            Arrays.toString(attributes)+", object="+objectHandle );
			}
		}
		
		// we have the notification, make sure all the required attributes, and ONLY those
		Set<Integer> offered = attributesOffered.remove( objectHandle );
		if( attributes.length != offered.size() )
		{
			Assert.fail( "Waiting for offering of attribute "+Arrays.toString(attributes)+
			             ", but got notice for "+offered );
		}
		
		for( int expected : attributes )
		{
			if( offered.contains(expected) == false )
			{
				Assert.fail( "Waiting for offering of attributes "+Arrays.toString(attributes)+
				             ", got some, but not ["+expected+"]" );
			}
		}
	}
	
	/**
	 * Same as {@link #waitForOwnershipOffering(int, int...)} except that it expects a timeout and
	 * will fail the test if a timeout doesn't occur.
	 */
	public void waitForOwnershipOfferingTimeout( int objectHandle, int... attributes )
	{
		try
		{
			waitForOwnershipOffering( objectHandle, attributes );
			Assert.fail( "Expected a timeout waiting for ownership offering of attributes="+
			             Arrays.toString(attributes)+", object="+objectHandle );
		}
		catch( TimeoutException te )
		{
			// success!
		}
	}

	/**
	 * This method waits for a notification that the attributes of the specified object have
	 * been successfully divested. If a notification comes in but all the attributes are not
	 * present, an exception will be thrown. If no notification is received in a timely manner,
	 * an exception will be thrown.
	 */
	public void waitForOwnershipDivest( int objectHandle, int... attributes )
	{
		long finishTime = getTimeout();
		while( attributesDivested.containsKey(objectHandle) == false )
		{
			tick();
			
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for attribute divest: attributes="+
				                            Arrays.toString(attributes)+", object="+objectHandle );
			}
		}
		
		// we have the notification, make sure all the required attributes, and ONLY those
		Set<Integer> requested = attributesDivested.remove( objectHandle );
		if( attributes.length != requested.size() )
		{
			Assert.fail( "Waiting for attribute divest of "+Arrays.toString(attributes)+
			             ", but got notice for "+requested );
		}
		
		for( int expected : attributes )
		{
			if( requested.contains(expected) == false )
			{
				Assert.fail( "Timeout waiting for divest of attributes "+
				             Arrays.toString(attributes)+", got some, but not ["+expected+"]" );
			}
		}
	}
	
	/**
	 * This method waits for a notification that the given attributes of the given object are
	 * not available for ownership assumption. If a notification comes in but all the attributes
	 * are not present, an exception will be thrown. If no notification is received in a timely
	 * mapper, an exception will be thrown.
	 */
	public void waitForOwnershipUnavailable( int objectHandle, int... attributes )
	{
		long finishTime = getTimeout();
		while( attributesUnavailable.containsKey(objectHandle) == false )
		{
			tick();
			
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for attributes unavailable: attributes="+
				                            Arrays.toString(attributes)+", object="+objectHandle );
			}
		}
		
		// we have the notification, make sure all the required attributes, and ONLY those
		Set<Integer> unavailable = attributesUnavailable.remove( objectHandle );
		if( attributes.length != unavailable.size() )
		{
			Assert.fail( "Waiting for notice that attributes "+Arrays.toString(attributes)+
			             "are unavailable, but got notice for "+unavailable );
		}
		
		for( int expected : attributes )
		{
			if( unavailable.contains(expected) == false )
			{
				Assert.fail( "Timeout waiting for notice that attributes "+Arrays.toString(attributes)+
				             " are unavailable. Got some, but not ["+expected+"]" );
			}
		}
	}
	
	/**
	 * This method waits for notification that the given attributes of the given object have
	 * been the subject of a request to release their ownership. If a notification comes in but
	 * all the attributes are not present, an exception will be thrown. If no notification is
	 * received in a timely mapper, an exception will be thrown.
	 */
	public void waitForOwnershipReleaseRequest( int objectHandle, int... attributes )
	{
		long finishTime = getTimeout();
		while( attributesRequestedForRelease.containsKey(objectHandle) == false )
		{
			tick();
			
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for ownership release request: attributes="+
				                            Arrays.toString(attributes)+", object="+objectHandle );
			}
		}
		
		// we have the notification, make sure all the required attributes, and ONLY those
		Set<Integer> requested = attributesRequestedForRelease.remove( objectHandle );
		if( attributes.length != requested.size() )
		{
			Assert.fail( "Waiting for request to release attributes "+Arrays.toString(attributes)+
			             ", but got notice for "+requested );
		}
		
		for( int expected : attributes )
		{
			if( requested.contains(expected) == false )
			{
				Assert.fail( "Timeout waiting for request to release attributes "+
				             Arrays.toString(attributes)+". Got some, but not ["+expected+"]" );
			}
		}
	}
	
	/**
	 * This method is the inverse of {@link #waitForOwnershipReleaseRequest(int, int...)}. It
	 * expectes that a timeout will occur. If a timeout doesn't occur, the current test is failed.
	 */
	public void waitForOwnershipReleaseRequestTimeout( int objectHandle, int... attributes )
	{
		try
		{
			waitForOwnershipReleaseRequest( objectHandle, attributes );
			Assert.fail( "Expected a timeout waiting for attribute release request: attributes="+
			             Arrays.toString(attributes)+", object="+objectHandle );
		}
		catch( TimeoutException te )
		{
			// success!
		}
	}

	/**
	 * This method waits for notification that the previously issued request to aquire ownership
	 * of the attributes of the given object has been cancelled. If a notification comes in but
	 * all the attributes are not present, an exception will be thrown. If no notification is
	 * received in a timely mapper, an exception will be thrown.
	 */
	public void waitForOwnershipAcquireCancel( int objectHandle, int... attributes )
	{
		long finishTime = getTimeout();
		while( attributesCancelled.containsKey(objectHandle) == false )
		{
			tick();
			
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for aquire cancellation: attributes="+
				                            Arrays.toString(attributes)+", object="+objectHandle );
			}
		}
		
		// we have the notification, make sure all the required attributes, and ONLY those
		Set<Integer> cancelled = attributesCancelled.remove( objectHandle );
		if( attributes.length != cancelled.size() )
		{
			Assert.fail( "Waiting for aquire cancellation of attributes "+Arrays.toString(attributes)+
			             ", but got notice for "+cancelled );
		}
		
		for( int expected : attributes )
		{
			if( cancelled.contains(expected) == false )
			{
				Assert.fail( "Timeout waiting for aquire cancellation of attributes "+
				             Arrays.toString(attributes)+". Got some, but not ["+expected+"]" );
			}
		}
	}
	
	/**
	 * This method is the opposite of {@link #waitForOwnershipAcquireCancel(int, int...)}. It will
	 * call that method BUT it EXPECTS a timeout. If a timeout doesn't occur, the current test will
	 * be failed.
	 */
	public void waitForOwnershipAcquireCancelTimeout( int objectHandle, int... attributes )
	{
		try
		{
			waitForOwnershipAcquireCancel( objectHandle, attributes );
			Assert.fail( "Expected timeout waiting for ownership acquire cancellation: attributes="+
			             Arrays.toString(attributes)+", object="+objectHandle );
		}
		catch( TimeoutException te )
		{
			// success!
		}
	}
	
	/**
	 * This method waits for the results of a queryAttributeOwnership() call. If a notification for
	 * the given object of the given attribute is received, the ownership information is returned.
	 * If no notification is received in a timely manner, an exception will be thrown.
	 */
	public OwnershipInfo waitForOwnershipQueryResponse( int objectHandle, int attribute )
	{
		long finishTime = getTimeout();
		while( true )
		{
			// tick for callbacks
			tick();

			// check to see if the response is there
			OwnershipInfo result = null;
			for( OwnershipInfo local : attributeOwnershipQueryResponses )
			{
				if( local.object == objectHandle && local.attribute == attribute )
				{
					result = local;
					break;
				}
			}
			
			if( result != null )
			{
				attributeOwnershipQueryResponses.remove( result );
				return result;
			}
			
			// check timeout value
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for ownership query callback: object="+
				                            objectHandle+", attribute="+attribute );
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Time Helper Methods ///////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	public boolean isConstrained()
	{
		return this.constrained;
	}
	
	public boolean isRegulating()
	{
		return this.regulating;
	}
	
	public double getLogicalTime()
	{
		return this.logicalTime;
	}
	
	/**
	 * This method blocks until time constrained is enabled. If this does not happen before the
	 * timeout time is reached, a TimeoutException will be thrown.
	 */
	public void waitForConstrainedEnabled()
	{
		long finishTime = getTimeout();
		while( constrained == false )
		{
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for time constrained to be enabled" );
			}
			
			tick();
		}
	}
	
	/**
	 * This method is the inverse of {@link #waitForConstrainedEnabled()}. It will wait for
	 * constrained to be enabled, but it is expecting a timeout.
	 */
	public void waitForConstrainedEnabledTimeout()
	{
		try
		{
			waitForConstrainedEnabled();
			// oh noes! there was no exception
			Assert.fail( "Constrained was enabled while waiting for timeout" );
		}
		catch( TimeoutException te )
		{
			// success!
		}
	}

	/**
	 * This method blocks until time constrained is enabled. If this does not happen before the
	 * timeout time is reached, a TimeoutException will be thrown.
	 */
	public void waitForRegulatingEnabled()
	{
		long finishTime = getTimeout();
		while( regulating == false )
		{
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for time regulating to be enabled" );
			}
			
			tick();
		}
	}

	/**
	 * This method is the inverse of {@link #waitForRegulatingEnabled()}. It will wait for
	 * regulating to be enabled, but it is expecting a timeout.
	 */
	public void waitForRegulatingEnabledTimeout()
	{
		try
		{
			waitForRegulatingEnabled();
			// oh noes! there was no exception
			Assert.fail( "Regulating was enabled while waiting for timeout" );
		}
		catch( TimeoutException te )
		{
			// success!
		}
	}

	/**
	 * This method will block until a time advancement to at least the given time is received. If
	 * there is no advance before the given timeout, a TimeoutException will be thrown (detailing
	 * the error).
	 */
	public void waitForTimeAdvance( double toTime )
	{
		long finishTime = getTimeout();
		while( logicalTime < toTime )
		{
			if( finishTime < System.currentTimeMillis() )
			{
				throw new TimeoutException( "Timeout waiting for time advancement to [" +
				                            toTime + "]" );
			}
			
			tick();
		}
	}
	
	/**
	 * This method is similar to {@link #waitForTimeAdvance(double)} except that it is to
	 * be used when you are *NOT* expecting an advance (and want to wait for a timeout). If an
	 * advance to at least the given value is received before the timeout, Assert.fail() will
	 * be used to kill the test.
	 */
	public void waitForTimeAdvanceTimeout( double toTime )
	{
		long finishTime = getTimeout();
		while( logicalTime < toTime )
		{
			if( finishTime < System.currentTimeMillis() )
			{
				return;
			}
			
			tick();
		}
		
		// if we get here, it means we didn't timeout. fail the test
		Assert.fail( "Received time advance to [" + toTime + "] but was EXPECTING A TIMEOUT!" );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// HLA Callback Methods ///////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void announceSynchronizationPoint( String label, byte[] tag )
	{
		this.announced.put( label, tag );
	}
	
	@Override
	public void synchronizationPointRegistrationFailed( String label ) throws FederateInternalError
	{
		this.syncFailed = label;
	}

	@Override
	public void synchronizationPointRegistrationSucceeded( String label )
	{
		this.syncSucceeded = label;
	}
	
	@Override
	public void federationSynchronized( String synchronizationPointLabel )
	{
		this.synched.add( synchronizationPointLabel );
	}

	@Override
	public void timeConstrainedEnabled( LogicalTime time )
	{
		this.constrained = true;
		this.logicalTime = convertTime( time ); 
	}
	
	@Override
	public void timeRegulationEnabled( LogicalTime time )
	{
		this.regulating = true;
		this.logicalTime = convertTime( time );
	}
	
	@Override
	public void timeAdvanceGrant( LogicalTime time )
	{
		double newTime = convertTime( time );
		validateEventTime( newTime, "timeAdvanceGrant" );
		this.logicalTime = newTime;
	}
	
	@Override
	public void discoverObjectInstance( int theObject, int theObjectClass, String objectName )
	{
		Test13Instance instance = new Test13Instance( theObject, theObjectClass, objectName );
		this.instances.put( theObject, instance );
		this.discovered.add( instance );
	}
	
	@Override
	public void removeObjectInstance( int theObject, byte[] userSuppliedTag )
	{
		Test13Instance instance = this.instances.remove( theObject );
		this.discovered.remove( instance ); // in case it is still hanging around
		this.roRemoved.add( theObject );
	}
	
	@Override
	public void removeObjectInstance( int theObject,
	                                  byte[] userSuppliedTag,
	                                  LogicalTime theTime,
	                                  EventRetractionHandle retractionHandle )
		throws ObjectNotKnown, InvalidFederationTime, FederateInternalError
	{
		if( theTime != null )
			validateEventTime( convertTime(theTime), "removeObjectInstance" );
		
		Test13Instance instance = this.instances.remove( theObject );
		this.discovered.remove( instance ); // in case it is still hanging around
		this.tsoRemoved.add( theObject );
	}

	@Override
	public void provideAttributeValueUpdate( int theObject, AttributeHandleSet theAttributes )
	{
		synchronized( this.updatesRequested )
		{
			//System.out.println( "Received update for [" + theObject + "] with [" +
			//                    theAttributes.size() + "] attributes" );
			this.updatesRequested.put( theObject, ((HLA13Set)theAttributes).toJavaSet() );
		}
	}
	
	@Override
	public void reflectAttributeValues( int theObject,
	                                    ReflectedAttributes theAttributes,
	                                    byte[] userSuppliedTag )
	{
		incrementCounter( roUpdateCount, theObject );

		// fetch the instance and update it's values //
		Test13Instance instance = this.instances.get( theObject );
		for( int i = 0; i < theAttributes.size(); i++ )
		{
			try
			{
				int handle = theAttributes.getAttributeHandle( i );
				byte[] value = theAttributes.getValue( i );
				instance.attributes.put( handle, value );
				instance.regions.put( handle, theAttributes.getRegion(i) );
			}
			catch( ArrayIndexOutOfBounds willNeverHappenAndTheHlaJavaInterfaceIsTerrible )
			{}
		}
		
		// mark the instance as recently updated //
		this.roUpdated.add( theObject );
	}
	
	@Override
	public void reflectAttributeValues( int theObject,
	                                    ReflectedAttributes theAttributes,
	                                    byte[] userSuppliedTag,
	                                    LogicalTime time,
	                                    EventRetractionHandle retractionHandle )
	{
		incrementCounter( tsoUpdateCount, theObject );

		if( time != null )
			validateEventTime( convertTime(time), "removeObjectInstance" );

		// fetch the instance and update it's values //
		Test13Instance instance = this.instances.get( theObject );
		for( int i = 0; i < theAttributes.size(); i++ )
		{
			try
			{
				int handle = theAttributes.getAttributeHandle( i );
				byte[] value = theAttributes.getValue( i );
				instance.attributes.put( handle, value );
				instance.regions.put( handle, theAttributes.getRegion(i) );
			}
			catch( ArrayIndexOutOfBounds willNeverHappenAndTheHlaJavaInterfaceIsTerrible )
			{}
		}
		
		// mark the instance as recently updated //
		this.tsoUpdated.add( theObject );
	}
	
	@Override
	public void receiveInteraction( int iClass, ReceivedInteraction params, byte[] tag )
	{
		// store the interaction information //
		incrementCounter( roInteractionCount, iClass );
		roInteractions.add( new Test13Interaction(iClass, params, tag) );
	}
	
	@Override
	public void receiveInteraction( int iClass,
	                                ReceivedInteraction params,
	                                byte[] tag,
	                                LogicalTime time,
	                                EventRetractionHandle eventRetractionHandle )
	{
		incrementCounter( tsoInteractionCount, iClass );

		if( time != null )
			validateEventTime( convertTime(time), "receiveInteraction" );

		double dtime = convertTime( time );
		tsoInteractions.add( new Test13Interaction(iClass,params,tag,dtime) );
	}
	
	@Override
	public void requestAttributeOwnershipAssumption( int theObject,
	                                                 AttributeHandleSet offered,
	                                                 byte[] tag )
	{
		attributesOffered.put( theObject, convertAHS(offered) );
	}

	@Override
	public void attributeOwnershipDivestitureNotification( int theObject,
	                                                       AttributeHandleSet released )
	{
		attributesDivested.put( theObject, convertAHS(released) );
	}

	@Override
	public void attributeOwnershipAcquisitionNotification( int theObject,
	                                                       AttributeHandleSet secured )
	{
		attributesAcquired.put( theObject, convertAHS(secured) );
	}

	@Override
	public void attributeOwnershipUnavailable( int theObject, AttributeHandleSet theAttributes )
	{
		attributesUnavailable.put( theObject, convertAHS(theAttributes) );
	}

	@Override
	public void requestAttributeOwnershipRelease( int theObject,
	                                              AttributeHandleSet candidate,
	                                              byte[] tag )
	{
		attributesRequestedForRelease.put( theObject, convertAHS(candidate) );
	}

	@Override
	public void confirmAttributeOwnershipAcquisitionCancellation( int theObject,
	                                                              AttributeHandleSet attributes )
	{
		attributesCancelled.put( theObject, convertAHS(attributes) );
	}

	@Override
	public void informAttributeOwnership( int theObject, int theAttribute, int theOwner )
	{
		OwnershipInfo info = new OwnershipInfo( theObject, theAttribute, theOwner );
		attributeOwnershipQueryResponses.add( info );
	}

	@Override
	public void attributeIsNotOwned( int theObject, int theAttribute )
	{
		OwnershipInfo info = new OwnershipInfo( theObject, theAttribute, -1 );
		attributeOwnershipQueryResponses.add( info );
	}

	@Override
	public void attributeOwnedByRTI( int theObject, int theAttribute )
	{
		OwnershipInfo info = new OwnershipInfo( theObject, theAttribute, 0 );
		attributeOwnershipQueryResponses.add( info );
	}

	@Override
	public void initiateFederateSave( String label )
	{
		// reset the current save information
		currentSave.initiate( label );
	}

	@Override
	public void federationSaved()
	{
		currentSave.success();
	}

	@Override
	public void federationNotSaved()
	{
		currentSave.failure();
	}

	@Override
	public void initiateFederateRestore( String label, int federateHandle )
	{
		currentRestore.initiate( label, federateHandle );
	}

	@Override
	public void federationRestoreBegun() throws FederateInternalError
	{
		currentRestore.begun();
	}

	@Override
	public void federationRestored() throws FederateInternalError
	{
		currentRestore.success();
	}

	@Override
	public void federationNotRestored() throws FederateInternalError
	{
		currentRestore.failure();
	}

	@Override
	public void requestFederationRestoreSucceeded( String label )
	{
		this.successfulRestoreRequest = label;
	}

	@Override
	public void requestFederationRestoreFailed( String label, String reason )
	{
		this.failedRestoreRequest = label;
	}

	//////////////////////////
	//    helper methods    //
	//////////////////////////
	private double convertTime( LogicalTime time )
	{
		if( time instanceof DoubleTime )
		{
			return ((DoubleTime)time).getTime();
		}
		else
		{
			throw new TimeoutException( "Didn't receive Portico LogicalTime implementation" );
		}
	}
	
	private Set<Integer> convertAHS( AttributeHandleSet set )
	{
		if( set instanceof HLA13Set )
		{
			return ((HLA13Set)set).toJavaSet();
		}
		else
		{
			throw new RuntimeException( "Can't convert non-Portico set implementation" );
		}
	}

	private void validateEventTime( double eventTime, String event )
	{
		if( !this.checkForPastEvent )
			return;
		
		if( logicalTime >= eventTime )
		{
			String message = "**PAST EVENT** Received event [" +event+ "] with timestamp [" +
			                 eventTime+ "], but current time is [" +logicalTime+ "]";
			Assert.fail( message );
			//System.err.println( message );
			//throw new RuntimeException( message );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Private Inner Classes /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	public class OwnershipInfo implements Serializable
	{
		private static final long serialVersionUID = 98121116105109L;

		public int object, attribute, owner;
		public OwnershipInfo( int object, int attribute, int owner )
		{
			this.object = object;
			this.attribute = attribute;
			this.owner = owner;
		}
		
		public boolean isUnowned()
		{
			return owner == -1;
		}
		
		public boolean isOwnedByRti()
		{
			return owner == 0;
		}
	}
	
	/** Enum for Save/Restore Status */
	public enum SRStatus{ UNINITIATED, INITIATED, BEGUN, SUCCESS, FAILURE };
	
	/** Information about an active Save/Restore */
	public class ActiveSR implements Serializable
	{
		private static final long serialVersionUID = 98121116105109L;

		public String label;
		public SRStatus status;
		public int federateHandle; // used on restore requests
		
		public void reset()
		{
			this.label = null;
			this.status = SRStatus.UNINITIATED;
		}
		
		public void initiate( String label )
		{
			this.label = label;
			this.status = SRStatus.INITIATED;
		}
		
		public void initiate( String label, int federateHandle )
		{
			this.label = label;
			this.federateHandle = federateHandle;
			this.status = SRStatus.INITIATED;
		}
		
		public void begun()
		{
			this.status = SRStatus.BEGUN;
		}

		public void success()
		{
			this.status = SRStatus.SUCCESS;
		}
		
		public void failure()
		{
			this.status = SRStatus.FAILURE;
		}
		
		public boolean isInitiated( String expectedLabel )
		{
			if( expectedLabel.equals(label) && this.status == SRStatus.INITIATED )
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		
		public boolean isBegun()
		{
			return status == SRStatus.BEGUN || status == SRStatus.INITIATED;
		}

		public boolean isSuccess()
		{
			return status == SRStatus.SUCCESS;
		}
		
		public boolean isFailure()
		{
			return status == SRStatus.FAILURE;
		}
		
		public boolean isComplete()
		{
			return (isSuccess() || isFailure());
		}
	}
}
