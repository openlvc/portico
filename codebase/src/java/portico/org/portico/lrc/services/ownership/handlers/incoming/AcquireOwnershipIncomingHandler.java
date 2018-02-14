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
package org.portico.lrc.services.ownership.handlers.incoming;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.services.ownership.msg.AttributeRelease;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.ownership.msg.AttributeAcquire;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7,
                messages=AttributeAcquire.class)
public class AcquireOwnershipIncomingHandler extends LRCMessageHandler
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
	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
	}
	
	public void process( MessageContext context ) throws Exception
	{
		AttributeAcquire acquire = context.getRequest( AttributeAcquire.class, this );
		vetoIfMessageFromUs( acquire ); // ignore our notifications, we've already recorded this
		int sourceFederate = acquire.getSourceFederate();
		int objectHandle = acquire.getObjectHandle();
		Set<Integer> attributes = acquire.getAttributes();
		
		if( logger.isDebugEnabled() )
		{
			String available = acquire.isIfAvailable() ? " (if available)" : "";
			logger.debug( "@REMOTE Attribute acquisition request"+available+" by ["+
			              moniker(sourceFederate)+"]: object="+objectMoniker(objectHandle)+
			              ", attributes="+acMoniker(attributes) );
		}
		
		// record the acquisition request, if it is an "if-available" request then our work is done
		if( acquire.isIfAvailable() )
		{
			ownership.requestAcquisitionIfAvailable( objectHandle, attributes, sourceFederate );
			veto();
		}
		
		ownership.requestAcquisition( objectHandle, attributes, sourceFederate );
		// check to see if we need to deliver any callbacks
		OCInstance objectInstance = repository.getInstance( objectHandle );
		if( objectInstance == null )
		{
			logger.warn( "WARNING Can't assess ownership acquisition request, unknown object: "+
			             objectHandle );
			veto();
		}
		
		// RELEASE THOSE WE ARE DIVESTING
		// we may be divesting some of these attributes, if we are take appropriate action
		Set<Integer> remaining = releaseAttributesWeAreDivesting( objectHandle, attributes );
		
		// if we own any of these attributes, deliver a callback to the local federate for them
		if( remaining.isEmpty() )
			noLocallyOwnedAttributes(); // nothing left to notify the local federate about

		
		// CALLBACK ABOUT THOSE WE OWN
		// find attributes owned by the local fedeate so we can deliver a FedAmb callback for them
		remaining = getAttributesOwnedByLocalFederate( objectInstance, remaining );
		if( remaining.isEmpty() )
			noLocallyOwnedAttributes(); // nothing to notify locally about
		else
			acquire.setAttributes( remaining );
		
		// let it through to the callback handler
		if( logger.isDebugEnabled() )
		{
			logger.debug( "Federate ["+moniker()+"] owns attributes "+acMoniker(remaining)+
			              " of object ["+objectMoniker(objectHandle)+"] which are under "+
			              "acquisition request. Notify FederateAmbassador of release request." );
		}
	}

	/**
	 * Goes through and finds all the attributes of the given set that are owned locally. If there
	 * are none, an empty set is returned.
	 */
	private Set<Integer> getAttributesOwnedByLocalFederate( OCInstance objectInstance,
	                                                        Set<Integer> attributes )
	{
		int objectHandle = objectInstance.getHandle();
		// filter the attributes down to those that we own
		Set<Integer> localNoticeSet = new HashSet<Integer>();
		for( Integer attributeHandle : attributes )
		{
			ACInstance attributeInstance = objectInstance.getAttribute( attributeHandle );
			if( attributeInstance == null )
			{
				logger.warn( "WARNING Can't assess ownership acquisition request for attribute "+
				             acMoniker(attributeHandle)+": not found in object ["+
				             objectMoniker(objectHandle)+"]" );
			}
			
			if( attributeInstance.getOwner() == federateHandle() )
				localNoticeSet.add( attributeHandle );
		}
		
		return localNoticeSet;
	}
	
	/**
	 * This method goes through the given attributes to see if the local federate is currently
	 * divesting any of them. If it is, a release response is automatically generated for them
	 * and sent to the federation.
	 * <p/>
	 * The set that is returned are those attributes which are NOT being divested. That is, all
	 * the attributes that are being divested are removed from the given set, and those remaining
	 * are returned.
	 */
	private Set<Integer> releaseAttributesWeAreDivesting( int objectHandle,
	                                                      Set<Integer> attributes )
	{
		Set<Integer> divesting = ownership.getAttributesOfferedForDivest( objectHandle,
		                                                                  attributes,
		                                                                  federateHandle() );
		// if there is nothing to divest, don't!
		if( divesting.isEmpty() )
			return attributes;
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "Automatically releasing divesting attributes "+acMoniker(divesting)+
			              " of object ["+objectMoniker(objectHandle)+"] on behalf of ["+moniker()+"]" );
		}

		// generate a release response for the attributes
		try
		{
			AttributeRelease release = new AttributeRelease( objectHandle, divesting );
			reprocessOutgoing( fill(release) );
		}
		catch( Exception e )
		{
			logger.error( "Exception trying to automatically release divesting attributes "+
			              acMoniker(divesting)+" of object ["+objectMoniker(objectHandle)+
			              "]: "+e.getMessage(), e );
		}
	
		// return those that are not being divested
		HashSet<Integer> set = new HashSet<Integer>( attributes );
		set.removeAll( divesting );
		return set;
	}

	private void noLocallyOwnedAttributes()
	{
		// log that we don't own any of the attributes and leave
		if( logger.isDebugEnabled() )
		{
			logger.debug( "Federate ["+moniker()+
			              "] doesn't own any of requested attributes, ignore request" );
		}

		veto();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
