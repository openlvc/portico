/*
 *   Copyright 2012 The Portico Project
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
package org.portico.impl.hla1516e.handlers;

import java.util.Map;

import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.sync.msg.RegisterSyncPoint;

/**
 * This handler generates IEEE1516e callbacks for synchronization point announcements.
 */
@MessageHandler(modules="lrc1516e-callback",
                keywords="lrc1516e",
                sinks="incoming",
                priority=3,
                messages=RegisterSyncPoint.class)
public class SyncAnnounceCallbackHandler extends HLA1516eCallbackHandler
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
		RegisterSyncPoint request = context.getRequest( RegisterSyncPoint.class, this );
		
		// make sure we are in the announcement group
		if( request.getFederateSet() != null &&
			request.getFederateSet().isEmpty() == false &&
			request.getFederateSet().contains(lrcState.getFederateHandle()) == false )
		{
			// we're not in the set, ignore the request
			context.success();
			return;
		}
		
		logger.trace( "CALLBACK announceSynchronizationPoint(label="+request.getLabel()+")" );
		fedamb().announceSynchronizationPoint( request.getLabel(), request.getTag() );

		context.success();
		
		logger.trace( "         announceSynchronizationPoint() callback complete" );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
