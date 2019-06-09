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

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.model.OCMetadata;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.DiscoverObject;
import org.portico2.lrc.LRCMessageHandler;
import org.portico2.lrc.services.object.data.LOCInstance;

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
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		super.configure( properties );
	}

	@Override
	public void process( MessageContext context ) throws JException
	{
		DiscoverObject notice = context.getRequest( DiscoverObject.class, this );
		// veto if it is our message, but only if it ISN'T a rediscovery check
		if( notice.isRediscoveryCheck() == false )
			vetoIfMessageFromUs( notice );
		
		int federateHandle = notice.getSourceFederate();
		int objectHandle = notice.getObjectHandle();
		int classHandle = notice.getClassHandle();
		
		if( logger.isDebugEnabled() )
		{
//			if( classHandle == Mom.FederateClass )
//			{
//				logger.trace( "@REMOTE Received MOM object discovery for federate ["+
//				              notice.getObjectName()+"]" );
//			}
//			else
//			{
				logger.debug( "@REMOTE Discover object: handle=%d, class=%s, name=%s, owner=%d",
				              objectHandle,
				              ocMoniker(classHandle),
				              notice.getObjectName(),
				              federateHandle );
//			}
		}

		// do we already know about it?
		if( repository.containsObject(objectHandle) )
			veto( "object already known" );
		
		// check the subscription data to see if we are actually interested
		// in this object class or not
		OCMetadata registeredType = getObjectClass( classHandle );
		OCMetadata discoveredType = interests.getDiscoveryType( lrcState.getFederateHandle(),
		                                                        classHandle );
		
		// do we have a discovery interest?
		if( discoveredType == null )
		{
			logger.debug( "DISCARD Discovery of object (not subscribed): object="+
			              objectMoniker(objectHandle) );
			veto( "not subscribed" );
		}

		// we don't already know about it, create and return it
		LOCInstance newInstance = repository.createObject( registeredType,
		                                                   discoveredType,
		                                                   objectHandle,
		                                                   notice.getObjectName() );

		repository.addObject( newInstance );

		// replace the class that the object is of in the notice with the class we discovered it as
		notice.setClassHandle( discoveredType.getHandle() );
		context.success();
		
		if( logger.isInfoEnabled() )
		{
			logger.info( "DISCOVER object: object=%s, registeredAs=%s, discoveredAs=%s",
			             objectMoniker(objectHandle),
			             ocMoniker(classHandle),
			             ocMoniker(discoveredType) );
		}

	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
