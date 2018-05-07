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

import org.portico.lrc.LRCMessageHandler;
//import org.portico.lrc.model.Mom;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.DiscoverObject;

/**
 * Handles the discovery of a new object instance. This handler will not generate a callback. That
 * is left to an implementation specific handler that should have a lower priority than this handler
 * (forcing it to run after us). This class will store information about the discovery locally.
 */
@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
                messages=DiscoverObject.class)
public class DiscoverObjectHandler extends LRCMessageHandler
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
//		DiscoverObject notice = context.getRequest( DiscoverObject.class, this );
//		// veto if it is our message, but only if it ISN'T a rediscovery check
//		if( notice.isRediscoveryCheck() == false )
//			vetoIfMessageFromUs( notice );
//		
//		int federate = notice.getSourceFederate();
//		int objectHandle = notice.getObjectHandle();
//		int classHandle = notice.getClassHandle();
//		int[] ownedAttributes = notice.getOwnedAttributes();
//		int[][] regionTokens = notice.getRegionTokens();
//		
//		if( logger.isDebugEnabled() )
//		{
//			if( classHandle == Mom.FederateClass )
//			{
//				logger.trace( "@REMOTE Received MOM object discovery for federate ["+
//				              notice.getObjectName()+"]" );
//			}
//			else
//			{
//				logger.debug( "@REMOTE Discover object: owner="+moniker(federate)+", object="+
//				              objectMoniker(objectHandle)+", class="+ocMoniker(classHandle)+
//				              ", owned="+acMoniker(ownedAttributes) );
//			}
//		}
//		
//		// check the subscription data to see if we are actually interested
//		// in this object class or not
//		OCMetadata registeredType = getObjectClass( classHandle );
//		OCMetadata discoveryType = interests.getDiscoveryType( lrcState.getFederateHandle(),
//		                                                       classHandle );
//		
//		// do we have a discovery interest?
//		if( discoveryType == null )
//		{
//			logger.debug( "DISCARD Discovery of object (not subscribed): object="+
//			              objectMoniker(objectHandle) );
//			// still store the object, we may need the data later if we subscribe,
//			// create the object with its registered type and store it in a special location
//			OCInstance newInstance = fetchOrCreateInstance( federate,
//			                                                registeredType,
//			                                                registeredType,
//			                                                objectHandle,
//			                                                notice.getObjectName(),
//			                                                ownedAttributes,
//			                                                regionTokens );
//
//			// if this is a rediscovery check, this object will already be in the undiscovered
//			// store, so this call will have no effect
//			repository.addUndiscoveredInstance( newInstance );
//			veto();
//		}
//
//		// create the OCInstance using the discovered type and store locally, just get the
//		// object if it already exists (this could be a rediscovery check
//		OCInstance newInstance = fetchOrCreateInstance( federate,
//		                                                registeredType,
//		                                                discoveryType,
//		                                                objectHandle,
//		                                                notice.getObjectName(),
//		                                                ownedAttributes,
//		                                                regionTokens );
//
//		repository.discoverInstance( newInstance, discoveryType );
//		// replace the class that the object is of in the notice with the class we discovered it as
//		notice.setClassHandle( discoveryType.getHandle() );
//		context.success();
//		
//		if( logger.isInfoEnabled() )
//		{
//			logger.info( "DISCOVER object ["+objectMoniker(objectHandle)+"] registeredAs=" +
//			             ocMoniker(classHandle)+", discoveredAs="+ocMoniker(discoveryType) );
//		}
	}
	
	private OCInstance fetchOrCreateInstance( int        federateHandle,
	                                          OCMetadata registeredType,
	                                          OCMetadata discoveryType,
	                                          int        objectHandle,
	                                          String     objectName,
	                                          int[]      ownedAttributes,
	                                          int[][]    regionTokens )
	{
		// check to see if we already know about this object in undiscovered form
		OCInstance theInstance = repository.getUndiscoveredInstance( objectHandle );
		if( theInstance != null )
			return theInstance;
		
		// we don't already know about it, create and return it
//		return repository.newInstance( federateHandle,
//		                               registeredType,
//		                               discoveryType,
//		                               objectHandle,
//		                               objectName,
//		                               ownedAttributes,
//		                               regionTokens );
		return null;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
