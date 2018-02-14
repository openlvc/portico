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
package org.portico.lrc.services.object.handlers.incoming;

import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JObjectClassNotSubscribed;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.RegionInstance;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.ddm.data.RegionGroup;
import org.portico2.common.services.object.msg.UpdateAttributes;
import org.portico2.common.services.pubsub.data.OCInterest;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
                messages=UpdateAttributes.class)
public class ReflectAttributesHandler extends LRCMessageHandler
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
		UpdateAttributes notice = context.getRequest( UpdateAttributes.class, this );

		// if we produced this message, ignore it, we don't need to generate callbacks
		// for our own attribute reflections
		vetoIfMessageFromUs( notice ); // throws VetoException
		
		int objectHandle = notice.getObjectId();
		Map<Integer,byte[]> attributes = notice.getAttributes();

		if( logger.isDebugEnabled() )
		{
			String timeStatus = notice.isTimestamped() ? " @"+notice.getTimestamp() : " (RO)";
			logger.debug( "@REMOTE Received object UPDATE [" + objectMoniker(objectHandle) +
			              "] with attributes " + acMoniker(attributes.keySet()) + timeStatus );
		}
		
		// find the instance, we can't reflect values for an instance we haven't discovered
		OCInstance instance = repository.getInstance( objectHandle );
		if( instance == null )
		{
			logger.debug( "DISCARD reflection for object ["+objectMoniker(objectHandle)+
			              "]: object unknown" );
			veto("Reflection ignored: object unknown");
		}
		
		try
		{
			// filter the incoming attributes down to those we are interested in
			filter( instance, notice );
		}
		catch( JObjectClassNotSubscribed ocns )
		{
			// we're not subscribed, ignore the reflection
			veto( "Reflection ignored: no subscribed attributes" );
		}

		if( notice.getFilteredAttributes().isEmpty() )
		{
			logger.debug( "DISCARD reflection for object ["+objectMoniker(objectHandle)+
			              "]: no subscribed attributes" );
			veto("Reflection ignored: no subscribed attributes");
		}

		// don't set a success, we're letting it through so that a callback handler can
		// take care of it. If there is no callback handler, that is a problem and so
		// should probably generate an error
		// context.success();
	}
	
	/**
	 * Based on the subscription set for the local federate, filter the raw attributes down to
	 * those that are interesting locally. If there are none of interest, an empty set will be
	 * returned.
	 */
	private void filter( OCInstance instance, UpdateAttributes request ) throws Exception
	{
		request.clearFilteredAttributes();

		// get the interest this federate has in the object (use the discovered type as that's the
		// type we discovered the object as, so it's the one that related to our subscription
		int federateHandle = lrcState.getFederateHandle();
		OCInterest interest = interests.getSubscribedInterest( lrcState.getFederateHandle(),
		                                                       instance.getDiscoveredType() );
		
		// this is the group of subscribed attributes, each with the set of regions that
		// are associated with the subscription
		RegionGroup subscriptionGroup = interest.getInterestWithDDM( federateHandle );
		
		// if the federate has no interest, the group will be null, make sure that isn't the case
		if( subscriptionGroup == null )
			return;
		
		Map<Integer,byte[]> raw = request.getAttributes();
		for( Integer reflectedHandle : raw.keySet() )
		{
			// are we subscribed to this attribute?
			Set<RegionInstance> subscribedRegions =
				subscriptionGroup.getRegionsForAttribute( reflectedHandle );
			
			if( subscribedRegions == null )
				continue; // no subscription interest

			// are we subscribed to the default region? if we are we can skip the rest
			// of the processing as it is only ddm related
			if( subscribedRegions.contains(null) )
			{
				request.addFilteredAttribute( reflectedHandle, raw.get(reflectedHandle), null );
				continue;
			}
			
			// is the reflected attribute using DDM? if not, we can skip the rest of this
			ACInstance reflectedAttribute = instance.getAttribute( reflectedHandle );
			if( reflectedAttribute.getRegion() == null )
			{
				request.addFilteredAttribute( reflectedHandle, raw.get(reflectedHandle), null );
				continue;
			}

			// are we interested in this attribute with OVERLAPPING regions?
			for( RegionInstance subscribedRegion : subscribedRegions )
			{
				if( subscribedRegion.overlapsWith(reflectedAttribute.getRegion()) )
				{
					request.addFilteredAttribute( reflectedHandle,
					                              raw.get(reflectedHandle),
					                              subscribedRegion );
					break;
				}
			}
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
