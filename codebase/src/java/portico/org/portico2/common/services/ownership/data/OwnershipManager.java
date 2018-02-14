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

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.services.saverestore.data.SaveRestoreTarget;

/**
 * This class handles requests to transfer ownership of attributes between federates. It records
 * active requests for particular attributes as they are received by the LRC (either for the local
 * federate or remote ones).
 */
public class OwnershipManager implements SaveRestoreTarget
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<Integer,AcquireRequest> acquisitions;
	private Map<Integer,DivestRequest> divestitures;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public OwnershipManager()
	{
		this.acquisitions = new HashMap<Integer,AcquireRequest>();
		this.divestitures = new HashMap<Integer,DivestRequest>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////// Request & Request-if-Available Methods //////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will request an acquire-if-available request for the given attributes of the
	 * given object for the given federate. If there is already an outstanding request-if-available
	 * for those attributes by someone else, each attribute that is part of the request will be
	 * assessed to see if it can be replaced by this request. For that to happen, the given federate
	 * handle must be lower than the existing federate handle.
	 * <p/>
	 * If there is already an outstanding request (not if-available) for any of the attributes,
	 * those attributes will not be available. When it is ready, a call to the
	 * {@link #completeAcquisitionIfAvailable(int, int)} method completes any request and tells the
	 * method which attributes a federate has that are available (which can be less than those
	 * actually requested). 
	 */
	public void requestAcquisitionIfAvailable( int objectHandle,
	                                           Set<Integer> attributes,
	                                           int federateHandle )
	{
		// find the existing transfer request
		AcquireRequest request = acquisitions.get( objectHandle );
		if( request == null )
		{
			request = new AcquireRequest( objectHandle,
			                              attributes,
			                              federateHandle,
			                              AcquireRequest.Status.REQUEST_AVAILABLE );
			acquisitions.put( objectHandle, request );
		}
		else
		{
			request.updateRequest( attributes,
			                       federateHandle,
			                       AcquireRequest.Status.REQUEST_AVAILABLE );
		}
	}
	
	/**
	 * This method records an attribute acquisition request for the given attributes of the given
	 * object in the identified federate. If there is an outstanding request for any of the
	 * attributes, they will be individually assessed to see who has precedence. In that situation,
	 * the federate with the lower handle (be it the given federate or the existing one) will be
	 * the one assigned to the request.
	 */
	public void requestAcquisition( int objectHandle, Set<Integer> attributes, int federateHandle )
	{
		// find the existing transfer request
		AcquireRequest request = acquisitions.get( objectHandle );
		if( request == null )
		{
			request = new AcquireRequest( objectHandle,
			                              attributes,
			                              federateHandle,
			                              AcquireRequest.Status.REQUEST );
			acquisitions.put( objectHandle, request );
		}
		else
		{
			request.updateRequest( attributes, federateHandle, AcquireRequest.Status.REQUEST );
		}
	}
	
	/**
	 * This method removes any record of the request to transfer the given attributes of the
	 * given object. If this cancellation means that there are no more attributes associated
	 * with the transfer request, that to is removed.
	 */
	public void cancelAcquisition( int objectHandle, Set<Integer> attributes )
	{
		AcquireRequest request = acquisitions.get( objectHandle );
		if( request == null )
			return;
		
		request.cancelTransfer( attributes );
		if( request.isEmpty() )
			acquisitions.remove( objectHandle );
	}

	/**
	 * Returns <code>true</code> if the attribute of the identified object is currently under a 
	 * acquisition request.
	 */
	public boolean isAttributeUnderAcquisitionRequest( int objectHandle, int attribute )
	{
		AcquireRequest request = acquisitions.get( objectHandle );
		if( request == null )
			return false;
		
		return request.isAttributeUnderAcquisitionRequest( attribute );
	}

	/**
	 * Get the set of attributes (selected from the given set) that are currently under an
	 * outstanding ownership acquisition request by the identified federate. If there are
	 * none, an empty set is returned.
	 * <p/>
	 * <b>Note:</b> This only considered attributes under full requests, not "requests if available"
	 *              style requests.
	 */
	public Set<Integer> getAttributesUnderAcquisitionRequest( int objectHandle,
	                                                          Set<Integer> attributes,
	                                                          int federateHandle )
	{
		AcquireRequest request = acquisitions.get( objectHandle );
		if( request == null )
			return new HashSet<Integer>();
		
		return request.getAttributesUnderAcquisitionRequest( attributes, federateHandle );
	}
	
	/**
	 * This method is much the same as {@link #getAttributesUnderAcquisitionRequest(int, Set, int)}
	 * except that it will find any attributes under a release request, REGARDLESS of the federate
	 * that has requested the release.
	 * <p/>
	 * This method will return a map that contains the attribute handle as the key and the federate
	 * handle that has requested the release as the value. If none of the provided attributes are
	 * under a release request, an empty map is returned.
	 */
	public Map<Integer,Integer> getAttributesUnderAcquisitionRequest( int objectHandle,
	                                                                  Set<Integer> attributes )
	{
		AcquireRequest request = acquisitions.get( objectHandle );
		if( request == null )
			return new HashMap<Integer,Integer>();
		
		return request.getAttributesUnderAcquisitionRequest( attributes );
	}
	
	/**
	 * Gets a set of all the attributes for the given object that are currently under an
	 * acquisition request by the identified federate. If there is no outstanding acquisition
	 * request, an empty set it returned.
	 */
	public Set<Integer> getAttributesUnderAcquisitionRequest( int objectHandle, int federateHandle )
	{
		AcquireRequest request = acquisitions.get( objectHandle );
		if( request == null )
			return new HashSet<Integer>();
		
		return request.getAttributesUnderAcquisitionRequest( federateHandle );
	}
	
	/**
	 * This method will complete the requests for an acquisition-if-available for the identified
	 * object by the given federate. The return value will be the set of all attributes that can
	 * now be transferred into the ownership of the federate (which may be less than those that
	 * were requested). After this call, any record of the request will be removed. If none can
	 * be transferred, an empty set is returned.  
	 */
	public Set<Integer> completeAcquisitionIfAvailable( int objectHandle, int federateHandle )
	{
		// do we have a request for that object?
		AcquireRequest request = acquisitions.get( objectHandle );
		if( request == null )
			return new HashSet<Integer>();
		
		Set<Integer> obtained = request.completeTransfer( federateHandle, true );
		if( request.isEmpty() )
			acquisitions.remove( objectHandle );
		
		return obtained;
	}
	
	/**
	 * This method completes and removes an ownership acquisition request (NOT a
	 * request-if-available) and returns the set of attributes that can be obtained by the federate
	 * in question for the given object. Following this call, any record of the request is
	 * removed. If there are no transfers available for the federate, an empty set is returned.
	 */
	public Set<Integer> completeAcquisition( int objectHandle, int federateHandle )
	{
		AcquireRequest request = acquisitions.get( objectHandle );
		if( request == null )
			return new HashSet<Integer>();
		
		Set<Integer> obtained = request.completeTransfer( federateHandle, false );
		if( request.isEmpty() )
			acquisitions.remove( objectHandle );
		
		return obtained;
	}

	/**
	 * This method changes the status of each identified attribute to
	 * {@link AcquireRequest.Status#RELEASED}. This should be called when the owning federate
	 * has released the attributes, but the acquiring federates have not yet been notified.
	 * The returns map contains the attribute handles as keys and the acquiring federate handles
	 * as the values.
	 */
	public Map<Integer,Integer> releaseAttributes( int objectHandle, Set<Integer> attributes )
	{
		AcquireRequest request = acquisitions.get( objectHandle );
		if( request == null )
			return new HashMap<Integer,Integer>();
		else
			return request.releaseAttributes( attributes );
	}

	/**
	 * This method returns a set of all the attributes of the identified object that have been
	 * released to the specified federate. This call is for information only and has no lasting
	 * effect on the ownership data. If there are no attributes released to the federate, an
	 * empty set is returned.
	 */
	public Set<Integer> getAttributesReleasedToFederate( int objectHandle, int federateHandle )
	{
		AcquireRequest request = acquisitions.get( objectHandle );
		if( request == null )
			return new HashSet<Integer>();
		else
			return request.getAttributesReleasedToFederate( federateHandle );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Attribute Divest Methods ////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Records the attribute divestiture request for later reference.
	 */
	public void requestDivestiture( int objectHandle, Set<Integer> attributes, int federateHandle )
	{
		DivestRequest request = divestitures.get( objectHandle );
		if( request == null )
		{
			request = new DivestRequest( objectHandle, attributes, federateHandle );
			divestitures.put( objectHandle, request );
		}
		else
		{
			request.updateRequest( attributes, federateHandle );
		}
	}

	/**
	 * Find all the attributes from the given set that are under an outstanding attribute
	 * divestiture request by the identified federate. If there are none, an empty set will
	 * be returned.
	 */
	public Set<Integer> getAttributesOfferedForDivest( int objectHandle,
	                                                   Set<Integer> attributes,
	                                                   int federateHandle )
	{
		DivestRequest request = divestitures.get( objectHandle );
		if( request == null )
			return new HashSet<Integer>();
		else
			return request.getAttributesUnderDivestRequest( attributes, federateHandle );
	}

	/**
	 * This method will remove any outstanding divest request for the given attributes of the given
	 * object. The set of those attributes that were completed (and whose divest can be confirmed
	 * to the FederateAmbassador) is returned.
	 */
	public Set<Integer> completeDivest( int objectHandle, Set<Integer> attributes )
	{
		DivestRequest request = divestitures.get( objectHandle );
		if( request == null )
			return new HashSet<Integer>();
		
		Set<Integer> completed = request.completeDivest( attributes );
		if( request.isEmpty() )
			divestitures.remove( objectHandle );
		
		return completed;
	}

	/**
	 * Returns <code>true</code> if the specified attribute is currently under an divestiture
	 * request, <code>false</code> otherwise.
	 */
	public boolean isAttributeUnderDivestRequest( int objectHandle, int attributeHandle )
	{
		DivestRequest request = divestitures.get( objectHandle );
		if( request == null )
			return false;
		else
			return request.isAttributeUnderDivestRequest( attributeHandle );
	}

	/**
	 * Removes any outstanding divest request for the given attribute in the specified object.
	 */
	public void cancelDivest( int objectHandle, Set<Integer> attributes )
	{
		DivestRequest request = divestitures.get( objectHandle );
		if( request == null )
			return;
		
		request.cancelDivest( attributes );
		if( request.isEmpty() )
			divestitures.remove( objectHandle );
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Save/Restore Methods /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	public void saveToStream( ObjectOutput output ) throws Exception
	{
		output.writeObject( acquisitions );
		output.writeObject( divestitures );
	}

	@SuppressWarnings("unchecked")
	public void restoreFromStream( ObjectInput input ) throws Exception
	{
		this.acquisitions = (Map<Integer,AcquireRequest>)input.readObject();
		this.divestitures = (Map<Integer,DivestRequest>)input.readObject();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
