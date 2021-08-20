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

import java.util.Map;
import java.util.Set;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JObjectClassNotDefined;
import org.portico.lrc.compat.JObjectClassNotPublished;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.OCMetadata;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.DiscoverObject;
import org.portico2.common.services.object.msg.RegisterObject;
import org.portico2.rti.services.RTIMessageHandler;
import org.portico2.rti.services.object.data.ROCInstance;

public class RegisterObjectHandler extends RTIMessageHandler
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
		RegisterObject request = context.getRequest( RegisterObject.class, this );
		int federateHandle = request.getSourceFederate();
		int classHandle = request.getClassHandle();
		String objectName = request.getObjectName();

		if( logger.isDebugEnabled() )
		{
			logger.debug( "ATTEMPT Federate [%s] creating object of class [%s] with name [%s]",
			              federateName(federateHandle),
			              icMoniker(classHandle),
			              objectName );
		}

		//
		// Step 1. Perform Checks
		//
		// Check to make sure that the federate publishes the class
		OCMetadata objectClass = checkPublished( federateHandle, classHandle );
		
		// Get the set of attributes this federate publishes
		Set<Integer> published = interests.getPublishedAttributes( federateHandle, classHandle );
		
		//
		// Step 2. Create the object and return key information
		//
		// Create and store a new object instance
		ROCInstance newInstance = repository.createObject( objectClass,
		                                                   objectName,
		                                                   federateHandle,
		                                                   published ); // published attributes
		repository.addObject( newInstance );
		int instanceHandle = newInstance.getHandle();
		
		// Log registrant metrics
		momManager.objectRegistered( federateHandle, newInstance );

		// TODO Region Support

		// Stuff the object handle into the context so we can return it
		context.success( RegisterObject.KEY_RETURN_HANDLE, instanceHandle );
		context.success( RegisterObject.KEY_RETURN_NAME, newInstance.getName() );
		context.success( RegisterObject.KEY_RETURN_CLASS, classHandle );
		// FIXME Could we stuff the hash of the class hierarchy in here to avoid any
		//       issues from FOM misalignment following late joiners?
		if( logger.isInfoEnabled() )
		{
			logger.debug( "SUCCESS Federate [%s] created object [%d] of class [%s] with name [%s]",
			              federateName(federateHandle),
			              newInstance.getHandle(),
			              icMoniker(classHandle),
			              objectName );
		}

		//
		// Step 3. Notify federates with a subscription interest
		//
		Map<Integer,OCMetadata> subscriptions = interests.getAllSubscribersWithTypes( objectClass );
		for( Integer subscriberHandle : subscriptions.keySet() )
		{
			OCMetadata discoveredAs = subscriptions.get( subscriberHandle );
			newInstance.discover( subscriberHandle, discoveredAs );
			
			// Log discovery metrics
			if( federateHandle != subscriberHandle)
				momManager.objectDiscovered( subscriberHandle, newInstance );
		}

		DiscoverObject discover = fill( new DiscoverObject(newInstance), federateHandle );
		subscriptions.remove( federateHandle ); // don't need to notify the one who created it

		if ( !subscriptions.isEmpty() )
			super.queueManycast( discover, subscriptions.keySet() );
	}

	/**
	 * This method will try to find the {@link OCMetadata} for the object class with the given
	 * handle, validate that this federate is publishing that class and then return the metadata.
	 */
	private OCMetadata checkPublished( int federateHandle, int classHandle )
		throws JObjectClassNotDefined,
		       JObjectClassNotPublished,
		       JRTIinternalError
	{
		// validate that the class exists in the FOM
		OCMetadata oMetadata = federation.getFOM().getObjectClass( classHandle );
		if( oMetadata == null )
		{
			// there is no such object class, ObjectClassNotDefined
			throw new JObjectClassNotDefined( "class ["+classHandle+"] not in FOM" );
		}

		// validate that we are a publisher of this class
		if( interests.isObjectClassPublished(federateHandle,classHandle) == false )
		{
			// we are not a publisher
			throw new JObjectClassNotPublished( "class ["+classHandle+"] not published by [" +
			                                    moniker(federateHandle)+"]" );
		}

		return oMetadata;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
