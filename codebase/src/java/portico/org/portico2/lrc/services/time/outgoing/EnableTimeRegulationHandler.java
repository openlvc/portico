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
import org.portico.lrc.compat.JInvalidFederationTime;
import org.portico.lrc.compat.JInvalidLookahead;
import org.portico.lrc.compat.JTimeRegulationAlreadyEnabled;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.common.services.time.msg.EnableTimeRegulation;
import org.portico2.lrc.LRCMessageHandler;

public class EnableTimeRegulationHandler extends LRCMessageHandler
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
		lrcState.checkJoined();         // FederateNotExecutionMember
		lrcState.checkAdvancing();      // TimeAdvanceAlreadyInProgress
		lrcState.checkTimeRegulation(); // EnableTimeConstrainedPending
		lrcState.checkSave();           // SaveInProgress
		lrcState.checkRestore();        // RestoreInProgress

		EnableTimeRegulation request = context.getRequest( EnableTimeRegulation.class, this );
		double federateTime = request.getFederateTime();
		double lookahead = request.getLookahead();

		if( logger.isDebugEnabled() )
		{
			logger.debug( "REQUEST Enable time regulation for ["+moniker()+"]: fedtime="+
			              federateTime+", lookahead="+lookahead );
		}
		
		// check the parameters to validate them
		if( timeStatus().isRegulating() )
			throw new JTimeRegulationAlreadyEnabled();
		if( federateTime < 0 )
			throw new JInvalidFederationTime( "time: " + federateTime );
		if( lookahead < 0 )
			throw new JInvalidLookahead( "lookahead: " + lookahead );
		
		/////////////////////
		// lookahead check //
		/////////////////////
		// I hate 0-lookaheads, and seeing as I'm the supreme overlord of Earth,
		// I hath declared them banned! However, it is known to me that some of
		// my subjects are not so loyal, and in an effort to head off any insubordinate
		// activity, I'll just add a tiny value to all lookaheads that are 0
		if( lookahead == 0 )
		{
			logger.debug( "Zero-lookahead detected: Adding 10^-9 to lookahead" );
			logger.debug( "Insubordinate activity will not be permitted!" );
			lookahead = Math.pow(10.0,-9.0);
			request.setLookahead( lookahead );
		}

		// Update our state and wait for the callback to be processed
		timeStatus().setRegulating( TimeStatus.TriState.PENDING );
		timeStatus().setLookahead( lookahead );
		if( logger.isInfoEnabled() )
			logger.info( "PENDING Enable time regulation PENDING for ["+moniker()+"]" );

		// Pass the request on to the RTI
		connection.sendControlRequest( context );

		// If there was an error, toss that out
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();
		
		// Record the time status change
		double newTime = context.getSuccessResultAsDouble();
		timeStatus().setRegulating( TimeStatus.TriState.ON );
		timeStatus().setCurrentTime( newTime );
		
		// Queue the required callback
		request.setFederateTime( newTime );
		super.lrcQueue.offer( request );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
