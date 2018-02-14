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
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.time.msg.NextEventRequest;
import org.portico2.common.services.time.msg.TimeAdvanceRequest;
import org.portico2.lrc.LRCMessageHandler;

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
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		super.configure( properties );
	}

	@Override
	public void process( MessageContext context ) throws JException
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
