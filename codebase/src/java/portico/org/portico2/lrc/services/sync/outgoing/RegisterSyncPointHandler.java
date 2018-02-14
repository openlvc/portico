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

import java.util.HashSet;
import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.sync.msg.RegisterSyncPoint;
import org.portico2.common.services.sync.msg.RegisterSyncPointResult;
import org.portico2.lrc.LRCMessageHandler;

public class RegisterSyncPointHandler extends LRCMessageHandler
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
		
		RegisterSyncPoint request = context.getRequest( RegisterSyncPoint.class, this );
		String label = request.getLabel();
		HashSet<Integer> syncset = request.getFederateSet();

		/////////////////////////////////////////////
		// Run the message/request validity checks //
		/////////////////////////////////////////////
		logger.debug( "ATTEMPT Register sync point ["+label+"] by ["+moniker()+"]" );
		
		// check for a null label
		if( label == null || label.trim().equals("") )
			throw new JRTIinternalError( "Can't register sync point with null or empty label" );
		
		// if the sync set exists, but is empty, set it to null so as to indicate that this point
		// is a federation-wide point (empty set == every federate, every federate denoted by null)
		if( syncset != null && syncset.isEmpty() )
		{
			syncset = null;
			request.makeFederationWide();
		}

		/////////////////////////////////
		// Send the request to the RTI //
		/////////////////////////////////
		connection.sendControlRequest( context );
		
		if( context.isErrorResponse() )
		{
			String reason = context.getErrorResponse().getError().getMessage();
			logger.info( "FAILURE Register sync point [%s] failed: reason=%s", label, reason );
			lrcQueue.offer( new RegisterSyncPointResult(false,reason) );
			
			// Mark the call as a success, even though it failed. We'll get the callback
			// signalling the failure, but if we leave this as an error response then the
			// LRC will throw an RTIinternalError, which is incorrect.
			context.success();
		}
		else
		{
			logger.debug( "SUCCESS Register sync point ["+label+"] by ["+moniker()+"]" );
			lrcQueue.offer( new RegisterSyncPointResult(true,label) );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
