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
package org.portico2.rti.services.pubsub.incoming;

import java.util.Map;
import java.util.Set;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.pubsub.msg.SubscribeObjectClass;
import org.portico2.rti.services.RTIMessageHandler;

public class SubscribeObjectClassHandler extends RTIMessageHandler
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
		SubscribeObjectClass request = context.getRequest( SubscribeObjectClass.class, this );
		int federateHandle = request.getSourceFederate();
		int classHandle = request.getClassHandle();
		Set<Integer> attributes = request.getAttributes();
		int regionToken = request.getRegionToken();

		if( logger.isDebugEnabled() )
		{
			logger.debug( "ATTEMPT Federate [%s] subscribing to [%s] with attributes %s %s",
			              federateName(federateHandle),
			              ocMoniker(classHandle),
			              acMoniker(attributes),
			              request.usesDdm() ? "(region: "+regionToken+")" : "" );
		}

		if( attributes.isEmpty() )
			throw new JRTIinternalError( "Subscription attribute set is empty - this should have been fixed in the LRC" );

		// Store the interest information -- regionToken is NULL_HANDLE for non-ddm requests
		interests.subscribeObjectClass( federateHandle, classHandle, attributes, regionToken );

		context.success();
		if( logger.isInfoEnabled() )
		{
			logger.info( "SUCCESS Federate [%s] subscribed to  [%s] with attributes %s %s",
			             federateName(federateHandle),
			             ocMoniker(classHandle),
			             acMoniker(attributes),
			             request.usesDdm() ? "(region: "+regionToken+")" : "" );
		}

		//////////////////////////////////////////////////
		//  Discover Check   /////////////////////////////
		//////////////////////////////////////////////////
		// After we subscribe to a class, it may mean that we can now discover more
		// instances than we previously knew about. This check burns through all the
		// known objects to see if there are any for which discovery notifications
		// should now be sent.
		
		
// TODO
//		// see if there are any objects we can discover now that we subscribe to this class
//		Map<OCInstance,OCMetadata> discoverable = getDiscoverableData( federateHandle );
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
	
//	/**
//	 * Loop through all the undiscovered objects we have registered and see if any of them could
//	 * be discovered if we were subscribed to the given initial object class handle. This will also
//	 * take into account child classes of the given initial class when making the determination.
//	 * The method will return a map with each of the instances that can now be discovered, along
//	 * with the object class they can be discovered as.
//	 */
//	private Map<OCInstance,OCMetadata> getDiscoverableData( int federate )
//	{
//		Map<OCInstance,OCMetadata> data = new HashMap<OCInstance,OCMetadata>();
//		for( OCInstance instance : repository.getAllUndiscoveredInstances(federateHandle) )
//		{
//			OCMetadata discoverableType = interests.getDiscoveryType( federate,
//			                                                          instance.getRegisteredClassHandle() );
//			if( discoverableType != null )
//				data.put( instance, discoverableType );
//		}
//
//		return data;
//	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
