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
package org.portico2.lrc.services.pubsub.outgoing;

import java.util.Map;
import java.util.Set;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.pubsub.msg.UnsubscribeObjectClass;
import org.portico2.lrc.LRCMessageHandler;

public class UnsubscribeObjectClassHandler extends LRCMessageHandler
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
		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();

		UnsubscribeObjectClass request = context.getRequest( UnsubscribeObjectClass.class, this );
		int classHandle = request.getClassHandle();
		Set<Integer> attributes = request.getAttributes();
		int regionToken = request.getRegionToken();

		if( logger.isDebugEnabled() )
		{
			logger.debug( "ATTEMPT Unsubscribe from [%s] with attributes %s %s",
			              ocMoniker(classHandle),
			              acMoniker(attributes),
			              request.usesDdm() ? "(region: "+regionToken+")" : "" );
		}

		// All checks done on the RTI side - send onward to it
		connection.sendControlRequest( context );
		
		if( context.isSuccessResponse() )
		{
			interests.unsubscribeObjectClass( federateHandle(), classHandle, attributes );
			logger.info( "SUCCESS Unsubscribe from [%s] with attributes %s %s",
			             ocMoniker(classHandle),
			             acMoniker(attributes),
			             request.usesDdm() ? "(region: "+regionToken+")" : "" );
		}
		else
		{
			throw context.getErrorResponseException();
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
