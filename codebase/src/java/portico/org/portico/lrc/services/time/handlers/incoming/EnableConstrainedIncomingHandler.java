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
import org.portico2.common.services.time.msg.EnableTimeConstrained;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
                messages=EnableTimeConstrained.class)
public class EnableConstrainedIncomingHandler extends LRCMessageHandler
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
		EnableTimeConstrained request = context.getRequest( EnableTimeConstrained.class, this );
		int federate = request.getSourceFederate();

		logger.debug( "@REMOTE Federate ["+moniker(federate)+"] requests ENABLE time constrained" );
		
		/////////////////////////////////////
		// determine new time for federate //
		/////////////////////////////////////
		// we used to bump the federate time up to the LBTS of the federation if it were to low,
		// however, in order to maintain a link to DMSO, we now just do what it does, which is 
		// give the federate its current time as the new time.
		timeManager.enableConstrained( federate );
		logger.info( "ENABLED time constrained for ["+moniker(federate)+"]" );

		// OLD WAY:
		// FEDERATE TIME >  FEDERATION LBTS: Use federate time
		// FEDERATE TIME <= FEDERATION LBTS: Use federation lbts

		// let a callback flow through to the callback handler if this is the federate that
		// requested to have time constrained enabled, otherwise just kill processing now
		// that we have recorded the notice
		if( federate != federateHandle() )
			veto();
		else
			context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
