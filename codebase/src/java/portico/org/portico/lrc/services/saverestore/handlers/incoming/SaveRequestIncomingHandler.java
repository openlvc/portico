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
import org.portico.lrc.services.saverestore.msg.SaveRequest;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
                messages=SaveRequest.class)
public class SaveRequestIncomingHandler extends LRCMessageHandler
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
		SaveRequest request = context.getRequest( SaveRequest.class, this );
		String label = request.getLabel();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Request to start a federation SAVE with label ["+label+
			              "] by federate ["+moniker(request.getSourceFederate())+"]" );
		}
		
		// store the active save, if there is already an active save, log a big error message
		// a successful registration of this will mark the LRC as being in the progress of a
		// save, blocking any calls through to the RTIambassador.
		try
		{
			saveManager.requestSave( request.getSourceFederate(), label );
		}
		catch( Exception e )
		{
			logger.warn( "Couldn't register federation save request", e );
		}
		
		// let processing fall through to a callback handler
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
