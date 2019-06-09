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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.portico.lrc.model.ACMetadata;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.RegionInstance;
import org.portico2.common.PorticoConstants;

/**
 * This class represents an "LRC Object Class Instance" (LOCInstance).
 * <p/>
 * 
 * <b>What is an OCInstance? - A History Lesson</b>
 * <p/>
 * An "OCInstance is a container type that represents an object intstance within a federation.
 * In the earlier days of Portico when it was fully distributed, there was only one representation
 * of an object instance. It was duplicated within every LRC and was a mix of generic content
 * (links to FOM metadata object representing its type, instance name and so on) and content that
 * was relevant to the LOCAL FEDERATE, such as the object class it was <i>discovered</i> locally
 * as, which could be different from the type it was registered as.
 * <p/>
 * This was fine when all data was local. However, when moving to a central RTI, the RTI itself
 * has a need to track this information for <i>all</i> federates, and thus, the same structure
 * cannot be maintained. At the same time, we still need some basic tracking data local to the
 * federate. Given this, we must now split the humble "OCInstance" into one representation for
 * the LRC (the LOCInstance) and one for the RTI (the ROCInstance).
 */
public class LOCInstance
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int handle;
	private String name;
	private OCMetadata registeredType;
	private OCMetadata discoveredType;

	private Map<Integer,LACInstance> attributes;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private LOCInstance()
	{
		this.handle = PorticoConstants.NULL_HANDLE;
		this.name = "unknown";
		this.registeredType = null;
		this.discoveredType = null;
		this.attributes = new HashMap<>();
	}
	
	public LOCInstance( OCMetadata registeredType,
	                    int objectHandle,
	                    String objectName,
	                    int owningFederate,
	                    Set<Integer> publishedAttributes )
	{
		this();
		this.registeredType = registeredType;
		this.discoveredType = registeredType;
		this.handle = objectHandle;
		this.name = objectName;

		// Create instances for all the attributes
		// We mark ourselves as the owner for app published attributes. If the set of published
		// attributes provided is null or empty, this means we are publishing them all.
		
		// make sure we're not working on a null set
		if( publishedAttributes == null )
			publishedAttributes = Collections.emptySet();
		
		// populate all the attributes
		for( ACMetadata attributeClass : registeredType.getAllAttributes() )
		{
			int attributeHandle = attributeClass.getHandle();
			LACInstance attributeInstance = new LACInstance( attributeClass, this );
			// if the published set is empty it means we're publishing everything
			if( publishedAttributes.isEmpty() || publishedAttributes.contains(attributeHandle) )
				attributeInstance.setOwner( owningFederate );
			
			this.attributes.put( attributeHandle, attributeInstance );
		}
	}

	public LOCInstance( OCMetadata registeredType,
	                    OCMetadata discoveredType,
	                    int objectHandle,
	                    String objectName )
	{
		this();
		this.registeredType = registeredType;
		this.discoveredType = discoveredType;
		this.handle = objectHandle;
		this.name = objectName;

		// Create instances for all the attributes
		// We mark ourselves as the owner for app published attributes. If the set of published
		// attributes provided is null or empty, this means we are publishing them all.
		
		// Create instances for all the attributes
		for( ACMetadata attributeClass : registeredType.getAllAttributes() )
		{
			int attributeHandle = attributeClass.getHandle();
			LACInstance attributeInstance = new LACInstance( attributeClass, this );
			this.attributes.put( attributeHandle, attributeInstance );
		}
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Attributes and Ownership Methods   ////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Fetch and return the contained attribute of the given handle. If there is none, null will
	 * be returned.
	 */
	public LACInstance getAttribute( int handle )
	{
		return this.attributes.get( handle );
	}
	
	/**
	 * Returns a set of all the contained attributes for this object instance.
	 */
	public Set<LACInstance> getAllAttributes()
	{
		return new HashSet<LACInstance>( attributes.values() );
	}
	
	/**
	 * Returns a set of all the attribute instances contained within this instance that are owned
	 * by the federate of the given handle. If none are owned, an empty set is returned.
	 */
	public Set<LACInstance> getAllAttributesOwnedBy( int federateHandle )
	{
		return attributes.values().stream()
		                          .filter( a -> a.getOwner() == federateHandle )
		                          .collect( Collectors.toSet() );
	}
	
	/**
	 * Returns <code>true</code> if the federate with the given handle owns at least one of the
	 * attributes contained in this instance. Returns <code>false</code> otherwise.
	 */
	public boolean ownsAnyAttributes( int federateHandle )
	{
		for( LACInstance attribute : attributes.values() )
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
	public Map<LACInstance,RegionInstance> getAllRegionAssociatedAttributes()
	{
		HashMap<LACInstance,RegionInstance> map = new HashMap<LACInstance,RegionInstance>();
		for( LACInstance attribute : attributes.values() )
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

	public String getName()
	{
		return name;
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
		for( LACInstance att : this.attributes.values() )
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
	 * Returns <code>true</code> if the federate with the given handle owns at least one of the
	 * attributes contained in this instance. Returns <code>false</code> otherwise.
	 */
	public boolean ownsAttributes( int federateHandle )
	{
		for( LACInstance attribute : attributes.values() )
		{
			if( attribute.getOwner() == federateHandle )
				return true;
		}
		
		return false;
	}
	
	public OCMetadata getRegisteredType()
	{
		return registeredType;
	}

	public int getRegisteredClassHandle()
	{
		return this.registeredType.getHandle();
	}

	public OCMetadata getDiscoveredType()
	{
		return this.discoveredType;
	}
	
	public void setDiscoveredType( OCMetadata type )
	{
		this.discoveredType = type;
	}

	public int getDiscoveredClassHandle()
	{
		return this.discoveredType.getHandle();
	}

	/**
	 * Returns <code>true</code> if the instance has been discovered (that is, it has its
	 * discoveryType set), <code>false</code> otherwise.
	 */
	public boolean isDiscovered()
	{
		return this.discoveredType != null;
	}
	
	@Override
	public boolean equals( Object other )
	{
		if( other instanceof LOCInstance )
			return ((LOCInstance)other).handle == this.handle;
		else
			return false;
	}

	@Override
	public String toString()
	{
		return String.valueOf( this.handle );
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
