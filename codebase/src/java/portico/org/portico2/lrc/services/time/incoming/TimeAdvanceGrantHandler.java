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
package org.portico2.lrc.services.time.incoming;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.time.msg.TimeAdvanceGrant;
import org.portico2.lrc.LRCMessageHandler;

public class TimeAdvanceGrantHandler extends LRCMessageHandler
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
		TimeAdvanceGrant request = context.getRequest( TimeAdvanceGrant.class, this );
		int federate = request.getTargetFederate(); // TARGET!! **NOT SOURCE**
		double newTime = request.getTime();
		
		// make double sure this is for us
		if( federate != federateHandle() )
			veto();
		
		// get our time status and update it
		timeStatus().advanceGrantCallbackProcessed( newTime );
		if( logger.isDebugEnabled() )
			logger.debug( "ADVANCE (GRANTED) for federate ["+moniker()+"] to time ["+newTime+"]" );
		
		// let the call through to the callback handler
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
