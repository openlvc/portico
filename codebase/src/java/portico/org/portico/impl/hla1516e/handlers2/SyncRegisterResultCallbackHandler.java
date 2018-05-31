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
package org.portico.impl.hla1516e.handlers2;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.sync.msg.RegisterSyncPointResult;

import hla.rti1516e.SynchronizationPointFailureReason;
import hla.rti1516e.exceptions.FederateInternalError;

public class SyncRegisterResultCallbackHandler extends LRC1516eCallbackHandler
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
	public void callback( MessageContext context ) throws FederateInternalError
	{
		RegisterSyncPointResult request = context.getRequest( RegisterSyncPointResult.class, this );
		String label = request.getLabel();
		if( request.wasSuccess() )
		{
			logger.trace( "CALLBACK synchronizationPointRegistrationSucceeded(label="+label+")" );
			fedamb().synchronizationPointRegistrationSucceeded( label );
			helper.reportServiceInvocation( "synchronizationPointRegistrationSucceeded", 
			                                true, 
			                                null, 
			                                label );
			logger.trace( "         synchronizationPointRegistrationSucceeded() callback complete" );
			
			// deliver the announcement for the local federate as well
			// REMOVED - We used to do this in the decentralized version because certain
			//           messages were not looped back around. No need for this now.
			//logger.trace( "CALLBACK announceSynchronizationPoint(label="+label+")" );
			//fedamb().announceSynchronizationPoint( label, request.getTag() );
			//logger.trace( "         announceSynchronizationPoint() callback complete" );
		}
		else
		{
			logger.trace( "CALLBACK synchronizationPointRegistrationFailed(label="+label+")" );
			fedamb().synchronizationPointRegistrationFailed( label,
			         SynchronizationPointFailureReason.SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE );
			helper.reportServiceInvocation( "synchronizationPointRegistrationFailed", 
			                                true, 
			                                null, 
			                                label,
			                                SynchronizationPointFailureReason.SYNCHRONIZATION_POINT_LABEL_NOT_UNIQUE );
			logger.trace( "         synchronizationPointRegistrationFailed() callback complete" );
		}
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
