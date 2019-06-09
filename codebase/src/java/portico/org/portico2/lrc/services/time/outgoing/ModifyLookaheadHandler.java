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
import org.portico.lrc.compat.JInvalidLookahead;
import org.portico.lrc.compat.JTimeRegulationWasNotEnabled;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.common.services.time.msg.ModifyLookahead;
import org.portico2.lrc.LRCMessageHandler;

public class ModifyLookaheadHandler extends LRCMessageHandler
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
		lrcState.checkSave();            // SaveInProgress
		lrcState.checkRestore();         // RestoreInProgress

		ModifyLookahead request = context.getRequest( ModifyLookahead.class, this );
		double newLookahead = request.getLookahead();
		TimeStatus status = timeStatus();
		double oldLookahead = status.getLookahead();
		double proposedLbts = status.getRequestedTime() + newLookahead;
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "REQUEST Modify lookahead for [%s] to [%f] from [%f]",
			              moniker(), newLookahead, oldLookahead );
		}
		
		// make sure we are actually regulating
		if( timeStatus().isRegulating() == false )
			throw new JTimeRegulationWasNotEnabled( "Tried to modify lookahead when not regulating" );
		
		// validate the lookahead value
		if( newLookahead < 0 )
			throw new JInvalidLookahead( "Can't modify lookahead to be negative: " + newLookahead );
		
		/////////////////////
		// lookahead check //
		/////////////////////
		// I hate 0-lookaheads, and seeing as I'm the supreme overlord of Earth,
		// I hath declared them banned! However, it is known to me that some of
		// my subjects are not so loyal, and in an effort to head off any insubordinate
		// activity, I'll just add a tiny value to all lookaheads that are 0
		if( newLookahead == 0 )
		{
			logger.debug( "Zero-lookahead detected: Adding 10^-9 to lookahead" );
			logger.debug( "Insubordinate activity will not be permitted!" );
			newLookahead = Math.pow(10.0,-9.0);
			request.setLookahead( newLookahead );
		}
		
		// Send it off to the RTI for processings
		connection.sendControlRequest( context );
		
		// Was there a problem?
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();
		
		// Set our lookahead if all was OK
		double givenLookahead = context.getSuccessResultAsDouble();
		timeStatus().setLookahead( givenLookahead );
		logger.debug( "SUCCESS Modified lookahead for [%s] to [%d] from [%d]",
		              moniker(), givenLookahead, oldLookahead );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
