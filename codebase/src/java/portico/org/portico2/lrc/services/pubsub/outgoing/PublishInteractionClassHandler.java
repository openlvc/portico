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

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.pubsub.msg.PublishInteractionClass;
import org.portico2.lrc.LRCMessageHandler;

public class PublishInteractionClassHandler extends LRCMessageHandler
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

		PublishInteractionClass request = context.getRequest( PublishInteractionClass.class, this );
		int classHandle = request.getClassHandle();

		if( logger.isDebugEnabled() )
			logger.debug( "ATTEMPT Publish interaction class [" +icMoniker(classHandle)+ "]" );
		
		// just send it on and see what the RTI says
		connection.sendControlRequest( context );
		
		if( context.isSuccessResponse() )
		{
			// record the publication
			interests.publishInteractionClass( federateHandle(), classHandle );
			
			if( logger.isInfoEnabled() )
				logger.info( "SUCCESS Publish interaction class [" +icMoniker(classHandle)+ "]" );
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
