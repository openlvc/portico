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
package org.portico2.lrc.services.object.outgoing;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JFederateOwnsAttributes;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.LocalDelete;
import org.portico2.lrc.LRCMessageHandler;
import org.portico2.lrc.services.object.data.LOCInstance;

public class LocalDeleteObjectHandler extends LRCMessageHandler
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
		LocalDelete request = context.getRequest( LocalDelete.class, this );
		int objectHandle = request.getObjectHandle();

		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();
		
		if( logger.isDebugEnabled() )
			logger.debug( "ATTEMPT *Local* Delete object ["+objectMoniker(objectHandle)+"]" );

		// check that the object exists and that we own it
		LOCInstance instance = repository.getObject( objectHandle );
		if( instance == null )
			throw new JObjectNotKnown( "Can't delete object ["+objectHandle+"]: unknown" );
		
		// do we own attributes for the object?
		if( instance.ownsAttributes(lrcState.getFederateHandle()) )
		{
			throw new JFederateOwnsAttributes( "Can't local delete object ["+objectHandle+
			                                   "], attributes owned" );
		}

		// tell the repository that we no longer know about this object
		repository.undiscoverInstance( instance );
		
		context.success();
		if( logger.isInfoEnabled() )
			logger.info( "SUCCESS *Locally* Deleted object ["+objectMoniker(objectHandle)+"]" );
		
		// do a rediscovery check
//		if( logger.isDebugEnabled() )
//			logger.debug( "Queuing a false discovery notification to trigger rediscovery test" );
//		DiscoverObject discover = fill( new DiscoverObject(instance) );
//		discover.setRediscoveryCheck( true );
//		lrcQueue.offer( discover );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
