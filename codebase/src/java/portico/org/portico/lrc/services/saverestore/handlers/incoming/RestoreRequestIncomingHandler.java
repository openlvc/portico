/*
 *   Copyright 2009 The Portico Project
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
package org.portico.lrc.services.saverestore.handlers.incoming;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.services.saverestore.msg.RestoreRequest;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
                messages=RestoreRequest.class)
public class RestoreRequestIncomingHandler extends LRCMessageHandler
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
		RestoreRequest request = context.getRequest( RestoreRequest.class, this );
		vetoIfMessageFromUs( request ); // we've already stored the information
		String label = request.getLabel();
		int sourceFederate = request.getSourceFederate();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Request to start RESTORE with label ["+label+"] by ["+
			              moniker(request.getSourceFederate())+"], need final confirmation" );
		}
		
		// Record locally the fact that someone wants to initiate a restore. If someone else
		// has already tried, the federate with the lowest handle wins and their request
		// replaces the incoming/existing request.
		if( restoreManager.isRestorePending() )
		{
			// check to see if we have a lower handle
			if( sourceFederate < restoreManager.getRegisteringFederate() )
			{
				String currentLabel = restoreManager.getActiveLabel();
				int currentFederate = restoreManager.getRegisteringFederate();

				// LOWER HANDLE! REPLACE!
				restoreManager.requestRestore( sourceFederate, label );
				// log the events - at info because this is a replace
				logger.info( "Received restore request from older federate. Request [label="+
				             label+",federate="+moniker(sourceFederate)+"] *replacing* [label="+
				             currentLabel+",federate="+moniker(currentFederate)+"]" );
			}
			else
			{
				logger.debug( "Received restore request from younger federate. Request [label="+
				              label+",federate="+moniker(sourceFederate)+"] remains pending" );
			}
		}
		else
		{
			// store the information
			restoreManager.requestRestore( sourceFederate, label );
			logger.debug( "Stored pending request for federation restore [label="+label+
			              ",federate="+moniker(sourceFederate)+"]" );
		}

		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
