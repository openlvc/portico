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
package org.portico2.rti.services.ownership.incoming;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JAttributeNotPublished;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JFederateOwnsAttributes;
import org.portico.lrc.compat.JObjectClassNotDefined;
import org.portico.lrc.compat.JObjectClassNotPublished;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico.lrc.services.ownership.msg.AttributesUnavailable;
import org.portico.lrc.services.ownership.msg.OwnershipAcquired;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.ownership.msg.AttributeAcquire;
import org.portico2.rti.services.RTIMessageHandler;
import org.portico2.rti.services.object.data.RACInstance;
import org.portico2.rti.services.object.data.ROCInstance;

public class AcquireOwnershipHandler extends RTIMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		super.configure( properties );
	}

	@Override
	public void process( MessageContext context ) throws JException
	{
		// Get the request information
		AttributeAcquire request = context.getRequest( AttributeAcquire.class, this );
		int federate = request.getSourceFederate();
		int objectHandle = request.getObjectHandle();
		Set<Integer> attributes = request.getAttributes();
		
		// Log the request
		// TODO
		
		// Do some basic checks on the request
		Set<RACInstance> instances = validateRequest( federate, objectHandle, attributes );
		
		// Split the attributes into owned and unowned
		Set<RACInstance> owned   = new HashSet<>();
		Set<RACInstance> unowned = new HashSet<>();
		for( RACInstance temp : instances )
		{
			if( temp.isUnowned() || temp.isOwnedByRti() )
				unowned.add( temp );
			else
				owned.add( temp );
		}

		// If this is an "If-Available" request, assign what we can and reject the rest
		// If this is a standard request, assign what we can and request the rest from owners
		if( request.isIfAvailable() )
		{
			// Assign ownership for unowned
			acquireUnowned( federate, objectHandle, unowned );
			
			// Send a failure notification for those that are owned
			sendFailure( federate, objectHandle, owned );
		}
		else
		{
			// Assign ownership for unowned
			acquireUnowned( federate, objectHandle, unowned );
			
			// Request ownership from the owners for owned ones
			requestDivest( federate, objectHandle, owned );			
		}

		// FIXME - Start here
		// FIXME - Do I need to record acquisition intents? YES
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	///  Supporting Methods  /////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Assign over those that are unowned. If there are any that are owned, send a failure.
	 * 
	 * @param federateHandle The federate who made the request
	 * @param objectHandle   The handle of the object the request is for
	 * @param unowned        The set of attributes that are meant to be unowned.
	 */
	private synchronized void acquireUnowned( int federateHandle, int objectHandle, Set<RACInstance> unowned )
	{
		Set<Integer> acquired = new HashSet<>();
		Set<Integer> failed   = new HashSet<>();

		// do the ownership acquisition
		for( RACInstance attribute : unowned )
		{
			if( attribute.isUnowned() || attribute.isOwnedByRti() )
			{
				attribute.setOwner( federateHandle );
				acquired.add( attribute.getHandle() );
			}
			else
			{
				failed.add( attribute.getHandle() );
			}
		}

		// send the notifications
		if( acquired.isEmpty() == false )
		{
			OwnershipAcquired notice = new OwnershipAcquired( objectHandle, acquired, false );
			queueUnicast( notice, federateHandle );
			
			logger.debug( "ACQUIRE SUCCESS by federate [%s] (object=%s, attributes=%s)",
			              moniker(federateHandle),
			              objectMoniker(objectHandle),
			              acMoniker(acquired) );
		}

		if( failed.isEmpty() == false )
		{
			AttributesUnavailable notice = new AttributesUnavailable( objectHandle, failed );
			queueUnicast( notice, federateHandle );

			logger.debug( "ACQUIRE FAILURE by federate [%s]: Attributes Owned (object=%s, attributes=%s)",
			              moniker(federateHandle),
			              objectMoniker(objectHandle),
			              acMoniker(notice.getAttributeHandles()) );
		}
	}

	/**
	 * Send a request to acquire the specified attributes to each of the owners. We assume that
	 * all the given attributes are currently owned by someone. The set are split into subsets
	 * for each relevant owner, with the acquire request forwarded to the owning federate.
	 * 
	 * @param federateHandle The handle of the federate making the request
	 * @param objectHandle   The handle of the object that the request is for
	 * @param attributes     The set of attributes being requested
	 */
	private synchronized void requestDivest( int federateHandle, int objectHandle, Set<RACInstance> attributes )
	{
		// Split the attributes out into their respective owners
		Map<Integer,Set<Integer>> splitByOwner = new HashMap<>();
		for( RACInstance attribute : attributes )
		{
			int owner = attribute.getOwner();
			if( splitByOwner.containsKey(owner) )
			{
				splitByOwner.get(owner).add( attribute.getHandle() );
			}
			else
			{
				HashSet<Integer> set = new HashSet<>();
				set.add( attribute.getHandle() );
				splitByOwner.put( owner, set );
			}
		}
		
		// Set out the divesture requests
		for( Integer owner : splitByOwner.keySet() )
		{
			AttributeAcquire request = new AttributeAcquire( objectHandle,
			                                                 splitByOwner.get(owner),
			                                                 false );
			queueUnicast( request, owner );
			
			if( logger.isDebugEnabled() )
			{
				logger.debug( "DIVEST REQUEST sent to [%s] (object=%s, attributes=%s)",
				              moniker(owner),
				              objectMoniker(objectHandle),
				              acMoniker(request.getAttributes()) );
			}
		}
	}

	/**
	 * Queues and unicasts out an {@link AttributesUnavailable} notificatio to the requesting
	 * federate for the set of attributes and related object that is provided.
	 * 
	 * @param federateHandle Handle of the federate the requested
	 * @param objectHandle   Handle of the object the action was requested for
	 * @param attributes     Set of attributes that are not available
	 */
	private void sendFailure( int federateHandle, int objectHandle, Set<RACInstance> attributes )
	{
		AttributesUnavailable notice = new AttributesUnavailable( objectHandle,
		                                                          instancesToHandles(attributes) );
		queueUnicast( notice, federateHandle );
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "ACQUIRE FAILURE by federate [%s]: Attributes Owned (object=%s, attributes=%s)",
			              moniker(federateHandle),
			              objectMoniker(objectHandle),
			              acMoniker(notice.getAttributeHandles()) );
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// Helper Methods /////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	private final Set<Integer> instancesToHandles( Set<RACInstance> instances )
	{
		HashSet<Integer> set = new HashSet<Integer>();
		for( RACInstance instance : instances )
			set.add( instance.getHandle() );
		
		return set;
	}
	

	/**
	 * Validates the information given in the request. This method will check to see that all
	 * the attributes exist, that they are all published by the requesting federate and that
	 * it does not currently own any of them. 
	 * 
	 * @param requestingFederate Handle of the federate making the request
	 * @param objectHandle       Handle of the object the request is for
	 * @param attributes         Set of handles the request is for
	 * @return                   Set of {@link ACInstance}s that the handles represent
	 */
	private Set<RACInstance> validateRequest( int requestingFederate,
	                                          int objectHandle,
	                                          Set<Integer> attributes )
		throws JAttributeNotDefined,
		       JFederateOwnsAttributes,
		       JAttributeNotPublished,
		       JObjectClassNotPublished,
		       JObjectClassNotDefined,
		       JObjectNotKnown
	{
		// Make sure the object exists
		ROCInstance instance = repository.getObject( objectHandle );
		if( instance == null )
		{
			throw new JObjectNotKnown( "Can't aquire attributes of object "+
			                           objectMoniker(objectHandle)+": unknown" );
		}

		// Make sure that every attribute requested exists, is published by the requester and
		// isn't already owned by them
		Set<RACInstance> instances = new HashSet<>();
		for( Integer requestedAttribute : attributes )
		{
			RACInstance attributeInstance = instance.getAttribute( requestedAttribute );
			// does the attribute exist?
			if( attributeInstance == null )
			{
				throw new JAttributeNotDefined( "Can't aquire attribute "+acMoniker(requestedAttribute)+
				                                ": not found" );
			}
			
			// do we already own it?
			if( attributeInstance.getOwner() == requestingFederate )
			{
				throw new JFederateOwnsAttributes( "Can't aquire attribute "+acMoniker(requestedAttribute)+
				                                   ": federate already owns it" );
			}
			
			
			// do we even publish it?
			int classHandle = attributeInstance.getContainer().getRegisteredType().getHandle();
			if( !interests.isAttributeClassPublished(requestingFederate,classHandle,requestedAttribute) )
			{
				throw new JAttributeNotPublished( "Can't aquire attribute "+acMoniker(requestedAttribute)+
				                                  ": Not published" );
			}
			
			instances.add( attributeInstance );
		}
		
		// return the set of ACInstances represented by the handles
		return instances;
	}
	
	/**
	 * Go through each of the attributes requested below and filter out any that are already
	 * owned by someone else. This is used for "Is-Available" requests which only seek to acquire
	 * ownership of an attribute if it is not currently owned by anyone.
	 * <p/>
	 * This method will modify the given set rather than return a new set.
	 * 
	 * @param requestedAttributes The attributes that have been requested for acquisition.
	 */
	private void filterOutUnavailable( Set<RACInstance> requestedAttributes )
	{
		Iterator<RACInstance> iterator = requestedAttributes.iterator();
		while( iterator.hasNext() )
		{
			if( iterator.next().isUnowned() == false )
				iterator.remove();
		}
	}


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
