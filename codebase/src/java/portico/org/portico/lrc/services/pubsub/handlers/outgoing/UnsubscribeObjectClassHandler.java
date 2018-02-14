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
package org.portico.lrc.services.pubsub.handlers.outgoing;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.pubsub.msg.UnsubscribeObjectClass;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=UnsubscribeObjectClass.class)
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
	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
	}

	@Override
	public void process( MessageContext context ) throws Exception
	{
		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();

		UnsubscribeObjectClass request = context.getRequest( UnsubscribeObjectClass.class, this );
		int classHandle = request.getClassHandle();
		int regionToken = request.getRegionToken();

		if( logger.isDebugEnabled() )
		{
			String message = "ATTEMPT Unsubscribe object class ["+ocMoniker(classHandle)+"]";
			if( request.usesDdm() )
				message += " (region: "+regionToken+")";
			logger.debug( message );
		}
		
		// store the interest information -- regionToken is NULL_HANDLE for non-ddm requests
		interests.unsubscribeObjectClass( request.getSourceFederate(), classHandle, regionToken );
		// forward the information to the rest of the federation in case they want it
		connection.broadcast( request );
		context.success();

		if( logger.isInfoEnabled() )
		{
			String message = "SUCCESS Unsubscribe object class ["+ocMoniker(classHandle)+ "]";
			if( request.usesDdm() )
				message += " (region: "+regionToken+")";
			logger.info( message );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
