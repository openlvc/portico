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
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.common.services.time.msg.TimeAdvanceGrant;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
                messages=TimeAdvanceGrant.class)
public class TimeAdvanceGrantedIncomingHandler extends LRCMessageHandler
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
		TimeAdvanceGrant request = context.getRequest( TimeAdvanceGrant.class, this );
		int federate = request.getTargetFederate(); // TARGET!! **NOT SOURCE**
		double newTime = request.getTime();
		
		// get the time status for the federate and record the grant
		TimeStatus status = timeManager.getTimeStatus( federate );
		if( status == null )
		{
			veto( "ADVANCE (GRANTED) for unknown federate (may have resigned): handle="+federate );
		}

		if( logger.isDebugEnabled() )
		{
			logger.debug( "ADVANCE (GRANTED) for federate ["+moniker(federate)+
			              "] to time ["+newTime+"]" );
		}
		
		status.advanceGrantCallbackProcessed( newTime );

		////////////////////////////////////////////////////////////////
		// everything after this point is ONLY FOR THE LOCAL FEDERATE //
		////////////////////////////////////////////////////////////////
		vetoUnlessForUs( request );
		
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
