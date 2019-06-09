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
package org.portico.impl.hla13.handlers;

import java.util.Map;

import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.sync.msg.RegisterSyncPoint;

/**
 * This handler generates HLA 1.3 callbacks for synchronization point announcements.
 */
@MessageHandler(modules="lrc13-callback",
                keywords= {"lrc13","lrcjava1"},
                sinks="incoming",
                priority=3,
                messages=RegisterSyncPoint.class)
public class SyncAnnounceCallbackHandler extends HLA13CallbackHandler
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
		if( isStandard() )
			hla13().announceSynchronizationPoint( request.getLabel(), request.getTag() );
		else
			java1().announceSynchronizationPoint( request.getLabel(), new String(request.getTag()) );

		context.success();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
