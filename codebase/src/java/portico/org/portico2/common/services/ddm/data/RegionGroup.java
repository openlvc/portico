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
package org.portico2.common.services.ddm.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.model.RegionInstance;
import org.portico2.common.services.pubsub.data.OCInterest;

/**
 * This class represents an association between a set of attributes and a particular
 * {@link RegionInstance}. It is used in the {@link OCInterest} class to associate a set of
 * attributes and a region for subscription purposes. Although it is also used in publication data
 * calculations, this is only because the {@link OCInterest} class is meant to be agnostic to being
 * used for either publication or subscription.
 * <p/>
 * <b>Implementaiton Note:</b> In situations where DDM information isn't relevant to the use of
 * instances of this class (such as when an object publication is being represented by the
 * {@link OCInterest} with which this class is associated), the value <code>null</code> should be
 * used to represent the {@link RegionInstance}. In this case, <code>null</code> is somewhat akin
 * to the "default region" mentioned in the spec. <i>Repeating:</i> <code>null</code> is the
 * default region instance and the region at which all non-DDM subscription interests are held.
 */
public class RegionGroup implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<Integer,Set<RegionInstance>> associations;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public RegionGroup()
	{
		this.associations = new HashMap<Integer,Set<RegionInstance>>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * This method will first remove any interest that currently exists in *ANY* attributes if that
	 * interest is associated with the given region. After this, it will make an association
	 * between the given attributes and the region. If you imagine a given region to have a
	 * particular set of regions associated with it, this call will replace that set of attributes
	 * with the given set of attributes. If the two sets are the same, there is no difference, but
	 * if the old set has attributes that are NOT in the new set, those attributes will no longer
	 * have a subscription associated with the region.
	 */
	public void replace( RegionInstance region, Set<Integer> attributes )
	{
		// as we are REPLACING the existing interest, we must first remove any
		// association with the given region that we already contain
		// we need a new set of handles so that we can modify the original associations map
		// without causing a ConcurrentModificationException
		HashSet<Integer> keySetCopy = new HashSet<Integer>( associations.keySet() );
		for( Integer existingAttribute : keySetCopy )
		{
			Set<RegionInstance> regionSet = associations.get( existingAttribute );
			regionSet.remove( region );
			if( regionSet.isEmpty() )
				associations.remove( existingAttribute );
		}
		
		//for( Set<RegionInstance> existingSet : associations.values() )
		//	existingSet.remove( region );

		// now that any existing association has been removed, we make the
		// association with the new, incoming attributes
		for( Integer incomingAttribute : attributes )
		{
			Set<RegionInstance> regions = associations.get( incomingAttribute );
			if( regions == null )
			{
				// only create the region instance set if it doesn't already exist
				regions = new HashSet<RegionInstance>();
				associations.put( incomingAttribute, regions );
			}
			
			regions.add( region );
		}
	}

	/**
	 * Augments any existing association for the region by adding the provided attributes to the
	 * set of those that are currently associated with the region.
	 */
	public void augment( RegionInstance region, Set<Integer> attributes )
	{
		for( Integer attribute : attributes )
		{
			Set<RegionInstance> regions = associations.get( attribute );
			if( regions == null )
			{
				// mapping doesn't exist yet, create and add it
				regions = new HashSet<RegionInstance>();
				associations.put( attribute, regions );
			}
			
			regions.add( region );
		}
	}

	/**
	 * This method will remove <b>any</b> interest associated with the given {@link RegionInstance}
	 * in the provided attributes.
	 * <p/>
	 * If the {@link RegionInstance} is <code>null</code>, this call will remove <b>all</b>
	 * associations (the same as calling {@link #empty}).
	 */
	public void remove( RegionInstance region, Set<Integer> attributes )
	{
		// null region means the default region (caused by a non-ddm request), so we just
		// have to wipe everything out
		if( region == null )
		{
			empty();
			return;
		}
		
		// for each attribute, remove the region association
		for( Integer attribute : attributes )
		{
			Set<RegionInstance> regions = associations.get( attribute );
			if( regions == null )
				continue;

			regions.remove( region );
			if( regions.isEmpty() )
				associations.remove( attribute );
		}
	}

	/**
	 * This is like {@link #remove(RegionInstance, Set)} except that it will remove the interest
	 * in ALL attributes associated with the given region. If the region is null, this is taken
	 * to be a non-DDM request, and as such, all interests (regardless of region association) will
	 * be removed (essentially calling {@link #empty()}.
	 */
	public void remove( RegionInstance region )
	{
		// we have to make a copy of the key set when removing the association for all keys
		// because otherwise we'd get a ConcurrentModificationException when attempting to
		// remove information from the map if there are no more region associations
		remove( region, new HashSet<Integer>(associations.keySet()) );
	}

	/**
	 * Returns <code>true</code> if there is a registered interest associated with the provided
	 * {@link RegionInstance}, <code>false</code> false otherwise.
	 */
	public boolean hasInterest( RegionInstance region )
	{
		for( Set<RegionInstance> regions : associations.values() )
		{
			if( regions.contains(region) )
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns <code>true</code> if there is an interest (for *ANY* region) in the identified
	 * attribute class.
	 */
	public boolean hasInterest( int attributeHandle )
	{
		return associations.containsKey( attributeHandle );
	}

	/**
	 * Returns the set of attribute handles that are currently associated with the provided
	 * {@link RegionInstance}. If there are none, null is returned.
	 */
	public Set<Integer> getAttributesForRegion( RegionInstance region )
	{
		HashSet<Integer> returnSet = new HashSet<Integer>();
		for( Integer attributeHandle : associations.keySet() )
		{
			Set<RegionInstance> regions = associations.get( attributeHandle );
			if( regions.contains(region) )
				returnSet.add( attributeHandle );
		}
		
		return returnSet;
	}

	/**
	 * Returns a set fo all the {@link RegionInstance}s associated with the subscription of the
	 * identified attribute handle.
	 */
	public Set<RegionInstance> getRegionsForAttribute( int attributeHandle )
	{
		return associations.get( attributeHandle );
	}

	/**
	 * Returns a set of all the attributes that we have an interest in. This method does not take
	 * into account any DDM/region information and will just return a set of all the attribute
	 * handles which are recorded as being subscribed to in any region.
	 */
	public Set<Integer> getAttributesNoDdm()
	{
		return associations.keySet();
	}
	
	/**
	 * Remove any and all associations in this group, regardless of which regions they are for.
	 * This will reset the group back to its initial post-construction state.
	 */
	public void empty()
	{
		this.associations.clear();
	}

	/**
	 * Returns <code>true</code> if the given group is empty (has no associations for any region,
	 * including the default region - null).
	 */
	public boolean isEmpty()
	{
		return this.associations.isEmpty();
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder( "[" );
		for( Integer handle : associations.keySet() )
		{
			builder.append( handle );
			builder.append(":" );
			builder.append( getRegionsForAttribute(handle) );
			builder.append(", ");
		}
		
		// put ] on the end and replace the left over ", "
		builder.replace( builder.length()-2, builder.length(), "]" );
		return builder.toString();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
