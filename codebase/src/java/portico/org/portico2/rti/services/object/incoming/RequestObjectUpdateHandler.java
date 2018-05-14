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
package org.portico2.rti.services.object.incoming;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.RequestObjectUpdate;
import org.portico2.rti.services.RTIMessageHandler;
import org.portico2.rti.services.object.data.RACInstance;
import org.portico2.rti.services.object.data.ROCInstance;

public class RequestObjectUpdateHandler extends RTIMessageHandler
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
		RequestObjectUpdate notice = context.getRequest( RequestObjectUpdate.class, this );
		int objectHandle = notice.getObjectId();
		Set<Integer> attributeHandles = notice.getAttributes();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Request object update: object="+objectMoniker(objectHandle)+
			              ", attributes="+acMoniker(attributeHandles)+", sourceFederate="+
			              moniker(notice.getSourceFederate()) );
		}

		// find the local copy of the object
		ROCInstance instance = repository.getObject( objectHandle );
		if( instance == null )
		{
			// we don't know the instance, don't do anything
			logger.debug( "Object not known ["+objectHandle+"], we can't provide any update" );
			throw new JObjectNotKnown( "Object not known ["+objectHandle+"], we can't provide any update" );
		}

		// find out which federates own which attributes
		Map<Integer,HashSet<Integer>> owners = findOwners( instance, attributeHandles );

		// send a Provide request to each of the owners
		for( Integer owner : owners.keySet() )
		{
			HashSet<Integer> ownedAttributes = owners.get( owner );
			if( owner == PorticoConstants.RTI_HANDLE )
				momManager.updateMomObject( objectHandle, ownedAttributes );
			else
				sendProvideRequest( objectHandle, owner, ownedAttributes );
		}

		// everything is awesome!
		context.success();
	}
	
	private Map<Integer,HashSet<Integer>> findOwners( ROCInstance objectInstance, Set<Integer> attributeHandles )
	{
		Map<Integer,HashSet<Integer>> owners = new HashMap<>();
		
		for( int attributeHandle : attributeHandles )
		{
			RACInstance attributeInstance = objectInstance.getAttribute( attributeHandle );
			if( attributeInstance == null )
			{
				throw new JRTIinternalError( "Update requested for undefined attribute [class=%s, attribute=%s]",
				                             ocMoniker(objectInstance.getRegisteredType()),
				                             acMoniker(attributeHandle) );
			}
			
			int owner = attributeInstance.getOwner();
			if( owners.containsKey(owner) == false )
				owners.put( owner, new HashSet<Integer>() );
			
			owners.get(owner).add( attributeHandle );
		}
		
		return owners;
	}
	
	private void sendProvideRequest( int objectHandle, int owner, HashSet<Integer> attributes )
	{
		if( logger.isDebugEnabled() )
		{
			logger.debug( "Requesting update of attributes %s in object [%s] from federate [%s]",
			              acMoniker(attributes),
			              objectMoniker(objectHandle),
			              moniker(owner) );
		}
		
		RequestObjectUpdate request = new RequestObjectUpdate( objectHandle, attributes );
		super.queueUnicast( request, owner );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
