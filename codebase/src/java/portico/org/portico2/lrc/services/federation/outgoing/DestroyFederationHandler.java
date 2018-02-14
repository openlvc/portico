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
package org.portico2.lrc.services.federation.outgoing;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JFederationExecutionDoesNotExist;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.federation.msg.DestroyFederation;
import org.portico2.lrc.LRCMessageHandler;

public class DestroyFederationHandler extends LRCMessageHandler
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
		DestroyFederation request = context.getRequest( DestroyFederation.class, this );
		if( request.getFederationName() == null )
			throw new JFederationExecutionDoesNotExist( "Can't use null for federation name" );

		// log the request and pass it on to the connection
		logger.debug( "ATTEMPT Destroy federation execution [" +request.getFederationName()+ "]" );
		connection.sendControlRequest( context );
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();
		logger.info( "SUCCESS Destroyed federation execution [" +request.getFederationName()+ "]" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
