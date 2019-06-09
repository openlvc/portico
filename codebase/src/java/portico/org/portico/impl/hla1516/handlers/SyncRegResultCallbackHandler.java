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
package org.portico.impl.hla1516.handlers;

import java.util.Map;

import org.portico.impl.hla1516.Impl1516Helper;
import org.portico.lrc.LRCMessageHandler;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.sync.msg.RegisterSyncPointResult;

/**
 * This handler generates HLA 1.3 callbacks for synchronization registration results. If the point
 * registration was successful, it will also announce the point to the local federate AFTER the
 * sync successful callback has been delivered.
 */
@MessageHandler(modules="lrc1516-callback",
                keywords="lrc1516",
                sinks="incoming",
                messages=RegisterSyncPointResult.class)
public class SyncRegResultCallbackHandler extends LRCMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Impl1516Helper helper;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
		this.helper = (Impl1516Helper)lrc.getSpecHelper();
	}
	
	public void process( MessageContext context ) throws Exception
	{
		RegisterSyncPointResult request = context.getRequest( RegisterSyncPointResult.class, this );
		String label = request.getLabel();
		if( request.wasSuccess() )
		{
			logger.trace( "CALLBACK synchronizationPointRegistrationSucceeded(label="+label+")" );
			helper.getFederateAmbassador().synchronizationPointRegistrationSucceeded( label );
			
			// deliver the announcement for the local federate as well
			logger.trace( "CALLBACK announceSynchronizationPoint(label="+label+")" );
			helper.getFederateAmbassador().announceSynchronizationPoint( label, request.getTag() );
		}
		else
		{
			logger.trace( "CALLBACK synchronizationPointRegistrationFailed(label="+label+")" );
			helper.getFederateAmbassador().synchronizationPointRegistrationFailed( label, null );
		}
		
		context.success();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
