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
package org.portico.lrc.model;

import org.portico.lrc.PorticoConstants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.Serializable;

/**
 * This class represents a specific object instance. It contains all the relevant information
 * about the instance (such as name, handle and attributes).
 */
public class OCInstance implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int handle;
	private String name;
	private OCMetadata registeredType;
	private OCMetadata discoveredType;

	private Map<Integer,ACInstance> attributes;
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public OCInstance()
	{
		this.handle = PorticoConstants.NULL_HANDLE;
		this.name = "unknown";
		this.registeredType = null;
		this.discoveredType = null;
		this.attributes = new HashMap<Integer,ACInstance>();
	}
	
	public OCInstance( int handle, String name, OCMetadata registeredAs, OCMetadata discoveredAs )
	{
		this();
		this.handle = handle;
		this.name = name;
		this.registeredType = registeredAs;
		//this.discoveredType = discoveredAs;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Stores the given attribute in this object instance. This will set the container of the
	 * instance to this OCInstance and will store it locally. No check is made to see if this
	 * is actually valid according to the FOM or not. The attributes are stored locally in a
	 * map, the key to which is the handle of the attribute (as defined from the FOM). If there
	 * is already an attribute for that attribute handle in this object, it will be overwritten.
	 */
	public void addAttribute( ACInstance attribute )
	{
		// ignore the request if it is null
		if( attribute == null )
		{
			return;
		}
		
		// remove the association between this object and the currently stored attribute if one
		// is already in the local map
		int aHandle = attribute.getHandle();
		if( this.attributes.containsKey(aHandle) )
		{
			this.attributes.get(aHandle).setContainer( null );
		}
		
		// set us as the container
		attribute.setContainer( this );
		// store the attribute
		this.attributes.put( attribute.getHandle(), attribute );
	}
	
	/**
	 * This method will just call {@link #addAttribute(ACInstance) addAttribute(ACInstance)} for
	 * each of the attributes contained in the given set.
	 */
	public void addAllAttributes( Set<ACInstance> atts )
	{
		for( ACInstance temp : atts )
		{
			this.addAttribute( temp );
		}
	}
	
	/**
	 * Remove and return the attribute contained within this instance of the given handle. If there
	 * is no attribute for that handle, null will be returned. If there is, its contains variable
	 * will be set to null as it is removed and returned.
	 */
	public ACInstance removeAttribute( int handle )
	{
		ACInstance instance = attributes.remove( handle );
		if( instance != null )
		{
			instance.setContainer( null );
		}
		
		return instance;
	}
	
	/**
	 * Fetch and return the contained attribute of the given handle. If there is none, null will
	 * be returned.
	 */
	public ACInstance getAttribute( int handle )
	{
		return this.attributes.get( handle );
	}
	
	/**
	 * Returns a set of all the contained attributes for this object instance.
	 */
	public Set<ACInstance> getAllAttributes()
	{
		return new HashSet<ACInstance>( attributes.values() );
	}
	
	/**
	 * Returns a set of all the attribute instances contained within this instance that are owned
	 * by the federate of the given handle. If none are owned, an empty set is returned.
	 */
	public Set<ACInstance> getAllOwnedAttributes( int federateHandle )
	{
		HashSet<ACInstance> instances = new HashSet<ACInstance>();
		for( ACInstance attribute : attributes.values() )
		{
			if( attribute.getOwner() == federateHandle )
				instances.add( attribute );
		}
		
		return instances;
	}
	
	/**
	 * Returns <code>true</code> if the federate with the given handle owns at least one of the
	 * attributes contained in this instance. Returns <code>false</code> otherwise.
	 */
	public boolean ownsAttributes( int federateHandle )
	{
		for( ACInstance attribute : attributes.values() )
		{
			if( attribute.getOwner() == federateHandle )
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns a map containing the set of attributes that have regions associated with them. The
	 * keys are the attribute instances, the values are the region instances. If there are no
	 * attributes that have associations, an empty map is returned.
	 */
	public Map<ACInstance,RegionInstance> getAllRegionAssociatedAttributes()
	{
		HashMap<ACInstance,RegionInstance> map = new HashMap<ACInstance,RegionInstance>();
		for( ACInstance attribute : attributes.values() )
		{
			RegionInstance region = attribute.getRegion();
			if( region != null )
				map.put( attribute, region );
		}
		
		return map;
	}
	
	public int getHandle()
	{
		return handle;
	}

	public void setHandle( int handle )
	{
		this.handle = handle;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	/**
	 * Returns the handle of the owning federate or {@link PorticoConstants#NULL_HANDLE} if it is
	 * not owned. This method will look for the privilegeToDelete attribute within the instance and
	 * return the owner of that. If it can't be found for any reason, PorticoConstants.NULL_HANDLER
	 * will be returned.
	 * <p/>
	 * <i>The "owner" of the instance is defined as the federate that holds ownership of the
	 * privilegeToDelete attribute.</i>
	 */
	public int getOwner()
	{
		int privHandle = this.registeredType.getModel().getPrivilegeToDelete();
		for( ACInstance att : this.attributes.values() )
		{
			if( att.getHandle() == privHandle )
			{
				return att.getOwner();
			}
		}
		
		// if we get here, we didn't find the privilegeToDelete attribute
		return PorticoConstants.NULL_HANDLE;
	}

	/**
	 * Returns true if the given parameter is equal to result of {@link #getOwner() getOwned()},
	 * false otherwise.
	 * <p/>
	 * <i>The "owner" of the instance is defined as the federate that holds ownership of the
	 * privilegeToDelete attribute.</i>
	 */
	public boolean isOwner( int federateHandle )
	{
		return this.getOwner() == federateHandle;
	}
	
	/**
	 * Returns <code>true</code> if the instance has been discovered (that is, it has its
	 * discoveryType set), <code>false</code> otherwise.
	 */
	public boolean isDiscovered()
	{
		return this.discoveredType != null;
	}
	
	public OCMetadata getRegisteredType()
	{
		return registeredType;
	}

	public void setRegisteredType( OCMetadata registeredType )
	{
		this.registeredType = registeredType;
	}
	
	/**
	 * Depending on the subscription interests of a federate, objects can be discovered as a
	 * different type to what they actually are. This method will return the {@link OCMetadata}
	 * that corresponds to the type this object was discovered as (defaults to the type of the
	 * object class)
	 */
	public OCMetadata getDiscoveredType()
	{
		return this.discoveredType;
	}
	
	/**
	 * Depending on the subscription interests of a federate, objects can be discovered as a
	 * different type to what they actually are. This method will set the {@link OCMetadata}
	 * that corresponds to the type this object has been discovered as.
	 */
	public void setDiscoveredType( OCMetadata metadata )
	{
		this.discoveredType = metadata;
	}

	public int getDiscoveredClassHandle()
	{
		if( this.discoveredType == null )
			return PorticoConstants.NULL_HANDLE;
		else
			return this.discoveredType.getHandle();
	}

	public int getRegisteredClassHandle()
	{
		return this.registeredType.getHandle();
	}
	
	public boolean equals( Object other )
	{
		if( other instanceof OCInstance )
			return ((OCInstance)other).handle == this.handle;
		else
			return false;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
