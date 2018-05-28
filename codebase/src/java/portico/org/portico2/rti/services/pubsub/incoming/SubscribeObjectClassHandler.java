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
import java.util.stream.Collectors;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JObjectClassNotDefined;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.OCMetadata;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.DiscoverObject;
import org.portico2.common.services.pubsub.msg.SubscribeObjectClass;
import org.portico2.rti.federation.Federate;
import org.portico2.rti.services.RTIMessageHandler;
import org.portico2.rti.services.object.data.ROCInstance;

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
		
		OCMetadata classType = fom().getObjectClass( classHandle );
		if( classType == null )
			throw new JObjectClassNotDefined( "No known class for handle: "+classHandle );

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
		// After a federate newly subscribes to a class, there may be existing objects that
		// they can now also discover. Loop through and find any, generating DiscoverObject
		// callbacks as appropriate
		Set<ROCInstance> instances = repository.getAllInstancesAssignableFrom( classType );
		instances = instances.stream()
		                     .filter( instance -> instance.hasDiscovered(federateHandle) == false )
		                     .collect( Collectors.toSet() );
		
		Federate federate = federation.getFederate( federateHandle );
		
		for( ROCInstance instance : instances )
		{
			// have we already discovered this one?
			if( instance.hasDiscovered(federateHandle) )
				continue;
			
			// this one is new to us, register the discovery and queue a callback
			instance.discover( federateHandle, classType );
			DiscoverObject discover = new DiscoverObject( instance );
			discover.setClassHandle( classType.getHandle() );
			
			// The subscriber may not have had a chance to update its internal subscription list yet so
			// discoveries need to be queued to avoid false vetos due to race conditions.
			discover.setImmediateProcessingFlag( false );
			
			// Update discovery metrics
			momManager.objectDiscovered( federateHandle, instance );
			
			super.queueUnicast( discover, federateHandle );
			if( logger.isDebugEnabled() )
			{
				logger.debug( "Queued Discover callback. Federate [%s] discovered instance [%s] (type=%s, discoveredAs=%s)",
				              moniker(federateHandle),
				              objectMoniker(instance.getHandle()),
				              ocMoniker(instance.getRegisteredClassHandle()),
				              ocMoniker(classHandle) );
			}
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
