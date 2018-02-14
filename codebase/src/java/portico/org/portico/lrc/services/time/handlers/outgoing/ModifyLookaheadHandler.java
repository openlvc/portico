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
import org.portico.lrc.compat.JInvalidLookahead;
import org.portico.lrc.compat.JTimeRegulationWasNotEnabled;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.common.services.time.msg.ModifyLookahead;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=ModifyLookahead.class)
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
	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
	}
	
	public void process( MessageContext context ) throws Exception
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
			logger.debug( "REQUEST Modify lookahead for ["+moniker()+"] from ["+oldLookahead+
			              "] to ["+newLookahead+"]" );
		}
		
		// make sure we are actually regulating
		if( timeStatus().isRegulating() == false )
			throw new JTimeRegulationWasNotEnabled( "try to modify lookahead when not regulating" );
		
		// validate the lookahead value
		if( newLookahead < 0 )
			throw new JInvalidLookahead( "can't modify lookahead to be negative: " + newLookahead );
		
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
		
		// ensure that the new LBTS for the federate won't be less than the current federation LBTS
		// NOTE: this doesn't matter if there are no constrained federates, only check if there are
		if( timeManager.hasConstrainedFederates() && proposedLbts < timeManager.getLBTS() )
		{
			logger.error("FAILURE ["+moniker()+"] requested lookahead was too low: "+newLookahead);
			throw new JInvalidLookahead( "Requested lookahead of ["+newLookahead+"] was too low" );
		}
		
		// we're good, set the new lookahead and send out notification
		timeManager.setLookahead( lrcState.getFederateHandle(), newLookahead );

		// check to see if anyone needs an advance in light of the lookahead change
		queueDummyAdvance();
		if( logger.isInfoEnabled() )
		{
			logger.info( "PENDING Modified lookahead of ["+moniker()+"] to ["+newLookahead+
			             "], must wait for time advance now" );
		}

		// notify the federation
		connection.broadcast( request );
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
