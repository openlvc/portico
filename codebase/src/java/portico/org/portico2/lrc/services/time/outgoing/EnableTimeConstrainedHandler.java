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
import org.portico.lrc.compat.JTimeConstrainedAlreadyEnabled;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.common.services.time.msg.EnableTimeConstrained;
import org.portico2.lrc.LRCMessageHandler;

public class EnableTimeConstrainedHandler extends LRCMessageHandler
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
		lrcState.checkJoined();          // FederateNotExecutionMember
		lrcState.checkAdvancing();       // TimeAdvanceAlreadyInProgress
		lrcState.checkTimeConstrained(); // EnableTimeConstrainedPending
		lrcState.checkSave();            // SaveInProgress
		lrcState.checkRestore();         // RestoreInProgress

		EnableTimeConstrained request = context.getRequest( EnableTimeConstrained.class, this );

		if( logger.isDebugEnabled() )
			logger.debug( "REQUEST Enable time constrained for ["+moniker()+"]" );
		
		if( timeStatus().isConstrained() )
			throw new JTimeConstrainedAlreadyEnabled();
		
		// Update our state and wait for the callback to be processed.
		// All calculations will be handled by the incoming handler
		// where callbacks will be generated.
		timeStatus().setConstrained( TimeStatus.TriState.PENDING );
		if( logger.isDebugEnabled() )
			logger.debug( "PENDING Enable time constrained PENDING for ["+moniker()+"]" );

		// Pass on to the RTI
		connection.sendControlRequest( context );
		
		// If there was an error, toss that out
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();
		
		// Record the status change
		timeStatus().setConstrained( TimeStatus.TriState.ON );
		
		// Queue a callback for us
		lrcState.getQueue().offer( request );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
