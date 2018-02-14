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
package org.portico.lrc.services.time.handlers.outgoing;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.services.time.msg.NextEventRequest;
import org.portico2.common.services.time.msg.TimeAdvanceRequest;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=NextEventRequest.class)
public class NextEventRequestHandler extends LRCMessageHandler
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
		// basic state validity checks
		lrcState.checkJoined();          // FederateNotExecutionMemeber
		lrcState.checkAdvancing();       // TimeAdvanceAlreadyInProgress
		lrcState.checkTimeRegulation();  // EnableTimeRegulationPending
		lrcState.checkTimeConstrained(); // EnableTimeConstrainedPending
		lrcState.checkSave();            // SaveInProgress
		lrcState.checkRestore();         // RestoreInProgress

		NextEventRequest request = context.getRequest( NextEventRequest.class, this );
		double time = request.getTime();

		if( logger.isDebugEnabled() )
			logger.debug( "REQUEST Next event request for ["+moniker()+"]: time="+time );

		// find out the timestamp of the next TSO event and if it is smaller than the requested
		// time, reset the requested time to be the time of that event
		PorticoMessage next = lrcState.getQueue().peekTSO();
		if( next != null && next.getTimestamp() < time )
			time = next.getTimestamp();

		// create a time advance request and reprocess it as that
		// TODO this *is* wrong, but its what v0.8 does so we'll fix it up after porting is done
		if( request.isNera() )
			context.setRequest( fill(new TimeAdvanceRequest(time,true)) );
		else
			context.setRequest( fill(new TimeAdvanceRequest(time)) );
		
		lrc.getOutgoingSink().process( context );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
