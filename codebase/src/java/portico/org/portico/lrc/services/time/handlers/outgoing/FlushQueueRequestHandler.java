/*
 *   Copyright 2009 The Portico Project
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
package org.portico.lrc.services.time.handlers.outgoing;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.time.msg.FlushQueueRequest;
import org.portico2.common.services.time.msg.TimeAdvanceGrant;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=FlushQueueRequest.class)
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
	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
	}
	
	public void process( MessageContext context ) throws Exception
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
		
		// broadcast out a TAG for us (which we will also get)
		TimeAdvanceGrant tag = new TimeAdvanceGrant( grantTime );
		fill( tag, federateHandle() );
		connection.broadcast( tag );
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
