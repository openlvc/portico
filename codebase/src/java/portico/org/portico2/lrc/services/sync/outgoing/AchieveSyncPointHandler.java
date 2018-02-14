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
package org.portico2.lrc.services.sync.outgoing;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.sync.msg.SyncPointAchieved;
import org.portico2.lrc.LRCMessageHandler;

public class AchieveSyncPointHandler extends LRCMessageHandler
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
		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();

		SyncPointAchieved achieved = context.getRequest( SyncPointAchieved.class, this );
		String label = achieved.getLabel();

		// Check to make sure that the sync point has been announced
		// TODO Should we even track this here, or just forward to the RTI?
		
		if( logger.isDebugEnabled() )
			logger.debug( "ATTEMPT Federate [%s] achieving sync point [%s]", moniker(), label );

		// Pass to the RTI
		connection.sendControlRequest( context );
		
		// Check the result
		if( context.isErrorResponse() )
		{
			JException exception = context.getErrorResponseException();
			logger.error( "FAILURE Federate["+moniker()+"] failed achieving sync point ["+label+"]: "+
			              exception.getMessage(), exception );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
