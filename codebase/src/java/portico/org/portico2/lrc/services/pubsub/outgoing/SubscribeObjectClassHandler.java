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
package org.portico2.lrc.services.pubsub.outgoing;

import java.util.Map;
import java.util.Set;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JObjectClassNotSubscribed;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.pubsub.msg.SubscribeObjectClass;
import org.portico2.common.services.pubsub.msg.UnsubscribeObjectClass;
import org.portico2.lrc.LRCMessageHandler;

public class SubscribeObjectClassHandler extends LRCMessageHandler
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
		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();

		SubscribeObjectClass request = context.getRequest( SubscribeObjectClass.class, this );
		int classHandle = request.getClassHandle();
		Set<Integer> attributes = request.getAttributes();
		int regionToken = request.getRegionToken();

		if( logger.isDebugEnabled() )
		{
			logger.debug( "ATTEMPT Subscribe to [%s] with attributes %s %s",
			              ocMoniker(classHandle),
			              acMoniker(attributes),
			              request.usesDdm() ? "(region: "+regionToken+")" : "" );
		}
		
		// if this is a is a request with 0-attributes, it is an implicit unsubscribe //
		if( attributes.isEmpty() )
		{
			////////////////////////
			// IMPLICIT UNPUBLISH //
			////////////////////////
			// queue an unpublish request
			logger.debug("NOTICE  Subscribe with 0 attributes. Queue implicit unsubscribe request");
			UnsubscribeObjectClass unsubscribe = fill( new UnsubscribeObjectClass(classHandle) );
			context.setRequest( unsubscribe );
			
			try
			{
				// if the unsubscribe is implicit, this exception should not get thrown, we just
				// have to live with it, so let's just keep up with the jones' and pretend that
				// everything is perfect while it is slowly being ripped apart at the seams.
				// I really would prefer this threw an exception, as it is most likely the signal
				// of an error condition that will go unnoticed because we remain silent, ultimately
				// leaving the user scratching their heads and probably wanting to rip mine from
				// its home. Note that the only reason we do this is because DMSO does it. Grrrr!
				lrc.getOutgoingSink().process( context );
			}
			catch( JObjectClassNotSubscribed ocns )
			{
				// swallow the exception up then take a shower to get the ick off us
				// if this comes up, it means the un-sub handler won't have set the success
				// flag on the context because it thinks there was an error. set it now
				context.success();
			}
			
			return;
		}

		// Send it to the RTI for handling
		connection.sendControlRequest( context );
		
		// What happened!?
		if( context.isSuccessResponse() )
		{
			// Record the subscription
			interests.subscribeObjectClass( federateHandle(), classHandle, attributes );
			logger.info( "SUCCESS Subscribe to [%s] with attributes %s %s",
			              ocMoniker(classHandle),
			              acMoniker(attributes),
			              request.usesDdm() ? "(region: "+regionToken+")" : "" );
			
			// We are now listening for new things, so we can discover more about the previously
			// unknown universe, LIKE SCIENCE!
			// TODO Extract any new objects we can discover as a result of this subscription
			processNewDiscoveries( context );
		}
		else
		{
			throw context.getErrorResponseException();
		}
	}

	private void processNewDiscoveries( MessageContext context )
	{
//		Map<OCInstance,OCMetadata> discoverable = getDiscoverableData( federateHandle() );
//		for( OCInstance instance : discoverable.keySet() )
//		{
//			// generate a discover object callback
//			OCMetadata discoveredType = discoverable.get( instance );
//			instance.setDiscoveredType( discoveredType );
//			DiscoverObject discover = new DiscoverObject( instance );
//			discover.setClassHandle( discoveredType.getHandle() );
//			discover.setSourceFederate( instance.getOwner() );
//			lrcState.getQueue().offer( discover );
//			if( logger.isDebugEnabled() )
//			{
//				logger.debug( "Queued Discover callback for instance ["+
//				              objectMoniker(instance.getHandle())+
//				              "] after subscription to class ["+ocMoniker(classHandle)+"]" );
//			}
//		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
