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
 * This class contains information about any outstanding ownership acquisition requests.
 * It is only intended for internal use by the {@link OwnershipManager}.
 */
public class AcquireRequest implements Serializable
{
	//----------------------------------------------------------
	//                      ENUMERATIONS
	//----------------------------------------------------------
	protected enum Status{ REQUEST_AVAILABLE, REQUEST, RELEASED };

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int objectHandle;
	private Map<Integer,AttributeRequest> requests;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected AcquireRequest( int objectHandle,
	                          Set<Integer> attributes,
	                          int federate,
	                          Status initialStatus )
	{
		this.objectHandle = objectHandle;
		this.requests = new HashMap<Integer,AttributeRequest>();
		for( Integer attribute : attributes )
		{
			AttributeRequest request = new AttributeRequest( federate, initialStatus );
			requests.put( attribute, request );
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
	 * Considering only those given attributes, find all that are currently under an outstanding
	 * ownership acquisition request by the given federate. If there are none, an empty set is
	 * returned. Note that this will only return attributes under acquisition request, *NOT* a
	 * "requestIfAvailable".
	 */
	public Set<Integer> getAttributesUnderAcquisitionRequest( Set<Integer> attributes,
	                                                          int federateHandle )
	{
		HashSet<Integer> underRequest = new HashSet<Integer>();
		for( Integer requestedAttribute : attributes )
		{
			AttributeRequest request = requests.get( requestedAttribute );
			if( request != null &&
				request.status == Status.REQUEST &&
				request.federateHandle == federateHandle )
			{
				underRequest.add( requestedAttribute );
			}
		}
		
		return underRequest;
	}
	
	/**
	 * This method is much like {@link #getAttributesUnderAcquisitionRequest(Set, int)} except that
	 * it will return the attributes that are under an acquisition request from ANY federate. The
	 * returned value is a map whose keys are the attribute handles and whose values are the
	 * federates that are requesting the ownership assumption. If there are none under outstanding
	 * request, an empty map is returned.
	 */
	public Map<Integer,Integer> getAttributesUnderAcquisitionRequest( Set<Integer> attributes )
	{
		HashMap<Integer,Integer> underRequest = new HashMap<Integer,Integer>();
		for( Integer attribute : attributes )
		{
			AttributeRequest request = requests.get( attribute );
			if( request != null && request.status == Status.REQUEST )
				underRequest.put( attribute, request.federateHandle );
		}
		
		return underRequest;
	}
	
	/**
	 * Returns a set of all the attributes currently under an acquisition request by the federate
	 * with the given handle. If there are none, an empty set is returned.
	 */
	public Set<Integer> getAttributesUnderAcquisitionRequest( int federateHandle )
	{
		HashSet<Integer> underRequest = new HashSet<Integer>();
		for( Integer attributeHandle : requests.keySet() )
		{
			AttributeRequest request = requests.get( attributeHandle );
			if( request.federateHandle == federateHandle )
				underRequest.add( attributeHandle );
		}
		
		return underRequest;
	}
	
	/**
	 * This method will update the local request, recording new information for the specified
	 * attributes.
	 * <p/>
	 * If the incoming status types are the same, then the associated federate handle for an
	 * attribute will only be changed if the incoming handle is <i>LESS</i> than the existing
	 * handle.
	 * <p/>
	 * If the incoming request is a REQUEST and the existing is REQUEST_AVAILABLE, the existing
	 * will be overwritten regardless of the federate handles (R trumps RA in our implementation!)
	 * <p/>
	 * If the incoming request is a REQUEST_AVAILABLE and the existing request is a normal REQUEST,
	 * no action will be taken as the RA is treated kind of like a "best effort" kind of request.
	 */
	public void updateRequest( Set<Integer> attributes, int federateHandle, Status incomingStatus )
	{
		for( Integer attribute : attributes )
		{
			AttributeRequest attributeRequest = requests.get( attribute );
			if( attributeRequest == null )
			{
				requests.put( attribute, new AttributeRequest(federateHandle,incomingStatus) );
			}
			else
			{
				// if the EXISTING status is RELEASED, only make a change if the incoming status
				// is a new request.
				// Fixes GH #166: A federate may release some attributes and then immediately
				//                request them back, which is fine, but we may not have yet
				//                received the acquisition notification from the other federate,
				//                so although these are just sitting here marked as released, it
				//                prevents us from marking them locally as requested by us. For
				//                this reason, if they are marked as released and the incoming
				//                status is a request, we mark it as that.
				if( attributeRequest.status == Status.RELEASED || incomingStatus == Status.RELEASED )
				{
					if( incomingStatus == Status.REQUEST )
					{
						attributeRequest.status = Status.REQUEST;
						attributeRequest.federateHandle = federateHandle;
					}

					// nothing more to do
					continue;
				}
				
				// if we get here, the only valid states for EXISTING status are
				// Status.REQUEST and Status.REQUEST_AVAILABLE

				// there is already a transfer request for the attribute, take action!
				if( attributeRequest.status == incomingStatus )
				{
					// they're the same status, either RA & RA or R & R, if the incoming
					// federate handle is lower, it trumps the existing request
					if( attributeRequest.federateHandle > federateHandle )
					{
						attributeRequest.federateHandle = federateHandle;
					}
				}
				else if( incomingStatus == Status.REQUEST )
				{
					// the two statuses are not the same, so if incoming is R, existing must be RA
					// in this case, overwrite existing, as RA is just a "best effort" style thing
					attributeRequest.status = Status.REQUEST;
					attributeRequest.federateHandle = federateHandle;
				}
				else
				{
					// existing must be R and incoming is RA, ignore it
				}
			}
		}
	}

	/**
	 * This method removes any local trace of the request for ownership transfer for the attributes
	 * with the given handles.
	 */
	public void cancelTransfer( Set<Integer> attributes )
	{
		for( Integer attributeHandle : attributes )
			requests.remove( attributeHandle );
	}

	/**
	 * This method signals that the federate with the given handle is ending the ownership
	 * transfer request. A set of all the attributes it can acquire at this point are returned
	 * and the associations for those attributes removed. If there are none available for the
	 * identified federate, an empty set is returned.
	 * <p/>
	 * If the <code>ifAvailable</code> flag is set to <code>true</code>, only those attributes
	 * that were requested as ifAvailable are removed and returned. If it is set to
	 * <code>false</code>, only those attributes that have explicitly been released to the federate
	 * will be removed and returned. Only those attributes marked as linked to the given federate
	 * AND having the same status are considered.
	 */
	public Set<Integer> completeTransfer( int federateHandle, boolean ifAvailable )
	{
		Status desiredStatus = ifAvailable ? Status.REQUEST_AVAILABLE : Status.RELEASED;
		
		Set<Integer> complete = new HashSet<Integer>();
		for( Integer attributeHandle : requests.keySet() )
		{
			AttributeRequest attributeRequest = requests.get( attributeHandle );
			if( attributeRequest.status == desiredStatus &&
				attributeRequest.federateHandle == federateHandle )
			{
				// remove it later, otherwise we'll cause a ConcurrentAccessException
				complete.add( attributeHandle );
			}
		}

		for( Integer confirmed : complete )
			requests.remove( confirmed );
		
		return complete;
	}

	/**
	 * This method marks the identified attributes as RELEASED. Attributes remain in this status
	 * until the {@link #completeTransfer(int, boolean)} method is called and the ownership transfer
	 * is completed. The returned map contains the attributes as the keys and the handles of the 
	 * federates as the values. If any of the attributes specified are not under a release request,
	 * they are ignored.
	 */
	public Map<Integer,Integer> releaseAttributes( Set<Integer> attributes )
	{
		Map<Integer,Integer> released = new HashMap<Integer,Integer>();
		for( Integer attributeHandle : attributes )
		{
			AttributeRequest attributeRequest = requests.get( attributeHandle );
			if( attributeRequest != null )
			{
				attributeRequest.status = Status.RELEASED;
				released.put( attributeHandle, attributeRequest.federateHandle );
			}
		}
		
		return released;
	}

	/**
	 * Returns <code>true</code> if the status for the designated attribute is REQUEST or 
	 * REQUEST_AVAILABLE.
	 */
	public boolean isAttributeUnderAcquisitionRequest( int attribute )
	{
		AttributeRequest request = requests.get( attribute );
		if( request == null )
			return false;

		switch( requests.get(attribute).status )
		{
			case REQUEST:
			case REQUEST_AVAILABLE:
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * This method returns a set of all the attributes that have been released to the specified
	 * federate (that is, their status is {@link Status#RELEASED} and their associated federate
	 * is the same as the given handle. This call is for information only and has no lasting
	 * effect on the ownership data. If there are no attributes released to the federate, an
	 * empty set is returned.
	 */
	public Set<Integer> getAttributesReleasedToFederate( int federateHandle )
	{
		Set<Integer> released = new HashSet<Integer>();
		for( Integer attributeHandle : requests.keySet() )
		{
			AttributeRequest request = requests.get( attributeHandle );
			if( request.status == Status.RELEASED && request.federateHandle == federateHandle )
				released.add( attributeHandle );
		}
		
		return released;
	}
	
	public boolean isEmpty()
	{
		return requests.isEmpty();
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( "objectHandle=" );
		builder.append( objectHandle );
		builder.append( " " );
		builder.append( requests );
		return builder.toString();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////// Private Inner Class: AttributeRequest ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	private class AttributeRequest implements Serializable
	{
		private static final long serialVersionUID = 98121116105109L;

		private int federateHandle;
		private Status status;
		private AttributeRequest( int federateHandle, Status status )
		{
			this.federateHandle = federateHandle;
			this.status = status;
		}
		
		public String toString()
		{
			return "[status="+status+",federate="+federateHandle+"]";
		}
	}
}
