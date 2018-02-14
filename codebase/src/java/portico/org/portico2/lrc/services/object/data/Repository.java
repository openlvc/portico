/*
 *   Copyright 2018 The Portico Project
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
package org.portico2.lrc.services.object.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.compat.JObjectAlreadyRegistered;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.RegionInstance;
import org.portico2.common.services.ddm.data.RegionStore;

public class Repository
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<Integer,OCInstance> discovered;
	private Map<Integer,OCInstance> undiscovered;
	private RegionStore regionStore;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Repository( RegionStore regionStore )
	{
		this.discovered = new HashMap<>();
		this.undiscovered = new HashMap<>();
		this.regionStore = regionStore;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void addObject( OCInstance instance ) throws JObjectAlreadyRegistered
	{
		int handle = instance.getHandle();
		if( discovered.containsKey(handle) )
			throw new JObjectAlreadyRegistered( "Object handle [%d] already exists in repository", handle ); 
	
		this.discovered.put( handle, instance );
	}

	/**
	 * Returns the {@link OCInstance} associated with the given handle (or null if there isn't one).
	 * 
	 * @param objectHandle The handle of the instance to get the object for
	 * @return The instance associated with the handle, or null of one isn't registered
	 */
	public OCInstance getObject( int objectHandle )
	{
		return this.discovered.get( objectHandle );
	}
	
	public OCInstance deleteObject( int objectHandle )
	{
		return this.discovered.remove( objectHandle );
	}
	
	public boolean containsObject( int objectHandle )
	{
		return this.discovered.containsKey( objectHandle );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	///  Methods for Discovered/Subscribed Instances  /////////////////////////////////////////
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
	public void discoverInstance2( OCInstance instance, OCMetadata discoveredType )
	{
		if( instance == null )
			return;

		instance.setDiscoveredType( discoveredType );
		discovered.put( instance.getHandle(), instance );
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

	///////////////////////////////////////////////////////////////////////////////////////////
	///  Object Creation Methods  /////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
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
			
			RegionInstance region = regionStore.getRegion( regionToken );
			attributeInstance.setRegion( region );
		}
		
		return newInstance;
	}

	/**
	 * The local federate has created a new object instance and had that confirmed by the RTI.
	 * Now we need to create the new instance so we can add it to the federate's local repository.
	 * The signature for this method is designed to make it simpler for the information that is
	 * readily at hand when creating the instance (rather than the information that is packed into
	 * a discovery callback).
	 * <p/>
	 * NOTE: This will _not_ add the object instance to the repository. It will just create it.
	 * 
	 * @param owningFederate      The handle of the local federate that created the instance
	 * @param registeredType      The class of the object that we created
	 * @param objectHandle        The handle that was returned from the RTI for the new instance
	 * @param objectName          The name that was returned from the RTI for the new instance
	 * @param publishedAttributes The set of attributes the local federate publishes for the class
	 * @return A newly created and populated {@link OCInstance} instance.
	 */
	public OCInstance newInstance( int owningFederate,
	                               OCMetadata registeredType,
	                               int objectHandle,
	                               String objectName,
	                               Set<Integer> publishedAttributes )
	{
		OCInstance newInstance = registeredType.newInstance( owningFederate, publishedAttributes );
		newInstance.setHandle( objectHandle );
		newInstance.setName( objectName );
		newInstance.setRegisteredType( registeredType );
		newInstance.setDiscoveredType( registeredType );
		
		// TODO Add Region Support
		return newInstance;
	}

	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
