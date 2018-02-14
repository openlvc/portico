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
import org.portico2.common.services.time.msg.DisableTimeConstrained;
import org.portico2.common.services.time.msg.TimeAdvanceRequest;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
                messages=DisableTimeConstrained.class)
public class DisableConstrainedIncomingHandler extends LRCMessageHandler
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
		DisableTimeConstrained request = context.getRequest( DisableTimeConstrained.class, this );
		int federate = request.getSourceFederate();

		logger.debug( "@REMOTE Federate ["+moniker(federate)+"] DISABLED time constrained" );
		timeManager.disableConstrained( federate );
		logger.info( "DISABLE time constrained for ["+moniker(federate)+"]" );
		
		// can we advance now? by disabling contrained we are no longer limited by
		// what other federates want to do, so let's check to see if we can
		TimeStatus status = timeManager.getTimeStatus( federate );
		if( status.isInAdvancingState() )
		{
			TimeAdvanceRequest tar = new TimeAdvanceRequest( status.getRequestedTime() );
			tar.setSourceFederate( federate );
			lrcState.getQueue().offer( tar );
		}
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
