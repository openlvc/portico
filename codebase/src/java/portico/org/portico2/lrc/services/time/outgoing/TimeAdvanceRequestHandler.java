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
import org.portico.lrc.compat.JFederationTimeAlreadyPassed;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.common.services.time.msg.TimeAdvanceRequest;
import org.portico2.lrc.LRCMessageHandler;

public class TimeAdvanceRequestHandler extends LRCMessageHandler
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

		TimeAdvanceRequest request = context.getRequest( TimeAdvanceRequest.class, this );
		double time = request.getTime();
		TimeStatus ourStatus = timeStatus();

		if( logger.isDebugEnabled() )
			logger.debug( "REQUEST Time advance request for ["+moniker()+"] to ["+time+"]" );

		// check that the time is valid
		if( time <= ourStatus.getCurrentTime() )
		{
			// requested time is less than current time, exception
			throw new JFederationTimeAlreadyPassed( "Time " + time + " has already passed" );
		}

		// set the status
		ourStatus.timeAdvanceRequested( time );

		// notify everyone else
		if( logger.isInfoEnabled() )
		{
			logger.info( "PENDING Requested time advance for ["+moniker()+"] to ["+time+
			             "], waiting for grant..." );
		}                             

		// send to the RTI
		connection.sendControlRequest( context );

		// did the RTI send us back an error?
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();

		// nothing to do for now... just wait
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
