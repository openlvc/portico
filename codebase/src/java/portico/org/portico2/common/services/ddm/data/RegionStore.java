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
package org.portico2.common.services.ddm.data;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.portico.impl.hla13.types.HLA13Region;
import org.portico.impl.hla13.types.Java1Region;
import org.portico.lrc.compat.JInvalidRegionContext;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.RegionInstance;
import org.portico.lrc.model.Space;
import org.portico.lrc.services.saverestore.data.SaveRestoreTarget;

/**
 * This class maintains a list of all {@link RegionInstance} instances known locally.
 */
public class RegionStore implements SaveRestoreTarget
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<Integer,RegionInstance> regions;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public RegionStore()
	{
		this.regions = new HashMap<Integer,RegionInstance>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Adds the given {@link RegionInstance} to the store. If there is already a
	 * {@link RegionInstance} with the given handle within this store, that {@link RegionInstance}
	 * will be replaced with the one that is being added. If the given region is null, nothing
	 * will happen.
	 */
	public void addRegion( RegionInstance region )
	{
		if( region != null )
		{
			regions.put( region.getToken(), region );
		}
	}

	/**
	 * Fetch the contained region with the supplied region handle. If no {@link RegionInstance}
	 * can be found with that handle, <code>null</code> is returned.
	 */
	public RegionInstance getRegion( int regionHandle )
	{
		return regions.get( regionHandle );
	}

	/**
	 * This method will find the region of the given handle and if it was created by the federate
	 * this store serves, it will be returned. If the region doesn't exist, or it was created by
	 * another federate, <code>null</code> will be returned.
	 */
//	public RegionInstance getRegionWeCreated( int regionHandle )
//	{
//		RegionInstance region = regions.get( regionHandle );
//		if( region != null && region.getFederateHandle() == state.getFederateHandle() )
//			return region;
//		else
//			return null;
//	}
	
	/**
	 * Finds the {@link RegionInstance} associated with the provided regionHandle and returns it,
	 * but *ONLY* is the federate identified by the given federateHandle was the one that created
	 * it in the first place.
	 * <p/>
	 * If there is no known region for the handle, or the region was created by a federate
	 * different to that identified by the federate handle, null is returned.
	 * 
	 * @param regionHandle The handle of the region to find the {@link RegionInstance} for
	 * @param federateHandle The handle of the federate we expect to have created the region
	 * @return The region associated with the handle, only if it was also created by the identified
	 *         federete. Null if there is no region, or a different federate created it
	 */
	public RegionInstance getRegionCreatedBy( int regionHandle, int federateHandle )
	{
		RegionInstance region = regions.get( regionHandle );
		if( region == null )
			return null;
		
		// we have a region, check to see if the provided federate is the creator
		if( region.getFederateHandle() == federateHandle )
			return region;
		else
			return null;
	}

	/**
	 * Remove and return the contained {@link RegionInstance} with the given handle. If the
	 * {@link RegionInstance} exists, it will be removed and returned. If no {@link RegionInstance}
	 * with the given handle exists, null will be returned and the store will remain unaffected.
	 */
	public RegionInstance removeRegion( int regionHandle )
	{
		return regions.remove( regionHandle );
	}

	/**
	 * Remove and return the contained {@link RegionInstance}. If the {@link RegionInstance}
	 * exists in the store, it will be removed and returned. If the {@link RegionInstance} doesn't
	 * exist in the collection, null will be returned and the store will remain unaffected.
	 */
	public RegionInstance removeRegion( RegionInstance theRegion )
	{
		if( theRegion != null )
			return regions.remove( theRegion.getToken() );
		else
			return null;
	}

	/**
	 * Returns <code>true</code> if the store contains a {@link RegionInstance} with the given
	 * handle, <code>false</code> otherwise.
	 */
	public boolean containsRegion( int regionHandle )
	{
		return regions.containsKey( regionHandle );
	}

	/**
	 * Returns <code>true</code> if the store contains the given {@link RegionInstance},
	 * <code>false</code> otherwise.
	 */
	public boolean containsRegion( RegionInstance region )
	{
		if( region == null )
			return false;
		else
			return regions.containsKey( region.getToken() );
	}

	/**
	 * Returns <code>true</code> if the store contains the given {@link HLA13Region},
	 * <code>false</code> otherwise. It will actually get the wrapped {@link RegionInstance}
	 * instance inside the {@link HLA13Region} and check using that.
	 */
	public boolean containsRegion( HLA13Region region )
	{
		if( region == null )
			return false;

		return regions.containsKey( region.getWrappedRegion().getToken() );
	}

	/**
	 * Returns <code>true</code> if the store contains the given {@link Java1Region},
	 * <code>false</code> otherwise. It will actually get the wrapped {@link RegionInstance}
	 * instance inside the {@link Java1Region} and check using that.
	 */
	public boolean containsRegion( Java1Region region )
	{
		if( region == null )
			return false;

		return regions.containsKey( region.getWrappedRegion().getToken() );
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Save/Restore Methods /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	public void saveToStream( ObjectOutput output ) throws Exception
	{
		output.writeObject( regions );
	}

	@SuppressWarnings("unchecked")
	public void restoreFromStream( ObjectInput input ) throws Exception
	{
		this.regions = (Map<Integer,RegionInstance>)input.readObject();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * This is a convenience method that will validate the request to associate the given region
	 * with the identified attributes of the given object.
	 * 
	 * @param region      the region you wish to associate the attributes with
	 * @param theObject   the object instance this association applies to
	 * @param objectClass the type of the underlyling object (NOT the discovered type)
	 * @param attributes  the set of attributes to link with the given region
	 * @throws JInvalidRegionContext If the region is of a different routing space to that which the
	 *                               FOM declares as being allowed to be associated with a specific
	 *                               attribute type
	 */
	public static void associateForUpdates( RegionInstance region,
	                                        OCInstance theObject,
	                                        OCMetadata objectClass,
	                                        Set<Integer> attributes )
		throws JInvalidRegionContext
	{
		// get the handle of the routing space being associated with the attributes 
		int spaceHandle = region.getSpaceHandle();

		// link the region to each of the identified attributes, validating that the attribute
		// exists, and that the region is valid for the attribute
		for( Integer attributeHandle : attributes )
		{
			// make sure regions of this routing space are valid in the fom for this attribute			
			Space attributeSpace = objectClass.getAttribute( attributeHandle ).getSpace();
			if( attributeSpace == null || attributeSpace.getHandle() != spaceHandle )
			{
				throw new JInvalidRegionContext( "attribute [" + attributeHandle +
				    "] can't be associated with region [token:" + region.getToken() +
				    "]: Routing space not associated with attribute in FOM" );
			}

			// this is a valid association, link it up
			ACInstance theAttribute = theObject.getAttribute( attributeHandle );
			theAttribute.setRegion( region );
		}
	}

	/**
	 * Convenience method that will unassociate each of the attributes in the given object instance
	 * from the supplied region. If the attribute isn't associated with a region, or is associated
	 * with a different region, it will be left alone.
	 * 
	 * @param region    The region to remove all attribute/region associations for
	 * @param theObject The object to remove all attribute/region associations from
	 */
	public static void unassociateForUpdates( RegionInstance region, OCInstance theObject )
	{
		int regionToken = region.getToken();
		for( ACInstance attribute : theObject.getAllAttributes() )
		{
			if( attribute.getRegion() != null && attribute.getRegion().getToken() == regionToken )
				attribute.setRegion( null );
		}
	}
}
