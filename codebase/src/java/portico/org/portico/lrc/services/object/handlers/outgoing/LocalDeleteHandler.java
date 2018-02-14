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
package org.portico.lrc.services.object.handlers.outgoing;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JFederateOwnsAttributes;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico.lrc.model.OCInstance;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.DiscoverObject;
import org.portico2.common.services.object.msg.LocalDelete;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=LocalDelete.class)
public class LocalDeleteHandler extends LRCMessageHandler
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
		LocalDelete request = context.getRequest( LocalDelete.class, this );
		int objectHandle = request.getObjectHandle();

		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();
		
		if( logger.isDebugEnabled() )
			logger.debug( "ATTEMPT *Local* Delete object ["+objectMoniker(objectHandle)+"]" );

		// check that the object exists and that we own it
		OCInstance instance = repository.getInstance( objectHandle );
		if( instance == null )
			throw new JObjectNotKnown( "can't delete object ["+objectHandle+"]: unknown" );
		
		if( instance.ownsAttributes(lrcState.getFederateHandle()) )
			throw new JFederateOwnsAttributes( "can't local delete object ["+objectHandle+
			                                   "], attributes owned" );

		// tell the repository that we no longer know about this object
		repository.undiscoverInstance( instance );
		
		context.success();
		if( logger.isInfoEnabled() )
			logger.info( "SUCCESS *Locally* Deleted object ["+objectMoniker(objectHandle)+"]" );
		
		// do a rediscovery check
		if( logger.isDebugEnabled() )
			logger.debug( "Issuing a false discovery notification to trigger rediscovery test" );
		DiscoverObject discover = fill( new DiscoverObject(instance) );
		discover.setRediscoveryCheck( true );
		reprocessIncoming( discover );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
