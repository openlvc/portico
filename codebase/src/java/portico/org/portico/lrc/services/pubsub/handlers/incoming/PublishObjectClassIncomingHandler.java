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
package org.portico.lrc.services.pubsub.handlers.incoming;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.pubsub.msg.PublishObjectClass;

/**
 * Record the object class publication of remote federates. If we are the federate responsible
 * for the publication, the request should be ignored as we'll already have done this before the
 * message was sent out.
 */
@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before any callback handler
                messages=PublishObjectClass.class)
public class PublishObjectClassIncomingHandler extends LRCMessageHandler
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
		PublishObjectClass request = context.getRequest( PublishObjectClass.class, this );
		// ignore if we sent this message
		vetoIfMessageFromUs( request );
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Federate ["+moniker(request)+
			              "] published object class ["+ocMoniker(request.getClassHandle())+
			              "] with attributes "+acMoniker(request.getAttributes()) );
		}
		
		// store the publication information in the interest manager and move on with life
		interests.publishObjectClass( request.getSourceFederate(),
		                              request.getClassHandle(),
		                              request.getAttributes() );
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
