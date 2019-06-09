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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.RegionInstance;

/**
 * This class represents a particular publication or subscription interest a particular federate
 * has in an interaction class. It contains the handle of the federate in question, as well as the
 * {@link ICMetadata} instance representing the interaction class. It also has an optional set of
 * {@link RegionInstance}s that represent the regions that the federate has associated with the 
 * interest (**only applies if the interest is a subscription).
 * <p/>
 * Instances of this class are used by the {@link InterestManager}.
 * <p/>
 * <b>NOTE that DDM information only applies to subscription interests. It is not used for
 * publication interests and can be safely ignored if it is representing a publication.
 */
public class ICInterest implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private ICMetadata interactionClass;
	private Map<Integer,Set<RegionInstance>> interests;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public ICInterest( ICMetadata interactionClass )
	{
		this.interactionClass = interactionClass;
		this.interests = new HashMap<Integer,Set<RegionInstance>>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Get the {@link ICMetadata} class representing the interaction that this interest
	 * is associated with.
	 */
	public ICMetadata getInteractionClass()
	{
		return this.interactionClass;
	}

	/**
	 * Register the federate as having an interest in the interaction class.
	 */
	public void registerInterest( int federateHandle )
	{
		// only register the interest if it doesn't already exist, as we are
		// lazy-creating the region sets, we don't want to just overwrite the
		// existing link, as it may replace the region data.
		if( interests.containsKey(federateHandle) == false )
			this.interests.put( federateHandle, null );
	}

	/**
	 * Register the federate as having an interest in the interaction class with the given
	 * region. If the federate already has an interest in the class, this will just augment
	 * that interest with the new region.
	 * <p/>
	 * <b>NOTE:</b> If the given {@link RegionInstance} is null, this is just the same as
	 * calling {@link #registerInterest(int)}. This is the signal to ignore DDM concerns.
	 */
	public void registerInterest( int federateHandle, RegionInstance region )
	{
		// make sure we have a region to work with
		if( region == null )
		{
			// we don't, this is a non-DDM call, just fob the request off to the other method
			registerInterest( federateHandle );
			return;
		}
		
		// try and get the existing map
		Set<RegionInstance> regions = interests.get( federateHandle );
		if( regions != null )
		{
			// the federate already has regions associated with this interest, extend them
			regions.add( region );
			return;
		}
		
		// the federate doesn't have an interest that have region associations yet,
		// create a set to hold that data and throw it into the interest map
		regions = new HashSet<RegionInstance>();
		regions.add( region );
		interests.put( federateHandle, regions ); // overwrite is safe, no region data exists to replace
	}
	
	/**
	 * Returns <code>true</code> if the given federate has an interest in the
	 * interaction class associated with this interest (<code>false</code> otherwise).
	 */
	public boolean hasInterest( int federateHandle )
	{
		return interests.containsKey( federateHandle );
	}
	
	/**
	 * Removes the interest of the federate in the interaction class <b>TOTALLY</b>. If there
	 * is any region data associated with the interest, all that will also be removed.
	 */
	public void removeInterest( int federateHandle )
	{
		interests.remove( federateHandle );
	}
	
	/**
	 * This method will remove the given region from those associated with the interest the
	 * federate has in the interaction class. If that region is the last one associated with
	 * the interest, then the interest will itself be removed as well. If there is no existing
	 * region data associated with the interests, the entire interest will be removed (it will
	 * act the same was as {@link #removeInterest(int)}.
	 * <p/>
	 * <b>NOTE:</b> If the given {@link RegionInstance} is null, this is just the same as
	 * calling {@link #registerInterest(int)}. This is the signal to ignore DDM concerns.
	 */
	public void removeInterest( int federateHandle, RegionInstance region )
	{
		// make sure we have a region to work with
		if( region == null )
		{
			// we don't, this is a non-DDM call, just fob the request off to the other method
			removeInterest( federateHandle );
			return;
		}

		Set<RegionInstance> regions = interests.get( federateHandle );
		if( regions == null )
		{
			// there is no existing set of regions associated with this interest,
			// remove the entire interest
			interests.remove( federateHandle );
			return;
		}
		
		// remove the region from the set of those associated with the interest for the federate
		regions.remove( region );
		
		// if that was the last one, remove the interest altogether
		if( regions.isEmpty() )
			interests.remove( federateHandle );
	}

	/**
	 * Get the full set of federates that have an interest in the associated interaction class.
	 * This set is an <i>unmodifiable</i> version of the underlying collection.
	 */
	public Set<Integer> getFederates()
	{
		return Collections.unmodifiableSet( interests.keySet() );
	}

	/**
	 * Get all the regions associated with the interests this federate has in the interaction
	 * class. If there are none, an empty set will be returned. If there are some, an
	 * <i>unmodifiable</i> version of the underlying collection will be returned.
	 */
	public Set<RegionInstance> getRegions( int federateHandle )
	{
		Set<RegionInstance> regions = interests.get( federateHandle );
		if( regions == null )
			return Collections.emptySet();
		else
			return Collections.unmodifiableSet( regions );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
