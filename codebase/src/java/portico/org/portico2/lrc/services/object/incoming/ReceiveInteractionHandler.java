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
package org.portico2.lrc.services.object.incoming;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.RegionInstance;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.SendInteraction;
import org.portico2.common.services.pubsub.data.ICInterest;
import org.portico2.lrc.LRCMessageHandler;

public class ReceiveInteractionHandler extends LRCMessageHandler
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
		SendInteraction notice = context.getRequest( SendInteraction.class, this );

		// if we produced this message, ignore it, we don't need to generate callbacks
		// for our own attribute reflections
		vetoIfMessageFromUs( notice ); // throws VetoException

		int classHandle = notice.getInteractionId();
		Map<Integer,byte[]> parameters = notice.getParameters();
		int regionToken = notice.getRegionToken();
		int ourHandle = lrcState.getFederateHandle();

		if( logger.isDebugEnabled() )
		{
			String timeStatus = notice.isTimestamped() ? " @" + notice.getTimestamp() : " (RO)";
			String ddmStatus = notice.usesDDM() ? ", regionToken=" + regionToken : "";
			logger.debug( "@REMOTE Received INTERACTION [%s] with parameters %s%s%s",
			              icMoniker(classHandle),
			              pcMoniker(parameters.keySet()),
			              ddmStatus,
			              timeStatus );
		}

		// get the interaction class the federate is actually subscribed to (could be a parent
		// class rather than the one that was actually sent)
		ICInterest interest = interests.getSubscribedInteractionInterest( ourHandle, classHandle );
		if( interest == null )
		{
			// there is no subscription, ignore the incoming interaction and move on
			if( logger.isDebugEnabled() )
				logger.debug( "DISCARD interaction of class [%s]: no subscription", icMoniker(classHandle) );
			veto( "Interaction ignored: not subscribed" );
		}

		// see if we should filter this out based on DDM
		// note that if we are NOT subscribed with region data, the regionsOverlap method will
		// always return true. If there is an overlap, the regionsOverlap() method will put the
		// token of the subscribed region into the SendInteraction notice so it is available to
		// any callback handlers
		if( notice.usesDDM() && !regionsOverlap(interest.getRegions(ourHandle),notice) )
		{
			// regions don't overlap, filter it out
			logger.debug( "DISCARD interaction of class ["+icMoniker(classHandle)+"]: regions don't overlap" );
			veto( "Interaction ignored: regions don't overlap" );
		}

		// Filter the incoming parameters down to those we are interested in. Only need to do this
		// if the class we're subscribed to is different from that the interaction was sent with
		ICMetadata subscribed = interest.getInteractionClass();
		if( subscribed.getHandle() != classHandle )
		{
			// replace the existing parameters with the filtered set
			notice.setParameters( filter(subscribed,parameters) );
			// also set the interaction class handle on the message to be the appropriate type
			notice.setInteractionId( subscribed.getHandle() );

			if( logger.isDebugEnabled() )
			{
				logger.debug( "FILTER  incoming interaction type="+icMoniker(classHandle)+
				              ", subscribed type="+icMoniker(subscribed.getHandle()) );
			}
		}

		// don't set a success, we're letting it through so that a callback handler can
		// take care of it. If there is no callback handler, that is a problem and so
		// should probably generate an error
		// context.success();
	}

	/**
	 * Given the incoming raw parameters, remove any that are not valid in the provided interaction
	 * class (which is the one we are subscribed to, potentially higher up the inheritance hierarchy
	 * than the one the interaction was sent with).
	 */
	private HashMap<Integer,byte[]> filter( ICMetadata subscribed,
	                                        Map<Integer,byte[]> raw )
	{
		HashMap<Integer,byte[]> filtered = new HashMap<Integer,byte[]>();
		for( Integer receivedHandle : raw.keySet() )
		{
			if( subscribed.getParameter( receivedHandle ) != null )
				filtered.put( receivedHandle, raw.get( receivedHandle ) );
		}

		return filtered;
	}


	/**
	 * This method takes the given set of regions that the federate is subscribed to for an
	 * interaciton class and compares it to the region data sent with the interaction to see if
	 * there is any overlap. If there is, <code>true</code> is returned. If there isn't
	 * <code>false</code> is returned. If there *IS* an overlap, the region used for subscription
	 * that caused the overlap it stored inside the given {@link SendInteraction} notice.
	 * <p/>
	 * Note that if the federate is not subscribed using DDM (as signaled to the method by passing
	 * an empty region set) then this method will always return <code>true</code> as the "default
	 * region" overlaps with every region.
	 */
	private boolean regionsOverlap( Set<RegionInstance> subscribed,
	                                SendInteraction notice ) throws JRTIinternalError
	{
		int receivedToken = notice.getRegionToken();

		// there are no subscribed regions, thus, the subscribed federate
		// must not be using DDM. return true so that the send goes ahead
		if( subscribed.size() == 0 )
			return true;

		// find the sent region
		RegionInstance sentRegion = regionStore.getRegion( receivedToken );
		if( sentRegion == null )
		{
			throw new JRTIinternalError( "unknown region token sent with interaction: " +
			                             receivedToken );
		}

		// look at each region and consider if there is overlap
		for( RegionInstance subscribedRegion : subscribed )
		{
			if( subscribedRegion.overlapsWith(sentRegion) )
			{
				// store the information about the region that caused the overlap in the message
				notice.setReceivingRegionToken( subscribedRegion.getToken() );
				return true;
			}
		}

		// none of the provided regions overlap with the sent region, return false
		return false;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
