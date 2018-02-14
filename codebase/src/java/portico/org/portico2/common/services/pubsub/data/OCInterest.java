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
package org.portico2.common.services.pubsub.data;

import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.RegionInstance;
import org.portico2.common.services.ddm.data.RegionGroup;

import java.io.Serializable;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;

/**
 * This class represents an interest in an particular object class (be it publication or
 * subscription). Each instance contains a link to the {@link OCMetadata OCMetadata} instance it
 * represents an interest in.
 * <p/>
 * Maintained along with each interest is a set of federate handles that hold the interest data,
 * in addition to the set of attribute handles that they are interested in.
 * <p/>
 * Instances of this class are used by the {@link InterestManager}.
 */
public class OCInterest implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private OCMetadata objectClass;
	private Map<Integer,RegionGroup> interests; // key: federaetHandle
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public OCInterest( OCMetadata objectClass )
	{
		this.objectClass = objectClass;
		this.interests = new HashMap<Integer,RegionGroup>();
	}
	
	/**
	 * Creates a new OCInterest and registers the given federate and attributes. 
	 */
	public OCInterest( OCMetadata objectClass, int federateHandle, Set<Integer> attributeHandles )
	{
		this( objectClass );
		this.registerInterest( federateHandle, attributeHandles );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Registers the federate as having an interest in the given attribute handles. If the size of
	 * the set is 0, any previous interest will be removed (thus, un-pub or un-sub'ing). If there is
	 * already an interest set for the given federate handle, it will be replaced by the new set.
	 */
	public void registerInterest( int federateHandle, Set<Integer> attributes )
	{
		registerInterest( federateHandle, attributes, null );
	}

	/**
	 * Registers the federate as having an interest in the given attribute handles within the range
	 * of the provided {@link RegionInstance}. Unlike vanilla interest registrations, registrations
	 * with DDM data are <b>cumulative</b> (they don't necessarily replace the set of interests
	 * that existed before hand).
	 * <p/>
	 * If there already exists a set of attributes associated with the given region for the
	 * federate, it will be <b>replaced</b> by the provided set (effectively unsubscribing the
	 * provided federate from those attributes that were previously subscribe to, but which are not
	 * included in thhe provided set). If there is no existing set of attributes associated with
	 * the region, the association will be made. A single federate can be subscribed to the same
	 * attribute with more than one region associations at a time.
	 * <p/>
	 * <b>Implementation Note:</b> A <code>null</code> region is used to signal a registration that
	 * contains no DDM information. If <code>null</code> is passed for the {@link RegionInstance},
	 * this will result in any existing interest to be replaced with the new attributes (rather
	 * than augmented).
	 * <p/>
	 * <b>Implementation Note:</b> If the provided attributes are null, no action will take place.
	 * If the provided set is empty, this is taken to be an <i>implicit unsubscribe/unpublish</i>
	 * and any association between the federate/region/attributes will be removed.
	 * 
	 * @param federate The handle of the federate the interest registration relates to
	 * @param handleSet The attributes to associate with the region
	 * @param region The region to associate with the provided attribute handles
	 */
	public void registerInterest( int federate, Set<Integer> handleSet, RegionInstance region )
	{
		if( handleSet == null )
			return;
		
		// if there isn't a group for the federate already, create one and store it in the map
		RegionGroup theGroup = interests.get( federate );
		if( theGroup == null )
		{
			theGroup = new RegionGroup();
			interests.put( federate, theGroup );
		}

		// if the provided set it empty, this is an implicit remove
		if( handleSet.isEmpty() )
		{
			// wipe the group clean, then, if we have no region (thus a non-DDM call), remove
			// any local association we have as it is no longer required.
			theGroup.remove( region, handleSet );
			if( region == null )
				interests.remove( federate ); // remove it locally as well
			return;
		}
		
		// a follow up subscription for a region that has already been the subject of a prior
		// subscription should cause the new subscription to replace the existing on (effectively
		// unsubscribing the attributes from the original that aren't in the new set)
		theGroup.replace( region, new HashSet<Integer>(handleSet) );
	}
	
	/**
	 * Returns true if there is an interest for the given federate, false otherwise 
	 */
	public boolean hasInterest( int federateHandle )
	{
		return this.interests.containsKey( federateHandle );
	}

	/**
	 * Returns <code>true</code> if the specified federate has an interest in the identified
	 * attribute (for *ANY* region).
	 */
	public boolean hasAttributeInterest( int federateHandle, int attributeClass )
	{
		RegionGroup group = interests.get( federateHandle );
		if( group == null )
			return false;
		else
			return group.hasInterest( attributeClass );
	}

	/**
	 * Removes any previously registered interest for the given federate
	 */
	public void removeInterest( int federateHandle )
	{
		// using (RegionInstance)null looks dirty I know, but it is safe because it
		// directs the call to removeInstance(Federate,RegionInstance) and that method
		// will check for null (where it means something special). No NPE should occur.
		removeInterest( federateHandle, (RegionInstance)null );
	}

	/**
	 * Removes the interest in any attributes for the associated object class that have been
	 * linked to the given {@link RegionInstance}. If the federate has an interest associated
	 * with other regions for this object class, those will not be removed (unless the provided
	 * region is null). If the provided {@link RegionInstance} is <code>null</code>, then this
	 * request is treated as one that is not associated with DDM, and as such, will cause all
	 * interests (regardless of region) to be discarded for this object class (as it relates to
	 * the identified federate). 
	 */
	public void removeInterest( int federateHandle, RegionInstance region )
	{
		// fetch the existing group, exit if one doesn't exist
		RegionGroup group = interests.get( federateHandle );
		if( group == null )
			return;
		
		// remove the information from the group
		group.remove( region );
		
		// clear out the local map if this is a non-ddm request, or, if it is a ddm request
		// clear out the local map if the federate no longer cares about any attributes
		if( region == null || group.isEmpty() )
			interests.remove( federateHandle );
	}

	/**
	 * Removes any previously registered interested in the given attributes for the federate. If
	 * the given set is null or empty, the interest in all attributes for the class is removed.
	 */
	public void removeInterest( int federateHandle, Set<Integer> attributes )
	{
		// if there are no attributes, remove the interest for the entire class and return
		if( attributes == null || attributes.size() == 0 )
		{
			removeInterest( federateHandle );
			return;
		}
		
		// locate the interest information to remove
		RegionGroup group = interests.get( federateHandle );
		if( group == null )
			return;
		
		// pass null for the region as this method doesn't take DDM into account
		group.remove( null, attributes );

		// if current is now empty, just remove any interest
		if( group.hasInterest(null) == false )
			removeInterest( federateHandle );
	}

	/**
	 * Returns the set of attributes that make up the interest set for the given federate.
	 * If there is no registered interest, null is returned. Note that this set is
	 * <b>unmodifiable</b>.
	 */
	public Set<Integer> getInterest( int federateHandle )
	{
		// this method doesn't take into account region data, pass null for the associated region
		RegionGroup group = interests.get( federateHandle );
		if( group == null )
			return null;
		
		Set<Integer> theSet = group.getAttributesNoDdm();

		if( theSet.isEmpty() )
			return null;
		else
			return Collections.unmodifiableSet( theSet );
	}

	/**
	 * Return the {@link RegionGroup} associated with this interest for the provided federate. If
	 * the federate has no interest, return <code>null</code>.
	 */
	public RegionGroup getInterestWithDDM( int federateHandle )
	{
		return interests.get( federateHandle );
	}

	/**
	 * This method will return the set of attribute interests that are bound to the specified
	 * {@link RegionInstance} for the given federate. If there are no attributes associated with
	 * the {@link RegionInstance}, null will be returend.
	 */
	public Set<Integer> getInterest( int federateHandle, RegionInstance region )
	{
		RegionGroup group = interests.get( federateHandle );
		if( group == null )
			return null;
		else
			return group.getAttributesForRegion( region );
	}

	/**
	 * Return the set of federates contained in this interest 
	 */
	public Set<Integer> getFederates()
	{
		return Collections.unmodifiableSet( this.interests.keySet() );
	}
	
	public OCMetadata getObjectClass()
	{
		return this.objectClass;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
