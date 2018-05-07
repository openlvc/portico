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
package org.portico2.rti.services.object.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JObjectAlreadyRegistered;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.OCMetadata;
import org.portico2.common.services.ddm.data.RegionStore;

/**
 * 
 */
public class Repository
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private AtomicInteger nextObjectHandle;
	private Map<Integer,ROCInstance> objectsByHandle;
	private Map<String,ROCInstance>  objectsByName;
	private Map<String,Integer>      reservedNames;
	
	private RegionStore regionStore;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Repository( RegionStore regionStore )
	{
		this.nextObjectHandle = new AtomicInteger( 0 );
		this.objectsByHandle = new HashMap<>();
		this.objectsByName = new HashMap<>();
		this.reservedNames = new HashMap<>();
		
		this.regionStore = regionStore;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Create and store a new {@link OCInstance}. Populate it with the given data. This method
	 * will generate a new object handle and will assign it to the instance. For each of the given
	 * attributes in <code>publishedAttributes</code> it will assign the identified federate as the
	 * owner. The others will be left as having no owner.
	 * <p/>
	 * If the given name is <code>null</code>, a new name will be auto generated for the object.
	 * If the given name is NOT <code>null</code>, that name will be used for the object. This
	 * means it is the responsibility of the caller to ensure that the name is free and available
	 * for use within the federation.
	 * 
	 * @param federateHandle       The handle of the creating federate
	 * @param objectClass          The object class that this instance is of
	 * @param objectName           The name of the object. If empty or null it will be auto-assigned
	 * @param publishedAttributes  The attributes that are published and thus owned by the creator
	 * @return A populated {@link OCInstance} that has been stored inside the repository
	 * @throws JRTIinternalError If this registration pushes the federate past the max number of
	 *                           objects it is allowed to register
	 */
	public synchronized ROCInstance createObject( OCMetadata objectClass,
	                                              String objectName,
	                                              int ownerHandle,
	                                              Set<Integer> publishedAttributes )
	{
		// generate the handle and name
		int objectHandle = nextObjectHandle.incrementAndGet();
		if( objectName == null )
			objectName = "HLA"+objectHandle;
		else if( objectsByName.containsKey(objectName) )
			throw new JObjectAlreadyRegistered( objectName+" in use" );
		
		// create the new instance for the object
		ROCInstance object = new ROCInstance( objectClass,
		                                      objectHandle,
		                                      objectName,
		                                      ownerHandle,
		                                      publishedAttributes );
		
		return object;
	}

	public synchronized void addObject( ROCInstance instance )
	{
		this.storeObject( instance );
	}
	
	/**
	 * Removes and returns the object with the given handle from the repository. If there is no
	 * object with the given handle, null is returned.
	 * 
	 * @param instanceHandle The handle of the object to remove
	 * @return The object that was removed, or null if we couldn't find such an object
	 */
	public synchronized ROCInstance deleteObject( int instanceHandle )
	{
		ROCInstance removed = objectsByHandle.remove( instanceHandle );
		if( removed != null )
			objectsByName.remove( removed.getName() );
		
		return removed;
	}

	public ROCInstance getObject( int handle )
	{
		return objectsByHandle.get( handle );
	}

	public ROCInstance getObject( String name )
	{
		return objectsByName.get( name );
	}

	/**
	 * @return A collection of all the object in the repository. 
	 */
	public Collection <ROCInstance> getAllInstances()
	{
		return objectsByHandle.values();
	}

	/**
	 * @return A set of all the {@link ROCInstance} types in the repository that are explicitly
	 *         registered with the given class handle (_NOT_ any parent of the type).
	 */
	public Set<ROCInstance> getAllInstances( int classHandle )
	{
		HashSet<ROCInstance> objects = new HashSet<>();
		for( ROCInstance instance : objectsByHandle.values() )
		{
			if( instance.getRegisteredClassHandle() == classHandle )
				objects.add( instance );
		}
		
		return objects;
	}

	/**
	 * Returns a set of all {@link ROCInstance}s that are either explicitly of the given class,
	 * or are "assignable" to it (that is, are a subclass of).
	 * 
	 * @param initialClass Find all instances that are types (or subtypes) of this class
	 * @return The set of all instances that are types (or subtypes) of the given class
	 */
	public Set<ROCInstance> getAllInstancesAssignableFrom( OCMetadata initialClass )
	{
		HashSet<ROCInstance> objects = new HashSet<>();
		for( ROCInstance instance : objectsByHandle.values() )
		{
			if( instance.getRegisteredType().isAssignableTo(initialClass) )
				objects.add( instance );
		}
		
		return objects;
	}
	
	public boolean containsObject( int handle )
	{
		return objectsByHandle.containsKey( handle );
	}
	
	public boolean containsObject( String name )
	{
		return objectsByName.containsKey( name );
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	///  Methods for Name Registration Handling  /////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
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
	 * Return <code>true</code> if the name is already reserved or is in use. 
	 * 
	 * @param name The name to look up 
	 * @return <code>true</code> if the name is reserved or in use; <code>false</code> otherwise
	 */
	public boolean isNameReservedOrInUse( String name )
	{
		return reservedNames.containsKey(name) || objectsByName.containsKey(name);
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

	//////////////////////////////////////////////////////////////////////////////////////////
	/// Private Helper Methods  //////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	private synchronized void storeObject( ROCInstance instance )
	{
		this.objectsByHandle.put( instance.getHandle(), instance );
		this.objectsByName.put( instance.getName(), instance );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
