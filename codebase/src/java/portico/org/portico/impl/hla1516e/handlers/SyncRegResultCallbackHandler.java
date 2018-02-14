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
import org.portico2.common.services.sync.msg.RegisterSyncPointResult;

import hla.rti1516e.SynchronizationPointFailureReason;

/**
 * This handler generates IEEE1516e callbacks for synchronization registration results. If the point
 * registration was successful, it will also announce the point to the local federate AFTER the
 * sync successful callback has been delivered.
 */
@MessageHandler(modules="lrc1516e-callback",
                keywords="lrc1516e",
                sinks="incoming",
                messages=RegisterSyncPointResult.class)
public class SyncRegResultCallbackHandler extends HLA1516eCallbackHandler
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
			fedamb().synchronizationPointRegistrationSucceeded( label );
			logger.trace( "         synchronizationPointRegistrationSucceeded() callback complete" );
			
			// deliver the announcement for the local federate as well
			logger.trace( "CALLBACK announceSynchronizationPoint(label="+label+")" );
			fedamb().announceSynchronizationPoint( label, request.getTag() );
			logger.trace( "         announceSynchronizationPoint() callback complete" );
		}
		else
		{
			logger.trace( "CALLBACK synchronizationPointRegistrationFailed(label="+label+")" );
			fedamb().synchronizationPointRegistrationFailed( label,
			         SynchronizationPointFailureReason.SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE );
			logger.trace( "         synchronizationPointRegistrationFailed() callback complete" );
		}
		
		context.success();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
