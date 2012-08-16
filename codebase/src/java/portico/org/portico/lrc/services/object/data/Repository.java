/*
 *   Copyright 2008 The Portico Project
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
package org.portico.lrc.services.object.data;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCState;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.RegionInstance;
import org.portico.lrc.services.saverestore.data.SaveRestoreTarget;

/**
 * The {@link Repository} is where all object information maintained by the Portico LRC is stored.
 * Internally, the Repository contains two separate stores: one for <b>discovered</b> instances
 * (those that the local federate has received a discoverObjectInstance() callback for) and
 * <b>undiscovered</b> instances (instances that the LRC has been notified about, but for which
 * the federate has not yet been notified about - generally due to the lack of subscription interest
 * in the class of the object).
 */
public class Repository implements SaveRestoreTarget
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private LRCState state;
	private Map<Integer,OCInstance> undiscovered;
	private Map<Integer,OCInstance> discovered;
	private Map<String,Integer> reservedNames; // used or reserved names

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public Repository( LRCState state )
	{
		this.state = state;
		this.undiscovered = new HashMap<Integer,OCInstance>();
		this.discovered = new HashMap<Integer,OCInstance>();
		this.reservedNames = new HashMap<String,Integer>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////// Methods for Discovered/Subscribed Instances ///////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// These are the general methods that are used to interact with the repository, they refer
	// to objects that the federate wants to know about (has a valid subscription to at the time
	// of registration normally).
	/**
	 * Takes the given instance from the "undiscovered" internal store and puts it in the
	 * "discovered" internal store. After this call, the instance will be available through
	 * calls to {@link #getInstance(int)} (and its other variants), and will *no longer* be
	 * available through {@link #getUndiscoveredInstance(int)} (and its variants).
	 */
	public void discoverInstance( OCInstance instance, OCMetadata discoveredType )
	{
		if( instance == null )
			return;

		instance.setDiscoveredType( discoveredType );
		discovered.put( instance.getHandle(), instance );
		// remove the object from the undiscovered instances (will do nothing if not in there)
		undiscovered.remove( instance.getHandle() );
		reservedNames.remove( instance.getName() );
	}
	
	/**
	 * Fetch and return the *discovered* {@link OCInstance} with the given handle. If there is
	 * no instance, <code>null</code> is returned. Note that the repository might know about the
	 * instance, but it might be in the "undiscovered" part of the repository.
	 */
	public OCInstance getInstance( int objectHandle )
	{
		return discovered.get( objectHandle );
	}
	
	/**
	 * Fetch and return the *discovered* {@link OCInstance} with the given name. If there is
	 * no instance, <code>null</code> is returned. Note that the repository might know about the
	 * instance, but it might be in the "undiscovered" part of the repository.
	 */
	public OCInstance getInstance( String name )
	{
		for( OCInstance instance : discovered.values() )
			if( instance.getName().equals(name) )
				return instance;
		
		return null;
	}
	
	/**
	 * Get a collection of all the *discovered* {@link OCInstance}s.
	 */
	public Collection<OCInstance> getAllInstances()
	{
		return discovered.values();
	}
	
	/**
	 * Return a set of all the discovered {@link OCInstance} types in the repository that are of the
	 * given class handle. Note that this includes only the ACTUAL class, not any of its parents.
	 */
	public Set<OCInstance> getAllInstances( int classHandle )
	{
		HashSet<OCInstance> objects = new HashSet<OCInstance>();
		for( OCInstance instance : discovered.values() )
			if( instance.getDiscoveredClassHandle() == classHandle )
				objects.add( instance );
		
		return objects;
	}

	/**
	 * Remove the object with the given handle from the *discovered* collection. This won't affect
	 * the undiscovered collection in any way.
	 */
	public OCInstance removeDiscoveredInstance( int objectHandle )
	{
		return discovered.remove( objectHandle );
	}

	/**
	 * Returns <code>true</code> if the *discovered* collection contains an object with the given
	 * handle, <code>false</code> otherwise. The undiscovered collection is never consulted.
	 */
	public boolean containsInstance( int objectHandle )
	{
		return discovered.containsKey( objectHandle );
	}

	/**
	 * Returns an array of all the {@link OCInstance}s that the identified federate "controls" 
	 * (that is, the ones it has the privilege to delete). This only consults the *discovered*
	 * collection.
	 */
	public OCInstance[] getControlledData( int federate )
	{
		ArrayList<OCInstance> data = new ArrayList<OCInstance>();
		for( OCInstance instance : discovered.values() )
		{
			if( instance.isOwner(federate) )
				data.add( instance );
		}
		
		return data.toArray( new OCInstance[data.size()] );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	//////////////////// Methods for Undiscovered/Non-Subscribed Instances ////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// When a federate subscribes to an object class for the first time, it should receive
	// discoveries for all the instances that exist and it is now happy to know about (but
	// has not yet discovered). However, this requires that object information about undiscovered
	// instances be stored somewhere. These methods provide support for this. Data added to
	// the repository this way will not show up with the normal methods are being used.
	/**
	 * Add the given {@link OCInstance} to the store of *undiscovered* instances. These instances
	 * are only accessible through xxxUndiscoveredInstancexxx() style methods.
	 */
	public void addUndiscoveredInstance( OCInstance instance )
	{
		if( instance == null )
			return;
		
		instance.setDiscoveredType( null );
		undiscovered.put( instance.getHandle(), instance );
		reservedNames.remove( instance.getName() );
	}

	/**
	 * Fetch and return the *undiscovered* {@link OCInstance} with the given handle. If there is
	 * no instance, <code>null</code> is returned.
	 */
	public OCInstance getUndiscoveredInstance( int handle )
	{
		return undiscovered.get( handle );
	}
	
	/**
	 * Loops through all the undiscovered instances looking for one with the given name. If one is
	 * found, it is returned, if one isn't, <code>null</code> is returned.
	 */
	public OCInstance getUndiscoveredInstance( String name )
	{
		for( OCInstance instance : undiscovered.values() )
			if( instance.getName().equals(name) )
				return instance;
		
		return null;
	}

	/**
	 * Get the collection of all known *undiscovered* instances.
	 */
	public Collection<OCInstance> getAllUndiscoveredInstances()
	{
		return undiscovered.values();
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Miscellaneous Methods //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will search the discovered and undiscovered stores for the object with the
	 * given handle. If it is found, it will be returned, otherwise null is returned.
	 */
	public OCInstance getDiscoveredOrUndiscovered( int objectHandle )
	{
		if( discovered.containsKey(objectHandle) )
			return discovered.get( objectHandle );
		else
			return undiscovered.get( objectHandle );
	}

	/**
	 * This removes the {@link OCInstance} with the given handle from either the discovered or
	 * undiscovered collection (wherever it is) and returns it. If there is no instance with that
	 * handle in either collection, null is returned.
	 */
	public OCInstance deleteDiscoveredOrUndiscovered( int objectHandle )
	{
		if( discovered.containsKey(objectHandle) )
			return discovered.remove( objectHandle );
		else if( undiscovered.containsKey(objectHandle) )
			return undiscovered.remove( objectHandle );
		else
			return null;
	}
	
	/**
	 * Remove the {@link OCInstance} from the discovered collection and put it in the undiscovered
	 * collection. This method will also {@link OCInstance#setDiscoveredType(OCMetadata)} set the
	 * discovered type) of the instance to <code>null</code> to reflect the fact that it now has
	 * no discovered type.
	 */
	public void undiscoverInstance( OCInstance instance )
	{
		// reset the discovered type, as we no longer have one!
		instance.setDiscoveredType( null );
		
		// remove the object from the discovered collection and insert it into the undiscovered one
		discovered.remove( instance.getHandle() );
		undiscovered.put( instance.getHandle(), instance );
	}

	/**
	 * Finds and returns the name of the object corresponding to the given handle, whether that
	 * object is discovered or not. If no object for the given handle can be found, the string
	 * "&lt;unknown&gt;" will be returned.
	 */
	public String findObjectName( int objectHandle )
	{
		OCInstance instance = discovered.get( objectHandle );
		if( instance != null )
			return instance.getName();
		
		instance = undiscovered.get( objectHandle );
		if( instance != null )
			return instance.getName();
		else
			return "<unknown>";
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Object Creation Methods /////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Create a new {@link OCInstance}, populate it with the given data and return it. This method
	 * will generate a new object handle (using {@link LRCState#nextObjectHandle()}) and will assign
	 * it to the instance. For each of the given attributes in <code>publishedAttributes</code> it
	 * will assign the local federate (idenfieid by {@link LRCState#getFederateHandle()}) as the
	 * owner. The others will be left as having no owner.
	 * <p/>
	 * If the given name is <code>null</code>, a new name will be auto generated for the object.
	 * If the given name is NOT <code>null</code>, that name will be used for the object. This
	 * means it is the responsibility of the caller to ensure that the name is free and available
	 * for use within the federation.
	 * 
	 * @throws JRTIinternalError If this registration pushes the federate past the max number of
	 *                           objects it is allowed to register
	 */
	public OCInstance newInstance( OCMetadata objectClass,
	                               String objectName,
	                               Set<Integer> publishedAttributes )
		throws JRTIinternalError
	{
		// get the next available handle
		OCInstance newInstance = objectClass.newInstance( state.getFederateHandle(),
		                                                  publishedAttributes );
		newInstance.setHandle( state.nextObjectHandle() );
		if( objectName == null )
			objectName = "HLA" + newInstance.getHandle();
		newInstance.setName( objectName );
		newInstance.setDiscoveredType( objectClass );
		newInstance.setRegisteredType( objectClass );
		return newInstance;
	}
	
	/**
	 * This method is much like {@link #newInstance(OCMetadata, String, Set)} except that it is
	 * geared towards remote federates discovering an object rather than the local federate
	 * registering one. A new {@link OCInstance} will be created and have its name and handle set
	 * to the given values. For each of the attribute handles in the <code>ownedAttributes</code>
	 * array, this method will set the owner of those attributes to the value given in the first
	 * parameter (<code>owningFederate</code>).
	 * 
	 * @param owningFederate The federate who is the creator of this object
	 * @param registeredType The ACTUAL type the object was registered as
	 * @param discoveredType The type the object has been discovered as
	 * @param objectHandle The handle for the object
	 * @param objectName The name for the object
	 * @param ownedAttributes The attributes that are owned by the creating federate (those that
	 *                        the creating federate published.
	 * @param regionTokens The regions tokens that are associated with each particular attribute
	 */
	public OCInstance newInstance( int owningFederate,
	                               OCMetadata registeredType,
	                               OCMetadata discoveredType,
	                               int objectHandle,
	                               String objectName,
	                               int[] ownedAttributes,
	                               int[][] regionTokens )
	{
		HashSet<Integer> publishedSet = new HashSet<Integer>();
		for( int handle : ownedAttributes )
			publishedSet.add( handle );
		
		OCInstance newInstance = registeredType.newInstance( owningFederate, publishedSet );
		newInstance.setHandle( objectHandle );
		newInstance.setName( objectName );
		newInstance.setRegisteredType( registeredType );
		newInstance.setDiscoveredType( discoveredType );

		// set up the region information
		for( int i = 0; i < regionTokens.length; i++ )
		{
			int attributeHandle = regionTokens[i][0];
			int regionToken = regionTokens[i][1];
			
			// because we might have discovered the object at a higher class than it actually
			// is (due to subscription), there might be some attributes that are in the region
			// token array that are not in our instance, in that case, just skip them
			ACInstance attributeInstance = newInstance.getAttribute( attributeHandle );
			if( attributeInstance == null )
				continue;
			
			RegionInstance region = state.getRegionStore().getRegion( regionToken );
			attributeInstance.setRegion( region );
		}
		
		return newInstance;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// Methods for Name Registration Handling /////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	public int getObjectHandleForName( String name )
	{
		// nobody has registered the name, check to see if anyone is using the name, which 
		// could happen if (for whatever reason) they choose a name that classes with our
		// default naming scheme (not likely, but possible)
		for( OCInstance instance : discovered.values() )
		{
			if( name.equals(instance.getName()) )
				return instance.getHandle();
		}
		
		for( OCInstance instance : undiscovered.values() )
		{
			if( name.equals(instance.getName()) )
				return instance.getHandle();
		}

		// nobody has the name
		return PorticoConstants.NULL_HANDLE;
	}
	
	public int getReserverOfName( String name )
	{
		// check to see if anyone has registered the name yet
		Integer currentHolder = reservedNames.get( name );
		if( currentHolder != null )
			return currentHolder;
		else
			return PorticoConstants.NULL_HANDLE;
	}
	
	/**
	 * This stores the federate has having reserved the name. NOTE: No checking to see if someone
	 * else has already reserved the name or not, this call will just overwrite what was previously
	 * there. All checking will need to be done by the handler that uses this service.
	 * @param federateHandle
	 * @param name
	 */
	public void reserveName( int federateHandle, String name )
	{
		reservedNames.put( name, federateHandle );
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Save/Restore Methods /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	public void saveToStream( ObjectOutput output ) throws Exception
	{
		output.writeObject( undiscovered );
		output.writeObject( discovered );
		output.writeObject( reservedNames );
	}

	@SuppressWarnings("unchecked")
	public void restoreFromStream( ObjectInput input ) throws Exception
	{
		this.undiscovered = (Map<Integer,OCInstance>)input.readObject();
		this.discovered = (Map<Integer,OCInstance>)input.readObject();
		this.reservedNames = (Map<String,Integer>)input.readObject();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
