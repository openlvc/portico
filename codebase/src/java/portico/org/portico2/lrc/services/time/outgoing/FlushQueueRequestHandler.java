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
package org.portico2.lrc.services.time.outgoing;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.time.msg.FlushQueueRequest;
import org.portico2.common.services.time.msg.TimeAdvanceRequest;
import org.portico2.lrc.LRCMessageHandler;

public class FlushQueueRequestHandler extends LRCMessageHandler
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
		FlushQueueRequest request = context.getRequest( FlushQueueRequest.class, this );
		double maxTime = request.getTime();
		
		// basic state validity checks
		lrcState.checkJoined();               // FederateNotExecutionMemeber
		lrcState.checkAdvancing();            // TimeAdvanceAlreadyInProgress
		lrcState.checkTimeRegulation();       // EnableTimeRegulationPending
		lrcState.checkTimeConstrained();      // EnableTimeConstrainedPending
		lrcState.checkSave();                 // SaveInProgress
		lrcState.checkRestore();              // RestoreInProgress
		lrcState.checkTimeNotInPast(maxTime); // FederationTimeAlreadyPassed
		
		if( logger.isDebugEnabled() )
			logger.debug( "REQUEST Flush queue, time threshold="+maxTime );

		// flush the queue
		double grantTime = lrc.tickFlush( maxTime );
		
		// send in a TAR for us to the time the queue is now flushed from
		TimeAdvanceRequest tar = fill(new TimeAdvanceRequest(grantTime)); // FIXME Need to jump, not wait
		MessageContext tarContext = new MessageContext( tar );
		lrc.getOutgoingSink().process( tarContext );

		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
