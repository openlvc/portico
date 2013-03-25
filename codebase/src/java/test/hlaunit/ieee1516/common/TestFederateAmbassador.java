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
package hlaunit.ieee1516.common;

import static hlaunit.ieee1516.common.TypeFactory.*;

import org.portico.impl.hla1516.types.HLA1516AttributeHandleSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.testng.Assert;

import hla.rti1516.AttributeHandle;
import hla.rti1516.AttributeHandleSet;
import hla.rti1516.AttributeHandleValueMap;
import hla.rti1516.InteractionClassHandle;
import hla.rti1516.LogicalTime;
import hla.rti1516.ObjectClassHandle;
import hla.rti1516.ObjectInstanceHandle;
import hla.rti1516.OrderType;
import hla.rti1516.ParameterHandleValueMap;
import hla.rti1516.SynchronizationPointFailureReason;
import hla.rti1516.TransportationType;
import hla.rti1516.jlc.NullFederateAmbassador;
import hlaunit.CommonSetup;

/**
 * <b>NOTE:</b> I have replaced all the timeout settings with a single, TestFederateAmbassador
 * specific value (set via the get/set defaultTimeout methods). So if you see a mention of a
 * timeout the doco for any methods, you'll know I now mean that default value.
 */
public class TestFederateAmbassador extends NullFederateAmbassador
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TestFederate federate;
	public Map<String,byte[]> announced;
	public Set<String> synched;
	public String syncSucceeded; // last sync point to get a "success" message for
	public String syncFailed; // last sync point to get a "failure" message for
	
	public boolean constrained;
	public boolean regulating;
	public double  logicalTime;
	
	protected HashMap<Integer,TestObject> instances;
	protected List<TestObject> discovered;
	protected HashSet<Integer> roUpdated;
	protected HashSet<Integer> tsoUpdated;
	protected HashSet<Integer> roRemoved;
	protected HashSet<Integer> tsoRemoved;
	protected HashMap<Integer,Set<Integer>> updatesRequested;
	
	protected List<TestInteraction> roInteractions;
	protected List<TestInteraction> tsoInteractions;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public TestFederateAmbassador( TestFederate federate )
	{
		this.federate = federate;
		
		this.announced = new HashMap<String,byte[]>();
		this.synched = new HashSet<String>();
		this.syncSucceeded = "";
		this.syncFailed = "";
		
		this.constrained = false;
		this.regulating = false;
		this.logicalTime = -1;
		
		this.instances = new HashMap<Integer,TestObject>();
		this.discovered = new Vector<TestObject>();
		this.roUpdated = new HashSet<Integer>();
		this.tsoUpdated = new HashSet<Integer>();
		this.roRemoved = new HashSet<Integer>();
		this.tsoRemoved = new HashSet<Integer>();
		this.updatesRequested = new HashMap<Integer,Set<Integer>>();
		
		this.roInteractions = new Vector<TestInteraction>();
		this.tsoInteractions = new Vector<TestInteraction>();
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Helper Methods //////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private long getTimeout()
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
	
	public HashMap<Integer,TestObject> getInstances()
	{
		return this.instances;
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
				throw new TimeoutException( "Time out waiting for sync point announce result" );
			}

			tick();
		}

		// we have the label //
		if( this.syncSucceeded.equals(label) )
		{
			return true;
		}
		else
		{
			return false;
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
		TestObject instance = instances.get( instanceHandle );
		if( instance.getClassHandle() != expectedClass )
		{
			Assert.fail( "Expected to discover object [" + instanceHandle + "] as instance of [" +
			             expectedClass + "]: was discovered as instance of [" + 
			             instance.getClassHandle() + "]" );
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
	public TestObject waitForLatestDiscovery( int classHandle )
	{
		long finishTime = getTimeout();
		do
		{
			// tick some to get the party started
			tick();

			// look for the first instance of the given class handle //
			for( TestObject temp : this.discovered )
			{
				if( temp.getClassHandle() == classHandle )
				{
					// this is the first instance of the given class, remove and return it
					this.discovered.remove( temp );
					return temp;
				}
			}
		}
		while( System.currentTimeMillis() < finishTime );

		// we didn't find the instance before the timeout :(
		throw new TimeoutException( "Timeout waiting for next discovery of instance from class [" +
		                            classHandle + "]" );
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
	public TestInteraction waitForROInteraction( int classHandle )
	{
		// wait until we get the interaction //
		TestInteraction interaction = null;
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
	public TestInteraction waitForROInteraction( String className )
	{
		// resolve the handle for the class name //
		int iHandle = -1;
		try
		{
			iHandle = getInteractionHandle( federate.rtiamb.getInteractionClassHandle(className) );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception resolving interaction handle from name: " +
			             className + ". Can't wait for RO interaction", e );
		}
		
		// wait for the interaction //
		return waitForROInteraction( iHandle );
	}
	
	private TestInteraction fetchROInteraction( int classHandle )
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
			iHandle = getInteractionHandle( federate.rtiamb.getInteractionClassHandle(className) );
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
	public TestInteraction waitForTSOInteraction( int classHandle )
	{
		// wait until we get the interaction //
		TestInteraction interaction = null;
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
	public TestInteraction waitForTSOInteraction( String className )
	{
		// resolve the handle for the class name //
		int iHandle = -1;
		try
		{
			iHandle = getInteractionHandle( federate.rtiamb.getInteractionClassHandle(className) );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception resolving interaction handle from name: " +
			             className + ". Can't wait for TSO interaction", e );
		}
		
		// wait for the interaction //
		return waitForTSOInteraction( iHandle );
	}
	
	private TestInteraction fetchTSOInteraction( int classHandle )
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
			iHandle = getInteractionHandle( federate.rtiamb.getInteractionClassHandle(className) );
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
	private void validateEventTime( double eventTime, String event )
	{
		if( logicalTime >= eventTime )
		{
			String message = "**PAST EVENT** Received event [" +event+ "] with timestamp [" +
			                 eventTime+ "], but current time is [" +logicalTime+ "]";
			Assert.fail( message );
			//System.err.println( message );
			//throw new RuntimeException( message );
		}
	}

	/////////////////////////////////////////////////////////////////////////
	////////////////////////////// Sync Points //////////////////////////////
	/////////////////////////////////////////////////////////////////////////
	@Override
	public void announceSynchronizationPoint( String label, byte[] tag )
	{
		this.announced.put( label, tag );
	}
	
	@Override
	public void synchronizationPointRegistrationFailed( String label,
	                                                    SynchronizationPointFailureReason reason )
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

	/////////////////////////////////////////////////////////////////////////
	//////////////////////////// Time Callbacks /////////////////////////////
	/////////////////////////////////////////////////////////////////////////
	@Override
	public void timeConstrainedEnabled( LogicalTime time )
	{
		this.constrained = true;
		this.logicalTime = TypeFactory.fromTime( time ); 
	}
	
	@Override
	public void timeRegulationEnabled( LogicalTime time )
	{
		this.regulating = true;
		this.logicalTime = TypeFactory.fromTime( time );
	}
	
	@Override
	public void timeAdvanceGrant( LogicalTime time )
	{
		validateEventTime( TypeFactory.fromTime(time), "timeAdvanceGrant" );
		this.logicalTime = TypeFactory.fromTime( time );
	}

	/////////////////////////////////////////////////////////////////////////
	//////////////////////// Discover/Remove Methods ////////////////////////
	/////////////////////////////////////////////////////////////////////////
	@Override
	public void discoverObjectInstance( ObjectInstanceHandle theObject,
	                                    ObjectClassHandle theObjectClass,
	                                    String objectName )
	{
   		int objectHandle = TypeFactory.getObjectHandle( theObject );
   		int classHandle = TypeFactory.getObjectClassHandle( theObjectClass );

   		TestObject instance = new TestObject( objectHandle, classHandle, objectName );
   		this.instances.put( objectHandle, instance );
   		this.discovered.add( instance );
	}
	
	@Override
	public void removeObjectInstance( ObjectInstanceHandle theObject,
	                                  byte[] userSuppliedTag,
	                                  OrderType sentOrdering )
	{
		int objectHandle = TypeFactory.getObjectHandle( theObject );
   		TestObject instance = this.instances.remove( objectHandle );
   		this.discovered.remove( instance ); // in case it is still hanging around
   		this.roRemoved.add( objectHandle );
	}
	
	@Override
	public void removeObjectInstance( ObjectInstanceHandle theObject,
	                                  byte[] userSuppliedTag,
	                                  OrderType sentOrdering,
	                                  LogicalTime theTime,
	                                  OrderType receivedOrdering ) 
	{
		if( theTime != null )
			validateEventTime( TypeFactory.fromTime(theTime), "removeObjectInstance" );
		
		int objectHandle = TypeFactory.getObjectHandle( theObject );
   		TestObject instance = this.instances.remove( objectHandle );
   		this.discovered.remove( instance ); // in case it is still hanging around
   		this.tsoRemoved.add( objectHandle );
	}

	/////////////////////////////////////////////////////////////////////////
	//////////////////////// Provide Update Methods /////////////////////////
	/////////////////////////////////////////////////////////////////////////
	@Override
	public void provideAttributeValueUpdate( ObjectInstanceHandle theObject,
	                                         AttributeHandleSet theAttributes,
	                                         byte[] userSuppliedTag )	
	{
		try
		{
			//System.out.println( "Received update for [" + theObject + "] with [" +
			//theAttributes.size() + "] attributes" );
			updatesRequested.put( TypeFactory.getObjectHandle(theObject),
			                      HLA1516AttributeHandleSet.toJavaSet(theAttributes) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception in provide update callback: " + e.getMessage(), e );
		}
	}

	/////////////////////////////////////////////////////////////////////////
	//////////////////////////// Reflect Methods ////////////////////////////
	/////////////////////////////////////////////////////////////////////////
	@Override
	public void reflectAttributeValues( ObjectInstanceHandle theObject,
	                                    AttributeHandleValueMap theAttributes,
	                                    byte[] userSuppliedTag, OrderType sentOrdering,
	                                    TransportationType theTransport )
	{
		// fetch the instance and update it's values //
		TestObject instance = this.instances.get( TypeFactory.getObjectHandle(theObject) );
		for( AttributeHandle attHandle : theAttributes.keySet() )
		{
			int handle = TypeFactory.getAttributeHandle( attHandle );
			byte[] value = theAttributes.get( attHandle );
			instance.attributes.put( handle, value );
		}
		
		// mark the instance as recently updated //
		this.roUpdated.add( TypeFactory.getObjectHandle(theObject) );
	}
	
	@Override
	public void reflectAttributeValues( ObjectInstanceHandle theObject,
	                                    AttributeHandleValueMap theAttributes,
	                                    byte[] userSuppliedTag,
	                                    OrderType sentOrdering,
	                                    TransportationType theTransport,
	                                    LogicalTime theTime,
	                                    OrderType receivedOrdering )
	{
		if( theTime != null )
			validateEventTime( TypeFactory.fromTime(theTime), "reflectAttributeValues" );
		
		// fetch the instance and update it's values //
		TestObject instance = this.instances.get( TypeFactory.getObjectHandle(theObject) );
		for( AttributeHandle attHandle : theAttributes.keySet() )
		{
			int handle = TypeFactory.getAttributeHandle( attHandle );
			byte[] value = theAttributes.get( attHandle );
			instance.attributes.put( handle, value );
		}
		
		// mark the instance as recently updated //
		this.tsoUpdated.add( TypeFactory.getObjectHandle(theObject) );
	}
	
	/////////////////////////////////////////////////////////////////////////
	////////////////////////// Interaction Methods //////////////////////////
	/////////////////////////////////////////////////////////////////////////
	@Override
	public void receiveInteraction( InteractionClassHandle interactionClass,
	                                ParameterHandleValueMap theParameters,
	                                byte[] userSuppliedTag,
	                                OrderType sentOrdering,
	                                TransportationType theTransport )
	{
   		// store the interaction information //
   		roInteractions.add( new TestInteraction(getInteractionHandle(interactionClass),
   		                                        theParameters,
   		                                        userSuppliedTag) );
	}
	
	@Override
	public void receiveInteraction( InteractionClassHandle interactionClass,
	                                ParameterHandleValueMap theParameters,
	                                byte[] tag,
	                                OrderType sentOrdering,
	                                TransportationType theTransport,
	                                LogicalTime theTime,
	                                OrderType receivedOrdering )
	{
		if( theTime != null )
			validateEventTime( TypeFactory.fromTime(theTime), "receiveInteraction" );
		
		try
		{
    		double time = TypeFactory.fromTime( theTime );
    		int classHandle = TypeFactory.getInteractionHandle( interactionClass );
    		tsoInteractions.add( new TestInteraction(classHandle,theParameters,tag,time) );
		}
		catch( Exception e )
		{
			Assert.fail( "Exception in interaction(time) callback: " + e.getMessage(), e );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
