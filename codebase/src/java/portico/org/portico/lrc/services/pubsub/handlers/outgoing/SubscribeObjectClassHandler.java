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
package org.portico.lrc.services.pubsub.handlers.outgoing;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JObjectClassNotSubscribed;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.DiscoverObject;
import org.portico2.common.services.pubsub.msg.SubscribeObjectClass;
import org.portico2.common.services.pubsub.msg.UnsubscribeObjectClass;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=SubscribeObjectClass.class)
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
	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
	}
	
	public void process( MessageContext context ) throws Exception
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
			String message = "ATTEMPT Subscribe to ["+ocMoniker(classHandle)+"] with attributes "+
			                 acMoniker(attributes);
			if( request.usesDdm() )
				message += " (region: "+regionToken+")";
			logger.debug( message );
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
		
		// store the interest information -- regionToken is NULL_HANDLE for non-ddm requests
		interests.subscribeObjectClass( request.getSourceFederate(),
		                                classHandle,
		                                attributes,
		                                regionToken ); 
		
		// see if there are any objects we can discover now that we subscribe to this class
		Map<OCInstance,OCMetadata> discoverable = getDiscoverableData( federateHandle() );
		for( OCInstance instance : discoverable.keySet() )
		{
			// generate a discover object callback
			OCMetadata discoveredType = discoverable.get( instance );
			instance.setDiscoveredType( discoveredType );
			DiscoverObject discover = new DiscoverObject( instance );
			discover.setClassHandle( discoveredType.getHandle() );
			discover.setSourceFederate( instance.getOwner() );
			lrcState.getQueue().offer( discover );
			if( logger.isDebugEnabled() )
			{
				logger.debug( "Queued Discover callback for instance ["+
				              objectMoniker(instance.getHandle())+
				              "] after subscription to class ["+ocMoniker(classHandle)+"]" );
			}
		}
		
		// forward the information to the rest of the federation in case they want it
		connection.broadcast( request );
		context.success();

		if( logger.isInfoEnabled() )
		{
			String message = "SUCCESS Subscribeed to ["+ocMoniker(classHandle)+"] with attributes "+
			                 acMoniker(attributes);
			if( request.usesDdm() )
				message += " (region: "+regionToken+")";
			logger.info( message );
		}
	}

	/**
	 * Loop through all the undiscovered objects we have registered and see if any of them could
	 * be discovered if we were subscribed to the given initial object class handle. This will also
	 * take into account child classes of the given initial class when making the determination.
	 * The method will return a map with each of the instances that can now be discovered, along
	 * with the object class they can be discovered as.
	 */
	private Map<OCInstance,OCMetadata> getDiscoverableData( int federate )
	{
		Map<OCInstance,OCMetadata> data = new HashMap<OCInstance,OCMetadata>();
		for( OCInstance instance : repository.getAllUndiscoveredInstances() )
		{
			OCMetadata discoverableType =
				interests.getDiscoveryType( federate, instance.getRegisteredClassHandle() );
			if( discoverableType != null )
				data.put( instance, discoverableType );
		}
		
		return data;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
