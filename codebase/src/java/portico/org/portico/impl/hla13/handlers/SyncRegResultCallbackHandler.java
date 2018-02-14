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
import org.portico2.common.services.sync.msg.RegisterSyncPointResult;

/**
 * This handler generates HLA 1.3 callbacks for synchronization registration results. If the point
 * registration was successful, it will also announce the point to the local federate AFTER the
 * sync successful callback has been delivered.
 */
@MessageHandler(modules="lrc13-callback",
                keywords= {"lrc13","lrcjava1"},
                sinks="incoming",
                messages=RegisterSyncPointResult.class)
public class SyncRegResultCallbackHandler extends HLA13CallbackHandler
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
		RegisterSyncPointResult request = context.getRequest( RegisterSyncPointResult.class, this );
		String label = request.getLabel();
		if( request.wasSuccess() )
		{
			logger.trace( "CALLBACK synchronizationPointRegistrationSucceeded(label="+label+")" );
			if( isStandard() )
				hla13().synchronizationPointRegistrationSucceeded( label );
			else
				java1().synchronizationPointRegistrationSucceeded( label );
			
			// announce the point as well
			logger.trace( "CALLBACK announceSynchronizationPoint(label="+label+")" );
			if( isStandard() )
				hla13().announceSynchronizationPoint( label, request.getTag() );
			else
				java1().announceSynchronizationPoint( label, new String(request.getTag()) );

		}
		else
		{
			if( logger.isTraceEnabled() )
			{
				logger.trace( "CALLBACK synchronizationPointRegistrationFailed(label="+label+
				              "): reason="+request.getFailureReason() );
			}

			if( isStandard() )
				hla13().synchronizationPointRegistrationFailed( label );
			else
				java1().synchronizationPointRegistrationFailed( label );
		}
		
		context.success();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
