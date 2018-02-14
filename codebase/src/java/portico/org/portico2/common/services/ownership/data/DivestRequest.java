/*
 *   Copyright 2009 The Portico Project
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
package org.portico2.common.services.ownership.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class contains information about any outstanding ownership divestiture requests.
 * It is only intended for internal use by the {@link OwnershipManager}.
 */
public class DivestRequest implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int objectHandle;
	private Map<Integer,Integer> requests; // AttributeHandle/FederateHandle

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected DivestRequest( int objectHandle, Set<Integer> attributes, int federateHandle )
	{
		this.objectHandle = objectHandle;
		this.requests = new HashMap<Integer,Integer>();
		for( Integer attribute : attributes )
		{
			requests.put( attribute, federateHandle );
		}
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public int getObjectHandle()
	{
		return this.objectHandle;
	}

	/**
	 * Stores the given federate as having requested a divestiture of the given attributes. If
	 * a recording already exists, it will be replaced.
	 */
	public void updateRequest( Set<Integer> attributes, int federateHandle )
	{
		for( Integer attribute : attributes )
		{
			// this will overwrite the value if it exists, or put the value in there if it doesn't
			requests.put( attribute, federateHandle );
		}
	}
	
	/**
	 * Gets a set of all the attributes (only considering those given to the method) that are
	 * currently under a divestiture request by the given federate. If there are none, this will
	 * return an empty set.
	 */
	public Set<Integer> getAttributesUnderDivestRequest( Set<Integer> attributes,
	                                                     int federateHandle )
	{
		Set<Integer> underRequest = new HashSet<Integer>();
		for( Integer attribute : attributes )
		{
			if( requests.containsKey(attribute) && requests.get(attribute) == federateHandle )
				underRequest.add( attribute );
		}
		
		return underRequest;
	}

	/**
	 * Removes any divest notification for the given set of attributes from this request. The set
	 * of those that were removed is returned (note: this may be less than what was requested if
	 * all attributes are not under a divestiture request).
	 */
	public Set<Integer> completeDivest( Set<Integer> attributes )
	{
		Set<Integer> completed = new HashSet<Integer>();
		for( Integer attribute : attributes )
		{
			if( requests.containsKey(attribute) )
			{
				requests.remove( attribute );
				completed.add( attribute );
			}
		}

		return completed;
	}

	/**
	 * Cancels the divest request for any of the attributes, removing them from this record.
	 */
	public void cancelDivest( Set<Integer> attributes )
	{
		for( Integer attribute : attributes )
			requests.remove( attribute );
	}
	
	/**
	 * Returns <code>true</code> if the specified attribute is currently under an divestiture
	 * request, <code>false</code> otherwise.
	 */
	public boolean isAttributeUnderDivestRequest( int attributeHandle )
	{
		return requests.containsKey( attributeHandle );
	}
	
	public boolean isEmpty()
	{
		return requests.isEmpty();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
