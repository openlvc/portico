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
package org.portico.lrc.services.time.handlers.incoming;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.time.msg.ModifyLookahead;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
                messages=ModifyLookahead.class)
public class ModifyLookaheadIncomingHandler extends LRCMessageHandler
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
		ModifyLookahead request = context.getRequest( ModifyLookahead.class, this );
		int federate = request.getSourceFederate();
		double newLookahead = request.getLookahead();

		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Federate ["+moniker(federate)+
			              "] MODIFIED lookahead to ["+newLookahead+"]" );
		}

		// Only process this message if it ISN'T for us. If it is for us, all the work will
		// have already been done in the outgoing handler. We're just updating local stores
		// here with the new information
		vetoIfMessageFromUs( request );
		
		// update the local status and do any pending advanced notifications
		timeManager.setLookahead( federate, newLookahead );
		// check to see if anyone needs an advance in light of the lookahead change
		queueDummyAdvance();
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
